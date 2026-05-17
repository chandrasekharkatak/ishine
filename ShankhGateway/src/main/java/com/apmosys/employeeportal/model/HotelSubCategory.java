package com.apmosys.employeeportal.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
public class HotelSubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hotel_category_id", nullable = false)
    private HotelCategory hotelCategory;

    private String hotelSubCategoryName;

    private String description;

    private String isActive;

    private Long createdBy;
    private String createdByName;
    @Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private Timestamp createdOn;
}
