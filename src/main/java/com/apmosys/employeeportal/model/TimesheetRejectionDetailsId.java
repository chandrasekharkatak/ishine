package com.apmosys.employeeportal.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class TimesheetRejectionDetailsId implements Serializable{

	@Column(name = "timesheet_id")
    private Long timesheetId;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "rejection_id")
    private Long rejectionId;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimesheetRejectionDetailsId)) return false;
        TimesheetRejectionDetailsId that = (TimesheetRejectionDetailsId) o;
        return Objects.equals(timesheetId, that.timesheetId) &&
               Objects.equals(projectId, that.projectId) &&
               Objects.equals(rejectionId, that.rejectionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timesheetId, projectId, rejectionId);
    }
}
