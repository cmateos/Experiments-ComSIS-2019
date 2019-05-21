package bateria.Apps;

import bateria.runner.Runner;

import android.content.BroadcastReceiver;
import android.util.Log;

public abstract class BatteryReceiver extends BroadcastReceiver {
	
	protected static String TAG="BatteryReceiver";
	protected Lock lock;
	protected Runner runner1=null, runner2=null;
		
	public BatteryReceiver(Lock lock, Runner runner1, Runner runner2){
		Log.d(TAG, "Starting Battery Receiver");
		this.lock=lock;
		this.runner1=runner1;
		this.runner2=runner2;
	}

}
