package hr.unizg.fer.zemris.ppj.maheri.semantics.type;

public abstract class NumericType extends PrimitiveType {
	@Override
	public boolean canConvertImplicit(Type target) {
		/*
		 * sve vrijednosti tipa int ili char mogu se implicitno pretvoriti u
		 * vrijednosti odgovarajucg const-kvalificiranog tipa
		 */
		if (target instanceof ConstType) {
			ConstType constType = (ConstType) target;
			return constType.getType() == this;
		}
		return super.canConvertImplicit(target);
	}

	@Override
	public boolean canConvertExplicit(Type target) {
		if (canConvertImplicit(target))
			return true;
		/*
		 * Eksplicitne promjene tipa dozvoljene su samo nad vrijednostima
		 * brojevnih tipova, a zadaju se cast operatorom. Drugim rijecima,
		 * jedina promjena tipa koju je moguce ostvariti samo eksplicitno je
		 * promjena iz vrijednosti tipa int u vrijednost tipa char
		 */
		if (target instanceof NumericType)
			return true;
		if (target instanceof ConstType) {
			ConstType constType = (ConstType) target;
			if (constType.getType() instanceof NumericType)
				return true;
		}

		return false;
	}
}
