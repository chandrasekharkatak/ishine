package com.apmosys.employeeportal.service;

import java.text.SimpleDateFormat;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.PoDetailsForProjectPoMappingDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectPoDetails;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.ProjectPoDetailsRepository;
import com.apmosys.employeeportal.utility.ExceptionLogContext;
import com.apmosys.employeeportal.utility.PoportalApiException;

@Service
public class PoDetailsService {

	@Autowired
	ClientService clientService;
	
	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	ProjectPoDetailsRepository projectPoDetailsRepository;

	public ProjectPoDetails createPoRTS(Project project, ProjectPoMappingWithResourceDTO dto, Client client) {

		PoDetailsForProjectPoMappingDTO poDto = dto.getPoDetailsList().get(0);

		ClientLocation cl = clientService.resolveClientLocation(client.getClientId(), poDto.getClientLocation(),
				poDto.getClientState());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		ProjectPoDetails po = new ProjectPoDetails();
		po.setPoId(poDto.getPoId());
		po.setProjectId(project.getProjectId());
		po.setPoProjectId(dto.getProjectId());
		po.setPoNo(poDto.getPoNo());
		po.setPoStartDate(poDto.getPoStartDate() != null ? dateFormat.format(poDto.getPoStartDate()) : null);
		po.setPoEndDate(poDto.getPoEndDate() != null ? dateFormat.format(poDto.getPoEndDate()) : null);
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
		po.setPoCreatedOn(poDto.getCreatedOn() != null ? dateFormat.format(poDto.getCreatedOn()) : null);
		po.setPoUpdatedOn(poDto.getUpdatedOn() != null ? dateFormat.format(poDto.getUpdatedOn()) : null);
		
		
		
		
		
		return projectPoDetailsRepository.save(po);
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

	    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	    boolean changed = false;

	    if (!Objects.equals(po.getPoNo(), poDto.getPoNo())) {
	        po.setPoNo(poDto.getPoNo());
	        changed = true;
	    }

	    String start =
	            poDto.getPoStartDate() != null
	                    ? df.format(poDto.getPoStartDate())
	                    : null;

	    if (!Objects.equals(po.getPoStartDate(), start)) {
	        po.setPoStartDate(start);
	        changed = true;
	    }

	    String end =
	            poDto.getPoEndDate() != null
	                    ? df.format(poDto.getPoEndDate())
	                    : null;

	    if (!Objects.equals(po.getPoEndDate(), end)) {
	        po.setPoEndDate(end);
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
	        po.setPoUpdatedOn(poDto.getUpdatedOn() != null ? df.format(poDto.getUpdatedOn()) : null);

	        projectPoDetailsRepository.save(po);
	    }

	    return changed;

	   
	}

}
