package hr.unizg.fer.zemris.ppj.maheri.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LRparser {
	
	private int tDebug = 1;
	
	
	private lifoStack tStack;
	private Map<Integer, Map <String, String>> tAction;
	private Map<Integer, Map<String, List<String>>> tTransitions;
	private ArrayList<String> tInput;
	private int tState;
	
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
	public LRparser(ArrayList<String> aInput, Map<Integer, Map <String, String>> actionsTable, Map<Integer, Map <String, List<String>>> aTransitions) 
	{
		tStack    	 = new lifoStack();
		tAction   	 = actionsTable;
		tTransitions = aTransitions;
		tInput    	 = aInput;
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
		tState = 0;
		
		tInput.add("\0");
		tStack.push(new lifoStackItem(0, "\0"));
		
		ArrayList<TreeNode> treeBranches = new ArrayList<TreeNode>();
		
		while (running)
		{
			//Get action
			hAction = tAction.get(tState).get(tInput.get(counter));
			action = -1;
			if (hAction.toLowerCase().charAt(0) == 's') 	   action = 0; //Stavi
			else if (hAction.toLowerCase().charAt(0) == 'r')   action = 1; //Reduciraj
			else if (hAction.toLowerCase().equals("prihvati")) action = 2; //Prihvati
			
			if (action != 2) nextState = Integer.parseInt(hAction.substring(1));
						
			//Do Action
			switch (action) {
			//Pomak
			case 0:
				tState = nextState;
				hItem = new lifoStackItem(tState, tInput.get(counter));
				tStack.push(hItem);
				++counter;	
				break;
				
			//Reduciraj
			case 1:
				int noRemove = 0;
				hProductLeft  = (String) tTransitions.get(nextState).get("L").get(0);
				noRemove = tTransitions.get(nextState).get("R").size();
								
				
				int numJoins = 0;
				List<String> removed = new LinkedList<String>();
				for (int i = 0; i < noRemove; i++) 
				{
					//TODO Napravi stablo
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
				
				TreeNode node = new TreeNode(hProductLeft, rhs);
				
				if (treeBranches.size() != 0)
					treeBranches.subList(treeBranches.size() - numJoins, treeBranches.size()).clear();
				treeBranches.add(node);
				
				nextState = tStack.look().getState();
				tState = Integer.parseInt(tAction.get(nextState).get(hProductLeft));
				hItem = new lifoStackItem(tState, hProductLeft);
				tStack.push(hItem);
								
				break;
				
			//Prihvati
			case 2:
				if (tDebug == 1) System.err.println("LR_PARSER: Niz je u jeziku!");
				running = false;
				break;
			
			//Nedefinirani slucaj = ERROR
			default:
				if (tDebug == 1) System.err.println("LR_PARSER: Niz nije u jeziku!");
				running = false;
				break;
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
		
		System.err.println(out.toString());
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
			
			System.err.print("\""+hItem.getSymbol()+""+hItem.getState()+"\", ");
		}
		System.err.print("\n");
	}
}

class lifoStackItem
{
	int tState;
	String tSymbol;
	
	public lifoStackItem()
	{
		setState(0);
		setSymbol(new String());
	}
	
	public lifoStackItem(int aState, String aSymbol)
	{
		setState(aState);
		setSymbol(aSymbol);
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

