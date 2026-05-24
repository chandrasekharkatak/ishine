package com.apmosys.employeeportal.dto;

public class RoleFeatureMapDTO {

	
	private Long roleFeatureMapId;
	private Long jobRoleId;
	private Long subFeatureMasterId;
	private String subFeatureName;
	private Long featureId;
	private String featureName;
	private String tabName;
	private String tabIcon;
	private String tabRouteName;
	private String tabGroup;
	private Integer groupSequence;
	private String groupIcon;
	
	
	public Long getRoleFeatureMapId() {
		return roleFeatureMapId;
	}
	public void setRoleFeatureMapId(Long roleFeatureMapId) {
		this.roleFeatureMapId = roleFeatureMapId;
	}
	public Long getJobRoleId() {
		return jobRoleId;
	}
	public void setJobRoleId(Long jobRoleId) {
		this.jobRoleId = jobRoleId;
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
	public String getTabName() {
		return tabName;
	}
	public void setTabName(String tabName) {
		this.tabName = tabName;
	}
	public String getTabIcon() {
		return tabIcon;
	}
	public void setTabIcon(String tabIcon) {
		this.tabIcon = tabIcon;
	}
	public String getTabRouteName() {
		return tabRouteName;
	}
	public void setTabRouteName(String tabRouteName) {
		this.tabRouteName = tabRouteName;
	}
	 public String getTabGroup() { return tabGroup; }
	    public void setTabGroup(String tabGroup) { this.tabGroup = tabGroup; }

	   public Integer getGroupSequence() { return groupSequence; }
	    public void setGroupSequence(Integer groupSequence) { this.groupSequence = groupSequence; }
	
	    public String getGroupIcon() { return groupIcon; }
	    public void setGroupIcon(String groupIcon) { this.groupIcon = groupIcon; }
	
}
