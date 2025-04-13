package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class GetEmployeeProjectReportDTO {
	
    private List<GetEmployeeProjectReportForEmployeeDTO> getEmployeeProjectReportForEmployeeDTO;
	private List<GetProjectToEmployeeReportForProjectDTO> getProjectToEmployeeReportForProjectDTO;
	private Map<String, Map<String, Map<String, Object>>> projectSummary;
    
}
