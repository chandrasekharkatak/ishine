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
@Table(name = "employee_team_mapping")
public class EmployeeTeamMap {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long employeeTeamMapId;
	
	private Long empId;
	private Long teamId;
	
	private Long jobRoleId;
	private Long active;
	
	private Long rescRemovedBy;
	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = true ,updatable = true)
	private LocalDateTime startDate;
	
	private String employeeRole;
	
	private LocalDateTime endDate;
	
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime updatedOn;
	
    private Long updatedBy;
    private Long resourceOverviewId;
    private Integer isShadow;
    private Long createdBy;
    @Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = true ,updatable = true)
	private Timestamp createdOn;
    
    @Column(name = "is_custom_date_flag")
    private Boolean isCustomDate; 
}
