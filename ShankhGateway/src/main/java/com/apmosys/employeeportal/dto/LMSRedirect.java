package com.apmosys.employeeportal.dto;

public class LMSRedirect {

	private String email;
	private String tokem;
	private String url;
	private String message;
	
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getTokem() {
		return tokem;
	}
	public void setTokem(String tokem) {
		this.tokem = tokem;
	}
	
}
