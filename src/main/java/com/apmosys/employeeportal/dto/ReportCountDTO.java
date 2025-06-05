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
	
	
	
	
	
	
}
