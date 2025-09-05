package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ApiSourceDTO;
import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.DomainInfo;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.HierarchyOptionDTO;
import com.apmosys.employeeportal.dto.ParentCount;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.dto.ProjectInsightDomainDTO;
import com.apmosys.employeeportal.enums.ProjectInsightDomainApprovedStatus;
import com.apmosys.employeeportal.model.ApiSource;
import com.apmosys.employeeportal.model.Domain;
import com.apmosys.employeeportal.model.ProjectInsightDomain;
import com.apmosys.employeeportal.model.ProjectInsightServiceModel;
import com.apmosys.employeeportal.model.ProjectInsightSubDomain;
import com.apmosys.employeeportal.model.ProjectInsightSubService;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightStructure;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightQuestionDetailsRepository;
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightStructureRepository;
import com.apmosys.employeeportal.repository.ApiSourceRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.ProjectInsightDomainRepository;
import com.apmosys.employeeportal.repository.ProjectInsightServiceModelRepository;
import com.apmosys.employeeportal.repository.ProjectInsightSubDomainRepository;
import com.apmosys.employeeportal.repository.ProjectInsightSubServiceRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;

import lombok.Data;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.apmosys.employeeportal.model.ProjectInsightDomainData;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightProjectDetails;
import com.apmosys.employeeportal.repository.ProjectInsightDomainDataRepository;

@Service
public class ApiSourceService {

	@Autowired
	ApiSourceRepository apiSourceRepository;

	@Autowired
	ProjectRepository projectRepository;

	@Autowired
	DepartmentRepository departmentRepository;

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	ProjectInsightDomainRepository projectInsightDomainRepository;

	@Autowired
	ProjectInsightSubDomainRepository projectInsightSubDomainRepository;

	@Autowired
	ProjectInsightServiceModelRepository projectInsightServiceModelRepository;

	@Autowired
	ProjectInsightSubServiceRepository projectInsightSubServiceRepository;

	@Autowired
	ProjectInsightStructureRepository projectInsightStructureRepository;

	@Autowired
	private ProjectInsightQuestionDetailsRepository projectInsightQuestionDetailsRepository;

	public List<ApiSourceDTO> getAllApiList() {
		try {
			List<ApiSource> sources = apiSourceRepository.findAll();
			return sources.stream()
					.map(api -> new ApiSourceDTO(
							api.getApiSourceId(),
							api.getLabel(),
							api.getUrl(),
							api.getLabelKey(),
							api.getValueKey(),
							api.getIsdependent()))
					.collect(Collectors.toList());

		} catch (Exception e) {
			throw new RuntimeException("Unable to fetch API source list at this time. Please try again later.");
		}
	}

	public List<ProjectDTO> getAllProject() {
		try {
			return projectRepository.getAllProjectNameAndProjectManagerId();
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong.", e);
		}
	}

	public List<ProjectDTO> getClientByProjectId(String id) {
		try {
			return projectRepository.getClientByProjectId(Integer.parseInt(id));
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong.", e);
		}
	}

	public List<DepartmentDTO> getAllDepartment() {
		try {
			return departmentRepository.findAll().stream()
					.map(obj -> {
						DepartmentDTO newDeptDto = new DepartmentDTO();
						newDeptDto.setDeptId(obj.getDeptId());
						newDeptDto.setDeptName(obj.getName());
						return newDeptDto;
					})
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong.", e);
		}
	}

	public List<EmployeeDTO> getAllEmployee() {
		try {
			return employeeRepository.getAllEmployeeAsApiSource();
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong.", e);
		}
	}

	@Autowired
	private ProjectInsightDomainDataRepository projectInsightDomainDataRepository;

	public List<HierarchyOptionDTO> getAllDomain() {
		try {
			List<ProjectInsightDomainData> domains = projectInsightDomainDataRepository.findAllApprovedDomain();
			// domains = domains.stream()
			// .filter(d -> !"rejected".equalsIgnoreCase(d.getIsApproved()) &&
			// d.getIsActive() != false)
			// .collect(Collectors.toList());
			List<HierarchyOptionDTO> result = new ArrayList<>();
			for (ProjectInsightDomainData domain : domains) {
				boolean isChildAvailable = domain.getChildren() != null && !domain.getChildren().isEmpty();
				String hierarchyType = "domain";
				HierarchyOptionDTO dto = new HierarchyOptionDTO();
				dto.setId(domain.getId());
				dto.setName(domain.getName());
				dto.setIsChildAvailable(isChildAvailable);
				dto.setHierarchyType(hierarchyType);
				dto.setIsActive(domain.getIsActive());
				result.add(dto);
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Something went wrong.", e);
		}
	}

	public List<HierarchyOptionDTO> getAllNextFieldAndOption(String id, String type) {
		List<HierarchyOptionDTO> result = new ArrayList<>();
		ProjectInsightDomainData data = projectInsightDomainDataRepository.findByIdAndType(Long.valueOf(id), type);
		if (data != null) {
			if (data.getChildren() != null && !data.getChildren().isEmpty()) {
				for (ProjectInsightDomainData d : data.getChildren()) {
					boolean hasChildren = d.getChildren() != null && !d.getChildren().isEmpty();
					HierarchyOptionDTO dto = new HierarchyOptionDTO();
					dto.setId(d.getId());
					dto.setName(d.getName());
					dto.setIsChildAvailable(hasChildren);
					dto.setIsActive(d.getIsActive());
					dto.setHierarchyType(
							d.getType().equalsIgnoreCase("domain") ? "DOMAIN"
									: d.getType().equalsIgnoreCase("subDomain") ? "SUBDOMAIN"
											: d.getType().equalsIgnoreCase("service") ? "SERVICE" : "SUBSERVICE");
					result.add(dto);
				}
			}
		}
		return result;
	}

	@Autowired
	private MongoTemplate mongoTemplate;

	public Map<String, DomainInfo> findIdsWithDomainKey() {
		Query query = new Query(Criteria.where("additionalInfo.domainname").exists(true));

		List<ProjectInsightProjectDetails> results = mongoTemplate.find(query, ProjectInsightProjectDetails.class);

		Set<Long> domainIds = new HashSet<>();
		List<String> domainIdsInProject = new ArrayList<>();
		Map<String, Set<String>> clientMap = new HashMap<>(); // {banking : {axis bank, }}

		for (ProjectInsightProjectDetails result : results) {
			Object domainField = result.getAdditionalInfo().get("domainname");

			if (domainField instanceof List<?>) {
				for (Object idObj : (List<?>) domainField) {
					if (idObj instanceof Map) {
						Map<String, Object> map = (Map<String, Object>) idObj;
						if (map.containsKey("id")) {
							Object val = map.get("id");
							// String key = result.getId().toString();
							clientMap.putIfAbsent(val.toString(), new HashSet<>());
							clientMap.get(val.toString()).add(result.getClient().getClientName());
							if (val instanceof Number) {
								domainIds.add(((Number) val).longValue());
							}
						}
					} else if (idObj instanceof Number) {
						clientMap.putIfAbsent(idObj.toString(), new HashSet<>());
						clientMap.get(idObj.toString()).add(result.getClient().getClientName());
						domainIds.add(((Number) idObj).longValue());
					}
				}
				domainIdsInProject.add(result.getId().toString());
			}
		}

		List<ProjectInsightDomainData> domainEntities = projectInsightDomainDataRepository.findAllById(domainIds);

		// Ensure domainIdsInProject contains strings that match the parentPathIds format
		List<String> domainIdsInProjectStrings = domainIdsInProject.stream()
			.map(Object::toString)
			.collect(Collectors.toList());

		Aggregation aggregation = Aggregation.newAggregation(
			Aggregation.unwind("parentPathIds"),
			Aggregation.match(Criteria.where("parentPathIds").in(domainIdsInProject)),
			Aggregation.group("parentPathIds").count().as("count")
		);

		// Execute
		AggregationResults<Document> parentResults = mongoTemplate.aggregate(
			aggregation,
			"project_insight_questions_details", // collection name
			Document.class
		);

		Map<String, String> parentPathIdToCount = new HashMap<>();
		for (Document doc : parentResults.getMappedResults()) {
			String id = doc.getString("_id");
			String count = doc.get("count").toString();
			parentPathIdToCount.put(id, count);
		}

		Map<Long, ProjectInsightDomainData> domainIdToEntity = domainEntities.stream()
				.collect(Collectors.toMap(ProjectInsightDomainData::getId, d -> d));

		Map<String, DomainInfo> domainMap = new HashMap<>();

		for (ProjectInsightProjectDetails result : results) {
			List<Object> domainField = (List<Object>) result.getAdditionalInfo().get("domainname");
			for (Object domainIdObj : domainField) {
				if (domainIdObj instanceof Map) {
					Map<String, Object> map = (Map<String, Object>) domainIdObj;
					if (map.containsKey("id")) {
						Object val = map.get("id");
						if (val instanceof Number) {
							Long domainId = ((Number) val).longValue();
							ProjectInsightDomainData domainEntity = domainIdToEntity.get(domainId);
							if(domainEntity == null || domainEntity.getName() == null) {
								continue;
							}
							if (!domainMap.containsKey(domainEntity.getName())) {
								DomainInfo domainInfo = new DomainInfo();
								domainInfo.setColor(domainEntity.getDomaincolorCode());
								domainInfo.setData(result.getProjectName());
								domainInfo.setTotalQuestions(parentPathIdToCount.get(result.getId().toString()) != null ? parentPathIdToCount.get(result.getId().toString()) : "0");
								domainInfo.setTotalClients(clientMap.get(domainId.toString()));
								domainMap.put(domainEntity.getName(), domainInfo);
							} else {
								// domainMap.get(domainEntity.getName()).setTotalQuestions(parentPathIdToCount.get(result.getId().toString()));
								if(parentPathIdToCount.get(result.getId().toString()) != null) {
									Long count = Long.parseLong(parentPathIdToCount.get(result.getId().toString()));
									domainMap.get(domainEntity.getName()).setTotalQuestions(String.valueOf(count + Long.parseLong(domainMap.get(domainEntity.getName()).getTotalQuestions())));
								}
								domainMap.get(domainEntity.getName()).getTotalClients()
										.addAll(clientMap.get(domainId.toString()));
								domainMap.get(domainEntity.getName()).getData().add(result.getProjectName());
							}
						}
					}
				} else if (domainIdObj instanceof Number) {
					Long domainId = ((Number) domainIdObj).longValue();
					ProjectInsightDomainData domainEntity = domainIdToEntity.get(domainId);
					if (!domainMap.containsKey(domainEntity.getName())) {
						DomainInfo domainInfo = new DomainInfo();
						domainInfo.setTotalQuestions(parentPathIdToCount.get(result.getId().toString()));
						domainInfo.setTotalClients(clientMap.get(domainId.toString()));
						domainInfo.setColor(domainEntity.getDomaincolorCode());
						domainInfo.setData(new HashSet<String>(Arrays.asList(result.getProjectName())));
						domainMap.put(domainEntity.getName(), domainInfo);
					} else {
						if(parentPathIdToCount.get(result.getId().toString()) != null) {
									Long count = Long.parseLong(parentPathIdToCount.get(result.getId().toString()));
									domainMap.get(domainEntity.getName()).setTotalQuestions(String.valueOf(count + Long.parseLong(domainMap.get(domainEntity.getName()).getTotalQuestions())));
								}
						domainMap.get(domainEntity.getName()).getTotalClients()
								.addAll(clientMap.get(domainId.toString()));
						domainMap.get(domainEntity.getName()).getData().add(result.getProjectName());
					}
				}
			}

		}

		return domainMap;
	}

}
