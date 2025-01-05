package hywt.mandel.colors;

/**
 * A color class that represents a color in RGB format
 */
public class Color {
    public final float r;
    public final float g;
    public final float b;

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

    /**
     * Create a new color with the given RGB values
     * @param r The red value
     * @param g The green value
     * @param b The blue value
     */
    public Color(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /**
     * Create a new color with the given RGB values in the range 0-255
     * @param r The red value
     * @param g The green value
     * @param b The blue value
     */
    public Color(int r, int g, int b) {
        this.r = r / 255f;
        this.g = g / 255f;
        this.b = b / 255f;
    }

    /**
     * Create a new color with the given RGB value in the format 0xRRGGBB
     * @param rgb The RGB value
     */
    public Color(int rgb) {
        this(rgb >> 16 & 0xff,
                rgb >> 8 & 0xff,
                rgb | 0xff);
    }

    /**
     * Get the RGB value of this color
     * @return The RGB value
     */
    public int getRGB() {
        return  (getIntColor(r) << 16) | (getIntColor(g) << 8) | getIntColor(b);
    }

    /**
     * Add two colors together
     * @param other The color to add
     * @return The sum of the two colors
     */
    public Color add(Color other) {
        return new Color(this.r + other.r, this.g + other.g, this.b + other.b);
    }

    /**
     * Subtract two colors
     * @param other The color to subtract
     * @return The difference of the two colors
     */
    public Color sub(Color other) {
        return new Color(this.r - other.r, this.g - other.g, this.b - other.r);
    }

    /**
     * Add a value to the brightness of this color
     * @param val The value to add
     * @return The new color
     */
    public Color brightness(float val) {
        return new Color(this.r * val, this.g * val, this.b * val);
    }

    private float clamp(float value) {
        return Math.max(0, Math.min(1, value));
    }

    private int getIntColor(float value) {
        return (int) (clamp(value) * 255);
    }

    /**
     * Get the hue of this color
     * @return The hue
     */
    public float getHue() {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float h = 0;
        if (max == r) {
            h = (g - b) / (max - min);
        } else if (max == g) {
            h = 2 + (b - r) / (max - min);
        } else if (max == b) {
            h = 4 + (r - g) / (max - min);
        }
        h *= 60;
        if (h < 0) {
            h += 360;
        }
        return h;
    }

    /**
     * Get the saturation of this color
     * @return The saturation
     */
    public float getSaturation() {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        if (max == 0) {
            return 0;
        }
        return (max - min) / max;
    }

    /**
     * Get the brightness of this color
     * @return The brightness
     */
    public float getBrightness() {
        return Math.max(r, Math.max(g, b));
    }

    /**
     * Obtain a color from the given HSV values
     * @param h The hue
     * @param s The saturation
     * @param v The brightness
     * @return The HSV color
     */
    public static Color fromHSV(float h, float s, float v) {
        float r, g, b;
        if (s == 0) {
            r = g = b = v; // achromatic (grey)
        } else {
            // clamp hsv range
            h = h % 1;
            s = Math.max(0, Math.min(1, s));
            v = Math.max(0, Math.min(1, v));

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
        return new Color(r, g, b);
    }
}
