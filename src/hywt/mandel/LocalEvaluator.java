package hywt.mandel;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import hywt.mandel.colors.*;
import hywt.mandel.numtype.FloatExp;

public class LocalEvaluator {
    private Configuration config;
    private Map<Integer, File> impFiles;

    public LocalEvaluator(Configuration config) {
        this.config = config;
        impFiles = new HashMap<>();

        double startValue = config.getParameter().getScale().log2Value();
        double finishScale = new FloatExp(16).log2Value();

        for (int i = config.getStart(); i < finishScale - startValue; i++) {
            File file = config.createFile(String.format("%08d.imp", i));
            impFiles.put(i, file);
        }

    }

    /**
     * Evaluate all imp files
     */
    public void evaluate() {
        System.out.println("LocalEvaluator started");

        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();

        for (Map.Entry<Integer, File> fileEntry : impFiles.entrySet()) {
            File file = fileEntry.getValue();
            if (file.exists()) {
                futures.add(executor.submit(() -> {
                    try (InputStream in = new GZIPInputStream(new FileInputStream(file))) {
                        System.out.print("evaulate " + file + "\r");
                        IterationMap.read(in);
                    } catch (IOException e) {
                        System.out.println("corrupted imp file: " + file);
                        file.delete();
                    }
                }));
            }
        }

        executor.shutdown();
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Colorize all imp files
     */
    public void colorize() {
        System.out.println("Colorizer started");

        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();

        BasicEscapeColorizer colorizer = new BasicEscapeColorizer(new BWColorPalette(323L),32, false);
        int width = config.getWidth();
        int height = config.getHeight();

        for (Map.Entry<Integer, File> fileEntry : impFiles.entrySet()) {
            File file = fileEntry.getValue();
            if (file.exists()) {
                futures.add( executor.submit(() -> {
                    try (InputStream in = new GZIPInputStream(new FileInputStream(file))) {
                        IterationMap map = IterationMap.read(in);
                        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                        File pngFile = config.createFile(
                                String.format("%08d.png", fileEntry.getKey()));
                        System.out.println("Writing " + pngFile.getName());
                        colorizer.paint(map, image);
                        ImageIO.write(image, "png", pngFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
            }
        }
        executor.shutdown();
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Export all imp files to KFB
     */
    public void exportKFB() {
        System.out.println("KFBConverter started");

        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();

        for (Map.Entry<Integer, File> fileEntry : impFiles.entrySet()) {
            File file = fileEntry.getValue();
            if (file.exists()) {
                futures.add(executor.submit(() -> {
                    try (InputStream in = new GZIPInputStream(new FileInputStream(file))) {
                        IterationMap map = IterationMap.read(in);
                        FloatExp scale = new FloatExp(4)
                                .div(config.getParameter().getScale().mul(FloatExp.fromLog2(fileEntry.getKey())));
                        File kfbFile = config.createFile(
                                String.format("%05d_%.2fe%03d.kfb", fileEntry.getKey(), scale.getBase(),
                                        scale.getExp()));
                        System.out.println("Writing " + kfbFile.getName());
                        OutputStream out = new FileOutputStream(kfbFile);
                        map.writeKFB(out);
                        out.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }));
            }
        }
        executor.shutdown();
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
