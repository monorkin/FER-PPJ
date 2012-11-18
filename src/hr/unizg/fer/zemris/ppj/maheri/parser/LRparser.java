package hr.unizg.fer.zemris.ppj.maheri.parser;

import hr.unizg.fer.zemris.ppj.maheri.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class LRparser {
	
	private int tDebug = 1;
	
	
	private lifoStack tStack;
	private ArrayList<HashMap <String, String>> tAction;
	private ArrayList<HashMap<String, ArrayList<String>>> tTransitions;
	private List<String> tInput;
	private int tState;
	private HashSet<String> sync;


	private int startState;
	
	/*
	 * Inicijalizacija
	 * Prima ulazni niz znakova, te Map<Integer, Map<String, String>> 
	 * Objasnjenje ::: Integer = broj retka; Mapa = sadrzaj retka -> prvi string = naslov stupca (znak) -> drugi string = akcija
	 * 				   drzim se sintakse iz UTR tako da drugi string sadrzi "rX", "lX" ili "prihvati"
	 * 
	 * 				   Za tranzicije vrijedi isto Integer sadrzi redni broj produkcije, potom u mapi ocekujem za kljuc "L" ljevu stranu
	 * 				   a za kljuc "R" desnu stranu produkcije (koja je spremljena kao hash dakle {a,b,c,patka,guska,kokos,...})
	 * 				   
	 */
	public LRparser(List<String> aInput, ArrayList<HashMap <String, String>> actionsTable, ArrayList<HashMap <String, ArrayList<String>>> aTransitions, int startState, HashSet<String> sync) 
	{
		tStack    	 = new lifoStack();
		tAction   	 = actionsTable;
		tTransitions = aTransitions;
		tInput    	 = aInput;
		this.startState = startState;
		this.sync = sync;
	}
	
	//Parsiraj niz
	public void parse()
	{
		String hAction;
		String hProductLeft;
		lifoStackItem hItem;
		boolean running = true;
		int counter = 0;
		int action = -1;
		int nextState = 0;
		tState = startState;
		
		sync.add("#END#");
		tInput.add("#END# #END# #END#");
		tStack.push(new lifoStackItem(tState, "#END# #END# #END#", false));
		
		ArrayList<TreeNode> treeBranches = new ArrayList<TreeNode>();
		
		while (running)
		{
			Logger.log("In state " + tState);
			Logger.log("Read char " + tInput.get(counter));
			
			//Get action
			String currInput = tInput.get(counter);
			String[] split = currInput.split(" ", 3);
			String symbol = split[0];
			
			hAction = tAction.get(tState).get(symbol);
			action = -1;
			if (hAction == null) action = -32144; // ODBACI
			else if (hAction.toLowerCase().charAt(0) == 's') 	   action = 0; //shift u UTR, Pomakni u PPJ
			else if (hAction.toLowerCase().charAt(0) == 'r')   action = 1; //Reduciraj
			else if (hAction.equals("Prihvati")) action = 2; //Prihvati
			Logger.log("Action is " + hAction + ", " + action);
			
			if (action == 1 || action == 0) nextState = Integer.parseInt(hAction.substring(1));
						
			//Do Action
			switch (action) {
			//shift, to jest Pomakni, to jest s12345 to jest Pomakni(123455)
			case 0:
				tState = nextState;
				hItem = new lifoStackItem(tState, currInput, false);
				tStack.push(hItem);
				++counter;	
				break;
				
			//Reduciraj
			case 1:
				int noRemove = 0;
				hProductLeft  = (String) tTransitions.get(nextState).get("L").get(0);
				noRemove = tTransitions.get(nextState).get("R").size();
				
				Logger.log("Reduce using transition " + hProductLeft + " ::= " + tTransitions.get(nextState).get("R"));
				Logger.log("Removing " + noRemove + " items");
				
				int numJoins = 0;
				List<String> removed = new LinkedList<String>();
				for (int i = 0; i < noRemove; i++) 
				{
					lifoStackItem top = tStack.pop();
					removed.add(top.getSymbol());
					
					if (top.getSymbol().startsWith("<"))
						++numJoins;
				}
				
				if (numJoins > treeBranches.size()) {
					throw new IllegalStateException("Parser has blown up, too few branches");
				}
				
				List<TreeNode> rhs = new LinkedList<TreeNode>();				
				
				int rightmostBranchIndex = treeBranches.size()-1;
				for (String r : removed) {
					if (r.startsWith("<")) {
						rhs.add(0, treeBranches.get(rightmostBranchIndex--));
					} else {
						rhs.add(0, new TreeNode(r, null));
					}
				}
				if (removed.size() == 0)
					rhs.add(0, new TreeNode("$", null));
				
				TreeNode node = new TreeNode(hProductLeft, rhs);
				
				if (treeBranches.size() != 0)
					treeBranches.subList(treeBranches.size() - numJoins, treeBranches.size()).clear();
				treeBranches.add(node);
				
				nextState = tStack.look().getState();
				tState = Integer.parseInt(tAction.get(nextState).get(hProductLeft)); // citaj NovoStanje
				hItem = new lifoStackItem(tState, hProductLeft, true);
				tStack.push(hItem);
								
				break;
				
			//Prihvati
			case 2:
				if (tDebug == 1) System.err.println("LR_PARSER: Niz je u jeziku!");
				running = false;
				treeBranches.get(0).printRecursive(0);
				break;
				
			//Nedefinirani slucaj = ERROR
			default:
				System.err.println("Syntax error on line " + split[1] + ", error token is " + split[2]);
				System.err.print("Expected one of: [");
				for (String possible : tAction.get(tState).keySet()) {
					System.err.print(possible + " , ");
				}
				System.err.println("]");
				Logger.log("Looking for " + sync);
				while (counter < tInput.size() && ! sync.contains(tInput.get(counter).split(" ")[0])) {
					Logger.log("Skipping symbol " + tInput.get(counter));
					counter++;
				}
				String sym = tInput.get(counter).split(" ")[0];
				while (tStack.look() != null && tAction.get(tState).get(sym) == null) {
					Logger.log("in state " + tState);
					tStack.print();
					lifoStackItem top = tStack.pop();
					tState = tStack.look().getState();
					if (top.isSubtreee())
						treeBranches.remove(treeBranches.size()-1);
				}
				if (tStack.look() == null) {
					System.err.println("The syntax error is fatal");
					running = false;
					break;
				} else {
				}
			}
			if (tDebug == 1) tStack.print();
		}
	}
}

class TreeNode {
	String name;
	List<TreeNode> children;
	
	/**
	 * @param name
	 * @param children
	 */
	public TreeNode(String name, List<TreeNode> children) {
		this.name = name;
		this.children = children;
	}
	
	void printRecursive(int depth) {
		StringBuilder out = new StringBuilder(depth + name.length());
		for (int i = 0; i < depth; ++i)
			out.append(' ');
		
		out.append(name);
		
		System.out.println(out.toString());
		if (children != null)
			for (TreeNode node : children)
				node.printRecursive(depth+1);
	}
}



/*
 * LIFO STACK
 */
class lifoStack
{
	private static List<lifoStackItem> tStack;
	
	public lifoStack()
	{
		tStack = new LinkedList<lifoStackItem>();
	}
	
	public void push(lifoStackItem aItem)
	{
		tStack.add(0, aItem);
	}
	
	public lifoStackItem pop()
	{
		if (tStack.size() < 1) return null;
		lifoStackItem hItem = tStack.get(0);
		tStack.remove(0);
		return hItem;
	}
	
	public lifoStackItem look()
	{
		if (tStack.size() < 1) return null;
		return tStack.get(0);
	}
	
	public void print()
	{
		lifoStackItem hItem;
		for (int i = 0; i < tStack.size(); i++) {
			hItem = tStack.get(i);
			
			System.err.print("\""+hItem.getSymbol()+"|"+hItem.getState()+"\", ");
		}
		System.err.print("\n");
	}
}

class lifoStackItem
{
	int tState;
	String tSymbol;
	private boolean subtreee;
	
	public lifoStackItem()
	{
		setState(0);
		setSymbol(new String());
	}
	
	public boolean isSubtreee() {
		return subtreee;
	}
	
	public lifoStackItem(int aState, String aSymbol, boolean subtree)
	{
		setState(aState);
		setSymbol(aSymbol);
		this.subtreee = subtree;
	}
	
	public int getState()
	{
		return tState;
	}
	
	public void setState(int aState)
	{
		tState = aState;
	}
	
	public String getSymbol()
	{
		return tSymbol;
	}
	
	public void setSymbol(String aSymbol)
	{
		tSymbol = aSymbol;
	}
}

