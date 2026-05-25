package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;

@Data
public class TravelLineAdminFulfillmentDTO {
	private Long lineId;
	private String bookingReference;
	private List<Long> adminProofDocIds;
}
