/**
 * An entry, which contains a password and its probability.
 */
package main;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * @author Li Zhigong
 *
 */
public class MarkovEntry implements Comparable<MarkovEntry>{
	public double prob;		// Probability
	public String str;		// Password, may be not a complete one.
	public boolean isEnd = false;		// Identify if it is a complete one.
	
	public MarkovEntry(){}		//Empty Constructor.
	
	public MarkovEntry(String str, double p){
		this.str = str;
		this.prob = p;
	}
	
	public MarkovEntry(String str, double p, boolean isEnd){
		this.str = str;
		this.prob = p;
		this.isEnd = isEnd;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/* Test in Priority Queue. */
		PriorityQueue<MarkovEntry> queue = new PriorityQueue<MarkovEntry>(1000);
		MarkovEntry a = new MarkovEntry();
		MarkovEntry b = new MarkovEntry();
		MarkovEntry c = new MarkovEntry();
			
		b.prob = Math.random() * 10;
		c.prob = Math.random() * 10;
		a.prob = Math.random() * 10;

		queue.add(b);
		queue.add(a);
		queue.add(c);
	
		PriorityQueue<MarkovEntry> queue2 = new PriorityQueue<MarkovEntry>(queue);
		
		System.out.println(queue2.poll().prob);
		System.out.println(queue2.poll().prob);
		System.out.println(queue2.poll().prob);
	}

	@Override
	public int compareTo(MarkovEntry o) {
		if (o.prob > prob)
			return 1;
		else if (o.prob < prob)
			return -1;
		else 
			return 0;
		
	}
}