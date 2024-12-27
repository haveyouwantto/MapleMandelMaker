package hywt.mandel.numtype;

public class BLAFE {
    public FloatExpComplex A;
    public FloatExpComplex B;
    public FloatExp radius;

    public BLAFE(FloatExpComplex A, FloatExpComplex B, FloatExp radius) {
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