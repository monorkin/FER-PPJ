package hr.unizg.fer.zemris.ppj.maheri.parser;

public class Production {

	private final String key;
	private final String value;

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public Production(final String key, final String value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("%s->%s", key, value);
	}

}
