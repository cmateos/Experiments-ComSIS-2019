package jaligner.example.main;

import java.util.concurrent.CountDownLatch;

import jaligner.Sequence;
import jaligner.SmithWatermanGotoh;
import jaligner.matrix.MatrixLoader;
import jaligner.matrix.MatrixLoaderException;

public class RunnerThread implements Runnable {

	private int runs;
	private CountDownLatch latch;
	private Sequence s1, s2;
	
	public RunnerThread(int runs, CountDownLatch latch, Sequence s1, Sequence s2){
		this.runs = runs;
		this.latch = latch;
		this.s1 = s1;
		this.s2 = s2;
	}
	
	@Override
	public void run() {
		try { 
		jaligner.matrix.Matrix matrix62 = MatrixLoader.load("BLOSUM62");
		for (int i = 0; i < runs; i++)
				SmithWatermanGotoh.align(s1, s2, matrix62, 10f, 0.5f);
		latch.countDown();
		} catch (MatrixLoaderException e) {	e.printStackTrace(); }
	}

}
