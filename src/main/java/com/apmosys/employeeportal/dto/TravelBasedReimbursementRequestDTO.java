package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;

import javax.persistence.Column;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TravelBasedReimbursementRequestDTO {
	
	private Integer serialNo;

    private Integer travelId;

    private String invoiceNo;

    private Timestamp invoiceDate;

    private Double amount;

    private String uploadedBy;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = true, updatable = true)
    private Timestamp uploadedOn;

    private String updatedBy;

    private Timestamp updatedOn;
}
