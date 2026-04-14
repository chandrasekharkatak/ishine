package com.apmosys.employeeportal.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectFlatSearch;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightResponseDetails;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectFlatSearchRepository;
import com.apmosys.employeeportal.utility.ValidationUtility;

@Component
public class ProjectInsightResponseDetailsListener extends AbstractMongoEventListener<ProjectInsightResponseDetails> {

    @Autowired
    private ProjectInsightProjectFlatSearchRepository flatSearchRepo;

    @Override
    public void onAfterSave(AfterSaveEvent<ProjectInsightResponseDetails> event) {
        ProjectInsightResponseDetails details = event.getSource();

        ProjectInsightProjectFlatSearch flatSearch = flatSearchRepo
                .findByParentId(details.getId())
                .orElseGet(ProjectInsightProjectFlatSearch::new);
        flatSearch.setParentId(details.getId());
        flatSearch.setType("response");
        flatSearch.setFacetCategoryIds(details.getFacetCategoryIds());
        flatSearch.setFacetValueIds(details.getFacetValueIds());
        flatSearch.setFlatSearchableText(generateFlatSearchableText(details).toLowerCase());
        // Save or update
        flatSearchRepo.save(flatSearch);
        super.onAfterSave(event);
    }

    private String generateFlatSearchableText(ProjectInsightResponseDetails response) {
        StringBuilder sb = new StringBuilder();

        if (ValidationUtility.isStringNotNullOrEmpty(response.getResponse())) {
            sb.append(" " + response.getResponse().trim()).append(" || ");
        }

        // if (ValidationUtility.isListNotNullOrEmpty(questionDetails.getOptionsList()))
        // {
        // String options = questionDetails.getOptionsList()
        // .stream()
        // .map(opt -> opt.getOptionValue() != null ? opt.getOptionValue() : "")
        // .collect(Collectors.joining(", "));
        // sb.append(" ").append(options).append(" || ");
        // }

        String result = sb.toString().trim();
        if (result.endsWith("||")) {
            result = result.substring(0, result.length() - 2).trim();
        }
        return result;
    }
}
