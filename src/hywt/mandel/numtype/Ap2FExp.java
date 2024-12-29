package hywt.mandel.numtype;

import org.apfloat.Apcomplex;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

public class Ap2FExp {
    public static FloatExp ap2fe(Apfloat ap){
        double mant = ApfloatMath.scale(ap, -ap.scale() + 1).doubleValue();
        long exp = ap.scale() - 1;
        return new FloatExp(mant, (int) exp);
    }

    public static FloatExpComplex apc2fec(Apcomplex c){
        return new FloatExpComplex(
                ap2fe(c.real()), ap2fe(c.imag())
        );
    }
}
