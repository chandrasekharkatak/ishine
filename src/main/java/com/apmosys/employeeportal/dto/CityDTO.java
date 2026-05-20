package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CityDTO {
    private Long cityId;
    private Long hotelCategoryId;
    private Long hotelSubCategoryId;
    private String cityName;
    private String description;
    private Long createdBy;
}
