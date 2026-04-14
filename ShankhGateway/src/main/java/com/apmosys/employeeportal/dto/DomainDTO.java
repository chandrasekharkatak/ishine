package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DomainDTO {

	private Long domainId;
	private String domainName;
	private Long createdBy;
	private Long updatedBy;
	private String updatedOn;
	private String createdOn;
	private Long specializationId;
	private String specializationName;
	private List<SpecializationDTO> allSpecializationList;
	private String createdByName;
	private String updatedByName;
	private String isActive;
	private Long[] domainIdList;
	private Long empId;
	
}
