package com.apmosys.employeeportal.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetDashboardResponseDTO {
	private TimesheetDashboardCountDTO summary;
    private Map<String, DepartmentWiseStatusDTO> departmentWise;
}
