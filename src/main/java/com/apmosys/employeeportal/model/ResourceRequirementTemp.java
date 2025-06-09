package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Audited
@ToString
@Table(name="resource_requirement_temp")
public class ResourceRequirementTemp {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long resourceRequirementId;
	private String role;
    private Integer count;
    private String experience;
    private String department;
    private Long resourceOverviewId;
	private Long poProjectId;
}
