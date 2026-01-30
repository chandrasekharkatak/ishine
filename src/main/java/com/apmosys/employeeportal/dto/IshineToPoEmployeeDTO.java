package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IshineToPoEmployeeDTO {
	
	 	private Long empId;
	    private String empName;
	    private String roleName;
	    private String exp;
	    private String departmentName;

	    private Long departmentId;
	    private Long clientRoleId;
	    private String clientSideId;
	    
	    private Long billableDays;
	    private Long noOfWorkingDays;
	    private LocalDate startDate;
	    private LocalDate endDate;

	    public IshineToPoEmployeeDTO(long empId,String empName,String roleName,String experience,String departmentName,
	            long departmentId,long clientRoleId,String clientSideId,long timesheetFilledCount,
	            LocalDate startDate,LocalDate endDate ) {
	    	
	        this.empId = empId;
	        this.empName = empName;
	        this.roleName = roleName;
	        this.exp = experience;
	        this.departmentName = departmentName;
	        this.departmentId = departmentId;
	        this.clientRoleId = clientRoleId;
	        this.clientSideId = clientSideId;
	        this.noOfWorkingDays = timesheetFilledCount;
	        this.billableDays=timesheetFilledCount;
	        this.startDate = startDate;
	        this.endDate = endDate;
	    }
	    

}
