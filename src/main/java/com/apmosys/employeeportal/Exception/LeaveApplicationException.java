package com.apmosys.employeeportal.Exception;

/**
 * Expected validation / business-rule failure for leave apply flow.
 * Mapped to HTTP 400 + {@link com.apmosys.employeeportal.utility.ServiceResponse} by {@link GlobalException}.
 */
public class LeaveApplicationException extends RuntimeException {

    public LeaveApplicationException(String message) {
        super(message);
    }
}
