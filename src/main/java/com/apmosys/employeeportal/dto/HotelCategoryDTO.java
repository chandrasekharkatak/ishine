package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HotelCategoryDTO {
    private Long id;
    private String hotelCategory;
    private String description;
    private Long createdBy;
}
