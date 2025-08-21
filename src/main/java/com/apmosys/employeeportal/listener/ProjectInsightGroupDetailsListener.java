package com.apmosys.employeeportal.listener;

import java.util.List;
import java.util.Map;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightGroupDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectFlatSearch;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectFlatSearchRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.mongodb.repository.ProjectInsightGroupDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectDetailsRepository;

@Component
public class ProjectInsightGroupDetailsListener extends AbstractMongoEventListener<ProjectInsightGroupDetails> {

    @Autowired
    private ProjectInsightProjectFlatSearchRepository flatSearchRepo;

    @Autowired
    private ProjectInsightProjectDetailsRepository projectDetailsRepo;

    @Autowired
    private ProjectInsightGroupDetailsRepository groupDetailsRepo;

    @Override
    public void onAfterSave(AfterSaveEvent<ProjectInsightGroupDetails> event) {
        ProjectInsightGroupDetails details = event.getSource();

        // Fetch or create the flat search entity
        ProjectInsightProjectFlatSearch flatSearch = flatSearchRepo
                .findByParentId(details.getId())
                .orElseGet(ProjectInsightProjectFlatSearch::new);

        flatSearch.setParentId(details.getId());

        flatSearch.setPrefixPath(generatePath(details.getParentPathIds()));
        flatSearch.setParentIds(details.getParentPathIds());
        flatSearch.setType("group");

        flatSearch.setFlatSearchableText(generateFlatSearchableText(details).toLowerCase());

        flatSearchRepo.save(flatSearch);
        super.onAfterSave(event);
    }

    private String generatePath(List<String> parentPathIds) {
        if(parentPathIds == null ) return "";
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


    private String generateFlatSearchableText(ProjectInsightGroupDetails details) {
        StringBuilder sb = new StringBuilder();

        // Group title
        appendIfNotNull(sb, "groupTitle", details.getGroupTitle());

        // Additional info (can be nested objects)
        if (details.getAdditionalInfo() != null) {
            flattenValueWithKey("", details.getAdditionalInfo(), sb);
        }

        // Remove trailing delimiter if any
        String result = sb.toString().trim();
        if (result.endsWith("||")) {
            result = result.substring(0, result.length() - 2).trim();
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private void flattenValueWithKey(String prefix, Object value, StringBuilder sb) {
        if (value == null) return;

        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String newPrefix = prefix.isEmpty() ? entry.getKey() : prefix + "/" + entry.getKey();
                flattenValueWithKey(newPrefix, entry.getValue(), sb);
            }
        }
        else if (value instanceof Iterable) {
            Iterable<?> iterable = (Iterable<?>) value;
            StringBuilder listValues = new StringBuilder();
            for (Object item : iterable) {
                if (item != null) {
                    if (listValues.length() > 0) {
                        listValues.append(",");
                    }
                    listValues.append(cleanText(item.toString()));
                }
            }
            if (listValues.length() > 0) {
                sb.append(prefix).append(":").append(listValues).append(" || ");
            }
        }
        else if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            StringBuilder listValues = new StringBuilder();
            for (Object item : array) {
                if (item != null) {
                    if (listValues.length() > 0) {
                        listValues.append(",");
                    }
                    listValues.append(cleanText(item.toString()));
                }
            }
            if (listValues.length() > 0) {
                sb.append(prefix).append(":").append(listValues).append(" || ");
            }
        }
        else {
            sb.append(prefix).append(":").append(cleanText(value.toString())).append(" || ");
        }
    }

    private void appendIfNotNull(StringBuilder sb, String key, String text) {
        if (text != null && !text.trim().isEmpty()) {
            sb.append(key).append(":").append(cleanText(text)).append(" || ");
        }
    }

    private String cleanText(String input) {
        return input.replaceAll("[\\{\\}\\[\\]\\\"\\,\\:]", "")
                .replaceAll("\\s+", "")
                .trim();
    }
}
