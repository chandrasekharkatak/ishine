package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "timesheet_rejection_details_new")
public class TimesheetRejectionDetailsNew {

	@EmbeddedId
    private TimesheetRejectionDetailsId id;

    @Column(name = "remarks")
    private String remarks;
}
