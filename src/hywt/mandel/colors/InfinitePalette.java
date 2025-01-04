package hywt.mandel.colors;

import java.util.Random;

public class InfinitePalette implements Palette {
    private long seed;

    public InfinitePalette(long seed) {
        this.seed = seed;
    }

    @Override
    public Color get(int index) {
        Random rand = new Random(seed + index);
        rand.nextFloat();
        float hue = rand.nextFloat();
        float saturation = rand.nextFloat() * 0.5f + 0.5f;
        float brightness = rand.nextFloat() * 0.5f + 0.5f;

        return Color.fromHSV(hue, saturation, brightness); 
    }
}