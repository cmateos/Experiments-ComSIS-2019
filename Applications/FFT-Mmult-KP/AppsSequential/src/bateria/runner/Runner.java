package bateria.runner;

import bateria.Apps.Lock;
import util.*;

public abstract class Runner implements Runnable{
	
	protected Lock lock;
	protected int runs = Constants.RUNS;
	protected int sleep = Constants.SLEEP;
	
	public Lock getLock() {
		return lock;
	}
	
	public void setLock(Lock lock) {
		this.lock = lock;
	}

	public int getRuns(){
		return runs;
	}
	
	public abstract void run();
	public abstract void JGFrun(int size);
	

}
