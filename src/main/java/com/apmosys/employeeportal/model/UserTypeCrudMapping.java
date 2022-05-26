package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
public class UserTypeCrudMapping {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long mapId;
	
	private Long userTypeId;
	
	private String module;	
	private Long moduleId;	
	private String moduleVisibilty;	
	private String creation;	
	private String edit;	
	private String deletion;
	private String view;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;
	
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime updatedOn;
	
	private int createdBy;	
	private int updatedBy;

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

	public Timestamp getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public LocalDateTime getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(LocalDateTime updatedOn) {
		this.updatedOn = updatedOn;
	}

	public int getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}

	public int getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}

	@Override
	public String toString() {
		return "UserTypeCrudMapping [mapId=" + mapId + ", userTypeId=" + userTypeId + ", module=" + module
				+ ", moduleId=" + moduleId + ", moduleVisibilty=" + moduleVisibilty + ", creation=" + creation
				+ ", edit=" + edit + ", deletion=" + deletion + ", view=" + view + "]";
	}
	
	
	
	
	

}
