package hywt.mandel;

import java.io.FilterOutputStream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * VLELongOutputStream encodes long values into a variable-length encoding (1-9 bytes)
 * and writes them to the underlying OutputStream.
 */
public class VLELongOutputStream extends FilterOutputStream {

    public VLELongOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Encodes a long value using variable-length encoding and writes it to the stream.
     *
     * @param value the long value to encode and write.
     * @throws IOException if an I/O error occurs.
     */
    public void writeLong(long value) throws IOException {
        while ((value & ~0x7FL) != 0) {
            out.write((int) ((value & 0x7F) | 0x80)); // Write 7 bits and set the continuation bit
            value >>>= 7; // Shift value to process the next 7 bits
        }
        out.write((int) value); // Write the final 7 bits without the continuation bit
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
