package bateria.log;

import bateria.log.Logger;

public class LogLogger extends Logger {

	private String tag;
	public LogLogger(String tag) {
		super();
		this.tag = tag;
	}
	@Override
	public void log(String data) {
		//Log.i(tag, data);
	}

}
