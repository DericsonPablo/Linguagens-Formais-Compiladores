

import java.util.*;





public class Parser {
	public static final int _EOF = 0;
	public static final int _Identifier = 1;
	public static final int _Number = 2;
	public static final int _true = 3;
	public static final int _false = 4;
	public static final int maxT = 33;

	static final boolean _T = true;
	static final boolean _x = false;
	static final int minErrDist = 2;

	public Token t;    // last recognized token
	public Token la;   // lookahead token
	int errDist = minErrDist;
	
	public Scanner scanner;
	public Errors errors;

	class IntVar {                       // variavel inteira

   public int lBound, uBound, iValue;



   public IntVar(int lb, int ub, int v) {

      lBound = lb;

      uBound = ub;

      iValue = v;

   }



   public String toString() {

     String s = "range:(" + Integer.toString(lBound) + "," + Integer.toString(uBound)+")";

     s = s + " ---> current value :" + Integer.toString(iValue);

     return s;

   }

}



class BoolVar {           // variavel logica

   public boolean bValue;



   public BoolVar(boolean v) {

      bValue = v;

   }



   public String toString() {

     String s = " ---> current value :" + Boolean.toString(bValue);

     return s;

   }

}



//tabela de variaveis logicas

Dictionary lbooleans = new java.util.Hashtable();



//tabela de variaveis inteiras

Dictionary lints = new java.util.Hashtable();



//lista de comandos

Vector lprogram = new Vector();



//tabela de rotulos

Dictionary llabels = new java.util.Hashtable();



public String report() {

        String s = "**********************************\n";

        s = s + "Valor atual do conteudo do programa\n\n";

        s = s +"variaveis logicas\n";

        

        s = s+lbooleans.toString()+"\n";

        

        s = s +"\nvariaveis logicas\n";

        s = s + lints.toString() + "\n";

        

        s = s +"\nlabels\n";

        s = s + llabels.toString() + "\n";

        

        s = s +"\ncodigo intermediario\n";

        java.util.Enumeration e4 = lprogram.elements();

        while (e4.hasMoreElements()) {

            String a = (String) e4.nextElement();

            s = s + a +"\n";

        }

        

        s = s +"FIM";

        

        return s;

}



int statementNo = 0; //contador do programa

java.lang.String cmd ; //comando atual

java.lang.String varName;  //variavel sendo processada

java.lang.String progName; //programa sendo processado













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
	
	void TinyCode() {
		Expect(5);
		Expect(1);
		progName = t.val; 
		Expect(6);
		ProgramBody();
	}

	void ProgramBody() {
		if (la.kind == 7) {
			Get();
			DeclareVar();
			while (la.kind == 1) {
				DeclareVar();
			}
		}
		Expect(8);
		while (StartOf(1)) {
			Statement();
		}
		Expect(9);
		lprogram.add("end"); 
	}

	void DeclareVar() {
		Expect(1);
		varName = t.val; 
		Expect(10);
		DeclareVarBody();
	}

	void Statement() {
		if (la.kind == 1) {
			Get();
			llabels.put(t.val,statementNo); 
			Expect(10);
		}
		StatementBody();
	}

	void DeclareVarBody() {
		if (la.kind == 11) {
			Get();
			Expect(12);
			Expect(2);
			int lbound = (Integer.valueOf(t.val)).intValue(); 
			Expect(13);
			Expect(2);
			int ubound = (Integer.valueOf(t.val)).intValue(); 
			Expect(14);
			Expect(15);
			Expect(2);
			int cvalue = (Integer.valueOf(t.val)).intValue(); 
			Expect(16);
			lints.put(varName,new IntVar(lbound,ubound,cvalue)); 
		} else if (la.kind == 17) {
			Get();
			Expect(15);
			if (la.kind == 3) {
				Get();
			} else if (la.kind == 4) {
				Get();
			} else SynErr(34);
			boolean bvalue = Boolean.parseBoolean(t.val); 
			Expect(16);
		} else SynErr(35);
	}

	void StatementBody() {
		if (la.kind == 19) {
			Assignment();
		} else if (la.kind == 25) {
			Branching();
		} else if (la.kind == 18) {
			Goto();
		} else SynErr(36);
	}

	void Assignment() {
		Expect(19);
		LValue();
		cmd = "asgn " + t.val + " "; 
		Expect(15);
		if (StartOf(2)) {
			RValue();
			cmd = cmd + t.val; 
			if (StartOf(3)) {
				OP();
				cmd = cmd + " " + t.val; 
				RValue();
				cmd = cmd + " " + t.val; 
			}
		} else if (la.kind == 20) {
			Get();
			cmd = cmd + "not"; 
			RValue();
			cmd = cmd + t.val; 
		} else SynErr(37);
		Expect(16);
		lprogram.add(cmd); statementNo++; 
	}

	void Branching() {
		Expect(25);
		cmd = "if "; 
		CompareExpr();
		Expect(18);
		cmd = cmd + " goto "; 
		Expect(1);
		cmd = cmd + t.val; 
		Expect(16);
		if (la.kind == 26) {
			Get();
			Expect(18);
			cmd = cmd + " elsegoto "; 
			Expect(1);
			cmd = cmd + t.val; 
			Expect(16);
		}
		lprogram.add(cmd); statementNo++; 
	}

	void Goto() {
		Expect(18);
		Expect(1);
		lprogram.add("goto " + t.val);
		statementNo++; 
		Expect(16);
	}

	void LValue() {
		Expect(1);
	}

	void RValue() {
		if (la.kind == 1) {
			Get();
		} else if (la.kind == 2) {
			Get();
		} else if (la.kind == 3) {
			Get();
		} else if (la.kind == 4) {
			Get();
		} else SynErr(38);
	}

	void OP() {
		if (la.kind == 21) {
			Get();
		} else if (la.kind == 22) {
			Get();
		} else if (la.kind == 23) {
			Get();
		} else if (la.kind == 24) {
			Get();
		} else SynErr(39);
	}

	void CompareExpr() {
		RValue();
		cmd = cmd + t.val; 
		CompareOp();
		cmd = cmd + " " + t.val + " "; 
		RValue();
		cmd = cmd + t.val ; 
	}

	void CompareOp() {
		switch (la.kind) {
		case 27: {
			Get();
			break;
		}
		case 28: {
			Get();
			break;
		}
		case 29: {
			Get();
			break;
		}
		case 30: {
			Get();
			break;
		}
		case 31: {
			Get();
			break;
		}
		case 32: {
			Get();
			break;
		}
		default: SynErr(40); break;
		}
	}



	public void Parse() {
		la = new Token();
		la.val = "";		
		Get();
		TinyCode();
		Expect(0);

	}

	private static final boolean[][] set = {
		{_T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x},
		{_x,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_T,_T, _x,_x,_x,_x, _x,_T,_x,_x, _x,_x,_x,_x, _x,_x,_x},
		{_x,_T,_T,_T, _T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x},
		{_x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x,_x, _x,_T,_T,_T, _T,_x,_x,_x, _x,_x,_x,_x, _x,_x,_x}

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
			case 1: s = "Identifier expected"; break;
			case 2: s = "Number expected"; break;
			case 3: s = "true expected"; break;
			case 4: s = "false expected"; break;
			case 5: s = "\"program\" expected"; break;
			case 6: s = "\".\" expected"; break;
			case 7: s = "\"var\" expected"; break;
			case 8: s = "\"begin\" expected"; break;
			case 9: s = "\"end.\" expected"; break;
			case 10: s = "\":\" expected"; break;
			case 11: s = "\"integer\" expected"; break;
			case 12: s = "\"(\" expected"; break;
			case 13: s = "\"..\" expected"; break;
			case 14: s = "\")\" expected"; break;
			case 15: s = "\":=\" expected"; break;
			case 16: s = "\";\" expected"; break;
			case 17: s = "\"boolean\" expected"; break;
			case 18: s = "\"goto\" expected"; break;
			case 19: s = "\"let\" expected"; break;
			case 20: s = "\"not\" expected"; break;
			case 21: s = "\"+\" expected"; break;
			case 22: s = "\"-\" expected"; break;
			case 23: s = "\"and\" expected"; break;
			case 24: s = "\"or\" expected"; break;
			case 25: s = "\"if\" expected"; break;
			case 26: s = "\"else\" expected"; break;
			case 27: s = "\"=\" expected"; break;
			case 28: s = "\"<>\" expected"; break;
			case 29: s = "\"<\" expected"; break;
			case 30: s = "\"<=\" expected"; break;
			case 31: s = "\">\" expected"; break;
			case 32: s = "\">=\" expected"; break;
			case 33: s = "??? expected"; break;
			case 34: s = "invalid DeclareVarBody"; break;
			case 35: s = "invalid DeclareVarBody"; break;
			case 36: s = "invalid StatementBody"; break;
			case 37: s = "invalid Assignment"; break;
			case 38: s = "invalid RValue"; break;
			case 39: s = "invalid OP"; break;
			case 40: s = "invalid CompareOp"; break;
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
