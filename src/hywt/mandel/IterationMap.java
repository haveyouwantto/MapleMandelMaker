package hywt.mandel;

public class IterationMap {
    private final double[][] map;
    private long maxIter;

    public IterationMap(int width, int height) {
        map = new double[height][width];
    }

    public void setPixel(int x, int y, double value) {
        map[y][x] = value;
    }

    public double getPixel(int x, int y) {
        return map[y][x];
    }

    public int getWidth() {
        return map[0].length;
    }

    public int getHeight() {
        return map.length;
    }

    public long getMaxIter() {
        return maxIter;
    }

    public void setMaxIter(long maxIter) {
        this.maxIter = maxIter;
    }
}
