package hr.unizg.fer.zemris.ppj.maheri.codegen;

import hr.unizg.fer.zemris.ppj.maheri.Logger;
import hr.unizg.fer.zemris.ppj.maheri.semantics.TerminalNode;
import hr.unizg.fer.zemris.ppj.maheri.semantics.type.Type;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FriscAsmBuilderWithExtras {
	
	private final FriscAsmBuilder dataSection = new FriscAsmBuilder();
	private final FriscAsmBuilder code = new FriscAsmBuilder();
	private final FriscAsmBuilder init = new FriscAsmBuilder();
	
	private FriscAsmBuilder builder = code;
	
	public void switchToInit() {
		this.builder = init;
	}
	public void switchToCode() {
		this.builder = code;
	}

	private final LinkedList<String> blockStartLabels = new LinkedList<String>();
	private final LinkedList<String> blockEndLabels = new LinkedList<String>();
	private List<?> blockLocalVariables;
	private String subroutineName;

	private int labelCounter = 0;
	private int initStatus = 0;

	public FriscAsmBuilderWithExtras() {
		builder.addLabel("start");
		builder.addInstruction("MOVE 40000, R7");
		builder.addInstruction("CALL " + getLabelForGlobal("INITIALIZERS"));
		builder.addInstruction("CALL " + getLabelForGlobal("main"));
		builder.addInstruction("HALT");

		genMultProcedure();
		genDivProcedure();

		init.addLabel(getLabelForGlobal("INITIALIZERS"));
		init.addInstruction("");
		++initStatus;
	}

	public void finish() {
		if (initStatus < 2) {
			init.addInstruction("RET");
			++initStatus;
		}
	}

	@Override
	public String toString() {
		return code.toString() + init.toString() + dataSection.toString();
	}

	/**
	 * creates instructions to use numeric literal as expression operand
	 * 
	 * @param op
	 */
	public void genNumericLiteralRef(int op) {

		if (signExtended20bitOk(op)) {
			Logger.log("reference to 20bit extensible numeric literal:");
			builder.addInstruction("MOVE " + Integer.toHexString(op) + ", R1");
			builder.addInstruction("PUSH R1");
		} else {
			Logger.log("reference to big numeric literal");
			// store in mem (`DW) and load

			String label = tmpLabelName("NUM20BIT");
			dataSection.addLabel(label);
			dataSection.addInstruction("DW " + op);

			builder.addInstruction("LOAD R1, (" + label + ")");
			builder.addInstruction("PUSH R1");
		}
	}

	/**
	 * creates instructions to use character literal as expression operand
	 * 
	 * @param op
	 */
	public void genCharacterLiteralRef(char c) {
		Logger.log("reference to 20bit extensible numeric literal:");
		builder.addInstruction("MOVE " + c + ", R1");
		builder.addInstruction("PUSH R1");
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

		builder.addInstruction("MOVE R1, " + label);
		builder.addInstruction("PUSH R1");
	}

	public void genAddressToValue(boolean isByteSized) {
		Logger.log("address is on op-stack, convert to value");
		builder.addInstruction("POP R1");
		addLoadInstruction("R1, (R1)", isByteSized);
		builder.addInstruction("PUSH R1");
	}

	/**
	 * creates instructions to use global variable as expression operand
	 * 
	 * @param varname
	 *            name of variable
	 * 
	 */
	public void genGlobalRef(String var, boolean isByteSized, boolean passByAddress) {
		Logger.log("reference to global variable " + var);
		builder.addInstruction("MOVE " + getLabelForGlobal(var) + ", R1");
		if (!passByAddress) {
			addLoadInstruction("R1, (R1)", isByteSized);
		}
		builder.addInstruction("PUSH R1");
	}

	/**
	 * creates instructions to use local variable as expression operand
	 * 
	 * @param offset
	 *            byte offset of the local variable (positive = locals, negative
	 *            = params)
	 */
	public void genLocalRef(int offset, boolean isByteSized, boolean passByAddress) {
		Logger.log("reference to local variable @" + offset);
		if (passByAddress) {
			builder.addInstruction("ADD R5, " + Integer.toHexString(offset) + ", R1");
		} else {
			addLoadInstruction("R1, (R5+" + Integer.toHexString(offset) + ")", isByteSized);
		}
		builder.addInstruction("PUSH R1");
	}

	public void genArrayAccessRef(boolean isByteSized, boolean indexPassedByAddress, boolean passByAddress) {
		Logger.log("array member access");
		builder.addInstruction("POP R1 ; arrIndex");
		builder.addInstruction("POP R2 ; arrAddress");

		if (indexPassedByAddress) {
			addLoadInstruction("R1, (R1)", isByteSized);
		}

		if (!isByteSized) {
			builder.addInstruction("SHL R1, 2, R1");
		}

		builder.addInstruction("ADD R1, R2, R1");

		if (!passByAddress) {
			addLoadInstruction("R1, (R1)", isByteSized);
		}

		builder.addInstruction("PUSH R1");
	}

	// maybe useful for loops ?
	public void genBlockStart() {
		String blockLabel = tmpLabelName("BLOCK_START");
		builder.addLabel(blockLabel);

		blockStartLabels.add(blockLabel);
		// blockLocalVariables.add(new ArrayList<?????>());
		blockEndLabels.add(tmpLabelName("BLOCK_END"));
	}

	public void genBlockEnd() {
		builder.addLabel(blockEndLabels.getLast());
		blockStartLabels.removeLast();
		blockEndLabels.removeLast();
	}

	public void genJumpToStartOfBlock() {
		builder.addInstruction("JR " + blockStartLabels.getLast());
	}

	public void genJumpToEndOfBlock() {
		builder.addInstruction("JR " + blockEndLabels.getLast());
	}

	/**
	 * creates instructions for start of new subroutine
	 * 
	 * @param subName
	 */
	public void genSubroutinePrologue(String subName) {
		Logger.log("Start of " + subName);

		subroutineName = subName;
		builder.addLabel(getLabelForGlobal(subName));
		builder.addInstruction("");
		// R5 is used as frame pointer, save it
		builder.addInstruction("PUSH R5");
		builder.addInstruction("MOVE R7, R5");
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
		builder.addLabel(getReturnLabelForSub(subroutineName));
		// R5 is used as frame pointer, restore it
		builder.addInstruction("POP R5");
		builder.addInstruction("ADD R7, " + Integer.toHexString(localsSize) + ", R7");
		builder.addInstruction("RET ");
	}

	public void genReturnVal() {
		builder.addInstruction("POP R6");
		builder.addInstruction("JR " + getReturnLabelForSub(subroutineName));
	}

	public void genReturnVoid() {
		builder.addInstruction("JR " + getReturnLabelForSub(subroutineName));
	}

	public void genCall(boolean hasReturnValue) {
		builder.addInstruction("POP R1");
		builder.addInstruction("CALL R1");
		if (hasReturnValue) {
			builder.addInstruction("PUSH R6");
		}
	}

	public void genPostIncrement(boolean byteSized) {
		builder.addInstruction("POP R1");
		addLoadInstruction("R2, (R1)", byteSized);
		builder.addInstruction("PUSH R2");
		builder.addInstruction("ADD R2, 1, R2");
		addStoreInstruction("R2, (R1)", byteSized);
	}

	public void genPostDecrement(boolean byteSized) {
		builder.addInstruction("POP R1");
		addLoadInstruction("R2, (R1)", byteSized);
		builder.addInstruction("PUSH R2");
		builder.addInstruction("SUB R2, 1, R2");
		addStoreInstruction("R2, (R1)", byteSized);
	}

	public void genPreIncrement(boolean byteSized) {
		builder.addInstruction("POP R1");
		addLoadInstruction("R2, (R1)", byteSized);
		builder.addInstruction("ADD R2, 1, R2");
		addStoreInstruction("R2, (R1)", byteSized);
		builder.addInstruction("PUSH R2");
	}

	public void genPreDecrement(boolean byteSized) {
		builder.addInstruction("POP R1");
		addLoadInstruction("R2, (R1)", byteSized);
		builder.addInstruction("ADD R2, 1, R2");
		addStoreInstruction("R2, (R1)", byteSized);
		builder.addInstruction("PUSH R2");
	}

	public void genNegate() {
		builder.addInstruction("POP R1");
		builder.addInstruction("XOR R2, R2, R2");
		builder.addInstruction("SUB R2, R1, R1");
		builder.addInstruction("PUSH R1");
	}

	public void genBitwiseNot() {
		builder.addInstruction("POP R1");
		builder.addInstruction("XOR R1, -1, R1");
		builder.addInstruction("PUSH R1");
	}

	public void genLogicalNot() {
		builder.addInstruction("POP R1");
		builder.addInstruction("AND R1, R1, R1"); // set Z flag
		equal();
		builder.addInstruction("XOR R1, 1, R1"); // result = not z
		builder.addInstruction("PUSH R1");
	}

	public void genAssignment(boolean byteSized) {
		builder.addInstruction("POP R1"); // right (value)
		builder.addInstruction("POP R2"); // left (address)

		addStoreInstruction("R1, (R2)", byteSized);

		builder.addInstruction("PUSH R1");
	}

	public void genArithmeticBinaryOperation(String opSymbol, boolean leftOperandPassedByAddress,
			boolean rightOperandPassedByAddress) {
		builder.addInstruction("POP R2");
		if (rightOperandPassedByAddress) {
			addLoadInstruction("R2, (R2)", false);
		}

		builder.addInstruction("POP R1");
		if (leftOperandPassedByAddress) {
			addLoadInstruction("R1, (R1)", false);
		}

		boolean handled = true;
		if (opSymbol.length() == 1) {
			char op = opSymbol.charAt(0);
			switch (op) {
			case '*':
				builder.addInstruction("CALL " + getLabelForGlobal("BUILTIN_MULT"));
				break;
			case '/':
				builder.addInstruction("CALL " + getLabelForGlobal("BUILTIN_DIV"));
				break;
			case '%':
				builder.addInstruction("CALL " + getLabelForGlobal("BUILTIN_DIV"));
				builder.addInstruction("MOVE R2, R1");
				break;
			case '+':
				builder.addInstruction("ADD R1, R2, R1");
				break;
			case '-':
				builder.addInstruction("SUB R1, R2, R1");
				break;
			case '&':
				builder.addInstruction("AND R1, R2, R1");
				break;
			case '^':
				builder.addInstruction("XOR R1, R2, R1");
				break;
			case '|':
				builder.addInstruction("OR R1, R2, R1");
				break;
			case '<':
				builder.addInstruction("CMP R1, R2");
				lessThan();
				break;
			case '>':
				builder.addInstruction("CMP R2, R1");
				lessThan();
				break;
			default:
				handled = false;
			}
		} else if ("<=".equals(opSymbol)) {
			// r1 <= r2 === !(r1 > r2) === !(r2 < r1)
			builder.addInstruction("CMP R2, R1");
			lessThan();
			builder.addInstruction("XOR R1, 1, R1");
		} else if (">=".equals(opSymbol)) {
			// r1 >= r2
			// !(r1 < r2)
			builder.addInstruction("CMP R1, R2");
			lessThan();
			builder.addInstruction("XOR R1, 1, R1");
		} else if ("==".equals(opSymbol)) {
			builder.addInstruction("CMP R1, R2");
			equal();
		} else if ("!=".equals(opSymbol)) {
			builder.addInstruction("CMP R1, R2");
			equal();
			builder.addInstruction("XOR R1, 1, R1");
		} else {
			handled = false;
		}

		if (!handled) {
			Logger.log("Unhandled operation " + opSymbol + ", setting result to 0");
			builder.addInstruction("XOR R1, R1, R1");
		}

		builder.addInstruction("PUSH R1");
	}

	public void genDiscard() {
		builder.addInstruction("POP R1");
	}

	public void genAddDefault() {
		builder.addInstruction("MOVE 1, R1");
		builder.addInstruction("PUSH R1");
	}
	
	public void genGlobalAllocation(String name, int bytes) {
		Logger.log("allocating global " + name);
		dataSection.addLabel(getLabelForGlobal(name));
		dataSection.addInstruction("`DS " + bytes);
	}
	
	public void prepGlobalInitialiser(String text) {
		Logger.log("prepare init of " + text);
		builder.addInstruction("MOVE " + getLabelForGlobal(text) + ", R1");
		builder.addInstruction("PUSH R1");
	}

	// TODO gen: allocate + init za int, char, string, array...
	public void genLocalsDeallocation() {

	}

	private void lessThan() {
		// N xor V -> R1
		builder.addInstruction("MOVE SR, R1"); // SR = ZVCN
		builder.addInstruction("SHR R1, 2, R2");
		builder.addInstruction("XOR R1, R2, R1");
		builder.addInstruction("AND R1, 1, R1");
	}

	private void equal() {
		builder.addInstruction("MOVE SR, R1");
		builder.addInstruction("SHR R1, 3, R1");
		builder.addInstruction("AND R1, 1, R1");
	}

	private void genMultProcedure() {
		// TODO, procedure must put R1*R2 in R1 and preserve all registers
	}

	private void genDivProcedure() {
		// TODO, procedure puts R1/R2 in R1, R1%r2 in R2 and keep others
	}

	private void addLoadInstruction(String args, boolean isByte) {
		// TODO make sure caller passes isByte correctly
		builder.addInstruction((isByte ? "LOADB " : "LOAD ") + args);
	}

	private void addStoreInstruction(String args, boolean isByte) {
		// TODO make sure caller passes isByte correctly
		builder.addInstruction((isByte ? "STOREB " : "STORE ") + args);
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
