package hr.unizg.fer.zemris.ppj.maheri.lexer.actions;

import java.io.Serializable;

import hr.unizg.fer.zemris.ppj.maheri.lexer.Action;
import hr.unizg.fer.zemris.ppj.maheri.lexer.Lexer;

public class NewLineAction implements Action, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3245244263709223535L;

	@Override
	public void doAction(Lexer lexer) {
		lexer.incrementLineCount();
	}

}
