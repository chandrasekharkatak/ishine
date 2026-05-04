package com.apmosys.employeeportal.Exception;

/**
 * Expected validation / business-rule failure for comp-off flows.
 * Mapped to HTTP 400 + {@link com.apmosys.employeeportal.utility.ServiceResponse} by {@link GlobalException}.
 */
public class CompOffLeaveException extends RuntimeException {

	public CompOffLeaveException(String message) {
		super(message);
	}
}
