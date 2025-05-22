package com.apmosys.employeeportal.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
public class TravelClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long travelClassId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_reason_id", nullable = false)
    private TravelReason travelReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_mode_id", nullable = false)
    private TravelMode travelMode;

    private String travelClass; // e.g., Economy, Business
    private String description;
    private String isActive;
    private Long createdBy;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private Timestamp createdOn;
}
