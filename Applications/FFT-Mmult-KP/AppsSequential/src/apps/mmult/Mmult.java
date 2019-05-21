package apps.mmult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import apps.main.Properties;
import bateria.log.Logger;
import bateria.runner.Runner;

/* $Id$ */

//
// Class MmultMod
//
// MatrixMod multiply functionality
// This is the ony really interesting part
// The rest is dead weight
//
public  class Mmult extends Runner implements MmultInterface{

	private long inicio, fin;
	
	public void run() {
		int iteracion = 0;
		int runs = Properties.runs;
		try {
			Thread.sleep(Properties.espera);
		} catch (Exception e) {}
		inicio = System.currentTimeMillis();
		
		while(iteracion < Properties.iteraciones){
			try {
				//lock.waitForAutorization();
				 // test client
				for(int j=0; j<runs; j++){
					int task = 2, rec = 2, loop = power(2, 2);

			        Mmult m = new Mmult();

//		            task = Integer.parseInt(args[0]);
//		            rec = Integer.parseInt(args[1]);
//		            loop = Integer.parseInt(args[2]);
			        
			        int cells = power(2, task + rec) * loop;

			        Matrix a = new Matrix(task, rec, loop, 1.0f, false); // a is row-wise flipped, to make product zero
			        Matrix b = new Matrix(task, rec, loop, 1.0f, false);
			        Matrix c = new Matrix(task, rec, loop, 0.0f, false);

			        c = /* spawn */m.mult(task, rec, a, b, c);
//			        Log.d("MmultMod","termino");

				}
				//Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.getLogger().log("Excepcion al intentar de ejecutar el thread \n");
			}
			iteracion++;
		}
		fin = System.currentTimeMillis();
		escribir("Medicion manual Mmult");
	}

	public void escribir(String nombreArchivo)
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

	@Override
	public void JGFrun(int size) {
		// TODO Auto-generated method stub
		
	}
	
    // real  functionality: tasked-mat-mul
    public Matrix mult(int task, int rec, Matrix a, Matrix b, Matrix c) {

        Matrix f_00 = null;
        Matrix f_01;
        Matrix f_10;
        Matrix f_11;

        if (task == 0) {
            // switch to serial recursive part
            // pass instance variables
            c.recMatMul(rec, a, b);
            return c;
        }

        if (task > 0) {
            task--;
        } else {
            rec--;
        }

        f_00 = /* spawn */mult(task, rec, a._00, b._00, c._00);
        f_01 = /* spawn */mult(task, rec, a._00, b._01, c._01);
        f_10 = /* spawn */mult(task, rec, a._10, b._00, c._10);
        f_11 = /* spawn */mult(task, rec, a._10, b._01, c._11);

        c._00 = /* spawn */mult(task, rec, a._01, b._10, f_00);
        c._01 = /* spawn */mult(task, rec, a._01, b._11, f_01);
        c._10 = /* spawn */mult(task, rec, a._11, b._10, f_10);
        c._11 = /* spawn */mult(task, rec, a._11, b._11, f_11);

        return c;
    }

    public static int power(int base, int exponent) {
        return (int) Math.pow(base, exponent);
    }

//    public static void main(String args[]) {
//        int task = 2, rec = 2, loop = power(2, 2);
//
//        MmultMod m = new MmultMod();
//
//        if (args.length == 3) {
//            task = Integer.parseInt(args[0]);
//            rec = Integer.parseInt(args[1]);
//            loop = Integer.parseInt(args[2]);
//        } else if (args.length != 0) {
//            System.out.println("usage: mmult [task rec loop]");
//            System.exit(66);
//        }
//
//        int cells = power(2, task + rec) * loop;
//        System.out.println("Running MatrixMod multiply, on a matrix of size "
//                + cells + " x " + cells + ", threads = " + power(8, task));
//
//        MatrixMod a = new MatrixMod(task, rec, loop, 1.0f, false); // a is row-wise flipped, to make product zero
//        MatrixMod b = new MatrixMod(task, rec, loop, 1.0f, false);
//        MatrixMod c = new MatrixMod(task, rec, loop, 0.0f, false);
//
//        c = /* spawn */m.mult(task, rec, a, b, c);
//
//    }
}

