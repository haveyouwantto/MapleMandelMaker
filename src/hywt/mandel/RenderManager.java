package hywt.mandel;

import hywt.mandel.colors.BasicEscapeColorizer;
import hywt.mandel.colors.Colorizer;
import hywt.mandel.numtype.FloatExp;
import hywt.mandel.numtype.FloatExpComplex;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class RenderManager {
    private Mandelbrot mandelbrot;
    private Configuration config;

    public RenderManager(Configuration config) {
        mandelbrot = new Mandelbrot(config.getParameter());
        this.config = config;
    }

    public void start() throws IOException {
        Colorizer colorizer = new BasicEscapeColorizer();

        IterationMap iterationMap = new IterationMap(config.getWidth(), config.getHeight());
        BufferedImage image = new BufferedImage(config.getWidth(), config.getHeight(), BufferedImage.TYPE_INT_BGR);

        File refFile = config.createFile("ref.dat");
        if (refFile.exists()) {
            InputStream is = new GZIPInputStream(new FileInputStream(refFile));
            List<FloatExpComplex> ref = readRef(is);
            mandelbrot.setRef(ref);
            System.out.println("Loaded reference");
            is.close();
        } else {
            List<FloatExpComplex> ref = mandelbrot.getRef();
            OutputStream os =  new GZIPOutputStream(new FileOutputStream(refFile));
            writeRef(ref, os);
            os.close();
        }

        int i = config.getStart();
        while (mandelbrot.getScale().doubleValue() < 16) {
            File outFile = config.createFile(String.format("%08d.imp", i));
            if (!outFile.exists()) {
                mandelbrot.setZoomOrd(i);
                System.out.printf("Frame %d: %s\n", i, mandelbrot.getScale());
                long t = System.currentTimeMillis();
                mandelbrot.render(iterationMap);
                System.out.println((System.currentTimeMillis() - t) / 1000.0);
                OutputStream mapOut = new GZIPOutputStream(
                        new FileOutputStream(outFile)
                );
                iterationMap.write(mapOut);
                mapOut.close();
                if (config.isSavePreview()) {
                    colorizer.paint(iterationMap, image);
                    ImageIO.write(image, "jpg", config.createFile(String.format("%08d.jpg", i)));
                }
            }

            i++;

        }
    }

    public static void writeRef(List<FloatExpComplex> ref, OutputStream os) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeInt(ref.size());
        for (FloatExpComplex complex : ref) {
            dos.writeDouble(complex.getRe().getBase());
            dos.writeInt(complex.getRe().getExp());
            dos.writeDouble(complex.getIm().getBase());
            dos.writeInt(complex.getIm().getExp());
        }
        dos.flush();
    }

    public static List<FloatExpComplex> readRef(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        List<FloatExpComplex> ref = new ArrayList<>();
        int size = dis.readInt();
        for (int i = 0; i < size; i++) {
            double reMant = dis.readDouble();
            int reExp = dis.readInt();
            double imMant = dis.readDouble();
            int imExp = dis.readInt();
            ref.add(new FloatExpComplex(
                    new FloatExp(reMant, reExp),
                    new FloatExp(imMant, imExp)));
        }
        return ref;
    }

}
