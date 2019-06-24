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

package it.cnr.cool.mail;

import it.cnr.cool.mail.model.EmailMessage;
import org.springframework.mail.MailException;

public interface MailService {
	void send(String to, String subject, String text) throws MailException;
	void send(String to, String cc, String bcc, String subject, String text) throws MailException;
	void send(String subject, String text) throws MailException;
	void send(EmailMessage emailMessage) throws MailException;
	void sendErrorMessage(final String currentUser, String url, String serverPath, final Exception we) throws MailException;
	void sendErrorMessage(final String currentUser, String subject, String body) throws MailException;
	String getMailToHelpDesk();
}
