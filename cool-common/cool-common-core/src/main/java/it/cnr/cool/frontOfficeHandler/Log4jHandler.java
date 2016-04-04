package it.cnr.cool.frontOfficeHandler;

import it.cnr.cool.service.frontOffice.TypeDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log4jHandler implements ILoggerHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(Log4jHandler.class);

	@Override
	public String write(String json, TypeDocument typeDocument) {

		LOGGER.error(typeDocument.getName() + "," + json);

		return null;
	}
}
