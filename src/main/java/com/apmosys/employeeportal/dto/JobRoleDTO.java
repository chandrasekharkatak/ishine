package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class JobRoleDTO {

	private Long jobRoleId;	
	private String name;	
	private Long departmentId;	
	private String createdOn;	
	private String createdBy;	
	private LocalDateTime updatedOn;	
	private int updatedBy;
	private int createdById;
	private String departmentName;
	private Long hodId;
	
	
	public Long getJobRoleId() {
		return jobRoleId;
	}
	public void setJobRoleId(Long jobRoleId) {
		this.jobRoleId = jobRoleId;
	}	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getDepartmentId() {
		return departmentId;
	}
	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}
	public String getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}	
	public int getCreatedById() {
		return createdById;
	}
	public void setCreatedById(int createdById) {
		this.createdById = createdById;
	}
	public LocalDateTime getUpdatedOn() {
		return updatedOn;
	}
	public void setUpdatedOn(LocalDateTime updatedOn) {
		this.updatedOn = updatedOn;
	}
	public int getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}	
	public String getDepartmentName() {
		return departmentName;
	}
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	public Long getHodId() {
		return hodId;
	}
	public void setHodId(Long hodId) {
		this.hodId = hodId;
	}
	
	
	
	
	
}
