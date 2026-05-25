package com.apmosys.employeeportal.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class TravelLineAdminFulfillmentDTO {
	private Long lineId;
	private String bookingReference;
	private BigDecimal bookingAmount;
	private List<Long> adminProofDocIds;
}
