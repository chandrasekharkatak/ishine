package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "employee_kpis")
public class 
EmployeeKpis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private String review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kpi_mapping_id") 
    private EmployeeKpiMapping employeeKpiMapping;
    
    private Long progress;
}