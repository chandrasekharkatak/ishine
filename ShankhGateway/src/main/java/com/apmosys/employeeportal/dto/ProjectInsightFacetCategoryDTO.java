package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectInsightFacetCategoryDTO {
    private Long facetCategoryId;
    private String categoryName;
    private String description;
    private List<ProjectInsightFacetValueDTO> projectInsightFacetValueDTOList;

    public ProjectInsightFacetCategoryDTO(Long facetCategoryId, String categoryName, String description) {
        this.facetCategoryId = facetCategoryId;
        this.categoryName = categoryName;
        this.description = description;
    }
}
