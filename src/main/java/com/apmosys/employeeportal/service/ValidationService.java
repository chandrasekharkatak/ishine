package com.apmosys.employeeportal.service;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.POResourceRequirementDTO;
import com.apmosys.employeeportal.dto.PoDetailsForProjectPoMappingDTO;
import com.apmosys.employeeportal.dto.ProjectPoMappingWithResourceDTO;
import com.apmosys.employeeportal.dto.RenewedPoSyncDto;
import com.apmosys.employeeportal.dto.RmUpdateSyncDto;
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
		require(poDto.getApmosysRmEmpId(), "apmosysRmEmpId");	
		require(poDto.getApmosysRmEmpName(), "apmosysRmEmpName");
		require(poDto.getApmosysRmEmail(), "apmosysRmEmail");
		require(poDto.getClientRmName(), "clientRmName");

		if (poDto.getDepartmentList() == null || poDto.getDepartmentList().isEmpty()) {
			ExceptionLogContext.add("departmentList cannot be null or empty from po");
			throw new RuntimeException("departmentList cannot be null or empty");
		}

		
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
	
	
	public Long validateAndGetEmployeeEmpId(String createdByEmpId, String createdByEmpName) {

		if (createdByEmpId == null || !createdByEmpId.startsWith("A-")) {
			ExceptionLogContext.add(
		            "EmpId or EmpName missing from PO"
		            + " | EmpId=" + createdByEmpId
		            + " | EmpName=" + createdByEmpName
		        );
			throw new RuntimeException("Invalid EmpId format");
		}

		Long employmentId;
		try {
			employmentId = Long.parseLong(createdByEmpId.substring(2));
		} catch (NumberFormatException e) {
			 ExceptionLogContext.add(
			            "Invalid EmpId format from PO"
			            + " | EmpId=" + createdByEmpId
			        );
			throw new RuntimeException("Invalid employment id in EmpId");
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
		                    "Employee mismatch for employee"
		                );
		            });
	}
	
	
	public void validateRmUpdateSyncPayload(RmUpdateSyncDto dto) {

	    if (dto == null) {
			ExceptionLogContext.add("Request payload is missing");
	        throw new RuntimeException("Request payload is missing");
	    }

	   
	    if (dto.getPoIds() == null || dto.getPoIds().isEmpty()) {
	    	ExceptionLogContext.add("PO IDs cannot be null or empty");
	        throw new RuntimeException("PO IDs cannot be null or empty");
	    }

	    if (dto.getPoIds().stream().anyMatch(Objects::isNull)) {
	    	ExceptionLogContext.add("PO IDs list contains null value");
	        throw new RuntimeException("PO IDs list contains null value");
	    }

	   
	    if (dto.getUpdatedApmosysRmEmpId() == null) {
	    	ExceptionLogContext.add("RM employee ID is missing");
	        throw new RuntimeException("RM employee ID is missing");
	    }

	   
	    if (dto.getUpdatedApmosysRmEmpName() == null ||
	        dto.getUpdatedApmosysRmEmpName().trim().isEmpty()) {
	    	ExceptionLogContext.add("RM employee name is missing");
	        throw new RuntimeException("RM employee name is missing");
	    }

	   
	    if (dto.getUpdatedApmosysRmEmail() == null ||
	        dto.getUpdatedApmosysRmEmail().trim().isEmpty()) {
	    	ExceptionLogContext.add("RM email is missing");
	        throw new RuntimeException("RM email is missing");
	    }

	    
	    if (!dto.getUpdatedApmosysRmEmail()
	            .matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
	    	ExceptionLogContext.add("Invalid RM email format");
	        throw new RuntimeException("Invalid RM email format");
	    }
	}
	
	
	public void validateEmployeeExists(Long empId, String empName) {

	    boolean exists = employeeRepository
	            .existsByEmpIdAndEmployeeName(empId, empName);

	    if (!exists) {
	    	ExceptionLogContext.add(
		            "employee not found for empId=" + empId + ", name=" + empName
	    	        );
	        throw new RuntimeException(
	            "employee not found for empId=" + empId + ", name=" + empName
	        );
	    }
	}

	
	
	
	
}
