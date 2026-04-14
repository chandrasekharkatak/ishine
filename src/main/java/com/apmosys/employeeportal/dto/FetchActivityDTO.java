package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchActivityDTO {


    private Long id;

    private Long timesheetId;

    private Long activityId;

    private Integer projectId;

    private Long locationMappingId;

    private String description;

    private Short durationMinutes;

    private Long teamId;

}
