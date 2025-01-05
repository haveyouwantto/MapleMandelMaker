package hywt.mandel.colors;

import hywt.mandel.IterationMap;

import java.awt.image.BufferedImage;

/**
 * A colorizer that paints an image based on an iteration map using escape time coloring
 */
public class BasicEscapeColorizer implements Colorizer {
    private Palette palette;
    private double step;

    public BasicEscapeColorizer() {
        this(new DefaultPalette(), 6);
    }

    public BasicEscapeColorizer(Palette palette) {
        this(palette, 6);
    }

    public BasicEscapeColorizer(Palette palette, double step) {
        this.palette = palette;
        this.step = step;
    }

    public void paint(IterationMap iterMap, BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, ColorInterpolator.getColor(palette, iterMap.getPixel(x, y), iterMap.getMaxIter(), step).getRGB());
            }
        }
    }
}
