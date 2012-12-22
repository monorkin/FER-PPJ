package hr.unizg.fer.zemris.ppj.maheri.semantics.type;

public abstract class Type {
	/**
	 * Test if the type described by this class, <code>U</code>, can be
	 * automatically, implicitly converted into the another type <code>V</code>.
	 * <p>
	 * In other words, test if <code>
	 * U ~ V
	 * </code> is true
	 * 
	 * @param v
	 *            describes other class
	 * @return <code>true</code> if conversion can happen, <code>false</code>
	 *         otherwise
	 */
	public boolean canConvertImplicit(Type v) {
		// reflexive relation
		return this == v;
	}

	/**
	 * Test if the type can be explicitly cast into another type (using cast
	 * operator in source file).
	 * 
	 * @param target
	 *            other type
	 * @return <code>true</code> if cast is allowed, <code>false</code>
	 *         otherwise
	 */
	public boolean canConvertExplicit(Type target) {
		/*
		 * Eksplicitne promjene tipa dozvoljene su samo nad vrijednostima
		 * brojevnih tipova, a zadaju se cast operatorom
		 */
		// default: no same as implicit
		return canConvertImplicit(target);
	}

	public abstract boolean equals(Type t);

	public String toString() {
		return this.getClass().getSimpleName();
	}

}