package com.apmosys.employeeportal.mongodb.dto;

import java.util.Map;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightFormDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightGroupDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightResponseDetails;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectInsightDetailsMapDTO {
    private Map<String, ProjectInsightFormDetails> formDetailsMap;
    private Map<String, ProjectInsightGroupDetails> groupMap;
    private Map<String, ProjectInsightQuestionDetails> questionMap;
    private Map<String, ProjectInsightResponseDetails> responseMap;
    private Map<String, ProjectInsightProjectDetails> projectMap;

    public ProjectInsightDetailsMapDTO(
            Map<String, ProjectInsightFormDetails> formDetailsMap,
            Map<String, ProjectInsightProjectDetails> projectMap,
            Map<String, ProjectInsightGroupDetails> groupMap,
            Map<String, ProjectInsightQuestionDetails> questionMap,
            Map<String, ProjectInsightResponseDetails> responseMap) {
        this.formDetailsMap = formDetailsMap;
        this.projectMap = projectMap;
        this.groupMap = groupMap;
        this.questionMap = questionMap;
        this.responseMap = responseMap;
    }
}
