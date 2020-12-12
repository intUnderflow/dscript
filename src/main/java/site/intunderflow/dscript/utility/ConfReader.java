package site.intunderflow.dscript.utility;

import site.intunderflow.dscript.application.lddb.resource.Identifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfReader {

    private static final Logger logger = Logger.getLogger(ConfReader.class.getName());

    public static void readConfigurationFile(File file, DoubleConsumer<String, String> entryConsumer){
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                int split = currentLine.indexOf("=");
                String index = currentLine.substring(0, split);
                String value = currentLine.substring(split + 1);
                entryConsumer.accept(index, value);
            }
        }
        catch(Exception e){
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

}
