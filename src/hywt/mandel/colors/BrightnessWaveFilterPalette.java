package hywt.mandel.colors;

public class BrightnessWaveFilterPalette extends FilterPalette {

    private final float period = 600;
    private final float depth = 0.5f;

    public BrightnessWaveFilterPalette(Palette in) {
        super(in);
    }

    @Override
    public Color get(int index) {
        float brightness = Math.abs(
                (index % period) - (period / 2)
        ) / period * 2 * (1 - depth) + depth;
        return in.get(index).brightness(brightness);
    }
}
