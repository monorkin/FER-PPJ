package hr.unizg.fer.zemris.ppj.maheri.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FriscAsmBuilder {
	private List< InstructionData > instr = new ArrayList<InstructionData>();
	private InstructionData next = new InstructionData("");
	
	/**
	 * write label
	 * @param label
	 */
	public void addLabel(String label) {
		next.getLabels().add(label);
	}
	
	/**
	 * add label at instruction of given index
	 * @param label
	 * @param where
	 */
	public void addLabel(String label, int where) {
		if (where == instr.size()) {
			addLabel(label);
		} else {
			instr.get(where).getLabels().add(label);
		}
	}
	
	/**
	 * add next instruction
	 * @param instr
	 */
	public void addInstruction(String body) {
		next.setInstructionBody(body);
		instr.add(next);
		next = new InstructionData("");
	}
	
	/**
	 * @return index of NEXT instruction (the one being added/built)
	 */
	public int getIndex() {
		return instr.size();
	}
	
	static class InstructionData {
		private String instructionBody;
		private List<String> labels = new ArrayList<String>();
		
		public String getInstructionBody() {
			return instructionBody;
		}
		public List<String> getLabels() {
			return labels;
		}
		
		public void setInstructionBody(String instructionBody) {
			this.instructionBody = instructionBody;
		}
		public void setLabels(List<String> labels) {
			this.labels = labels;
		}
		
		public InstructionData(String instructionBody, String... labels) {
			this.instructionBody = instructionBody;
			this.labels.addAll(Arrays.asList(labels));
		}
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (InstructionData i : instr) {
			for (String label : i.getLabels()) {
				builder.append(label + "\n");
			}
			builder.append("\t" + i.getInstructionBody() + "\n");
		}
		return builder.toString();
	}
}
