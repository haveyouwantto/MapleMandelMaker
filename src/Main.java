import hywt.mandel.*;

import java.io.*;
import java.net.InetAddress;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println(Arrays.toString(args));

        if (args.length == 0) { // local computation
            Configuration configuration = Configuration.load(new FileInputStream("config.prop"));
            RenderManager manager = new RenderManager(configuration);
            manager.start();
        } else {
            String mode = args[0];
            switch (mode) {
                case "server":
                    Configuration configuration = Configuration.load(new FileInputStream("config.prop"));
                    new RenderServer(configuration);
                case "client":
                    InetAddress address = InetAddress.getByName(args[1]);
                    int port = Integer.parseInt(args[2]);
                    int thread = -1;
                    if (args.length >= 4)
                        thread = Integer.parseInt(args[3]);
                    new RenderClient(address, port, thread).start();
                case "kfb": // kfb convert
                    Configuration cfg = Configuration.load(new FileInputStream("config.prop"));
                    new LocalEvaluator(cfg).exportKFB();
                    break;
                case "colorize": // colorize
                    Configuration cfg2 = Configuration.load(new FileInputStream("config.prop"));
                    new LocalEvaluator(cfg2).colorize();
                    break;
                case "evaluate":
                    Configuration cfg3 = Configuration.load(new FileInputStream("config.prop"));
                    new LocalEvaluator(cfg3).evaluate();
                    break;
                
            }
        }

    }
}