package com.apmosys.employeeportal.model;

import lombok.*;
import javax.persistence.*;

@Data
@Entity
@Table(name = "project_insight_feature")
public class ProjectInsightFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String featureName;

    private String parentFeatureId;

    private String BusinessFeatureId;

    
}