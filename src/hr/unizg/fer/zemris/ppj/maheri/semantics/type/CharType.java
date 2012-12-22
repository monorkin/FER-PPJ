package hr.unizg.fer.zemris.ppj.maheri.semantics.type;

public class CharType extends NumericType {
	public static final CharType INSTANCE = new CharType();

	private CharType() {
	}

	@Override
	public boolean canConvertImplicit(Type target) {
		/*
		 * sve vrijednosti tipa char mogu se implicitno pretvoriti u vrijednost
		 * tipa int.
		 */
		if (target instanceof PrimitiveType)
			return true;
		return super.canConvertImplicit(target);
	}

	@Override
	public boolean equals(Type t) {
		return this == t;
	}
}