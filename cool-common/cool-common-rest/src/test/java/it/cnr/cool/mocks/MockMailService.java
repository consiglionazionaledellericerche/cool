/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.cool.mocks;

import it.cnr.cool.mail.MailService;
import it.cnr.cool.mail.model.EmailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Service
@Primary
public class MockMailService implements MailService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MockMailService.class);

	@Override
	public void send(String to, String subject, String text)
			throws MailException {

		LOGGER.info("send mail to: " + to + " [" + subject + "] " + text);

	}

	@Override
	public void send(String to, String cc, String bcc, String subject, String text) throws MailException {

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
