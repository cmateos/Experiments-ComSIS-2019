package apps.fft;

import bateria.runner.Runner;

public class Fft extends Runner implements FFTInterface{
		
	public void run() {
		//int iteracion = 0;
		//int runs = Properties.runs;
		//try {
		//	Thread.sleep(Properties.espera);
		//} catch (Exception e) {}
		//inicio = System.currentTimeMillis();
		//while(iteracion < Properties.iteraciones){
		//	try {
				//lock.waitForAutorization();
				 // test client
		//		for(int i=0; i<runs; i++){
			        int N = 64; //ver
			        Complex[] x = new Complex[N];
			        // original data
			        for (int j = 0; j < N; j++) {
			            x[j] = new Complex(j, 0);
			        }
			        //FFT apps.fft = new FFT();
			        // FFT of original data
			        fft(x);
		//		}
				//Thread.sleep(100);
		//	} catch (Exception e) {
		//		e.printStackTrace();
		//		Logger.getLogger().log("Excepcion al intentar de ejecutar el thread \n");
		//	}
		//	iteracion++;
		//}
		//fin = System.currentTimeMillis();
		//escribir("Medicion manual Fft");
	}

	@Override
	public void JGFrun(int size) {
		// TODO Auto-generated method stub
		
	}
	
	// compute the FFT of x[], assuming its length is a power of 2
    public Complex[] fft(Complex[] x) {
        int N = x.length;
        Complex[] y = new Complex[N];

        // base case
        if (N == 1) {
            y[0] = x[0];
            return y;
        }

        // radix 2 Cooley-Tukey FFT
        if (N % 2 != 0)
            throw new RuntimeException("N is not a power of 2");
        Complex[] even = new Complex[N / 2];
        Complex[] odd = new Complex[N / 2];
        for (int k = 0; k < N / 2; k++)
            even[k] = x[2 * k];
        for (int k = 0; k < N / 2; k++)
            odd[k] = x[2 * k + 1];

        Complex[] q = fft(even);
        Complex[] r = fft(odd);

        for (int k = 0; k < N / 2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k] = q[k].plus(wk.times(r[k]));
            y[k + N / 2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }

    // compute the inverse FFT of x[], assuming its length is a power of 2
    public Complex[] ifft(Complex[] x) {
        int N = x.length;

        // take conjugate
        for (int i = 0; i < N; i++)
            x[i] = x[i].conjugate();

        // compute forward FFT
        Complex[] y = fft(x);

        // take conjugate again
        for (int i = 0; i < N; i++)
            y[i] = y[i].conjugate();

        // divide by N
        for (int i = 0; i < N; i++)
            y[i] = y[i].times(1.0 / N);

        return y;

    }

    // compute the convolution of x and y
    public Complex[] convolve(Complex[] x, Complex[] y) {
        if (x.length != y.length)
            throw new RuntimeException("Dimensions don't agree");
        int N = x.length;

        // compute FFT of each sequence
        Complex[] a = fft(x);
        Complex[] b = fft(y);

        // point-wise multiply
        Complex[] c = new Complex[N];
        for (int i = 0; i < N; i++)
            c[i] = a[i].times(b[i]);

        // compute inverse FFT
        return ifft(c);
    }

   
}