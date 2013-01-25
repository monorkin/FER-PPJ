package hr.unizg.fer.zemris.ppj.maheri;

public class Logger {

	public static final boolean DEBUG = false;

	public static final void log(Object s) {
		if (DEBUG) {
			System.err.println(s.toString());
		}
	}

}
