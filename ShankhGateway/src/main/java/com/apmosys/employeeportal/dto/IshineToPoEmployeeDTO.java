package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IshineToPoEmployeeDTO {
	
	 
	private String empId;
	private String empName;
	private String roleName;
	private String exp;
	private String departmentName;

	private Long roleId; //Client Role id from project_requirement_mapping
	private Integer clientSideId;
	private String isApmosysProduct;
	
	private Long billableDays;
	private Long noOfWorkingDays;
	private LocalDate startDate;
	private LocalDate endDate;
	
	 private Long poId;
	private Long ishineEmpId;
	private Long shadowEmpId;
	private Integer isShadow;
	private String msg;
	 private Long empLeaveCount;
	private List<LocalDate>empLeaveDates;
	private List<LocalDate>shadowTimeSheetDate;
	private List<LocalDate>weekoffDate;
	private List<LocalDate>clientHoliday;
	private List<LocalDate> compoffleave;
	private List<LocalDate>workingOnANonWorkingDay;
	private LocalDate empEndDate;
	private Boolean isEmployeeOnLeaveWhenDeboarded;
	@JsonIgnore
	private Long etmRoleId;



	public IshineToPoEmployeeDTO(Long empId,String empName,String roleName,String experience,String departmentName,
			Long departmentId,Long clientRoleId,Integer clientSideId,Long timesheetFilledCount,
			LocalDate startDate,LocalDate endDate,String isApmosysProduct ) {
		
		this.empId = "true".equalsIgnoreCase(isApmosysProduct)? "AP-" + empId: "A-" + empId;
//	        this.empId = empId;
		this.empName = empName;
		this.roleName = roleName;
		this.exp = experience;
		this.departmentName = departmentName;
		this.roleId = clientRoleId;
		this.clientSideId = clientSideId;
		this.noOfWorkingDays = timesheetFilledCount;
		this.billableDays=timesheetFilledCount;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public IshineToPoEmployeeDTO(Long empId,String empName,String roleName,String experience,String departmentName,
			Long clientRoleId,Integer clientSideId,Long timesheetFilledCount,
			LocalDate startDate,LocalDate endDate,String isApmosysProduct,Long poId,Long ishineEmpId,
			Long shadowEmpId,Integer isShadow) {
		
		this.empId = "true".equalsIgnoreCase(isApmosysProduct)? "AP-" + empId: "A-" + empId;
		this.empName = empName;
		this.roleName = roleName;
		this.exp = experience;
		this.departmentName = departmentName;
		this.roleId = clientRoleId; 
		this.clientSideId = clientSideId;
		this.noOfWorkingDays = timesheetFilledCount;
		this.billableDays=timesheetFilledCount;
		this.startDate = startDate;
		this.endDate = endDate;
		this.poId=poId;
		this.ishineEmpId=ishineEmpId;
		this.shadowEmpId=shadowEmpId;
		this.isShadow=isShadow;
		this.isApmosysProduct=isApmosysProduct;
		
	}

	public IshineToPoEmployeeDTO(Long employeementId, String empName, String roleName, String experience, String departmentName,
			Long clientRoleId, Integer clientSideId, Long timesheetFilledCount, 
			String isApmosysProduct, Long poId, Long ishineEmpId
			) {
		
		this.empId = "true".equalsIgnoreCase(isApmosysProduct) ? "AP-" + employeementId : "A-" + employeementId;
		this.empName = empName;
		this.roleName = roleName;
		this.exp = experience;
		this.departmentName = departmentName;
		this.roleId = clientRoleId; 
		this.clientSideId = clientSideId;
		this.noOfWorkingDays = timesheetFilledCount;
		this.billableDays = timesheetFilledCount;
		this.isApmosysProduct = isApmosysProduct;
		this.poId = poId;
		this.ishineEmpId = ishineEmpId;
	   
	}
	
    public IshineToPoEmployeeDTO(Long employeementId, String empName, String roleName, String experience, String departmentName,
            Long clientRoleId, Integer clientSideId, Long timesheetFilledCount,
            String isApmosysProduct, Long poId, Long ishineEmpId, Long etmRoleId
            ) {
        this.empId = "true".equalsIgnoreCase(isApmosysProduct) ? "AP-" + employeementId : "A-" + employeementId;
        this.empName = empName;
        this.roleName = roleName;
        this.exp = experience;
        this.departmentName = departmentName;
        this.roleId = clientRoleId;
        this.clientSideId = clientSideId;
        this.noOfWorkingDays = timesheetFilledCount;
        this.billableDays = timesheetFilledCount;
        this.isApmosysProduct = isApmosysProduct;
        this.poId = poId;
        this.ishineEmpId = ishineEmpId;
        this.etmRoleId = etmRoleId;
    }

	
}
