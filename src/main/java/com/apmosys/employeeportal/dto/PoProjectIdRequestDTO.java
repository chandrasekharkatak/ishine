package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PoProjectIdRequestDTO {
	
	private Set<Long> poProjectIdList;

}
