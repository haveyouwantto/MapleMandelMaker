package hywt.mandel;

import hywt.mandel.numtype.DeepComplex;
import hywt.mandel.numtype.FloatExp;
import org.apfloat.Apcomplex;

import java.io.Serializable;

public class Parameter implements Serializable {
    private final Apcomplex center;
    private final FloatExp scale;
    private final long maxIter;
    private final double bailout;

    public Parameter(Apcomplex center, FloatExp scale, long maxIter, double bailout) {
        this.center = center;
        this.scale = scale;
        this.maxIter = maxIter;
        this.bailout = bailout;
    }

    public Apcomplex getCenter() {
        return center;
    }

    public FloatExp getScale() {
        return scale;
    }

    public long getMaxIter() {
        return maxIter;
    }

    public double getBailout() {
        return bailout;
    }

    @Override
    public String toString() {
        return "Parameter{" +
                "center=" + center +
                ", scale=" + scale +
                ", maxIter=" + maxIter +
                '}';
    }
}
