package com.apmosys.employeeportal.dto;

import java.util.List;

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
public class MultiProjectEmployeeDTO {

    private Long empId;
    private String employeeCode;
    private String employeeName;
    
    private List<Long> projectIds;
    private List<String> projectNames;
}
