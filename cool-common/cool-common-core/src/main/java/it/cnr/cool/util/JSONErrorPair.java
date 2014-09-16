package it.cnr.cool.util;

public class JSONErrorPair extends StringPair {
	private static final long serialVersionUID = 1L;
	
	public JSONErrorPair() {
		super();
	}

	public JSONErrorPair(String first, String second) {
		super(first, second);
	}

	@Override
	public String toString() {
		return "[\"" + first.toString() + "\",\"" + second.toString()
				+ "\"]";
	}
}
