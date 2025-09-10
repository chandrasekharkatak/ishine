package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.FormFieldDTO;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightFormDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightGroupDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectFlatSearch;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightGroupDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectFlatSearchRepository;
import com.apmosys.employeeportal.utility.ValidationUtility;

@Service
public class ProjectInsightFlatSearchDetailsService {

    @Autowired
    private ProjectInsightProjectFlatSearchRepository projectInsightProjectFlatSearchRepository;

    @Autowired
    private ProjectInsightProjectDetailsRepository projectInsightProjectDetailsRepository;

    @Autowired
    private ProjectInsightGroupDetailsRepository projectInsightGroupDetailsRepository;

    @Async
    public void saveProjectInsightFlatSearchDetailsProjectOrGroup(ProjectInsightFormDetails formDetails) {
        try {
            if (formDetails.getParentType().equalsIgnoreCase("project")) {
                saveProjectInsightProjectFlatSearchObject(formDetails);
            } else if (formDetails.getParentType().equalsIgnoreCase("group")) {
                saveProjectInsightGroupFlatSearchObject(formDetails);
            } else {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveProjectInsightProjectFlatSearchObject(ProjectInsightFormDetails formDetails) {

        if (formDetails == null) {
            return;
        }

        Optional<ProjectInsightProjectDetails> projectDetails = projectInsightProjectDetailsRepository
                .findById(formDetails.getParentId());
        if (projectDetails == null || projectDetails.isEmpty()) {
            return;
        }
        ProjectInsightProjectDetails projectDetailsObj = projectDetails.get();

        ProjectInsightProjectFlatSearch flatSearch = projectInsightProjectFlatSearchRepository
                .findByParentId(projectDetailsObj.getId())
                .orElseGet(ProjectInsightProjectFlatSearch::new);
        flatSearch.setParentId(projectDetailsObj.getId());
        flatSearch.setPrefixPath(projectDetailsObj.getProjectName());
        List<String> parentIds = new ArrayList<>();
        parentIds.add(projectDetailsObj.getId());
        flatSearch.setParentIds(parentIds);
        flatSearch.setType("project");

        StringBuilder sb = new StringBuilder();

        if (ValidationUtility.isStringNotNullOrEmpty(projectDetailsObj.getProjectName())) {
            sb.append(" " + projectDetailsObj.getProjectName()).append(" || ");
        }
        if (ValidationUtility.isStringNotNullOrEmpty(projectDetailsObj.getProjectManagerName())) {
            sb.append(" " + projectDetailsObj.getProjectManagerName()).append(" || ");
        }
        if (projectDetailsObj.getClient() != null && projectDetailsObj.getClient().getClientName() != null) {
            sb.append(" " + projectDetailsObj.getClient().getClientName()).append(" || ");
        }

        if (ValidationUtility.isStringNotNullOrEmpty(projectDetailsObj.getApmosysRM())) {
            sb.append(" " + projectDetailsObj.getApmosysRM()).append(" || ");
        }

        if (ValidationUtility.isStringNotNullOrEmpty(projectDetailsObj.getClientRM())) {
            sb.append(" " + projectDetailsObj.getClientRM()).append(" || ");
        }
        if (ValidationUtility.isListNotNullOrEmpty(projectDetailsObj.getDepartments())) {
            String deptNames = projectDetailsObj.getDepartments()
                    .stream()
                    .map(d -> d.getName() != null ? d.getName() : "")
                    .collect(Collectors.joining(", "));
            sb.append(" ").append(deptNames).append(" || ");
        }

        Map<String, Object> additiionalInfoMap = null;
        if (ValidationUtility.isListNotNullOrEmpty(formDetails.getFields())) {
            additiionalInfoMap = mapLabelToValue(projectDetailsObj.getAdditionalInfo(), formDetails.getFields());
        }
        sb.append(generateFlatSearchableTextFromMap(additiionalInfoMap).toLowerCase());
        flatSearch.setFlatSearchableText(sb.toString());
        projectInsightProjectFlatSearchRepository.save(flatSearch);
    }

    private void saveProjectInsightGroupFlatSearchObject(ProjectInsightFormDetails formDetails) {
        Optional<ProjectInsightGroupDetails> groupDetails = projectInsightGroupDetailsRepository
                .findById(formDetails.getParentId());
        if (groupDetails == null || groupDetails.isEmpty()) {
            return;
        }
        ProjectInsightGroupDetails groupDetailsObj = groupDetails.get();

        ProjectInsightProjectFlatSearch flatSearch = projectInsightProjectFlatSearchRepository
                .findByParentId(groupDetailsObj.getId())
                .orElseGet(ProjectInsightProjectFlatSearch::new);
        flatSearch.setParentId(groupDetailsObj.getId());
        flatSearch.setPrefixPath(generatePath(groupDetailsObj.getParentPathIds()));
        flatSearch.setParentIds(groupDetailsObj.getParentPathIds());
        flatSearch.setType("group");

        StringBuilder sb = new StringBuilder();
        if (groupDetailsObj.getGroupTitle() != null) {
            sb.append(" " + groupDetailsObj.getGroupTitle()).append(" || ");
        }

        Map<String, Object> additiionalInfoMap = null;
        if (ValidationUtility.isListNotNullOrEmpty(formDetails.getFields())) {
            additiionalInfoMap = mapLabelToValue(groupDetailsObj.getAdditionalInfo(), formDetails.getFields());
        }
        sb.append(generateFlatSearchableTextFromMap(additiionalInfoMap).toLowerCase());
        flatSearch.setFlatSearchableText(sb.toString());
        projectInsightProjectFlatSearchRepository.save(flatSearch);
    }

    public Map<String, Object> mapLabelToValue(Map<String, Object> additionalInfo, List<FormFieldDTO> formFields) {
        if (additionalInfo == null || additionalInfo.isEmpty() || formFields == null) {
            return Collections.emptyMap();
        }
        return formFields.stream()
                .filter(field -> additionalInfo.containsKey(field.getName()))
                .collect(Collectors.toMap(
                        FormFieldDTO::getLabel, // key = label
                        field -> additionalInfo.get(field.getName()) // value = additionalInfo value
                ));
    }

    private String generateFlatSearchableTextFromMap(Map<String, Object> additionalInfo) {
        if (additionalInfo == null || additionalInfo.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        flattenValueWithKey("", additionalInfo, sb);
        String result = sb.toString().trim();
        if (result.endsWith("||")) {
            result = result.substring(0, result.length() - 2).trim();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void flattenValueWithKey(String prefix, Object value, StringBuilder sb) {
        if (value == null) {
            return;
        }

        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String newPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "/" + entry.getKey();
                flattenValueWithKey(newPrefix, entry.getValue(), sb);
            }
        } else if (value instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) value;
            StringBuilder listValues = new StringBuilder();
            for (Object item : iterable) {
                if (item != null) {
                    if (listValues.length() > 0) {
                        listValues.append(", ");
                    }
                    listValues.append(item.toString().trim());
                }
            }
            if (listValues.length() > 0) {
                sb.append(prefix).append(" : ").append(listValues).append(" || ");
            }
        } else if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            StringBuilder listValues = new StringBuilder();
            for (Object item : array) {
                if (item != null) {
                    if (listValues.length() > 0) {
                        listValues.append(", ");
                    }
                    listValues.append(item.toString().trim());
                }
            }
            if (listValues.length() > 0) {
                sb.append(prefix).append(" : ").append(listValues).append(" || ");
            }
        } else {
            sb.append(prefix).append(" : ").append(value.toString().trim()).append(" || ");
        }
    }

    private String generatePath(List<String> parentPathIds) {
        if (parentPathIds == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parentPathIds.size(); i++) {
            if (i == 0) {
                projectInsightProjectDetailsRepository.findById(parentPathIds.get(i))
                        .ifPresent(pd -> sb.append(pd.getProjectName().trim()));
            } else {
                projectInsightGroupDetailsRepository.findById(parentPathIds.get(i))
                        .ifPresent(gd -> sb.append(" / ").append(gd.getGroupTitle().trim()));
            }
        }
        return sb.toString().trim();
    }

}
