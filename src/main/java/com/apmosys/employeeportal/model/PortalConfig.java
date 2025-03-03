package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PortalConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Short portalConfigId;
	
	private String configName;
	
	private Float configPeriod;
	
	private Float mailTrigger;
	
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime updatedOn;
	
	private String updatedBy;
	private String empId;
	private Long departmentId;
	private String configValue;
}
