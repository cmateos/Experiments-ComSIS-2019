package primitives;

import matrixIteration.MatrixIterationBenchmark;

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

public class PrimitivesBenchmark extends SimpleBenchmark {
	@Param({"2000", "10000","20000"}) int limit;
	
	public long timeNonPrimitive(int reps){
		Long ret=0L;
		for (int rep=0; rep < reps; ++rep){
			for (long i = 0; i < limit; i++) {
				ret+= i;
			}
		}
		return ret;		
	}
	
	public long timePrimitive(int reps){
		long ret=0L;
		for (int rep=0; rep < reps; ++rep){
			for (long i = 0; i < limit; i++) {
				ret+= i;
			}
		}
		return ret;		
	}
	
	public static void main(String[] args) throws Exception {
		String[] a = new String[1];
		a[0] = "-Jvmtype=-server,-client";
		Runner.main(PrimitivesBenchmark.class, a);
	}

}
