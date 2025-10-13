package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ResourceRequirement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long resourceRequirementId;
	private String role;
	private Integer count;
	private String experience;
	private String department;
	private Long resourceOverviewId;
	private Integer projectId;
	private Long poId;
}
