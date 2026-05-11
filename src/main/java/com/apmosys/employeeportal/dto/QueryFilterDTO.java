package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QueryFilterDTO {
    private String column;
    private String operator;
    private String value;      // main value
    private String valueTo;    // for BETWEEN
    /** AND / OR (optional, applies between rows; default AND). */
    private String conjunction;
}
