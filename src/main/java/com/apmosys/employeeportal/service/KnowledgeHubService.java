package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SkipOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.FormFieldDTO;
import com.apmosys.employeeportal.mongodb.dto.DepartmentDTO;
import com.apmosys.employeeportal.mongodb.dto.FlatSearchResultDTO;
import com.apmosys.employeeportal.mongodb.dto.KnowledgeHubSearchDTO;
import com.apmosys.employeeportal.mongodb.dto.KnowledgeHubSearchResultObject;
import com.apmosys.employeeportal.mongodb.dto.KnowledgeHubSearchResultObjectField;
import com.apmosys.employeeportal.mongodb.dto.KnowledgeHubSearchResultProject;
import com.apmosys.employeeportal.mongodb.dto.KnowledgeHubSearchResultWrapper;
import com.apmosys.employeeportal.mongodb.dto.ParentCountDTO;
import com.apmosys.employeeportal.mongodb.dto.ParentCountResponseDTO;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightFormDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightGroupDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectFlatSearch;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightResponseDetails;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightFormDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightGroupDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightQuestionDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightResponseDetailsRepository;
import com.apmosys.employeeportal.utility.ValidationUtility;

@Service
public class KnowledgeHubService {

    @Autowired
    private ProjectInsightProjectDetailsRepository projectInsightProjectDetailsRepository;

    @Autowired
    private ProjectInsightGroupDetailsRepository projectInsightGroupDetailsRepository;

    @Autowired
    private ProjectInsightFormDetailsRepository projectInsightFormDetailsRepository;

    @Autowired
    private ProjectInsightQuestionDetailsRepository projectInsightQuestionDetailsRepository;

    @Autowired
    private ProjectInsightResponseDetailsRepository projectInsightResponseDetailsRepository;

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
            if (entry.getFlatSearchableText() != null) {
                // In case the entry.getParentIds() does not have its current id
                if (!entry.getParentIds().contains(entry.getParentId())) {
                    entry.getParentIds().add(entry.getParentId());
                }
                String flatText = entry.getFlatSearchableText();
                List<Map<String, Object>> parsedList = parseFlatSearchableText(flatText, query, entry.getParentIds(),
                        entry.getType(), entry.getPrefixPath(), names, parentIdsSet);
                // List<Map<String, Object>> parsedList = parseFlatSearchableText(flatText,
                // query, entry.getParentIds(), entry.getType());
                result.addAll(parsedList);
            }
        }

        Map<String, Object> resultMap = new LinkedHashMap<>();

        long matchedCount = mongoTemplate.count(
                new Query(Criteria.where("flatSearchableText").regex(regexPattern, "i")),
                ProjectInsightProjectFlatSearch.class);

        Map<String, Map<String, String>> labelMap = getLabelsByParentAndNames(new ArrayList<>(parentIdsSet),
                new ArrayList<>(names));

        for (Map<String, Object> obj : result) {
            // String parentId = (String) obj.get("parentIds");
            // String fieldName = (String) obj.get("key");
            // String label = labelMap.get(parentId).get("k").equalsIgnoreCase(fieldName) ?
            // labelMap.get(parentId).get("v") : fieldName;
            // obj.put("label", label);
            List<String> parentIds = (List<String>) obj.get("parentIds");
            String currParentId = parentIds.get(parentIds.size() - 1);

            String fieldName = (String) obj.get("key");
            if (labelMap.containsKey(currParentId)) {
                if (labelMap.get(currParentId).containsKey(fieldName)) {
                    String label = labelMap.get(currParentId).get(fieldName);
                    obj.put("key", label);
                }
            }
        }

        resultMap.put("size", matchedCount);

        resultMap.put("data", result);

        return resultMap;
    }

    public Map<String, Map<String, String>> getLabelsByParentAndNames(
            List<String> parentIds, List<String> names) {

        MatchOperation matchParent = Aggregation.match(
                Criteria.where("parentId").in(parentIds));

        UnwindOperation unwindFields = Aggregation.unwind("fields");

        MatchOperation matchNames = Aggregation.match(
                Criteria.where("fields.name").in(names));

        // Modified group operation to include fieldName
        GroupOperation groupByParentAndName = Aggregation.group("parentId", "fields.name")
                .first("fields.name").as("fieldName")
                .first("fields.label").as("label");

        GroupOperation groupByParent = Aggregation.group("_id.parentId")
                .push(
                        new Document("k", "$fieldName") // Use the alias
                                .append("v", "$label"))
                .as("nameLabelMap");

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
                project);

        // Add debug output to see what the aggregation produces
        System.out.println("Aggregation: " + aggregation.toString());

        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation,
                "project_insight_form_details",
                Document.class);

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

    private List<Map<String, Object>> parseFlatSearchableText(String flatText, String query, List<String> parentIds,
            String type, String parentPath, Set<String> names, Set<String> parentIdsSet) {
        List<Map<String, Object>> list = new ArrayList<>();

        String[] pairs = flatText.split("\\s*\\|\\|\\s*");

        for (String pair : pairs) {
            if (pair.isEmpty())
                continue;

            String[] keyValue = pair.split(":", 10);
            if (keyValue.length != 10)
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

    // Search New Impl [Start]
    public KnowledgeHubSearchResultWrapper onSearchTerm(KnowledgeHubSearchDTO knowledgeHubSearchDTO) {
        try {
            KnowledgeHubSearchResultWrapper knowledgeHubSearchResultWrapper = new KnowledgeHubSearchResultWrapper();
            if (knowledgeHubSearchDTO == null || knowledgeHubSearchDTO.getKeyword() == null) {
                return knowledgeHubSearchResultWrapper;
            }

            String regexPattern;
            if (knowledgeHubSearchDTO.isExactMatch()) {
                regexPattern = "(?<=^|[^A-Za-z0-9_])" + Pattern.quote(knowledgeHubSearchDTO.getKeyword())
                        + "(?=$|[^A-Za-z0-9_])";
            } else {
                regexPattern = Pattern.quote(knowledgeHubSearchDTO.getKeyword());
            }
            String options = knowledgeHubSearchDTO.isMatchCase() ? "" : "i";

            ParentCountResponseDTO parentCountResponseDTO = searchInProjectFlatSearchWithFrequency(regexPattern,
                    options, knowledgeHubSearchDTO.getSkip(), knowledgeHubSearchDTO.getLimit());

            if (parentCountResponseDTO == null
                    || !ValidationUtility.isListNotNullOrEmpty(parentCountResponseDTO.getResults())
                    || parentCountResponseDTO.getTotalProjects() == 0L) {
                return knowledgeHubSearchResultWrapper;
            }

            List<ParentCountDTO> projectWiseCount = parentCountResponseDTO.getResults();
            List<KnowledgeHubSearchResultProject> knowledgeHubSearchResultProjectList = new ArrayList<>();
            for (ParentCountDTO parentCountDTO : projectWiseCount) {
                List<FlatSearchResultDTO> flatSearchResultDTOList = getTopMatchingObjects(parentCountDTO.getParentId(),
                        regexPattern, options, 0, 10);

                KnowledgeHubSearchResultProject knowledgeHubSearchResultProject = transformFlatSearchResultDTOListToKnowledgeHubSearchResultProject(
                        flatSearchResultDTOList, parentCountDTO.getParentId(), regexPattern, options,
                        knowledgeHubSearchDTO.getKeyword());
                if (knowledgeHubSearchResultProject != null) {
                    knowledgeHubSearchResultProject.setSkip(0);
                    knowledgeHubSearchResultProject.setLimit(10);
                    knowledgeHubSearchResultProject.setTotalGroupCount(
                            getDistinctGroupCountForProject(parentCountDTO.getParentId(), regexPattern, options));
                    knowledgeHubSearchResultProject.setTotalProjectOccurenceCount(parentCountDTO.getCount());
                    knowledgeHubSearchResultProjectList.add(knowledgeHubSearchResultProject);
                }
            }
            System.out.println(
                    "==========================================---------------------------------{}{}{}{}{}{}{}");
            System.out.println(knowledgeHubSearchResultProjectList);

            knowledgeHubSearchResultWrapper.setKnowledgeHubSearchResultProjectList(knowledgeHubSearchResultProjectList);
            knowledgeHubSearchResultWrapper.setLimit(knowledgeHubSearchDTO.getLimit());
            knowledgeHubSearchResultWrapper.setSkip(knowledgeHubSearchResultProjectList.size());
            knowledgeHubSearchResultWrapper.setTotalCount(parentCountResponseDTO.getTotalProjects());
            return knowledgeHubSearchResultWrapper;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public ParentCountResponseDTO searchInProjectFlatSearchWithFrequency(String regexPattern, String options,
            Integer skip, Integer limit) {
        try {
            // Filter only matching documents
            MatchOperation matchStage = Aggregation.match(
                    Criteria.where("flatSearchableText").regex(regexPattern, options));

            // Add occurrence count per document
            AggregationExpression regexFindAllExpr = context -> new Document("$regexFindAll",
                    new Document("input", "$flatSearchableText")
                            .append("regex", regexPattern)
                            .append("options", options));

            AddFieldsOperation addOccurrenceStage = Aggregation.addFields()
                    .addFieldWithValue("occurrenceCount", ArrayOperators.Size.lengthOfArray(regexFindAllExpr))
                    .build();

            // Extract rootParent = parentIds[0]
            AddFieldsOperation addRootParentStage = Aggregation.addFields()
                    .addFieldWithValue("rootParent", ArrayOperators.ArrayElemAt.arrayOf("parentIds").elementAt(0))
                    .build();

            // Group by rootParent
            GroupOperation groupStage = Aggregation.group("rootParent")
                    .sum("occurrenceCount").as("totalOccurrences") // total matches across docs
                    .sum(ConditionalOperators
                            .when(ComparisonOperators.Gt.valueOf("occurrenceCount").greaterThanValue(0))
                            .then(1).otherwise(0))
                    .as("docCount"); // number of docs with ≥1 match

            // Sorting
            SortOperation sortStage = Aggregation.sort(Sort.by(Sort.Direction.DESC, "totalOccurrences"));

            // Pagination
            SkipOperation skipStage = Aggregation.skip(skip != null ? skip.longValue() : 0);
            LimitOperation limitStage = Aggregation.limit(limit != null ? limit : 10);

            // Projection
            ProjectionOperation projectStage = Aggregation.project()
                    .and(ConvertOperators.ToString.toString("$_id")).as("parentId")
                    .and("totalOccurrences").as("count")
                    .and("docCount").as("docCount");

            // Build aggregation
            Aggregation pagedAgg = Aggregation.newAggregation(matchStage, addOccurrenceStage, addRootParentStage,
                    groupStage, sortStage, skipStage, limitStage, projectStage);

            Aggregation countAgg = Aggregation.newAggregation(matchStage, addOccurrenceStage, addRootParentStage,
                    groupStage, Aggregation.count().as("totalProjects"));

            // Execute aggregation
            AggregationResults<Document> pagedResults = mongoTemplate.aggregate(
                    pagedAgg, "project_insight_project_flat_search", Document.class);

            AggregationResults<Document> countResults = mongoTemplate.aggregate(
                    countAgg, "project_insight_project_flat_search", Document.class);

            // Map to DTO
            List<ParentCountDTO> resultList = pagedResults.getMappedResults().stream()
                    .map(doc -> {
                        String parentId = doc.getString("parentId");
                        Number countNumber = doc.get("count", Number.class);
                        long count = (countNumber != null) ? countNumber.longValue() : 0L;
                        return new ParentCountDTO(parentId, count);
                    })
                    .collect(Collectors.toList());

            long totalProjects = countResults.getUniqueMappedResult() != null
                    ? Long.parseLong(countResults.getUniqueMappedResult().getInteger("totalProjects").toString())
                    : 0L;

            return new ParentCountResponseDTO(resultList, totalProjects);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public long getDistinctGroupCountForProject(
            String projectId, String regexPattern, String options) {

        // Get matching docs from project_insight_project_flat_search
        MatchOperation matchStage = Aggregation.match(
                Criteria.where("parentIds.0").is(projectId)
                        .and("flatSearchableText").regex(regexPattern, options)
                        .and("type").ne("project"));

        Aggregation agg = Aggregation.newAggregation(matchStage);

        List<Document> matchedDocs = mongoTemplate.aggregate(
                agg, "project_insight_project_flat_search", Document.class).getMappedResults();

        // Collect parentIds of groups
        Set<String> groupParentIdsFromFlatSearch = matchedDocs.stream()
                .filter(doc -> "group".equals(doc.getString("type")))
                .map(doc -> doc.getString("parentId"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Collect parentIds of questions
        Set<ObjectId> questionObjectIds = matchedDocs.stream()
                .filter(doc -> "question".equals(doc.getString("type")))
                .map(doc -> doc.getString("parentId")) // extract parentId
                .filter(Objects::nonNull) // ignore nulls
                .map(ObjectId::new) // convert directly to ObjectId
                .collect(Collectors.toSet());

        // Go to project_insight_question_details for those questions
        Set<String> groupIdsFromQuestionDetails = new HashSet<>();
        if (!questionObjectIds.isEmpty()) {

            Query q = new Query();
            q.addCriteria(
                    Criteria.where("parentType").is("Group")
                            .and("_id").in(questionObjectIds));
            q.fields().include("parentId");

            List<Document> questionDetails = mongoTemplate.find(
                    q, Document.class, "project_insight_question_details");

            groupIdsFromQuestionDetails = questionDetails.stream()
                    .map(d -> d.getString("parentId"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }

        // Union all groups
        Set<String> allDistinctGroups = new HashSet<>();
        allDistinctGroups.addAll(groupParentIdsFromFlatSearch);
        allDistinctGroups.addAll(groupIdsFromQuestionDetails);

        // Final count
        return allDistinctGroups.size();
    }

    public List<FlatSearchResultDTO> getTopMatchingObjects(String parentId0, String regexPattern, String options,
            int skip, int limit) {
        try {
            // Filter by parentIds[0]
            MatchOperation matchParentStage = Aggregation.match(
                    Criteria.where("parentIds.0").is(parentId0));

            // Match by regex on flatSearchableText
            MatchOperation matchTextStage = Aggregation
                    .match(Criteria.where("flatSearchableText").regex(regexPattern, options));

            // Exclude parentType = "Project"
            MatchOperation excludeProjectsStage = Aggregation.match(
                    Criteria.where("type").ne("project"));

            // Add per-document occurrence count using $regexFindAll
            AggregationExpression regexFindAllExpr = context -> new Document("$regexFindAll",
                    new Document("input", "$flatSearchableText")
                            .append("regex", regexPattern)
                            .append("options", options));

            AddFieldsOperation addOccurrenceStage = Aggregation.addFields()
                    .addFieldWithValue("occurrenceCount", ArrayOperators.Size.lengthOfArray(regexFindAllExpr))
                    .build();

            // Project only necessary fields
            ProjectionOperation projectStage = Aggregation.project()
                    .and("parentId").as("parentId")
                    .and("type").as("type")
                    .and("occurrenceCount").as("occurrenceCount")
                    .and("prefixPath").as("prefixPath");

            // Sort by occurrenceCount descending
            SortOperation sortStage = Aggregation.sort(Sort.by(Sort.Direction.DESC, "occurrenceCount"));

            // Pagination
            SkipOperation skipStage = Aggregation.skip((long) skip);
            LimitOperation limitStage = Aggregation.limit(limit);

            // Build aggregation pipeline
            Aggregation aggregation = Aggregation.newAggregation(matchParentStage, matchTextStage, excludeProjectsStage,
                    addOccurrenceStage,
                    sortStage, skipStage, limitStage, projectStage);

            // Execute
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregation,
                    "project_insight_project_flat_search",
                    Document.class);

            // Map to DTO
            return results.getMappedResults().stream()
                    .map(doc -> new FlatSearchResultDTO(
                            doc.getString("parentId"),
                            doc.getString("type"),
                            doc.getInteger("occurrenceCount") != null
                                    ? Long.parseLong(doc.getInteger("occurrenceCount").toString())
                                    : 0L,
                            doc.getString("prefixPath")))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public ProjectInsightProjectFlatSearch findSingleObjectMatch(String parentId, String regexPattern, String options,
            String objectType) {
        try {
            // Build criteria
            Criteria criteria = new Criteria().andOperator(
                    Criteria.where("type").is(objectType),
                    Criteria.where("parentId").is(parentId),
                    Criteria.where("flatSearchableText").regex(regexPattern, options));

            Query query = new Query(criteria);
            query.limit(1); // ensure only 1 object is returned

            return mongoTemplate.findOne(query, ProjectInsightProjectFlatSearch.class,
                    "project_insight_project_flat_search");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public KnowledgeHubSearchResultProject transformFlatSearchResultDTOListToKnowledgeHubSearchResultProject(
            List<FlatSearchResultDTO> flatSearchResultDTOList, String projectDetailsId, String regexPattern,
            String options, String keyword) {
        try {
            KnowledgeHubSearchResultProject knowledgeHubSearchResultProject = new KnowledgeHubSearchResultProject();
            Optional<ProjectInsightProjectDetails> projectInsightProjectDetails = projectInsightProjectDetailsRepository
                    .findById(projectDetailsId);
            if (projectInsightProjectDetails == null || projectInsightProjectDetails.isEmpty()) {
                return null;
            }

            ProjectInsightProjectFlatSearch projectInsightProjectFlatSearch = findSingleObjectMatch(projectDetailsId,
                    regexPattern, options, "project");
            ProjectInsightFormDetails projectFormDetails = projectInsightFormDetailsRepository
                    .findByParentIdAndParentType(projectDetailsId, "Project");
            if (projectInsightProjectFlatSearch != null && projectFormDetails != null) {
                knowledgeHubSearchResultProject.setKnowledgeHubSearchResultObjectFields(addFieldsToSearchResultProject(
                        projectInsightProjectDetails.get(), projectFormDetails, keyword));
            }

            if (ValidationUtility.isListNotNullOrEmpty(flatSearchResultDTOList)) {

                Map<String, ProjectInsightFormDetails> formDetailsMap = projectInsightFormDetailsRepository
                        .findByParentIdIn(
                                flatSearchResultDTOList.stream()
                                        .map(FlatSearchResultDTO::getObjectId)
                                        .collect(Collectors.toList()))
                        .stream().collect(Collectors.toMap(ProjectInsightFormDetails::getParentId, form -> form));

                Map<String, ProjectInsightGroupDetails> groupMap = projectInsightGroupDetailsRepository
                        .findByIdIn(
                                flatSearchResultDTOList.stream()
                                        .filter(dto -> "group".equalsIgnoreCase(dto.getType()))
                                        .map(FlatSearchResultDTO::getObjectId)
                                        .collect(Collectors.toList()))
                        .stream().collect(Collectors.toMap(ProjectInsightGroupDetails::getId, g -> g));

                Map<String, ProjectInsightQuestionDetails> questionMap = projectInsightQuestionDetailsRepository
                        .findByIdIn(
                                flatSearchResultDTOList.stream()
                                        .filter(dto -> "question".equalsIgnoreCase(dto.getType()))
                                        .map(FlatSearchResultDTO::getObjectId)
                                        .collect(Collectors.toList()))
                        .stream().collect(Collectors.toMap(ProjectInsightQuestionDetails::getId, q -> q));

                Map<String, ProjectInsightResponseDetails> responseMap = projectInsightResponseDetailsRepository
                        .findByIdIn(
                                flatSearchResultDTOList.stream()
                                        .filter(dto -> "response".equalsIgnoreCase(dto.getType()))
                                        .map(FlatSearchResultDTO::getObjectId)
                                        .collect(Collectors.toList()))
                        .stream().collect(Collectors.toMap(ProjectInsightResponseDetails::getId, r -> r));

                List<KnowledgeHubSearchResultObject> knowledgeHubSearchResultObjectList = flatSearchResultDTOList
                        .stream()
                        .map(dto -> transformFlatSearchResultDTOToKnowledgeHubSearchResultObject(dto, keyword,
                                formDetailsMap, groupMap,
                                questionMap, responseMap))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                knowledgeHubSearchResultProject
                        .setKnowledgeHubSearchResultObjectList(knowledgeHubSearchResultObjectList);
            }
            knowledgeHubSearchResultProject.setProjectId(projectInsightProjectDetails.get().getId());
            knowledgeHubSearchResultProject.setProjectName(projectInsightProjectDetails.get().getProjectName());
            return knowledgeHubSearchResultProject;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private KnowledgeHubSearchResultObject transformFlatSearchResultDTOToKnowledgeHubSearchResultObject(
            FlatSearchResultDTO flatSearchResultDTO, String keyword,
            Map<String, ProjectInsightFormDetails> formDetailsMap,
            Map<String, ProjectInsightGroupDetails> groupMap,
            Map<String, ProjectInsightQuestionDetails> questionMap,
            Map<String, ProjectInsightResponseDetails> responseMap) {

        if (flatSearchResultDTO == null || flatSearchResultDTO.getType() == null
                || flatSearchResultDTO.getObjectId() == null) {
            return null;
        }

        String type = flatSearchResultDTO.getType();

        ProjectInsightFormDetails objectFormDetails = formDetailsMap.get(flatSearchResultDTO.getObjectId());
        if (objectFormDetails == null) {
            return null;
        }

        KnowledgeHubSearchResultObject searchResultObj = null;

        switch (type.toLowerCase()) {
            case "group":
                ProjectInsightGroupDetails g = groupMap.get(flatSearchResultDTO.getObjectId());
                if (g != null) {
                    searchResultObj = buildGroupObject(g, objectFormDetails, keyword);
                    searchResultObj.setGroupTitle(g.getGroupTitle());
                }
                break;
            case "question":
                ProjectInsightQuestionDetails q = questionMap.get(flatSearchResultDTO.getObjectId());
                if (q != null) {
                    searchResultObj = buildQuestionObject(q, keyword);
                }
                break;
            case "response":
                ProjectInsightResponseDetails r = responseMap.get(flatSearchResultDTO.getObjectId());
                if (r != null) {
                    searchResultObj = buildResponseObject(r, keyword);
                }
                break;
            default:
                break;
        }

        if (searchResultObj != null) {
            searchResultObj.setPrefixPath(flatSearchResultDTO.getPrefixPath());
            searchResultObj.setTotalObjectOccurenceCount(flatSearchResultDTO.getOccurrenceCount());
        }
        return searchResultObj;
    }

    private List<KnowledgeHubSearchResultObjectField> addFieldsToSearchResultProject(
            ProjectInsightProjectDetails project,
            ProjectInsightFormDetails projectFormDetails, String keyword) {
        List<KnowledgeHubSearchResultObjectField> fields = new ArrayList<>();

        if (checkIfKeywordExistsInString(project.getProjectName(), keyword)) {
            KnowledgeHubSearchResultObjectField projectNameField = new KnowledgeHubSearchResultObjectField();
            projectNameField.setLabel("Project Name");
            projectNameField.setName("ProjectName");
            projectNameField.setValue(project.getProjectName());
            fields.add(projectNameField);
        }
        if (checkIfKeywordExistsInString(project.getProjectManagerName(), keyword)) {
            KnowledgeHubSearchResultObjectField projectManagerNameField = new KnowledgeHubSearchResultObjectField();
            projectManagerNameField.setLabel("Project Manager");
            projectManagerNameField.setName("ProjectManagerName");
            projectManagerNameField.setValue(project.getProjectManagerName());
            fields.add(projectManagerNameField);
        }
        if (checkIfKeywordExistsInString(project.getClientRM(), keyword)) {
            KnowledgeHubSearchResultObjectField clientRMField = new KnowledgeHubSearchResultObjectField();
            clientRMField.setLabel("Cleint RM");
            clientRMField.setName("ClientRM");
            clientRMField.setValue(project.getClientRM());
            fields.add(clientRMField);
        }
        if (checkIfKeywordExistsInString(project.getApmosysRM(), keyword)) {
            KnowledgeHubSearchResultObjectField apmosysRMField = new KnowledgeHubSearchResultObjectField();
            apmosysRMField.setLabel("Apmosys RM");
            apmosysRMField.setName("ApmosysRM");
            apmosysRMField.setValue(project.getApmosysRM());
            fields.add(apmosysRMField);
        }
        if (project.getClient() != null &&
                checkIfKeywordExistsInString(project.getClient().getClientName(), keyword)) {
            KnowledgeHubSearchResultObjectField clientNameField = new KnowledgeHubSearchResultObjectField();
            clientNameField.setLabel("Client Name");
            clientNameField.setName("clientName");
            clientNameField.setValue(project.getApmosysRM());
            fields.add(clientNameField);
        }

        String depts = ValidationUtility.isListNotNullOrEmpty(project.getDepartments())
                ? project.getDepartments().stream().map(DepartmentDTO::getName)
                        .collect(Collectors.joining(", "))
                : "";
        if (checkIfKeywordExistsInString(depts, keyword)) {
            KnowledgeHubSearchResultObjectField deptsField = new KnowledgeHubSearchResultObjectField();
            deptsField.setLabel("Departments");
            deptsField.setName("Departments");
            deptsField.setValue(depts);
            fields.add(deptsField);
        }
        fields.addAll(
                extractSearchResultFieldsFromFormFields(project.getAdditionalInfo(), projectFormDetails, keyword));
        return fields;
    }

    private KnowledgeHubSearchResultObject buildGroupObject(ProjectInsightGroupDetails group,
            ProjectInsightFormDetails formDetails, String keyword) {
        KnowledgeHubSearchResultObject obj = new KnowledgeHubSearchResultObject();
        obj.setObjectId(group.getId());
        obj.setObjectType("Group");
        obj.setParentId(group.getParentId());

        List<KnowledgeHubSearchResultObjectField> fields = new ArrayList<>();
        if (checkIfKeywordExistsInString(group.getGroupTitle(), keyword)) {
            KnowledgeHubSearchResultObjectField titleField = new KnowledgeHubSearchResultObjectField();
            titleField.setLabel("Group Title");
            titleField.setName("GroupTitle");
            titleField.setValue(group.getGroupTitle());
            fields.add(titleField);
        }
        fields.addAll(extractSearchResultFieldsFromFormFields(group.getAdditionalInfo(), formDetails, keyword));
        obj.setKnowledgeHubSearchResultObjectFields(fields);
        return obj;
    }

    private List<KnowledgeHubSearchResultObjectField> extractSearchResultFieldsFromFormFields(
            Map<String, Object> additionalInfo, ProjectInsightFormDetails formDetails, String keyword) {
        try {
            if (additionalInfo == null || additionalInfo.isEmpty() || formDetails == null
                    || !ValidationUtility.isListNotNullOrEmpty(formDetails.getFields())) {
                return Collections.emptyList();
            }
            List<KnowledgeHubSearchResultObjectField> result = new ArrayList<>();

            for (FormFieldDTO formFieldDTO : formDetails.getFields()) {
                Set<String> listOptionType = Set.of("select", "checkbox");

                // String options =
                // ValidationUtility.isListNotNullOrEmpty(formFieldDTO.getOptions())
                // ? formFieldDTO.getOptions().stream().map(OptionDTO::getValue)
                // .collect(Collectors.joining(", "))
                // : "";

                String values = "";
                Object valueObj = additionalInfo.get(formFieldDTO.getName());

                if (listOptionType.contains(formFieldDTO.getType().toLowerCase())) {
                    if (valueObj != null && valueObj instanceof List<?>) {

                        List<?> rawList = (List<?>) valueObj;

                        if (ValidationUtility.isListNotNullOrEmpty(rawList)) {
                            values = rawList.stream()
                                    .map(obj -> obj != null ? obj.toString() : "")
                                    .collect(Collectors.joining(", "));
                        } else {
                            values = "";
                        }
                    } else {
                        values = valueObj != null ? valueObj.toString() : "";
                    }
                } else if ("table".equalsIgnoreCase(formFieldDTO.getType().toLowerCase())
                        || "file".equalsIgnoreCase(formFieldDTO.getType().toLowerCase())) {
                    continue;
                } else {
                    values = valueObj != null ? valueObj.toString() : "";
                }

                if (checkIfKeywordExistsInString(formFieldDTO.getLabel(), keyword) ||
                        checkIfKeywordExistsInString(values, keyword)
                // || checkIfKeywordExistsInString(options, keyword)
                ) {
                    KnowledgeHubSearchResultObjectField field = new KnowledgeHubSearchResultObjectField();
                    field.setType(formFieldDTO.getType().toLowerCase());
                    field.setLabel(formFieldDTO.getLabel());
                    field.setName(formFieldDTO.getName());
                    field.setValue(additionalInfo.get(formFieldDTO.getName()));
                    // field.setQuestionOptions(options);
                    result.add(field);
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private KnowledgeHubSearchResultObject buildQuestionObject(ProjectInsightQuestionDetails questionDetails,
            String keyword) {
        KnowledgeHubSearchResultObject obj = new KnowledgeHubSearchResultObject();
        obj.setObjectId(questionDetails.getId());
        obj.setObjectType("Question");
        obj.setParentId(questionDetails.getParentId());

        // String options = "";
        List<KnowledgeHubSearchResultObjectField> fields = new ArrayList<>();
        // Set<String> listOptionType = Set.of("select", "checkbox", "radio");

        // if (listOptionType.contains(questionDetails.getOptionType().toLowerCase())) {
        // options =
        // ValidationUtility.isListNotNullOrEmpty(questionDetails.getOptionsList())
        // ?
        // questionDetails.getOptionsList().stream().map(OptionValueDTO::getOptionValue)
        // .collect(Collectors.joining(", "))
        // : "";
        // }

        if (checkIfKeywordExistsInString(questionDetails.getQuestion(), keyword) ||
                checkIfKeywordExistsInString(questionDetails.getDescription(), keyword)
        // || checkIfKeywordExistsInString(options, keyword)
        ) {
            KnowledgeHubSearchResultObjectField question = new KnowledgeHubSearchResultObjectField();
            question.setName(questionDetails.getQuestion());
            question.setLabel(questionDetails.getQuestion());
            question.setDescription(questionDetails.getDescription());
            // question.setQuestionOptions(options);
            fields.add(question);
        }
        obj.setKnowledgeHubSearchResultObjectFields(fields);
        return obj;
    }

    private KnowledgeHubSearchResultObject buildResponseObject(ProjectInsightResponseDetails responseDetails,
            String keyword) {
        KnowledgeHubSearchResultObject obj = new KnowledgeHubSearchResultObject();
        obj.setObjectId(responseDetails.getId());
        obj.setObjectType("Response");
        obj.setParentId(responseDetails.getQuesId());
        return obj;
    }

    private boolean checkIfKeywordExistsInString(String sentence, String keyword) {
        return ValidationUtility.isStringNotNullOrEmpty(sentence) &&
                ValidationUtility.isStringNotNullOrEmpty(keyword) &&
                sentence.toLowerCase().contains(keyword.toLowerCase().trim());
    }

    public KnowledgeHubSearchResultProject loadProjectSearchObject(KnowledgeHubSearchDTO knowledgeHubSearchDTO) {
        try {
            if (knowledgeHubSearchDTO == null || knowledgeHubSearchDTO.getProjectId() == null) {
                return null;
            }
            String regexPattern;
            if (knowledgeHubSearchDTO.isExactMatch()) {
                regexPattern = "(?<=^|[^A-Za-z0-9_])" + Pattern.quote(knowledgeHubSearchDTO.getKeyword())
                        + "(?=$|[^A-Za-z0-9_])";
            } else {
                regexPattern = Pattern.quote(knowledgeHubSearchDTO.getKeyword());
            }
            String options = knowledgeHubSearchDTO.isMatchCase() ? "" : "i";
            List<FlatSearchResultDTO> flatSearchResultDTOList = getTopMatchingObjects(
                    knowledgeHubSearchDTO.getProjectId(),
                    regexPattern, options, knowledgeHubSearchDTO.getSkip(), knowledgeHubSearchDTO.getLimit());

            KnowledgeHubSearchResultProject knowledgeHubSearchResultProject = transformFlatSearchResultDTOListToKnowledgeHubSearchResultProject(
                    flatSearchResultDTOList, knowledgeHubSearchDTO.getProjectId(), regexPattern, options,
                    knowledgeHubSearchDTO.getKeyword());
            if (knowledgeHubSearchResultProject != null) {
                knowledgeHubSearchResultProject.setTotalGroupCount(getDistinctGroupCountForProject(knowledgeHubSearchDTO.getProjectId(), regexPattern, options));
            }
            return knowledgeHubSearchResultProject;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    // Search New Impl [End]

}