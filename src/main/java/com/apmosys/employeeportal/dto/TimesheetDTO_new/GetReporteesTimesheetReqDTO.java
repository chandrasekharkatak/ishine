package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class GetReporteesTimesheetReqDTO {

	private Long timesheetId;
    private Long empId;
    private String employeeName;
    private String dayType;
    private String date;
    private Boolean isNightShift;
    private String workCheckIn;
    private String workCheckOut;

    private List<GetReporteesTimesheetLocationsDTO> locationSessions = new ArrayList<>();
    private List<GetReporteesTimesheetDocsDTO> documentData = new ArrayList<>();
    
}
