package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class LeaveTypeMasterService {

	@Autowired
	LeaveTypeMasterRepository leaveTypeMasterRepository;

	public ServiceResponse getAllLeaveTypes() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			List<LeaveTypeMaster> list = leaveTypeMasterRepository.findAll();

			if (list != null) {
				for (LeaveTypeMaster leaveMaster : list) {
					LeaveDTO leaveDTO = new LeaveDTO();

					leaveDTO.setLeaveTypeMasterId(leaveMaster.getLeaveTypeMasterId());
					leaveDTO.setLeaveType(leaveMaster.getLeaveType());
					leaveDTO.setNoOfDays(leaveMaster.getNoOfDays());
					leaveDTO.setDescription(leaveMaster.getDescription());
					leaveDTO.setPaidLeave(leaveMaster.getPaidLeave());
					leaveDTO.setRules(leaveMaster.getRules());
					leaveDTO.setLeaveTypeCode(leaveMaster.getLeaveTypeCode());
					leaveDTO.setGender(leaveMaster.getGender());	
					dtoList.add(leaveDTO);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave List is null.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse updateLeaveType(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Optional<LeaveTypeMaster> leaveTypeMaster = leaveTypeMasterRepository
					.findById(leaveDTO.getLeaveTypeMasterId());

			if (leaveTypeMaster.isPresent()) {
				LeaveTypeMaster leaveType = leaveTypeMaster.get();

				leaveType.setDescription(leaveDTO.getDescription());
				leaveType.setRules(leaveDTO.getRules());
				leaveType.setLeaveType(leaveDTO.getLeaveType());
				leaveType.setLeaveTypeCode(leaveDTO.getLeaveTypeCode());
				leaveType.setGender(leaveDTO.getGender());	
				leaveType.setNoOfDays(leaveDTO.getNoOfDays());
				leaveType.setPaidLeave(leaveDTO.getPaidLeave());
				
				LeaveTypeMaster updatedLeaveType = leaveTypeMasterRepository.save(leaveType);

				if (updatedLeaveType != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Leave type updated.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Leave type updation failed.");
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Type Not Found.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse createLeaveType(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			LeaveTypeMaster leaveType = new LeaveTypeMaster();
			
			leaveType.setLeaveType(leaveDTO.getLeaveType());
			leaveType.setLeaveTypeCode(leaveDTO.getLeaveTypeCode());
			leaveType.setGender(leaveDTO.getGender());	
			leaveType.setNoOfDays(leaveDTO.getNoOfDays());
			leaveType.setPaidLeave(leaveDTO.getPaidLeave());
			leaveType.setRules(leaveDTO.getRules());
			leaveType.setDescription(leaveDTO.getDescription());
			
			
			LeaveTypeMaster newLeaveType = leaveTypeMasterRepository.save(leaveType);
			
			if (newLeaveType != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Leave type created.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave type creation failed.");
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
