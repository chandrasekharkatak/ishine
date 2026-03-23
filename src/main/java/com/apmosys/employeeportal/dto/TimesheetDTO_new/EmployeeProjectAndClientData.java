package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeProjectAndClientData {

	private Integer projectId;
	private Long clientId;
	private Long clientLocationId;
}
