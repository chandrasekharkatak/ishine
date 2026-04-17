package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class EmployeeSpecializationMap {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long empSpecializationMapId;
	private Long empId;
	private Long specializationId;
	private Timestamp updatedOn;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;

}
