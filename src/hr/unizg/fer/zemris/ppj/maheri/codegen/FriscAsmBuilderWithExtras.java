package hr.unizg.fer.zemris.ppj.maheri.codegen;

import hr.unizg.fer.zemris.ppj.maheri.Logger;
import hr.unizg.fer.zemris.ppj.maheri.semantics.TerminalNode;
import hr.unizg.fer.zemris.ppj.maheri.semantics.type.Type;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FriscAsmBuilderWithExtras extends FriscAsmBuilder {

	private final FriscAsmBuilder dataSection = new FriscAsmBuilder();

	private final LinkedList<String> blockStartLabels = new LinkedList<String>();
	private final LinkedList<String> blockEndLabels = new LinkedList<String>();
	private List<?> blockLocalVariables;
	private String subroutineName;

	private int labelCounter = 0;

	@Override
	public String toString() {
		return super.toString() + dataSection.toString();
	}

	/**
	 * creates instructions to use numeric literal as expression operand
	 * 
	 * @param op
	 */
	public void genNumericLiteralRef(int op) {

		if (signExtended20bitOk(op)) {
			Logger.log("reference to 20bit extensible numeric literal:");
			addInstruction("MOVE " + Integer.toHexString(op) + ", R1");
			addInstruction("PUSH R1");
		} else {
			Logger.log("reference to big numeric literal");
			// store in mem (`DW) and load

			String label = tmpLabelName("NUM20BIT");
			dataSection.addLabel(label);
			dataSection.addInstruction("DW " + op);

			addInstruction("LOAD R1, (" + label + ")");
			addInstruction("PUSH R1");
		}
	}

	/**
	 * creates instructions to use character literal as expression operand
	 * 
	 * @param op
	 */
	public void genCharacterLiteralRef(char c) {
		Logger.log("reference to 20bit extensible numeric literal:");
		addInstruction("MOVE " + c + ", R1");
		addInstruction("PUSH R1");
	}

	/**
	 * creates instructions to use string literal as expression operand
	 * 
	 * @param unescapedString
	 */
	public void genStringLiteralRef(String unescapedString) {
		Logger.log("reference to string literal, storing it and passing pointer");

		String label = tmpLabelName("STRING");
		dataSection.addLabel(label);
		for (int i = 0; i < unescapedString.length(); ++i)
			dataSection.addInstruction("`DW " + Integer.toHexString(unescapedString.charAt(i)));

		addInstruction("MOVE R1, " + label);
		addInstruction("PUSH R1");
	}

	public void genAddressToValue(boolean isByteSized) {
		addInstruction("POP R1");
		addLoadInstruction("R1, (R1)", isByteSized);
		addInstruction("PUSH R1");
	}

	/**
	 * creates instructions to use global variable as expression operand
	 * 
	 * @param varname
	 *            name of variable
	 * 
	 */
	public void genGlobalRef(String var, boolean isByteSized, boolean passByAddress) {
		addInstruction("MOVE " + getLabelForGlobal(var) + ", R1");
		if (!passByAddress) {
			addLoadInstruction("R1, (R1)", isByteSized);
		}
		addInstruction("PUSH R1");
	}

	/**
	 * creates instructions to use local variable as expression operand
	 * 
	 * @param offset
	 *            byte offset of the local variable (positive = locals, negative
	 *            = params)
	 */
	public void genLocalRef(int offset, boolean isByteSized, boolean passByAddress) {
		if (passByAddress) {
			addInstruction("ADD R5, " + Integer.toHexString(offset) + ", R1");
		} else {
			addLoadInstruction("R1, (R5+" + Integer.toHexString(offset) + ")", isByteSized);
		}
		addInstruction("PUSH R1");
	}

	public void genArrayAccessRef(boolean isByteSized, boolean indexPassedByAddress, boolean passByAddress) {
		addInstruction("POP R1 ; arrIndex");
		addInstruction("POP R2 ; arrAddress");

		if (indexPassedByAddress) {
			addLoadInstruction("R1, (R1)", isByteSized);
		}

		if (!isByteSized) {
			addInstruction("SHL R1, 2, R1");
		}

		addInstruction("ADD R1, R2, R1");

		if (!passByAddress) {
			addLoadInstruction("R1, (R1)", isByteSized);
		}

		addInstruction("PUSH R1");
	}

	// maybe useful for loops ?
	public void genBlockStart() {
		String blockLabel = tmpLabelName("BLOCK_START");
		addLabel(blockLabel);

		blockStartLabels.add(blockLabel);
		// blockLocalVariables.add(new ArrayList<?????>());
		blockEndLabels.add(tmpLabelName("BLOCK_END"));
	}

	public void genBlockEnd() {
		addLabel(blockEndLabels.getLast());
		blockStartLabels.removeLast();
		blockEndLabels.removeLast();
	}

	public void genJumpToStartOfBlock() {
		addInstruction("JR " + blockStartLabels.getLast());
	}

	public void genJumpToEndOfBlock() {
		addInstruction("JR " + blockEndLabels.getLast());
	}

	/**
	 * creates instructions for start of new subroutine
	 * 
	 * @param subName
	 */
	public void genSubroutinePrologue(String subName) {
		Logger.log("Start of " + subName);

		subroutineName = subName;
		addLabel(getLabelForGlobal(subName));
		addInstruction("");
		// R5 is used as frame pointer, save it
		addInstruction("PUSH R5");
		addInstruction("MOVE R7, R5");
	}

	/**
	 * creates instructions at end of a subroutine
	 * 
	 * @param subName
	 *            name of routine
	 * @param localsSize
	 *            number of bytes to deallocate
	 */
	public void genSubroutineEpilogue(String subName, int localsSize) {
		Logger.log("End of " + subName);
		addLabel(getReturnLabelForSub(subroutineName));
		// R5 is used as frame pointer, restore it
		addInstruction("POP R5");
		addInstruction("ADD R7, " + Integer.toHexString(localsSize) + ", R7");
		addInstruction("RET ");
	}

	public void genReturnVal() {
		addInstruction("POP R6");
		addInstruction("JR " + getReturnLabelForSub(subroutineName));
	}

	public void genReturnVoid() {
		addInstruction("JR " + getReturnLabelForSub(subroutineName));
	}

	public void genCall(boolean hasReturnValue) {
		addInstruction("POP R1");
		addInstruction("CALL R1");
		if (hasReturnValue) {
			addInstruction("PUSH R6");
		}
	}

	public void genPostIncrement(boolean byteSized) {
		addInstruction("POP R1");
		addLoadInstruction("R2, (R1)", byteSized);
		addInstruction("PUSH R2");
		addInstruction("ADD R2, 1, R2");
		addStoreInstruction("R2, (R1)", byteSized);
	}

	public void genPostDecrement(boolean byteSized) {
		addInstruction("POP R1");
		addLoadInstruction("R2, (R1)", byteSized);
		addInstruction("PUSH R2");
		addInstruction("SUB R2, 1, R2");
		addStoreInstruction("R2, (R1)", byteSized);
	}

	public void genPreIncrement(boolean byteSized) {
		addInstruction("POP R1");
		addLoadInstruction("R2, (R1)", byteSized);
		addInstruction("ADD R2, 1, R2");
		addStoreInstruction("R2, (R1)", byteSized);
		addInstruction("PUSH R2");
	}

	public void genPreDecrement(boolean byteSized) {
		addInstruction("POP R1");
		addLoadInstruction("R2, (R1)", byteSized);
		addInstruction("ADD R2, 1, R2");
		addStoreInstruction("R2, (R1)", byteSized);
		addInstruction("PUSH R2");
	}

	public void genNegate() {
		addInstruction("POP R1");
		addInstruction("XOR R2, R2, R2");
		addInstruction("SUB R2, R1, R1");
		addInstruction("PUSH R1");
	}

	public void genBitwiseNot() {
		addInstruction("POP R1");
		addInstruction("XOR R1, -1, R1");
		addInstruction("PUSH R1");
	}

	public void genLogicalNot() {
		addInstruction("POP R1");
		addInstruction("AND R1, R1, R1"); // set Z flag
		equal();
		addInstruction("XOR R1, 1, R1"); // result = not z
		addInstruction("PUSH R1");
	}

	public void genAssignment(boolean byteSized) {
		addInstruction("POP R1"); // right (value)
		addInstruction("POP R2"); // left (address)

		addStoreInstruction("R1, (R2)", byteSized);

		addInstruction("PUSH R1");
	}

	public void genArithmeticBinaryOperation(String opSymbol, boolean leftOperandPassedByAddress,
			boolean rightOperandPassedByAddress) {
		addInstruction("POP R2");
		if (rightOperandPassedByAddress) {
			addLoadInstruction("R2, (R2)", false);
		}

		addInstruction("POP R1");
		if (leftOperandPassedByAddress) {
			addLoadInstruction("R1, (R1)", false);
		}

		boolean handled = true;
		if (opSymbol.length() == 1) {
			char op = opSymbol.charAt(0);
			switch (op) {
			case '*':
				addInstruction("CALL " + getLabelForGlobal("BUILTIN_MULT"));
				break;
			case '/':
				addInstruction("CALL " + getLabelForGlobal("BUILTIN_DIV"));
				break;
			case '%':
				addInstruction("CALL " + getLabelForGlobal("BUILTIN_DIV"));
				addInstruction("MOVE R2, R1");
				break;
			case '+':
				addInstruction("ADD R1, R2, R1");
				break;
			case '-':
				addInstruction("SUB R1, R2, R1");
				break;
			case '&':
				addInstruction("AND R1, R2, R1");
				break;
			case '^':
				addInstruction("XOR R1, R2, R1");
				break;
			case '|':
				addInstruction("OR R1, R2, R1");
				break;
			case '<':
				addInstruction("CMP R1, R2");
				lessThan();
				break;
			case '>':
				addInstruction("CMP R2, R1");
				lessThan();
				break;
			default:
				handled = false;
			}
		} else if ("<=".equals(opSymbol)) {
			// r1 <= r2 === !(r1 > r2) === !(r2 < r1)
			addInstruction("CMP R2, R1");
			lessThan();
			addInstruction("XOR R1, 1, R1");
		} else if (">=".equals(opSymbol)) {
			// r1 >= r2
			// !(r1 < r2)
			addInstruction("CMP R1, R2");
			lessThan();
			addInstruction("XOR R1, 1, R1");
		} else if ("==".equals(opSymbol)) {
			addInstruction("CMP R1, R2");
			equal();
		} else if ("!=".equals(opSymbol)) {
			addInstruction("CMP R1, R2");
			equal();
			addInstruction("XOR R1, 1, R1");
		} else {
			handled = false;
		}
		
		if (!handled) {
			Logger.log("Unhandled operation " + opSymbol + ", setting result to 0");
			addInstruction("XOR R1, R1, R1");
		}

		addInstruction("PUSH R1");
	}
	
	public void genDiscard() {
		addInstruction("POP R1");
	}
	
	public void genAddDefault() {
		addInstruction("MOVE 1, R1");
		addInstruction("PUSH R1");
	}

	// TODO gen: allocate + init za int, char, string, array...
	public void genLocalsDeallocation() {

	}

	public void genStartProgram() {
		addLabel("start");
		addInstruction("MOVE 40000, R7");
		addInstruction("CALL " + getLabelForGlobal("main"));
		addInstruction("HALT");

		genMultProcedure();
		genDivProcedure();
	}

	private void lessThan() {
		// N xor V -> R1
		addInstruction("MOVE SR, R1"); // SR = ZVCN
		addInstruction("SHR R1, 2, R2");
		addInstruction("XOR R1, R2, R1");
		addInstruction("AND R1, 1, R1");
	}

	private void equal() {
		addInstruction("MOVE SR, R1");
		addInstruction("SHR R1, 3, R1");
		addInstruction("AND R1, 1, R1");
	}

	private void genMultProcedure() {
		// TODO, procedure must put R1*R2 in R1 and preserve all registers
	}

	private void genDivProcedure() {
		// TODO, procedure puts R1/R2 in R1, R1%r2 in R2 and keep others
	}

	private void addLoadInstruction(String args, boolean isByte) {
		// TODO make sure caller passes isByte correctly
		addInstruction((isByte ? "LOADB " : "LOAD ") + args);
	}

	private void addStoreInstruction(String args, boolean isByte) {
		// TODO make sure caller passes isByte correctly
		addInstruction((isByte ? "STOREB " : "STORE ") + args);
	}

	private String tmpLabelName(String base) {
		return "TMP_" + base + "_" + labelCounter++;
	}

	private String getLabelForGlobal(String var) {
		return "GLOBAL_" + var;
	}

	private String getReturnLabelForSub(String var) {
		return "RET_FROM_" + var;
	}

	private boolean signExtended20bitOk(int val) {
		int high12 = (val & 0xfff00000);
		boolean signBit = (val & 0x00010000) != 0;
		int bitCount12 = Integer.bitCount(high12);
		return bitCount12 == 12 && signBit || bitCount12 == 0 && !signBit;

	}

}
