package nez.lang.expr;

import nez.ast.SourcePosition;
import nez.lang.Expression;
import nez.lang.ExpressionTransducer;
import nez.lang.PossibleAcceptance;
import nez.util.StringUtils;
import nez.vm.Instruction;
import nez.vm.NezEncoder;

public class Cbyte extends Char {
	public int byteChar;

	Cbyte(SourcePosition s, boolean binary, int ch) {
		super(s, binary);
		this.byteChar = ch;
	}

	@Override
	public final boolean equalsExpression(Expression o) {
		if (o instanceof Cbyte) {
			return this.byteChar == ((Cbyte) o).byteChar && this.binary == ((Cbyte) o).isBinary();
		}
		return false;
	}

	@Override
	public final void format(StringBuilder sb) {
		sb.append(StringUtils.stringfyCharacter(this.byteChar));
	}

	@Override
	public Expression reshape(ExpressionTransducer m) {
		return m.reshapeByteChar(this);
	}

	@Override
	public boolean isConsumed() {
		return true;
	}

	@Override
	public short acceptByte(int ch) {
		return PossibleAcceptance.acceptByteChar(byteChar, ch);
	}

	@Override
	public Instruction encode(NezEncoder bc, Instruction next, Instruction failjump) {
		return bc.encodeCbyte(this, next, failjump);
	}

}