package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectFlatSearch;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectFlatSearchRepository;
import com.mongodb.BasicDBObject;

@Service
public class KnowledgeHubService {

    @Autowired
    private ProjectInsightProjectFlatSearchRepository flatSearchRepo;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Map<String, Object> getProject(String query, Integer limit, Integer skip) {
        query = query.toLowerCase().trim();
        String regexPattern = ".*" + Pattern.quote(query.toLowerCase()) + ".*";

        Query mongoQuery = new Query();
        mongoQuery.addCriteria(Criteria.where("flatSearchableText").regex(regexPattern, "i")).limit(limit).skip(skip);

        List<ProjectInsightProjectFlatSearch> matchedEntries = mongoTemplate.find(mongoQuery,
                ProjectInsightProjectFlatSearch.class);

        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, String> nameAndKeyMapping = new HashMap<>();
        Set<String> parentIdsSet = new HashSet<>();
        Set<String> names = new HashSet<>();
        
        for (ProjectInsightProjectFlatSearch entry : matchedEntries) {
            if(entry.getFlatSearchableText() != null) {
                // In case the entry.getParentIds() does not have its current id
                if(!entry.getParentIds().contains(entry.getParentId())) {
                    entry.getParentIds().add(entry.getParentId());
                }
                String flatText = entry.getFlatSearchableText();
                List<Map<String, Object>> parsedList = parseFlatSearchableText(flatText, query, entry.getParentIds(), entry.getType(), entry.getPrefixPath(), names, parentIdsSet);
                // List<Map<String, Object>> parsedList = parseFlatSearchableText(flatText, query, entry.getParentIds(), entry.getType());
                result.addAll(parsedList); 
            }
        }

        Map<String, Object> resultMap = new LinkedHashMap<>();

        long matchedCount = mongoTemplate.count(
            new Query(Criteria.where("flatSearchableText").regex(regexPattern, "i")),
            ProjectInsightProjectFlatSearch.class
        );

        Map<String, Map<String, String>> labelMap = getLabelsByParentAndNames(new ArrayList<>(parentIdsSet), new ArrayList<>(names));

        for(Map<String, Object> obj: result){
            // String parentId = (String) obj.get("parentIds");
            // String fieldName = (String) obj.get("key");
            // String label = labelMap.get(parentId).get("k").equalsIgnoreCase(fieldName) ? labelMap.get(parentId).get("v") : fieldName;
            // obj.put("label", label);
            List<String> parentIds = (List<String>) obj.get("parentIds");
            String currParentId = parentIds.get(parentIds.size() - 1);

            String fieldName = (String) obj.get("key");
            if(labelMap.containsKey(currParentId)) {
                if(labelMap.get(currParentId).containsKey(fieldName)) {
                    String label = labelMap.get(currParentId).get(fieldName);
                    obj.put("key", label);
                }
            }
        }

        resultMap.put("size", matchedCount);

        resultMap.put("data", result);

        return resultMap;
    }

    // public Map<String, Map<String, String>> getLabelsByParentAndNames(
    //     List<String> parentIds, List<String> names) {

    //     MatchOperation matchParent = Aggregation.match(
    //             Criteria.where("parentId").in(parentIds)
    //     );

    //     UnwindOperation unwindFields = Aggregation.unwind("fields");

    //     MatchOperation matchNames = Aggregation.match(
    //             Criteria.where("fields.name").in(names)
    //     );

    //     GroupOperation groupByParentAndName = Aggregation.group("parentId", "fields.name")
    //             .first("fields.label").as("label");

    //     GroupOperation groupByParent = Aggregation.group("_id.parentId")
    //         .push(
    //             new Document("k", "$_id.fields.name")
    //                 .append("v", "$label")
    //         ).as("nameLabelMap");

    //     ProjectionOperation project = Aggregation.project()
    //         .and("_id").as("parentId")
    //         .and("nameLabelMap").as("mappings")  
    //         .andExclude("_id");


    //     Aggregation aggregation = Aggregation.newAggregation(
    //             matchParent,
    //             unwindFields,
    //             matchNames,
    //             groupByParentAndName,
    //             groupByParent,
    //             project
    //     );

    //     AggregationResults<Document> results = mongoTemplate.aggregate(
    //             aggregation,
    //             "project_insight_form_details",
    //             Document.class
    //     );

    //     Map<String, Map<String, String>> finalResult = new HashMap<>();

    //     for (Document doc : results.getMappedResults()) {
    //         String parentId = doc.getString("parentId");

    //         // mappings is actually a List<Document>, not a Document
    //         List<Document> mappingsArray = (List<Document>) doc.get("mappings");

    //         Map<String, String> nameLabelMap = new HashMap<>();
    //         for (Document mapping : mappingsArray) {
    //             nameLabelMap.put(mapping.getString("k"), mapping.getString("v"));
    //         }

    //         finalResult.put(parentId, nameLabelMap);
    //     }

    //     return finalResult;
    // }

    public Map<String, Map<String, String>> getLabelsByParentAndNames(
        List<String> parentIds, List<String> names) {

        MatchOperation matchParent = Aggregation.match(
                Criteria.where("parentId").in(parentIds)
        );

        UnwindOperation unwindFields = Aggregation.unwind("fields");

        MatchOperation matchNames = Aggregation.match(
                Criteria.where("fields.name").in(names)
        );

        // Modified group operation to include fieldName
        GroupOperation groupByParentAndName = Aggregation.group("parentId", "fields.name")
                .first("fields.name").as("fieldName")
                .first("fields.label").as("label");

        GroupOperation groupByParent = Aggregation.group("_id.parentId")
                .push(
                    new Document("k", "$fieldName")  // Use the alias
                        .append("v", "$label")
                ).as("nameLabelMap");

        ProjectionOperation project = Aggregation.project()
            .and("_id").as("parentId")
            .and("nameLabelMap").as("mappings")  
            .andExclude("_id");

        Aggregation aggregation = Aggregation.newAggregation(
                matchParent,
                unwindFields,
                matchNames,
                groupByParentAndName,
                groupByParent,
                project
        );

        // Add debug output to see what the aggregation produces
        System.out.println("Aggregation: " + aggregation.toString());
        
        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation,
                "project_insight_form_details",
                Document.class
        );

        // Debug: print raw results
        for (Document doc : results.getMappedResults()) {
            System.out.println("Result doc: " + doc.toJson());
        }

        Map<String, Map<String, String>> finalResult = new HashMap<>();

        for (Document doc : results.getMappedResults()) {
            String parentId = doc.getString("parentId");
            List<Document> mappingsArray = (List<Document>) doc.get("mappings");

            Map<String, String> nameLabelMap = new HashMap<>();
            for (Document mapping : mappingsArray) {
                nameLabelMap.put(mapping.getString("k"), mapping.getString("v"));
            }

            finalResult.put(parentId, nameLabelMap);
        }

        return finalResult;
    }

    private List<Map<String, Object>> parseFlatSearchableText(String flatText, String query, List<String> parentIds, String type, String parentPath, Set<String> names, Set<String> parentIdsSet) {
        List<Map<String, Object>> list = new ArrayList<>();

        String[] pairs = flatText.split("\\s*\\|\\|\\s*");

        for (String pair : pairs) {
            if (pair.isEmpty())
                continue;

            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2)
                continue;

            String key = keyValue[0].trim();
            String valueStr = keyValue[1].trim();

            if (valueStr.contains(",")) {
                // Multiple values, create one map per value
                for (String v : valueStr.split(",")) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    if (v.contains(query)) {
                        names.add(key);
                        parentIdsSet.add(parentIds.get(parentIds.size() - 1));
                        map.put("path", parentPath.trim() + "/");
                        map.put("key", key);
                        map.put("value", v.trim());
                        map.put("parentIds", parentIds);
                        map.put("type", type);
                        list.add(map);
                    }
                }
            } else {
                Map<String, Object> map = new LinkedHashMap<>();
                if (valueStr.contains(query)) {
                    names.add(key);
                    parentIdsSet.add(parentIds.get(parentIds.size() - 1));
                    map.put("path", parentPath.trim() + "/");
                    map.put("key", key);
                    map.put("value", valueStr);
                    map.put("parentIds", parentIds);
                    map.put("type", type);
                    list.add(map);
                }
            }
        }

        return list;
    }

}