package com.apmosys.employeeportal.utility;

public class ServiceResponse {

	public static final String STATUS_FAIL="Fail";
	public static final String STATUS_FAIL_1="Fail_1";
	public static final String STATUS_SUCCESS="Success";
	public static final String SOMETHING_WENT_WRONG="Something Went Wrong";
	private Object serviceResponse;
	private String serviceStatus;
	private Object serviceError;
	private String errorStackTrace;
	private String serviceMessage;
	
	
	public Object getServiceResponse() {
		return serviceResponse;
	}
	public void setServiceResponse(Object serviceResponse) {
		this.serviceResponse = serviceResponse;
	}
	public String getServiceStatus() {
		return serviceStatus;
	}
	public void setServiceStatus(String serviceStatus) {
		this.serviceStatus = serviceStatus;
	}
	public Object getServiceError() {
		return serviceError;
	}
	public void setServiceError(Object serviceError) {
		this.serviceError = serviceError;
	}
	public String getErrorStackTrace() {
		return errorStackTrace;
	}
	public void setErrorStackTrace(String errorStackTrace) {
		this.errorStackTrace = errorStackTrace;
	}
	public String getServiceMessage() {
		return serviceMessage;
	}
	public void setServiceMessage(String serviceMessage) {
		this.serviceMessage = serviceMessage;
	}
	
	
	
	
}
