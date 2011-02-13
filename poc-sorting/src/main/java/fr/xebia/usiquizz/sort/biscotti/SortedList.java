package fr.xebia.usiquizz.sort.biscotti;

/*
 *
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

import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

/**
 * A {@link List} that further provides a <i>total ordering</i> on its elements.
 * This interface is the {@code List} analog of {@link SortedSet}. The elements
 * are ordered using their <i>natural ordering</i>, or by an explicit
 * {@link Comparator} provided at creation time.
 * <p>
 * To take advantage of the specified ordering additional {@code headList(E)},
 * {@code subList(E, E)}, {@code tailList(E)} operations are provided.
 * 
 * @author Zhenya Leonov
 * @param <E>
 *            the type of elements held in this list
 */
public interface SortedList<E> extends SortedCollection<E>, List<E> {

	/**
	 * Returns a view of the portion of this list whose elements are strictly
	 * less than {@code toElement}. This implies that if the list contains
	 * duplicate elements, <i>none</i> of the elements which are equal to
	 * {@code toElement}, will be included in the returned list. The returned
	 * list is backed by this list, so changes in the returned list are
	 * reflected in this list.
	 * <p>
	 * Attempts to insert an element outside the specified range will result in
	 * an {@code IllegalArgumentException}.
	 * 
	 * @param toElement
	 *            high endpoint (exclusive) of the returned list
	 * @return a view of the portion of this list whose elements are strictly
	 *         less than {@code toElement}
	 * @throws ClassCastException
	 *             if {@code toElement} is not comparable to the elements in
	 *             this list
	 * @throws NullPointerException
	 *             if {@code toElement} is {@code null} and this list does not
	 *             permit {@code null} elements
	 */
	public SortedList<E> headList(E toElement);

	/**
	 * Returns a view of the portion of this list whose elements range from
	 * {@code fromElement}, inclusive, to {@code toElement}, exclusive. This
	 * implies that if the list contains duplicate elements, <i>all</i> of the
	 * elements which are equal to {@code fromElement} and <i>none</i> of the
	 * elements which are equal to {@code toElement}, will be included in the
	 * returned list. (If {@code fromElement} and {@code toElement} are equal,
	 * the returned list is empty.) The returned list is backed by this list, so
	 * changes in the returned list are reflected in this list.
	 * <p>
	 * Attempts to insert an element outside the specified range will result in
	 * an {@code IllegalArgumentException}.
	 * 
	 * @param fromElement
	 *            low endpoint (inclusive) of the returned list
	 * @param toElement
	 *            high endpoint (exclusive) of the returned list
	 * @return a view of the portion of this list whose elements range from
	 *         {@code fromElement}, inclusive, to {@code toElement}, exclusive
	 * @throws ClassCastException
	 *             if {@code fromElement} or {@code toElement} is not comparable
	 *             to the elements in this list
	 * @throws NullPointerException
	 *             if {@code fromElement} or {@code toElement} is {@code null}
	 *             and this list does not permit {@code null} elements
	 * @throws IllegalArgumentException
	 *             if {@code fromElement} is greater than {@code toElement}
	 */
	public SortedList<E> subList(E fromElement, E toElement);

	/**
	 * {@inheritDoc}
	 * <p>
	 * Attempts to insert an element outside the specified range will result in
	 * an {@code IllegalArgumentException}.
	 */
	@Override
	public SortedList<E> subList(int fromIndex, int toIndex);

	/**
	 * Returns a view of the portion of this list whose elements are greater
	 * than or equal to {@code fromElement}. This implies that if the list
	 * contains duplicate elements, <i>all</i> of the elements which are equal
	 * to {@code fromElement}, will be included in the returned list. The
	 * returned list is backed by this list, so changes in the returned list are
	 * reflected in this list.
	 * <p>
	 * Attempts to insert an element outside the specified range will result in
	 * an {@code IllegalArgumentException}.
	 * 
	 * @param fromElement
	 *            low endpoint (inclusive) of the returned list
	 * @return a view of the portion of this list whose elements are greater
	 *         than or equal to {@code fromElement}
	 * @throws ClassCastException
	 *             if {@code fromElement} is not comparable to the elements in
	 *             this list
	 * @throws NullPointerException
	 *             if {@code fromElement} is {@code null} and this list does not
	 *             permit {@code null} elements
	 */
	public SortedList<E> tailList(E fromElement);

}