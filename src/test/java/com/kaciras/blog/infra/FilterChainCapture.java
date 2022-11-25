package com.kaciras.blog.infra;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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
}
