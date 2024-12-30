package hywt.mandel;

import java.io.*;
import java.util.Arrays;

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

        VLELongOutputStream vle = new VLELongOutputStream(new BufferedOutputStream(dos));

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                double itF = getPixel(x,y);
                long it = (long) itF;
                double phase = itF - it;
                int phaseQuant = (int) (phase * 255);
                vle.writeLong(it);
                vle.write(phaseQuant);
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

        VLELongInputStream vle = new VLELongInputStream(new BufferedInputStream(dis));

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                long it = vle.readLong();
                double phase = vle.read() / 255.0;
                iterationMap.setPixel(x, y, it + phase);
            }
        }

        return iterationMap;
    }

    @Override
    public String toString() {
        return "IterationMap{" + getWidth() +"x"+getHeight()+
                ", maxIter=" + maxIter +
                '}';
    }
}
