package com.apmosys.employeeportal.dto;

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
public class DeletedPoSyncDTO {

	
	private SyncRequestType eventType; // PO_DELETED
	private Long projectId;
	private String projectName;
	
	private Long deletedByEmpId;
	private String deletedByEmpName; 
	private Date deletedOn; 
	private List<PoDetailsForProjectPoMappingDTO> associatePos;
	private PoDetailsForProjectPoMappingDTO deletedPo;

}
