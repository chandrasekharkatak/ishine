package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ExportExcelPerformance {

	private Long employeementId;
	private String employmentstatus;
	private String departmentName;
	private String email;
	private String name;
	private String hodRemarks;
	private String managerRemarks;
	private String managerRating;
	private String hodRating;
	private String hrRating;
	private String reportingManagerName;
	private String hodName;
	private String completionStatus;
	private String hrReviewStatus;
	private String hrRemarks;
	private String finalRating;
	private String quarterycle;
	private String billable_type;
	private String financialYear;
	private Float totalExperience;
	private String billable;
	private String billableType;
	private String managerName;
	/** HR rater name from employee_performance.hr_id (export). */
	private String hrName;
	private Long  managerId;
	private String dateOfJoining;
	
}
