import hywt.mandel.*;
import hywt.mandel.numtype.DeepComplex;
import hywt.mandel.numtype.FloatExp;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length ==0){
            RenderManager manager = RenderManager.load(new FileInputStream("config.prop"));
            manager.start();
        } else if (args.length>=1) {
            String mode = args[1];
        }

    }
}