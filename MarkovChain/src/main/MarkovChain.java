/**
 * Git repository: https://github.com/lizhigong/MarkovChain.git 
 * 
 * Markov gussing method.
 * (First order).
 * 
 * p[a|b]=0.1 is represented by a(b) -- 0.1
 * p[a|x0]=0.1 is represented by a() -- 0.1, x0 is the start symbol. 
 * p[xe|a]=0.1 is represented by (a) -- 0.1, xe is the end symbol.
 * 
 * Running order:
 * 		1. training
 * 		2.
 */
package main;
import java.io.*;
import java.util.HashMap;

/**
 * @author Li Zhigong
 *
 */
public class MarkovChain {
	private String trainingSet;		//Each line contains a password/key/entry.
	private int order = 1;		// By default, it is 1-order markov chain.

	private HashMap<String,Integer> map; 	// Stores the probabilities like p[a|b] and p[xe|a].
	private HashMap<String,Integer> begin;	// Stores the probabilities like p[a|x0].

	private String beginProb = null;
	private String midProb = null;
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
	public void training(String traningSet){
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
						key = genKey(charArray[i], charArray[i-1]);
						addtoMap("",map);
					}
					
					/* Deal with end symbol*/
					addtoMap("(" + charArray[len-1] + ")",map);
					
					/* Sort and Write */

				}
				
				br.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
			e.printStackTrace();
		} 

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
	 * Representation of p[str1|str2]
	 * @param str1
	 * @param str2
	 * @return
	 */
	private String genKey(char str1, char str2){
		return str1 + "(" + str2 + ")";
	}
	
	/**
	 * Representation of p[str1|x0]
	 * @param str1
	 * @return
	 */
	private String genKey(char str1){
		return str1 + "()";

	}
	public static void main (String[] args){
		MarkovChain mc = new MarkovChain();
		// Test addtoMap: WORK.
//		HashMap<String,Integer> map = new HashMap<String,Integer>();
//		mc.addtoMap("hello", map);
//		mc.addtoMap("hello", map);
//		System.out.println(map.get("hello"));
	}
}
