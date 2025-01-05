package hywt.mandel.colors;

import java.awt.image.BufferedImage;

import hywt.mandel.IterationMap;

/**
 * A colorizer that colors pixels based on the difference between the pixel's
 * iteration and the iterations of its neighbors.
 */
public class DifferentialColorizer implements Colorizer {
    private Palette palette;

    public DifferentialColorizer() {
        this(new DefaultPalette());
    }

    public DifferentialColorizer(Palette palette) {
        this.palette = palette;
    }

    @Override
    public void paint(IterationMap map, BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                double iter = map.getPixel(x, y);
                double iter1 = map.getPixel(x + 1, y);
                double iter2 = map.getPixel(x, y + 1);
                double iter3 = map.getPixel(x + 1, y + 1);
                double diff = Math.abs(iter - iter1) + Math.abs(iter - iter2) + Math.abs(iter - iter3);
                Color color = ColorInterpolator.getColor(palette, diff, map.getMaxIter(), 6);
                image.setRGB(x, y, color.getRGB());
            }
        }
    }
    
}
