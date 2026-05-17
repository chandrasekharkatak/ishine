package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FilterMatrix {
	
	private Long empId;
	private List<Long> skillIds;
	private List<Long> certificateIds;
	private List<Long> deptIds;
	private List<Long> certificationDeptIds;
	private String certificateStatus;
	

}
