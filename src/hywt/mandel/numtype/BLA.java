package hywt.mandel.numtype;

public class BLA {
    public Complex A;
    public Complex B;
    public double radius;

    public BLA(Complex A, Complex B, double radius) {
        this.A = A;
        this.B = B;
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "BLA{" +
                "A=" + A +
                ", B=" + B +
                ", radius=" + radius +
                '}';
    }
}