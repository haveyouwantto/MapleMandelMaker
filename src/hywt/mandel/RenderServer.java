package hywt.mandel;

import hywt.mandel.numtype.FloatExp;
import hywt.mandel.numtype.FloatExpComplex;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;
import javax.xml.crypto.Data;

public class RenderServer {
    private Mandelbrot mandelbrot;
    private Configuration configuration;
    private Map<Integer, TaskState> state;
    private ServerSocket serverSocket;
    private Colorizer colorizer;

    public RenderServer(Configuration config) throws IOException {
        this.configuration = config;
        mandelbrot = new Mandelbrot(config.getParameter());
        colorizer = new BasicEscapeColorizer();

        state = Collections.synchronizedMap(new TreeMap<>());

        double startValue = config.getParameter().getScale().log2Value();
        double finishScale = new FloatExp(16).log2Value();

        for (int i = 0; i < finishScale - startValue; i++) {
            File file = config.createFile(String.format("%08d.png", i));
            state.put(i, file.exists() ? TaskState.COMPLETED : TaskState.PENDING);
        }

        File refFile = config.createFile("ref.dat");
        if (refFile.exists()) {
            List<FloatExpComplex> ref = RenderManager.readRef(new GZIPInputStream(new FileInputStream(refFile)));
            mandelbrot.setRef(ref);
            System.out.println("Loaded reference");
        } else {
            List<FloatExpComplex> ref = mandelbrot.getRef();
            OutputStream os = new GZIPOutputStream(new FileOutputStream(refFile));
            RenderManager.writeRef(ref, os);
            os.close();
        }

        serverSocket = new ServerSocket(47392);
        System.out.println("Server started on port " + serverSocket.getLocalPort());
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

    private void handleSocket(Socket socket) throws IOException {
        Map.Entry<Integer, TaskState> entry = null;
        try {
            System.out.println("Client " + socket.getInetAddress() + " connected");
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            // Send config to client
            System.out.println("Sending configuration");
            oos.writeObject(configuration);
            oos.flush();

            // Send reference orbit to client
            System.out.println("Sending reference data");
            RenderManager.writeRef(mandelbrot.getRef(), oos);
            oos.flush();

            BufferedImage image = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_BGR);
            while (true) {
                // Find the first pending task
                entry = state.entrySet().stream().filter(e -> e.getValue() == TaskState.PENDING).findFirst()
                        .orElse(null);
                if (entry != null) {
                    entry.setValue(TaskState.PROCESSING);

                    System.out.println("Sending frame number " + entry.getKey());
                    oos.writeInt(entry.getKey());
                    oos.flush();

                    IterationMap iterationMap = IterationMap.read(ois);
                    System.out.println("Received frame " + entry.getKey() + " from client " + socket.getInetAddress());

                    colorizer.paint(iterationMap, image);
                    ImageIO.write(image, "png", configuration.createFile(String.format("%08d.png", entry.getKey())));

                    entry.setValue(TaskState.COMPLETED);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // set state to pending
            entry.setValue(TaskState.PENDING);
        } finally {
            socket.close();
            System.out.println("Client " + socket.getInetAddress() + " disconnected");
        }
    }

    enum TaskState {
        PENDING, PROCESSING, COMPLETED
    }
}
