package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SkillCertConfigDTO {
	
	private Long uploadedBy;
	private String importType;

}
