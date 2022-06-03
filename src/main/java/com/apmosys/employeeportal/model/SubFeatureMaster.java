package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class SubFeatureMaster {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long subFeatureMasterId;
	
	private String subFeatureName;
	
//	@ManyToOne
//	@JoinColumn(name="featureId")
	private Long featureId;
	
//	@ManyToOne
//	private RoleFeatureMap roleFeatureMap;
	
	private short subFeatureType;

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

	public short getSubFeatureType() {
		return subFeatureType;
	}

	public void setSubFeatureType(short subFeatureType) {
		this.subFeatureType = subFeatureType;
	}

	
	
	

}
