package com.apmosys.employeeportal.dto;

import java.util.List;

import com.apmosys.employeeportal.model.Asset;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AssetDTO {

	private Long empId;
	private Long assetId;
	private String assestName;
	private Long employeementId;
	private String isAssigned;
	private String departmentName;
	private Long deptId;
	private List<EmployeeAssetMapDTO> departmentWiseAssetList;
	private Long updatedBy;
	private String deptConsent;
	private String employeeName;
	private Long employeeAssetMapId;
	private String updatedByName;
	private String assetType;
	private String assetDetail;
	
}
