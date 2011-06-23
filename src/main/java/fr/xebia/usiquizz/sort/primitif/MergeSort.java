package fr.xebia.usiquizz.sort.primitif;

public class MergeSort {

	private static final int SIZE_THRESHOLD = 16;

	public static void sort(int[] a) {
		sort(a, 0, a.length - 1);
	}

	public static void sort(int[] a, int lo, int hi) {
		if (hi - lo < SIZE_THRESHOLD) {
			insertionsort(a, lo, hi);
			return;
		}

		int[] tmp = new int[(hi - lo) / 2 + 1];
		mergeSort(a, tmp, lo, hi);
	}

	private static void mergeSort(int[] a, int[] tmp, int lo, int hi) {
		if (hi - lo < SIZE_THRESHOLD) {
			insertionsort(a, lo, hi);
			return;
		}

		int m = (lo + hi) / 2;
		mergeSort(a, tmp, lo, m);
		mergeSort(a, tmp, m + 1, hi);
		merge(a, tmp, lo, m, hi);
	}

	private static void merge(int[] a, int[] b, int lo, int m, int hi) {
		if (a[m] - a[m + 1] <= 0) {
			return;
		}

		System.arraycopy(a, lo, b, 0, m - lo + 1);

		int i = 0;
		int j = m + 1;
		int k = lo;

		// copy back next-greatest element at each time
		while (k < j && j <= hi) {
			if (b[i] - a[j] <= 0) {
				a[k++] = b[i++];
			} else {
				a[k++] = a[j++];
			}
		}

		// copy back remaining elements of first half (if any)
		System.arraycopy(b, i, a, k, j - k);

	}

	private static void insertionsort(int[] a, int lo, int hi) {
		for (int i = lo + 1; i <= hi; i++) {
			int j = i;
			int t = a[j];
			while (j > lo && t - a[j - 1] < 0) {
				a[j] = a[j - 1];
				--j;
			}
			a[j] = t;
		}
	}
}
