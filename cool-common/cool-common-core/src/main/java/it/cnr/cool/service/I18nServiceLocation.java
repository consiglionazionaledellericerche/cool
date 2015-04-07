package it.cnr.cool.service;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

//TODO: usare http://stackoverflow.com/questions/1771166/access-properties-file-programatically-with-spring

public class I18nServiceLocation implements InitializingBean{

	@Autowired
	private I18nService i18nService;
	private String location;
	private List<String> locations;

	public void setLocations(List<String> locations) {
		this.locations = locations;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (location != null)
			i18nService.addLocation(location);
		if (locations != null) {
			for (String location : locations) {
				i18nService.addLocation(location);
			}
		}
	}
}