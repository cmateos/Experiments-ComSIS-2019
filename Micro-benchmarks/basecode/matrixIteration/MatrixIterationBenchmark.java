package matrixIteration;

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

public class MatrixIterationBenchmark extends SimpleBenchmark {
	@Param({"2", "20", "2000"}) int matrixSize;
	int[][] matrix;
	
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		matrix= new int[matrixSize][matrixSize];
	}
	
	public int timeIterateCols(int reps){
		int result=0;
		for (int rep=0; rep<reps; ++rep){
			for (int i=0; i<matrixSize; i++){
				for (int j=0; j<matrixSize; j++){
					result= matrix[j][i];
				}
			}
		}
		return result;
	}
	
	public int timeIterateRows(int reps){
		int result=0;
		for (int rep=0; rep<reps; ++rep){
			for (int i=0; i<matrixSize; i++){
				for (int j=0; j<matrixSize; j++){
					result= matrix[i][j];
				}
			}
		}
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		String[] a = new String[1];
		a[0] = "-Jvmtype=-client";
		Runner.main(MatrixIterationBenchmark.class, a);
	}

}
