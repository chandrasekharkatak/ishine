package com.apmosys.employeeportal.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ProjectTimesheetStatusId {

	@Column(name="timesheet_id")
	private Long timesheetId;
	@Column(name="project_id")
    private Long projectId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectTimesheetStatusId)) return false;
        ProjectTimesheetStatusId that = (ProjectTimesheetStatusId) o;
        return Objects.equals(timesheetId, that.timesheetId)
                && Objects.equals(projectId, that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timesheetId, projectId);
    }
}
