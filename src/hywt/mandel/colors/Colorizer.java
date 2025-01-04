package hywt.mandel.colors;

import hywt.mandel.IterationMap;

import java.awt.image.BufferedImage;

public interface Colorizer {
    void paint(IterationMap map, BufferedImage image);
}
