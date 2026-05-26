package com.apmosys.employeeportal.dto;

import java.util.Date;

import lombok.Data;

@Data
public class SkillMatrixApproveCertificationDTO {
	private String certName;
	private String issuingBody;
	private Date dateObtained;
}

