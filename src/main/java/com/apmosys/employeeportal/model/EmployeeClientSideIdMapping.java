package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Audited
public class EmployeeClientSideIdMapping {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long mappingId;
	private String clientSideId;
	private Long projectId;
	private Long empId;
	private Boolean active;
	
	private Long createdBy;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createdOn;
	
	private Long updatedBy;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime updatedOn;

}
