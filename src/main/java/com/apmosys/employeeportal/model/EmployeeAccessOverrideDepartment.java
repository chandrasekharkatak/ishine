package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class EmployeeAccessOverrideDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long overrideDeptId;
    
    private Long overrideId;
    
    private Long deptId;

    private boolean isActive;

    private String featureName;

    private String subFeatureName;
}
