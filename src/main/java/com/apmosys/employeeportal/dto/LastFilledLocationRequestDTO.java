package com.apmosys.employeeportal.dto;

public class LastFilledLocationRequestDTO {
    private Integer projectId;
    private Long empId;

    // Getters and Setters
    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }

    public Long getEmpId() { return empId; }
    public void setEmpId(Long empId) { this.empId = empId; }

}
