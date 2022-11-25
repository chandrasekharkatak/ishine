package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.model.LeaveRevokeApplication;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.repository.CompOffMasterRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.repository.LeaveRevokeApplicationRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.utility.LeaveLogMessage;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class EmployeeLeaveService {

	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

	@Autowired
	EmployeeLeavesMapRepository employeeLeavesMapRepository;

	@Autowired
	CompOffMasterRepository compOffMasterRepository;

	@Autowired
	LeaveBalanceLogRepository leaveBalanceLogRepository;

	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	LeaveTypeMasterRepository leaveTypeMasterRepository;
	
	@Autowired
	LeaveRevokeApplicationRepository leaveRevokeApplicationRepository;
	
	@Autowired
	private MailService mailService;
	
	@Value("${hr.mail}")
	private String hrMailAddress;
	
	@Autowired
	private LogService logService;

	@Autowired
	private HttpServletRequest httpRequest;
	
	@Transactional
	public ServiceResponse applyLeave(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Apply Leave");
		apiLogInfo.setApiUrl("/api/applyLeave");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+leaveDTO.getEmpId()+ ", leaveTypeMasterId : "+ leaveDTO.getLeaveTypeMasterId()+", leaveTypeCode : "+ leaveDTO.getLeaveTypeCode() +", noOfDays : "+ leaveDTO.getNoOfDays());
		
		try {
			//LeaveTypeMaster leaveTypeMasterObj = leaveTypeMasterRepository.findByLeaveTypeCode(leaveDTO.getLeaveTypeCode());		
			EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository
					.findByEmpIdAndLeaveTypeMasterId(leaveDTO.getEmpId(), leaveDTO.getLeaveTypeMasterId());
			
			System.out.println("Leave DTO check :"+leaveDTO);
			
			List<Object[]> empObj = employeeRepository.getManagerEmail(leaveDTO.getEmpId());
			
			EmployeeDTO empDto = new EmployeeDTO();
			
				empObj.forEach((object) -> {
					
					empDto.setEmail(object[0] != null ? object[0].toString() : null);
					empDto.setManagerEmail(object[1] != null ? object[1].toString() : null);
					empDto.setName(object[2] != null ? object[2].toString() : null);
					empDto.setEmployeementId(object[3] != null ? Long.parseLong(object[3].toString()): null);
					empDto.setManagerName(object[4] != null ? object[4].toString() : null);
					});
				
			
			// HERE : Effective Leave Balance = employeeLeavesMap.getBalance()
			if (!leaveDTO.getLeaveTypeCode().equalsIgnoreCase("LWP") && (employeeLeavesMap.getBalance() == 0
					|| employeeLeavesMap.getBalance() < leaveDTO.getNoOfDays())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Your available balance of " + employeeLeavesMap.getBalance()
						+ " day(s) is not sufficient for this Leave Application.");

				return response;
			}

			EmployeeLeave leaveApplication = new EmployeeLeave();

			leaveApplication.setEmpId(leaveDTO.getEmpId());
			leaveApplication.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
			leaveApplication.setLeaveStatusId((short) 1);
			leaveApplication.setFromDate(stringToDateTimeParser.getDate(leaveDTO.getFromDate(), "yyyy-MM-dd"));
			leaveApplication.setToDate(stringToDateTimeParser.getDate(leaveDTO.getToDate(), "yyyy-MM-dd"));
			leaveApplication.setNoOfDays((Float) leaveDTO.getNoOfDays());
			leaveApplication.setReason(leaveDTO.getReason());
			leaveApplication.setManagerId(leaveDTO.getManagerId());
			leaveApplication.getCommonProperty().setCreatedBy(leaveDTO.getCreatedBy());

			// Leave deduction from balance leaves
			Float balance = employeeLeavesMap.getBalance();
			balance = balance - leaveDTO.getNoOfDays();
			Float pendingForApproval = employeeLeavesMap.getPendingForApproval();
			pendingForApproval = pendingForApproval + leaveDTO.getNoOfDays();

			employeeLeavesMap.setBalance(balance);
			employeeLeavesMap.setPendingForApproval(pendingForApproval);

			EmployeeLeavesMap dbResponse1 = employeeLeavesMapRepository.save(employeeLeavesMap);

			EmployeeLeave dbResponse2 = employeeLeaveRepository.save(leaveApplication);

			if (dbResponse1 != null && dbResponse2 != null) {
				LeaveBalanceLog log = new LeaveBalanceLog();
				
				log.setBalance(balance);
				log.setEmpId(leaveDTO.getEmpId());
				log.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
				log.setMessage(LeaveLogMessage.requestDeductLeave.replace("0.0", leaveDTO.getNoOfDays().toString()));
				log.setUpdateBalanceBy("-" + leaveDTO.getNoOfDays());

				leaveBalanceLogRepository.save(log);

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Leave application submitted.");
				
				LeaveTypeMaster leaveType = leaveTypeMasterRepository.findByLeaveTypeCode(leaveDTO.getLeaveTypeCode());
				
				if(leaveDTO.getCreatedBy().equals(leaveDTO.getEmpId())){
					//Leave Applied for self
					
					mailService.sendMailWithCC(empDto.getManagerEmail(), hrMailAddress +","+ empDto.getEmail(),
							"Regarding Leave Application Request",
							"Dear "+ empDto.getManagerName() + ","
							+"<br>"+ empDto.getName() + " has applied leave for " + leaveDTO.getNoOfDays() + " days" 
							+"<br><br> Leave Details :"
							+"<br> EmpId : A-" + empDto.getEmployeementId()
							+"<br> Name : " + empDto.getName()
							+"<br> From Date : " + leaveDTO.getFromDate() + "   To Date : " + leaveDTO.getToDate()
							+"<br> No. Of Days : " + leaveDTO.getNoOfDays()
							+"<br> Leave Type : " + leaveType.getLeaveType()
							+"<br> Leave reason : " + leaveDTO.getReason());
					
				}else {
					//Leave Applied for team
					Optional<Employee> createdByEmp = employeeRepository.findById(leaveDTO.getCreatedBy());
					if(!createdByEmp.isEmpty()) {
						Employee createdByObj = createdByEmp.get();
						
						mailService.sendMailWithCC(empDto.getManagerEmail(), hrMailAddress +","+ empDto.getEmail() +","+ createdByObj.getEmail(),
								"Regarding Leave Application Request",
								"Dear "+ empDto.getManagerName() + ","
								+"<br> Leave has been applied for "+ empDto.getName() +" by "+createdByObj.getName()
								+"<br><br> Leave Details :"
								+"<br> EmpId : A-" + empDto.getEmployeementId()
								+"<br> Name : " + empDto.getName()
								+"<br> From Date : " + leaveDTO.getFromDate() + "   To Date : " + leaveDTO.getToDate()
								+"<br> No. Of Days : " + leaveDTO.getNoOfDays()
								+"<br> Leave Type : " + leaveType.getLeaveType()
								+"<br> Leave reason : " + leaveDTO.getReason());
					}
				}
			
				apiLogInfo.setApiResponse("Leave application submitted.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);	
			} else {
				response.setServiceResponse("Leave Creation Failed.");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				
				apiLogInfo.setApiResponse("Leave Creation Failed.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiError(e.getMessage());			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Transactional
	public ServiceResponse deletePendingLeave(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Delete Leave");
		apiLogInfo.setApiUrl("/api/deletePendingLeave");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("LeaveId : "+leaveDTO.getLeaveId()+", NoOfDays : "+ leaveDTO.getNoOfDays());
		
		try {

			Optional<EmployeeLeave> leaveObject = employeeLeaveRepository.findById(leaveDTO.getLeaveId());

			if (leaveObject.isPresent()) {

				EmployeeLeave leaveToBeDeleted = leaveObject.get();

				employeeLeaveRepository.deleteById(leaveDTO.getLeaveId());

				EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository.findByEmpIdAndLeaveTypeMasterId(
						leaveToBeDeleted.getEmpId(), leaveToBeDeleted.getLeaveTypeMasterId());

				Float balance = employeeLeavesMap.getBalance();
				balance = balance + leaveDTO.getNoOfDays();
				Float pendingForApproval = employeeLeavesMap.getPendingForApproval();
				pendingForApproval = pendingForApproval - leaveDTO.getNoOfDays();

				employeeLeavesMap.setBalance(balance);
				employeeLeavesMap.setPendingForApproval(pendingForApproval);

				System.out.println(employeeLeavesMap + " employee leave");
				EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(employeeLeavesMap);

				if (dbResponse != null) { 

					LeaveBalanceLog log = new LeaveBalanceLog();

					log.setBalance(balance);
					log.setEmpId(leaveToBeDeleted.getEmpId());
					log.setLeaveTypeMasterId(leaveToBeDeleted.getLeaveTypeMasterId());
					log.setMessage(LeaveLogMessage.deleteLeave.replace("0.0", leaveDTO.getNoOfDays().toString()));
					log.setUpdateBalanceBy("+" + leaveDTO.getNoOfDays());

					leaveBalanceLogRepository.save(log);

				}

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Leave Application Deleted.");
				
				apiLogInfo.setApiResponse("Leave Application Deleted.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);	

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Application Not Found.");
				
				apiLogInfo.setApiResponse("Leave Application Not Found.");			
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

	@Transactional
	public ServiceResponse updatePendingLeave(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Update Leave");
		apiLogInfo.setApiUrl("/api/updatePendingLeave");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("LeaveId : "+leaveDTO.getLeaveId()+", Reason : "+ leaveDTO.getReason());
		
		try {

			Optional<EmployeeLeave> leaveObject = employeeLeaveRepository.findById(leaveDTO.getLeaveId());

			if (leaveObject.isPresent()) {

				EmployeeLeave leaveToBeUpdated = leaveObject.get();

				leaveToBeUpdated.setReason(leaveDTO.getReason());

				EmployeeLeave dbResponse = employeeLeaveRepository.save(leaveToBeUpdated);

				if (dbResponse != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Leave Application Updated.");
					
					apiLogInfo.setApiResponse("Leave Application Updated.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Leave Application Updation Failed.");
					
					apiLogInfo.setApiResponse("Leave Application Updation Failed.");			
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

	public ServiceResponse getAllMyLeaveApplicationsByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllMyLeaveApplicationsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+leaveDTO.getEmpId());
		
		try {
			List<Object[]> list = employeeLeaveRepository.getAllMyLeaveApplicationsByEmpId(leaveDTO.getEmpId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leave Application found");

				apiLogInfo.setApiResponse("No Leave Application found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {

				list.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setLeaveId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setLeaveType(object[1] != null ? object[1].toString() : null);
					dto.setFromDate(object[2] != null ? object[2].toString() : null);
					dto.setToDate(object[3] != null ? object[3].toString() : null);
					dto.setNoOfDays(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
					dto.setStatus(object[5] != null ? object[5].toString() : null);
					dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
					dto.setCreatedOn(object[7] != null ? object[7].toString() : null);
					dto.setReason(object[8] != null ? object[8].toString() : null);
					dto.setLeaveTypeMasterId(object[9] != null ? Short.parseShort(object[9].toString()) : null);
					dto.setRemark(object[10] != null ? object[10].toString() : null);
					dtoList.add(dto);
				});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse(dtoList.size() + " Applications found.");
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

	public ServiceResponse getAllMyTeamsPendingLeaveApplicationsByManagerId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllMyTeamsPendingLeaveApplicationsByManagerId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("ManagerId : "+leaveDTO.getManagerId());
		
		try {
			List<Object[]> list = employeeLeaveRepository
					.getAllMyTeamsPendingLeaveApplicationsByManagerId(leaveDTO.getManagerId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leave Application found");

				apiLogInfo.setApiResponse("No Leave Application found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {

				list.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setLeaveId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setLeaveType(object[1] != null ? object[1].toString() : null);
					dto.setFromDate(object[2] != null ? object[2].toString() : null);
					dto.setToDate(object[3] != null ? object[3].toString() : null);
					dto.setNoOfDays(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
					dto.setStatus(object[5] != null ? object[5].toString() : null);
					dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
					dto.setCreatedOn(object[7] != null ? object[7].toString() : null);
					dto.setReason(object[8] != null ? object[8].toString() : null);
					dto.setEmpId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
					dto.setLeaveTypeMasterId(object[10] != null ? Short.parseShort(object[10].toString()) : null);
					dto.setEmployeeName(object[11] != null ? object[11].toString() : null);
					dto.setEmail(object[12] != null ? object[12].toString() : null);
					dto.setEmployeementId(object[13] != null ? Long.parseLong(object[13].toString()) : null);
					dtoList.add(dto);
				});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse(dtoList.size() + " Applications found.");
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

	@Transactional
	public ServiceResponse updateLeaveStatus(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Update Leave Status");
		apiLogInfo.setApiUrl("/api/updateLeaveStatus");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("LeaveId : "+leaveDTO.getLeaveId()+", EmpId : "+ leaveDTO.getEmpId()+", LeaveTypeMasterId : "+ leaveDTO.getLeaveTypeMasterId()+", LeaveStatusId : "+ leaveDTO.getLeaveStatusId()+", LeaveStatusUpdatedBy : "+ leaveDTO.getLeaveStatusUpdatedBy());
		
		try {
			Optional<EmployeeLeave> leaveApplication = employeeLeaveRepository.findById(leaveDTO.getLeaveId());
			EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository
					.findByEmpIdAndLeaveTypeMasterId(leaveDTO.getEmpId(), leaveDTO.getLeaveTypeMasterId());
			System.out.println("leaveDTO.getEmpId() : -- " +leaveDTO.getEmpId());
			System.out.println("leaveDTO.getLeaveTypeMasterId() : -- " +leaveDTO.getLeaveTypeMasterId());
			if (leaveApplication.isPresent()) {
				EmployeeLeave pendingLeaveApplication = leaveApplication.get();

				pendingLeaveApplication.setLeaveStatusUpdatedBy(leaveDTO.getLeaveStatusUpdatedBy());
				pendingLeaveApplication.getCommonProperty().setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());

				employeeLeavesMap.setPendingForApproval(
						employeeLeavesMap.getPendingForApproval() - pendingLeaveApplication.getNoOfDays());

				// 1 = pending , 2 = Approved , 3= Rejected
				if (leaveDTO.getLeaveStatusId() == 2) {
					pendingLeaveApplication.setLeaveStatusId((short) 2);
					
					// Increase Notice period If employee resigned
					
					Optional<Employee> employee = employeeRepository.findById(leaveDTO.getEmpId());
					Optional<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findById(leaveDTO.getLeaveTypeMasterId());
					
					LeaveTypeMaster leaveTypeObj = leaveType.get();
					
					if (!employee.isEmpty()) {
						Employee empObj = employee.get();
						if (empObj.getEmploymentstatus().equals("Resigned") && 
								(leaveTypeObj.getLeaveTypeCode().equals("PL") || leaveTypeObj.getLeaveTypeCode().equals("CL"))) {
							
							empObj.setNoticePeriod((short) Math
									.ceil(empObj.getNoticePeriod() + pendingLeaveApplication.getNoOfDays()));
							employeeRepository.save(empObj);
						}
					}
					
					response.setServiceResponse("Leave application approved.");
					apiLogInfo.setApiResponse("Leave application approved.");
					
				} else if (leaveDTO.getLeaveStatusId() == 3) {
					pendingLeaveApplication.setLeaveStatusId((short) 3);
					pendingLeaveApplication.setRemark(leaveDTO.getRejectReason());
					employeeLeavesMap
							.setBalance(employeeLeavesMap.getBalance() + pendingLeaveApplication.getNoOfDays());

					LeaveBalanceLog log = new LeaveBalanceLog();
					log.setBalance(employeeLeavesMap.getBalance());
					log.setEmpId(leaveDTO.getEmpId());
					log.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
					log.setMessage(LeaveLogMessage.requestAddLeave.replace("0.0",
							pendingLeaveApplication.getNoOfDays().toString()));
					log.setUpdateBalanceBy("+" + pendingLeaveApplication.getNoOfDays());
					leaveBalanceLogRepository.save(log);
					response.setServiceResponse("Leave application rejected.");
					apiLogInfo.setApiResponse("Leave application rejected.");
				}
				EmployeeLeave updatedLeaveApplication = employeeLeaveRepository.save(pendingLeaveApplication);
				EmployeeLeavesMap updatedEmployeeLeavesMap = employeeLeavesMapRepository.save(employeeLeavesMap);
				
				
				
				mailService.sendMail(leaveDTO.getEmail(),
						"Regarding leave Rejection ", "Employee Id"+" A-"+leaveDTO.getEmployeementId()+
						" "+ " <br> "+" Employee Name -"+" "+leaveDTO.getEmployeeName()+" <br> "+" Reason -: "+leaveDTO.getRejectReason());
			
				System.out.println(" leaveDTO.getEmployeementId() :  "+leaveDTO.getEmployeementId());
				System.out.println(" leaveDTO.getEmail()  :  "+leaveDTO.getEmail());
				System.out.println("  leaveDTO.getRejectReason()   :  "+leaveDTO.getRejectReason());

				if (updatedLeaveApplication != null && updatedEmployeeLeavesMap != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Leave Updation Failed.");
					
					apiLogInfo.setApiResponse("Leave Updation Failed.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leave Application found.");
				
				apiLogInfo.setApiResponse("No Leave Application found.");			
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

	public ServiceResponse getMyLeaveBalancesByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		Float totalbalance = 0.0f;
		Float totalPendingForApproval = 0.0f;
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getMyLeaveBalancesByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmployeementId : "+leaveDTO.getEmployeementId());

		try {

			Employee employee = employeeRepository.findByEmployeementId(leaveDTO.getEmployeementId());

			if (employee != null) {
				List<Object[]> employeeLeavesList = employeeLeavesMapRepository
						.getMyLeaveBalancesByEmpId(employee.getEmpId());
				List<Object[]> employeeData = employeeRepository
						.getEmployeeData(employee.getEmpId());
				List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
				List<LeaveDTO> employeeDataList = new ArrayList<LeaveDTO>();

				if (employeeLeavesList.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Leaves balance found.");

				} else {

					employeeLeavesList.forEach((object) -> {
						LeaveDTO dto = new LeaveDTO();
						dto.setLeaveType(object[0] != null ? object[0].toString() : null);
						dto.setBalance(object[1] != null ? Float.parseFloat(object[1].toString()) : null);
						dto.setPendingForApproval(object[2] != null ? Float.parseFloat(object[2].toString()) : null);
						dto.setLeaveTypeMasterId(object[3] != null ? Short.parseShort(object[3].toString()) : null);
						dto.setLeaveTypeCode(object[4] != null ? object[4].toString() : null);

						dtoList.add(dto);
					});

					for (Object[] leave : employeeLeavesList) {
						totalbalance = totalbalance + (leave[1] != null ? Float.parseFloat(leave[1].toString()) : null);
						totalPendingForApproval = totalPendingForApproval
								+ (leave[2] != null ? Float.parseFloat(leave[2].toString()) : null);
					}

					LeaveDTO dto = new LeaveDTO();
					dto.setLeaveType("Total");
					dto.setBalance(totalbalance);
					dto.setPendingForApproval(totalPendingForApproval);
					dtoList.add(dto);
					
					employeeData.forEach((object) -> {
						LeaveDTO empDto = new LeaveDTO();
						empDto.setManagerName(object[0] != null ? object[0].toString() : null);
						empDto.setJobRoleName(object[1] != null ? object[1].toString() : null);
						empDto.setDepartmentName(object[2] != null ? object[2].toString() : null);
						empDto.setEmploymentStatus(object[3] != null ? object[3].toString() : null);
						empDto.setEmployeeName(employee.getName());
						employeeDataList.add(empDto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					response.setServiceResponse1(employee.getEmpId());
					response.setServiceResponse2(employeeDataList);
					
					apiLogInfo.setApiResponse("Leave Balance Found.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee not found. Kindly check Employee ID.");
				
				apiLogInfo.setApiResponse("Employee not found. Kindly check Employee ID.");			
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

	@Transactional
	public ServiceResponse updateLeavesByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/updateLeavesByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+leaveDTO.getEmpId());
		
		try {

			List<EmployeeLeavesMap> employeeLeavesList = employeeLeavesMapRepository
					.findAllByEmpId(leaveDTO.getEmpId());

			if (employeeLeavesList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leaves found.");
				
				apiLogInfo.setApiResponse("No Leaves found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {
				employeeLeavesList.forEach((leave) -> {

					leaveDTO.getEmployeeLeaveList().forEach((dto) -> {

						if (leave.getLeaveTypeMasterId() == dto.getLeaveTypeMasterId()) {

							if (leave.getBalance() == 0) {
								// set leaves to newly created employee when his/her bucket is 0.0 for all leave
								// types
								LeaveBalanceLog log = new LeaveBalanceLog();
								log.setBalance(dto.getBalance());
								log.setEmpId(leaveDTO.getEmpId());
								log.setLeaveTypeMasterId(dto.getLeaveTypeMasterId());
								log.setMessage(
										LeaveLogMessage.adminAddLeave.replace("0.0", dto.getBalance().toString()));
								log.setUpdateBalanceBy("+" + dto.getBalance());
								leaveBalanceLogRepository.save(log);

								leave.setBalance(dto.getBalance());
							} else {

								if (dto.getBalance().equals(leave.getBalance())) {
									// No change in balance leave
									leave.setBalance(dto.getBalance());
								} else {

									LeaveBalanceLog log = new LeaveBalanceLog();
									log.setBalance(dto.getBalance());
									log.setEmpId(leaveDTO.getEmpId());
									log.setLeaveTypeMasterId(dto.getLeaveTypeMasterId());

									if (dto.getBalance() > leave.getBalance()) {
										// leave added to bucket balance
										Float change = dto.getBalance() - leave.getBalance();
										log.setMessage(LeaveLogMessage.adminAddLeave.replace("0.0", change.toString()));
										log.setUpdateBalanceBy("+" + change);

									} else if (dto.getBalance() < leave.getBalance()) {
										// leave deducted from bucket balance
										Float change = dto.getBalance() - leave.getBalance();
										log.setMessage(
												LeaveLogMessage.adminDeductLeave.replace("0.0", (change * -1) + ""));
										log.setUpdateBalanceBy(change.toString());
									}
									leave.setBalance(dto.getBalance());
									leaveBalanceLogRepository.save(log);
								}
							}

						}

					});

				});
			}

			List<EmployeeLeavesMap> updatedEmployeeLeavesList = employeeLeavesMapRepository.saveAll(employeeLeavesList);

			if (updatedEmployeeLeavesList.isEmpty() || updatedEmployeeLeavesList == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee leaves updation failed.");
				
				apiLogInfo.setApiResponse("Employee leaves updation failed.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Employee leaves updated.");
				
				apiLogInfo.setApiResponse("Employee leaves updated.");
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

	public ServiceResponse updateLeaveBalanceByEmployeementId(LeaveDTO leaveDTO) {			
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/updateLeaveBalanceByEmployeementId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmployeementId : "+leaveDTO.getEmployeementId()+ ", Balance : "+leaveDTO.getBalance());
		
		try {			
								
			System.out.println(leaveDTO.getEmployeementId() + " employeement id");			
			Optional<Employee> EmpId = Optional.ofNullable(employeeRepository.findByEmployeementId(leaveDTO.getEmployeementId()));			
			if(EmpId.isPresent()) {			
							
				Employee employee = EmpId.get();			
				Long primaryEmpid = employee.getEmpId();			
				System.out.println(primaryEmpid + " primary empid");			
							
							
					List<EmployeeLeavesMap> employeeLeavesList = employeeLeavesMapRepository.findAllByEmpId(primaryEmpid);			
								
					if (employeeLeavesList.isEmpty()) {			
						System.out.println("in if block");			
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);			
						response.setServiceResponse("No Leaves found.");
						
						apiLogInfo.setApiResponse("No Leaves found.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

					} else {			
									
						for(EmployeeLeavesMap e: employeeLeavesList) {			
							System.out.println(e.getLeaveTypeMasterId() + " leave type id");			
										
							if(e.getLeaveTypeMasterId() == 4) {			
								e.setBalance(leaveDTO.getBalance());			
								System.out.println(leaveDTO.getBalance() + " set balance");			
    						}			
//							else if(e.getLeaveTypeMasterId() == 3) {			
//								e.setBalance(leaveDTO.getBalance());			
//								System.out.println(leaveDTO.getBalance() + " set balance");			
//							}			
//							else if(e.getLeaveTypeMasterId() == 5) {			
//								e.setBalance(leaveDTO.getBalance());			
//								System.out.println(leaveDTO.getBalance() + " set balance");			
//							}			
						
											
							EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(e);			
							System.out.println(dbResponse + " dp response");			
										
							if(dbResponse != null) {			
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);					
								response.setServiceResponse("leave balance Updated.");	
								
								apiLogInfo.setApiResponse("leave balance Updated.");
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
							}else {			
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);					
								response.setServiceResponse("leave balance Updation Failed.");
								
								apiLogInfo.setApiResponse("leave balance Updation Failed.");			
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
							}			
						}			
									
									
					}			
							
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);					
				response.setServiceResponse("Employee not found");
				
				apiLogInfo.setApiResponse("Employee not found");			
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
		
	public ServiceResponse addOldLeaveApplicationByList(LeaveDTO leaveDTO) {			
		ServiceResponse response = new ServiceResponse();			
		try {			
						
			Optional<Employee> EmpId = Optional.ofNullable(employeeRepository.findByEmployeementId(leaveDTO.getEmployeementId()));			
			if(EmpId.isPresent()) {			
							
				Employee employee = EmpId.get();			
				Long primaryEmpid = employee.getEmpId();			
				Long managerId = employee.getManagerId();			
				Long approverId;			
							
				Employee approverName = employeeRepository.findByName(leaveDTO.getLeaveStatusUpdatedByName());			
				if(approverName == null) {			
					approverId = (long) 2;			
				}else {			
					approverId = approverName.getEmpId();			
				}			
							
			EmployeeLeave leaveApplication = new EmployeeLeave();			
			leaveApplication.setEmpId(primaryEmpid);			
						
			if(leaveDTO.getLeaveType().equals("PL")) {			
				leaveApplication.setLeaveTypeMasterId((short) 1);			
			}else if(leaveDTO.getLeaveType().equals("LWP")) {			
				leaveApplication.setLeaveTypeMasterId((short) 3);			
			}else if(leaveDTO.getLeaveType().equals("CO")) {			
				leaveApplication.setLeaveTypeMasterId((short) 4);			
			}			
						
			if(leaveDTO.getStatus().equals("Approved")) {			
				leaveApplication.setLeaveStatusId((short) 2);			
			}else if(leaveDTO.getStatus().equals("Rejected")) {			
				leaveApplication.setLeaveStatusId((short) 3);			
			}else if(leaveDTO.getStatus().equals("Pending")) {			
				leaveApplication.setLeaveStatusId((short) 1);			
			}			
						
			leaveApplication.setFromDate(stringToDateTimeParser.getDate(leaveDTO.getFromDate(), "yyyy-MM-dd"));			
			leaveApplication.setToDate(stringToDateTimeParser.getDate(leaveDTO.getToDate(), "yyyy-MM-dd"));			
			leaveApplication.setNoOfDays((Float) leaveDTO.getNoOfDays());			
			leaveApplication.setReason(leaveDTO.getReason());			
						
			if(managerId == null) {			
				leaveApplication.setManagerId(2);			
			}else {			
				leaveApplication.setManagerId(managerId.intValue());			
			}			
						
			leaveApplication.setHodId(approverId);			
			leaveApplication.setLeaveStatusUpdatedBy(approverId);			
						
			leaveApplication.getCommonProperty().setCreatedBy(primaryEmpid);			
						
						
			final String OLD_FORMAT = "yyyy-MM-dd";			
			final String NEW_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";			
			String oldDateString = leaveDTO.getCreatedOn();			
			String newDateString;			
			DateFormat formatter = new SimpleDateFormat(OLD_FORMAT);			
			Date d = formatter.parse(oldDateString);			
			((SimpleDateFormat) formatter).applyPattern(NEW_FORMAT);			
			newDateString = formatter.format(d);		
			Timestamp ts = Timestamp.valueOf(newDateString);		
					
					
			leaveApplication.getCommonProperty().setCreatedOn(ts);			
			EmployeeLeave dbResponse = employeeLeaveRepository.save(leaveApplication);			
			System.out.println("db response=============================================");			
			if(dbResponse != null) {			
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);					
				response.setServiceResponse("Application created");			
			}else {			
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);					
				response.setServiceResponse("Application creation Failed.");			
			}			
							
			}			
						
		}catch (Exception e) {			
			e.printStackTrace();			
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);			
			response.setServiceResponse("Something Went Wrong.");			
			response.setServiceError(e.getMessage());			
		}			
		return response;			
	}					
	
	public ServiceResponse getLeaveLogsByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getLeaveLogsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+leaveDTO.getEmpId());
		
		try {
			List<Object[]> objectList = leaveBalanceLogRepository.getLeaveLogsByEmpId(leaveDTO.getEmpId());

			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (objectList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No leave logs found for employee.");

				apiLogInfo.setApiResponse("No leave logs found for employee.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {
				int i = 0;
				LeaveDTO dto;
				for (Object[] object : objectList) {
					dto = new LeaveDTO();
					i++;
					dto.setLeaveType(object[0] != null ? object[0].toString() : null);
					dto.setUpdateBalanceBy(object[1] != null ? object[1].toString() : null);
					dto.setBalance(object[2] != null ? Float.parseFloat(object[2].toString()) : null);
					dto.setMessage(object[3] != null ? object[3].toString() : null);
					dto.setCreatedOn(object[4] != null ? object[4].toString() : null);
					dto.setRowNumber(i);
					dtoList.add(dto);
				}

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse(dtoList.size() +" Leave Logs Found.");
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

	public ServiceResponse getAppliedLeaveApplicationsByEmpIdAndDateRange(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAppliedLeaveApplicationsByEmpIdAndDateRange");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+leaveDTO.getEmpId()+", FromDate : "+ leaveDTO.getFromDate()+", ToDate : "+ leaveDTO.getToDate());
		
		try {
			// HERE : We are fetching All Past Leave Applications with Pending & Approved
			// Status
			List<Object[]> list = employeeLeaveRepository.getAppliedLeaveApplicationsByEmpIdAndDateRange(
					leaveDTO.getEmpId(), leaveDTO.getFromDate(), leaveDTO.getToDate(), leaveDTO.getLeaveTypeMasterId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leave Application found");

				apiLogInfo.setApiResponse("No Leave Application found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {

				list.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setLeaveId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setLeaveType(object[1] != null ? object[1].toString() : null);
					dto.setFromDate(object[2] != null ? object[2].toString() : null);
					dto.setToDate(object[3] != null ? object[3].toString() : null);
					dto.setNoOfDays(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
					dto.setStatus(object[5] != null ? object[5].toString() : null);
					dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
					dto.setCreatedOn(object[7] != null ? object[7].toString() : null);
					dto.setReason(object[8] != null ? object[8].toString() : null);
					dtoList.add(dto);
				});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse(dtoList.size() + " Leaves Found.");
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

	public ServiceResponse countAllMyTeamsPendingLeaveApplicationsByManagerId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/countAllMyTeamsPendingLeaveApplicationsByManagerId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("ManagerId : "+leaveDTO.getManagerId());
		
		try {
			Long applicationCount = employeeLeaveRepository
					.countAllMyTeamsPendingLeaveApplicationsByManagerId(leaveDTO.getManagerId());
			if (applicationCount == 0) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No leave applications found.");
				
				apiLogInfo.setApiResponse("No leave applications found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			} else {
				leaveDTO = new LeaveDTO();
				leaveDTO.setApplicationCount(applicationCount);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(leaveDTO);
				
				apiLogInfo.setApiResponse(applicationCount + " Leave Applications found.");
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

	public ServiceResponse countMyPendingLeaveApplicationsByLeaveType(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/countMyPendingLeaveApplicationsByLeaveType");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+leaveDTO.getEmpId());
		
		try {

			List<Object[]> list = employeeLeaveRepository
					.countMyPendingLeaveApplicationsByLeaveType(leaveDTO.getEmpId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			Optional.ofNullable(list).ifPresentOrElse((employeeLeavesList) -> {

				if (employeeLeavesList.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Application list is empty.Count is zero.");
					
					apiLogInfo.setApiResponse("Application list is empty.Count is zero.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					employeeLeavesList.forEach((object) -> {
						LeaveDTO dto = new LeaveDTO();
						dto.setApplicationCount(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setLeaveType(object[1] != null ? object[1].toString() : null);
						dto.setLeaveTypeCode(object[2] != null ? object[2].toString() : null);
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse(dtoList.size() +" Leave Applications found.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Application list is null");
				
				apiLogInfo.setApiResponse("Application list is null");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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

	public ServiceResponse countMyApprovedLeaveApplicationsByLeaveType(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/countMyApprovedLeaveApplicationsByLeaveType");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+leaveDTO.getEmpId());
		try {

			List<Object[]> list = employeeLeaveRepository
					.countMyApprovedLeaveApplicationsByLeaveType(leaveDTO.getEmpId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			Optional.ofNullable(list).ifPresentOrElse((employeeLeavesList) -> {

				if (employeeLeavesList.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Application list is empty.Count is zero");
					
					apiLogInfo.setApiResponse("Application list is empty.Count is zero");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				} else {
					employeeLeavesList.forEach((object) -> {
						LeaveDTO dto = new LeaveDTO();
						dto.setApplicationCount(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						dto.setLeaveType(object[1] != null ? object[1].toString() : null);
						dto.setLeaveTypeCode(object[2] != null ? object[2].toString() : null);
						dtoList.add(dto);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse(dtoList.size()+ " Leave Applications Found.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Application list is null");
				
				apiLogInfo.setApiResponse("Application list is null.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

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
	
	public ServiceResponse getAllMyTeamApplicationsByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllMyTeamApplicationsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+leaveDTO.getEmpId());
		
		try {
			List<Object[]> list = employeeLeaveRepository.getAllMyTeamApplicationsByEmpId(leaveDTO.getEmpId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leave Application found");

				apiLogInfo.setApiResponse("No Leave Application found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {

				list.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setLeaveId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setLeaveType(object[1] != null ? object[1].toString() : null);
					dto.setFromDate(object[2] != null ? object[2].toString() : null);
					dto.setToDate(object[3] != null ? object[3].toString() : null);
					dto.setNoOfDays(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
					dto.setStatus(object[5] != null ? object[5].toString() : null);
					dto.setEmployeeName(object[6] != null ? object[6].toString() : null);
					dto.setCreatedOn(object[7] != null ? object[7].toString() : null);
					dto.setReason(object[8] != null ? object[8].toString() : null);
					dto.setLeaveTypeMasterId(object[9] != null ? Short.parseShort(object[9].toString()) : null);
					dto.setEmpId(object[10] != null ? Long.parseLong(object[10].toString()) : null);
					dto.setRemark(object[11] != null ? object[11].toString() : null);
					dtoList.add(dto);
				});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse(dtoList.size()+ " Leave Applications Found.");
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
	
	public ServiceResponse bulkApproveLeaveRequest(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			for (LeaveDTO leave : leaveDTO.getBulkLeaveApprovedList()) {
				leave.setLeaveStatusId(leaveDTO.getLeaveStatusId());
				leave.setLeaveStatusUpdatedBy(leaveDTO.getLeaveStatusUpdatedBy());
				response = updateLeaveStatus(leave);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}

		return response;
	}

	public ServiceResponse bulkRejectLeaveRequest(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			

			for (LeaveDTO leave : leaveDTO.getBulkLeaveRejectList()) {
				leave.setLeaveStatusId(leaveDTO.getLeaveStatusId());
				leave.setLeaveStatusUpdatedBy(leaveDTO.getLeaveStatusUpdatedBy());
				leave.setRejectReason(leaveDTO.getRejectReason());
			
				response = updateLeaveStatus(leave);
				System.out.println(" leaveDTO.getReason() : "+leaveDTO.getRejectReason());
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}

		return response;
	}
	
	/*
	  revoke leave Application methods start --
	  */

	
	public ServiceResponse revokeApprovedLeaveApplication(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Revoke Leave");
		apiLogInfo.setApiUrl("/api/revokeApprovedLeaveApplication");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("LeaveId : "+leaveDTO.getLeaveId()+", LeaveTypeMasterId : "+ leaveDTO.getLeaveTypeMasterId());
		
		try {
			
			Optional<EmployeeLeave> leaveObj = employeeLeaveRepository.findById(leaveDTO.getLeaveId());
			
			if(leaveObj.isPresent()) {
				EmployeeLeave leaveToBeRevoked = leaveObj.get();
				
				LeaveRevokeApplication leaveRevokeObj = new LeaveRevokeApplication();
				
				leaveRevokeObj.setLeaveId(leaveToBeRevoked.getLeaveId());
				leaveRevokeObj.setLeaveRevokeStatusId((short) 1);
				leaveRevokeObj.setReason(leaveDTO.getRevokeReason());
				leaveRevokeObj.setEmpId(leaveToBeRevoked.getEmpId());
				leaveRevokeObj.getCommonProperty().setCreatedBy(leaveDTO.getCreatedBy().longValue());
				
				LeaveRevokeApplication leaveRevokeApplicationResponse = leaveRevokeApplicationRepository.save(leaveRevokeObj);
				
				if(leaveRevokeApplicationResponse != null) {
					
					leaveToBeRevoked.setLeaveStatusId((short) 4);
					employeeLeaveRepository.save(leaveToBeRevoked);
					
					// send mail on Leave Revoke
					
					Optional<Employee> employee = employeeRepository.findById(leaveToBeRevoked.getEmpId());
					
					if(!employee.isEmpty()) {
						Employee empObj = employee.get();
						Optional<Employee> empManager = employeeRepository.findById(empObj.getManagerId());
						if(!empManager.isEmpty()) {
							Employee empManagerObj = empManager.get();
							
							if(leaveToBeRevoked.getEmpId().equals(leaveDTO.getCreatedBy())) {
								// Revoked leave for self
										mailService.sendMailWithCC(empManagerObj.getEmail(), hrMailAddress +","+ empObj.getEmail(),
												"Revoke Request for Leave Application",
												"Dear "+ empManagerObj.getName() + ","
												+"<br>"+empObj.getName()+" has revoked its approved leave"
												+"<br><br> Leave Details :"
												+"<br>EmpId : A-" + empObj.getEmployeementId()
												+"<br> Name : " + empObj.getName()
												+"<br> From Date : " + leaveToBeRevoked.getFromDate() + "   To Date : " + leaveToBeRevoked.getToDate()
												+"<br> No. Of Days : " + leaveToBeRevoked.getNoOfDays()
												+"<br> Leave Type : " + leaveDTO.getLeaveType()
												+"<br> Revoke reason : " + leaveDTO.getRevokeReason());
							}else {
								// Revoked leave for Team
								Optional<Employee> createdByEmp = employeeRepository.findById(leaveDTO.getCreatedBy());
								if(!createdByEmp.isEmpty()) {
									Employee createdByObj = createdByEmp.get();
									
									mailService.sendMailWithCC(empManagerObj.getEmail(), hrMailAddress +","+ empObj.getEmail() +","+ createdByObj.getEmail(),
											"Revoke Request for Leave Application",
											"Dear "+ empManagerObj.getName() + ","
											+"<br> Leave has been revoked for "+ empObj.getName() +" by "+createdByObj.getName()
											+"<br><br> Leave Details :"
											+"<br> EmpId : A-" + empObj.getEmployeementId()
											+"<br> Name : " + empObj.getName()
											+"<br> From Date : " + leaveToBeRevoked.getFromDate() + "   To Date : " + leaveToBeRevoked.getToDate()
											+"<br> No. Of Days : " + leaveToBeRevoked.getNoOfDays()
											+"<br> Leave Type : " + leaveDTO.getLeaveType()
											+"<br> Revoke reason : " + leaveDTO.getRevokeReason());
								}
							}
						}
					}
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Leave Revoke Application Sumitted Successfully.");
					
					apiLogInfo.setApiResponse("Leave Revoke Application Sumitted Successfully.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Leave Revoke Application Failed.");
					
					apiLogInfo.setApiResponse("Leave Revoke Application Failed.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
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
	
	public ServiceResponse getRevokeLeaveApplicationByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getRevokeLeaveApplicationByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+leaveDTO.getEmpId());
		
		try {
			List<Object[]> list = leaveRevokeApplicationRepository.getRevokeLeaveApplicationByEmpId(leaveDTO.getEmpId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Revoke Leave Application found");

				apiLogInfo.setApiResponse("No Revoke Leave Application found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {

				list.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setLeaveRevokeId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setCreatedByName(object[1] != null ? object[1].toString() : null);
					dto.setCreatedOn(object[2] != null ? object[2].toString() : null);
					dto.setEmpId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
					dto.setEmployeeName(object[4] != null ? object[4].toString() : null);
					dto.setRevokeReason(object[5] != null ? object[5].toString() : null);
					dto.setFromDate(object[6] != null ? object[6].toString() : null);
					dto.setToDate(object[7] != null ? object[7].toString() : null);
					dto.setNoOfDays(object[8] != null ? Float.parseFloat(object[8].toString()) : null);
					dto.setStatus(object[9] != null ? object[9].toString() : null);
					dto.setLeaveType(object[10] != null ? object[10].toString() : null);
					dto.setRemark(object[11] != null ? object[11].toString() : null);
					
					dtoList.add(dto);
				});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse(dtoList.size() + " Applications found.");
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
	
	public ServiceResponse getAllMyTeamLeaveRevokeApplicationsByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllMyTeamLeaveRevokeApplicationsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+leaveDTO.getEmpId());
		
		try {
			List<Object[]> list = leaveRevokeApplicationRepository.getAllMyTeamLeaveRevokeApplicationsByEmpId(leaveDTO.getEmpId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Team Revoke Leave Application found");

				apiLogInfo.setApiResponse("No Team Revoke Leave Application found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {

				list.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setLeaveRevokeId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setCreatedByName(object[1] != null ? object[1].toString() : null);
					dto.setCreatedOn(object[2] != null ? object[2].toString() : null);
					dto.setEmpId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
					dto.setEmployeeName(object[4] != null ? object[4].toString() : null);
					dto.setRevokeReason(object[5] != null ? object[5].toString() : null);
					dto.setFromDate(object[6] != null ? object[6].toString() : null);
					dto.setToDate(object[7] != null ? object[7].toString() : null);
					dto.setNoOfDays(object[8] != null ? Float.parseFloat(object[8].toString()) : null);
					dto.setStatus(object[9] != null ? object[9].toString() : null);
					dto.setLeaveType(object[10] != null ? object[10].toString() : null);
					dto.setRemark(object[11] != null ? object[11].toString() : null);
					
					dtoList.add(dto);
				});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse(dtoList.size() + "Team Revoke Applications found.");
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

	public ServiceResponse getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+leaveDTO.getEmpId());
		try {
			
			List<Object[]> list = leaveRevokeApplicationRepository.
					getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId(leaveDTO.getEmpId());
			
			ArrayList<LeaveDTO> dtoList = new ArrayList<>();
			
			if(list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Revoke Leave Application found");

				apiLogInfo.setApiResponse("No Revoke Leave Application found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}else {
				
				list.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setLeaveRevokeId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setLeaveType(object[1] != null ? object[1].toString() : null);
					dto.setFromDate(object[2] != null ? object[2].toString() : null);
					dto.setToDate(object[3] != null ? object[3].toString() : null);
					dto.setNoOfDays(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
					dto.setStatus(object[5] != null ? object[5].toString() : null);
					dto.setCreatedByName(object[6] != null ? object[6].toString() : null);
					dto.setCreatedOn(object[7] != null ? object[7].toString() : null);
					dto.setRevokeReason(object[8] != null ? object[8].toString() : null);
					dto.setLeaveId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
					
					dtoList.add(dto);
				});

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse(dtoList.size() + "Revoke Applications found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		return response;
	}

	public ServiceResponse updateRevokeLeaveStatus(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("LeaveRevokeId : "+leaveDTO.getLeaveRevokeId()+", LeaveTypeMasterId : "+ leaveDTO.getLeaveTypeMasterId()+", LeaveStatusId : "+ leaveDTO.getLeaveStatusId()+", LeaveStatusUpdatedBy : "+ leaveDTO.getLeaveStatusUpdatedBy());
		
		try {
			
			Optional<LeaveRevokeApplication> leaveRevoke = leaveRevokeApplicationRepository.findById(leaveDTO.getLeaveRevokeId());
			Optional<EmployeeLeave> employeeLeave = employeeLeaveRepository.findById(leaveDTO.getLeaveId());

			if(!employeeLeave.isEmpty()) {
				EmployeeLeave leaveObj = employeeLeave.get();
				if(!leaveRevoke.isEmpty()) {
					LeaveRevokeApplication leaveRevokeObj = leaveRevoke.get();
					Optional<Employee> emp = employeeRepository.findById(leaveObj.getEmpId());
					
					// 1 : pending, 2 : approved, 3 : reject
					if(leaveDTO.getLeaveRevokeStatusId() == 2) {
					// change revoke leave application status
						leaveRevokeObj.setLeaveRevokeStatusId(leaveDTO.getLeaveRevokeStatusId());
						leaveRevokeObj.setLeaveRevokeStatusUpdatedBy(leaveDTO.getLeaveRevokeStatusUpdatedBy());
						leaveRevokeApplicationRepository.save(leaveRevokeObj);
						
					// change leave application status
						leaveObj.setLeaveStatusId((short) 5);
						leaveObj.setLeaveStatusUpdatedBy(leaveDTO.getLeaveRevokeStatusUpdatedBy());
						employeeLeaveRepository.save(leaveObj);
						
					// If employee in resignation and revoke its leave application, undo its notice period count
						Optional<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findById(leaveObj.getLeaveTypeMasterId());
						
						LeaveTypeMaster leaveTypeObj = leaveType.get();
						
						LocalDate createdOnDate = leaveObj.getCommonProperty().getCreatedOn().toLocalDateTime().toLocalDate();
						
						if (!emp.isEmpty()) {
							Employee empObj = emp.get();
							if (empObj.getEmploymentstatus().equals("Resigned") && createdOnDate.isAfter(empObj.getDateOfResign()) &&
									(leaveTypeObj.getLeaveTypeCode().equals("PL") || leaveTypeObj.getLeaveTypeCode().equals("CL"))) {
								empObj.setNoticePeriod((short) Math.floor(empObj.getNoticePeriod() - leaveObj.getNoOfDays()));
								employeeRepository.save(empObj);
							}
						}	
						
					//update leave balance bucket
						EmployeeLeavesMap employeeLeaveMapObj = employeeLeavesMapRepository
								.findByEmpIdAndLeaveTypeMasterId(leaveObj.getEmpId(), leaveObj.getLeaveTypeMasterId());
						
						if(employeeLeaveMapObj != null) {
							Float newBalance = employeeLeaveMapObj.getBalance() + leaveObj.getNoOfDays();
							employeeLeaveMapObj.setBalance(newBalance);
							
							EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(employeeLeaveMapObj);
							
							if(dbResponse != null) {
								LeaveBalanceLog log = new LeaveBalanceLog();

								log.setBalance(newBalance);
								log.setEmpId(leaveObj.getEmpId());
								log.setLeaveTypeMasterId(leaveObj.getLeaveTypeMasterId());
								log.setMessage(LeaveLogMessage.leaveRevoked.replace("0.0", leaveObj.getNoOfDays().toString()));
								log.setUpdateBalanceBy("+" + leaveObj.getNoOfDays());

								leaveBalanceLogRepository.save(log);
							}
						}
						
					//send Approval Mail
						if (!emp.isEmpty()) {
							Employee empObj = emp.get();
							Optional<Employee> manager = employeeRepository.findById(empObj.getManagerId());
							
							if(!manager.isEmpty()) {
								Employee managerObj = manager.get();
								mailService.sendMailWithCC(empObj.getEmail(), hrMailAddress +","+ managerObj.getEmail(),
										"Revoke Request for Leave Application Approved",
										"Dear "+ empObj.getName() + ","
										+"<br>Your Revoke Leave Application has been Approved by " + managerObj.getName()
										+"<br><br> Revoke Leave Application Details :"
										+"<br> From Date : " + leaveObj.getFromDate() + "   To Date : " + leaveObj.getToDate()
										+"<br> No. Of Days : " + leaveObj.getNoOfDays()
										+"<br> Leave Type : " + leaveDTO.getLeaveType()
										+"<br> Revoke reason : " + leaveRevokeObj.getReason());
							}
						}
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Revoke Leave Application Approved");
						
						apiLogInfo.setApiResponse("Revoke Leave Application Approved.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						
					}else if(leaveDTO.getLeaveRevokeStatusId() == 3){
						
					// change revoke leave application status
						leaveRevokeObj.setLeaveRevokeStatusId(leaveDTO.getLeaveRevokeStatusId());
						leaveRevokeObj.setLeaveRevokeStatusUpdatedBy(leaveDTO.getLeaveRevokeStatusUpdatedBy());
						leaveRevokeObj.setRemark(leaveDTO.getRejectReason());
						leaveRevokeApplicationRepository.save(leaveRevokeObj);
						
					// change leave application status
						leaveObj.setLeaveStatusId((short) 2);
						leaveObj.setLeaveStatusUpdatedBy(leaveDTO.getLeaveRevokeStatusUpdatedBy());
						employeeLeaveRepository.save(leaveObj);
						
					//send Reject Mail
						if (!emp.isEmpty()) {
							Employee empObj = emp.get();
							Optional<Employee> manager = employeeRepository.findById(empObj.getManagerId());
							
							if(!manager.isEmpty()) {
								Employee managerObj = manager.get();
								mailService.sendMailWithCC(empObj.getEmail(), hrMailAddress +","+ managerObj.getEmail(),
										"Revoke Request for Leave Application Rejected",
										"Dear "+ empObj.getName() + ","
										+"<br>Your Revoke Leave Application has been Rejected by " + managerObj.getName()
										+"<br><br> Revoke Leave Application Details :"
										+"<br> From Date : " + leaveObj.getFromDate() + "   To Date : " + leaveObj.getToDate()
										+"<br> No. Of Days : " + leaveObj.getNoOfDays()
										+"<br> Leave Type : " + leaveDTO.getLeaveType()
										+"<br> Revoke reason : " + leaveRevokeObj.getReason());
							}
						}
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Revoke Leave Application Rejected.");
						
						apiLogInfo.setApiResponse("Revoke Leave Application Rejected.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Leave Revoke Application not found.");
					
					apiLogInfo.setApiResponse("Leave Revoke Application not found.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leave Application not found.");
				
				apiLogInfo.setApiResponse("Leave Application not found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		return response;
	}
}
