package hywt.mandel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import hywt.mandel.numtype.FloatExp;

public class KFBConverter {
    private Configuration config;

    public KFBConverter(Configuration config) {
        this.config = config;
    }

    public void start() {
        System.out.println("KFBConverter started");

        double startValue = config.getParameter().getScale().log2Value();
        double finishScale = new FloatExp(16).log2Value();

        for (int i = config.getStart(); i < finishScale - startValue; i++) {
            File file = config.createFile(String.format("%08d.imp", i));
            if (file.exists()) {
                try {
                    IterationMap map = IterationMap.read(new GZIPInputStream(new FileInputStream(file)));
                    FloatExp scale = new FloatExp(4).div(config.getParameter().getScale().mul(FloatExp.fromLog2(i)));
                    File kfbFile = config.createFile(
                        String.format("%05d_%.2fe%03d.kfb", i, scale.getBase(), scale.getExp())
                    );
                    System.out.println("Writing " + kfbFile.getName());
                    map.writeKFB(new FileOutputStream(kfbFile));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } 
        }
    }
}
