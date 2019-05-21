

import java.util.concurrent.CountDownLatch;

public class RunnerThread implements Runnable {

	private Runner runner;
	private int runs;
	private CountDownLatch latch;
	
	public RunnerThread(Runner runner, int runs, CountDownLatch latch){
		this.runner = runner;
		this.runs = runs;
		this.latch = latch;
	}
	
	@Override
	public void run() {
		for (int i = 0; i < runs; i++)
			runner.run();
		latch.countDown();
	}

}
