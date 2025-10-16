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
public class SearchEmployeeDTO {
	private Long empId;
	private String employeeName;
	private String employeementId;
	private String email;
	private Long jobRoleId;
	private String jobRole;
	private Long deptId;
	private String deptName;
	private List<EmployeeSkillProficiencyDTO> skillsEmp;
	private List<CertificateDTO> certificatesEmp; 
	private byte[] imageBytes;

}
