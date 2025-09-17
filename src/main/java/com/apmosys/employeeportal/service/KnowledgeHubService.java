package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.FormFieldDTO;
import com.apmosys.employeeportal.dto.ProjectInsightFacetCategoryDTO;
import com.apmosys.employeeportal.dto.ProjectInsightFacetValueDTO;
import com.apmosys.employeeportal.model.ProjectInsightFacetCategory;
import com.apmosys.employeeportal.mongodb.dto.DepartmentDTO;
import com.apmosys.employeeportal.mongodb.dto.FacetSearchResultDTO;
import com.apmosys.employeeportal.mongodb.dto.FlatSearchResultDTO;
import com.apmosys.employeeportal.mongodb.dto.KnowledgeHubSearchDTO;
import com.apmosys.employeeportal.mongodb.dto.KnowledgeHubSearchResultObject;
import com.apmosys.employeeportal.mongodb.dto.KnowledgeHubSearchResultObjectField;
import com.apmosys.employeeportal.mongodb.dto.KnowledgeHubSearchResultProject;
import com.apmosys.employeeportal.mongodb.dto.KnowledgeHubSearchResultWrapper;
import com.apmosys.employeeportal.mongodb.dto.ParentCountDTO;
import com.apmosys.employeeportal.mongodb.dto.ParentCountResponseDTO;
import com.apmosys.employeeportal.mongodb.dto.ProjectInsightDetailsMapDTO;
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
import com.apmosys.employeeportal.repository.ProjectInsightFacetCategoryRepository;
import com.apmosys.employeeportal.repository.ProjectInsightFacetValueRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
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

    @Autowired
    private ProjectInsightFacetCategoryRepository projectInsightFacetCategoryRepository;

    @Autowired
    private ProjectInsightFacetValueRepository projectInsightFacetValueRepository;

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
                    options, knowledgeHubSearchDTO.getSkip(), knowledgeHubSearchDTO.getLimit(),
                    knowledgeHubSearchDTO.getFacetCategories());

            if (parentCountResponseDTO == null
                    || !ValidationUtility.isListNotNullOrEmpty(parentCountResponseDTO.getResults())
                    || parentCountResponseDTO.getTotalProjects() == 0L) {
                return knowledgeHubSearchResultWrapper;
            }

            List<ParentCountDTO> projectWiseCount = parentCountResponseDTO.getResults();
            List<KnowledgeHubSearchResultProject> knowledgeHubSearchResultProjectList = new ArrayList<>();
            for (ParentCountDTO parentCountDTO : projectWiseCount) {
                List<FlatSearchResultDTO> flatSearchResultDTOList = getTopMatchingObjects(parentCountDTO.getParentId(),
                        regexPattern, options, 0, 10, knowledgeHubSearchDTO.getFacetCategories());

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
            Integer skip, Integer limit, List<ProjectInsightFacetCategoryDTO> facetCategories) {
        try {

            List<Criteria> criteriaList = new ArrayList<>();
            criteriaList.add(Criteria.where("flatSearchableText").regex(regexPattern, options));

            Criteria facetCriteria = buildFacetCriteria(facetCategories);
            if (facetCriteria != null && facetCriteria.getCriteriaObject() != null
                    && !facetCriteria.getCriteriaObject().isEmpty()) {
                criteriaList.add(facetCriteria);
            }

            Criteria combinedCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
            MatchOperation matchStage = Aggregation.match(combinedCriteria);

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
            int skip, int limit, List<ProjectInsightFacetCategoryDTO> facetCategories) {
        try {
            // Filter by parentIds[0]
            MatchOperation matchParentStage = Aggregation.match(
                    Criteria.where("parentIds.0").is(parentId0));

            List<Criteria> criteriaList = new ArrayList<>();
            criteriaList.add(Criteria.where("flatSearchableText").regex(regexPattern, options));

            Criteria facetCriteria = buildFacetCriteria(facetCategories);
            if (facetCriteria != null && facetCriteria.getCriteriaObject() != null
                    && !facetCriteria.getCriteriaObject().isEmpty()) {
                criteriaList.add(facetCriteria);
            }

            Criteria combinedCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
            MatchOperation matchTextStage = Aggregation.match(combinedCriteria);

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

                List<FacetSearchResultDTO> facetSearchResultDTOList = flatSearchResultDTOList.stream()
                        .map(obj -> new FacetSearchResultDTO(
                                obj.getObjectId(),
                                obj.getType()))
                        .collect(Collectors.toList());

                ProjectInsightDetailsMapDTO projectInsightDetailsMapDTO = fetchProjectInsightDetailsMapDTO(
                        facetSearchResultDTOList);
                if (projectInsightDetailsMapDTO == null) {
                    return null;
                }
                Map<String, ProjectInsightFormDetails> formDetailsMap = projectInsightDetailsMapDTO.getFormDetailsMap();
                Map<String, ProjectInsightGroupDetails> groupMap = projectInsightDetailsMapDTO.getGroupMap();
                Map<String, ProjectInsightQuestionDetails> questionMap = projectInsightDetailsMapDTO.getQuestionMap();
                Map<String, ProjectInsightResponseDetails> responseMap = projectInsightDetailsMapDTO.getResponseMap();

                List<KnowledgeHubSearchResultObject> knowledgeHubSearchResultObjectList = flatSearchResultDTOList
                        .parallelStream()
                        .map(dto -> transformFlatSearchResultDTOToKnowledgeHubSearchResultObject(
                                dto, keyword, formDetailsMap, groupMap, questionMap, responseMap))
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
        KnowledgeHubSearchResultObject searchResultObj = null;

        switch (type.toLowerCase()) {
            case "group":
                ProjectInsightGroupDetails g = groupMap.get(flatSearchResultDTO.getObjectId());
                if (g != null) {
                    ProjectInsightFormDetails objectFormDetails = formDetailsMap.get(flatSearchResultDTO.getObjectId());
                    if (objectFormDetails == null) {
                        return null;
                    }
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
            clientNameField.setValue(project.getClient().getClientName());
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

            Set<String> listOptionType = Set.of("select", "checkbox");
            for (FormFieldDTO formFieldDTO : formDetails.getFields()) {
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
                        checkIfKeywordExistsInString(values, keyword)) {
                    KnowledgeHubSearchResultObjectField field = new KnowledgeHubSearchResultObjectField();
                    field.setType(formFieldDTO.getType().toLowerCase());
                    field.setLabel(formFieldDTO.getLabel());
                    field.setName(formFieldDTO.getName());
                    field.setValue(additionalInfo.get(formFieldDTO.getName()));
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

        List<KnowledgeHubSearchResultObjectField> fields = new ArrayList<>();

        if (checkIfKeywordExistsInString(questionDetails.getQuestion(), keyword) ||
                checkIfKeywordExistsInString(questionDetails.getDescription(), keyword)) {
            KnowledgeHubSearchResultObjectField question = new KnowledgeHubSearchResultObjectField();
            question.setName(questionDetails.getQuestion());
            question.setLabel(questionDetails.getQuestion());
            question.setDescription(questionDetails.getDescription());
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
                    regexPattern, options, knowledgeHubSearchDTO.getSkip(), knowledgeHubSearchDTO.getLimit(),
                    knowledgeHubSearchDTO.getFacetCategories());

            KnowledgeHubSearchResultProject knowledgeHubSearchResultProject = transformFlatSearchResultDTOListToKnowledgeHubSearchResultProject(
                    flatSearchResultDTOList, knowledgeHubSearchDTO.getProjectId(), regexPattern, options,
                    knowledgeHubSearchDTO.getKeyword());
            if (knowledgeHubSearchResultProject != null) {
                knowledgeHubSearchResultProject.setTotalGroupCount(
                        getDistinctGroupCountForProject(knowledgeHubSearchDTO.getProjectId(), regexPattern, options));
            }
            return knowledgeHubSearchResultProject;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public ProjectInsightDetailsMapDTO fetchProjectInsightDetailsMapDTO(
            List<FacetSearchResultDTO> facetSearchResultDTOList) {

        List<String> allIds = facetSearchResultDTOList.stream()
                .map(FacetSearchResultDTO::getParentId)
                .collect(Collectors.toList());

        List<String> projectIds = facetSearchResultDTOList.stream()
                .filter(dto -> "project".equalsIgnoreCase(dto.getType()))
                .map(FacetSearchResultDTO::getParentId)
                .collect(Collectors.toList());

        List<String> groupIds = facetSearchResultDTOList.stream()
                .filter(dto -> "group".equalsIgnoreCase(dto.getType()))
                .map(FacetSearchResultDTO::getParentId)
                .collect(Collectors.toList());

        List<String> questionIds = facetSearchResultDTOList.stream()
                .filter(dto -> "question".equalsIgnoreCase(dto.getType()))
                .map(FacetSearchResultDTO::getParentId)
                .collect(Collectors.toList());

        List<String> responseIds = facetSearchResultDTOList.stream()
                .filter(dto -> "response".equalsIgnoreCase(dto.getType()))
                .map(FacetSearchResultDTO::getParentId)
                .collect(Collectors.toList());

        // Run repo calls in parallel
        CompletableFuture<Map<String, ProjectInsightFormDetails>> formFuture = CompletableFuture
                .supplyAsync(() -> projectInsightFormDetailsRepository.findByParentIdIn(allIds).stream()
                        .collect(Collectors.toMap(ProjectInsightFormDetails::getParentId, f -> f)));

        CompletableFuture<Map<String, ProjectInsightProjectDetails>> projectFuture = CompletableFuture
                .supplyAsync(() -> projectInsightProjectDetailsRepository.findByIdIn(projectIds).stream()
                        .collect(Collectors.toMap(ProjectInsightProjectDetails::getId, p -> p)));

        CompletableFuture<Map<String, ProjectInsightGroupDetails>> groupFuture = CompletableFuture
                .supplyAsync(() -> projectInsightGroupDetailsRepository.findByIdIn(groupIds).stream()
                        .collect(Collectors.toMap(ProjectInsightGroupDetails::getId, g -> g)));

        CompletableFuture<Map<String, ProjectInsightQuestionDetails>> questionFuture = CompletableFuture
                .supplyAsync(() -> projectInsightQuestionDetailsRepository.findByIdIn(questionIds).stream()
                        .collect(Collectors.toMap(ProjectInsightQuestionDetails::getId, q -> q)));

        CompletableFuture<Map<String, ProjectInsightResponseDetails>> responseFuture = CompletableFuture
                .supplyAsync(() -> projectInsightResponseDetailsRepository.findByIdIn(responseIds).stream()
                        .collect(Collectors.toMap(ProjectInsightResponseDetails::getId, r -> r)));

        // Wait for all futures to complete
        CompletableFuture.allOf(formFuture, projectFuture, groupFuture, questionFuture, responseFuture).join();

        return new ProjectInsightDetailsMapDTO(
                formFuture.join(),
                projectFuture.join(),
                groupFuture.join(),
                questionFuture.join(),
                responseFuture.join());
    }

    public ServiceResponse getAllFacetsForKeyword(KnowledgeHubSearchDTO knowledgeHubSearchDTO) {
        ServiceResponse serviceResponse = new ServiceResponse();
        try {
            if (knowledgeHubSearchDTO == null || knowledgeHubSearchDTO.getKeyword() == null) {
                serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
                serviceResponse.setServiceResponse("Request Body or Search Keyword cannot be null.");
                return serviceResponse;
            }

            String regexPattern;
            if (knowledgeHubSearchDTO.isExactMatch()) {
                regexPattern = "(?<=^|[^A-Za-z0-9_])" + Pattern.quote(knowledgeHubSearchDTO.getKeyword())
                        + "(?=$|[^A-Za-z0-9_])";
            } else {
                regexPattern = Pattern.quote(knowledgeHubSearchDTO.getKeyword());
            }
            String options = knowledgeHubSearchDTO.isMatchCase() ? "" : "i";

            List<FacetSearchResultDTO> facetSearchResultDTOList = getParentIdAndTypeFromProjectFlatSearch(regexPattern,
                    options);
            List<Long> facetCategoryIds = extractFacetCategoryFromFacetSearchResultDTOList(
                    facetSearchResultDTOList, knowledgeHubSearchDTO.getKeyword());
            serviceResponse.setServiceResponse(extractFacetCategoryAndValue(new HashSet<>(facetCategoryIds), ""));
            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
            serviceResponse.setServiceResponse("Something went wrong.");
        }
        return serviceResponse;
    }

    public List<FacetSearchResultDTO> getParentIdAndTypeFromProjectFlatSearch(String regexPattern, String options) {
        try {
            MatchOperation matchStage = Aggregation.match(
                    Criteria.where("flatSearchableText").regex(regexPattern, options));

            ProjectionOperation projectStage = Aggregation.project("parentId", "type");

            Aggregation aggregation = Aggregation.newAggregation(matchStage, projectStage);

            AggregationResults<Document> results = mongoTemplate.aggregate(
                    aggregation,
                    "project_insight_project_flat_search",
                    Document.class);

            return results.getMappedResults().stream()
                    .map(doc -> new FacetSearchResultDTO(
                            doc.getString("parentId"),
                            doc.getString("type")))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    List<Long> extractFacetCategoryFromFacetSearchResultDTOList(List<FacetSearchResultDTO> facetSearchResultDTOList,
            String keyword) {
        try {
            if (!ValidationUtility.isListNotNullOrEmpty(facetSearchResultDTOList)) {
                return Collections.emptyList();
            }

            ProjectInsightDetailsMapDTO projectInsightDetailsMapDTO = fetchProjectInsightDetailsMapDTO(
                    facetSearchResultDTOList);
            if (projectInsightDetailsMapDTO == null) {
                return null;
            }
            Map<String, ProjectInsightFormDetails> formDetailsMap = projectInsightDetailsMapDTO.getFormDetailsMap();
            Map<String, ProjectInsightProjectDetails> projectMap = projectInsightDetailsMapDTO.getProjectMap();
            Map<String, ProjectInsightGroupDetails> groupMap = projectInsightDetailsMapDTO.getGroupMap();
            Map<String, ProjectInsightQuestionDetails> questionMap = projectInsightDetailsMapDTO.getQuestionMap();
            Map<String, ProjectInsightResponseDetails> responseMap = projectInsightDetailsMapDTO.getResponseMap();

            List<Long> facetCategoryIds = facetSearchResultDTOList
                    .parallelStream()
                    .map(dto -> transformFlatSearchResultDTOToKnowledgeHubSearchResultObject(
                            dto, keyword, formDetailsMap, projectMap, groupMap, questionMap, responseMap))
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            if (!ValidationUtility.isListNotNullOrEmpty(facetCategoryIds)) {
                return Collections.emptyList();
            }

            return new ArrayList<>(new HashSet<>(facetCategoryIds));
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<Long> transformFlatSearchResultDTOToKnowledgeHubSearchResultObject(
            FacetSearchResultDTO facetSearchResultDTO, String keyword,
            Map<String, ProjectInsightFormDetails> formDetailsMap,
            Map<String, ProjectInsightProjectDetails> projectMap,
            Map<String, ProjectInsightGroupDetails> groupMap,
            Map<String, ProjectInsightQuestionDetails> questionMap,
            Map<String, ProjectInsightResponseDetails> responseMap) {

        if (facetSearchResultDTO == null || facetSearchResultDTO.getType() == null
                || facetSearchResultDTO.getParentId() == null) {
            return null;
        }

        String type = facetSearchResultDTO.getType();
        List<Long> facetCategoryIds = new ArrayList<>();

        ProjectInsightFormDetails objectFormDetails = formDetailsMap.get(facetSearchResultDTO.getParentId());
        if (objectFormDetails == null) {
            return null;
        }

        switch (type.toLowerCase()) {
            case "group":
                ProjectInsightGroupDetails g = groupMap.get(facetSearchResultDTO.getParentId());
                if (g != null) {
                    facetCategoryIds.addAll(extractFacetCategoryIdsFromGroupDetails(g, objectFormDetails, keyword));
                }
            case "project":
                ProjectInsightProjectDetails p = projectMap.get(facetSearchResultDTO.getParentId());
                if (p != null) {
                    facetCategoryIds.addAll(extractFacetCategoryIdsFromProjectDetails(p, objectFormDetails,
                            keyword));
                }
                break;
            case "question":
                ProjectInsightQuestionDetails q = questionMap.get(facetSearchResultDTO.getParentId());
                if (q != null) {
                    facetCategoryIds.addAll(extractFacetCategoryIdsFromQuestionDetails(q, keyword));
                }
                break;
            case "response":
                ProjectInsightResponseDetails r = responseMap.get(facetSearchResultDTO.getParentId());
                if (r != null) {
                    // facetCategoryIds.addAll(buildResponseObject(r, keyword));
                }
                break;
            default:
                break;
        }
        return facetCategoryIds;
    }

    private List<Long> extractFacetCategoryIdsFromProjectDetails(
            ProjectInsightProjectDetails project, ProjectInsightFormDetails projectFormDetails, String keyword) {
        List<Long> facetCategoryIds = new ArrayList<>();

        Set<String> projectDetailsFacetNames = Set.of("Project", "Project Manager", "Departments",
                "Client", "Client RM", "Apmosys RM");

        List<ProjectInsightFacetCategory> existingFacets = projectInsightFacetCategoryRepository
                .findAllByCategoryNameInIgnoreCase(
                        new ArrayList<>(projectDetailsFacetNames.stream().map(String::toLowerCase)
                                .collect(Collectors.toList())));

        Map<String, Long> facetIdMap = existingFacets.stream()
                .map(obj -> {
                    return Map.entry(obj.getCategoryName(), obj.getFacetCategoryId());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));

        if (checkIfKeywordExistsInString(project.getProjectName(), keyword)) {
            facetCategoryIds.add(facetIdMap.getOrDefault("Project", 0L));
        }
        if (checkIfKeywordExistsInString(project.getProjectManagerName(), keyword)) {
            facetCategoryIds.add(facetIdMap.getOrDefault("Project Manager", 0L));
        }
        if (checkIfKeywordExistsInString(project.getClientRM(), keyword)) {
            facetCategoryIds.add(facetIdMap.getOrDefault("Client RM", 0L));
        }
        if (checkIfKeywordExistsInString(project.getApmosysRM(), keyword)) {
            facetCategoryIds.add(facetIdMap.getOrDefault("Apmosys RM", 0L));
        }
        if (project.getClient() != null &&
                checkIfKeywordExistsInString(project.getClient().getClientName(), keyword)) {
            facetCategoryIds.add(facetIdMap.getOrDefault("Client", 0L));
        }

        String depts = ValidationUtility.isListNotNullOrEmpty(project.getDepartments())
                ? project.getDepartments().stream().map(DepartmentDTO::getName)
                        .collect(Collectors.joining(", "))
                : "";

        if (checkIfKeywordExistsInString(depts, keyword)) {
            facetCategoryIds.add(facetIdMap.getOrDefault("Departments", 0L));
        }
        facetCategoryIds
                .addAll(extractFacetCategoryIdsFromFormFields(project.getAdditionalInfo(), projectFormDetails,
                        keyword));
        return facetCategoryIds;
    }

    private List<Long> extractFacetCategoryIdsFromGroupDetails(ProjectInsightGroupDetails group,
            ProjectInsightFormDetails projectFormDetails, String keyword) {
        List<Long> facetCategoryIds = new ArrayList<>();

        Set<String> groupDetailsFacetNames = Set.of("Group");

        List<ProjectInsightFacetCategory> existingFacets = projectInsightFacetCategoryRepository
                .findAllByCategoryNameInIgnoreCase(
                        new ArrayList<>(groupDetailsFacetNames.stream().map(String::toLowerCase)
                                .collect(Collectors.toList())));

        Map<String, Long> facetIdMap = existingFacets.stream()
                .map(obj -> {
                    return Map.entry(obj.getCategoryName(), obj.getFacetCategoryId());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));

        if (checkIfKeywordExistsInString(group.getGroupTitle(), keyword)) {
            facetCategoryIds.add(facetIdMap.getOrDefault("Group", 0L));
        }
        facetCategoryIds
                .addAll(extractFacetCategoryIdsFromFormFields(group.getAdditionalInfo(), projectFormDetails,
                        keyword));
        return facetCategoryIds;
    }

    private List<Long> extractFacetCategoryIdsFromFormFields(
            Map<String, Object> additionalInfo, ProjectInsightFormDetails formDetails, String keyword) {
        try {
            if (additionalInfo == null || additionalInfo.isEmpty() || formDetails == null
                    || !ValidationUtility.isListNotNullOrEmpty(formDetails.getFields())) {
                return Collections.emptyList();
            }
            List<Long> result = new ArrayList<>();

            Set<String> listOptionType = Set.of("select", "checkbox");
            for (FormFieldDTO formFieldDTO : formDetails.getFields()) {
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
                        checkIfKeywordExistsInString(values, keyword)) {
                    if (ValidationUtility.isListNotNullOrEmpty(formFieldDTO.getFacetCategoryIds())) {
                        result.addAll(formFieldDTO.getFacetCategoryIds());
                    }
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private List<Long> extractFacetCategoryIdsFromQuestionDetails(
            ProjectInsightQuestionDetails questionDetails,
            String keyword) {
        if (checkIfKeywordExistsInString(questionDetails.getQuestion(), keyword) ||
                checkIfKeywordExistsInString(questionDetails.getDescription(), keyword)) {
            if (ValidationUtility.isListNotNullOrEmpty(questionDetails.getFacetCategoryIds())) {
                return questionDetails.getFacetCategoryIds();
            }
        }
        return Collections.emptyList();
    }

    private List<ProjectInsightFacetCategoryDTO> extractFacetCategoryAndValue(Set<Long> allFacetCategoryIds,
            String keyword) {
        List<ProjectInsightFacetCategoryDTO> newProjectInsightFacetCategoryDTOList = new ArrayList<>();
        try {
            List<ProjectInsightFacetCategoryDTO> projectInsightFacetCategoryDTOList = projectInsightFacetCategoryRepository
                    .findFacetCategoryByFacetCategoryIdIn(new ArrayList<>(allFacetCategoryIds));
            if (!projectInsightFacetCategoryDTOList.isEmpty()) {
                for (ProjectInsightFacetCategoryDTO facet : projectInsightFacetCategoryDTOList) {
                    List<ProjectInsightFacetValueDTO> projectInsightFacetValueDTOList = projectInsightFacetValueRepository
                            .findProjectInsightFacetValueByFacetCategoryId(facet.getFacetCategoryId());
                    if (ValidationUtility.isListNotNullOrEmpty(projectInsightFacetValueDTOList)) {
                        facet.setProjectInsightFacetValueDTOList(projectInsightFacetValueDTOList);
                    }
                }
                newProjectInsightFacetCategoryDTOList = projectInsightFacetCategoryDTOList.stream()
                        .filter(obj -> ValidationUtility.isListNotNullOrEmpty(obj.getProjectInsightFacetValueDTOList()))
                        .collect(Collectors.toList());

                newProjectInsightFacetCategoryDTOList.sort(
                        Comparator.comparing(ProjectInsightFacetCategoryDTO::getCategoryName,
                                String.CASE_INSENSITIVE_ORDER));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return newProjectInsightFacetCategoryDTOList;
    }

    private Criteria buildFacetCriteria(List<ProjectInsightFacetCategoryDTO> facetCategories) {
        if (!ValidationUtility.isListNotNullOrEmpty(facetCategories)) {
            return new Criteria();
        }

        List<Criteria> categoryCriteriaList = new ArrayList<>();

        for (ProjectInsightFacetCategoryDTO categoryDTO : facetCategories) {
            if (categoryDTO.getProjectInsightFacetValueDTOList() == null
                    || categoryDTO.getProjectInsightFacetValueDTOList().isEmpty()) {
                continue;
            }

            List<Long> valueIds = categoryDTO.getProjectInsightFacetValueDTOList()
                    .stream()
                    .map(ProjectInsightFacetValueDTO::getFacetValueId)
                    .collect(Collectors.toList());
            Criteria categoryCriteria = new Criteria().andOperator(
                    Criteria.where("facetCategoryIds").in(categoryDTO.getFacetCategoryId()),
                    Criteria.where("facetValueIds").in(valueIds));

            categoryCriteriaList.add(categoryCriteria);
        }
        if (categoryCriteriaList.isEmpty()) {
            return new Criteria(); // no-op
        }
        return new Criteria().andOperator(categoryCriteriaList.toArray(new Criteria[0]));
    }
}