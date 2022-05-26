package com.apmosys.employeeportal.dto;

public class UserTypeCrudMappingDTO {
	
private Long mapId;
	
	private Long userTypeId;
	
	private String module;	
	private Long moduleId;	
	private String moduleVisibilty;	
	private String creation;	
	private String edit;	
	private String deletion;
	private String view;
	
	public Long getMapId() {
		return mapId;
	}
	public void setMapId(Long mapId) {
		this.mapId = mapId;
	}
	public Long getUserTypeId() {
		return userTypeId;
	}
	public void setUserTypeId(Long userTypeId) {
		this.userTypeId = userTypeId;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public Long getModuleId() {
		return moduleId;
	}
	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}
	public String getModuleVisibilty() {
		return moduleVisibilty;
	}
	public void setModuleVisibilty(String moduleVisibilty) {
		this.moduleVisibilty = moduleVisibilty;
	}
	public String getCreation() {
		return creation;
	}
	public void setCreation(String creation) {
		this.creation = creation;
	}
	public String getEdit() {
		return edit;
	}
	public void setEdit(String edit) {
		this.edit = edit;
	}
	public String getDeletion() {
		return deletion;
	}
	public void setDeletion(String deletion) {
		this.deletion = deletion;
	}
	public String getView() {
		return view;
	}
	public void setView(String view) {
		this.view = view;
	}
	
	

}
