package hywt.mandel.colors;

import java.util.Random;

public class RandomPalette implements Palette {
    protected long seed;

    public RandomPalette(long seed) {
        this.seed = seed;
    }

    @Override
    public Color get(int index) {
        Random rand = new Random(seed + index);
        rand.nextFloat();

        return new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
    }
}