package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmployeeDetailsForTeamMemberDTO {
	
	private Long empId;
	private Long employeementId;
	private String name;
	private Long jobRoleId;
	private String jobRoleName;
	private String departmentId;
//	private String employeeRole;
	private String isTeamLead;
	private String startDate;
	private String employeeName;
	private String deptName;
	private Long deptId;
	private String isConsultant;
    private Long resourceOverviewId;
    public EmployeeDetailsForTeamMemberDTO(Long empId, Long employeementId, String name,
            Long jobRoleId, String jobRoleName,
            Long deptId, String deptName,
            String isConsultant) {
           this.empId = empId;
           this.employeementId = employeementId;
           this.name = name;
           this.jobRoleId = jobRoleId;
           this.jobRoleName = jobRoleName;
           this.deptId = deptId;
           this.deptName = deptName;
           this.isConsultant = isConsultant;
}
	public EmployeeDetailsForTeamMemberDTO() {}

}
