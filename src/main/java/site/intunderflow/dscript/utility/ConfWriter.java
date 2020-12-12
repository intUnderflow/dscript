package site.intunderflow.dscript.utility;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfWriter {

    private static final Logger logger = Logger.getLogger(ConfWriter.class.getName());

    public static void writeConfigurationFile(File file, Provider<Dual<String, String>> lineProvider){
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            boolean firstWrite = true;
            while (!lineProvider.exhausted()) {
                if (!firstWrite) {
                    bufferedWriter.write("\r\n");
                } else {
                    firstWrite = false;
                }
                Dual<String, String> line = lineProvider.provide();
                bufferedWriter.write(line.getA() + "=" + line.getB());
            }
            bufferedWriter.close();
        }
        catch(Exception e){
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

}
