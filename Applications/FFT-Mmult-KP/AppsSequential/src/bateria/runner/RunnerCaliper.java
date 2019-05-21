package bateria.runner;

import util.Constants;
import bateria.Apps.Lock;


public abstract class RunnerCaliper implements Runnable{
	
	protected Lock lock;
	protected int runs = Constants.RUNS;
	protected int sleep = Constants.SLEEP;
	
	public Lock getLock() {
		return lock;
	}
	
	public void setLock(Lock lock) {
		this.lock = lock;
	}

	public abstract void run();
	public abstract void JGFrun(int size);
	

}
