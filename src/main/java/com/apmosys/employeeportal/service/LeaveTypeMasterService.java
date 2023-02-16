package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.internal.build.AllowSysOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
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
	
	public ServiceResponse getAllLeaveTypes(LeaveDTO leaveDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			List<Object[]> list = leaveTypeMasterRepository.findByLeaveTypeMasterId(leaveDto.getLeaveTypeMasterId());

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
				leaveType.setUpdatedBy(leaveDTO.getUpdatedBy());
				leaveType.setUpdatedOn(stringToDateTimePasParser.getCurrentDateTime());

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
			
			LeaveTypeMaster existingLeaveType = leaveTypeMasterRepository.findByLeaveTypeCode(leaveDTO.getLeaveTypeCode());
			
			if(existingLeaveType == null) {
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

					List<Employee> employeeList = employeeRepository.findAll();

					List<EmployeeLeavesMap> mapList = new ArrayList<EmployeeLeavesMap>();

					employeeList.forEach((employee) -> {

						EmployeeLeavesMap map = new EmployeeLeavesMap();

						map.setBalance(newLeaveType.getNoOfDays());
						map.setEmpId(employee.getEmpId());
						map.setLeaveTypeMasterId(newLeaveType.getLeaveTypeMasterId());
						map.setPendingForApproval((float) 0);
						mapList.add(map);
					});

					employeeLeavesMapRepository.saveAll(mapList);

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Leave type created.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Leave type creation failed.");
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave type against "+ leaveDTO.getLeaveTypeCode() +" Already Exist.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllLeaveTypesByLeavePolicies(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> list = leaveTypeMasterRepository
					.getAllLeaveTypesByLeavePolicies(leaveDTO.getEmploymentStatus(), leaveDTO.getGender());

			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Type list is empty.");
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
					
					// set locking references 
					if(dto.getLockingPeriod().equalsIgnoreCase("Yes")) {
						dto.setFinancialYearStartDate(financialYearStartDate);
						dto.setFinancialYearStartMonth(financialYearStartMonth);
					}

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
	
	public ServiceResponse deleteLeaveType(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Optional<LeaveTypeMaster> leaveTypeObject = leaveTypeMasterRepository.findById(leaveDTO.getLeaveTypeMasterId());
			if (leaveTypeObject.isPresent()) {
				LeaveTypeMaster leaveTypeToBeDeleted = leaveTypeObject.get();
				
				Long employeeLeavesMapCount = employeeLeavesMapRepository.countByLeaveTypeMasterId(leaveTypeToBeDeleted.getLeaveTypeMasterId());
				Long leavePolicyCount = leavePolicyMasterRepository.countByLeaveTypeMasterId(leaveTypeToBeDeleted.getLeaveTypeMasterId());
				
				if(employeeLeavesMapCount == 0 && leavePolicyCount == 0) {
					
					leaveTypeMasterRepository.deleteById(leaveTypeToBeDeleted.getLeaveTypeMasterId());
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Leave Type deleted.");
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Leave Type cannot be deleted as it is mapped to employee(s) & Leave Policy.");
				}
					
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Job Role Not Found.");
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse changeLeaveTypeMapping(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Optional<LeaveTypeMaster> leaveTypeObject = leaveTypeMasterRepository.findById(leaveDTO.getOldLeaveTypeMasterId());
			if(leaveTypeObject.isPresent()) {
				
				//change balance mapping
				List<EmployeeLeavesMap> oldBalanceMapping = employeeLeavesMapRepository.findByLeaveTypeMasterId(leaveDTO.getOldLeaveTypeMasterId());
				EmployeeLeavesMap dbResponse = null;
				
				if(!oldBalanceMapping.isEmpty()) {
					for(EmployeeLeavesMap oldLeaveType :oldBalanceMapping) {
						
						Float leaveTypeToBeDeletedBalance = oldLeaveType.getBalance();
						Long employeeLeaveMapId = oldLeaveType.getEmployeeLeavesMapId();
						
						Optional<EmployeeLeavesMap> newBalanceMapping = employeeLeavesMapRepository.findById(employeeLeaveMapId);
						
						if(!newBalanceMapping.isEmpty()) {
							
							EmployeeLeavesMap employeeLeavesMap = newBalanceMapping.get();
							Float leaveTypeToBeMappedBalance = employeeLeavesMap.getBalance();
							
							Float newBalance = leaveTypeToBeDeletedBalance + leaveTypeToBeMappedBalance;
							employeeLeavesMap.setBalance(newBalance);
							
							dbResponse = employeeLeavesMapRepository.save(employeeLeavesMap);
							
							if(dbResponse != null) {
								
								//delete leaveType from employeeLeaveMapping
								employeeLeavesMapRepository.deleteById(oldLeaveType.getEmployeeLeavesMapId());
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse("Deleted successfully");
								
							}else {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("Balance Mapping Failed.");
							}
							
						}
					}
				}
				
				//change leave policy mapping
				
				List<LeavePolicyMaster> oldLeaveTypePolicy = leavePolicyMasterRepository.findByLeaveTypeMasterId(leaveDTO.getOldLeaveTypeMasterId());
				
				if(!oldLeaveTypePolicy.isEmpty()) {
					
					for(LeavePolicyMaster leavePolicy :oldLeaveTypePolicy) {
						leavePolicy.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
						
						LeavePolicyMaster leavePolicyDbResponse = leavePolicyMasterRepository.save(leavePolicy);
						
						if(leavePolicyDbResponse != null) {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Leave Policy mapping changed.");
						}
					}
					
					//change leave Application Mapping
					
					List<CompOffLeave> compOffLeaveApplication = compOffLeaveRepository.findByLeaveTypeMasterId(leaveDTO.getOldLeaveTypeMasterId());
					List<EmployeeLeave> leaveApplication = employeeLeaveRepository.findByLeaveTypeMasterId(leaveDTO.getOldLeaveTypeMasterId());
					
					if(!compOffLeaveApplication.isEmpty()) {
						
						for(CompOffLeave compOffLeave:compOffLeaveApplication) {
							
							compOffLeave.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
							CompOffLeave compOffApplicationResponse = compOffLeaveRepository.save(compOffLeave);
							
							if(compOffApplicationResponse != null) {
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse("CompOff leave application mapping changed.");
							}else {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("CompOff leave application mapping changes failed.");
							}
						}
						
					}
					
					if(!leaveApplication.isEmpty()) {
						
						for(EmployeeLeave leaveApp :leaveApplication) {
							
							leaveApp.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
							EmployeeLeave leaveApplicationDbResponse = employeeLeaveRepository.save(leaveApp);
							
							if(leaveApplicationDbResponse != null) {
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse("Leave application mapping changed.");
							}else {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("Leave application mapping change failed.");
							}
							
						}
					}
					
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("No Leave Policy Found.");
				}
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leave Type Found.");
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse checkLeaveType(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LeaveTypeMaster existingLeaveType = null;
		try {
			
			if(leaveDTO.getLeaveTypeMasterId() == null) {
				existingLeaveType = leaveTypeMasterRepository.findByLeaveType(leaveDTO.getLeaveType());

			}else {				
				existingLeaveType = leaveTypeMasterRepository.findByLeaveTypeAndLeaveTypeMasterIdIsNot(leaveDTO.getLeaveType(), leaveDTO.getLeaveTypeMasterId());
			}

			
			if ( existingLeaveType == null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Type already exist!");
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
