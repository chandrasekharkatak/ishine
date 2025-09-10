package com.apmosys.employeeportal.mongodb.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeHubSearchResultProject {

    private String projectId;
    private String projectName;
    private List<KnowledgeHubSearchResultObject> knowledgeHubSearchResultObjectList;
    private List<KnowledgeHubSearchResultObjectField> knowledgeHubSearchResultObjectFields;
    private Integer limit;
    private Integer skip;
    private Long totalGroupCount;
    private Long totalProjectOccurenceCount;
}
