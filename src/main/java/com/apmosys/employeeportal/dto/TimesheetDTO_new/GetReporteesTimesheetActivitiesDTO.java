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

	private Long timesheetId;
	private Long locationMappingId;
	private Integer projectId;
	private String activity;
    // private String activityDescription;
    private String durationMinutes;
    private String teamName;
    
}
