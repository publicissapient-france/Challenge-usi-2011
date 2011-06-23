package fr.xebia.usiquizz.sort.primitif;

/**
 * Selection sort min=O(N2) max=O(N2) avg=O(N2) Perhaps best of O(N2) sorts
 * 
 * @author Erwan ALLIAUME
 */
public class SelectionSort {

	public static void sort(int[] num) {
		int i, j, first, temp;
		for (i = num.length - 1; i > 0; i--) {
			first = 0; // initialize to subscript of first element
			for (j = 1; j <= i; j++) // locate smallest element between
			// positions 1 and i.
			{
				if (num[j] < num[first]) {
					first = j;
				}
			}
			temp = num[first]; // swap smallest found with element in position
			// i.
			num[first] = num[i];
			num[i] = temp;
		}
	}

	// Move every value only once
	public static void sortBetter(int[] x) {
		for (int i = 0; i < x.length - 1; i++) {
			int minIndex = i; // Index of smallest remaining value.
			for (int j = i + 1; j < x.length; j++) {
				if (x[minIndex] > x[j]) {
					minIndex = j; // Remember index of new minimum
				}
			}
			if (minIndex != i) {
				// ... Exchange current element with smallest remaining.
				int temp = x[i];
				x[i] = x[minIndex];
				x[minIndex] = temp;
			}
		}
	}

	public static void sortSemblePasMarcher(int[] array) {
		sort(array, array.length);
	}

	private static void sort(int array[], int n) {
		for (int x = 0; x < n; x++) {
			int index_of_min = x;
			for (int y = x; y < n; y++) {
				if (array[index_of_min] < array[y]) {
					index_of_min = y;
				}
			}
			int temp = array[x];
			array[x] = array[index_of_min];
			array[index_of_min] = temp;
		}
	}

}
