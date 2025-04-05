package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name="ReviewType")
public class ReviewType {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long reviewTypeId;
	private Long quarterId;
	private String reviewLabel;
	private String reviewFieldType;
	private Long deptId;
	private Boolean flag;
	@Transient
	private String departmentName;
	@Transient
	private String employeeName;
	
	@Transient
	private String quarterCycle;
	
	@Transient
	private String updatedByName;
	
	@Transient
	private boolean active;
	
	 @Column(name = "`condition`")  // Using backticks to escape reserved keyword
	 private String condition;
	    private Long createdBy;
		private Long updatedBy;
		private Timestamp updatedOn;	
		@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
		private Timestamp createdOn;
		
		

}
