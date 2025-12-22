package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "project_insight_facet_value")
@NoArgsConstructor
public class ProjectInsightFacetValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long facetValueId;

    private String facetValue;

    private Long facetCategoryId;

    public ProjectInsightFacetValue(String facetValue, Long facetCategoryId) {
        this.facetValue = facetValue;
        this.facetCategoryId = facetCategoryId;
    }
}
