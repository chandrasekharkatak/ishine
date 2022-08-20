package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class DepartmentDTO {

	private Long deptId;
	private String name;	
	private Long hodId;
	private String hodName;	
	private String createdOn;	
	private Integer createdBy;
	private String createdByName;	
	private LocalDateTime updatedOn;	
	private Integer updatedBy;
	private List<HolidayDTO> holidays;
	private Long oldDeptId;
	
	
	
	
	
}
