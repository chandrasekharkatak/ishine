package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TimesheetIdAndEmpIdDTO {
    
    private Long timesheetId;
    private Long empId;

}
