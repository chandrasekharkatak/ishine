package com.apmosys.employeeportal.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentEmployeeTypeCountDTO {
    private String departmentName;
    private String type; // "Apprentice" or "Employee"
    private Long empCount;
}
