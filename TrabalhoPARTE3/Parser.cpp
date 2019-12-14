

#include <wchar.h>
#include "Parser.h"
#include "Scanner.h"




void Parser::SynErr(int n) {
	if (errDist >= minErrDist) errors->SynErr(la->line, la->col, n);
	errDist = 0;
}

void Parser::SemErr(const wchar_t* msg) {
	if (errDist >= minErrDist) errors->Error(t->line, t->col, msg);
	errDist = 0;
}

void Parser::Get() {
	for (;;) {
		t = la;
		la = scanner->Scan();
		if (la->kind <= maxT) { ++errDist; break; }

		if (dummyToken != t) {
			dummyToken->kind = t->kind;
			dummyToken->pos = t->pos;
			dummyToken->col = t->col;
			dummyToken->line = t->line;
			dummyToken->next = NULL;
			coco_string_delete(dummyToken->val);
			dummyToken->val = coco_string_create(t->val);
			t = dummyToken;
		}
		la = t;
	}
}

void Parser::Expect(int n) {
	if (la->kind==n) Get(); else { SynErr(n); }
}

void Parser::ExpectWeak(int n, int follow) {
	if (la->kind == n) Get();
	else {
		SynErr(n);
		while (!StartOf(follow)) Get();
	}
}

bool Parser::WeakSeparator(int n, int syFol, int repFol) {
	if (la->kind == n) {Get(); return true;}
	else if (StartOf(repFol)) {return false;}
	else {
		SynErr(n);
		while (!(StartOf(syFol) || StartOf(repFol) || StartOf(0))) {
			Get();
		}
		return StartOf(syFol);
	}
}

void Parser::CLC() {
		Expect(3 /* "begin" */);
		L();
		Expect(4 /* "end." */);
}

void Parser::L() {
		if (StartOf(1)) {
			C();
			Expect(5 /* ";" */);
			L();
		} else if (la->kind == 6 /* "showSymbolTable" */) {
			Get();
			Expect(7 /* "(" */);
			Expect(8 /* ")" */);
			Expect(5 /* ";" */);
		} else if (la->kind == 9 /* "showSymbolTree" */) {
			Get();
			Expect(7 /* "(" */);
			Expect(8 /* ")" */);
			Expect(5 /* ";" */);
		} else SynErr(28);
}

void Parser::C() {
		if (la->kind == _id) {
			Get();
			Expect(10 /* ":=" */);
			E();
		} else if (la->kind == 11 /* "input" */) {
			Get();
			Expect(7 /* "(" */);
			Expect(_id);
			Expect(8 /* ")" */);
		} else if (la->kind == 12 /* "print" */) {
			Get();
			Expect(7 /* "(" */);
			Expect(_id);
			Expect(8 /* ")" */);
		} else if (la->kind == 23 /* "do" */) {
			DOWHILE();
		} else SynErr(29);
}

void Parser::E() {
		T();
		while (la->kind == 13 /* "+" */ || la->kind == 14 /* "-" */) {
			if (la->kind == 13 /* "+" */) {
				Get();
			} else {
				Get();
			}
			T();
		}
}

void Parser::DOWHILE() {
		Expect(23 /* "do" */);
		Expect(24 /* "{" */);
		L();
		Expect(25 /* "}" */);
		Expect(26 /* "while" */);
		Expect(7 /* "(" */);
		COMPARA();
		Expect(8 /* ")" */);
		Expect(5 /* ";" */);
}

void Parser::T() {
		F();
		while (la->kind == 15 /* "*" */ || la->kind == 16 /* "/" */) {
			if (la->kind == 15 /* "*" */) {
				Get();
			} else {
				Get();
			}
			F();
		}
}

void Parser::F() {
		if (la->kind == _id) {
			Get();
		} else if (la->kind == _num) {
			Get();
		} else if (la->kind == 7 /* "(" */) {
			Get();
			E();
			Expect(8 /* ")" */);
		} else SynErr(30);
}

void Parser::COMPARA() {
		if (StartOf(1)) {
			C();
			Expect(17 /* "==" */);
			C();
		} else if (StartOf(1)) {
			C();
			Expect(18 /* ">=" */);
			C();
		} else if (StartOf(1)) {
			C();
			Expect(19 /* "<=" */);
			C();
		} else if (StartOf(1)) {
			C();
			Expect(20 /* ">" */);
			C();
		} else if (StartOf(1)) {
			C();
			Expect(21 /* "<" */);
			C();
		} else if (StartOf(1)) {
			C();
			Expect(22 /* "!=" */);
			C();
		} else SynErr(31);
}




// If the user declared a method Init and a mehtod Destroy they should
// be called in the contructur and the destructor respctively.
//
// The following templates are used to recognize if the user declared
// the methods Init and Destroy.

template<typename T>
struct ParserInitExistsRecognizer {
	template<typename U, void (U::*)() = &U::Init>
	struct ExistsIfInitIsDefinedMarker{};

	struct InitIsMissingType {
		char dummy1;
	};
	
	struct InitExistsType {
		char dummy1; char dummy2;
	};

	// exists always
	template<typename U>
	static InitIsMissingType is_here(...);

	// exist only if ExistsIfInitIsDefinedMarker is defined
	template<typename U>
	static InitExistsType is_here(ExistsIfInitIsDefinedMarker<U>*);

	enum { InitExists = (sizeof(is_here<T>(NULL)) == sizeof(InitExistsType)) };
};

template<typename T>
struct ParserDestroyExistsRecognizer {
	template<typename U, void (U::*)() = &U::Destroy>
	struct ExistsIfDestroyIsDefinedMarker{};

	struct DestroyIsMissingType {
		char dummy1;
	};
	
	struct DestroyExistsType {
		char dummy1; char dummy2;
	};

	// exists always
	template<typename U>
	static DestroyIsMissingType is_here(...);

	// exist only if ExistsIfDestroyIsDefinedMarker is defined
	template<typename U>
	static DestroyExistsType is_here(ExistsIfDestroyIsDefinedMarker<U>*);

	enum { DestroyExists = (sizeof(is_here<T>(NULL)) == sizeof(DestroyExistsType)) };
};

// The folloing templates are used to call the Init and Destroy methods if they exist.

// Generic case of the ParserInitCaller, gets used if the Init method is missing
template<typename T, bool = ParserInitExistsRecognizer<T>::InitExists>
struct ParserInitCaller {
	static void CallInit(T *t) {
		// nothing to do
	}
};

// True case of the ParserInitCaller, gets used if the Init method exists
template<typename T>
struct ParserInitCaller<T, true> {
	static void CallInit(T *t) {
		t->Init();
	}
};

// Generic case of the ParserDestroyCaller, gets used if the Destroy method is missing
template<typename T, bool = ParserDestroyExistsRecognizer<T>::DestroyExists>
struct ParserDestroyCaller {
	static void CallDestroy(T *t) {
		// nothing to do
	}
};

// True case of the ParserDestroyCaller, gets used if the Destroy method exists
template<typename T>
struct ParserDestroyCaller<T, true> {
	static void CallDestroy(T *t) {
		t->Destroy();
	}
};

void Parser::Parse() {
	t = NULL;
	la = dummyToken = new Token();
	la->val = coco_string_create(L"Dummy Token");
	Get();
	CLC();
	Expect(0);
}

Parser::Parser(Scanner *scanner) {
	maxT = 27;

	ParserInitCaller<Parser>::CallInit(this);
	dummyToken = NULL;
	t = la = NULL;
	minErrDist = 2;
	errDist = minErrDist;
	this->scanner = scanner;
	errors = new Errors();
}

bool Parser::StartOf(int s) {
	const bool T = true;
	const bool x = false;

	static bool set[2][29] = {
		{T,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x,x,x,x, x},
		{x,T,x,x, x,x,x,x, x,x,x,T, T,x,x,x, x,x,x,x, x,x,x,T, x,x,x,x, x}
	};



	return set[s][la->kind];
}

Parser::~Parser() {
	ParserDestroyCaller<Parser>::CallDestroy(this);
	delete errors;
	delete dummyToken;
}

Errors::Errors() {
	count = 0;
}

void Errors::SynErr(int line, int col, int n) {
	wchar_t* s;
	switch (n) {
			case 0: s = coco_string_create(L"EOF expected"); break;
			case 1: s = coco_string_create(L"id expected"); break;
			case 2: s = coco_string_create(L"num expected"); break;
			case 3: s = coco_string_create(L"\"begin\" expected"); break;
			case 4: s = coco_string_create(L"\"end.\" expected"); break;
			case 5: s = coco_string_create(L"\";\" expected"); break;
			case 6: s = coco_string_create(L"\"showSymbolTable\" expected"); break;
			case 7: s = coco_string_create(L"\"(\" expected"); break;
			case 8: s = coco_string_create(L"\")\" expected"); break;
			case 9: s = coco_string_create(L"\"showSymbolTree\" expected"); break;
			case 10: s = coco_string_create(L"\":=\" expected"); break;
			case 11: s = coco_string_create(L"\"input\" expected"); break;
			case 12: s = coco_string_create(L"\"print\" expected"); break;
			case 13: s = coco_string_create(L"\"+\" expected"); break;
			case 14: s = coco_string_create(L"\"-\" expected"); break;
			case 15: s = coco_string_create(L"\"*\" expected"); break;
			case 16: s = coco_string_create(L"\"/\" expected"); break;
			case 17: s = coco_string_create(L"\"==\" expected"); break;
			case 18: s = coco_string_create(L"\">=\" expected"); break;
			case 19: s = coco_string_create(L"\"<=\" expected"); break;
			case 20: s = coco_string_create(L"\">\" expected"); break;
			case 21: s = coco_string_create(L"\"<\" expected"); break;
			case 22: s = coco_string_create(L"\"!=\" expected"); break;
			case 23: s = coco_string_create(L"\"do\" expected"); break;
			case 24: s = coco_string_create(L"\"{\" expected"); break;
			case 25: s = coco_string_create(L"\"}\" expected"); break;
			case 26: s = coco_string_create(L"\"while\" expected"); break;
			case 27: s = coco_string_create(L"??? expected"); break;
			case 28: s = coco_string_create(L"invalid L"); break;
			case 29: s = coco_string_create(L"invalid C"); break;
			case 30: s = coco_string_create(L"invalid F"); break;
			case 31: s = coco_string_create(L"invalid COMPARA"); break;

		default:
		{
			wchar_t format[20];
			coco_swprintf(format, 20, L"error %d", n);
			s = coco_string_create(format);
		}
		break;
	}
	wprintf(L"-- line %d col %d: %ls\n", line, col, s);
	coco_string_delete(s);
	count++;
}

void Errors::Error(int line, int col, const wchar_t *s) {
	wprintf(L"-- line %d col %d: %ls\n", line, col, s);
	count++;
}

void Errors::Warning(int line, int col, const wchar_t *s) {
	wprintf(L"-- line %d col %d: %ls\n", line, col, s);
}

void Errors::Warning(const wchar_t *s) {
	wprintf(L"%ls\n", s);
}

void Errors::Exception(const wchar_t* s) {
	wprintf(L"%ls", s); 
	exit(1);
}


