package bateria.Apps;

public class Lock{

	private int count=0;
	private boolean canWork=true;
	
	public Lock(){
	}
	
	//Método para el Runner
	public synchronized void waitForAutorization() throws InterruptedException{
	   while(!canWork){
		  wait();
	   }
	   count++;
	}
	
	public synchronized int lock(){
		canWork=false;
		return count;
	}
	
	public synchronized void unlock(){
	   count=0;
	   canWork=true;
	   notifyAll();
	}

}