package com.apmosys.employeeportal.dto;

import com.apmosys.employeeportal.controller.ProjectStructureRequest;

public class ProjectStructureWrapper {
    private ProjectStructureRequest projectStructure;
    private ProjectFilterDTO projectFilter;

    public ProjectStructureRequest getProjectStructure() {
        return projectStructure;
    }
    public void setProjectStructure(ProjectStructureRequest projectStructure) {
        this.projectStructure = projectStructure;
    }

    public ProjectFilterDTO getProjectFilter() {
        return projectFilter;
    }
    public void setProjectFilter(ProjectFilterDTO projectFilter) {
        this.projectFilter = projectFilter;
    }
}

