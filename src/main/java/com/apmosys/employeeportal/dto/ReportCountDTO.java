package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportCountDTO {

	//billable employee summary
	private Long billableYes;
	private Long billableNo;
	private Long billableOther;
	
	//employee status summary
	private Long employeeStatusProbation;
	private Long employeeStatusConfirmed;
	private Long employeeStatusResigned;
	private Long employeeStatusInActive;
	private Long employeeStatusRetain;
	
	//gender summary
	private Long genderMale;
	private Long genderFemale;
	private Long genderOther;
	
	//age summary
	private Long age18to25;
	private Long age25to35;
	private Long age35to45;
	private Long ageAbove45;
	
	//fresher-lateral summary graph
	private Long fresherCount;
	private Long lateralCount;
	
	//Employee Experience 
	private Long employeeYears0to1;
	private Long employeeYears1to2;
	private Long employeeYears2to5;
	private Long employeeYears5to10;
	private Long employeeYearsAbove10;
	
	private Long apprenticeYears0to1;
	private Long apprenticeYears1to2;
	private Long apprenticeYears2to5;
	private Long apprenticeYears5to10;
	private Long apprenticeYearsAbove10;
	
	private Long consultantYear0to1;
	private Long consultantYear1to2;
	private Long consultantYear2to5;
	private Long consultantYear5to10;
	private Long consultantYearAbove10;
	
	//departmentwise billable / non-billable summary
	private Long fixedCost;
	private Long TNM;
	private Long bench;
	private Long shadow;
	private Long internalRNDProducts;
	
	// for showing total counts only 
	private Long totalEmployeeCountDisplay;
	private Long probationCountDisplay;
	private Long apprenticeCountDisplay;
	private Long consultantCountDisplay;
	private Long regularCountDisplay;
	
	
	private String departmentName;
    private String status;
    private Long empCount;
    
    
    public ReportCountDTO(String departmentName, String status, Long empCount) {
        this.departmentName = departmentName;
        this.status = status;
        this.empCount = empCount;
    }
    

	private Long totalEmployeeCount;
    private Long probationCount;
    private Long apprenticeCount;
    private Long consultantCount;
    private Long regularCount;
    
	
	
	
}
