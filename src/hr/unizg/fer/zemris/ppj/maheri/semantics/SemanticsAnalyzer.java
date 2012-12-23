package hr.unizg.fer.zemris.ppj.maheri.semantics;

import hr.unizg.fer.zemris.ppj.maheri.semantics.Node.Attribute;
import hr.unizg.fer.zemris.ppj.maheri.semantics.SymbolTable.SymbolEntry;
import hr.unizg.fer.zemris.ppj.maheri.semantics.type.ArrayType;
import hr.unizg.fer.zemris.ppj.maheri.semantics.type.CharType;
import hr.unizg.fer.zemris.ppj.maheri.semantics.type.ConstType;
import hr.unizg.fer.zemris.ppj.maheri.semantics.type.FunctionType;
import hr.unizg.fer.zemris.ppj.maheri.semantics.type.IntType;
import hr.unizg.fer.zemris.ppj.maheri.semantics.type.NumericType;
import hr.unizg.fer.zemris.ppj.maheri.semantics.type.PrimitiveType;
import hr.unizg.fer.zemris.ppj.maheri.semantics.type.Type;
import hr.unizg.fer.zemris.ppj.maheri.semantics.type.TypeList;
import hr.unizg.fer.zemris.ppj.maheri.semantics.type.VoidType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class SemanticsAnalyzer {
	private Node generativeTree;
	private Map<String, List<orderedProduction>> productions;
	private PPJCProduction[] productionEnum;
	private StringBuilder output;

	public String getOutput() {
		return output.toString();
	}

	private class orderedProduction {
		String[] production;
		int index;

		/**
		 * @param production
		 * @param index
		 */
		public orderedProduction(String[] production, int index) {
			this.production = production;
			this.index = index;
		}

	}

	public SemanticsAnalyzer(Node tree) {
		SymbolTable.GLOBAL = new SymbolTable(null);
		output = new StringBuilder();
		this.generativeTree = tree;
		Scanner fr = null;
		try {
			fr = new Scanner(new File("produkcije.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String curr = null;
		int index = 0;
		productions = new HashMap<String, List<SemanticsAnalyzer.orderedProduction>>();
		productionEnum = PPJCProduction.values();
		while (fr.hasNextLine()) {
			String ln = fr.nextLine();
			if (ln.equals("\n"))
				continue;
			if (ln.startsWith("\t")) {
				if (productions.get(curr) == null)
					productions.put(curr, new ArrayList<SemanticsAnalyzer.orderedProduction>());
				productions.get(curr).add(new orderedProduction(ln.substring(1).split("\\s+"), index));
				index++;
			} else {
				curr = ln;
			}
		}
		fr.close();
	}

	private String errorString(Node errorNode) {
		if (errorNode.getChildren() == null) {
			return errorNode.toString();
		}
		StringBuilder sb = new StringBuilder(errorNode.toString());
		sb.append(" ::= ");
		if (errorNode.getChildren().isEmpty()) {
			return sb.append("$").toString();
		}
		for (Node c : errorNode.getChildren()) {
			sb.append(c.toString()).append(" ");
			System.err.println(c);
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * Performs an attribute and function check
	 * 
	 * @return call source for easy chaining
	 */
	public SemanticsAnalyzer check() {
		try {
			checkAttributes();
			checkFunctions();
		} catch (SemanticsException e) {
			if (e instanceof SemanticsFunctionException) {
				output.append("funkcija");
			} else if (e instanceof SemanticsMainException) {
				output.append("main");
			} else {
				output.append(errorString(e.getErrorNode()));
			}
			System.err.println(e.getMessage());
		}
		return this;
	}

	private void checkAttributes() throws SemanticsException {
		SymbolTable symbolTable = SymbolTable.GLOBAL;
		checkSubtree(generativeTree, symbolTable);
	}

	private void checkFunctions() throws SemanticsException {
		/*
		 * 4.4.7 Provjere nakon obilaska stabla
		 * 
		 * Konačno, nakon obilaska stabla i provjere svih navedenih semantičkih
		 * pravila, semantički analizator treba provjeriti još dva pravila
		 * 
		 * 1. u programu postoji funkcija imena main i tipa funkcija(void → int)
		 * Ako ovo pravilo nije zadovoljeno, semantički analizator treba na
		 * standardni izlaz ispisati samo main u vlastiti redak i završiti s
		 * radom.
		 * 
		 * 2. svaka funkcija koja je deklarirana bilo gdje u programu (u bilo
		 * kojem djelokrugu) mora biti definirana Ako ovo pravilo nije
		 * zadovoljeno, semantički analizator treba na standardni izlaz ispisati
		 * samo funkcija u vlastiti redak i završiti s radom.
		 */
		// #1 can be checked in symbol table, global scope

		// #2 add entry to symbol table which will be updated on function
		// definition.
		// then check this value
		checkMain();
		checkFunctionsAreDefined(SymbolTable.GLOBAL);
	}

	private void checkMain() {
		SymbolEntry mainEntry = SymbolTable.GLOBAL.get("main");
		if (mainEntry == null)
			throw new SemanticsMainException("main function undeclared", null);
		FunctionType mainType = (FunctionType) mainEntry.getType();
		if (!mainType.getReturnType().equals(IntType.INSTANCE))
			throw new SemanticsMainException("main function must return int", null);
		if (!mainType.getParameterTypes().getTypes().isEmpty())
			throw new SemanticsMainException("main function takes no arguments", null);
	}

	private void checkFunctionsAreDefined(SymbolTable table) {
		for (Entry<String, SymbolEntry> entry : table.getEntries()) {
			SymbolEntry s = entry.getValue();
			String funcName = entry.getKey();
			SymbolEntry globalFunction = SymbolTable.GLOBAL.get(funcName);
			if (s.getType() instanceof FunctionType && (globalFunction == null || !globalFunction.isDefined())) {
				throw new SemanticsFunctionException("Declaration of function which is not defined in global scope", null);
			}
		}
		for (SymbolTable nested : table.getNested())
			checkFunctionsAreDefined(nested);
	}

	private void checkSubtree(Node node, SymbolTable table) throws SemanticsException {
		PPJCProduction production = determineProduction(node);
		List<Node> children = node.getChildren();

		switch (production) {
		// <primarni_izraz> ::= IDN
		case PRIMARNI_IZRAZ_1: {
			TerminalNode idn = (TerminalNode) children.get(0);
			SymbolEntry idnEntry = table.get(idn.getText());

			// 1. IDN.ime je deklarirano
			if (idnEntry == null) {
				throw new SemanticsException("Variable " + idn.getText() + " not declared", node);
			}
			idn.setAttribute(Attribute.TIP, idnEntry.getType());
			idn.setAttribute(Attribute.L_IZRAZ, idnEntry.isLvalue());

			// tip <-- IDN.tip
			// l-izraz <-- IDN.l-izraz
			node.setAttribute(Attribute.TIP, idn.getAttribute(Attribute.TIP));
			node.setAttribute(Attribute.L_IZRAZ, idn.getAttribute(Attribute.L_IZRAZ));
			break;
		}
		// <primarni_izraz> ::= BROJ
		case PRIMARNI_IZRAZ_2: {
			TerminalNode broj = (TerminalNode) children.get(0);

			// 1. vrijednost je u rasponu tipa int
			try {
				int intValue = Integer.parseInt(broj.getText());
				// ovaj int se nigdje ne koristi
				// vjerojatno se ne treba ovdje koristiti, tek u generiranju
				// koda
			} catch (NumberFormatException nf) {
				throw new SemanticsException("Invalid integer constant value " + broj.getText(), node);
			}

			// tip <-- int
			// l-izraz <-- 0
			node.setAttribute(Attribute.TIP, IntType.INSTANCE);
			node.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}
		// <primarni_izraz> ::= ZNAK
		case PRIMARNI_IZRAZ_3: {
			TerminalNode znak = (TerminalNode) children.get(0);

			/*
			 * 1. znak je ispravan tj. ppjC dozvoljava iskljucivo znakove '\t',
			 * '\n', '\0', '\'', '\"' i '\\' sve ostale konstante predstavljaju
			 * gresku Nadalje znak " se moze pojaviti neprefiksiran s istim
			 * znacenjem (pretpostavljam da misle na 'Ide patka preko "Save" i
			 * nosi pismo...' tu nemoramo escapeati
			 * " jer je okruzen sa ' koji imaju vecu "tezinu")
			 */
			String charValue = znak.getText();
			if (charValue.length() > 3) {
				String[] ok = new String[] { "'\\n'", "'\\t'", "'\\0'", "'\\''", "'\\\"'", "'\\\\'" };
				// zasto je tu sve \\x umjesto \x i zasto je sve u '' ?
				// ' ' navodnici sluze jer koliko znam u leksickoj analizi se u
				// ZNAK sprema s navodnicima pa i getText daje s navodnicima
				// backslash treba jer u getText pise BACKSLASH pa onda n, a ne
				// NOVIRED kao jedan znak
				boolean found = false;
				for (String s : ok)
					if (s.equals(charValue))
						found = true;
				if (!found)
					throw new SemanticsException("Invalid character constant value " + znak.getText(), node);
			}

			// tip <-- char
			// l-izraz <-- 0
			node.setAttribute(Attribute.TIP, CharType.INSTANCE);
			node.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}

		// <primarni_izraz> ::= NIZ_ZNAKOVA
		case PRIMARNI_IZRAZ_4: {
			TerminalNode niz = (TerminalNode) children.get(0);

			/*
			 * 1. konstantni niz znakova je ispravan Konstantni znakovni nizovi
			 * (uniformni znak NIZ_ZNAKOVA) su tipa niz(const(char)) i
			 * implicitno zavrsavaju znakom '\0' (kao u C -u). Ispravni
			 * konstantni znakovini nizovi pocinju i zavrsavaju dvostrukim
			 * navodnikom (ovo je osigurano leksickom analizom), i mogu
			 * sadrzavati sve ispisive ASCII znakove, a znakom \ mogu biti
			 * prefiksirani samo znakovi '\t', '\n', '\0', '\'', '\"' i '\\'.
			 * Slicno kao za znakove konstante, leksicka analiza ce propustiti
			 * neke konstantme znakovne nizove koji nisu ispravni, npr. nizove
			 * "\" i "\x"
			 */
			String stringValue = niz.getText();
			boolean esc = false;
			int len = stringValue.length();
			for (int i = 1; i < len - 1; ++i) {
				if (esc) {
					esc = false;
					if (-1 == "nt0'\"\\".indexOf(stringValue.charAt(i))) {
						esc = true;
						break; // nece li ovo prekinuti samo vanjski if ???
						// lol break samo petlje i switcheve dira
					}
				}
				if (stringValue.charAt(i) == '\\') {
					esc = true;
				}
			}
			if (esc == true)
				throw new SemanticsException("Invalid char-array constant value", node);

			// tip <-- niz (const(char))
			// l-izraz <-- 0
			node.setAttribute(Attribute.TIP, new ArrayType(new ConstType(CharType.INSTANCE)));
			node.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}
		// <primarni_izraz> ::= L_ZAGRADA <izraz> D_ZAGRADA
		// (nesto)
		case PRIMARNI_IZRAZ_5: {
			NonterminalNode izraz = (NonterminalNode) children.get(1);

			// 1. provjeri(izraz)
			checkSubtree(izraz, table);

			// tip <-- izraz.tip
			// l-izraz <-- izraz.l-izraz
			node.setAttribute(Attribute.TIP, izraz.getAttribute(Attribute.TIP));
			node.setAttribute(Attribute.L_IZRAZ, izraz.getAttribute(Attribute.L_IZRAZ));
			break;
		}
		// <postfiks_izraz> ::= <primarni_izraz>
		case POSTFIX_IZRAZ_1: {
			NonterminalNode primarniIzraz = (NonterminalNode) children.get(0);

			// 1. provjeri(primarniIzraz)
			checkSubtree(primarniIzraz, table);

			// tip <-- primarniIzraz.tip
			// l-izraz <-- primarniIzraz-l-izraz
			node.setAttribute(Attribute.TIP, primarniIzraz.getAttribute(Attribute.TIP));
			node.setAttribute(Attribute.L_IZRAZ, primarniIzraz.getAttribute(Attribute.L_IZRAZ));
			break;
		}
		// <postfiks_izraz> ::= <postfiks_izraz> L_UGL_ZAGRADA <izraz>
		// D_UGL_ZAGRADA
		// nesto[nesto] - ocekujemo indeksiranje niza
		case POSTFIX_IZRAZ_2: {
			NonterminalNode postfiksIzraz = (NonterminalNode) children.get(0);
			NonterminalNode izraz = (NonterminalNode) children.get(2);

			/*
			 * Izbjegavamo indeksiranje niza kao polje, odnosno: int a[10];
			 * a[1][2]; -- nije validan izraz Niz nemozemo dvostruko
			 * indeksirati, ergo javljamo gresku.
			 */

			// 1. provjeri (<postfiks_izraz>)
			checkSubtree(postfiksIzraz, table);
			Type t = (Type) postfiksIzraz.getAttribute(Attribute.TIP);
			if (!(t instanceof ArrayType)) {
				// 2. <postfiks_izraz>.tip = niz (X )
				throw new SemanticsException("Dereferencing array member of non-array type", node);
			}

			ArrayType nizX = (ArrayType) t;

			// 3. provjeri (<izraz>)
			checkSubtree(izraz, table);

			Type indexType = (Type) izraz.getAttribute(Attribute.TIP);
			if (!indexType.canConvertImplicit(IntType.INSTANCE)) {
				// 4. <izraz>.tip ~ int
				throw new SemanticsException("Non-integer type in array index", node);
			}

			// tip <-- X
			// l-izraz <-- X != const(T )
			node.setAttribute(Attribute.TIP, nizX.getElementType());
			node.setAttribute(Attribute.L_IZRAZ, !(nizX.getElementType() instanceof ConstType));
			break;
		}
		// <postfiks_izraz> ::= <postfiks_izraz> L_ZAGRADA D_ZAGRADA
		// nesto() - ocekujemo funkciju koja nema argumente
		case POSTFIX_IZRAZ_3: {
			NonterminalNode postfiksIzraz = (NonterminalNode) children.get(0);

			// 1. provjeri (<postfiks_izraz>)
			checkSubtree(postfiksIzraz, table);

			// 2. <postfiks_izraz>.tip = funkcija(void --> pov )
			Type t = (Type) postfiksIzraz.getAttribute(Attribute.TIP);
			if (!(t instanceof FunctionType)) {
				throw new SemanticsException("Invalid function call", node);
			}
			FunctionType func = (FunctionType) t;
			// Jeli funkcija tipa VOID?
			if (!func.getParameterTypes().getTypes().isEmpty()) {
				throw new SemanticsException("Function requires arguments", node);
			}

			// tip <-- pov
			// l-izraz <-- 0
			node.setAttribute(Attribute.TIP, func.getReturnType());
			node.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}
		// <postfiks_izraz> ::= <postfiks_izraz> L_ZAGRADA <lista_argumenata>
		// D_ZAGRADA
		// nesto(nesto, nesto, ...) - ocekujemo funkciju koja ima argumente,
		// argumente typecatama u oblik koji je zadan u funkciji
		case POSTFIX_IZRAZ_4: {
			NonterminalNode postfiksIzraz = (NonterminalNode) children.get(0);
			NonterminalNode listaArgumenata = (NonterminalNode) children.get(2);

			// 1. provjeri (<postfiks_izraz>)
			checkSubtree(postfiksIzraz, table);

			// 2. provjeri (<lista_argumenata>)
			checkSubtree(listaArgumenata, table);

			Type t = (Type) postfiksIzraz.getAttribute(Attribute.TIP);
			if (!(t instanceof FunctionType)) {
				throw new SemanticsException("Invalid function call", node);
			}
			FunctionType func = (FunctionType) t;

			TypeList argTypes = (TypeList) listaArgumenata.getAttribute(Attribute.TIPOVI);
			TypeList paramTypes = func.getParameterTypes();

			// 3. <postfiks_izraz>.tip = funkcija(params <-- pov ) i redom po
			// elementima arg-tip iz <lista_argumenata>.tipovi i param-tip iz
			// params vrijedi arg-tip ~ param-tip

			if (!argTypes.canConvertImplicit(paramTypes)) {
				throw new SemanticsException("Incompatible arguments for function call parameters", node);
			}

			node.setAttribute(Attribute.TIP, func.getReturnType());
			node.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}
		// <postfiks_izraz> ::= <postfiks_izraz> OP_INC
		// <postfiks_izraz> ::= <postfiks_izraz> OP_DEC
		case POSTFIX_IZRAZ_5:
		case POSTFIX_IZRAZ_6: {
			NonterminalNode postfiksIzraz = (NonterminalNode) children.get(0);

			/*
			 * I prefiks i postfiks inkrement operatori u dijelu promjene
			 * vrijednosti varijable imaju znacenje kao naredba v = (v.tip)(v +
			 * 1);. Za dekrement operatore znacenje je isto uz zamjenu operatora
			 * zbrajanja s operatorom oduzimanja. Drugim rijecima, moguce je
			 * inkrementirati ili dekrementirati varijable tipova int i char.
			 * Provjera vrijednosti svojstva l-izraz u tocki dva osigurava da se
			 * radi o varijabli bez const- kvalifikatora, a zajedno s drugim
			 * uvjetom osigurava se da se radi o varijabli brojevnog tipa. Vazno
			 * je uociti da rezultat primjene ovih operatora vise nije l-izraz,
			 * nego je vrijednost tipa int.
			 */

			// 1. provjeri (<postfiks_izraz>)
			checkSubtree(postfiksIzraz, table);

			// 2. <postfiks_izraz>.l-izraz = 1 i <postfiks_izraz>.tip <-- int
			Type type = (Type) postfiksIzraz.getAttribute(Attribute.TIP);
			if (!type.canConvertImplicit(IntType.INSTANCE)) {
				throw new SemanticsException("Incrementing/decrementing incompatible type", node);
			}
			boolean lvalue = (Boolean) postfiksIzraz.getAttribute(Attribute.L_IZRAZ);
			if (!lvalue) {
				throw new SemanticsException("Incrementing/decrementing non-lvalue", node);
			}

			// tip <-- int
			// l-izraz <-- 0
			node.setAttribute(Attribute.TIP, IntType.INSTANCE);
			node.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}

		/*
		 * <lista_argumenata> Nezavrsni znak <lista_argumenata> generira listu
		 * argumenata za poziv funkcije, a za razliku od nezavrsnih znakova koji
		 * generiraju izraze, imat ce svojsto tipovi koje predstavlja listu
		 * tipova argumenata, s lijeva na desno.
		 */

		// <lista_argumenata> ::= <izraz_pridruzivanja>
		case LISTA_ARGUMENATA_1: {
			NonterminalNode izrazPridruzivanja = (NonterminalNode) children.get(0);

			/*
			 * Ova produkcija generira krajnje lijevi (moguce i jedini) argument
			 * i postavlja njegov tip kao jedini element liste u svojstvu
			 * tipovi.
			 */

			// 1. provjeri(izrazPridruzivanja)
			checkSubtree(izrazPridruzivanja, table);

			Type type = (Type) izrazPridruzivanja.getAttribute(Attribute.TIP);
			TypeList list = new TypeList(new ArrayList<Type>(Arrays.asList(type)));

			// tipovi <-- [ <izraz_pridruzivanja>.tip ]
			node.setAttribute(Attribute.TIPOVI, list);
			break;
		}
		// <lista_argumenata> ::= <lista_argumenata> ZAREZ <izraz_pridruzivanja>
		case LISTA_ARGUMENATA_2: {
			NonterminalNode listaArgumenata = (NonterminalNode) children.get(0);
			NonterminalNode izrazPridruzivanja = (NonterminalNode) children.get(2);

			/*
			 * Ova produkcija omogucuje nizanje argumenata odvojenih zarezom.
			 * Tip novog ar- gumenta koji je predstavljen nezavrsnim znakom
			 * <izraz_pridruzivanja> dodaje se na desni kraj liste tipova koji
			 * su odredeni za prethodne argumente.
			 */

			// 1. provjeri(<lista_argumenata>)
			checkSubtree(listaArgumenata, table);

			// 2. provjeri(<izraz_pridruzivanja>)
			checkSubtree(izrazPridruzivanja, table);

			Type type = (Type) izrazPridruzivanja.getAttribute(Attribute.TIP);
			TypeList list = (TypeList) listaArgumenata.getAttribute(Attribute.TIPOVI);
			list.getTypes().add(type);

			// tipovi <-- <lista_argumenata>.tipovi + [
			// <izraz_pridruzivanja>.tip ]
			node.setAttribute(Attribute.TIPOVI, list);
			break;
		}

		/*
		 * <unarni_izraz> Nezavrsni znak <unarni_izraz> generira izraze s
		 * opcionalnim prefils unarnim operatorima
		 */

		// unarni_izraz> ::= <postfiks_izraz>
		case UNARNI_IZRAZ_1: {
			NonterminalNode postfiksIzraz = (NonterminalNode) children.get(0);

			// 1. provjeri(postfiksIzraz)
			checkSubtree(postfiksIzraz, table);

			// tip <-- <postfiks_izraz>.tip
			// l-izraz <-- <postfiks_izraz>.l-izraz
			node.setAttribute(Attribute.TIP, postfiksIzraz.getAttribute(Attribute.TIP));
			node.setAttribute(Attribute.L_IZRAZ, postfiksIzraz.getAttribute(Attribute.L_IZRAZ));
			break;
		}

		// <unarni_izraz> ::= (OP_INC | OP_DEC) <unarni_izraz>
		case UNARNI_IZRAZ_2:
		case UNARNI_IZRAZ_3: {
			NonterminalNode unarniIzraz = (NonterminalNode) children.get(1);

			// Prefiks inkrement i dekrement imaju analogna semanticka pravila
			// postfiks inacicama istih operatora

			// 1. provjeri(unarniIzraz)
			checkSubtree(unarniIzraz, table);

			// 2. <unarni_izraz>.l-izraz = 1 i <unarni_izraz>.tip ~ int
			// Falio je kod za tocku 2
			Type type = (Type) unarniIzraz.getAttribute(Attribute.TIP);
			if (!type.canConvertImplicit(IntType.INSTANCE)) {
				throw new SemanticsException("Incrementing/decrementing incompatible type", node);
			}
			boolean lvalue = (Boolean) unarniIzraz.getAttribute(Attribute.L_IZRAZ);
			if (!lvalue) {
				throw new SemanticsException("Incrementing/decrementing non-lvalue", node);
			}

			// tip <-- int
			// l-izraz <-- 0
			node.setAttribute(Attribute.TIP, IntType.INSTANCE);
			node.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}
		// <unarni_izraz> ::= <unarni_operator> <cast_izraz>
		case UNARNI_IZRAZ_4: {
			NonterminalNode castIzraz = (NonterminalNode) children.get(1);

			/*
			 * Unarni operatori primjenjivi su na vrijednosti tipa int sto se
			 * provjerava u tocki 2, a rezultat je opet tipa int. Iako je u
			 * produkciji nezavrsni znak <unarni_operator>, nije potrebno
			 * provjeravati nikakva semanticka pravila u toj grani stabla jer
			 * taj nezavrsni znak jednostavno generira neki od unarnih operatora
			 * (sto je prikazano u nastavku). Konacno, bez obzira na to je li
			 * <cast_izraz> l-izraz ili ne, rezultat primjene unarnog operatora
			 * je samo vrijednost i nije l-izraz. Na primjer, naredba +a = 3; je
			 * semanticki neispravna zbog ovog pravila.
			 */

			// 1. provjeri(cast_izraz)
			checkSubtree(castIzraz, table);

			// 2. cast_izraz.tip ~ int
			Type castType = (Type) castIzraz.getAttribute(Attribute.TIP);

			if (!castType.canConvertImplicit(IntType.INSTANCE)) {
				throw new SemanticsException("Invalid type for unary operand", node);
			}

			// tip <-- int
			// l-izraz <-- 0
			node.setAttribute(Attribute.TIP, IntType.INSTANCE);
			node.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}

		/*
		 * Nezavrsni znak <unarni_operator> generira aritmeticke (PLUS i MINUS),
		 * bitovne (OP_TILDA) i logicke (OP_NEG) prefiks unarne operatore. Kako
		 * u ovim produkcijama u semantickoj analizi ne treba nista provjeriti,
		 * produkcije ovdje nisu navedene.
		 */
		case UNARNI_OPERATOR_1: {
			// u semantickoj analizi ne treba nista provjeriti,
			break;
		}
		case UNARNI_OPERATOR_2: {
			// u semantickoj analizi ne treba nista provjeriti,
			break;
		}
		case UNARNI_OPERATOR_3: {
			// u semantickoj analizi ne treba nista provjeriti,
			break;
		}
		case UNARNI_OPERATOR_4: {
			// u semantickoj analizi ne treba nista provjeriti,
			break;
		}

		// Nezavrsni znak <cast_izraz> generira izraze s opcionalnim cast
		// operatorom.

		// <cast_izraz> ::= <unarni_izraz>
		case CAST_IZRAZ_1: {
			NonterminalNode unarniIzraz = (NonterminalNode) children.get(0);

			// 1. provjeri(unarni_izraz)
			checkSubtree(unarniIzraz, table);

			// tip <-- unarni_izraz.tip
			// l-izraz <-- unarni_izraz.l-izraz
			node.setAttribute(Attribute.TIP, unarniIzraz.getAttribute(Attribute.TIP));
			node.setAttribute(Attribute.L_IZRAZ, unarniIzraz.getAttribute(Attribute.L_IZRAZ));
			break;
		}
		// <cast_izraz> ::= L_ZAGRADA <ime_tipa> D_ZAGRADA <cast_izraz>
		case CAST_IZRAZ_2: {
			NonterminalNode imeTipa = (NonterminalNode) children.get(1);
			NonterminalNode castIzraz = (NonterminalNode) children.get(3);

			// 1. provjeri(imeTipa)
			checkSubtree(imeTipa, table);

			// 2. provjeri(castIzraz)
			checkSubtree(castIzraz, table);

			// 3. <cast_izraz>.tip se moze pretvoriti u <ime_tipa>.tip
			Type from = (Type) castIzraz.getAttribute(Attribute.TIP);
			Type to = (Type) imeTipa.getAttribute(Attribute.TIP);

			if (!from.canConvertExplicit(to)) {
				throw new SemanticsException("Invalid cast " + from + " -> " + to, node);
			}

			// tip <-- ime_tipa.tip
			// l-izraz <-- 0
			// nigdje se ne sprema? cini mi se da fali "l.setAttribute(Att...."
			// pa sam ih dodao dolje
			// OK
			node.setAttribute(Attribute.TIP, imeTipa.getAttribute(Attribute.TIP));
			node.setAttribute(Attribute.L_IZRAZ, false);

			break;
		}

		/*
		 * <ime tipa> Nezavrsni znak <ime_tipa> generira imena opcionalno
		 * const-kvalificiranih brojevnih ti- pova i kljucnu rijec void. U ovim
		 * produkcijama ce se izracunati izvedeno svojstvo tip koje se koristi u
		 * produkcijama gdje se <ime_tipa> pojavljuje s desne strane i dodatno
		 * ce se onemoguciti tip const void (koji je sintaksno ispravan, ali
		 * nema smisla).
		 */

		// <ime_tipa> ::= <specifikator_tipa>
		case IME_TIPA_1: {
			NonterminalNode specifikatorTipa = (NonterminalNode) children.get(0);

			// 1. provjeri(specifikatorTipa)
			checkSubtree(specifikatorTipa, table);

			// tip <-- specifikatorTipa.tip
			node.setAttribute(Attribute.TIP, specifikatorTipa.getAttribute(Attribute.TIP));
			break;
		}
		// <ime_tipa> ::= KR_CONST <specifikator_tipa>
		case IME_TIPA_2: {
			NonterminalNode specifikatorTipa = (NonterminalNode) children.get(1);

			// 1. provjeri(specifikatorTipa)
			checkSubtree(specifikatorTipa, table);

			// 2. specifikatorTipa.tip != void
			Type type = (Type) specifikatorTipa.getAttribute(Attribute.TIP);
			if (type instanceof VoidType) {
				throw new SemanticsException("const void is disallowed", node);
			}

			// tip <-- const(specifikatorTipa.tip)
			node.setAttribute(Attribute.TIP, new ConstType((NumericType) specifikatorTipa.getAttribute(Attribute.TIP)));
			break;
		}

		/*
		 * <specifikator tipa> Nezavrsni znak <specifikator_tipa> generira jedan
		 * od tri zavrsna znaka KR_VOID, KR_CHAR i KR_INT. U semantickoj analizi
		 * cemo iz zavrsnog znaka odrediti vrijednost svoj- stva tip nezavrsnog
		 * znaka, ali u ovim produkcijama ne moze doci do semanticke pogreske.
		 */

		// <specifikator_tipa> ::= KR_VOID
		case SPECIFIKATOR_TIPA_1: {
			// tip <-- void
			node.setAttribute(Attribute.TIP, VoidType.INSTANCE);
			break;
		}
		// <specifikator_tipa> ::= KR_CHAR
		case SPECIFIKATOR_TIPA_2: {
			// tip <-- char
			node.setAttribute(Attribute.TIP, CharType.INSTANCE);
			break;
		}
		// <specifikator_tipa> ::= KR_INT
		case SPECIFIKATOR_TIPA_3: {
			// tip <-- int
			node.setAttribute(Attribute.TIP, IntType.INSTANCE);
			break;
		}

		/*
		 * <multiplikativni izraz> Nezavrsni znak <multiplikativni_izraz>
		 * generira izraze u kojima se opcionalno ko- riste operatori mnozenja,
		 * dijeljenja i ostatka. Struktura produkcija osigurava da sva tri
		 * operatora imaju isti prioritet i to manji od unarnih (prefiks i
		 * postfiks) operatora, a veci od ostalih operatora. Nadalje, lijeva
		 * asocijativnost ovih operatora osigurana je lijevom rekurzijom u
		 * produkcijama. Svi ostali binarni operatori u jeziku (cija pravila su
		 * prikazana kasnije) ostvareni su slicnim produkcijama i provjeravaju
		 * se slicna pravila.
		 */

		// <multiplikativni_izraz> ::= <cast_izraz>
		case MULTIPLIKATIVNI_IZRAZ_1: {
			// 1. provjeri(cast_izraz)
			// tip <-- cast_izraz.tip
			// l-izraz <-- cast_izraz.l-izraz
			checkExpressionUnitProduction(node, table);
			break;
		}
		// <multiplikativni_izraz> ::= <multiplikativni_izraz> (OP_PUTA |
		// OP_DIJELI | OP_MOD) <cast_izraz>
		case MULTIPLIKATIVNI_IZRAZ_2:
		case MULTIPLIKATIVNI_IZRAZ_3:
		case MULTIPLIKATIVNI_IZRAZ_4: {
			// 1. provjeri(<multiplikativni_izraz>)
			// 2. <multiplikativni_izraz>.tip <-- int
			// 3. provjeri(<cast_izraz>)
			// 4. <cast_izraz>.tip ~ int
			// tip <-- int
			// l-izraz <-- 0
			checkIntBinaryOperator(node, table);
			break;
		}

		/*
		 * <aditivni izraz> Nezavrsni znak <aditivni_izraz> generira izraze u
		 * kojima se opcionalno koriste opera- tori zbrajanja i oduzimanja.
		 */

		// <aditivni_izraz> ::= <multiplikativni_izraz>
		case ADITIVNI_IZRAZ_1: {
			// 1. provjeri(<multiplikativni_izraz>)
			// tip <-- <multiplikativni_izraz>.tip
			// l-izraz <-- <multiplikativni_izraz>.l-izraz
			checkExpressionUnitProduction(node, table);
			break;
		}
		// <aditivni_izraz> ::= <aditivni_izraz> (PLUS | MINUS)
		// <multiplikativni_izraz>
		case ADITIVNI_IZRAZ_2:
		case ADITIVNI_IZRAZ_3: {
			// 1. provjeri(<aditivni_izraz>)
			// 2. <aditivni_izraz>.tip <-- int
			// 3. provjeri(<multiplikativni_izraz>)
			// 4. <multiplikativni_izraz>.tip ~ int
			// tip <-- int
			// l-izraz <-- 0
			checkIntBinaryOperator(node, table);
			break;
		}

		/*
		 * <odnosni izraz> Nezavrssni znak <odnosni_izraz> generira izraze u
		 * kojima se opcionalno koriste od- nosni operatori < (uniformni znak
		 * OP_LT), > (uniformni znak OP_GT), <= (uniformni znak OP_LTE) i >=
		 * (uniformni znak OP_GTE).
		 */

		// <odnosni_izraz> ::= <aditivni_izraz>
		case ODNOSNI_IZRAZ_1: {
			// tip <-- <aditivni_izraz>.tip
			// l-izraz <-- <aditivni_izraz>.l-izraz
			// 1. provjeri(<aditivni_izraz>)
			checkExpressionUnitProduction(node, table);
			break;
		}
		// <odnosni_izraz> ::= <odnosni_izraz> (OP_LT | OP_GT | OP_LTE | OP_GTE)
		// <aditivni_izraz>
		case ODNOSNI_IZRAZ_2:
		case ODNOSNI_IZRAZ_3:
		case ODNOSNI_IZRAZ_4:
		case ODNOSNI_IZRAZ_5: {
			// tip <-- int
			// l-izraz <-- 0
			// 1. provjeri(<odnosni_izraz>)
			// 2. <odnosni_izraz>.tip <-- int
			// 3. provjeri(<aditivni_izraz>)
			// 4. <aditivni_izraz>.tip ~ int
			checkIntBinaryOperator(node, table);
			break;
		}

		/*
		 * <jednakosni izraz> Nezavrsni znak <jednakosni_izraz> generira izraze
		 * u kojima se opcionalno koriste jed- nakosni operatori == (uniformni
		 * znak OP_EQ) i != (uniformni znak OP_NEQ).
		 */

		// <jednakosni_izraz> ::= <odnosni_izraz>
		case JEDNAKOSNI_IZRAZ_1: {
			// tip <-- <odnosni_izraz>.tip
			// l-izraz <-- <odnosni_izraz>.l-izraz
			// 1. provjeri(<odnosni_izraz>)
			checkExpressionUnitProduction(node, table);
			break;
		}
		// <jednakosni_izraz> ::= <jednakosni_izraz> (OP_EQ | OP_NEQ)
		// <odnosni_izraz>
		case JEDNAKOSNI_IZRAZ_2:
		case JEDNAKOSNI_IZRAZ_3: {
			// tip <-- int
			// l-izraz <-- 0
			// 1. provjeri(<jednakosni_izraz>)
			// 2. <jednakosni_izraz>.tip <-- int
			// 3. provjeri(<odnosni_izraz>)
			// 4. <odnosni_izraz>.tip ~ int
			checkIntBinaryOperator(node, table);
			break;
		}

		/*
		 * <bin i izraz> Nezavrsni znak <bin_i_izraz> generira izraze u kojima
		 * se opcionalno koristi bitovni operator & (uniformni znak OP_BIN_I).
		 * Bitovni operatori imaju razlicite prioritete pa zato svaki operator
		 * ima pripadni nezavrsni znak.
		 */

		// <bin_i_izraz> ::= <jednakosni_izraz>
		case BIN_I_IZRAZ_1: {
			// tip <-- <jednakosni_izraz>.tip
			// l-izraz <-- <jednakosni_izraz>.l-izraz
			// 1. provjeri(<jednakosni_izraz>)
			checkExpressionUnitProduction(node, table);
			break;
		}
		// <bin_i_izraz> ::= <bin_i_izraz> OP_BIN_I <jednakosni_izraz>
		case BIN_I_IZRAZ_2: {
			// tip <-- int
			// l-izraz <-- 0
			// 1. provjeri(<bin_i_izraz>)
			// 2. <bin_i_izraz>.tip ~ int
			// 3. provjeri(<jednakosni_izraz>)
			// 4. <jednakosni_izraz>.tip ~ int
			checkIntBinaryOperator(node, table);
			break;
		}

		/*
		 * <bin xili izraz> Nezavrsni znak <bin_xili_izraz> generira izraze u
		 * kojima se opcionalno koristi bitovni operator ^ (uniformni znak
		 * OP_BIN_XILI)
		 */

		// <bin_xili_izraz> ::= <bin_i_izraz>
		case BIN_XILI_IZRAZ_1: {
			// tip <-- <bin_i_izraz>.tip
			// l-izraz <-- <bin_i_izraz>.l-izraz
			// 1. provjeri(<bin_i_izraz>)
			checkExpressionUnitProduction(node, table);
			break;
		}
		// <bin_xili_izraz> ::= <bin_xili_izraz> OP_BIN_XILI <bin_i_izraz>
		case BIN_XILI_IZRAZ_2: {
			// tip <-- int
			// l-izraz <-- 0
			// 1. provjeri(<bin_xili_izraz>)
			// 2. <bin_xili_izraz>.tip ~ int
			// 3. provjeri(<bin_i_izraz>)
			// 4. <bin_i_izraz>.tip ~ int
			checkIntBinaryOperator(node, table);
			break;
		}

		/*
		 * <bin ili izraz> Nezavrsni znak <bin_ili_izraz> generira izraze u
		 * kojima se opcionalno koristi bitovni operator | (uniformni znak
		 * OP_BIN_ILI).
		 */

		// <bin_ili_izraz> ::= <bin_xili_izraz>
		case BIN_ILI_IZRAZ_1: {
			// tip <-- <bin_xili_izraz>.tip
			// l-izraz <-- <bin_xili_izraz>.l-izraz
			// 1. provjeri(<bin_xili_izraz>)
			checkExpressionUnitProduction(node, table);
			break;
		}
		// <bin_ili_izraz> ::= <bin_ili_izraz> OP_BIN_ILI <bin_xili_izraz>
		case BIN_ILI_IZRAZ_2: {
			// tip <-- int
			// l-izraz <-- 0
			// 1. provjeri(<bin_ili_izraz>)
			// 2. <bin_ili_izraz>.tip  int
			// 3. provjeri(<bin_xili_izraz>)
			// 4. <bin_xili_izraz>.tip  int
			checkIntBinaryOperator(node, table);
			break;
		}

		/*
		 * <log i izraz> Nezavrsni znak <log_i_izraz> generira izraze u kojima
		 * se opcionalno koristi logicki ope- rator konjunkcije && (uniformni
		 * znak OP_I). Slicno kao za bitovne operatore, kako logicki operatori
		 * imaju razlicite prioritete svakom je pridruzen vlastiti nezavrsni
		 * znak.
		 */
		// <log_i_izraz> ::= <bin_ili_izraz>
		case LOG_I_IZRAZ_1: {
			// tip <-- <bin_ili_izraz>.tip
			// l-izraz <-- <bin_ili_izraz>.l-izraz
			// 1. provjeri(<bin_ili_izraz>)
			checkExpressionUnitProduction(node, table);
			break;
		}
		// <log_i_izraz> ::= <log_i_izraz> OP_I <bin_ili_izraz>
		case LOG_I_IZRAZ_2: {
			// tip <-- int
			// l-izraz <-- 0
			// 1. provjeri(<log_i_izraz>)
			// 2. <log_i_izraz>.tip ~ int
			// 3. provjeri(<bin_ili_izraz>)
			// 4. <bin_ili_izraz>.tip ~ int
			checkIntBinaryOperator(node, table);
			break;
		}

		/*
		 * <log ili izraz> Nezavrsni znak <log_ili_izraz> generira izraze u
		 * kojima se opcionalno koristi logicki operator disjunkcije ||
		 * (uniformni znak OP_ILI).
		 */
		// <log_ili_izraz> ::= <log_i_izraz>
		case LOG_ILI_IZRAZ_1: {
			// tip <-- <log_i_izraz>.tip
			// l-izraz <-- <log_i_izraz>.l-izraz
			// 1. provjeri(<log_i_izraz>)
			checkExpressionUnitProduction(node, table);
			break;
		}
		// <log_ili_izraz> ::= <log_ili_izraz> OP_ILI <log_i_izraz>
		case LOG_ILI_IZRAZ_2: {
			// tip <-- int
			// l-izraz <-- 0
			// 1. provjeri(<log_ili_izraz>)
			// 2. <log_ili_izraz>.tip ~ int
			// 3. provjeri(<log_i_izraz>)
			// 4. <log_i_izraz>.tip ~ int
			checkIntBinaryOperator(node, table);
			break;
		}

		/*
		 * <izraz pridruzivanja> Nezavrsni znak <izraz_pridruzivanja> generira
		 * izraze u kojima se neka vrijednost opci- onalno pridruzuje varijabli
		 * koristeci operator pridruzivanja = (uniformni znak OP_PRIDRUZI). Za
		 * razliku od prethodno prikazanih binarnih operatora, operator
		 * pridruzivanja je desno asocijativan sto je u gramatici osigurano
		 * primjenom desne rekurzije. Desna asocijativnost omogucuje nizanje
		 * pridruzivanja, npr. a = b = c = 42;.
		 */
		// <izraz_pridruzivanja> ::= <log_ili_izraz>
		case IZRAZ_PRIDRUZIVANJA_1: {
			// tip <log_ili_izraz>.tip
			// l-izraz <log_ili_izraz>.l-izraz
			// 1. provjeri(<log_ili_izraz>)
			checkExpressionUnitProduction(node, table);
			break;
		}
		// <izraz_pridruzivanja> ::= <postfiks_izraz> OP_PRIDRUZI
		// <izraz_pridruzivanja>
		case IZRAZ_PRIDRUZIVANJA_2: {
			NonterminalNode postfiksIzraz = (NonterminalNode) children.get(0);
			NonterminalNode izrazPridruzivanja = (NonterminalNode) children.get(2);

			// 1. provjeri(<postfiks_izraz>)
			checkSubtree(postfiksIzraz, table);

			boolean lvalue = (Boolean) postfiksIzraz.getAttribute(Attribute.L_IZRAZ);
			if (!lvalue)
				// 2. <postfiks_izraz>.l-izraz = 1
				throw new SemanticsException("Non-lvalue assignment", node);

			// 3. provjeri(<izraz_pridruzivanja>)
			checkSubtree(izrazPridruzivanja, table);

			Type rhsType = (Type) izrazPridruzivanja.getAttribute(Attribute.TIP);
			Type lhsType = (Type) postfiksIzraz.getAttribute(Attribute.TIP);
			if (!rhsType.canConvertImplicit(lhsType))
				// 4. <izraz_pridruzivanja>.tip ~ <postfiks_izraz>.tip
				throw new SemanticsException("Incompatible types in assignment", node);

			// tip <-- <postfiks_izraz>.tip
			// l-izraz <-- 0
			node.setAttribute(Attribute.TIP, postfiksIzraz.getAttribute(Attribute.TIP));
			node.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}

		/*
		 * <izraz> Nezavrsni znak <izraz> omogucuje opcionalno nizanje izraza
		 * koristeci operator , (unifor- mni znak ZAREZ). Vrijednost takvog
		 * slozenog izraza jednaka je vrijednosti krajnje desnog izraza u nizu.
		 */

		// <izraz> ::= <izraz_pridruzivanja>
		case IZRAZ_1: {
			// tip <-- <izraz_pridruzivanja>.tip
			// l-izraz <-- <izraz_pridruzivanja>.l-izraz
			// 1. provjeri(<izraz_pridruzivanja>)
			checkExpressionUnitProduction(node, table);
			break;
		}
		// <izraz> ::= <izraz> ZAREZ <izraz_pridruzivanja>
		case IZRAZ_2: {
			NonterminalNode izraz = (NonterminalNode) children.get(0);
			NonterminalNode izrazPridruzivanja = (NonterminalNode) children.get(2);

			// 1. provjeri(<izraz>)
			checkSubtree(izraz, table);

			// 2. provjeri(<izraz_pridruzivanja>)
			checkSubtree(izrazPridruzivanja, table);

			// tip <-- <izraz_pridruzivanja>.tip
			// l-izraz <-- 0
			node.setAttribute(Attribute.TIP, izrazPridruzivanja.getAttribute(Attribute.TIP));
			node.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}

		/*
		 * <slozena naredba> Nezavrsni znak <slozena_naredba> predstavlja blok
		 * naredbi koji opcionalno pocinje lis- tom deklaracija. Svaki blok je
		 * odvojeni djelokrug, a nelokalnim imenima se pristupa u ugnijezdujucem
		 * bloku (i potencijalno tako dalje sve do globalnog djelokruga).
		 */

		// <slozena_naredba> ::= L_VIT_ZAGRADA <lista_naredbi> D_VIT_ZAGRADA
		case SLOZENA_NAREDBA_1: {
			NonterminalNode listaNaredbi = (NonterminalNode) children.get(1);

			/*
			 * Ova produkcija generira blok koji nema vlastite deklaracije (ali
			 * neka od naredbi u bloku moze biti novi blok koji ima lokalne
			 * deklaracije).
			 */

			/*
			 * Svaki blok je odvojeni djelokrug, a nelokalnim imenima se
			 * pristupa u ugnijezdujucem bloku
			 */
			// FIXME maybe some attributes need to be forwarded (PETLJA)? Or
			// retrofit into SymbolTable?

			// 1. provjeri(<lista_naredbi>)
			checkSubtree(listaNaredbi, table);
			break;
		}
		// <slozena_naredba> ::= L_VIT_ZAGRADA <lista_deklaracija>
		// <lista_naredbi> D_VIT_ZAGRADA
		case SLOZENA_NAREDBA_2: {
			NonterminalNode listaDeklaracija = (NonterminalNode) children.get(1);
			NonterminalNode listaNaredbi = (NonterminalNode) children.get(2);

			/*
			 * S druge strane, ova produkcija generira blok s lokalnim
			 * deklaracijama. Kao i u jeziku C, deklaracije su dozvoljene samo
			 * na pocetku bloka.
			 */

			/*
			 * Svaki blok je odvojeni djelokrug, a nelokalnim imenima se
			 * pristupa u ugnijezdujucem bloku
			 */
			// FIXME maybe some attributes need to be forwarded (PETLJA)? Or
			// retrofit into SymbolTable?

			// 1. provjeri(<lista_deklaracija>)
			checkSubtree(listaDeklaracija, table);

			// 2. provjeri(<lista_naredbi>)
			checkSubtree(listaNaredbi, table);
			break;
		}

		// <lista naredbi>
		// Nezavrsni znak <lista_naredbi> omogucuje nizanje naredbi u bloku.

		// lista_naredbi> ::= <naredba>
		case LISTA_NAREDBI_1: {
			NonterminalNode naredba = (NonterminalNode) children.get(0);
			// 1. provjeri(<naredba>)
			checkSubtree(naredba, table);
			break;
		}
		// lista_naredbi> ::= <lista_naredbi> <naredba>
		case LISTA_NAREDBI_2: {
			NonterminalNode listaNaredbi = (NonterminalNode) children.get(0);
			NonterminalNode naredba = (NonterminalNode) children.get(1);

			// 1. provjeri(<lista_naredbi>)
			checkSubtree(listaNaredbi, table);

			// 2. provjeri(<naredba>)
			checkSubtree(naredba, table);
			break;
		}

		/*
		 * <naredba> Nezavrsni znak <naredba> generira blokove
		 * (<slozena_naredba>) i razlicite vrste jednos- tavnih naredbi
		 * (<izraz_naredba>, <naredba_grananja>, <naredba_petlje> i
		 * <naredba_skoka>). Kako su sve produkcije jedinicne (s desne strane
		 * imaju jedan nezavrsni znak) i u svim pro- dukcijama se provjeravaju
		 * semanticka pravila na znaku s desne strane, produkcije ovdje nisu
		 * prikazane.
		 */
		// <naredba> ::= <izraz_naredba>
		case NAREDBA_2: {
			NonterminalNode u = (NonterminalNode) children.get(0);
			checkSubtree(u, table);
			break;
		}
		// <naredba> ::= <slozena_naredba> | <naredba_grananja>
		// |<naredba_petlje> | <naredba_skoka>
		case NAREDBA_1:
		case NAREDBA_3:
		case NAREDBA_4:
		case NAREDBA_5: {
			NonterminalNode u = (NonterminalNode) children.get(0);
			checkSubtree(u, table);
			break;
		}

		/*
		 * <izraz naredba> Nezavrsni znak <izraz_naredba> generira opcionalni
		 * izraz i znak ; (uniformni znak TOCKAZAREZ) i predstavlja jednostavnu
		 * naredbu. Za potrebe uvjeta u for-petlji, znaku <izraz_naredba> se
		 * pridjeljuje izvedeno svojstvo tip
		 */
		// <izraz_naredba> ::= TOCKAZAREZ
		case IZRAZ_NAREDBA_1: {

			/*
			 * Ova produkcija generira \praznu naredbu" koja moze biti korisna
			 * kao tijelo petlje koja ne radi nista i slicno. Prazna naredba ce
			 * imati tip int kako bi se mogla koristiti kao uvijek zadovoljen
			 * uvjet u for-petlji (na primjer u kakonskoj beskonacnoj petlji
			 * for(;;)).
			 */

			// tip <-- int
			node.setAttribute(Attribute.TIP, IntType.INSTANCE);
			break;
		}
		// izraz_naredba> ::= <izraz> TOCKAZAREZ
		case IZRAZ_NAREDBA_2: {
			NonterminalNode izraz = (NonterminalNode) children.get(0);

			// Za zadani izraz, svojstvo tip se preuzima od znaka <izraz>.

			// 1. provjeri(<izraz>)
			checkSubtree(izraz, table);

			// tip <-- <izraz>.tip
			node.setAttribute(Attribute.TIP, izraz.getAttribute(Attribute.TIP));

			break;
		}

		/*
		 * <naredba grananja> Nezavrsni znak <naredba_grananja> generira if
		 * naredbu u jeziku.
		 */
		// <naredba_grananja> ::= KR_IF L_ZAGRADA <izraz> D_ZAGRADA <naredba>
		case NAREDBA_GRANANJA_1: {
			NonterminalNode izraz = (NonterminalNode) children.get(2);
			NonterminalNode naredba = (NonterminalNode) children.get(4);

			// Ova produkcija generira if naredbu bez else dijela.

			// 1. provjeri(<izraz>)
			checkSubtree(izraz, table);

			Type izrazType = (Type) izraz.getAttribute(Attribute.TIP);
			if (!izrazType.canConvertImplicit(IntType.INSTANCE))
				// 2. <izraz>.tip ~ int
				throw new SemanticsException("If-condition expression has invalid type", node);

			// 3. provjeri(<naredba>)
			checkSubtree(naredba, table);

			break;
		}
		// <naredba_grananja> ::= KR_IF L_ZAGRADA <izraz> D_ZAGRADA <naredba>
		// KR_ELSE <naredba>
		case NAREDBA_GRANANJA_2: {
			NonterminalNode izraz = (NonterminalNode) children.get(2);
			NonterminalNode naredba1 = (NonterminalNode) children.get(4);
			NonterminalNode naredba2 = (NonterminalNode) children.get(6);

			// 1. provjeri(<izraz>)
			checkSubtree(izraz, table);
			Type izrazType = (Type) izraz.getAttribute(Attribute.TIP);
			if (!izrazType.canConvertImplicit(IntType.INSTANCE))
				// 2. <izraz>.tip ~ int
				throw new SemanticsException("If-condition expression has invalid type", node);

			// 3. provjeri(<naredba>1)
			checkSubtree(naredba1, table);

			// 4. provjeri(<naredba>2)
			checkSubtree(naredba2, table);
			break;
		}

		/*
		 * <naredba petlje> Nezavrsni znak <naredba_petlje> generira while i for
		 * petlje.
		 */
		// <naredba_petlje> ::= KR_WHILE L_ZAGRADA <izraz> D_ZAGRADA <naredba>
		case NAREDBA_PETLJE_1: {
			NonterminalNode izraz = (NonterminalNode) children.get(2);
			NonterminalNode naredba = (NonterminalNode) children.get(4);
			/*
			 * Mark this subtree as part of a loop. If the node is already a
			 * part of a different loop, set this to false in order to prevent
			 * continue / break, as it is illegal in a nested-loop context
			 */
			checkAndApplyLoop(node);

			// 1. provjeri(<izraz>)
			checkSubtree(izraz, table);
			Type izrazType = (Type) izraz.getAttribute(Attribute.TIP);
			if (!izrazType.canConvertImplicit(IntType.INSTANCE))
				// 2. <izraz>.tip ~ int
				throw new SemanticsException("While-loop condition is of invalid type", node);

			// 3. provjeri(<naredba>)
			checkSubtree(naredba, table);
			break;
		}
		// <naredba_petlje> ::= KR_FOR L_ZAGRADA <izraz_naredba>1
		// <izraz_naredba>2 D_ZAGRADA <naredba>
		case NAREDBA_PETLJE_2: {
			NonterminalNode izrazNaredba1 = (NonterminalNode) children.get(2);
			NonterminalNode izrazNaredba2 = (NonterminalNode) children.get(3);
			NonterminalNode naredba = (NonterminalNode) children.get(5);

			checkAndApplyLoop(node);
			// Ova produkcija generira for-petlju bez opcionalnog izraza koji se
			// tipicno koristi za
			// promjenu indeksa petlje dok sljedeca produkcija generira petlju
			// sa tim izrazom.

			// 1. provjeri(<izraz_naredba>1)
			checkSubtree(izrazNaredba1, table);

			// 2. provjeri(<izraz_naredba>2)
			checkSubtree(izrazNaredba2, table);

			Type type = (Type) izrazNaredba2.getAttribute(Attribute.TIP);
			if (!type.canConvertImplicit(IntType.INSTANCE))
				// 3. <izraz_naredba>2.tip ~ int
				throw new SemanticsException("For-loop condition of invalit type", node);

			// 4. provjeri(<naredba>)
			checkSubtree(naredba, table);
			break;
		}
		// <naredba_petlje> ::= KR_FOR L_ZAGRADA <izraz_naredba>1
		// <izraz_naredba>2 <izraz> D_ZAGRADA <naredba>
		case NAREDBA_PETLJE_3: {
			NonterminalNode izrazNaredba1 = (NonterminalNode) children.get(2);
			NonterminalNode izrazNaredba2 = (NonterminalNode) children.get(3);
			NonterminalNode izraz = (NonterminalNode) children.get(4);
			NonterminalNode naredba = (NonterminalNode) children.get(6);

			checkAndApplyLoop(node);

			// 1. provjeri(<izraz_naredba>1)
			checkSubtree(izrazNaredba1, table);

			// 2. provjeri(<izraz_naredba>2)
			checkSubtree(izrazNaredba2, table);

			Type type = (Type) izrazNaredba2.getAttribute(Attribute.TIP);
			if (!type.canConvertImplicit(IntType.INSTANCE))
				// 3. <izraz_naredba>2.tip ~ int
				throw new SemanticsException("For-loop condition of invalid type", node);

			// 4. provjeri(<izraz>)
			checkSubtree(izraz, table);

			// 5. provjeri(<naredba>)
			checkSubtree(naredba, table);
			break;
		}

		// <naredba skoka>
		// Nezavrsni znak <naredba_skoka> generira continue, break i return
		// naredbe.

		// naredba_skoka> ::= (KR_CONTINUE | KR_BREAK) TOCKAZAREZ
		case NAREDBA_SKOKA_1:
		case NAREDBA_SKOKA_2: {

			/*
			 * 1. naredba se nalazi unutar petlje ili unutar bloka koji je
			 * ugnijezden u petlji naredba continue (uniformni znak KR_CONTINUE)
			 * i naredba break (uniformni znak KR_BREAK) dozvoljene su
			 * iskljucivo unutar neke petlje, a imaju isto znacenje kao u jeziku
			 * C.
			 */
			boolean insideLoop;
			try {
				insideLoop = (Boolean) node.getAttribute(Attribute.PETLJA);
			} catch (IllegalArgumentException e) {
				insideLoop = false;
			}
			if (!insideLoop) {
				throw new SemanticsException("break/continue in non-loop scope", node);
			}
			break;
		}
		// <naredba_skoka> ::= KR_RETURN TOCKAZAREZ
		case NAREDBA_SKOKA_3: {
			// 1. naredba se nalazi unutar funkcije tipa funkcija(params ! void)
			// Naredba return bez povratne vrijednosti moze se koristiti jedino
			// u
			// funkcijama koje ne vracaju nista.

			if (!(table.getReturnType().equals(VoidType.INSTANCE)))
				throw new SemanticsException("function must return ", node);

			break;
		}
		// <naredba_skoka> ::= KR_RETURN <izraz> TOCKAZAREZ
		case NAREDBA_SKOKA_4: {
			// 1. provjeri(<izraz>)
			// 2. naredba se nalazi unutar funkcije tipa funkcija(params ! pov)
			// i vrijedi <izraz>.tip  pov
			NonterminalNode izraz = (NonterminalNode) children.get(1);
			checkSubtree(izraz, table);

			if (!((Type) izraz.getAttribute(Attribute.TIP)).canConvertImplicit(table.getReturnType())) {
				throw new SemanticsException("function must return " + table.getReturnType() + ", got "
						+ izraz.getAttribute(Attribute.TIP), node);
			}
			break;
		}

		/*
		 * <prijevodna jedinica> Nezavrsni znak <prijevodna_jedinica> je pocetni
		 * nezavrsni znak gramatike i generira niz nezavrsnih znakova
		 * <vanjska_deklaracija> koji generiraju definicije (i deklaracije) u
		 * globalnom djelokrugu programa.
		 */
		// <prijevodna_jedinica> ::= <vanjska_deklaracija>
		case PRIJEVODNA_JEDINICA_1: {
			NonterminalNode vanjskaDeklaracija = (NonterminalNode) children.get(0);
			// 1. provjeri(<vanjska_deklaracija>)
			checkSubtree(vanjskaDeklaracija, table);
			break;
		}
		// <prijevodna_jedinica> ::= <prijevodna_jedinica> <vanjska_deklaracija>
		case PRIJEVODNA_JEDINICA_2: {
			NonterminalNode prijevodnaJedinica = (NonterminalNode) children.get(0);
			NonterminalNode vanjskaDeklaracija = (NonterminalNode) children.get(1);
			// 1. provjeri(<prijevodna_jedinica>)
			checkSubtree(prijevodnaJedinica, table);
			// 2. provjeri(<vanjska_deklaracija>)
			checkSubtree(vanjskaDeklaracija, table);
			break;
		}

		/*
		 * <vanjska deklaracija> Nezavrsni znak <vanjska_deklaracija> generira
		 * ili definiciju funkcije (znak <definicija_funkcije>) ili deklaraciju
		 * varijable ili funkcije (znak <deklaracija>). Obje produkcije su
		 * jedinicne i u obje se provjeravaju pravila u podstablu kojem je znak
		 * s desne strane korijen.
		 */
		// <vanjska_deklaracija> ::= <definicija_funkcije> | <deklaracija>
		case VANJSKA_DEKLARACIJA_1:
		case VANJSKA_DEKLARACIJA_2: {
			checkSubtree((NonterminalNode) children.get(0), table);
			break;
		}

		// Deklaracije i definicije
		// = magic with symbol table happens

		// <definicija_funkcije> ::= <ime_tipa> IDN L_ZAGRADA KR_VOID D_ZAGRADA
		// <slozena_naredba>
		case DEFINICIJA_FUNKCIJE_1: {
			NonterminalNode imeTipa = (NonterminalNode) children.get(0);
			TerminalNode functionName = (TerminalNode) children.get(1);
			NonterminalNode slozenaNaredba = (NonterminalNode) children.get(5);

			// 1. provjeri (<ime_tipa>)
			checkSubtree(imeTipa, table);

			// 2. <ime_tipa>.tip = const(T )
			if (imeTipa.getAttribute(Attribute.TIP) instanceof ConstType) {
				throw new SemanticsException("Function return type cannot be const-qualified", node);
			}
			Type retType = (Type) imeTipa.getAttribute(Attribute.TIP);
			// if (table != SymbolTable.GLOBAL)
			// throw new IllegalStateException("internal error, scope mixup");
			SymbolEntry functionEntry = SymbolTable.GLOBAL.get(functionName.getText());
			FunctionType fType = new FunctionType(retType, new TypeList(new ArrayList<Type>()));

			if (functionEntry != null) {
				// 3. ne postoji prije definirana funkcija imena IDN.ime
				if (functionEntry.isDefined())
					throw new SemanticsException("Redefinition of function", node);

				/*
				 * 4. ako postoji deklaracija imena IDN.ime u globalnom //
				 * djelokrugu onda je pripadni tip te deklaracije
				 * 
				 * funkcija(void <-- <ime_tipa>.tip)
				 */
				if (!functionEntry.getType().canConvertImplicit(fType))
					throw new SemanticsException("Function definition and declaration differ in prototypes", node);
			} else {
				// 5. zabiljezi definiciju i deklaraciju funkcije
				functionEntry = new SymbolEntry(fType);
				SymbolTable.GLOBAL.addLocal(functionName.getText(), functionEntry);
			}
			functionEntry.markDefined();

			SymbolTable inner = table.createNested();
			inner.setReturnType(retType);

			// 6. provjeri (<slozena_naredba>)
			checkSubtree(slozenaNaredba, inner);

			break;
		}
		// <definicija_funkcije> ::= <ime_tipa> IDN L_ZAGRADA <lista_parametara>
		// D_ZAGRADA <slozena_naredba>
		case DEFINICIJA_FUNKCIJE_2: {
			NonterminalNode imeTipa = (NonterminalNode) children.get(0);
			TerminalNode functionName = (TerminalNode) children.get(1);
			NonterminalNode listaParametara = (NonterminalNode) children.get(3);
			NonterminalNode slozenaNaredba = (NonterminalNode) children.get(5);

			/*
			 * Ova produkcija generira definicije funkcija s listom parametara,
			 * tj. funkcije koje primaju jedan ili vise argumenata. Za tocku 7
			 * je zato vazno osigurati da se prije provjere pravila u tijelu
			 * funkcije u lokalni djelokrug ugrade parametri funkcije.
			 */

			// 1. provjeri (<ime_tipa>)
			checkSubtree(imeTipa, table);

			// 2. <ime_tipa>.tip = const(T )
			if (imeTipa.getAttribute(Attribute.TIP) instanceof ConstType) {
				throw new SemanticsException("Function return type cannot be const-qualified", node);
			}
			Type retType = (Type) imeTipa.getAttribute(Attribute.TIP);
			SymbolEntry functionEntry = SymbolTable.GLOBAL.get(functionName.getText());

			if (functionEntry != null && functionEntry.isDefined()) {
				// 3. ne postoji prije definirana funkcija imena IDN.ime
				if (functionEntry.isDefined())
					throw new SemanticsException("Redefinition of function", node);
			}

			// 4. provjeri (<lista_parametara>)
			checkSubtree(listaParametara, table);

			FunctionType fType = new FunctionType(retType, (TypeList) listaParametara.getAttribute(Attribute.TIPOVI));
			// 5. ako postoji deklaracija imena IDN.ime u globalnom djelokrugu
			// onda
			// je pripadni tip te deklaracije ...
			if (functionEntry != null) {
				if (!functionEntry.getType().canConvertImplicit(fType))
					throw new SemanticsException("Function definition and declaration differ in prototypes", node);
			} else {
				// 6. zabiljezi definiciju i deklaraciju funkcije
				functionEntry = new SymbolEntry(fType);
				SymbolTable.GLOBAL.addLocal(functionName.getText(), functionEntry);
			}
			functionEntry.markDefined();

			// 7. provjeri(<slozena_naredba>) uz parametre funkcije koristeci
			// <lista_parametara>.tipovi
			// i <lista_parametara>.imena.

			SymbolTable inner = table.createNested();
			inner.setReturnType(retType);

			@SuppressWarnings("unchecked")
			List<String> paramNames = (List<String>) listaParametara.getAttribute(Attribute.IMENA);
			List<Type> paramTypes = ((TypeList) listaParametara.getAttribute(Attribute.TIPOVI)).getTypes();

			int size = paramNames.size();
			for (int i = 0; i < size; ++i)
				inner.addLocal(paramNames.get(i), new SymbolEntry(paramTypes.get(i)));

			checkSubtree(slozenaNaredba, inner);

			break;
		}

		/*
		 * <lista parametara> Nezavrsnom znaku <lista_parametara> pridruzit cemo
		 * svojstvo tipovi koje sadrzi listu tipova parametara i svojstvo imena
		 * koje sadrzi imena parametara. Vrijednosti svojstva grade se analogno
		 * kao kod znaka <lista_argumenata>.
		 */

		// <lista_parametara> ::= <deklaracija_parametra>
		case LISTA_PARAMETARA_1: {
			NonterminalNode deklaracijaParametra = (NonterminalNode) children.get(0);

			// 1. provjeri(<deklaracija_parametra>)
			checkSubtree(deklaracijaParametra, table);

			// tipovi <-- [ <deklaracija_parametra>.tip ]
			// imena <-- [ <deklaracija_parametra>.ime ]
			node.setAttribute(Attribute.IMENA,
					new ArrayList<String>(Arrays.asList((String) deklaracijaParametra.getAttribute(Attribute.IME))));
			node.setAttribute(
					Attribute.TIPOVI,
					new TypeList(new ArrayList<Type>(Arrays.asList((Type) deklaracijaParametra
							.getAttribute(Attribute.TIP)))));
			break;
		}
		// <lista_parametara> ::= <lista_parametara> ZAREZ
		// <deklaracija_parametra>
		case LISTA_PARAMETARA_2: {
			NonterminalNode listaParametara = (NonterminalNode) children.get(0);
			NonterminalNode deklaracijaParametra = (NonterminalNode) children.get(2);

			// 1. provjeri(<lista_parametara>)
			checkSubtree(listaParametara, table);

			// 2. provjeri(<deklaracija_parametra>)
			checkSubtree(deklaracijaParametra, table);

			@SuppressWarnings("unchecked")
			List<String> names = (List<String>) listaParametara.getAttribute(Attribute.IMENA);
			TypeList typeList = (TypeList) listaParametara.getAttribute(Attribute.TIPOVI);

			String name = (String) deklaracijaParametra.getAttribute(Attribute.IME);
			Type type = (Type) deklaracijaParametra.getAttribute(Attribute.TIP);

			if (names.contains(name)) {
				// 3. <deklaracija_parametra>.ime ne postoji u
				// <lista_parametara>.imena
				throw new SemanticsException("Duplicate parameter name", node);
			}
			names.add(name);
			typeList.getTypes().add(type);

			// tipovi <-- <lista_parametara>.tipovi + [
			// <deklaracija_parametra>.tip ]
			// imena <-- <lista_parametara>.imena + [
			// <deklaracija_parametra>.ime ]
			node.setAttribute(Attribute.TIPOVI, typeList);
			node.setAttribute(Attribute.IMENA, names);
			break;
		}

		/*
		 * <deklaracija parametra> Nezavrsni znak <deklaracija_parametra> sluzi
		 * za deklaraciju jednog parametra i ima svojstva tip i ime.
		 */

		// <deklaracija_parametra> ::= <ime_tipa> IDN
		case DEKLARACIJA_PARAMETRA_1: {
			NonterminalNode imeTipa = (NonterminalNode) children.get(0);
			TerminalNode idn = (TerminalNode) children.get(1);

			// 1. provjeri(<ime_tipa>)
			checkSubtree(imeTipa, table);

			Type type = (Type) imeTipa.getAttribute(Attribute.TIP);
			if (type instanceof VoidType)
				// 2. <ime_tipa>.tip != void
				throw new SemanticsException("illegal parameter type void", node);

			// tip <-- <ime_tipa>.tip
			// ime <-- IDN.ime
			node.setAttribute(Attribute.TIP, type);
			node.setAttribute(Attribute.IME, idn.getText());
			break;
		}
		// <deklaracija_parametra> ::= <ime_tipa> IDN L_UGL_ZAGRADA
		// D_UGL_ZAGRADA
		case DEKLARACIJA_PARAMETRA_2: {
			NonterminalNode imeTipa = (NonterminalNode) children.get(0);
			TerminalNode idn = (TerminalNode) children.get(1);

			// Ova produkcija generira parametre koji su nizovi.

			// 1. provjeri(<ime_tipa>)
			checkSubtree(imeTipa, table);

			Type type = (Type) imeTipa.getAttribute(Attribute.TIP);
			if (type instanceof VoidType)
				// 2. <ime_tipa>.tip != void
				throw new SemanticsException("illegal parameter type array of void", node);

			// tip <-- niz(<ime_tipa>.tip)
			// ime <-- IDN.ime
			node.setAttribute(Attribute.TIP, new ArrayType((PrimitiveType) type));
			node.setAttribute(Attribute.IME, idn.getText());
			break;
		}

		// <lista deklaracija>
		// Nezavrsni znak <lista_deklaracija> generira jednu ili vise
		// deklaracija na pocetku bloka
		// <lista_deklaracija> ::= <deklaracija>
		case LISTA_DEKLARACIJA_1: {
			// 1. provjeri(<deklaracija>)
			checkSubtree(children.get(0), table);
			break;
		}
		// <lista_deklaracija> ::= <lista_deklaracija> <deklaracija>
		case LISTA_DEKLARACIJA_2: {
			// 1. provjeri(<lista_deklaracija>)
			checkSubtree(children.get(0), table);

			// 2. provjeri(<deklaracija>)
			checkSubtree(children.get(1), table);
			break;
		}

		// <deklaracija>
		// Nezavrsni znak <deklaracija> generira jednu naredbu deklaracije.
		// <deklaracija> ::= <ime_tipa> <lista_init_deklaratora> TOCKAZAREZ
		case DEKLARACIJA_1: {
			NonterminalNode imeTipa = (NonterminalNode) children.get(0);
			NonterminalNode listaInitDeklaratora = (NonterminalNode) children.get(1);

			// 1. provjeri(<ime_tipa>)
			checkSubtree(imeTipa, table);

			// 2. provjeri(<lista_init_deklaratora>) uz nasljedno svojstvo
			// <lista_init_deklaratora>.ntip <ime_tipa>.tip
			Type type = (Type) imeTipa.getAttribute(Attribute.TIP);
			listaInitDeklaratora.setAttribute(Attribute.NTIP, type);
			checkSubtree(listaInitDeklaratora, table);
			break;
		}

		/*
		 * <lista init deklaratora> Nezavrsni znak <lista_init_deklaratora>
		 * generira deklaratore odvojene zarezima. Na primjer, u naredbi int x,
		 * y=3, z=y+1;, znak <lista_init_deklaratora> generira x, y=3, z=y+1 dio
		 * (dakako, generira odgovarajuce uniformne znakove).
		 */

		// <lista_init_deklaratora> ::= <init_deklarator>
		case LISTA_INIT_DEKLARATORA_1: {
			NonterminalNode initDeklarator = (NonterminalNode) children.get(0);

			// 1. provjeri(<init_deklarator>) uz nasljedno svojstvo
			// <init_deklarator>.ntip <lista_init_deklaratora>.ntip
			initDeklarator.setAttribute(Attribute.NTIP, node.getAttribute(Attribute.NTIP));
			checkSubtree(initDeklarator, table);
			break;
		}
		// <lista_init_deklaratora> ::= <lista_init_deklaratora> ZAREZ
		// <init_deklarator>
		case LISTA_INIT_DEKLARATORA_2: {
			NonterminalNode listaInitDeklaratora = (NonterminalNode) children.get(0);
			NonterminalNode initDeklarator = (NonterminalNode) children.get(2);

			// 1. provjeri(<lista_init_deklaratora>2) uz nasljedno svojstvo
			// <lista_init_deklaratora>2.ntip <lista_init_deklaratora>1.ntip
			listaInitDeklaratora.setAttribute(Attribute.NTIP, node.getAttribute(Attribute.NTIP));
			checkSubtree(listaInitDeklaratora, table);

			// 2. provjeri(<init_deklarator>) uz nasljedno svojstvo
			// <init_deklarator>.ntip <lista_init_deklaratora>1.ntip
			initDeklarator.setAttribute(Attribute.NTIP, node.getAttribute(Attribute.NTIP));
			checkSubtree(initDeklarator, table);
			break;
		}

		// <init deklarator>
		// Nezavrsni znak <init_deklarator> generira deklarator s opcionalnim
		// inicijalizatorom.

		// <init deklarator> ::= <izravni_deklarator>
		case INIT_DEKLARATOR_1: {
			NonterminalNode izravniDeklarator = (NonterminalNode) children.get(0);

			// 1. provjeri(<izravni_deklarator>) uz nasljedno svojstvo
			// <izravni_deklarator>.ntip <-- <init_deklarator>.ntip
			izravniDeklarator.setAttribute(Attribute.NTIP, node.getAttribute(Attribute.NTIP));
			checkSubtree(izravniDeklarator, table);

			// 2. <izravni_deklarator>.tip =6 const(T) i
			// <izravni_deklarator>.tip =6 niz(const(T))
			Type type = (Type) izravniDeklarator.getAttribute(Attribute.TIP);
			if (type instanceof ConstType)
				throw new SemanticsException("Const types must have initializer", node);
			if (type instanceof ArrayType) {
				ArrayType arrayType = (ArrayType) type;
				if (arrayType.getElementType() instanceof ConstType)
					throw new SemanticsException("Const types must have initializer", node);
			}
			break;
		}
		// <init_deklarator> ::= <izravni_deklarator> OP_PRIDRUZI
		// <inicijalizator>
		case INIT_DEKLARATOR_2: {
			NonterminalNode izravniDeklarator = (NonterminalNode) children.get(0);

			// 1. provjeri(<izravni_deklarator>) uz nasljedno svojstvo
			// <izravni_deklarator>.ntip <-- <init_deklarator>.ntip
			izravniDeklarator.setAttribute(Attribute.NTIP, node.getAttribute(Attribute.NTIP));
			checkSubtree(izravniDeklarator, table);

			// 2. provjeri(<incijalizator>)
			NonterminalNode inicijalizator = (NonterminalNode) children.get(2);
			checkSubtree(inicijalizator, table);

			/*
			 * 3. ako je <izravni_deklarator>.tip T ili const(T)
			 * <inicijalizator>.tip T inace ako je <izravni_deklarator>.tip
			 * niz(T) ili niz(const(T)) <inicijalizator>.br-elem <--
			 * <izravni_deklarator>.br-elem za svaki U iz
			 * <inicijalizator>.tipovi vrijedi U ~ T inace greska
			 */
			Type type = (Type) izravniDeklarator.getAttribute(Attribute.TIP);

			Type to;
			if (type instanceof PrimitiveType) {
				if (type instanceof ConstType) {
					to = ((ConstType) type).getType();
				} else {
					to = type;
				}
				Type from = (Type) inicijalizator.getAttribute(Attribute.TIP);
				if (!from.canConvertImplicit(to))
					throw new SemanticsException("Incompatible types", node);
			} else if (type instanceof ArrayType) {
				Long arraySize = (Long) izravniDeklarator.getAttribute(Attribute.BR_ELEM);
				Long initializerSize = (Long) inicijalizator.getAttribute(Attribute.BR_ELEM);
				if (initializerSize > arraySize)
					throw new SemanticsException("Initializer too long", node);

				Type elementType = ((ArrayType) type).getElementType();
				if (elementType instanceof ConstType) {
					to = ((ConstType) elementType).getType();
				} else {
					to = elementType;
				}

				TypeList fromTypes = (TypeList) inicijalizator.getAttribute(Attribute.TIPOVI);
				for (Type from : fromTypes.getTypes())
					if (!from.canConvertImplicit(to))
						throw new SemanticsException("Incompatible member type in initializer array", node);
			} else {
				throw new SemanticsException("Invalid type initializer", node);
			}
			break;
		}

		/*
		 * <izravni deklarator> Nezavrsni znak <izravni_deklarator> generira
		 * deklarator varijable ili funkcije. Znak ima nasljedno svojstvo ntip i
		 * izvedeno svojstvo tip u koje se pohranjuje potpuni tip varijable ili
		 * funkcije. Ako se deklarira niz, znak dodatno ima i izvedeno svojstvo
		 * br-elem koje oznacava broj elemenata niza.
		 */

		// <izravni_deklarator> ::= IDN
		case IZRAVNI_DEKLARATOR_1: {
			TerminalNode idn = (TerminalNode) children.get(0);
			Type ntype = (Type) node.getAttribute(Attribute.NTIP);

			/*
			 * Ova produkcija sluzi za generiranje varijabli cjelobrojnog tipa.
			 * Vazno je uociti da je varijabla deklarirana odmah nakon navedenog
			 * identifikatora, a prije opcionalnog inicijalizatora. To znaci da
			 * je inicijalizacija int x = x + 1; semanticki ispravna, ali
			 * rezultat nije definiran jer x na desnoj strani ima neodredenu
			 * vrijednost.
			 */

			// 1. ntip != void
			if (ntype instanceof VoidType)
				throw new SemanticsException("Cannot declare identifier of type void", node);
			// 2. IDN.ime nije deklarirano u lokalnom djelokrugu
			if (table.getLocal(idn.getText()) != null)
				throw new SemanticsException("Redeclaration in local scope", node);

			// 3. zabilježi deklaraciju IDN.ime s odgovarajucim tipom
			table.addLocal(idn.getText(), new SymbolEntry(ntype));

			// tip <-- ntip
			node.setAttribute(Attribute.TIP, ntype);
			break;
		}
		// <izravni_deklarator> ::= IDN L_UGL_ZAGRADA BROJ D_UGL_ZAGRADA
		case IZRAVNI_DEKLARATOR_2: {
			TerminalNode idn = (TerminalNode) children.get(0);
			TerminalNode broj = (TerminalNode) children.get(2);

			/*
			 * Ova produkcija sluzi za deklariranje nizova. Obavezno mora biti
			 * naveden broj ele- menata niza (to je sintaksno osigurano ovom
			 * produkcijom) i taj broj mora biti pozitivan i maksimalnog iznosa
			 * 1024. Sintaksno nije moguce broj elemenata zadati nekakvim
			 * izrazom, cak ni ako se on u cijelosti sastoji od konstanti.
			 */

			Type ntype = (Type) node.getAttribute(Attribute.NTIP);
			Type type = new ArrayType((PrimitiveType) ntype);

			// 1. ntip != void
			if (ntype instanceof VoidType)
				throw new SemanticsException("Cannot declare identifier of type void", node);

			// 2. IDN.ime nije deklarirano u lokalnom djelokrugu
			if (table.getLocal(idn.getText()) != null)
				throw new SemanticsException("Redeclaration in local scope", node);

			// 3. BROJ.vrijednost je pozitivan broj (> 0) ne veci od 1024
			Long size = Long.parseLong(broj.getText());

			// 4. zabiljezi deklaraciju IDN.ime s odgovarajucim tipom
			if (size <= 0 || size > 1024)
				throw new SemanticsException("illegal array size", node);
			table.addLocal(idn.getText(), new SymbolEntry(type));

			// tip <-- niz(ntip)
			// br-elem <-- BROJ.vrijednost
			node.setAttribute(Attribute.TIP, type);
			node.setAttribute(Attribute.BR_ELEM, size);
			break;
		}
		// <izravni_deklarator> ::= IDN L_ZAGRADA KR_VOID D_ZAGRADA
		case IZRAVNI_DEKLARATOR_3: {
			TerminalNode idn = (TerminalNode) children.get(0);
			Type ntype = (Type) node.getAttribute(Attribute.NTIP);
			Type type = new FunctionType(ntype, new TypeList(new ArrayList<Type>()));

			SymbolEntry entry = table.getLocal(idn.getText());
			// 1. ako je IDN.ime deklarirano u lokalnom djelokrugu, tip
			// prethodne deklaracije
			// je jednak funkcija(void --> ntip)
			if (entry != null) {
				Type definedType = entry.getType();
				if (!definedType.equals(type))
					throw new SemanticsFunctionException("redefinition of function with differing prototype", node);
			} else {
				// 2. zabiljezi deklaraciju IDN.ime s odgovarajucim tipom ako
				// ista funkcija vec nije
				// deklarirana u lokalnom djelokrugu
				entry = new SymbolEntry(type);
				table.addLocal(idn.getText(), entry);
			}

			// tip <-- funkcija(void --> ntip)
			node.setAttribute(Attribute.TIP, type);
			break;
		}
		// <izravni_deklarator> ::= IDN L_ZAGRADA <lista_parametara> D_ZAGRADA
		case IZRAVNI_DEKLARATOR_4: {
			TerminalNode idn = (TerminalNode) children.get(0);
			NonterminalNode listaParametara = (NonterminalNode) children.get(2);

			/*
			 * Ova i prethodna produkcija generiraju deklaracije funkcija. Za
			 * razliku od varijabli, funkcije se mogu deklarirati u istom
			 * djelokrugu proizvoljan broj puta (zato jer je deklaracija
			 * varijable ujedno i njena definicija, sto kod funkcija nije
			 * slucaj).
			 */

			// 1. provjeri(<lista_parametara>)
			checkSubtree(listaParametara, table);

			Type ntype = (Type) node.getAttribute(Attribute.NTIP);
			Type type = new FunctionType(ntype, (TypeList) listaParametara.getAttribute(Attribute.TIPOVI));

			// 2. ako je IDN.ime deklarirano u lokalnom djelokrugu, tip
			// prethodne deklaracije
			// je jednak funkcija(<lista_parametara>.tipovi ! ntip)
			SymbolEntry entry = table.getLocal(idn.getText());
			if (entry != null) {
				Type definedType = entry.getType();
				if (!definedType.equals(type))
					throw new SemanticsFunctionException("redefinition of function with differing prototype", node);
			} else {
				// 3. zabiljezi deklaraciju IDN.ime s odgovarajucim tipom ako
				// ista funkcija vec nije
				// deklarirana u lokalnom djelokrugu
				entry = new SymbolEntry(type);
				table.addLocal(idn.getText(), entry);
			}

			// tip <-- funkcija(<lista_parametara>.tipovi --> ntip)
			node.setAttribute(Attribute.TIP, type);
			break;
		}

		// <inicijalizator> ::= <izraz_pridruzivanja>
		case INICIJALIZATOR_1: {
			NonterminalNode izrazPridruzivanja = (NonterminalNode) children.get(0);

			// 1. provjeri(<izraz_pridruzivanja>)
			checkSubtree(izrazPridruzivanja, table);

			Node n = izrazPridruzivanja;
			while (n.getChildren() != null && n.getChildren().size() == 1)
				n = n.getChildren().get(0);

			// ako je <izraz_pridruzivanja> --> NIZ_ZNAKOVA
			if (n instanceof TerminalNode && "NIZ_ZNAKOVA".equals(((TerminalNode) n).getSymbol().getValue())) {
				long c = ((TerminalNode) n).getText().length() - 2 + 1; // 2
																		// quotes,
																		// add
																		// NUL
				// br-elem <-- duljina niza znakova + 1
				node.setAttribute(Attribute.BR_ELEM, c);

				ArrayList<Type> list = new ArrayList<Type>();
				for (int i = 0; i < c; ++i)
					list.add(CharType.INSTANCE);

				// tipovi <-- lista duljine br-elem, svi elementi su char
				node.setAttribute(Attribute.TIPOVI, new TypeList(list));
			} else {
				// tip <-- <izraz_pridruzivanja>.tip
				node.setAttribute(Attribute.TIP, izrazPridruzivanja.getAttribute(Attribute.TIP));
			}
			break;
		}
		// <inicijalizator> ::= L_VIT_ZAGRADA <lista_izraza_pridruzivanja>
		// D_VIT_ZAGRADA
		case INICIJALIZATOR_2: {
			NonterminalNode listaIzrazaPridruzivanja = (NonterminalNode) children.get(1);

			// 1. provjeri(<lista_izraza_pridruzivanja>)
			checkSubtree(listaIzrazaPridruzivanja, table);

			// br-elem <-- <lista_izraza_pridruzivanja>.br-elem
			// tipovi <-- <lista_izraza_pridruzivanja>.tipovi
			node.setAttribute(Attribute.BR_ELEM, listaIzrazaPridruzivanja.getAttribute(Attribute.BR_ELEM));
			node.setAttribute(Attribute.TIPOVI, listaIzrazaPridruzivanja.getAttribute(Attribute.TIPOVI));
			break;
		}

		// <lista_izraza_pridruzivanja> ::= <izraz_pridruzivanja>
		case LISTA_IZRAZA_PRIDRUZIVANJA_1: {
			NonterminalNode izrazPridruzivanja = (NonterminalNode) children.get(0);

			// 1. provjeri(<izraz_pridruzivanja>)
			checkSubtree(izrazPridruzivanja, table);

			// tipovi <-- [ <izraz_pridruzivanja>.tip ]
			// br-elem <-- 1
			node.setAttribute(Attribute.BR_ELEM, 1L);
			node.setAttribute(
					Attribute.TIPOVI,
					new TypeList(new ArrayList<Type>(Arrays.asList((Type) izrazPridruzivanja
							.getAttribute(Attribute.TIP)))));
			break;
		}
		// <lista_izraza_pridruzivanja> ::= <lista_izraza_pridruzivanja> ZAREZ
		// <izraz_pridruzivanja>
		case LISTA_IZRAZA_PRIDRUZIVANJA_2: {
			NonterminalNode listaIzrazaPridruzivanja = (NonterminalNode) children.get(0);
			NonterminalNode izrazPridruzivanja = (NonterminalNode) children.get(2);

			// 1. provjeri(<lista_izraza_pridruzivanja>)
			checkSubtree(listaIzrazaPridruzivanja, table);

			// 2. provjeri(<izraz_pridruzivanja>)
			checkSubtree(izrazPridruzivanja, table);

			Long c = (Long) listaIzrazaPridruzivanja.getAttribute(Attribute.BR_ELEM);
			TypeList list = (TypeList) listaIzrazaPridruzivanja.getAttribute(Attribute.TIPOVI);
			Type type = (Type) izrazPridruzivanja.getAttribute(Attribute.TIP);
			list.getTypes().add(type);

			// tipovi <-- <lista_izraza_pridruzivanja>.tipovi + [
			// <izraz_pridruzivanja>.tip ]
			// br-elem <-- <lista_izraza_pridruzivanja>.br-elem+ 1
			node.setAttribute(Attribute.BR_ELEM, c + 1);
			node.setAttribute(Attribute.TIPOVI, list);
			break;
		}
		}
	}

	/**
	 * Helper function: common case is production for operations or relations
	 * between two elements, where both operands must be int-compatible, and
	 * result is int. This helper function handles that case:
	 * 
	 * <ol>
	 * <li>Left operand is checked recursively</li>
	 * <li>Left operand type is checked to be int-compatible</li>
	 * <li>Right operand is checked recursively</li>
	 * <li>Right operand type is checked to be int-compatible</li>
	 * </ol>
	 * 
	 * <ul>
	 * <li>result type is set to int</li>
	 * <li>result is not lvalue</li>
	 * </ul>
	 * 
	 * @param parent
	 *            node which represents a production as described
	 * @param syms
	 *            symbol table
	 * @throws SemanticsException
	 *             if any checks fail
	 */
	private void checkIntBinaryOperator(Node parent, SymbolTable syms) throws SemanticsException {
		NonterminalNode a = (NonterminalNode) parent.getChildren().get(0);
		NonterminalNode b = (NonterminalNode) parent.getChildren().get(2);
		TerminalNode op = (TerminalNode) parent.getChildren().get(1);

		checkSubtree(a, syms);
		Type aType = (Type) a.getAttribute(Attribute.TIP);
		if (!aType.canConvertImplicit(IntType.INSTANCE))
			throw new SemanticsException("Left operand to '" + op.getText() + "' is of invalid type", parent);
		checkSubtree(b, syms);
		Type bType = (Type) b.getAttribute(Attribute.TIP);
		if (!bType.canConvertImplicit(IntType.INSTANCE))
			throw new SemanticsException("Right operand to '" + op.getText() + "' is of invalid type", parent);

		parent.setAttribute(Attribute.TIP, IntType.INSTANCE);
		parent.setAttribute(Attribute.L_IZRAZ, false);
	}

	/**
	 * Helper function: common case is production for choosing another binary
	 * int operator of higher precedence, which is a unit production, e.g.: <br />
	 * <code>&lt;addition&gt; ::= &lt;multiplication&gt;</code>
	 * 
	 * This function handles that case doing the following.
	 * 
	 * <ol>
	 * <li>Inner operation is checked recursively</li>
	 * </ol>
	 * 
	 * <ul>
	 * <li>result type is set to that of inner operation</li>
	 * <li>result is lvalue iff inner operation is lvalue</li>
	 * </ul>
	 * 
	 * @param op
	 *            node which represents a production as described
	 * @param syms
	 *            symbol table
	 * @throws SemanticsException
	 *             if any checks fail
	 */
	private void checkExpressionUnitProduction(Node op, SymbolTable syms) {
		NonterminalNode innerOp = (NonterminalNode) op.getChildren().get(0);

		checkSubtree(innerOp, syms);

		op.setAttribute(Attribute.TIP, innerOp.getAttribute(Attribute.TIP));
		op.setAttribute(Attribute.L_IZRAZ, innerOp.getAttribute(Attribute.L_IZRAZ));
	}

	private PPJCProduction determineProduction(Node node) {
		// System.err.println("TRAŽIM PRODUKCIJU ZA NEZAVRŠNI: " +
		// node.getSymbol());

		List<Node> children = node.getChildren();

		List<orderedProduction> prods = productions.get(node.getSymbol().toString());

		if (prods == null)
			throw new SemanticsException("NEMA PRODUKCIJE HURR", node);

		for (orderedProduction o : prods) {
			boolean matchFound = true;
			if (o.production.length != children.size())
				continue;
			for (int i = 0; i < children.size(); i++) {
				if (!o.production[i].equals(children.get(i).getSymbol().toString())) {
					matchFound = false;
					break;
				}
			}
			if (matchFound) {
				// System.err.println("NAŠO PRODUKCIJU:" +
				// productionEnum[o.index]);
				return productionEnum[o.index];
			}
		}
		throw new SemanticsException("OPET NEMA PRODUKCIJE JEBIGA", node);
	}

	private void checkAndApplyLoop(Node node) {
		node.setAttribute(Attribute.PETLJA, true, true);
	}

}
