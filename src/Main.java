import hywt.mandel.*;
import hywt.mandel.numtype.FloatExp;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println(Arrays.toString(args));

        if (args.length == 0) {
            Configuration configuration = Configuration.load(new FileInputStream("config.prop"));
            RenderManager manager = new RenderManager(configuration);
            manager.start();
        } else {
            String mode = args[0];
            switch (mode) {
                case "s":
                    Configuration configuration = Configuration.load(new FileInputStream("config.prop"));
                    new RenderServer(configuration);
                case "c":
                    InetAddress address = InetAddress.getByName(args[1]);
                    int port = Integer.parseInt(args[2]);
                    int thread = -1;
                    if (args.length >= 4)
                        thread = Integer.parseInt(args[3]);
                    new RenderClient(address, port, thread).start();
                    case "k": // kfb convert
                    Configuration cfg = Configuration.load(new FileInputStream("config.prop"));
                    new KFBConverter(cfg).start();
            }
        }

    }
}