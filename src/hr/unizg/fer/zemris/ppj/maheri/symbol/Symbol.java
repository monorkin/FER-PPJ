package hr.unizg.fer.zemris.ppj.maheri.symbol;

public abstract class Symbol {

	public static final String DOT = "*";
	public static final String ARROW = "->";
	
	private final String value;

	public Symbol(final String value) {
		this.value = value;
	}

	public final String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}

}
