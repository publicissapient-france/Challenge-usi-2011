package fr.xebia.usiquizz.sort.primitif;

/**
 * BubbleSort min=O(N) max=O(N2) avg=O(N2) Not a good sort, except with ideal
 * data.
 * 
 * http://leepoint.net/notes-java/data/arrays/32arraybubblesort.html
 * 
 * @author Erwan ALLIAUME
 */
public class BubbleSort {

	public static void sortFixedNumberOfPass(int[] x) {
		int n = x.length;
		for (int pass = 1; pass < n; pass++) { // count how many times
			// This next loop becomes shorter and shorter
			for (int i = 0; i < n - pass; i++) {
				if (x[i] > x[i + 1]) {
					// exchange elements
					int temp = x[i];
					x[i] = x[i + 1];
					x[i + 1] = temp;
				}
			}
		}
	}

	// stop when no exchanges
	public static void sortStopFast(int[] x) {
		boolean doMore = true;
		while (doMore) {
			doMore = false; // assume this is last pass over array
			for (int i = 0; i < x.length - 1; i++) {
				if (x[i] > x[i + 1]) {
					// exchange elements
					int temp = x[i];
					x[i] = x[i + 1];
					x[i + 1] = temp;
					doMore = true; // after an exchange, must look again
				}
			}
		}
	}

	public static void sortStopFastAndShorterRange(int[] x) {
		int n = x.length;
		boolean doMore = true;
		while (doMore) {
			n--;
			doMore = false; // assume this is our last pass over the array
			for (int i = 0; i < n; i++) {
				if (x[i] > x[i + 1]) {
					// exchange elements
					int temp = x[i];
					x[i] = x[i + 1];
					x[i + 1] = temp;
					doMore = true; // after an exchange, must look again
				}
			}
		}
	}

	// Sort only necessary range
	public static void sortBestSolution(int[] x) {
		int newLowest = 0; // index of first comparison
		int newHighest = x.length - 1; // index of last comparison

		while (newLowest < newHighest) {
			int highest = newHighest;
			int lowest = newLowest;
			newLowest = x.length; // start higher than any legal index
			for (int i = lowest; i < highest; i++) {
				if (x[i] > x[i + 1]) {
					// exchange elements
					int temp = x[i];
					x[i] = x[i + 1];
					x[i + 1] = temp;
					if (i < newLowest) {
						newLowest = i - 1;
						if (newLowest < 0) {
							newLowest = 0;
						}
					} else if (i > newHighest) {
						newHighest = i + 1;
					}
				}
			}
		}
	}

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
