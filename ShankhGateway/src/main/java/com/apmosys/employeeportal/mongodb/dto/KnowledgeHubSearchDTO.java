package com.apmosys.employeeportal.mongodb.dto;

import java.util.List;

import com.apmosys.employeeportal.dto.ProjectInsightFacetCategoryDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeHubSearchDTO {

    private String keyword;
    private Integer limit;
    private Integer skip;
    private boolean matchCase;
    private boolean exactMatch;
    private String regexPattern;
    private String options;
    private String projectId;
    private List<ProjectInsightFacetCategoryDTO> facetCategories;

}
