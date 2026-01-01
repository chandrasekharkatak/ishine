package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class timesheetDetailsDTO_new {
    
    private Long timesheetId;

    private Long empId;
    private LocalDate date;

    private Integer dayTypeId;
    private Integer leaveTypeId;

    private Integer status; // calculated logically

    private Integer totalWorkingMinutes;
    private Integer totalActivitiesMinutes;

    private LocalDateTime officeInTime;
    private LocalDateTime officeOutTime;

    private Long createdBy;
    private LocalDateTime createdOn;

    private Long updatedBy;
    private LocalDateTime updatedOn;
}
