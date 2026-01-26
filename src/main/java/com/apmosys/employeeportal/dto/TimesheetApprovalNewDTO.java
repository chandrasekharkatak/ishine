package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class TimesheetApprovalNewDTO {
    private List<Long> timesheetIds;
    private String status;
    private Long updatedBy;
    private String rejectReason;
}
