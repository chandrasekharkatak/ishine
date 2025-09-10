package com.apmosys.employeeportal.mongodb.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeHubSearchResultWrapper {

    private List<KnowledgeHubSearchResultProject> knowledgeHubSearchResultProjectList;
    private Integer limit;
    private Integer skip;
    private Long totalCount;

}
