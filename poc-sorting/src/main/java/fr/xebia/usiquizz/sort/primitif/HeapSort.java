package fr.xebia.usiquizz.sort.primitif;

/**
 * HeapSort min=O(N log N) max=O(N log N) avg=O(N log N) Typically slower than
 * QuickSort, but worst case is much better.
 * 
 * @author Erwan ALLIAUME
 */
public class HeapSort {

	public static void sort(int[] arr) {
		for (int i = arr.length; i > 1; i--) {
			fnSortHeap(arr, i - 1);
		}
	}

	private static void fnSortHeap(int array[], int arr_ubound) {
		int i, o;
		int lChild, rChild, mChild, root, temp;
		root = (arr_ubound - 1) / 2;

		for (o = root; o >= 0; o--) {
			for (i = root; i >= 0; i--) {
				lChild = 2 * i + 1;
				rChild = 2 * i + 2;
				if (lChild <= arr_ubound && rChild <= arr_ubound) {
					if (array[rChild] >= array[lChild]) {
						mChild = rChild;
					} else {
						mChild = lChild;
					}
				} else {
					if (rChild > arr_ubound) {
						mChild = lChild;
					} else {
						mChild = rChild;
					}
				}

				if (array[i] < array[mChild]) {
					temp = array[i];
					array[i] = array[mChild];
					array[mChild] = temp;
				}
			}
		}
		temp = array[0];
		array[0] = array[arr_ubound];
		array[arr_ubound] = temp;
		return;
	}

}
