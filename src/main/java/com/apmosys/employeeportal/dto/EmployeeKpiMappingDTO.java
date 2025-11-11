package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.List;

import com.apmosys.employeeportal.model.Kresponse;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter
public class EmployeeKpiMappingDTO {
	
    private Long id;
    private Long empId;
    private Long quarterId;
    private Long departmentId;
    private String name;
    private String description;
    private List<EmployeeKpisDTO> kpis = new ArrayList<>();
    private String approvedBy;
    private String createdBy;
    private String department;
    private Long managerRating;
    private String managerRemark;
    private String employeeRole;
   
	public void addKpi(EmployeeKpisDTO kpiDto) {
		// TODO Auto-generated method stub
		
	}
}
