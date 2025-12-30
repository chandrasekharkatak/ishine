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
public class TimesheetActivityMapId implements Serializable {

    @Column(name = "timesheet_id", nullable = false)
    private Long timesheetId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    // equals & hashCode (MANDATORY for composite key)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimesheetActivityMapId)) return false;
        TimesheetActivityMapId that = (TimesheetActivityMapId) o;
        return Objects.equals(timesheetId, that.timesheetId)
                && Objects.equals(activityId, that.activityId)
                && Objects.equals(projectId, that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timesheetId, activityId, projectId);
    }
}
