package hywt.mandel.numtype;

import java.io.Serializable;

public class FloatExpComplex implements Serializable {
    private FloatExp re;
    private FloatExp im;

    public FloatExpComplex(FloatExp re, FloatExp im) {
        this.re = re;
        this.im = im;
    }

    public FloatExpComplex(double i, double j) {
        this(FloatExp.fromDouble(i), FloatExp.fromDouble(j));
    }

    public FloatExp abs2() {
        // |z| = sqrt(re^2 + im^2)
        return re.mul(re).addMut(im.mul(im));
    }

    public FloatExpComplex mul(FloatExpComplex other) {
        return new FloatExpComplex(
                re.mul(other.re).sub(im.mul(other.im)),
                re.mul(other.im).add(im.mul(other.re))
        );
    }

    public FloatExpComplex mul(double other) {
        return new FloatExpComplex(re.mul(new FloatExp(other,0)), im.mul(new FloatExp(other,0)));
    }

    public FloatExpComplex square() {
        return new FloatExpComplex(re.square().subMut(im.square()), re.mul(im).mul(2));
    }

    public FloatExpComplex div(FloatExpComplex other) {
        return new FloatExpComplex(
                this.re.div(other.re).subMut(this.im.div(other.im)),
                this.re.div(other.im).addMut(this.im.div(other.re))
        );
    }

    public FloatExpComplex add(FloatExpComplex other) {
        return new FloatExpComplex(
                this.re.add(other.re),
                this.im.add(other.im)
        );
    }

    public FloatExpComplex sub(FloatExpComplex other) {
        return new FloatExpComplex(
                this.re.sub(other.re),
                this.im.sub(other.im)
        );
    }

    public FloatExpComplex addMut(FloatExpComplex other) {
        this.re.addMut(other.re);
        this.im.addMut(other.im);
        return this;
    }

    public FloatExpComplex subMut(FloatExpComplex other) {
        this.re.subMut(other.re);
        this.im.subMut(other.im);
        return this;
    }

    public FloatExpComplex mulMut(FloatExpComplex other) {
        FloatExp x = re;
        FloatExp y = im;
        re = x.mul(other.re).subMut(y.mul(other.im));
        im = x.mulMut(other.im).addMut(y.mulMut(other.re));
        return this;
    }

    public FloatExpComplex copy() {
        return new FloatExpComplex(re.copy(), im.copy());
    }

    public Complex toComplex() {
        return new Complex(re.doubleValue(), im.doubleValue());
    }

    public DeepComplex toDeepComplex() {
        return new DeepComplex(
                re.toBigDecimal(),
                im.toBigDecimal()
        );
    }

    public FloatExp norm() {
        return re.abs().addMut(im.abs());
//        if (re.compareTo(im) > 0) return re.abs();
//        else return im.abs();
    }

    public FloatExp getRe() {
        return re;
    }

    public FloatExp getIm() {
        return im;
    }

    @Override
    public String toString() {
        return String.format("%s+%si", re, im);
    }
}

