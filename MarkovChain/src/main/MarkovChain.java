/**
 * Git repository: https://github.com/lizhigong/MarkovChain.git 
 * 
 * Markov gussing method.
 * (First order).
 * 
 * p[X2|X1]=0.1 is represented by X1(X2) -- 0.1
 * p[X1|X0]=0.1 is represented by (X1) -- 0.1, X0 is the start symbol. 
 * p[Xe|X6]=0.1 is represented by X6() -- 0.1, Xe is the end symbol.
 * 
 * Running order:
 * 		1. training
 * 		2.
 */
package main;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import myUtil.*;
/**
 * @author Li Zhigong
 *
 */
public class MarkovChain {
	private String trainingSet;		//Each line contains a password/key/entry.
	private int order = 1;		// By default, it is 1-order markov chain.
	
	private String splitToken = "---";
	private HashMap<String,Integer> map; 	// Stores the probabilities like p[a|b] and p[xe|a].
	private HashMap<String,Integer> begin;	// Stores the probabilities like p[a|x0].

	private String beginProb = null;
	private String prob = null;
	/* Constructor */
	
	public MarkovChain(){
		begin = new HashMap<String,Integer>();
		map = new HashMap<String,Integer>();
	}
	
	public MarkovChain(int order){
		super();
		this.order = order;
	}

	private void initiate(){
		
	}

	public void training(){
		training(trainingSet);
	}

	/**
	 * Training the markov method using trainingSet.
	 * @param traningSet
	 */
	public void training(String trainingSet){
		this.trainingSet = trainingSet;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(trainingSet));
			
			try {
				String line = br.readLine();
				String key;
				while (line != null){
					line = line.replaceAll(" ", "");	// Ignore spaces.
					
					if (line.length() < 4) 				// At least 4 characters.
						continue;
					
					char[] charArray = line.toCharArray();

					/* Deal with the first letter. */
					key = genKey(charArray[0]);
					addtoMap(key,begin);
					int len = charArray.length;

					/* Deal with the rest letters. */
					for (int i = 1; i < len; i++){
						key = genKey(charArray[i-1], charArray[i]);
						addtoMap(key,map);

						/* Total number of charArray[i-1], used to calculate the probabilities. */
						addtoMap(charArray[i-1] + "*",map);	
					}
					
					/* Deal with last character and the end symbol*/
					addtoMap(charArray[len-1] + "()",map); 				// Add End Symbol.
					addtoMap("end*",map);
					line = br.readLine();
				}
				
				br.close();

				writeBeginProb(begin,"beginProb.txt");
				writeProb(map,"Prob.txt");

				beginProb = "beginProb.txt";
				prob = "Prob.txt";
				
				/* Sort the files */
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
			e.printStackTrace();
		} 
	}

	private void writeProb(HashMap<String,Integer> map, String fileName) throws IOException{
		int size = map.size();
		String[] keys = new String[size];
		int[] frequencies= new int[size];
		
		exportHashmap(map,keys,frequencies);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		int total;
		for (int i = size - 1; i >= 0; i--){
			if (keys[i].endsWith("*")) 		// Ignore the entries ended with '*', because it is the total number.
				continue;

			total = (keys[i].length() == 3) ? map.get("end*") : map.get(keys[i].charAt(0) + "*"); 
			
			bw.write(keys[i] + splitToken + (1.0D * frequencies[i] / total));
			bw.newLine();
		}
		
		bw.close();
	}
	
	/**
	 * Write the <String,Integer> pairs into files,
	 * and change Integer(frequencies) to Double(probabilities).
	 * @param map
	 * @param fileName
	 * @throws IOException
	 */
	private void writeBeginProb(HashMap<String,Integer> map, String fileName) throws IOException{
		int size = map.size();
		String[] keys = new String[size];
		int[] frequencies= new int[size];

		exportHashmap(map,keys,frequencies);

		int total = 0;
		for (int x: frequencies){
			total += x;
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		
		for (int i = size - 1; i >= 0; i--){
			bw.write(keys[i] + splitToken + (1.0D * frequencies[i] / total));
			bw.newLine();
		}
		
		bw.close();
	}
	
	/**
	 * Auxiliary function which adds 1 to map.get(key). 
	 */
	private void addtoMap(String key, HashMap<String,Integer> map){
		Integer x = map.get(key);
		if (x == null)
			x = 0;
		
		map.put(key, x+1);
	}
	
	/**
	 * Representation of p[X2|X1]
	 * @param X1
	 * @param X2
	 * @return
	 */
	private String genKey(char X1, char X2){
		return X1 + "(" + X2 + ")";
	}
	
	/**
	 * Representation of p[X1|X0]
	 * @param X1 
	 * @return
	 */
	private String genKey(char X1){
		return "(" + X1 + ")";
	}

	/**
	 * export a hashmap<String,Integer> to str[] and num[].
	 * And sort them.
	 * It seems that I have to initiate the two strings before using this method.
	 * Because if I initiate the two strings in this method, java will throw a NULL Pointer Exception.
	 * @param hashmap
	 * @param str
	 * @param num
	 */
	public static void exportHashmap(HashMap<String,Integer> hashmap, String[] str, int[] num){
		int size = hashmap.size();
		
		if (size == 0){
			System.out.println("Hashmap is empty!!");
			return;
		}
		
		int k = 0;
		Iterator iter = hashmap.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
			str[k] = entry.getKey();
			num[k++] = entry.getValue();
		}

//		MyQuickSort2.sort(str, num);
	}
	
	public static void main (String[] args){
		MarkovChain mc = new MarkovChain();

		/* Test addtoMap: WORK */
//		HashMap<String,Integer> map = new HashMap<String,Integer>();
//		mc.addtoMap("hello", map);
//		mc.addtoMap("hello", map);
//		System.out.println(map.get("hello"));
		
		/* Test training. */

		mc.training("TestTraining.txt");
	}
}
