package site.intunderflow.dscript.utility;

import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TemplateMerger {

    public static String getForTemplate(Template template, Map<String, Object> data)
            throws IOException, TemplateException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream);
        template.process(data, writer);
        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }

}
