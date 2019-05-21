package weka;

import java.util.concurrent.CountDownLatch;

import weka.classifiers.Classifier;
import weka.core.Instances;

public class RunnerThread implements Runnable {

	private Runner runner;
	private int runs;
	private CountDownLatch latch;
	
//	public RunnerThread(Runner runner, int runs, CountDownLatch latch){
//		this.runner = runner;
//		this.runs = runs;
//		this.latch = latch;
//	}
	
	public RunnerThread(int runs, CountDownLatch latch, Classifier classifier, Instances tempTrain) throws Exception {
		this.runner = new ClassifierRunner(classifier, tempTrain);
		this.runs = runs;
		this.latch = latch;
		
	}

	@Override
	public void run() {
		long millis=System.currentTimeMillis();
		for (int i = 0; i < runs; i++)
			runner.run();
		latch.countDown();
		System.out.println("runner time: "+(System.currentTimeMillis()-millis));
	}

}
