package it.cnr.cool.frontOfficeHandler;

import it.cnr.cool.service.frontOffice.TypeDocument;

public interface ILoggerHandler {
	
	String write(String json, TypeDocument typeDocument) ;
}
