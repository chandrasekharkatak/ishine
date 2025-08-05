package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

import javax.transaction.Transactional;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.apmosys.employeeportal.dto.ProjectInsighProjectMappingDTO;
import com.apmosys.employeeportal.model.TechStack;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightStructure;

import com.apmosys.employeeportal.dto.DomainDataDTO;
import com.apmosys.employeeportal.dto.FilterProjectInsightDTO;
import com.apmosys.employeeportal.dto.ProjectInsighProjectMappingDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDomainCreatedBy;
import com.apmosys.employeeportal.dto.ProjectInsightDomainDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDomainDataDto;
import com.apmosys.employeeportal.dto.ProjectInsightEditDomainDTO;
import com.apmosys.employeeportal.dto.ProjectInsightServiceDTO;
import com.apmosys.employeeportal.repository.ProjectInsightServiceRepository;
import com.apmosys.employeeportal.repository.ProjectInsightSubDomainRepository;
import com.apmosys.employeeportal.repository.ProjectInsightSubServiceRepository;
import com.apmosys.employeeportal.repository.TechStackRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.apmosys.employeeportal.dto.ProjectInsightSubDomainDTO;
import com.apmosys.employeeportal.dto.ProjectInsightSubServiceDTO;
import com.apmosys.employeeportal.dto.ServiceDataDTO;
import com.apmosys.employeeportal.dto.SubDomainDataDTO;
import com.apmosys.employeeportal.dto.SubServiceDataDTO;
import com.apmosys.employeeportal.enums.ProjectInsightDomainApprovedStatus;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.DeliveryMode;
import com.apmosys.employeeportal.model.ProjectInsightDomain;
import com.apmosys.employeeportal.model.ProjectInsightDomainData;
import com.apmosys.employeeportal.model.ProjectInsightSubDomain;
import com.apmosys.employeeportal.model.ProjectInsightSubService;
import com.apmosys.employeeportal.model.TechStack;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightStructure;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightStructureRepository;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.DeliveryModeRepository;
import com.apmosys.employeeportal.repository.ProjectInsightDomainRepository;

import com.apmosys.employeeportal.model.ProjectInsightServiceModel;
import com.apmosys.employeeportal.repository.ProjectInsightDomainDataRepository;
import com.apmosys.employeeportal.utility.SearchUtils;

@Service
public class ProjectInsightDomainService {

    @Autowired
    ProjectInsightServiceRepository projectInsightServiceRepository;

    @Autowired
    ProjectInsightDomainRepository projectInsightDomainRepository;

    @Autowired
    ProjectInsightSubDomainRepository projectInsightSubDomainRepository;

    @Autowired
    ProjectInsightSubServiceRepository projectInsightSubServiceRepository;

    @Autowired
    private ProjectInsightDomainDataRepository nodeRepository;

    public String saveDomainTree(DomainDataDTO dto, boolean editing, Long createdBy) {

      if(!editing){
        if(nodeRepository.existsByName(dto.getName())) {
          throw new RuntimeException("Domain name already exists");
        }
      }

    ProjectInsightDomainData domain = editing && dto.getId() != null
        ? nodeRepository.findById(dto.getId()).orElseThrow()
        : new ProjectInsightDomainData();

    domain.setName(dto.getName());
    domain.setType(dto.getType());
    domain.setParent(null);
    domain.setCreatedBy(createdBy);

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

    nodeRepository.save(domain);

    return "Domain saved successfully";
  }

  private ProjectInsightDomainData saveSubDomain(SubDomainDataDTO dto, ProjectInsightDomainData parent, boolean editing, Long createdBy) {
    ProjectInsightDomainData node = editing && dto.getId() != null
        ? nodeRepository.findById(dto.getId()).orElse(new ProjectInsightDomainData())
        : new ProjectInsightDomainData();

    node.setName(dto.getName());
    node.setType(dto.getType());
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
        ? nodeRepository.findById(dto.getId()).orElse(new ProjectInsightDomainData())
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
        ? nodeRepository.findById(dto.getId()).orElse(new ProjectInsightDomainData())
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
        List<ProjectInsightDomainData> domains = nodeRepository.findAllById(ids);
        return domains;
    }

    public Long addNewData(ProjectInsightEditDomainDTO projectInsightEditDomainDTO) {
        ProjectInsightDomainData parentDomain = nodeRepository.findById(projectInsightEditDomainDTO.getParent_id()).orElseThrow();
        ProjectInsightDomainData domain = new ProjectInsightDomainData();
        domain.setName(projectInsightEditDomainDTO.getName());
        domain.setType(projectInsightEditDomainDTO.getType());
        domain.setParent(parentDomain);
        nodeRepository.save(domain);
        return domain.getId();
    }

    public Page<ProjectInsightDomainCreatedBy> findAllDomainAndCreatedBy(Integer page, Integer limit) {

        Pageable pageable = PageRequest.of(page, limit);

        return nodeRepository.findAllDomainAndCreatedBy(pageable);
    }

    public Page<ProjectInsightDomainCreatedBy> findAllDomainSearched(String domain, String createdBy,
            LocalDateTime createdOn, Boolean isActive, Integer page, Integer limit, String isApproved) {

        Pageable pageable = PageRequest.of(page, limit);

        return nodeRepository.findAllDomainSearched(domain, createdBy, createdOn, isActive, isApproved, pageable);
    }

    public ProjectInsightDomainData findDomain(String domain) {
        return nodeRepository.findByDomain(domain);
    }

    public void softDelete(Long id) {
        ProjectInsightDomainData projectInsightDomainData = nodeRepository.findById(id).orElseThrow();

        projectInsightDomainData.setIsActive(!projectInsightDomainData.getIsActive());

        nodeRepository.save(projectInsightDomainData);
    }

    @Transactional
    public void editDomains(List<Map<Long, String>> listProjectInsightEditDomainDTO) {
        nodeRepository.bulkUpdateDomainNames(listProjectInsightEditDomainDTO);
    }

    @Transactional
    public void approveDomain(Long id,String isApproved, Long approvedBy) {
        ProjectInsightDomainData projectInsightDomainData = nodeRepository.findById(id).orElseThrow();
        projectInsightDomainData.setIsApproved(isApproved);
        projectInsightDomainData.setApprovedBy(approvedBy);
        nodeRepository.save(projectInsightDomainData);
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

        List<ProjectInsightStructure> projectInsightStructures = getProjectInsightStructuresByValues(fieldValueMap);


        if (projectInsightStructures != null && !projectInsightStructures.isEmpty()) {
            for (ProjectInsightStructure s : projectInsightStructures) {
                Object projectName = s.getData().getFields().get("projectname");
                
                if (projectName instanceof Integer) {
                    ids.add((Integer) projectName);
                }
            }
        }

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

    private List<ProjectInsightStructure> getProjectInsightStructures(List<String> fields) {
        List<Criteria> orCriteriaList = new ArrayList<>();
        for (String field : fields) {
            orCriteriaList.add(Criteria.where(field).exists(true));  
        }

        Criteria orCriteria = new Criteria().orOperator(orCriteriaList.toArray(new Criteria[0]));

        Criteria criteria = Criteria.where("data.questions.projectResponseList")
                                    .elemMatch(orCriteria);

        Query query = new Query(criteria);

        List<ProjectInsightStructure> results = mongoTemplate.find(query, ProjectInsightStructure.class);

        return results;
    }

    private List<ProjectInsightStructure> getProjectInsightStructuresByValues(
        Map<String, List<String>> fieldValueMap) {
    
        List<Criteria> orCriteriaList = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : fieldValueMap.entrySet()) {
            String field = entry.getKey(); 
            List<String> values = entry.getValue();

            if (values != null && !values.isEmpty()) {
                orCriteriaList.add(Criteria.where(field).in(values));
            }
        }

        if (orCriteriaList.isEmpty()) {
            return Collections.emptyList();
        }

        Criteria orCriteria = new Criteria().orOperator(orCriteriaList.toArray(new Criteria[0]));

        Criteria criteria = Criteria.where("data.questions.projectResponseList")
                                .elemMatch(orCriteria);

        Query query = new Query(criteria);
        
        return mongoTemplate.find(query, ProjectInsightStructure.class);
    }

    @Autowired
    private SearchUtils searchUtils;

    public Page<ProjectInsighProjectMappingDTO> search(String search, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<ProjectInsighProjectMappingDTO> list = projectInsightDomainRepository.searchProjectInsight(search, pageable);

        return list;
    }

    @Autowired
    private ProjectInsightDomainDataRepository projectInsightDomainDataRepository;

    public List<ProjectInsightDomainDataDto> getDomainsData(List<String> type, Long parentId) {
        List<ProjectInsightDomainDataDto> list = projectInsightDomainDataRepository.findDomainsByTypeAndParentId(type,parentId);

        return list;
    }
    
}