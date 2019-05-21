package apps.fftModificada;

public class ComplexMod {
	public float re; // the real part

     public float im; // the imaginary part

    // create a new object with the given real and imaginary parts
    public ComplexMod(float real, float imag) {
        this.re = real;
        this.im = imag;
    }

    public void setReal(float real){
        this.re = real;
    }
    
    public void setImag(float imag){
        this.im = imag;
    }
    // return a string representation of the invoking object
    public String toString() {
        return re + " + " + im + "i";
    }

    // return |this|
    public float abs() {
        return (float) Math.sqrt(re * re + im * im);
    }
    
    //CAMBIO
    // return a new object whose value is (this + b)
    public void plus(ComplexMod b, ComplexMod c) {
        this.re = c.re + b.re;
        this.im = c.im + b.im;
    }

    //CAMBIO
    // return a new object whose value is (this - b)
    public void minus(ComplexMod b, ComplexMod c) {
        this.re = b.re - c.re;
        this.im = b.im - c.im;
    }

    // return a new object whose value is (this * b)
    public void times(ComplexMod b) {
    	float a = this.re * b.re - this.im * b.im;
        this.setImag(this.re * b.im + this.im * b.re);
        this.setReal(a);
    }

    // return a new object whose value is (this * alpha)
    public void times(float alpha) {
        this.setReal(alpha * re);
        this.setImag(alpha * im);
    }

    // return a new object whose value is the conjugate of this
    public ComplexMod conjugate() {
        return new ComplexMod(re, -im);
    }

}
