package hywt.mandel;

import hywt.mandel.numtype.FloatExp;
import hywt.mandel.numtype.FloatExpComplex;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
            List<FloatExpComplex> ref = readRef(new GZIPInputStream(new FileInputStream(refFile)));
            mandelbrot.setRef(ref);
        } else {
            List<FloatExpComplex> ref = mandelbrot.getRef();
            writeRef(ref,new GZIPOutputStream(new FileOutputStream(refFile)));
        }

        int i = 0;
        while (mandelbrot.getScale().doubleValue() < 16) {
            System.out.println(mandelbrot.getScale());
            File outFile = config.createFile(String.format("%08d.png", i++));
            if (!outFile.exists()) {
                mandelbrot.render(iterationMap);
                colorizer.paint(iterationMap, image);
                ImageIO.write(image, "png", outFile);
            }

            mandelbrot.zoomOut();
        }
    }

    public void writeRef(List<FloatExpComplex> ref, OutputStream os) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        for (FloatExpComplex complex : ref) {
            dos.writeDouble(complex.getRe().getBase());
            dos.writeInt(complex.getRe().getExp());
            dos.writeDouble(complex.getIm().getBase());
            dos.writeInt(complex.getIm().getExp());
        }
        os.close();
    }

    public List<FloatExpComplex> readRef(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        List<FloatExpComplex> ref = new ArrayList<>();
        while (dis.available() > 0) {
            double reMant = dis.readDouble();
            int reExp = dis.readInt();
            double imMant = dis.readDouble();
            int imExp = dis.readInt();
            ref.add(new FloatExpComplex(
                    new FloatExp(reMant, reExp),
                    new FloatExp(imMant, imExp)
            ));
        }
        is.close();
        return ref;
    }

    public void save() throws IOException {
        File file = config.createFile("config.prop");
        Properties prop = new Properties();
        prop.setProperty("re", config.getParameter().getCenter().getRe().toString());
        prop.setProperty("im", config.getParameter().getCenter().getIm().toString());
        prop.setProperty("scale", config.getParameter().getScale().toString());
        prop.setProperty("iterations", String.valueOf(config.getParameter().getMaxIter()));
        prop.setProperty("path", config.getOutputDir().toString());
        prop.store(new FileOutputStream(file), "");
    }
}