package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

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

	    private Long departmentId;
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

	    public IshineToPoEmployeeDTO(Long empId,String empName,String roleName,String experience,String departmentName,
	            Long departmentId,Long clientRoleId,Integer clientSideId,Long timesheetFilledCount,
	            LocalDate startDate,LocalDate endDate,String isApmosysProduct ) {
	    	
	    	this.empId = "true".equalsIgnoreCase(isApmosysProduct)? "AP-" + empId: "A-" + empId;
//	        this.empId = empId;
	        this.empName = empName;
	        this.roleName = roleName;
	        this.exp = experience;
	        this.departmentName = departmentName;
	        this.departmentId = departmentId;
	        this.roleId = clientRoleId;
	        this.clientSideId = clientSideId;
	        this.noOfWorkingDays = timesheetFilledCount;
	        this.billableDays=timesheetFilledCount;
	        this.startDate = startDate;
	        this.endDate = endDate;
	    }
	    
	    public IshineToPoEmployeeDTO(Long empId,String empName,String roleName,String experience,String departmentName,
	            Long departmentId,Long clientRoleId,Integer clientSideId,Long timesheetFilledCount,
	            LocalDate startDate,LocalDate endDate,String isApmosysProduct,Long poId,Long ishineEmpId,
	            Long shadowEmpId,Integer isShadow) {
	    	
	    	this.empId = "true".equalsIgnoreCase(isApmosysProduct)? "AP-" + empId: "A-" + empId;
	        this.empName = empName;
	        this.roleName = roleName;
	        this.exp = experience;
	        this.departmentName = departmentName;
	        this.departmentId = departmentId;
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
	        
	    }

	    

}
