package bateria.Apps;

import bateria.runner.Runner;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class RunnerSender implements Parcelable{

	public static final Parcelable.Creator<RunnerSender> CREATOR = new Parcelable.Creator<RunnerSender>() {

		public RunnerSender createFromParcel(Parcel source) {
			return new RunnerSender(source.readString());
		}

		
		public RunnerSender[] newArray(int size) {
			return new RunnerSender[size];
		}
		
	};
	
	private String runnerClass;
	
	public RunnerSender(String runnerClass) {
		super();
		this.runnerClass = runnerClass;
	}

	@SuppressWarnings("unchecked")
	public Runner getRunner() {
		Runner r=null;
		try {
			Class<Runner> c=(Class<Runner>) Class.forName(runnerClass);
			r=(Runner) c.newInstance();
		} catch (Exception e) {
			Log.d("Benchmark", "",e);
		}
		return r;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(runnerClass);
	}

}
