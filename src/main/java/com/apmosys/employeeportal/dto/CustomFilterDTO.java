package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CustomFilterDTO {

	String column;
	String operator;
	String value;
	String conjunction;
	String customQuery;
	String startDate;
	String endDate;
	Long empId;
}
