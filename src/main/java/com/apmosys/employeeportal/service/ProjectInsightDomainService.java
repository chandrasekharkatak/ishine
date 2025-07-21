package com.apmosys.employeeportal.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.apmosys.employeeportal.dto.ProjectInsightDomainCreatedBy;
import com.apmosys.employeeportal.dto.ProjectInsightDomainDTO;
import com.apmosys.employeeportal.dto.ProjectInsightEditDomainDTO;
import com.apmosys.employeeportal.dto.ProjectInsightServiceDTO;
import com.apmosys.employeeportal.repository.ProjectInsightServiceRepository;
import com.apmosys.employeeportal.repository.ProjectInsightSubDomainRepository;
import com.apmosys.employeeportal.repository.ProjectInsightSubServiceRepository;

import org.springframework.beans.factory.annotation.Autowired;

import com.apmosys.employeeportal.dto.ProjectInsightSubDomainDTO;
import com.apmosys.employeeportal.dto.ProjectInsightSubServiceDTO;
import com.apmosys.employeeportal.enums.ProjectInsightDomainApprovedStatus;
import com.apmosys.employeeportal.model.ProjectInsightDomain;
import com.apmosys.employeeportal.model.ProjectInsightSubDomain;
import com.apmosys.employeeportal.model.ProjectInsightSubService;
import com.apmosys.employeeportal.repository.ProjectInsightDomainRepository;

import com.apmosys.employeeportal.model.ProjectInsightServiceModel;

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

    @Transactional
    public Map<String, Object> createProjectInsightDomain(ProjectInsightDomainDTO projectInsightDomainDTO, Long createdBy) {
        if (projectInsightDomainRepository.existsByDomain(projectInsightDomainDTO.getDomain())) {
            throw new RuntimeException("Domain already exists");
        }

        ProjectInsightDomain domain = new ProjectInsightDomain();
        domain.setDomain(projectInsightDomainDTO.getDomain());
        domain.setCreatedBy(createdBy);
        domain.setIsActive(true);
        domain.setIsApproved(ProjectInsightDomainApprovedStatus.pending);


        // Process subdomains
        for (ProjectInsightSubDomainDTO subDomainDTO : projectInsightDomainDTO.getSubDomainList()) {
            domain.getSubDomains().add(createSubDomainHierarchy(subDomainDTO, domain, null, createdBy));
        }

        for (ProjectInsightServiceDTO serviceDTO : projectInsightDomainDTO.getServiceList()) {
            domain.getServices().add(createServiceHierarchy(serviceDTO, null, createdBy, domain));
        }

        projectInsightDomainRepository.save(domain);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Domain created successfully");
        
        
        return response;
    }

    private ProjectInsightSubDomain createSubDomainHierarchy(ProjectInsightSubDomainDTO dto,
            ProjectInsightDomain domain, ProjectInsightSubDomain parent, Long createdBy) {
        ProjectInsightSubDomain subDomain = new ProjectInsightSubDomain();
        subDomain.setSubDomain(dto.getSubdomain());
        subDomain.setDomain(domain); // Set the domain object, not ID
        subDomain.setParentSubDomain(parent); // Set the parent object, not ID
        subDomain.setIsActive(true);

        // Process child subdomains
        for (ProjectInsightSubDomainDTO childDTO : dto.getSubDomainChildrenList()) {
            subDomain.getChildren().add(createSubDomainHierarchy(childDTO, null, subDomain, createdBy));
        }

        // Process services
        for (ProjectInsightServiceDTO serviceDTO : dto.getServiceList()) {
            subDomain.getServices().add(createServiceHierarchy(serviceDTO, subDomain, createdBy, null));
        }

        return subDomain;
    }

    private ProjectInsightServiceModel createServiceHierarchy(ProjectInsightServiceDTO dto,
            ProjectInsightSubDomain subDomain, Long createdBy, ProjectInsightDomain domain) {
        ProjectInsightServiceModel service = new ProjectInsightServiceModel();
        service.setService(dto.getService());
        service.setIsActive(true);
        service.setParentDomain(domain);
        service.setSubDomain(subDomain); // Set the subDomain object, not ID

        // Process subservices
        for (ProjectInsightSubServiceDTO subServiceDTO : dto.getSubServiceList()) {
            service.getSubServices().add(createSubServiceHierarchy(subServiceDTO, service, null, createdBy));
        }

        return service;
    }

    private ProjectInsightSubService createSubServiceHierarchy(ProjectInsightSubServiceDTO dto,
            ProjectInsightServiceModel service, ProjectInsightSubService parent, Long createdBy) {
        ProjectInsightSubService subService = new ProjectInsightSubService();
        subService.setSubService(dto.getSubService());
        subService.setService(service);
        subService.setParentSubService(parent);
        subService.setIsActive(true);
        subService.setCreatedBy(createdBy);

        // Process child subservices
        for (ProjectInsightSubServiceDTO childDTO : dto.getSubServiceChildren()) {
            subService.getChildren().add(createSubServiceHierarchy(childDTO, null, subService, createdBy));
        }

        return subService;
    }

    public List<ProjectInsightDomain> getAllProjectInsightDomains(List<Long> ids) {
        List<ProjectInsightDomain> domains = projectInsightDomainRepository.findAllById(ids);
        return domains;
    }

    public Long addNewData(ProjectInsightEditDomainDTO projectInsightEditDomainDTO) {
        if (projectInsightEditDomainDTO.getParent_id_name().equalsIgnoreCase("domain")) {
            if (projectInsightEditDomainDTO.getChildren_name().equalsIgnoreCase("subDomain")) {
                ProjectInsightDomain domain = projectInsightDomainRepository
                        .findById(projectInsightEditDomainDTO.getParent_id()).get();
                ProjectInsightSubDomain subDomain = new ProjectInsightSubDomain();
                subDomain.setChildren(null);
                subDomain.setParentSubDomain(null);
                subDomain.setServices(null);
                subDomain.setIsActive(true);
                subDomain.setDomain(domain);
                subDomain.setSubDomain(projectInsightEditDomainDTO.getName());
                ProjectInsightSubDomain subDomain1 = projectInsightSubDomainRepository.save(subDomain);
                return subDomain1.getSubDomainId();
            } else {
                ProjectInsightDomain domain = projectInsightDomainRepository
                        .findById(projectInsightEditDomainDTO.getParent_id()).get();
                ProjectInsightServiceModel service = new ProjectInsightServiceModel();
                service.setService(projectInsightEditDomainDTO.getName());
                service.setIsActive(true);
                service.setParentDomain(domain);
                service.setSubDomain(null);
                ProjectInsightServiceModel service1 = projectInsightServiceRepository.save(service);
                return service1.getServiceId();
            }

        } else if (projectInsightEditDomainDTO.getParent_id_name().equalsIgnoreCase("subdomain")) {
            if (projectInsightEditDomainDTO.getChildren_name().equalsIgnoreCase("subdomain")) {
                ProjectInsightSubDomain subDomain = projectInsightSubDomainRepository
                        .findById(projectInsightEditDomainDTO.getParent_id()).get();
                ProjectInsightSubDomain subDomain1 = new ProjectInsightSubDomain();
                subDomain1.setChildren(null);
                subDomain1.setParentSubDomain(subDomain);
                subDomain1.setServices(null);
                subDomain1.setDomain(null);
                subDomain1.setIsActive(true);
                subDomain1.setSubDomain(projectInsightEditDomainDTO.getName());
                ProjectInsightSubDomain subDomain2 = projectInsightSubDomainRepository.save(subDomain1);
                return subDomain2.getSubDomainId();
            } else {
                ProjectInsightSubDomain subDomain = projectInsightSubDomainRepository
                        .findById(projectInsightEditDomainDTO.getParent_id()).get();
                ProjectInsightServiceModel service = new ProjectInsightServiceModel();
                service.setService(null);
                service.setSubDomain(subDomain);
                service.setIsActive(true);
                service.setService(projectInsightEditDomainDTO.getName());
                ProjectInsightServiceModel service1 = projectInsightServiceRepository.save(service);
                return service1.getServiceId();
            }
        } else if (projectInsightEditDomainDTO.getParent_id_name().equalsIgnoreCase("service")) {
            ProjectInsightServiceModel service = projectInsightServiceRepository
                    .findById(projectInsightEditDomainDTO.getParent_id()).get();
            ProjectInsightSubService subService = new ProjectInsightSubService();
            subService.setChildren(null);
            subService.setParentSubService(null);
            subService.setService(service);
            subService.setIsActive(true);
            subService.setSubService(projectInsightEditDomainDTO.getName());
            ProjectInsightSubService subService1 = projectInsightSubServiceRepository.save(subService);
            return subService1.getId();

        } else if (projectInsightEditDomainDTO.getParent_id_name().equalsIgnoreCase("subservice")) {
            ProjectInsightSubService subService = projectInsightSubServiceRepository
                    .findById(projectInsightEditDomainDTO.getParent_id()).get();
            ProjectInsightSubService subService1 = new ProjectInsightSubService();
            subService1.setChildren(null);
            subService1.setParentSubService(subService);
            subService1.setService(null);
            subService1.setIsActive(true);
            subService1.setSubService(projectInsightEditDomainDTO.getName());
            ProjectInsightSubService subService2 = projectInsightSubServiceRepository.save(subService1);
            return subService2.getId();
        }

        return 0L;
    }

    public Page<ProjectInsightDomainCreatedBy> findAllDomainAndCreatedBy(Integer page, Integer limit) {

        Pageable pageable = PageRequest.of(page, limit);

        return projectInsightDomainRepository.findAllDomainAndCreatedBy(pageable);
    }

    public Page<ProjectInsightDomainCreatedBy> findAllDomainSearched(String domain, String createdBy,
            LocalDateTime createdOn, Boolean isActive, Integer page, Integer limit, ProjectInsightDomainApprovedStatus isApproved) {

        Pageable pageable = PageRequest.of(page, limit);

        return projectInsightDomainRepository.findAllDomainSearched(domain, createdBy, createdOn, isActive, isApproved, pageable);
    }

    public ProjectInsightDomain findDomain(String domain) {
        return projectInsightDomainRepository.findByDomain(domain);
    }

    public void softDelete(Long id, String type, String name) {
        if (type.equals("domain")) {
            if (name != null) {
                ProjectInsightDomain domain = projectInsightDomainRepository.findByDomain(name);
                if (domain != null) {
                    domain.setIsActive(!domain.getIsActive());
                    projectInsightDomainRepository.save(domain);
                } else {
                    throw new RuntimeException("Domain not found");
                }
            } else {
                ProjectInsightDomain domain = projectInsightDomainRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Domain not found"));
                domain.setIsActive(!domain.getIsActive());
                projectInsightDomainRepository.save(domain);
            }
        } else if (type.equalsIgnoreCase("subdomain")) {
            ProjectInsightSubDomain subDomain = projectInsightSubDomainRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("SubDomain not found"));
            subDomain.setIsActive(!subDomain.getIsActive());
            projectInsightSubDomainRepository.save(subDomain);
        } else if (type.equalsIgnoreCase("service")) {
            ProjectInsightServiceModel service = projectInsightServiceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Service not found"));
            service.setIsActive(!service.getIsActive());
            projectInsightServiceRepository.save(service);
        } else {
            ProjectInsightSubService subService = projectInsightSubServiceRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("SubService not found"));
            subService.setIsActive(!subService.getIsActive());
            projectInsightSubServiceRepository.save(subService);
        }
    }

    @Transactional
    public void editDomains(List<ProjectInsightEditDomainDTO> listProjectInsightEditDomainDTO) {

        for (ProjectInsightEditDomainDTO projectInsightEditDomainDTO : listProjectInsightEditDomainDTO) {
            if (projectInsightEditDomainDTO.getParent_id_name().equals("domain")) {
                projectInsightDomainRepository.updateDomainName(projectInsightEditDomainDTO.getParent_id(),
                        projectInsightEditDomainDTO.getName());
            } else if (projectInsightEditDomainDTO.getParent_id_name().equalsIgnoreCase("subdomain")) {
                projectInsightSubDomainRepository.updateSubDomainName(projectInsightEditDomainDTO.getParent_id(),
                        projectInsightEditDomainDTO.getName());
            } else if (projectInsightEditDomainDTO.getParent_id_name().equalsIgnoreCase("service")) {
                projectInsightServiceRepository.updateServiceName(projectInsightEditDomainDTO.getParent_id(),
                        projectInsightEditDomainDTO.getName());
            } else {
                projectInsightSubServiceRepository.updateSubServiceName(projectInsightEditDomainDTO.getParent_id(),
                        projectInsightEditDomainDTO.getName());
            }

        }
    }

    @Transactional
    public void approveDomain(Long domainId, ProjectInsightDomainApprovedStatus isApproved, Long approvedBy) {
        if( isApproved == ProjectInsightDomainApprovedStatus.approved){
            projectInsightDomainRepository.updateIsApproved(domainId, ProjectInsightDomainApprovedStatus.approved, approvedBy);
        } else{
            projectInsightDomainRepository.updateIsApproved(domainId, ProjectInsightDomainApprovedStatus.rejected, approvedBy);
        }
    }

}