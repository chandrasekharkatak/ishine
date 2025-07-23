package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class EmployeeClientSideIdMapping {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long mappingId;
	private String clientSideId;
	private Long projectId;
	private Long empId;
	private Long createdBy;
	private LocalDateTime createdOn;
	private Long updatedBy;
	private LocalDateTime updatedOn;
	private Boolean active;

}
