import hywt.mandel.*;

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

        if (args.length ==0){
            Configuration configuration = Configuration.load(new FileInputStream("config.prop"));
            RenderManager manager = new RenderManager(configuration);
            manager.start();
        } else if (args.length >= 1) {
            String mode = args[0];
            switch (mode) {
                case "s":
                    Configuration configuration = Configuration.load(new FileInputStream("config.prop"));
                    new RenderServer(configuration);
                    case "c":
                    InetAddress address = InetAddress.getByName(args[1]);
                    int port = Integer.parseInt(args[2]);
                    new RenderClient(address, port).start();
            }
        }

    }
}