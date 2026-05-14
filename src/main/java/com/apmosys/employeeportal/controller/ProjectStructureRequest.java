package com.apmosys.employeeportal.controller;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectStructureRequest {

	private String[] deptName;
	private String type;
	private String[] depts;
	private List<Long> departmentIds;
	private List<String> employeeNames;
}
