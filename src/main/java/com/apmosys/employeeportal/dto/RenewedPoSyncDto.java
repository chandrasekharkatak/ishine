package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import com.apmosys.employeeportal.enums.SyncRequestType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RenewedPoSyncDto {
	
	
	private SyncRequestType eventType;
	private Long projectId;
	private String projectName;
	
	private Long renewedByEmpId; 
	private String renewedByEmpName;
	private Date renewedOn;   
	private List<PoDetailsForProjectPoMappingDTO> associatePosAfterRenewal;
	private PoDetailsForProjectPoMappingDTO renewedPo;


}
