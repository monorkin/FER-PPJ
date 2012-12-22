package hr.unizg.fer.zemris.ppj.maheri.semantics.type;

public class ConstType extends PrimitiveType {
	private final NumericType type;

	public ConstType(NumericType type) {
		this.type = type;
	}

	public PrimitiveType getType() {
		return type;
	}

	@Override
	public boolean canConvertImplicit(Type target) {
		/*
		 * Sve vrijednosti tipa const(T) mogu se implicitno pretvoriti u
		 * vrijednost tipa T
		 */
		return target == type || equals(target) || type.canConvertImplicit(target);
	}

	@Override
	public boolean canConvertExplicit(Type target) {
		if (canConvertImplicit(target))
			return true;
		return type.canConvertExplicit(target);
	}

	@Override
	public boolean equals(Type t) {
		if (t instanceof ConstType)
			return type.equals(((ConstType) t).type);
		return false;
	}
}