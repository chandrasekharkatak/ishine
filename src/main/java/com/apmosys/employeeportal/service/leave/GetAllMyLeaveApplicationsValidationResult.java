package com.apmosys.employeeportal.service.leave;

/**
 * Bundled output for get-all-my-leave-applications validation.
 */
public final class GetAllMyLeaveApplicationsValidationResult {

    private final Long empId;

    public GetAllMyLeaveApplicationsValidationResult(Long empId) {
        this.empId = empId;
    }

    public Long empId() {
        return empId;
    }
}
