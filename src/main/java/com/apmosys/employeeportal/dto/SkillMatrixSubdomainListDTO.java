package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixSubdomainListDTO {

	private Integer subdomainId;
	private Integer domainId;
	private String domainName;
	private String subdomainName;
	private Boolean isActive;
}
