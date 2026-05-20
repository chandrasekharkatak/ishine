package com.apmosys.employeeportal.mongodb.modal;

import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
public class ProjectInsightQuestionLibraryQuestionNormalizer
        implements BeforeConvertCallback<ProjectInsightQuestionLibraryEntry> {

    @Override
    public ProjectInsightQuestionLibraryEntry onBeforeConvert(ProjectInsightQuestionLibraryEntry entry,
            String collection) {
        if (entry.getQuestion() != null) {
            entry.setNormalizedQuestion(
                    entry.getQuestion()
                            .replaceAll("[^a-zA-Z0-9]", "")
                            .toLowerCase()
                            .trim());
        }
        return entry;
    }
}
