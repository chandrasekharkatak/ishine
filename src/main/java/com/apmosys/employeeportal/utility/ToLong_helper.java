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
	

	public Long safeParseLong(Object obj) {
	    try {
	        String str = (obj != null) ? obj.toString().trim() : "";
	        return (!str.isEmpty()) ? Long.parseLong(str) : null;
	    } catch (NumberFormatException e) {
	        System.err.println("Invalid Long value: " + obj);
	        return null;
	    }
	}

	public Float safeParseFloat(Object obj) {
	    try {
	        String str = (obj != null) ? obj.toString().trim() : "";
	        return (!str.isEmpty()) ? Float.parseFloat(str) : null;
	    } catch (NumberFormatException e) {
	        System.err.println("Invalid Float value: " + obj);
	        return null;
	    }
	}

	public String getSafeString(Object obj) {
	    return (obj != null) ? obj.toString().trim() : null;
	}
  
	  public static Double safeParseDouble(Object obj) {
	        if (obj == null) return null;
	        return Double.valueOf(obj.toString());
	    }
	  
	  public static Integer safeParseInt(Object obj) {
		    if (obj == null) return null;
		    try {
		        return Integer.valueOf(obj.toString());
		    } catch (Exception e) {
		        return null;
		    }
		}


}
