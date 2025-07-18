package com.apmosys.employeeportal.dto;

import java.time.LocalDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RMGProjectToEmployeeFlatDTO {
	
	 private Integer projectId;
	 private String projectName;
	 private String apmosysRM;
	 private String clientRM;
	 private String poStartDate;
	 private String poEndDate;
	 private String poProjectType;
	 private String poNo;
	  private Long pmEmpId;
	    private String pmName;
	    private Long teamId;
	    private String teamName;
	    private Long empId;
	    private Long employeementId;
	    private String name;
	    private String jobRoleName;
	    private String department;
	    private Long mobileNo;
	    private String email;
	    private String billable;
	    private String billableType;
	    private LocalDateTime effectiveStartDate;
	    private String clientName;
	    
		public RMGProjectToEmployeeFlatDTO(Integer projectId, String projectName, String apmosysRM, String clientRM,
				String poStartDate, String poEndDate,String poNo, String poProjectType,String clientName , 
				Long pmEmpId, String pmName, Long teamId, String teamName, Long empId, String name, String jobRoleName,
				String department,Long mobileNo, String email, String billable, String billableType,
				LocalDateTime effectiveStartDate,Long employeementId) {
//			super(); 
			this.projectId = projectId;
			this.projectName = projectName;
			this.apmosysRM = apmosysRM;
			this.clientRM = clientRM;
			this.poStartDate = poStartDate;
			this.poEndDate = poEndDate;
			this.poProjectType = poProjectType;
			this.clientName = clientName;
			this.poNo = poNo;
			this.pmEmpId = pmEmpId;
			this.pmName = pmName;
			this.teamId = teamId;
			this.teamName = teamName;
			this.empId = empId;
			this.employeementId = employeementId;
			this.name = name;
			this.jobRoleName = jobRoleName;
			this.department = department;
			this.mobileNo = mobileNo;
			this.email = email;
			this.billable = billable;
			this.billableType = billableType;
			this.effectiveStartDate = effectiveStartDate;
		}

}
