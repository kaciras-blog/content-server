package net.kaciras.blog.facade;

import lombok.extern.slf4j.Slf4j;
import net.kaciras.blog.infrastructure.bootstarp.ConnectorBuilder;
import net.kaciras.blog.infrastructure.bootstarp.HostBuilder;
import net.kaciras.blog.infrastructure.bootstarp.TomcatBuilder;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.HttpPutFormContentFilter;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.*;
import java.util.EnumSet;
import java.util.Properties;

@Slf4j
public class Main {

	private static AnnotationConfigApplicationContext appContext;

	public static void main(String[] args) throws Exception {
		Thread.currentThread().setUncaughtExceptionHandler((t, e) -> log.error("线程抛出了未检查的错误", e));

		//add config bean from args
		appContext = new AnnotationConfigApplicationContext(ServiceConfiguration.class);

		Properties config = appContext.getBean("config", Properties.class);
		ConnectorBuilder connectorBuilder = new ConnectorBuilder()
				.protocol("org.apache.coyote.http11.Http11Nio2Protocol")
				.port(80)
				.enableCompress();

		if (config.get("web.cert") != null) {
			SSLHostConfig sc = new SSLHostConfig();
			sc.setCertificateFile(config.getProperty("web.cert"));
			sc.setCertificateChainFile(config.getProperty("web.chain"));
			sc.setCertificateKeyFile(config.getProperty("web.pvk"));
			connectorBuilder.port(443).enableSsl(sc).enableHttp2();
		}

		if (config.get("web.port") != null) {
			connectorBuilder.port(Integer.parseInt(config.getProperty("web.port")));
		}

		TomcatBuilder.redirectLog();
		Tomcat tomcat = new TomcatBuilder()
				.baseDir(".")
				.executor("Tomcat-", 2, 8)
				.addConnector(connectorBuilder.build())
				.addHost(new HostBuilder()
						.hostName("localhost")
						.appBase("")
						.addContext("app", "", "", Main::configServletContext)
						.build())
				.build();
		tomcat.getServer().setShutdown("SHUTDOWN");
		tomcat.getServer().setPort(60002);

		tomcat.start();
		tomcat.getServer().await();

		tomcat.stop();
		tomcat.destroy();
		appContext.close();
	}

	/**
	 * 配置ServletContext，与web.xml作用相同。
	 *
	 * @param context ServletContext
	 */
	private static void configServletContext(ServletContext context) {
		AnnotationConfigWebApplicationContext webApplicationContext = new AnnotationConfigWebApplicationContext();
		webApplicationContext.setParent(appContext);
		webApplicationContext.setServletContext(context);
		webApplicationContext.register(WebContextConfig.class);
		webApplicationContext.refresh();

		Properties config = webApplicationContext.getBean("config", Properties.class);
		context.addListener("org.springframework.web.util.IntrospectorCleanupListener");

		FilterRegistration.Dynamic putFilter = context
				.addFilter("httpPutFormContentFilter", new HttpPutFormContentFilter());
		putFilter.setAsyncSupported(true);
		putFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC), false, "*");

		SessionCookieConfig cookieConfig = context.getSessionCookieConfig();
		cookieConfig.setHttpOnly(true);
		cookieConfig.setDomain(config.getProperty("web.SessionCookieDomain"));

		context.setSessionTimeout(24 * 60);

		ServletRegistration.Dynamic servlet = context
				.addServlet("dispatcherServlet", new DispatcherServlet(webApplicationContext));
		servlet.setLoadOnStartup(1);
		servlet.setAsyncSupported(true);
		servlet.addMapping("/");
		servlet.setMultipartConfig(new MultipartConfigElement(null, 8388608, 8388608, 0));
	}
}
