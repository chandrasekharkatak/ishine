package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "travel_based_reimbursement_request")
public class TravelBasedReimbursementRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serialNo;

    private Integer travelId;

    private String invoiceNo;

    private Timestamp invoiceDate;

    private Double amount;

    private String uploadedBy;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private Timestamp uploadedOn;

    private String updatedBy;

    private Timestamp updatedOn;
}
