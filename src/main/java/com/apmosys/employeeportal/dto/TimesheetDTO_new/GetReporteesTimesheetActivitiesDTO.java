package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GetReporteesTimesheetActivitiesDTO {

	private String activity;
    private String activityDescription;
    private Short durationMinutes;
    private String teamName;
    
}
