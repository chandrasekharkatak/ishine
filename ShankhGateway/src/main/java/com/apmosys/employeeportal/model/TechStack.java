package com.apmosys.employeeportal.model;

import javax.persistence.*;

import lombok.Data;

@Entity
@Table(name = "tech_stack")
@Data
public class TechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String stack;

}