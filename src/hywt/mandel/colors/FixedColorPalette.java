package hywt.mandel.colors;

import java.util.List;

public class FixedColorPalette implements Palette{
    private List<Color> colors;
    public FixedColorPalette(List<Color> colors) {
        this.colors = colors;
    }

    public FixedColorPalette(Color[] colors) {
        this.colors = List.of(colors);
    }

    @Override
    public Color get(int index) {
        return colors.get(index % colors.size());
    }
}
