package com.apmosys.employeeportal.utility;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomLogger {
	private String userInfo;
	private String apiInfo;
	private String requestBody;
	private String response;

	
	public CustomLogger() {
		super();
	}

	public CustomLogger(String userInfo, String apiInfo, String requestBody, String response) {
		super();
		this.userInfo = userInfo;
		this.apiInfo = apiInfo;
		this.requestBody = requestBody;
		this.response = response;
	}
	
	public String getUserInfo() {
		return userInfo;
	}
	public void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}
	public String getApiInfo() {
		return apiInfo;
	}
	public void setApiInfo(String apiInfo) {
		this.apiInfo = apiInfo;
	}
	public String getRequestBody() {
		return requestBody;
	}
	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	
	public void log() {
		String logMsg = "";
		logMsg = "User : "+this.userInfo +" | API : "+ this.apiInfo +" | Request-body : "+this.requestBody +" | Response : "+ this.response;
		log.info(logMsg);
	}
	
}
