package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.model.CompOffLeave;
import com.apmosys.employeeportal.model.CompOffMaster;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.repository.CompOffLeaveRepository;
import com.apmosys.employeeportal.repository.CompOffMasterRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.utility.LeaveLogMessage;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class CompOffLeaveService {
	
	@Autowired
	CompOffLeaveRepository compOffLeaveRepository;
	
	@Autowired
	CompOffMasterRepository compOffMasterRepository;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	EmployeeLeavesMapRepository employeeLeavesMapRepository;
	
	@Autowired
	LeaveBalanceLogRepository leaveBalanceLogRepository;
	
	public ServiceResponse getAllCompOffReasons() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<CompOffMaster> compOffReasonsList = compOffMasterRepository.findAll();
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			
			if(compOffReasonsList.isEmpty())
			{
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Comp off reasons list is empty.");
			}
			else
			{
				compOffReasonsList.forEach((reason)-> {
					LeaveDTO dto = new LeaveDTO();
					dto.setCompOffId(reason.getCompOffId());
					dto.setCompOffReasons(reason.getCompOffReasons());
					dtoList.add(dto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse applyForCompOff(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			CompOffLeave leave = new CompOffLeave();
			
			leave.setDescription(leaveDTO.getDescription());
			leave.setReason(leaveDTO.getReason());
			leave.setEmpId(leaveDTO.getEmpId());
			leave.setManagerId(leaveDTO.getManagerId());
			//1= pending , 2= approved , 3 = rejected
			leave.setLeaveStatusId((short)1);
			//1 = CL , 2 = PL , 3 = ML , 4 = PTL , 5 = CO
//	        leave.setLeaveTypeMasterId((short)5);
			leave.setLeaveCode("CO");
			leave.setReason("comp off");
			leave.getCommonProperties().setCreatedBy(leaveDTO.getCreatedBy());
			leave.setFromDate(stringToDateTimeParser.getDate(leaveDTO.getFromDate(),"yyyy-MM-dd"));
			leave.setToDate(stringToDateTimeParser.getDate(leaveDTO.getToDate(),"yyyy-MM-dd"));
			leave.setNoOfDays((Float) leaveDTO.getNoOfDays());
			
			CompOffLeave leaveApplied = compOffLeaveRepository.save(leave);
			
			if (leaveApplied != null ) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Compoff leave applied.");

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Compoff leave creation failed.");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;

	}
	
	public ServiceResponse getPendingCompOffRequestsByManagerId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			List<Object[]> objectList = compOffLeaveRepository.getPendingCompOffRequestsByManagerId(leaveDTO.getManagerId());
			
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (objectList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No compoff request(s) found.");

			} else {

				objectList.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();				
					
					dto.setCompOffLeaveId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setCompOffReasons(object[1] != null ? object[1].toString() : null);
					dto.setDescription(object[2] != null ? object[2].toString() : null);
					dto.setCreatedOn(object[3] != null ? object[3].toString() : null);
					dto.setCreatedByName(object[4] != null ? object[4].toString() : null);
					dto.setStatus(object[5] != null ? object[5].toString() : null);
					dto.setFromDate(object[6] != null ? object[6].toString() : null);
					dto.setToDate(object[7] != null ? object[7].toString() : null);
					dto.setNoOfDays(object[8] != null ? Float.parseFloat(object[8].toString()) : null);
					dto.setEmpId(object[9] != null ? Long.parseLong(object[9].toString()) : null); 
					
					dtoList.add(dto);
				});

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
	
	
	public ServiceResponse getAllCompOffRequestsByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
		List<Object[]> compOffList =	compOffLeaveRepository.getAllCompOffRequestsByEmpId(leaveDTO.getEmpId());
		List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
		
		if(compOffList.isEmpty())
		{
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("No compoff request(s) found for employee.");
		}
		else
		{
			compOffList.forEach((object) -> {
				LeaveDTO dto = new LeaveDTO();
				
				dto.setCompOffLeaveId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
				dto.setCompOffReasons(object[1] != null ? object[1].toString() : null);
				dto.setDescription(object[2] != null ? object[2].toString() : null);
				dto.setCreatedOn(object[3] != null ? object[3].toString() : null);
				dto.setStatus(object[4] != null ? object[4].toString() : null);
				dto.setFromDate(object[5] != null ? object[5].toString() : null);
				dto.setToDate(object[6] != null ? object[6].toString() : null);
				dto.setNoOfDays(object[7] != null ? Float.parseFloat(object[7].toString()) : null);
				
				dtoList.add(dto);
			});
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

	@Transactional
	public ServiceResponse updateCompOffById(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			Optional<CompOffLeave> leaveObject = compOffLeaveRepository.findById(leaveDTO.getCompOffLeaveId());

			if (leaveObject.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No compoff request(s) found.");
			} else {

				CompOffLeave compOffLeave = leaveObject.get();
				// 1 = pending , 2 = Approved , 3= Rejected
				compOffLeave.setLeaveStatusId(leaveDTO.getLeaveStatusId());
				
				if (leaveDTO.getLeaveStatusId() == 2) {
					// 1 = CO 
					EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository
							.findByEmpIdAndLeaveTypeMasterId(leaveDTO.getEmpId(), (short) 1);

					employeeLeavesMap.setBalance(employeeLeavesMap.getBalance() + compOffLeave.getNoOfDays());
					employeeLeavesMapRepository.save(employeeLeavesMap);

					LeaveBalanceLog log = new LeaveBalanceLog();
					log.setBalance(employeeLeavesMap.getBalance());
					log.setEmpId(leaveDTO.getEmpId());
					log.setLeaveTypeMasterId((short) 1);
					log.setMessage(LeaveLogMessage.compOffAddLeave.replace("0.0", compOffLeave.getNoOfDays().toString()));
					log.setUpdateBalanceBy("+" + compOffLeave.getNoOfDays());
					leaveBalanceLogRepository.save(log);
					response.setServiceResponse("CompOff leave application approved.");
				} else if (leaveDTO.getLeaveStatusId() == 3) {
					response.setServiceResponse("CompOff leave application rejected.");
				}

				CompOffLeave compOffUpdated = compOffLeaveRepository.save(compOffLeave);

				if (compOffUpdated != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Compoff leave status updation failed.");
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

}
