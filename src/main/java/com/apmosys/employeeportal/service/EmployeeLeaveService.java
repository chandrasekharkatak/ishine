package com.apmosys.employeeportal.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import com.apmosys.employeeportal.model.*;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.HolidayDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LeaveExcludeIncludeDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.exception.UnauthorizedAccessException;
import com.apmosys.employeeportal.repository.CompOffLeaveRepository;
import com.apmosys.employeeportal.repository.CompOffMasterRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.EmployeeexcludedFromLeaveRepository;
import com.apmosys.employeeportal.repository.HolidayRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.repository.LeavePolicyMasterRepository;
import com.apmosys.employeeportal.repository.LeaveRevokeApplicationRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.repository.PIPRepository;
import com.apmosys.employeeportal.repository.TimesheetActivityMapRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.EmployeeHirarchyCache;
import com.apmosys.employeeportal.utility.LeaveLogMessage;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;


@Service
public class EmployeeLeaveService {
	
//	@Autowired
//	CompOffLeave compOffLeave;

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
	CronJobService cronJobService;
	
	@Autowired
	LeaveTypeMasterRepository leaveTypeMasterRepository;
	
	@Autowired
	LeaveRevokeApplicationRepository leaveRevokeApplicationRepository;
	
	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;
	
	@Autowired
	private MailService mailService;
	
	@Value("${hr.mail}")
	private String hrMailAddress;
	
	@Value("${CLleave.maxDays}")
	private Long clLeaveDays;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HolidayService holidayService;

	@Autowired
	private HttpServletRequest httpRequest;
	
	@Autowired
	TimesheetsRepository timesheetsRepository;
	
	@Autowired
	TimesheetActivityMapRepository timesheetActivityRepository;
	
	@Autowired
	HolidayRepository holidayRepository;
	
	@Autowired
	CompOffLeaveRepository compOffLeaveRepository;
	
	@Autowired
	LeavePolicyMasterRepository leavePolicyMasterRepository;
	
	@Autowired
	CompOffLeaveService compOffLeaveService;
	
	@Autowired
	PIPRepository pipRepository;

	@Autowired
	EmployeeHirarchyCache empCache ;
	
	@Autowired
	EmployeeexcludedFromLeaveRepository employeeexcludedFromLeaveRepository;
	
	@Value("${reminder_Mail_Date}")
	private Long reminderMailDays;

	// added by anurag
	private Object hodEmail;
		
	@PersistenceContext
	private EntityManager entityManager;
	
//	@Transactional
//	public ServiceResponse applyLeave(LeaveDTO leaveDTO) {
//		ServiceResponse response = new ServiceResponse();
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("Apply Leave");
//		apiLogInfo.setApiUrl("/api/applyLeave");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("empId : "+leaveDTO.getEmpId()+ ", leaveTypeMasterId : "+ leaveDTO.getLeaveTypeMasterId()+", leaveTypeCode : "+ leaveDTO.getLeaveTypeCode() +", noOfDays : "+ leaveDTO.getNoOfDays());
//		System.out.println(leaveDTO);
//		try {
//
////			LeaveTypeMaster leaveTypeMasterObj = leaveTypeMasterRepository.findByLeaveTypeCode(leaveDTO.getLeaveTypeCode());	
//			
//			// added by anurag
////			EmployeeLeave recentLeaves = employeeLeaveRepository.findRecentLeavesByEmpId(leaveDTO.getEmpId()).get(0);
////
////System.out.println(" recentLeaves    "+recentLeaves);
////	                LocalDate newLeaveFromDate = LocalDate.parse(leaveDTO.getFromDate());
////	                LocalDate recentLeaveToDate = recentLeaves.getToDate();
////	                if (newLeaveFromDate.isEqual(recentLeaveToDate.plusDays(1))) {
////	                	
////	                    if (!recentLeaves.getLeaveTypeMasterId().equals(leaveDTO.getLeaveTypeMasterId())) {
////	                        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
////	                        response.setServiceResponse("Two different types of leaves are not allowed on consecutive days. Please ensure that the same type of leave is applied for consecutive days.");
////	                        return response;
////	                    }
////	                } else {
////	                	System.err.println("recentLeaveToDate    ::  "+recentLeaveToDate);
////	                	boolean isWeekOff = this.isWeekOffFind(recentLeaveToDate.plusDays(1),newLeaveFromDate.minusDays(1), leaveDTO.getState());
////	                	
////	                	if(isWeekOff) {
////	                		EmployeeLeave findLeaveOnToDate = employeeLeaveRepository.findEmployeeLeaveByToDate(newLeaveFromDate.minusDays(1),leaveDTO.getEmpId());
////	                		System.err.println("findLeaveOnToDate   ::  "+findLeaveOnToDate);
////	                		if(findLeaveOnToDate != null) {
////	                			if((!leaveDTO.getLeaveTypeMasterId().equals(recentLeaves.getLeaveTypeMasterId()))) {
////		                			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
////			                        response.setServiceResponse("Two different types of leaves are not allowed on consecutive days.Week Offs also consider your last leave To date");
////			                        return response;
////		                		}
////	                		}else {
////	                			List<Holiday> findWeekOffAndFestival = holidayRepository.findWeekOffCountByFromAndToDate(newLeaveFromDate.minusDays(1), leaveDTO.getState());
////	                			EmployeeLeave findPreviousLeaveByFromDate = employeeLeaveRepository.findLeaveByFromDate(leaveDTO.getFromDate(), leaveDTO.getEmpId()).get(0);
////	                			System.err.println("findPreviousLeaveByFromDate    ::   "+findPreviousLeaveByFromDate);
////	                			System.err.println(" Anurag call else part "+findWeekOffAndFestival);
////	                			if(!findWeekOffAndFestival.isEmpty()) {
////	                				if(!leaveDTO.getLeaveTypeMasterId().equals(findPreviousLeaveByFromDate.getLeaveTypeMasterId())) {
////	                					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
////				                        response.setServiceResponse("Two different types of leaves are not allowed on consecutive days.Week Offs also consider your last leave To date");
////				                        return response;
////	                				}
////	                				}
////	                			}
////	                		
////	                	}
////	                }
////			Optional<List<EmployeeLeave>> recentLeavesOptional = Optional.ofNullable(employeeLeaveRepository.findRecentLeavesByEmpId(leaveDTO.getEmpId()));
////			if (recentLeavesOptional.isPresent() && !recentLeavesOptional.get().isEmpty()) {
////			    EmployeeLeave recentLeaves = recentLeavesOptional.get().get(0);
////			    System.out.println(" recentLeaves    " + recentLeaves);
////			    LocalDate newLeaveFromDate = LocalDate.parse(leaveDTO.getFromDate());
////			    LocalDate recentLeaveToDate = recentLeaves.getToDate();
////			    
////			    if (newLeaveFromDate.isEqual(recentLeaveToDate.plusDays(1))) {
////			        if (!recentLeaves.getLeaveTypeMasterId().equals(leaveDTO.getLeaveTypeMasterId())) {
////			            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
////			            response.setServiceResponse("Two different types of leaves are not allowed on consecutive days. Please ensure that the same type of leave is applied for consecutive days.");
////			            return response;
////			        }
////			    } else {
////			        System.err.println("recentLeaveToDate    ::  " + recentLeaveToDate);
////			        boolean isWeekOff = this.isWeekOffFind(recentLeaveToDate.plusDays(1), newLeaveFromDate.minusDays(1), leaveDTO.getState());
////
////			        if (isWeekOff) {
////			            EmployeeLeave findLeaveOnToDate = employeeLeaveRepository.findEmployeeLeaveByToDate(newLeaveFromDate.minusDays(1), leaveDTO.getEmpId());
////			            System.err.println("findLeaveOnToDate   ::  " + findLeaveOnToDate);
////			            if (findLeaveOnToDate != null) {
////			                if ((!leaveDTO.getLeaveTypeMasterId().equals(recentLeaves.getLeaveTypeMasterId()))) {
////			                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
////			                    response.setServiceResponse("Two different types of leaves are not allowed on consecutive days.Week Offs also consider your last leave To date");
////			                    return response;
////			                }
////			            } else {
////			                List<Holiday> findWeekOffAndFestival = holidayRepository.findWeekOffCountByFromAndToDate(newLeaveFromDate.minusDays(1), leaveDTO.getState());
////			                Optional<List<EmployeeLeave>> findPreviousLeaveByFromDateOptional = Optional.ofNullable(employeeLeaveRepository.findLeaveByFromDate(leaveDTO.getFromDate(), leaveDTO.getEmpId()));
////			               
////			                
////			                if (findPreviousLeaveByFromDateOptional.isPresent() && !findPreviousLeaveByFromDateOptional.get().isEmpty()) {
////			    			    EmployeeLeave findPreviousLeaveByFromDate = findPreviousLeaveByFromDateOptional.get().get(0);
////			    			    System.err.println("findPreviousLeaveByFromDate    ::   " + findPreviousLeaveByFromDate);
////			                System.err.println(" Anurag call else part " + findWeekOffAndFestival);
////			                if (!findWeekOffAndFestival.isEmpty()) {
////			                    if (!leaveDTO.getLeaveTypeMasterId().equals(findPreviousLeaveByFromDate.getLeaveTypeMasterId())) {
////			                        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
////			                        response.setServiceResponse("Two different types of leaves are not allowed on consecutive days.Week Offs also consider your last leave To date");
////			                        return response;
////			                    }
////			                }
////			                
////			                }
////			            }
////			        }
////			    }
////			}
//			
//			
//			LocalDate fromDate1 = LocalDate.parse(leaveDTO.getFromDate());
//			LocalDate toDate1 = LocalDate.parse(leaveDTO.getToDate());
//
//			boolean overlapping = employeeLeaveRepository.existsOverlappingLeave(leaveDTO.getEmpId(), fromDate1, toDate1);
//
//			if (overlapping) {
//			    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			    response.setServiceResponse("You already have an existing leave during the selected dates.");
//			    apiLogInfo.setApiResponse("Leave details are already present for selected dates.");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				apiLogInfo.setApiRequest(logBuilder.toString());
//				logService.logMyInfo(httpRequest, apiLogInfo);
//			    return response;
//			}
//
//			EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository
//					.findByEmpIdAndLeaveTypeMasterId(leaveDTO.getEmpId(), leaveDTO.getLeaveTypeMasterId());
//			
//			Optional<LeaveTypeMaster> leavetype = leaveTypeMasterRepository.findById(leaveDTO.getLeaveTypeMasterId());
//			
//			System.out.println("Leave DTO check :"+leaveDTO);
//			
//			List<Object[]> empObj = employeeRepository.getManagerEmail(leaveDTO.getEmpId());
//			
//			// added by anurag
//			
//			Employee findEmployee= employeeRepository.findByEmpId((long)leaveDTO.getManagerId());
//			System.out.println(" Reporting manager details : "+findEmployee);
//			
//			EmployeeDTO empDto = new EmployeeDTO();
//			
//				empObj.forEach((object) -> {
//					
//					empDto.setEmail(object[0] != null ? object[0].toString() : null);
//					empDto.setManagerEmail(object[1] != null ? object[1].toString() : null);
//					empDto.setName(object[2] != null ? object[2].toString() : null);
//					empDto.setEmployeementId(object[3] != null ? Long.parseLong(object[3].toString()): null);
//					empDto.setManagerName(object[4] != null ? object[4].toString() : null);
//					});
//				
//				System.out.println("Senior manager data :: "+findEmployee.getManagerId());
//				
//			Float availableCompOffBalance = 0.0F;
//			if(leaveDTO.getLeaveTypeCode().equalsIgnoreCase("CO")) {
//				ServiceResponse compOffResponse = compOffLeaveService.getCompOffBalanceDetailsByEmpIdAndFromDate(leaveDTO);
//				
//				if(compOffResponse.getServiceStatus().equals("Success")) {
//					List<LeaveDTO> availableCompOffList = (List<LeaveDTO>)compOffResponse.getServiceResponse();
//					
//					for(LeaveDTO compOff: availableCompOffList){
//						availableCompOffBalance = availableCompOffBalance + compOff.getNoOfDays();
//					};
//				}
//			}
//				
//			
//			// HERE : Effective Leave Balance = employeeLeavesMap.getBalance()
//			if (!leaveDTO.getLeaveTypeCode().equalsIgnoreCase("LWP") && (employeeLeavesMap.getBalance() == 0
//					|| employeeLeavesMap.getBalance() < leaveDTO.getNoOfDays())) {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Your available balance of " + employeeLeavesMap.getBalance()
//						+ " day(s) is not sufficient for this Leave Application.");
//				return response;
//			}else if(leaveDTO.getLeaveTypeCode().equalsIgnoreCase("CO") && availableCompOffBalance < leaveDTO.getNoOfDays()) {
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Your available Compensatory off balance of " + availableCompOffBalance
//						+ " day(s) before "+ leaveDTO.getFromDate() +" is not sufficient for this Leave Application.");
//				return response;
//			}
//			
//			// added by anurag for maternity leave apply
//			else if(leaveDTO.getLeaveTypeCode().equalsIgnoreCase("ML")) {
//				System.err.println(" Maternity apply leave call ");
//			}
//			
//			// added by anurag for CL		
//				
//				if(leaveDTO.getLeaveTypeCode().equalsIgnoreCase("CL") && leaveDTO.getNoOfDays()>clLeaveDays) {
//				
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);	
//				response.setServiceResponse("Casual Leave Can't take more than "+clLeaveDays+" days");
//				
//				return response;
//			}else {
////				if(leaveDTO.getLeaveTypeCode().equalsIgnoreCase("CL")) {
////					Optional<List<EmployeeLeave>> recentLeavesForCL = Optional.ofNullable(employeeLeaveRepository.findRecentLeavesByEmpId(leaveDTO.getEmpId()));
////					if(recentLeavesForCL.isPresent() && !recentLeavesForCL.get().isEmpty()) {
////						EmployeeLeave getLeaves = recentLeavesForCL.get().get(0);
////						LocalDate newFromDate = LocalDate.parse(leaveDTO.getFromDate());
////						LocalDate prevToDate = getLeaves.getToDate();		
////						
////					boolean valid = this.isValidateCasualLeave(prevToDate, newFromDate, leaveDTO.getLeaveTypeCode());
////					System.err.println(" valid "+valid);
////					if(valid) {
////						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);	
////						response.setServiceResponse("Leave application submitted. Your timesheet will be automatically added by system");
////					}else {
////						response.setServiceStatus(ServiceResponse.STATUS_FAIL);	
////						response.setServiceResponse("Casual Leave Can't take Consecutively , another CL will be applicable after 15 days of your last CL applied !! ");
////					return response;
////					}
////				}
////				}
////				else {
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);	
//					response.setServiceResponse("Leave application submitted. Your timesheet will be automatically added by system");
////				}
//				
//		}
//			
//			// ended
//
//			EmployeeLeave leaveApplication = new EmployeeLeave();
//
//			leaveApplication.setEmpId(leaveDTO.getEmpId());
//			leaveApplication.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
//			leaveApplication.setLeaveStatusId((short) 1);
//			leaveApplication.setFromDate(stringToDateTimeParser.getDate(leaveDTO.getFromDate(), "yyyy-MM-dd"));
//			leaveApplication.setToDate(stringToDateTimeParser.getDate(leaveDTO.getToDate(), "yyyy-MM-dd"));
//			leaveApplication.setNoOfDays((Float) leaveDTO.getNoOfDays());
//			leaveApplication.setReason(leaveDTO.getReason());
//			leaveApplication.getCommonProperty().setCreatedBy(leaveDTO.getCreatedBy());
//			leaveApplication.setFromDateDayType(leaveDTO.getFromDateDayType());
//			leaveApplication.setToDateDayType(leaveDTO.getToDateDayType());	
//			leaveApplication.setMaternityType(leaveDTO.getMaternityType());
//			leaveApplication.setMaternityLeaveDays(leaveDTO.getMaternityLeaveDays());
//			/* ----------- Multi-Level Approval ---------- */
//			// Approval Status : NA - Pending - Approved - Rejected
//			leaveApplication.setFinalApprovalLevel(leaveDTO.getFinalApprovalLevel());
//			
//			// By Default Current Approval level will be 1 i.e. Manager Approval			
//			
//			leaveApplication.setCurrentApprovalLevel(1);
//			// added by anurag
//						if(!findEmployee.getEmploymentstatus().equals("InActive")) {
//							System.out.println(" Manager is active "+leaveDTO.getManagerId());
//							leaveApplication.setManagerId(leaveDTO.getManagerId());
//						}else {
//							leaveApplication.setManagerId(Math.toIntExact(findEmployee.getManagerId()));
//							System.err.println(" in case of inactive manager "+findEmployee.getManagerId());
//						}
////			leaveApplication.setManagerId(leaveDTO.getManagerId());
//			leaveApplication.setManagerApprovalStatus("Pending");
//			
//			if(leaveApplication.getFinalApprovalLevel() == 2) {
//				leaveApplication.setLevel2ApproverId(leaveDTO.getLevel2ApproverId());
//				leaveApplication.setLevel2ApprovalStatus("Pending");
//				
//				leaveApplication.setLevel3ApproverId(null);
//				leaveApplication.setLevel3ApprovalStatus("NA");
//				
//			}else if(leaveApplication.getFinalApprovalLevel() == 3) {
//				leaveApplication.setLevel2ApproverId(leaveDTO.getLevel2ApproverId());
//				leaveApplication.setLevel2ApprovalStatus("Pending");
//				
//				leaveApplication.setLevel3ApproverId(leaveDTO.getLevel3ApproverId());
//				leaveApplication.setLevel3ApprovalStatus("Pending");
//			}else {
//				leaveApplication.setLevel2ApproverId(null);
//				leaveApplication.setLevel2ApprovalStatus("NA");
//				
//				leaveApplication.setLevel3ApproverId(null);
//				leaveApplication.setLevel3ApprovalStatus("NA");
//			}
//
//			// Leave deduction from balance leaves
//			Float balance = employeeLeavesMap.getBalance();
//			if(leaveDTO.getLeaveTypeCode().equalsIgnoreCase("LWP")) {
//				balance = 0F;
//			}else {
//				balance = balance - leaveDTO.getNoOfDays();
//			}
//			Float pendingForApproval = employeeLeavesMap.getPendingForApproval();
//			pendingForApproval = pendingForApproval + leaveDTO.getNoOfDays();
//
//			employeeLeavesMap.setBalance(balance);
//			employeeLeavesMap.setPendingForApproval(pendingForApproval);
//
//			EmployeeLeavesMap dbResponse1 = employeeLeavesMapRepository.save(employeeLeavesMap);
//
//			EmployeeLeave dbResponse2 = employeeLeaveRepository.save(leaveApplication);
//
//			if (dbResponse1 != null && dbResponse2 != null) {
//				
//				// 4 : compOff leave type Id
//				if(leavetype.get().getLeaveTypeCode().equals("CO")) {
//					long elapsedDays = ChronoUnit.DAYS.between(dbResponse2.getFromDate(),dbResponse2.getToDate());
//					
//					if(elapsedDays == 0) {
//							CompOffLeave oldestCompOffApplication = compOffLeaveRepository.findOldestCompOffApplicationByEmpId(dbResponse2.getEmpId(), "Pending");
//							
//							if(oldestCompOffApplication != null) {
//								oldestCompOffApplication.setCompOffStatus("Pending For Approval");
//								oldestCompOffApplication.setLeaveId(dbResponse2.getLeaveId());
//								
//								compOffLeaveRepository.save(oldestCompOffApplication);
//							}
//					}
//					if((elapsedDays != 0)) {
//						LocalDate tempDate = dbResponse2.getFromDate();
//
//						while(tempDate.compareTo(dbResponse2.getToDate()) != 1) {
//								CompOffLeave oldestCompOffApplication = compOffLeaveRepository.findOldestCompOffApplicationByEmpId(dbResponse2.getEmpId(), "Pending");
//								
//								if(oldestCompOffApplication != null) {
//									oldestCompOffApplication.setCompOffStatus("Pending For Approval");
//									oldestCompOffApplication.setLeaveId(dbResponse2.getLeaveId());
//									
//									compOffLeaveRepository.save(oldestCompOffApplication);
//								}
//								tempDate = tempDate.plusDays(1);
//							}
//						}
//				}
//				
//				LeaveBalanceLog log = new LeaveBalanceLog();
//				
//				log.setBalance(balance);
//				log.setEmpId(leaveDTO.getEmpId());
//				log.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
//				
//				if(!leaveDTO.getLeaveTypeCode().equalsIgnoreCase("LWP")) {
//					log.setMessage(LeaveLogMessage.requestDeductLeave.replace("0.0", leaveDTO.getNoOfDays().toString()));
//					log.setUpdateBalanceBy("-" + leaveDTO.getNoOfDays());
//				}
//				else {
//					log.setMessage(LeaveLogMessage.requestDeductLeave);
//					log.setUpdateBalanceBy("0"); 
//					}
//				leaveBalanceLogRepository.save(log);
//
//				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//				
//				if(LocalDate.parse(leaveDTO.getFromDate()).isBefore(LocalDate.now())) {
//					response.setServiceResponse("Leave application submitted. If you already filled the timesheet,that will be automatically updated by system");
//				}else {
//					response.setServiceResponse("Leave application submitted. Your timesheet will be automatically added by system");
//				}
//				
//				LeaveTypeMaster leaveType = leaveTypeMasterRepository.findByLeaveTypeCode(leaveDTO.getLeaveTypeCode());
//				
//				String managerEmail = "";
//				if(!leaveDTO.getApproverEmail().equals(empDto.getManagerEmail())) {
//					managerEmail = ","+ empDto.getManagerEmail();
//				}
//				
//				if(leaveApplication.getFinalApprovalLevel() == 2) {
//					managerEmail = ","+ leaveDTO.getLevel2ApproverEmail();
//					
//				}else if(leaveApplication.getFinalApprovalLevel() == 3) {
//					managerEmail = ","+ leaveDTO.getLevel2ApproverEmail() + ","+ leaveDTO.getLevel3ApproverEmail();
//				}
//				
//				if(leaveDTO.getCreatedBy().equals(leaveDTO.getEmpId())){
//					//Leave Applied for self
//					
//					mailService.sendMailWithCC(leaveDTO.getApproverEmail(), hrMailAddress +","+ leaveDTO.getEmail()+ managerEmail,
//							"Regarding Leave Application Request",
//							"Dear "+ leaveDTO.getApproverName() + ","+"<br>"
//							+"<br>"+" &nbsp"+" &nbsp"+" "+"Leave Application has been applied by "+ leaveDTO.getName() +" "+"for "+leaveDTO.getNoOfDays()+" day(s), Please take necessary action."+
//							"<br>"+"<br>"+"<b>"+"Leave Details"+"<b>"+
//							"<br>"+
//							"EmpID :"+" "+ leaveDTO.getEmployeementId()+
//							"<br>"+
//							"Name :"+" "+ leaveDTO.getName()+
//							"<br>"+
//							" From :"+" "+ leaveDTO.getFromDate()+
//							"<br>"+
//							" To :"+" "+ leaveDTO.getToDate() +
//							"<br>"+
//							" No. Of Days : "+ leaveDTO.getNoOfDays() + " day(s)" 
//							+"<br>"+
//							" Leave Type :"+" "+leavetype.get().getLeaveType()+
//							"<br>"+
//							"leave Reason :"+" "+leaveDTO.getReason());
//					
//				}else {
//					//Leave Applied for team
//					Optional<Employee> createdByEmp = employeeRepository.findById(leaveDTO.getCreatedBy());
//					if(!createdByEmp.isEmpty()) {
//						Employee createdByObj = createdByEmp.get();
//						
//						mailService.sendMailWithCC(leaveDTO.getApproverEmail(), hrMailAddress +","+ leaveDTO.getEmail() +","+ createdByObj.getEmail()+ managerEmail,
//								"Regarding Leave Application Request",
//								"Dear "+ leaveDTO.getApproverName() + ","
//								+"<br> Leave Application has been applied for "+ leaveDTO.getName() +" for "+leaveDTO.getNoOfDays()+" day(s)"+" by "+createdByObj.getName()+","+"Please take necessary action."
//								+"<br><br> Leave Details :"
//								+"<br> EmpId : A-" + leaveDTO.getEmployeementId()
//								+"<br> Name : " + leaveDTO.getName()
//								+"<br> From Date : " + leaveDTO.getFromDate() 
//								+"<br> To Date : " + leaveDTO.getToDate()
//								+"<br> No. Of Days : " + leaveDTO.getNoOfDays() +" day(s)"
//								+"<br> Leave Type :"+" "+leavetype.get().getLeaveType()
//								+"<br> Leave reason : " + leaveDTO.getReason());
//					}
//				}
//			
//				apiLogInfo.setApiResponse("Leave application submitted.");
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);	
//				System.out.println(leaveDTO.getFromDate());
//				System.out.println(leaveDTO.getToDate());
//
//				// IF Employee is Applying Leave for Half Day then, Automatic timesheet will not be filled as Leave
//				if(leaveDTO.getNoOfDays() > 0.5) {
//					LocalDate fromDate = LocalDate.parse(leaveDTO.getFromDate());
//					LocalDate toDate = LocalDate.parse( leaveDTO.getToDate());
//
//					long elapsedDays = ChronoUnit.DAYS.between(fromDate,toDate);
//					
//					List<Object[]> holidayList = holidayRepository.getHolidayWeekOffSize(leaveDTO.getFromDate(), leaveDTO.getToDate(),leaveDTO.getState());
//					
//				    List<Timesheet> empTimeSheet = timesheetsRepository.findTimesheetOnLeaveDate(leaveDTO.getEmpId(),leaveDTO.getFromDate(),leaveDTO.getToDate());
//				    if(!empTimeSheet.isEmpty()) {
//				    empTimeSheet.forEach((timesheet)->{
//				     	
//				    	List<TimesheetActivityMap> timesheetactivities = timesheetActivityRepository.getTimesheetActivityByTimesheetId(timesheet.getTimesheetId());
//				    	
//				    	timesheetactivities.forEach((timesheetactivity)->{
//				    		
//				    		timesheetActivityRepository.deleteById(timesheetactivity.getTimesheetActivityMapId());
//
//				    	});
//				    	
////				    	if(!timesheet.getDayType().equals("Public Holiday") && !timesheet.getDayType().equals("Week Off")) {
////				    		 timesheetsRepository.deleteById(timesheet.getTimesheetId());				    		
////				    	}
//				    	System.out.println("ANurag  find leave type  "+ leaveDTO.getLeaveTypeCode());
////				    	if(leaveDTO.getLeaveTypeCode().equals("ML")) {
////				    		timesheetsRepository.deleteById(timesheet.getTimesheetId());				    		
////				    	}else {
////				    		if(!timesheet.getDayType().equals("Public Holiday") && !timesheet.getDayType().equals("Week Off")) {
////					    		timesheetsRepository.deleteById(timesheet.getTimesheetId());				    		
////					    	}
////				    	}
//				    	timesheetsRepository.deleteById(timesheet.getTimesheetId());
//				    });
//				    }
//				    //after timesheet deletion
//				    List<Timesheet> empTimeSheetAfterDelete = timesheetsRepository.findTimesheetOnLeaveDate(leaveDTO.getEmpId(),leaveDTO.getFromDate(),leaveDTO.getToDate());
//
//					if((elapsedDays == 0)) {
//						
//						if(holidayList.isEmpty()) {
//							Timesheet newTimesheet = new Timesheet();
//							
//							newTimesheet.getCommonProperty().setCreatedBy(leaveDTO.getCreatedBy());
//							newTimesheet.setDate(fromDate);
//							newTimesheet.setDayType("Leave");
//							newTimesheet.setDescription("On leave");
//							newTimesheet.setEmpId(leaveDTO.getEmpId());
//							newTimesheet.setStatus("Approved");
//							newTimesheet.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
//
//							timesheetsRepository.save(newTimesheet);
//						}
//					}
//					if((elapsedDays != 0)) {
//						LocalDate tempDateToday = fromDate;
//
//						while(tempDateToday.compareTo(toDate) != 1) {
//							
////							if(tempDateToday.isEqual(fromDate) && leaveDTO.getFromDateDayType() == 0.5) {
////								System.out.println("From Date is Half Day");
////							}else if(tempDateToday.isEqual(toDate) && leaveDTO.getToDateDayType() == 0.5) {
////								System.out.println("To Date is Half Day");
//							if (tempDateToday.isEqual(fromDate) && Objects.equals(leaveDTO.getFromDateDayType(), 0.5)) {
//							    System.out.println("From Date is Half Day");
//							} else if (tempDateToday.isEqual(toDate) && Objects.equals(leaveDTO.getToDateDayType(), 0.5)) {
//							    System.out.println("To Date is Half Day");
//							
//
//							}else {
//								boolean isHoliday = false;
//								
//								if(leaveDTO.getIsWeekOffsExcluded().equals("false")) {
//									if(!holidayList.isEmpty()) {
//										for(Object[] holiday: holidayList) {
//											if(holiday[1].toString() != null) {
//												LocalDate holidayDate = LocalDate.parse(holiday[1].toString());
//												
//												if(tempDateToday.isEqual(holidayDate)){
//													isHoliday = true;
//													break;
//												}
//											}
//										}
//									}	
//								}
//								
//								System.out.println("check Date : "+ tempDateToday.toString() +", isHoliday : "+ isHoliday);
//								
//								if(!isHoliday) {
//									Timesheet newTimesheet = new Timesheet();
//
//									newTimesheet.getCommonProperty().setCreatedBy(leaveDTO.getCreatedBy());
//									newTimesheet.setDate(tempDateToday);
//									newTimesheet.setDayType("Leave");
//									newTimesheet.setDescription("On leave");
//									newTimesheet.setEmpId(leaveDTO.getEmpId());
//									newTimesheet.setStatus("Approved");
//									newTimesheet.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
//
//									timesheetsRepository.save(newTimesheet);
//								}
//							}
//							
//							tempDateToday = tempDateToday.plusDays(1);
//							
//						}
//					}
//				}
//				
//			} else {
//				response.setServiceResponse("Leave Creation Failed.");
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				
//				apiLogInfo.setApiResponse("Leave Creation Failed.");			
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiError(e.getMessage());			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//		}
//		
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		logService.logMyInfo(httpRequest, apiLogInfo);
//		return response;
//	}

//public boolean isWeekOffFind(LocalDate fromDate , LocalDate toDate, String state) {
//		
//		System.out.println(" from date :: "+fromDate);
//		System.out.println("toDate :: "+toDate);
//		List<Holiday> weekOffFind = holidayRepository.findWeekOffCountByFromAndToDate(fromDate,state);
//		System.err.println("weekOffFind   ::   "+weekOffFind.size());
//		for (Holiday holiday : weekOffFind) {
//			System.err.println(holiday.toString()+"\n");
//		}
//		
//		if(weekOffFind.size() < 3) 
//			return true;
//		
//		else 
//			return false;
//	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse applyLeave(LeaveDTO leaveDTO) throws Exception {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Apply Leave");
	    apiLogInfo.setApiUrl("/api/applyLeave");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("empId : " + leaveDTO.getEmpId()
	            + ", leaveTypeMasterId : " + leaveDTO.getLeaveTypeMasterId()
	            + ", leaveTypeCode : " + leaveDTO.getLeaveTypeCode()
	            + ", noOfDays : " + leaveDTO.getNoOfDays()
	            + ", createdBy : " + leaveDTO.getCreatedBy());
	    System.out.println(leaveDTO);

	    try {
	        // -------------------------
	        // 1) Basic null checks
	        // -------------------------
	        if (leaveDTO == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Invalid request payload");
	            return response;
	        }
	        if (leaveDTO.getEmpId() == null || leaveDTO.getCreatedBy() == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("empId and createdBy are required.");
	            return response;
	        }
	        if (!StringUtils.hasText(leaveDTO.getFromDate()) || !StringUtils.hasText(leaveDTO.getToDate())) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("From date and To date are required.");
	            return response;
	        }

	        // -------------------------
	        // 2) Parse and validate date formats (expecting yyyy-MM-dd)
	        // -------------------------
	        LocalDate fromDate;
	        LocalDate toDate;
	        try {
	            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	            fromDate = LocalDate.parse(leaveDTO.getFromDate(), fmt);
	            toDate = LocalDate.parse(leaveDTO.getToDate(), fmt);
	        } catch (DateTimeParseException dtpe) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Invalid date format. Please use yyyy-MM-dd.");
	            return response;
	        }

	        // fromDate must not be after toDate
	        if (fromDate.isAfter(toDate)) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("From date cannot be after To date.");
	            return response;
	        }

	        // Optional: you may want to disallow leave longer than some limit; not implemented here.

	        // -------------------------
	        // 3) Authorization: who can apply leave for empId?
	        //    Allowed:
	        //       - self (createdBy == empId)
	        //       - teammate (same team)
	        //       - reporting manager
	        //       - HR or HOD role
	        // -------------------------
	        boolean isSelfApply = Objects.equals(leaveDTO.getCreatedBy(), leaveDTO.getEmpId());
	        if (!isSelfApply) {
	            // Attempt role / relationship checks via repository/service.
	            // Implement the following repository methods if missing:
	            // - employeeRepository.isTeamMate(createdBy, empId)
	            // - employeeRepository.isReportingManager(createdBy, empId)
	            // - employeeRepository.hasAnyRole(createdBy, Arrays.asList("HR","HOD"))
	            boolean isTeamMate = false;
//	            boolean isReportingManager = false;
//	            boolean isHrOrHod = false;

	           
	            	List<Long> teamList = getAllTeamMemberView(leaveDTO.getCreatedBy());
	            	isTeamMate = teamList.stream()
							.anyMatch(emp -> emp != null && emp.equals(leaveDTO.getEmpId()));
//					if (!isTeamMate) {
//						throw new UnauthorizedAccessException("Employee not authorized to perform this action");
//					}
////	                

	            if (!isTeamMate ) {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("You are not authorized to apply leave for this employee.");
	                apiLogInfo.setApiResponse("Unauthorized leave apply attempt for empId: " + leaveDTO.getEmpId());
	                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	                apiLogInfo.setApiRequest(logBuilder.toString());
	                logService.logMyInfo(httpRequest, apiLogInfo);
	                return response;
	            }
	        }

	        // -------------------------
	        // 4) Overlap check - prevents adding leaves over existing leaves
	        // -------------------------
	        boolean overlapping = employeeLeaveRepository.existsOverlappingLeave(leaveDTO.getEmpId(), fromDate, toDate);
	        if (overlapping) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("You already have an existing leave during the selected dates.");
	            apiLogInfo.setApiResponse("Leave details are already present for selected dates.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            logService.logMyInfo(httpRequest, apiLogInfo);
	            return response;
	        }

	        // -------------------------
	        // 5) Load leave type & employee leaves map safely
	        // -------------------------
	        EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository
	                .findByEmpIdAndLeaveTypeMasterId(leaveDTO.getEmpId(), leaveDTO.getLeaveTypeMasterId());
	        if (employeeLeavesMap == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Leave balance not configured for this employee and leave type.");
	            return response;
	        }

	        Optional<LeaveTypeMaster> leavetypeOpt = leaveTypeMasterRepository.findById(leaveDTO.getLeaveTypeMasterId());
	        if (!leavetypeOpt.isPresent()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Invalid leave type.");
	            return response;
	        }
	        LeaveTypeMaster leavetype = leavetypeOpt.get();

	        // -------------------------
	        // 6) Comp-off validation (if applicable)
	        // -------------------------
	        Float availableCompOffBalance = 0.0F;
	        if ("CO".equalsIgnoreCase(leaveDTO.getLeaveTypeCode())) {
	            ServiceResponse compOffResponse = compOffLeaveService.getCompOffBalanceDetailsByEmpIdAndFromDate(leaveDTO);
	            if (compOffResponse != null && ServiceResponse.STATUS_SUCCESS.equals(compOffResponse.getServiceStatus())) {
	                List<LeaveDTO> availableCompOffList = (List<LeaveDTO>) compOffResponse.getServiceResponse();
	                if (availableCompOffList != null) {
	                    for (LeaveDTO compOff : availableCompOffList) {
	                        availableCompOffBalance += (compOff.getNoOfDays() != null ? compOff.getNoOfDays() : 0f);
	                    }
	                }
	            }
	            if (availableCompOffBalance < leaveDTO.getNoOfDays()) {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Your available Compensatory off balance of " + availableCompOffBalance
	                        + " day(s) before " + leaveDTO.getFromDate() + " is not sufficient for this Leave Application.");
	                return response;
	            }
	        }

	        // -------------------------
	        // 7) Balance validation (unless LWP)
	        // -------------------------
	        if (!"LWP".equalsIgnoreCase(leaveDTO.getLeaveTypeCode())) {
	            if (employeeLeavesMap.getBalance() == null || employeeLeavesMap.getBalance() == 0
	                    || employeeLeavesMap.getBalance() < leaveDTO.getNoOfDays()) {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("Your available balance of " + employeeLeavesMap.getBalance()
	                        + " day(s) is not sufficient for this Leave Application.");
	                return response;
	            }
	        }

//	        // -------------------------
//	        // 8) Specific business rule: CL max days
//	        // -------------------------
//	        if ("CL".equalsIgnoreCase(leaveDTO.getLeaveTypeCode()) && leaveDTO.getNoOfDays() > clLeaveDays) {
//	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	            response.setServiceResponse("Casual Leave can't be taken for more than " + clLeaveDays + " day(s).");
//	            return response;
//	        }
	        if(leaveDTO.getLeaveTypeCode().equalsIgnoreCase("CL") && leaveDTO.getNoOfDays()>clLeaveDays) {
				
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);	
				response.setServiceResponse("Casual Leave Can't take more than "+clLeaveDays+" days");
				
				return response;
			}else if (leaveDTO.getLeaveTypeCode().equalsIgnoreCase("CL")) {

			    YearMonth appliedMonth = YearMonth.from(
			            LocalDate.parse(leaveDTO.getFromDate())
			    );

			    List<EmployeeLeave> clLeavesThisMonth =
			            employeeLeaveRepository.findByEmpIdAndLeaveTypeAndMonth(
			                    leaveDTO.getEmpId(),
			                    leaveDTO.getLeaveTypeMasterId(),
			                    appliedMonth.getYear(),
			                    appliedMonth.getMonthValue()
			            );

			    double totalCLDaysThisMonth = clLeavesThisMonth.stream()
			            .filter(leave ->
			                    leave.getLeaveStatusId() != null &&
			                    (leave.getLeaveStatusId() == 1 || leave.getLeaveStatusId() == 2))
			            .mapToDouble(EmployeeLeave::getNoOfDays)
			            .sum();

			    if (totalCLDaysThisMonth + leaveDTO.getNoOfDays() > 2.0) {
			        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			        response.setServiceResponse(
			                "Casual Leave cannot exceed 2 days in a month. Already applied: "
			                + totalCLDaysThisMonth + " days."
			        );
			        return response;
			    }
			}

			else {

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);	
					response.setServiceResponse("Leave application submitted. Your timesheet will be automatically added by system");
				
		}

	        // Additional business checks (maternity etc.) can be kept here (no-op logging as in original).
	        if ("ML".equalsIgnoreCase(leaveDTO.getLeaveTypeCode())) {
	            System.err.println("Maternity leave application detected - additional validations may apply.");
	        }

	        // If validations pass, set initial positive response (as your original logic does)
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Leave application submitted. Your timesheet will be automatically added by system");

	        // -------------------------
	        // 9) Build & persist EmployeeLeave entity
	        // -------------------------
	        EmployeeLeave leaveApplication = new EmployeeLeave();
	        leaveApplication.setEmpId(leaveDTO.getEmpId());
	        leaveApplication.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
	        leaveApplication.setLeaveStatusId((short) 1); // pending
	        leaveApplication.setFromDate(stringToDateTimeParser.getDate(leaveDTO.getFromDate(), "yyyy-MM-dd"));
	        leaveApplication.setToDate(stringToDateTimeParser.getDate(leaveDTO.getToDate(), "yyyy-MM-dd"));
	        leaveApplication.setNoOfDays((Float) leaveDTO.getNoOfDays());
	        leaveApplication.setReason(leaveDTO.getReason());
	        leaveApplication.getCommonProperty().setCreatedBy(leaveDTO.getCreatedBy());
	        leaveApplication.setFromDateDayType(leaveDTO.getFromDateDayType());
	        leaveApplication.setToDateDayType(leaveDTO.getToDateDayType());
	        leaveApplication.setMaternityType(leaveDTO.getMaternityType());
	        leaveApplication.setMaternityLeaveDays(leaveDTO.getMaternityLeaveDays());

	        // Multi level approval fields
	        leaveApplication.setFinalApprovalLevel(leaveDTO.getFinalApprovalLevel());
	        leaveApplication.setCurrentApprovalLevel(1);

	        // Manager assignment with fallback when manager is inactive
	        Employee findEmployee = employeeRepository.findByEmpId((long) leaveDTO.getManagerId());
	        if (findEmployee != null && !"InActive".equalsIgnoreCase(findEmployee.getEmploymentstatus())) {
	            leaveApplication.setManagerId(leaveDTO.getManagerId());
	        } else {
	            if (findEmployee != null && findEmployee.getManagerId() != null) {
	                leaveApplication.setManagerId(Math.toIntExact(findEmployee.getManagerId()));
	            } else {
	                // no valid manager -> leave it null or set to default HR approver id as per org rule
	                leaveApplication.setManagerId(leaveDTO.getManagerId()); // fallback to original passed manager id
	            }
	        }
	        leaveApplication.setManagerApprovalStatus("Pending");

	        if (leaveApplication.getFinalApprovalLevel() == 2) {
	            leaveApplication.setLevel2ApproverId(leaveDTO.getLevel2ApproverId());
	            leaveApplication.setLevel2ApprovalStatus("Pending");
	            leaveApplication.setLevel3ApproverId(null);
	            leaveApplication.setLevel3ApprovalStatus("NA");
	        } else if (leaveApplication.getFinalApprovalLevel() == 3) {
	            leaveApplication.setLevel2ApproverId(leaveDTO.getLevel2ApproverId());
	            leaveApplication.setLevel2ApprovalStatus("Pending");
	            leaveApplication.setLevel3ApproverId(leaveDTO.getLevel3ApproverId());
	            leaveApplication.setLevel3ApprovalStatus("Pending");
	        } else {
	            leaveApplication.setLevel2ApproverId(null);
	            leaveApplication.setLevel2ApprovalStatus("NA");
	            leaveApplication.setLevel3ApproverId(null);
	            leaveApplication.setLevel3ApprovalStatus("NA");
	        }

	        // -------------------------
	        // 10) Deduct pending & update balances
	        // -------------------------
	        Float balance = employeeLeavesMap.getBalance() != null ? employeeLeavesMap.getBalance() : 0F;
	        if ("LWP".equalsIgnoreCase(leaveDTO.getLeaveTypeCode())) {
	            balance = 0F;
	        } else {
	            balance = balance - (leaveDTO.getNoOfDays() != null ? leaveDTO.getNoOfDays() : 0f);
	        }
	        Float pendingForApproval = employeeLeavesMap.getPendingForApproval() != null
	                ? employeeLeavesMap.getPendingForApproval() : 0F;
	        pendingForApproval = pendingForApproval + (leaveDTO.getNoOfDays() != null ? leaveDTO.getNoOfDays() : 0f);

	        employeeLeavesMap.setBalance(balance);
	        employeeLeavesMap.setPendingForApproval(pendingForApproval);

	        EmployeeLeavesMap dbResponse1 = employeeLeavesMapRepository.save(employeeLeavesMap);
	        EmployeeLeave dbResponse2 = employeeLeaveRepository.save(leaveApplication);

	        // -------------------------
	        // 11) Post-save actions on success
	        // -------------------------
	        if (dbResponse1 != null && dbResponse2 != null) {
	            // Comp-off linking if required (CO)
	            if ("CO".equalsIgnoreCase(leavetype.getLeaveTypeCode())) {
	                long elapsedDays = ChronoUnit.DAYS.between(dbResponse2.getFromDate(), dbResponse2.getToDate());
	                if (elapsedDays == 0) {
	                    CompOffLeave oldestCompOffApplication = compOffLeaveRepository
	                            .findOldestCompOffApplicationByEmpId(dbResponse2.getEmpId(), "Pending");
	                    if (oldestCompOffApplication != null) {
	                        oldestCompOffApplication.setCompOffStatus("Pending For Approval");
	                        oldestCompOffApplication.setLeaveId(dbResponse2.getLeaveId());
	                        compOffLeaveRepository.save(oldestCompOffApplication);
	                    }
	                } else {
	                    LocalDate tempDate = dbResponse2.getFromDate();
	                    while (tempDate.compareTo(dbResponse2.getToDate()) != 1) {
	                        CompOffLeave oldestCompOffApplication = compOffLeaveRepository
	                                .findOldestCompOffApplicationByEmpId(dbResponse2.getEmpId(), "Pending");
	                        if (oldestCompOffApplication != null) {
	                            oldestCompOffApplication.setCompOffStatus("Pending For Approval");
	                            oldestCompOffApplication.setLeaveId(dbResponse2.getLeaveId());
	                            compOffLeaveRepository.save(oldestCompOffApplication);
	                        }
	                        tempDate = tempDate.plusDays(1);
	                    }
	                }
	            }

	            // Leave balance log
	            LeaveBalanceLog log = new LeaveBalanceLog();
	            log.setBalance(balance);
	            log.setEmpId(leaveDTO.getEmpId());
	            log.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
	            if (!"LWP".equalsIgnoreCase(leaveDTO.getLeaveTypeCode())) {
	                log.setMessage(LeaveLogMessage.requestDeductLeave.replace("0.0",
	                        String.valueOf(leaveDTO.getNoOfDays())));
	                log.setUpdateBalanceBy("-" + leaveDTO.getNoOfDays());
	            } else {
	                log.setMessage(LeaveLogMessage.requestDeductLeave);
	                log.setUpdateBalanceBy("0");
	            }
	            leaveBalanceLogRepository.save(log);

	            // Response message depending on whether fromDate is before today or not
	            if (fromDate.isBefore(LocalDate.now())) {
	                response.setServiceResponse(
	                        "Leave application submitted. If you already filled the timesheet, that will be automatically updated by the system");
	            } else {
	                response.setServiceResponse("Leave application submitted. Your timesheet will be automatically added by system");
	            }

	            // Notification / email composition
	            String managerEmail = "";
	            List<Object[]> empObj = employeeRepository.getManagerEmail(leaveDTO.getEmpId());
	            EmployeeDTO empDto = new EmployeeDTO();
	            if (empObj != null) {
	                empObj.forEach((object) -> {
	                    empDto.setEmail(object[0] != null ? object[0].toString() : null);
	                    empDto.setManagerEmail(object[1] != null ? object[1].toString() : null);
	                    empDto.setName(object[2] != null ? object[2].toString() : null);
	                    empDto.setEmployeementId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
	                    empDto.setManagerName(object[4] != null ? object[4].toString() : null);
	                });
	            }

	            if (!StringUtils.isEmpty(leaveDTO.getApproverEmail()) && !leaveDTO.getApproverEmail().equals(empDto.getManagerEmail())) {
	                managerEmail = "," + (empDto.getManagerEmail() != null ? empDto.getManagerEmail() : "");
	            }

	            if (leaveApplication.getFinalApprovalLevel() == 2) {
	                managerEmail = "," + leaveDTO.getLevel2ApproverEmail();
	            } else if (leaveApplication.getFinalApprovalLevel() == 3) {
	                managerEmail = "," + leaveDTO.getLevel2ApproverEmail() + "," + leaveDTO.getLevel3ApproverEmail();
	            }

	            // send email (self vs apply for team)
	            if (Objects.equals(leaveDTO.getCreatedBy(), leaveDTO.getEmpId())) {
	                // self apply
	            	mailService.sendMailWithCC(leaveDTO.getApproverEmail(),
	                        hrMailAddress + "," + leaveDTO.getEmail() + managerEmail,
	                        "Regarding Leave Application Request",
	                        "Dear " + leaveDTO.getApproverName() + ",<br><br>" +
	                                "Leave Application has been applied by " + leaveDTO.getName() + " for "
	                                + leaveDTO.getNoOfDays() + " day(s). Please take necessary action.<br><br>" +
	                                "<b>Leave Details</b><br>" +
	                                "EmpID : " + leaveDTO.getEmployeementId() + "<br>" +
	                                "Name : " + leaveDTO.getName() + "<br>" +
	                                "From : " + leaveDTO.getFromDate() + "<br>" +
	                                "To : " + leaveDTO.getToDate() + "<br>" +
	                                "No. Of Days : " + leaveDTO.getNoOfDays() + " day(s)" + "<br>" +
	                                "Leave Type : " + leavetype.getLeaveType() + "<br>" +
	                                "Leave reason : " + leaveDTO.getReason());
	            } else {
	                // applied by teammate/manager/hr/hod
	                Optional<Employee> createdByEmp = employeeRepository.findById(leaveDTO.getCreatedBy());
	                if (createdByEmp.isPresent()) {
	                    Employee createdByObj = createdByEmp.get();
	                    mailService.sendMailWithCC(leaveDTO.getApproverEmail(),
	                            hrMailAddress + "," + leaveDTO.getEmail() + "," + createdByObj.getEmail() + managerEmail,
	                            "Regarding Leave Application Request",
	                            "Dear " + leaveDTO.getApproverName() + ",<br>" +
	                                    "Leave Application has been applied for " + leaveDTO.getName() + " for "
	                                    + leaveDTO.getNoOfDays() + " day(s) by " + createdByObj.getName()
	                                    + ". Please take necessary action.<br><br>Leave Details :<br>" +
	                                    "EmpId : A-" + leaveDTO.getEmployeementId() + "<br>" +
	                                    "Name : " + leaveDTO.getName() + "<br>" +
	                                    "From Date : " + leaveDTO.getFromDate() + "<br>" +
	                                    "To Date : " + leaveDTO.getToDate() + "<br>" +
	                                    "No. Of Days : " + leaveDTO.getNoOfDays() + " day(s)" + "<br>" +
	                                    "Leave Type : " + leavetype.getLeaveType() + "<br>" +
	                                    "Leave reason : " + leaveDTO.getReason());
	                }
	               
	            }
	            
	            cronJobService.sendHrDepartmentNotification(leaveDTO, leavetype);

	            apiLogInfo.setApiResponse("Leave application submitted.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

	            // -------------------------
	            // 12) Timesheet create/delete logic for applied leave (if > 0.5 days)
	            // -------------------------
	            if (leaveDTO.getNoOfDays() != null && leaveDTO.getNoOfDays() > 0.5) {
	                long elapsedDays = ChronoUnit.DAYS.between(fromDate, toDate);
	                List<Object[]> holidayList = holidayRepository.getHolidayWeekOffSize(leaveDTO.getFromDate(),
	                        leaveDTO.getToDate(), leaveDTO.getState());

	                List<Timesheet> empTimeSheet = timesheetsRepository.findTimesheetOnLeaveDateOLD(leaveDTO.getEmpId(),
	                        leaveDTO.getFromDate(), leaveDTO.getToDate());
	                if (empTimeSheet != null && !empTimeSheet.isEmpty()) {
	                    for (Timesheet timesheet : empTimeSheet) {
	                    	if(timesheet.getDayType()==null || (timesheet.getDayType().equals("Working") || timesheet.getDayType().equals("Non-working"))) {
	                        List<TimesheetActivityMap> timesheetactivities = timesheetActivityRepository
	                                .getTimesheetActivityByTimesheetId(timesheet.getTimesheetId());
	                        if (timesheetactivities != null) {
	                            for (TimesheetActivityMap timesheetactivity : timesheetactivities) {
	                                timesheetActivityRepository.deleteById(timesheetactivity.getTimesheetActivityMapId());
	                            }
	                        }
	                        timesheetsRepository.deleteById(timesheet.getTimesheetId());
	                    	}
	                    }
	                    entityManager.flush();
	                }

	                // create timesheets for days that are not holidays (and not half-day edges)
	                if (elapsedDays == 0) {
	                    if (holidayList == null || holidayList.isEmpty()) {
	                        Timesheet newTimesheet = new Timesheet();
	                        newTimesheet.getCommonProperty().setCreatedBy(leaveDTO.getCreatedBy());
	                        newTimesheet.setDate(fromDate);
	                        newTimesheet.setDayType("Leave");
	                        newTimesheet.setDescription("On leave");
	                        newTimesheet.setEmpId(leaveDTO.getEmpId());
	                        newTimesheet.setStatus("Approved");
	                        newTimesheet.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
	                        timesheetsRepository.save(newTimesheet);
	                    }
	                } else {
	                    LocalDate tempDateToday = fromDate;
	                    while (tempDateToday.compareTo(toDate) != 1) {
	                        if (tempDateToday.isEqual(fromDate) && Objects.equals(leaveDTO.getFromDateDayType(), 0.5)) {
	                            // skip half day start
	                        } else if (tempDateToday.isEqual(toDate) && Objects.equals(leaveDTO.getToDateDayType(), 0.5)) {
	                            // skip half day end
	                        } else {
	                            boolean isHoliday = false;
	                            if ("false".equalsIgnoreCase(leaveDTO.getIsWeekOffsExcluded())) {
	                                if (holidayList != null) {
	                                    for (Object[] holiday : holidayList) {
	                                        if (holiday[1] != null) {
	                                            LocalDate holidayDate = LocalDate.parse(holiday[1].toString());
	                                            if (tempDateToday.isEqual(holidayDate)) {
	                                                isHoliday = true;
	                                                break;
	                                            }
	                                        }
	                                    }
	                                }
	                            }
	                            if (!isHoliday) {
	                                Timesheet newTimesheet = new Timesheet();
	                                newTimesheet.getCommonProperty().setCreatedBy(leaveDTO.getCreatedBy());
	                                newTimesheet.setDate(tempDateToday);
	                                newTimesheet.setDayType("Leave");
	                                newTimesheet.setDescription("On leave");
	                                newTimesheet.setEmpId(leaveDTO.getEmpId());
	                                newTimesheet.setStatus("Approved");
	                                newTimesheet.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
	                                timesheetsRepository.save(newTimesheet);
	                            }
	                        }
	                        tempDateToday = tempDateToday.plusDays(1);
	                    }
	                }
	            }

	        } else {
	            // Save failed
	            response.setServiceResponse("Leave Creation Failed.");
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("Leave Creation Failed.");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        }

	    } catch (Exception e) {
	        // Log and rethrow to ensure transactional rollback
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");

	        // persist log before throwing
	        apiLogInfo.setApiRequest(logBuilder.toString());
	        logService.logMyInfo(httpRequest, apiLogInfo);

	        // rethrow so transaction rolls back (caller/controller will receive exception)
	        throw e;
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}

	public List<Long> getAllTeamMemberView(Long empId) {
//	    LocalDate date = LocalDate.now().minusDays(Long.parseLong(timesheetCheckPeriod));
	    List<Object[]> list = employeeRepository.getAllTeamMemberView(empId);
	    List<Long> empList = new ArrayList<>();

	    if (list == null || list.isEmpty()) {
	        return Collections.emptyList(); // Return empty list if no team members found
	    }

	    list.forEach((object) -> {
	    	empList.add(object[0] != null ? Long.parseLong(object[0].toString()) : null);
	    });

	    return empList;
	}
	
	
	
/**
 * create validation method for CL to validate 15 days gap in between each CL leaves
 * @param leaveDTO
 * @return
 */

public boolean isValidateCasualLeave(LocalDate toDate, LocalDate fromDate, String leaveTypeCode) {

    System.err.println("Validation method toDate " + toDate);

    Optional<List<Object[]>> leaveRecords = employeeLeaveRepository.findClLeavesInBetweenDates(toDate, leaveTypeCode);
    LocalDate after15Days = toDate.plusDays(15);

    System.out.println("Validation from date " + fromDate);
    System.err.println("After 15 Days " + after15Days);

    if (leaveRecords.isPresent()) {
        List<Object[]> leaves = leaveRecords.get();

        if (!leaves.isEmpty() && fromDate.isAfter(after15Days)) {
            return true;
        } else {
            return false;
        }
    } else {
        return true;
    }
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
				Employee empObj = employeeRepository.findByEmpId(leaveToBeDeleted.getEmpId());

				LeaveRevokeApplication leaveRevokeApplication = new LeaveRevokeApplication();

				leaveRevokeApplication.setLeaveId(leaveToBeDeleted.getLeaveId());
				leaveRevokeApplication.setEmpId(leaveToBeDeleted.getEmpId());
				leaveRevokeApplication.setManagerId(empObj.getManagerId());
				leaveRevokeApplication.setLeaveRevokeStatusId((short)5);
				leaveRevokeApplication.setReason(leaveToBeDeleted.getReason());
				leaveRevokeApplication.setRemark(leaveToBeDeleted.getRemark());
				leaveRevokeApplication.setLeaveType(leaveDTO.getLeaveType());
				leaveRevokeApplication.setFromDate(leaveToBeDeleted.getFromDate());
				leaveRevokeApplication.setNoOfDays(leaveToBeDeleted.getNoOfDays());
				leaveRevokeApplication.setToDate(leaveToBeDeleted.getToDate());
				CommonProperties commonProp = new CommonProperties();
				commonProp.setCreatedBy(leaveDTO.getEmpId());
				leaveRevokeApplication.setCommonProperty(commonProp);
				leaveRevokeApplicationRepository.save(leaveRevokeApplication);

				employeeLeaveRepository.deleteById(leaveDTO.getLeaveId());

				//Get Expiration Period of CompOff

				Integer expirationPeriod = null;
				Optional<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findById(leaveDTO.getLeaveTypeMasterId());
				LeaveTypeMaster leaveTypeObj = leaveType.get();
				if(leaveTypeObj.getLeaveTypeCode().equals("CO")) {
					LeavePolicyMaster leavePolicy  = leavePolicyMasterRepository.findByLeaveTypeMasterIdAndEmploymentStatus(leaveTypeObj.getLeaveTypeMasterId(),empObj.getEmploymentstatus());
				      if(leavePolicy != null){
				    	  if(leavePolicy.getExpirationPeriod().equals("Yes")) {
					        	 expirationPeriod = leavePolicy.getExpirationPeriodValue();
			               }
				      }
				}
				
				if(!leaveTypeObj.getLeaveTypeCode().equals("CO")){
					EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository.findByEmpIdAndLeaveTypeMasterId(
							leaveToBeDeleted.getEmpId(), leaveToBeDeleted.getLeaveTypeMasterId());
					
					Float balance = employeeLeavesMap.getBalance();
					if("LWP".equalsIgnoreCase(leaveTypeObj.getLeaveTypeCode())) {
						balance = 0F;
					}else {					
						balance = balance + leaveDTO.getNoOfDays();
					}
					
					Float pendingForApproval = employeeLeavesMap.getPendingForApproval();
					pendingForApproval = pendingForApproval - leaveDTO.getNoOfDays();
					
					employeeLeavesMap.setBalance(balance);
					employeeLeavesMap.setPendingForApproval(pendingForApproval);
					
					System.out.println(employeeLeavesMap + " employee leave");
					EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(employeeLeavesMap);
					
					if(dbResponse != null) {
						LeaveBalanceLog log = new LeaveBalanceLog();
						
						log.setBalance(balance);
						log.setEmpId(leaveToBeDeleted.getEmpId());
						log.setLeaveTypeMasterId(leaveToBeDeleted.getLeaveTypeMasterId());
						if("LWP".equalsIgnoreCase(leaveTypeObj.getLeaveTypeCode())) {
							log.setMessage(LeaveLogMessage.deleteLeave);
						}else {					
							log.setMessage(LeaveLogMessage.deleteLeave.replace("0.0", leaveDTO.getNoOfDays().toString()));
						}
						
						if(!leaveDTO.getLeaveTypeMasterId().equals((short)3)) // 16 is in local and 19 in UAT But in prod it is 3
							log.setUpdateBalanceBy("+" + leaveDTO.getNoOfDays());
						else
							log.setUpdateBalanceBy("0");
						leaveBalanceLogRepository.save(log);
					}
				}
				

				// CompOff Leave : 4 (LeaveTypeMasterId)
				if(leaveTypeObj.getLeaveTypeCode().equals("CO")) {
					
					List<CompOffLeave> compOffLeave = compOffLeaveRepository.findByLeaveId(leaveToBeDeleted.getLeaveId());
					EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository.findByEmpIdAndLeaveTypeMasterId(
							leaveToBeDeleted.getEmpId(), leaveToBeDeleted.getLeaveTypeMasterId());
					
					if(!compOffLeave.isEmpty()) {
//					if(employeeLeavesMap.getPendingForApproval() > 0) {
							
							for(CompOffLeave leave: compOffLeave) {
								
								LocalDate expireDate = leave.getFromDate().plusDays(expirationPeriod);
								if(LocalDate.now().isAfter(expireDate) || LocalDate.now().isEqual(expireDate)) {
									
									leave.setCompOffStatus("Expired");
									compOffLeaveRepository.save(leave);
								}else {
									
									//Update Leave Balance
//									EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository.findByEmpIdAndLeaveTypeMasterId(
//											leaveToBeDeleted.getEmpId(), leaveToBeDeleted.getLeaveTypeMasterId());
									
									Float balance = employeeLeavesMap.getBalance();
									balance = balance + leave.getNoOfDays();
									Float pendingForApproval = employeeLeavesMap.getPendingForApproval();
									pendingForApproval = pendingForApproval - leave.getNoOfDays();
									
									employeeLeavesMap.setBalance(balance);
									employeeLeavesMap.setPendingForApproval(pendingForApproval);
									
									System.out.println(employeeLeavesMap + " employee leave");
									EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(employeeLeavesMap);
									
									if(dbResponse != null) {
										LeaveBalanceLog log = new LeaveBalanceLog();
										
										log.setBalance(balance);
										log.setEmpId(leaveToBeDeleted.getEmpId());
										log.setLeaveTypeMasterId(leaveToBeDeleted.getLeaveTypeMasterId());
										log.setMessage(LeaveLogMessage.deleteLeave.replace("0.0", leave.getNoOfDays().toString()));
										log.setUpdateBalanceBy("+" + leave.getNoOfDays());
										
										leaveBalanceLogRepository.save(log);
									}
								
									
									// change compOff application status
										leave.setCompOffStatus("Pending");
										leave.setLeaveId(null);
										
										compOffLeaveRepository.save(leave);
								}
								
							}
					}
				}

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Leave Application Deleted.");
				
				apiLogInfo.setApiResponse("Leave Application Deleted.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
				Optional<Employee> reportingManager = employeeRepository.findById(empObj.getManagerId());
				
				String managerEmail = "";
				if(!leaveDTO.getApproverEmail().equals(reportingManager.get().getEmail())) {
					managerEmail = ","+ reportingManager.get().getEmail();
				}
				
				// Level 2/3 Approver Email
				if(leaveDTO.getFinalApprovalLevel() == 2) {
					managerEmail = ","+ leaveDTO.getLevel2ApproverEmail();
					
				}else if(leaveDTO.getFinalApprovalLevel() == 3) {
					managerEmail = ","+ leaveDTO.getLevel2ApproverEmail() + ","+ leaveDTO.getLevel3ApproverEmail();
				}
				
				mailService.sendMailWithCC(leaveDTO.getManagerEmail(), leaveDTO.getEmail() +","+hrMailAddress+managerEmail, "Regarding Leave Application Request Deletion", 
						"Dear "+ leaveDTO.getManagerName()+","+
				"<br> "
				+" &nbsp;"+" &nbsp;"+" "+"Pending leave application has been deleted by "+ leaveDTO.getEmployeeName() +"."+
				"<br>"+"<br>"+"<b>"+"Leave Details :"+"<b>"+
				"<br>"+
				"EmpID :"+"A- "+ leaveDTO.getEmployeementId()+
				"<br>"+
				"Name :"+" "+ leaveDTO.getEmployeeName()+
				"<br>"+
				" from "+" "+ leaveDTO.getFromDate() +
				"<br>"+
				" To Date : "+" "+ leaveDTO.getToDate() 
				+"<br>"+
				"No. Of Days :"+" "+leaveDTO.getNoOfDays()+" "+"day(s)"+
				"<br>"+
				"Leave Type:"+" "+leaveDTO.getLeaveType());
				
				
				//Autofill timesheet delete on deleting pending leave
				
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			    DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			    
			    String start =  LocalDate.parse(leaveDTO.getFromDate(), formatter).format(formatter2);
			    String end =  LocalDate.parse(leaveDTO.getToDate(), formatter).format(formatter2);

				 // DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//				  LocalDate start = LocalDate.parse(leaveDTO.getFromDate(),df);
//				  LocalDate end = LocalDate.parse(leaveDTO.getToDate(),df);



//		        LocalDate start = LocalDate.parse(leaveDTO.getFromDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//		        LocalDate end = LocalDate.parse(leaveDTO.getToDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

				
				List<Timesheet> empTimeSheet = timesheetsRepository.findTimesheetOnLeaveDateOLD(leaveDTO.getEmpId(),start,end);

				if (empTimeSheet != null) {

					empTimeSheet.forEach((timesheet)->{
						timesheetsRepository.deleteById(timesheet.getTimesheetId());
					});
				}

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
			Integer difference = 0;
			
			if (leaveObject.isPresent()) {
				EmployeeLeave leaveToBeUpdated = leaveObject.get();
				
				Optional<LeaveTypeMaster> leavetype = leaveTypeMasterRepository.findById(leaveToBeUpdated.getLeaveTypeMasterId());
				
				List<Object[]> empObj = employeeRepository.getManagerEmail(leaveToBeUpdated.getEmpId());
				EmployeeDTO empDto = new EmployeeDTO();
					empObj.forEach((object) -> {
						
						empDto.setEmail(object[0] != null ? object[0].toString() : null);
						empDto.setManagerEmail(object[1] != null ? object[1].toString() : null);
						empDto.setName(object[2] != null ? object[2].toString() : null);
						empDto.setEmployeementId(object[3] != null ? Long.parseLong(object[3].toString()): null);
						empDto.setManagerName(object[4] != null ? object[4].toString() : null);
					});
				
				EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository
						.findByEmpIdAndLeaveTypeMasterId(leaveToBeUpdated.getEmpId(), leaveToBeUpdated.getLeaveTypeMasterId());
				
				if(employeeLeavesMap != null) {
					
					//Check if fromDate & toDate are same
					if(!leaveDTO.getFromDate().equals(leaveToBeUpdated.getFromDate().toString())
							|| !leaveDTO.getToDate().equals(leaveToBeUpdated.getToDate().toString())) {
						
						// db : From date & toDate
						Period dbDateDifference = Period.between(leaveToBeUpdated.getFromDate(), leaveToBeUpdated.getToDate());
						
						// new : From date & toDate
						LocalDate fromDate = stringToDateTimeParser.getDate(leaveDTO.getFromDate(), "yyyy-MM-dd");
						LocalDate toDate = stringToDateTimeParser.getDate(leaveDTO.getToDate(), "yyyy-MM-dd");
						
						Period newDateDifference = Period.between(fromDate, toDate);
						
						difference = dbDateDifference.getDays() - newDateDifference.getDays();
					}
					
					// For CompOff
					Float availableCompOffBalance = 0.0F;
					if(leavetype.get().getLeaveTypeCode().equalsIgnoreCase("CO")) {
						ServiceResponse compOffResponse = compOffLeaveService.getCompOffBalanceDetailsByEmpIdAndFromDate(leaveDTO);
						
						if(compOffResponse.getServiceStatus().equals("Success")) {
							List<LeaveDTO> availableCompOffList = (List<LeaveDTO>)compOffResponse.getServiceResponse();
							
							for(LeaveDTO compOff: availableCompOffList){
								availableCompOffBalance = availableCompOffBalance + compOff.getNoOfDays();
							};
						}
					}
					
					// HERE : Effective Leave Balance = employeeLeavesMap.getBalance()
					if (!leavetype.get().getLeaveTypeCode().equalsIgnoreCase("LWP") && (employeeLeavesMap.getBalance() == 0
							|| employeeLeavesMap.getBalance() < difference)) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Your available balance of " + employeeLeavesMap.getBalance()
								+ " day(s) is not sufficient for this Leave Application.");

						return response;
					}else if(leavetype.get().getLeaveTypeCode().equalsIgnoreCase("CO") && availableCompOffBalance < difference) {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Your available Compensatory off balance of " + availableCompOffBalance
								+ " day(s) before "+ leaveDTO.getFromDate() +" is not sufficient for this Leave Application.");
						return response;
					}
					
					//Update Balance
						Float balance = employeeLeavesMap.getBalance();
						if (leavetype.get().getLeaveTypeCode().equalsIgnoreCase("LWP")) {
							balance = balance;
						} else {
							if(difference > 0) {
								balance = balance + difference;
							}else if(difference < 0) {
								balance = balance - difference;
							}
						}
						Float pendingForApproval = employeeLeavesMap.getPendingForApproval();
						if(difference > 0) {
							pendingForApproval = pendingForApproval - difference;
						}else if(difference < 0) {
							pendingForApproval = pendingForApproval + difference;
						}

						employeeLeavesMap.setBalance(balance);
						employeeLeavesMap.setPendingForApproval(pendingForApproval);

					leaveToBeUpdated.getCommonProperty().setUpdatedBy(leaveDTO.getUpdatedBy().longValue());
					leaveToBeUpdated.setFromDate(stringToDateTimeParser.getDate(leaveDTO.getFromDate(), "yyyy-MM-dd"));
					leaveToBeUpdated.setToDate(stringToDateTimeParser.getDate(leaveDTO.getToDate(), "yyyy-MM-dd"));
					leaveToBeUpdated.setReason(leaveDTO.getReason());
					leaveToBeUpdated.setNoOfDays(leaveDTO.getNoOfDays());
					leaveToBeUpdated.setLeaveStatusId((short) 1);
					
					/* ----------- Multi-Level Approval ---------- */
					// Approval Status : NA - Pending - Approved - Rejected
					leaveToBeUpdated.setFinalApprovalLevel(leaveDTO.getFinalApprovalLevel());
					
					// By Default Current Approval level will be 1 i.e. Manager Approval
					leaveToBeUpdated.setCurrentApprovalLevel(1);
					leaveToBeUpdated.setManagerId(leaveDTO.getManagerId());
					leaveToBeUpdated.setManagerApprovalStatus("Pending");
					
					if(leaveToBeUpdated.getFinalApprovalLevel() == 2) {
						leaveToBeUpdated.setLevel2ApproverId(leaveDTO.getLevel2ApproverId());
						leaveToBeUpdated.setLevel2ApprovalStatus("Pending");
						
						leaveToBeUpdated.setLevel3ApproverId(null);
						leaveToBeUpdated.setLevel3ApprovalStatus("NA");
						
					}else if(leaveToBeUpdated.getFinalApprovalLevel() == 3) {
						leaveToBeUpdated.setLevel2ApproverId(leaveDTO.getLevel2ApproverId());
						leaveToBeUpdated.setLevel2ApprovalStatus("Pending");
						
						leaveToBeUpdated.setLevel3ApproverId(leaveDTO.getLevel3ApproverId());
						leaveToBeUpdated.setLevel3ApprovalStatus("Pending");
					}else {
						leaveToBeUpdated.setLevel2ApproverId(null);
						leaveToBeUpdated.setLevel2ApprovalStatus("NA");
						
						leaveToBeUpdated.setLevel3ApproverId(null);
						leaveToBeUpdated.setLevel3ApprovalStatus("NA");
					}
					
					EmployeeLeave dbResponse = employeeLeaveRepository.save(leaveToBeUpdated);
					EmployeeLeavesMap dbResponse1 = employeeLeavesMapRepository.save(employeeLeavesMap);

					if (dbResponse != null && dbResponse1 != null) {

						if(difference != 0) {
							LeaveBalanceLog log = new LeaveBalanceLog();

							log.setBalance(balance);
							log.setEmpId(leaveToBeUpdated.getEmpId());
							log.setLeaveTypeMasterId(leaveToBeUpdated.getLeaveTypeMasterId());
							if(difference > 0) {
								log.setMessage(
										LeaveLogMessage.requestUpdateLeaveAdd.replace("0.0", difference.toString()));
								log.setUpdateBalanceBy("+" + difference);
							}else if(difference < 0) {
								log.setMessage(
										LeaveLogMessage.requestUpdateLeaveSub.replace("0.0", difference.toString()));
								log.setUpdateBalanceBy("-" + difference);
							}

							LeaveBalanceLog dbLogResponse =  leaveBalanceLogRepository.save(log);
						}
						
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Leave Application Updated.");
						
						apiLogInfo.setApiResponse("Leave Application Updated.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						
						//Send Mail
						String managerEmail = "";
						if(!leaveDTO.getApproverEmail().equals(empDto.getManagerEmail())) {
							managerEmail = ","+ empDto.getManagerEmail();
						}
						
						if(leaveToBeUpdated.getFinalApprovalLevel() == 2) {
							managerEmail = ","+ leaveDTO.getLevel2ApproverEmail();
							
						}else if(leaveToBeUpdated.getFinalApprovalLevel() == 3) {
							managerEmail = ","+ leaveDTO.getLevel2ApproverEmail() + ","+ leaveDTO.getLevel3ApproverEmail();
						}

						
						
						if(leaveDTO.getUpdatedBy().equals(leaveDTO.getEmpId())){
							//Leave Applied for self
							
							mailService.sendMailWithCC(leaveDTO.getApproverEmail(), hrMailAddress +","+ leaveDTO.getEmail()+ managerEmail,
									"Regarding Leave Application Update",
									"Dear "+ leaveDTO.getApproverName() + ","+"<br>"
									+"<br>"+" &nbsp"+" &nbsp"+" "+"Leave Application has been updated by "+ leaveDTO.getName() +" "+"for "+leaveDTO.getNoOfDays()+" day(s), Please take necessary action."+
									"<br>"+"<br>"+"<b>"+"Leave Details"+"<b>"+
									"<br>"+
									"EmpID :"+" "+ leaveDTO.getEmployeementId()+
									"<br>"+
									"Name :"+" "+ leaveDTO.getName()+
									"<br>"+
									" From :"+" "+ leaveDTO.getFromDate()+
									"<br>"+
									" To :"+" "+ leaveDTO.getToDate() +
									"<br>"+
									" No. Of Days : "+ leaveDTO.getNoOfDays() + " day(s)" 
									+"<br>"+
									" Leave Type :"+" "+leavetype.get().getLeaveType()+
									"<br>"+
									"leave Reason :"+" "+leaveDTO.getReason());
							
						}else {
							//Leave Applied for team
							Optional<Employee> updatedByEmp = employeeRepository.findById(Long.parseLong(leaveDTO.getUpdatedBy().toString()));
							if(!updatedByEmp.isEmpty()) {
								Employee updatedByObj = updatedByEmp.get();
								
								mailService.sendMailWithCC(leaveDTO.getApproverEmail(), hrMailAddress +","+ leaveDTO.getEmail()+ managerEmail,
										"Regarding Leave Application Update",
										"Dear "+ leaveDTO.getApproverName() + ","+"<br>"
										+"<br>"+" &nbsp"+" &nbsp"+" "+"Leave Application has been updated for "+ leaveDTO.getName() +" "+"for "+leaveDTO.getNoOfDays()+" day(s) by "+ updatedByObj.getName() + ", Please take necessary action."+
										"<br>"+"<br>"+"<b>"+"Leave Details"+"<b>"+
										"<br>"+
										"EmpID :"+" "+ leaveDTO.getEmployeementId()+
										"<br>"+
										"Name :"+" "+ leaveDTO.getName()+
										"<br>"+
										" From :"+" "+ leaveDTO.getFromDate()+
										"<br>"+
										" To :"+" "+ leaveDTO.getToDate() +
										"<br>"+
										" No. Of Days : "+ leaveDTO.getNoOfDays() + " day(s)" 
										+"<br>"+
										" Leave Type :"+" "+leavetype.get().getLeaveType()+
										"<br>"+
										"leave Reason :"+" "+leaveDTO.getReason());
							}
						}
						
						cronJobService.sendHrDepartmentNotificationUpdateCase(leaveDTO, leavetype);
						
						//Timesheet Update
						// IF Employee is Applying Leave for Half Day then, Automatic timesheet will not be filled as Leave
						if(leaveDTO.getNoOfDays() > 0.5) {
							LocalDate fromDate = LocalDate.parse(leaveDTO.getFromDate());
							LocalDate toDate = LocalDate.parse( leaveDTO.getToDate());

							long elapsedDays = ChronoUnit.DAYS.between(fromDate,toDate);
							
							List<Object[]> holidayList = holidayRepository.getHolidayWeekOffSize(leaveDTO.getFromDate(), leaveDTO.getToDate(), leaveDTO.getState());
							
						    List<Timesheet> empTimeSheet = timesheetsRepository.findTimesheetOnLeaveDateOLD(leaveDTO.getEmpId(),leaveDTO.getFromDate(),leaveDTO.getToDate());
						    if(!empTimeSheet.isEmpty()) {
						    empTimeSheet.forEach((timesheet)->{
						     	
						    	List<TimesheetActivityMap> timesheetactivities = timesheetActivityRepository.getTimesheetActivityByTimesheetId(timesheet.getTimesheetId());
						    	
						    	timesheetactivities.forEach((timesheetactivity)->{
						    		
						    		timesheetActivityRepository.deleteById(timesheetactivity.getTimesheetActivityMapId());

						    	});
						    	
						    	if(!timesheet.getDayType().equals("Public Holiday") && !timesheet.getDayType().equals("Week Off")) {
						    		timesheetsRepository.deleteById(timesheet.getTimesheetId());				    		
						    	}
						    });
						    }
						    //after timesheet deletion
						    List<Timesheet> empTimeSheetAfterDelete = timesheetsRepository.findTimesheetOnLeaveDateOLD(leaveDTO.getEmpId(),leaveDTO.getFromDate(),leaveDTO.getToDate());

							if((elapsedDays == 0)) {
								
								if(holidayList.isEmpty()) {
									Timesheet newTimesheet = new Timesheet();
									
									newTimesheet.getCommonProperty().setCreatedBy(leaveDTO.getCreatedBy());
									newTimesheet.setDate(fromDate);
									newTimesheet.setDayType("Leave");
									newTimesheet.setDescription("On leave");
									newTimesheet.setEmpId(leaveDTO.getEmpId());
									newTimesheet.setStatus("Approved");
									newTimesheet.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());

									timesheetsRepository.save(newTimesheet);
								}
							}
							if((elapsedDays != 0)) {
								LocalDate tempDateToday = fromDate;

								while(tempDateToday.compareTo(toDate) != 1) {
									
									if(tempDateToday.isEqual(fromDate) && leaveDTO.getFromDateDayType() == 0.5) {
										System.out.println("From Date is Half Day");
									}else if(tempDateToday.isEqual(toDate) && leaveDTO.getToDateDayType() == 0.5) {
										System.out.println("To Date is Half Day");
									}else {
										boolean isHoliday = false;
										
										if(leaveDTO.getIsWeekOffsExcluded().equals("false")) {
											if(!holidayList.isEmpty()) {
												for(Object[] holiday: holidayList) {
													if(holiday[1].toString() != null) {
														LocalDate holidayDate = LocalDate.parse(holiday[1].toString());
														
														if(tempDateToday.isEqual(holidayDate)){
															isHoliday = true;
															break;
														}
													}
												}
											}	
										}
										
										System.out.println("check Date : "+ tempDateToday.toString() +", isHoliday : "+ isHoliday);
										
										if(!isHoliday) {
											Timesheet newTimesheet = new Timesheet();

											newTimesheet.getCommonProperty().setCreatedBy(leaveDTO.getCreatedBy());
											newTimesheet.setDate(tempDateToday);
											newTimesheet.setDayType("Leave");
											newTimesheet.setDescription("On leave");
											newTimesheet.setEmpId(leaveDTO.getEmpId());
											newTimesheet.setStatus("Approved");
											newTimesheet.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());

											timesheetsRepository.save(newTimesheet);
										}
									}
									
									tempDateToday = tempDateToday.plusDays(1);
									
								}
							}
						}
					} else {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Leave Application Updation Failed.");
						
						apiLogInfo.setApiResponse("Leave Application Updation Failed.");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
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
			System.err.println(" listvvvv     list    list   "+list.toString());
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
					dto.setFromDateDayType(object[11] != null ? Float.parseFloat(object[11].toString()) : null);
					dto.setToDateDayType(object[12] != null ? Float.parseFloat(object[12].toString()) : null);
					dto.setApproverName(object[13] != null ? object[13].toString() : null);
					dto.setApproverEmail(object[14] != null ? object[14].toString() : null);

					dto.setManagerApprovalStatus(object[15] != null ? object[15].toString() : null);
					dto.setLevel2ApproverId(object[16] != null ? Long.parseLong(object[16].toString()) : null);
					dto.setLevel2ApproverName(object[17] != null ? object[17].toString() : null);
					dto.setLevel2ApproverEmail(object[18] != null ? object[18].toString() : null);
					dto.setLevel2ApprovalStatus(object[19] != null ? object[19].toString() : null);
					
					dto.setLevel3ApproverId(object[20] != null ? Long.parseLong(object[20].toString()) : null);
					dto.setLevel3ApproverName(object[21] != null ? object[21].toString() : null);
					dto.setLevel3ApprovalStatus(object[22] != null ? object[22].toString() : null);
					dto.setLevel3ApproverEmail(object[23] != null ? object[23].toString() : null);
					
					dto.setCurrentApprovalLevel(object[24] != null ? Integer.parseInt(object[24].toString()) : null);
					dto.setFinalApprovalLevel(object[25] != null ? Integer.parseInt(object[25].toString()) : null);
					dto.setMaternityType(object[26] != null ? object[26].toString() : null);
					dto.setMaternityLeaveDays(object[27] != null ? Long.parseLong(object[27].toString()) : null);
					
					dto.setEmployeeName(object[28] != null ? object[28].toString() : null);
					dto.setEmpId(object[29] != null ? Long.parseLong(object[29].toString()) : null);
					dto.setApproverEmpId(object[30] != null ? Long.parseLong(object[30].toString()) : null);			
					
					
					
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

	public ServiceResponse getAllMyTeamsPendingLeaveApplicationsByManagerId1(LeaveDTO leaveDTO) {
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
					
					dto.setApproverName(object[14] != null ? object[14].toString() : null);
					//for approver level 1 empid emp 360
					dto.setLevel1ApproverId(object[15] != null ? Long.parseLong(object[15].toString()) : null);
					dto.setApproverEmail(object[16] != null ? object[16].toString() : null);
					dto.setManagerApprovalStatus(object[17] != null ? object[17].toString() : null);
					dto.setLevel2ApproverId(object[18] != null ? Long.parseLong(object[18].toString()) : null);
					dto.setLevel2ApproverName(object[19] != null ? object[19].toString() : null);
					dto.setLevel2ApproverEmail(object[20] != null ? object[20].toString() : null);
					dto.setLevel2ApprovalStatus(object[21] != null ? object[21].toString() : null);

					dto.setLevel3ApproverId(object[22] != null ? Long.parseLong(object[22].toString()) : null);
					dto.setLevel3ApproverName(object[23] != null ? object[23].toString() : null);
					dto.setLevel3ApprovalStatus(object[24] != null ? object[24].toString() : null);
					dto.setLevel3ApproverEmail(object[25] != null ? object[25].toString() : null);

					dto.setCurrentApprovalLevel(object[26] != null ? Integer.parseInt(object[26].toString()) : null);
					dto.setFinalApprovalLevel(object[27] != null ? Integer.parseInt(object[27].toString()) : null);
					dto.setLeaveEmpId(object[28] != null ? Long.parseLong(object[28].toString()) : null);
					dto.setManagerId(object[29] != null ? Integer.parseInt(object[29].toString()) : null);
					dto.setClientName(object[30] != null ? object[30].toString() : null);
					dto.setTeamName(object[31] != null ? object[31].toString() : null);
					
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
	
	private LeaveDTO mapPendingLeaveObjectToDTO(Object[] object) {
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
	    dto.setApproverName(object[14] != null ? object[14].toString() : null);
	    dto.setLevel1ApproverId(object[15] != null ? Long.parseLong(object[15].toString()) : null);
	    dto.setApproverEmail(object[16] != null ? object[16].toString() : null);
	    dto.setManagerApprovalStatus(object[17] != null ? object[17].toString() : null);
	    dto.setLevel2ApproverId(object[18] != null ? Long.parseLong(object[18].toString()) : null);
	    dto.setLevel2ApproverName(object[19] != null ? object[19].toString() : null);
	    dto.setLevel2ApproverEmail(object[20] != null ? object[20].toString() : null);
	    dto.setLevel2ApprovalStatus(object[21] != null ? object[21].toString() : null);
	    dto.setLevel3ApproverId(object[22] != null ? Long.parseLong(object[22].toString()) : null);
	    dto.setLevel3ApproverName(object[23] != null ? object[23].toString() : null);
	    dto.setLevel3ApprovalStatus(object[24] != null ? object[24].toString() : null);
	    dto.setLevel3ApproverEmail(object[25] != null ? object[25].toString() : null);
	    dto.setCurrentApprovalLevel(object[26] != null ? Integer.parseInt(object[26].toString()) : null);
	    dto.setFinalApprovalLevel(object[27] != null ? Integer.parseInt(object[27].toString()) : null);
	    dto.setLeaveEmpId(object[28] != null ? Long.parseLong(object[28].toString()) : null);
	    dto.setManagerId(object[29] != null ? Integer.parseInt(object[29].toString()) : null);
	    dto.setClientName(object[30] != null ? object[30].toString() : null);
	    dto.setTeamName(object[31] != null ? object[31].toString() : null);
	    return dto;
	}

	private List<LeaveDTO> getPendingLeavesRecursively(Integer managerId) {
	    List<LeaveDTO> result = new ArrayList<>();

	    List<Object[]> list = employeeLeaveRepository
	            .getAllMyTeamsPendingLeaveApplicationsByManagerId(managerId);

	    for (Object[] obj : list) {
	        LeaveDTO dto = mapPendingLeaveObjectToDTO(obj);
	        result.add(dto);

	        // recursive call for each employee's team
	        Integer empId = dto.getEmpId().intValue();
	        if (empId != null) {
	            List<LeaveDTO> subLeaves = getPendingLeavesRecursively(empId);
	            result.addAll(subLeaves);
	        }
	    }
	    return result;
	}

	public ServiceResponse getAllMyTeamsPendingLeaveApplicationsByManagerId(LeaveDTO leaveDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getAllMyTeamsPendingLeaveApplicationsByManagerId");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("ManagerId : " + leaveDTO.getManagerId());

	    try {
	        List<LeaveDTO> dtoList;

	        if (Boolean.TRUE.equals(leaveDTO.getIsHierarchyView())) {
	            // dtoList = getPendingLeavesRecursively(leaveDTO.getManagerId());
				List<Long> empIds = empCache.getEmployeesUnderAnyLeadingPerson((leaveDTO.getManagerId()).longValue());

				List<Object[]> list = employeeLeaveRepository
	                    .getAllMyTeamsPendingLeaveApplicationsByManagerIdInHirarchy(empIds,leaveDTO.getManagerId());
	            dtoList = list.stream()
	                          .map(this::mapPendingLeaveObjectToDTO)
	                          .collect(Collectors.toList());
	        } else {
	            List<Object[]> list = employeeLeaveRepository
	                    .getAllMyTeamsPendingLeaveApplicationsByManagerId(leaveDTO.getManagerId());
	            dtoList = list.stream()
	                          .map(this::mapPendingLeaveObjectToDTO)
	                          .collect(Collectors.toList());
	        }

	        if (dtoList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No Leave Application found");
	            apiLogInfo.setApiResponse("No Leave Application found");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        } else {
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

	
	public ServiceResponse getAllLeaveApplicationsByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllLeaveApplicationsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Employee Emp Id : "+leaveDTO.getEmpId());
		
		try {
			List<Object[]> list = employeeLeaveRepository
					.getAllLeaveApplicationsByEmpId(leaveDTO.getEmpId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leave Application found");

				apiLogInfo.setApiResponse("No Leave Application found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {

				list.forEach((object) -> {LeaveDTO dto = new LeaveDTO();
				dto.setCreatedByName(object[24] != null ? object[24].toString() : null);
				dto.setFromDate(object[1] != null ? object[1].toString() : null);
				dto.setToDate(object[2] != null ? object[2].toString() : null);
				dto.setCreatedOn(object[3] != null ? object[3].toString() : null);
				dto.setNoOfDays(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
				dto.setStatus(object[5] != null ? object[5].toString() : null);
				dto.setReason(object[6] != null ? object[6].toString() : null);
				dto.setLeaveType(object[7] != null ? object[7].toString() : null);
				dto.setLeaveStatusUpdatedByName(object[8] != null ? object[8].toString() : null);
				dto.setLeaveId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
				dto.setRemark(object[10] != null ? object[10].toString() : null);dto.setApproverName(object[11] != null ? object[11].toString() : null);
				dto.setApproverEmail(object[12] != null ? object[12].toString() : null);
				dto.setManagerApprovalStatus(object[13] != null ? object[13].toString() : null);
				dto.setLevel2ApproverId(object[14] != null ? Long.parseLong(object[14].toString()) : null);
				dto.setLevel2ApproverName(object[15] != null ? object[15].toString() : null);
				dto.setLevel2ApproverEmail(object[16] != null ? object[16].toString() : null);
				dto.setLevel2ApprovalStatus(object[17] != null ? object[17].toString() : null);
				dto.setLevel3ApproverId(object[18] != null ? Long.parseLong(object[18].toString()) : null);
				dto.setLevel3ApproverName(object[19] != null ? object[19].toString() : null);
				dto.setLevel3ApprovalStatus(object[20] != null ? object[20].toString() : null);
				dto.setLevel3ApproverEmail(object[21] != null ? object[21].toString() : null);
				dto.setCurrentApprovalLevel(object[22] != null ? Integer.parseInt(object[22].toString()) : null);
				dto.setFinalApprovalLevel(object[23] != null ? Integer.parseInt(object[23].toString()) : null);
				dto.setEmployeeName(object[0] != null ? object[0].toString() : null);
				dto.setLeaveTypeMasterId(object[27] != null ? Short.parseShort(object[27].toString()) : null);
				dto.setEmpId(object[28] != null ? Long.parseLong(object[28].toString()) : null);
				dto.setEmployeementId(object[26] != null ? Long.parseLong(object[26].toString()) : null);
				dto.setLeaveEmpId(object[25] != null ? Long.parseLong(object[25].toString()) : null);
				dto.setManagerId(object[29] != null ? Integer.parseInt(object[29].toString()) : null);
				
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
	
	public ServiceResponse getAllLeaveApplicationsByTeamId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllLeaveApplicationsByTeamId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Employee Emp Id : "+leaveDTO.getEmpId());
		
		try {
			
			List<Long> teamIds = employeeLeaveRepository.findTeamIdsByEmpId( leaveDTO.getEmpId());

			if (teamIds.isEmpty()) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No teams found for the given employee.");
                apiLogInfo.setApiResponse("No teams found.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                return response;
            }

			List<Object[]> list = employeeLeaveRepository.getAllLeaveApplicationsByTeamId(teamIds);
			
			System.out.println ("team ids:"+ list);	        
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			if (list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leave Application found");

				apiLogInfo.setApiResponse("No Leave Application found");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {

				list.forEach((object) -> {LeaveDTO dto = new LeaveDTO();
				dto.setCreatedByName(object[0] != null ? object[0].toString() : null);
				dto.setFromDate(object[1] != null ? object[1].toString() : null);
				dto.setToDate(object[2] != null ? object[2].toString() : null);
				dto.setCreatedOn(object[3] != null ? object[3].toString() : null);
				dto.setNoOfDays(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
				dto.setStatus(object[5] != null ? object[5].toString() : null);
				dto.setReason(object[6] != null ? object[6].toString() : null);
				dto.setLeaveType(object[7] != null ? object[7].toString() : null);
				dto.setLeaveStatusUpdatedByName(object[8] != null ? object[8].toString() : null);
				dto.setLeaveId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
				dto.setRemark(object[10] != null ? object[10].toString() : null);dto.setApproverName(object[11] != null ? object[11].toString() : null);
				dto.setApproverEmail(object[12] != null ? object[12].toString() : null);
				dto.setManagerApprovalStatus(object[13] != null ? object[13].toString() : null);
				dto.setLevel2ApproverId(object[14] != null ? Long.parseLong(object[14].toString()) : null);
				dto.setLevel2ApproverName(object[15] != null ? object[15].toString() : null);
				dto.setLevel2ApproverEmail(object[16] != null ? object[16].toString() : null);
				dto.setLevel2ApprovalStatus(object[17] != null ? object[17].toString() : null);
				dto.setLevel3ApproverId(object[18] != null ? Long.parseLong(object[18].toString()) : null);
				dto.setLevel3ApproverName(object[19] != null ? object[19].toString() : null);
				dto.setLevel3ApprovalStatus(object[20] != null ? object[20].toString() : null);
				dto.setLevel3ApproverEmail(object[21] != null ? object[21].toString() : null);
				dto.setCurrentApprovalLevel(object[22] != null ? Integer.parseInt(object[22].toString()) : null);
				dto.setFinalApprovalLevel(object[23] != null ? Integer.parseInt(object[23].toString()) : null);
				
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
        logBuilder.append("LeaveId : " + leaveDTO.getLeaveId() + ", EmpId : " + leaveDTO.getEmpId() + ", LeaveTypeMasterId : " + leaveDTO.getLeaveTypeMasterId() + ", LeaveStatusId : " + leaveDTO.getLeaveStatusId() + ", LeaveStatusUpdatedBy : " + leaveDTO.getLeaveStatusUpdatedBy());

        try {
            Optional<EmployeeLeave> leaveApplication = employeeLeaveRepository.findById(leaveDTO.getLeaveId());
            EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository
                    .findByEmpIdAndLeaveTypeMasterId(leaveDTO.getLeaveEmpId(), leaveDTO.getLeaveTypeMasterId());
            Optional<Employee> employee = employeeRepository.findById(leaveDTO.getEmpId());

            if (leaveApplication.isPresent()) {
                EmployeeLeave pendingLeaveApplication = leaveApplication.get();

                pendingLeaveApplication.setLeaveStatusUpdatedBy(leaveDTO.getLeaveStatusUpdatedBy());
                pendingLeaveApplication.getCommonProperty().setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());


                // 1 = pending , 2 = Approved , 3= Rejected
                if (leaveDTO.getLeaveStatusId() == 2) {

                    if (leaveDTO.getCurrentApprovalLevel() == 2) {
                        pendingLeaveApplication.setLevel2ApprovalStatus("Approved");
                        pendingLeaveApplication.setCurrentApprovalLevel(3);
                    } else if (leaveDTO.getCurrentApprovalLevel() == 3) {
                        pendingLeaveApplication.setLevel3ApprovalStatus("Approved");
                    } else {
                        pendingLeaveApplication.setManagerApprovalStatus("Approved");
                        pendingLeaveApplication.setCurrentApprovalLevel(2);
                    }

                    // Final Approval
                    if ((leaveDTO.getCurrentApprovalLevel() == null && leaveDTO.getFinalApprovalLevel() == null) ||
                            (leaveDTO.getCurrentApprovalLevel() != null && leaveDTO.getFinalApprovalLevel() != null && leaveDTO.getCurrentApprovalLevel() == leaveDTO.getFinalApprovalLevel())) {
                        employeeLeavesMap.setPendingForApproval(
                                employeeLeavesMap.getPendingForApproval() - pendingLeaveApplication.getNoOfDays());

                        pendingLeaveApplication.setLeaveStatusId((short) 2);

                        // Increase Notice period If employee resigned
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

                        //send Approval Mail
                        if (!employee.isEmpty()) {
                            Employee empObj = employee.get();
                            Optional<Employee> approver = employeeRepository.findById(Long.parseLong(pendingLeaveApplication.getManagerId().toString()));
                            Optional<Employee> reportingManager = employeeRepository.findById(empObj.getManagerId());

                            String managerEmail = "";
                            if (!leaveDTO.getApproverEmail().equals(reportingManager.get().getEmail())) {
                                managerEmail = "," + reportingManager.get().getEmail();
                            }

                            // Level 2/3 Approver Email
                            if (leaveDTO.getFinalApprovalLevel() == 2) {
                                managerEmail = "," + leaveDTO.getLevel2ApproverEmail();

                            } else if (leaveDTO.getFinalApprovalLevel() == 3) {
                                managerEmail = "," + leaveDTO.getLevel2ApproverEmail() + "," + leaveDTO.getLevel3ApproverEmail();
                            }

                            if (!approver.isEmpty()) {
                                Employee approverObj = approver.get();
                                mailService.sendMailWithCC(empObj.getEmail(), hrMailAddress + "," + approverObj.getEmail() + managerEmail,
                                        "Regarding leave Approval",
                                        "Dear " + empObj.getName() + ","
                                                + " <br> " + "Your leave request from" + "&nbsp;" + leaveDTO.getFromDate() + " to " + leaveDTO.getToDate() + " has been approved"
                                                + "<br><br> Leave Application Details :"
                                                + "<br> From Date : " + leaveDTO.getFromDate() + "   To Date : " + leaveDTO.getToDate()
                                                + "<br> No. Of Days : " + leaveDTO.getNoOfDays()
                                                + "<br> Leave Type : " + leaveDTO.getLeaveType()
                                                + "<br> Final Approval Status : Approved");
                            }
                        }

//						mailService.sendMail(leaveDTO.getEmail(),
//								"Regarding leave Approval ",
//						"Dear "+leaveDTO.getEmployeeName()+","+
//						" <br> "+
//						" <br> "+ "Your leave request from"+"&nbsp;"+ leaveDTO.getFromDate()+" to "+leaveDTO.getToDate() + " has been approved");


                        // CompOff Leave : 4 (LeaveTypeMasterId)
                        if (leaveTypeObj.getLeaveTypeCode().equals("CO")) {

                            List<CompOffLeave> compOffLeave = compOffLeaveRepository.findByLeaveId(pendingLeaveApplication.getLeaveId());

                            if (!compOffLeave.isEmpty()) {
                                compOffLeave.forEach((leave) -> {
                                    leave.setCompOffStatus("Availed");
                                    compOffLeaveRepository.save(leave);
                                });
                            }
                        }

                    } else {
                        // Intermediate Approval i.e. Level 1 OR 2

                        // Next and Current Approval Email
                        String nextApproverName = null;
                        String nextApproverEmail = null;

                        String currentApproverName = null;
                        String currentApproverEmail = null;

                        if (leaveDTO.getCurrentApprovalLevel() == 2 && leaveDTO.getFinalApprovalLevel() != 2) {
                            nextApproverEmail = leaveDTO.getLevel3ApproverEmail();
                            nextApproverName = leaveDTO.getLevel3ApproverName();

                            currentApproverEmail = leaveDTO.getLevel2ApproverEmail();
                            currentApproverName = leaveDTO.getLevel2ApproverName();

                        } else {
                            nextApproverEmail = leaveDTO.getLevel2ApproverEmail();
                            nextApproverName = leaveDTO.getLevel2ApproverName();

                            currentApproverEmail = leaveDTO.getApproverEmail();
                            currentApproverName = leaveDTO.getApproverName();
                        }


                        response.setServiceResponse("Leave application approved.");
                        apiLogInfo.setApiResponse("Leave application approved.");

                        //send Approval Mail
                        if (!employee.isEmpty()) {
                            Employee empObj = employee.get();
                            Optional<Employee> reportingManager = employeeRepository.findById(empObj.getManagerId());

                            String managerEmail = "";
                            if (!leaveDTO.getApproverEmail().equals(reportingManager.get().getEmail())) {
                                managerEmail = "," + reportingManager.get().getEmail();
                            }

                            // Level 2/3 Approver Email
                            if (leaveDTO.getFinalApprovalLevel() == 2) {
                                managerEmail = "," + leaveDTO.getLevel2ApproverEmail();

                            } else if (leaveDTO.getFinalApprovalLevel() == 3) {
                                managerEmail = "," + leaveDTO.getLevel2ApproverEmail() + "," + leaveDTO.getLevel3ApproverEmail();
                            }

                            if (nextApproverName != null && currentApproverName != null) {
                                mailService.sendMailWithCC(empObj.getEmail(), hrMailAddress + "," + currentApproverEmail + managerEmail,
                                        "Regarding leave Approval",
                                        "Dear " + empObj.getName() + ","
                                                + " <br> " + "Your leave request from" + "&nbsp;" + leaveDTO.getFromDate() + " to " + leaveDTO.getToDate() + " has been approved By " + currentApproverName + "."
                                                + " <br> " + "Next Approval will be done by" + "&nbsp;" + nextApproverName + "."
                                                + "<br><br> Leave Application Details :"
                                                + "<br> From Date : " + leaveDTO.getFromDate() + "   To Date : " + leaveDTO.getToDate()
                                                + "<br> No. Of Days : " + leaveDTO.getNoOfDays()
                                                + "<br> Leave Type : " + leaveDTO.getLeaveType()
                                                + "<br> Final Approval Status : Pending");
                            }
                        }

                    }

                } else if (leaveDTO.getLeaveStatusId() == 3) {

                    pendingLeaveApplication.setLeaveStatusId((short) 3);

                    if (leaveDTO.getCurrentApprovalLevel() == 2) {
                        pendingLeaveApplication.setLevel2ApprovalStatus("Rejected");
                        pendingLeaveApplication.setLevel3ApprovalStatus("NA");
                    } else if (leaveDTO.getCurrentApprovalLevel() == 3) {
                        pendingLeaveApplication.setLevel3ApprovalStatus("Rejected");
                    } else {
                        pendingLeaveApplication.setManagerApprovalStatus("Rejected");
                        pendingLeaveApplication.setLevel2ApprovalStatus("NA");
                        pendingLeaveApplication.setLevel3ApprovalStatus("NA");
                    }

                    //Get Expiration Period of CompOff
                    Integer expirationPeriod = null;
                    boolean isExpirationValid = false;
                    Optional<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findById(leaveDTO.getLeaveTypeMasterId());
                    LeaveTypeMaster leaveTypeObj = leaveType.get();
                    if (leaveTypeObj.getLeaveTypeCode().equals("CO")) {
                        LeavePolicyMaster leavePolicy = leavePolicyMasterRepository.findByLeaveTypeMasterIdAndEmploymentStatus(leaveTypeObj.getLeaveTypeMasterId(), employee.get().getEmploymentstatus());
                        if (leavePolicy != null) {
                            if (leavePolicy.getExpirationPeriod().equals("Yes")) {
                                isExpirationValid = true;
                                expirationPeriod = leavePolicy.getExpirationPeriodValue();
                            }
                        }
                    }

                    if (!leaveTypeObj.getLeaveTypeCode().equals("CO")) {
                        pendingLeaveApplication.setRemark(leaveDTO.getRejectReason());

                        Float balance = employeeLeavesMap.getBalance();
                        if (leaveTypeObj.getLeaveTypeCode().equalsIgnoreCase("LWP")) {
                            balance = 0F;
                        } else {
                            balance = balance + pendingLeaveApplication.getNoOfDays();
                            System.err.println("check balance :: " + balance);
                        }

                        employeeLeavesMap.setBalance(balance);


                        LeaveBalanceLog log = new LeaveBalanceLog();
                        log.setBalance(employeeLeavesMap.getBalance());
                        log.setEmpId(leaveDTO.getEmpId());
                        log.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
                        if (leaveTypeObj.getLeaveTypeCode().equalsIgnoreCase("LWP")) {
                            log.setMessage(LeaveLogMessage.requestAddLeave);
                        } else {
                            log.setMessage(LeaveLogMessage.requestAddLeave.replace("0.0", pendingLeaveApplication.getNoOfDays().toString()));
                        }
                        if (!leaveDTO.getLeaveTypeMasterId().equals((short) 3))
                            log.setUpdateBalanceBy("+" + pendingLeaveApplication.getNoOfDays());
                        else
                            log.setUpdateBalanceBy("0");
                        leaveBalanceLogRepository.save(log);
                        response.setServiceResponse("Leave application rejected.");
                        apiLogInfo.setApiResponse("Leave application rejected.");
                    }

                    Optional<Employee> employeeInfo = employeeRepository.findById(leaveDTO.getEmpId());

                    //send reject Mail
                    if (!employeeInfo.isEmpty()) {
                        Employee empObj = employeeInfo.get();
                        Optional<Employee> approver = employeeRepository.findById(Long.parseLong(pendingLeaveApplication.getManagerId().toString()));
                        Optional<Employee> reportingManager = employeeRepository.findById(empObj.getManagerId());

                        String managerEmail = "";
                        if (!leaveDTO.getApproverEmail().equals(reportingManager.get().getEmail())) {
                            managerEmail = "," + reportingManager.get().getEmail();
                        }

                        // Level 2/3 Approver Email
                        if (leaveDTO.getFinalApprovalLevel() == 2) {
                            managerEmail = "," + leaveDTO.getLevel2ApproverEmail();

                        } else if (leaveDTO.getFinalApprovalLevel() == 3) {
                            managerEmail = "," + leaveDTO.getLevel2ApproverEmail() + "," + leaveDTO.getLevel3ApproverEmail();
                        }

                        if (!approver.isEmpty()) {
                            Employee approverObj = approver.get();
                            mailService.sendMailWithCC(empObj.getEmail(), hrMailAddress + "," + approverObj.getEmail() + managerEmail,
                                    "Regarding leave Rejection",
                                    "Dear " + empObj.getName() + ","
                                            + " <br> " + "Your leave request from" + "&nbsp;" + leaveDTO.getFromDate() + " to " + leaveDTO.getToDate() + " has been rejected"
                                            + "<br><br> Leave Application Details :"
                                            + "<br> From Date : " + leaveDTO.getFromDate() + "   To Date : " + leaveDTO.getToDate()
                                            + "<br> No. Of Days : " + leaveDTO.getNoOfDays()
                                            + "<br> Leave Type : " + leaveDTO.getLeaveType()
                                            + "<br>" + " Reason -: " + leaveDTO.getRejectReason());
                        }
                    }


//					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//
//					String start = LocalDate.parse(leaveDTO.getFromDate(), formatter)
//					                         .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//					String end = LocalDate.parse(leaveDTO.getToDate(), formatter)
//					                       .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    LocalDate startDate;
                    LocalDate endDate;

                    if (leaveDTO.getFromDate().matches("\\d{4}-\\d{2}-\\d{2}")) {
                        startDate = LocalDate.parse(leaveDTO.getFromDate());
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                        startDate = LocalDate.parse(leaveDTO.getFromDate(), formatter);
                    }

                    if (leaveDTO.getToDate().matches("\\d{4}-\\d{2}-\\d{2}")) {
                        endDate = LocalDate.parse(leaveDTO.getToDate());
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                        endDate = LocalDate.parse(leaveDTO.getToDate(), formatter);
                    }



                    List<Timesheet> empTimeSheet = timesheetsRepository.findTimesheetsForRejection(leaveDTO.getLeaveEmpId(), startDate, endDate);

                    if (empTimeSheet != null && !empTimeSheet.isEmpty()) {

                        empTimeSheet.forEach((timesheet) -> {
                            System.out.println("Deleting Timesheet ID: " + timesheet.getTimesheetId());
                            timesheetsRepository.deleteById(timesheet.getTimesheetId());
                        });

                    } else {
                        System.out.println("Result is NULL or Empty. No timesheets found to delete.");
                    }


//					mailService.sendMail(leaveDTO.getEmail(),
//							"Regarding leave Rejection ",
//					" <br> "+"Dear "+leaveDTO.getEmployeeName()+","+
//					" <br> "+ "   Your leave has been rejected  "+
//							" <br>"+" Reason -: "+leaveDTO.getRejectReason());



                    // CompOff Leave : 4 (LeaveTypeMasterId)
                    if (leaveTypeObj.getLeaveTypeCode().equals("CO")) {

                        List<CompOffLeave> compOffLeave = compOffLeaveRepository.findByLeaveId(pendingLeaveApplication.getLeaveId());

                        if (!compOffLeave.isEmpty()) {

                            for (CompOffLeave leave : compOffLeave) {

                                LocalDate expireDate = leave.getFromDate().plusDays(expirationPeriod);
                                if (LocalDate.now().isAfter(expireDate) || LocalDate.now().isEqual(expireDate)) {
                                    pendingLeaveApplication.setRemark(leaveDTO.getRejectReason());

                                    leave.setCompOffStatus("Expired");
                                    compOffLeaveRepository.save(leave);

                                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                                    response.setServiceResponse("CompOff Leave application rejected.");
                                    apiLogInfo.setApiResponse("CompOff Leave application rejected.");
                                } else {

                                    //Update Leave Balance
                                    pendingLeaveApplication.setRemark(leaveDTO.getRejectReason());
                                    employeeLeavesMap.setBalance(employeeLeavesMap.getBalance() + leave.getNoOfDays());

                                    LeaveBalanceLog log = new LeaveBalanceLog();
                                    log.setBalance(employeeLeavesMap.getBalance());
                                    log.setEmpId(leaveDTO.getEmpId());
                                    log.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
                                    log.setMessage(LeaveLogMessage.requestAddLeave.replace("0.0",
                                            leave.getNoOfDays().toString()));
                                    log.setUpdateBalanceBy("+" + leave.getNoOfDays());
                                    leaveBalanceLogRepository.save(log);

                                    // change compOff application status
                                    leave.setCompOffStatus("Pending");
                                    leave.setLeaveId(null);

                                    compOffLeaveRepository.save(leave);

                                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                                    response.setServiceResponse("CompOff Leave application rejected.");
                                    apiLogInfo.setApiResponse("CompOff Leave application rejected.");
                                }

                            }
                        }
                    }

                }
                EmployeeLeave updatedLeaveApplication = employeeLeaveRepository.save(pendingLeaveApplication);
                EmployeeLeavesMap updatedEmployeeLeavesMap = employeeLeavesMapRepository.save(employeeLeavesMap);

                System.err.println(updatedLeaveApplication);
                System.err.println(updatedEmployeeLeavesMap);


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
			
			Employee employee;
			if("Apmosys Product".equalsIgnoreCase(leaveDTO.getEmployeeType())){
				employee = employeeRepository.findByEmployeementIdForApmosysProduct(leaveDTO.getEmployeementId());	
			}else {
				employee = employeeRepository.findByEmployeementIdForOthers(leaveDTO.getEmployeementId());			
			}

//			Employee employee = employeeRepository.findByEmployeementId(leaveDTO.getEmployeementId());

			if (employee != null) {
				
				List<Object[]> employeeLeavesList;
				if(employee.getEmploymentstatus().equals("InActive")) {
					employeeLeavesList = employeeLeavesMapRepository.getInActiveEmployeeLeaveBalance(employee.getEmpId());	
				}else {
					employeeLeavesList = employeeLeavesMapRepository
							.getMyLeaveBalancesByEmpId(employee.getEmpId(), employee.getEmploymentstatus(), employee.getGender());					
				}
				
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
						empDto.setEmpId(employee.getEmpId());
						empDto.setManagerId(object[7] != null ? Integer.parseInt(object[7].toString()) : null);
						
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
							if(dto.getBalance() != 0) {
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
									System.err.println(" ANurag added balance   ::  "+dto.getBalance()+" leaveTypeMasterId  "+dto.getLeaveTypeMasterId());
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
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/addOldLeaveApplicationByList");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("");
		try {			
						
			Optional<Employee> EmpId = Optional.ofNullable(employeeRepository.findByEmployeementId(leaveDTO.getEmployeementId()));			
			if(EmpId.isPresent()) {			
							
				Employee employee = EmpId.get();			
				Long primaryEmpid = employee.getEmpId();			
				Long managerId = employee.getManagerId();			
				Long approverId;			
							
				if(leaveDTO.getLeaveStatusUpdatedByName() != null && leaveDTO.getLeaveStatusUpdatedByName() != "") {
					Employee approverName = employeeRepository.findByName(leaveDTO.getLeaveStatusUpdatedByName());			
					if(approverName == null) {			
						approverId = (long) 2;			
					}else {			
						approverId = approverName.getEmpId();			
					}	
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
						
//			leaveApplication.setHodId(approverId);			
//			leaveApplication.setLeaveStatusUpdatedBy(approverId);			
						
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
				apiLogInfo.setApiResponse("Application created");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {			
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);					
				response.setServiceResponse("Application creation Failed.");
				apiLogInfo.setApiResponse("Application creation Failed.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}							
			}			
						
		}catch (Exception e) {			
			e.printStackTrace();			
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);			
			response.setServiceResponse("Something Went Wrong.");			
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
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
			String applicationCount = employeeLeaveRepository
					.countAllMyTeamsPendingLeaveApplicationsByManagerId(leaveDTO.getManagerId());
			if ( applicationCount.length() == 0) {
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
						dto.setApplicationCount(object[0] != null ? (object[0].toString()) : null);
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

	public ServiceResponse countMyRejectedLeaveApplicationsByLeaveType(LeaveDTO leaveDTO) {
		
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> list = employeeLeaveRepository
					.countMyRejectedLeaveApplicationsByLeaveType(leaveDTO.getEmpId());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			
			Optional.ofNullable(list).ifPresentOrElse((employeeLeaveList)->{
				if (employeeLeaveList.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Application list is empty.Count is zero");
					
				}else {
					employeeLeaveList.forEach((object)->{
						LeaveDTO dto = new LeaveDTO();
						dto.setApplicationCount(object[0] != null ? (object[0].toString()) : null);
						dto.setLeaveType(object[1] != null ? object[1].toString() : null);
						dto.setLeaveTypeCode(object[2] != null ? object[2].toString() : null);
						dtoList.add(dto);	
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
				}
			} , ()->{
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Application list is null");
			});
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		
		
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
						dto.setApplicationCount(object[0] != null ? (object[0].toString()) : null);
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
					dto.setApproverName(object[12] != null ? object[12].toString() : null);
					dto.setApproverEmail(object[13] != null ? object[13].toString() : null);
					
					dto.setManagerApprovalStatus(object[14] != null ? object[14].toString() : null);
					dto.setLevel2ApproverId(object[15] != null ? Long.parseLong(object[15].toString()) : null);
					dto.setLevel2ApproverName(object[16] != null ? object[16].toString() : null);
					dto.setLevel2ApproverEmail(object[17] != null ? object[17].toString() : null);
					dto.setLevel2ApprovalStatus(object[18] != null ? object[18].toString() : null);
					
					dto.setLevel3ApproverId(object[19] != null ? Long.parseLong(object[19].toString()) : null);
					dto.setLevel3ApproverName(object[20] != null ? object[20].toString() : null);
					dto.setLevel3ApprovalStatus(object[21] != null ? object[21].toString() : null);
					dto.setLevel3ApproverEmail(object[22] != null ? object[22].toString() : null);
					
					dto.setCurrentApprovalLevel(object[23] != null ? Integer.parseInt(object[23].toString()) : null);
					dto.setFinalApprovalLevel(object[24] != null ? Integer.parseInt(object[24].toString()) : null);
					
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
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/bulkApproveLeaveRequest");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("");
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
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
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
	
	/*
	  revoke leave Application methods start --
	  */

	@Transactional
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
						Optional<Employee> empManager = employeeRepository.findById(Long.parseLong(leaveToBeRevoked.getManagerId().toString()));
						Optional<Employee> reportingManager = employeeRepository.findById(empObj.getManagerId());
						
						String managerEmail = "";
						if(!leaveDTO.getApproverEmail().equals(reportingManager.get().getEmail())) {
							managerEmail = ","+ reportingManager.get().getEmail();
						}
						
						// Level 2/3 Approver Email
						if(leaveDTO.getFinalApprovalLevel() == 2) {
							managerEmail = ","+ leaveDTO.getLevel2ApproverEmail();
							
						}else if(leaveDTO.getFinalApprovalLevel() == 3) {
							managerEmail = ","+ leaveDTO.getLevel2ApproverEmail() + ","+ leaveDTO.getLevel3ApproverEmail();
						}
						
						if(!empManager.isEmpty()) {
							Employee empManagerObj = empManager.get();
							
							if(leaveToBeRevoked.getEmpId().equals(leaveDTO.getCreatedBy())) {
								// Revoked leave for self
										mailService.sendMailWithCC(empManagerObj.getEmail(), hrMailAddress +","+ empObj.getEmail()+ managerEmail,
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
									
									mailService.sendMailWithCC(empManagerObj.getEmail(), hrMailAddress +","+ empObj.getEmail() +","+ createdByObj.getEmail()+ managerEmail,
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
					dto.setApproverName(object[12] != null ? object[12].toString() : null);
					
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
					dto.setApproverName(object[12] != null ? object[12].toString() : null);
					
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

	public ServiceResponse getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId1(LeaveDTO leaveDTO) {
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
					dto.setEmpId(object[10] != null ? Long.parseLong(object[10].toString()) : null);
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
	
	public LeaveDTO mapRevokeApplication(Object[] object) {
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
	    dto.setEmpId(object[10] != null ? Long.parseLong(object[10].toString()) : null);
	    return dto;
	}

	public ServiceResponse getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId(LeaveDTO leaveDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId");
	    apiLogInfo.setLogLevel("INFO");

	    try {
	        List<LeaveDTO> dtoList;

	        if (Boolean.TRUE.equals(leaveDTO.getIsHierarchyView())) {
	            // recursive fetch (direct + indirect)
//	            dtoList = getRevokeLeavesRecursively(leaveDTO.getEmpId());
	            List<Long> empIds= empCache.getEmployeesUnderAnyLeadingPerson(leaveDTO.getEmpId().longValue());
	           List<Object[]> list =  leaveRevokeApplicationRepository.getAllMyTeamsPendingLeaveRevokeApplicationsByManagerIdHirarchy(empIds);
	           dtoList = list.stream()
	                    .map(this::mapRevokeApplication)	
	                    .collect(Collectors.toList());
	        } else {
	            // direct
	            List<Object[]> list = leaveRevokeApplicationRepository
	                    .getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId(leaveDTO.getEmpId());

	            dtoList = list.stream()
	                    .map(this::mapRevokeApplication)	
	                    .collect(Collectors.toList());
	        }

	        if (dtoList.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No Revoke Leave Application found");

	            apiLogInfo.setApiResponse("No Revoke Leave Application found");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(dtoList);

	            apiLogInfo.setApiResponse(dtoList.size() + " Revoke Applications found.");
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

	    return response;
	}

	private List<LeaveDTO> getRevokeLeavesRecursively(Long managerId) {
	    List<LeaveDTO> result = new ArrayList<>();

	    List<Object[]> list = leaveRevokeApplicationRepository
	            .getAllMyTeamsPendingLeaveRevokeApplicationsByManagerId(managerId);

	    if (list.isEmpty()) {
	        return result;
	    }

	    for (Object[] obj : list) {
	        LeaveDTO dto = mapRevokeApplication(obj);
	        result.add(dto);

	        Long empId = dto.getEmpId();
	        if (empId != null) {
	            result.addAll(getRevokeLeavesRecursively(empId));
	        }
	    }

	    return result;
	}


	
	public ServiceResponse getAllMyTeamsPendingLeaveRevokeApplicationsByEmpId(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllMyTeamsPendingLeaveRevokeApplicationsByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+leaveDTO.getEmpId());
		try {
			
			List<Object[]> list = leaveRevokeApplicationRepository.
					getAllMyTeamsPendingLeaveRevokeApplicationsByEmpId(leaveDTO.getEmpId());
			
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
					dto.setEmpId(object[10] != null ? Long.parseLong(object[10].toString()) : null);
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
		apiLogInfo.setApiUrl("/api/updateRevokeLeaveStatus");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("LeaveRevokeId : "+leaveDTO.getLeaveRevokeId()+", LeaveTypeMasterId : "+ leaveDTO.getLeaveTypeMasterId()+", LeaveStatusId : "+ leaveDTO.getLeaveStatusId()+", LeaveStatusUpdatedBy : "+ leaveDTO.getLeaveStatusUpdatedBy());
		
		try {
			
			Optional<LeaveRevokeApplication> leaveRevoke = leaveRevokeApplicationRepository.findById(leaveDTO.getLeaveRevokeId());
			Optional<EmployeeLeave> employeeLeave = employeeLeaveRepository.findById(leaveDTO.getLeaveId());
			Integer expirationPeriod = null;
			
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
						
						//Get Expiration Period of CompOff
						if(leaveTypeObj.getLeaveTypeCode().equals("CO")) {
							LeavePolicyMaster leavePolicy  = leavePolicyMasterRepository.findByLeaveTypeMasterIdAndEmploymentStatus(leaveTypeObj.getLeaveTypeMasterId(),emp.get().getEmploymentstatus());
						      if(leavePolicy != null){
						    	  if(leavePolicy.getExpirationPeriod().equals("Yes")) {
							        	 expirationPeriod = leavePolicy.getExpirationPeriodValue();
					               }
						      }
						}
						
					//update leave balance bucket
						if(!leaveTypeObj.getLeaveTypeCode().equals("CO")){
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
						
						}
						
					//send Approval Mail
						if (!emp.isEmpty()) {
							Employee empObj = emp.get();
							Optional<Employee> approver = employeeRepository.findById(Long.parseLong(employeeLeave.get().getManagerId().toString()));
							Optional<Employee> reportingManager = employeeRepository.findById(empObj.getManagerId());
							
							String managerEmail = "";
							if(!leaveDTO.getApproverEmail().equals(reportingManager.get().getEmail())) {
								managerEmail = ","+ reportingManager.get().getEmail();
							}
							
							// Level 2/3 Approver Email
							if(employeeLeave.get().getFinalApprovalLevel() == 2) {
								Optional<Employee> level2Approver = employeeRepository.findById(employeeLeave.get().getLevel2ApproverId());
								managerEmail = ","+ level2Approver.get().getEmail();
								
							}else if(employeeLeave.get().getFinalApprovalLevel() == 3) {
								Optional<Employee> level2Approver = employeeRepository.findById(employeeLeave.get().getLevel2ApproverId());
								Optional<Employee> level3Approver = employeeRepository.findById(employeeLeave.get().getLevel3ApproverId());
								managerEmail = ","+ level2Approver.get().getEmail() + ","+ level3Approver.get().getEmail();
							}
							
							if(!approver.isEmpty()) {
								Employee approverObj = approver.get();
								mailService.sendMailWithCC(empObj.getEmail(), hrMailAddress +","+ approverObj.getEmail()+ managerEmail,
										"Revoke Request for Leave Application Approved",
										"Dear "+ empObj.getName() + ","
										+"<br>Your Revoke Leave Application has been Approved by " + approverObj.getName()
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
						
						//Autofill timesheet delete on approving revoke leave application 
						
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
					    DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
					    
					    String start =  LocalDate.parse(leaveDTO.getFromDate(), formatter).format(formatter2);
					    String end =  LocalDate.parse(leaveDTO.getToDate(), formatter).format(formatter2);
						
					    List<Timesheet> empTimeSheet = timesheetsRepository.findTimesheetOnLeaveDateOLD(leaveDTO.getEmpId(),start,end);

						if (empTimeSheet != null) {

							empTimeSheet.forEach((timesheet)->{
								timesheetsRepository.deleteById(timesheet.getTimesheetId());
							});
						}
						
						//If compOff leave is revoked : changes compOff application status
						//CompOff leave type id : 4 (LeaveTypeMasterId)
						if(leaveTypeObj.getLeaveTypeCode().equals("CO")) {

							
							List<CompOffLeave> compOffLeave = compOffLeaveRepository.findByLeaveId(leaveObj.getLeaveId());
							
							if(!compOffLeave.isEmpty()) {
									
									for(CompOffLeave leave: compOffLeave) {
										
										LocalDate expireDate = leave.getFromDate().plusDays(expirationPeriod != null ? expirationPeriod : 30);
										if(LocalDate.now().isAfter(expireDate) || LocalDate.now().isEqual(expireDate)) {
											
											leave.setCompOffStatus("Expired");
											compOffLeaveRepository.save(leave);
										}else {
											
											//Update Leave Balance
											

											EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository.findByEmpIdAndLeaveTypeMasterId(
													leaveObj.getEmpId(), leaveObj.getLeaveTypeMasterId());
											
											Float balance = employeeLeavesMap.getBalance();
											balance = balance + leave.getNoOfDays();
											Float pendingForApproval = employeeLeavesMap.getPendingForApproval();
											pendingForApproval = pendingForApproval - leave.getNoOfDays();
											
											employeeLeavesMap.setBalance(balance);
//											employeeLeavesMap.setPendingForApproval(pendingForApproval);
											
											System.out.println(employeeLeavesMap + " employee leave");
											EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(employeeLeavesMap);
											
											if(dbResponse != null) {
												LeaveBalanceLog log = new LeaveBalanceLog();
												
												log.setBalance(balance);
												log.setEmpId(leaveObj.getEmpId());
												log.setLeaveTypeMasterId(leaveObj.getLeaveTypeMasterId());
												log.setMessage(LeaveLogMessage.leaveRevoked.replace("0.0", leave.getNoOfDays().toString()));
												log.setUpdateBalanceBy("+" + leave.getNoOfDays());
												
												leaveBalanceLogRepository.save(log);
											}
										
											
											// change compOff application status
												leave.setCompOffStatus("Pending");
												leave.setLeaveId(null);
												
												compOffLeaveRepository.save(leave);
										}
										
									}
							}
						}
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
							Optional<Employee> approver = employeeRepository.findById(Long.parseLong(employeeLeave.get().getManagerId().toString()));
							Optional<Employee> reportingManager = employeeRepository.findById(empObj.getManagerId());
							
							String managerEmail = "";
							if(!leaveDTO.getApproverEmail().equals(reportingManager.get().getEmail())) {
								managerEmail = ","+ reportingManager.get().getEmail();
							}
							
							if(!approver.isEmpty()) {
								Employee approverObj = approver.get();
								mailService.sendMailWithCC(empObj.getEmail(), hrMailAddress +","+ approverObj.getEmail()+ managerEmail,
										"Revoke Request for Leave Application Rejected",
										"Dear "+ empObj.getName() + ","
										+"<br>Your Revoke Leave Application has been Rejected by " + approverObj.getName()
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

	public ServiceResponse getAllLeaveBalanceByEmpId(LeaveDTO leaveDTO) {
		
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllLeaveBalanceByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+leaveDTO.getEmpId());

		try {

			Employee employee = employeeRepository.findByEmpId(leaveDTO.getEmpId());
					
			if (employee != null) {
				List<Object[]> employeeLeavesListByEmpId = employeeLeavesMapRepository
						.getAllLeaveBalancesByEmpId(employee.getEmpId(),leaveDTO.getLeaveTypeMasterId());
				List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

				if (!employeeLeavesListByEmpId.isEmpty()) {
					
					
					employeeLeavesListByEmpId.forEach((object) -> {
						LeaveDTO dto = new LeaveDTO();
						dto.setLeaveType(object[0] != null ? object[0].toString() : null);
						dto.setBalance(object[1] != null ? Float.parseFloat(object[1].toString()) : null);
						dto.setLeaveTypeMasterId(object[2] != null ? Short.parseShort(object[2].toString()) : null);
						dto.setLeaveTypeCode(object[3] != null ? object[3].toString() : null);

						dtoList.add(dto);
					});
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse("dtoList : " +dtoList);
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				} else {

					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Leaves balance found.");
					
					apiLogInfo.setApiResponse("No Leave Balance Found.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee not found.");
				
				apiLogInfo.setApiResponse("Employee not found.");			
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
	
public ServiceResponse getEmployeeLeaveApplicationwithHolidays(LeaveDTO leaveDTO) {
		
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getEmployeeLeaveApplicationwithHolidays");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("FromDate : "+leaveDTO.getFromDate() + " ToDate : "+leaveDTO.getToDate());
		List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

		try {
			HolidayDTO holidayRange = new HolidayDTO();
			holidayRange.setFromDate(leaveDTO.getFromDate());
			holidayRange.setToDate(leaveDTO.getToDate());
			
			List<Object[]> employeeLeavesListByFromDate = employeeLeaveRepository.getAllLeaveApplicationByFromDate(leaveDTO.getFromDate());
			List<LeaveDTO> leaveList = new ArrayList<LeaveDTO>();
			
			ServiceResponse holidayResponse = holidayService.getHolidayWeekOffSize(holidayRange);
			List<HolidayDTO> holidayList = (List<HolidayDTO>)holidayResponse.getServiceResponse();
			
			System.out.println("employeeLeavesListByFromDate : "+ employeeLeavesListByFromDate);
			System.out.println("holidayList : "+ holidayList);
			
			if (!employeeLeavesListByFromDate.isEmpty()) {
				
				
				employeeLeavesListByFromDate.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setLeaveId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setFromDate(object[1] != null ? object[1].toString() : null);
					dto.setToDate(object[2] != null ? object[2].toString() : null);
					dto.setNoOfDays(object[3] != null ? Float.parseFloat(object[3].toString()) : null);
					dto.setEmployeeName(object[4] != null ? object[4].toString() : null);
					dto.setCreatedOn(object[5] != null ? object[5].toString() : null);
					dto.setLeaveStatusId(object[6] != null ? Short.parseShort(object[6].toString()) : null);
					dto.setEmail(object[7] != null ? object[7].toString() : null);
					dto.setManagerName(object[8] != null ? object[8].toString() : null);
					dto.setManagerEmail(object[9] != null ? object[9].toString() : null);

					leaveList.add(dto);
				});
				
				System.out.println("leaveList : "+ leaveList);
				
				if(!leaveList.isEmpty()) {
					for(LeaveDTO leaveApplication : leaveList) {
						
						long elapsedDays = ChronoUnit.DAYS.between(stringToDateTimeParser.getDate(leaveApplication.getFromDate(), "yyyy-MM-dd"), stringToDateTimeParser.getDate(leaveApplication.getToDate(), "yyyy-MM-dd"));
						Long NO_OF_DAYS = elapsedDays + 1;
						
						if(elapsedDays == 0) {
							for(HolidayDTO holiday : holidayList) {
								if(stringToDateTimeParser.getDate(holiday.getDateOfHoliday(), "yyyy-MM-dd").equals(stringToDateTimeParser.getDate(leaveApplication.getFromDate(), "yyyy-MM-dd")) && leaveApplication.getLeaveStatusId() != 3) {
									System.out.println(" =============================== ");
									System.out.println("leaveApplication : "+ leaveApplication);
									System.out.println(" =============================== ");
									dtoList.add(leaveApplication);
									break;
								}
							}
						
						}
						
						if(elapsedDays != 0) {
							LocalDate checkDate = stringToDateTimeParser.getDate(leaveApplication.getFromDate(), "yyyy-MM-dd");
							
							while(checkDate.compareTo(stringToDateTimeParser.getDate(leaveApplication.getToDate(), "yyyy-MM-dd")) != 1) {
								boolean flag = false;
								
								for(HolidayDTO holiday : holidayList) {
									if(stringToDateTimeParser.getDate(holiday.getDateOfHoliday(), "yyyy-MM-dd").equals(checkDate) && leaveApplication.getNoOfDays().equals(Float.parseFloat(NO_OF_DAYS.toString())) && leaveApplication.getLeaveStatusId() != 3) {
										flag = true;
										dtoList.add(leaveApplication);
										System.out.println(" =============================== ");
										System.out.println("leaveApplication : "+ leaveApplication);
										System.out.println(" =============================== ");
										break;
									}
								}
								
								if(flag) {
									break;
								}
								
								checkDate = checkDate.plusDays(1);
							}
						}
						
					}
					
					if(!dtoList.isEmpty()) {
						for(LeaveDTO leaveApp : dtoList) {
							if (leaveApp != null) {
								mailService.sendMailWithCC(leaveApp.getEmail(), hrMailAddress +","+ leaveApp.getManagerEmail(),
										"Regarding Re-Application of your Leave",
										"Dear "+ leaveApp.getEmployeeName() + ","
										+"<br>  Kindly re-apply your leave application, "
										+"<br> as weekoffs are also included in leave days of your leave application, "
										+"<br> if reflecting correct number of days then ignore this message."
										+"<br><br>Leave Application Details :"
										+"<br> From Date : " + leaveApp.getFromDate() + "   To Date : " + leaveApp.getToDate()
										+"<br> No. Of Days : " + leaveApp.getNoOfDays());
							}
						}
					}
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					
					apiLogInfo.setApiResponse(dtoList.size()+ " Leave Applications with Holiday.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Leave Applications found.");
					
					apiLogInfo.setApiResponse("No Leave Applications found.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}

			} else {

				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Leave Applications found.");
				
				apiLogInfo.setApiResponse("No Leave Applications found.");
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

	public ServiceResponse getOverlappedTeamMemberLeave(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getOverlappedTeamMemberLeave");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("");
		try {
			
			List<Object[]> overLappedTeamMember = employeeLeaveRepository
					.findOverlappedTeamMemberLeave(leaveDTO.getEmpId(), leaveDTO.getToDate(), leaveDTO.getFromDate());
			List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
			
			if(!overLappedTeamMember.isEmpty()) {
				overLappedTeamMember.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					
					dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setEmployeementId(object[1] != null ? Long.parseLong(object[1].toString()) : null);
					dto.setName(object[2] != null ? object[2].toString() : null);
					dto.setFromDate(object[3] != null ? object[3].toString() : null);
					dto.setToDate(object[4] != null ? object[4].toString() : null);
					
					dtoList.add(dto);
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				apiLogInfo.setApiResponse("dtoList size : "+dtoList.size());
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Overlapped Leave found.");
				apiLogInfo.setApiResponse("No Overlapped Leave found.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse setEmployeeLeaveEntitlement(LeaveDTO leaveDTO) {

		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/setEmployeeLeaveEntitlement");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("FROM DATE : " + leaveDTO.getFromDate() + ", TO DATE : " + leaveDTO.getToDate());

		List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();
		List<LeaveDTO> employeeBalanceList = new ArrayList<LeaveDTO>();
		List<EmployeeLeavesMap> updatedBalanceList = new ArrayList<EmployeeLeavesMap>();
		 
		try {

			List<Object[]> employees = employeeLeavesMapRepository.getEmployeesInProbation();

			if (employees != null) {
				
				employees.forEach((object) -> {
					LeaveDTO dto = new LeaveDTO();
					dto.setEmployeeName(object[0] != null ? object[0].toString() : null);
					dto.setEmpId(object[1] != null ? Long.parseLong(object[1].toString()) : null);
					dto.setEmployeementId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
					dto.setEmploymentStatus(object[3] != null ? object[3].toString() : null);
					dto.setDateOfJoining(object[4] != null ? object[4].toString() : null);
					dto.setLeaveTypeMasterId(object[5] != null ? Short.parseShort(object[5].toString()) : null);
					dto.setLeaveType(object[6] != null ? object[6].toString() : null);
					dto.setEmployeeLeavesMapId(object[7] != null ? Long.parseLong(object[7].toString()) : null);
					dto.setBalance(object[8] != null ? Float.parseFloat(object[8].toString()) : null);

					employeeBalanceList.add(dto);
				});
				
				
				System.out.println("employeeBalanceList : "+ employeeBalanceList);
				
				if(!employeeBalanceList.isEmpty()) {
					DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
					LocalDate checkDate = LocalDate.parse("2022-12-01" , format);
					
					employeeBalanceList.forEach((emp) -> {
						LocalDate dateOfJoining = LocalDate.parse(emp.getDateOfJoining() , format);
						Float newBalance = 0.0F;
						
						Period period = Period.between(dateOfJoining, checkDate);
						long elapsedMonths = period.getMonths();
						long elapsedDays = period.getDays();
						double leavesForMonths = (double)(2.5*elapsedMonths);
						double leavesForDays = (double)((2.5*elapsedDays)/30);
						
						if(leavesForDays != 0) {
							BigDecimal BIG_O5 = new BigDecimal(0.5);

						    BigDecimal bd = new BigDecimal( leavesForDays - Math.floor(leavesForDays));
						    bd = bd.setScale(4,RoundingMode.HALF_DOWN);
						    System.out.println("Decimal value " + bd.toString());
						    
						    if(bd.compareTo(BIG_O5) == 1) {
						    	leavesForDays = Math.ceil(leavesForDays);
						    }else if(bd.compareTo(BIG_O5) == 0){
						    	leavesForDays = Math.floor(leavesForDays) + 0.5;
						    }else {
						    	leavesForDays = Math.floor(leavesForDays);
						    }
						}
						
						 System.out.println("\n============\n " + emp.getEmployeeName()+ " : "+ emp.getBalance() + " : "+ emp.getDateOfJoining());
						 System.out.println("Leaves for " + elapsedMonths + " Months : "+ leavesForMonths + "\nLeaves for Days "+ elapsedDays + " Days : "+ leavesForDays);
						 System.out.println("============\n");
						 
						// for Extra Days Leaves  
//						newBalance =  (float) leavesForDays;
//						emp.setCreditedBalance(newBalance);
//						dtoList.add(emp);
						
						newBalance =  (float)(leavesForMonths + leavesForDays);
						
						Optional<EmployeeLeavesMap> empMap = employeeLeavesMapRepository.findById(emp.getEmployeeLeavesMapId());
						if(!empMap.isEmpty()) {
							EmployeeLeavesMap leaveMap = empMap.get();
							float updatedBalance = leaveMap.getBalance() + newBalance;
							leaveMap.setBalance(updatedBalance);
							
							updatedBalanceList.add(leaveMap);
							
							emp.setCreditedBalance(newBalance);
							dtoList.add(emp);
						}
					});
					
					List<EmployeeLeavesMap> updatedList = employeeLeavesMapRepository.saveAll(updatedBalanceList);
					
					if(!updatedList.isEmpty()){
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse(dtoList);
						
						apiLogInfo.setApiResponse(dtoList.size()+ " Leave Buckets Updated.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("failed to update Leave Buckets.");

						apiLogInfo.setApiResponse("failed to update Leave Buckets.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
				}

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employees not found.");

				apiLogInfo.setApiResponse("Employees not found.");
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

	public ServiceResponse fillTimesheetForOldLeaves() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/fillTimesheetForOldLeaves");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("");
		try {
			
			LocalDate leaveFromDate = LocalDate.parse("2022-12-01");
			List<EmployeeLeave> leaveApplication = employeeLeaveRepository.findByFromDateAfterAndLeaveStatusId(leaveFromDate,(short) 2);
			
			List<EmployeeDTO> filledTimesheet = new ArrayList<EmployeeDTO>();
			List<EmployeeDTO> pendingTimesheet = new ArrayList<EmployeeDTO>();
			
			if(!leaveApplication.isEmpty()) {
				
				leaveApplication.forEach((leave) -> {
					
					
					
					if((leave.getFromDateDayType() != null) && leave.getFromDateDayType() == 0.5) {
						System.out.println("From Date is Half Day");
					}else if((leave.getToDateDayType() != null) && leave.getToDateDayType() == 0.5) {
						System.out.println("To Date is Half Day");
					}else {
						
						// If leave is of 1 day
						if(leave.getNoOfDays().equals((float)1)) {
							
							System.out.println(leave.getLeaveId() + "id \n\n\n");
							
							Timesheet employeeTimesheet = timesheetsRepository.findByEmpIdAndDate(leave.getEmpId(), leave.getFromDate());
							
							Employee empObj = employeeRepository.findByEmpId(leave.getEmpId());
							
							if(employeeTimesheet != null) {
								System.out.println("Timesheet filled already");
								
								if(empObj != null) {
									EmployeeDTO employee = new EmployeeDTO();
									
									employee.setEmpId(leave.getEmpId());
									employee.setEmployeementId(empObj.getEmployeementId());
									employee.setDateOfJoining(leave.getFromDate().toString());
									employee.setName(empObj.getName());
									
									filledTimesheet.add(employee);
								}
								
							}else {
								Timesheet newTimesheet = new Timesheet();
								
								newTimesheet.getCommonProperty().setCreatedBy(leave.getEmpId());
								newTimesheet.setDate(leave.getFromDate());
								newTimesheet.setDayType("Leave");
								newTimesheet.setDescription("On leave");
								newTimesheet.setEmpId(leave.getEmpId());
								newTimesheet.setStatus("Approved");
								
								Timesheet dbResposne = timesheetsRepository.save(newTimesheet);
								
								if(dbResposne != null) {
									
									
                                    EmployeeDTO employee = new EmployeeDTO();
									
									employee.setEmpId(leave.getEmpId());
									employee.setEmployeementId(empObj.getEmployeementId());
									employee.setDateOfJoining(leave.getFromDate().toString());
									employee.setName(empObj.getName());
									
									pendingTimesheet.add(employee);
									
									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("timesheet Added successfully");
									apiLogInfo.setApiResponse("timesheet Added successfully");
									apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
								}else {
									response.setServiceStatus(ServiceResponse.STATUS_FAIL);
									response.setServiceResponse("Unable to add timesheet.");
									apiLogInfo.setApiResponse("Unable to add timesheet.");
									apiLogInfo.setApiResponse(ServiceResponse.STATUS_FAIL);								}
							}
						}
						// If leave is of multiple days
						if(leave.getNoOfDays() > 1.0F) {
							LocalDate tempFromDate = leave.getFromDate();
							
							while(tempFromDate.compareTo(leave.getToDate()) != 1) {
								
								System.out.println(leave.getEmpId() + " empid \n\n");
								System.out.println(tempFromDate + " tempFromDate \n\n");
								
								Timesheet employeeTimesheet = timesheetsRepository.findByEmpIdAndDate(leave.getEmpId(), tempFromDate);
								
								Employee empObj = employeeRepository.findByEmpId(leave.getEmpId());
								
								if(employeeTimesheet != null) {
									System.out.println("Timesheet filled already");
									
                                    EmployeeDTO employee = new EmployeeDTO();
									
									employee.setEmpId(leave.getEmpId());
									employee.setEmployeementId(empObj.getEmployeementId());
									employee.setDateOfJoining(tempFromDate.toString());
									employee.setName(empObj.getName());
									
									filledTimesheet.add(employee);
									
									
									tempFromDate = tempFromDate.plusDays(1);
								}else {
									Timesheet newTimesheet = new Timesheet();
									
									newTimesheet.getCommonProperty().setCreatedBy(leave.getEmpId());
									newTimesheet.setDate(tempFromDate);
									newTimesheet.setDayType("Leave");
									newTimesheet.setDescription("On leave");
									newTimesheet.setEmpId(leave.getEmpId());
									newTimesheet.setStatus("Approved");
									
									Timesheet dbResposne = timesheetsRepository.save(newTimesheet);
									
                                    EmployeeDTO employee = new EmployeeDTO();
									
									employee.setEmpId(leave.getEmpId());
									employee.setEmployeementId(empObj.getEmployeementId());
									employee.setDateOfJoining(tempFromDate.toString());
									employee.setName(empObj.getName());
									
									pendingTimesheet.add(employee);
									
									tempFromDate = tempFromDate.plusDays(1);
									System.out.println("timesheet filled");
								}
							}
							
						}
						
					}
				});
				
			}
			
			
			response.setServiceResponse(filledTimesheet);
			response.setServiceResponse1(pendingTimesheet);
			
			JSONArray jsonarray = new JSONArray(pendingTimesheet);
			JSONArray jsonarray2 = new JSONArray(filledTimesheet);
			
			mailService.sendMail("prasad.more@apmosys.com", "Timesheet not filled", 
					jsonarray.toString() + "<br><br><br><br><br><br><br>" + jsonarray2.toString());
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			apiLogInfo.setLogLevel("ERROR");	
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse pendingForApprovalReconsilation() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/pendingForApprovalReconsilation");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("");
		try {
			
			List<Employee> allEmployeeList = employeeRepository.findAll();
			List<LeaveDTO> dtoList = new ArrayList<>();
			
			if(!allEmployeeList.isEmpty()) {
				allEmployeeList.forEach((object) -> {
					List<LeaveTypeMaster> allLeaveType = leaveTypeMasterRepository.findAll();
					
					if(!allLeaveType.isEmpty()) {
						allLeaveType.forEach((leaveType) -> {
							//4 : compOff && 5 : ML
							if(leaveType.getLeaveTypeMasterId() != 4 && leaveType.getLeaveTypeMasterId() != 5){
								// 1 = pending							
								List<EmployeeLeave> employeeLeave = employeeLeaveRepository
										.findAllByEmpIdAndLeaveTypeMasterId(object.getEmpId(), leaveType.getLeaveTypeMasterId());
									
									//Get EmployeeLeaveMapping to update "Pending For Approval" count
									
									EmployeeLeavesMap employeeLeaveMap = employeeLeavesMapRepository.
											findByEmpIdAndLeaveTypeMasterId(object.getEmpId(), leaveType.getLeaveTypeMasterId());
									
									if(employeeLeaveMap != null) {
										
										Float pendingForApprovalCount = employeeLeaveMap.getPendingForApproval();
										
										if(!employeeLeave.isEmpty()) {
											
											for(EmployeeLeave leaveApplication: employeeLeave) {
												if(leaveApplication.getLeaveStatusId() == 2) {
													pendingForApprovalCount = pendingForApprovalCount - leaveApplication.getNoOfDays();
												}
											}
										
										employeeLeaveMap.setPendingForApproval(pendingForApprovalCount);
										
										LeaveDTO dto = new LeaveDTO();
										dto.setEmpId(object.getEmpId());
										dto.setEmployeementId(object.getEmployeementId());
										dto.setPendingForApproval(pendingForApprovalCount);
										
										dtoList.add(dto);									
										EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(employeeLeaveMap);
									}
								}
							}
						});
					}
					
				});
			}
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(dtoList);
			apiLogInfo.setApiResponse("dtoList size : "+dtoList.size());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
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

	public ServiceResponse reconsileCasualBalance() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/reconsileCasualBalance");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			
			List<Employee> allEmployee = employeeRepository.findAll();
			
			List<Employee> json = new ArrayList<>();
			
			if(!allEmployee.isEmpty()) {
				allEmployee.forEach((object) -> {
					List<LeaveBalanceLog> balanceLogObj = leaveBalanceLogRepository
							.findLogToReconsile(object.getEmpId(), (short) 2);
					
					Float updatedBalance = (float) 0;
					
					for(LeaveBalanceLog logObj : balanceLogObj) {
						String message = logObj.getUpdateBalanceBy();
						float number = Float.parseFloat(message.replaceAll("[^\\d.]", ""));
						
						System.out.println(number + " number \n\n\n");
						
						updatedBalance = updatedBalance + number;
					}
					
					EmployeeLeavesMap empLeaveMap = employeeLeavesMapRepository
							.findByEmpIdAndLeaveTypeMasterId(object.getEmpId(), (short) 2);
					
					if(empLeaveMap != null) {
						empLeaveMap.setBalance(updatedBalance);
						EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(empLeaveMap);		
						
						if(dbResponse != null) {
							LeaveBalanceLog log = new LeaveBalanceLog();

							log.setBalance(updatedBalance);
							log.setEmpId(object.getEmpId());
							log.setLeaveTypeMasterId((short) 2);
							log.setMessage(LeaveLogMessage.autoAddLeave.replace("0.0",
									Float.toString(updatedBalance)));
							log.setUpdateBalanceBy("-" + updatedBalance);

							LeaveBalanceLog logResponse = leaveBalanceLogRepository.save(log);
							
							if(logResponse != null) {
								
//								Employee newObj = new Employee();
//								newObj.setEmpId(object.getEmpId());
//								newObj.setEmployeementId(object.getEmployeementId());
//								newObj.setName(object.getName());
//								newObj.setPreviousLog(balanceLogObj.get(0).getMessage());
//								newObj.setNewLog(log.getMessage());
//								
//								json.add(newObj);
								
								response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
								response.setServiceResponse("Casual Leave recosiled Successfully.");
							}else {
								response.setServiceStatus(ServiceResponse.STATUS_FAIL);
								response.setServiceResponse("Unable to reconsile Casual Leave.");
							}
						}
					}
					
				});
			}
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse1(json);
			apiLogInfo.setApiResponse("json");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
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

	
	public ServiceResponse addMaternityLeaves() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/addMaternityLeaves");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		
		final Float MATERNITY_LEAVES = 180F;
		
		try {
			System.err.println(" Maternity API's call from postman ");
			
			List<Employee> allEmployee = employeeRepository.findAll();
			
			List<EmployeeDTO> json = new ArrayList<>();
			
			if(!allEmployee.isEmpty()) {
				allEmployee.forEach((object) -> {
					
					if("female".equalsIgnoreCase(object.getGender())){
//						if(object.getEmploymentstatus().equals("Confirmed")) {}

						EmployeeLeavesMap empLeaveMap = employeeLeavesMapRepository
								.findByEmpIdAndLeaveTypeMasterId(object.getEmpId(), (short) 5); // 18 is in local and Uat and 5 is in Prod
						
						if(empLeaveMap != null) {
							empLeaveMap.setBalance(MATERNITY_LEAVES);
							EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(empLeaveMap);		
							
							if(dbResponse != null) {
								LeaveBalanceLog log = new LeaveBalanceLog();

								log.setBalance(MATERNITY_LEAVES);
								log.setEmpId(object.getEmpId());
								log.setLeaveTypeMasterId((short) 5);
								log.setMessage(LeaveLogMessage.autoAddLeave.replace("0.0",
										Float.toString(MATERNITY_LEAVES)));
								log.setUpdateBalanceBy("-" + MATERNITY_LEAVES);

//								LeaveBalanceLog logResponse = leaveBalanceLogRepository.save(log);
								
								if(log != null) {
									
									EmployeeDTO newObj = new EmployeeDTO();
									newObj.setEmpId(object.getEmpId());
									newObj.setEmployeementId(object.getEmployeementId());
									newObj.setName(object.getName());
									newObj.setRemarks(log.getMessage());

									
									json.add(newObj);
									
									response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
									response.setServiceResponse("Maternity Leave Added Successfully.");
								}else {
									response.setServiceStatus(ServiceResponse.STATUS_FAIL);
									response.setServiceResponse("Unable to Add Maternity Leave.");
								}
							}
						}
					
					}
				});
			}
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse1(json);
			apiLogInfo.setApiResponse("json");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
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
	
	public ServiceResponse getLeaveAppliedListByFromAndToDate(LeaveDTO leaveDto) {
		
		ServiceResponse response = new ServiceResponse();
		Optional<List<EmployeeLeave>> getLeaveByFromDateToDate = employeeLeaveRepository.findLeaveByFromDateAndToDate(leaveDto.getFromDate(),leaveDto.getToDate(),leaveDto.getEmpId());
		
		if(getLeaveByFromDateToDate.isPresent()) {
			List<EmployeeLeave> listOfLeaveByEmpId =getLeaveByFromDateToDate.get();
			EmployeeLeave findLeave = null;
			if(listOfLeaveByEmpId.size() > 0) {
				findLeave = listOfLeaveByEmpId.get(0);			
			}
			
			if(findLeave != null) {
				System.out.println(" There is leave present so that status will send as fail "+listOfLeaveByEmpId);
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(listOfLeaveByEmpId);
			}else {
				System.err.println(" Success call because there is no leave present in between "+leaveDto.getFromDate()+" and "+leaveDto.getToDate());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(new ArrayList<>());
			}
		}
		  		
		return response;
	}

	public ServiceResponse getAllLeaveByEmpId(LeaveDTO leaveDto) {
		
		ServiceResponse response = new ServiceResponse();
		
		
		Optional<List<EmployeeLeave>> findLeavesByEmpId = employeeLeaveRepository.findLeavesByEmpId(leaveDto.getEmpId());
		if(findLeavesByEmpId.isPresent() && !findLeavesByEmpId.get().isEmpty()) {
			List<EmployeeLeave> getLeaves = findLeavesByEmpId.get();
			System.err.println("getLeaves   "+getLeaves);
		if(getLeaves != null) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(getLeaves);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Leaves not present ");
			}
		}
		
		return response;
	}
	
//	 added by anurag
	
	public ServiceResponse pipGenerateToUser(LeaveDTO leaveDto) {

		ServiceResponse response = new ServiceResponse();

		try {

			Employee findEmployee = employeeRepository.findByEmpId(leaveDto.getEmpId());
			Employee findManager = employeeRepository.findByEmpId(findEmployee.getManagerId());
			String empEmail = findEmployee.getEmail();
			String managerEmail = findManager.getEmail();
			String pipReason = leaveDto.getPipReason();

			System.out.println("empEmail     " + empEmail);
			System.err.println("managerEmail  " + managerEmail);

			PIP pipCreate = new PIP();
			pipCreate.setEmpId(leaveDto.getEmpId());
			pipCreate.setPipReason(leaveDto.getPipReason());
			pipCreate.setPipFlag(true);
			pipCreate.setStartDate(leaveDto.getStartDate());
			pipCreate.setEndDate(leaveDto.getEndDate());
			pipCreate.setCreatedOn(LocalDateTime.now());
			pipCreate.setCreatedBy(leaveDto.getCreatedByName());
			pipCreate.setExtendDays("No");

			PIP pipCreated = pipRepository.save(pipCreate);
			findEmployee.setPipFlag(true);
			findEmployee.setPipId(pipCreated.getPipId());
			employeeRepository.save(findEmployee);

			System.err.println("findEmployee   +  " + findEmployee);

			if (pipCreated != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("PIP Raised ");
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("PIP not Raised");
			}
			List<Object[]> findData = employeeLeaveRepository.findHodsData(leaveDto.getEmpId());

			findData.forEach((object) -> {
				this.hodEmail = object[3] != null ? object[3].toString() : null;
			});

			String subject = " Attention - Performance Improvement Plan";
			mailService.sendMailWithCC(empEmail, managerEmail + "," + hrMailAddress + "," + this.hodEmail, subject,
					"Dear " + findEmployee.getName() + " " + "<br>" + "<br>"
							+ "This email is to formally notify you that you are being placed on a Performance Improvement Plan (PIP) effective "
							+ leaveDto.getStartDate() + ". "
							+ "The purpose of this plan is to provide you with clear expectations and support to "
							+ "improve your performance in specific areas." + "<br><br>"
							+ "During the recent performance evaluations, "
							+ "we identified areas where your performance has not met "
							+ "the expectations of the organization. <br><br>" + "These areas include : </b>" + "<b>"
							+ pipReason + "</b>" + "<br><br>"
							+ "This PIP emphasizes on the specific goals and objectives "
							+ "you will need to achieve within a timeframe of one month. "
							+ "Your progress towards these goals will be closely monitored and regular feedback "
							+ "will be provided to support your development and improvement." + "<br><br>"
							+ "It is important to understand that failure to meet the expectations outlined "
							+ "in this plan within the timeframe may result in further disciplinary action, "
							+ "up to and including termination of your employment." + "<br><br>" + " Sincerely,"
							+ "<br>" + "Team HR - ApMoSys Technologies");

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong");
		}

		return response;
	}
	
	public ServiceResponse pipReturnFromUser(LeaveDTO leaveDto) throws AddressException, MessagingException {

		ServiceResponse response = new ServiceResponse();

		Employee findEmployee = employeeRepository.findEmployeeByPipId(leaveDto.getPipId());
		Employee findManager = employeeRepository.findByEmpId(findEmployee.getManagerId());
		String empEmail = findEmployee.getEmail();
		String managerEmail = findManager.getEmail();
		String pipReason = leaveDto.getRevReason();

		findEmployee.setPipFlag(false);

		Employee dbResponse = employeeRepository.save(findEmployee);

		PIP findpip = new PIP();
		findpip.setUpdatedBy(leaveDto.getUpdatedByName());
		findpip.setUpdatedOn(LocalDateTime.now());
		findpip.setPipFlag(false);
		findpip.setEmpId(leaveDto.getEmpId());
		findpip.setRevReason(leaveDto.getRevReason());
//		findpip.setEndDate(LocalDate.now().toString().formatted("dd-mm-yyyy")); 
		findpip.setStartDate(leaveDto.getStartDate());
		findpip.setEndDate(leaveDto.getEndDate());

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		LocalDate startDate = LocalDate.parse(findpip.getStartDate(), dateFormatter);
		LocalDate dtoEndDate = LocalDate.parse(leaveDto.getEndDate(), dateFormatter);

		Long agingDays = Math.abs(ChronoUnit.DAYS.between(dtoEndDate, startDate));

		System.out.println("dtoEndDate " + dtoEndDate);
		System.out.println("startDate " + startDate);

		System.err.println(" aging   " + agingDays);
		findpip.setAging(agingDays);

		PIP dbPipResponse = pipRepository.save(findpip);

		if (dbPipResponse != null) {
			response.setServiceResponse("PIP reverse successfully ");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);

		}

		List<Object[]> findData = employeeLeaveRepository.findHodsData(leaveDto.getEmpId());

		findData.forEach((object) -> {
			this.hodEmail = object[3] != null ? object[3].toString() : null;
		});

		String subject = " Attention - Regarding PIP reversal mail";
		mailService.sendMailWithCC(empEmail, managerEmail + "," + hrMailAddress + "," + this.hodEmail, subject, "Dear "
				+ findEmployee.getName() + " " + "<br>" + "<br>"
				+ "This email is to formally notify you that you are not placed on a Performance Improvement Plan (PIP) effective . "
				+ "your performance is now up to the mark so you are not in Performance Improvement Plan, "
				+ "below the reason provided why we revert you from PIP " + "<br>" + "These areas include : </b>"
				+ "<b>" + pipReason + "</b>" + "<br><br>" + " Sincerely," + "<br>" + "Team HR - ApMoSys Technologies");

		return response;
	}
	
	
	@Scheduled(cron ="${leaveMapToNewManager_cron}")
	public void mapOldPendingLeaveToNewManager() {
		
		ServiceResponse response = new ServiceResponse();
		
		System.out.println(" old leave to new leave manager map method call  ");
		List<EmployeeLeave> findLeaves = employeeLeaveRepository.findOldPendingLeaves();
		System.err.println(" findLeave ki size ...........   "+findLeaves.size());
		int count = 0 ;
//		findLeaves.forEach((leave)->{
		for(EmployeeLeave leave : findLeaves) {
			
			Long employeeId = leave.getEmpId();
			Employee employee = employeeRepository.findByEmpId(employeeId);
			Long managerId = employee.getManagerId();
			leave.setManagerId(Math.toIntExact(managerId));	
		EmployeeLeave dbLeave = employeeLeaveRepository.save(leave);
		count = count+1;
		
		System.err.println("count  :  "+count);
		}
		//		});
		
		
	}
	
	public ServiceResponse getOverLapsLeaveForManager(LeaveDTO leaveDto) {
		
		    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		    ServiceResponse response = new ServiceResponse();

		    try {
		        LocalDate fromDate = LocalDate.parse(leaveDto.getFromDate(), inputFormatter);
		        LocalDate toDate = LocalDate.parse(leaveDto.getToDate(), inputFormatter);
		        
		        List<LeaveDTO> dtoList = new ArrayList<>();

		        String fromDateStr = fromDate.format(outputFormatter);
		        String toDateStr = toDate.format(outputFormatter);

		        System.out.println(" fromDate and ToDate " + fromDateStr + " = " + toDateStr + " ");

		        List<Object[]> findOverLapsLeave = employeeLeaveRepository.getOverLapsLeaveForManager(fromDateStr, toDateStr, leaveDto.getManagerId());
		        System.err.println("findOverLapsLeave :: " + findOverLapsLeave.size());
		        
		        findOverLapsLeave.forEach((object)->{
		        	LeaveDTO dto = new LeaveDTO();
		        	
		        	dto.setLeaveId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
		        	dto.setEmployeementId(object[1] != null ? Long.parseLong(object[1].toString()) : null);      	
		        	dto.setEmpId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
		        	dto.setEmployeeName(object[3] != null ? object[3].toString() : null);
		        	dto.setFromDate(object[4] != null ? object[4].toString() : null);
		        	dto.setToDate(object[5] != null ? object[5].toString() : null);
		        	dto.setCreatedOn(object[6] != null ? object[6].toString() : null);
		        	dto.setCreatedByName(object[8] != null ? object[8].toString() : null);
		        	dto.setManagerName(object[9] != null ? object[9].toString() : null);
		        	dto.setStatus(object[10] != null ? object[10].toString() : null);
		        	
		        	dtoList.add(dto);
		        });        
		        
		        
		        if(dtoList != null) {
		        	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        	response.setServiceResponse(dtoList);
		        }
	
		    } catch (Exception e) {
		    	e.printStackTrace();
		    	response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		    	response.setServiceResponse("Something went wrong ");  
		    }
		    
		return response;
	}
	
//	PIP Reasons
	
	public ServiceResponse getPipReasons(LeaveDTO leaveDto) {

		ServiceResponse response = new ServiceResponse();
		System.err.println(" leaveDto.getEmpId(),leaveDto.getPipFlag() " + leaveDto.getEmpId() + " "
				+ Boolean.valueOf(leaveDto.getPipFlag()));
//		List<Object[]> listOfPip = pipRepository.findPipReasonByEmpIdAndPipFlag(leaveDto.getEmpId(),Boolean.valueOf(leaveDto.getPipFlag()));
		List<Object[]> listOfPip = pipRepository.findPipReasonByEmpIdAndPipFlag(leaveDto.getEmpId());
		List<LeaveDTO> dtoList = new ArrayList<LeaveDTO>();

		listOfPip.forEach(object -> {
			LeaveDTO dto = new LeaveDTO();
			dto.setEmployeeName(object[0] != null ? object[0].toString() : null);
			dto.setEmployeementId(object[1] != null ? Long.parseLong(object[1].toString()) : null);
			dto.setPipId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
			dto.setPipReason(object[3] != null ? object[3].toString() : null);
			dto.setCreatedByName(object[4] != null ? object[4].toString() : null);
			dto.setCreatedOn(object[5] != null ? object[5].toString() : null);
			dto.setUpdatedByName(object[6] != null ? object[6].toString() : null);
			dto.setUpdatedOn(object[7] != null ? object[7].toString() : null);

			dto.setRevReason(object[8] != null ? object[8].toString() : null);
			dto.setPipFlag(object[9] != null ? object[9].toString() : null);
			dto.setAging(object[10] != null ? Long.parseLong(object[10].toString()) : null);
			dto.setStartDate(object[11] != null ? object[11].toString() : null);
			dto.setEndDate(object[12] != null ? object[12].toString() : null);
			dto.setExtendDays(object[13] != null ? object[13].toString() : null);
			dto.setExtendReason(object[14] != null ? object[14].toString() : null);

			System.err.println(" dto   " + "\n" + dto);
			dtoList.add(dto);

		});
		if (dtoList != null) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(dtoList);
		}

		return response;
	}
	
	
//	create cron for reminder mail to the manager and HR of PIP users
	
//	@Scheduled(cron = "${pip_reminder_mail}")
	public void createCronForPIPUserReminderMailToManagerAndHR() throws AddressException, MessagingException {

		List<Object[]> listOfAddedPipUser = employeeRepository.findPipUserWithStatus();
		Long empId = null;
		String managerEmail = null;
		Long extendDays = null;
		LocalDate createdOn = null;
		String employeeEmail = null;
		String name = null;
		String managerName = null;
		String subject = null;
		for (Object[] object : listOfAddedPipUser) {
			empId = object[0] != null ? Long.parseLong(object[0].toString()) : null;
			employeeEmail = object[1] != null ? object[1].toString() : null;
			createdOn = object[2] != null ? LocalDate.parse(object[2].toString()) : null;
			managerEmail = object[3] != null ? object[3].toString() : null;
			extendDays = object[4] != null ? Long.parseLong(object[4].toString()) : null;
			name = object[5] != null ? object[5].toString() : null;
			managerName = object[6] != null ? object[6].toString() : null;
			
			System.out.println("empId   " + empId);
			System.out.println("createdOn   " + createdOn);
			System.out.println("managerEmail  " + managerEmail);
			System.out.println("extendDays    " + extendDays);

			LocalDate reminderMailDate = null;
			if (extendDays == null) {
				reminderMailDate = createdOn.plusDays(reminderMailDays);
				if (reminderMailDate.equals(LocalDate.now())) {
					subject = "Attention - Regarding Reminder mail";
					System.out.println(" Hii    mail is triggered ");

					mailService.sendMailWithCC(employeeEmail, managerEmail + "," + hrMailAddress, subject, "Dear " + managerName
							+ "," + "<br><br>"
							+ "&nbsp;&nbsp;This email is to formally notify you that "+name+" has completed PIP period if their performance isn't upto the mark then you can extend PIP."+"<br><br>"
							+ " Sincerely,"+"<br>"
							+ "Team HR - ApMoSys Technologies");

				}
			} else {
				subject = "Regarding PIP extend";
				reminderMailDate = createdOn.plusDays(reminderMailDays + extendDays);
				
				if (reminderMailDate.equals(LocalDate.now())) {

					System.out.println(" Hii    mail is triggered ");

					mailService.sendMailWithCC(employeeEmail, managerEmail + "," + hrMailAddress, subject, "Dear " + name
							+ "," + "<br><br>"
							+ "&nbsp;&nbsp;This email is to formally notify you that your PIP duration has been extend for some period."+"<br><br>"
							+ " Sincerely,"+"<br>"
							+ "Team HR - ApMoSys Technologies");

				}
			}
			System.err.println(" createdOn =  " + createdOn + " and reminderMailDate = " + reminderMailDate);
			System.err.println("reminderMailDate    " + reminderMailDate);
			

		}

	}
	
	public ServiceResponse setExtendPeriodByPipId(LeaveDTO leaveDto) throws AddressException, MessagingException {
		ServiceResponse response = new ServiceResponse();

		Employee findEmp = employeeRepository.findByEmpId(leaveDto.getEmpId());
		Employee findManager = employeeRepository.findByEmpId(findEmp.getManagerId());
		String extendReason = leaveDto.getExtendReason();
		PIP findPip = pipRepository.findByPipId(leaveDto.getPipId());

		findPip.setExtendDays("Yes");
		findPip.setUpdatedBy(leaveDto.getUpdatedByName());
		findPip.setEndDate(leaveDto.getEndDate());
		findPip.setExtendReason(leaveDto.getExtendReason());

		findPip.setUpdatedOn(LocalDateTime.now());

		PIP updatePip = pipRepository.save(findPip);

		if (updatePip != null) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("PIP extended successfully !! ");

			String employeeEmail = findEmp.getEmail();
			String managerEmail = findManager.getEmail();
			String name = findEmp.getName();

			String subject = "Regarding PIP extend";
			mailService.sendMailWithCC(employeeEmail, managerEmail + "," + hrMailAddress, subject, "Dear " + name + ","
					+ "<br><br>"
					+ "&nbsp;&nbsp;This email is to formally notify you that your PIP duration has been extend for some period."
					+ "<br>" + "&nbsp;&nbsp;" + "<b>" + " Extend Reason : </b> " + "&nbsp;&nbsp; " + extendReason + "."
					+ "<br><br>" + " Sincerely," + "<br>" + "Team HR - ApMoSys Technologies");

		} else {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("PIP not extended !! ");
		}
		return response;
	}
	public ServiceResponse getPipDetailsByEmpId(LeaveDTO leaveDto) {
		ServiceResponse response = new ServiceResponse();

		List<PIP> findPipDetails = pipRepository.getPipDetailsByEmployeeId(leaveDto.getEmpId());

		PIP findPIP = findPipDetails.get(0);

		if (findPIP != null) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(findPIP);
		} else {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(" PIP details not found");
		}
		return response;
	}

	public ServiceResponse isManager(LeaveDTO leaveDto) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/isManager");
	    apiLogInfo.setLogLevel("INFO");
	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("Leave ID: ").append(leaveDto.getLeaveId()).append(", Current User ID: ").append(leaveDto.getCurrentUserEmpId());

	    try {
	    	List<Object[]> leaveDataList = employeeLeaveRepository.getAllLeaveApplicationsByLeaveId(leaveDto.getLeaveId());

	    	if (leaveDataList.isEmpty()) {
	    	    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	    	    response.setServiceResponse("No leave applications found for the given leaveId.");
	    	    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	    	    apiLogInfo.setApiResponse("No leave applications found.");
	    	    return response;
	    	}

	        Object[] leaveData = leaveDataList.get(0);
	        Integer currentApprovalLevel = leaveData[22] instanceof Integer ? (Integer) leaveData[22] : 0;
	        
	        System.out.println("currentApprovalLevel : "+currentApprovalLevel);
	        
	        long approverId = 0;
	        switch (currentApprovalLevel) {
	            case 0:
	                approverId = leaveData[14] != null ? Long.parseLong(leaveData[13].toString()) : 0;
	                break;
	            case 1:
	                approverId = leaveData[15] != null ? Long.parseLong(leaveData[16].toString()) : 0;
	                break;
	            case 2:
	                approverId = leaveData[19] != null ? Long.parseLong(leaveData[20].toString()) : 0;
	                break;
	            default:
	                approverId = 0;
	        }


	        boolean isManager = approverId != 0 && approverId == leaveDto.getCurrentUserEmpId();
	        
	        System.out.println("isManager : "+isManager);

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(isManager);
	        apiLogInfo.setApiResponse(isManager ? "User is the authorised approver." : "User is not the authorised approver.");

	    } catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setLogLevel("ERROR");
	    }

	    apiLogInfo.setApiRequest(logBuilder.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    return response;
	}
	
	
		public ServiceResponse getAllMyTeamsApprovedLeaveApplicationsByManagerId(LeaveDTO leaveDTO) {
			ServiceResponse response = new ServiceResponse();
			LogDTO apiLogInfo = new LogDTO();
			apiLogInfo.setApiUrl("/api/getAllMyTeamsApprovedLeaveApplicationsByManagerId");
			apiLogInfo.setLogLevel("INFO");
			StringBuilder logBuilder = new StringBuilder();
			logBuilder.append("ManagerId : "+leaveDTO.getManagerId());
			
			try {
				List<Object[]> list = employeeLeaveRepository
						.getAllMyTeamsApprovedLeaveApplicationsByManagerId(leaveDTO.getManagerId());
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
						
						dto.setApproverName(object[14] != null ? object[14].toString() : null);
						//for approver level 1 empid emp 360
						dto.setLevel1ApproverId(object[15] != null ? Long.parseLong(object[15].toString()) : null);
						dto.setApproverEmail(object[16] != null ? object[16].toString() : null);
						dto.setManagerApprovalStatus(object[17] != null ? object[17].toString() : null);
						dto.setLevel2ApproverId(object[18] != null ? Long.parseLong(object[18].toString()) : null);
						dto.setLevel2ApproverName(object[19] != null ? object[19].toString() : null);
						dto.setLevel2ApproverEmail(object[20] != null ? object[20].toString() : null);
						dto.setLevel2ApprovalStatus(object[21] != null ? object[21].toString() : null);

						dto.setLevel3ApproverId(object[22] != null ? Long.parseLong(object[22].toString()) : null);
						dto.setLevel3ApproverName(object[23] != null ? object[23].toString() : null);
						dto.setLevel3ApprovalStatus(object[24] != null ? object[24].toString() : null);
						dto.setLevel3ApproverEmail(object[25] != null ? object[25].toString() : null);

						dto.setCurrentApprovalLevel(object[26] != null ? Integer.parseInt(object[26].toString()) : null);
						dto.setFinalApprovalLevel(object[27] != null ? Integer.parseInt(object[27].toString()) : null);
						dto.setLeaveEmpId(object[28] != null ? Long.parseLong(object[28].toString()) : null);
						dto.setManagerId(object[29] != null ? Integer.parseInt(object[29].toString()) : null);
						dto.setClientName(object[30] != null ? object[30].toString() : null);
						dto.setTeamName(object[31] != null ? object[31].toString() : null);
						
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
		
		public ServiceResponse getEmpIdToExcludeFromLeave(LeaveExcludeIncludeDTO request) {
		    ServiceResponse response = new ServiceResponse();
		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setApiUrl("/api/getEmpIdToExcludeFromLeave");
		    apiLogInfo.setLogLevel("INFO");
		    StringBuilder logBuilder = new StringBuilder();
		    logBuilder.append("EmployeeId : " + request.getEmpIds());

		    try {
		    	
		    	if (request.getEmpIds() == null || request.getEmpIds().isEmpty()) {
		    	    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		    	    response.setServiceResponse("Employee list is empty.");
		    	    return response;
		    	}

		    	
	    		List<EmployeeexcludedFromLeave> existingRecords =
	    		        employeeexcludedFromLeaveRepository.findByEmpIdIn(request.getEmpIds());
    		    
	    		Set<Long> existingEmpIds = existingRecords.stream()
    		            .map(EmployeeexcludedFromLeave::getEmpId)
    		            .collect(Collectors.toSet());

    		    List<Long> newEmpIds = request.getEmpIds().stream()
    		            .filter(empId -> !existingEmpIds.contains(empId))
    		            .collect(Collectors.toList());

		    	if(request.getIsExclude()) {

		    		    existingRecords.forEach(record -> {
		    		        record.setIsExcluded(true);
		    		        record.setUpdatedOn(LocalDateTime.now());
		    		        record.setCreatedBy(request.getCreatedBy());    	
		    		        });

		    		    List<EmployeeexcludedFromLeave> newEntities = newEmpIds.stream()
		    		            .map(empId -> {
		    		                EmployeeexcludedFromLeave e = new EmployeeexcludedFromLeave();
		    		                e.setEmpId(empId);
		    		                e.setCreatedOn(LocalDateTime.now());
		    		                e.setUpdatedOn(LocalDateTime.now());
		    		                e.setIsExcluded(true);
		    		                e.setCreatedBy(request.getCreatedBy());
		    		                return e;
		    		            })
		    		            .collect(Collectors.toList());

		    		    existingRecords.addAll(newEntities);
		        		
		    	}else {
	    		    existingRecords.forEach(record -> {
	    		        record.setIsExcluded(false);
	    		        record.setUpdatedOn(LocalDateTime.now());
	    		        record.setCreatedBy(request.getCreatedBy());    	
	    		    });

	    		    List<EmployeeexcludedFromLeave> newEntities = newEmpIds.stream()
	    		            .map(empId -> {
	    		                EmployeeexcludedFromLeave e = new EmployeeexcludedFromLeave();
	    		                e.setEmpId(empId);
	    		                e.setCreatedOn(LocalDateTime.now());
	    		                e.setUpdatedOn(LocalDateTime.now());
	    		                e.setIsExcluded(false);
	    		                e.setCreatedBy(request.getCreatedBy());
	    		                return e;
	    		            })
	    		            .collect(Collectors.toList());

	    		     existingRecords.addAll(newEntities);
		    	   }
		        if (!existingRecords.isEmpty()) {
	    		    employeeexcludedFromLeaveRepository.saveAll(existingRecords);	
		        }

		        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		        String msg = existingRecords.size() + " employee(s)  " + 
		                   (request.getIsExclude() ? "excluded" : "included") + " from leave.";
		        
		        response.setServiceResponse(msg);

				apiLogInfo.setApiResponse(msg);
		        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

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
