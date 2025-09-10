// package com.apmosys.employeeportal.listener;

// import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectDetails;
// import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectFlatSearch;
// import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectFlatSearchRepository;

// import lombok.RequiredArgsConstructor;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
// import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
// import org.springframework.stereotype.Component;

// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;

// @Component
// @RequiredArgsConstructor
// public class ProjectInsightProjectDetailsListener extends AbstractMongoEventListener<ProjectInsightProjectDetails> {

//     @Autowired
//     private ProjectInsightProjectFlatSearchRepository flatSearchRepo;

//     @Override
//     public void onAfterSave(AfterSaveEvent<ProjectInsightProjectDetails> event) {
//         ProjectInsightProjectDetails details = event.getSource();

//        ProjectInsightProjectFlatSearch flatSearch =
//         flatSearchRepo.findByParentId(details.getId())
//             .orElseGet(ProjectInsightProjectFlatSearch::new);


//         flatSearch.setParentId(details.getId());
//         flatSearch.setFlatSearchableText(generateFlatSearchableText(details).toLowerCase());
//         flatSearch.setPrefixPath(details.getProjectName());
//         flatSearch.setType("project");
//         flatSearch.setParentIds(List.of(details.getId()));

//         flatSearchRepo.save(flatSearch);
//         super.onAfterSave(event);
//     }

//     private String generateFlatSearchableText(ProjectInsightProjectDetails details) {
//         StringBuilder sb = new StringBuilder();

//         if (details.getProjectName() != null) {
//             sb.append("projectname:"+details.getProjectName()).append(" || ");
//         }
//         if (details.getProjectManagerName() != null) {
//             sb.append("projectmanager:"+details.getProjectManagerName()).append(" || ");
//         }
//         if (details.getClient() != null && details.getClient().getClientName() != null) {
//             sb.append("client:"+details.getClient().getClientName()).append(" || ");
//         }

//         if(details.getApmosysRM() != null && details.getApmosysRM() != null) {
//             sb.append("apmosysRM:"+details.getApmosysRM()).append(" || ");
//         }

//         if(details.getClientRM() != null && details.getClientRM() != null) {
//             sb.append("clientRM:"+details.getClientRM()).append(" || ");
//         }

//         if (details.getDepartments() != null) {
//             String deptNames = details.getDepartments()
//                     .stream()
//                     .map(d -> d.getName() != null ? d.getName() : "")
//                     .collect(Collectors.joining(","));
//             sb.append("departments:").append(deptNames).append(" || ");
//         }
//         if (details.getAdditionalInfo() != null) {
//             String additional = flattenAdditionalInfo(details.getAdditionalInfo());
//             sb.append(additional).append(" ");
//         }

//         return sb.toString().trim().toLowerCase();
//     }

//     private String flattenAdditionalInfo(Map<String, Object> map) {
//         StringBuilder sb = new StringBuilder();
//         flattenMapRecursively(map, sb);
//         return sb.toString().trim();
//     }

//     private void flattenMapRecursively(Map<String, Object> map, StringBuilder sb) {
//         for (Map.Entry<String, Object> entry : map.entrySet()) {
//             Object value = entry.getValue();

//             // Check if value is a map containing only "id" and "name"
//             if (value instanceof Map) {
//                 Map<String, Object> innerMap = (Map<String, Object>) value;
//                 if (innerMap.containsKey("id") && innerMap.containsKey("name") && innerMap.size() == 2) {
//                     sb.append(entry.getKey()).append(": ").append(innerMap.get("name")).append(" || ");
//                 } else {
//                     sb.append(entry.getKey()).append(": ");
//                     flattenMapRecursively(innerMap, sb);
//                 }
//             } 
//             else if (value instanceof List) {
//                 sb.append(entry.getKey()).append(": ");
//                 ((List<?>) value).forEach(item -> {
//                     if (item instanceof Map) {
//                         Map<String, Object> itemMap = (Map<String, Object>) item;
//                         if (itemMap.containsKey("id") && itemMap.containsKey("name") && itemMap.size() == 2) {
//                             sb.append(entry.getKey()).append(": ").append(itemMap.get("name")).append(" || ");
//                         } else {
//                             sb.append(entry.getKey()).append(": ");
//                             flattenMapRecursively(itemMap, sb);
//                         }
//                     } else {
//                         // sb.append(item).append(" || ");
//                         sb.append(entry.getKey()).append(": ").append(item).append(" || ");
//                     }
//                 });
//             } 
//             else {
//                 sb.append(entry.getKey()).append(": ").append(value).append(" || ");
//             }
//         }
//     }

// }
