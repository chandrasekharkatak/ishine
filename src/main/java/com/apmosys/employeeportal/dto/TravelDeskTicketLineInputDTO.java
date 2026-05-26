package com.apmosys.employeeportal.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class TravelDeskTicketLineInputDTO {
	private String requestType;
	private String travelMode;
	private String travelClass;
	/** ONE_WAY | ROUND | MULTI_CITY | HOTEL */
	private String tripType;
	private String travelReason;
	private String purpose;
	private String fromLocation;
	private String toLocation;
	private Date fromDate;
	private Date toDate;
	private String hotelCategory;
	private String hotelSubCategory;
	private String city;
	private Long projectId;
	private String projectName;
	private Integer clientId;
	private String clientName;
	private List<Long> docIds = new ArrayList<>();
	private Long kycDocumentId;
}
