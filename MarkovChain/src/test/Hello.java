/**
 * 
 */
package test;
import java.io.*;
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
		BufferedReader br = new BufferedReader(new FileReader("D:/tianya_total_index_2.txt"));
		
		String line;
		
		for (int i = 1; i < 11; i++){
			line = br.readLine();
			
			System.out.println(line);
		}
		
		br.close();	
	}

}
