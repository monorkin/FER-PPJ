package hr.unizg.fer.zemris.ppj.maheri.semantics;

public class SemanticsException extends RuntimeException {
	private final Node errorNode;

	public SemanticsException(String message, Node l) {
		super(message);
		this.errorNode = l;
	}

}
