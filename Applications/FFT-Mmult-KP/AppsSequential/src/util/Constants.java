package util;

public class Constants {
	public static int RUNS= 150000;
	public final static int SLEEP= 0;
	public static final String BATERIABAJA = "Bateria baja";
	public static final String EJECUCIONES = "Ejecuciones";
	
	public static void setEjecuciones(int cant){
		RUNS = cant;
	}
}
