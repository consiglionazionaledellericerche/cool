/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.cool.util;

/**
 * A pair of keys. It is used with DualHashSet and DualHashMap to represent a
 * pair of keys as an object.
 * 
 * @author mspasiano
 */
public class StringPair implements java.io.Serializable, Comparable<StringPair> {
	private static final long serialVersionUID = 1L;
	/** The first key. */
	public final String first;
	/** The second key. */
	public final String second;

	public StringPair(String first, String second) {
		this.first = first;
		this.second = second;
	}

	protected StringPair() {
		this(null, null);
	}

	/**
	 * Returns the first value of the pair.
	 */
	public String getFirst() {
		return this.first;
	}

	/**
	 * Returns the second value of the pair.
	 */
	public String getSecond() {
		return this.second;
	}

	// -- Object --//
	public final boolean equals(Object o) {
		if (!(o instanceof StringPair))
			return false;
		final StringPair pair = (StringPair) o;
		return first.equals(pair.first)
				&& second.equals(pair.second);
	}

	public final int hashCode() {
		return first.hashCode() ^ second.hashCode();
	}

	public String toString() {
		return '(' + first.toString() + ", " + second.toString()
				+ ')';
	}

	@Override
	public int compareTo(StringPair o) {
		return first.compareTo(o.first);
	}
}
