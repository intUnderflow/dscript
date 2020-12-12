package site.intunderflow.dscript.utility;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

import java.io.File;
import java.io.IOException;

public class FreemarkerConfiguration {

    public static Configuration getConfiguration(Object objectFor){
        try{
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
            configuration.setDirectoryForTemplateLoading(
                    new File(objectFor.getClass().getResource("").getFile()
                            .replace("production/classes", "production/resources")
                            .replace("production\\classes", "production\\resources"))
            );
            configuration.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
            configuration.setDefaultEncoding("UTF-8");
            return configuration;
        }
        catch(IOException e){
            throw new Error(e);
        }
    }

}
