package hywt.mandel;

import hywt.mandel.numtype.FloatExpComplex;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class RenderClient {
    private InetAddress address;
    private int port;
    private int thread;

    public RenderClient(InetAddress address, int port, int thread) {
        this.address = address;
        this.port = port;
        this.thread = thread;
    }

    public RenderClient(InetAddress address, int port) {
        this(address, port, -1);
    }

    public void start() {
        while (true) {
            Socket socket = null;
            try {
                socket = new Socket(address, port);
                System.out.println("Connected to server");
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                System.out.println("Waiting for configuration");
                Configuration config = (Configuration) ois.readObject();

                Mandelbrot mandelbrot = new Mandelbrot(config.getParameter(), thread);

                System.out.println("Reading reference data");
                List<FloatExpComplex> ref = RenderManager.readRef(ois);
                mandelbrot.setRef(ref);

                IterationMap iterationMap = new IterationMap(config.getWidth(), config.getHeight());

                while (true) {
                    System.out.printf("Waiting for frame number\r");
                    int frameNumber = ois.readInt();

                    if (frameNumber == -1) {
                        System.out.println("No more frames to render");
                        return;
                    }
                    mandelbrot.setZoomOrd(frameNumber);
                    System.out.println("Rendering frame " + frameNumber + " with scale " + mandelbrot.getScale());
                    mandelbrot.render(iterationMap);
                    iterationMap.write(oos);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {
                    
                }
            }
            System.out.println("Reconnecting in 5 seconds");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
