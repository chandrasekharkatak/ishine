package com.apmosys.employeeportal.dto;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ExpiredPoDto {

	String poNo;
	String projectName;
	String endDate;
	String apmosysRmName;
	String clientName;
	String clientRmName;
	Long poId;
	Long createdBy;
	Integer poExpiredBeforeDays;
	
	

	    public ExpiredPoDto(String poNo, String projectName, String endDate,
	                        String apmosysRmName, String clientName,
	                        String clientRmName, Integer poExpiredBeforeDays) {
	        this.poNo = poNo;
	        this.projectName = projectName;
	        this.endDate = endDate;
	        this.apmosysRmName = apmosysRmName;
	        this.clientName = clientName;
	        this.clientRmName = clientRmName;
	        this.poExpiredBeforeDays = poExpiredBeforeDays;
	    }
	}
