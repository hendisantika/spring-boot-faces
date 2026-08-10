package com.auth0.samples.bootfaces;

import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.DispatcherType;
import org.apache.myfaces.webapp.StartupServletContextListener;
import org.apache.webbeans.servlet.WebBeansConfigurationListener;
import org.ocpsoft.rewrite.servlet.RewriteFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import java.util.EnumSet;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	/**
	 * Boots the CDI container. Faces 4.0 is specified on top of CDI, so this has
	 * to be in place before MyFaces starts up — hence the explicit ordering.
	 */
	@Bean
	public ServletListenerRegistrationBean<WebBeansConfigurationListener> cdiStartupListener() {
		ServletListenerRegistrationBean<WebBeansConfigurationListener> registration =
				new ServletListenerRegistrationBean<>(new WebBeansConfigurationListener());
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}

	/**
	 * Embedded Tomcat does not run the {@code ServletContainerInitializer} that
	 * MyFaces ships with, so the listener that bootstraps the Faces factories has
	 * to be registered by hand — without it FacesServlet.init() fails with
	 * "No Factories configured for this Application".
	 */
	@Bean
	public ServletListenerRegistrationBean<StartupServletContextListener> facesStartupListener() {
		ServletListenerRegistrationBean<StartupServletContextListener> registration =
				new ServletListenerRegistrationBean<>(new StartupServletContextListener());
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
		return registration;
	}

	@Bean
	public ServletRegistrationBean<FacesServlet> facesServletRegistration() {
		ServletRegistrationBean<FacesServlet> registration =
				new ServletRegistrationBean<>(new FacesServlet(), "*.jsf");
		registration.setLoadOnStartup(1);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<RewriteFilter> rewriteFilter() {
		FilterRegistrationBean<RewriteFilter> rwFilter = new FilterRegistrationBean<>(new RewriteFilter());
		rwFilter.setDispatcherTypes(EnumSet.of(DispatcherType.FORWARD, DispatcherType.REQUEST,
				DispatcherType.ASYNC, DispatcherType.ERROR));
		rwFilter.addUrlPatterns("/*");
		return rwFilter;
	}
}
