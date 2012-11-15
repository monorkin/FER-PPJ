package hr.unizg.fer.zemris.ppj.maheri.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class LRparser {
	
	private static int tDebug = 1;
	
	
	private static lifoStack tStack;
	private static Map<Integer, Map <String, String>> tAction;
	private static Map<Integer, Map<String, HashSet<String>>> tTransitions;
	private static ArrayList<String> tInput;
	private static int tState;
	
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
	public LRparser(ArrayList<String> aInput, Map<Integer, Map <String, String>> actionsTable, Map<Integer, Map <String, HashSet<String>>> aTransitions) 
	{
		tStack    	 = new lifoStack();
		tAction   	 = actionsTable;
		tTransitions = aTransitions;
		tInput    	 = aInput;
	}
	
	//Parsiraj niz
	public static void parse()
	{
		String hAction;
		String hProductLeft;
		lifoStackItem hItem;
		boolean running = true;
		int counter = 0;
		int action = -1;
		int nextState = 0;
		tState = 0;
		
		tInput.add("_|_");
		tStack.push(new lifoStackItem(0, "_|_"));
		
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
				hProductLeft  = (String) tTransitions.get(nextState).get("L").toArray()[0];
				noRemove = tTransitions.get(nextState).get("R").size();
								
				for (int i = 0; i < noRemove; i++) 
				{
					//TODO Napravi stablo
					tStack.pop();
				}
				
				nextState = tStack.look().getState();
				tState = Integer.parseInt(tAction.get(nextState).get(hProductLeft));
				hItem = new lifoStackItem(tState, hProductLeft);
				tStack.push(hItem);
								
				break;
				
			//Prihvati
			case 2:
				if (tDebug == 1) System.out.println("LR_PARSER: Niz je u jeziku!");
				running = false;
				break;
			
			//Nedefinirani slucaj = ERROR
			default:
				if (tDebug == 1) System.out.println("LR_PARSER: Niz nije u jeziku!");
				running = false;
				break;
			}
			if (tDebug == 1) tStack.print();
		}	
	}
}



/*
 * LIFO STACK
 */
class lifoStack
{
	private static ArrayList<lifoStackItem> tStack;
	
	public lifoStack()
	{
		tStack = new ArrayList<lifoStackItem>();
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
			
			System.out.print("\""+hItem.getSymbol()+""+hItem.getState()+"\", ");
		}
		System.out.print("\n");
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

