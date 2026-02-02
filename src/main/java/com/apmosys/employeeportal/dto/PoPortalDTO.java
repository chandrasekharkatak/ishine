package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PoPortalDTO {

	private Long deptId;
	private String empId;
	private String empName;
	private String isActive;
	private String isHead;
	private String mailId;
	private String mobile;
	private Long roleId;
	private String deptName;
	private String hodId;
	private String roleName;
	private String deptAbbreviation;
	private Long employeeId;
	private Boolean isBillable;
	private Boolean isTnm;
	private String employeeMentId;
	private String isApmosysProduct;



	public PoPortalDTO(Long empId, String empName, Long deptId, String employeementStatus, String mailId, Long mobile, Long roleId, Long hodId,
			Long employeeId, String isHead,String isApmosysProduct) {
		this.empId = employeeId != null ? employeeId.toString() : null; //iShine Primary key
		this.empName = empName;
		this.deptId = deptId;
		this.isActive = employeementStatus != null ? !employeementStatus.equals("InActive") ? "Y" : "N" : null;
		this.mailId = mailId;
		this.mobile = mobile != null ? mobile.toString() : null;
		this.roleId = roleId;
		this.hodId = hodId != null ? hodId.toString() : null;
//		this.employeeId = employeeId;
		this.isHead = isHead;
		this.employeeMentId=Boolean.TRUE.equals(isApmosysProduct)?"AP-"+empId:"A-"+empId;
	}

	public PoPortalDTO(Long deptId, String deptName, Long hodId, String deptAbbreviation, Boolean isBillable, Boolean isTnm) {
		this.deptName = deptName;
		this.deptId = deptId;
		this.hodId = hodId != null ? hodId.toString() : null;
		this.deptAbbreviation = deptAbbreviation;
		this.isBillable = isBillable;
		this.isTnm = isTnm;
	}
}
