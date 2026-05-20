package com.apmosys.employeeportal.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PendingTimesheetDTO {

    private Long pendingCount;
    private List<String> pendingProjects;
    private LocalDate checkDate;

}
