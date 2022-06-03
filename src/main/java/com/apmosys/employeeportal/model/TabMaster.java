package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class TabMaster {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long tabId;
	
	private String tabName;
	
	private String tabRouteName;
	
	private String tabIcon;

	public Long getTabId() {
		return tabId;
	}

	public void setTabId(Long tabId) {
		this.tabId = tabId;
	}

	public String getTabName() {
		return tabName;
	}

	public void setTabName(String tabName) {
		this.tabName = tabName;
	}	

	public String getTabRouteName() {
		return tabRouteName;
	}

	public void setTabRouteName(String tabRouteName) {
		this.tabRouteName = tabRouteName;
	}

	public String getTabIcon() {
		return tabIcon;
	}

	public void setTabIcon(String tabIcon) {
		this.tabIcon = tabIcon;
	}

	@Override
	public String toString() {
		return "TabMaster [tabId=" + tabId + ", tabName=" + tabName + ", tabRouteName=" + tabRouteName + ", tabIcon="
				+ tabIcon + "]";
	}

	
	
	

}
