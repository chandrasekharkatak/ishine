package com.apmosys.employeeportal.dto;

import com.apmosys.employeeportal.model.TabMaster;

public class FeatureMasterDTO {
	
	private Long featureId;	
	private String featureName;	
	private Long tabId;
	private TabMaster tabMaster;
	
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
	public TabMaster getTabMaster() {
		return tabMaster;
	}
	public void setTabMaster(TabMaster tabMaster) {
		this.tabMaster = tabMaster;
	}
	
	
	
	

}
