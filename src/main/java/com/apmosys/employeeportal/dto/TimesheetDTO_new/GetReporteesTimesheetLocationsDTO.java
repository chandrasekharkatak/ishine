package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class GetReporteesTimesheetLocationsDTO {

	private String workLocationType;
    private String locationInTime;
    private String locationOutTime;
    private Long locationMappingId;
    
    private List<GetReporteesTimesheetProjectsDTO> projects;
    
}
