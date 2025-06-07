package com.apmosys.employeeportal.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
public class TravelMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long travelModeId;

    // Proper ManyToOne relationship
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "travel_reason_id", nullable = false)
    private TravelReason travelReason;

    private String modeType;
    private String description;
    private String isActive;
    private Long createdBy;
    private String createdByName;
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private Timestamp createdOn;
}
