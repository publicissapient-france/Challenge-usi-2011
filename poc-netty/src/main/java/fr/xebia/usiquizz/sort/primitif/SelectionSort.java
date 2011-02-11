package fr.xebia.usiquizz.sort.primitif;

/**
 * Selection sort min=O(N2) max=O(N2) avg=O(N2) Perhaps best of O(N2) sorts
 * 
 * @author Erwan ALLIAUME
 */
public class SelectionSort {

	public static void sort(int[] array) {
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
