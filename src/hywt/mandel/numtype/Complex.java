package hywt.mandel.numtype;

public class Complex {
    private double re;
    private double im;
    private Double absValue;

    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public double abs() {
        if (absValue == null) {
            absValue = Math.sqrt(abs2());
        }
        return absValue;
    }

    public double abs2() {
        return re * re + im * im;
    }

    public Complex mul(Complex o) {
        return new Complex(
                re * o.re - im * o.im,
                re * o.im + im * o.re
        );
    }

    public Complex mulMut(Complex c) {
        double temp = re;
        re = re * c.re - im * c.im;
        im = temp * c.im + im * c.re;
        return this;
    }

    public Complex mul(double m) {
        return new Complex(re * m, im * m);
    }

    public Complex mulMut(double m) {
        re *= m;
        im *= m;
        return this;
    }


    public Complex add(Complex other) {
        return new Complex(re + other.re, im + other.im);
    }

    public Complex addMut(Complex c) {
        re += c.re;
        im += c.im;
        return this;
    }

    public Complex sub(Complex other) {
        return new Complex(re - other.re, im - other.im);
    }

    public Complex subMut(Complex c) {
        re -= c.re;
        im -= c.im;
        return this;
    }

    public double getRe() {
        return re;
    }

    public double getIm() {
        return im;
    }

    @Override
    public String toString() {
        return String.format("%g%+gi", re, im);
    }

    public FloatExpComplex toFloatExp() {
        return new FloatExpComplex(
                FloatExp.fromDouble(re),
                FloatExp.fromDouble(im)
        );
    }

    public double norm(){
        return Math.abs(Math.max(re,im));
    }

    public Complex copy()  {
        return new Complex(re, im);
    }
}
