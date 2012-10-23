package hr.unizg.fer.zemris.ppj.maheri.lexer.actions;

import hr.unizg.fer.zemris.ppj.maheri.lexer.Action;
import hr.unizg.fer.zemris.ppj.maheri.lexer.Lexer;

import java.io.Serializable;

public class DeclareClassAction implements Action, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7050188394998221356L;

	private String lexemClass;

	public DeclareClassAction(String lexemClass) {
		this.lexemClass = lexemClass;
	}

	@Override
	public void doAction(Lexer lexer) {
		lexer.getOutput().println(
				lexemClass + " " + lexer.getLineCount() + " "
						+ lexer.getInput().substring(lexer.getStartIndex(), lexer.getFinishIndex() + 1));
		lexer.setStartIndex(lexer.getFinishIndex() + 1);
	}

}
