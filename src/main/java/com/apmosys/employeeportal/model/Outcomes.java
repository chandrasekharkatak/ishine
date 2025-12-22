package com.apmosys.employeeportal.model;

import javax.persistence.*;

import lombok.Data;

@Data
@Entity
@Table(name = "outcomes")
public class Outcomes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "outcome")
    private String outcome;

}