package nez.lang.expr;

import nez.ast.SourcePosition;
import nez.lang.Contextual;
import nez.lang.Expression;
import nez.lang.ExpressionTransducer;
import nez.lang.PossibleAcceptance;
import nez.lang.Typestate;
import nez.lang.Visa;
import nez.vm.Instruction;
import nez.vm.NezEncoder;

@Deprecated
public class Xindent extends Term implements Contextual {
	Xindent(SourcePosition s) {
		super(s);
	}

	@Override
	public final boolean equalsExpression(Expression o) {
		return (o instanceof Xindent);
	}

	@Override
	public Expression reshape(ExpressionTransducer m) {
		return m.reshapeIsIndent(this);
	}

	@Override
	public boolean isConsumed() {
		return false;
	}

	@Override
	public int inferTypestate(Visa v) {
		return Typestate.BooleanType;
	}

	@Override
	public short acceptByte(int ch) {
		if (ch == '\t' || ch == ' ') {
			return PossibleAcceptance.Accept;
		}
		return PossibleAcceptance.Unconsumed;
	}

	@Override
	public Instruction encode(NezEncoder bc, Instruction next, Instruction failjump) {
		return bc.encodeXindent(this, next, failjump);
	}
}