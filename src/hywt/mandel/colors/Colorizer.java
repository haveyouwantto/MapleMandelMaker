package hywt.mandel.colors;

import hywt.mandel.IterationMap;

import java.awt.image.BufferedImage;

/**
 * A colorizer that paints an image based on an iteration map
 */
public interface Colorizer {
    void paint(IterationMap map, BufferedImage image);
}
