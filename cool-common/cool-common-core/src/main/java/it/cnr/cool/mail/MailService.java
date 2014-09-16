package it.cnr.cool.mail;

import it.cnr.cool.mail.model.EmailMessage;

import org.springframework.mail.MailException;

public interface MailService {
	void send(String to, String subject, String text) throws MailException;
	void send(String subject, String text) throws MailException;
	void send(EmailMessage emailMessage) throws MailException;
	void sendErrorMessage(final String currentUser, String url, String serverPath, final Exception we) throws MailException;
	void sendErrorMessage(final String currentUser, String subject, String body) throws MailException;
	String getMailToProtocollo();
	String getMailToHelpDesk();
}
