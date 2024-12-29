package hywt.mandel;

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

        IterationMap iterationMap = new IterationMap(1920, 1080);
        BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_BGR);

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

        int i = 0;
        while (mandelbrot.getScale().doubleValue() < 16) {
            File outFile = config.createFile(String.format("%08d.png", i));
            if (!outFile.exists()) {
                System.out.printf("Frame %d: %s\n", i, mandelbrot.getScale());
                mandelbrot.setZoomOrd(i);
                mandelbrot.render(iterationMap);
                colorizer.paint(iterationMap, image);
                ImageIO.write(image, "png", outFile);
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
