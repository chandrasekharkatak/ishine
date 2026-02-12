package com.apmosys.employeeportal.dto;

import java.util.Date;
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
    private Date createdOn;
    private Date updatedOn;
    
    private Long clientAddressId;
    private String clientLocation;
    private String clientState;
    private Long createdByEmpId;
    private String createdByEmpName;
    private Long updatedByEmpId;
    private String updatedByEmpName;
    private List<DepartmentIdAndNameDto> departmentList;
    private String commentForRmg;
    private Long apmosysRmEmpId;
    private String apmosysRmEmpName;
    private String apmosysRmEmail;
    private String clientRmName;
    private Long prevPo;
    private Long nextPO;
    private boolean isRenewable;
    private List<POResourceRequirementDTO> resourceRequirementList;
    private boolean isActive;
    private String clientRm;
}
