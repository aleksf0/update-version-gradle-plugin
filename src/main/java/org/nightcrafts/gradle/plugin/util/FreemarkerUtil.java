package org.nightcrafts.gradle.plugin.util;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public class FreemarkerUtil {

    private static FreemarkerUtil INSTANCE;

    private Configuration configuration;

    private FreemarkerUtil() {
        configuration = new Configuration(new freemarker.template.Version(Configuration.VERSION_2_3_32.toString()));
        configuration.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_32).build());
    }

    public static FreemarkerUtil getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new FreemarkerUtil();
        }

        return INSTANCE;
    }

    public String process(String template, Object model) {
        try {
            Template freemarkerTemplate = new Template("dummy", new StringReader(template), configuration);
            Writer writer = new StringWriter();
            freemarkerTemplate.process(model, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }
}
