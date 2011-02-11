package fr.xebia.usiquizz.sort.primitif;

/**
 * BubbleSort min=O(N) max=O(N2) avg=O(N2) Not a good sort, except with ideal
 * data.
 * 
 * @author Erwan ALLIAUME
 */
public class BubbleSort {

	public static void sort(int[] array) {
		sort(array, array.length);
	}

	private static void sort(int a[], int n) {
		int i, j, t = 0;
		for (i = 0; i < n; i++) {
			for (j = 1; j < n - i; j++) {
				if (a[j - 1] > a[j]) {
					t = a[j - 1];
					a[j - 1] = a[j];
					a[j] = t;
				}
			}
		}
	}

}
