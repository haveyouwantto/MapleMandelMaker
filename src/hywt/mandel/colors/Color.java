package hywt.mandel.colors;

public class Color {
    public final int r;
    public final int g;
    public final int b;

    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color BLUE = new Color(0, 0, 255);
    public static final Color GREEN = new Color(0, 255, 0);
    public static final Color AQUA = new Color(0, 255, 255);
    public static final Color RED = new Color(255, 0, 0);
    public static final Color MAGENTA = new Color(255, 0, 255);
    public static final Color YELLOW = new Color(255, 255, 0);
    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color GRAY = new Color(192, 192, 192);
    public static final Color LIGHT_GRAY = new Color(160, 160, 160);
    public static final Color DARK_GRAY = new Color(128, 128, 128);
    public static final Color LIGHT_BLUE = new Color(160, 192, 255);
    public static final Color LIGHT_GREEN = new Color(160, 255, 160);
    public static final Color LIGHT_AQUA = new Color(160, 255, 255);
    public static final Color LIGHT_RED = new Color(255, 160, 160);
    public static final Color LIGHT_MAGENTA = new Color(255, 160, 255);
    public static final Color LIGHT_YELLOW = new Color(255, 255, 160);

    public Color(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Color(int rgb) {
        this(
                rgb >> 16 & 0xff,
                rgb >> 8 & 0xff,
                rgb | 0xff);
    }

    public int getRGB() {
        return (r << 16) | (g << 8) | b;
    }

    public static Color fromHSV(float h, float s, float v) {
        float r, g, b;
        if (s == 0) {
            r = g = b = v; // achromatic (grey)
        } else {
            float h_ = h * 6f;
            float f = h_ - (int) h_;
            float p = v * (1f - s);
            float q = v * (1f - s * f);
            float t = v * (1f - s * (1f - f));

            int i = (int) h_;
            switch (i) {
                case 0:
                    r = v;
                    g = t;
                    b = p;
                    break;
                case 1:
                    r = q;
                    g = v;
                    b = p;
                    break;
                case 2:
                    r = p;
                    g = v;
                    b = t;
                    break;
                case 3:
                    r = p;
                    g = q;
                    b = v;
                    break;
                case 4:
                    r = t;
                    g = p;
                    b = v;
                    break;
                case 5:
                    r = v;
                    g = p;
                    b = q;
                    break;
                default:
                    r = g = b = 0; // this shouldn't happen
            }
        }
        return new Color((int) (r * 255), (int) (g * 255), (int) (b * 255));
    }
}
