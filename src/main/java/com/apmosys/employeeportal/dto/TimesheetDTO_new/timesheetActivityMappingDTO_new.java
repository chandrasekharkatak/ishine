package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class timesheetActivityMappingDTO_new {
    
    private Long timesheetId;
    private Long activityId;
    private Long projectId;

    private String description;

    private Integer durationMinutes;
    private Long clientLocationId;
}
