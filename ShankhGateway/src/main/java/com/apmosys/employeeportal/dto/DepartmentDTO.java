package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@NoArgsConstructor
public class DepartmentDTO {

	private Long deptId;
	private String name;	
	private Long hodId;
	private String hodName;	
	private String createdOn;	
	private Integer createdBy;
	private String createdByName;	
	private String updatedOn;	  
	private Integer updatedBy;
	private List<HolidayDTO> holidays;
	private Long oldDeptId;
	private String updatedByName;
	private String isDeptUsedInIshine;
	private String isDeptUsedInPoPortal;
	
	private String deptName;
	private String hodEmploymentId;
	private String deptAbbreviation; // New field
	private String deptColorCode;
	private String isBillable;
    private String isTnm;
	public DepartmentDTO(Long deptId, Integer createdBy, Date createdOn
			, String name, String createdByName, String hodName, Long hodId, LocalDateTime updatedOn
			, String updatedByName, String deptAbbreviation,Integer updatedBy,String isBillable, String isTnm) {
				this.deptId = deptId;
				this.createdBy = createdBy;
				this.createdOn = createdOn != null ? createdOn.toString() : null;
				this.name = name;
				this.createdByName = createdByName;
				this.hodName = hodName;
				this.hodId = hodId;
				this.updatedOn = updatedOn != null ? updatedOn.toString() : null;
				this.updatedByName = updatedByName;
				this.deptAbbreviation = deptAbbreviation;
				this.updatedBy = updatedBy;
				this.isBillable =isBillable;
				this.isTnm =isTnm;
			}
	
	public DepartmentDTO(Long deptId,String name) {
		this.deptId = deptId;
	this.name= name;
	}
	
	
	
	
}
