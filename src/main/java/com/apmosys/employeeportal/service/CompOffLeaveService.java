package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.CompOffLeave;
import com.apmosys.employeeportal.model.CompOffMaster;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.repository.CompOffLeaveRepository;
import com.apmosys.employeeportal.repository.CompOffMasterRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
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
	
	@Autowired
	LeaveTypeMasterRepository leaveTypeMasterRepository;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Autowired
	MailService mailService;
	
	@Value("${hr.mail}")
	private String hrMailAddress;

	public ServiceResponse getAllCompOffReasons() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<CompOffMaster> compOffReasonsList = compOffMasterRepository.findAll();
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			if (compOffReasonsList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Comp off reasons list is empty.");
			} else {
				compOffReasonsList.forEach((reason) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setCompOffId(reason.getCompOffId());
					dto.setCompOffReasons(reason.getCompOffReasons());
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

	public ServiceResponse applyForCompOff(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Apply Comp off req");
		apiLogInfo.setApiUrl("/api/applyForCompOff");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + leaveDTO.getEmpId()+ "managerId : " +leaveDTO.getManagerId()+ "createdBy : " +leaveDTO.getCreatedBy()+ "noOfDays : " +(Float) leaveDTO.getNoOfDays());
		try {

			CompOffLeave leave = new CompOffLeave();

			leave.setDescription(leaveDTO.getDescription());
			leave.setEmpId(leaveDTO.getEmpId());
			leave.setManagerId(leaveDTO.getManagerId());
			// 1= pending , 2= approved , 3 = rejected
			leave.setLeaveStatusId((short) 1);
			// 1 = CL , 2 = PL , 3 = ML , 4 = PTL , 5 = CO
//	        leave.setLeaveTypeMasterId((short)5);
			leave.setLeaveCode("CO");
			leave.setReason(leaveDTO.getReasonId());
			leave.setCreatedBy(leaveDTO.getCreatedBy());
			leave.setFromDate(stringToDateTimeParser.getDate(leaveDTO.getFromDate(), "yyyy-MM-dd"));
			leave.setToDate(stringToDateTimeParser.getDate(leaveDTO.getToDate(), "yyyy-MM-dd"));
			leave.setNoOfDays((Float) leaveDTO.getNoOfDays());

			CompOffLeave leaveApplied = compOffLeaveRepository.save(leave);

			if (leaveApplied != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Compoff Request applied.");
				
				apiLogInfo.setApiResponse("Compoff Request applied.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
				
				
				mailService.sendMailWithCC(leaveDTO.getEmail(), leaveDTO.getManagerEmail()+","+hrMailAddress, "Regarding Comp-Off Request", 
						"Dear "+ leaveDTO.getManagerName()+","+
				"<br> "
				+" &nbsp;"+" &nbsp;"+" "+"Comp-Off Request has been applied by "+ leaveDTO.getEmployeeName() +"for"+" "+leaveDTO.getNoOfDays() +" day(s)"+", Please take necessary action."+
				"<br>"+"<br>"+"<b>"+"Comp-Off Details :"+"<b>"+
				"<br>"+
				"EmpID :"+"A- "+ leaveDTO.getEmployeementId()+
				"<br>"+
				"Name :"+" "+ leaveDTO.getEmployeeName()+
				"<br>"+
				" From "+" "+ leaveDTO.getFromDate() +
				"<br>"+
				" To Date : "+" "+ leaveDTO.getToDate() 
				+"<br>"+
				"No. Of Days :"+" "+leaveDTO.getNoOfDays()+" "+"day(s)"+".");
				
				
				

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Compoff Request creation failed.");
				
				apiLogInfo.setApiResponse("Compoff Request creation failed.");			
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

	public ServiceResponse getPendingCompOffRequestsByManagerId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("view_reportee_comp_off_applications ");
		apiLogInfo.setApiUrl("/api/getPendingCompOffRequestsByManagerId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("managerId : " + leaveDTO.getManagerId() );
		try {

			List<Object[]> objectList = compOffLeaveRepository
					.getPendingCompOffRequestsByManagerId(leaveDTO.getManagerId());

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
				
				apiLogInfo.setApiResponse("dtoList" +dtoList);			
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

	public ServiceResponse getAllCompOffRequestsByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("delete_role");
		apiLogInfo.setApiUrl("/api/getAllCompOffRequestsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " +leaveDTO.getEmpId());
		try {

			List<Object[]> compOffList = compOffLeaveRepository.getAllCompOffRequestsByEmpId(leaveDTO.getEmpId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			if (compOffList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No compoff request(s) found for employee.");
				
				apiLogInfo.setApiResponse("No compoff request(s) found for employee");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				
			} else {
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
				
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("dtoList" +dtoList);			

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
	public ServiceResponse updateCompOffById(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Update Comp off Applications Status");
		apiLogInfo.setApiUrl("/api/updateCompOffById");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " +leaveDTO.getEmpId()+ "compOffLeaveId : " +leaveDTO.getCompOffLeaveId()+ "leaveStatusUpdatedBy :" +leaveDTO.getLeaveStatusUpdatedBy()+ "leaveStatusId" +leaveDTO.getLeaveStatusId());
		try {

			Optional<CompOffLeave> leaveObject = compOffLeaveRepository.findById(leaveDTO.getCompOffLeaveId());
			LeaveTypeMaster leavetypeObj = leaveTypeMasterRepository.findByLeaveTypeCode("CO");
			System.out.println("  leave type" + " leavetypeObj " +leavetypeObj);
			
			if (leaveObject.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No compoff request(s) found.");
			} else {

				CompOffLeave compOffLeave = leaveObject.get();

				compOffLeave.setLeaveStatusUpdatedBy(leaveDTO.getLeaveStatusUpdatedBy());
				compOffLeave.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());

				// 1 = pending , 2 = Approved , 3= Rejected
				compOffLeave.setLeaveStatusId(leaveDTO.getLeaveStatusId());

				if (leaveDTO.getLeaveStatusId() == 2) {
					// 1 = CO
					EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository
							.findByEmpIdAndLeaveTypeMasterId(leaveDTO.getEmpId(), leavetypeObj.getLeaveTypeMasterId());

					employeeLeavesMap.setBalance(employeeLeavesMap.getBalance() + compOffLeave.getNoOfDays());
					employeeLeavesMapRepository.save(employeeLeavesMap);

					LeaveBalanceLog log = new LeaveBalanceLog();
					log.setBalance(employeeLeavesMap.getBalance());
					log.setEmpId(leaveDTO.getEmpId());
					log.setLeaveTypeMasterId(leavetypeObj.getLeaveTypeMasterId());
					log.setMessage(
							LeaveLogMessage.compOffAddLeave.replace("0.0", compOffLeave.getNoOfDays().toString()));
					log.setUpdateBalanceBy("+" + compOffLeave.getNoOfDays());
					leaveBalanceLogRepository.save(log);
					response.setServiceResponse("Compoff request application approved.");
				} else if (leaveDTO.getLeaveStatusId() == 3) {
					response.setServiceResponse("CompOff request application rejected.");
				}

				CompOffLeave compOffUpdated = compOffLeaveRepository.save(compOffLeave);

				if (compOffUpdated != null) {
					if(leaveDTO.getLeaveStatusId() == 2) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Compoff Request Approved");
					}else if(leaveDTO.getLeaveStatusId() == 3){
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Compoff Request Rejected");
					}
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse("Compoff leave status updated");

					
					apiLogInfo.setApiResponse("Compoff leave status updated");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Compoff leave status updation failed.");
					
					apiLogInfo.setApiResponse("Compoff leave status updation failed.");			
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

	public ServiceResponse countPendingCompOffRequestsByManagerId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Update Comp off Applications Status");
		apiLogInfo.setApiUrl("/api/countPendingCompOffRequestsByManagerId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("managerId : " +leaveDTO.getManagerId());
		try {

			Long applicationCount = compOffLeaveRepository
					.countPendingCompOffRequestsByManagerId(leaveDTO.getManagerId());

			if (applicationCount == 0) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No compoff request(s) found.");
				
				apiLogInfo.setApiResponse("No compoff request(s) found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			} else {
				leaveDTO = new LeaveDTO();
				leaveDTO.setApplicationCount(applicationCount);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(leaveDTO);
				
				apiLogInfo.setApiResponse("leaveDTO" +leaveDTO);			
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

}
