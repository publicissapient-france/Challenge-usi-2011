package fr.xebia.usiquizz.sort.biscotti;

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

import java.util.Collection;
import java.util.Comparator;

/**
 * A {@link Collection} that further provides a <i>total ordering</i> on its
 * elements. This interface is the root of all <i>sorted</i> collection
 * interfaces and implementations.
 * <p>
 * Classes which implement this interface (directly or indirectly) are required
 * to implement the {@code comparator()} method which returns the comparator
 * used to order the elements in this collection. Essentially this allows
 * another sorted collection of a desired type to create a copy of this
 * collection.
 * <p>
 * In addition all implementing classes are expected to provide three static
 * creation methods: {@code create()}, returning a collection that orders its
 * elements according to their <i>natural ordering</i>, {@code
 * create(Comparator)} returning a collection which uses the specified
 * comparator to order its elements, and {@code create(Iterable)} returning a
 * collection containing the given initial elements. This is simply a refinement
 * of Java's constructor recommendations, reflecting the new developments of
 * Java 5.
 * 
 * @author Zhenya Leonov
 * @param <E>
 *            the type of elements held in this collection
 */
public interface SortedCollection<E> extends Collection<E> {

	/**
	 * Returns the comparator used to order the elements in this collection.
	 * Care must be taken when using <i>natural ordering</i>. Implementations
	 * may choose to return a natural order comparator or {@code null}.
	 */
	public Comparator<? super E> comparator();

}
