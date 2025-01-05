package hywt.mandel.colors;

import java.util.Random;

public class BWColorPalette extends RandomPalette {

    public BWColorPalette(long seed) {
        super(seed);
    }

    @Override
    public Color get(int index) {
        int ord = index % 4;
        Random r = new Random(seed + index);
        r.nextFloat();
        float h = r.nextFloat();
        switch (ord) {
            case 0:
            case 2:
                float s = r.nextFloat() * 0.2f + 0.8f;
                float v = r.nextFloat() * 0.2f + 0.8f;
                return Color.fromHSV(h, s, v);
            case 1:
                float s1 = r.nextFloat() * 0.2f + 0.8f;
                float v1 = r.nextFloat() * 0.2f;
                return Color.fromHSV(h, s1, v1);
            default:
                float s2 = r.nextFloat() * 0.2f;
                float v2 = r.nextFloat() * 0.8f + 0.2f;
                return Color.fromHSV(h, s2, v2);
        }
    }
}
