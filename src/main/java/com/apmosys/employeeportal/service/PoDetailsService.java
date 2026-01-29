package com.apmosys.employeeportal.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.DeletedPoSyncDTO;
import com.apmosys.employeeportal.dto.PoDetailsForProjectPoMappingDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;
import com.apmosys.employeeportal.dto.RenewedPoSyncDto;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectPoDetails;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.ProjectPoDetailsRepository;
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

	public ProjectPoDetails createPoRTS(Project project, ProjectPoMappingWithResourceDTO dto, Client client) {

		PoDetailsForProjectPoMappingDTO poDto = dto.getPoDetailsList().get(0);

		ClientLocation cl = clientService.resolveClientLocation(client.getClientId(), poDto.getClientLocation(),
				poDto.getClientState());

		ProjectPoDetails po = new ProjectPoDetails();
		po.setPoId(poDto.getPoId());
		po.setProjectId(project.getProjectId());
		po.setPoProjectId(dto.getProjectId());
		po.setPoNo(poDto.getPoNo());
		po.setPoStartDate(convert(poDto.getPoStartDate()));
		po.setPoEndDate(convert(poDto.getPoEndDate()));
		po.setClientLocationId(Long.valueOf(cl.getClientLocationId()));
		po.setClientAddressId(poDto.getClientAddressId());
		po.setCreatedBy(validateAndGetEmployeeEmpId(poDto.getCreatedByEmpId(),poDto.getCreatedByEmpName()));
		po.setUpdatedBy(validateAndGetEmployeeEmpId(poDto.getUpdatedByEmpId(),poDto.getUpdatedByEmpName()));
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
	    return date == null ? null :
	            date.toInstant()
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
	                    poDto.getClientState());

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
	        po.setUpdatedBy(
	                validateAndGetEmployeeEmpId(
	                        poDto.getUpdatedByEmpId(),
	                        poDto.getUpdatedByEmpName())
	        );
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
				poDto.getClientState());

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

	    po.setCreatedBy(
	            validateAndGetEmployeeEmpId(
	                    dto.getRenewedByEmpId(),
	                    dto.getRenewedByEmpName()));

	    po.setPoCreatedOn(poDto.getCreatedOn() != null ? convert(poDto.getCreatedOn()) : null);
		po.setPoUpdatedOn(poDto.getUpdatedOn() != null ? convert(poDto.getUpdatedOn()) : null);
	    

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

	        if (changed) {
	            po.setUpdatedBy(
	                    validateAndGetEmployeeEmpId(
	                            dto.getRenewedByEmpId(),
	                            dto.getRenewedByEmpName()));
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
	            teamRepository.existsByPoIdAndIsActive(poId, "Y");

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
	        String deletedByEmpId,
	        String deletedByEmpName,
	        Date deletedOn) {

	    po.setActive(false);
	    po.setUpdatedBy(
	            validateAndGetEmployeeEmpId(
	                    deletedByEmpId,
	                    deletedByEmpName
	            ));
	    po.setPoUpdatedOn(convert(deletedOn)
	    );

	    projectPoDetailsRepository.save(po);
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

	        if (changed) {
	            po.setUpdatedBy(
	                    validateAndGetEmployeeEmpId(
	                            dto.getDeletedByEmpId(),
	                            dto.getDeletedByEmpName()
	                    ));
	            po.setPoUpdatedOn(convert(dto.getDeletedOn())
	            );
	            projectPoDetailsRepository.save(po);
	        }
	    }
	}








}
