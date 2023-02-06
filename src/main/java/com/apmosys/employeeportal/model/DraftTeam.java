package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DraftTeam {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long draftTeamId;
	private Long poProjectId;
	private String projectName;
	private String teamName;
	private String description;
	private Long teamLeadId;
	private String teamMember;
	private String deptIds;
	
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;
	private Long createdBy;
	private Long updatedBy;
	private LocalDateTime updatedOn;

}
