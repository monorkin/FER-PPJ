package hr.unizg.fer.zemris.ppj.maheri;

public class Logger {

	public static final boolean DEBUG = true;

	public static final void log(Object s) {
		if (DEBUG) {
			System.out.println(s);
		}
	}

}
