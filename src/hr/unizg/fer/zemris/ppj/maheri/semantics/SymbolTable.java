package hr.unizg.fer.zemris.ppj.maheri.semantics;

import hr.unizg.fer.zemris.ppj.maheri.Logger;
import hr.unizg.fer.zemris.ppj.maheri.semantics.type.NumericType;
import hr.unizg.fer.zemris.ppj.maheri.semantics.type.Type;

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
	public static SymbolTable GLOBAL = new SymbolTable(null);
	
	public static void resetAll () {
		SymbolTable.GLOBAL = new SymbolTable(null);
	}

	private final HashMap<String, SymbolEntry> map = new HashMap<String, SymbolEntry>();
	private final SymbolTable parentScope;
	
	private final StorageInfo storageInfo;
	
	public StorageInfo getStorageInfo() {
		return storageInfo;
	}

	private Type returnType = null;

	private final List<SymbolTable> nested = new ArrayList<SymbolTable>();


	protected Set<Entry<String, SymbolEntry>> getEntries() {
		return Collections.unmodifiableSet(map.entrySet());
	}

	protected List<SymbolTable> getNested() {
		return Collections.unmodifiableList(nested);
	}

	/**
	 * @return the returnType
	 */
	public Type getReturnType() {
		return returnType;
	}

	/**
	 * @param returnType
	 *            the returnType to set
	 */
	public void setReturnType(Type returnType) {
		this.returnType = returnType;
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
		if (parentScope != null)
			this.returnType = parentScope.returnType;
		
		// size starts at 0
		this.storageInfo = new StorageInfo(parentScope, 0);
	}

	/**
	 * Create a nested symbol table (for nesting scopes)
	 * 
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
		
		if (!data.isParameter()) {
			this.storageInfo.size += data.storageInfo.size;
		} else {
			Logger.log("parameters not handled yet in table...");
		}
	}

	/**
	 * Class which stores info about symbols in table.
	 * 
	 * @author dosvald
	 */
	public static class SymbolEntry {
		// add extra data for each symbol ?
		private final Type type;

		public SymbolEntry(Type symbolType, StorageInfo info, boolean parameter) {
			this.type = symbolType;
			this.storageInfo = info;
			this.parameter = parameter;
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
		
		@Override
		public String toString() {
			return this.type.toString();
		}
		
		private final StorageInfo storageInfo;
		public  StorageInfo getStorageInfo() {
			return storageInfo;
		}
		
		private final boolean parameter;
		public boolean isParameter() {
			return parameter;
		}
		
	}
	
	public static class StorageInfo {
		public static final int LOCAL = 0;
		public static final int GLOBAL = 1;
		
		private final int type;
		
		public int getType() {
			return type;
		}

		public StorageInfo(SymbolTable parent, int size) {
			if (parent == null || parent == SymbolTable.GLOBAL) {
				this.type = GLOBAL;
			} else {
				this.type = LOCAL;
			}
			if (type == LOCAL) {
				this.offset = parent.storageInfo.offset + parent.storageInfo.size + 4;
			} else {
				this.offset = 0;
			}
			this.size = size;
		}
		
		private final int offset;
		/**
		 * byte offset for storage. for local vars, offset is positive, for parameters it is negative.
		 * @return the offset as explained above
		 */
		public int getOffset() {
			if (type == GLOBAL)
				throw new IllegalStateException("offset no applicable for globals");
			return offset;
		}
		
		private int size;
	
		public int getSize() {
			return size;
		}
	}

}
