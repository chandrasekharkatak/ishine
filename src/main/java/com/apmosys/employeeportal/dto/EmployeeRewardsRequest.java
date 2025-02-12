package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeRewardsRequest {
    private Long empId;
    private String ofMonthYear;
}
