package hr.unizg.fer.zemris.ppj.maheri.lexer.actions;

import hr.unizg.fer.zemris.ppj.maheri.lexer.Action;
import hr.unizg.fer.zemris.ppj.maheri.lexer.Lexer;

import java.io.Serializable;

public class SkipAction implements Action, Serializable {

	@Override
	public void doAction(Lexer lexer) {
		lexer.setStartIndex(lexer.getFinishIndex());
		lexer.setFinishIndex(lexer.getStartIndex()+1);
	}

}
