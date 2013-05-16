package net.time4tea.jni;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

public class Templates {

    private Configuration freeMarker;

    public Templates(final Class<?> ownerClass) {
        this(ownerClass,"");
    }

    public Templates(final Class<?> ownerClass, final String templateLocation) {
        freeMarker = new Configuration() {{
            setDefaultEncoding("UTF-8");
            setEncoding(Locale.UK, "UTF-8");
            setURLEscapingCharset("UTF-8");
            setTemplateLoader(new ClassTemplateLoader(ownerClass, templateLocation));
        }};
    }

    public Template template(String templateName) throws IOException {
        return freeMarker.getTemplate(templateName);
    }

    public String process(String templateName, Map<String, Object> model) throws TemplateProcessingException {
        try {
            StringWriter out = new StringWriter();
            template(templateName).process(model, out);
            return out.toString();
        } catch (IOException e) {
            throw new TemplateProcessingException("IO Problem processing template",e);
        } catch (TemplateException e) {
            throw new TemplateProcessingException("Template error", e);
        }
    }

    public void process(String templateName, Map<String, Object> model, Writer writer) throws TemplateProcessingException {
        try {
            template(templateName).process(model, writer);
        } catch (IOException e) {
            throw new TemplateProcessingException("IO Problem processing template",e);
        } catch (TemplateException e) {
            throw new TemplateProcessingException("Template error", e);
        }
    }

    public class TemplateProcessingException extends Exception {

        public TemplateProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
