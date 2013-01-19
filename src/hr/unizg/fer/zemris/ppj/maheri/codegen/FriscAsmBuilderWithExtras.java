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
	private  List<?> blockLocalVariables;

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
	
	/**
	 * creates instructions to use global variable as expression operand
	 * 
	 * @param varname
	 *            name of variable
	 */
	public void genGlobalRef(String var, boolean character) {
		String op = character ? "MOVEB " : "MOVE ";
		addInstruction(op + getLabelForVariable(var) + ", R1");
		addInstruction("PUSH R1");
	}

	/**
	 * creates instructions to use local variable as expression operand
	 * 
	 * @param offset
	 *            byte offset of the local variable (positive = locals, negative
	 *            = params)
	 */
	public void genLocalRef(long offset, boolean character) {
		String op = character ? "LOADB" : "LOAD";
		addInstruction(op + " R1, (R5+" + Long.toHexString(offset) + ")");
	}

	// maybe useful for loops ?
	public void genBlockStart() {
		String blockLabel = tmpLabelName("BLOCK_START");
		addLabel(blockLabel);

		blockStartLabels.add(blockLabel);
//		blockLocalVariables.add(new ArrayList<?????>());
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
		addLabel(getLabelForSub(subName));
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
	public void genSubroutineEpilogue(String subName, long localsSize) {
		Logger.log("End of " + subName);
		addLabel(getReturnLabelForSub(subroutineName));
		// R5 is used as frame pointer, restore it
		addInstruction("POP R5");
		addInstruction("ADD R7, " + Long.toHexString(localsSize) + ", R7");
		addInstruction("RET ");
	}

	public void genReturnVal() {
		addInstruction("POP R6");
		addInstruction("JR " + getReturnLabelForSub(subroutineName));
	}

	public void genReturnVoid() {
		addInstruction("JR " + getReturnLabelForSub(subroutineName));
	}
	
	// TODO gen: allocate + init za int, char, string, array...
	
	public void genLocalsDeallocation() {
		
	}

	public void genStartProgram() {
		addLabel("start");
		addInstruction("MOVE 40000, R7");
		addInstruction("CALL " + getLabelForSub("main"));
		addInstruction("HALT");
	}

	//

	private String tmpLabelName(String base) {
		return "TMP_" + base + "_" + labelCounter++;
	}

	private String getLabelForVariable(String var) {
		return "VAR_" + var;
	}

	private String getLabelForSub(String var) {
		return "F_" + var;
	}

	private String getReturnLabelForSub(String var) {
		return "RET_" + var;
	}

	private boolean signExtended20bitOk(int val) {
		int high12 = (val & 0xfff00000);
		boolean signBit = (val & 0x00010000) != 0;
		int bitCount12 = Integer.bitCount(high12);
		return bitCount12 == 12 && signBit || bitCount12 == 0 && !signBit;

	}
}
