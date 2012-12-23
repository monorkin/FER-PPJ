package hr.unizg.fer.zemris.ppj.maheri.semantics.type;

import java.util.ArrayList;
import java.util.List;

public class TypeList extends Type {
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
			for (int i = 0; i < types.size(); ++i) {
				if (!types.get(i).equals(list.types.get(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
}