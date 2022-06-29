package com.apmosys.employeeportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.model.LeavePolicyMaster;
import com.apmosys.employeeportal.repository.LeavePolicyMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class LeavePolicyMasterService {
	
	@Autowired
	LeavePolicyMasterRepository leavePolicyMasterRepository;

	@Transactional
	public ServiceResponse addLeavePolicy(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			LeavePolicyMaster newLeavePolicy = new LeavePolicyMaster();
			
			newLeavePolicy.setLeavePolicyName(leaveDTO.getLeavePolicyName());
			newLeavePolicy.setEmploymentStatus(leaveDTO.getEmploymentStatus());
			

			LeavePolicyMaster newLeavePolicyCreated = leavePolicyMasterRepository.save(null);

			if (newLeavePolicyCreated != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("New leave policy added.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave policy creation failed");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

}
