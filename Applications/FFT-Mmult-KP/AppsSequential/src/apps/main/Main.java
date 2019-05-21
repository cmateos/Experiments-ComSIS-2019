package apps.main;

import apps.Knapsack.Knapsack;
import apps.KnapsackMod.KnapsackMod;
import apps.fft.Fft;
import apps.fftModificada.FftMod;
import apps.mmult.Mmult;
import apps.mmultMod.MmultMod;
import bateria.runner.Runner;

public class Main {

	public static void main(String[] args) {
		Properties.espera=0;
		if (args.length==1){
			run(args[0]);
		} else if (args.length > 1){
			for (int i=2; i<args.length; i++){
				if (args[i].startsWith("-i")){
					Properties.iteraciones = Integer.parseInt(args[i].substring(2));
				} else if (args[i].startsWith("-e")){
					Properties.espera = Integer.parseInt(args[i].substring(2));
				} else if (args[i].startsWith("-r")){
					Properties.runs = Integer.parseInt(args[i].substring(2));
				} else {
					System.err.println("Error en el pasaje de parámetros..");
					System.exit(1);
				}
			}
			run(args[0]);
		} else {
			System.err.println("Error en el pasaje de parámetros..");
			System.exit(1);
		}
		System.out.println("La ejecución termino exitosamente");
	}
	
	public static void run(String method){
		Runner runner = null;
		switch(method){
		case "fft": runner = new Fft(); Properties.fileLabel="original"; break;
		case "fftMod": runner = new FftMod(); Properties.fileLabel="refactored"; break;
		case "knap": runner = new Knapsack(); Properties.fileLabel="original"; break;
		case "knapMod": runner = new KnapsackMod(); Properties.fileLabel="refactored"; break;
		case "mmult": runner = new Mmult(); Properties.fileLabel="refactored"; break;
		case "mmultMod": runner = new MmultMod(); Properties.fileLabel="original"; break;
		}
		if (runner == null){
			System.err.println("Error en el pasaje de parámetros..");
			System.exit(1);
		}
		else runner.run();
	}

}
