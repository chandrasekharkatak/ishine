package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Audited
@Entity
public class EmployeeSkillProficiencyMapping {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long empSkillId;
	
	private Long empId;
	
	private Long skillId;
	
    private String additionalSkill;
    
    private Long proficiencyId;
    
    private Boolean active;
    
    @Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;
	
	private Long createdBy;
	
	
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime updatedOn;
	
	private Long updatedBy;	
}
