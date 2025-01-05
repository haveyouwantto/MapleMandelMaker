package hywt.mandel.colors;

import java.util.HashSet;
import java.util.Set;

/**
 * A palette that generates colors based on a set of waves
 */
public class InfiniteWavePalette implements Palette {
    private Set<WaveDef> waves;

    public InfiniteWavePalette() {
        this.waves = new HashSet<>();
    }

    public InfiniteWavePalette(WaveDef... waves) {
        this();
        for (WaveDef wave : waves) {
            this.waves.add(wave);
        }
    }

    private float getSine(double period, int index) {
        return (float) Math.sin(Math.PI * 2 * index / period) / 2 + 0.5f;
    }

    @Override
    public Color get(int index) {
        float h = 0.5f;
        float s = 0;
        float b = 0;

        int hn = 0, sn = 0, bn = 0;

        for (WaveDef wave : waves) {
            switch (wave.type) {
                case HUE:
                    h += getSine(wave.period, index);
                    hn++;
                    break;
                case SATURATION:
                    s += getSine(wave.period, index);
                    sn++;
                    break;
                case BRIGHTNESS:
                    b += getSine(wave.period, index);
                    bn++;
                    break;
                case COLOR:
                    h += wave.color.getHue();
                    s += wave.color.getSaturation();
                    b += wave.color.getBrightness();
                    break;
            }
        }

        if (hn != 0)
            h /= hn;
        else
            h = 0.5f;
        if (sn != 0)
            s /= sn;
        else
            s = 0.5f;
        if (bn != 0)
            b /= bn;
        else
            b = 0.5f;

        // System.out.println(h + " " + s + " " + b);
        return Color.fromHSV(h, s, b);
    }

    public static class WaveDef {
        Type type;
        double period;
        Color color;

        public WaveDef(Type type, double period, Color color) {
            this.type = type;
            this.period = period;

            if (type == Type.COLOR) {
                if (color == null) {
                    throw new IllegalArgumentException("Color must be specified for COLOR type");
                }
                this.color = color;
            }
        }

        public WaveDef(Type type, double period) {
            this(type, period, null);
        }
    }

    public static enum Type {
        HUE, SATURATION, BRIGHTNESS, COLOR
    }
}
