package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LeaveTrendAnalysisDTO {

	  private String employeementId;
	    private String departmentName;
	    private String employeeName;
	    private LocalDate fromDate;
	    private LocalDate toDate;
	    private String status;
	    private String fromDateDayType;
	    private String toDateDayType;
	    private String employeeType;
	    private Long managerId;
	    private String typeOfLeave;
	    private LocalDate leaveDate;
	    private Long empId;
	    private String managerName;
}