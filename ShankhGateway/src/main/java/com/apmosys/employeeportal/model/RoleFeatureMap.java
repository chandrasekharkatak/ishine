package com.apmosys.employeeportal.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="RoleSubfeatureMapping")
public class RoleFeatureMap {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long roleFeatureMapId;
	
//	@ManyToOne
//	@JoinColumn(name="jobRoleId")
	private Long jobRoleId;
	
//	@ManyToMany
//	@JoinColumn(name="featureId")
//	private List<FeatureMaster> featureMaster;
	
//	@OneToMany
	private  Long subFeatureMasterId;

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
	
	
}
