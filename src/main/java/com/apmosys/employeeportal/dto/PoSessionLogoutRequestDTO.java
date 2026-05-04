package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PoSessionLogoutRequestDTO {
    private Long empId;
    private String poToken;
}
