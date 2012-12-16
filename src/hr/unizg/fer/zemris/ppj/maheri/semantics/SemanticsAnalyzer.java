package hr.unizg.fer.zemris.ppj.maheri.semantics;

public class SemanticsAnalyzer {
	private Node generativeTree;

	public SemanticsAnalyzer(Node tree) {
		this.generativeTree = tree;
	}

	public void checkAttributes() {
		SymbolTable syms = new SymbolTable();
	}

	public void checkFunctions() {
		/*
		 * TODO
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
	}

}
