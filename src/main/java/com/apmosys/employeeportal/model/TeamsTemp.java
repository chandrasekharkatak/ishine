package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "teams_temp")
@Audited
public class TeamsTemp {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long teamId;
	
	private String teamName;
	private Long teamLeadId;
	private Integer projectId;
	private String teamLeadName;
	private String isActive;
	private Long poTeamId;
	private String description;
	private String deptIds;
 
	@Embedded
	public CommonProperties commonProperty = new CommonProperties();

}
