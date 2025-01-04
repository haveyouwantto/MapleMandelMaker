package hywt.mandel.colors;

public class ColorInterpolator {

    private static Color colorTrans(Palette palette, double it) {
        double percent = it - Math.floor(it);
        int idx = (int) Math.floor(it);
        Color c1 = palette.get(idx);
        Color c2 = palette.get(idx + 1);
        double r = (1 - percent) * c1.r + percent * c2.r;
        double g = (1 - percent) * c1.g + percent * c2.g;
        double b = (1 - percent) * c1.b + percent * c2.b;
        // double g = (1 - percent) * palette[c1][1] + (percent) * palette[c2][1];
        // double b = (1 - percent) * palette[c1][2] + (percent) * palette[c2][2];
        return new Color((int)r, (int)g, (int)b);
    }

    public static Color getColor(Palette palette, double it, long maxIter, double step) {
        if (it == -1 || it >= maxIter) {
            return Color.BLACK;
        } else if (it <= 0) return palette.get(0);
        return colorTrans(palette, it / step);
    }
}
