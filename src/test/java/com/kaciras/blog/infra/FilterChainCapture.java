package com.kaciras.blog.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public final class FilterChainCapture implements FilterChain {

	// 传递给过滤器的请求和响应参数 filter.doFilter(inRequest, inResponse, chain)
	public HttpServletRequest inRequest;
	public HttpServletResponse inResponse;

	// 传递给本过滤链的请求和响应参数 chain.doFilter(outRequest, outResponse)
	public HttpServletRequest outRequest;
	public HttpServletResponse outResponse;

	public static FilterChainCapture doFilter(Filter filter) throws Exception {
		return doFilter(filter, new MockHttpServletRequest());
	}

	public static FilterChainCapture doFilter(Filter filter, HttpServletRequest request) throws Exception {
		var capture = new FilterChainCapture();
		capture.inRequest = request;
		capture.inResponse = new MockHttpServletResponse();
		filter.doFilter(request, capture.inResponse, capture);
		return capture;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response) {
		outRequest = (HttpServletRequest) request;
		outResponse = (HttpServletResponse) response;
	}

	/** 断言请求被拦截，没有传递到后续处理链 */
	public void assertIntercepted() {
		if (outRequest != null) {
			throw new AssertionError("请求应当被拦截，但是却调用了 chain.doFilter(...)");
		}
	}
}
