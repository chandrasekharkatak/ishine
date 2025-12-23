package com.apmosys.employeeportal.mongodb.dto;

import java.util.List;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionLibraryEntry;

import lombok.Data;

@Data
public class ProjectInsightQuestionLibraryEntryRequest {

    private List<ProjectInsightQuestionLibraryEntry> projectInsightQuestionLibraryEntryList;
    private Long createdBy;

}
