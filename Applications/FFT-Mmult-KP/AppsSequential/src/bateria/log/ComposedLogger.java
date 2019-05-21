package bateria.log;

public class ComposedLogger extends Logger {
	
	private Logger log1;
	private Logger log2;

	public ComposedLogger(Logger log1, Logger log2) {
		super();
		this.log1 = log1;
		this.log2 = log2;
	}

	@Override
	public void log(String data) {
		this.log1.log(data);
		this.log2.log(data);
	}

}
