package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Audited
@Table(name = "employee_timesheet_location_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeTimesheetLocationMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_mapping_id")
    private Long locationMappingId;

    @Column(name = "timesheet_id", nullable = false)
    private Long timesheetId;

    @Column(name = "location_type_id", nullable = false)
    private Integer locationTypeId;

    @Column(name = "location_in_time")
    private LocalDateTime locationInTime;

    @Column(name = "location_out_time")
    private LocalDateTime locationOutTime;
}
