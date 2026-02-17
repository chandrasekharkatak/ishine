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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.DeletedPoSyncDTO;
import com.apmosys.employeeportal.dto.IshineLinkProjectDto;
import com.apmosys.employeeportal.dto.PoClientAddressUpdateDTO;
import com.apmosys.employeeportal.dto.PoDetailsForProjectPoMappingDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;
import com.apmosys.employeeportal.dto.RenewedPoSyncDto;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.EmployeeClientSideIdMapping;
import com.apmosys.employeeportal.model.PoDepartmentMapping;
import com.apmosys.employeeportal.model.PoRequirementMapping;
import com.apmosys.employeeportal.model.Project;
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
import com.apmosys.employeeportal.utility.ExceptionLogContext;

@Service
public class PoDetailsService {

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
	private EmployeeClientSideIdMappingRepository employeeClientSideIdMappingRepository;
	
	@Autowired
	MailService mailService;
	
	@Value("${rmg.mail}")
	private String rmgMail;
	
	@Value("${bd.mail}")
	private String bdMail;

	@Value("${finance.mail}")
	private String financeMail;

	public ProjectPoDetails createPoRTS(Project project, ProjectPoMappingWithResourceDTO dto, Client client) {

		PoDetailsForProjectPoMappingDTO poDto = dto.getPoDetailsList().get(0);

		ClientLocation cl = clientService.resolveClientLocation(client.getClientId(), poDto.getClientLocation(),
				poDto.getClientState(),poDto.getClientAddressId());
		
	      validationService.validateEmployeeExists(poDto.getCreatedByEmpId(),poDto.getCreatedByEmpName());
          validationService.validateEmployeeExists(poDto.getUpdatedByEmpId(),poDto.getUpdatedByEmpName());
		
		
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
		po.setUpdatedBy(poDto.getUpdatedByEmpId());
		po.setMsg(poDto.getCommentForRmg());
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

	    if (!Objects.equals(po.getPoNo(), poDto.getPoNo())) {
	        po.setPoNo(poDto.getPoNo());
	        changed = true;
	    }
	    
	    LocalDateTime dtoStartDate = convert(poDto.getPoEndDate());

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
	    
	    if (!Objects.equals(po.getApmosysRM(), poDto.getApmosysRmEmpName())) {
	        po.setApmosysRM(poDto.getApmosysRmEmpName());
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
	                return new RuntimeException("Deleted PO does not exist or inactive");
	            });
	}
	
	public void validateNoActiveTeamsForPo(Long poId) {

		boolean hasActiveTeams =
				employeeTeamMapRepository.existsActiveTeams(poId);

	    if (hasActiveTeams) {
	        ExceptionLogContext.add(
	                "PO cannot be deleted due to active teams | poId=" + poId
	        );
	        throw new RuntimeException(
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
	
	
	public void updatePoOrderOnly(
	        Integer primaryProjectId,
	        ProjectPoMappingWithResourceDTO primaryProjectDto) {

		if (primaryProjectDto == null || primaryProjectDto.getPoDetailsList() == null) {
	        ExceptionLogContext.add("PO update skipped - empty payload | projectId=" + primaryProjectId);
	        return;
	    }
	   
	    Map<Long, PoDetailsForProjectPoMappingDTO> incomingMap =
	            primaryProjectDto.getPoDetailsList()
	                    .stream()
	                    .collect(Collectors.toMap(
	                            PoDetailsForProjectPoMappingDTO::getPoId,
	                            Function.identity()
	                    ));

	   
	    List<ProjectPoDetails> existingPos =
	            projectPoDetailsRepository
	                    .findByProjectIdAndActiveTrue(primaryProjectId);

	    for (ProjectPoDetails existingPo : existingPos) {

	        PoDetailsForProjectPoMappingDTO incoming =
	                incomingMap.get(existingPo.getPoId());

	        boolean changed = false;

	        if (!Objects.equals(existingPo.getPrevPO(), incoming.getPrevPo())) {
	            existingPo.setPrevPO(incoming.getPrevPo());
	            changed = true;
	        }

	        if (!Objects.equals(existingPo.getNextPO(), incoming.getNextPO())) {
	            existingPo.setNextPO(incoming.getNextPO());
	            changed = true;
	        }
	        
	        validationService.validateEmployeeExists(incoming.getUpdatedByEmpId(),
                    incoming.getUpdatedByEmpName());

	        if (changed) {
	            existingPo.setUpdatedBy(incoming.getUpdatedByEmpId());
	            
	          
	            existingPo.setPoUpdatedOn(
	                    convert(incoming.getUpdatedOn())
	            );

	            projectPoDetailsRepository.save(existingPo);
	        }
	    }
	}
	
	
	public void validatePoLinkIntegrity(Project primaryProject, IshineLinkProjectDto dto) {

		if (primaryProject == null)
			throw new RuntimeException("Primary project missing");
		
		if (dto == null || dto.getPrimaryProject() == null)
			throw new RuntimeException("Invalid linking payload");
		
		// Collect DB POs (primary + deleted)
		Set<Long> dbTotalPos = new HashSet<>();
		
		// Primary DB POs
		dbTotalPos.addAll(projectPoDetailsRepository.findActivePoIdsByProjectId(primaryProject.getProjectId()));
		
		// Deleted DB POs
		for (ProjectPoMappingWithResourceDTO deleted : dto.getDeletedProjects()) {
			
			Project deletedEntity = projectRepository.findByPoProjectId(deleted.getProjectId());
			
			if (deletedEntity == null) {
				throw new RuntimeException(
				"Deleted project not found | poProjectId=" + deleted.getProjectId());
			}
			
			dbTotalPos.addAll(projectPoDetailsRepository.findActivePoIdsByProjectId(deletedEntity.getProjectId()));
		}
		
		// Collect Portal POs (final merged state)
		Set<Long> payloadPos = dto.getPrimaryProject().getPoDetailsList()
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
		
			throw new RuntimeException(
			"PO integrity failed during linking | " +
			"MissingInDB=" + missingInDb +
			" | ExtraInDB=" + extraInDb);
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

	    // PRIMARY POS
	    Map<Long, ProjectPoDetails> primaryPos = projectPoDetailsRepository
								                    .findByProjectIdAndActiveTrue(primaryProjectId)
								                    .stream()
								                    .collect(Collectors.toMap(ProjectPoDetails::getPoId, Function.identity()));

	    // DELETED POS
	    Map<Long, ProjectPoDetails> deletedPos = projectPoDetailsRepository
								                    .findByPoProjectIdInAndActiveTrue(deletedPoProjectIds)
								                    .stream()
								                    .collect(Collectors.toMap(ProjectPoDetails::getPoId, Function.identity()));

	    // PORTAL POS (contains primary + deleted together)
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

	    validationService.validateEmployeeExists(
	            incoming.getUpdatedByEmpId(),
	            incoming.getUpdatedByEmpName());

	    deletedPo.setProjectId(primaryProjectId);
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

	    Long updatedBy = fetchUpdatedBy(payloadDTO);

	    ProjectPoMappingWithResourceDTO primaryProjectDTO = payloadDTO.getPrimaryProject();

	    Set<String> primaryTeamNames = fetchPrimaryTeamNames(primaryProjectDTO.getProjectId());
	    
	    Set<Long> deletedProjectIds = payloadDTO.getDeletedProjects()
	            .stream()
	            .map(ProjectPoMappingWithResourceDTO::getProjectId)
	            .filter(Objects::nonNull)
	            .collect(Collectors.toSet());

	    for (ProjectPoMappingWithResourceDTO deletedProject : payloadDTO.getDeletedProjects()) {

	        updateDuplicateTeamNames( deletedProject, primaryTeamNames );

	        maintainHasClientSideId( deletedProject.getProjectId(), primaryProjectDTO.getProjectId(), updatedBy );

	        handlePoDepartmentMappings( deletedProject.getProjectId(), primaryProjectDTO.getProjectId(), updatedBy );

	        handleProjectManagerMappings( deletedProject.getProjectId(), primaryProjectDTO.getProjectId(), updatedBy );

	        handleProjectOverheadMappings( deletedProject.getProjectId(), primaryProjectDTO.getProjectId(), updatedBy );

	        handleTeamsLiftAndShift( deletedProject.getProjectId(), primaryProjectDTO.getProjectId(), updatedBy );
	        
	        handleEmployeeClientSideIdMapping( primaryProjectDTO.getProjectId(), deletedProjectIds, updatedBy );
	    }
	}

	private Long fetchUpdatedBy(IshineLinkProjectDto payloadDTO) {

		if (payloadDTO.getDeletedProjects().isEmpty()
	            || payloadDTO.getDeletedProjects().get(0).getPoDetailsList().isEmpty()) {
	        throw new RuntimeException("Unable to derive updatedBy from payload");
	    }

	    PoDetailsForProjectPoMappingDTO poDto = payloadDTO.getDeletedProjects().get(0).getPoDetailsList().get(0);

		validationService.validateEmployeeExists(
	            poDto.getUpdatedByEmpId(),
	            poDto.getUpdatedByEmpName()
	    );
		
	    return poDto.getUpdatedByEmpId();
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
	
	private void updateDuplicateTeamNames( ProjectPoMappingWithResourceDTO deletedProject, Set<String> primaryTeamNames) {

	    List<Object[]> deletedTeams = projectRepository.getTeamIdsForPoProjectId(deletedProject.getProjectId());

	    if (deletedTeams == null || deletedTeams.isEmpty()) {
	        return;
	    }

	    for (Object[] team : deletedTeams) {

	        Long teamId = team[0] != null ? Long.valueOf(team[0].toString()) : null;
	        String teamName = team[1] != null ? team[1].toString() : null;

	        if (teamId == null || teamName == null) {
	            continue;
	        }

	        String newName = primaryTeamNames.contains(teamName)
	                ? teamName + " | " + deletedProject.getProjectName()
	                : teamName;

	        teamRepository.updateTeamName(teamId, newName);
	    }
	}

	private void maintainHasClientSideId(Long deletedPoProjectId, Long primaryPoProjectId, Long updatedBy) {

	    Project source = projectRepository.findByPoProjectId(deletedPoProjectId);
	    Project target = projectRepository.findByPoProjectId(primaryPoProjectId);

	    if (source == null || target == null) {
	        return;
	    }

	    if (Boolean.TRUE.equals(source.getHasClientSideId())) {
	        target.setHasClientSideId(true);
	        target.setUpdatedBy(updatedBy);
	        target.setUpdatedOn(LocalDateTime.now());
	        projectRepository.save(target);
	    }
	}
	
	private void handlePoDepartmentMappings( Long deletedPoProjectId, Long primaryPoProjectId, Long updatedBy) {

	    Project source = projectRepository.findByPoProjectId(deletedPoProjectId);
	    Project target = projectRepository.findByPoProjectId(primaryPoProjectId);

	    List<PoDepartmentMapping> sourceMappings = poDepartmentMappingRepository.findByProjectIdAndActiveTrue(source.getProjectId());

	    if (sourceMappings == null || sourceMappings.isEmpty()) {
	        return;
	    }

	    sourceMappings.forEach(m -> {
	        m.setActive(false);
	        m.setUpdatedBy(updatedBy);
	    });
	    poDepartmentMappingRepository.saveAll(sourceMappings);

	    List<PoDepartmentMapping> newMappings = sourceMappings.stream().map(old -> {
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

	    List<ProjectManagerMapping> sourceMappings = projectManagerMappingRepository.findByProjectIdAndActive(deletedProjectId, 1);

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

	    List<ProjectOverheadMapping> sourceMappings = projectOverheadMappingRepository.findByProjectIdAndActive(deletedProjectId, 1);

	    if (sourceMappings.isEmpty()) {
	        return;
	    }

	    sourceMappings.forEach(m -> {
	        m.setProjectId(primaryProjectId);
	        m.setUpdatedBy(updatedBy);
	    });

	    projectOverheadMappingRepository.saveAll(sourceMappings);
	}

	private void handleTeamsLiftAndShift( Long deletedProjectId, Long primaryProjectId, Long updatedBy) {

	    List<Team> teams = teamRepository.findActiveTeamsByProjectId(Integer.parseInt(deletedProjectId.toString()));

	    if (teams.isEmpty()) {
	        return;
	    }

	    teams.forEach(t -> {
	        t.setProjectId(Integer.parseInt(primaryProjectId.toString()));
	        t.setUpdatedBy(updatedBy);
	        t.setUpdatedOn(LocalDateTime.now());
	    });

	    teamRepository.saveAll(teams);
	}
	
	private void handleEmployeeClientSideIdMapping( Long primaryProjectId, Set<Long> deletedProjectIds, Long updatedBy) {

	    if (primaryProjectId == null || deletedProjectIds == null || deletedProjectIds.isEmpty()) {
	        return;
	    }

	    // 1. Fetch empIds from primary project (based on active teams & mappings)
	    List<Long> empIds = employeeTeamMapRepository.findDistinctEmpIdsByProjectId(primaryProjectId);

	    if (empIds == null || empIds.isEmpty()) {
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
	        m.setProjectId(primaryProjectId);
	        m.setUpdatedBy(updatedBy);
	        m.setUpdatedOn(LocalDateTime.now());
	    });

	    employeeClientSideIdMappingRepository.saveAll(mappings);
	}
	
	
	
	public void updateClientAddressForPos(PoClientAddressUpdateDTO dto) {

	    List<ProjectPoDetails> pos =
	            projectPoDetailsRepository.findByPoIdInAndActive(dto.getPoIds());

	    if (pos.size() != dto.getPoIds().size()) {
	        throw new RuntimeException("Some PO IDs not found");
	    }

	    // Group by clientId
	    Map<Integer, List<ProjectPoDetails>> posByClient =
	            pos.stream().collect(Collectors.groupingBy(po -> {

	                Project project = projectRepository
	                        .findByProjectId(po.getProjectId());

	                if (project == null || !"true".equalsIgnoreCase(project.getActive())) {
	                    throw new RuntimeException("Inactive or missing project for PO: " + po.getPoId());
	                }

	                return project.getClientId();
	            }));

	    for (Map.Entry<Integer, List<ProjectPoDetails>> entry : posByClient.entrySet()) {

	        Integer clientId = entry.getKey();

	        ClientLocation clientLocation =
	                clientService.resolveClientLocation(
	                        clientId,
	                        dto.getClientLocation(),
	                        dto.getClientState(),
	                        dto.getClientAddressId()
	                );

	        for (ProjectPoDetails po : entry.getValue()) {

	            po.setClientAddressId(dto.getClientAddressId());
	            po.setClientLocationId(
	                    Long.valueOf(clientLocation.getClientLocationId()));
	            po.setUpdatedBy(dto.getUpdatedByEmpId());
	          
	        }
	    }

	    projectPoDetailsRepository.saveAll(pos);
	}

	public void sendPoLinkSuccessMail(Project primaryProject, IshineLinkProjectDto dto) {

	    try {
	    	
	        List<Integer> projectIds = new ArrayList<>();
	        Integer projectId;

	        projectIds.add(primaryProject.getProjectId());

	        for (ProjectPoMappingWithResourceDTO deleted : dto.getDeletedProjects()) {
	        	Project project = projectRepository.findByPoProjectId(deleted.getProjectId());
	        	projectId = project.getProjectId();
	        	projectIds.add(projectId);
	        }

	        List<String> toEmails = projectRepository.findManagerAndOverheadEmails(projectIds);

	        if (toEmails == null || toEmails.isEmpty()) {
	            System.err.println("No PM/Overhead emails found for PO link success mail");
	            return;
	        }

	        String receiver = String.join(",", toEmails);
	        
	        String cc = String.join(",", rmgMail, bdMail, financeMail );

	        String subject = "PO Linking Completed - " + primaryProject.getProjectName();

	        StringBuilder body = new StringBuilder();

	        body.append("Dear Team,<br><br>");
	        body.append("PO Linking completed successfully in iShine.<br><br>");

	        body.append("<b>Primary Project:</b><br>");
	        body.append(primaryProject.getProjectName());

	        body.append("<b>Merged Projects:</b><br>");
	        for (ProjectPoMappingWithResourceDTO deleted : dto.getDeletedProjects()) {
	            body.append("• ")
	                .append(deleted.getProjectName())
	                .append("<br>");
	        }

	        body.append("<br><b>Final PO List:</b><br>");
	        for (PoDetailsForProjectPoMappingDTO po : dto.getPrimaryProject().getPoDetailsList()) {
	            body.append("• ")
	                .append(po.getPoNo())
	                .append("<br>");
	        }

	        body.append("<br>Sincerely,<br>Team RMG - iShine");

	        mailService.sendMailWithCC(
	        		receiver, 
	                cc, 
	                subject,
	                body.toString()
	        );

	    } catch (Exception e) {
	        e.printStackTrace(); 
	    }
	}

}
