package hywt.mandel;

import hywt.mandel.numtype.DeepComplex;
import hywt.mandel.numtype.FloatExp;
import org.apfloat.Apcomplex;
import org.apfloat.Apfloat;

import java.io.*;
import java.math.BigDecimal;
import java.util.Properties;

public class Configuration implements Serializable {
    private Parameter parameter;
    private File outputDir;
    private int width;
    private int height;
    private int start;
    private boolean savePreview;

    public final static long serialVersionUID = 1L;

    public Configuration(Parameter parameter, File outputDir) {
        this.parameter = parameter;
        this.outputDir = outputDir;
    }

    public static Configuration load(InputStream is) throws IOException {
        Properties prop = new Properties();
        prop.load(is);

        BigDecimal re = new BigDecimal(prop.getProperty("real"));
        BigDecimal im = new BigDecimal(prop.getProperty("imaginary"));
        FloatExp scale = new FloatExp(4).div(FloatExp.parseFloatExp(prop.getProperty("zoom")));
        long maxIter = Long.parseLong(prop.getProperty("iterations"));
        double bailout = Double.parseDouble(prop.getProperty("bailout", "2"));
        Parameter p = new Parameter(new Apcomplex(new Apfloat(re), new Apfloat(im)), scale, maxIter, bailout);
        Configuration c = new Configuration(p, new File(prop.getProperty("path")));
        c.width = Integer.parseInt(prop.getProperty("width", "1920"));
        c.height = Integer.parseInt(prop.getProperty("height", "1080"));
        c.start = Integer.parseInt(prop.getProperty("start", "0"));
        c.savePreview = Boolean.parseBoolean(prop.getProperty("savePreview", "true"));
        return c;
    }

    public File createFile(String name){
        return new File(outputDir, name);
    }

    public Parameter getParameter() {
        return parameter;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public void save(OutputStream os) throws IOException {
        Properties prop = new Properties();
        prop.setProperty("real", parameter.getCenter().real().toString());
        prop.setProperty("imaginary", parameter.getCenter().imag().toString());
        prop.setProperty("zoom", new FloatExp(4).div(parameter.getScale()).toString());
        prop.setProperty("iterations", String.valueOf(parameter.getMaxIter()));
        prop.setProperty("path", outputDir.toString());
        prop.store(os, "MapleMandelMaker Config");
        os.close();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getStart() {
        return start;
    }

    public boolean isSavePreview() {
        return savePreview;
    }
}
