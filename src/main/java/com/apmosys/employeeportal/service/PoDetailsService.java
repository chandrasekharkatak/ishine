package com.apmosys.employeeportal.service;

import java.text.SimpleDateFormat;

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
		po.setActive(poDto.isActive());
		po.setPrevPO(poDto.getPrevPo());
		po.setNextPO(poDto.getNextPO());

		return projectPoDetailsRepository.save(po);
	}

	private Long validateAndGetEmployeeEmpId(String createdByEmpId, String createdByEmpName) {

		if (createdByEmpId == null || !createdByEmpId.startsWith("A-")) {
			throw new RuntimeException("Invalid createdByEmpId format");
		}

		Long employmentId;
		try {
			employmentId = Long.parseLong(createdByEmpId.substring(2));
		} catch (NumberFormatException e) {
			throw new RuntimeException("Invalid employment id in createdByEmpId");
		}

		Long empId = employeeRepository.findByEmploymentIdAndEmployeeName(employmentId, createdByEmpName)
				.orElseThrow(() -> new RuntimeException(
						"Employee mismatch: employmentId=" + employmentId + ", name=" + createdByEmpName));
//		logger.info("created by empId =  \n" + empId);
		return empId;
	}
}
