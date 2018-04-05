package net.kaciras.blog.facade.filter;

import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import eu.bitwalker.useragentutils.Version;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.accesslog.AccessLogDAO;
import net.kaciras.blog.domain.accesslog.AccessRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;

@RequiredArgsConstructor
@Component
public class AccessLogInterceptor extends HandlerInterceptorAdapter {

	private final AccessLogDAO accessLogDAO;

	@Value("${accessLog.enable}")
	private boolean enable;

	@Async
	@Override
	public void postHandle(HttpServletRequest req,
						   HttpServletResponse res,
						   Object handler,
						   ModelAndView modelAndView) throws Exception {
		if (!enable) {
			return;
		}
		String uri = req.getRequestURI();
		if (uri == null) {
			return; // TODO: Unknow reason
		}
		AccessRecord entity = new AccessRecord();
		entity.setAddress(InetAddress.getByName(req.getRemoteAddr()));
		entity.setPath(uri);
		entity.setReferer(req.getHeader("Referer"));
		entity.setStatusCode(res.getStatus());

		UserAgent userAgent = UserAgent.parseUserAgentString(req.getHeader("User-Agent"));
		entity.setBrowser(userAgent.getBrowser().getName());

		OperatingSystem operatingSystem = userAgent.getOperatingSystem();
		if (operatingSystem != null) {
			entity.setSystem(operatingSystem.getName());
		}

		Version browserVersion = userAgent.getBrowserVersion();
		if (browserVersion != null) {
			entity.setBrowserVersion(browserVersion.toString());
		}
		accessLogDAO.insert(entity);
	}

}
