package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating timesheet status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetStatusUpdateRequestDTO {
    private Long timesheetId;
    private Long projectId;
    private Integer status;
    private Long updatedBy;
}

