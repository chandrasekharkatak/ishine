package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TravelModeDTO {
    private String travelReason;
    private String modeType;
    private String description;
    private Long createdBy;
}
