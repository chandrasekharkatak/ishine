package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrievanceAuditFieldChangeDTO {

	private String fieldName;
	private String oldValue;
	private String newValue;
}
