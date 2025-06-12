package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportListDTO {

	    private String employeementId;
	    private String employeeType;
	    private String name;
	    private String experience;
	    private String departmentName;
	    private String email;
	    private String managerName;
	    private String billable;
	    private String billableType;
	    private String projectName;
	    private String clientName;
	    private String dateOfJoining;
	    private String mobileNo;
	    private String employmentstatus;
	    private String totalExperience;
	    private String gender;
	    private String workLocation;
	    private Integer age;
	    private String profileKycStatus;
	    private String clientLocation;

	    // Constructors
	    public ReportListDTO () {}

	    public ReportListDTO(Object[] row) {
	        this.employeementId = (String) row[0];
	        this.employeeType = (String) row[1];
	        this.name = (String) row[2];
	        this.experience = (String) row[3];
	        this.departmentName = (String) row[4];
	        this.email = (String) row[5];
	        this.managerName = (String) row[6];
	        this.billable = (String) row[7];
	        this.billableType = (String) row[8];
	        this.projectName= (String) row[9];
	        this.clientName = (String) row[10];
	        this.dateOfJoining = (String) row[11];
	        this.mobileNo = (String) row[12];
	        this.employmentstatus = (String) row[13];
	        this.totalExperience = (String) row[14];
	        this.gender = (String) row[15];
	        this.workLocation = (String) row[16];
	        this.age = (Integer) row[17];
	        this.profileKycStatus = (String) row[18];
	    }
}
