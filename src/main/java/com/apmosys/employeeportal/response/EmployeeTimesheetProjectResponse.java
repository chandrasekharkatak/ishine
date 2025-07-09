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

    public EmployeeTimesheetProjectResponse(String poNo,Long poProjectId, Long ishineProjectId, String projectName, Long empId,
            String employmentId, String empName, String billableType, String departmentName, String jobRole,
            Long filledTimesheetCount) {
        this.poNo = poNo;
        this.poProjectId = poProjectId;
        this.ishineProjectId = ishineProjectId;
        this.projectName = projectName;
        this.empId = empId;
        this.employmentId = employmentId;
        this.empName = empName;
        this.billableType = billableType;
        this.departmentName = departmentName;
        this.jobRole = jobRole;
        this.filledTimesheetCount = filledTimesheetCount;
    }

    public EmployeeTimesheetProjectResponse(String poNo,Long poProjectId, Long ishineProjectId, String projectName, Long empId,
            String employmentId, String empName, String billableType, String departmentName, String jobRole,
            Long filledTimesheetCount,Long teamActive) {
        this.poNo = poNo;
        this.poProjectId = poProjectId;
        this.ishineProjectId = ishineProjectId;
        this.projectName = projectName;
        this.empId = empId;
        this.employmentId = employmentId;
        this.empName = empName;
        this.billableType = billableType;
        this.departmentName = departmentName;
        this.jobRole = jobRole;
        this.filledTimesheetCount = filledTimesheetCount;
        this.teamActive = teamActive;
    }

}
