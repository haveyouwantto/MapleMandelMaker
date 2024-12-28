package hywt.mandel;

import java.io.*;

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


    // Write the object to an OutputStream
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeInt(getWidth());
        dos.writeInt(getHeight());
        dos.writeLong(maxIter);

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                dos.writeDouble(getPixel(x, y));
            }
        }

        dos.flush();
    }

    // Read the object from an InputStream
    public static IterationMap read(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);
        int width = dis.readInt();
        int height = dis.readInt();
        long maxIter = dis.readLong();

        IterationMap iterationMap = new IterationMap(width, height);
        iterationMap.maxIter = maxIter;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                iterationMap.setPixel(x, y, dis.readDouble());
            }
        }

        return iterationMap;
    }
}
