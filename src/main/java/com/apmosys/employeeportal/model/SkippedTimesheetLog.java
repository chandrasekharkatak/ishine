package com.apmosys.employeeportal.model;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "skipped_timesheet_log")
public class SkippedTimesheetLog {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timesheet_id")
    private Long timesheetId;

    @Column(name = "emp_id")
    private Long empId;

    @Column(name = "timesheet_date")
    private LocalDate timesheetDate;

    private String reason;

    private LocalDateTime createdAt;

    private Long createdBy;
}
