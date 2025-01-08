package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Audited
@Table(name = "EmployeeTeamMapping")
public class EmployeeTeamMap {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long employeeTeamMapId;
	
	private Long empId;
	private Long teamId;
	
	private Long jobRoleId;
	private Long active;
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp startDate;
	
	private String employeeRole;
	
	private LocalDateTime endDate;
	
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime updatedOn;
	

}
