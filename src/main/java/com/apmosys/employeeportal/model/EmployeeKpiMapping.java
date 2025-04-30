package com.apmosys.employeeportal.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "employee_kpi_mapping")
public class EmployeeKpiMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long empId;  // Employee ID for the mapping

    private Long quarterId;
    private Long departmentId;

    private String name;
    private String description;
    
    private Long kpiMappingId;
    // One mapping has many KPIs
    @OneToMany(mappedBy = "employeeKpiMapping", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeKpis> kpis = new ArrayList<>();

    private String approvedBy;
    private String createdBy;

    private String department;
    private Long managerRating;
    private String managerRemark;

    private String employeeRole;
}