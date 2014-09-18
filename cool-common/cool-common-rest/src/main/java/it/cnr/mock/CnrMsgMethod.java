package it.cnr.mock;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class CnrMsgMethod implements TemplateMethodModelEx {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CnrMsgMethod.class);

	@Override
	public Object exec(@SuppressWarnings("rawtypes") List arguments)
			throws TemplateModelException {
		LOGGER.debug(arguments.size() + " arguments" + arguments.toString());
		return "MSG";
	}

}
