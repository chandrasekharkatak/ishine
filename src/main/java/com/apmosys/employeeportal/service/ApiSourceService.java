package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ApiSourceDTO;
import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.DomainInfo;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.HierarchyOptionDTO;
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
import com.apmosys.employeeportal.mongodb.repository.ProjectInsightStructureRepository;
import com.apmosys.employeeportal.repository.ApiSourceRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.ProjectInsightDomainRepository;
import com.apmosys.employeeportal.repository.ProjectInsightServiceModelRepository;
import com.apmosys.employeeportal.repository.ProjectInsightSubDomainRepository;
import com.apmosys.employeeportal.repository.ProjectInsightSubServiceRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.apmosys.employeeportal.model.ProjectInsightDomainData;
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
			List<ProjectInsightDomainData> domains = projectInsightDomainDataRepository.findAllActiveAndApprovedDomain();
			// domains = domains.stream()
			// 		.filter(d -> !"rejected".equalsIgnoreCase(d.getIsApproved())  && d.getIsActive() != false)
			// 		.collect(Collectors.toList());
			List<HierarchyOptionDTO> result = new ArrayList<>();
			for (ProjectInsightDomainData domain : domains) {
				boolean isChildAvailable = domain.getChildren() != null && !domain.getChildren().isEmpty();
				String hierarchyType = "domain";
				HierarchyOptionDTO dto = new HierarchyOptionDTO();
				dto.setId(domain.getId());
				dto.setName(domain.getName());
				dto.setIsChildAvailable(isChildAvailable);
				dto.setHierarchyType(hierarchyType);
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
					dto.setHierarchyType(
							d.getType().equalsIgnoreCase("domain") ? "DOMAIN" : d.getType().equalsIgnoreCase("subDomain") ? "SUBDOMAIN" : d.getType().equalsIgnoreCase("service") ? "SERVICE" : "SUBSERVICE"
						);
					result.add(dto);
				}
			}
		}
		return result;
	}

	@Autowired
	private MongoTemplate mongoTemplate;

	public Map<String, DomainInfo> findIdsWithDomainKey() {
		Query query = new Query(Criteria.where("data.fields.domain").exists(true));
		query.fields().include("id");
		query.fields().include("data.fields.domain");
		query.fields().include("data.fields.DomainLabel");
		query.fields().include("structure.formName");

		List<ProjectInsightStructure> results = mongoTemplate.find(query, ProjectInsightStructure.class);

		Set<Long> domainIds = new HashSet<>();
		for (ProjectInsightStructure result : results) {
			Object domainField = result.getData().getFields().get("domain");
			if (domainField instanceof List<?>) {
				for (Object idObj : (List<?>) domainField) {
					try {
						domainIds.add(Long.valueOf(idObj.toString()));
					} catch (NumberFormatException ignored) {}
				}
			}
		}

		List<ProjectInsightDomainData> domainEntities = projectInsightDomainDataRepository.findAllById(domainIds);

		// Map<Long, String> domainIdToName = domainEntities.stream()
		// .collect(Collectors.toMap(
		// 	d -> d.getId(),
		// 	d -> d.getName()
		// ));

		Map<Long, ProjectInsightDomainData> domainIdToEntity = domainEntities.stream()
    	.collect(Collectors.toMap(ProjectInsightDomainData::getId, d -> d));


		// Step 4: Build domainName -> formNames map
		// Map<String, Set<String>> domainMap = new HashMap<>();

		// for (ProjectInsightStructure result : results) {
		// 	Object domainField = result.getData().getFields().get("domain");
		// 	String formName = result.getStructure() != null ? result.getStructure().getFormName() : null;

		// 	if (formName != null && domainField instanceof List<?>) {
		// 		for (Object domainIdObj : (List<?>) domainField) {
		// 			try {
		// 				Long domainId = Long.valueOf(domainIdObj.toString());
		// 				String domainName = domainIdToName.get(domainId);
		// 				if (domainName != null) {
		// 					domainMap.computeIfAbsent(domainName, k -> new HashSet<>()).add(formName);
		// 				}
		// 			} catch (NumberFormatException ignored) {}
		// 		}
		// 	}
		// }

		Map<String, DomainInfo> domainMap = new HashMap<>();

		for (ProjectInsightStructure result : results) {
			Object domainField = result.getData().getFields().get("domain");
			String formName = result.getStructure() != null ? result.getStructure().getFormName() : null;

			if (formName != null && domainField instanceof List<?>) {
				for (Object domainIdObj : (List<?>) domainField) {
					try {
						Long domainId = Long.valueOf(domainIdObj.toString());
						ProjectInsightDomainData domainEntity = domainIdToEntity.get(domainId);

						if (domainEntity != null) {
							String domainName = domainEntity.getName();
							String color = domainEntity.getDomaincolorCode();

							domainMap
								.computeIfAbsent(domainName, k -> new DomainInfo(color))
								.getData()
								.add(formName);
						}
					} catch (NumberFormatException ignored) {}
				}
			}
		}


		return domainMap;
	}


}
