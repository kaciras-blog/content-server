package net.kaciras.blog.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.facade.filter.AccessLogInterceptor;
import net.kaciras.blog.facade.filter.CSRFInterceptor;
import net.kaciras.blog.facade.filter.DefenseInterceptor;
import net.kaciras.blog.facade.filter.SecurtyContextInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Component
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

	private final ObjectMapper objectMapper;
	private final SecurtyContextInterceptor securtyContextInterceptor;
	private final AccessLogInterceptor accessLogInterceptor;
	private final CSRFInterceptor csrfInterceptor;
	private final DefenseInterceptor defenseInterceptor;

	@Value("${web.CorsOrigin}")
	private String corsOrigin;

	@Value("${web.accessLog}")
	private boolean accessLog;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("http://localhost", "https://localhost", corsOrigin)
				.allowedMethods("*")
				.allowCredentials(true)
				.allowedHeaders("X-CSRF-Token", "X-Requested-With", "Content-Type")
				.exposedHeaders("Location")
				.maxAge(864000);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(securtyContextInterceptor);
		if (accessLog) {
			registry.addInterceptor(accessLogInterceptor);
		}
		registry.addInterceptor(csrfInterceptor);
		registry.addInterceptor(defenseInterceptor);
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
	}

	@Override
	public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
		configurer.setDefaultTimeout(10000);
	}
}
