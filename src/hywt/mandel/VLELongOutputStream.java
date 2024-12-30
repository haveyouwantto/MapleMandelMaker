package hywt.mandel;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * VLELongOutputStream encodes long values into a variable-length encoding (1-9 bytes),
 * using the 7th bit of the first byte as the sign bit, and writes them to the underlying OutputStream.
 */
public class VLELongOutputStream extends FilterOutputStream {

    public VLELongOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Encodes a long value using variable-length encoding with the 7th bit of the first byte
     * as the sign bit and writes it to the stream.
     *
     * @param value the long value to encode and write.
     * @throws IOException if an I/O error occurs.
     */
    public void writeLong(long value) throws IOException {
        boolean isNegative = value < 0;
        long magnitude = Math.abs(value);

        // Write the first byte with the sign bit
        int firstByte = (int) (magnitude & 0x3F); // Take the least significant 6 bits
        if (isNegative) {
            firstByte |= 0x40; // Set the 7th bit for negative values
        }
        if (magnitude >= 0x40) {
            firstByte |= 0x80; // Set the continuation bit if there are more bytes
        }
        out.write(firstByte);
        magnitude >>>= 6; // Shift by 6 bits for the next byte

        // Write subsequent bytes
        while (magnitude != 0) {
            int nextByte = (int) (magnitude & 0x7F);
            if ((magnitude >>> 7) != 0) {
                nextByte |= 0x80; // Set the continuation bit
            }
            out.write(nextByte);
            magnitude >>>= 7; // Shift by 7 bits for the next byte
        }
    }

    /**
     * Closes the stream, flushing any buffered data.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        super.close();
    }
}
