package com.apmosys.employeeportal.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
public class ReimbursementTravelMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long travelModeId;

    // Proper ManyToOne relationship
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "expenditure_type_id", nullable = false)
    private ExpenditureType expenditureType;

    private String modeType;
    private String description;
    private String isActive;
    private String requiresVehicleType;
    private Long createdBy;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private Timestamp createdOn;
}
