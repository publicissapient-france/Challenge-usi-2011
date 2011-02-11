import java.util.Arrays;
import java.util.Random;

import fr.xebia.usiquizz.sort.primitif.BubbleSort;
import fr.xebia.usiquizz.sort.primitif.HeapSort;
import fr.xebia.usiquizz.sort.primitif.MergeSort;
import fr.xebia.usiquizz.sort.primitif.ParallelMergeSort;
import fr.xebia.usiquizz.sort.primitif.QuickSort;
import fr.xebia.usiquizz.sort.primitif.SelectionSort;
import fr.xebia.usiquizz.sort.wrapper.MergeCompSort;
import fr.xebia.usiquizz.sort.wrapper.ParallelMergeCompSort;

@SuppressWarnings("unchecked")
public class SortMain {

	private static int[] numbers;
	private static Comparable[] comparables;

	public static void main(String... args) {
		if (args.length != 2) {
			System.err.println("Usage: SortMain [algo] [nbElts]\n * algos: java merge parallelMerge");
			return;
		}
		String algo = args[0];
		int size = Integer.valueOf(args[1]);
		boolean isPrimitif = !algo.endsWith("Comp");

		// 

		initArray(size, isPrimitif);

		// 

		long startTime = System.currentTimeMillis();

		if ("java".equals(algo)) {
			standardJavaSort(numbers);
		}
		if ("merge".equals(algo)) {
			MergeSort.sort(numbers);
		}
		if ("parallelMerge".equals(algo)) {
			ParallelMergeSort.sort(numbers);
		}
		if ("heapSort".equals(algo)) {
			HeapSort.sort(numbers);
		}
		if ("quickSort".equals(algo)) {
			QuickSort.sort(numbers);
		}
		if ("selectionSort".equals(algo)) {
			SelectionSort.sort(numbers);
		}
		if ("bubbleSort".equals(algo)) {
			BubbleSort.sort(numbers);
		}

		if ("javaComp".equals(algo)) {
			standardJavaCompSortComp(comparables);
		}
		if ("mergeComp".equals(algo)) {
			MergeCompSort.sort(comparables);
		}
		if ("parallelMergeComp".equals(algo)) {
			ParallelMergeCompSort.sort(comparables);
		}

		System.out.println(algo + "(" + size + ") -> " + (System.currentTimeMillis() - startTime) + " ms");

		//

		checkResult(isPrimitif);
	}

	public static void standardJavaSort(int[] numbers) {
		Arrays.sort(numbers);
	}

	public static void standardJavaCompSortComp(Comparable[] comparables) {
		Arrays.sort(comparables);
	}

	private static void checkResult(boolean isPrimitif) {
		if (isPrimitif) {
			for (int i = 0; i < numbers.length - 1; i++) {
				if (numbers[i] > numbers[i + 1]) {
					throw new RuntimeException();
				}
			}
		} else {
			for (int i = 0; i < comparables.length - 1; i++) {
				if (comparables[i].compareTo(comparables[i + 1]) > 0) {
					throw new RuntimeException();
				}
			}
		}
	}

	public static void initArray(int size, boolean isPrimitif) {
		Random generator = new Random();
		if (isPrimitif) {
			numbers = new int[size];
			for (int i = 0; i < numbers.length; i++) {
				numbers[i] = generator.nextInt(size * 2);
			}
		} else {
			comparables = new Comparable[size];
			for (int i = 0; i < comparables.length; i++) {
				comparables[i] = generator.nextInt(size * 2);
			}
		}
	}

}
