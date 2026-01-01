package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class projectTimesheetMappingDTO_new {
    private Long timesheetId;
    private Long projectId;

    private String poNo;
    private Long poId;

    private LocalDateTime clientInTime;
    private LocalDateTime clientOutTime;

    private Boolean isNightShift;

    private Integer clientApprovalStatus;
    private Integer status;

    private Long shadowEmpId;
    private Integer totalClientWorkingMinutes;
    
     private List<timesheetActivityMappingDTO_new> activities;
}
