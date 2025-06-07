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
public class HotelCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hotelCategory;
    private String description;

    private String isActive = "Y";

    private Long createdBy;
    private Long updatedBy;
    private String createdByName;
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private Timestamp createdOn;

    private Timestamp updatedOn;
}
