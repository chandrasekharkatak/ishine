package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.CompOffLeave;
import com.apmosys.employeeportal.model.CompOffMaster;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.repository.CompOffLeaveRepository;
import com.apmosys.employeeportal.repository.CompOffMasterRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.utility.LeaveLogMessage;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class CompOffLeaveService {

	private static final Long Long = null;

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
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Value("${hr.mail}")
	private String hrMailAddress;
	
	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;

	public ServiceResponse getAllCompOffReasons() {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Apply Comp off req");
		apiLogInfo.setApiUrl("/api/getAllCompOffReasons");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder=new StringBuilder();
		logBuilder.append("compOffLeaveRepository size : "+compOffLeaveRepository.findAll().size());		
		try {
			List<CompOffMaster> compOffReasonsList = compOffMasterRepository.findAll();
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			if (compOffReasonsList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Comp off reasons list is empty.");
				apiLogInfo.setApiResponse("Comp off reasons list is empty.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {
				compOffReasonsList.forEach((reason) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setCompOffId(reason.getCompOffId());
					dto.setCompOffReasons(reason.getCompOffReasons());
					dtoList.add(dto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("List fetched of size : "+dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse applyForCompOff(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Apply Comp off req");
		apiLogInfo.setApiUrl("/api/applyForCompOff");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + leaveDTO.getEmpId()+ "managerId : " +leaveDTO.getManagerId()+ "hodId : " +leaveDTO.getHodId()+ "createdBy : " +leaveDTO.getCreatedBy()+", ompOffReasons : "+leaveDTO.getCompOffReasons());
		try {

			CompOffLeave leave = new CompOffLeave();

			leave.setDescription(leaveDTO.getDescription());
			leave.setEmpId(leaveDTO.getEmpId());
			

				leave.setManagerId(leaveDTO.getManagerId());
			
//			leave.setManagerId(leaveDTO.getHodId()); // Here Approvals will go to Department Head for CompOff Application
			// 1= pending , 2= approved , 3 = rejected
			leave.setLeaveStatusId((short) 1);
			// 1 = CL , 2 = PL , 3 = ML , 4 = PTL , 5 = CO
//	        leave.setLeaveTypeMasterId((short)5);
			leave.setLeaveCode("CO");
			leave.setReason(leaveDTO.getReasonId());
			leave.setCreatedBy(leaveDTO.getCreatedBy());
			leave.setFromDate(stringToDateTimeParser.getDate(leaveDTO.getFromDate(), "yyyy-MM-dd"));
			leave.setToDate(stringToDateTimeParser.getDate(leaveDTO.getFromDate(), "yyyy-MM-dd"));
			leave.setNoOfDays(1F);
			leave.setCompOffStatus("Pending");

			CompOffLeave leaveApplied = compOffLeaveRepository.save(leave);

			if (leaveApplied != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Compoff Request applied.");
				
				apiLogInfo.setApiResponse("Compoff Request applied.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
				CompOffMaster compOffObject = compOffMasterRepository.findByCompOffId(leaveDTO.getReasonId().shortValue());
				
				mailService.sendMailWithCC(leaveDTO.getEmail(), leaveDTO.getHodEmail()+","+leaveDTO.getManagerEmail()+","+hrMailAddress, "Regarding Comp-Off Request", 
						"Dear "+ leaveDTO.getHodName()+","+
				"<br> "
				+" &nbsp;"+" &nbsp;"+" "+"Comp-Off Request has been applied by "+ leaveDTO.getEmployeeName() +" for 1" +" day(s)"+", Please take necessary action."+
				"<br>"+"<br>"+"<b>"+"Comp-Off Details :"+"<b>"+
				"<br>"+
				"EmpID :"+"A- "+ leaveDTO.getEmployeementId()+
				"<br>"+
				"Name :"+" "+ leaveDTO.getEmployeeName()+
				"<br>"+
				" Date "+" "+ leaveDTO.getFromDate() +
				"<br>"+
				"No. Of Days :"+" 1 "+"day(s)"
				+"<br>"+
				"comp-Off Description :"+" "+leaveDTO.getDescription() + "."
				+"<br>"+
				"comp-Off Reason : " + compOffObject.getCompOffReasons());
				
				
				

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
					dto.setEmail(object[10] != null ? object[10].toString() : null) ;
					dto.setEmployeementId(object[11] != null ? Long.parseLong(object[11].toString()) : null);
					dto.setManagerId(object[12] != null ? Integer.parseInt(object[12].toString()) : null);
					dto.setManagerEmail(object[13] != null ? object[13].toString() : null);
					
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
			List<Object[]> findHod = employeeRepository.findHodByEmpId(leaveDTO.getEmpId());
			LeaveTypeMaster leavetypeObj = leaveTypeMasterRepository.findByLeaveTypeCode("CO");
			System.out.println("  leave type" + " leavetypeObj " +leavetypeObj);
			System.out.println(" findHod :  "+findHod.get(0));
			
			if (leaveObject.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No compoff request(s) found.");
			} else {
				findHod.forEach((object)->{
					CompOffLeave compOffLeave = leaveObject.get();
					
				
					compOffLeave.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());

					// 1 = pending , 2 = Approved , 3= Rejected
					compOffLeave.setLeaveStatusId(leaveDTO.getLeaveStatusId());

					if (leaveDTO.getLeaveStatusId() == 2) {
						// 1 = CO
						EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository
								.findByEmpIdAndLeaveTypeMasterId(leaveDTO.getEmpId(), leavetypeObj.getLeaveTypeMasterId());

						// added by anurag comp off two step verification   
						compOffLeave.setManagerId(object[1] != null ? Integer.parseInt(object[1].toString()) : null);
						if(compOffLeave.getLeaveStatusUpdatedBy() != null) {
							compOffLeave.setLeaveStatusUpdatedBy(leaveDTO.getLeaveStatusUpdatedBy());
							compOffLeave.setLeaveStatusId((short) 2);
						}
						
						else {
							compOffLeave.setLeaveStatusUpdatedBy(leaveDTO.getLeaveStatusUpdatedBy());
							compOffLeave.setLeaveStatusId((short) 1);
						}
						System.err.println(" Anurag find hod    :   "+compOffLeave.getManagerId());
						employeeLeavesMap.setBalance(employeeLeavesMap.getBalance() + compOffLeave.getNoOfDays());
						employeeLeavesMapRepository.save(employeeLeavesMap);

						LeaveBalanceLog log = new LeaveBalanceLog();
						log.setBalance(employeeLeavesMap.getBalance());
						log.setEmpId(leaveDTO.getEmpId());
						log.setLeaveTypeMasterId(leavetypeObj.getLeaveTypeMasterId());
						log.setMessage(
								LeaveLogMessage.compOffAddLeave.replace("0.0", compOffLeave.getNoOfDays().toString()));
						log.setUpdateBalanceBy("+" + compOffLeave.getNoOfDays());
						LeaveBalanceLog dbResponse = leaveBalanceLogRepository.save(log);
						
						if(dbResponse != null) {
							compOffLeave.setCompOffStatus("Pending");
							response.setServiceResponse("Compoff request application approved.");
						}
					} else if (leaveDTO.getLeaveStatusId() == 3) {
						System.err.println(" Anurag find hod    :   "+compOffLeave.getManagerId());
						compOffLeave.setCompOffStatus("Pending");
						compOffLeave.setRejectCompOffReason(leaveDTO.getRejectCompOffReason());					
						response.setServiceResponse("CompOff request application rejected.");
					}

					CompOffLeave compOffUpdated = compOffLeaveRepository.save(compOffLeave);

					if (compOffUpdated != null) {
						if(leaveDTO.getLeaveStatusId() == 2) {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Compoff Request Approved");
							
							try {
								mailService.sendMailWithCC(leaveDTO.getEmail(), leaveDTO.getHodEmail()+","+leaveDTO.getManagerEmail()+","+hrMailAddress, "Regarding Compensatory off Request Approval", 
										"Dear "+ leaveDTO.getEmployeeName()+","+
								"<br> "
								+" &nbsp;"+" &nbsp;"+" "+"Your Compensatory off application has been approved by "+ leaveDTO.getHodName() +"."+
								"<br>"+"<br>"+"<b>"+"Comp-Off Details :"+"<b>"+
								"<br>"+
								"EmpID :"+"A- "+ leaveDTO.getEmployeementId()+
								"<br>"+
								"Name :"+" "+ leaveDTO.getEmployeeName()+
								"<br>"+
								" Date "+" "+ leaveDTO.getFromDate() +
								"<br>"+
								"No. Of Days :"+" 1 "+"day(s)"
								+"<br>"+
								"Comp-off Description :"+" "+leaveDTO.getDescription()+ "."
								+"<br>"+
								"Comp-off reason :"+" "+leaveDTO.getCompOffReasons()+ ".");
							} catch (AddressException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (MessagingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}else if(leaveDTO.getLeaveStatusId() == 3){
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Compoff Request Rejected");
							
							try {
								mailService.sendMailWithCC(leaveDTO.getEmail(), leaveDTO.getHodEmail()+","+leaveDTO.getManagerEmail()+","+hrMailAddress, "Regarding Compensatory off Request Rejection", 
										"Dear "+ leaveDTO.getEmployeeName()+","+
								"<br> "
								+" &nbsp;"+" &nbsp;"+" "+"Your Compensatory off application has been rejected by "+ leaveDTO.getHodName() +"."+
								"<br>"+"<br>"+"<b>"+"Comp-Off Details :"+"<b>"+
								"<br>"+
								"EmpID :"+"A- "+ leaveDTO.getEmployeementId()+
								"<br>"+
								"Name :"+" "+ leaveDTO.getEmployeeName()+
								"<br>"+
								" Date "+" "+ leaveDTO.getFromDate() +
								"<br>"+
								"No. Of Days :"+" 1 "+"day(s)"
								+"<br>"+
								"Comp-off Description :"+" "+leaveDTO.getDescription()+ "."
								+"<br>"+
								"Comp-off reason :"+" "+leaveDTO.getCompOffReasons()+".");
							} catch (MessagingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
//						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//						response.setServiceResponse("Compoff leave status updated");

						
						apiLogInfo.setApiResponse("Compoff leave status updated");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

					} else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Compoff leave status updation failed.");
						
						apiLogInfo.setApiResponse("Compoff leave status updation failed.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
						
					});
			
				
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
	
	public ServiceResponse updateCompOff(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Update CompOff");
		apiLogInfo.setApiUrl("/api/updateCompOff");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " + leaveDTO.getEmpId()+ "CompOffLeaveId : " +leaveDTO.getCompOffLeaveId());
		try {

			Optional<CompOffLeave> leaveObject = compOffLeaveRepository.findById(leaveDTO.getCompOffLeaveId());
			
			if (leaveObject.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("compoff request Not found.");
			} else {
				CompOffLeave leave = leaveObject.get();

				leave.setReason(leaveDTO.getReasonId());
				leave.setFromDate(stringToDateTimeParser.getDate(leaveDTO.getFromDate(), "yyyy-MM-dd"));
				leave.setToDate(stringToDateTimeParser.getDate(leaveDTO.getFromDate(), "yyyy-MM-dd"));
				leave.setNoOfDays(1F);
				leave.setDescription(leaveDTO.getDescription());
				leave.setCompOffStatus("Pending");
				
				leave.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				leave.setUpdatedBy(Long.parseLong(leaveDTO.getUpdatedBy().toString()));

				CompOffLeave leaveUpdated = compOffLeaveRepository.save(leave);

				if (leaveUpdated != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Compoff Request Updated.");
					
					apiLogInfo.setApiResponse("Compoff Request Updated.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Compoff Request updation failed.");
					
					apiLogInfo.setApiResponse("Compoff Request updation failed.");			
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
	public ServiceResponse deleteCompOff(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Delete CompOff");
		apiLogInfo.setApiUrl("/api/deleteCompOff");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("CompOffLeaveId : " +leaveDTO.getCompOffLeaveId()+", NoOfDays : "+ leaveDTO.getNoOfDays());
		
		try {

			Optional<CompOffLeave> leaveObject = compOffLeaveRepository.findById(leaveDTO.getCompOffLeaveId());

			if (leaveObject.isPresent()) {

				CompOffLeave leave = leaveObject.get();

				compOffLeaveRepository.deleteById(leave.getCompOffLeaveId());

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("CompOff Request Deleted.");
				
				apiLogInfo.setApiResponse("CompOff Request Deleted.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
				mailService.sendMailWithCC(leaveDTO.getHodEmail(), leaveDTO.getManagerEmail()+","+leaveDTO.getEmail()+","+hrMailAddress, "Regarding CompOff Request Deletion", 
						"Dear "+ leaveDTO.getHodName()+","+
				"<br> "
				+" &nbsp;"+" &nbsp;"+" "+"Pending CompOff application has been deleted by "+ leaveDTO.getEmployeeName() +"."+
				"<br>"+"<br>"+"<b>"+"CompOff Details :"+"<b>"+
				"<br>"+
				"EmpID :"+"A- "+ leaveDTO.getEmployeementId()+
				"<br>"+
				"Name :"+" "+ leaveDTO.getEmployeeName()+
				"<br>"+
				" Date "+" "+ leaveDTO.getFromDate() +
				"<br>"+
				"No. Of Days :"+" 1 "+"day(s)"+
				"<br>"+
				"CompOff Description :"+" "+leaveDTO.getDescription()+".");
				
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("CompOff Request Not Found.");
				
				apiLogInfo.setApiResponse("CompOff Request Not Found.");			
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

	public ServiceResponse getCompOffBalanceDetailsByEmpIdAndFromDate(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getCompOffBalanceDetailsByEmpIdAndFromDate");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : " +leaveDTO.getEmpId() + ", From Date : "+ leaveDTO.getFromDate());
		
		try {

			List<Object[]> compOffList = compOffLeaveRepository.getCompOffBalanceDetailsByEmpIdAndFromDate(leaveDTO.getEmpId(),leaveDTO.getFromDate());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

			if (compOffList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse("No compoff request(s) found for employee");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				
			} else {
				compOffList.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();

					dto.setCompOffLeaveId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setCompOffStatus(object[1] != null ? object[1].toString() : null);
					dto.setEmpId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
					dto.setLeaveStatusId(object[3] != null ? Short.parseShort(object[3].toString()) : null);
					dto.setFromDate(object[4] != null ? object[4].toString() : null);
					dto.setNoOfDays(object[5] != null ? Float.parseFloat(object[5].toString()) : null);
					
					dtoList.add(dto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				apiLogInfo.setApiResponse("Found " + dtoList.size() +" CompOff Available.");			

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
	
	
	
	public ServiceResponse getCompOffBalanceMigratedFromOldLeavePortal() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getCompOffBalanceMigratedFromOldLeavePortal");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			
			List<Employee> employeeList = employeeRepository.findAll();
			List<LeaveDTO> dtoList = new ArrayList<>();
			
			if(!employeeList.isEmpty()) {
				employeeList.forEach((object) -> {
					List<CompOffLeave> compOffList = compOffLeaveRepository.findByEmpId(object.getEmpId());
					Float compOffAppliedOnIShine = 0f;
					logBuilder.append("empId : "+compOffLeaveRepository.findByEmpId(object.getEmpId()));
					
					if(!compOffList.isEmpty()) {
						for(CompOffLeave application: compOffList) {
							compOffAppliedOnIShine = compOffAppliedOnIShine + application.getNoOfDays();
							logBuilder.append("compOffAppliedOnIShine : "+compOffAppliedOnIShine);
						}
					}
					
					EmployeeLeavesMap empLeave = employeeLeavesMapRepository.findByEmpIdAndLeaveTypeMasterId(object.getEmpId(), (short)4);
					
					if(empLeave != null) {
						Float balanceToBeReconsile = empLeave.getBalance() - compOffAppliedOnIShine;
						
						LeaveDTO dto = new LeaveDTO();
						
						dto.setEmployeementId(object.getEmployeementId());
						dto.setName(object.getName());
						dto.setCompOffAppliedOnIshine(compOffAppliedOnIShine);
						dto.setBalanceFromOldPortal(balanceToBeReconsile); //Balance to be reconsiled
						dto.setBalance(empLeave.getBalance());
						
						dtoList.add(dto);
					}
				});
			}
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(dtoList);
			apiLogInfo.setApiResponse("dtoList fetched of size : "+dtoList);
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse setCompOffStatusAndLeaveId() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/setCompOffStatusAndLeaveId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		
		try {
			//set correct compOff balance 
			
//			List<Employee> employeeList = employeeRepository.findAll();
//			
//			if(!employeeList.isEmpty()) {
//				employeeList.forEach((emp) -> {
//					List<LeaveBalanceLog> leaveLogs = leaveBalanceLogRepository.findByEmpIdAndLeaveTypeMasterId(emp.getEmpId(), (short) 4); 
//					
//					if(!leaveLogs.isEmpty()) {
//						
//					}
//				});
//			}
			
			//Add CompOff Status & leaveID
			List<CompOffLeave> compOffApplication = compOffLeaveRepository.findAll();
			
			if(!compOffApplication.isEmpty()) {
				compOffApplication.forEach((object) -> {
					
					LocalDate dateToday = LocalDate.now().minusDays(30);
					if(object.getFromDate().isBefore(dateToday)) {
						object.setCompOffStatus("Expired");
						
						compOffLeaveRepository.save(object);
					}else {
						LeaveBalanceLog compOffLogs = leaveBalanceLogRepository.findCompOffLogByEmpId(object.getEmpId(), (short)4, "Employee requested for leave", object.getUpdatedOn());
						
						if(compOffLogs != null) {
								LocalDate createdOn = compOffLogs.getCreatedOn().toLocalDateTime().toLocalDate();
								EmployeeLeave compOffLeave = employeeLeaveRepository.findLeaveApplicationByCreatedOnDate(object.getEmpId(), (short)4, createdOn);
								
								if(compOffLeave != null) {
									//1 : pending, 2: Approved, 3 : Rejected
									if(compOffLeave.getLeaveStatusId() == 1) {
										object.setCompOffStatus("Pending For Approval");										
									}else if(compOffLeave.getLeaveStatusId() == 2) {
										object.setCompOffStatus("Availed");
									}else {
										object.setCompOffStatus("Pending");
									}
									object.setLeaveId(compOffLeave.getLeaveId());
									logBuilder.append("leaveId : "+compOffLeave.getLeaveId());								
									compOffLeaveRepository.save(object);
								}
						}else {
							object.setCompOffStatus("Pending");
							compOffLeaveRepository.save(object);
						}
						logBuilder.append(object.getCompOffStatus());
					}
				});
			}
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse convertSingleCompOffApplicationToken() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/convertSingleCompOffApplicationToken");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		
		try {
			
			List<CompOffLeave> allCompOffApplication = compOffLeaveRepository.findAll();
			
			if(!allCompOffApplication.isEmpty()) {
				allCompOffApplication.forEach((object) -> {
					
					if(!object.getCompOffStatus().equals("Expired")) {
						if(object.getNoOfDays() > 1f) {
							Float tempNoOfDays = object.getNoOfDays();
							LocalDate fromDate = object.getFromDate().plusDays(1);
							while(tempNoOfDays != 1f) {
								CompOffLeave leave = new CompOffLeave();

								leave.setEmpId(object.getEmpId());
								leave.setManagerId(object.getManagerId());
								leave.setLeaveStatusId(object.getLeaveStatusId());
								leave.setLeaveCode("CO");
								leave.setReason(object.getReason());
								leave.setCreatedBy(object.getCreatedBy());
								leave.setFromDate(fromDate);
								leave.setToDate(fromDate);
								leave.setNoOfDays(1F);
								leave.setDescription(object.getDescription());
								leave.setCreatedOn(Timestamp.valueOf(fromDate.atTime(00,00,00)));	
								
								compOffLeaveRepository.save(leave);
								
								tempNoOfDays = tempNoOfDays - 1;
								fromDate = fromDate.plusDays(1);
							}
							
							object.setToDate(object.getFromDate());
							object.setNoOfDays(1F);
							
							compOffLeaveRepository.save(object);
						}
					}
				});
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	public ServiceResponse lapseAndReconcileCompOffBalance() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/lapseAndReconcileCompOffBalance");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();

		List<EmployeeDTO> reconcileResponses = new ArrayList<EmployeeDTO>();
		try {
			
			List<Employee> activeEmployeeList = employeeRepository.findByEmploymentstatusIsNot("InActive");
			logBuilder.append("employement status inactive size : "+employeeRepository.findByEmploymentstatusIsNot("InActive").size());
			
			if(!activeEmployeeList.isEmpty()) {
				activeEmployeeList.forEach(employee -> {
					EmployeeDTO employeeReconcileResponse  = new EmployeeDTO();
					
					
					System.out.println("=================== Start : "+ employee.getEmployeementId() + " - "+ employee.getName() + " ===================");
					
					/* Previous Pending CompOffs to Expire */
					List<CompOffLeave> allPreviousPendingCompOffApplication = compOffLeaveRepository.findByEmpIdAndFromDateLessThanEqualAndCompOffStatusIs(employee.getEmpId(), stringToDateTimeParser.getDate("2023-04-09", "yyyy-MM-dd"), "Pending");
					
					if(!allPreviousPendingCompOffApplication.isEmpty()) {
						int expiredCompOffs = 0;
						for(CompOffLeave leave : allPreviousPendingCompOffApplication) {
							leave.setCompOffStatus("Expired");
							
							CompOffLeave resp = compOffLeaveRepository.save(leave);
							if(resp != null) {
								expiredCompOffs++;
							}
						}
						
						employeeReconcileResponse.setPreviouseCompOffExpiredCount(expiredCompOffs);
					}else {
						employeeReconcileResponse.setPreviouseCompOffExpiredCount(0);
					}
					
					/* Last Month CompOffs */
					List<CompOffLeave> allCompOffApplication = compOffLeaveRepository.findByEmpIdAndFromDateGreaterThan(employee.getEmpId(), stringToDateTimeParser.getDate("2023-04-09", "yyyy-MM-dd"));
					
					/* Employee ComOff Leave Balance */
					EmployeeLeavesMap compOffLeaveBal = employeeLeavesMapRepository.findByEmpIdAndLeaveTypeMasterId(employee.getEmpId(), (short) 4);
					
					if(!allCompOffApplication.isEmpty()) {
						System.out.println("Current CompOff Balance : "+  compOffLeaveBal.getBalance());
						System.out.println("New CompOff Balance / CompOff in Last Month before 2023-04-09 : "+  allCompOffApplication.size());
						
						float newBalance = 0;
						
						for(CompOffLeave compOff:allCompOffApplication) {
							
							compOff.setCompOffStatus("Pending");
							compOff.setLeaveId(null);
							
							CompOffLeave resp = compOffLeaveRepository.save(compOff);
							if(resp != null) {
								newBalance++;
							}
						}
						
						compOffLeaveBal.setBalance(newBalance); 
						employeeLeavesMapRepository.save(compOffLeaveBal);
						
						employeeReconcileResponse.setNewBalance(newBalance);
						employeeReconcileResponse.setOldBalance(compOffLeaveBal.getBalance());
					}else {
						System.out.println("No CompOffs Applied in last month setting updated CompOff Balance to zero.");
						
						compOffLeaveBal.setBalance(0F); 
						employeeLeavesMapRepository.save(compOffLeaveBal);
						
						employeeReconcileResponse.setNewBalance(0F);
						employeeReconcileResponse.setOldBalance(compOffLeaveBal.getBalance());
						
					}
					
					System.out.println("=================== End : "+ employee.getEmployeementId() + " - "+ employee.getName() + " ===================");
					reconcileResponses.add(employeeReconcileResponse);
				});
				
				response.setServiceResponse(reconcileResponses);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse bulkCompOffRejectRequest(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			

			for (LeaveDTO leave : leaveDTO.getBulkLeaveRejectList()) {
				leave.setLeaveStatusId(leaveDTO.getLeaveStatusId());
				leave.setLeaveStatusUpdatedBy(leaveDTO.getLeaveStatusUpdatedBy());
				leave.setRejectCompOffReason(leaveDTO.getRejectCompOffReason());
			
				response = updateCompOffById(leave);
				System.out.println(" leaveDTO.getReason() : "+leaveDTO.getRejectCompOffReason());
				
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			    DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			    
				
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}

		return response;
	}

	public ServiceResponse bulkCompOffApproveRequest(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			

			for (LeaveDTO leave : leaveDTO.getBulkLeaveApprovedList()) {
				leave.setLeaveStatusId(leaveDTO.getLeaveStatusId());
				leave.setLeaveStatusUpdatedBy(leaveDTO.getLeaveStatusUpdatedBy());
				leave.setRejectCompOffReason(leaveDTO.getRejectCompOffReason());
			
				response = updateCompOffById(leave);
				System.out.println(" leaveDTO.getReason() : "+leaveDTO.getRejectCompOffReason());
				
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			    DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			    
				
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}

		return response;
	}
	


//	@Scheduled(cron = "0 */6 * * * ?") // Run every 5 minutes @Scheduled(cron="${compOff_TAT}")
	@Scheduled(cron="${compOff_TAT}")
	public void checkCompOffTAT() {
	    List<Employee> findAllEmployee = employeeRepository.findAll();

	    findAllEmployee.forEach((employee) -> {
	        Long managerId = employee.getManagerId();
	    	  
	        short leaveStatusId = 1;
	        Employee manager = employeeRepository.findByEmpId(managerId);
//	        System.err.println(" Manager   ::  "+manager);
	        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
	        Timestamp sevenDaysAgoTimestamp = Timestamp.valueOf(sevenDaysAgo);


	        List<CompOffLeave> overdueCompOffs = compOffLeaveRepository.findByLeaveStatusIdAndManagerIdAndCreatedOnBefore(
	                leaveStatusId, managerId, sevenDaysAgoTimestamp);

	        for (CompOffLeave compOffLeave : overdueCompOffs) {
	            Employee requestor = employeeRepository.findByEmpId(compOffLeave.getEmpId());
	            System.err.println(" compOffLeave leave Id "+compOffLeave.getCompOffLeaveId());
	            Long leaveId = compOffLeave.getCompOffLeaveId();
	            String subject = "Regarding Comp Off Request pending";
	            String message = "Dear " + manager.getName() + ",\n\n"
	                    + "Kindly Approve Pending Comp off request. It has already breached the TAT."+" Comp off leave Id is "+Long.toString(leaveId);

	            try {
	                mailService.sendMailWithCC(manager.getEmail(), requestor.getEmail(), subject, message);
//	            	mailService.sendMailWithCC("anurag.chaturvedi@apmosys.com", "anurag.chaturvedi@apmosys.com", subject, message);
	            } catch (MessagingException e) {
	                // Log or handle the exception
	                e.printStackTrace();
	            }
	        }
	    });
	}

}
