package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
	
	@NotNull(message = "eventType is required")
	private SyncRequestType eventType;
	
	@NotNull(message = "projectId is required")
	private Long projectId;
	
	@NotBlank(message = "projectName is required")
	private String projectName;
	
	@NotNull(message = "renewedByEmpId is required")
	private Long renewedByEmpId; 
	
	@NotBlank(message = "renewedByEmpName is required")
	private String renewedByEmpName;
	
	@NotNull(message = "renewedOn is required")
	private Date renewedOn; 
	
	/** Optional; when null or empty, remaining active POs and teams are reconcoded without portal link updates. */
	private List<PoDetailsForProjectPoMappingDTO> associatePosAfterRenewal;
	
	@NotNull(message = "renewedPo is required")
	private PoDetailsForProjectPoMappingDTO renewedPo;


}
