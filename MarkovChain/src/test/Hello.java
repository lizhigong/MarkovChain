/**
 * 
 */
package test;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

/**
 * @author Li Zhigong
 *
 */
public class Hello {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		/* Test Priority Queue. */
		PriorityQueue pq = new PriorityQueue();
		BufferedWriter bw = new BufferedWriter(new FileWriter("test.txt"));
		double x = 0.0000000000000003112;
		double y = 3.112E-10;
		System.out.println(x > y);
		bw.write(x+"");
		bw.close();
	}

}
