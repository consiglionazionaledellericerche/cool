package it.cnr.mock;

import it.cnr.cool.service.I18nService;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class CnrMessageMethod implements TemplateMethodModelEx {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CnrMessageMethod.class);

	private final Locale locale;

	private final I18nService i18nService;

	public CnrMessageMethod(Locale locale, I18nService i18nService) {
		this.locale = locale;
		this.i18nService = i18nService;
	}

	@Override
	public Object exec(@SuppressWarnings("rawtypes") List arguments)
			throws TemplateModelException {
		LOGGER.debug(arguments.size() + " arguments: " + arguments.toString());
		String key = ((SimpleScalar) arguments.get(0)).getAsString();
		String label = i18nService.getLabel(key, locale);
		LOGGER.debug(key + ": " + label);
		return label != null ? label : key;
	}

}