package it.cnr.cool.web.scripts.processor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.processor.FTLTemplateProcessor;

import freemarker.cache.MruCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class LocaleFTLTemplateProcessor extends FTLTemplateProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocaleFTLTemplateProcessor.class);

	/** FreeMarker config for templates */
    private Configuration localeTemplateConfig;
    /** Time in seconds between FreeMarker checking for new template instances */
    private int updateDelay = 0;

    /** Size of the FreeMarker in-memory template cache */
    private int cacheSize = 256;

	@Override
    public void reset() {
    	super.reset();
        if (localeTemplateConfig != null)
        {
            localeTemplateConfig.clearTemplateCache();
        }
    }
    /* (non-Javadoc)
     * @see org.alfresco.web.scripts.TemplateProcessor#hasTemplate(java.lang.String)
     */
    public boolean hasLocaleTemplate(String templatePath)
    {
        boolean hasTemplate = false;
        try
        {
            Template template = localeTemplateConfig.getTemplate(templatePath);
            hasTemplate = template != null;
        }
        catch(FileNotFoundException e)
        {
            // NOTE: return false as template is not found
			LOGGER.error("template " + templatePath + " not found", e);
        }
        catch(IOException e)
        {
            throw new WebScriptException("Failed to retrieve template " + templatePath, e);
        }
        return hasTemplate;
    }

    @Override
    protected void initConfig() {
    	super.initConfig();
        // construct template config
        Configuration config = new Configuration();
        config.setCacheStorage(new MruCacheStorage(cacheSize, cacheSize << 1));
        config.setTemplateUpdateDelay(updateDelay);
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        config.setLocalizedLookup(true);
        config.setOutputEncoding("UTF-8");
        if (getDefaultEncoding() != null)
        {
            config.setDefaultEncoding(getDefaultEncoding());
        }

        if (getTemplateLoader() != null)
        {
            config.setTemplateLoader(getTemplateLoader());
        }
        localeTemplateConfig = config;
    }

    public void processLocale(String template, Object model, Writer out)
    {
        if (template == null || template.length() == 0)
        {
            throw new IllegalArgumentException("Template name is mandatory.");
        }
        if (model == null)
        {
            throw new IllegalArgumentException("Model is mandatory.");
        }
        if (out == null)
        {
            throw new IllegalArgumentException("Output Writer is mandatory.");
        }

        try
        {
            long startTime = 0;
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Executing template: " + template);
                startTime = System.nanoTime();
            }

            addProcessorModelExtensions(model);

            Template t = localeTemplateConfig.getTemplate(template);
            if (t != null)
            {
                try
                {
                    // perform the template processing against supplied data model
                    t.process(model, out);
                }
                catch (Exception err)
                {
                    throw new WebScriptException("Failed to process template " + template, err);
                }
            }
            else
            {
                throw new WebScriptException("Cannot find template " + template);
            }

            if (LOGGER.isDebugEnabled())
            {
                long endTime = System.nanoTime();
                LOGGER.debug("Time to execute template: " + (endTime - startTime)/1000000f + "ms");
            }
        }
        catch (IOException ioerr)
        {
            throw new WebScriptException("Failed to process template " + template, ioerr);
        }
    }
}
