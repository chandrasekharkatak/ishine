package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TravelModeDTO {
    private String travelReason;
    private Long travelModeId;
    private String modeType;
    private String description;
    private String isActive;
    private Long createdBy;
    private Timestamp createdOn;
    private String expenditureType ;
    private String requiresVehicleType ;
    private String vehicleTypeName;
    private String travelReasonName;
    private String foodTypeName;
    /** Vehicle master row id (update/delete vehicle type) */
    private Long vehicleTypeId;
    /** Food allowance master row id (update/delete food type) */
    private Long foodTypeId;
    private String createdByName;
}
