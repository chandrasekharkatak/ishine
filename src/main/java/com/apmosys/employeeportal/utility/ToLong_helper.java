package com.apmosys.employeeportal.utility;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;


@Data
public class ToLong_helper {
	
	public List<Long> convertToLongList(List<Integer> inputList) {
	    return inputList == null ? null : inputList.stream()
	            .map(Integer::longValue)
	            .collect(Collectors.toList());
	}


}
