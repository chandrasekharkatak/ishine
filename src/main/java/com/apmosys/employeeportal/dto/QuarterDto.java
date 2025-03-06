package com.apmosys.employeeportal.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
//@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuarterDto {
	private String startDate;
	private String endDate;
}
