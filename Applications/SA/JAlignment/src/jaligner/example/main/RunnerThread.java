package jaligner.example.main;

import java.util.concurrent.CountDownLatch;

import jaligner.Alignment;
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
		for (int i = 0; i < runs; i++)
			try { 
				Alignment al = SmithWatermanGotoh.align(s1, s2, MatrixLoader.load("BLOSUM62"), 10f, 0.5f);
				//System.out.println(al.getOriginalSequence1().toString().substring(10).trim());
				//System.out.println(al.getSequence1());
				//System.out.println(al.getSequence2());
				//System.out.println(al.getOriginalSequence2().toString().substring(10).trim());
			} catch (MatrixLoaderException e) {	e.printStackTrace(); }
		
		latch.countDown();
	}

}
