package com.apmosys.employeeportal.utility;

import java.lang.reflect.Type;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import lombok.extern.slf4j.Slf4j;

@Component
@RestControllerAdvice
@Slf4j
public class LoggerInterceptor extends RequestBodyAdviceAdapter implements ResponseBodyAdvice<ServiceResponse>, HandlerInterceptor {

	
	private CustomLogger logger = new CustomLogger();
	
	/* Request Logging */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		logger.setApiInfo(request.getRequestURI());
		logger.setUserInfo(request.getHeader("currentUser"));
		return HandlerInterceptor.super.preHandle(request, response, handler);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		logger.log();
		HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
	}
	
	
	/* Request Body Logging */
	@Override
	public boolean supports(MethodParameter methodParameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}
	
	@Override
	public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
			Class<? extends HttpMessageConverter<?>> converterType) {
		logger.setRequestBody(body.toString());
		return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
	}

	
	/* Response Body Logging */
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public ServiceResponse beforeBodyWrite(ServiceResponse body, MethodParameter returnType,
			MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
			ServerHttpRequest request, ServerHttpResponse response) {
		logger.setResponse(body.toString());
		return body;
	}

}
