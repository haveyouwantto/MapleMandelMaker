import hywt.mandel.*;
import hywt.mandel.numtype.DeepComplex;
import hywt.mandel.numtype.FloatExp;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) throws IOException {
        BigDecimal re = new BigDecimal("-0.10109629004872408548585126380133943559");
        BigDecimal im = new BigDecimal("0.9562865745329222746947099385459096648");
        FloatExp scale = FloatExp.fromDouble(4).div(FloatExp.fromDouble(6.169126e29));
        long iterations = 2048;

        Parameter p = new Parameter(new DeepComplex(re, im), scale, iterations);
        Mandelbrot mandelbrot = new Mandelbrot(p);
        Colorizer colorizer = new BasicEscapeColorizer();

        IterationMap iterationMap = new IterationMap(1920,1080);
        BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_BGR);

        mandelbrot.render(iterationMap);
        colorizer.paint(iterationMap, image);

        ImageIO.write(image, "png", new File("out.png"));

    }
}