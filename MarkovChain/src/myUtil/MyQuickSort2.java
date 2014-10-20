/**
 * Sort two arrays str[] and num[] according to num[].
 */
package myUtil;


/**
 * @author Li Zhigong
 *
 */
public class MyQuickSort2 {

	public static void sort(String[] str, int[] a){
		if (str.length != a.length){
			System.out.println("length of strings and length of integers should be same!");
		}
		else sort(str,a,0,a.length-1);
	}
	
	public static void sort(String[] str, int[] a, int lo, int hi){
		if (lo >= hi) return;
		
		int j = partition(str,a,lo,hi);
		sort(str,a,lo,j-1);
		sort(str,a,j+1,hi);
	}
	
	private static int partition(String[] str,int[] a,int lo, int hi){
		int i = lo;
		int j = hi + 1;	//ATTENTION: Why j = hi + 1? Related to --j!!
		int v = a[lo];
		
		while (true){
			
			while (a[++i] < v)
				if (i == hi) break;
			
			while (v < a[--j])
				if (j == lo) break;
			
			if (i >= j) break;
			
			exch(a,i,j);
			exchstr(str,i,j);
			
		}
		
		exch(a,lo,j);
		exchstr(str,lo,j);
		
		return j;
		
	}
	
	private static void exch(int[] a,int i, int j){
		int swap = a[i];
		a[i] = a[j];
		a[j] = swap;
	}
	
	private static void exchstr(String[] a,int i, int j){
		String swap = a[i];
		a[i] = a[j];
		a[j] = swap;
	}
	
	public static void show(String[] str,int[] a){
		for (int i = 0, len = a.length; i < len; i++){
			System.out.print(str[i] + " ");
		}
		System.out.println();
		for (int i = 0, len = a.length; i < len; i++){
			System.out.print(a[i] + " ");
		}
		System.out.println();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int[] needSort = {2,5,3,6,6,4,4,8,6,2,3,1,0,8,5,4,3,2};
		String[] needStr = new String[needSort.length];
		for (int i = 0, len = needSort.length; i < len; i++){
			needStr[i]  = (char)((int)('a') + i) + "";
		}
		show(needStr,needSort);
		MyQuickSort2.sort(needStr,needSort);
		show(needStr,needSort);
	}

}