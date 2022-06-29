package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "LeavePoliciesMaster")
public class LeavePolicyMaster {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Short leavePolicyId;
	
	private String leavePolicyName;

	private String employmentStatus;
}
