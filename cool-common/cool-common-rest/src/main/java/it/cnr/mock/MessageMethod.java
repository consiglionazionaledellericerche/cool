package it.cnr.mock;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import it.cnr.cool.service.I18nService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Locale;

public class MessageMethod implements TemplateMethodModelEx{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MessageMethod.class);

	@Autowired
	private I18nService i18nService;
	
	private Locale locale;
	private String uri;
	protected MessageMethod() {
		super();
	}

	public MessageMethod(Locale locale) {
		super();
		this.locale = locale;
	}
	
	public MessageMethod(Locale locale, String uri) {
		super();
		this.locale = locale;
		this.uri = uri;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object exec(List arguments) throws TemplateModelException {
		LOGGER.debug(arguments.size() + " arguments: "
				+ arguments.toString());
		String key = ((SimpleScalar) arguments.get(0)).getAsString();
		List subList = arguments.subList(1, arguments.size());
		String label = uri != null ? 
				(subList.size() > 0 ? i18nService.getLabel(key, uri, locale, subList.toArray()) : i18nService.getLabel(key, uri, locale)): 
				subList.size() > 0 ? i18nService.getLabel(key, locale, subList.toArray()) : i18nService.getLabel(key, locale);
		LOGGER.debug(key + ": " + label);
		return label != null ? label : key;
	}
}
