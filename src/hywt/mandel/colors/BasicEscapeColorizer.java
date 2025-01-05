package hywt.mandel.colors;

import hywt.mandel.IterationMap;

import java.awt.image.BufferedImage;

/**
 * A colorizer that paints an image based on an iteration map using escape time coloring
 */
public class BasicEscapeColorizer implements Colorizer {
    private Palette palette;
    private double step;
    private boolean smooth;

    public BasicEscapeColorizer() {
        this(new DefaultPalette(), 6, true);
    }

    public BasicEscapeColorizer(Palette palette) {
        this(palette, 6, true);
    }

    public BasicEscapeColorizer(Palette palette, double step, boolean smooth) {
        this.palette = palette;
        this.step = step;
        this.smooth = smooth;
    }

    public void paint(IterationMap iterMap, BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                double it = iterMap.getPixel(x, y);
                image.setRGB(x, y, ColorInterpolator.getColor(palette, smooth ? it : Math.floor(it), iterMap.getMaxIter(), step).getRGB());
            }
        }
    }
}
