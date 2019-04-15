import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.LinkedHashSet;

public class TwoPassLinker {
	
	public static void main(String[] args) throws FileNotFoundException{
		Scanner in = new Scanner(System.in);
		ArrayList<String> data = new ArrayList<String>();
		//continues to take input until reaching the word "stop"
		/*while(in.hasNext()){
			data.add(in.next());
			if(data.get(data.size() - 1).equals("stop")){
				break;
			}
		}
		data.remove(data.size() - 1); */
		
		int moduleCount = in.nextInt();
		data.add(Integer.toString(moduleCount));
		for(int i=0; i<moduleCount; i++){
			
			//parse through symbol defs input
			int defCount = in.nextInt(); 
			data.add(Integer.toString(defCount));
			for(int j=0; j<2 * defCount; j++){
				data.add(in.next());
			}
			
			//parse through definition uses input
			int defUses = in.nextInt();
			data.add(Integer.toString(defUses));
			while(defUses != 0){
				String cur = in.next();
				data.add(cur);
				if(cur.equals("-1")) defUses--;
			}
			
			int addressCount = in.nextInt();
			data.add(Integer.toString(addressCount));
			for(int j=0; j<addressCount; j++){
				data.add(in.next());
			}
		}
		in.close();
		
		String[] dataArray = data.toArray(new String[0]);
		
		FirstPassData firstPassRes = firstPass(dataArray);
		if(!firstPassRes.symbolHashMap.keySet().isEmpty()) System.out.println("\nSymbol Table");
		for(String key : firstPassRes.symbolHashMap.keySet()){
			System.out.printf("%1$s%2$d\n", key + "=", firstPassRes.symbolHashMap.get(key));
		}
		System.out.println();
		
		ArrayList<Integer> secondPassRes = secondPass(dataArray, firstPassRes);
		System.out.println("\nMemory Map");
		for(int i=0; i<secondPassRes.size(); i++){
			System.out.printf("%1$-4s%2$d\n", i + ":", secondPassRes.get(i));
		}
		
		
	}
	
	static FirstPassData firstPass(String[] data){
		FirstPassData ret = new FirstPassData();
		ArrayList<String> symbols = new ArrayList<String>();
		LinkedHashMap<String, Integer> symbolHashMap = new LinkedHashMap<String, Integer>();
		//addresses stores the base address of each module - index 0 is module 0, with base 0, index 1 is module 1, base 5, etc
		ArrayList<Integer> addresses = new ArrayList<Integer>();
		addresses.add(0);
		
		int moduleCount = Integer.parseInt(data[0]);
		int totalAddresses = 0;
		int index = 1;
		
		//loop iterates once per module grouping
		for(int i=0; i<moduleCount; i++){
			//check definition row
			int defItrCount = 2 * Integer.parseInt(data[index]);
			//loop is skipped if defItrCount = 0, i.e. no defs in module
			for(int j=0; j<defItrCount; j++){
				String varName = data[index];
				if(symbolHashMap.keySet().contains(varName)){
					System.out.printf("Error: %s is multiply defined; value of last definition used.\n", varName);
				}
				index++;
				if(j%2==0){
					symbols.add(data[index]);
				}
				else{
					int val;
					if(i==0){
						val = Integer.parseInt(data[index]);
					}
					else{
						val = Integer.parseInt(data[index]) + (addresses.get(i));
					}
					symbols.add(Integer.toString(val));
					symbolHashMap.put(varName, val);
				}
			}
			index++;
			
			//skip past definition uses by using the -1s
			int useCount = Integer.parseInt(data[index]); //gives us number of -1s to look for
			while(useCount != 0){
				index++;
				if(data[index].equals("-1")) useCount--;
			}
			index++;
			
			//skip past addresses, but count how many
			totalAddresses += Integer.parseInt(data[index]);
			//error check that no definitions exist above module size
			for(int j=0; j<defItrCount/2; j++){
				if(Integer.parseInt(symbols.get(symbols.size() - ((j*2)+1))) > totalAddresses - 1){
					System.out.printf("Error: symbol %s definition exceeds module size; last word in module used.\n", symbols.get(symbols.size() - (j+2)));
					symbolHashMap.put(symbols.get(symbols.size() - ((j*2)+2)), totalAddresses - 1);
					symbols.set(symbols.size() - (j+2), Integer.toString(totalAddresses - 1));
				}
			}
			addresses.add(totalAddresses);
			index += 1 + Integer.parseInt(data[index]);
		}
		
		String[] symbolTable = symbols.toArray(new String[0]);
		ret.symbolTable = symbolTable;
		ret.addressTable = addresses;
		ret.symbolHashMap = symbolHashMap;
		return ret;
	}
	
	static ArrayList<Integer> secondPass(String[] data, FirstPassData fpd){
		ArrayList<Integer> values = new ArrayList<Integer>();
		int moduleCount = Integer.parseInt(data[0]);
		int index = 1;
		//used to error check for symbols that are defined and not used
		Set<String> keysDefined = new LinkedHashSet<String>(fpd.symbolHashMap.keySet());
		for(int i=0; i<moduleCount; i++){
			//skip past definitions section
			int defsItrCount = 2 * Integer.parseInt(data[index]);
			index += 1 + defsItrCount;	//index now at number of def uses
				
			//iterate over var uses to generate hashmap of which variables are used at what address
			int usesCount = Integer.parseInt(data[index]);
			//HashMap contains a key that is the var name, and value that is an arraylist of the location of its uses
			LinkedHashMap<String, ArrayList<Integer>> defUses = new LinkedHashMap<String, ArrayList<Integer>>();
			index++; //brings index to name of first variable, or number of addresses if usesCount = 0
			for(int j=0; j<usesCount; j++){
				String varName = data[index];
				ArrayList<Integer> varUseAddresses = new ArrayList<Integer>();

				index++;
				while(!data[index].equals("-1")){
					varUseAddresses.add(Integer.parseInt(data[index]));
					index++;
				}
				defUses.put(varName, varUseAddresses);
				index++; //sets index to name of next var, or number of addresses if end of use list
				
				keysDefined.remove(varName);
				
			}

			//iterate through addresses, with different modifications depending on last digit of each item
			int addressCount = Integer.parseInt(data[index]);
			for(int j=0; j<addressCount; j++){
				index++;
				int address = Integer.parseInt(data[index]);
				int addressType = address % 10;
				address /= 10; //strip out address type
				int firstDigit = address;
				while(firstDigit >= 10){
					firstDigit /= 10;
				}
				
				switch(addressType){
				//leaves address alone
				case 1:
					values.add(address);
					break;
				//leaves address alone unless >= 300
				case 2:					
					if(address - (firstDigit * 1000) >= 300){
						values.add(firstDigit * 1000 + 299);
						System.out.printf("Error: Absolute address at entry %d exceeds machine size; largest legal value used.\n", j + fpd.addressTable.get(i));
					}
					else values.add(address);
					break;
				//add based on base address of module
				case 3:
					address += fpd.addressTable.get(i);
					if(address - (firstDigit * 1000) >= 300){
						values.add(firstDigit * 1000 + 299);
						System.out.printf("Error: Absolute address at entry %d exceeds machine size; largest legal value used.\n", j + fpd.addressTable.get(i));
					}
					else values.add(address);
					break;
				//reassign based on external symbol values
				case 4:
					//find match for j (the relative address) in the previously created hash map
					String[] keys = defUses.keySet().toArray(new String[defUses.keySet().size()]);
					ArrayList<ArrayList<Integer>> vals = new ArrayList<ArrayList<Integer>>(defUses.values());
					
					//iterate over all addresses declared to use symbols and find last occuring match case
					int counter = 0;
					int[] idxOfMatch = new int[2];
					String symbolOfMatch;
					int symbolValue;
					for(int k=0; k<vals.size(); k++){
						for(int l=0; l<vals.get(k).size(); l++){
							if(vals.get(k).get(l) == j){
								counter++;
								idxOfMatch[0] = k;
								idxOfMatch[1] = l;
								symbolOfMatch = keys[k];
								if(fpd.symbolHashMap.get(symbolOfMatch) != null){
									symbolValue = fpd.symbolHashMap.get(symbolOfMatch);
								}
								else {
									System.out.printf("Error: %s is not defined, 111 used for entry %d.\n", symbolOfMatch, j + fpd.addressTable.get(i));
									symbolValue = 111;
								}
								
								address = (firstDigit * 1000) + symbolValue;
							}
						}
					}
					values.add(address);
					if(counter > 1) System.out.printf("Error: multiple symbols used for same instruction; resolved to last instruction %d.\n", address);
					break;
					
				default:
					values.add(address);
					break;
				}
			}
			index++;
		}
		if(!keysDefined.isEmpty()){
			for(String key : keysDefined){
				System.out.printf("Warning: %s is defined but not used.\n", key);
			}
		}	
		return values;
	}
}

class FirstPassData {
	String[] symbolTable; //used for error checking
	ArrayList<Integer> addressTable;
	LinkedHashMap<String, Integer> symbolHashMap;
}
