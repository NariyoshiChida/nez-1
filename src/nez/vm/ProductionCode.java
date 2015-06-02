package nez.vm;

import nez.lang.Expression;
import nez.lang.Production;

class ProductionCode {
	Production production;
	Expression localExpression;
	Instruction codePoint;
	int start;
	int end;
	int ref = 0;
	boolean inlining = false;
	MemoPoint memoPoint = null;
	Instruction memoCodePoint = null;
	ProductionCode(Production p, Expression deref) {
		this.production = p;
		this.localExpression = deref;
	}
}