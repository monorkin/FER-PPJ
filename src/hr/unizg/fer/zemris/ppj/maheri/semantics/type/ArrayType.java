package hr.unizg.fer.zemris.ppj.maheri.semantics.type;

public class ArrayType extends Type {
	private final PrimitiveType elementType;

	public ArrayType(PrimitiveType elementType) {
		this.elementType = elementType;
	}

	public PrimitiveType getElementType() {
		return elementType;
	}

	@Override
	public boolean canConvertImplicit(Type target) {
		/*
		 * vrijednost tipa niz (T ) gdje T nije const-kvalificiran tip moze se
		 * pretvoriti u vrijednost tipa niz (const(T )).
		 */
		if (equals(target))
			return true;
		else if (target instanceof ArrayType) {
			// converting to array
			ArrayType array = (ArrayType) target;
			if (array.elementType instanceof ConstType) {
				// converting to array of const
				ConstType arrayConst = (ConstType) array.elementType;
				if (!(this.elementType instanceof ConstType)) {
					// array of X -> array of Const y if y == x
					return arrayConst.getType() == elementType;
				} else {
					// array of const x -> array of const y;
					return array.elementType == elementType;
				}
			} else {
				// convert to array of nonconst
				// ???
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Type t) {
		if (t instanceof ArrayType)
			return elementType.equals(((ArrayType) t).elementType);
		return false;
	}
}
