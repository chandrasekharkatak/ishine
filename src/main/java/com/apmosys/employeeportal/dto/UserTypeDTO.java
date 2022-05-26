package com.apmosys.employeeportal.dto;

import java.util.List;

import com.apmosys.employeeportal.model.UserTypeCrudMapping;

public class UserTypeDTO {
	
	private Long userTypeId;
	private String userType;
	private int createdBy;
	private String createdOn;
	private int updatedBy;
	
	private List<UserTypeCrudMappingDTO> allCrudMapping; 
	
	public Long getUserTypeId() {
		return userTypeId;
	}
	public void setUserTypeId(Long userTypeId) {
		this.userTypeId = userTypeId;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public int getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}
	public String getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}
	public int getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}
	public List<UserTypeCrudMappingDTO> getAllCrudMapping() {
		return allCrudMapping;
	}
	public void setAllCrudMapping(List<UserTypeCrudMappingDTO> allCrudMapping) {
		this.allCrudMapping = allCrudMapping;
	}
	
	

}
