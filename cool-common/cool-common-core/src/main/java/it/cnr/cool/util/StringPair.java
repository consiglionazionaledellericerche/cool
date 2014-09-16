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
