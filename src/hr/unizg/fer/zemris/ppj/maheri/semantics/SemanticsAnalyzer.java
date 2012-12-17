package hr.unizg.fer.zemris.ppj.maheri.semantics;

import static hr.unizg.fer.zemris.ppj.maheri.semantics.Node.Attribute;

import hr.unizg.fer.zemris.ppj.maheri.semantics.SymbolTable.SymbolEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SemanticsAnalyzer {
	private Node generativeTree;

	public SemanticsAnalyzer(Node tree) {
		this.generativeTree = tree;
	}

	private static String errorString(Node errorNode) {
		if (errorNode.getChildren() == null) {
			return errorNode.toString();
		}
		StringBuilder sb = new StringBuilder(errorNode.toString());
		sb.append(" ::= ");
		if (errorNode.getChildren().isEmpty()) {
			return sb.append("$").toString();
		}
		for (Node c : errorNode.getChildren())
			sb.append(c.toString()).append(" ");
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	public void checkAttributes() {
		SymbolTable symbolTable = SymbolTable.GLOBAL;
		try {
			check(generativeTree, symbolTable);
		} catch (SemanticsException e) {
			System.out.println(errorString(e.getErrorNode()));
			System.err.println(e.getMessage());
		}
	}

	public void checkFunctions() {
		/*
		 * TODO 4.4.7 Provjere nakon obilaska stabla
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
		try {
			SymbolEntry mainEntry = SymbolTable.GLOBAL.get("main");
			if (mainEntry == null)
				throw new SemanticsException("main function undeclared", null);
			FunctionType mainType = (FunctionType) mainEntry.getType();
			if (!mainType.getReturnType().equals(IntType.INSTANCE))
				throw new SemanticsException("main function must return int", null);
			if (!mainType.getParameterTypes().getTypes().isEmpty())
				throw new SemanticsException("main function takes no arguments", null);
		} catch (SemanticsException e) {
			System.out.println("main");
			System.err.println(e.getMessage());
		}
		
		try {
			
		} catch (SemanticsException e) {
			
		}
	}

	private static void check(Node l, SymbolTable table) throws SemanticsException {
		PPJCProduction production = determineProduction(l);
		List<Node> r = l.getChildren();

		switch (production) {
		// <primarni_izraz> ::= IDN
		case PRIMARNI_IZRAZ_1: {
			TerminalNode idn = (TerminalNode) r.get(0);
			SymbolEntry idnEntry = table.get(idn.getText());

			// 1. IDN.ime je deklarirano
			if (idnEntry == null) {
				throw new SemanticsException("Variable " + idn.getText() + " not declared", l);
			}
			idn.setAttribute(Attribute.TIP, idnEntry.getType());
			idn.setAttribute(Attribute.L_IZRAZ, idnEntry.isLvalue());

			// tip ← IDN.tip
			// l-izraz ← IDN.l-izraz
			l.setAttribute(Attribute.TIP, idn.getAttribute(Attribute.TIP));
			l.setAttribute(Attribute.L_IZRAZ, idn.getAttribute(Attribute.L_IZRAZ));
			break;
		}
		// <primarni_izraz> ::= BROJ
		case PRIMARNI_IZRAZ_2: {
			TerminalNode broj = (TerminalNode) r.get(0);

			// 1. vrijednost je u rasponu tipa int
			try {
				int intValue = Integer.parseInt(broj.getText());
			} catch (NumberFormatException nf) {
				throw new SemanticsException("Invalid integer constant value " + broj.getText(), l);
			}

			// tip ← int
			// l-izraz ← 0
			l.setAttribute(Attribute.TIP, IntType.INSTANCE);
			l.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}
		// <primarni_izraz> ::= ZNAK
		case PRIMARNI_IZRAZ_3: {
			TerminalNode znak = (TerminalNode) r.get(0);

			String charValue = znak.getText();
			if (charValue.length() > 3) {
				String[] ok = new String[] { "'\\n'", "'\\t'", "'\\0'", "'\\''", "'\\\"'", "'\\\\'" };
				boolean found = false;
				for (String s : ok)
					if (s.equals(charValue))
						found = true;
				if (!found)
					throw new SemanticsException("Invalid character constant value " + znak.getText(), l);
			}

			// tip ← char
			// l-izraz ← 0
			l.setAttribute(Attribute.TIP, CharType.INSTANCE);
			l.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}

		// <primarni_izraz> ::= NIZ_ZNAKOVA
		case PRIMARNI_IZRAZ_4: {
			TerminalNode niz = (TerminalNode) r.get(0);

			String stringValue = niz.getText();
			boolean esc = false;
			int len = stringValue.length();
			for (int i = 1; i < len - 1; ++i) {
				if (esc) {
					esc = false;
					if (-1 == "nt0'\"\\".indexOf(stringValue.charAt(i))) {
						esc = true;
						break;
					}
				}
				if (stringValue.charAt(i) == '\\') {
					esc = true;
				}
			}
			if (esc = true)
				throw new SemanticsException("Invalid char-array constant value", l);

			// tip ← niz (const(char))
			// l-izraz ← 0
			l.setAttribute(Attribute.TIP, new ArrayType(new ConstType(CharType.INSTANCE)));
			l.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}
		// <primarni_izraz> ::= L_ZAGRADA <izraz> D_ZAGRADA
		case PRIMARNI_IZRAZ_5: {
			NonterminalNode izraz = (NonterminalNode) r.get(1);

			// provjeri(izraz)
			check(izraz, table);

			// tip ← izraz.tip
			// l-izraz ← izraz.l-izraz
			l.setAttribute(Attribute.TIP, izraz.getAttribute(Attribute.TIP));
			l.setAttribute(Attribute.L_IZRAZ, izraz.getAttribute(Attribute.L_IZRAZ));
			break;
		}

		// <postfiks_izraz> ::= <primarni_izraz>
		case POSTFIX_IZRAZ_1: {
			NonterminalNode primarniIzraz = (NonterminalNode) r.get(0);

			check(primarniIzraz, table);

			l.setAttribute(Attribute.TIP, primarniIzraz.getAttribute(Attribute.TIP));
			l.setAttribute(Attribute.L_IZRAZ, primarniIzraz.getAttribute(Attribute.L_IZRAZ));
			break;
		}
		// <postfiks_izraz> ::= <postfiks_izraz> L_UGL_ZAGRADA <izraz>
		// D_UGL_ZAGRADA
		case POSTFIX_IZRAZ_2: {
			NonterminalNode postfiksIzraz = (NonterminalNode) r.get(0);
			NonterminalNode izraz = (NonterminalNode) r.get(2);

			// 1. provjeri (<postfiks_izraz>)
			check(postfiksIzraz, table);
			Type t = (Type) postfiksIzraz.getAttribute(Attribute.TIP);
			if (!(t instanceof ArrayType)) {
				// 2. <postfiks_izraz>.tip = niz (X )
				throw new SemanticsException("Dereferencing array member of non-array type", l);
			}

			ArrayType nizX = (ArrayType) t;
			// 3. provjeri (<izraz>)
			check(izraz, table);

			Type indexType = (Type) izraz.getAttribute(Attribute.TIP);
			if (!indexType.canConvertImplicit(IntType.INSTANCE)) {
				// 4. <izraz>.tip ~ int
				throw new SemanticsException("Non-integer type in array index", l);
			}

			// tip ← X
			// l-izraz ← X != const(T )
			l.setAttribute(Attribute.TIP, nizX.getElementType());
			l.setAttribute(Attribute.L_IZRAZ, !(nizX.getElementType() instanceof ConstType));
			break;
		}
		// <postfiks_izraz> ::= <postfiks_izraz> L_ZAGRADA D_ZAGRADA
		case POSTFIX_IZRAZ_3: {
			NonterminalNode postfiksIzraz = (NonterminalNode) r.get(0);

			// 1. provjeri (<postfiks_izraz>)
			check(postfiksIzraz, table);
			// 2. <postfiks_izraz>.tip = funkcija(void → pov )
			Type t = (Type) postfiksIzraz.getAttribute(Attribute.TIP);
			if (!(t instanceof FunctionType)) {
				throw new SemanticsException("Invalid function call", l);
			}
			FunctionType func = (FunctionType) t;
			// void
			if (!func.getParameterTypes().getTypes().isEmpty()) {
				throw new SemanticsException("Function requires arguments", l);
			}

			l.setAttribute(Attribute.TIP, func.getReturnType());
			l.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}
		// <postfiks_izraz> ::= <postfiks_izraz> L_ZAGRADA <lista_argumenata>
		// D_ZAGRADA
		case POSTFIX_IZRAZ_4: {
			NonterminalNode postfiksIzraz = (NonterminalNode) r.get(0);
			NonterminalNode listaArgumenata = (NonterminalNode) r.get(2);

			// 1. provjeri (<postfiks_izraz>)
			check(postfiksIzraz, table);
			// 2. provjeri (<lista_argumenata>)
			check(listaArgumenata, table);

			Type t = (Type) postfiksIzraz.getAttribute(Attribute.TIP);
			if (!(t instanceof FunctionType)) {
				throw new SemanticsException("Invalid function call", l);
			}
			FunctionType func = (FunctionType) t;

			TypeList argTypes = (TypeList) listaArgumenata.getAttribute(Attribute.TIPOVI);
			TypeList paramTypes = func.getParameterTypes();

			// 3. <postfiks_izraz>.tip = funkcija(params → pov ) i redom po
			// elementima arg-tip iz <lista_argumenata>.tipovi i param-tip iz
			// params vrijedi arg-tip ~ param-tip

			if (!argTypes.canConvertImplicit(paramTypes)) {
				throw new SemanticsException("Incompatible arguments for function call parameters", l);
			}

			l.setAttribute(Attribute.TIP, func.getReturnType());
			l.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}
		// <postfiks_izraz> ::= <postfiks_izraz> OP_INC
		// <postfiks_izraz> ::= <postfiks_izraz> OP_DEC
		case POSTFIX_IZRAZ_5:
		case POSTFIX_IZRAZ_6: {
			NonterminalNode postfiksIzraz = (NonterminalNode) r.get(0);

			// 1. provjeri (<postfiks_izraz>)
			check(postfiksIzraz, table);

			// 2. <postfiks_izraz>.l-izraz = 1 i <postfiks_izraz>.tip ∼ int
			Type type = (Type) postfiksIzraz.getAttribute(Attribute.TIP);
			if (!type.canConvertImplicit(IntType.INSTANCE)) {
				throw new SemanticsException("Incrementing/decrementing incompatible type", l);
			}
			boolean lvalue = (Boolean) postfiksIzraz.getAttribute(Attribute.L_IZRAZ);
			if (!lvalue) {
				throw new SemanticsException("Incrementing/decrementing non-lvalue", l);
			}

			l.setAttribute(Attribute.TIP, IntType.INSTANCE);
			l.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}

		// <lista_argumenata> ::= <izraz_pridruzivanja>
		case LISTA_ARGUMENATA_1: {
			NonterminalNode izrazPridruzivanja = (NonterminalNode) r.get(0);

			check(izrazPridruzivanja, table);

			Type type = (Type) izrazPridruzivanja.getAttribute(Attribute.TIP);
			TypeList list = new TypeList(Arrays.asList(type));
			l.setAttribute(Attribute.TIPOVI, list);
			break;
		}
		// <lista_argumenata> ::= <lista_argumenata> ZAREZ <izraz_pridruzivanja>
		case LISTA_ARGUMENATA_2: {
			NonterminalNode listaArgumenata = (NonterminalNode) r.get(0);
			NonterminalNode izrazPridruzivanja = (NonterminalNode) r.get(2);

			check(listaArgumenata, table);
			check(izrazPridruzivanja, table);

			Type type = (Type) izrazPridruzivanja.getAttribute(Attribute.TIP);
			TypeList list = (TypeList) l.getAttribute(Attribute.TIPOVI);
			list.getTypes().add(type);

			l.setAttribute(Attribute.TIPOVI, list);
			break;
		}

		// unarni_izraz> ::= <postfiks_izraz>
		case UNARNI_IZRAZ_1: {
			NonterminalNode postfiksIzraz = (NonterminalNode) r.get(0);

			check(postfiksIzraz, table);

			l.setAttribute(Attribute.TIP, postfiksIzraz.getAttribute(Attribute.TIP));
			l.setAttribute(Attribute.L_IZRAZ, postfiksIzraz.getAttribute(Attribute.L_IZRAZ));
			break;
		}

		// <unarni_izraz> ::= (OP_INC | OP_DEC) <unarni_izraz>
		case UNARNI_IZRAZ_2:
		case UNARNI_IZRAZ_3: {
			NonterminalNode unarniIzraz = (NonterminalNode) r.get(1);

			check(unarniIzraz, table);

			l.setAttribute(Attribute.TIP, IntType.INSTANCE);
			l.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}
		// <unarni_izraz> ::= <unarni_operator> <cast_izraz>
		case UNARNI_IZRAZ_4: {
			NonterminalNode castIzraz = (NonterminalNode) r.get(1);

			check(castIzraz, table);

			Type castType = (Type) castIzraz.getAttribute(Attribute.TIP);

			if (!castType.canConvertImplicit(IntType.INSTANCE)) {
				throw new SemanticsException("Invalid type for unary operand", l);
			}

			l.setAttribute(Attribute.TIP, IntType.INSTANCE);
			l.setAttribute(Attribute.L_IZRAZ, false);
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

		// <cast_izraz> ::= <unarni_izraz>
		case CAST_IZRAZ_1: {
			NonterminalNode unarniIzraz = (NonterminalNode) r.get(0);

			check(unarniIzraz, table);

			l.setAttribute(Attribute.TIP, unarniIzraz.getAttribute(Attribute.TIP));
			l.setAttribute(Attribute.L_IZRAZ, unarniIzraz.getAttribute(Attribute.L_IZRAZ));
			break;
		}
		// <cast_izraz> ::= L_ZAGRADA <ime_tipa> D_ZAGRADA <cast_izraz>
		case CAST_IZRAZ_2: {
			NonterminalNode imeTipa = (NonterminalNode) r.get(1);
			NonterminalNode castIzraz = (NonterminalNode) r.get(3);

			check(imeTipa, table);
			check(castIzraz, table);

			Type from = (Type) castIzraz.getAttribute(Attribute.TIP);
			Type to = (Type) imeTipa.getAttribute(Attribute.TIP);

			if (!from.canConvertExplicit(to)) {
				throw new SemanticsException("Invalid cast", l);
			}

			break;
		}
		// <ime_tipa> ::= <specifikator_tipa>
		case IME_TIPA_1: {
			NonterminalNode specifikatorTipa = (NonterminalNode) r.get(0);

			check(specifikatorTipa, table);

			l.setAttribute(Attribute.TIP, specifikatorTipa.getAttribute(Attribute.TIP));
			break;
		}
		// <ime_tipa> ::= KR_CONST <specifikator_tipa>
		case IME_TIPA_2: {
			NonterminalNode specifikatorTipa = (NonterminalNode) r.get(1);

			check(specifikatorTipa, table);
			Type type = (Type) specifikatorTipa.getAttribute(Attribute.TIP);
			if (type instanceof VoidType) {
				throw new SemanticsException("const void is disallowed", l);
			}

			l.setAttribute(Attribute.TIP, new ConstType((PrimitiveType) specifikatorTipa.getAttribute(Attribute.TIP)));
			break;
		}
		// <specifikator_tipa> ::= KR_VOID
		case SPECIFIKATOR_TIPA_1: {
			l.setAttribute(Attribute.TIP, VoidType.INSTANCE);
			break;
		}
		// <specifikator_tipa> ::= KR_CHAR
		case SPECIFIKATOR_TIPA_2: {
			l.setAttribute(Attribute.TIP, CharType.INSTANCE);
			break;
		}
		// <specifikator_tipa> ::= KR_INT
		case SPECIFIKATOR_TIPA_3: {
			l.setAttribute(Attribute.TIP, IntType.INSTANCE);
			break;
		}

		// <multiplikativni_izraz> ::= <cast_izraz>
		case MULTIPLIKATIVNI_IZRAZ_1: {
			checkExpressionUnitProduction(l, table);
			break;
		}
		// <multiplikativni_izraz> ::= <multiplikativni_izraz> (OP_PUTA |
		// OP_DIJELI | OP_MOD) <cast_izraz>
		case MULTIPLIKATIVNI_IZRAZ_2:
		case MULTIPLIKATIVNI_IZRAZ_3:
		case MULTIPLIKATIVNI_IZRAZ_4: {
			checkIntBinaryOperator(l, table);
			break;
		}

		// <aditivni_izraz> ::= <multiplikativni_izraz>
		case ADITIVNI_IZRAZ_1: {
			checkExpressionUnitProduction(l, table);
			break;
		}
		// <aditivni_izraz> ::= <aditivni_izraz> (PLUS | MINUS)
		// <multiplikativni_izraz>
		case ADITIVNI_IZRAZ_2:
		case ADITIVNI_IZRAZ_3: {
			checkIntBinaryOperator(l, table);
			break;
		}

		// <odnosni_izraz> ::= <aditivni_izraz>
		case ODNOSNI_IZRAZ_1: {
			checkExpressionUnitProduction(l, table);
			break;
		}
		// <odnosni_izraz> ::= <odnosni_izraz> (OP_LT | OP_GT | OP_LTE | OP_GTE)
		// <aditivni_izraz>
		case ODNOSNI_IZRAZ_2:
		case ODNOSNI_IZRAZ_3:
		case ODNOSNI_IZRAZ_4:
		case ODNOSNI_IZRAZ_5: {
			checkIntBinaryOperator(l, table);
			break;
		}

		// <jednakosni_izraz> ::= <odnosni_izraz>
		case JEDNAKOSNI_IZRAZ_1: {
			checkExpressionUnitProduction(l, table);
			break;
		}
		// <jednakosni_izraz> ::= <jednakosni_izraz> (OP_EQ | OP_NEQ)
		// <odnosni_izraz>
		case JEDNAKOSNI_IZRAZ_2:
		case JEDNAKOSNI_IZRAZ_3: {
			checkIntBinaryOperator(l, table);
			break;
		}

		// <bin_i_izraz> ::= <jednakosni_izraz>
		case BIN_I_IZRAZ_1: {
			checkExpressionUnitProduction(l, table);
			break;
		}
		// <bin_i_izraz> ::= <bin_i_izraz> OP_BIN_I <jednakosni_izraz>
		case BIN_I_IZRAZ_2: {
			checkIntBinaryOperator(l, table);
			break;
		}

		case BIN_XILI_IZRAZ_1: {
			checkExpressionUnitProduction(l, table);
			break;
		}
		case BIN_XILI_IZRAZ_2: {
			checkIntBinaryOperator(l, table);
			break;
		}

		case BIN_ILI_IZRAZ_1: {
			checkExpressionUnitProduction(l, table);
			break;
		}
		case BIN_ILI_IZRAZ_2: {
			checkIntBinaryOperator(l, table);
			break;
		}

		case LOG_I_IZRAZ_1: {
			checkExpressionUnitProduction(l, table);
			break;
		}
		case LOG_I_IZRAZ_2: {
			checkIntBinaryOperator(l, table);
			break;
		}

		case LOG_ILI_IZRAZ_1: {
			checkExpressionUnitProduction(l, table);
			break;
		}
		case LOG_ILI_IZRAZ_2: {
			checkIntBinaryOperator(l, table);
			break;
		}

		// <izraz_pridruzivanja> ::= <log_ili_izraz>
		case IZRAZ_PRIDRUZIVANJA_1: {
			checkExpressionUnitProduction(l, table);
			break;
		}
		// <izraz_pridruzivanja> ::= <postfiks_izraz> OP_PRIDRUZI
		// <izraz_pridruzivanja>
		case IZRAZ_PRIDRUZIVANJA_2: {
			NonterminalNode postfiksIzraz = (NonterminalNode) r.get(0);
			NonterminalNode izrazPridruzivanja = (NonterminalNode) r.get(2);

			check(postfiksIzraz, table);

			boolean lvalue = (Boolean) postfiksIzraz.getAttribute(Attribute.L_IZRAZ);
			if (!lvalue)
				throw new SemanticsException("Non-lvalue assignment", l);
			check(izrazPridruzivanja, table);

			Type rhsType = (Type) izrazPridruzivanja.getAttribute(Attribute.TIP);
			Type lhsType = (Type) postfiksIzraz.getAttribute(Attribute.TIP);
			if (!rhsType.canConvertImplicit(lhsType))
				throw new SemanticsException("Incompatible types in assignment", l);
			break;
		}

		// <izraz> ::= <izraz_pridruzivanja>
		case IZRAZ_1: {
			checkExpressionUnitProduction(l, table);
			break;
		}
		// <izraz> ::= <izraz> ZAREZ <izraz_pridruzivanja>
		case IZRAZ_2: {
			NonterminalNode izraz = (NonterminalNode) r.get(0);
			NonterminalNode izrazPridruzivanja = (NonterminalNode) r.get(2);

			check(izraz, table);
			check(izrazPridruzivanja, table);

			l.setAttribute(Attribute.TIP, izrazPridruzivanja.getAttribute(Attribute.TIP));
			l.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}

		// <slozena_naredba> ::= L_VIT_ZAGRADA <lista_naredbi> D_VIT_ZAGRADA
		case SLOZENA_NAREDBA_1: {
			NonterminalNode listaNaredbi = (NonterminalNode) r.get(1);

			/*
			 * Svaki blok je odvojeni djelokrug, a nelokalnim imenima se
			 * pristupa u ugnijezdujucem bloku
			 */
			// FIXME ?

			check(listaNaredbi, table);
			break;
		}
		// <slozena_naredba> ::= L_VIT_ZAGRADA <lista_deklaracija>
		// <lista_naredbi> D_VIT_ZAGRADA
		case SLOZENA_NAREDBA_2: {
			NonterminalNode listaDeklaracija = (NonterminalNode) r.get(1);
			NonterminalNode listaNaredbi = (NonterminalNode) r.get(2);

			/*
			 * Svaki blok je odvojeni djelokrug, a nelokalnim imenima se
			 * pristupa u ugnijezdujucem bloku
			 */
			// / FIXME ?

			check(listaDeklaracija, table);
			check(listaNaredbi, table);
			break;
		}

		// lista_naredbi> ::= <naredba>
		case LISTA_NAREDBI_1: {
			NonterminalNode naredba = (NonterminalNode) r.get(0);

			check(naredba, table);
			break;
		}
		// lista_naredbi> ::= <lista_naredbi> <naredba>
		case LISTA_NAREDBI_2: {
			NonterminalNode listaNaredbi = (NonterminalNode) r.get(0);
			NonterminalNode naredba = (NonterminalNode) r.get(1);

			check(listaNaredbi, table);
			check(naredba, table);
			break;
		}

		// <naredba> ::= <izraz_naredba>
		case NAREDBA_2: {
			NonterminalNode u = (NonterminalNode) r.get(0);
			check(u, table);
			break;
		}
		// <naredba> ::= <slozena_naredba> | <naredba_grananja>
		// |<naredba_petlje> | <naredba_skoka>
		case NAREDBA_1:
		case NAREDBA_3:
		case NAREDBA_4:
		case NAREDBA_5: {
			// treba forwardati svojstvo PETLJA radi continue i break, ako se
			// naredba prosiruje u bilo sto osim <izraz_naredba>
			NonterminalNode u = (NonterminalNode) r.get(0);
			u.setAttribute(Attribute.PETLJA, l.getAttribute(Attribute.PETLJA));
			check(u, table);
			break;
		}

		// <izraz_naredba> ::= TOCKAZAREZ
		case IZRAZ_NAREDBA_1: {
			l.setAttribute(Attribute.TIP, IntType.INSTANCE);
			break;
		}
		// izraz_naredba> ::= <izraz> TOCKAZAREZ
		case IZRAZ_NAREDBA_2: {
			NonterminalNode izraz = (NonterminalNode) r.get(0);

			check(izraz, table);

			l.setAttribute(Attribute.TIP, IntType.INSTANCE);
			break;
		}

		// <naredba_grananja> ::= KR_IF L_ZAGRADA <izraz> D_ZAGRADA <naredba>
		case NAREDBA_GRANANJA_1: {
			NonterminalNode izraz = (NonterminalNode) r.get(2);
			NonterminalNode naredba = (NonterminalNode) r.get(4);

			check(izraz, table);
			Type izrazType = (Type) izraz.getAttribute(Attribute.TIP);
			if (!izrazType.canConvertImplicit(IntType.INSTANCE))
				throw new SemanticsException("If-condition expression has invalid type", l);
			check(naredba, table);

			break;
		}
		// <naredba_grananja> ::= KR_IF L_ZAGRADA <izraz> D_ZAGRADA <naredba>
		// KR_ELSE <naredba>
		case NAREDBA_GRANANJA_2: {
			NonterminalNode izraz = (NonterminalNode) r.get(2);
			NonterminalNode naredba1 = (NonterminalNode) r.get(4);
			NonterminalNode naredba2 = (NonterminalNode) r.get(6);

			check(izraz, table);
			Type izrazType = (Type) izraz.getAttribute(Attribute.TIP);
			if (!izrazType.canConvertImplicit(IntType.INSTANCE))
				throw new SemanticsException("If-condition expression has invalid type", l);
			check(naredba1, table);
			check(naredba2, table);
			break;
		}

		// <naredba_petlje> ::= KR_WHILE L_ZAGRADA <izraz> D_ZAGRADA <naredba>
		case NAREDBA_PETLJE_1: {
			NonterminalNode izraz = (NonterminalNode) r.get(2);
			NonterminalNode naredba = (NonterminalNode) r.get(4);

			check(izraz, table);
			Type izrazType = (Type) izraz.getAttribute(Attribute.TIP);
			if (!izrazType.canConvertImplicit(IntType.INSTANCE))
				throw new SemanticsException("While-loop condition is of invalid type", l);

			naredba.setAttribute(Attribute.PETLJA, true); // FIXME ?

			check(naredba, table);
			break;
		}
		// <naredba_petlje> ::= KR_FOR L_ZAGRADA <izraz_naredba>1
		// <izraz_naredba>2 D_ZAGRADA <naredba>
		case NAREDBA_PETLJE_2: {
			NonterminalNode izrazNaredba1 = (NonterminalNode) r.get(2);
			NonterminalNode izrazNaredba2 = (NonterminalNode) r.get(3);
			NonterminalNode naredba = (NonterminalNode) r.get(5);

			check(izrazNaredba1, table);
			check(izrazNaredba2, table);

			Type type = (Type) izrazNaredba2.getAttribute(Attribute.TIP);
			if (!type.canConvertImplicit(IntType.INSTANCE))
				throw new SemanticsException("For-loop condition of invalit type", l);

			naredba.setAttribute(Attribute.PETLJA, true); // FIXME ?

			check(naredba, table);
			break;
		}
		// <naredba_petlje> ::= KR_FOR L_ZAGRADA <izraz_naredba>1
		// <izraz_naredba>2 <izraz> D_ZAGRADA <naredba>
		case NAREDBA_PETLJE_3: {
			NonterminalNode izrazNaredba1 = (NonterminalNode) r.get(2);
			NonterminalNode izrazNaredba2 = (NonterminalNode) r.get(3);
			NonterminalNode izraz = (NonterminalNode) r.get(4);
			NonterminalNode naredba = (NonterminalNode) r.get(6);

			check(izrazNaredba1, table);
			check(izrazNaredba2, table);

			Type type = (Type) izrazNaredba2.getAttribute(Attribute.TIP);
			if (!type.canConvertImplicit(IntType.INSTANCE))
				throw new SemanticsException("For-loop condition of invalid type", l);

			check(izraz, table);

			naredba.setAttribute(Attribute.PETLJA, true); // FIXME ?

			check(naredba, table);
			break;
		}

		// naredba_skoka> ::= (KR_CONTINUE | KR_BREAK) TOCKAZAREZ
		case NAREDBA_SKOKA_1:
		case NAREDBA_SKOKA_2: {
			// TODO maybe better using symboltable and scope
			boolean insideLoop = (Boolean) l.getAttribute(Attribute.PETLJA);
			if (!insideLoop) {
				throw new SemanticsException("break/continue in non-loop scope", l);
			}
			break;
		}
		// <naredba_skoka> ::= KR_RETURN TOCKAZAREZ
		case NAREDBA_SKOKA_3: {
			// TODO check current function return type thru symboltable
			break;
		}
		// <naredba_skoka> ::= KR_RETURN <izraz> TOCKAZAREZ
		case NAREDBA_SKOKA_4: {
			// TODO check current function return type thru symboltable
			break;
		}

		// <prijevodna_jedinica> ::= <vanjska_deklaracija>
		case PRIJEVODNA_JEDINICA_1: {
			NonterminalNode vanjskaDeklaracija = (NonterminalNode) r.get(0);
			check(vanjskaDeklaracija, table);
			break;
		}
		// <prijevodna_jedinica> ::= <prijevodna_jedinica> <vanjska_deklaracija>
		case PRIJEVODNA_JEDINICA_2: {
			NonterminalNode prijevodnaJedinica = (NonterminalNode) r.get(0);
			NonterminalNode vanjskaDeklaracija = (NonterminalNode) r.get(1);
			check(prijevodnaJedinica, table);
			check(vanjskaDeklaracija, table);
			break;
		}
		// <vanjska_deklaracija> ::= <definicija_funkcije> | <deklaracija>
		case VANJSKA_DEKLARACIJA_1:
		case VANJSKA_DEKLARACIJA_2: {
			check((NonterminalNode) r.get(0), table);
			break;
		}

		// Deklaracije i definicije
		// = magic with symbol table happens

		// <definicija_funkcije> ::= <ime_tipa> IDN L_ZAGRADA KR_VOID D_ZAGRADA
		// <slozena_naredba>
		case DEFINICIJA_FUNKCIJE_1: {
			NonterminalNode imeTipa = (NonterminalNode) r.get(0);
			TerminalNode functionName = (TerminalNode) r.get(1);
			NonterminalNode slozenaNaredba = (NonterminalNode) r.get(5);

			// 1. provjeri (<ime_tipa>)
			check(imeTipa, table);

			// 2. <ime_tipa>.tip = const(T )
			if (imeTipa.getAttribute(Attribute.TIP) instanceof ConstType) {
				throw new SemanticsException("Function return type cannot be const-qualified", l);
			}
			Type retType = (Type) imeTipa.getAttribute(Attribute.TIP);
//			if (table != SymbolTable.GLOBAL)
//				throw new IllegalStateException("internal error, scope mixup");
			SymbolEntry functionEntry = SymbolTable.GLOBAL.get(functionName.getText());
			FunctionType fType = new FunctionType(retType, new TypeList(new ArrayList<Type>()));

			if (functionEntry != null) {
				// 3. ne postoji prije definirana funkcija imena IDN.ime
				if (functionEntry.isDefined())
					throw new SemanticsException("Redefinition of function", l);

				/*
				 * 4. ako postoji deklaracija imena IDN.ime u globalnom //
				 * djelokrugu onda je pripadni tip te deklaracije
				 * 
				 * funkcija(void→ <ime_tipa>.tip)
				 */
				if (!functionEntry.getType().canConvertImplicit(fType))
					throw new SemanticsException("Function definition and declaration differ in prototypes", l);
			} else {
				// 5. zabiljezi definiciju i deklaraciju funkcije
				functionEntry = new SymbolEntry(fType);
				SymbolTable.GLOBAL.addLocal(functionName.getText(), functionEntry);
			}
			functionEntry.markDefined();

			// FIXME add something for return check

			SymbolTable inner = table.createNested();

			// 6. provjeri (<slozena_naredba>)
			check(slozenaNaredba, inner);

			break;
		}
		// <definicija_funkcije> ::= <ime_tipa> IDN L_ZAGRADA <lista_parametara>
		// D_ZAGRADA <slozena_naredba>
		case DEFINICIJA_FUNKCIJE_2: {
			NonterminalNode imeTipa = (NonterminalNode) r.get(0);
			TerminalNode functionName = (TerminalNode) r.get(1);
			NonterminalNode listaParametara = (NonterminalNode) r.get(3);
			NonterminalNode slozenaNaredba = (NonterminalNode) r.get(5);

			// 1. provjeri (<ime_tipa>)
			check(imeTipa, table);

			// 2. <ime_tipa>.tip = const(T )
			if (imeTipa.getAttribute(Attribute.TIP) instanceof ConstType) {
				throw new SemanticsException("Function return type cannot be const-qualified", l);
			}
			Type retType = (Type) imeTipa.getAttribute(Attribute.TIP);
			SymbolEntry functionEntry = SymbolTable.GLOBAL.get(functionName.getText());
			FunctionType fType = new FunctionType(retType, (TypeList) listaParametara.getAttribute(Attribute.TIPOVI));

			if (functionEntry != null && functionEntry.isDefined()) {
				// 3. ne postoji prije definirana funkcija imena IDN.ime
				if (functionEntry.isDefined())
					throw new SemanticsException("Redefinition of function", l);
			}

			// 4. provjeri (<lista_parametara>)
			check(listaParametara, table);

			// 5. ako postoji deklaracija imena IDN.ime u globalnom djelokrugu
			// onda
			// je pripadni tip te deklaracije ...
			if (functionEntry != null) {
				if (!functionEntry.getType().canConvertImplicit(fType))
					throw new SemanticsException("Function definition and declaration differ in prototypes", l);
			} else {
				// 6. zabiljezi definiciju i deklaraciju funkcije
				functionEntry = new SymbolEntry(fType);
				SymbolTable.GLOBAL.addLocal(functionName.getText(), functionEntry);
			}
			functionEntry.markDefined();

			// 7.

			SymbolTable inner = table.createNested();

			@SuppressWarnings("unchecked")
			List<String> paramNames = (List<String>) listaParametara.getAttribute(Attribute.IMENA);
			List<Type> paramTypes = ((TypeList) listaParametara.getAttribute(Attribute.TIPOVI)).getTypes();

			int size = paramNames.size();
			for (int i = 0; i < size; ++i)
				inner.addLocal(paramNames.get(i), new SymbolEntry(paramTypes.get(i)));

			// FIXME add something so return check works

			check(slozenaNaredba, inner);

			break;
		}

		// <lista_parametara> ::= <deklaracija_parametra>
		case LISTA_PARAMETARA_1: {
			NonterminalNode deklaracijaParametra = (NonterminalNode) r.get(0);

			check(deklaracijaParametra, table);

			l.setAttribute(Attribute.IMENA, Arrays.asList((String) deklaracijaParametra.getAttribute(Attribute.IME)));
			l.setAttribute(Attribute.TIPOVI,
					new TypeList(Arrays.asList((Type) deklaracijaParametra.getAttribute(Attribute.TIP))));
			break;
		}
		// <lista_parametara> ::= <lista_parametara> ZAREZ
		// <deklaracija_parametra>
		case LISTA_PARAMETARA_2: {
			NonterminalNode listaParametara = (NonterminalNode) r.get(0);
			NonterminalNode deklaracijaParametra = (NonterminalNode) r.get(3);

			check(listaParametara, table);
			check(deklaracijaParametra, table);

			@SuppressWarnings("unchecked")
			List<String> names = (List<String>) listaParametara.getAttribute(Attribute.IMENA);
			TypeList typeList = (TypeList) listaParametara.getAttribute(Attribute.TIPOVI);

			String name = (String) deklaracijaParametra.getAttribute(Attribute.IME);
			Type type = (Type) deklaracijaParametra.getAttribute(Attribute.TIP);

			if (names.contains(name)) {
				throw new SemanticsException("Duplicate parameter name", l);
			}
			names.add(name);
			typeList.getTypes().add(type);

			l.setAttribute(Attribute.TIPOVI, typeList);
			l.setAttribute(Attribute.IMENA, names);
			break;
		}

		// <deklaracija_parametra> ::= <ime_tipa> IDN
		case DEKLARACIJA_PARAMETRA_1: {
			NonterminalNode imeTipa = (NonterminalNode) r.get(0);
			TerminalNode idn = (TerminalNode) r.get(1);

			check(imeTipa, table);

			Type type = (Type) imeTipa.getAttribute(Attribute.TIP);
			if (type instanceof VoidType)
				throw new SemanticsException("illegal parameter type void", l);

			l.setAttribute(Attribute.TIP, type);
			l.setAttribute(Attribute.IME, idn.getText());
			break;
		}
		// <deklaracija_parametra> ::= <ime_tipa> IDN L_UGL_ZAGRADA
		// D_UGL_ZAGRADA
		case DEKLARACIJA_PARAMETRA_2: {
			NonterminalNode imeTipa = (NonterminalNode) r.get(0);
			TerminalNode idn = (TerminalNode) r.get(1);

			check(imeTipa, table);

			Type type = (Type) imeTipa.getAttribute(Attribute.TIP);
			if (type instanceof VoidType)
				throw new SemanticsException("illegal parameter type array of void", l);

			l.setAttribute(Attribute.TIP, new ArrayType((PrimitiveType) type));
			l.setAttribute(Attribute.IME, idn.getText());
			break;
		}

		// <lista_deklaracija> ::= <deklaracija>
		case LISTA_DEKLARACIJA_1: {
			check(r.get(0), table);
			break;
		}
		// <lista_deklaracija> ::= <lista_deklaracija> <deklaracija>
		case LISTA_DEKLARACIJA_2: {
			check(r.get(0), table);
			check(r.get(1), table);
			break;
		}

		// <deklaracija> ::= <ime_tipa> <lista_init_deklaratora> TOCKAZAREZ
		case DEKLARACIJA_1: {
			NonterminalNode imeTipa = (NonterminalNode) r.get(0);
			NonterminalNode listaInitDeklaratora = (NonterminalNode) r.get(1);

			check(imeTipa, table);

			Type type = (Type) imeTipa.getAttribute(Attribute.TIP);
			listaInitDeklaratora.setAttribute(Attribute.NTIP, type);
			check(listaInitDeklaratora, table);
			break;
		}

		// <lista_init_deklaratora> ::= <init_deklarator>
		case LISTA_INIT_DEKLARATORA_1: {
			NonterminalNode initDeklarator = (NonterminalNode) r.get(0);
			initDeklarator.setAttribute(Attribute.NTIP, l.getAttribute(Attribute.NTIP));
			check(initDeklarator, table);
			break;
		}
		// <lista_init_deklaratora> ::= <lista_init_deklaratora> ZAREZ
		// <init_deklarator>
		case LISTA_INIT_DEKLARATORA_2: {
			NonterminalNode listaInitDeklaratora = (NonterminalNode) r.get(0);
			NonterminalNode initDeklarator = (NonterminalNode) r.get(2);

			listaInitDeklaratora.setAttribute(Attribute.NTIP, l.getAttribute(Attribute.NTIP));
			check(listaInitDeklaratora, table);

			initDeklarator.setAttribute(Attribute.NTIP, l.getAttribute(Attribute.NTIP));
			check(initDeklarator, table);
			break;
		}

		// <init deklarator> ::= <izravni_deklarator>
		case INIT_DEKLARATOR_1: {
			NonterminalNode izravniDeklarator = (NonterminalNode) r.get(0);
			izravniDeklarator.setAttribute(Attribute.NTIP, l.getAttribute(Attribute.NTIP));
			check(izravniDeklarator, table);

			Type type = (Type) izravniDeklarator.getAttribute(Attribute.TIP);
			if (type instanceof ConstType)
				throw new SemanticsException("Const types must have initializer", l);
			if (type instanceof ArrayType) {
				ArrayType arrayType = (ArrayType) type;
				if (arrayType.getElementType() instanceof ConstType)
					throw new SemanticsException("Const types must have initializer", l);
			}
			break;
		}
		// <init_deklarator> ::= <izravni_deklarator> OP_PRIDRUZI
		// <inicijalizator>
		case INIT_DEKLARATOR_2: {
			NonterminalNode izravniDeklarator = (NonterminalNode) r.get(0);
			izravniDeklarator.setAttribute(Attribute.NTIP, l.getAttribute(Attribute.NTIP));
			check(izravniDeklarator, table);

			NonterminalNode inicijalizator = (NonterminalNode) r.get(2);
			check(inicijalizator, table);

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
					throw new SemanticsException("Incompatible types", l);
			} else if (type instanceof ArrayType) {
				int arraySize = (Integer) izravniDeklarator.getAttribute(Attribute.BR_ELEM);
				int initializerSize = (Integer) inicijalizator.getAttribute(Attribute.BR_ELEM);
				if (initializerSize > arraySize)
					throw new SemanticsException("Initializer too long", l);

				Type elementType = ((ArrayType) type).getElementType();
				if (elementType instanceof ConstType) {
					to = ((ConstType) elementType).getType();
				} else {
					to = elementType;
				}

				TypeList fromTypes = (TypeList) inicijalizator.getAttribute(Attribute.TIPOVI);
				for (Type from : fromTypes.getTypes())
					if (!from.canConvertImplicit(to))
						throw new SemanticsException("Incompatible member type in initializer array", l);
			} else {
				throw new SemanticsException("Invalid type initializer", l);
			}
			break;
		}

		// <izravni_deklarator> ::= IDN
		case IZRAVNI_DEKLARATOR_1: {
			TerminalNode idn = (TerminalNode) r.get(0);
			Type ntype = (Type) l.getAttribute(Attribute.NTIP);

			if (ntype instanceof VoidType)
				throw new SemanticsException("Cannot declare identifier of type void", l);
			if (table.getLocal(idn.getText()) != null)
				throw new SemanticsException("Redeclaration in local scope", l);
			table.addLocal(idn.getText(), new SymbolEntry(ntype));

			l.setAttribute(Attribute.TIP, ntype);
			break;
		}
		// <izravni_deklarator> ::= IDN L_UGL_ZAGRADA BROJ D_UGL_ZAGRADA
		case IZRAVNI_DEKLARATOR_2: {
			TerminalNode idn = (TerminalNode) r.get(0);
			TerminalNode broj = (TerminalNode) r.get(2);

			Type ntype = (Type) l.getAttribute(Attribute.NTIP);
			Type type = new ArrayType((PrimitiveType) ntype);

			if (ntype instanceof VoidType)
				throw new SemanticsException("Cannot declare identifier of type void", l);
			if (table.getLocal(idn.getText()) != null)
				throw new SemanticsException("Redeclaration in local scope", l);
			int size = Integer.parseInt(broj.getText());
			if (size <= 0 || size > 1024)
				throw new SemanticsException("illegal array size", l);
			table.addLocal(idn.getText(), new SymbolEntry(type));

			l.setAttribute(Attribute.TIP, type);
			l.setAttribute(Attribute.BR_ELEM, size);
			break;
		}
		// <izravni_deklarator> ::= IDN L_ZAGRADA KR_VOID D_ZAGRADA
		case IZRAVNI_DEKLARATOR_3: {
			TerminalNode idn = (TerminalNode) r.get(0);
			Type ntype = (Type) l.getAttribute(Attribute.NTIP);
			Type type = new FunctionType(ntype, new TypeList(new ArrayList<Type>()));

			SymbolEntry entry = table.getLocal(idn.getText());
			if (entry != null) {
				Type definedType = entry.getType();
				if (!definedType.equals(type))
					throw new SemanticsException("redefinition of function with differing prototype", l);
			} else {
				entry = new SymbolEntry(type);
				table.addLocal(idn.getText(), entry);
			}

			l.setAttribute(Attribute.TIP, type);
			break;
		}
		// <izravni_deklarator> ::= IDN L_ZAGRADA <lista_parametara> D_ZAGRADA
		case IZRAVNI_DEKLARATOR_4: {
			TerminalNode idn = (TerminalNode) r.get(0);
			NonterminalNode listaParametara = (NonterminalNode) r.get(2);

			check(listaParametara, table);

			Type ntype = (Type) l.getAttribute(Attribute.NTIP);
			Type type = new FunctionType(ntype, (TypeList) listaParametara.getAttribute(Attribute.TIPOVI));

			SymbolEntry entry = table.getLocal(idn.getText());
			if (entry != null) {
				Type definedType = entry.getType();
				if (!definedType.equals(type))
					throw new SemanticsException("redefinition of function with differing prototype", l);
			} else {
				entry = new SymbolEntry(type);
				table.addLocal(idn.getText(), entry);
			}

			l.setAttribute(Attribute.TIP, type);
			break;
		}

		// <inicijalizator> ::= <izraz_pridruzivanja>
		case INICIJALIZATOR_1: {
			NonterminalNode izrazPridruzivanja = (NonterminalNode) r.get(0);

			check(izrazPridruzivanja, table);

			Node n = izrazPridruzivanja;
			while (n.getChildren() != null && n.getChildren().size() == 1)
				n = n.getChildren().get(0);
			if (n instanceof TerminalNode && "NIZ_ZNAKOVA".equals(((TerminalNode) n).getSymbol().getValue())) {
				int c = ((TerminalNode) n).getText().length() - 2 + 1; // 2
																		// quotes,
																		// add
																		// NUL
				l.setAttribute(Attribute.BR_ELEM, c);

				ArrayList<Type> list = new ArrayList<Type>();
				for (int i = 0; i < c; ++i)
					list.add(CharType.INSTANCE);

				l.setAttribute(Attribute.TIPOVI, new TypeList(list));
			} else {
				l.setAttribute(Attribute.TIP, izrazPridruzivanja.getAttribute(Attribute.TIP));
			}
			break;
		}
		// <inicijalizator> ::= L_VIT_ZAGRADA <lista_izraza_pridruzivanja>
		// D_VIT_ZAGRADA
		case INICIJALIZATOR_2: {
			NonterminalNode listaIzrazaPridruzivanja = (NonterminalNode) r.get(1);

			check(listaIzrazaPridruzivanja, table);

			l.setAttribute(Attribute.BR_ELEM, listaIzrazaPridruzivanja.getAttribute(Attribute.BR_ELEM));
			l.setAttribute(Attribute.TIPOVI, listaIzrazaPridruzivanja.getAttribute(Attribute.TIPOVI));
			break;
		}

		// <lista_izraza_pridruzivanja> ::= <izraz_pridruzivanja>
		case LISTA_IZRAZA_PRIDRUZIVANJA_1: {
			NonterminalNode izrazPridruzivanja = (NonterminalNode) r.get(0);

			check(izrazPridruzivanja, table);

			l.setAttribute(Attribute.BR_ELEM, 1);
			l.setAttribute(Attribute.TIPOVI,
					new TypeList(Arrays.asList((Type) izrazPridruzivanja.getAttribute(Attribute.TIP))));
			break;
		}
		// <lista_izraza_pridruzivanja> ::= <lista_izraza_pridruzivanja> ZAREZ
		// <izraz_pridruzivanja>
		case LISTA_IZRAZA_PRIDRUZIVANJA_2: {
			NonterminalNode listaIzrazaPridruzivanja = (NonterminalNode) r.get(0);
			NonterminalNode izrazPridruzivanja = (NonterminalNode) r.get(2);

			check(listaIzrazaPridruzivanja, table);
			check(izrazPridruzivanja, table);

			int c = (Integer) listaIzrazaPridruzivanja.getAttribute(Attribute.BR_ELEM);
			TypeList list = (TypeList) listaIzrazaPridruzivanja.getAttribute(Attribute.TIPOVI);
			Type type = (Type) izrazPridruzivanja.getAttribute(Attribute.TIP);
			list.getTypes().add(type);

			l.setAttribute(Attribute.BR_ELEM, c + 1);
			l.setAttribute(Attribute.TIPOVI, list);
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
	private static void checkIntBinaryOperator(Node parent, SymbolTable syms) throws SemanticsException {
		NonterminalNode a = (NonterminalNode) parent.getChildren().get(0);
		NonterminalNode b = (NonterminalNode) parent.getChildren().get(2);
		TerminalNode op = (TerminalNode) parent.getChildren().get(1);

		check(a, syms);
		Type aType = (Type) a.getAttribute(Attribute.TIP);
		if (!aType.canConvertImplicit(IntType.INSTANCE))
			throw new SemanticsException("Left operand to '" + op.getText() + "' is of invalid type", parent);
		check(b, syms);
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
	private static void checkExpressionUnitProduction(Node op, SymbolTable syms) {
		NonterminalNode innerOp = (NonterminalNode) op.getChildren().get(0);

		check(innerOp, syms);

		op.setAttribute(Attribute.TIP, innerOp.getAttribute(Attribute.TIP));
		op.setAttribute(Attribute.L_IZRAZ, innerOp.getAttribute(Attribute.L_IZRAZ));
	}

	private static PPJCProduction determineProduction(Node node) {
		// TODO implement
		throw new UnsupportedOperationException("not implemented");
	}

}
