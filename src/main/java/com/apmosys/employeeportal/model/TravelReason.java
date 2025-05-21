package com.apmosys.employeeportal.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@ToString
@Entity
public class TravelReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String travelReasonName;
    private String description;

    private String isActive;

    private Long createdBy;
    private Long updatedBy;

    @Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private Timestamp createdOn;

    private Timestamp updatedOn;
}
