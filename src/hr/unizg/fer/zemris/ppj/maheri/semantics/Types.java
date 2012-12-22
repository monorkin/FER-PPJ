package hr.unizg.fer.zemris.ppj.maheri.semantics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

abstract class Type {
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
}

class VoidType extends Type {
	public static final VoidType INSTANCE = new VoidType();

	private VoidType() {
	}

	@Override
	public boolean equals(Type t) {
		return this == t;
	}
}

abstract class PrimitiveType extends Type {
}

abstract class NumericType extends PrimitiveType {
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

		return false;
	}
}

class IntType extends NumericType {
	public static final IntType INSTANCE = new IntType();

	private IntType() {
	}

	@Override
	public boolean equals(Type t) {
		return this == t;
	}
}

class CharType extends NumericType {
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

class ConstType extends PrimitiveType {
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
	public boolean equals(Type t) {
		if (t instanceof ConstType)
			return type.equals(((ConstType) t).type);
		return false;
	}
}

class ArrayType extends Type {
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

class TypeList extends Type {
	private final ArrayList<Type> types;

	@Override
	public boolean canConvertImplicit(Type v) {
		if (this == v)
			return true;
		if (v instanceof TypeList) {
			TypeList tl = (TypeList) v;
			if (types.size() != tl.types.size())
				return false;
			for (int i = 0; i < types.size(); ++i) {
				if (!types.get(i).canConvertImplicit(tl.types.get(i)))
					return false;
			}
			return true;
		}
		return false;
	}

	public TypeList(List<Type> types) {
		this.types = new ArrayList<Type>(types);
	}

	public ArrayList<Type> getTypes() {
		return types;
	}

	@Override
	public boolean equals(Type t) {
		if (t instanceof TypeList) {
			TypeList list = (TypeList) t;
			if (types.size() != list.types.size())
				return false;
			for (int i = 0; i < types.size(); ++i)
				if (!types.get(i).equals(list.types.get(i)))
					return false;
			return true;
		}
		return false;
	}
}

class FunctionType extends Type {
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

public class Types {
	private Type[] typesToTest = {

	CharType.INSTANCE, new ConstType(CharType.INSTANCE), IntType.INSTANCE, new ConstType(IntType.INSTANCE),
			new ArrayType(IntType.INSTANCE), new ArrayType(new ConstType(IntType.INSTANCE)),
			new ArrayType(CharType.INSTANCE), new ArrayType(new ConstType(CharType.INSTANCE))

	};

	List<String> names = Arrays.asList("char", "const char", "int", "const int", "int[]", "const int[]", "char[]",
			"const char[]");

	@Test
	public void testImplicitConversions() {

		System.out.print(String.format("%12s\t", "from \\ to"));
		for (int i = 0; i < typesToTest.length; ++i) {
			System.out.print(String.format("%12s\t", names.get(i)));
		}
		System.out.println();

		for (int i = 0; i < typesToTest.length; ++i) {
			System.out.print(String.format("%12s\t", names.get(i)));
			Type from = typesToTest[i];
			for (int j = 0; j < typesToTest.length; ++j) {
				boolean can = from.canConvertImplicit(typesToTest[j]);
				System.out.print(String.format("%12s\t", can ? "~" : ""));
			}
			System.out.println();
		}

		for (int i = 0; i < typesToTest.length; ++i) {
			Assert.assertTrue("reflexive relation", typesToTest[i].canConvertImplicit(typesToTest[i]));
			for (int j = 0; j < typesToTest.length; ++j) {
				boolean ij = typesToTest[i].canConvertImplicit(typesToTest[j]);
				for (int k = 0; k < typesToTest.length; ++k) {
					boolean jk = typesToTest[i].canConvertImplicit(typesToTest[k]);
					boolean ik = typesToTest[i].canConvertImplicit(typesToTest[k]);
					if (ij && jk)
						Assert.assertTrue("transitive relation", ik);
				}
			}
		}
	}
	
	@Test
	public void testExplicitConversions() {
		for (int i = 0; i < typesToTest.length; ++i) {
			for (int j = 0; j < typesToTest.length; ++j) {
				boolean canImplicit = typesToTest[i].canConvertImplicit(typesToTest[j]);
				boolean canExplicit = typesToTest[i].canConvertExplicit(typesToTest[j]);
				
				String castString = names.get(i) + " -> " + names.get(j);
				if (canImplicit) {
					Assert.assertTrue(castString, canExplicit);
				}
				if (canExplicit && !canImplicit)
					System.out.println("Explicit-only casting: " + castString);
			}
		}
	}
}