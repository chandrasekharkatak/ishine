package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRequestDTONew {
    
    private Long activityId;
    private Long clientId;
    private Long clientLocationId;
    private Long teamId;
    private String description;
    private Integer durationMinutes;
    private Double durationHours;
}

