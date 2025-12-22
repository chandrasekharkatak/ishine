package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name = "project_insight_facet_category")
public class ProjectInsightFacetCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long facetCategoryId;

    private String categoryName;

    private String description;

}
