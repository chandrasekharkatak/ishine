package com.apmosys.employeeportal.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
public class ReimbursementTravelMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long travelModeId;

    // ManyToOne: DB may contain orphan expenditure_type_id rows; do not fail entire list load.
    @ManyToOne(fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "expenditure_type_id", nullable = false)
    private ExpenditureType expenditureType;

    private String modeType;
    private String description;
    private String isActive;
    private String requiresVehicleType;
    private Long createdBy;
    private String createdByName;
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private Timestamp createdOn;
}
