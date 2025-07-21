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
			return projectRepository.findAll()
					.stream()
					.map(obj -> {
						ProjectDTO newdto = new ProjectDTO();
						newdto.setProjectId(obj.getProjectId());
						newdto.setProjectName(obj.getProjectName());
						return newdto;
					})
					.collect(Collectors.toList());
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

	public List<HierarchyOptionDTO> getAllDomain() {
		try {
			List<ProjectInsightDomain> domains = projectInsightDomainRepository.findAll();
			domains = domains.stream()
					.filter(d -> !ProjectInsightDomainApprovedStatus.rejected.equals(d.getIsApproved())
							&& d.getIsActive() != false)
					.collect(Collectors.toList());
			List<HierarchyOptionDTO> result = new ArrayList<>();
			for (ProjectInsightDomain domain : domains) {
				boolean hasSubDomains = domain.getSubDomains() != null && !domain.getSubDomains().isEmpty();
				boolean hasServices = domain.getServices() != null && !domain.getServices().isEmpty();
				boolean isChildAvailable = hasSubDomains || hasServices;
				String hierarchyType = "DOMAIN";
				HierarchyOptionDTO dto = new HierarchyOptionDTO();
				dto.setId(domain.getDomainId());
				dto.setName(domain.getDomain());
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
		switch (type.toUpperCase()) {
			case "DOMAIN":
				Optional<ProjectInsightDomain> domainOpt = projectInsightDomainRepository.findById(Long.valueOf(id));
				if (domainOpt.isPresent()) {
					ProjectInsightDomain domain = domainOpt.get();
					// Add subdomains if present
					if (domain.getSubDomains() != null && !domain.getSubDomains().isEmpty()) {
						for (ProjectInsightSubDomain subDomain : domain.getSubDomains()) {
							boolean hasServices = subDomain.getServices() != null && !subDomain.getServices().isEmpty();
							HierarchyOptionDTO dto = new HierarchyOptionDTO();
							dto.setId(subDomain.getSubDomainId());
							dto.setName(subDomain.getSubDomain());
							dto.setIsChildAvailable(hasServices);
							dto.setHierarchyType("SUBDOMAIN");
							result.add(dto);
						}
					}
					// Add services if present (even if subdomains exist)
					if (domain.getServices() != null && !domain.getServices().isEmpty()) {
						for (ProjectInsightServiceModel service : domain.getServices()) {
							boolean hasSubServices = service.getSubServices() != null
									&& !service.getSubServices().isEmpty();
							HierarchyOptionDTO dto = new HierarchyOptionDTO();
							dto.setId(service.getServiceId());
							dto.setName(service.getService());
							dto.setIsChildAvailable(hasSubServices);
							dto.setHierarchyType("SERVICE");
							result.add(dto);
						}
					}
				}
				break;

			case "SUBDOMAIN":
				Optional<ProjectInsightSubDomain> subDomainOpt = projectInsightSubDomainRepository
						.findById(Long.valueOf(id));
				if (subDomainOpt.isPresent()) {
					ProjectInsightSubDomain subDomain = subDomainOpt.get();
					if (subDomain.getServices() != null && !subDomain.getServices().isEmpty()) {
						for (ProjectInsightServiceModel service : subDomain.getServices()) {
							boolean hasSubServices = service.getSubServices() != null
									&& !service.getSubServices().isEmpty();
							HierarchyOptionDTO dto = new HierarchyOptionDTO();
							dto.setId(service.getServiceId());
							dto.setName(service.getService());
							dto.setIsChildAvailable(hasSubServices);
							dto.setHierarchyType("SERVICE");
							result.add(dto);
						}
					}
				}
				break;

			case "SERVICE":
				Optional<ProjectInsightServiceModel> serviceOpt = projectInsightServiceModelRepository
						.findById(Long.valueOf(id));
				if (serviceOpt.isPresent()) {
					ProjectInsightServiceModel service = serviceOpt.get();
					if (service.getSubServices() != null && !service.getSubServices().isEmpty()) {
						for (ProjectInsightSubService subService : service.getSubServices()) {
							boolean hasChildren = subService.getChildren() != null
									&& !subService.getChildren().isEmpty();
							HierarchyOptionDTO dto = new HierarchyOptionDTO();
							dto.setId(subService.getId());
							dto.setName(subService.getSubService());
							dto.setIsChildAvailable(hasChildren);
							dto.setHierarchyType("SUBSERVICE");
							result.add(dto);
						}
					}
				}
				break;

			case "SUBSERVICE":
				Optional<ProjectInsightSubService> subServiceOpt = projectInsightSubServiceRepository
						.findById(Long.valueOf(id));
				if (subServiceOpt.isPresent()) {
					ProjectInsightSubService subService = subServiceOpt.get();
					if (subService.getChildren() != null && !subService.getChildren().isEmpty()) {
						for (ProjectInsightSubService child : subService.getChildren()) {
							boolean hasChildren = child.getChildren() != null && !child.getChildren().isEmpty();
							HierarchyOptionDTO dto = new HierarchyOptionDTO();
							dto.setId(child.getId());
							dto.setName(child.getSubService());
							dto.setIsChildAvailable(hasChildren);
							dto.setHierarchyType("SUBSERVICE");
							result.add(dto);
						}
					}
				}
				break;
			default:
				break;
		}
		return result;
	}

	public Map<String, Set<String>> findIdsWithDomainKey() {
		// Query query = new Query(new Criteria().orOperator(
		// Criteria.where("data.fields.domain").exists(true),
		// Criteria.where("data.child.fields.domain").exists(true)));

		// query.fields().include("id"); // Project only the ID field

		List<ProjectInsightStructure> results = projectInsightStructureRepository.findAll();
		Map<String, Set<String>> domainMap = new HashMap<>();

		// Map<String, Set<String>> domainMap = results.stream()
		// .filter(p -> p.getData() != null
		// && p.getData().getFields() != null
		// && p.getData().getFields().get("domain") != null
		// && p.getStructure() != null
		// && p.getStructure().getFormName() != null)
		// .flatMap(DomainMapper::toDomainEntries)
		// .collect(Collectors.groupingBy(
		// Map.Entry::getKey,
		// Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
		// ));

		List<Object> objectIds = results.stream()
				.filter(p -> p.getData() != null && p.getData().getFields() != null
						&& p.getData().getFields().get("domain") != null && p.getStructure() != null
						&& p.getStructure().getFormName() != null)
				.map(p -> p.getData().getFields().get("domain"))
				.collect(Collectors.toList());

		List<Long> ids = new ArrayList<>();

		for (Object id : objectIds) {
			for(Object id1 : (List<Object>) id) {
				ids.add(Long.valueOf(id1.toString()));
			}
		}

		List<ProjectInsightDomain> allDomainsWithName = projectInsightDomainRepository.findAllById(ids);

		for (ProjectInsightStructure p : results) {
			if (p.getData() != null && p.getData().getFields() != null
					&& p.getData().getFields().get("domain") != null && p.getStructure() != null
					&& p.getStructure().getFormName() != null) {
				List<Object> domainList = (List<Object>) p.getData().getFields().get("domain");

				for (Object domain : domainList) {
					String domainKey = domain.toString();
					String actualName = "";
					for(ProjectInsightDomain projectInsightDomain : allDomainsWithName) {
						if(projectInsightDomain.getDomainId().toString().equals(domainKey)) {
							actualName = projectInsightDomain.getDomain();
							break;
						}
					}

					Set<String> formNames = domainMap.getOrDefault(actualName, new HashSet<>());
					formNames.add(p.getStructure().getFormName());
					domainMap.put(actualName, formNames);
				}

			}
		}

		return domainMap;
	}

}
