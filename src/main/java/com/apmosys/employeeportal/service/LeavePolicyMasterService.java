package com.apmosys.employeeportal.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
			
			Optional<LeavePolicyMaster> existingLeavePolicy = 
					leavePolicyMasterRepository.findByEmployentStatusAndLeaveTypeMasterId(leaveDTO.getEmploymentStatus(), leaveDTO.getLeaveTypeMasterId());
			
			if (existingLeavePolicy.isPresent()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave policy already exist");
			}else {
				LeavePolicyMaster newLeavePolicy = new LeavePolicyMaster();
				
				newLeavePolicy.setLeavePolicyName(leaveDTO.getLeavePolicyName());
				newLeavePolicy.setEmploymentStatus(leaveDTO.getEmploymentStatus());
				newLeavePolicy.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());			
				newLeavePolicy.setDescription(leaveDTO.getDescription());
				newLeavePolicy.setLeaveApplication(leaveDTO.getLeaveApplication());
				newLeavePolicy.setIncrement(leaveDTO.getIncrement());
				newLeavePolicy.setIncrementValue(leaveDTO.getIncrementValue());
				newLeavePolicy.setOneTimeLeave(leaveDTO.getOneTimeLeave());
				newLeavePolicy.setOneTimeLeaveMinCount(leaveDTO.getOneTimeLeaveMinCount());
				newLeavePolicy.setOneTimeLeaveCount(leaveDTO.getOneTimeLeaveCount());
				newLeavePolicy.setCarryForward(leaveDTO.getCarryForward());
				newLeavePolicy.setCarryForwardValue(leaveDTO.getCarryForwardValue());
				newLeavePolicy.setExpirationPeriod(leaveDTO.getExpirationPeriod());
				newLeavePolicy.setExpirationPeriodValue(leaveDTO.getExpirationPeriodValue());
				newLeavePolicy.setLockingPeriod(leaveDTO.getLockingPeriod());
				newLeavePolicy.setLockingPeriodValue(leaveDTO.getLockingPeriodValue());
				newLeavePolicy.setLockingValue(leaveDTO.getLockingValue());
				newLeavePolicy.setProbation(leaveDTO.getProbation());
				newLeavePolicy.setProbationPeriod(leaveDTO.getProbationPeriod());
				
				newLeavePolicy.setCreatedBy(leaveDTO.getCreatedBy());
				
				LeavePolicyMaster newLeavePolicyCreated = leavePolicyMasterRepository.save(newLeavePolicy);
				
				if (newLeavePolicyCreated != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("New leave policy added.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Leave policy creation failed");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	@Transactional
	public ServiceResponse updateLeavePolicy(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		String message = "";
		try {
			Optional<LeavePolicyMaster> existingLeavePolicy = leavePolicyMasterRepository.findById(leaveDTO.getLeavePolicyMasterId());

			if (existingLeavePolicy.isPresent()) {
				LeavePolicyMaster leavePolicy = existingLeavePolicy.get();

				leavePolicy.setLeavePolicyName(leaveDTO.getLeavePolicyName());
				leavePolicy.setEmploymentStatus(leaveDTO.getEmploymentStatus());
				leavePolicy.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());			
				leavePolicy.setDescription(leaveDTO.getDescription());
				leavePolicy.setLeaveApplication(leaveDTO.getLeaveApplication());
				leavePolicy.setIncrement(leaveDTO.getIncrement());
				leavePolicy.setIncrementValue(leaveDTO.getIncrementValue());
				leavePolicy.setOneTimeLeave(leaveDTO.getOneTimeLeave());
				leavePolicy.setOneTimeLeaveMinCount(leaveDTO.getOneTimeLeaveMinCount());
				leavePolicy.setOneTimeLeaveCount(leaveDTO.getOneTimeLeaveCount());
				leavePolicy.setCarryForward(leaveDTO.getCarryForward());
				leavePolicy.setCarryForwardValue(leaveDTO.getCarryForwardValue());
				leavePolicy.setExpirationPeriod(leaveDTO.getExpirationPeriod());
				leavePolicy.setExpirationPeriodValue(leaveDTO.getExpirationPeriodValue());
				leavePolicy.setLockingPeriod(leaveDTO.getLockingPeriod());
				leavePolicy.setLockingPeriodValue(leaveDTO.getLockingPeriodValue());
				leavePolicy.setLockingValue(leaveDTO.getLockingValue());
				leavePolicy.setProbation(leaveDTO.getProbation());
				leavePolicy.setProbationPeriod(leaveDTO.getProbationPeriod());
		
				leavePolicy.setUpdatedBy(leaveDTO.getUpdatedBy());

				LeavePolicyMaster dbResponse = leavePolicyMasterRepository.save(leavePolicy);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Leave policy updated successfully." + message);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to update Leave policy.");
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No such Leave policy available.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse deleteLeavePolicyByLeavePolicyMasterId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			Optional<LeavePolicyMaster> existingLeavePolicy = leavePolicyMasterRepository.findById(leaveDTO.getLeavePolicyMasterId());
			if (existingLeavePolicy.isPresent()) {
				LeavePolicyMaster leavePolicy = existingLeavePolicy.get();
				leavePolicyMasterRepository.deleteById(leavePolicy.getLeavePolicyMasterId());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Leave policy Deleted");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave policy Not Found");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	
	public ServiceResponse getAllLeavePolicy() {
		ServiceResponse response = new ServiceResponse();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			List<Object[]> list = leavePolicyMasterRepository.getAllLeavePolicies();

			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Policy list is empty.");
			} else {
				for (Object[] object : list) {

					LeaveDTO dto = new LeaveDTO();
					
					dto.setLeavePolicyMasterId(object[0] != null ? Short.parseShort(object[0].toString()) : null);
					dto.setLeavePolicyName(object[1] != null ? object[1].toString() : null);
					dto.setLeaveTypeMasterId(object[2] != null ? Short.parseShort(object[2].toString()) : null);
					dto.setLeaveType(object[3] != null ? object[3].toString() : null);
					dto.setEmploymentStatus(object[4] != null ? object[4].toString() : null);
					dto.setDescription(object[5] != null ? object[5].toString() : null);
					dto.setLeaveApplication(object[6] != null ? object[6].toString() : null);
					dto.setIncrement(object[7] != null ? object[7].toString() : null);
					dto.setIncrementValue(object[8] != null ? Float.parseFloat(object[8].toString()) : null);
					dto.setOneTimeLeave(object[9] != null ? object[9].toString() : null);
					dto.setOneTimeLeaveMinCount(object[10] != null ? Float.parseFloat(object[10].toString()) : null);
					dto.setOneTimeLeaveCount(object[11] != null ? Float.parseFloat(object[11].toString()) : null);
					dto.setCarryForward(object[12] != null ? object[12].toString() : null);
					dto.setCarryForwardValue(object[13] != null ? Integer.parseInt(object[13].toString()) : null);
					dto.setExpirationPeriod(object[14] != null ? object[14].toString() : null);
					dto.setExpirationPeriodValue(object[15] != null ? Integer.parseInt(object[15].toString()) : null);
					dto.setLockingPeriod(object[16] != null ? object[16].toString() : null);
					dto.setLockingPeriodValue(object[17] != null ? Integer.parseInt(object[17].toString()) : null);
					dto.setLockingValue(object[18] != null ? Integer.parseInt(object[18].toString()) : null);
					dto.setProbation(object[19] != null ? object[19].toString() : null);
					dto.setProbationPeriod(object[20] != null ? Integer.parseInt(object[20].toString()) : null);
					dto.setCreatedByName(object[21] != null ? object[21].toString() : null);
					dto.setCreatedOn(object[22] != null ? format.format(format.parse(object[22].toString())) : null);
					dtoList.add(dto);
				}
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse getLeavePolicyByEmployentStatusAndLeaveTypeMasterId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Optional<LeavePolicyMaster> leavePolicy = 
					leavePolicyMasterRepository.findByEmployentStatusAndLeaveTypeMasterId(leaveDTO.getEmploymentStatus(), leaveDTO.getLeaveTypeMasterId());
			
			if (leavePolicy.isPresent()) {
				response.setServiceResponse(leavePolicy.get());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave policy Not Found");
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
