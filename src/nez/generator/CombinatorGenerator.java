package nez.generator;

import nez.Parser;
import nez.lang.Expression;
import nez.lang.Production;
import nez.lang.expr.Pand;
import nez.lang.expr.Cany;
import nez.lang.expr.Cbyte;
import nez.lang.expr.Cset;
import nez.lang.expr.Tcapture;
import nez.lang.expr.Pchoice;
import nez.lang.expr.Pempty;
import nez.lang.expr.Pfail;
import nez.lang.expr.Tlink;
import nez.lang.expr.Tnew;
import nez.lang.expr.NonTerminal;
import nez.lang.expr.Pnot;
import nez.lang.expr.Poption;
import nez.lang.expr.Pzero;
import nez.lang.expr.Pone;
import nez.lang.expr.Treplace;
import nez.lang.expr.Psequence;
import nez.lang.expr.Ttag;
import nez.util.StringUtils;

public class CombinatorGenerator extends GrammarGenerator {
	@Override
	public String getDesc() {
		return "a Nez combinator for Java";
	}

	protected String _Delim() {
		return ", ";
	}

	@Override
	public void makeHeader(Parser g) {
		L("/* Parsing Expression Grammars for Nez */");
		L("import nez.ParserCombinator;");
		L("import nez.lang.Expression;");
		L("");
		L("class G extends ParserCombinator").Begin();
	}

	public void makeFooter(Parser g) {
		End();
	}

	@Override
	public void visitProduction(Production rule) {
		Expression e = rule.getExpression();
		L("public Expression p").W(_NonTerminal(rule)).W("() ").Begin();
		L("return ");
		visitExpression(e);
		W(";");
		End();
	}

	public void visitEmpty(Pempty e) {
		C("Empty");
	}

	public void visitFailure(Pfail e) {
		C("Failure");
	}

	public void visitNonTerminal(NonTerminal e) {
		C("P", _NonTerminal(e.getProduction()));
	}

	public void visitByteChar(Cbyte e) {
		C("t", StringUtils.stringfyByte('"', e.byteChar, '"'));
	}

	public void visitByteMap(Cset e) {
		C("c", e.byteMap);
	}

	public void visitString(String s) {
		C("t", s);
	}

	public void visitAnyChar(Cany e) {
		C("AnyChar");
	}

	public void visitOption(Poption e) {
		C("Option", e);
	}

	public void visitRepetition(Pzero e) {
		C("ZeroMore", e);
	}

	public void visitRepetition1(Pone e) {
		C("OneMore", e);
	}

	public void visitAnd(Pand e) {
		C("And", e);
	}

	public void visitNot(Pnot e) {
		C("Not", e);
	}

	public void visitChoice(Pchoice e) {
		C("Choice", e);
	}

	public void visitSequence(Psequence e) {
		W("Sequence(");
		super.visitSequence(e);
		W(")");
	}

	public void visitNew(Tnew e) {
		if (e.leftFold) {
			C("LCapture", e.shift);
		} else {
			C("NCapture", e.shift);
		}
	}

	public void visitCapture(Tcapture e) {
		C("Capture", e.shift);
	}

	public void visitTagging(Ttag e) {
		C("Tagging", e.getTagName());
	}

	public void visitReplace(Treplace e) {
		C("Replace", StringUtils.quoteString('"', e.value, '"'));
	}

	public void visitLink(Tlink e) {
		if (e.index != -1) {
			C("Link", String.valueOf(e.index), e);
		} else {
			C("Link", e);
		}
	}

	@Override
	public void visitUndefined(Expression e) {
		W("<");
		W(e.getPredicate());
		for (Expression se : e) {
			W(" ");
			visitExpression(se);
		}
		W(">");
	}

}
