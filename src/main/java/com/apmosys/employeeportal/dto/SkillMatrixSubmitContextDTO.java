package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Logged-in employee snapshot + skills for their HRMS department (job role → department → skills_master).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillMatrixSubmitContextDTO {

	private String fullName;
	private String employmentId;
	private String reportingManagerName;
	private String hodName;
	private String dateOfJoining;
	private Long departmentId;
	private String departmentName;
	private String designation;
	private String experience;
	private String mobileNumber;
	private String jobRoleName;
	/** When job role or department mapping is missing */
	private String contextMessage;
	private List<SkillMatrixSubmitSkillDTO> skills = new ArrayList<>();
}
