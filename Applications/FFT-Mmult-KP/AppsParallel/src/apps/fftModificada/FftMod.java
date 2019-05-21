package apps.fftModificada;

import bateria.runner.Runner;

//reemplazo double por float, creo menos objetos
public class FftMod extends Runner implements FFTInterface {

	public void run() {

		int N = 64; // ver
		ComplexMod[] x = new ComplexMod[N];
		// original data
		for (int j = 0; j < N; j++) {
			x[j] = new ComplexMod(j, 0);
		}
		// FFT apps.fft = new FFT();
		// FFT of original data
		fft(x);

	}

	@Override
	public void JGFrun(int size) {
		// TODO Auto-generated method stub

	}

	// compute the FFT of x[], assuming its length is a power of 2
	public ComplexMod[] fft(ComplexMod[] x) {
		int N = x.length;

		// base case
		if (N == 1) {// cambio
			return x;
			// y[0] = new ComplexMod(x[0].re,x[0].im);
			// return y;
		}
		ComplexMod[] y = new ComplexMod[N];

		// radix 2 Cooley-Tukey FFT
		if (N % 2 != 0)
			throw new RuntimeException("N is not a power of 2");
		int j = N / 2;
		ComplexMod[] even = new ComplexMod[j];
		ComplexMod[] odd = new ComplexMod[j];

		for (int k = 0; k < j; k++) {
			even[k] = x[2 * k];
			// for (int k = 0; k < N / 2; k++)
			odd[k] = x[2 * k + 1];
		}

		ComplexMod[] q = fft(even);
		ComplexMod[] r = fft(odd);

		ComplexMod wk = new ComplexMod(0, 0); // CAMBIO!!!!!!!!!!!!

		double kth1 = -2 * Math.PI / N;
		double kth;

		for (int k = 0; k < j; k++) {
			kth = (k * kth1);
			wk.setReal((float) Math.cos(kth));
			wk.setImag((float) Math.sin(kth));
			// CAMBIO
			wk.times(r[k]);

			// CAMBIOOOO!!!!!!
			y[k] = new ComplexMod(0, 0);
			y[k + N / 2] = new ComplexMod(0, 0);

			y[k + N / 2].minus(q[k], wk);
			y[k].plus(wk, q[k]);
		}
		return y;
	}

	// compute the inverse FFT of x[], assuming its length is a power of 2
	// public ComplexMod[] ifft(ComplexMod[] x) {
	// int N = x.length;
	//
	// // take conjugate
	// for (int i = 0; i < N; i++)
	// x[i] = x[i].conjugate();
	//
	// // compute forward FFT
	// ComplexMod[] y = fft(x);
	//
	// // take conjugate again
	// for (int i = 0; i < N; i++)
	// y[i] = y[i].conjugate();
	//
	// // divide by N
	// for (int i = 0; i < N; i++)
	// y[i] = y[i].times(1 / N);
	//
	// return y;
	//
	// }

	// // compute the convolution of x and y
	// public ComplexMod[] convolve(ComplexMod[] x, ComplexMod[] y) {
	// if (x.length != y.length)
	// throw new RuntimeException("Dimensions don't agree");
	// int N = x.length;
	//
	// // compute FFT of each sequence
	// ComplexMod[] a = fft(x);
	// ComplexMod[] b = fft(y);
	//
	// // point-wise multiply
	// ComplexMod[] c = new ComplexMod[N];
	// for (int i = 0; i < N; i++)
	// c[i] = a[i].times(b[i]);
	//
	// // compute inverse FFT
	// return ifft(c);
	// }

}
