package com.apmosys.employeeportal.listener;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectFlatSearch;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightGroupDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectFlatSearchRepository;
import com.apmosys.employeeportal.utility.ValidationUtility;

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

        ProjectInsightProjectFlatSearch flatSearch = flatSearchRepo
                .findByParentId(details.getId())
                .orElseGet(ProjectInsightProjectFlatSearch::new);
        flatSearch.setParentId(details.getId());
        flatSearch.setPrefixPath(generatePath(details.getParentPathIds()));
        flatSearch.setParentIds(details.getParentPathIds());
        flatSearch.setType("question");
        flatSearch.setFacetCategoryIds(details.getFacetCategoryIds());
        flatSearch.setFacetValueIds(details.getFacetValueIds());
        flatSearch.setFlatSearchableText(generateFlatSearchableText(details).toLowerCase());
        // Save or update
        flatSearchRepo.save(flatSearch);
        super.onAfterSave(event);
    }

    private String generatePath(List<String> parentPathIds) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parentPathIds.size(); i++) {
            if (i == 0) {
                projectDetailsRepo.findById(parentPathIds.get(i))
                        .ifPresent(pd -> sb.append(pd.getProjectName().trim()));
            } else {
                groupDetailsRepo.findById(parentPathIds.get(i))
                        .ifPresent(gd -> sb.append(" / ").append(gd.getGroupTitle().trim()));
            }
        }
        return sb.toString().trim();
    }

    private String generateFlatSearchableText(ProjectInsightQuestionDetails questionDetails) {
        StringBuilder sb = new StringBuilder();

        if (ValidationUtility.isStringNotNullOrEmpty(questionDetails.getQuestion())) {
            sb.append(" " + questionDetails.getQuestion().trim()).append(" || ");
        }

        if (ValidationUtility.isStringNotNullOrEmpty(questionDetails.getDescription())) {
            sb.append(" " + questionDetails.getDescription().trim()).append(" || ");
        }

        // if (ValidationUtility.isListNotNullOrEmpty(questionDetails.getOptionsList())) {
        //     String options = questionDetails.getOptionsList()
        //             .stream()
        //             .map(opt -> opt.getOptionValue() != null ? opt.getOptionValue() : "")
        //             .collect(Collectors.joining(", "));
        //     sb.append(" ").append(options).append(" || ");
        // }

        String result = sb.toString().trim();
        if (result.endsWith("||")) {
            result = result.substring(0, result.length() - 2).trim();
        }
        return result;
    }
}
