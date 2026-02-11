package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "teams")
@Audited
public class Team {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long teamId;
	
	private String teamName;
	private Long teamLeadId;
	private Integer projectId;
	private String teamLeadName;
	private String isActive;
	private Long poTeamId;
	private Long poId;
	private String description;
	private String deptIds;
	private Long spocId;
 
	
	private Timestamp createdOn;
	
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime updatedOn;
	private Long updatedBy;
	private Long createdBy;
//	@Embedded
//	public CommonProperties commonProperty = new CommonProperties();
	
	@Transient
    private Long oldTeamId;
	
}
