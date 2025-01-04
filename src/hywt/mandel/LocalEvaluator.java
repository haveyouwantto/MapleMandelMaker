package hywt.mandel;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import hywt.mandel.colors.BasicEscapeColorizer;
import hywt.mandel.colors.Colorizer;
import hywt.mandel.colors.InfinitePalette;
import hywt.mandel.numtype.FloatExp;

public class LocalEvaluator {
    private Configuration config;
    private List<File> impFiles;

    public LocalEvaluator(Configuration config) {
        this.config = config;
        impFiles = new ArrayList<>();

        double startValue = config.getParameter().getScale().log2Value();
        double finishScale = new FloatExp(16).log2Value();
        
        for (int i = config.getStart(); i < finishScale - startValue; i++) {
            File file = config.createFile(String.format("%08d.imp", i));
            impFiles.add(file);
        }
    }

    public void evaluate(){
        System.out.println("LocalEvaluator started");
        for (File file : impFiles) {
            if (file.exists()) {
                try (InputStream in = new GZIPInputStream(new FileInputStream(file))) {
                    System.out.print("evaulate "+file+"\r");
                    IterationMap.read(in);
                } catch (IOException e) {
                    System.out.println("corrupted imp file: "+file);
                    file.delete();
                }
            } 
        }
    }

    public void colorize() {
        System.out.println("Colorizer started");

        Colorizer colorizer = new BasicEscapeColorizer(new InfinitePalette(3));
        int width = config.getWidth();
        int height = config.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        int i = 0;
        for (File file : impFiles) {
            if (file.exists()) {
                try (InputStream in = new GZIPInputStream(new FileInputStream(file))) {
                    IterationMap map = IterationMap.read(in);
                    File pngFile = config.createFile(
                        String.format("%08d.png", i)
                    );
                    System.out.println("Writing " + pngFile.getName());
                    colorizer.paint(map, image);
                    ImageIO.write(image, "png", pngFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } 
            i++;
        }
    }

    public void exportKFB() {
        System.out.println("KFBConverter started");
        
        int i = 0;
        for (File file : impFiles) {
            if (file.exists()) {
                try (InputStream in = new GZIPInputStream(new FileInputStream(file))) {
                    IterationMap map = IterationMap.read(in);
                    FloatExp scale = new FloatExp(4).div(config.getParameter().getScale().mul(FloatExp.fromLog2(i)));
                    File kfbFile = config.createFile(
                        String.format("%05d_%.2fe%03d.kfb", i, scale.getBase(), scale.getExp())
                    );
                    System.out.println("Writing " + kfbFile.getName());
                    OutputStream out = new FileOutputStream(kfbFile);
                    map.writeKFB(out);
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } 
            i++;
        }
    }
}
