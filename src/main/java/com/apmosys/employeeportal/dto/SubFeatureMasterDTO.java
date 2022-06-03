package com.apmosys.employeeportal.dto;

import com.apmosys.employeeportal.model.TabMaster;

public class SubFeatureMasterDTO {

	private Long featureId;
	private String featureName;
	private Long tabId;
	private TabMaster tabMaster;
	private Long subFeatureMasterId;
	private String subFeatureName;
	private short subFeatureType;
	
	
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
	
	
	
}
