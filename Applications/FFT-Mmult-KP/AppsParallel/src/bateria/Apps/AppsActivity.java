package bateria.Apps;

import java.io.FileNotFoundException;

import bateria.log.ComposedLogger;
import bateria.log.FileThreadLogger;
import bateria.log.LogLogger;
import bateria.log.Logger;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import apps.Knapsack.Knapsack;
import apps.KnapsackMod.KnapsackMod;
import apps.mmult.Mmult;
import apps.mmultMod.MmultMod;
import apps.fft.Fft;
import apps.fftModificada.FftMod;

public class AppsActivity extends Activity {
private static String TAG="Activity";
	
	private static EditText cant;
	private Button[] buttons;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d(TAG,"Create");     
        cant = (EditText)findViewById(R.id.cant);
        this.llenarBotones();
           
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG,"Destroyed");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG,"Paused");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG,"Stopped");
	}
	
	private void llenarBotones(){
		this.buttons=new Button[6];
        int i=0;
        this.buttons[i]=(Button)findViewById(R.id.fft);
        this.buttons[i].setOnClickListener(new ButtonListener(Fft.class.getCanonicalName()));
        i++;
        this.buttons[i]=(Button)findViewById(R.id.FftMod);
        this.buttons[i].setOnClickListener(new ButtonListener(FftMod.class.getCanonicalName()));
        i++;
        this.buttons[i]=(Button)findViewById(R.id.Knapsack);
        this.buttons[i].setOnClickListener(new ButtonListener(Knapsack.class.getCanonicalName()));
        i++;
        this.buttons[i]=(Button)findViewById(R.id.KnapsackMod);
        this.buttons[i].setOnClickListener(new ButtonListener(KnapsackMod.class.getCanonicalName()));
        i++;
        this.buttons[i]=(Button)findViewById(R.id.Mmult);
        this.buttons[i].setOnClickListener(new ButtonListener(Mmult.class.getCanonicalName()));
        i++;
        this.buttons[i]=(Button)findViewById(R.id.MmultMod);
        this.buttons[i].setOnClickListener(new ButtonListener(MmultMod.class.getCanonicalName()));
        i++;
	}
	
	private class ButtonListener implements View.OnClickListener {	
		private String clazz;
		
		public ButtonListener(String clazz) {
			super();
			this.clazz = clazz;
		}

		public void onClick(View v) {
			for(Button b:buttons)
				b.setEnabled(false);	
			try {
				Log.d(TAG,"Creo el archivo");
				Log.d(TAG,clazz.toString());
				Logger logger = new FileThreadLogger(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Bateria--"+clazz.toString()+".csv",true);			
				logger=new ComposedLogger(logger, new LogLogger("Benchmark"));
        		Logger.setLogger(logger);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			Intent i=new Intent(AppsActivity.this, BatteryService.class);
			i.putExtra(BatteryService.RUNNER, new RunnerSender(clazz));
			i.putExtra(util.Constants.EJECUCIONES, cant.getText().toString());

			startService(i);
		}
	}
}