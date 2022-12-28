package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ActivityTemplate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long activityTemplateId;
	private String templateActivity;
	private Long deptId;
	private String employeeRole;
	
}
