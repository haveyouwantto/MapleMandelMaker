package hywt.mandel.colors;

import java.awt.image.BufferedImage;

import hywt.mandel.IterationMap;

/**
 * A colorizer that colors pixels based on the difference between the pixel's
 * iteration and the iterations of its neighbors.
 */
public class DifferentialColorizer implements Colorizer {
    private Palette palette;
    private boolean blendOriginal;

    public DifferentialColorizer() {
        this(new DefaultPalette());
    }

    public DifferentialColorizer(Palette palette) {
        this.palette = palette;
    }

    public void setBlendOriginal(boolean blendOriginal) {
        this.blendOriginal = blendOriginal;
    }

    @Override
    public void paint(IterationMap map, BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                double iter = getPixel(map, x, y);
                double iter1 = getPixel(map, x + 1, y);
                double iter2 = getPixel(map, x, y + 1);
                double iter3 = getPixel(map, x + 1, y + 1);
                double diff = Math.abs(iter - iter1) + Math.abs(iter - iter2) + Math.abs(iter - iter3);
                double logDiff = Math.log(diff + 1);
                if (blendOriginal) {
                    logDiff += Math.log(iter);
                }
                Color color = ColorInterpolator.getColor(palette, logDiff, map.getMaxIter(), 1);
                image.setRGB(x, y, color.getRGB());
            }
        }
    }

    private double getPixel(IterationMap map, int x, int y) {
        int width = map.getWidth();
        int height = map.getHeight();

        // if the requested pixel in inside the map
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return map.getPixel(x, y);
        } else {
            // use edge pixels for out of bounds pixels
            if (x < 0)
                x = 0;
            if (x >= width)
                x = width - 1;
            if (y < 0)
                y = 0;
            if (y >= height)
                y = height - 1;
            return map.getPixel(x, y);
        }
    }

}
