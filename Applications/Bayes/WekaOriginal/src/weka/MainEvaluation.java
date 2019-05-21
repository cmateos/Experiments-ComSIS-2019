// 
// Decompiled by Procyon v0.5.30
// 

package weka;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import weka.classifiers.bayes.BayesNet;
import weka.core.Instances;
import weka.classifiers.Classifier;

public class MainEvaluation {
	public static String[] ARGS = new String[] { "-t", "datasets/training.arff", "-c", "last" };
	public static int ITERATIONS;
	public static int RUNS;
	public static int WAIT;
	public static int NUM_THREADS;

	private CountDownLatch latch;
	private ExecutorService pool;
	private Classifier classifier;
	private Instances tempTrain;
/**
	static {
		MainEvaluation.ARGS = 
		MainEvaluation.ITERATIONS = 1500;
		MainEvaluation.RUNS = 5;
		MainEvaluation.WAIT = 0;
		MainEvaluation.NUM_THREADS = 1;
	}
**/
	public MainEvaluation(Classifier classifier, Instances tempTrain) {
		this.classifier = classifier;
		this.tempTrain = tempTrain;
	}

	public static void evaluate(final Classifier classifier, final Instances tempTrain) throws Exception {
		MainEvaluation main = new MainEvaluation(classifier, tempTrain);
		System.out.println("Iniciando la prueba..");
		main.runEvaluation();
		System.out.println("Prueba finalizada..");
		System.exit(0);
	}

	public static void main(final String[] args) throws InterruptedException, FileNotFoundException {
		proccessParameters(args);
		Thread.sleep(MainEvaluation.WAIT);
		BayesNet.main(MainEvaluation.ARGS);
	}

	public static void proccessParameters(final String[] args) {
		MainEvaluation.WAIT = 0;
		if (args.length > 0) {
			for (int i = 0; i < args.length; ++i) {
				if (args[i].startsWith("-i")) {
					MainEvaluation.ITERATIONS = Integer.parseInt(args[i].substring(2));
				} else if (args[i].startsWith("-w")) {
					MainEvaluation.WAIT = Integer.parseInt(args[i].substring(2));
				} else if (args[i].startsWith("-r")) {
					MainEvaluation.RUNS = Integer.parseInt(args[i].substring(2));
				} else if (args[i].startsWith("-t")){
					MainEvaluation.NUM_THREADS = Integer.parseInt(args[i].substring(2));
				} else {
					System.err.println("Error en el pasaje de parametros..");
					System.exit(1);
				}
			}
		}
		System.out.println("Properties configuration:");
		System.out.println("Wait: " + MainEvaluation.WAIT);
		System.out.println("Iterations: " + MainEvaluation.ITERATIONS);
		System.out.println("Runs: " + MainEvaluation.RUNS);
		System.out.println("Number of threads: " + MainEvaluation.NUM_THREADS);
	}

	public static void escribir(final String nombreArchivo, final long inicio, final long fin) {
		final File f = new File(nombreArchivo);
		try {
			final FileWriter w = new FileWriter(f);
			final BufferedWriter bw = new BufferedWriter(w);
			final PrintWriter wr = new PrintWriter(bw);
			wr.write("Inicio: " + inicio + "\n");
			wr.append("Fin: " + fin + "\n");
			wr.append("Segundos Totales: " + (fin - inicio) / 1000L + "\n");
			wr.append("Mediciones Totales: " + (fin - inicio) / 1000L * 2L + "\n");
			wr.close();
			bw.close();
		} catch (IOException ex) {
		}
	}

	public void runEvaluation() throws Exception {
		// try {
		// Thread.sleep(MainEvaluation.WAIT);
		// } catch (Exception e) {}
		//
		for (int j = 0; j < MainEvaluation.RUNS; j++) {
			long inicio = System.currentTimeMillis();
			latch = new CountDownLatch(MainEvaluation.NUM_THREADS);
			pool = Executors.newFixedThreadPool(MainEvaluation.NUM_THREADS);

			runSingleExecution();

			pool.shutdown();
			MainEvaluation.escribir("original_" + j, inicio, System.currentTimeMillis());
		}
	}

	public void runSingleExecution() throws Exception {
		int runs = MainEvaluation.ITERATIONS / MainEvaluation.NUM_THREADS;
//		copy = AbstractClassifier.makeCopy(classifier);
		for (int i = 0; i < MainEvaluation.NUM_THREADS; i++) {
			pool.execute(new RunnerThread(runs, latch, classifier, tempTrain));
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
