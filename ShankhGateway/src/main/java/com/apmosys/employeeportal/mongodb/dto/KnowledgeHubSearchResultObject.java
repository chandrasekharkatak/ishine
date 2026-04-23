package com.apmosys.employeeportal.mongodb.dto;

import java.util.List;

import com.apmosys.employeeportal.dto.FormFieldDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeHubSearchResultObject {

    private String objectId;
    private String objectType;
    private String groupTitle;
    private String parentId;
    private String prefixPath;
    private List<KnowledgeHubSearchResultObjectField> knowledgeHubSearchResultObjectFields;
    private Long totalObjectOccurenceCount;

}
