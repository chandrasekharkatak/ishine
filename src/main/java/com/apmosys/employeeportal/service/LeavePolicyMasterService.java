package com.apmosys.employeeportal.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.LeavePolicyMaster;
import com.apmosys.employeeportal.repository.LeavePolicyMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class LeavePolicyMasterService {
	
	@Autowired
	LeavePolicyMasterRepository leavePolicyMasterRepository;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private LogService logService;
	 

	@Transactional
	public ServiceResponse addLeavePolicy(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Add Leave Policy");
		apiLogInfo.setApiUrl("api/addLeavePolicy");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("policy name : "+ leaveDTO.getLeavePolicyName() + " ,EmploymentStatus : " + leaveDTO.getEmploymentStatus() + " ,leaveTypeMasterId :"+ leaveDTO.getLeaveTypeMasterId() + "Createdby: " + leaveDTO.getCreatedBy());
		try {
			
			Optional<LeavePolicyMaster> existingLeavePolicy = 
					leavePolicyMasterRepository.findByEmployentStatusAndLeaveTypeMasterIdAndMaternityType(leaveDTO.getEmploymentStatus(), leaveDTO.getLeaveTypeMasterId(), leaveDTO.getMaternityType());
			
			if (existingLeavePolicy.isPresent()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave policy already exist");
				apiLogInfo.setApiResponse("leave policy is already present");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}else {
				LeavePolicyMaster newLeavePolicy = new LeavePolicyMaster();
				
				newLeavePolicy.setLeavePolicyName(leaveDTO.getLeavePolicyName());
				newLeavePolicy.setEmploymentStatus(leaveDTO.getEmploymentStatus());
				newLeavePolicy.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());			
				newLeavePolicy.setDescription(leaveDTO.getDescription());
				newLeavePolicy.setMaritalStatus(leaveDTO.getMaritalStatus());
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
				newLeavePolicy.setMaternityType(leaveDTO.getMaternityType());
				newLeavePolicy.setMaternityLeaveDays(leaveDTO.getMaternityLeaveDays());
				
				newLeavePolicy.setCreatedBy(leaveDTO.getCreatedBy());
				
				LeavePolicyMaster newLeavePolicyCreated = leavePolicyMasterRepository.save(newLeavePolicy);
				
				if (newLeavePolicyCreated != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("New leave policy added.");
					apiLogInfo.setApiResponse("New Leave Policy added");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Leave policy creation failed");
					apiLogInfo.setApiResponse("leave policy creation failed");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	@Transactional
	public ServiceResponse updateLeavePolicy(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("update Leave Policy");
		apiLogInfo.setApiUrl("api/updateLeavePolicy");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("policy name : "+ leaveDTO.getLeavePolicyName() + " ,EmploymentStatus : " + leaveDTO.getEmploymentStatus() + " ,leavePolicyMasterId :"+ leaveDTO.getLeavePolicyMasterId() + " ,CreatedBy: " + leaveDTO.getCreatedBy());
		String message = "";
		try {
			Optional<LeavePolicyMaster> existingLeavePolicy = leavePolicyMasterRepository.findById(leaveDTO.getLeavePolicyMasterId());

			if (existingLeavePolicy.isPresent()) {
				LeavePolicyMaster leavePolicy = existingLeavePolicy.get();

				leavePolicy.setLeavePolicyName(leaveDTO.getLeavePolicyName());
				leavePolicy.setEmploymentStatus(leaveDTO.getEmploymentStatus());
				leavePolicy.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());			
				leavePolicy.setDescription(leaveDTO.getDescription());
				leavePolicy.setMaritalStatus(leaveDTO.getMaritalStatus());
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
				leavePolicy.setMaternityType(leaveDTO.getMaternityType());
				leavePolicy.setMaternityLeaveDays(leaveDTO.getMaternityLeaveDays());
		
				leavePolicy.setUpdatedBy(leaveDTO.getUpdatedBy());
				leavePolicy.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());

				LeavePolicyMaster dbResponse = leavePolicyMasterRepository.save(leavePolicy);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Leave policy updated successfully." + message);
                    apiLogInfo.setApiResponse("Leave Policy Updated !");
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

					
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to update Leave policy.");
					apiLogInfo.setApiResponse("Failed to Update Leave Policy !");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No such Leave policy available.");
				apiLogInfo.setApiResponse("No such Leave Policy available!");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");

		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	public ServiceResponse deleteLeavePolicyByLeavePolicyMasterId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("delete Leave Policy");
		apiLogInfo.setApiUrl("api/deleteLeavePolicyByLeavePolicyMasterId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("policy name : "+ leaveDTO.getLeavePolicyName() + " ,leavePolicyMasterId :"+ leaveDTO.getLeavePolicyMasterId());
		try {

			Optional<LeavePolicyMaster> existingLeavePolicy = leavePolicyMasterRepository.findById(leaveDTO.getLeavePolicyMasterId());
			if (existingLeavePolicy.isPresent()) {
				LeavePolicyMaster leavePolicy = existingLeavePolicy.get();
				leavePolicyMasterRepository.deleteById(leavePolicy.getLeavePolicyMasterId());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Leave policy Deleted");
				apiLogInfo.setApiResponse("Leave Policy Deleted Successfully!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave policy Not Found");
				apiLogInfo.setApiResponse("Leave Policy Not Found!");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	
	public ServiceResponse getAllLeavePolicy() {
		ServiceResponse response = new ServiceResponse();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("api/getAllLeavePolicy");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			List<Object[]> list = leavePolicyMasterRepository.getAllLeavePolicies();
			logBuilder.append("AllLeavePolicyList : " + list.size());

			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Policy list is empty.");
				apiLogInfo.setApiResponse("leave policy list is Empty!");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

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
					dto.setUpdatedOn(object[23] != null ? object[23].toString() : null);
					dto.setUpdatedBy(object[24] != null ? Integer.parseInt(object[24].toString()) : null);
					dto.setUpdatedByName(object[25] != null ? object[25].toString() : null);
					dto.setMaritalStatus(object[26] != null ? object[26].toString() : null);
					dto.setMaternityType(object[27] != null ? object[27].toString() : null);
					dto.setMaternityLeaveDays(object[28] != null ? Long.parseLong(object[28].toString()) : null);
					dto.setCreatedBy(object[29] != null ? Long.parseLong(object[29].toString()) : null);
					
					dtoList.add(dto);
				}
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("allLeavePolicyList Fetched Successfully!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");

		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	public ServiceResponse getLeavePolicyByEmployentStatusAndLeaveTypeMasterId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("api/getLeavePolicyByEmployentStatusAndLeaveTypeMasterId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Employment Status :" + leaveDTO.getEmploymentStatus() + " ,leaveTypeMasterId :" + leaveDTO.getLeaveTypeMasterId());
		try {
			
			Optional<LeavePolicyMaster> leavePolicy = 
					leavePolicyMasterRepository.findByEmployentStatusAndLeaveTypeMasterId(leaveDTO.getEmploymentStatus(), leaveDTO.getLeaveTypeMasterId());
			
			if (leavePolicy.isPresent()) {
				response.setServiceResponse(leavePolicy.get());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("leave Policy By Employment Id and LeaveTypeMasterId Fetched SuccessFully!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave policy Not Found");
                apiLogInfo.setApiResponse("leave Policy Not Found!");			
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse getMaternityLeaveDaysByMaternityType(LeaveDTO leaveDto) {
		System.err.println(leaveDto);
		ServiceResponse response = new ServiceResponse();
		LeavePolicyMaster findAllowedDaysForMaternity = leavePolicyMasterRepository.findLeavePolicyByMaternityType(leaveDto.getMaternityType());
		System.err.println(findAllowedDaysForMaternity.toString());
		response.setServiceResponse(findAllowedDaysForMaternity);
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		return response;
	}

}
