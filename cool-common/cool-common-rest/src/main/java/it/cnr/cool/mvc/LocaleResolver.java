package it.cnr.cool.mvc;

import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.util.WebUtils;

public class LocaleResolver extends AcceptHeaderLocaleResolver 
{
	/**
	 * Default name of the locale specification parameter: "locale".
	 */
	public static final String DEFAULT_PARAM_NAME = "locale";

	private String paramName = DEFAULT_PARAM_NAME;


	/**
	 * Set the name of the parameter that contains a locale specification
	 * in a locale change request. Default is "locale".
	 */
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	/**
	 * Return the name of the parameter that contains a locale specification
	 * in a locale change request.
	 */
	public String getParamName() {
		return this.paramName;
	}

	@Override
    public Locale resolveLocale(HttpServletRequest request) 
    {
        Locale locale = Locale.getDefault();
		String newLocale = request.getParameter(this.paramName);
		if (newLocale != null) {
			LocaleEditor localeEditor = new LocaleEditor();
			localeEditor.setAsText(newLocale);
			locale = (Locale) localeEditor.getValue();			
			WebUtils.setSessionAttribute(request, SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);
		}else if (WebUtils.getSessionAttribute(request, SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME) != null) {
			locale = (Locale)WebUtils.getSessionAttribute(request, SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
		}else{
	        // set language locale from browser header if available
	        final String acceptLang = request.getHeader("Accept-Language");
	        if (acceptLang != null && acceptLang.length() != 0)
	        {
	           StringTokenizer t = new StringTokenizer(acceptLang, ",; ");
	           
	           // get language and convert to java locale format
	           String language = t.nextToken().replace('-', '_');
	           locale = I18NUtil.parseLocale(language);
	        }
		}
        // set locale onto Alfresco thread local
        I18NUtil.setLocale(locale);           
        
        return locale;
    }


}
