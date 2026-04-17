package com.apmosys.employeeportal.mongodb.modal;

import java.util.List;

import javax.persistence.Id;
import javax.persistence.Transient;

import org.springframework.data.mongodb.core.mapping.Document;

import com.apmosys.employeeportal.model.ProjectInsightFacetCategory;
import com.apmosys.employeeportal.mongodb.dto.OptionValueDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "project_insight_question_library")

public class ProjectInsightQuestionLibraryEntry {

    @Id
    private String id;
    private String question;
    private String normalizedQuestion;
    private String description;
    private String optionType;
    private List<Long> deptIds;
    private List<String> depts;
    private List<OptionValueDTO> optionsList;

    private Long createdBy;
    private String createdByName;
    private String createdOn;
    private Long updatedBy;
    private String updatedByName;
    private String updatedOn;

    @Transient
	private List<ProjectInsightFacetCategory> facetCategoryList;
    private List<Long> facetCategoryIds;
    private List<Long> facetValueIds;

    public void setQuestion(String question) {
        this.question = question;
        if (question != null) {
            this.normalizedQuestion = question
                    .replaceAll("[^a-zA-Z0-9]", "")
                    .toLowerCase()
                    .trim();
        }
    }
}
