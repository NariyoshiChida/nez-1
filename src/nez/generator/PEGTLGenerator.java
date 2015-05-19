package nez.generator;

import nez.lang.ByteChar;
import nez.lang.Expression;
import nez.lang.Production;
import nez.lang.Sequence;
import nez.util.StringUtils;

public class PEGTLGenerator extends GrammarGenerator {

	PEGTLGenerator() {
		super(null);
	}

	PEGTLGenerator(String fileName) {
		super(fileName);
	}

	@Override
	public String getDesc() {
		return "Parsing Expression Grammar Template Library for C++11";
	}

	public void makeHeader() {
		//W("// The following is generated by the Nez Grammar Generator ");
	}
	
//	public void visitProduction(Production r) {
//
//	}

	public void makeFooter() {
		
	}

	@Override
	public void visitProduction(Production p) {
		Expression e = p.getExpression();
		L("struct " + p.getLocalName() + " : ");
		inc();
		visit(e);
		W(" {};");
		dec();
	}	

	@Override
	public void visitByteChar(ByteChar e) {
		W("pegtl::one<" + StringUtils.stringfyByte(e.byteChar) + ">");
	}	

	@Override
	public void visitSequence(Sequence e) {
		int c = 0;
		W("pegtl::seq<");
		for(Expression sub: e) {
			if(c > 0) {
				W(", ");
			}
			visit(sub);
			c++;
		}
		W(">");
	}	

}