package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponseDTONew {
    
    private Long activityId;
    private String activityName;
    private Long clientId;
    private String clientName;
    private Long clientLocationId;
    private String clientLocationName;
    private Long teamId;
    private String teamName;
    private String description;
    private Integer durationMinutes;
    private Double durationHours;
}

