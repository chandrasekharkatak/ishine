package com.apmosys.employeeportal.serviceInterface;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.BioMaxRequestDTO;
import com.apmosys.employeeportal.utility.ServiceResponse;


public interface BioMaxRequestService {
	
	ServiceResponse createBioMaxRequest(BioMaxRequestDTO biomaxRequestDTO);
	ServiceResponse  updateBioMaxRequest(Long id,BioMaxRequestDTO biomaxRequestDTO);
	ServiceResponse  getByEmployeeId(Long empid);
	ServiceResponse  getByRepostingManager(Long reportingManagerId);
	ServiceResponse deletedRequest(Long id);
	ServiceResponse getById(Long id);
	ServiceResponse getBioMaxRequestTypeCronJon();
	ServiceResponse getBioMaxRequestType();
	ServiceResponse leaveDeductRoleBackForEmloyee(BioMaxRequestDTO bioMaxRequestDTO);
}
