package com.apmosys.employeeportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.POResourceRequirementDTO;
import com.apmosys.employeeportal.dto.PoDetailsForProjectPoMappingDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;
import com.apmosys.employeeportal.dto.RenewedPoSyncDto;
import com.apmosys.employeeportal.enums.SyncRequestType;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.SurveyQuestionRepository;
import com.apmosys.employeeportal.utility.ExceptionLogContext;

@Service
public class ValidationService {

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	JobRoleRepository jobRoleRepository;
	
	@Autowired
	SurveyQuestionRepository surveyQuestionRepository;
	
	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	DepartmentRepository departmentRepository;
	
	public boolean validateEmpId(Long empId) {

		return (employeeRepository.existsByEmpId(empId)) ? true : false;

	}

	public boolean validateEmploymentId(Long employeementId) {

		return (employeeRepository.existsByEmployeementId(employeementId)) ? true : false;

	}

	public boolean validateManagerId(Long managerId) {

		return (employeeRepository.existsByManagerId(managerId)) ? true : false;

	}

	public boolean validateJobRoleId(Long jobRoleId) {

		return (jobRoleRepository.existsByJobRoleId(jobRoleId)) ? true : false;

	}
	
	public boolean validateSurveyId(Long surveyId) {

		return (surveyQuestionRepository.existsBySurveyId(surveyId)) ? true : false;

	}

	public boolean validateProjectName(String projectName) {
		
		return (projectRepository.existsProjectByProjectName(projectName)) ? true : false;
		
	}

	public boolean validateHodId(Long empId) {
		
		return (departmentRepository.existsByHodId(empId)) ? true : false;
		
	}
	
	public void validateRenewPoPayload(RenewedPoSyncDto dto) {

	    require(dto.getEventType(), "eventType");
	    require(dto.getProjectId(), "projectId");
	    require(dto.getProjectName(), "projectName");
	    require(dto.getRenewedByEmpId(), "renewedByEmpId");
	    require(dto.getRenewedByEmpName(), "renewedByEmpName");
	    require(dto.getRenewedOn(), "renewedOn");
//	    require(dto.getRenewedPo(), "renewedPo");

	    if (dto.getAssociatePosAfterRenewal() == null ||
	        dto.getAssociatePosAfterRenewal().isEmpty()) {
	        throw new RuntimeException("associatePosAfterRenewal cannot be empty from Po");
	    }
	    
	    if (dto.getRenewedPo() == null ) {
		        throw new RuntimeException("Renewed PO cannot be empty from PO");
		    }
	    
	    PoDetailsForProjectPoMappingDTO poDto = dto.getRenewedPo();
	    
	    
	    require(poDto.getPoId(), "poId");
		require(poDto.getPoNo(), "poNo");
		require(poDto.getPoStartDate(), "poStartDate");
		require(poDto.getPoEndDate(), "poEndDate");
		require(poDto.getClientAddressId(), "clientAddressId");
		require(poDto.getClientLocation(), "clientLocation");
		require(poDto.getClientState(), "clientState");
		require(poDto.getApmosysRmEmpName(), "apmosysRmEmpName");
		require(poDto.getApmosysRmEmail(), "apmosysRmEmail");
		require(poDto.getClientRmName(), "clientRmName");

		if (poDto.getDepartmentList() == null || poDto.getDepartmentList().isEmpty()) {
			ExceptionLogContext.add("departmentList cannot be null or empty from po");
			throw new RuntimeException("departmentList cannot be null or empty");
		}
		
		  if(poDto.getResourceRequirementList()!= null) {

			for (POResourceRequirementDTO r : poDto.getResourceRequirementList()) {
				validateResourceRequirement(r);
			}
		  }

		
	}
	
	public void validateIncomingPayload(ProjectPoMappingWithResourceDTO dto) {

		require(dto.getEventType(), "eventType");
		require(dto.getProjectId(), "projectId");
		require(dto.getProjectName(), "projectName");
		require(dto.getProjectType(), "projectType");
		require(dto.getProjectStartDate(), "projectStartDate");
		require(dto.getProjectEndDate(), "projectEndDate");
		require(dto.getClientId(), "clientId");
		require(dto.getClientName(), "clientName");

		if (dto.getPoDetailsList() == null || dto.getPoDetailsList().isEmpty()) {
			ExceptionLogContext.add("poDetailsList cannot be null or empty from PO");
			throw new RuntimeException("poDetailsList cannot be null or empty");
		}

		PoDetailsForProjectPoMappingDTO poDto = dto.getPoDetailsList().get(0);

		require(poDto.getPoId(), "poId");
		require(poDto.getPoNo(), "poNo");
		require(poDto.getPoStartDate(), "poStartDate");
		require(poDto.getPoEndDate(), "poEndDate");
		require(poDto.getClientAddressId(), "clientAddressId");
		require(poDto.getClientLocation(), "clientLocation");
		require(poDto.getClientState(), "clientState");
		require(poDto.getApmosysRmEmpName(), "apmosysRmEmpName");
		require(poDto.getApmosysRmEmail(), "apmosysRmEmail");
		require(poDto.getClientRmName(), "clientRmName");

		if (poDto.getDepartmentList() == null || poDto.getDepartmentList().isEmpty()) {
			ExceptionLogContext.add("departmentList cannot be null or empty from po");
			throw new RuntimeException("departmentList cannot be null or empty");
		}

		// ---------- CREATE vs UPDATE ----------
		if (dto.getEventType() == SyncRequestType.CREATE_PROJECT) {

			require(poDto.getCreatedByEmpId(), "createdByEmpId");
			require(poDto.getCreatedByEmpName(), "createdByEmpName");

		} else if (dto.getEventType() == SyncRequestType.UPDATE_PO) {

			require(poDto.getUpdatedByEmpId(), "updatedByEmpId");
			require(poDto.getUpdatedByEmpName(), "updatedByEmpName");

		} else {
			throw new RuntimeException("Unsupported eventType");
		}

		if ("TNM".equalsIgnoreCase(dto.getProjectType())) {

			if (poDto.getResourceRequirementList() == null || poDto.getResourceRequirementList().isEmpty()) {
				throw new RuntimeException("resourceRequirementList is mandatory for TNM projects");
			}

			for (POResourceRequirementDTO r : poDto.getResourceRequirementList()) {
				validateResourceRequirement(r);
			}

		}
	}

	private void validateResourceRequirement(POResourceRequirementDTO r) {

		require(r.getResourceOverviewId(), "resourceOverviewId");
		require(r.getClientRoleId(), "clientRoleId");
		require(r.getRole(), "role");
		require(r.getDepartment(), "department");
		require(r.getExperience(), "experience");
		require(r.getCount(), "count");
		require(r.getYearWiseRateCartStartDate(), "yearWiseRateCartStartDate");
		require(r.getYearWiseRateCartEndDate(), "yearWiseRateCartEndDate");
		require(r.getLineItemStartDate(), "lineItemStartDate");
		require(r.getLineItemEndDate(), "lineItemEndDate");
	}

	private void require(Object value, String fieldName) {
		if (value == null) {
			throw new RuntimeException(fieldName + " cannot be null from PO");
		}
	}
	
}
