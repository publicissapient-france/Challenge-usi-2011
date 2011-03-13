package fr.xebia.usiquizz.sort.biscotti.bench;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Iterators;

import fr.xebia.usiquizz.sort.biscotti.impl.SkipList;
import fr.xebia.usiquizz.sort.biscotti.impl.TreeList;

public class SkipList_vs_TreeList {

	private static final long seed = System.currentTimeMillis();

	private static Random random;
	private static int n = 3000000;
	private static int o = 20000;

	public static void main(String[] args) {

		SkipList<Integer> sl = SkipList.create();
		TreeList<Integer> tl = TreeList.create();

		addBenchmark(tl, "tl.add - " + n + " integers", n);
		// tl = null;
		System.gc();

		getBenchmark(tl, "tl.get - " + o + " integers", o);
		tl = null;
		System.gc();

		addBenchmark(sl, "sl.add - " + n + " integers", n);
		// sl = null;
		System.gc();

		getBenchmark(sl, "sl.get - " + o + " integers", o);
		sl = null;
		System.gc();
	}

	public static void addBenchmark(Collection<Integer> c, String s, int n) {
		random = new Random(seed);
		System.out.println(s);
		long start = System.nanoTime();
		for (int i = 0; i < n; i++) {
			c.add(random.nextInt());
		}
		System.out.println((System.nanoTime() - start) / 1000000000F);
	}

	public static void getBenchmark(List<?> c, String s, int n) {
		random = new Random(seed);
		System.out.println(s);
		long start = System.nanoTime();
		for (int i = 0; i < n; i++) {
			c.get(random.nextInt(n));
		}
		System.out.println((System.nanoTime() - start) / 1000000000F);
	}

	public static void println(Iterator<?>... i) {
		for (Iterator<?> itor : i) {
			System.out.println(Iterators.toString(itor));
		}
	}

	public static void println(Object... objs) {
		for (Object o : objs) {
			System.out.println(o);
		}
	}
}
