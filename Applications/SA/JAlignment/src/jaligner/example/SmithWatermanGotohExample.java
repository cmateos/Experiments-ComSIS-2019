/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jaligner.example;

import jaligner.Sequence;
import jaligner.example.main.Properties;
import jaligner.example.main.RunnerThread;
import jaligner.util.SequenceParser;
import jaligner.util.SequenceParserException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Example of using JAligner API to align P53 human aganist
 * P53 mouse using Smith-Waterman-Gotoh algorithm.
 *
 * @author Ahmed Moustafa
 */

public class SmithWatermanGotohExample {
	
	/**
	 * 
	 */
	private static final String SAMPLE_SEQUENCE_P35_HUMAN = "jaligner/example/sequences/p53_human.fasta";
	
	/**
	 * 
	 */
	private static final String SAMPLE_SEQUENCE_P35_MOUSE = "jaligner/example/sequences/p53_mouse.fasta";
	
	/**
	 * Logger
	 */
	private static final Logger logger = Logger.getLogger(SmithWatermanGotohExample.class.getName());
		
	/**
	 * 
	 * @param args
	 */
//	public static void main(String[] args) {
//		if (args.length > 0)
//			runs = Integer.parseInt(args[0]);
//        try {
//        	logger.info("Running example...");
//        	
//			Sequence s1 = SequenceParser.parse(loadP53Human());  
//			Sequence s2 = SequenceParser.parse(loadP53Mouse());
//	        
//			Alignment alignment = null;
//			for (int i=0; i<runs; i++){
//				 alignment = SmithWatermanGotoh.align(s1, s2, MatrixLoader.load("BLOSUM62"), 10f, 0.5f);
//			}
//			
//	        System.out.println ( alignment.getSummary() );
//	        System.out.println ( new Pair().format(alignment) );
//	        
//	        logger.info("Finished running example");
//        } catch (Exception e) {
//        	logger.log(Level.SEVERE, "Failed running example: " + e.getMessage(), e);
//        }
//    }
	
	private CountDownLatch latch;
	private ExecutorService pool;
	
	public static void main(String[] args) throws InterruptedException, SequenceParserException, IOException{		
		// Set the parameters
		SmithWatermanGotohExample.proccessParameters(args);
		Logger.getLogger(jaligner.SmithWatermanGotoh.class.getName()).setLevel(java.util.logging.Level.OFF);
		Logger.getLogger(jaligner.matrix.MatrixLoader.class.getName()).setLevel(java.util.logging.Level.OFF);
		SmithWatermanGotohExample main = new SmithWatermanGotohExample();
		main.run();
		
		System.out.println("Se ha terminado de ejecutar exitosamente..");
	}
	
	public static void proccessParameters(String[] args){
		Properties.WAIT =0;
		if (args.length > 0){
			for (int i=0; i<args.length; i++){
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
	
	public void run() throws SequenceParserException, IOException{
		try {
			Thread.sleep(Properties.WAIT);
		} catch (Exception e) {}
		
		Sequence s1 = SequenceParser.parse(loadP53Human());  
		Sequence s2 = SequenceParser.parse(loadP53Mouse());
		
		for (int j = 0; j < Properties.ITERATIONS; j++){
			long inicio = System.currentTimeMillis();
			latch = new CountDownLatch(Properties.NUM_THREADS);
			pool = Executors.newFixedThreadPool(Properties.NUM_THREADS);
        				
			runSingleExecution(s1, s2);
			
			pool.shutdown();
			SmithWatermanGotohExample.escribir("refactored_" + j, inicio, System.currentTimeMillis());
		}
	}

	public void runSingleExecution(Sequence s1, Sequence s2){
		int runs = Properties.RUNS / Properties.NUM_THREADS;
		for (int i = 0; i < Properties.NUM_THREADS; i++){
			pool.execute(new RunnerThread(runs, latch, s1, s2));
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("End..");
	}
	
//	public static void runS(){
//		try {
//			Thread.sleep(Properties.WAIT);
//		} catch (Exception e) {}
//		inicio = System.currentTimeMillis();
//		try {
//        	logger.info("Running example...");
//	        
//			Alignment alignment = null;
//			for (int i=0; i<Properties.RUNS; i++){
//				 alignment = SmithWatermanGotoh.align(s1, s2, MatrixLoader.load("BLOSUM62"), 10f, 0.5f);
//			}
//			
//	        System.out.println ( alignment.getSummary() );
//	        System.out.println ( new Pair().format(alignment) );
//	        
//	        logger.info("Finished running example");
//        } catch (Exception e) {
//        	logger.log(Level.SEVERE, "Failed running example: " + e.getMessage(), e);
//        }
//		fin = System.currentTimeMillis();
//		escribir("Medicion manual SmithWaterman");
//	}
	
	/**
	 * 
	 * @param path location of the sequence
	 * @return sequence string
	 * @throws IOException
	 */
	private static String loadSampleSequence(String path) throws IOException {
		InputStream inputStream = NeedlemanWunschExample.class.getClassLoader().getResourceAsStream(path);
        StringBuffer buffer = new StringBuffer();
        int ch;
        while ((ch = inputStream.read()) != -1) {
            buffer.append((char)ch);
        }
        return buffer.toString();
	}
	
	/**
	 * 
	 * @return sequence string
	 * @throws IOException
	 */
	public static String loadP53Human( ) throws IOException {
		return loadSampleSequence(SAMPLE_SEQUENCE_P35_HUMAN);
	}

	/**
	 * 
	 * @return sequence string
	 * @throws IOException
	 */
	public static String loadP53Mouse( ) throws IOException {
		return loadSampleSequence(SAMPLE_SEQUENCE_P35_MOUSE);
	}
}
