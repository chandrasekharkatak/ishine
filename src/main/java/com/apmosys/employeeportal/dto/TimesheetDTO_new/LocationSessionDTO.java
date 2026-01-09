package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing location session data.
 * Multiple location sessions per day (e.g., Office 9:00-13:00, Client Site 14:00-18:00).
 * NEW CONTRACT: Location sessions contain projects and activities.
 * 
 * @author System
 * @version 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationSessionDTO {

    /*primary column data*/
	private Long locationMappingId;
	
    private String workLocationType;
     
    /**
     * Work location type code (e.g., "APMOSYS_OFFICE", "CLIENT_LOCATION", "WFH")
     * Maps to work_location_type_master.code
     * Required for creation
     */
    private Integer workLocationTypeId;

    /**
     * Location in time (format: "HH:mm" or "HH:mm:ss")
     * Required for creation
     * Example: "09:00"
     */
    private String locationInTime;

    /**
     * Location out time (format: "HH:mm" or "HH:mm:ss")
     * Required for creation
     * Example: "13:00"
     */
    private String locationOutTime;

    /**
     * List of projects for this location session
     * Required - at least one project per location session
     */
    private List<ProjectTimesheetDTO> projects;
}

