package com.apmosys.employeeportal.service.leave;

/**
 * Bundled output for get-all-leave-applications-by-emp-id validation.
 */
public final class GetAllLeaveApplicationsByEmpIdValidationResult {

    private final Long empId;

    public GetAllLeaveApplicationsByEmpIdValidationResult(Long empId) {
        this.empId = empId;
    }

    public Long empId() {
        return empId;
    }
}
