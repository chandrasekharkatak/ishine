package com.apmosys.employeeportal.exception;

public class ExternalSyncFailedException extends RuntimeException {
	public ExternalSyncFailedException(String message) {
		super(message);
	}

	public ExternalSyncFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
