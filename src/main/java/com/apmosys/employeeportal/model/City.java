package com.apmosys.employeeportal.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cityId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hotel_category_id", nullable = false)
    private HotelCategory hotelCategory;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hotel_sub_category_id", nullable = false)
    private HotelSubCategory hotelSubCategory;

    private String cityName;
    private String description;
    private String isActive = "Y";
    private Long createdBy;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private Timestamp createdOn;
}
