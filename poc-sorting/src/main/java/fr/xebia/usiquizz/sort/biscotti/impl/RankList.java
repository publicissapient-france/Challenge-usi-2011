package fr.xebia.usiquizz.sort.biscotti.impl;

/*
 * Copyright (C) 2010 Zhenya Leonov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkState;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Random;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

/**
 * A {@code List} optimized for efficient <a
 * href="http://en.wikipedia.org/wiki/Random_access">random access</a> insertion
 * and removal operations. Implements all optional list operations, and permits
 * all elements, including {@code null}.
 * <p>
 * The iterators obtained from the {@link #iterator()} and
 * {@link #listIterator()} methods are <i>fail-fast</i>. Attempts to modify the
 * elements in this list at any time after an iterator is created, in any way
 * except through the iterator's own remove method, will result in a {@code
 * ConcurrentModificationException}.
 * <p>
 * This list is not <i>thread-safe</i>. If multiple threads modify this list
 * concurrently it must be synchronized externally.
 * <p>
 * This class implements an array-based <a
 * href="http://en.wikipedia.org/wiki/Skip_list">Skip List</a> modified to
 * provide logarithmic running time for linear list operations (e.g. insert an
 * element at the i<i>th</i> index). Linear list operations are sometimes
 * referred to as rank operations.
 * <p>
 * Invented by <a href="http://www.cs.umd.edu/~pugh/">Bill Pugh</a> in 1990, A
 * Skip List is a probabilistic data structure for maintaining items in sorted
 * order. Strictly speaking it is impossible to make any hard guarantees
 * regarding the worst-case performance of this class. Practical performance is
 * <i>expected</i> to be logarithmic with an extremely high degree of
 * probability as the list grows.
 * <p>
 * The following table summarizes the performance of this class compared to an
 * {@link ArrayList} and a {@link LinkedList} (where n is the size of the list
 * and m is the size of the specified collection):
 * <p>
 * <table border cellpadding="3" cellspacing="1">
 * <tr>
 * <th align="center" rowspan="2">Method</th>
 * <th align="center" colspan="3">Running Time</th>
 * </tr>
 * <tr>
 * <td align="center"><b>RankList</b><br>
 * (<i>expected</i>)</td>
 * <td align="center"><b>ArrayList</b><br>
 * (<i>amortized</i>)</td>
 * <td align="center"><b>LinkedList</b><br>
 * (<i>worst-case</i>)</td>
 * </tr>
 * <tr>
 * <td>
 * {@link #addAll(Collection) addAll(Collection)}</td>
 * <td align="center"><i>O(m log n)</i></td>
 * <td align="center" colspan="2"><i>O(m)</i></td>
 * </tr>
 * <tr>
 * <td>
 * {@link #containsAll(Collection) containsAll(Collection)}</td>
 * <td align="center" colspan="3" rowspan="3"><i>O(m * n)</i></td>
 * </tr>
 * <tr>
 * <td>
 * {@link #removeAll(Collection) removeAll(Collection)}</td>
 * </tr>
 * <tr>
 * <td>
 * {@link #retainAll(Collection) retainAll(Collection)}</td>
 * </tr>
 * <tr>
 * <td>
 * {@link #add(Object) add(E)}</td>
 * <td align="center" rowspan="9"><i>O(log n)</i></td>
 * <td align="center" rowspan="3"><i>O(1)</i></td>
 * <td align="center"><i>O(1)</i></td>
 * </tr>
 * <tr>
 * <td>
 * {@link #get(int)}</br>
 * <td align="center" rowspan="5"><i>O(n)</i></td>
 * </td>
 * </tr>
 * <tr>
 * <td>
 * {@link #set(int, Object) set(int, E)}</td>
 * </tr>
 * <tr>
 * <td>
 * {@link #add(int, Object) add(int, E)}</br></td>
 * <td align="center" rowspan="6"><i>O(n)</i></td>
 * </td>
 * </tr>
 * <tr>
 * <td>
 * {@link #remove(int)}</td>
 * </tr>
 * <tr>
 * <td>
 * {@link #listIterator(int)}</td>
 * </tr>
 * <tr>
 * <td>{@link Iterator#remove()}</td>
 * <td align="center" rowspan="3"><i>O(1)</i></td>
 * </tr>
 * <tr>
 * <td>{@link ListIterator#remove()}</td>
 * </tr>
 * <tr>
 * <td>{@link ListIterator#add(Object) ListIterator.add(E)}</td>
 * </tr>
 * <tr>
 * <td>{@link ListIterator#set(Object) ListIterator.set(E)}</td>
 * <td align="center" colspan="3" rowspan="2"><i>O(1)</i></td>
 * </tr>
 * <tr>
 * <td>{@link #clear()}</td>
 * </tr>
 * <tr>
 * <td>
 * {@link #contains(Object) contains(Object)}</br> {@link #indexOf(Object)
 * indexOf(Object)}</br> {@link #lastIndexOf(Object)}</br>
 * {@link #remove(Object) remove(Object)}</td>
 * <td align="center" rowspan="4" colspan="3"><i>O(n)</i></td>
 * </table>
 * <p>
 * The {@code subList} view exhibits identical time complexity, with the
 * exception of the {@code clear()} operation which runs in linear time
 * proportional to the size of the view.
 * 
 * @author Zhenya Leonov
 * @param <E>
 *            the type of elements maintained by this list
 */
public final class RankList<E> extends AbstractList<E> implements List<E>, Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	private static final double P = .5;
	private static final int MAX_LEVEL = 32;
	private transient int size = 0;
	private transient int level = 1;
	private transient Random random = new Random();
	private transient Node<E> head = new Node<E>(null, MAX_LEVEL);

	private RankList() {
		for (int i = 0; i < MAX_LEVEL; i++) {
			head.next[i] = head;
			head.dist[i] = 1;
		}
		head.prev = head;
	}

	/**
	 * Creates a new {@code RankList}.
	 * 
	 * @return a new {@code RankList}
	 */
	public static <E> RankList<E> create() {
		return new RankList<E>();
	}

	/**
	 * Creates a new {@code RankList} containing the elements of the specified
	 * {@code Iterable}.
	 * 
	 * @param elements
	 *            the iterable whose elements are to be placed into the list
	 * @return a new {@code RankList} containing the elements of the specified
	 *         iterable
	 */
	public static <E> RankList<E> create(final Iterable<? extends E> elements) {
		checkNotNull(elements);
		RankList<E> list = new RankList<E>();
		Iterables.addAll(list, elements);
		return list;
	}

	// /**
	// * Creates a {@code RankList} containing the specified initial elements.
	// *
	// * @param elements
	// * the elements to be placed into the list
	// * @return a {@code RankList} containing the specified initial elements
	// */
	// public static <E> RankList<E> create(final E... elements) {
	// checkNotNull(elements);
	// RankList<E> rankList = RankList.create();
	// Collections.addAll(rankList, elements);
	// return rankList;
	// }

	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void add(int index, E element) {
		checkPositionIndex(index, size);
		Node<E> curr = head;
		int pos = 0;
		final int newLevel = randomLevel();
		final Node<E> newNode = new Node<E>(element, newLevel);
		if (newLevel > level) {
			for (int i = level; i < newLevel; i++) {
				head.dist[i] = size + 1;
			}
			level = newLevel;
		}
		for (int i = level - 1; i >= 0; i--) {
			while (pos + curr.dist[i] <= index) {
				pos += curr.dist[i];
				curr = curr.next[i];
			}
			if (i > newLevel - 1) {
				curr.dist[i]++;
			} else {
				newNode.next[i] = curr.next[i];
				curr.next[i] = newNode;
				newNode.dist[i] = pos + curr.dist[i] - index;
				curr.dist[i] = index + 1 - pos;
			}
		}
		newNode.prev = curr;
		newNode.next[0].prev = newNode;
		modCount++;
		size++;
	}

	@Override
	public E get(int index) {
		checkElementIndex(index, size);
		return search(index).element;

	}

	@Override
	public int lastIndexOf(Object o) {
		Node<E> node = head;
		for (int i = size - 1; i >= 0; i--) {
			node = node.prev;
			if (Objects.equal(node.element, o)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public ListIterator<E> listIterator() {
		return new ListItor();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		checkPositionIndex(index, size);
		return new ListItor(index);
	}

	@Override
	public E remove(int index) {
		checkElementIndex(index, size);
		final Node<E>[] update = new Node[level];
		Node<E> node = head;
		int pos = 0;
		for (int i = level - 1; i >= 0; i--) {
			while (pos + node.dist[i] <= index) {
				pos += node.dist[i];
				node = node.next[i];
			}
			update[i] = node;
		}
		node = node.next[0];
		delete(node, update);
		return node.element;
	}

	@Override
	public void clear() {
		for (int i = 0; i < MAX_LEVEL; i++) {
			head.next[i] = head;
			head.dist[i] = 1;
		}
		head.prev = head;
		modCount++;
		size = 0;
	}

	@Override
	public E set(int index, E element) {
		checkElementIndex(index, size);
		Node<E> node = search(index);
		E e = node.element;
		node.element = element;
		return e;
	}

	/**
	 * Returns a shallow copy of this {@code RankList}. The elements themselves
	 * are not cloned.
	 * 
	 * @return a shallow copy of this list
	 */
	@Override
	public RankList<E> clone() {
		RankList<E> clone;
		try {
			clone = (RankList<E>) super.clone();
		} catch (java.lang.CloneNotSupportedException e) {
			throw new InternalError();
		}
		for (int i = 0; i < MAX_LEVEL; i++) {
			clone.head.next[i] = clone.head;
			clone.head.dist[i] = 1;
		}
		clone.head.prev = clone.head;
		clone.random = new Random();
		clone.level = 1;
		clone.modCount = 0;
		clone.size = 0;
		clone.addAll(this);
		return clone;
	}

	private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {
		oos.defaultWriteObject();
		oos.writeInt(size);
		for (E e : this) {
			oos.writeObject(e);
		}
	}

	private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {
		ois.defaultReadObject();
		head = new Node<E>(null, MAX_LEVEL);
		for (int i = 0; i < MAX_LEVEL; i++) {
			head.next[i] = head;
			head.dist[i] = 1;
		}
		head.prev = head;
		random = new Random();
		level = 1;
		int size = ois.readInt();
		for (int i = 0; i < size; i++) {
			add((E) ois.readObject());
		}
	}

	private class ListItor implements ListIterator<E> {
		private Node<E> node;
		private Node<E> last = null;
		private final int offset;
		private int index = 0;
		private int expectedModCount = modCount;

		private ListItor() {
			node = head.next[0];
			offset = 0;
		}

		private ListItor(final int index) {
			node = search(index);
			offset = index;
		}

		@Override
		public void add(E element) {
			checkForConcurrentModification();
			RankList.this.add(index++ + offset, element);
			expectedModCount = modCount;
			last = null;
			node = node.next[0];
		}

		@Override
		public boolean hasNext() {
			return index + offset < size();
		}

		@Override
		public boolean hasPrevious() {
			return index + offset > 0;
		}

		@Override
		public E next() {
			checkForConcurrentModification();
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			index++;
			last = node;
			node = node.next[0];
			return last.element;
		}

		@Override
		public int nextIndex() {
			return index + offset;
		}

		@Override
		public E previous() {
			checkForConcurrentModification();
			if (index == 0) {
				throw new NoSuchElementException();
			}
			index--;
			last = node = node.prev;
			return node.element;
		}

		@Override
		public int previousIndex() {
			return index + offset - 1;
		}

		@Override
		public void remove() {
			checkForConcurrentModification();
			checkState(last != null);
			RankList.this.remove(--index + offset);
			expectedModCount = modCount;
			last = null;
		}

		@Override
		public void set(E element) {
			checkForConcurrentModification();
			checkState(last != null);
			last.element = element;
		}

		private void checkForConcurrentModification() {
			if (expectedModCount != modCount) {
				throw new ConcurrentModificationException();
			}
		}
	}

	// Skip List

	private static class Node<E> {
		private E element;
		private Node<E> prev;
		private final Node<E>[] next;
		private final int[] dist;

		private Node(final E element, final int size) {
			this.element = element;
			next = new Node[size];
			dist = new int[size];
		}
	}

	private Node<E> search(final int index) {
		Node<E> curr = head;
		int pos = -1;
		for (int i = level - 1; i >= 0; i--) {
			while (pos + curr.dist[i] <= index) {
				pos = pos + curr.dist[i];
				curr = curr.next[i];
			}
		}
		return curr;
	}

	private void delete(final Node<E> node, final Node<E>[] update) {
		node.next[0].prev = node.prev;
		for (int i = 0; i < level; i++) {
			if (update[i].next[i] == node) {
				update[i].next[i] = node.next[i];
				update[i].dist[i] += node.dist[i] - 1;
			} else {
				update[i].dist[i]--;
			}
		}
		while (head.next[level - 1] == head && level > 1) {
			level--;
		}
		modCount++;
		size--;
	}

	private int randomLevel() {
		int randomLevel = 1;
		while (randomLevel < MAX_LEVEL - 1 && random.nextDouble() < P) {
			randomLevel++;
		}
		return randomLevel;
	}

}