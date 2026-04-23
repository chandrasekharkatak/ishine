package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentStatusSummaryDTO {
	
	
    private String dept;
    private Integer total;
    private Integer ready;
    private Integer pending;
    private Integer defaulter;
    private Integer notFilled;
    private Long deptId;
    private Integer approvedRepeat;
    private Integer pendingRepeat;
    private Integer defaulterRepeat;

}
