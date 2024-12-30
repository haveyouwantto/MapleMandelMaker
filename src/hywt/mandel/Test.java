package hywt.mandel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        VLELongOutputStream vlo = new VLELongOutputStream(new FileOutputStream("out.dat"));

        vlo.writeLong(942394);
        vlo.writeLong(43);
        vlo.writeLong(-94);
        vlo.writeLong(-3823);
        vlo.close();

        VLELongInputStream vli = new VLELongInputStream(new FileInputStream("out.dat"));
        System.out.println(vli.readLong());
        System.out.println(vli.readLong());
        System.out.println(vli.readLong());
        System.out.println(vli.readLong());
        vli.close();
    }
}
