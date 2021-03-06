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
 * 		2. guessing
 */
package main;
import java.io.*;
import java.util.*;
import java.sql.*;
/**
 * @author Li Zhigong
 *
 */
public class MarkovChain {
	private String trainingSet = null;		//Each line contains a password/key/entry.
	private int order = 1;		// By default, it is 1-order markov chain.
	
	private String splitToken = "---";
	private HashMap<String,Integer> map; 	// Stores the probabilities like p[a|b] and p[xe|a].
	private HashMap<String,Integer> begin;	// Stores the probabilities like p[a|x0].

	private String beginProb = null;
	private String prob = null;
	private String[] printableChar = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z",
									  "A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
									  "1","2","3","4","5","6","7","8","9","0","-","=","!","@","#","$","%","^","&","*","(",")","_","+",
									  "~","`","[","]","{","}",":","\'",";","\"",",",".","/","<",">","?","\\","|"," ",""}; 
									  // the null string at the end is used to represent the end symbol.
	
	long[][] point = new long[62][2];		// 10, 100, 1000 ......
	/* Constructor */
	
	public MarkovChain(){
		begin = new HashMap<String,Integer>();
		map = new HashMap<String,Integer>();
		initiatePoint();
	}
	
	public MarkovChain(int order){		// Now, order does not work.
		super();
		this.order = order;
	}

	public void training(){
		training(trainingSet);
	}

	private void initiatePoint(){
		point[0][0] = 0;
		point[0][1] = 1;
		int k = 1;
		int E = 7;
		
		for (int i = 1; i <= 7; i++){
			point[i][0] = i;
			point[i][1] = point[i-1][1] * 10;
		}
		
		for (int i = 7; i < point.length; i++){
			point[i][0] = E;
			point[i][1] = (long)(k * Math.pow(10, E));
			k++;
			
			if (k == 10){
				E++;
				k = 1;
			}
		}
	}
	
	/**
	 * Training the markov method using trainingSet.
	 * @param traningSet
	 */
	public void training(String trainingSet){
		this.trainingSet = trainingSet;
		System.out.print("Training......");
		long time = System.currentTimeMillis();
		try {
			BufferedReader br = new BufferedReader(new FileReader(trainingSet));
			
			try {
				String key;
				String line;
				while ((line=br.readLine()) != null){
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

		System.out.println("Time:" + operateTime(System.currentTimeMillis() - time));
	}

	/**
	 * Write the guesses into file.
	 * Finally, it is used to measure the efficiency of Markov Chain.
	 * 
	 * The guessing method is based on threshold search algorithm.
	 * In the i'th iteration, it generates passwords with probabilities
	 * in a range (p_i,t_i], which starts with p_1 = 1/n, t_1 = 1.
	 * After i'th iteration, we have p_{i+1} = p_i / max (2, 1.5n/m).
	 * That is, when m < 0.75n, we halve the probability threshold.
	 * 
	 * We may overshoot and generate more than n passwords, but are very
	 * unlikely to generate over 2n guesses.
	 * 
	 * @param guessnumbers, the number we want to guess.
	 */
	public void guessTest(long guessnumbers) throws Exception{
		/* Check, because we need training first. */ 
		check();
		int pointer = 1;		// Indicate which point (level of guessnumber) we arrive. 
		double p_i = 1 / guessnumbers;	// Threshold, lower bound.
		double t_i = 1;					// Threshold, upper bound. 
		BufferedWriter bw = new BufferedWriter(new FileWriter("guess.txt"));
		long m = 0;		// Current guess numbers
		HashMap<String, Double> map = new HashMap<String, Double>(10000);		// The distribution of P[X_2| X_1]. for 1-order Markov chain, the # of entries is less than 10000
		
		long initTime = System.currentTimeMillis();
		System.out.print("Initiating map......");
		/* Initiate map */
		BufferedReader br = new BufferedReader(new FileReader(prob));
		String line = br.readLine();
		String[] split;
		
		while ((line = br.readLine()) != null)
			if (line.length() > 0){
				split = line.split(splitToken);
				/* Check each line of the file. */
				if (split.length != 2){		// This should not happen. If this happens, there must be something wrong.
					System.out.println("[WRONG]: " + line);
					System.exit(0);
				}
				
				map.put(split[0], Double.parseDouble(split[1]));
			}
		
		br.close();

		long usedTime = System.currentTimeMillis() - initTime;
		System.out.println(operateTime(usedTime));
		initTime = System.currentTimeMillis();
		System.out.print("initiating map of first characters......");
		
		
		/* Initiate priority queue which contains the probabilities of the first characters */
		PriorityQueue<MarkovEntry> initQue= new PriorityQueue<MarkovEntry>(100);		// 100 is enough, there are only 95 printable characters.
		br = new BufferedReader(new FileReader(beginProb));
		line = br.readLine();
		
		while (line != null){
			if (line.length() > 0){
				split = line.split(splitToken);
				/* Check each line of the file. */
				if (split.length != 2){		// This should not happen. If this happens, there must be something wrong.
					System.out.println("[WRONG]: " + line);
					System.exit(0);
				}

				MarkovEntry me = new MarkovEntry();
				
				/* According to the format of the files, the characters are in parentheses, so we only need the charAt(1)*/
				me.str = split[0].charAt(1)+"";
				me.prob = Double.parseDouble(split[1]);

				initQue.add(me);
			}
			line = br.readLine();
		}
		
		br.close();

		usedTime = System.currentTimeMillis() - initTime;
		System.out.println(operateTime(usedTime));
		
		System.out.println("Guessing by iteration......");
		int iterCount = 1;
		
		long guessInitTime = System.currentTimeMillis();		// Used in guessing, separated with other time.
		
		/* Generating guesses by iteration */
		while (m < guessnumbers){
			initTime = System.currentTimeMillis();
			System.out.print("Iteration " + iterCount + "......");
			/* Deal with the priority queue. */
			PriorityQueue<MarkovEntry> que = new PriorityQueue<MarkovEntry>(initQue);		// The queue is initiated with the initQue, which contains the beginning characters.
			
			/* 1. Pop from priority queue, check the end symbol.
			 * 2. If it is the end symbol, then it is a guess. Repeat 1. Else go 3.
			 * 3. If it is not the end symbol, generate keys whose length is current_len+1, if their probabilities are in the iteration scope, put them into the queue. 
			 */
			while (!que.isEmpty()){
				MarkovEntry me = que.poll();
				if (me.isEnd){		// This is the guess.
					if (me.prob <= t_i && me.str.length() >=4){
						m++;

//						bw.write(me.str);
//						bw.newLine();
						
						if (m == point[pointer][1]){
							int y = (int)(m / Math.pow(10, point[pointer][0]));
							String print = String.format("Generate %dE%d guesses, total time : %s" , y, point[pointer][0],operateTime(System.currentTimeMillis() - guessInitTime));
							System.out.println(print);
							pointer++;
						}

						if (m > guessnumbers){
							System.out.print("Finish......");
							System.out.println(operateTime(System.currentTimeMillis() - guessInitTime));
							bw.close();
							System.exit(0);
						}
					}
				}
				else if (me.str.length() >  30){
					continue;
				}
				else{		// Generate other keys with length + 1
					/* Get the next character and their probability from hashmap. */
					System.out.println("\t" + que.size() + ":" + me.str + ":" + me.prob);
					char last = me.str.charAt(me.str.length()-1);	// Last character.
					for (String c : printableChar){		// E.g. last character is 'a', find a(*) in the hashmap.
						String key = last + "(" + c + ")";
						Double p = map.get(key);
						if (p != null){
							p *= me.prob;
							/* Test if they are in the scope: (p_i,t_i] */
							if (p > p_i)
								que.add(c.equals("") ? new MarkovEntry(me.str + c, p, true) : new MarkovEntry(me.str + c, p));
						}
					}
				}
				
			}		// End of !que.isEmpty().
				
			/* Generate pi and ti */
			iterCount++;
			t_i = p_i;
			p_i = Double.max(2.0, 1.5D * guessnumbers / m);
			
			usedTime = System.currentTimeMillis() - initTime;
			System.out.println(operateTime(usedTime));

		} // WHILE m < GUESSNUMBERS
		System.out.println("guessnumbers: " + m);
		bw.close();
	}

	/**
	 * guess $guessnumbers times and compare with the passwords in $tableName.
	 * @param guessnumbers
	 * @param tableName
	 */
	public void guess(long guessnumbers, String tableName) throws Exception{
		check();	
		/* Store the passwords in the hashmap. For now, there is no better solution.  Code here. */
		
		/* Generate passwords and test in the hashmap. */
		double p_i = 1 / guessnumbers;
		double t_i = 1;
		
		long m = 0;
		long hit = 0;
		
		/* Initiate map. */
		HashMap<String, Double> map = new HashMap<String, Double>(10000);
		BufferedReader br = new BufferedReader(new FileReader(prob));
		String line;
		String[] split;
		
		while ((line = br.readLine()) != null)
			if (line.length() > 0){
				split = line.split(splitToken);
				map.put(split[0], Double.parseDouble(split[1]));
			}
	
		br.close();
		
		/* Initiate intitial queue. */
		PriorityQueue<MarkovEntry> initQue = new PriorityQueue<MarkovEntry>(100);
		br = new BufferedReader(new FileReader(beginProb));
		
		while ((line = br.readLine()) != null)
			if (line.length() > 0){
				split = line.split(splitToken);
				
				MarkovEntry me = new MarkovEntry();
				me.str = split[0].charAt(1) + "";
				me.prob = Double.parseDouble(split[1]);
				initQue.add(me);
			}
			
		br.close();
		
		/* Genrating guesses by iteration. */
		int iterCount = 1;
		while (m < guessnumbers){
			System.out.println("Iteration: " + iterCount);
			/* Deal with the priority queue. */
			PriorityQueue<MarkovEntry> que = new PriorityQueue<MarkovEntry>(initQue);
			
			while (!que.isEmpty()){
				MarkovEntry me = que.poll();
				if (me.isEnd){
					if (me.prob <= t_i && me.str.length() >= 4){
						m++;
						
						/* Check it in the hash map. Code here. */
						if (true){
							hit += 2;
						}
						
						if (m > guessnumbers){
							System.out.println("Finnish.");
							System.exit(0);
						}
					}
				}
				else{
					char last = me.str.charAt(me.str.length()-1);
					for (String c : printableChar){
						String key = last + "(" + c + ")";
						Double p = map.get(key);
						
						if (p != null){
							p *= me.prob;
							if (p > p_i)
								que.add(c.equals("") ? new MarkovEntry(me.str + c, p, true): new MarkovEntry(me.str + c, p));
						}
					}

				}
			}
			iterCount++;
			t_i = p_i;
			p_i = Double.max(2.0, 1.5D * guessnumbers / m);
		}
		System.out.println("guessnumbers: " + m);

	}

	
	/**
	 * Guess $guessnumbers times and compare with passwords in $file.
	 * @param guessnumbers
	 * @param file
	 */
	public void guess(long guessnumbers, File file) throws Exception{
		check();	
		/* Store the passwords in the hashmap. For now, there is no better solution. Code here */
		
		/* Generate passwords and test in the hashmap. */
		double p_i = 1 / guessnumbers;
		double t_i = 1;
		
		long m = 0;
		long hit = 0;
		
		/* Initiate map. */
		HashMap<String, Double> map = new HashMap<String, Double>(10000);
		BufferedReader br = new BufferedReader(new FileReader(prob));
		String line;
		String[] split;
		
		while ((line = br.readLine()) != null)
			if (line.length() > 0){
				split = line.split(splitToken);
				map.put(split[0], Double.parseDouble(split[1]));
			}
	
		br.close();
		
		/* Initiate intitial queue. */
		PriorityQueue<MarkovEntry> initQue = new PriorityQueue<MarkovEntry>(100);
		br = new BufferedReader(new FileReader(beginProb));
		
		while ((line = br.readLine()) != null)
			if (line.length() > 0){
				split = line.split(splitToken);
				
				MarkovEntry me = new MarkovEntry();
				me.str = split[0].charAt(1) + "";
				me.prob = Double.parseDouble(split[1]);
				initQue.add(me);
			}
			
		br.close();
		
		/* Genrating guesses by iteration. */
		int iterCount = 1;
		while (m < guessnumbers){
			System.out.println("Iteration: " + iterCount);
			/* Deal with the priority queue. */
			PriorityQueue<MarkovEntry> que = new PriorityQueue<MarkovEntry>(initQue);
			
			while (!que.isEmpty()){
				MarkovEntry me = que.poll();
				if (me.isEnd){
					if (me.prob <= t_i && me.str.length() >= 4){
						m++;
						
						/* Check it in the hash map. Code here. */
						if (true){
							hit += 2;
						}
						
						if (m > guessnumbers){
							System.out.println("Finnish.");
							System.exit(0);
						}
					}
				}
				else{
					char last = me.str.charAt(me.str.length()-1);
					for (String c : printableChar){
						String key = last + "(" + c + ")";
						Double p = map.get(key);
						
						if (p != null){
							p *= me.prob;
							if (p > p_i)
								que.add(c.equals("") ? new MarkovEntry(me.str + c, p, true): new MarkovEntry(me.str + c, p));
						}
					}

				}
			}
			iterCount++;
			t_i = p_i;
			p_i = Double.max(2.0, 1.5D * guessnumbers / m);
		}
		System.out.println("guessnumbers: " + m);	
	}
	
	/**
	 * Before guess, check the conditions.
	 * 0: OK
	 * 1: no training set.
	 * 2: haven't training.
	 * @return
	 */
	private void check(){
		System.out.println("Checking....");

		int status = 0;
		if (trainingSet == null)
			status = 1;
			
		if (beginProb == null || prob == null)
			status = 2;
		
		switch(status){
		case 1:
			System.out.println("No training set.");
			System.exit(0);
			break;
		case 2:
			System.out.println("Need training.");
			System.exit(0);
			break;
		default:
			System.out.println("Checking Pass.");
		}

	}
	
	/**
	 * Write the frequencies of the keys into files, changing the frequencies to probabilities. 
	 * @param map
	 * @param fileName
	 * @throws IOException
	 */
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
	}

	/**
	 * Change the time (ms) to appropriate format.
	 * @param time
	 * @return
	 */
	private static String operateTime(long time){
		long ms;
		long s;
		long min;
		long h;
		long day;
		
		ms = time % 1000;
		s = time / 1000;
		
		min = s / 60;
		s = s % 60;
		
		h = min / 60;
		min = min % 60;
		
		day = h / 24;
		h = h % 24;

		if (day != 0)
			return day + " day " + h + " h";

		if (h != 0)
			return h + " h " + min + " min";
		
		if (min != 0)
			return min + " min " + s + " s";
		
		if (s != 0)
			return s + " s " + ms + " ms";
		
		return ms + " ms";
	}
	
	public static void main (String[] args) throws Exception{
		MarkovChain mc = new MarkovChain();

		/* Test training. */
		mc.training("TestTraining.txt");
		
		/* Test guessing */
		mc.guessTest(100000000l);
		
	}
}
