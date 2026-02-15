package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO for timesheet metadata used by date picker logic.
 * Contains only header-level fields from employee_timesheets_new.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetMetadataDTO {

    private Long timesheetId;
    private Long empId;
    private LocalDate date;
    private Integer dayTypeId;
    private Integer status;
}

