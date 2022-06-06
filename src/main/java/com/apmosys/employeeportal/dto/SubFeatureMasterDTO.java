package com.apmosys.employeeportal.dto;



public class SubFeatureMasterDTO {

	private Long featureId;
	private String featureName;
	private Long subFeatureMasterId;
	private String subFeatureName;
	private short subFeatureType;
	private boolean isActive;
	private Long roleFeatureMapId;
	
	
	public Long getFeatureId() {
		return featureId;
	}
	public void setFeatureId(Long featureId) {
		this.featureId = featureId;
	}	
	public String getFeatureName() {
		return featureName;
	}
	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}
	public Long getSubFeatureMasterId() {
		return subFeatureMasterId;
	}
	public void setSubFeatureMasterId(Long subFeatureMasterId) {
		this.subFeatureMasterId = subFeatureMasterId;
	}
	public String getSubFeatureName() {
		return subFeatureName;
	}
	public void setSubFeatureName(String subFeatureName) {
		this.subFeatureName = subFeatureName;
	}
	public short getSubFeatureType() {
		return subFeatureType;
	}
	public void setSubFeatureType(short subFeatureType) {
		this.subFeatureType = subFeatureType;
	}
	public boolean getIsActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	public Long getRoleFeatureMapId() {
		return roleFeatureMapId;
	}
	public void setRoleFeatureMapId(Long roleFeatureMapId) {
		this.roleFeatureMapId = roleFeatureMapId;
	}
	@Override
	public String toString() {
		return "SubFeatureMasterDTO [featureId=" + featureId + ", featureName=" + featureName + ", subFeatureMasterId="
				+ subFeatureMasterId + ", subFeatureName=" + subFeatureName + ", subFeatureType=" + subFeatureType
				+ ", isActive=" + isActive + ", roleFeatureMapId=" + roleFeatureMapId + "]";
	}
	
	
	
	
	
}
