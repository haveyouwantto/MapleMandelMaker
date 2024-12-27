package hywt.mandel;

import hywt.mandel.numtype.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Mandelbrot {
    private DeepComplex center;
    private FloatExp scale;
    private long maxIter;
    private double bailout;
    private List<FloatExpComplex> ref;
    private List<Complex> refComplex;
    private List<List<BLA>> tableComplex;
    private List<List<BLAFE>> table;

    protected boolean refVaild;


    private ExecutorService service;

    public Mandelbrot(DeepComplex center, FloatExp scale, long maxIter, double bailout) {
        this.center = center;
        this.scale = scale;
        this.maxIter = maxIter;
        this.bailout = bailout * bailout;
        ref = new ArrayList<>();
        refVaild = false;

        int threads = Runtime.getRuntime().availableProcessors();
        service = Executors.newFixedThreadPool(6);
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

    public static List<List<BLAFE>> createBLATableFE(List<FloatExpComplex> ref, FloatExp scale) {
        List<BLAFE> lv1 = new ArrayList<>();
        List<List<BLAFE>> table = new ArrayList<>();
        table.add(lv1);

        for (int i = 1; i < ref.size(); i++) {
            FloatExpComplex point = ref.get(i);
            FloatExpComplex A = point.mul(2);
            FloatExpComplex B = new FloatExpComplex(1, 0);

            lv1.add(new BLAFE(
                    A,
                    B,
                    FloatExp.max(new FloatExp(0, 0),
                            (point.norm().sub(B.norm().mul(scale))).div(A.norm().add(1)).mul(5.96e-8)
                    )
            ));
        }

        int level = 1;
        while (true) {
            table.add(new ArrayList<>());

            for (int i = 0; i < table.get(level - 1).size(); i += 2) {
                BLAFE bla1 = table.get(level - 1).get(i);
                BLAFE bla2 = (i + 1 < table.get(level - 1).size()) ? table.get(level - 1).get(i + 1) : null;

                if (bla2 == null) {
                    table.get(level).add(bla1);
                    continue;
                }

                table.get(level).add(new BLAFE(
                        bla1.A.mul(bla2.A),
                        bla1.B.mul(bla2.A).add(bla2.B),
                        FloatExp.min(
                                bla1.radius, FloatExp.max(
                                        new FloatExp(0, 0), (bla2.radius.sub(bla1.B.norm().mul(scale))).div(bla1.A.norm())
                                )
                        )
                ));
            }

            if (table.get(level).size() <= 1) break;
            level++;
        }

        return table;
    }

    static class BLALookupResult {
        BLAFE first;
        int second;

        BLALookupResult(BLAFE first, int second) {
            this.first = first;
            this.second = second;
        }
    }

    public static BLALookupResult lookupFE(List<List<BLAFE>> table, int i, int refLen, FloatExp norm_dz, FloatExp norm_dc) {
        if (i == 0 || i >= refLen || table.isEmpty()) {
            return new BLALookupResult(null, 0);
        }

        BLALookupResult result = new BLALookupResult(null, 0);
        int index = i - 1;
        int length = 1;

        for (List<BLAFE> level : table) {
            if (norm_dz.compareTo(level.get(index).radius) > 0) {
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

    PTBLAFEResult getPTBLAFE(FloatExpComplex dc, List<FloatExpComplex> ref, List<List<BLAFE>> table) {
        FloatExpComplex dz = new FloatExpComplex(0, 0);

        double iter = -1;
        int refIter = 0;
        FloatExp normDc = dc.norm();
        FloatExp bout = new FloatExp(bailout, 0);
        FloatExp zero = new FloatExp(0, 0);

        while (iter < maxIter) {
            FloatExp dzNorm = dz.norm();
            BLALookupResult result = lookupFE(table, refIter, ref.size(), dzNorm, normDc);

            if (result.first != null) {
                dz.mulMut(result.first.A).addMut(dc.mul(result.first.B));
                iter += result.second;
                refIter += result.second;
            } else {
                FloatExpComplex Z = ref.get(refIter);
                FloatExpComplex dz2 = dz.square();
                dz.addMut(dz).mulMut(Z).addMut(dz2).addMut(dc);
                iter++;
                refIter++;
            }
            if (refIter >= ref.size()) return new PTBLAFEResult(iter, true, null, refIter);
            if (dz.getRe().scale() > -75 && dz.getIm().scale() > -75) {
                return new PTBLAFEResult(iter, false, dz.toComplex(), refIter);
            }

            FloatExpComplex Z2 = ref.get(refIter);
            FloatExpComplex val = Z2.add(dz);
            FloatExp valAbs = val.abs2();
            if (valAbs.compareTo(bout) > 0) {
                double fracIter = Math.log(valAbs.doubleValue()) / 2;
                fracIter = Math.log(fracIter / Math.log(2)) / Math.log(2);
                iter += 1 - fracIter;
                return new PTBLAFEResult(iter, true, null, refIter);
            }
            if (valAbs.sub(dzNorm).compareTo(zero) < 0 || refIter == ref.size() - 1) {
                dz = val;
                refIter = 0;
            }
        }
        return new PTBLAFEResult(iter, true, null, refIter);
    }

    static class PTBLAFEResult {
        double it;
        boolean e;
        Complex z;
        int ref;

        PTBLAFEResult(double it, boolean e, Complex z, int ref) {
            this.it = it;
            this.e = e;
            this.z = z;
            this.ref = ref;
        }
    }

    public Complex getDelta(double x, double y, int w, int h) {
        int min = Math.min(w, h);
        double baseStep = 1d / min;
        double scale = this.scale.doubleValue();
        double deltaX = (x - w / 2.0) * baseStep;
        double deltaY = (h / 2.0 - y) * baseStep;
        return new Complex(scale * deltaX, scale * deltaY);
    }

    public FloatExpComplex getDeltaFE(double x, double y, int w, int h) {
        int min = Math.min(w, h);
        double baseStep = 1d / min;
        double deltaX = (x - w / 2.0) * baseStep;
        double deltaY = (h / 2.0 - y) * baseStep;
        if (deltaX ==0) deltaX += 0.0000000001;
        if (deltaY==0) deltaY +=0.0000000001;
        return new FloatExpComplex(scale.mul(deltaX), scale.mul(deltaY));
    }

    private double getIter(int x, int y, int width, int height, boolean isDeep) {
        if (!isDeep) {
            Complex delta = getDelta(x, y, width, height);
            return getPTBLA(delta, refComplex, tableComplex, 0, 0, 0, 0);
        } else {
            FloatExpComplex delta = getDeltaFE(x, y, width, height);
            PTBLAFEResult result = getPTBLAFE(delta, ref, table);
            double iterations;
            if (result.e) iterations = result.it;
            else {
                iterations = getPTBLA(delta.toComplex(), refComplex, tableComplex, result.z.getRe(), result.z.getIm(), result.it, result.ref);
            }
            return iterations;
        }
    }

    public void render(IterationMap map) {
        if(!refVaild) {
            System.out.println("Reference");
            getReferenceOrbit();
            refComplex = ref.stream().map(FloatExpComplex::toComplex).toList();
            refVaild = true;
        }

        tableComplex = createBLATable(refComplex, this.scale.doubleValue());

        int width = map.getWidth();
        int height = map.getHeight();

        boolean isDeep = this.scale.scale() < -300;
        if (isDeep) {
            table = createBLATableFE(ref, this.scale);
        }

        List<Future<?>> futures = new ArrayList<>();

        for (int y = 0; y < height; y += 2) {
            int finalY = y;
            futures.add(service.submit(()->{
                for (int x = 0; x < width; x += 2) {
                    map.setPixel(x, finalY, getIter(x, finalY, width, height, isDeep));
                }
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        futures.clear();

        for (int y = 0; y < height; y += 2) {
            int finalY = y;
            futures.add(service.submit(()->{
            for (int x = 1; x < width; x += 2) {
                if (finalY < height - 1 && x < width - 1) {
                    double left = map.getPixel(x - 1, finalY);
                    double right = map.getPixel(x + 1, finalY);
                    if (Math.floor(left) == Math.floor(right)) {
                        map.setPixel(x, finalY, (left + right) / 2);
                        continue;
                    }
                }
                map.setPixel(x, finalY, getIter(x, finalY, width, height, isDeep));
            }
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        futures.clear();

        for (int y = 1; y < height; y += 2) {
            int finalY = y;
            futures.add(service.submit(()->{
            for (int x = 0; x < width; x++) {
                if (finalY < height - 1) {
                    double up = map.getPixel(x, finalY - 1);
                    double down = map.getPixel(x, finalY + 1);
                    if (Math.floor(up) == Math.floor(down)) {
                        map.setPixel(x, finalY, (up + down) / 2);
                        continue;
                    }
                }
                map.setPixel(x, finalY, getIter(x, finalY, width, height, isDeep));
            }
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        futures.clear();

    }

    public void zoomOut(){
        this.scale.mulMut(new FloatExp(2));
    }

    public FloatExp getScale() {
        return scale;
    }

    public List<FloatExpComplex> getRef() {
        if (!refVaild) getReferenceOrbit();
        return ref;
    }

    protected void setRef(List<FloatExpComplex> ref) {
        this.ref = ref;
        refComplex = ref.stream().map(FloatExpComplex::toComplex).toList();
        refVaild = true;
    }
}
