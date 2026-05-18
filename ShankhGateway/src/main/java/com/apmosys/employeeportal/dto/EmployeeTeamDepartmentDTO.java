package com.apmosys.employeeportal.dto;

import java.sql.Date;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeTeamDepartmentDTO {
   private Long empId;
   private Long teamId;
   private LocalDate date;
}
