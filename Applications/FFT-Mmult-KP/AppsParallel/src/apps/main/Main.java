package apps.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import apps.Knapsack.Knapsack;
import apps.KnapsackMod.KnapsackMod;
import apps.fft.Fft;
import apps.fftModificada.FftMod;
import apps.mmult.Mmult;
import apps.mmultMod.MmultMod;
import bateria.runner.Runner;

public class Main {
	
	private CountDownLatch latch;
	private ExecutorService pool;
	
	public static void main(String[] args) {
		Properties.WAIT=0;
		Main main = new Main();
		if (args.length==1){
			main.run(args[0]);
		} else if (args.length > 1){
			for (int i=2; i<args.length; i++){
				if (args[i].startsWith("-i")){
					Properties.ITERATIONS = Integer.parseInt(args[i].substring(2));
				} else if (args[i].startsWith("-w")){
					Properties.WAIT = Integer.parseInt(args[i].substring(2));
				} else if (args[i].startsWith("-r")){
					Properties.RUNS = Integer.parseInt(args[i].substring(2));
				} else if (args[i].startsWith("-t")){
					Properties.NUM_THREADS = Integer.parseInt(args[i].substring(2));
				} else {
					System.err.println("Error en el pasaje de parámetros..");
					System.exit(1);
				}
			}
			System.out.println("Properties configuration:");
			System.out.println("Wait: " + Properties.WAIT);
			System.out.println("Iterations: " + Properties.ITERATIONS);
			System.out.println("Runs: " + Properties.RUNS);
			System.out.println("Number of threads: " + Properties.NUM_THREADS);
			main.run(args[0]);
		} else {
			System.err.println("Error en el pasaje de parámetros..");
			System.exit(1);
		}
		System.out.println("La ejecución termino exitosamente");
	}
	
	public static Runner getRunner(String method){
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
		return runner;
	}
	
	public void run(String method){
		Runner runner = getRunner(method);
		try {
			Thread.sleep(Properties.WAIT);
		} catch (Exception e) {}
		for (int j = 0; j < Properties.ITERATIONS; j++){
			long inicio = System.currentTimeMillis();
			latch = new CountDownLatch(Properties.NUM_THREADS);
			pool = Executors.newFixedThreadPool(Properties.NUM_THREADS);
			runSingleExecution(runner);
			pool.shutdown();
			Main.escribir(method + "_" + j, inicio, System.currentTimeMillis());
		}
	}

	public void runSingleExecution(Runner runner){
		int runs = Properties.RUNS / Properties.NUM_THREADS;
		for (int i = 0; i < Properties.NUM_THREADS; i++){
			pool.execute(new RunnerThread(runner, runs, latch));
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("End..");
	}
	
	public static void escribir(String nombreArchivo, long inicio, long fin)
	{
		File f;
		f = new File(nombreArchivo);
		try{
			FileWriter w = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(w);
			PrintWriter wr = new PrintWriter(bw);  
			wr.write("Inicio: " + inicio + "\n");//escribimos en el archivo
			wr.append("Fin: " + fin + "\n"); 
			wr.append("Segundos Totales: " + (fin-inicio)/1000 + "\n"); 
			wr.append("Mediciones Totales: " + (fin-inicio)/1000*2 + "\n"); 
			wr.close();
			bw.close();
		}catch(IOException e){};
	
	}

}
