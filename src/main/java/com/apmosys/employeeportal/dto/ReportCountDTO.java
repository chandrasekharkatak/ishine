package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
	
	
	
}

