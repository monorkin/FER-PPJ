package hr.unizg.fer.zemris.ppj.maheri.semantics;

import static hr.unizg.fer.zemris.ppj.maheri.semantics.Node.Attribute;

import hr.unizg.fer.zemris.ppj.maheri.semantics.SymbolTable.SymbolEntry;

import java.util.Arrays;
import java.util.List;

public class SemanticsAnalyzer {
	private Node generativeTree;

	public SemanticsAnalyzer(Node tree) {
		this.generativeTree = tree;
	}

	public void checkAttributes() {
		SymbolTable table = new SymbolTable(null);
		check(generativeTree, table);
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
		// probably need FunctionsTable which stores function prototypes, or
		// adapt SymbolTable and Type to have functions as types .
	}

	private static void check(Node l, SymbolTable table) {
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
		 * bitovne (OP_TILDA) i logiˇke (OP_NEG) prefiks unarne operatore. Kako
		 * u ovim produkcijama u semantickoj analizi ne treba nista provjeriti,
		 * produkcije ovdje nisu navedene.
		 */
		case UNARNI_OPERATOR_1: {
			break;
		}
		case UNARNI_OPERATOR_2: {
			break;
		}
		case UNARNI_OPERATOR_3: {
			break;
		}
		case UNARNI_OPERATOR_4: {
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
			NonterminalNode castIzraz = (NonterminalNode) r.get(0);

			check(castIzraz, table);

			l.setAttribute(Attribute.TIP, castIzraz.getAttribute(Attribute.TIP));
			l.setAttribute(Attribute.L_IZRAZ, castIzraz.getAttribute(Attribute.L_IZRAZ));
			break;
		}
		// <multiplikativni_izraz> ::= <multiplikativni_izraz> (OP_PUTA |
		// OP_DIJELI | OP_MOD) <cast_izraz>
		case MULTIPLIKATIVNI_IZRAZ_2:
		case MULTIPLIKATIVNI_IZRAZ_3:
		case MULTIPLIKATIVNI_IZRAZ_4: {
			NonterminalNode multiplikativniIzraz = (NonterminalNode) r.get(0);
			NonterminalNode castIzraz = (NonterminalNode) r.get(2);

			check(multiplikativniIzraz, table);
			Type mType = (Type) multiplikativniIzraz.getAttribute(Attribute.TIP);
			if (!mType.canConvertImplicit(IntType.INSTANCE))
				throw new SemanticsException("Left operand is not int-compatible", l);

			check(castIzraz, table);
			Type cType = (Type) castIzraz.getAttribute(Attribute.TIP);
			if (!cType.canConvertImplicit(IntType.INSTANCE))
				throw new SemanticsException("Right operand is not int-compatible", l);

			l.setAttribute(Attribute.TIP, IntType.INSTANCE);
			l.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}

		// <aditivni_izraz> ::= <multiplikativni_izraz>
		case ADITIVNI_IZRAZ_1: {
			NonterminalNode multiplikativniIzraz = (NonterminalNode) r.get(0);

			check(multiplikativniIzraz, table);

			l.setAttribute(Attribute.TIP, multiplikativniIzraz.getAttribute(Attribute.TIP));
			l.setAttribute(Attribute.L_IZRAZ, multiplikativniIzraz.getAttribute(Attribute.L_IZRAZ));
			break;
		}
		// <aditivni_izraz> ::= <aditivni_izraz> (PLUS | MINUS)
		// <multiplikativni_izraz>
		case ADITIVNI_IZRAZ_2:
		case ADITIVNI_IZRAZ_3: {
			NonterminalNode aditivniIzraz = (NonterminalNode) r.get(0);
			NonterminalNode multiplikativniIzraz = (NonterminalNode) r.get(2);

			check(aditivniIzraz, table);
			Type aType = (Type) aditivniIzraz.getAttribute(Attribute.TIP);
			if (!aType.canConvertImplicit(IntType.INSTANCE))
				throw new SemanticsException("Left operand is not int-compatible", l);

			check(multiplikativniIzraz, table);
			Type mType = (Type) multiplikativniIzraz.getAttribute(Attribute.TIP);
			if (!mType.canConvertImplicit(IntType.INSTANCE))
				throw new SemanticsException("Right operand is not int-compatible", l);

			l.setAttribute(Attribute.TIP, IntType.INSTANCE);
			l.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}

		// <odnosni_izraz> ::= <aditivni_izraz>
		case ODNOSNI_IZRAZ_1: {
			NonterminalNode aditivniIzraz = (NonterminalNode) r.get(0);

			check(aditivniIzraz, table);

			l.setAttribute(Attribute.TIP, aditivniIzraz.getAttribute(Attribute.TIP));
			l.setAttribute(Attribute.L_IZRAZ, aditivniIzraz.getAttribute(Attribute.L_IZRAZ));
			break;
		}
		// <odnosni_izraz> ::= <odnosni_izraz> (OP_LT | OP_GT | OP_LTE | OP_GTE)
		// <aditivni_izraz>
		case ODNOSNI_IZRAZ_2:
		case ODNOSNI_IZRAZ_3:
		case ODNOSNI_IZRAZ_4:
		case ODNOSNI_IZRAZ_5: {
			NonterminalNode odnosniIzraz = (NonterminalNode) r.get(0);
			NonterminalNode aditivniIzraz = (NonterminalNode) r.get(2);

			check(odnosniIzraz, table);
			Type oType = (Type) odnosniIzraz.getAttribute(Attribute.TIP);
			if (!oType.canConvertImplicit(IntType.INSTANCE))
				throw new SemanticsException("Left operand is not int-compatible", l);

			check(aditivniIzraz, table);
			Type aType = (Type) aditivniIzraz.getAttribute(Attribute.TIP);
			if (!aType.canConvertImplicit(IntType.INSTANCE))
				throw new SemanticsException("Right operand is not int-compatible", l);

			l.setAttribute(Attribute.TIP, IntType.INSTANCE);
			l.setAttribute(Attribute.L_IZRAZ, false);
			break;
		}

		case JEDNAKOSNI_IZRAZ_1: {
			break;
		}
		case JEDNAKOSNI_IZRAZ_2: {
			break;
		}
		case JEDNAKOSNI_IZRAZ_3: {
			break;
		}

		case BIN_I_IZRAZ_1: {
			break;
		}
		case BIN_I_IZRAZ_2: {
			break;
		}

		case BIN_XILI_IZRAZ_1: {
			break;
		}
		case BIN_XILI_IZRAZ_2: {
			break;
		}

		case BIN_ILI_IZRAZ_1: {
			break;
		}
		case BIN_ILI_IZRAZ_2: {
			break;
		}

		case LOG_I_IZRAZ_1: {
			break;
		}
		case LOG_I_IZRAZ_2: {
			break;
		}

		case LOG_ILI_IZRAZ_1: {
			break;
		}
		case LOG_ILI_IZRAZ_2: {
			break;
		}

		case IZRAZ_PRIDRUZIVANJA_1: {
			break;
		}
		case IZRAZ_PRIDRUZIVANJA_2: {
			break;
		}

		case IZRAZ_1: {
			break;
		}
		case IZRAZ_2: {
			break;
		}

		// <slozena_naredba> ::= L_VIT_ZAGRADA <lista_naredbi> D_VIT_ZAGRADA
		case SLOZENA_NAREDBA_1: {
			NonterminalNode listaNaredbi = (NonterminalNode) r.get(1);

			/*
			 * Svaki blok je odvojeni djelokrug, a nelokalnim imenima se
			 * pristupa u ugnijezdujucem bloku
			 */
			SymbolTable newTable = new SymbolTable(table);

			check(listaNaredbi, newTable);
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
			SymbolTable newTable = new SymbolTable(table);
			
			check(listaDeklaracija, newTable);
			check(listaNaredbi, newTable);
			break;
		}
		
		//lista_naredbi> ::= <naredba>
		case LISTA_NAREDBI_1: {
			NonterminalNode naredba = (NonterminalNode) r.get(0);
			
			check(naredba, table);
			break;
		}
		//lista_naredbi> ::= <lista_naredbi> <naredba>
		case LISTA_NAREDBI_2: {
			NonterminalNode listaNaredbi = (NonterminalNode) r.get(0);
			NonterminalNode naredba = (NonterminalNode) r.get(1);
			
			check(listaNaredbi, table);
			check(naredba, table);
			break;
		}

		case NAREDBA_1: {
			break;
		}
		case NAREDBA_2: {
			break;
		}

		case NAREDBA_3: {
			break;
		}
		case NAREDBA_4: {
			break;
		}
		case NAREDBA_5: {
			break;
		}

		case IZRAZ_NAREDBA_1: {
			break;
		}
		case IZRAZ_NAREDBA_2: {
			break;
		}

		case NAREDBA_GRANANJA_1: {
			break;
		}
		case NAREDBA_GRANANJA_2: {
			break;
		}

		case NAREDBA_PETLJE_1: {
			break;
		}
		case NAREDBA_PETLJE_2: {
			break;
		}
		case NAREDBA_PETLJE_3: {
			break;
		}

		case NAREDBA_SKOKA_1: {
			break;
		}
		case NAREDBA_SKOKA_2: {
			break;
		}
		case NAREDBA_SKOKA_3: {
			break;
		}
		case NAREDBA_SKOKA_4: {
			break;
		}

		case PRIJEVODNA_JEDINICA_1: {
			break;
		}
		case PRIJEVODNA_JEDINICA_2: {
			break;
		}

		case VANJSKA_DEKLARACIJA_1: {
			break;
		}
		case VANJSKA_DEKLARACIJA_2: {
			break;
		}

		case DEFINICIJA_FUNKCIJE_1: {
			break;
		}
		case DEFINICIJA_FUNKCIJE_2: {
			break;
		}

		case LISTA_PARAMETARA_1: {
			break;
		}
		case LISTA_PARAMETARA_2: {
			break;
		}

		case DEKLARACIJA_PARAMETRA_1: {
			break;
		}
		case DEKLARACIJA_PARAMETRA_2: {
			break;
		}

		case LISTA_DEKLARACIJA_1: {
			break;
		}
		case LISTA_DEKLARACIJA_2: {
			break;
		}

		case DEKLARACIJA_1: {
			break;
		}

		case LISTA_INIT_DEKLARATORA_1: {
			break;
		}
		case LISTA_INIT_DEKLARATORA_2: {
			break;
		}

		case INIT_DEKLARATOR_1: {
			break;
		}
		case INIT_DEKLARATOR_2: {
			break;
		}

		case IZRAVNI_DEKLARATOR_1: {
			break;
		}
		case IZRAVNI_DEKLARATOR_2: {
			break;
		}
		case IZRAVNI_DEKLARATOR_3: {
			break;
		}
		case IZRAVNI_DEKLARATOR_4: {
			break;
		}

		case INICIJALIZATOR_1: {
			break;
		}
		case INICIJALIZATOR_2: {
			break;
		}

		case LISTA_IZRAZA_PRIDRUZIVANJA_1: {
			break;
		}
		case LISTA_IZRAZA_PRIDRUZIVANJA_2: {
			break;
		}
		}
	}

	private static PPJCProduction determineProduction(Node node) {
		// TODO implement
		throw new UnsupportedOperationException("not implemented");
	}

}
