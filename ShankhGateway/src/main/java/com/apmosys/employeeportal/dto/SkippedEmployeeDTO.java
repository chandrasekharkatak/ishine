package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SkippedEmployeeDTO {

	private Long empId;
    private Long employmentId;
    private String empName;
    private String designationName;
    private String role;
    private String resourceDept;
    private String experience;
    private String hodEmail;
}
