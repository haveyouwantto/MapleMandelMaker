package hywt.mandel;

import java.io.*;
import java.nio.ByteBuffer;
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

        long prev = 0;

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                double itF = getPixel(x, y);

                long it = (long) itF;
                long diff = it - prev;
                prev = it;

                double phase = itF - it;
                int phaseQuant = (int) (phase * 255);
                vle.writeLong(diff);
                vle.write(phaseQuant);
            }
        }

        vle.flush();
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

        long prev = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                long it = vle.readLong();
                double phase = vle.read() / 255.0;

                prev += it;
                iterationMap.setPixel(x, y, prev + phase);
            }
        }

        return iterationMap;
    }

    public void writeKFB(OutputStream o) throws IOException {
        OutputStream out = new BufferedOutputStream(o);
        out.write("KFB".getBytes()); // Magic

        ByteBuffer buf = ByteBuffer.allocate(4);

        buf.order(java.nio.ByteOrder.LITTLE_ENDIAN);

        int width = getWidth();
        int height = getHeight();

        // write width
        buf.putInt(width);
        out.write(buf.array());

        // write height
        buf.clear();
        buf.putInt(height);
        out.write(buf.array());

        // write iteration
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double itF = getPixel(x, y);
                int it = (int) itF;

                buf.clear();
                buf.putInt(it);
                out.write(buf.array());
            }
        }

        out.write(new byte[] { 1, 0, 0, 0 });

        // write palette data (dummy)

        int colors = 3;
        buf.clear();
        buf.putInt(colors);
        out.write(buf.array());

        // write color
        out.write(new byte[] { 0, 0, 0,
                (byte) 0xFF, 0, 0,
                0, (byte) 0xFF, 0 });

        // write max iterations
        buf.clear();
        buf.putInt((int) getMaxIter());
        out.write(buf.array());

        // write phase
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double itF = getPixel(x, y);
                int it = (int) itF;
                double phase = itF - it;
                buf.clear();
                buf.putFloat((float) phase);
                out.write(buf.array());
            }
        }
    }

    @Override
    public String toString() {
        return "IterationMap{" + getWidth() + "x" + getHeight() +
                ", maxIter=" + maxIter +
                '}';
    }
}
