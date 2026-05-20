package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TravelClassRequest {
    private Long travelClassId;
    private Long travelReasonId;
    private String travelReason;
    private String travelMode;

    private Long travelModeId;
    private String travelClass;
    private String description;
    private Long createdBy;
}
