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

package it.cnr.mock;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import it.cnr.cool.service.I18nService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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
