package hr.unizg.fer.zemris.ppj.maheri.semantics.type;

public class IntType extends NumericType {
	public static final IntType INSTANCE = new IntType();

	private IntType() {
	}

	@Override
	public boolean equals(Type t) {
		return this.getClass().equals(t.getClass());
	}
}