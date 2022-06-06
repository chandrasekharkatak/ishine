package com.apmosys.employeeportal.dto;

import java.util.List;


import com.apmosys.employeeportal.model.TabMaster;

public class FeatureMasterDTO {
	
	private Long featureId;	
	private String featureName;	
	private Long tabId;
	private List<SubFeatureMasterDTO> subFeatures;
	private Long jobRoleId;
	
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
	public Long getTabId() {
		return tabId;
	}
	public void setTabId(Long tabId) {
		this.tabId = tabId;
	}
	public List<SubFeatureMasterDTO> getSubFeatures() {
		return subFeatures;
	}
	public void setSubFeatures(List<SubFeatureMasterDTO> subFeatures) {
		this.subFeatures = subFeatures;
	}
	public Long getJobRoleId() {
		return jobRoleId;
	}
	public void setJobRoleId(Long jobRoleId) {
		this.jobRoleId = jobRoleId;
	}
	@Override
	public String toString() {
		return "FeatureMasterDTO [featureId=" + featureId + ", featureName=" + featureName + ", tabId=" + tabId
				+ ", subFeatures=" + subFeatures + ", jobRoleId=" + jobRoleId + "]";
	}
	
	
	
	
	

}
