package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.ToString;

@Data
@Entity
@ToString
public class PoRequirementMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long poRequirementMappingId;
    private Long poId;
    private String role;
    private Long clientRoleId;
    private String experience;
    private String department;
    private boolean active;
    private Long count;

}