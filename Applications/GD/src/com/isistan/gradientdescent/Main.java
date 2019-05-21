package com.isistan.gradientdescent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.isistan.gradientdescent.modified.GDMatrix;
import com.isistan.gradientdescent.modified.IndexesNotMatchingException;
import com.isistan.gradientdescent.original.GradientDescent;

public class Main {
	
	private CountDownLatch latch;
	private ExecutorService pool;
	
	public static void main(String[] args) throws IndexesNotMatchingException, InterruptedException{
		// The first parameter is the type of algorithm used
		if (args.length < 1){
			System.err.println("Error en el pasaje de parámetros..");
			System.exit(1);
		}
		String algorithm = args[0];
		
		// Then we set the parameters
		Main.proccessParameters(args);
		
		Main main = new Main();
		main.run(algorithm);
		
		System.out.println("Se ha terminado de ejecutar exitosamente..");
	}
	
	public static void proccessParameters(String[] args){
		Properties.WAIT =0;
		if (args.length > 1){
			for (int i=1; i<args.length; i++){
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
		}
		System.out.println("Properties configuration:");
		System.out.println("Wait: " + Properties.WAIT);
		System.out.println("Iterations: " + Properties.ITERATIONS);
		System.out.println("Runs: " + Properties.RUNS);
		System.out.println("Number of threads: " + Properties.NUM_THREADS);
	}
	
	public Runner getRunner(String type){
		switch(type){
		case "refactored": return new GradientDescent(Properties.X_MATRIX, Properties.Y_MATRIX, 
				Properties.ALPHA, Properties.ALGORITHM_ITERATIONS);
		case "original": return new GDMatrix(Properties.X_MATRIX, Properties.Y_MATRIX, 
				Properties.ALPHA, Properties.ALGORITHM_ITERATIONS);
		}
		return null;
	}
	
	public static void escribir(String nombreArchivo, long inicio, long fin) {
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
	}
}
