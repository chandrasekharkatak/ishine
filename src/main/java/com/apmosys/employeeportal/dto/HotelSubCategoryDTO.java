package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HotelSubCategoryDTO {
    private Long id;
    private String hotelCategory;
    private String hotelSubCategoryName;
    private String description;
    private Long createdBy;
}
