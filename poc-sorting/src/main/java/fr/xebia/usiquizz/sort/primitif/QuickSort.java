package fr.xebia.usiquizz.sort.primitif;

/**
 * QuickSort min=O(N log N) max=O(N2) avg=O(N log N) Good, but it worst case is
 * O(N2)
 * 
 * @author Erwan ALLIAUME
 */
public class QuickSort {

	public static void sort(int[] array) {
		sort(array, 0, array.length - 1);
	}

	private static void sort(int array[], int low, int n) {
		int lo = low;
		int hi = n;
		if (lo >= n) {
			return;
		}
		int mid = array[(lo + hi) / 2];
		while (lo < hi) {
			while (lo < hi && array[lo] < mid) {
				lo++;
			}
			while (lo < hi && array[hi] > mid) {
				hi--;
			}
			if (lo < hi) {
				int T = array[lo];
				array[lo] = array[hi];
				array[hi] = T;
			}
		}
		if (hi < lo) {
			int T = hi;
			hi = lo;
			lo = T;
		}
		sort(array, low, lo);
		sort(array, lo == low ? lo + 1 : lo, n);
	}
}