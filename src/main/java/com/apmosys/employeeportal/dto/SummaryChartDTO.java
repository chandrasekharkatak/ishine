package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SummaryChartDTO {
	
	private String projectName;
	private Long timesheetId;
	private Long totalTimesheetsFilled;
	
	
	 //getProjectTimesheetSummaryByEmpId
    public SummaryChartDTO(String projectName, Long totalTimesheetsFilled) {
        this.projectName = projectName;
        this.totalTimesheetsFilled = totalTimesheetsFilled;
    }

    public SummaryChartDTO() {};
}
