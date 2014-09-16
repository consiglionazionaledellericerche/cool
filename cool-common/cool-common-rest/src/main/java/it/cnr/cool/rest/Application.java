package it.cnr.cool.rest;


import it.cnr.cool.security.SecurityCheckInterceptor;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

public class Application extends ResourceConfig {

	/**
	 * Register JAX-RS application components.
	 */
	public Application() {
		register(RequestContextFilter.class);
		register(JacksonFeature.class);
		register(SecurityCheckInterceptor.class);
		// packages con i servizi jax-rs
		packages("it.cnr.cool.rest,it.cnr.jconon.rest,it.cnr.doccnr.rest");
	}
}