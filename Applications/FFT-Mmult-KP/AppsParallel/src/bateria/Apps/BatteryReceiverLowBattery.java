package bateria.Apps;

import bateria.log.Logger;
import bateria.runner.Runner;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BatteryReceiverLowBattery extends BatteryReceiver {
	
	public BatteryReceiverLowBattery(Lock lock, Runner runner1, Runner runner2) {
		super(lock, runner1, runner2);
		this.lock.unlock();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive");
		Logger.getLogger().log("Bateria baja");
		Logger.getLogger().log("Veces; "+ lock.lock());
	}

}
