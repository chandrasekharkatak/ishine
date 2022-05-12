package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;


public class JobRoleDTO {

	private Long jobRoleId;	
	private String name;	
	private Long departmentId;	
	private Timestamp createdOn;	
	private int createdBy;	
	private LocalDateTime updatedOn;	
	private int updatedBy;
	
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
	public Timestamp getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}
	public int getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
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
	@Override
	public String toString() {
		return "JobRoleDTO [jobRoleId=" + jobRoleId + ", name=" + name + ", department=" + departmentId + ", createdOn="
				+ createdOn + ", createdBy=" + createdBy + ", updatedOn=" + updatedOn + ", updatedBy=" + updatedBy
				+ "]";
	}
	
	
}
