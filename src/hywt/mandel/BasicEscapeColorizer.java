package hywt.mandel;

import java.awt.image.BufferedImage;

public class BasicEscapeColorizer implements Colorizer {
    private int[][] palette;

    public BasicEscapeColorizer() {
        palette = Palette.palette;
    }

    public void paint(IterationMap iterMap, BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, Palette.getColor(palette, iterMap.getPixel(x, y)).getRGB());
            }
        }
    }
}
