package com.apmosys.employeeportal.dto;

import java.sql.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PoDetailsForProjectPoMappingDTO {
 
	private Long poId;
    private String poNo;
    private Date poStartDate;
    private Date poEndDate;
    private Long clientAddressId;
    private String clientLocation;
    private String clientState;
    private String createdByEmpId;
    private String createdByEmpName;
    private List<DepartmentIdAndNameDto> departmentList;
    private String commentForRmg;
    private String apmosysRmEmpId;
    private String apmosysRmEmpName;
    private String apmosysRmEmail;
    private String clientRmName;
    private Long prevPo;
    private Long nextPO;
    private Boolean isRenewable;
    private List<POResourceRequirementDTO> resourceRequirementList;
    private boolean isActive;
}
