package methodInvocation;

import matrixIteration.MatrixIterationBenchmark;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

public class MethodInvocationBenchmark extends SimpleBenchmark {
	private int field;
	
	public int getField(){
		return field;
	}
	
	public int timeGetter(int reps) {
		int result = 0;
        for (int i = 0; i < reps; ++i) {
            result = getField();
        }
        return result;
	}
	
	public int timeDirectAccess(int reps){
		int result= 0;
		for (int i=0; i<reps; ++i){
			result= field;
		}
		return result;
		
	}
	
	public static void main(String[] args) throws Exception {
		String[] a= new String[1];
		a[0]= "-Jvmtype=-client";
	    Runner.main(MethodInvocationBenchmark.class,a);
	  }

}
