package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.util.List;

import com.apmosys.employeeportal.model.ReviewType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL) 
public class ReviewTypeDTO {
   
    private Long reviewTypeId;
	private String reviewLabel;
	private String reviewFieldType;
	private Long[] deptId;
	@JsonIgnore    
	private boolean flag;
	private String condition;
	private Long createdBy;
	private Long quarterId;
	private Long quarterCycle;
	private Long updatedeptId;
	private String departmentName;
	private String employeeName;
    private Long updatedBy;
	private Timestamp updatedOn;
	private Timestamp createdOn;
	private Long DepartmentId;
	private ReviewType[] allSpecializationList;
	
}
