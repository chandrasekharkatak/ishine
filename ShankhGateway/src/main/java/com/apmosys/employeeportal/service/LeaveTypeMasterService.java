package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.internal.build.AllowSysOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.CompOffLeave;
import com.apmosys.employeeportal.model.DraftEmployee;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.LeavePolicyMaster;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.repository.CompOffLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.LeavePolicyMasterRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class LeaveTypeMasterService {

	@Autowired
	LeaveTypeMasterRepository leaveTypeMasterRepository;

	@Value("${financialYear.startDate}")
	String financialYearStartDate;

	@Value("${financialYear.startMonth}")
	String financialYearStartMonth;

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	EmployeeLeavesMapRepository employeeLeavesMapRepository;
	
	@Autowired
	LeavePolicyMasterRepository leavePolicyMasterRepository;
	
	@Autowired
	CompOffLeaveRepository compOffLeaveRepository;
	
	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimePasParser;
	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private LogService logService;
	
	public ServiceResponse getAllLeaveTypes(LeaveDTO leaveDto) {
		ServiceResponse response = new ServiceResponse();

        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("api/getAllLeaveTypes");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
		try {
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			List<Object[]> list = leaveTypeMasterRepository.findByLeaveTypeMasterId();
       		logBuilder.append("LeaveTypeList :" + list.size());

			if (list != null) {
				for (Object[] object : list) {
					LeaveDTO leaveDTO = new LeaveDTO();

					leaveDTO.setLeaveTypeMasterId(object[0] != null ? Short.parseShort(object[0].toString()) : null);
					leaveDTO.setDescription(object[1] != null ? object[1].toString() : null);
					leaveDTO.setLeaveType(object[2] != null ? object[2].toString() : null);
					leaveDTO.setLeaveTypeCode(object[3] != null ? object[3].toString() : null);
					leaveDTO.setNoOfDays(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
					leaveDTO.setPaidLeave(object[5] != null ? object[5].toString() : null);
					leaveDTO.setRules(object[6] != null ? object[6].toString() : null);
					leaveDTO.setCreatedBy(object[7] != null ? Long.parseLong(object[7].toString()) : null);
					leaveDTO.setCreatedOn(object[8] != null ? object[8].toString() : null);
					leaveDTO.setGender(object[9] != null ? object[9].toString() : null);
					leaveDTO.setUpdatedOn(object[11] != null ? object[11].toString() : null);
					leaveDTO.setUpdatedByName(object[10] != null ? object[10].toString() : null);
					leaveDTO.setCreatedByName(object[12] != null ? object[12].toString() : null);
					leaveDTO.setUpdatedBy(object[13] != null ? Integer.parseInt(object[13].toString()) : null);
					
					dtoList.add(leaveDTO);
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("AllLeaveTypeList fetched Successfully!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave List is null.");
                apiLogInfo.setApiResponse("leave list is empty !");			
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

//	public ServiceResponse updateLeaveType(LeaveDTO leaveDTO) {
//		ServiceResponse response = new ServiceResponse();
//		 LogDTO apiLogInfo = new LogDTO();
//	        apiLogInfo.setSubFeatureName("Update Leave Type");
//	        apiLogInfo.setApiUrl("api/updateLeaveType");
//	        apiLogInfo.setLogLevel("INFO");
//	        StringBuilder logBuilder = new StringBuilder();
//	        logBuilder.append("LeaveType : " + leaveDTO.getLeaveType()+", LeaveTypeCode : " + leaveDTO.getLeaveTypeCode() + " , UpdatedBy : " + leaveDTO.getUpdatedBy());
//		try {
//			Optional<LeaveTypeMaster> leaveTypeMaster = leaveTypeMasterRepository
//					.findById(leaveDTO.getLeaveTypeMasterId());
//
//			if (leaveTypeMaster.isPresent()) {
//				LeaveTypeMaster leaveType = leaveTypeMaster.get();
//
//				leaveType.setDescription(leaveDTO.getDescription());
//				leaveType.setRules(leaveDTO.getRules());
//				leaveType.setLeaveType(leaveDTO.getLeaveType());
//				leaveType.setLeaveTypeCode(leaveDTO.getLeaveTypeCode());
//				leaveType.setGender(leaveDTO.getGender());
//				leaveType.setNoOfDays(leaveDTO.getNoOfDays());
//				leaveType.setPaidLeave(leaveDTO.getPaidLeave());
//				leaveType.setUpdatedBy(leaveDTO.getUpdatedBy());
//				leaveType.setUpdatedOn(stringToDateTimePasParser.getCurrentDateTime());
//
//				LeaveTypeMaster updatedLeaveType = leaveTypeMasterRepository.save(leaveType);
//
//				if (updatedLeaveType != null) {
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse("Leave type updated.");
//					apiLogInfo.setApiResponse("LeaveType Updated Successfully!");
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//				} else {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("Leave type updation failed.");
//                    apiLogInfo.setApiResponse("Leave Type Updation Failed!");			
//                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				}
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Leave Type Not Found.");
//                apiLogInfo.setApiResponse("Leave Type Not Found");			
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//            apiLogInfo.setLogLevel("ERROR");
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateLeaveType(LeaveDTO leaveDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Update Leave Type");
	    apiLogInfo.setApiUrl("/api/updateLeaveType");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("LeaveTypeId : ").append(leaveDTO.getLeaveTypeMasterId())
	              .append(", LeaveType : ").append(leaveDTO.getLeaveType())
	              .append(", LeaveTypeCode : ").append(leaveDTO.getLeaveTypeCode())
	              .append(", UpdatedBy : ").append(leaveDTO.getUpdatedBy());

	    try {
	        // ===== Basic Validation =====
	        if (leaveDTO == null || leaveDTO.getLeaveTypeMasterId() == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Invalid data. LeaveTypeMasterId is required.");
	            apiLogInfo.setApiResponse("Invalid request data!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        Optional<LeaveTypeMaster> existing = leaveTypeMasterRepository.findById(leaveDTO.getLeaveTypeMasterId());

	        if (existing.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Leave Type Not Found.");
	            apiLogInfo.setApiResponse("Leave Type Not Found!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        LeaveTypeMaster leaveType = existing.get();

	        // ===== Duplicate Check =====
	        LeaveTypeMaster duplicate = leaveTypeMasterRepository.findByLeaveTypeCode(leaveDTO.getLeaveTypeCode());
	        if (duplicate != null && !duplicate.getLeaveTypeMasterId().equals(leaveDTO.getLeaveTypeMasterId())) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Another leave type with the same code already exists.");
	            apiLogInfo.setApiResponse("Duplicate LeaveTypeCode Detected!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // ===== Update Fields =====
	        leaveType.setDescription(leaveDTO.getDescription());
	        leaveType.setRules(leaveDTO.getRules());
	        leaveType.setLeaveType(leaveDTO.getLeaveType());
	        leaveType.setLeaveTypeCode(leaveDTO.getLeaveTypeCode());
	        leaveType.setGender(leaveDTO.getGender());
	        leaveType.setNoOfDays(leaveDTO.getNoOfDays());
	        leaveType.setPaidLeave(leaveDTO.getPaidLeave());
	        leaveType.setUpdatedBy(leaveDTO.getUpdatedBy());
	        leaveType.setUpdatedOn(LocalDateTime.now());

	        LeaveTypeMaster updated = leaveTypeMasterRepository.save(leaveType);

	        if (updated != null) {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Leave type updated successfully.");
	            apiLogInfo.setApiResponse("Leave Type Updated Successfully!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Leave type updation failed.");
	            apiLogInfo.setApiResponse("Leave Type Updation Failed!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong during update.");
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	        apiLogInfo.setApiResponse("Exception: " + e.getMessage());
	        throw e;
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}

//	public ServiceResponse createLeaveType(LeaveDTO leaveDTO) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("Create Leave Type");
//		apiLogInfo.setApiUrl("/api/createLeaveType");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("Leave Type Code: " + leaveDTO.getLeaveTypeCode() + ", LeaveType :" + leaveDTO.getLeaveType());
//
//		try {
//			
//			LeaveTypeMaster existingLeaveType = leaveTypeMasterRepository.findByLeaveTypeCode(leaveDTO.getLeaveTypeCode());
//			
//			if(existingLeaveType == null) {
//				LeaveTypeMaster leaveType = new LeaveTypeMaster();
//
//				leaveType.setLeaveType(leaveDTO.getLeaveType());
//				leaveType.setLeaveTypeCode(leaveDTO.getLeaveTypeCode());
//				leaveType.setGender(leaveDTO.getGender());
//				leaveType.setNoOfDays(leaveDTO.getNoOfDays());
//				leaveType.setPaidLeave(leaveDTO.getPaidLeave());
//				leaveType.setRules(leaveDTO.getRules());
//				leaveType.setDescription(leaveDTO.getDescription());
//				leaveType.setCreatedBy(Integer.parseInt(leaveDTO.getCreatedBy().toString()));
//
//				LeaveTypeMaster newLeaveType = leaveTypeMasterRepository.save(leaveType);
//
//				if (newLeaveType != null) {
//
//					List<Employee> employeeList = employeeRepository.findAll();
//
//					List<EmployeeLeavesMap> mapList = new ArrayList<EmployeeLeavesMap>();
//
//					employeeList.forEach((employee) -> {
//
//						EmployeeLeavesMap map = new EmployeeLeavesMap();
//
//						map.setBalance(newLeaveType.getNoOfDays());
//						map.setEmpId(employee.getEmpId());
//						map.setLeaveTypeMasterId(newLeaveType.getLeaveTypeMasterId());
//						map.setPendingForApproval((float) 0);
//						mapList.add(map);
//					});
//
//					employeeLeavesMapRepository.saveAll(mapList);
//
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse("Leave type created.");
//					apiLogInfo.setApiResponse("Leave type Created");
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//				} else {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("Leave type creation failed.");
//                    apiLogInfo.setApiResponse("Leave Type Creation failed");			
//                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//
//				}
//			}else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Leave type against "+ leaveDTO.getLeaveTypeCode() +" Already Exist.");
//                apiLogInfo.setApiResponse("Leave type against "+ leaveDTO.getLeaveTypeCode() + " Already Exist.!");			
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse createLeaveType(LeaveDTO leaveDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Create Leave Type");
	    apiLogInfo.setApiUrl("/api/createLeaveType");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("LeaveTypeCode : ").append(leaveDTO.getLeaveTypeCode())
	              .append(", LeaveType : ").append(leaveDTO.getLeaveType())
	              .append(", CreatedBy : ").append(leaveDTO.getCreatedBy());

	    try {
	        // ===== Basic Validation =====
	        if (leaveDTO == null || leaveDTO.getLeaveTypeCode() == null || leaveDTO.getLeaveType() == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Invalid data. LeaveType and LeaveTypeCode are required.");
	            apiLogInfo.setApiResponse("Invalid Request Data!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // ===== Duplicate Check =====
	        LeaveTypeMaster existingLeaveType = leaveTypeMasterRepository.findByLeaveTypeCode(leaveDTO.getLeaveTypeCode());
	        if (existingLeaveType != null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Leave type with code '" + leaveDTO.getLeaveTypeCode() + "' already exists.");
	            apiLogInfo.setApiResponse("Duplicate LeaveType Code!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // ===== Create New Leave Type =====
	        LeaveTypeMaster leaveType = new LeaveTypeMaster();
	        leaveType.setLeaveType(leaveDTO.getLeaveType());
	        leaveType.setLeaveTypeCode(leaveDTO.getLeaveTypeCode());
	        leaveType.setGender(leaveDTO.getGender());
	        leaveType.setNoOfDays(leaveDTO.getNoOfDays());
	        leaveType.setPaidLeave(leaveDTO.getPaidLeave());
	        leaveType.setRules(leaveDTO.getRules());
	        leaveType.setDescription(leaveDTO.getDescription());
	        leaveType.setCreatedBy(Integer.parseInt(leaveDTO.getCreatedBy().toString()));

	        LeaveTypeMaster newLeaveType = leaveTypeMasterRepository.save(leaveType);

	        if (newLeaveType != null) {
	            List<Employee> employeeList = employeeRepository.findAll();

	            if (employeeList != null && !employeeList.isEmpty()) {
	                List<EmployeeLeavesMap> mapList = new ArrayList<>();

	                for (Employee employee : employeeList) {
	                    EmployeeLeavesMap map = new EmployeeLeavesMap();
	                    map.setBalance(newLeaveType.getNoOfDays());
	                    map.setEmpId(employee.getEmpId());
	                    map.setLeaveTypeMasterId(newLeaveType.getLeaveTypeMasterId());
	                    map.setPendingForApproval(0f);
	                    mapList.add(map);
	                }

	                employeeLeavesMapRepository.saveAll(mapList);
	            }

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Leave type created successfully.");
	            apiLogInfo.setApiResponse("Leave Type Created Successfully!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Leave type creation failed.");
	            apiLogInfo.setApiResponse("Leave Type Creation Failed!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong during creation.");
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	        apiLogInfo.setApiResponse("Exception: " + e.getMessage());
	        throw e;
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}

	public ServiceResponse getAllLeaveTypesByLeavePolicies(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/getAllLeaveTypesByLeavePolicies");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("EmploymentStatus : " + leaveDTO.getEmploymentStatus() + " ,Gender : " + leaveDTO.getGender() + " ,leaveTypeList :" + leaveTypeMasterRepository
				.getAllLeaveTypesByLeavePolicies(leaveDTO.getEmploymentStatus(), leaveDTO.getGender(), leaveDTO.getMaritalStatus()).size());
		try {
			
			System.err.println(" leaveDTO    "+leaveDTO);
			
			List<Object[]> list = leaveTypeMasterRepository
					.getAllLeaveTypesByLeavePolicies(leaveDTO.getEmploymentStatus(), leaveDTO.getGender(), leaveDTO.getMaritalStatus());

			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Type list is empty.");
                apiLogInfo.setApiResponse("Leave Type List is Empty!");			
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {
				for (Object[] object : list) {

					LeaveDTO dto = new LeaveDTO();

					dto.setLeaveTypeMasterId(object[0] != null ? Short.parseShort(object[0].toString()) : null);
					dto.setLeaveType(object[1] != null ? object[1].toString() : null);
					dto.setLeaveTypeCode(object[2] != null ? object[2].toString() : null);
					dto.setLeavePolicyMasterId(object[3] != null ? Short.parseShort(object[3].toString()) : null);
					dto.setLeavePolicyName(object[4] != null ? object[4].toString() : null);
					dto.setEmploymentStatus(object[5] != null ? object[5].toString() : null);
					dto.setDescription(object[6] != null ? object[6].toString() : null);
					dto.setLeaveApplication(object[7] != null ? object[7].toString() : null);
					dto.setIncrement(object[8] != null ? object[8].toString() : null);
					dto.setIncrementValue(object[9] != null ? Float.parseFloat(object[9].toString()) : null);
					dto.setOneTimeLeave(object[10] != null ? object[10].toString() : null);
					dto.setOneTimeLeaveMinCount(object[11] != null ? Float.parseFloat(object[11].toString()) : null);
					dto.setOneTimeLeaveCount(object[12] != null ? Float.parseFloat(object[12].toString()) : null);
					dto.setCarryForward(object[13] != null ? object[13].toString() : null);
					dto.setCarryForwardValue(object[14] != null ? Integer.parseInt(object[14].toString()) : null);
					dto.setExpirationPeriod(object[15] != null ? object[15].toString() : null);
					dto.setExpirationPeriodValue(object[16] != null ? Integer.parseInt(object[16].toString()) : null);
					dto.setLockingPeriod(object[17] != null ? object[17].toString() : null);
					dto.setLockingPeriodValue(object[18] != null ? Integer.parseInt(object[18].toString()) : null);
					dto.setLockingValue(object[19] != null ? Integer.parseInt(object[19].toString()) : null);
					dto.setProbation(object[20] != null ? object[20].toString() : null);
					dto.setProbationPeriod(object[21] != null ? Integer.parseInt(object[21].toString()) : null);
					dto.setMaritalStatus(object[22] != null ? object[22].toString() : null);
					dto.setMaternityType(object[23] != null ? object[23].toString() : null);
					dto.setMaternityLeaveDays(object[24] != null ? Long.parseLong(object[24].toString()) :  null);
					
					// set locking references 
					if(dto.getLockingPeriod().equalsIgnoreCase("Yes")) {
						dto.setFinancialYearStartDate(financialYearStartDate);
						dto.setFinancialYearStartMonth(financialYearStartMonth);
					}

					dtoList.add(dto);
				}

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
                apiLogInfo.setApiResponse("dtoList :" + dtoList);
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
	
//	public ServiceResponse deleteLeaveType(LeaveDTO leaveDTO) {
//    
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//	    apiLogInfo.setSubFeatureName("Delete Leave Type");
//	    apiLogInfo.setApiUrl("/api/deleteLeaveType");
//	    apiLogInfo.setLogLevel("INFO");
//	    StringBuilder logBuilder = new StringBuilder();
//	    logBuilder.append("LeaveMasterId : " + leaveDTO.getLeaveTypeMasterId());
//		try {
//			Optional<LeaveTypeMaster> leaveTypeObject = leaveTypeMasterRepository.findById(leaveDTO.getLeaveTypeMasterId());
//			if (leaveTypeObject.isPresent()) {
//				LeaveTypeMaster leaveTypeToBeDeleted = leaveTypeObject.get();
//				
//				Long employeeLeavesMapCount = employeeLeavesMapRepository.countByLeaveTypeMasterId(leaveTypeToBeDeleted.getLeaveTypeMasterId());
//				Long leavePolicyCount = leavePolicyMasterRepository.countByLeaveTypeMasterId(leaveTypeToBeDeleted.getLeaveTypeMasterId());
//				
//				if(employeeLeavesMapCount == 0 && leavePolicyCount == 0) {
//					
//					leaveTypeMasterRepository.deleteById(leaveTypeToBeDeleted.getLeaveTypeMasterId());
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse("Leave Type deleted.");
//					apiLogInfo.setApiResponse("Leave Type Deleted!");
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//
//				}else {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("Leave Type cannot be deleted as it is mapped to employee(s) & Leave Policy.");
//                    apiLogInfo.setApiResponse("Leave Type cannot be deleted as it is mapped to employee(s) & Leave Policy.");			
//                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				}
//					
//			} else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Job Role Not Found.");
//                apiLogInfo.setApiResponse("Job Role Not Found");			
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//			
//		}catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//            apiLogInfo.setLogLevel("ERROR");
//
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse deleteLeaveType(LeaveDTO leaveDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Delete Leave Type");
	    apiLogInfo.setApiUrl("/api/deleteLeaveType");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();

	    try {
	        // === Basic Data Validation ===
	        if (leaveDTO == null || leaveDTO.getLeaveTypeMasterId() == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Invalid request. Leave Type ID cannot be null.");
	            apiLogInfo.setApiResponse("Invalid request. Leave Type ID is missing.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        logBuilder.append("LeaveMasterId : ").append(leaveDTO.getLeaveTypeMasterId());

	        // === Check if Leave Type exists ===
	        Optional<LeaveTypeMaster> leaveTypeOpt = leaveTypeMasterRepository.findById(leaveDTO.getLeaveTypeMasterId());
	        if (leaveTypeOpt.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Leave Type not found.");
	            apiLogInfo.setApiResponse("Leave Type not found for ID: " + leaveDTO.getLeaveTypeMasterId());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        LeaveTypeMaster leaveTypeToBeDeleted = leaveTypeOpt.get();

	        // === Referential Integrity Checks ===
	        Long employeeLeavesMapCount = employeeLeavesMapRepository.countByLeaveTypeMasterId(leaveTypeToBeDeleted.getLeaveTypeMasterId());
	        Long leavePolicyCount = leavePolicyMasterRepository.countByLeaveTypeMasterId(leaveTypeToBeDeleted.getLeaveTypeMasterId());

	        employeeLeavesMapCount = (employeeLeavesMapCount == null) ? 0L : employeeLeavesMapCount;
	        leavePolicyCount = (leavePolicyCount == null) ? 0L : leavePolicyCount;

	        if (employeeLeavesMapCount > 0 || leavePolicyCount > 0) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Leave Type cannot be deleted as it is mapped to employee(s) or Leave Policy.");
	            apiLogInfo.setApiResponse("Leave Type mapped to other entities. Deletion restricted.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // === Proceed with Delete ===
	        leaveTypeMasterRepository.deleteById(leaveTypeToBeDeleted.getLeaveTypeMasterId());

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Leave Type deleted successfully.");
	        apiLogInfo.setApiResponse("Leave Type deleted successfully for ID: " + leaveTypeToBeDeleted.getLeaveTypeMasterId());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong while deleting the Leave Type.");
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	        apiLogInfo.setApiResponse("Exception: " + e.getMessage());
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	
//	public ServiceResponse changeLeaveTypeMapping(LeaveDTO leaveDTO) {
//		ServiceResponse response = new ServiceResponse();
//        LogDTO apiLogInfo = new LogDTO();
//        apiLogInfo.setSubFeatureName("ChangeLeaveTypeMapping");
//        apiLogInfo.setApiUrl("/api/getAllLeaveTypesByLeavePolicies");
//        apiLogInfo.setLogLevel("INFO");
//        StringBuilder logBuilder = new StringBuilder();
//        logBuilder.append("OldLeaveTypeMasterId :" + leaveDTO.getOldLeaveTypeMasterId() + " ,LeaveTypeMasterId : " + leaveDTO.getLeaveTypeMasterId() + ", EmployeeLeaveMapId: " + leaveDTO.getEmployeeLeavesMapId());
//
//		try {
//			
//			Optional<LeaveTypeMaster> leaveTypeObject = leaveTypeMasterRepository.findById(leaveDTO.getOldLeaveTypeMasterId());
//			if(leaveTypeObject.isPresent()) {
//				
//				//change balance mapping
//				List<EmployeeLeavesMap> oldBalanceMapping = employeeLeavesMapRepository.findByLeaveTypeMasterId(leaveDTO.getOldLeaveTypeMasterId());
//				EmployeeLeavesMap dbResponse = null;
//				
//				if(!oldBalanceMapping.isEmpty()) {
//					for(EmployeeLeavesMap oldLeaveType :oldBalanceMapping) {
//						
//						Float leaveTypeToBeDeletedBalance = oldLeaveType.getBalance();
//						Long employeeLeaveMapId = oldLeaveType.getEmployeeLeavesMapId();
//						
//						Optional<EmployeeLeavesMap> newBalanceMapping = employeeLeavesMapRepository.findById(employeeLeaveMapId);
//						
//						if(!newBalanceMapping.isEmpty()) {
//							
//							EmployeeLeavesMap employeeLeavesMap = newBalanceMapping.get();
//							Float leaveTypeToBeMappedBalance = employeeLeavesMap.getBalance();
//							
//							Float newBalance = leaveTypeToBeDeletedBalance + leaveTypeToBeMappedBalance;
//							employeeLeavesMap.setBalance(newBalance);
//							
//							dbResponse = employeeLeavesMapRepository.save(employeeLeavesMap);
//							
//							if(dbResponse != null) {
//								
//								//delete leaveType from employeeLeaveMapping
//								employeeLeavesMapRepository.deleteById(oldLeaveType.getEmployeeLeavesMapId());
//								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//								response.setServiceResponse("Deleted successfully");
//                                apiLogInfo.setApiResponse("Deleted successfully!");
//                                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//								
//							}else {
//								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//								response.setServiceResponse("Balance Mapping Failed.");
//
//                                apiLogInfo.setApiResponse("Balance mapping Failed");
//                                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//								
//							}
//							
//						}
//					}
//				}
//				
//				//change leave policy mapping
//				
//				List<LeavePolicyMaster> oldLeaveTypePolicy = leavePolicyMasterRepository.findByLeaveTypeMasterId(leaveDTO.getOldLeaveTypeMasterId());
//				
//				if(!oldLeaveTypePolicy.isEmpty()) {
//					
//					for(LeavePolicyMaster leavePolicy :oldLeaveTypePolicy) {
//						leavePolicy.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
//						
//						LeavePolicyMaster leavePolicyDbResponse = leavePolicyMasterRepository.save(leavePolicy);
//						
//						if(leavePolicyDbResponse != null) {
//							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//							response.setServiceResponse("Leave Policy mapping changed.");
//							apiLogInfo.setApiResponse("Leave Policy mapping changed!");
//							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//						}
//					}
//					
//					//change leave Application Mapping
//					
//					List<CompOffLeave> compOffLeaveApplication = compOffLeaveRepository.findByLeaveTypeMasterId(leaveDTO.getOldLeaveTypeMasterId());
//					List<EmployeeLeave> leaveApplication = employeeLeaveRepository.findByLeaveTypeMasterId(leaveDTO.getOldLeaveTypeMasterId());
//					
//					if(!compOffLeaveApplication.isEmpty()) {
//						
//						for(CompOffLeave compOffLeave:compOffLeaveApplication) {
//							
//							compOffLeave.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
//							CompOffLeave compOffApplicationResponse = compOffLeaveRepository.save(compOffLeave);
//							
//							if(compOffApplicationResponse != null) {
//								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//								response.setServiceResponse("CompOff leave application mapping changed.");
//								apiLogInfo.setApiResponse("CompOff leave application mapping changed!");
//								apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//							}else {
//								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//								response.setServiceResponse("CompOff leave application mapping changes failed.");
//								apiLogInfo.setApiResponse("CompOff leave application mapping changes failed.!");			
//								apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//							}
//						}
//						
//					}
//					
//					if(!leaveApplication.isEmpty()) {
//						
//						for(EmployeeLeave leaveApp :leaveApplication) {
//							
//							leaveApp.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
//							EmployeeLeave leaveApplicationDbResponse = employeeLeaveRepository.save(leaveApp);
//							
//							if(leaveApplicationDbResponse != null) {
//								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//								response.setServiceResponse("Leave application mapping changed.");
//                                apiLogInfo.setApiResponse("Leave application mapping changed.");
//                                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//							}else {
//								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//								response.setServiceResponse("Leave application mapping change failed.");
//                                apiLogInfo.setApiResponse("Leave application mapping change failed.");			
//                                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//							}
//							
//						}
//					}
//					
//				}else {
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse("No Leave Policy Found.");
//					apiLogInfo.setApiResponse("No Leave Policy Found.!");
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//				}
//				
//			}else {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("No Leave Type Found.");
//                apiLogInfo.setApiResponse("No Leave Type Found.!");			
//                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//			
//		}catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}
	
	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse changeLeaveTypeMapping(LeaveDTO leaveDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("ChangeLeaveTypeMapping");
	    apiLogInfo.setApiUrl("/api/changeLeaveTypeMapping");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();

	    try {
	        // ===Validate Request ===
	        if (leaveDTO == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Invalid request: LeaveDTO cannot be null.");
	            apiLogInfo.setApiResponse("LeaveDTO is null");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        if (leaveDTO.getOldLeaveTypeMasterId() == null || leaveDTO.getLeaveTypeMasterId() == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Old and New Leave Type IDs must be provided.");
	            apiLogInfo.setApiResponse("Old/New Leave Type Master IDs missing.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        if (leaveDTO.getOldLeaveTypeMasterId().equals(leaveDTO.getLeaveTypeMasterId())) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Old and New Leave Type IDs cannot be the same.");
	            apiLogInfo.setApiResponse("Old and New Leave Type IDs are identical.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        logBuilder.append("OldLeaveTypeMasterId : ").append(leaveDTO.getOldLeaveTypeMasterId())
	                  .append(", NewLeaveTypeMasterId : ").append(leaveDTO.getLeaveTypeMasterId())
	                  .append(", EmployeeLeaveMapId : ").append(leaveDTO.getEmployeeLeavesMapId());

	        // ===Validate old Leave Type existence ===
	        Optional<LeaveTypeMaster> oldLeaveTypeOpt = leaveTypeMasterRepository.findById(leaveDTO.getOldLeaveTypeMasterId());
	        if (oldLeaveTypeOpt.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Old Leave Type not found.");
	            apiLogInfo.setApiResponse("Old Leave Type not found: " + leaveDTO.getOldLeaveTypeMasterId());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // ===Validate new Leave Type existence ===
	        Optional<LeaveTypeMaster> newLeaveTypeOpt = leaveTypeMasterRepository.findById(leaveDTO.getLeaveTypeMasterId());
	        if (newLeaveTypeOpt.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("New Leave Type not found.");
	            apiLogInfo.setApiResponse("New Leave Type not found: " + leaveDTO.getLeaveTypeMasterId());
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // ===Update Employee Leave Mappings ===
	        List<EmployeeLeavesMap> oldBalanceMapping = employeeLeavesMapRepository.findByLeaveTypeMasterId(leaveDTO.getOldLeaveTypeMasterId());
	        if (oldBalanceMapping != null && !oldBalanceMapping.isEmpty()) {
	            for (EmployeeLeavesMap oldLeaveType : oldBalanceMapping) {
	                if (oldLeaveType == null) continue;

	                Float oldBalance = oldLeaveType.getBalance() == null ? 0f : oldLeaveType.getBalance();
	                Long empLeaveMapId = oldLeaveType.getEmployeeLeavesMapId();
	                if (empLeaveMapId == null) continue;

	                Optional<EmployeeLeavesMap> newBalanceMappingOpt = employeeLeavesMapRepository.findById(empLeaveMapId);
	                if (newBalanceMappingOpt.isEmpty()) continue;

	                EmployeeLeavesMap employeeLeavesMap = newBalanceMappingOpt.get();
	                Float newBalance = (employeeLeavesMap.getBalance() == null ? 0f : employeeLeavesMap.getBalance()) + oldBalance;
	                employeeLeavesMap.setBalance(newBalance);

	                EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(employeeLeavesMap);
	                if (dbResponse != null) {
	                    employeeLeavesMapRepository.deleteById(oldLeaveType.getEmployeeLeavesMapId());
	                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                    response.setServiceResponse("Balance mapping updated successfully.");
	                    apiLogInfo.setApiResponse("Balance mapping updated successfully.");
	                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	                } else {
	                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                    response.setServiceResponse("Balance mapping update failed.");
	                    apiLogInfo.setApiResponse("Balance mapping update failed.");
	                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                }
	            }
	        }

	        // ===Update Leave Policy Mappings ===
	        List<LeavePolicyMaster> oldLeavePolicies = leavePolicyMasterRepository.findByLeaveTypeMasterId(leaveDTO.getOldLeaveTypeMasterId());
	        if (oldLeavePolicies != null && !oldLeavePolicies.isEmpty()) {
	            for (LeavePolicyMaster leavePolicy : oldLeavePolicies) {
	                if (leavePolicy == null) continue;
	                leavePolicy.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
	                LeavePolicyMaster updatedPolicy = leavePolicyMasterRepository.save(leavePolicy);
	                if (updatedPolicy != null) {
	                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                    response.setServiceResponse("Leave policy mapping updated.");
	                    apiLogInfo.setApiResponse("Leave policy mapping updated.");
	                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	                }
	            }

	            // === 6️⃣ Update Leave Applications (CompOff & Regular) ===
	            List<CompOffLeave> compOffLeaves = compOffLeaveRepository.findByLeaveTypeMasterId(leaveDTO.getOldLeaveTypeMasterId());
	            if (compOffLeaves != null && !compOffLeaves.isEmpty()) {
	                for (CompOffLeave compOff : compOffLeaves) {
	                    if (compOff == null) continue;
	                    compOff.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
	                    CompOffLeave updatedCompOff = compOffLeaveRepository.save(compOff);
	                    if (updatedCompOff != null) {
	                        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                        response.setServiceResponse("CompOff leave application mapping changed.");
	                        apiLogInfo.setApiResponse("CompOff leave application mapping changed.");
	                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	                    } else {
	                        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                        response.setServiceResponse("Failed to update CompOff leave mapping.");
	                        apiLogInfo.setApiResponse("Failed to update CompOff leave mapping.");
	                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                    }
	                }
	            }

	            List<EmployeeLeave> leaveApplications = employeeLeaveRepository.findByLeaveTypeMasterId(leaveDTO.getOldLeaveTypeMasterId());
	            if (leaveApplications != null && !leaveApplications.isEmpty()) {
	                for (EmployeeLeave leaveApp : leaveApplications) {
	                    if (leaveApp == null) continue;
	                    leaveApp.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
	                    EmployeeLeave updatedLeave = employeeLeaveRepository.save(leaveApp);
	                    if (updatedLeave != null) {
	                        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	                        response.setServiceResponse("Employee leave application mapping changed.");
	                        apiLogInfo.setApiResponse("Employee leave application mapping changed.");
	                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	                    } else {
	                        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                        response.setServiceResponse("Failed to update leave application mapping.");
	                        apiLogInfo.setApiResponse("Failed to update leave application mapping.");
	                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                    }
	                }
	            }
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("No Leave Policy found for old Leave Type.");
	            apiLogInfo.setApiResponse("No Leave Policy found for old Leave Type.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong while changing Leave Type mapping.");
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	        apiLogInfo.setApiResponse("Exception: " + e.getMessage());
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	
	public ServiceResponse checkLeaveType(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LeaveTypeMaster existingLeaveType = null;

        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("CheckLeaveType");
        apiLogInfo.setApiUrl("/api/checkLeaveType");
        apiLogInfo.setLogLevel("INFO");
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("LeaveType : " + leaveDTO.getLeaveType() + " ,LeaveTypeMasterId : " + leaveDTO.getLeaveTypeMasterId());
		try {
			
			if(leaveDTO.getLeaveTypeMasterId() == null) {
				existingLeaveType = leaveTypeMasterRepository.findByLeaveType(leaveDTO.getLeaveType());

			}else {				
				existingLeaveType = leaveTypeMasterRepository.findByLeaveTypeAndLeaveTypeMasterIdIsNot(leaveDTO.getLeaveType(), leaveDTO.getLeaveTypeMasterId());
			}

			
			if ( existingLeaveType == null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("LeaveType dont exist!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Type already exist!");
				apiLogInfo.setApiResponse("Leave Type already exist!");			
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

}
