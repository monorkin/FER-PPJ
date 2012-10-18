package hr.unizg.fer.zemris.ppj.maheri.lexer.actions;

import java.io.Serializable;

import hr.unizg.fer.zemris.ppj.maheri.lexer.Action;
import hr.unizg.fer.zemris.ppj.maheri.lexer.Lexer;

public class ChangeStateAction implements Action, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7355980103150175594L;
	private String newState;
	
	

	/**
	 * @param newState
	 */
	public ChangeStateAction(String newState) {
		this.newState = newState;
	}



	@Override
	public void doAction(Lexer lexer) {
		lexer.setCurrentState(lexer.getLexerStates().get(newState));
	}

}
