package it.cnr.cool.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class I18nServiceLocation implements InitializingBean{

	@Autowired
	private I18nService i18nService;
	private String location;
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		i18nService.addLocation(location);
	}

}
