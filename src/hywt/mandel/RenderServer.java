package hywt.mandel;

import hywt.mandel.colors.BasicEscapeColorizer;
import hywt.mandel.colors.Colorizer;
import hywt.mandel.numtype.FloatExp;
import hywt.mandel.numtype.FloatExpComplex;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.logging.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

public class RenderServer {
    private static final Logger logger = Logger.getLogger(RenderServer.class.getName());
    private Mandelbrot mandelbrot;
    private Configuration configuration;
    private Map<Integer, TaskState> state;
    private ServerSocket serverSocket;
    private Colorizer colorizer;
    private Map<String, Integer> contributions;

    public RenderServer(Configuration config) throws IOException {
        // Logger setup (optional custom formatting)
        LogManager.getLogManager().reset();
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(new TightFormatter()); // Set the custom tight formatter
        logger.addHandler(consoleHandler);

        this.configuration = config;
        mandelbrot = new Mandelbrot(config.getParameter());
        colorizer = new BasicEscapeColorizer();

        state = Collections.synchronizedMap(new TreeMap<>());

        double startValue = config.getParameter().getScale().log2Value();
        double finishScale = new FloatExp(16).log2Value();

        int completed = 0;
        for (int i = config.getStart(); i < finishScale - startValue; i++) {
            File file = config.createFile(String.format("%08d.imp", i));
            if (file.exists()){
                state.put(i,TaskState.COMPLETED );
                completed++;
            }else{
                state.put(i,TaskState.PENDING );
            }
        }
        logger.info(String.format("Total frames = %d, rendered = %d", state.size(), completed));

        File refFile = config.createFile("ref.dat");
        if (refFile.exists()) {
            List<FloatExpComplex> ref = RenderManager.readRef(new GZIPInputStream(new FileInputStream(refFile)));
            mandelbrot.setRef(ref);
            logger.info("Loaded reference");
        } else {
            List<FloatExpComplex> ref = mandelbrot.getRef();
            OutputStream os = new GZIPOutputStream(new FileOutputStream(refFile));
            RenderManager.writeRef(ref, os);
            os.close();
        }

        serverSocket = new ServerSocket(47392);
        logger.info("Server started on port " + serverSocket.getLocalPort());

        
        contributions = new HashMap<>();
        loadData();

        // 添加关闭钩子，在程序终止时执行
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping server...");
            try {
                saveData();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }));

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(() -> {
                try {
                    handleSocket(socket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    private void saveData() throws IOException {
        File contribFile = configuration.createFile("contrib.txt");
        Properties prop = new Properties();
        for (Map.Entry<String, Integer> entry : contributions.entrySet()) {
            prop.setProperty(entry.getKey(), entry.getValue().toString());
        }
        try (FileOutputStream fos = new FileOutputStream(contribFile)){
            prop.store(fos, "contrib record");
        }
    }

    private void loadData() throws IOException {
        File contribFile = configuration.createFile("contrib.txt");
        if (!contribFile.exists()) {
            return;
        }
        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(contribFile)){
            prop.load(fis);
        }
        for (String key : prop.stringPropertyNames()) {
            contributions.put(key, Integer.parseInt(prop.getProperty(key)));
        }
    }

    // Log method that includes client IP in the log message
    private void log(String clientIp, String message) {
        System.out.println(contributions);
        logger.info("[" + clientIp + "] " + message);
    }

    private void handleSocket(Socket socket) throws IOException {
        Map.Entry<Integer, TaskState> entry = null;
        String clientIp = socket.getInetAddress().toString();

        try {
            log(clientIp, "Client connected");
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            // Send config to client
            log(clientIp, "Sending configuration");
            oos.writeObject(configuration);
            oos.flush();

            // Send reference orbit to client
            log(clientIp, "Sending reference data");
            RenderManager.writeRef(mandelbrot.getRef(), oos);
            oos.flush();

            BufferedImage image = new BufferedImage(configuration.getWidth(), configuration.getHeight(), BufferedImage.TYPE_INT_BGR);
            while (true) {
                // Find the first pending task
                entry = state.entrySet().stream()
                        .filter(e -> e.getValue() == TaskState.PENDING)
                        .findFirst()
                        .orElse(null);
                if (entry != null) {
                    entry.setValue(TaskState.PROCESSING);

                    log(clientIp, "Sending frame number " + entry.getKey());
                    oos.writeInt(entry.getKey());
                    oos.flush();

                    IterationMap iterationMap = IterationMap.read(ois);
                    contributions.putIfAbsent(clientIp, 0);
                    contributions.put(clientIp, contributions.get(clientIp) + 1);
                    Runtime.getRuntime().exec("clear");
                    log(clientIp, "Received frame " + entry.getKey());

                    OutputStream mapOut = new GZIPOutputStream(
                            new FileOutputStream(
                                    configuration.createFile(String.format("%08d.imp", entry.getKey()))
                            )
                    );
                    iterationMap.write(mapOut);
                    mapOut.close();

                    if (configuration.isSavePreview()) {
                        colorizer.paint(iterationMap, image);
                        ImageIO.write(image, "jpg", configuration.createFile(String.format("%08d.jpg", entry.getKey())));
                    }

                    entry.setValue(TaskState.COMPLETED);
                } else {
                    oos.writeInt(-1);
                }
            }
        } catch (IOException e) {
            // Set state to pending
            if (entry != null) {
                entry.setValue(TaskState.PENDING);
            }
            log(clientIp, "Error occurred: " + e.getMessage());
        } finally {
            socket.close();
            log(clientIp, "Client disconnected");
        }
    }

    enum TaskState {
        PENDING, PROCESSING, COMPLETED
    }
}