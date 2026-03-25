package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class FeatureMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long featureId;
	
	private String featureName;
	
//	@ManyToOne
//	@JoinColumn(name = "tabId")
	private Long tabId;

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

	@Override
	public String toString() {
		return "FeatureMaster [featureId=" + featureId + ", featureName=" + featureName + ", tabId=" + tabId + "]";
	}
	
	
}
