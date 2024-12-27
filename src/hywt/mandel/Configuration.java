package hywt.mandel;

import java.io.File;
import java.math.BigDecimal;

public class Configuration {
    private Parameter parameter;
    private File outputDir;

    public Configuration(Parameter parameter, File outputDir) {
        this.parameter = parameter;
        this.outputDir = outputDir;
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
}
