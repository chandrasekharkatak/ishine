package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeRoleMasterRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class ReportService {
	
	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;
	
	@Autowired
	TimesheetsRepository timesheetsRepository;
	
	@Autowired
	EmployeeRoleMasterRepository employeeRoleMasterRepository;

	public ServiceResponse leaveReport() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> allLeaveData = employeeLeaveRepository.getLeaveReport();
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			if (allLeaveData != null) {
				allLeaveData.forEach((object) -> {
					LeaveDTO leavedto = new LeaveDTO();

					leavedto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					leavedto.setEmployeeName(object[1] != null ? object[1].toString() : null);
					leavedto.setLeaveType(object[2] != null ? object[2].toString() : null);
					leavedto.setFromDate(object[3] != null ? object[3].toString() : null);
					leavedto.setToDate(object[4] != null ? object[4].toString() : null);
					leavedto.setNoOfDays(object[5] != null ? Float.parseFloat(object[5].toString()) : null);
					leavedto.setReason(object[6] != null ? object[6].toString() : null);
					leavedto.setStatus(object[7] != null ? object[7].toString() : null);
					leavedto.setManagerName(object[8] != null ? object[8].toString() : null);
					leavedto.setCreatedOn(object[9] != null ? object[9].toString() : null);
					leavedto.setUpdatedOn(object[10] != null ? object[10].toString() : null);
					leavedto.setLeaveStatusUpdatedByName(object[11] != null ? object[11].toString() : null);
					leavedto.setDepartmentName(object[12] != null ? object[12].toString() : null);
					dtoList.add(leavedto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("leave Application list is empty.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse timesheetReport() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> allTimeSheetData = timesheetsRepository.getAllTimesheetData();
			List<TimesheetDTO> dtoList = new ArrayList<TimesheetDTO>();
			
			if(allTimeSheetData != null) {
				allTimeSheetData.forEach((object) -> {
					TimesheetDTO timesheetDto = new TimesheetDTO();
					
					timesheetDto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					timesheetDto.setEmployeeName(object[1] != null ? object[1].toString() : null);
					timesheetDto.setDate(object[2] != null ? object[2].toString() : null);
					timesheetDto.setDayType(object[3] != null ? object[3].toString() : null);
					timesheetDto.setDescription(object[4] != null ? object[4].toString() : null);
					timesheetDto.setStatus(object[5] != null ? object[5].toString() : null);
					timesheetDto.setTotalWorkingHours(object[6] != null ? Float.parseFloat(object[6].toString()) : null);
					timesheetDto.setCreatedOn(object[7] != null ? object[7].toString() : null);
					timesheetDto.setUpdatedOn(object[8] != null ? object[8].toString() : null);
					timesheetDto.setTimesheetStatusUpdatedByName(object[9] != null ? object[9].toString() : null);
					dtoList.add(timesheetDto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				System.out.println("DTOList :" +dtoList);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Timesheet list is empty.");
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAccessControlListData(EmployeeDTO employeeDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			List<Object[]> accessControlList = employeeRoleMasterRepository
					.getAccessControlList(employeeDto.getJobRoleName(), employeeDto.getDepartmentId(), employeeDto.getEmployeeRole());
			
			List<EmployeeDTO> dtoList = new ArrayList<>();
			
			if(!accessControlList.isEmpty()) {
				
				accessControlList.forEach((object) -> {
					EmployeeDTO empDTO = new EmployeeDTO();
					
					empDTO.setDepartmentName(object[0] != null ? object[0].toString() : null);
					empDTO.setJobRoleName(object[1] != null ? object[1].toString() : null);
					empDTO.setEmployeeRole(object[2] != null ? object[2].toString() : null);
					empDTO.setTabName(object[3] != null ? object[3].toString() : null);
					empDTO.setFeatureName(object[4] != null ? object[4].toString() : null);
					empDTO.setSubFeatureName(object[5] != null ? object[5].toString() : null);
					
					dtoList.add(empDTO);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS); 
				response.setServiceResponse(dtoList);
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("ACL (Access control list) is empty.");
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse getAccessControlListByPersona(EmployeeDTO employeeDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			List<Object[]> accessControlList = employeeRoleMasterRepository
					.getAccessControlListByPersona(employeeDto.getEmployeeRole());
			
			List<EmployeeDTO> dtoList = new ArrayList<>();
			
			if(!accessControlList.isEmpty()) {
				
				accessControlList.forEach((object) -> {
					EmployeeDTO empDTO = new EmployeeDTO();
					
					empDTO.setEmployeeRole(object[0] != null ? object[0].toString() : null);
					empDTO.setTabName(object[1] != null ? object[1].toString() : null);
					empDTO.setFeatureName(object[2] != null ? object[2].toString() : null);
					empDTO.setSubFeatureName(object[3] != null ? object[3].toString() : null);
					
					dtoList.add(empDTO);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS); 
				response.setServiceResponse(dtoList);
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("ACL (Access control list) is empty.");
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

}
