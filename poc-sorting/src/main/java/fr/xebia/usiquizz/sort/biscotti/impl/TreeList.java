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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkPositionIndexes;
import static com.google.common.base.Preconditions.checkState;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.SortedSet;

import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import fr.xebia.usiquizz.sort.biscotti.SortedCollection;
import fr.xebia.usiquizz.sort.biscotti.SortedList;

/**
 * A {@link SortedList} implementation, based on a modified <a
 * href="http://en.wikipedia.org/wiki/Red-black_tree">Red-Black Tree</a>.
 * Elements are sorted from <i>least</i> to <i>greatest</i> according to their
 * <i>natural ordering</i>, or by an explicit {@link Comparator} provided at
 * creation. Attempting to remove or insert {@code null} elements is prohibited.
 * Querying for {@code null} elements is allowed. Inserting non-comparable
 * elements will result in a {@code ClassCastException}. The {@code add(int, E)}
 * , {@code addAll(int, Collection)}, and {@code set(int, E)} operations are not
 * supported.
 * <p>
 * The iterators obtained from the {@link #iterator()} and
 * {@link #listIterator()} methods are <i>fail-fast</i>. Attempts to modify the
 * elements in this list at any time after an iterator is created, in any way
 * except through the iterator's own remove method, will result in a {@code
 * ConcurrentModificationException}. Further, the list iterator does not support
 * the {@code add(E)} and {@code set(E)} operations.
 * <p>
 * This list is not <i>thread-safe</i>. If multiple threads modify this list
 * concurrently it must be synchronized externally.
 * <p>
 * <b>Implementation Note:</b> This implementation uses a comparator (whether or
 * not one is explicitly provided) to perform all element comparisons. Two
 * elements which are deemed equal by the comparator's {@code compare(E, E)}
 * method are, from the standpoint of this list, equal. Further, no guarantee is
 * made as to the final order of elements with equal priority. Ties may be
 * broken arbitrarily.
 * <p>
 * The underlying Red-Black Tree provides the following worst case running time
 * (where <i>n</i> is the size of this list and <i>m</i> is the size of the
 * specified collection):
 * <p>
 * <table border cellpadding="3" cellspacing="1">
 * <tr>
 * <th align="center">Method</th>
 * <th align="center">Running Time</th>
 * </tr>
 * <tr>
 * <td>
 * {@link #addAll(Collection) addAll(Collection)}</br>
 * {@link #containsAll(Collection) containsAll(Collection)}</br>
 * {@link #retainAll(Collection) retainAll(Collection)}</br>
 * {@link #removeAll(Collection) removeAll(Collection)}</td>
 * <td align="center"><i>O(m log n)</i></td>
 * </tr>
 * <tr>
 * <td>
 * {@link #indexOf(Object)}</br> {@link #lastIndexOf(Object)}</br>
 * {@link #get(int)}</br> {@link #remove(int)}</br> {@link #listIterator(int)}</td>
 * <td align="center"><i>O(n)</i></td>
 * </tr>
 * <tr>
 * <td>
 * {@link #add(Object) add(E)}</br> {@link #contains(Object)}</br>
 * {@link #remove(Object)}</td>
 * <td align="center"><i>O(log n)</i></td>
 * </tr>
 * <tr>
 * <td>
 * {@link #clear() clear()}</br> {@link #isEmpty() isEmpty()}</br>
 * {@link #size()}</br> {@link Iterator#remove()}</br>
 * {@link ListIterator#remove()}</td>
 * <td align="center"><i>O(1)</i></td>
 * </tr>
 * </table>
 * <p>
 * The {@code headList}, {@code subList}, and {@code tailList} views exhibit
 * identical time complexity, with the exception of the {@code clear()}
 * operation which runs in linear time proportional to the size of the views.
 * 
 * @author Zhenya Leonov
 * @param <E>
 *            the type of elements maintained by this list
 * @see SkipList
 */
public class TreeList<E> extends AbstractList<E> implements SortedList<E>, Cloneable, Serializable {

	private static final long serialVersionUID = 1L;
	private transient int size = 0;
	private transient Node nil = new Node();
	private transient Node min = nil;
	private transient Node max = nil;
	private transient Node root = nil;
	private transient int modCount = 0;
	private final Comparator<? super E> comparator;

	private TreeList(final Comparator<? super E> comparator) {
		this.comparator = comparator;
	}

	private TreeList(final Comparator<? super E> comparator, final Iterable<? extends E> elements) {
		this(comparator);
		Iterables.addAll(this, elements);
	}

	/**
	 * Creates a new {@code TreeList} that orders its elements according to
	 * their natural ordering.
	 * 
	 * @return a new {@code TreeList} that orders its elements according to
	 *         their natural ordering
	 */
	public static <E extends Comparable<? super E>> TreeList<E> create() {
		return new TreeList<E>(Ordering.natural());
	}

	/**
	 * Creates a new {@code TreeList} that orders its elements according to the
	 * specified comparator.
	 * 
	 * @param comparator
	 *            the comparator that will be used to order this list
	 * @return a new {@code TreeList} that orders its elements according to
	 *         {@code comparator}
	 */
	public static <E> TreeList<E> create(final Comparator<? super E> comparator) {
		checkNotNull(comparator);
		return new TreeList<E>(comparator);
	}

	/**
	 * Creates a new {@code TreeList} containing the elements of the specified
	 * {@code Iterable}. If the specified iterable is an instance of
	 * {@link SortedSet}, {@link PriorityQueue}, or {@code SortedCollection},
	 * this list will be ordered according to the same ordering. Otherwise, this
	 * list will be ordered according to the <i>natural ordering</i> of its
	 * elements.
	 * 
	 * @param elements
	 *            the iterable whose elements are to be placed into the list
	 * @return a new {@code TreeList} containing the elements of the specified
	 *         iterable
	 * @throws ClassCastException
	 *             if elements of the specified iterable cannot be compared to
	 *             one another according to this list's ordering
	 * @throws NullPointerException
	 *             if any of the elements of the specified iterable or the
	 *             iterable itself is {@code null}
	 */
	public static <E> TreeList<E> create(final Iterable<? extends E> elements) {
		checkNotNull(elements);
		final Comparator<? super E> comparator;
		if (elements instanceof SortedSet<?>) {
			comparator = ((SortedSet) elements).comparator();
		} else if (elements instanceof PriorityQueue<?>) {
			comparator = ((PriorityQueue) elements).comparator();
		} else if (elements instanceof SortedCollection<?>) {
			comparator = ((SortedCollection) elements).comparator();
		} else {
			comparator = (Comparator<? super E>) Ordering.natural();
		}
		return new TreeList<E>(comparator, elements);
	}

	// /**
	// * Creates a {@code TreeList} containing the specified initial elements
	// * sorted according to their <i>natural ordering</i>.
	// *
	// * @param elements
	// * the initial elements to be placed in this queue
	// * @return a {@code TreeList} containing the specified initial elements
	// * sorted according to their <i>natural ordering</i>
	// */
	// public static <E extends Comparable<? super E>> TreeQueue<E> create(
	// final E... elements) {
	// checkNotNull(elements);
	// TreeQueue<E> q = TreeQueue.create();
	// Collections.addAll(q, elements);
	// return q;
	// }

	/**
	 * Returns the comparator used to order the elements in this list. If one
	 * was not explicitly provided a <i>natural order</i> comparator is
	 * returned.
	 * 
	 * @return the comparator used to order this list
	 */
	@Override
	public Comparator<? super E> comparator() {
		return comparator;
	}

	/**
	 * Inserts the specified element into this list in sorted order.
	 */
	@Override
	public boolean add(E e) {
		checkNotNull(e);
		Node newNode = new Node(e);
		insert(newNode);
		return true;
	}

	/**
	 * Guaranteed to throw an {@code UnsupportedOperationException} exception
	 * and leave the underlying data unmodified.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Guaranteed to throw an {@code UnsupportedOperationException} exception
	 * and leave the underlying data unmodified.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(Object o) {
		return o != null && search((E) o) != null;
	}

	@Override
	public E get(int index) {
		checkElementIndex(index, size);
		Iterator<E> itor = iterator();
		for (int i = 0; i < index; i++) {
			itor.next();
		}
		return itor.next();
	}

	@Override
	public int indexOf(Object o) {
		if (o != null) {
			E e = (E) o;
			ListIterator<E> itor = listIterator();
			while (itor.hasNext()) {
				if (comparator.compare(itor.next(), e) == 0) {
					return itor.previousIndex();
				}
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		if (o != null) {
			E e = (E) o;
			ListIterator<E> itor = listIterator();
			while (itor.hasNext()) {
				if (comparator.compare(itor.next(), e) == 0) {
					while (itor.hasNext() && comparator.compare(itor.next(), e) == 0) {
						;
					}
					return itor.previousIndex();
				}
			}
		}
		return -1;
	}

	@Override
	public Iterator<E> iterator() {
		return listIterator();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The returned iterator does not support the {@code add(E)} and {@code
	 * set(E)} operations.
	 */
	@Override
	public ListIterator<E> listIterator() {
		return new ListIterator<E>() {
			private int index = 0;
			private Node next = min;
			private Node prev = nil;
			private Node last = nil;
			private int expectedModCount = modCount;

			@Override
			public void add(E e) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean hasNext() {
				return index < size();
			}

			@Override
			public boolean hasPrevious() {
				return index > 0;
			}

			@Override
			public E next() {
				checkForConcurrentModification();
				if (index == size()) {
					throw new NoSuchElementException();
				}
				Node node = prev = next;
				index++;
				next = successor(node);
				last = node;
				return node.element;
			}

			@Override
			public int nextIndex() {
				return index;
			}

			@Override
			public E previous() {
				checkForConcurrentModification();
				if (index == 0) {
					throw new NoSuchElementException();
				}
				Node node = next = prev;
				index--;
				prev = predecessor(node);
				last = node;
				return node.element;
			}

			@Override
			public int previousIndex() {
				return index - 1;
			}

			@Override
			public void remove() {
				checkForConcurrentModification();
				checkState(last != nil);
				if (last.left != nil && last.right != nil) {
					next = last;
				}
				delete(last);
				index--;
				expectedModCount = modCount;
				last = nil;
			}

			@Override
			public void set(E e) {
				throw new UnsupportedOperationException();
			}

			private void checkForConcurrentModification() {
				if (expectedModCount != modCount) {
					throw new ConcurrentModificationException();
				}
			}
		};
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The returned iterator does not support the {@code add(E)} and {@code
	 * set(E)} operations.
	 */
	@Override
	public ListIterator<E> listIterator(int index) {
		checkPositionIndex(index, size);
		ListIterator<E> listIterator = listIterator();
		for (int i = 0; i < index; i++) {
			listIterator.next();
		}
		return listIterator;
	}

	@Override
	public boolean remove(Object o) {
		checkNotNull(o);
		Node node = search((E) o);
		if (node == null) {
			return false;
		}
		delete(node);
		return true;
	}

	@Override
	public E remove(int index) {
		checkElementIndex(index, size);
		ListIterator<E> li = listIterator(index);
		E e = li.next();
		li.remove();
		return e;
	}

	/**
	 * Guaranteed to throw an {@code UnsupportedOperationException} exception
	 * and leave the underlying data unmodified.
	 * 
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() {
		modCount++;
		root = nil;
		min = nil;
		max = nil;
		size = 0;
	}

	@Override
	public TreeList<E> headList(E toElement) {
		checkNotNull(toElement);
		Iterator<E> itor = iterator();
		int toIndex = 0;
		while (itor.hasNext() && comparator.compare(itor.next(), toElement) < 0) {
			toIndex++;
		}
		return new SubList(this, 0, toIndex, null, toElement);
	}

	@Override
	public TreeList<E> subList(int fromIndex, int toIndex) {
		checkPositionIndexes(fromIndex, toIndex, size());
		return new SubList(this, fromIndex, toIndex, null, null);
	}

	@Override
	public TreeList<E> subList(E fromElement, E toElement) {
		checkNotNull(fromElement);
		checkNotNull(toElement);
		checkArgument(comparator.compare(fromElement, toElement) <= 0);
		Iterator<E> itor = iterator();
		int fromIndex = 0;
		while (itor.hasNext() && comparator.compare(itor.next(), fromElement) < 0) {
			fromIndex++;
		}
		int toIndex = fromIndex + 1;
		while (itor.hasNext() && comparator.compare(itor.next(), toElement) < 0) {
			toIndex++;
		}
		return new SubList(this, fromIndex, toIndex, fromElement, toElement);
	}

	@Override
	public TreeList<E> tailList(E fromElement) {
		checkNotNull(fromElement);
		Iterator<E> itor = iterator();
		int fromIndex = 0;
		while (itor.hasNext() && comparator.compare(itor.next(), fromElement) < 0) {
			fromIndex++;
		}
		return new SubList(this, fromIndex, size, fromElement, null);
	}

	/**
	 * Returns a shallow copy of this {@code TreeList}. The elements themselves
	 * are not cloned.
	 * 
	 * @return a shallow copy of this list
	 * @throws CloneNotSupportedException
	 *             if an attempt is made to clone is a {@code subList}, {@code
	 *             headList}, or {@code tailList} view of the parent list
	 */
	@Override
	public TreeList<E> clone() {
		TreeList<E> clone;
		try {
			clone = (TreeList<E>) super.clone();
		} catch (java.lang.CloneNotSupportedException e) {
			throw new InternalError();
		}
		clone.nil = new Node();
		clone.min = clone.nil;
		clone.max = clone.nil;
		clone.root = clone.nil;
		clone.size = 0;
		clone.modCount = 0;
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
		nil = new Node();
		root = nil;
		min = nil;
		max = nil;
		int size = ois.readInt();
		for (int i = 0; i < size; i++) {
			add((E) ois.readObject());
		}
	}

	private class SubList extends TreeList<E> {
		private final TreeList<E> parent;
		private final int offset;
		private final E fromElement;
		private final E toElement;
		private int size;
		private Node min;
		private Node max;
		private int modCount;

		private void checkForConcurrentModification() {
			if (modCount != parent.modCount) {
				throw new ConcurrentModificationException();
			}
		}

		public SubList(TreeList<E> parent, int fromIndex, int toIndex, E fromElement, E toElement) {
			super(parent.comparator);
			this.parent = parent;
			min = parent.min;
			offset = fromIndex;
			modCount = parent.modCount;
			size = toIndex - fromIndex;
			int i = 0;
			for (; i < fromIndex; i++) {
				min = successor(min);
			}
			max = min;
			for (; i < toIndex - 1; i++) {
				max = successor(max);
			}
			if (fromElement != null) {
				this.fromElement = fromElement;
			} else {
				this.fromElement = min.element;
			}
			if (toElement != null) {
				this.toElement = toElement;
			} else {
				this.toElement = max.element;
			}
		}

		@Override
		public boolean add(E e) {
			checkForConcurrentModification();
			if (comparator.compare(e, fromElement) < 0 || comparator.compare(e, toElement) >= 0) {
				throw new IllegalArgumentException("element out of range");
			}
			parent.add(e);
			modCount = parent.modCount;
			size++;
			if (comparator.compare(max.element, e) <= 0) {
				max = successor(max);
			}
			return true;
		}

		@Override
		public void add(int index, E element) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll(int index, Collection<? extends E> c) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean contains(Object o) {
			checkForConcurrentModification();
			return o != null && search((E) o) != null;
		}

		@Override
		public E get(int index) {
			checkForConcurrentModification();
			checkElementIndex(index, size);
			return parent.get(index + offset);
		}

		@Override
		public ListIterator<E> listIterator() {
			return listIterator(0);
		}

		@Override
		public ListIterator<E> listIterator(final int index) {
			checkForConcurrentModification();
			checkPositionIndex(index, size);
			return new ListIterator<E>() {
				private final ListIterator<E> i = parent.listIterator(index + offset);

				@Override
				public boolean hasNext() {
					return nextIndex() < size;
				}

				@Override
				public E next() {
					if (hasNext()) {
						return i.next();
					} else {
						throw new NoSuchElementException();
					}
				}

				@Override
				public boolean hasPrevious() {
					return previousIndex() >= 0;
				}

				@Override
				public E previous() {
					if (hasPrevious()) {
						return i.previous();
					} else {
						throw new NoSuchElementException();
					}
				}

				@Override
				public int nextIndex() {
					return i.nextIndex() - offset;
				}

				@Override
				public int previousIndex() {
					return i.previousIndex() - offset;
				}

				@Override
				public void remove() {
					i.remove();
					modCount = parent.modCount;
					size--;
				}

				@Override
				public void set(E e) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void add(E e) {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public boolean remove(Object o) {
			checkForConcurrentModification();
			checkNotNull(o);
			Node node = search((E) o);
			if (node == null) {
				return false;
			}
			if (node == max) {
				max = predecessor(max);
			}
			if (node == min) {
				min = successor(min);
			}
			parent.delete(node);
			modCount = parent.modCount;
			size--;
			return true;
		}

		@Override
		public E remove(int index) {
			checkForConcurrentModification();
			checkElementIndex(index, size);
			if (index == 0) {
				min = successor(min);
			}
			if (index == size - 1) {
				max = predecessor(max);
			}
			E e = parent.remove(index + offset);
			modCount = parent.modCount;
			size--;
			return e;
		}

		@Override
		public E set(int index, E element) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int size() {
			checkForConcurrentModification();
			return size;
		}

		@Override
		public void clear() {
			checkForConcurrentModification();
			removeRange(0, size());
		}

		@Override
		public TreeList<E> clone() {
			throw new RuntimeException();
		}

		private void writeObject(java.io.ObjectOutputStream oos) throws NotSerializableException {
			throw new NotSerializableException();
		}

		private void readObject(java.io.ObjectInputStream ois) throws NotSerializableException {
			throw new NotSerializableException();
		}

		// Red-Black-Tree

		@Override
		Node search(final E e) {
			int i = comparator.compare(e, min.element);
			int j = comparator.compare(e, max.element);
			if (i < 0 || j > 0) {
				return null;
			}
			if (i == 0) {
				return min;
			} else if (j == 0) {
				return max;
			} else {
				return parent.search(e);
			}
		}
	}

	// Red-Black-Tree

	static enum Color {
		BLACK, RED;
	}

	private class Node {
		private E element = null;
		private Node parent, left, right;
		private Color color = Color.BLACK;

		private Node() {
			parent = this;
			right = this;
			left = this;
		}

		private Node(final E element) {
			this.element = element;
			parent = nil;
			right = nil;
			left = nil;
		}
	}

	Node search(final E e) {
		Node n = root;
		while (n != nil) {
			int cmp = comparator.compare(e, n.element);
			if (cmp == 0) {
				return n;
			}
			if (cmp < 0) {
				n = n.left;
			} else {
				n = n.right;
			}
		}
		return null;
	}

	/**
	 * Introduction to Algorithms (CLR) Second Edition
	 * 
	 * <pre>
	 * RB-INSERT(T, z)
	 * y = nil[T]
	 * x = root[T]
	 * while x != nil[T]
	 *    do y = x
	 *       if key[z] < key[x]
	 *          then x = left[x]
	 *          else x = right[x]
	 * p[z] = y
	 * if y = nil[T]
	 *    then root[T] = z
	 *    else if key[z] < key[y]
	 *            then left[y] = z
	 *            else right[y] = z
	 * left[z] = nil[T]
	 * right[z] = nil[T]
	 * color[z] = RED
	 * RB-INSERT-FIXUP(T, z)
	 */
	private void insert(Node z) {
		size++;
		modCount++;
		Node x = root;
		Node y = nil;
		while (x != nil) {
			y = x;
			if (comparator.compare(z.element, x.element) < 0) {
				x = x.left;
			} else {
				x = x.right;
			}
		}
		z.parent = y;
		if (y == nil) {
			root = z;
		} else if (comparator.compare(z.element, y.element) < 0) {
			y.left = z;
		} else {
			y.right = z;
		}
		fixAfterInsertion(z);
		if (max == nil || comparator.compare(z.element, max.element) >= 0) {
			max = z;
		}
		if (min == nil || comparator.compare(z.element, min.element) < 0) {
			min = z;
		}
	}

	/**
	 * Introduction to Algorithms (CLR) Second Edition
	 * 
	 * <pre>
	 * RB-DELETE-FIXUP(T, z)
	 * if left[z] = nil[T] or right[z] = nil[T]
	 *    then y = z
	 *    else y = TREE-SUCCESSOR(z)
	 * if left[y] != nil[T]
	 *    then x = left[y]
	 *    else x = right[y]
	 * p[x] = p[y]
	 * if p[y] = nil[T]
	 *    then root[T] = x
	 *    else if y = left[p[y]]
	 *            then left[p[y]] = x
	 *            else right[p[y]] = x
	 * if y != z
	 *    then key[z] = key[y]
	 *         copy y's satellite data into z
	 * if color[y] = BLACK
	 *    then RB-DELETE-FIXUP(T, x)
	 * return y
	 */
	void delete(Node z) {
		size--;
		modCount++;
		Node x, y;
		if (min == z) {
			min = successor(z);
		}
		if (max == z) {
			max = predecessor(z);
		}
		if (z.left == nil || z.right == nil) {
			y = z;
		} else {
			y = successor(z);
		}
		if (y.left != nil) {
			x = y.left;
		} else {
			x = y.right;
		}
		x.parent = y.parent;
		if (y.parent == nil) {
			root = x;
		} else if (y == y.parent.left) {
			y.parent.left = x;
		} else {
			y.parent.right = x;
		}
		if (y != z) {
			z.element = y.element;
		}
		if (y.color == Color.BLACK) {
			fixAfterDeletion(x);
		}
	}

	/**
	 * Introduction to Algorithms (CLR) Second Edition
	 * 
	 * <pre>
	 * TREE-SUCCESSOR(x)
	 * if right[x] != NIL
	 *    then return TREE-MINIMUM(right[x])
	 * y = p[x]
	 * while y != NIL and x = right[y]
	 *    do x = y
	 *       y = p[y]
	 * return y
	 */
	private Node successor(Node x) {
		if (x == nil) {
			return nil;
		}
		if (x.right != nil) {
			Node y = x.right;
			while (y.left != nil) {
				y = y.left;
			}
			return y;
		}
		Node y = x.parent;
		while (y != nil && x == y.right) {
			x = y;
			y = y.parent;
		}
		return y;
	}

	private Node predecessor(Node x) {
		if (x == nil) {
			return nil;
		}
		if (x.left != nil) {
			Node y = x.left;
			while (y.right != nil) {
				y = y.right;
			}
			return y;
		}
		Node y = x.parent;
		while (y != nil && x == y.left) {
			x = y;
			y = y.left;
		}
		return y;
	}

	/**
	 * Introduction to Algorithms (CLR) Second Edition
	 * 
	 * <pre>
	 * LEFT-ROTATE(T, x)
	 * y = right[x]                                                 Set y.
	 * right[x] = left[y]                                   Turn y's left subtree into x's right subtree.
	 * if left[y] != nil[T]
	 *    then p[left[y]] = x
	 * p[y] = p[x]                                                  Link x's parent to y.
	 * if p[x] = nil[T]
	 *    then root[T] = y
	 *    else if x = left[p[x]]
	 *            then left[p[x]] = y
	 *            else right[p[x]] = y
	 * left[y] = x                                                  Put x on y's left.
	 * p[x] = y
	 */
	private void leftRotate(final Node x) {
		if (x != nil) {
			Node n = x.right;
			x.right = n.left;
			if (n.left != nil) {
				n.left.parent = x;
			}
			n.parent = x.parent;
			if (x.parent == nil) {
				root = n;
			} else if (x.parent.left == x) {
				x.parent.left = n;
			} else {
				x.parent.right = n;
			}
			n.left = x;
			x.parent = n;
		}
	}

	private void rightRotate(final Node x) {
		if (x != nil) {
			Node n = x.left;
			x.left = n.right;
			if (n.right != nil) {
				n.right.parent = x;
			}
			n.parent = x.parent;
			if (x.parent == nil) {
				root = n;
			} else if (x.parent.right == x) {
				x.parent.right = n;
			} else {
				x.parent.left = n;
			}
			n.right = x;
			x.parent = n;
		}
	}

	/**
	 * Introduction to Algorithms (CLR) Second Edition
	 * 
	 * <pre>
	 * RB-INSERT-FIXUP(T, z)
	 * while color[p[z]] = RED
	 *    do if p[z] = left[p[p[z]]]
	 *          then y = right[p[p[z]]]
	 *               if color[y] = RED
	 *                  then color[p[z]] = BLACK                                    Case 1
	 *                       color[y] = BLACK                                               Case 1 
	 *                       color[p[p[z]]] = RED                                   Case 1
	 *                       z = p[p[z]]                                                    Case 1
	 *                  else if z = right[p[z]]
	 *                          then z = p[z]                                               Case 2
	 *                               LEFT-ROTATE(T, z)                              Case 2
	 *                       color[p[z]] = BLACK                                    Case 3
	 *                       color[p[p[z]]] = RED                                   Case 3
	 *                       RIGHT-ROTATE(T, p[p[z]])                               Case 3
	 *          else (same as then clause
	 *                        with right and left exchanged)
	 * color[root[T]] = BLACK
	 */
	private void fixAfterInsertion(Node z) {
		z.color = Color.RED;
		while (z.parent.color == Color.RED) {
			if (z.parent == z.parent.parent.left) {
				Node y = z.parent.parent.right;
				if (y.color == Color.RED) {
					z.parent.color = Color.BLACK;
					y.color = Color.BLACK;
					z.parent.parent.color = Color.RED;
					z = z.parent.parent;
				} else {
					if (z == z.parent.right) {
						z = z.parent;
						leftRotate(z);
					}
					z.parent.color = Color.BLACK;
					z.parent.parent.color = Color.RED;
					rightRotate(z.parent.parent);
				}
			} else {
				Node y = z.parent.parent.left;
				if (y.color == Color.RED) {
					z.parent.color = Color.BLACK;
					y.color = Color.BLACK;
					z.parent.parent.color = Color.RED;
					z = z.parent.parent;
				} else {
					if (z == z.parent.left) {
						z = z.parent;
						rightRotate(z);
					}
					z.parent.color = Color.BLACK;
					z.parent.parent.color = Color.RED;
					leftRotate(z.parent.parent);
				}
			}
		}
		root.color = Color.BLACK;
	}

	/**
	 * Introduction to Algorithms (CLR) Second Edition
	 * 
	 * <pre>
	 * RB-DELETE-FIXUP(T, x)
	 * while x != root[T] and color[x] = BLACK
	 *    do if x = left[p[x]]
	 *          then w = right[p[x]]
	 *               if color[w] = RED
	 *                  then color[w] = BLACK                                                               Case 1
	 *                       color[p[x]] = RED                                                              Case 1
	 *                       LEFT-ROTATE(T, p[x])                                                   Case 1
	 *                       w = right[p[x]]                                                                Case 1
	 *               if color[left[w]] = BLACK and color[right[w]] = BLACK
	 *                  then color[w] = RED                                                                 Case 2
	 *                       x = p[x]                                                                               Case 2
	 *                  else if color[right[w]] = BLACK
	 *                          then color[left[w]] = BLACK                                 Case 3
	 *                               color[w] = RED                                                 Case 3
	 *                               RIGHT-ROTATE(T,w)                                              Case 3
	 *                               w = right[p[x]]                                                Case 3
	 *                       color[w] = color[p[x]]                                                 Case 4
	 *                       color[p[x]] = BLACK                                                    Case 4
	 *                       color[right[w]] = BLACK                                                Case 4
	 *                       LEFT-ROTATE(T, p[x])                                                   Case 4
	 *                       x = root[T]                                                                    Case 4
	 *          else (same as then clause with right and left exchanged)
	 * color[x] = BLACK
	 */
	private void fixAfterDeletion(Node x) {
		while (x != root && x.color == Color.BLACK) {
			if (x == x.parent.left) {
				Node w = x.parent.right;
				if (w.color == Color.RED) {
					w.color = Color.BLACK;
					x.parent.color = Color.RED;
					leftRotate(x.parent);
					w = x.parent.right;
				}
				if (w.left.color == Color.BLACK && w.right.color == Color.BLACK) {
					w.color = Color.RED;
					x = x.parent;
				} else {
					if (w.right.color == Color.BLACK) {
						w.left.color = Color.BLACK;
						w.color = Color.RED;
						rightRotate(w);
						w = x.parent.right;
					}
					w.color = x.parent.color;
					x.parent.color = Color.BLACK;
					x.right.color = Color.BLACK;
					leftRotate(x.parent);
					x = root;
				}
			} else {
				Node w = x.parent.left;
				if (w.color == Color.RED) {
					w.color = Color.BLACK;
					x.parent.color = Color.RED;
					rightRotate(x.parent);
					w = x.parent.left;
				}
				if (w.left.color == Color.BLACK && w.right.color == Color.BLACK) {
					w.color = Color.RED;
					x = x.parent;
				} else {
					if (w.left.color == Color.BLACK) {
						w.right.color = Color.BLACK;
						w.color = Color.RED;
						leftRotate(w);
						w = x.parent.left;
					}
					w.color = x.parent.color;
					x.parent.color = Color.BLACK;
					w.left.color = Color.BLACK;
					rightRotate(x.parent);
					x = root;
				}
			}
		}
		x.color = Color.BLACK;
	}

}
