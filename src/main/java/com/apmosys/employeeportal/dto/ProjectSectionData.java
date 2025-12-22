package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Map;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;

import lombok.Data;

@Data
public class ProjectSectionData {

    private ProjectInsightProjectDetails projectInsightProjectDetails;
    private List<FormFieldDTO> fields;
    private List<ProjectInsightQuestionDetails> questions;
    private Map<String, GroupSectionData> groups;
    private String createdBy;

}
