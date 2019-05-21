package exceptions;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

public class ExceptionBenchmark extends SimpleBenchmark {
	
	private void isEven(int number) throws EvenException {
		if (number % 2 == 0)
			throw new EvenException();
	}

	public int timeUseException(int reps) {
		int ret = 0;
		for (int rep = 0; rep < reps; ++rep) {
			try {
				isEven(rep);
			} catch (EvenException e) {
				// TODO Auto-generated catch block
				ret++;
			}
		}
		return ret;
	}

	public int timeNoException(int reps) {
		int ret = 0;
		for (int rep = 0; rep < reps; ++rep) {
			if (rep % 2 == 0)
				ret++;
		}
		return ret;
	}
	
	public static void main(String[] args) throws Exception {
		String[] a = new String[1];
		a[0] = "-Jvmtype=-client";
		Runner.main(ExceptionBenchmark.class, a);
	}

}
