import java.util.Arrays;
import java.util.Random;

import fr.xebia.usiquizz.sort.MergeSort;
import fr.xebia.usiquizz.sort.ParallelMergeSort;

public class SortMain {

	private static int[] numbers;
	private static final int SIZE = 1000000;
	private static final int MAX = SIZE;

	public static void main(String... args) {
		if (args.length != 2) {
			System.err.println("Usage: SortMain [algo] [nbElts]\n * algos: java merge parallelMerge");
			return;
		}
		String algo = args[0];
		int size = Integer.valueOf(args[1]);

		// 

		initArray(size);

		// 

		long startTime = System.currentTimeMillis();

		if ("java".equals(algo)) {
			standardJavaSort();
		} else if ("merge".equals(algo)) {
			MergeSort.sort(numbers);
		} else if ("parallelMerge".equals(algo)) {
			ParallelMergeSort.sort(numbers);
		} else {
			throw new RuntimeException("Algo not found: " + algo);
		}

		System.out.println(algo + "(" + size + ") -> " + (System.currentTimeMillis() - startTime) + " ms");

		//

		checkResult();
	}

	public static void standardJavaSort() {
		Arrays.sort(numbers);
	}

	private static void checkResult() {
		for (int i = 0; i < numbers.length - 1; i++) {
			if (numbers[i] > numbers[i + 1]) {
				throw new RuntimeException();
			}
		}
	}

	public static void initArray(int size) {
		numbers = new int[SIZE];
		Random generator = new Random();
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = generator.nextInt(MAX);
		}
	}

}
