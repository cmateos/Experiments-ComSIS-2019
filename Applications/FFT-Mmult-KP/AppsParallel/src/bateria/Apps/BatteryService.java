package bateria.Apps;

import util.Constants;
import bateria.log.Logger;
import bateria.runner.Runner;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class BatteryService extends Service{
	
	private static final String TAG="Service";
	public static final String RUNNER = "RUNNER";
	
	private PowerManager pm;
	private PowerManager.WakeLock wl;
	private BatteryReceiver batteryReceiver;
	private Runner runner1, runner2;
	private Lock lock=new Lock();
	
	private int cant;
	
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "Create");
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
		this.lock.lock();
	}
	
	public void onStart(Intent intent, int startId){
		super.onStart(intent, startId);
		Log.d(TAG, "Start");
		wl.acquire();
		
		Bundle extras = intent.getExtras();
		this.cant = Integer.parseInt(extras.getString(util.Constants.EJECUCIONES));
		Constants.setEjecuciones(this.cant);
		Logger.getLogger().log("Corridas; " + Constants.RUNS);
		runner1=((RunnerSender)intent.getParcelableExtra(RUNNER)).getRunner();
		runner1.setLock(lock);
		runner2=((RunnerSender)intent.getParcelableExtra(RUNNER)).getRunner();
		runner2.setLock(lock);

		IntentFilter filter=null;
		this.batteryReceiver=new BatteryReceiverLowBattery(lock,runner1, runner2);		
		filter=new IntentFilter(Intent.ACTION_BATTERY_LOW);
		this.registerReceiver(this.batteryReceiver, filter);	
		
		Thread t=new Thread(runner1);
		Thread t2=new Thread(runner2);
		t.start();
		t2.start();
	}

	public void onDestroy() {
		Log.d(TAG, "Destroy");
		this.unregisterReceiver(this.batteryReceiver);
		this.stopForeground(true);
		this.stopForeground(true);
		wl.release();
		super.onDestroy();
	}

	public void onLowMemory() {
		Log.d(TAG, "Low Memory");
		super.onLowMemory();
	}

	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "unbind");
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		return null;
	}
	
}
