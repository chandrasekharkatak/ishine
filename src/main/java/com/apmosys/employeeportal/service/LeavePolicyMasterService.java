package com.apmosys.employeeportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.model.LeavePolicyLeaveTypeMap;
import com.apmosys.employeeportal.model.LeavePolicyMaster;
import com.apmosys.employeeportal.repository.LeavePolicyLeaveTypeMapRepository;
import com.apmosys.employeeportal.repository.LeavePolicyMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class LeavePolicyMasterService {
	
	@Autowired
	LeavePolicyMasterRepository leavePolicyMasterRepository;
	
	@Autowired
	LeavePolicyLeaveTypeMapRepository leavePolicyLeaveTypeMapRepository;

	@Transactional
	public ServiceResponse addLeavePolicy(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			LeavePolicyMaster newLeavePolicy = new LeavePolicyMaster();
			
			newLeavePolicy.setLeavePolicyName(leaveDTO.getLeavePolicyName());
			newLeavePolicy.setEmploymentStatus(leaveDTO.getEmploymentStatus());
			newLeavePolicy.setDefaultPolicy(leaveDTO.getDefaultPolicy());			

			LeavePolicyMaster newLeavePolicyCreated = leavePolicyMasterRepository.save(newLeavePolicy);
			
			leaveDTO.getLeaveTypeList().forEach((leaveType)-> {
				
				LeavePolicyLeaveTypeMap map = new LeavePolicyLeaveTypeMap();
				
				map.setLeavePolicyMasterId(newLeavePolicyCreated.getLeavePolicyMasterId());
				map.setLeaveTypeMasterId(leaveType.getLeaveTypeMasterId());
				map.setIncrement(leaveType.getIncrement());
				map.setIncrementValue(leaveType.getIncrementValue());				
				leavePolicyLeaveTypeMapRepository.save(map);
			});
			

			if (newLeavePolicyCreated != null) {
				
				if(newLeavePolicyCreated.getDefaultPolicy().equals("true"))
				{
	//				leavePolicyLeaveTypeMapRepository
				}
				else
				{
					
				}
				
				
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
