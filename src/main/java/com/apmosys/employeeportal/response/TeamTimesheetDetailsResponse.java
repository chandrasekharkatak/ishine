package com.apmosys.employeeportal.response;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamTimesheetDetailsResponse {

	private Long empId;
	private Long employementId;
	private String empName;
	private String department;
	private String role;
	private Long teamId;
	private String teamName;
	private String teamLeadName;
	private String managerName;
	private Integer projectId;
	private String projectName;
	private String projectManagerName;
	private List<String> projectMangerNameList;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
	private Long isEmployeeActive;
	private Long poId;
	private String billingRole;
	private String employementIdStr;
	private String isConsultant;
	private String isApprenticeship;
	private String isApmosysProduct;
	


	public TeamTimesheetDetailsResponse(Long empId, Long employementId, String empName, String department, String role,
			String teamName, Long teamId, String teamLeadName, String managerName, Integer projectId,
			String projectName, String projectManagerName, Long isEmployeeActive, Long poId) {
		this.empId = empId;
		this.employementId = employementId;
		this.empName = empName;
		this.department = department;
		role = role != null ? role.replaceAll(",+$", "") : null;
		this.role = role;
		this.teamName = teamName;
		this.teamId = teamId;
		this.teamLeadName = teamLeadName;
		this.managerName = managerName;
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectManagerName = projectManagerName;
		this.isEmployeeActive = isEmployeeActive;
		this.poId = poId;
	}

//	long, long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String, java.util.Date, java.time.LocalDateTime
	public TeamTimesheetDetailsResponse(Long empId, Long employementId, String empName, String department, String role,
			String teamName, Long teamId, String teamLeadName, String managerName, Integer projectId,
			String projectName, String projectManagerName, LocalDateTime startDate, LocalDateTime endDate) {
		this.empId = empId;
		this.employementId = employementId;
		this.empName = empName;
		this.department = department;
		role = role != null ? role.replaceAll(",+$", "") : null;
		this.role = role;
		this.teamName = teamName;
		this.teamId = teamId;
		this.teamLeadName = teamLeadName;
		this.managerName = managerName;
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectManagerName = projectManagerName;
		this.startDate = startDate;

		this.endDate = endDate;
	}
	
	public TeamTimesheetDetailsResponse(Long empId, Long employementId, String empName, String role,Long poId,String billingRole,
			String teamName, Long teamId, String teamLeadName, String managerName, Integer projectId,
			String projectName, String projectManagerName, LocalDateTime startDate, LocalDateTime endDate) {
		this.empId = empId;
		this.employementId = employementId;
		this.empName = empName;
		role = role != null ? role.replaceAll(",+$", "") : null;
		this.role = role;
		this.poId = poId;
		this.billingRole = billingRole;
		this.teamName = teamName;
		this.teamId = teamId;
		this.teamLeadName = teamLeadName;
		this.managerName = managerName;
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectManagerName = projectManagerName;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public TeamTimesheetDetailsResponse(Long empId, Long employementId, String empName, String department, String role,
			String teamName, Long teamId, String teamLeadName, String managerName, Integer projectId,
			String projectName, String projectManagerName, Long isEmployeeActive, Long poId,String isConsultant,
			String isApprenticeship,String isApmosysProduct) {
		this.empId = empId;
		this.employementId = employementId;
		this.empName = empName;
		this.department = department;
		role = role != null ? role.replaceAll(",+$", "") : null;
		this.role = role;
		this.teamName = teamName;
		this.teamId = teamId;
		this.teamLeadName = teamLeadName;
		this.managerName = managerName;
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectManagerName = projectManagerName;
		this.isEmployeeActive = isEmployeeActive;
		this.poId = poId;
		this.employementIdStr = employmentId != null ? getSuffix(isConsultant, isApprenticeship, isApmosysProduct)+ "-" + employmentId: null;		
		}

	
	private String getSuffix(String isConsultant, String isApprenticeship, String isApmosysProduct) {
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
