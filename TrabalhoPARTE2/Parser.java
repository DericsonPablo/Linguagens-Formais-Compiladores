

public class Parser {
	public static final int _EOF = 0;
	public static final int _id = 1;
	public static final int _num = 2;
	public static final int maxT = 17;

	static final boolean _T = true;
	static final boolean _x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	public void showProduction(String s) {
  	System.out.println("Token lido: "+s);
}

String s = "";



	public Parser(Scanner scanner) {
		this.scanner = scanner;
		errors = new Errors();
	}

	void SynErr (int n) {
		if (errDist >= minErrDist) errors.SynErr(la.line, la.col, n);
		errDist = 0;
	}

	public void SemErr (String msg) {
		if (errDist >= minErrDist) errors.SemErr(t.line, t.col, msg);
		errDist = 0;
	}
	
	void Get () {
		for (;;) {
			t = la;
			la = scanner.Scan();
			if (la.kind <= maxT) {
				++errDist;
				break;
			}

			la = t;
		}
	}
	
	void Expect (int n) {
		if (la.kind==n) Get(); else { SynErr(n); }
	}
	
	boolean StartOf (int s) {
		return set[s][la.kind];
	}
	
	void ExpectWeak (int n, int follow) {
		if (la.kind == n) Get();
		else {
			SynErr(n);
			while (!StartOf(follow)) Get();
		}
	}
	
	boolean WeakSeparator (int n, int syFol, int repFol) {
		int kind = la.kind;
		if (kind == n) { Get(); return true; }
		else if (StartOf(repFol)) return false;
		else {
			SynErr(n);
			while (!(set[syFol][kind] || set[repFol][kind] || set[0][kind])) {
				Get();
				kind = la.kind;
			}
			return StartOf(syFol);
		}
	}
	
	void CLC() {
		Expect(3);
		showProduction(t.val);
		L();
		Expect(4);
		showProduction(t.val);
	}

	void L() {
		if (la.kind == 1 || la.kind == 11 || la.kind == 12) {
			C();
			Expect(5);
			L();
		} else if (la.kind == 6) {
			Get();
			Expect(7);
			Expect(8);
			Expect(5);
		} else if (la.kind == 9) {
			Get();
			showProduction(t.val);
			Expect(7);
			Expect(8);
			Expect(5);
		} else SynErr(18);
	}

	void C() {
		if (la.kind == 1) {
			Get();
			Expect(10);
			E();
			showProduction(t.val);
		} else if (la.kind == 11) {
			Get();
			showProduction(t.val);
			Expect(7);
			Expect(1);
			Expect(8);
		} else if (la.kind == 12) {
			Get();
			showProduction(t.val);
			Expect(7);
			Expect(1);
			Expect(8);
		} else SynErr(19);
	}

	void E() {
		T();
		while (la.kind == 13 || la.kind == 14) {
			if (la.kind == 13) {
				Get();
			} else {
				Get();
			}
			T();
		}
		showProduction(t.val);
	}

	void T() {
		F();
		while (la.kind == 15 || la.kind == 16) {
			if (la.kind == 15) {
				Get();
			} else {
				Get();
			}
			F();
		}
		showProduction(t.val);
	}

	void F() {
		if (la.kind == 1) {
			Get();
			showProduction(t.val);
		} else if (la.kind == 2) {
			Get();
			showProduction(t.val);
		} else if (la.kind == 7) {
			Get();
			E();
			Expect(8);
		} else SynErr(20);
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		CLC();
		Expect(0);

	}

	private static final boolean[][] set = {
		{_T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x}

	};
} // end Parser


class Errors {
	public int count = 0;                                    // number of errors detected
	public java.io.PrintStream errorStream = System.out;     // error messages go to this stream
	public String errMsgFormat = "-- line {0} col {1}: {2}"; // 0=line, 1=column, 2=text
	
	protected void printMsg(int line, int column, String msg) {
		StringBuffer b = new StringBuffer(errMsgFormat);
		int pos = b.indexOf("{0}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, line); }
		pos = b.indexOf("{1}");
		if (pos >= 0) { b.delete(pos, pos+3); b.insert(pos, column); }
		pos = b.indexOf("{2}");
		if (pos >= 0) b.replace(pos, pos+3, msg);
		errorStream.println(b.toString());
	}
	
	public void SynErr (int line, int col, int n) {
		String s;
		switch (n) {
			case 0: s = "EOF expected"; break;
			case 1: s = "id expected"; break;
			case 2: s = "num expected"; break;
			case 3: s = "\"begin\" expected"; break;
			case 4: s = "\"end.\" expected"; break;
			case 5: s = "\";\" expected"; break;
			case 6: s = "\"showSymbolTable\" expected"; break;
			case 7: s = "\"(\" expected"; break;
			case 8: s = "\")\" expected"; break;
			case 9: s = "\"showSymbolTree\" expected"; break;
			case 10: s = "\":=\" expected"; break;
			case 11: s = "\"input\" expected"; break;
			case 12: s = "\"print\" expected"; break;
			case 13: s = "\"+\" expected"; break;
			case 14: s = "\"-\" expected"; break;
			case 15: s = "\"*\" expected"; break;
			case 16: s = "\"/\" expected"; break;
			case 17: s = "??? expected"; break;
			case 18: s = "invalid L"; break;
			case 19: s = "invalid C"; break;
			case 20: s = "invalid F"; break;
			default: s = "error " + n; break;
		}
		printMsg(line, col, s);
		count++;
	}

	public void SemErr (int line, int col, String s) {	
		printMsg(line, col, s);
		count++;
	}
	
	public void SemErr (String s) {
		errorStream.println(s);
		count++;
	}
	
	public void Warning (int line, int col, String s) {	
		printMsg(line, col, s);
	}
	
	public void Warning (String s) {
		errorStream.println(s);
	}
} // Errors


class FatalError extends RuntimeException {
	public static final long serialVersionUID = 1L;
	public FatalError(String s) { super(s); }
}
