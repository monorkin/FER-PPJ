package hr.unizg.fer.zemris.ppj.maheri.semantics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class which stores info about program identifiers (e.g. variable types),
 * needed during semantics check
 * 
 * @author dosvald
 * 
 */
public class SymbolTable {
	public static final SymbolTable GLOBAL = new SymbolTable(null);
	
	private final HashMap<String, SymbolEntry> map = new HashMap<String, SymbolEntry>();
	private final SymbolTable parentScope;
	
	private final List<SymbolTable> nested = new ArrayList<SymbolTable>();
	
	protected Set<Entry<String,SymbolEntry>> getEntries() {
		return Collections.unmodifiableSet(map.entrySet());
	}
	
	protected List<SymbolTable> getNested() {
		return Collections.unmodifiableList(nested);
	}

	/**
	 * Construct symbol table. The parent scope must be specified if table being
	 * constructed is not global.
	 * 
	 * @param parentScope
	 *            symbol table of parent scope, or <code>null</code> if global
	 *            scope
	 */
	private SymbolTable(SymbolTable parentScope) {
		this.parentScope = parentScope;
	}
	
	/**
	 * Create a nested symbol table (for nesting scopes)
	 * @return the created scope
	 */
	public SymbolTable createNested() {
		SymbolTable sub = new SymbolTable(this);
		nested.add(sub);
		return sub;
	}

	/**
	 * Get data about symbol by giving its name, in all scopes. If the symbol is
	 * not defined in local scope, parent scope is searched. If no scope
	 * (including global) contains symbol, <code>null</code> is returned.
	 * 
	 * @param symbolName
	 *            name of symbol
	 * @return entry describing symbol in closest enclosing scope, or
	 *         <code>null</code> if no such symbol
	 */
	public SymbolEntry get(String symbolName) {
		SymbolEntry entry = map.get(symbolName);
		if (entry == null && parentScope != null)
			return parentScope.get(symbolName);
		return entry;
	}

	/**
	 * Get data about symbol by giving its name, in local scope only. If the
	 * symbol is not defined in this scope, <code>null</code> is returned.
	 * 
	 * @param symbolName
	 *            name of symbol
	 * @return entry describing symbol, or <code>null</code> if no such symbol
	 *         in local scope
	 */
	public SymbolEntry getLocal(String symbolName) {
		return map.get(symbolName);
	}

	/**
	 * Add new symbol to current scope. If another symbol of same name exists,
	 * {@link IllegalStateException} is thrown.
	 * 
	 * @param symbolName
	 *            name of symbol to add
	 * @param data
	 *            info about symbol being added
	 * @throws IllegalArgumentException
	 *             if any arguments are null
	 * @throws IllegalStateException
	 *             if symbol exists in current scope
	 */
	public void addLocal(String symbolName, SymbolEntry data) {
		if (symbolName == null || data == null)
			throw new IllegalArgumentException("null arguments");
		if (map.get(symbolName) != null)
			throw new IllegalStateException("symbol exists in current scope");
		map.put(symbolName, data);
	}

	/**
	 * Class which stores info about symbols in table.
	 * 
	 * @author dosvald
	 */
	public static class SymbolEntry {
		// add extra data for each symbol ?
		private final Type type;

		public SymbolEntry(Type symbolType) {
			this.type = symbolType;
		}

		/**
		 * @return type of the symbol
		 */
		public Type getType() {
			return type;
		}

		public boolean isLvalue() {
			/*
			 * Od zavrsnih znakova gramatike, jedino IDN (identifikator) moze
			 * biti l-izraz i to samo ako predstavlja varijablu brojevnog tipa
			 * (char ili int) bez const-kvalifikatora. Identifikator koji
			 * predstavlja funkciju ili niz nije l-izraz.
			 */
			return type instanceof NumericType;
		}

		// used for functions
		private boolean defined = false;

		public void markDefined() {
			this.defined = true;
		}

		/**
		 * @return <code>true</code> if this entry describes a function which
		 *         was defined (as opposed to only declared); <code>false</code>
		 *         if non-function type or function is not defined
		 */
		public boolean isDefined() {
			return defined;
		}
	}

}

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
		// default: no cast allowed
		return false;
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
		if (target instanceof IntType)
			return true;
		return super.canConvertImplicit(target);
	}

	@Override
	public boolean equals(Type t) {
		return this == t;
	}
}

class ConstType extends PrimitiveType {
	private final PrimitiveType type;

	public ConstType(PrimitiveType type) {
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
		return target == type;
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
		if (target instanceof ArrayType) {
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
				if (types.get(i).canConvertImplicit(tl.types.get(i)))
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
