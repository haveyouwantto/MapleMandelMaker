package hywt.mandel;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * VLELongInputStream decodes long values encoded using variable-length encoding (1-9 bytes),
 * where the 7th bit of the first byte is used as the sign bit, from the underlying InputStream.
 */
public class VLELongInputStream extends FilterInputStream {

    public VLELongInputStream(InputStream in) {
        super(in);
    }

    /**
     * Reads and decodes a variable-length encoded long value from the stream.
     *
     * @return the decoded long value.
     * @throws IOException if an I/O error occurs or if the encoding is invalid.
     */
    public long readLong() throws IOException {
        int b = in.read();
        if (b == -1) {
            throw new IOException("Unexpected end of stream while decoding long.");
        }

        // Determine the sign from the 7th bit of the first byte
        boolean isNegative = (b & 0x40) != 0;
        long result = b & 0x3F; // Extract the lower 6 bits for the magnitude
        int shift = 6;

        // Read subsequent bytes
        while ((b & 0x80) != 0) { // Check continuation bit
            b = in.read();
            if (b == -1) {
                throw new IOException("Unexpected end of stream while decoding long.");
            }

            result |= (long) (b & 0x7F) << shift; // Extract the lower 7 bits and shift them into place
            shift += 7;

            if (shift > 63) {
                throw new IOException("Variable-length encoding is too long for a long value.");
            }
        }

        // Apply the sign if the value is negative
        return isNegative ? -result : result;
    }

    /**
     * Closes the stream, releasing any system resources associated with it.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        super.close();
    }
}
