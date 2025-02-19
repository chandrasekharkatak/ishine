package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.util.List;

import com.apmosys.employeeportal.model.ReviewType;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReviewTypeDTO {
   
    private Long reviewTypeId;
	private String reviewLabel;
	private String reviewFieldType;
	private Long[] deptId;
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
	private ReviewType[] allSpecializationList;
	
}
