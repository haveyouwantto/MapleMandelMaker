package hywt.mandel;

import hywt.mandel.numtype.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Mandelbrot {
    private DeepComplex center;
    private FloatExp scale;
    private long maxIter;
    private double bailout;
    private List<FloatExpComplex> ref;

    public Mandelbrot(DeepComplex center, FloatExp scale, long maxIter, double bailout) {
        this.center = center;
        this.scale = scale;
        this.maxIter = maxIter;
        this.bailout = bailout * bailout;
        ref = new ArrayList<>();
    }

    public Mandelbrot(DeepComplex center, FloatExp scale) {
        this(center, scale, 256, 4);
    }

    public Mandelbrot(Parameter p) {
        this(p.getCenter(), p.getScale(), p.getMaxIter(), 4);
    }

    private void getReferenceOrbit() {
        int precision = -scale.scale() + 10;
        var Z = new DeepComplex(0, 0).setPrecision(precision);
        ;
        var z = new FloatExpComplex(0, 0);
        var dzdc = new FloatExpComplex(1, 0);
        var one = new FloatExpComplex(1, 0);

        ref.clear();
        ref.add(z);

        for (int i = 0; i < this.maxIter; i++) {
            dzdc = z.add(z).mulMut(dzdc).addMut(one);
            Z = Z.mul(Z).add(this.center);

            z = Z.toFloatExp();
            ref.add(z);

            if (dzdc.norm().mul(this.scale).mul(2).compareTo(z.norm()) > 0) break;
            if (z.norm().doubleValue() > 16.0) break;

        }
        System.out.println(ref);
    }

    private List<List<BLA>> createBLATable(List<Complex> ref, double scale) {
        List<List<BLA>> table = new ArrayList<>();
        List<BLA> lv1 = new ArrayList<>();
        table.add(lv1);

        for (int i = 1; i < ref.size(); i++) {
            Complex point = ref.get(i);

            Complex A = point.mul(2);
            Complex B = new Complex(1, 0);
            double radius = Math.max(0, (point.norm() - B.norm() * scale) / (A.norm() + 1) * 5.96e-8);

            lv1.add(new BLA(A, B, radius));
        }

        int level = 1;
        while (true) {
            List<BLA> currentLevel = new ArrayList<>();
            table.add(currentLevel);

            List<BLA> previousLevel = table.get(level - 1);

            for (int i = 0; i < previousLevel.size(); i += 2) {
                BLA bla1 = previousLevel.get(i);
                BLA bla2 = (i + 1 < previousLevel.size()) ? previousLevel.get(i + 1) : null;

                if (bla2 == null) {
                    currentLevel.add(bla1);
                    continue;
                }

                Complex newA = bla1.A.mul(bla2.A);
                Complex newB = bla1.B.mul(bla2.A).add(bla2.B);
                double newRadius = Math.min(
                        bla1.radius,
                        Math.max(0, (bla2.radius - bla1.B.norm() * scale) / bla1.A.norm())
                );

                currentLevel.add(new BLA(newA, newB, newRadius));
            }

            if (currentLevel.size() <= 1) {
                break;
            }

            level++;
        }

        return table;
    }

    public static class LookupResult {
        public BLA first;
        public int second;

        public LookupResult(BLA first, int second) {
            this.first = first;
            this.second = second;
        }
    }

    public static LookupResult lookup(List<List<BLA>> table, int i, int refLen, double normDz, double normDc) {
        if (i == 0 || i >= refLen || table.isEmpty()) {
            return new LookupResult(null, 0);
        }

        LookupResult result = new LookupResult(null, 0);
        int index = i - 1;
        int length = 1;

        for (List<BLA> level : table) {
            if (normDz > level.get(index).radius) {
                break;
            }

            result.first = level.get(index);
            result.second = length;

            if (index % 2 != 0) {
                break;
            }

            index >>= 1; // equivalent to index = Math.floor(index / 2)
            length <<= 1; // equivalent to length *= 2
        }

        result.second = Math.min(result.second, refLen - i);
        return result;
    }

    private double getPTBLA(
            Complex dc,
            List<Complex> ref,
            List<List<BLA>> table,
            double dzRe,
            double dzIm,
            double iter,
            int refIter
    ) {
        double dcNorm = Math.max(Math.abs(dc.getRe()), Math.abs(dc.getIm()));

        while (iter < maxIter) {
            double dzNorm = Math.max(Math.abs(dzRe), Math.abs(dzIm)); // Chebyshev norm

            LookupResult result = lookup(table, refIter, ref.size(), dzNorm, dcNorm);

            if (result.first != null) {
                double aRe = result.first.A.getRe(), aIm = result.first.A.getIm();
                double bRe = result.first.B.getRe(), bIm = result.first.B.getIm();

                double newDzRe = dzRe * aRe - dzIm * aIm + dc.getRe() * bRe - dc.getIm() * bIm;
                double newDzIm = dzRe * aIm + dzIm * aRe + dc.getRe() * bIm + dc.getIm() * bRe;

                dzRe = newDzRe;
                dzIm = newDzIm;

                iter += result.second;
                refIter += result.second;
            } else {
                Complex Z = ref.get(refIter);

                double tempRe = dzRe, tempIm = dzIm;

                // dz = dz * 2 * Z + dz^2 + dc
                dzRe = (2 * Z.getRe() + tempRe) * tempRe - (2 * Z.getIm() + tempIm) * tempIm + dc.getRe();
                dzIm = 2 * (Z.getRe() * tempIm + Z.getIm() * tempRe + tempRe * tempIm) + dc.getIm();

                iter++;
                refIter++;
            }

            if (refIter >= ref.size()) {
                return iter;
            }

            Complex Z2 = ref.get(refIter);
            double valRe = Z2.getRe() + dzRe;
            double valIm = Z2.getIm() + dzIm;

            // Squared Euclidean distance (abs squared)
            double valAbsSq = valRe * valRe + valIm * valIm;
            if (valAbsSq > bailout) {
                double fracIter = Math.log(valAbsSq) / 2;
                fracIter = Math.log(fracIter / Math.log(2)) / Math.log(2);
                iter += 1 - fracIter;
                return iter;
            }

            double dzAbsSq = dzRe * dzRe + dzIm * dzIm;
            if (valAbsSq < dzAbsSq || refIter == ref.size() - 1) {
                dzRe = valRe;
                dzIm = valIm;
                refIter = 0;
            }
        }

        return iter;
    }

    public Complex getDelta(double x, double y, int w, int h) {
        int min = Math.min(w, h);
        double baseStep = 1d / min;
        double scale = this.scale.doubleValue();
        double deltaX = (x - w / 2.0) * baseStep;
        double deltaY = (h / 2.0 - y) * baseStep;
        return new Complex(scale * deltaX, scale * deltaY);
    }

    public void render(IterationMap map) {
        getReferenceOrbit();

        List<Complex> refComplex = ref.stream().map(FloatExpComplex::toComplex).toList();
        List<List<BLA>> table = createBLATable(refComplex, this.scale.doubleValue());

        int width = map.getWidth();
        int height = map.getHeight();
        int baseX = width / 2;
        int baseY = height / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Complex delta = getDelta(x, y, width, height);
                double iterations = getPTBLA(delta, refComplex, table, 0, 0, 0, 0);
                map.setPixel(x,y,iterations);
            }
        }
        System.out.println(table);
    }
}
