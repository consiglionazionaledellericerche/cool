package it.cnr.cool.mocks;

import it.cnr.cool.mail.MailService;
import it.cnr.cool.mail.model.EmailMessage;

import org.springframework.mail.MailException;

public class MockMailService implements MailService {

	public MockMailService() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void send(String to, String subject, String text)
			throws MailException {
		// TODO Auto-generated method stub

	}

	@Override
	public void send(String subject, String text) throws MailException {
		// TODO Auto-generated method stub

	}

	@Override
	public void send(EmailMessage emailMessage) throws MailException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendErrorMessage(String currentUser, String url,
			String serverPath, Exception we) throws MailException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendErrorMessage(String currentUser, String subject, String body)
			throws MailException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getMailToHelpDesk() {
		// TODO Auto-generated method stub
		return null;
	}

}
