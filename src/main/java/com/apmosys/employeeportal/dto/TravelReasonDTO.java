package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TravelReasonDTO {
    private Long id;
    private String travelReasonName;
    private String description;
    private Long createdBy;
    private String updatedBy;
}
