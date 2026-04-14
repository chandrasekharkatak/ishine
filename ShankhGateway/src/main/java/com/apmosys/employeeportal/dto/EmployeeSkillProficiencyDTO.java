package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeSkillProficiencyDTO {
	
	private Long empSkillId;
     private Long empId;
	
	private Long skillId;
	
	private String skillName;
	
    private String additionalSkill;
    
    private Long proficiencyId;
    
    private String proficiencyLevel;
    private String imageUrl;
    
    private Boolean active;
    
    private String createdOn;
    private Long createdBy;
    private String updatedOn;
    private Long updatedBy;
    
    public EmployeeSkillProficiencyDTO(Long empSkillId,Long empId,Long skillId,String skillName,String additionalSkill,
    Long proficiencyId,String proficiencyLevel,String imageUrl,Date createdOn,LocalDateTime updatedOn) {
    	this.empSkillId = empSkillId;
    	this.empId = empId;
    	this.skillId = skillId;
    	this.skillName = skillName;
    	this.additionalSkill = additionalSkill;
    	this.proficiencyId = proficiencyId;
    	this.proficiencyLevel = proficiencyLevel;
    	this.imageUrl = imageUrl;
    	this.createdOn = createdOn != null ? createdOn.toString() : null;
    	this.updatedOn = updatedOn != null ? updatedOn.toString() : null;
    }
    
    public EmployeeSkillProficiencyDTO(Long empId,Long skillId,String skillName,String additionalSkill) {
    	this.empId = empId;
    	this.skillId = skillId;
    	this.skillName = skillName;
    	this.additionalSkill = additionalSkill;
    }
}
