package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="emp_primary_project_mapping")
public class EmpPrimaryProjectMapping {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="mapping_id")
	private Long mappingId;
	
	@Column(name="emp_id")
	private Long empId; 
	
	@Column(name="primary_project_id")
	private Long primaryProjectId; 
	
	@Column(name="primary_project_name")
	private String primaryProjectName; 

	
}
