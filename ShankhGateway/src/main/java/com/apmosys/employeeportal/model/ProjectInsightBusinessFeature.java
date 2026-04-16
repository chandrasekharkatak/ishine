package com.apmosys.employeeportal.model;

import javax.persistence.*;

import lombok.*;

@Data
@Entity
@Table(name = "project_insight_business_feature")
public class ProjectInsightBusinessFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String businessFeature;

    private Long serviceId;

}