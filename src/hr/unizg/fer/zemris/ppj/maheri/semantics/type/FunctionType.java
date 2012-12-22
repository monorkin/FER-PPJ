package hr.unizg.fer.zemris.ppj.maheri.semantics.type;

public class FunctionType extends Type {
	private final Type returnType;
	private final TypeList parameterTypes;

	public FunctionType(Type returnType, TypeList parameterTypes) {
		this.returnType = returnType;
		this.parameterTypes = parameterTypes;
	}

	public TypeList getParameterTypes() {
		return parameterTypes;
	}

	public Type getReturnType() {
		return returnType;
	}

	@Override
	public boolean canConvertImplicit(Type v) {
		if (v instanceof FunctionType) {
			FunctionType func = (FunctionType) v;
			return returnType.canConvertImplicit(func.returnType)
					&& parameterTypes.canConvertImplicit(func.parameterTypes);
		}
		return false;
	}

	@Override
	public boolean equals(Type t) {
		if (t instanceof FunctionType) {
			FunctionType ft = (FunctionType) t;
			return returnType.equals(ft.returnType) && parameterTypes.equals(ft.parameterTypes);
		}
		return false;
	}
}