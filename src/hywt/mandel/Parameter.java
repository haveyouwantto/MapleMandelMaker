package hywt.mandel;

import hywt.mandel.numtype.DeepComplex;
import hywt.mandel.numtype.FloatExp;

public class Parameter {
    private final DeepComplex center;
    private final FloatExp scale;
    private final long maxIter;

    public Parameter(DeepComplex center, FloatExp scale, long maxIter) {
        this.center = center;
        this.scale = scale;
        this.maxIter = maxIter;
    }

    public DeepComplex getCenter() {
        return center;
    }

    public FloatExp getScale() {
        return scale;
    }

    public long getMaxIter() {
        return maxIter;
    }
}
