package hr.unizg.fer.zemris.ppj.maheri.lexer.actions;

import hr.unizg.fer.zemris.ppj.maheri.lexer.Action;
import hr.unizg.fer.zemris.ppj.maheri.lexer.Lexer;

import java.io.Serializable;

public class SkipAction implements Action, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -483412106813794893L;

	@Override
	public void doAction(Lexer lexer) {
		lexer.setStartIndex(lexer.getFinishIndex()+1);
	}

}
