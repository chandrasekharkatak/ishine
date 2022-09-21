package com.apmosys.employeeportal.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ProtalConfigDTO {
	
    private Short portalConfigId;
	private String configName;
	private Float configPeriod;
	private Float probationPeriod;
	private Float noticePeriod;
	private Float mailTrigger;
	private String updatedOn;	
	private Long updatedBy;

}
