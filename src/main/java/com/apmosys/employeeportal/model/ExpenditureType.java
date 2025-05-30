package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "expenditure_type")
@Getter
@Setter
public class ExpenditureType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "expenditure_type_name")
    private String expenditureTypeName;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active")
    private String isActive = "Y";

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private String updatedBy;
}
