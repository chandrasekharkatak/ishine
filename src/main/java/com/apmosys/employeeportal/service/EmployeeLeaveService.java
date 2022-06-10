package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class EmployeeLeaveService {

	@Autowired
	LeaveTypeMasterRepository leaveTypeMasterRepository;
	
	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	EmployeeLeavesMapRepository employeeLeavesMapRepository;

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
	
	@Transactional
	public ServiceResponse applyLeave(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<LeaveTypeMaster> leaveTypeList = leaveTypeMasterRepository.findAll();
			
			leaveTypeList.forEach((leaveType)-> {
				
				//checkpoint to determine what type of leave is applied
				if(leaveType.getLeaveTypeMasterId() == leaveDTO.getLeaveTypeMasterId())
				{
					EmployeeLeavesMap employeeLeaves = employeeLeavesMapRepository.findByEmpId(leaveDTO.getEmpId());
					
					
					
					
				}
				
			});
			
			
			
			EmployeeLeave leaveApplication = new EmployeeLeave();
			
				
			leaveApplication.setEmpId(leaveDTO.getEmpId());
			leaveApplication.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
			leaveApplication.setLeaveStatusId((short)1);
			leaveApplication.setFromDate(stringToDateTimeParser.getDate(leaveDTO.getFromDate()));
			leaveApplication.setToDate(stringToDateTimeParser.getDate(leaveDTO.getToDate()));
			leaveApplication.setNoOfDays((short)leaveDTO.getNoOfDays());
			leaveApplication.setReason(leaveDTO.getReason());
			leaveApplication.setManagerId(leaveDTO.getManagerId());
			leaveApplication.getCommonProperty().setCreatedBy(leaveDTO.getCreatedBy());
			
			EmployeeLeave dbResponse = employeeLeaveRepository.save(leaveApplication);
			
			if (dbResponse != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Leave application submitted.");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Creation Failed.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAllMyLeavesByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> list = employeeLeaveRepository.getAllMyLeavesByEmpId(leaveDTO.getEmpId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leave Application found");
				
			} else {
				
				list.forEach((object)-> {
					LeaveDTO dto = new LeaveDTO();
					dto.setLeaveId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setLeaveType(object[1] != null ? object[1].toString() : null);
					dto.setFromDate(object[2] != null ? object[2].toString() : null);
					dto.setToDate(object[3] != null ? object[3].toString() : null);
					dto.setNoOfDays(object[4] != null ? Short.parseShort(object[4].toString()):null);
					dto.setStatus(object[5] != null ? object[5].toString() : null);
					dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
					dto.setCreatedOn(object[7] != null ? object[7].toString() : null);
					dto.setReason(object[8] != null ? object[8].toString() : null);
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

	public ServiceResponse getAllMyTeamsLeavesByManagerId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> list = employeeLeaveRepository.getAllMyTeamsLeavesByManagerId(leaveDTO.getManagerId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leave Application found");
				
			} else {
				
				list.forEach((object)-> {
					LeaveDTO dto = new LeaveDTO();
					dto.setLeaveId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setLeaveType(object[1] != null ? object[1].toString() : null);
					dto.setFromDate(object[2] != null ? object[2].toString() : null);
					dto.setToDate(object[3] != null ? object[3].toString() : null);
					dto.setNoOfDays(object[4] != null ? Short.parseShort(object[4].toString()):null);
					dto.setStatus(object[5] != null ? object[5].toString() : null);
					dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
					dto.setCreatedOn(object[7] != null ? object[7].toString() : null);
					dto.setReason(object[8] != null ? object[8].toString() : null);
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

	public ServiceResponse updateLeaveStatus(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Optional<EmployeeLeave> leaveApplication = employeeLeaveRepository.findById(leaveDTO.getLeaveId());

			if (leaveApplication.isPresent()) {
				EmployeeLeave pendingLeaveApplication = leaveApplication.get();
				// 1 = pending , 2 = Approved , 3= Rejected
				if (leaveDTO.getLeaveStatusId() == 2) {
					pendingLeaveApplication.setLeaveStatusId((short) 2);
				} else if (leaveDTO.getLeaveStatusId() == 3) {
					pendingLeaveApplication.setLeaveStatusId((short) 3);
				}
				EmployeeLeave updatedLeaveApplication = employeeLeaveRepository.save(pendingLeaveApplication);

				if (updatedLeaveApplication != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Leave application updated.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Leave Updation Failed.");
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leave Application found.");
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
			Optional<LeaveTypeMaster> leaveTypeMaster = leaveTypeMasterRepository.findById(leaveDTO.getLeaveTypeMasterId());
			
			if(leaveTypeMaster.isPresent())
			{
				LeaveTypeMaster leaveType = leaveTypeMaster.get();
				
				leaveType.setDescription(leaveDTO.getDescription());
				leaveType.setRules(leaveDTO.getRules());
				LeaveTypeMaster updatedLeaveType = leaveTypeMasterRepository.save(leaveType);
				
				if (updatedLeaveType != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Leave type updated.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Leave type updation failed.");
				}				
			}
			else
			{
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

}
