package hywt.mandel.colors;

/**
 * A palette that filters another palette
 */
public abstract class FilterPalette implements Palette{
    protected Palette in;

    public FilterPalette(Palette in) {
        this.in = in;
    }
}
