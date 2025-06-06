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
	
	//Employee Join vs Resign
	//joined regular
	private Long joiningJanCount;
	private Long joiningFebCount;
	private Long joiningMarCount;
	private Long joiningAprilCount;
	private Long joiningMayCount;
	private Long joiningJuneCount;
	private Long joiningJulyCount;
	private Long joiningAugCount;
	private Long joiningSepCount;
	private Long joiningOctoberCount;
	private Long joiningNovCount;
	private Long joiningDecCount;
	
	//joined apprentice
	private Long joinApprenticeJanCount;
	private Long joinApprenticeFebCount;
	private Long joinApprenticeMarCount;
	private Long joinApprenticeAprCount;
	private Long joinApprenticeMayCount;
	private Long joinApprenticeJunCount;
	private Long joinApprenticeJulCount;
	private Long joinApprenticeAugCount;
	private Long joinApprenticeSepCount;
	private Long joinApprenticeOctCount;
	private Long joinApprenticeNovCount;
	private Long joinApprenticeDecCount;
	
	//joined consultant 
	private Long joinConsultantJanCount;
	private Long joinConsultantFebCount;
	private Long joinConsultantMarCount;
	private Long joinConsultantAprCount;
	private Long joinConsultantMayCount;
	private Long joinConsultantJunCount;
	private Long joinConsultantJulCount;
	private Long joinConsultantAugCount;
	private Long joinConsultantSepCount;
	private Long joinConsultantOctCount;
	private Long joinConsultantNovCount;
	private Long joinConsultantDecCount;
	
	//resigned 
	private Long resignJanCount;
	private Long resignFebCount;
	private Long resignMarCount;
	private Long resignAprilCount;
	private Long resignMayCount;
	private Long resignJuneCount;
	private Long resignJulyCount;
	private Long resignAugCount;
	private Long resignSepCount;
	private Long resignOctoberCount;
	private Long resignNovCount;
	private Long resignDecCount;
	
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
	
	
//    public ReportCountDTO(Long billableYes, Long billableNo, Long billableOther,
//            Long age18to25, Long age25to35, Long age35to45, Long ageAbove45,
//            Long genderMale, Long genderFemale, Long genderOther,
//            Long employeeStatusConfirmed, Long employeeStatusResigned, 
//            Long employeeStatusProbation, Long employeeStatusRetain, Long employeeStatusInActive,
//            Long lateralCount, Long fresherCount,
//            Long apprenticeYears0to1, Long apprenticeYears1to2, Long apprenticeYears2to5, 
//            Long apprenticeYears5to10, Long apprenticeYearsAbove10,
//            Long employeeYears0to1, Long employeeYears1to2, Long employeeYears2to5, 
//            Long employeeYears5to10, Long employeeYearsAbove10,
//            Long fixedCost, Long tnm, Long bench, Long shadow, Long internalRNDProducts) {
//
//this.billableYes = billableYes;
//this.billableNo = billableNo;
//this.billableOther = billableOther;
//
//this.age18to25 = age18to25;
//this.age25to35 = age25to35;
//this.age35to45 = age35to45;
//this.ageAbove45 = ageAbove45;
//
//this.genderMale = genderMale;
//this.genderFemale = genderFemale;
//this.genderOther = genderOther;
//
//this.employeeStatusConfirmed = employeeStatusConfirmed;
//this.employeeStatusResigned = employeeStatusResigned;
//this.employeeStatusProbation = employeeStatusProbation;
//this.employeeStatusRetain = employeeStatusRetain;
//this.employeeStatusInActive = employeeStatusInActive;
//
//this.lateralCount = lateralCount;
//this.fresherCount = fresherCount;
//
//this.apprenticeYears0to1 = apprenticeYears0to1;
//this.apprenticeYears1to2 = apprenticeYears1to2;
//this.apprenticeYears2to5 = apprenticeYears2to5;
//this.apprenticeYears5to10 = apprenticeYears5to10;
//this.apprenticeYearsAbove10 = apprenticeYearsAbove10;
//
//this.employeeYears0to1 = employeeYears0to1;
//this.employeeYears1to2 = employeeYears1to2;
//this.employeeYears2to5 = employeeYears2to5;
//this.employeeYears5to10 = employeeYears5to10;
//this.employeeYearsAbove10 = employeeYearsAbove10;
//
//this.fixedCost = fixedCost;
//this.TNM= tnm;
//this.bench = bench;
//this.shadow = shadow;
//this.internalRNDProducts = internalRNDProducts;
//}
	
	
	
	
	
	
	
	
}
