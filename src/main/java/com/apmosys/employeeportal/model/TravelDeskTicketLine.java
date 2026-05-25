package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "travel_desk_ticket_line")
public class TravelDeskTicketLine {

	public static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
	public static final String STATUS_LEVEL_REJECTED = "LEVEL_REJECTED";
	public static final String STATUS_PENDING_ADMIN = "PENDING_ADMIN";
	public static final String STATUS_FULFILLED = "FULFILLED";
	public static final String STATUS_ADMIN_REJECTED = "ADMIN_REJECTED";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "line_id")
	private Long lineId;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticket_id", nullable = false)
	private TravelDeskTicket ticket;

	@Column(name = "line_no")
	private Integer lineNo;

	@Column(name = "request_type")
	private String requestType;

	@Column(name = "travel_mode")
	private String travelMode;

	@Column(name = "travel_class")
	private String travelClass;

	@Column(name = "trip_type")
	private String tripType;

	@Column(name = "travel_reason")
	private String travelReason;

	@Column(name = "purpose")
	private String purpose;

	@Column(name = "from_location")
	private String fromLocation;

	@Column(name = "to_location")
	private String toLocation;

	@Column(name = "from_date")
	private Timestamp fromDate;

	@Column(name = "to_date")
	private Timestamp toDate;

	@Column(name = "hotel_category")
	private String hotelCategory;

	@Column(name = "hotel_sub_category")
	private String hotelSubCategory;

	@Column(name = "city")
	private String city;

	@Column(name = "project_id")
	private Long projectId;

	@Column(name = "project_name")
	private String projectName;

	@Column(name = "client_id")
	private Integer clientId;

	@Column(name = "client_name")
	private String clientName;

	@Column(name = "supporting_doc_ids")
	private String supportingDocIds;

	@Column(name = "line_status", nullable = false)
	private String lineStatus;

	@Column(name = "approver_remarks")
	private String approverRemarks;

	@Column(name = "booking_reference")
	private String bookingReference;

	@Column(name = "admin_proof_doc_ids")
	private String adminProofDocIds;

	@Column(name = "fulfilled_on")
	private Timestamp fulfilledOn;
}
