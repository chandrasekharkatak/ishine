package com.apmosys.employeeportal.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTimesheetProjectResponse {

	private Long poId;
	private String poNo;
	private Long poProjectId;
	private Long ishineProjectId;
	private String projectName;
	private Long empId;
	private String employmentId;
	private String empName;
	private String billableType;
	private String departmentName;
	private String jobRole;
	private Long totalTimesheetCount;
	private Long filledTimesheetCount;
	private Long teamActive;
	private String isConsultant;
	private String isApprenticeship;
	private String isApmosysProduct;


	public EmployeeTimesheetProjectResponse(String poNo, Long poProjectId, Integer ishineProjectId, String projectName,
			Long empId, String employmentId, String empName, String billableType, String departmentName, String jobRole,
			Long filledTimesheetCount) {
		this.poNo = poNo;
		this.poProjectId = poProjectId;
		this.ishineProjectId = ishineProjectId != null ? Long.parseLong(ishineProjectId.toString()) : null;
		this.projectName = projectName;
		this.empId = empId;
		this.employmentId = employmentId;
		this.empName = empName;
		this.billableType = billableType;
		this.departmentName = departmentName;
		this.jobRole = jobRole;
		this.filledTimesheetCount = filledTimesheetCount;
	}

	public EmployeeTimesheetProjectResponse(String poNo, Long poProjectId, Integer ishineProjectId, String projectName,
			Long empId, Long employmentId, String empName, String billableType, String departmentName, String jobRole,
			Long filledTimesheetCount, Long teamActive,String isConsultant,String isApprenticeship,String isApmosysProduct) {
		this.poNo = poNo;
		this.poProjectId = poProjectId;
		this.ishineProjectId = ishineProjectId != null ? Long.parseLong(ishineProjectId.toString()) : null;
		this.projectName = projectName;
		this.empId = empId;
		this.employmentId = employmentId != null ? getPrefix(isConsultant, isApprenticeship, isApmosysProduct)+ "-" + employmentId: null;
		this.empName = empName;
		this.billableType = billableType;
		this.departmentName = departmentName;
		this.jobRole = jobRole;
		this.filledTimesheetCount = filledTimesheetCount;
		this.teamActive = teamActive;
	}
	
	private String getPrefix(String isConsultant, String isApprenticeship, String isApmosysProduct) {
	    if ("true".equalsIgnoreCase(isConsultant) || "1".equals(isConsultant)) {
	        return "CS";
	    } else if ("true".equalsIgnoreCase(isApprenticeship) || "1".equals(isApprenticeship)) {
	        return "A";
	    } else if ("true".equalsIgnoreCase(isApmosysProduct) || "1".equals(isApmosysProduct)) {
	        return "AP";
	    }
	    return "A";
	}


}
