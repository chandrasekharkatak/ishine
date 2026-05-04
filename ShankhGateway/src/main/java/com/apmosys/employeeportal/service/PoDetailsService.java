package com.apmosys.employeeportal.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.DeletedPoSyncDTO;
import com.apmosys.employeeportal.dto.IshineLinkProjectDto;
import com.apmosys.employeeportal.dto.PoClientAddressUpdateDTO;
import com.apmosys.employeeportal.dto.PoDetailsForProjectPoMappingDTO;
import com.apmosys.employeeportal.dto.ProjectHierarchyResultDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;
import com.apmosys.employeeportal.dto.RenewedPoSyncDto;
import com.apmosys.employeeportal.exception.DataNotFoundException;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.EmployeeClientSideIdMapping;
import com.apmosys.employeeportal.model.PoDepartmentMapping;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectHierarchyMapping;
import com.apmosys.employeeportal.model.ProjectManagerMapping;
import com.apmosys.employeeportal.model.ProjectOverheadMapping;
import com.apmosys.employeeportal.model.ProjectPoDetails;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.repository.EmpPrimaryProjectMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeClientSideIdMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.PoDepartmentMappingRepository;
import com.apmosys.employeeportal.repository.PoRequirementMappingRepository;
import com.apmosys.employeeportal.repository.ProjectManagerMappingRepository;
import com.apmosys.employeeportal.repository.ProjectOverheadMappingRepository;
import com.apmosys.employeeportal.repository.ProjectPoDetailsRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.repository.ProjectHierarchyMappingRepository;
import com.apmosys.employeeportal.repository.ProjectTimesheetStatusNewRepository;
import com.apmosys.employeeportal.repository.TimesheetActivityMapNewRepository;
import com.apmosys.employeeportal.repository.TimesheetActionAuditNewRepository;
import com.apmosys.employeeportal.repository.TimesheetDocumentDetailsNewRepository;
import com.apmosys.employeeportal.repository.TimesheetRejectionDetailsNewRepository;
import com.apmosys.employeeportal.repository.FinalDocumentNewRepository;
import com.apmosys.employeeportal.utility.ExceptionLogContext;

@Service
public class PoDetailsService {
	private static final Logger log = LoggerFactory.getLogger(PoDetailsService.class);

	@Autowired
	ClientService clientService;
	
	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	ProjectPoDetailsRepository projectPoDetailsRepository;
	
	@Autowired
	TeamRepository teamRepository;
	
	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	ValidationService validationService;
	
	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;
	
	@Autowired
	PoDepartmentMappingRepository poDepartmentMappingRepository;
	
	@Autowired
	PoRequirementMappingRepository poRequirementMappingRepository;
	
	@Autowired
	EmpPrimaryProjectMappingRepository empPrimaryProjectMappingRepository;

	@Autowired
	ProjectManagerMappingRepository projectManagerMappingRepository;
	
	@Autowired
	ProjectOverheadMappingRepository projectOverheadMappingRepository;

	@Autowired
	ProjectHierarchyMappingRepository projectHierarchyMappingRepository;

	@Autowired
	ProjectTimesheetStatusNewRepository projectTimesheetStatusNewRepository;
	
	@Autowired
	TimesheetActivityMapNewRepository timesheetActivityMapNewRepository;
	
	@Autowired
	TimesheetDocumentDetailsNewRepository timesheetDocumentDetailsNewRepository;
	
	@Autowired
	FinalDocumentNewRepository finalDocumentNewRepository;
	
	@Autowired
	TimesheetActionAuditNewRepository timesheetActionAuditNewRepository;
	
	@Autowired
	TimesheetRejectionDetailsNewRepository timesheetRejectionDetailsNewRepository;
	
	@Autowired
	private EmployeeClientSideIdMappingRepository employeeClientSideIdMappingRepository;
	
	@Autowired
	MailService mailService;

	@Autowired
	PoLinkImpactEmailBuilder poLinkImpactEmailBuilder;
	
	@Autowired
	ProjectService projectService;
	
	@Autowired
	TeamsService teamsService;
	
	@Value("${rmg.mail}")
	private String rmgMail;
	
	@Value("${bd.mail}")
	private String bdMail;

	@Value("${finance.mail}")
	private String financeMail;

	public ProjectPoDetails createPoRTS(Project project, ProjectPoMappingWithResourceDTO dto, Client client) {

		PoDetailsForProjectPoMappingDTO poDto = dto.getPoDetailsList().get(0);
		
		boolean exists =
		        projectPoDetailsRepository
		                .existsByPoIdAndProjectIdAndActiveTrue(
		                        poDto.getPoId(),
		                        project.getProjectId()
		                );

		if (exists) {
		    ExceptionLogContext.add(
		            "PO already exists in active state | poId="
		                    + poDto.getPoId()
		                    + " | projectId="
		                    + project.getProjectId()
		    );

		    throw new RuntimeException(
		            "PO already exists in active state for this project"
		    );
		}

		ClientLocation cl = clientService.resolveClientLocation(client.getClientId(), poDto.getClientLocation(),
				poDto.getClientState(),poDto.getClientAddressId());
		
	      validationService.validateEmployeeExists(poDto.getCreatedByEmpId(),poDto.getCreatedByEmpName());
//          validationService.validateEmployeeExists(poDto.getUpdatedByEmpId(),poDto.getUpdatedByEmpName());
          validationService.validateEmployeeExists(poDto.getApmosysRmEmpId(),poDto.getApmosysRmEmpName());
		
		
		ProjectPoDetails po = new ProjectPoDetails();
		po.setPoId(poDto.getPoId());
		po.setProjectId(project.getProjectId());
		po.setPoProjectId(dto.getProjectId());
		po.setPoNo(poDto.getPoNo());
		po.setPoStartDate(convert(poDto.getPoStartDate()));
		po.setPoEndDate(convert(poDto.getPoEndDate()));
		po.setClientLocationId(Long.valueOf(cl.getClientLocationId()));
		po.setClientAddressId(poDto.getClientAddressId());
		po.setCreatedBy(poDto.getCreatedByEmpId());
//		po.setUpdatedBy(poDto.getUpdatedByEmpId());
		po.setMsg(poDto.getCommentForRmg());
		po.setEmpIdApmosysRm(poDto.getApmosysRmEmpId());
		po.setApmosysRM(poDto.getApmosysRmEmpName());
		po.setApmosysRmEmail(poDto.getApmosysRmEmail());
		po.setClientRm(poDto.getClientRmName());
		po.setActive(true);
		po.setPrevPO(poDto.getPrevPo());
		po.setNextPO(poDto.getNextPO());
		po.setRenewable(poDto.isRenewable());
		po.setPoCreatedOn(convert(poDto.getCreatedOn()));
		po.setPoUpdatedOn(convert(poDto.getUpdatedOn()));
		
		return projectPoDetailsRepository.save(po);
	}
	
	private LocalDateTime convert(Date date) {
	    if (date == null) return null;

	    if (date instanceof java.sql.Date) {
	        return ((java.sql.Date) date)
	                .toLocalDate()
	                .atStartOfDay();
	    }

	    return date.toInstant()
	            .atZone(ZoneId.systemDefault())
	            .toLocalDateTime();
	}

	private Long validateAndGetEmployeeEmpId(String createdByEmpId, String createdByEmpName) {

		if (createdByEmpId == null || !createdByEmpId.startsWith("A-")) {
			ExceptionLogContext.add(
		            "createdByEmpId or createdByEmpName missing from PO"
		            + " | createdByEmpId=" + createdByEmpId
		            + " | createdByEmpName=" + createdByEmpName
		        );
			throw new RuntimeException("Invalid createdByEmpId format");
		}

		Long employmentId;
		try {
			employmentId = Long.parseLong(createdByEmpId.substring(2));
		} catch (NumberFormatException e) {
			 ExceptionLogContext.add(
			            "Invalid createdByEmpId format from PO"
			            + " | createdByEmpId=" + createdByEmpId
			        );
			throw new RuntimeException("Invalid employment id in createdByEmpId");
		}

		 return employeeRepository
		            .findByEmploymentIdAndEmployeeName(employmentId, createdByEmpName)
		            .orElseThrow(() -> {
		                ExceptionLogContext.add(
		                    "Employee mismatch from PO"
		                    + " | employmentId=" + employmentId
		                    + " | employeeName=" + createdByEmpName
		                );
		                return new RuntimeException(
		                    "Employee mismatch for createdBy employee"
		                );
		            });
	}
	
	public boolean updatePoIfChanged(
	        ProjectPoDetails po,
	        ProjectPoMappingWithResourceDTO dto,
	        Client client) {

	    PoDetailsForProjectPoMappingDTO poDto =
	            dto.getPoDetailsList().get(0);

	    boolean changed = false;
	    
	    validationService.validateEmployeeExists(poDto.getUpdatedByEmpId(),
                poDto.getUpdatedByEmpName());
	    
	    validationService.validateEmployeeExists(poDto.getApmosysRmEmpId(),poDto.getApmosysRmEmpName());

	    if (!Objects.equals(po.getPoNo(), poDto.getPoNo())) {
	        po.setPoNo(poDto.getPoNo());
	        changed = true;
	    }
	    
	    LocalDateTime dtoStartDate = convert(poDto.getPoStartDate());

	    if (!Objects.equals(po.getPoStartDate(), dtoStartDate)) {
	        po.setPoStartDate(dtoStartDate);
	        changed = true;
	    }

	    LocalDateTime dtoEndDate = convert(poDto.getPoEndDate());
	    
	    if (!Objects.equals(po.getPoEndDate(), dtoEndDate)) {
	        po.setPoEndDate(dtoEndDate);
	        changed = true;
	    }

	    ClientLocation cl = 
	            clientService.resolveClientLocation(
	                    client.getClientId(),
	                    poDto.getClientLocation(),
	                    poDto.getClientState(),poDto.getClientAddressId());

	    if (!Objects.equals(
	            po.getClientLocationId(),
	            Long.valueOf(cl.getClientLocationId()))) {

	        po.setClientLocationId(
	                Long.valueOf(cl.getClientLocationId()));
	        changed = true;
	    }

	    if (!Objects.equals(po.getMsg(), poDto.getCommentForRmg())) {
	        po.setMsg(poDto.getCommentForRmg());
	        changed = true;
	    }
	    
	    
	    if (!Objects.equals(po.isRenewable(), poDto.isRenewable())) {
	        po.setRenewable(poDto.isRenewable());
	        changed = true;
	    }
	    
	    if (!Objects.equals(po.getClientAddressId(), poDto.getClientAddressId())) {
	        po.setClientAddressId(poDto.getClientAddressId());
	        changed = true;
	    }
	    
	    if (!Objects.equals(po.getEmpIdApmosysRm(), poDto.getApmosysRmEmpId())) {
	        po.setApmosysRM(poDto.getApmosysRmEmpName());
	        po.setEmpIdApmosysRm(poDto.getApmosysRmEmpId());
	        changed = true;
	    }
	    
	    if (!Objects.equals(po.getApmosysRmEmail(), poDto.getApmosysRmEmail())) {
	        po.setApmosysRmEmail(poDto.getApmosysRmEmail());
	        changed = true;
	    }
	    
	    if (!Objects.equals(po.getClientRm(), poDto.getClientRmName())) {
	        po.setClientRm(poDto.getClientRmName());
	        changed = true;
	    }
	    
	    if (!Objects.equals(po.getPrevPO(), poDto.getPrevPo())) {
	        po.setPrevPO(poDto.getPrevPo());
	        changed = true;
	    }

	    if (!Objects.equals(po.getNextPO(), poDto.getNextPO())) {
	        po.setNextPO(poDto.getNextPO());
	        changed = true;
	    }
	    
	    


	   

	    if (changed) {
	        po.setUpdatedBy(poDto.getUpdatedByEmpId());
	       
	        po.setPoUpdatedOn(convert(poDto.getUpdatedOn()));

	        projectPoDetailsRepository.save(po);
	    }

	    return changed;

	   
	}
	
	public ProjectPoDetails createRenewedPo(
	        Project project,
	        RenewedPoSyncDto dto, Client client) {

	    PoDetailsForProjectPoMappingDTO poDto = dto.getRenewedPo();
	    ClientLocation cl = clientService.resolveClientLocation(client.getClientId(), poDto.getClientLocation(),
				poDto.getClientState(),poDto.getClientAddressId());
	    
	    validationService.validateEmployeeExists(dto.getRenewedByEmpId(),
        dto.getRenewedByEmpName());
	    
	    validationService.validateEmployeeExists(dto.getRenewedByEmpId(),
                dto.getRenewedByEmpName());
	    
	    validationService.validateEmployeeExists(poDto.getApmosysRmEmpId(),poDto.getApmosysRmEmpName());
	    
	    
	    ProjectPoDetails po = new ProjectPoDetails();
	    po.setPoId(poDto.getPoId());
	    po.setProjectId(project.getProjectId());
	    po.setPoProjectId(dto.getProjectId());
	    po.setPoNo(poDto.getPoNo());
	    po.setPoStartDate(convert(poDto.getPoStartDate()));
	    po.setPoEndDate(convert(poDto.getPoEndDate()));
	    po.setPrevPO(poDto.getPrevPo());
	    po.setNextPO(poDto.getNextPO());
	    po.setRenewable(poDto.isRenewable());
	    po.setActive(true);
	    po.setClientAddressId(poDto.getClientAddressId());
	    po.setClientLocationId(Long.valueOf(cl.getClientLocationId()));
	    po.setEmpIdApmosysRm(poDto.getApmosysRmEmpId());
	    po.setApmosysRM(poDto.getApmosysRmEmpName());
	    po.setApmosysRmEmail(poDto.getApmosysRmEmail());
	    po.setMsg(poDto.getCommentForRmg());
	    po.setClientRm(poDto.getClientRm());
	    po.setCreatedBy(dto.getRenewedByEmpId());	    
	    po.setUpdatedBy(dto.getRenewedByEmpId()); 
	    po.setPoCreatedOn(poDto.getCreatedOn() != null ? convert(poDto.getCreatedOn()) : null);
		po.setPoUpdatedOn(poDto.getUpdatedOn() != null ? convert(poDto.getUpdatedOn()) : null);
		
		po.setRenewable(poDto.isRenewable());   

	    return projectPoDetailsRepository.save(po);
	}
	
	public void validateAssociatedPosIntegrity(
	        Integer projectId,
	        List<PoDetailsForProjectPoMappingDTO> poListFromPortal) {

	    
	    Set<Long> portalPoIds =
	            poListFromPortal == null
	                    ? Collections.emptySet()
	                    : poListFromPortal.stream()
	                            .map(PoDetailsForProjectPoMappingDTO::getPoId)
	                            .filter(Objects::nonNull)
	                            .collect(Collectors.toSet());

	  
	    Set<Long> dbActivePoIds =
	            new HashSet<>(
	                    projectPoDetailsRepository
	                            .findActivePoIdsByProjectId(projectId)
	            );

	    
	    Set<Long> missingInDb = new HashSet<>(portalPoIds);
	    missingInDb.removeAll(dbActivePoIds);

	    Set<Long> extraInDb = new HashSet<>(dbActivePoIds);
	    extraInDb.removeAll(portalPoIds);

	   
	    if (!missingInDb.isEmpty() || !extraInDb.isEmpty()) {

	        if (!missingInDb.isEmpty()) {
	            ExceptionLogContext.add(
	                    "Associated PO(s) missing in iShine for projectId="
	                            + projectId
	                            + " | PO IDs from PO portal but not found in DB="
	                            + missingInDb
	            );
	        }

	        if (!extraInDb.isEmpty()) {
	            ExceptionLogContext.add(
	                    "Extra active PO(s) found in iShine for projectId="
	                            + projectId
	                            + " | PO IDs present in DB but missing in PO portal="
	                            + extraInDb
	            );
	        }

	        throw new RuntimeException(
	                "Associated PO integrity check failed for projectId="
	                        + projectId
	        );
	    }
	}
	
	public void updatePoLinksAfterRenewal(
	        Integer projectId,
	        RenewedPoSyncDto dto) {

	    Map<Long, PoDetailsForProjectPoMappingDTO> incomingMap =
	            dto.getAssociatePosAfterRenewal()
	                    .stream()
	                    .collect(Collectors.toMap(
	                            PoDetailsForProjectPoMappingDTO::getPoId,
	                            Function.identity()));

	    List<ProjectPoDetails> existing =
	            projectPoDetailsRepository
	                    .findByProjectIdAndActiveTrue(projectId);

	    for (ProjectPoDetails po : existing) {

	        PoDetailsForProjectPoMappingDTO incoming =
	                incomingMap.get(po.getPoId());

	        boolean changed = false;

	        if (!Objects.equals(po.getPrevPO(), incoming.getPrevPo())) {
	            po.setPrevPO(incoming.getPrevPo());
	            changed = true;
	        }

	        if (!Objects.equals(po.getNextPO(), incoming.getNextPO())) {
	            po.setNextPO(incoming.getNextPO());
	            changed = true;
	        }
	        
	        validationService.validateEmployeeExists(dto.getRenewedByEmpId(), dto.getRenewedByEmpName());

	        if (changed) {
	            po.setUpdatedBy(dto.getRenewedByEmpId());
	            po.setPoUpdatedOn(convert(dto.getRenewedOn()));       
	            projectPoDetailsRepository.save(po);
	        }
	    }
	}
	
	public ProjectPoDetails validateDeletedPoExists(
	        Integer projectId,
	        Long poId) {

	    return projectPoDetailsRepository
	            .findByProjectIdAndPoIdAndActiveTrue(projectId, poId)
	            .orElseThrow(() -> {
	                ExceptionLogContext.add(
	                        "Deleted PO not found or already inactive"
	                        + " | projectId=" + projectId
	                        + " | poId=" + poId
	                );
	                return new DataNotFoundException("Deleted PO does not exist or inactive");
	            });
	}
	
	public void validateNoActiveTeamsForPo(Long poId) {

		boolean hasActiveTeams =
				employeeTeamMapRepository.existsActiveTeams(poId);

	    if (hasActiveTeams) {
	        ExceptionLogContext.add(
	                "PO cannot be deleted due to active teams | poId=" + poId
	        );
	        throw new IllegalStateException(
	                "PO cannot be deleted as active teams exist"
	        );
	    }
	}
	
	public void softDeletePo(
	        ProjectPoDetails po,
	        Long deletedByEmpId,
	        String deletedByEmpName,
	        Date deletedOn) {
		
		validationService.validateEmployeeExists(deletedByEmpId,
	                    deletedByEmpName);

	    po.setActive(false);
	    po.setUpdatedBy(deletedByEmpId);
	    po.setPoUpdatedOn(convert(deletedOn));

	    projectPoDetailsRepository.save(po);
	    
	    
//	    List<PoDepartmentMapping> deptMappings =
//	            poDepartmentMappingRepository
//	                    .findByPoIdAndActiveTrue(po.getPoId());
//
//	    for (PoDepartmentMapping dm : deptMappings) {
//	        dm.setActive(false);
//	    }
//	    poDepartmentMappingRepository.saveAll(deptMappings);
//	    
//	    
//	    List<PoRequirementMapping> reqMappings =
//	            poRequirementMappingRepository
//	                    .findByPoIdAndActiveTrue(po.getPoId());
//
//	    for (PoRequirementMapping rm : reqMappings) {
//	        rm.setActive(false);
//	    }
//	    poRequirementMappingRepository.saveAll(reqMappings);
	}
	
	public void updatePoLinksAfterDeletion(
	        Integer projectId,
	        DeletedPoSyncDTO dto) {
		

	    Map<Long, PoDetailsForProjectPoMappingDTO> incomingMap =
	            dto.getAssociatePos().stream()
	                    .collect(Collectors.toMap(
	                            PoDetailsForProjectPoMappingDTO::getPoId,
	                            Function.identity()
	                    ));

	    List<ProjectPoDetails> activePos =
	            projectPoDetailsRepository.findByProjectIdAndActiveTrue(projectId);

	    for (ProjectPoDetails po : activePos) {

	        PoDetailsForProjectPoMappingDTO incoming =
	                incomingMap.get(po.getPoId());

	        if (incoming == null) continue;

	        boolean changed = false;

	        if (!Objects.equals(po.getPrevPO(), incoming.getPrevPo())) {
	            po.setPrevPO(incoming.getPrevPo());
	            changed = true;
	        }

	        if (!Objects.equals(po.getNextPO(), incoming.getNextPO())) {
	            po.setNextPO(incoming.getNextPO());
	            changed = true;
	        }
	        
	        validationService.validateEmployeeExists(
                    dto.getDeletedByEmpId(),
                    dto.getDeletedByEmpName()
            );

	        if (changed) {
	            po.setUpdatedBy(dto.getDeletedByEmpId());
	            po.setPoUpdatedOn(convert(dto.getDeletedOn())
	            );
	            projectPoDetailsRepository.save(po);
	        }
	    }
	}
	
	
	public void updatePoOrderOnly( Integer primaryProjectId, ProjectPoMappingWithResourceDTO primaryProjectDto) {

	    if (primaryProjectDto == null || primaryProjectDto.getPoDetailsList() == null || primaryProjectDto.getPoDetailsList().isEmpty()) {

	        ExceptionLogContext.add( "PO update skipped - empty payload | projectId=" + primaryProjectId);
	        return;
	    }

	    Map<Long, PoDetailsForProjectPoMappingDTO> incomingMap = primaryProjectDto.getPoDetailsList()
											                    .stream()
											                    .collect(Collectors.toMap(
											                            PoDetailsForProjectPoMappingDTO::getPoId,
											                            Function.identity()
											                    ));

	    List<ProjectPoDetails> existingPos = projectPoDetailsRepository.findByProjectIdAndActiveTrue(primaryProjectId);

	    if (existingPos == null || existingPos.isEmpty()) {
	        ExceptionLogContext.add(
	                "No active PO records found for projectId: "
	                        + primaryProjectId);
	        throw new RuntimeException(
	                "No active Purchase Orders are available for the selected project in iShine."
	        );
	    }

	    List<ProjectPoDetails> updatedPos = new ArrayList<>();

	    for (ProjectPoDetails existingPo : existingPos) {

	        PoDetailsForProjectPoMappingDTO incoming = incomingMap.getOrDefault(existingPo.getPoId(), null);

	        if (incoming == null) {
	            ExceptionLogContext.add(
	                "Incoming PO not found for poId: " + existingPo.getPoId()
	            );
	            continue;
	        }

	        boolean changed = false;

	        if (!Objects.equals(existingPo.getPrevPO(), incoming.getPrevPo())) {
	            existingPo.setPrevPO(incoming.getPrevPo());
	            changed = true;
	        }

	        if (!Objects.equals(existingPo.getNextPO(), incoming.getNextPO())) {
	            existingPo.setNextPO(incoming.getNextPO());
	            changed = true;
	        }

	        validationService.validateEmployeeExists(
	                incoming.getUpdatedByEmpId(),
	                incoming.getUpdatedByEmpName()
	        );

	        if (changed) {
	            existingPo.setUpdatedBy(incoming.getUpdatedByEmpId());
	            existingPo.setPoUpdatedOn(convert(incoming.getUpdatedOn()));
	            updatedPos.add(existingPo);
	        }
	    }

	    if (!updatedPos.isEmpty()) {
	        projectPoDetailsRepository.saveAll(updatedPos);
	    }
	}	
	
	public void validatePoLinkIntegrity(Project primaryProject, IshineLinkProjectDto dto) {

	    if (primaryProject == null) {
	        throw new RuntimeException("Primary project not found.");
	    }

	    if (dto == null || dto.getPrimaryProject() == null) {
	        throw new RuntimeException("Invalid project linking request.");
	    }

	    // Collect ALL projectIds (primary + deleted)
	    Set<Integer> allProjectIds = new HashSet<>();
	    allProjectIds.add(primaryProject.getProjectId());

	    if (dto.getDeletedProjects() != null && !dto.getDeletedProjects().isEmpty()) {

	        Set<Long> deletedPoProjectIds = dto.getDeletedProjects()
	                .stream()
	                .map(ProjectPoMappingWithResourceDTO::getProjectId)
	                .collect(Collectors.toSet());

	        // Fetch all deleted project entities in single query
	        List<Project> deletedEntities =  projectRepository.findByPoProjectIdIn(deletedPoProjectIds);

	        if (deletedEntities.size() != deletedPoProjectIds.size()) {
	            throw new RuntimeException("One or more deleted projects were not found.");
	        }

	        deletedEntities.forEach(p -> allProjectIds.add(p.getProjectId()));
	    }

	    // Fetch ALL ishine DB PO IDs in single query
	    Set<Long> dbTotalPos = projectPoDetailsRepository.findActivePoIdsByProjectIdIn(allProjectIds);

	    // Collect Payload POs safely
	    if (dto.getPrimaryProject().getPoDetailsList() == null ||
	        dto.getPrimaryProject().getPoDetailsList().isEmpty()) {

	        throw new RuntimeException(
	                "No PO details provided in linking request."
	        );
	    }

	    Set<Long> payloadPos =
	            dto.getPrimaryProject().getPoDetailsList()
	                    .stream()
	                    .map(PoDetailsForProjectPoMappingDTO::getPoId)
	                    .filter(Objects::nonNull)
	                    .collect(Collectors.toSet());

	    // Compare
	    Set<Long> missingInDb = new HashSet<>(payloadPos);
	    missingInDb.removeAll(dbTotalPos);

	    Set<Long> extraInDb = new HashSet<>(dbTotalPos);
	    extraInDb.removeAll(payloadPos);

	    if (!missingInDb.isEmpty() || !extraInDb.isEmpty()) {

	        ExceptionLogContext.add(
	                "PO integrity failed | MissingInDB=" + missingInDb +
	                " | ExtraInDB=" + extraInDb
	        );

	        throw new RuntimeException(
	                "PO validation failed at iShine's end. Please verify project linking configuration."
	        );
	    }
	}
	
	public void deactivateDeletedProjectsPos(
	        List<ProjectPoMappingWithResourceDTO> deletedProjects) {
		
		PoDetailsForProjectPoMappingDTO poDto = deletedProjects.get(0).getPoDetailsList().get(0);
		   validationService.validateEmployeeExists(
				poDto.getUpdatedByEmpId(),
				poDto.getUpdatedByEmpName()
        );
		LocalDateTime updatedOn =   convert(poDto.getUpdatedOn());
	    for (ProjectPoMappingWithResourceDTO dto : deletedProjects) {

	        Project project =
	                projectRepository.findByPoProjectId(dto.getProjectId());

	        List<ProjectPoDetails> pos =
	                projectPoDetailsRepository
	                        .findByProjectIdAndActiveTrue(project.getProjectId());

	        for (ProjectPoDetails po : pos) {
	            po.setActive(false);
	            po.setUpdatedBy(poDto.getUpdatedByEmpId());
	            po.setPoUpdatedOn(updatedOn);
	            projectPoDetailsRepository.save(po);
	        }
	    }
	}
	
	public void movePosToPrimaryProject(Project primaryProject, IshineLinkProjectDto dto) {

	    Integer primaryProjectId = primaryProject.getProjectId();

	    List<Long> deletedPoProjectIds = dto.getDeletedProjects()
								            .stream()
								            .map(ProjectPoMappingWithResourceDTO::getProjectId)
								            .collect(Collectors.toList());

	    // PRIMARY POS  //Po1
	    Map<Long, ProjectPoDetails> primaryPos = projectPoDetailsRepository
								                    .findByProjectIdAndActiveTrue(primaryProjectId)
								                    .stream()
								                    .collect(Collectors.toMap(ProjectPoDetails::getPoId, Function.identity()));

	    // DELETED POS // po2, po3
	    Map<Long, ProjectPoDetails> deletedPos = projectPoDetailsRepository
								                    .findByPoProjectIdInAndActiveTrue(deletedPoProjectIds)
								                    .stream()
								                    .collect(Collectors.toMap(ProjectPoDetails::getPoId, Function.identity()));

	    // PORTAL POS (contains primary + deleted together)   // po1,po3,
	    List<PoDetailsForProjectPoMappingDTO> incomingPos = dto.getPrimaryProject().getPoDetailsList();

	    for (PoDetailsForProjectPoMappingDTO incoming : incomingPos) {

	        ProjectPoDetails primaryPo = primaryPos.get(incoming.getPoId());
	        ProjectPoDetails deletedPo = deletedPos.get(incoming.getPoId());

	        if (primaryPo != null && deletedPo == null) {
	            updatePrimaryPo(primaryPo, incoming);
	        }
	        else if (primaryPo == null && deletedPo != null) {
	            moveDeletedPoToPrimary(primaryProjectId, deletedPo, incoming);
	        }
	        else {
	            throw new IllegalStateException(
	                    "Ivalid PO ownership corruption for poId=" + incoming.getPoId());
	        }
	    }
	}
	
	private void updatePrimaryPo(ProjectPoDetails existing, PoDetailsForProjectPoMappingDTO incoming) {

	    boolean changed = false;

	    if (!Objects.equals(existing.getPrevPO(), incoming.getPrevPo())) {
	        existing.setPrevPO(incoming.getPrevPo());
	        changed = true;
	    }

	    if (!Objects.equals(existing.getNextPO(), incoming.getNextPO())) {
	        existing.setNextPO(incoming.getNextPO());
	        changed = true;
	    }

	    if (existing.isRenewable() != incoming.isRenewable()) {
	        existing.setRenewable(incoming.isRenewable());
	        changed = true;
	    }

	    if (changed) {
	        validationService.validateEmployeeExists(
	                incoming.getUpdatedByEmpId(),
	                incoming.getUpdatedByEmpName());

	        existing.setUpdatedBy(incoming.getUpdatedByEmpId());
	        existing.setPoUpdatedOn(convert(incoming.getUpdatedOn()));

	        projectPoDetailsRepository.save(existing);
	    }
	}

	private void moveDeletedPoToPrimary( Integer primaryProjectId, ProjectPoDetails deletedPo, PoDetailsForProjectPoMappingDTO incoming) {

	    if (primaryProjectId == null) {
	    	ExceptionLogContext.add(
                    "Primary project ID not found in moveDeletedPoToPrimary method");
	        throw new RuntimeException("Error occured while linking at iShine's end.");
	    }

	    if (deletedPo == null) {
	    	ExceptionLogContext.add("Deleted project list has no PO");
	        throw new RuntimeException("Deleted PO record is invalid.");
	    }

	    if (incoming == null) {
	    	ExceptionLogContext.add("Incoming PO payload is null.");
	        throw new RuntimeException("Incoming PO payload is invalid.");
	    }

	    validationService.validateEmployeeExists(
	            incoming.getUpdatedByEmpId(),
	            incoming.getUpdatedByEmpName());

	    Project primaryProject = projectRepository
	            .findById(primaryProjectId)
	            .orElseThrow(() -> {
	                ExceptionLogContext.add(
	                        "Primary project not found | projectId=" + primaryProjectId);
	                return new RuntimeException(
	                        "Unable to process request. Primary project not found.");
	            });

	    Long poProjectId = primaryProject.getPoProjectId();

	    // Move PO to primary
	    deletedPo.setProjectId(primaryProjectId);
	    deletedPo.setPoProjectId(poProjectId);

	    if (!Objects.equals(deletedPo.getPrevPO(), incoming.getPrevPo())) {
	        deletedPo.setPrevPO(incoming.getPrevPo());
	    }

	    if (!Objects.equals(deletedPo.getNextPO(), incoming.getNextPO())) {
	        deletedPo.setNextPO(incoming.getNextPO());
	    }

	    if (deletedPo.isRenewable() != incoming.isRenewable()) {
	        deletedPo.setRenewable(incoming.isRenewable());
	    }

	    deletedPo.setUpdatedBy(incoming.getUpdatedByEmpId());
	    deletedPo.setPoUpdatedOn(convert(incoming.getUpdatedOn()));

	    projectPoDetailsRepository.save(deletedPo);
	}
	
	public void validateAllPosAreActive(List<Long> poIds) {

	    

		List<Long> activePoIds =
	            projectPoDetailsRepository.findDistinctActivePoIds(poIds);

	    Set<Long> activePoIdSet = new HashSet<>(activePoIds);

	  
	    List<Long> invalidPoIds = poIds.stream()
	            .filter(poId -> !activePoIdSet.contains(poId))
	            .collect(Collectors.toList());

	    if (!invalidPoIds.isEmpty()) {
	        throw new RuntimeException(
	            "These PO IDs are either not present in iShine or inactive: "
	                    + invalidPoIds
	        );
	    }
	}

	public void liftAndShiftTeamNew(IshineLinkProjectDto payloadDTO) {

	    if (payloadDTO == null
	    		|| payloadDTO.getPrimaryProject() == null
	    		|| payloadDTO.getDeletedProjects() == null
	    		|| payloadDTO.getDeletedProjects().isEmpty()) {
	    	log.info("liftAndShiftTeamNew: skipped (null payload, missing primary, or no deleted projects)");
	    	return;
	    }

	    Long updatedBy = fetchUpdatedBy(payloadDTO);

	    ProjectPoMappingWithResourceDTO primaryDTO = payloadDTO.getPrimaryProject();

	    Project primaryProject = projectRepository.findByPoProjectId(primaryDTO.getProjectId());
	    if (primaryProject == null || primaryProject.getProjectId() == null) {
	    	ExceptionLogContext.add("liftAndShiftTeamNew: primary project not found for poProjectId="
	    			+ primaryDTO.getProjectId());
	    	throw new RuntimeException("Primary project not found for PO link team lift.");
	    }

	    Set<String> primaryTeamNames = fetchPrimaryTeamNames(primaryDTO.getProjectId());

	    List<Project> deletedProjects = Optional.ofNullable(
	            projectRepository.findByPoProjectIdIn(
	                    payloadDTO.getDeletedProjects()
	                            .stream()
	                            .map(ProjectPoMappingWithResourceDTO::getProjectId)
	                            .filter(Objects::nonNull)
	                            .collect(Collectors.toSet())))
	            .orElse(Collections.emptyList());

	    Set<Long> deletedProjectIds =
	            deletedProjects.stream()
	                    .map(p -> Long.valueOf(p.getProjectId()))
	                    .collect(Collectors.toSet());

	    for (Project deleted : deletedProjects) {
	    	
	    	Long deletedProjectId = Long.parseLong(deleted.getProjectId().toString());
	    	Long primaryProjectId = Long.parseLong(primaryProject.getProjectId().toString());

	        handleTeamsLiftAndShift(
	                Long.valueOf(deleted.getProjectId()),
	                Long.valueOf(primaryProject.getProjectId()),
	                updatedBy,
	                primaryTeamNames,
	                deleted.getProjectName()
	        );

	        maintainHasClientSideId(deleted, primaryProject, updatedBy);

	        handlePoDepartmentMappings(deleted, primaryProject, updatedBy);

	        handleProjectManagerMappings(deletedProjectId, primaryProjectId, updatedBy);

	        handleProjectOverheadMappings(deletedProjectId, primaryProjectId, updatedBy);
	    }

	    handleEmployeeClientSideIdMapping(
	            primaryProject.getProjectId(),
	            deletedProjectIds,
	            updatedBy
	    );
	    
	    projectService.deactivateDeletedProjects(payloadDTO.getDeletedProjects());
	}

	public void migrateTimesheetsAndCreateHierarchyMappings(Project primaryProject, IshineLinkProjectDto payloadDTO) {
		if (primaryProject == null || primaryProject.getProjectId() == null) {
			return;
		}
		if (payloadDTO == null || payloadDTO.getDeletedProjects() == null || payloadDTO.getDeletedProjects().isEmpty()) {
			return;
		}

		Integer primaryProjectId = primaryProject.getProjectId();
		Long updatedBy = fetchUpdatedBy(payloadDTO);

		Set<Long> deletedPoProjectIds = payloadDTO.getDeletedProjects()
				.stream()
				.map(ProjectPoMappingWithResourceDTO::getProjectId)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

		if (deletedPoProjectIds.isEmpty()) {
			return;
		}

		List<Project> deletedProjects = projectRepository.findByPoProjectIdIn(deletedPoProjectIds);
		if (deletedProjects == null || deletedProjects.isEmpty()) {
			return;
		}

		List<Integer> deletedProjectIds = deletedProjects.stream()
				.map(Project::getProjectId)
				.filter(Objects::nonNull)
				.distinct()
				.collect(Collectors.toList());

		if (deletedProjectIds.isEmpty()) {
			return;
		}

		migrateTimesheetsToPrimaryProject(primaryProjectId, deletedProjectIds, updatedBy);
		createProjectHierarchyMappings(primaryProjectId, deletedProjectIds, updatedBy);
	}

	public void migrateResourcesAfterPoLink(
	        Integer projectId,
	        List<PoDetailsForProjectPoMappingDTO> poList) {

	    if (poList == null || poList.isEmpty()) {
	        return;
	    }

	    Long updatedBy = poList.stream()
	            .map(PoDetailsForProjectPoMappingDTO::getUpdatedByEmpId)
	            .filter(Objects::nonNull)
	            .findFirst()
	            .orElse(null);

	    if (updatedBy == null) {
	        throw new RuntimeException("UpdatedBy missing for PO migration");
	    }

	    for (PoDetailsForProjectPoMappingDTO po : poList) {

	        Long renewedPoId = po.getPoId();

	        if (renewedPoId == null) continue;

	        try {
	        	teamsService.migrateResourcesAfterRenewal(projectId, renewedPoId, updatedBy);
	        } catch (Exception e) {
	            ExceptionLogContext.add(
	                "Resource migration failed for poId=" + renewedPoId + " | " + e.getMessage()
	            );
	            throw e; 
	        }
	    }
	}
	
	private void migrateTimesheetsToPrimaryProject(
			Integer primaryProjectId,
			List<Integer> deletedProjectIds,
			Long updatedBy
	) {
		if (primaryProjectId == null || deletedProjectIds == null || deletedProjectIds.isEmpty()) {
			return;
		}
		// (ii) Option B: direct bulk UPDATE (assumes no PK collision)
		projectTimesheetStatusNewRepository.bulkMoveProjectTimesheetsToPrimary(
				primaryProjectId,
				deletedProjectIds,
				updatedBy
		);

		// employee_timesheet_activities_mapping_new
		timesheetActivityMapNewRepository.bulkMoveActivitiesProjectToPrimary(primaryProjectId, deletedProjectIds);

		// timesheet_document_details_new
		timesheetDocumentDetailsNewRepository.bulkMoveDocumentDetailsProjectToPrimary(primaryProjectId, deletedProjectIds,
				updatedBy);

		// final_document_new
		finalDocumentNewRepository.bulkMoveFinalDocumentsProjectToPrimary(primaryProjectId, deletedProjectIds, updatedBy);

		// timesheet_action_audit
		timesheetActionAuditNewRepository.bulkMoveActionAuditProjectToPrimary(primaryProjectId, deletedProjectIds);

		// timesheet_rejection_details_new
		timesheetRejectionDetailsNewRepository.bulkMoveRejectionDetailsProjectToPrimary(primaryProjectId, deletedProjectIds,
				updatedBy);
	}
	
	private void createProjectHierarchyMappings(
			Integer primaryProjectId,
			List<Integer> deletedProjectIds,
			Long updatedBy
	) {
		if (primaryProjectId == null || deletedProjectIds == null || deletedProjectIds.isEmpty()) {
			return;
		}
		
		// (i) Practical JPA approach: fetch existing mappings, filter, saveAll
		List<ProjectHierarchyMapping> existing =
				projectHierarchyMappingRepository.findByParentProjectIdAndChildProjectIdIn(primaryProjectId, deletedProjectIds);

		Set<Integer> existingChildIds = (existing == null || existing.isEmpty())
				? Collections.emptySet()
				: existing.stream()
					.map(ProjectHierarchyMapping::getChildProjectId)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());

		List<ProjectHierarchyMapping> toInsert = new ArrayList<>();
		for (Integer childId : deletedProjectIds) {
			if (childId == null) continue;
			if (existingChildIds.contains(childId)) continue;
			ProjectHierarchyMapping m = new ProjectHierarchyMapping();
			m.setParentProjectId(primaryProjectId);
			m.setChildProjectId(childId);
			m.setActive(true);
			m.setCreatedBy(updatedBy);
			m.setUpdatedBy(updatedBy);
			toInsert.add(m);
		}

		if (!toInsert.isEmpty()) {
			projectHierarchyMappingRepository.saveAll(toInsert);
		}
	}

	private Long fetchUpdatedBy(IshineLinkProjectDto payloadDTO) {

		if (payloadDTO == null) {
			throw new RuntimeException("Unable to derive updatedBy: payload is null");
		}

		Long updatedBy = null;
		String updatedByName = null;

		if (payloadDTO.getDeletedProjects() != null) {
			for (ProjectPoMappingWithResourceDTO del : payloadDTO.getDeletedProjects()) {
				if (del == null || del.getPoDetailsList() == null) {
					continue;
				}
				for (PoDetailsForProjectPoMappingDTO poDto : del.getPoDetailsList()) {
					if (poDto != null && poDto.getUpdatedByEmpId() != null) {
						updatedBy = poDto.getUpdatedByEmpId();
						updatedByName = poDto.getUpdatedByEmpName();
						break;
					}
				}
				if (updatedBy != null) {
					break;
				}
			}
		}

		if (updatedBy == null && payloadDTO.getPrimaryProject() != null
				&& payloadDTO.getPrimaryProject().getPoDetailsList() != null) {
			for (PoDetailsForProjectPoMappingDTO poDto : payloadDTO.getPrimaryProject().getPoDetailsList()) {
				if (poDto != null && poDto.getUpdatedByEmpId() != null) {
					updatedBy = poDto.getUpdatedByEmpId();
					updatedByName = poDto.getUpdatedByEmpName();
					break;
				}
			}
		}

		if (updatedBy == null) {
			throw new RuntimeException(
					"Unable to derive updatedBy from payload (no updatedByEmpId on deleted or primary PO rows)");
		}

		validationService.validateEmployeeExists(updatedBy, updatedByName);

		return updatedBy;
	}
	
	private Set<String> fetchPrimaryTeamNames(Long poProjectId) {

	    List<Object[]> teams = projectRepository.getTeamIdsForPoProjectId(poProjectId);

	    if (teams == null || teams.isEmpty()) {
	        return Collections.emptySet();
	    }

	    return teams.stream()
	            .map(t -> t[1] != null ? t[1].toString() : null)
	            .filter(Objects::nonNull)
	            .collect(Collectors.toSet());
	}

	private void maintainHasClientSideId(Project source, Project target, Long updatedBy) {

	    if (source == null || target == null) return;

	    if (Boolean.TRUE.equals(source.getHasClientSideId())) {
	        target.setHasClientSideId(true);
	        target.setUpdatedBy(updatedBy);
	        target.setUpdatedOn(LocalDateTime.now());
	        projectRepository.save(target);
	    }
	}
	
	private void handlePoDepartmentMappings( Project source, Project target, Long updatedBy) {

	    List<PoDepartmentMapping> sourceMappings = poDepartmentMappingRepository .findByProjectIdAndActiveTrue(source.getProjectId());

	    if (sourceMappings == null || sourceMappings.isEmpty()) {
	        return;
	    }

	    sourceMappings.forEach(m -> {
	        m.setActive(false);
	        m.setUpdatedBy(updatedBy);
	    });

	    poDepartmentMappingRepository.saveAll(sourceMappings);

	    List<PoDepartmentMapping> newMappings =
	            sourceMappings.stream().map(old -> {
	                PoDepartmentMapping nm = new PoDepartmentMapping();
	                nm.setPoId(old.getPoId());
	                nm.setDeptId(old.getDeptId());
	                nm.setProjectId(target.getProjectId());
	                nm.setActive(true);
	                nm.setCreatedBy(updatedBy);
	                return nm;
	            }).collect(Collectors.toList());

	    poDepartmentMappingRepository.saveAll(newMappings);
	}
	
	private void handleProjectManagerMappings( Long deletedProjectId, Long primaryProjectId, Long updatedBy) {

	    List<ProjectManagerMapping> sourceMappings = Optional
	    		.ofNullable(projectManagerMappingRepository.findByProjectIdAndActive(deletedProjectId, 1))
	    		.orElse(Collections.emptyList());

	    if (sourceMappings.isEmpty()) {
	        return;
	    }

	    sourceMappings.forEach(m -> {
	        m.setProjectId(primaryProjectId);
	        m.setUpdatedBy(updatedBy);
	        m.setUpdatedOn(LocalDateTime.now());
	    });

	    projectManagerMappingRepository.saveAll(sourceMappings);
	}

	private void handleProjectOverheadMappings( Long deletedProjectId, Long primaryProjectId, Long updatedBy) {

	    List<ProjectOverheadMapping> sourceMappings = Optional
	    		.ofNullable(projectOverheadMappingRepository.findByProjectIdAndActive(deletedProjectId, 1))
	    		.orElse(Collections.emptyList());

	    if (sourceMappings.isEmpty()) {
	        return;
	    }

	    sourceMappings.forEach(m -> {
	        m.setProjectId(primaryProjectId);
	        m.setUpdatedBy(updatedBy);
	    });

	    projectOverheadMappingRepository.saveAll(sourceMappings);
	}

	private void handleTeamsLiftAndShift(Long deletedProjectId, Long primaryProjectId, Long updatedBy,
	        Set<String> primaryTeamNames, String deletedProjectName) {

	    List<Team> teams = Optional
	    		.ofNullable(teamRepository.findActiveTeamsByProjectId(deletedProjectId.intValue()))
	    		.orElse(Collections.emptyList());

	    if (teams.isEmpty()) {
	    	log.info("liftAndShiftTeamNew: no active teams for deleted internal projectId={}; continuing PO link",
	    			deletedProjectId);
	        return;
	    }

	    teams.forEach(team -> {

	        String teamName = team.getTeamName();

	        if (teamName != null && primaryTeamNames.contains(teamName)) {
	            team.setTeamName(teamName + " | " + deletedProjectName);
	        }

	        team.setProjectId(primaryProjectId.intValue());
	        team.setUpdatedBy(updatedBy);
	        team.setUpdatedOn(LocalDateTime.now());
	    });

	    teamRepository.saveAll(teams);
	}
	
	private void handleEmployeeClientSideIdMapping( Integer primaryProjectId, Set<Long> deletedProjectIds, Long updatedBy) {

	    if (primaryProjectId == null || deletedProjectIds == null || deletedProjectIds.isEmpty()) {
	        return;
	    }

	    // 1. Fetch empIds from primary project (based on active teams & mappings)
	   
        List<Long> empIds = Optional
        		.ofNullable(employeeTeamMapRepository.findDistinctEmpIdsByProjectId(primaryProjectId))
        		.orElse(Collections.emptyList());
        if (empIds.isEmpty()) {
        	log.info(
        			"handleEmployeeClientSideIdMapping: no employees mapped on primary projectId={}; skipping client-side id remap",
        			primaryProjectId);
	        return;
	    }

	    // 2. Fetch client-side ID mappings for these empIds
	    //    where projectId belongs to any deleted project
	    List<EmployeeClientSideIdMapping> mappings = employeeClientSideIdMappingRepository
	                    .findByEmpIdInAndProjectIdInAndActive(empIds,new ArrayList<>(deletedProjectIds),true);

	    if (mappings == null || mappings.isEmpty()) {
	        return;
	    }

	    // 3. Replace deleted projectId with primary projectId

	    mappings.forEach(m -> {
	        m.setProjectId(Long.valueOf(primaryProjectId));
	        m.setUpdatedBy(updatedBy);
	        m.setUpdatedOn(LocalDateTime.now());
	    });

	    employeeClientSideIdMappingRepository.saveAll(mappings);
	    
	}
	
	
	
	public void updateClientAddressForPos(PoClientAddressUpdateDTO dto) {

		List<ProjectPoDetails> pos = projectPoDetailsRepository.findByPoIdInAndActive(dto.getPoIds());

		if (pos == null || pos.size() != dto.getPoIds().size()) {
			throw new IllegalArgumentException("Some PO IDs not found or inactive");
		}

		Map<Integer, List<ProjectPoDetails>> posByClient = pos.stream().collect(Collectors.groupingBy(po -> {

			Project project = projectRepository.findByProjectId(po.getProjectId());

			if (project == null || !"true".equalsIgnoreCase(project.getActive())) {
				throw new IllegalArgumentException("Inactive or missing project for PO: " + po.getPoId());
			}

			return project.getClientId();
		}));

		for (Map.Entry<Integer, List<ProjectPoDetails>> entry : posByClient.entrySet()) {

			Integer clientId = entry.getKey();

			ClientLocation clientLocation = clientService.resolveClientLocation(clientId, dto.getClientLocation(),
					dto.getClientState(), dto.getClientAddressId());

			if (clientLocation == null || clientLocation.getClientLocationId() == null) {
				throw new IllegalStateException("Resolved client location record is missing for clientId=" + clientId);
			}

			for (ProjectPoDetails po : entry.getValue()) {

				po.setClientAddressId(dto.getClientAddressId());
				po.setClientLocationId(Long.valueOf(clientLocation.getClientLocationId()));
				po.setUpdatedBy(dto.getUpdatedByEmpId());

			}
		}

		projectPoDetailsRepository.saveAll(pos);
	}

	public void sendPoLinkSuccessMail(Project primaryProject, IshineLinkProjectDto dto) {

	    try {
	    	if (primaryProject == null) {
	    		log.warn("sendPoLinkSuccessMail: primaryProject is null; skipping mail");
	    		return;
	    	}

	        // NOTE:
	        // In PoPortal payloads, ProjectPoMappingWithResourceDTO.projectId is actually poProjectId (Long),
	        // NOT the internal projects.projectId (Integer).
	        // ProjectManagerMapping / ProjectOverheadMapping store internal projectId as Long
	        List<Long> internalProjectIds = new ArrayList<>();

	        if (primaryProject != null && primaryProject.getProjectId() != null) {
	        	internalProjectIds.add(primaryProject.getProjectId().longValue());
	        }

	        if (dto != null && dto.getDeletedProjects() != null) {
		        for (ProjectPoMappingWithResourceDTO deleted : dto.getDeletedProjects()) {
		        	if (deleted == null) continue;
		        	Long deletedPoProjectId = deleted.getProjectId(); // poProjectId from PO portal payload
		        	if (deletedPoProjectId == null) continue;

		        	Project deletedProjectEntity = projectRepository.findByPoProjectId(deletedPoProjectId);
		        	if (deletedProjectEntity == null || deletedProjectEntity.getProjectId() == null) {
		        		continue;
		        	}

		        	internalProjectIds.add(deletedProjectEntity.getProjectId().longValue());
		        }
	        }

	        List<String> toEmails = projectRepository.findManagerAndOverheadEmails(internalProjectIds);

	        if (toEmails == null || toEmails.isEmpty()) {
	            System.err.println("No PM/Overhead emails found for PO link success mail");
	            return;
	        }

	        String receiver = String.join(",", toEmails);
	        String cc = String.join(",", rmgMail, bdMail, financeMail);

	        String projectName = primaryProject.getProjectName() != null ? primaryProject.getProjectName() : "";
	        String subject = "PO Linking Completed - " + projectName;

	        String body = poLinkImpactEmailBuilder.buildEmailBody(primaryProject, dto);

	        mailService.sendMailWithCC(
	        		receiver, 
	                cc, 
	                subject,
	                body
	        );
	        log.info("sendPoLinkSuccessMail: sent successfully for primaryProjectId={}",
	        		primaryProject.getProjectId());

	    } catch (Exception e) {
	        Integer pid = primaryProject != null ? primaryProject.getProjectId() : null;
	        log.error("sendPoLinkSuccessMail: failed primaryProjectId={}", pid, e);
	    }
	}
	
	public void createProjectHierarchyMapping(IshineLinkProjectDto dto) {

	    List<PoDetailsForProjectPoMappingDTO> poList =
	            dto.getPrimaryProject().getPoDetailsList();

	    if (poList == null || poList.isEmpty()) {
	        throw new RuntimeException("Primary project has no PO details!");
	    }

	    Set<Long> poIds = poList.stream()
	            .map(PoDetailsForProjectPoMappingDTO::getPoId)
	            .filter(Objects::nonNull)
	            .collect(Collectors.toSet());

	    if (poIds.isEmpty()) return;

	    List<Object[]> rawResults = projectPoDetailsRepository.findHierarchyFromAudit(poIds);

	    if (rawResults == null || rawResults.isEmpty()) return;

	    List<ProjectHierarchyResultDTO> results = rawResults.stream().map(row -> {

	        ProjectHierarchyResultDTO r = new ProjectHierarchyResultDTO();

	        r.setCurrPo((Long) row[0]);
	        r.setCurrProject((Integer) row[1]);
	        r.setChildPo((Long) row[2]);
	        r.setChildProjectId((Integer) row[3]);
	        r.setParentPo((Long) row[4]);
	        r.setParentProjectId((Integer) row[5]);

	        return r;

	    }).collect(Collectors.toList());

	    Set<String> uniquePairs = new HashSet<>();
	    List<ProjectHierarchyMapping> mappings = new ArrayList<>();

	    Long empId = poList.get(0).getUpdatedByEmpId();

	    for (ProjectHierarchyResultDTO row : results) {

	        Integer currentProject = row.getCurrProject();

	        if (row.getParentProjectId() != null) {

	            String key = row.getParentProjectId() + "-" + currentProject;

	            if (uniquePairs.add(key)) {
	                mappings.add(buildMapping(
	                        row.getParentProjectId(),
	                        currentProject,
	                        empId
	                ));
	            }
	        }

	        if (row.getChildProjectId() != null) {

	            String key = currentProject + "-" + row.getChildProjectId();

	            if (uniquePairs.add(key)) {
	                mappings.add(buildMapping(
	                        currentProject,
	                        row.getChildProjectId(),
	                        empId
	                ));
	            }
	        }
	    }

	    if (!mappings.isEmpty()) {
	        projectHierarchyMappingRepository.saveAll(mappings);
	    }
	}
	
	private ProjectHierarchyMapping buildMapping(Integer parentProjectId,
            Integer childProjectId,
            Long empId) {

		ProjectHierarchyMapping mapping = new ProjectHierarchyMapping();
		
		mapping.setParentProjectId(parentProjectId);
		mapping.setChildProjectId(childProjectId);
	
		mapping.setActive(true);
		
		mapping.setCreatedBy(empId);
		mapping.setUpdatedBy(empId);
		
		return mapping;
	}

}
