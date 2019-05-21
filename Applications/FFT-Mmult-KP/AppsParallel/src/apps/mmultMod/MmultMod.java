package apps.mmultMod;

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
public class MmultMod extends Runner {

	private long inicio, fin;

	public void run() {
		int task = 2, rec = 2, loop = (int) Math.pow(2, 2);

		MmultMod m = new MmultMod();

		MatrixMod a = new MatrixMod(task, rec, loop, 1);
		MatrixMod b = new MatrixMod(task, rec, loop, 1);
		MatrixMod c = new MatrixMod(task, rec, loop, 0);

		m.mult(task, rec, a, b, c);
	}

	public void escribir(String nombreArchivo) {
		File f;
		f = new File(nombreArchivo);
		try {
			FileWriter w = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(w);
			PrintWriter wr = new PrintWriter(bw);
			wr.write("Inicio: " + inicio + "\n");// escribimos en el archivo
			wr.append("Fin: " + fin + "\n");
			wr.append("Segundos Totales: " + (fin - inicio) / 1000 + "\n");
			wr.append("Mediciones Totales: " + (fin - inicio) / 1000 * 2 + "\n");
			wr.close();
			bw.close();
		} catch (IOException e) {
		}
		;

	}

	@Override
	public void JGFrun(int size) {
		// TODO Auto-generated method stub

	}

	public void mult(int task, int rec, MatrixMod a, MatrixMod b, MatrixMod c) {

		// if (task == 0) {
		c.multiplyStride2(a, b);
		// }

		// task--;
		//
		// /* spawn */mult(task, rec, a._00, b._00, c._00);
		// /* spawn */mult(task, rec, a._00, b._01, c._01);
		// /* spawn */mult(task, rec, a._10, b._00, c._10);
		// /* spawn */mult(task, rec, a._10, b._01, c._11);
		//
		// /* spawn */mult(task, rec, a._01, b._10, c._00);
		// /* spawn */mult(task, rec, a._01, b._11, c._01);
		// /* spawn */mult(task, rec, a._11, b._10, c._10);
		// /* spawn */mult(task, rec, a._11, b._11, c._11);

	}

}
