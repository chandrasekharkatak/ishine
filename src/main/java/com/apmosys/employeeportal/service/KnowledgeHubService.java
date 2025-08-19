package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectFlatSearch;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectFlatSearchRepository;

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

        for (ProjectInsightProjectFlatSearch entry : matchedEntries) {
            String flatText = entry.getFlatSearchableText();
            List<Map<String, Object>> parsedList = parseFlatSearchableText(flatText, query, entry.getParentIds(),
                    entry.getType());
            result.addAll(parsedList); // add all from each document into one flat list
        }

        Map<String, Object> resultMap = new LinkedHashMap<>();

        resultMap.put("size", Long.toString(flatSearchRepo.count()));

        resultMap.put("data", result);

        return resultMap;
    }

    private List<Map<String, Object>> parseFlatSearchableText(String flatText, String query, List<String> parentIds,
            String type) {
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
                        map.put("path", key);
                        map.put("value", v.trim());
                        map.put("parentIds", parentIds);
                        map.put("type", type);
                        list.add(map);
                    }
                }
            } else {
                Map<String, Object> map = new LinkedHashMap<>();
                if (valueStr.contains(query)) {
                    map.put("path", key);
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