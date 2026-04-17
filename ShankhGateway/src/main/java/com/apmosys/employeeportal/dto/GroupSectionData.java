package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Map;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightGroupDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;

import lombok.Data;

@Data
public class GroupSectionData {

    private ProjectInsightGroupDetails projectInsightGroupDetails;
    private List<FormFieldDTO> fields;
    private List<ProjectInsightQuestionDetails> questions;
    private Map<String, GroupSectionData> subGroups;

}
