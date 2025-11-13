package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.transaction.Transactional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.apmosys.employeeportal.dto.ProjectInsighProjectMappingDTO;
import com.apmosys.employeeportal.model.TechStack;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectFlatSearch;

import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.DomainDataDTO;
import com.apmosys.employeeportal.dto.ProjectInsighProjectMappingDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDomainCreatedBy;
import com.apmosys.employeeportal.dto.ProjectInsightDomainDataDto;
import com.apmosys.employeeportal.dto.ProjectInsightEditDomainDTO;
import com.apmosys.employeeportal.dto.ProjectInsightMappingWithCount;
import com.apmosys.employeeportal.dto.ServiceDataDTO;
import com.apmosys.employeeportal.dto.SubDomainDataDTO;
import com.apmosys.employeeportal.dto.SubServiceDataDTO;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.DeliveryMode;
import com.apmosys.employeeportal.model.ProjectInsightDomainData;
import com.apmosys.employeeportal.model.ProjectInsightDomainDataFlatSearch;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightProjectFlatSearchRepository;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.DeliveryModeRepository;
import com.apmosys.employeeportal.repository.ProjectInsightDomainDataFlatSearchRepository;
import com.apmosys.employeeportal.repository.ProjectInsightDomainDataRepository;
import com.apmosys.employeeportal.repository.ProjectInsightDomainRepository;
import com.apmosys.employeeportal.repository.ProjectInsightServiceRepository;
import com.apmosys.employeeportal.repository.ProjectInsightSubDomainRepository;
import com.apmosys.employeeportal.repository.ProjectInsightSubServiceRepository;
import com.apmosys.employeeportal.repository.TechStackRepository;
import com.apmosys.employeeportal.utility.SearchUtils;

@Service
public class ProjectInsightDomainService {

    @Value("${project.domain.data.colours}") // must be a comma-separated string
    private String colorsProperty;

    @Autowired
    ProjectInsightServiceRepository projectInsightServiceRepository;

    @Autowired
    ProjectInsightDomainRepository projectInsightDomainRepository;

    @Autowired
    ProjectInsightSubDomainRepository projectInsightSubDomainRepository;

    @Autowired
    ProjectInsightSubServiceRepository projectInsightSubServiceRepository;

    @Autowired
    private ProjectInsightDomainDataRepository projectInsightDomainDataRepository;

    @Autowired
    private SearchUtils searchUtils;

    @Autowired
    private ProjectInsightProjectFlatSearchRepository flatSearchRepo;

    @Autowired
    private ProjectInsightProjectDetailsRepository projectInsightProjectDetailsRepository;

    @Autowired
    private ProjectInsightDomainDataFlatSearchRepository domainFlatSearchRepo;

    @Transactional
    public String saveDomainTree(DomainDataDTO dto, boolean editing, Long createdBy) {

      if(!editing){
        if(projectInsightDomainDataRepository.existsByName(dto.getName())) {
          throw new RuntimeException("Domain name already exists");
        }
      }

    ProjectInsightDomainData domain = editing && dto.getId() != null
        ? projectInsightDomainDataRepository.findById(dto.getId()).orElseThrow()
        : new ProjectInsightDomainData();

    domain.setName(dto.getName());
    domain.setType(dto.getType());
    domain.setParent(null);
    domain.setCreatedBy(createdBy);
    // domain.setDomaincolorCode(domain.getDomaincolorCode());
    assignRandomColor(domain);

    List<ProjectInsightDomainData> children = new ArrayList<>();

    // Handle subdomains
    if (dto.getSubDomains() != null) {
      for (SubDomainDataDTO subDto : dto.getSubDomains()) {
        children.add(saveSubDomain(subDto, domain, editing,createdBy));
      }
    }

    // Handle services
    if (dto.getServices() != null) {
      for (ServiceDataDTO svcDto : dto.getServices()) {
        children.add(saveService(svcDto, domain, editing,createdBy));
      }
    }

    if (editing) {
      if (domain.getChildren() == null) {
        domain.setChildren(new ArrayList<>());
      }
      mergeChildren(domain, children);
    } else {
      domain.setChildren(children);
    }

    ProjectInsightDomainData savedDomain = projectInsightDomainDataRepository.save(domain);

    // Save the flat search
    ProjectInsightDomainDataFlatSearch flatSearch = domainFlatSearchRepo.findByDomainId(savedDomain.getId()).orElse(new ProjectInsightDomainDataFlatSearch());
    flatSearch.setFlatSearch(buildFlatSearch(savedDomain));
    flatSearch.setDomainId(savedDomain.getId());
    domainFlatSearchRepo.save(flatSearch);

    return "Domain saved successfully";
  }

  public void assignRandomColor(ProjectInsightDomainData domain) {
    if ("domain".equalsIgnoreCase(domain.getType()) &&
        (domain.getDomaincolorCode() == null || domain.getDomaincolorCode().isEmpty())) {
      domain.setDomaincolorCode(getRandomWarmColor());
    }
  }

  private String getRandomWarmColor() {
    Random random = new Random();
    // Hue: allow all color ranges (0–360°)
    float hue = random.nextFloat();
    // Very low saturation → muted, pastel tones
    float saturation = 0.15f + random.nextFloat() * 0.15f; // 0.15–0.30
    // Very high lightness → soft and pale
    float lightness = 0.85f + random.nextFloat() * 0.10f; // 0.85–0.95
    return hslToHex(hue, saturation, lightness);
  }

  private String hslToHex(float h, float s, float l) {
    float r, g, b;
    if (s == 0) {
      r = g = b = l;
    } else {
      float q = l < 0.5 ? l * (1 + s) : l + s - l * s;
      float p = 2 * l - q;
      r = hue2rgb(p, q, h + 1f / 3f);
      g = hue2rgb(p, q, h);
      b = hue2rgb(p, q, h - 1f / 3f);
    }
    int R = Math.round(r * 255);
    int G = Math.round(g * 255);
    int B = Math.round(b * 255);
    return String.format("#%02X%02X%02X", R, G, B);
  }

  private float hue2rgb(float p, float q, float t) {
    if (t < 0)
      t += 1;
    if (t > 1)
      t -= 1;
    if (t < 1f / 6f)
      return p + (q - p) * 6 * t;
    if (t < 1f / 2f)
      return q;
    if (t < 2f / 3f)
      return p + (q - p) * (2f / 3f - t) * 6;
    return p;
  }

  private String buildFlatSearch(ProjectInsightDomainData entity) {
      StringBuilder sb = new StringBuilder();
      buildFlatSearchRecursively(entity, sb);
      return sb.toString();
  }

  private void buildFlatSearchRecursively(ProjectInsightDomainData entity, StringBuilder sb) {
      sb.append(entity.getName()).append(":");
      if (entity.getChildren() != null && !entity.getChildren().isEmpty()) {
          for (ProjectInsightDomainData child : entity.getChildren()) {
              buildFlatSearchRecursively(child, sb);
          }
      }
  }

  private ProjectInsightDomainData saveSubDomain(SubDomainDataDTO dto, ProjectInsightDomainData parent, boolean editing, Long createdBy) {
    ProjectInsightDomainData node = editing && dto.getId() != null
        ? projectInsightDomainDataRepository.findById(dto.getId()).orElse(new ProjectInsightDomainData())
        : new ProjectInsightDomainData();

    node.setName(dto.getName());
    node.setType(dto.getType());
    node.setDomaincolorCode(node.getDomaincolorCode());
    node.setParent(parent);

    List<ProjectInsightDomainData> children = new ArrayList<>();

    if (dto.getSubDomains() != null) {
      for (SubDomainDataDTO sub : dto.getSubDomains()) {
        children.add(saveSubDomain(sub, node, editing, createdBy));
      }
    }

    if (dto.getServices() != null) {
      for (ServiceDataDTO svc : dto.getServices()) {
        children.add(saveService(svc, node, editing, createdBy));
      }
    }

    if (editing) {
      if (node.getChildren() == null) {
        node.setChildren(new ArrayList<>());
      }
      mergeChildren(node, children);
    } else {
      node.setChildren(children);
    }

    return node;
  }

  private ProjectInsightDomainData saveService(ServiceDataDTO dto, ProjectInsightDomainData parent, boolean editing, Long createdBy) {
    ProjectInsightDomainData node = editing && dto.getId() != null
        ? projectInsightDomainDataRepository.findById(dto.getId()).orElse(new ProjectInsightDomainData())
        : new ProjectInsightDomainData();

    node.setName(dto.getName());
    node.setType(dto.getType());
    node.setParent(parent);

    List<ProjectInsightDomainData> children = new ArrayList<>();
    if (dto.getSubServices() != null) {
      for (SubServiceDataDTO sub : dto.getSubServices()) {
        children.add(saveSubService(sub, node, editing, createdBy));
      }
    }

    if (editing) {
      if (node.getChildren() == null) {
        node.setChildren(new ArrayList<>());
      }
      mergeChildren(node, children);
    } else {
      node.setChildren(children);
    }

    return node;
  }

  private ProjectInsightDomainData saveSubService(SubServiceDataDTO dto, ProjectInsightDomainData parent, boolean editing, Long createdBy) {
    ProjectInsightDomainData node = editing && dto.getId() != null
        ? projectInsightDomainDataRepository.findById(dto.getId()).orElse(new ProjectInsightDomainData())
        : new ProjectInsightDomainData();

    node.setName(dto.getName());
    node.setType(dto.getType());
    node.setParent(parent);

    List<ProjectInsightDomainData> children = new ArrayList<>();
    if (dto.getSubServices() != null) {
      for (SubServiceDataDTO sub : dto.getSubServices()) {
        children.add(saveSubService(sub, node, editing, createdBy));
      }
    }

    if (editing) {
      if (node.getChildren() == null) {
        node.setChildren(new ArrayList<>()); 
      }
      mergeChildren(node, children);
    } else {
      node.setChildren(children);
    }
    return node;
  }

  private void mergeChildren(ProjectInsightDomainData parent, List<ProjectInsightDomainData> newChildren) {
    Map<Long, ProjectInsightDomainData> existingById = new HashMap<>();
    for (ProjectInsightDomainData child : parent.getChildren()) {
      if (child.getId() != null) {
        existingById.put(child.getId(), child);
      }
    }

    List<ProjectInsightDomainData> finalChildren = new ArrayList<>();

    for (ProjectInsightDomainData newChild : newChildren) {
      if (newChild.getId() != null && existingById.containsKey(newChild.getId())) {
        ProjectInsightDomainData existingChild = existingById.get(newChild.getId());
        existingChild.setName(newChild.getName());
        existingChild.setType(newChild.getType());
        existingChild.setParent(parent);
        mergeChildren(existingChild, newChild.getChildren());
        finalChildren.add(existingChild);
        existingById.remove(newChild.getId());
      } else {
        newChild.setParent(parent);
        finalChildren.add(newChild);
      }
    }

    parent.getChildren().clear();
    parent.getChildren().addAll(finalChildren);
  }


    public List<ProjectInsightDomainData> getAllProjectInsightDomains(List<Long> ids) {
        List<ProjectInsightDomainData> domains = projectInsightDomainDataRepository.findAllById(ids);
        return domains;
    }

    public Long addNewData(ProjectInsightEditDomainDTO projectInsightEditDomainDTO) {
        ProjectInsightDomainData parentDomain = projectInsightDomainDataRepository.findById(projectInsightEditDomainDTO.getParent_id()).orElseThrow();
        ProjectInsightDomainData domain = new ProjectInsightDomainData();
        domain.setName(projectInsightEditDomainDTO.getName());
        domain.setType(projectInsightEditDomainDTO.getType());
        domain.setParent(parentDomain);
        projectInsightDomainDataRepository.save(domain);
        return domain.getId();
    }

    public Page<ProjectInsightDomainCreatedBy> findAllDomainAndCreatedBy(Integer page, Integer limit) {

        Pageable pageable = PageRequest.of(page, limit);

        return projectInsightDomainDataRepository.findAllDomainAndCreatedBy(pageable);
    }

    public Page<ProjectInsightDomainCreatedBy> findAllDomainSearched(String domain, String createdBy,
            LocalDateTime createdOn, Boolean isActive, Integer page, Integer limit, String isApproved) {

        Pageable pageable = PageRequest.of(page, limit);

        return projectInsightDomainDataRepository.findAllDomainSearched(domain, createdBy, createdOn, isActive, isApproved, pageable);
    }

    public ProjectInsightDomainData findDomain(String domain) {
        return projectInsightDomainDataRepository.findByDomain(domain);
    }

    public void softDelete(Long id) {
        ProjectInsightDomainData projectInsightDomainData = projectInsightDomainDataRepository.findById(id).orElseThrow();

        projectInsightDomainData.setIsActive(!projectInsightDomainData.getIsActive());

        projectInsightDomainDataRepository.save(projectInsightDomainData);
    }

    @Transactional
    public void editDomains(List<Map<Long, String>> listProjectInsightEditDomainDTO) {
        projectInsightDomainDataRepository.bulkUpdateDomainNames(listProjectInsightEditDomainDTO);
    }

    @Transactional
    public void approveDomain(Long id,String isApproved, Long approvedBy) {
        ProjectInsightDomainData projectInsightDomainData = projectInsightDomainDataRepository.findById(id).orElseThrow();
        projectInsightDomainData.setIsApproved(isApproved);
        projectInsightDomainData.setApprovedBy(approvedBy);
        projectInsightDomainDataRepository.save(projectInsightDomainData);
    }

    @Value("${FilterSqlFields}")
    private String filterSqlFields;
    
    @Value("${FilterMongodbFields}")
    private String filterMongodbFields;

    @Autowired
    private DeliveryModeRepository deliveryModeRepository;

    @Autowired
    private ClientsRepository clientRepository;

    @Autowired
    private TechStackRepository techStackRepository;

    public Map<String, Object> loadAllFilters() {
        Map<String, Object> map = new HashMap<>();
        List<String> sqlFilterist = Arrays.asList(filterSqlFields.split(",")).stream()
                .map(String::trim).collect(Collectors.toList());
        List<String> mongodbFilterist = Arrays.asList(filterMongodbFields.split(",")).stream()
                .map(String::trim).collect(Collectors.toList());
        
        for(String field : sqlFilterist) {
            if(field.equalsIgnoreCase("created_at")) {
                Map<String, Object> dateMap = new HashMap<>();
                dateMap.put("type", "date");
                dateMap.put("value", null);
                map.put(field, dateMap);
            } if (field.equalsIgnoreCase("Delivery_Mode")) {
                List<DeliveryMode> deliveryModes = deliveryModeRepository.findAll();
                List<Map<String, Object>> deliveryModeList = deliveryModes.stream().map(deliveryMode -> {
                    Map<String, Object> deliveryModeMap = new HashMap<>();
                    deliveryModeMap.put("id", deliveryMode.getId());
                    deliveryModeMap.put("name", deliveryMode.getDeliveryMode());
                    return deliveryModeMap;
                }).collect(Collectors.toList());
                Map<String, Object> deliveryModeMap = new HashMap<>();
                deliveryModeMap.put("type", "select");
                deliveryModeMap.put("value", deliveryModeList);
                map.put(field, deliveryModeMap);
                
            } if(field.equalsIgnoreCase("clients")) {
                List<Client> clients = clientRepository.findAll();
                List<Map<String, Object>> clientList = clients.stream().map(client -> {
                    Map<String, Object> clientMap = new HashMap<>();
                    clientMap.put("id", client.getClientId());
                    clientMap.put("name", client.getClientName());
                    return clientMap;
                }).collect(Collectors.toList());
                Map<String, Object> clientMap = new HashMap<>();
                clientMap.put("type", "select");
                clientMap.put("value", clientList);
                map.put(field, clientMap);
            } if ( field.equalsIgnoreCase("tech_stack")){
                List<TechStack> techStacks = techStackRepository.findAll();
                List<Map<String, Object>> techStackList = techStacks.stream().map(techStack -> {
                    Map<String, Object> techStackMap = new HashMap<>();
                    techStackMap.put("id", techStack.getId());
                    techStackMap.put("name", techStack.getStack());
                    return techStackMap;
                }).collect(Collectors.toList());
                Map<String, Object> techStackMap = new HashMap<>();
                techStackMap.put("type", "select");
                techStackMap.put("value", techStackList);
                map.put(field, techStackMap);
            }
        }

        // List<ProjectInsightStructure> projectInsightStructures = getProjectInsightStructures(mongodbFilterist);
        Map<String, Object> tagMap = new HashMap<>();
        tagMap.put("type", "input");
        tagMap.put("value", null);
        
        map.put("tags",tagMap);

        return map;
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    public Page<ProjectInsighProjectMappingDTO> filterProjectInsight(Map<String,Object> filterProjectInsight,
            Integer page, Integer limit) {
        List<Integer> ids = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, limit);

        List<String> sqlFilterist = Arrays.asList(filterSqlFields.split(",")).stream()
                .map(String::trim).collect(Collectors.toList());

        List<String> mongodbFilterist = Arrays.asList(filterMongodbFields.split(",")).stream()
                .map(String::trim).collect(Collectors.toList());
        
        Map<String, List<String>> fieldValueMap = new HashMap<>();
        
        for(String key : filterProjectInsight.keySet()) {
            Object value = filterProjectInsight.get(key);
            if(value instanceof List) {
                if(mongodbFilterist.contains(key)) {
                    fieldValueMap.put(key, (List<String>) value );
                }
            }
        }

        // List<ProjectInsightStructure> projectInsightStructures = getProjectInsightStructuresByValues(fieldValueMap);


        // if (projectInsightStructures != null && !projectInsightStructures.isEmpty()) {
        //     for (ProjectInsightStructure s : projectInsightStructures) {
        //         Object projectName = s.getData().getFields().get("projectname");
                
        //         if (projectName instanceof Integer) {
        //             ids.add((Integer) projectName);
        //         }
        //     }
        // }

        if (ids.isEmpty()) {
            ids = null; 
        }

        Object createdAtObj = filterProjectInsight.get("Created_At");
        LocalDate createdAt = null;

        if (createdAtObj != null) {
            if (createdAtObj instanceof String) {
                createdAt = LocalDate.parse((String) createdAtObj); // parse from string to LocalDate
            } else if (createdAtObj instanceof LocalDate) {
                createdAt = (LocalDate) createdAtObj;
            }
        }

        List<Integer> clients = filterProjectInsight.get("Clients") != null ? (List<Integer>) filterProjectInsight.get("Clients") : null;

        List<Integer> deliveryModes = filterProjectInsight.get("Delivery_Mode") != null ? (List<Integer>) filterProjectInsight.get("Delivery_Mode") : null;

        Page<ProjectInsighProjectMappingDTO> list = projectInsightDomainRepository.filterProjectInsight(
                clients, createdAt, ids, pageable);

        return list;
    }

    // private List<ProjectInsightStructure> getProjectInsightStructures(List<String> fields) {
    //     List<Criteria> orCriteriaList = new ArrayList<>();
    //     for (String field : fields) {
    //         orCriteriaList.add(Criteria.where(field).exists(true));  
    //     }

    //     Criteria orCriteria = new Criteria().orOperator(orCriteriaList.toArray(new Criteria[0]));

    //     Criteria criteria = Criteria.where("data.questions.projectResponseList")
    //                                 .elemMatch(orCriteria);

    //     Query query = new Query(criteria);

    //     List<ProjectInsightStructure> results = mongoTemplate.find(query, ProjectInsightStructure.class);

    //     return results;
    // }

    // private List<ProjectInsightStructure> getProjectInsightStructuresByValues(
    //     Map<String, List<String>> fieldValueMap) {
    
    //     List<Criteria> orCriteriaList = new ArrayList<>();

    //     for (Map.Entry<String, List<String>> entry : fieldValueMap.entrySet()) {
    //         String field = entry.getKey(); 
    //         List<String> values = entry.getValue();

    //         if (values != null && !values.isEmpty()) {
    //             orCriteriaList.add(Criteria.where(field).in(values));
    //         }
    //     }

    //     if (orCriteriaList.isEmpty()) {
    //         return Collections.emptyList();
    //     }

    //     Criteria orCriteria = new Criteria().orOperator(orCriteriaList.toArray(new Criteria[0]));

    //     Criteria criteria = Criteria.where("data.questions.projectResponseList")
    //                             .elemMatch(orCriteria);

    //     Query query = new Query(criteria);
        
    //     return mongoTemplate.find(query, ProjectInsightStructure.class);
    // }

    // public Page<ProjectInsighProjectMappingDTO> search(String search, Integer page, Integer limit) {
    //     Pageable pageable = PageRequest.of(page, limit);
        
    //     // List<ProjectInsightProjectFlatSearch> list = projectInsightProjectFlatSearchRepository.

    //     search = search.toLowerCase().trim();
    //     String regexPattern = ".*" + Pattern.quote(search.toLowerCase()) + ".*";

    //     // Query mongoQuery = new Query();
    //     // mongoQuery.addCriteria(Criteria.where("flatSearchableText").regex(regexPattern, "i")).limit(limit).skip(page * limit);

    //     // List<ProjectInsightProjectFlatSearch> matchedEntries = mongoTemplate.find(mongoQuery,
    //     //         ProjectInsightProjectFlatSearch.class);

    //     Query mongoQuery = new Query()
    //         .addCriteria(Criteria.where("flatSearchableText").regex(regexPattern, "i"))
    //         .with(pageable);

    //     List<ProjectInsightProjectFlatSearch> matchedEntries = mongoTemplate.find(mongoQuery, ProjectInsightProjectFlatSearch.class);

        
    //     List<String> projectIdsMongo = new ArrayList<>();

    //     for (ProjectInsightProjectFlatSearch entry : matchedEntries) {
    //       if(entry.getParentIds() != null && entry.getParentIds().size() > 0)
    //         projectIdsMongo.add(entry.getParentIds().get(0));
    //     }
        
    //     Iterable<ProjectInsightProjectDetails> projectInsightProjectDetails = 
    //     projectInsightProjectDetailsRepository.findAllById(projectIdsMongo);
    //     // projectInsightProjectDetailsRepository.findByProjectIdIn(projectIdsMongo);

    //     List<Integer> projectIds = new ArrayList<>();

    //     for (ProjectInsightProjectDetails entry : projectInsightProjectDetails) {
    //         projectIds.add(entry.getProjectId());
    //     }

    //     Page<ProjectInsighProjectMappingDTO> list = projectInsightDomainRepository.searchProjectInsightByProjectId(projectIds);

    //     return list;
    // }

    public List<ProjectInsighProjectMappingDTO> search(String search, Integer page, Integer limit) {
        search = search.toLowerCase().trim();
        // Pageable pageable = PageRequest.of(page, limit);
        // String regexPattern = ".*" + Pattern.quote(search.toLowerCase()) + ".*";

      //   Aggregation agg = Aggregation.newAggregation(
      //     // Match documents
      //     Aggregation.match(Criteria.where("flatSearchableText").regex(regexPattern, "i")),

      //     Aggregation.project()
      //         .and(ArrayOperators.ArrayElemAt.arrayOf("parentIds").elementAt(0))
      //         .as("firstParentId"),

      //     Aggregation.group("firstParentId"),

      //     Aggregation.facet(
      //         Aggregation.count().as("totalCount")
      //     ).as("metadata")
      //     .and(
      //         Aggregation.skip((long) page * limit),
      //         Aggregation.limit(limit)
      //     ).as("data")
      // );

        // Aggregation agg = Aggregation.newAggregation(
        //   Aggregation.match(Criteria.where("flatSearchableText").regex(regexPattern, "i")),

        //   Aggregation.project()
        //       .and(ArrayOperators.ArrayElemAt.arrayOf("parentIds").elementAt(0))
        //       .as("firstParentId"),

        //   Aggregation.group("firstParentId")
        // );

        // AggregationResults<Document> results =
        //     mongoTemplate.aggregate(agg, "project_insight_project_flat_search", Document.class);

        // List<Document> resultDocs = results.getMappedResults();

        // if (resultDocs == null || resultDocs.isEmpty()) {
        //     ProjectInsightMappingWithCount projectInsightMappingWithCount = new ProjectInsightMappingWithCount();
        //     projectInsightMappingWithCount.setCount(0);
        //     projectInsightMappingWithCount.setMappings(Collections.emptyList());
        //     return projectInsightMappingWithCount;
        // }

        // List<String> projectIdsMongo = resultDocs.stream()
        //     .map(d -> d.getString("_id"))
        //     .toList();

        // List<Document> metadata = (List<Document>) resultDoc.get("metadata");
        // Integer totalCount = 0;

        // if (metadata != null && !metadata.isEmpty()) {
        //     totalCount = metadata.get(0).getInteger("totalCount", 0);
        // }

        // Fetch project details from secondary repository
        // Iterable<ProjectInsightProjectDetails> projectInsightProjectDetails =
        //     projectInsightProjectDetailsRepository.findAllById(projectIdsMongo);

        // List<Integer> projectIds = new ArrayList<>();
        // for (ProjectInsightProjectDetails entry : projectInsightProjectDetails) {
        //     projectIds.add(entry.getProjectId());
        // }

        // Fetch final DTOs (just a List)
        List<ProjectInsighProjectMappingDTO> list = projectInsightDomainRepository.searchProjectInsight(search);

        return list;
    }

    public List<ProjectInsightDomainDataDto> getDomainsData(List<String> type, Long parentId) {
        List<ProjectInsightDomainDataDto> list = projectInsightDomainDataRepository.findDomainsByTypeAndParentId(type,parentId);
        return list;
    }

    public List<List<String>> getDomainHierarchy() {
      try {
        // Fetch all root-level domains (those without a parent)
        List<ProjectInsightDomainData> rootDomains = projectInsightDomainDataRepository.findAllActiveAndApprovedDomainAndParentIsNull();
        List<List<String>> result = new ArrayList<>();
        for (ProjectInsightDomainData root : rootDomains) {
          buildHierarchy(root, new ArrayList<>(), result);
        }

        // Print the result in comma-separated format
        for (List<String> path : result) {
          System.out.println(String.join(" , ", path));
        }
        return result;
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }

    }

    private void buildHierarchy(ProjectInsightDomainData current, List<String> path, List<List<String>> result) {
      path.add(current.getName());
      if (current.getChildren().isEmpty()) {
        // Leaf node → add a copy of the path to result
        result.add(new ArrayList<>(path));
      } else {
        // Recurse for each child
        for (ProjectInsightDomainData child : current.getChildren()) {
          buildHierarchy(child, path, result);
        }
      }
      // Backtrack (remove last element)
      path.remove(path.size() - 1);
    }
    
}