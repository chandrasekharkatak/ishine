package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

    
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShadowEntryDTO {


    private Long shadowEmpId;   // who filled the timesheet
    private Long shadowForEmpId; // on behalf of whom (main resource)
    private LocalDate date;
    private Long timesheetId;

}
