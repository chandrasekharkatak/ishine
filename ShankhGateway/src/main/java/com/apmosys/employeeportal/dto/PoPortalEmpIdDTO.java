package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PoPortalEmpIdDTO {
	

	private Long deptId;
	private String empId;
	private String empName;
	private String isActive;
	private String isHead;
	private String mailId;
	private String mobile;
	private Long roleId;
	private String deptName;
	private Long hodId;
	private String roleName;
	private String deptAbbreviation;
	private Long employeeId;
	private Boolean isBillable;
	private Boolean isTnm;
	private String employeeMentId;
	private String isApmosysProduct;
	private Long ishineEmpId;
	
	
	public PoPortalEmpIdDTO(Long deptId, String deptName, Long hodId, String deptAbbreviation, Boolean isBillable, Boolean isTnm) {
		this.deptName = deptName;
		this.deptId = deptId;
		this.hodId = hodId;
		this.deptAbbreviation = deptAbbreviation;
		this.isBillable = isBillable;
		this.isTnm = isTnm;
	}

}
