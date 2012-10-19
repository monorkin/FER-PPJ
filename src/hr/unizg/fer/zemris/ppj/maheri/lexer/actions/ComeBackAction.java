package hr.unizg.fer.zemris.ppj.maheri.lexer.actions;

import hr.unizg.fer.zemris.ppj.maheri.lexer.Action;
import hr.unizg.fer.zemris.ppj.maheri.lexer.Lexer;

import java.io.Serializable;

public class ComeBackAction implements Action, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8811556393064340843L;
	
	private int comeBack;
	
	

	/**
	 * @param comeBack
	 */
	public ComeBackAction(int comeBack) {
		this.comeBack = comeBack;
	}



	@Override
	public void doAction(Lexer lexer) {
		lexer.setFinishIndex(lexer.getStartIndex()+comeBack+1);
	}

}
