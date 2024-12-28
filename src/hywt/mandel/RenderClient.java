package hywt.mandel;

import hywt.mandel.numtype.FloatExpComplex;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class RenderClient {
    private Socket socket;
    public RenderClient(InetAddress address, int port) {
        try {
            socket = new Socket(address, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            System.out.println("Connected to server");
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            System.out.println("Waiting for configuration");
            Configuration config = (Configuration) ois.readObject();

            Mandelbrot mandelbrot = new Mandelbrot(config.getParameter());

            System.out.println("Reading reference data");
            List<FloatExpComplex> ref = RenderManager.readRef(ois);
            mandelbrot.setRef(ref);

            IterationMap iterationMap = new IterationMap(1920, 1080);

            while (true) {
                System.out.println("Waiting for frame number");
                int frameNumber = ois.readInt();

                mandelbrot.setZoomOrd(frameNumber);
                System.out.println("Rendering frame " + frameNumber + " with scale " + mandelbrot.getScale());
                mandelbrot.render(iterationMap);
                iterationMap.write(oos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
