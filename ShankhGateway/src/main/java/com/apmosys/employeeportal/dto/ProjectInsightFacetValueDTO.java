package com.apmosys.employeeportal.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectInsightFacetValueDTO {
    private Long facetValueId;
    private String facetValue;
    private Long facetCategoryId;

    public ProjectInsightFacetValueDTO(Long facetValueId, String facetValue, Long facetCategoryId) {
        this.facetValue = facetValue;
        this.facetValueId = facetValueId;
        this.facetCategoryId = facetCategoryId;
    }
}
