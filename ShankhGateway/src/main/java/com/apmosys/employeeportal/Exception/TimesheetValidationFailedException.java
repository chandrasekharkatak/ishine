package com.apmosys.employeeportal.Exception;

public class TimesheetValidationFailedException  extends RuntimeException {
	public TimesheetValidationFailedException(String message) {
		super(message);
	}
}
