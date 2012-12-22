package hr.unizg.fer.zemris.ppj.maheri.semantics.type;

public class VoidType extends Type {
	public static final VoidType INSTANCE = new VoidType();

	private VoidType() {
	}

	@Override
	public boolean equals(Type t) {
		return this == t;
	}
}