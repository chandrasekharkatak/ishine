package com.apmosys.employeeportal.listener;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectFlatSearch;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightGroupDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectFlatSearchRepository;

@Component
public class ProjectInsightQuestionDetailsListener extends AbstractMongoEventListener<ProjectInsightQuestionDetails> {

    @Autowired
    private ProjectInsightProjectFlatSearchRepository flatSearchRepo;

    @Autowired
    private ProjectInsightProjectDetailsRepository projectDetailsRepo;

    @Autowired
    private ProjectInsightGroupDetailsRepository groupDetailsRepo;

    @Override
    public void onAfterSave(AfterSaveEvent<ProjectInsightQuestionDetails> event) {
        ProjectInsightQuestionDetails details = event.getSource();

        // Check if flat search entry already exists for this Question
        ProjectInsightProjectFlatSearch flatSearch = flatSearchRepo
                .findByParentId(details.getId())
                .orElseGet(ProjectInsightProjectFlatSearch::new);

        // Link the original entity
        flatSearch.setParentId(details.getId());

        // Generate flat searchable text
        flatSearch.setPrefixPath(generatePath(details.getParentPathIds()));
        flatSearch.setParentIds(details.getParentPathIds());
        flatSearch.setFlatSearchableText(generateFlatSearchableText(details).toLowerCase());
        flatSearch.setType("question");

        // Save or update
        flatSearchRepo.save(flatSearch);

        super.onAfterSave(event);
    }

    private String generatePath(List<String> parentPathIds) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parentPathIds.size(); i++) {
            if (i == 0) {
                projectDetailsRepo.findById(parentPathIds.get(i))
                        .ifPresent(pd -> sb.append(pd.getProjectName()));
            } else {
                groupDetailsRepo.findById(parentPathIds.get(i))
                        .ifPresent(gd -> sb.append("/").append(gd.getGroupTitle()));
            }
        }
        return sb.toString();
    }

    private String generateFlatSearchableText(ProjectInsightQuestionDetails details) {
        StringBuilder sb = new StringBuilder();

        if (details.getQuestion() != null && !details.getQuestion().trim().isEmpty()) {
            sb.append("question:" +details.getQuestion().trim()).append(" || ");
        }

        if (details.getDescription() != null && !details.getDescription().trim().isEmpty()) {
            sb.append("question/description:" +details.getDescription().trim()).append(" || ");
        }

        // Remove extra spaces
        return sb.toString().trim().replaceAll("\\s+", " ");
    }
}
