package hywt.mandel;

import hywt.mandel.numtype.DeepComplex;
import hywt.mandel.numtype.FloatExp;

import java.io.*;
import java.math.BigDecimal;
import java.util.Properties;

public class Configuration implements Serializable {
    private Parameter parameter;
    private File outputDir;

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
        Parameter p = new Parameter(new DeepComplex(re, im), scale, maxIter);
        return new Configuration(p, new File(prop.getProperty("path")));
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
        prop.setProperty("real", parameter.getCenter().getRe().toString());
        prop.setProperty("imaginary", parameter.getCenter().getIm().toString());
        prop.setProperty("zoom", new FloatExp(4).div(parameter.getScale()).toString());
        prop.setProperty("iterations", String.valueOf(parameter.getMaxIter()));
        prop.setProperty("path", outputDir.toString());
        prop.store(os, "MapleMandelMaker Config");
        os.close();
    }
}
