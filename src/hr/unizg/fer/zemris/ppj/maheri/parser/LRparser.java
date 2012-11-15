package hr.unizg.fer.zemris.ppj.maheri.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LRparser {
	
	private static lifoStack tStack;
	private static Map<Integer, Map <String, String>> tAction;
	private static Map<Integer, Map <String, String>> tNewState;
	private static String tOutput;
	
	//Inicijalizacija
	//Prima ulazni niz znakova, te Map<Integer, Map<String, String>> 
	// Objasnjenje ::: Integer = broj retka; Mapa = sadrzaj retka -> prvi string = naslov stupca (znak) -> drugi string = akcija
	//				   drzim se sintakse iz UTR ako da drugi string sadrzi "rX", "lX" ili "prihvati"
	public LRparser(String inputString, Map<Integer, Map <String, String>> actionsTable, Map<Integer, Map <String, String>> newStatesTable) 
	{
		tStack    = new lifoStack();
		tAction   = actionsTable;
		tNewState = newStatesTable;
		tOutput   = new String();
		
		//Parsiraj
		lrParse();
	}
	
	//Parsiraj niz
	private static void lrParse()
	{
		boolean running = true;
		while (running)
		{
			
		}
		
	}
	
	//Reduciraj
	private static void Reduce()
	{
		
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
	
	public void add(lifoStackItem aItem)
	{
		tStack.add(0, aItem);
	}
	
	public lifoStackItem remove()
	{
		if (tStack.size() < 1) return null;
		lifoStackItem hItem = tStack.get(0);
		tStack.remove(0);
		return hItem;
	}
	
	public void print()
	{
		lifoStackItem hItem;
		for (int i = 0; i < tStack.size(); i++) {
			hItem = tStack.get(i);
			
			System.out.println("{"+hItem.getState()+", "+hItem.getSymbol()+"}");
		}
	}
}

class lifoStackItem
{
	static String tState;
	static String tSymbol;
	
	public lifoStackItem()
	{
		setState(new String());
		setSymbol(new String());
	}
	
	public lifoStackItem(String aState, String aSymbol)
	{
		setState(aState);
		setSymbol(aSymbol);
	}
	
	public String getState()
	{
		return tState;
	}
	
	public void setState(String aState)
	{
		lifoStackItem.tState = aState;
	}
	
	public String getSymbol()
	{
		return tSymbol;
	}
	
	public void setSymbol(String aSymbol)
	{
		lifoStackItem.tSymbol = aSymbol;
	}
}

