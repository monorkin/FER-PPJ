package hr.unizg.fer.zemris.ppj.maheri.semantics;

import java.util.HashMap;

public class SymbolTable {
	private HashMap<String, V> map = new HashMap<String, V>();
	
	public static class SymbolEntry {
	}
	public static class Type {
		public static final Type INTEGER;
		public static final Type CHAR;
		public static final Type ARRAY;
		
		private final boolean isConst;
		
		private Type arrayType(Type enclosing) {
			
		}
		
		public Type(boolean isConst) {
			this.isConst = isConst;
		}
		Type() {
			this.isConst = false;
		}
	}
}
