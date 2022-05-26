package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Modules {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long moduleId;
	
	private String module;
	
	private String isCRUD;

	public Long getModuleId() {
		return moduleId;
	}

	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getIsCRUD() {
		return isCRUD;
	}

	public void setIsCRUD(String isCRUD) {
		this.isCRUD = isCRUD;
	}
	
	

}
