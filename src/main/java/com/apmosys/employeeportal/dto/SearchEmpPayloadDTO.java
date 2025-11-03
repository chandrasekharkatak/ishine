package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor 
public class SearchEmpPayloadDTO {

	private List<Long> certificateIds;
	private List<Long> skillIds;
	private List<Long> deptIds;
	private List<Long> certificationDeptIds;
	private String certificateStatus;
	private String specialization;
	private int page;
	private int size;
}


