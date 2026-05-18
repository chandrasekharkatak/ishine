package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "employee_training_mapping")
public class EmployeeTrainingMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long trainingMappingId;

    @ManyToOne
    @JoinColumn(name = "training_id")
    private TrainingMaster trainingMaster;

    @Column(name = "emp_id")
    private Long empId;

    @Column(name = "active_status",
            nullable = false,
            columnDefinition = "VARCHAR(10) DEFAULT 'true'")
    private String activeStatus;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_on",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
            insertable = false,
            updatable = false)
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Timestamp createdOn;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_on")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Timestamp updatedOn;
}

