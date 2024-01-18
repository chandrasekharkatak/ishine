package com.apmosys.employeeportal.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.HolidayDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.CompOffLeave;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.Holiday;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.model.LeavePolicyMaster;
import com.apmosys.employeeportal.model.LeaveRevokeApplication;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.model.TimesheetActivityMap;
import com.apmosys.employeeportal.repository.CompOffLeaveRepository;
import com.apmosys.employeeportal.repository.CompOffMasterRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.HolidayRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.repository.LeavePolicyMasterRepository;
import com.apmosys.employeeportal.repository.LeaveRevokeApplicationRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.repository.TimesheetActivityMapRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
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
		
	@Transactional
	public ServiceResponse applyLeave(LeaveDTO leaveDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Apply Leave");
		apiLogInfo.setApiUrl("/api/applyLeave");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("empId : "+leaveDTO.getEmpId()+ ", leaveTypeMasterId : "+ leaveDTO.getLeaveTypeMasterId()+", leaveTypeCode : "+ leaveDTO.getLeaveTypeCode() +", noOfDays : "+ leaveDTO.getNoOfDays());
		System.out.println(leaveDTO);
		try {

//			LeaveTypeMaster leaveTypeMasterObj = leaveTypeMasterRepository.findByLeaveTypeCode(leaveDTO.getLeaveTypeCode());	
			
			// added by anurag
			EmployeeLeave recentLeaves = employeeLeaveRepository.findRecentLeavesByEmpId(leaveDTO.getEmpId()).get(0);

//	        if (!recentLeaves.isEmpty()) {
//	            for (EmployeeLeave recentLeave : recentLeaves) {
	                LocalDate newLeaveFromDate = LocalDate.parse(leaveDTO.getFromDate());
	                LocalDate recentLeaveToDate = recentLeaves.getToDate();
//	                System.err.println("recentLeaveToDate    ::  "+recentLeaveToDate);
	                if (newLeaveFromDate.isEqual(recentLeaveToDate.plusDays(1))) {
	                	
	                    if (!recentLeaves.getLeaveTypeMasterId().equals(leaveDTO.getLeaveTypeMasterId())) {
	                        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                        response.setServiceResponse("Two different types of leaves are not allowed on consecutive days. Please ensure that the same type of leave is applied for consecutive days.");
	                        return response;
	                    }
	                } else {
	                	System.err.println("recentLeaveToDate    ::  "+recentLeaveToDate);
	                	boolean isWeekOff = this.isWeekOffFind(recentLeaveToDate.plusDays(1),newLeaveFromDate.minusDays(1));
	                	if(isWeekOff) {
	                		if((!leaveDTO.getLeaveTypeMasterId().equals(recentLeaves.getLeaveTypeMasterId()))) {
	                			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		                        response.setServiceResponse("Two different types of leaves are not allowed on consecutive days.Week Offs also consider your last leave To date");
		                        return response;
	                		}
	                	}
	                }
//	            }
//	        }
			
			
			
			EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository
					.findByEmpIdAndLeaveTypeMasterId(leaveDTO.getEmpId(), leaveDTO.getLeaveTypeMasterId());
			
			Optional<LeaveTypeMaster> leavetype = leaveTypeMasterRepository.findById(leaveDTO.getLeaveTypeMasterId());
			
			System.out.println("Leave DTO check :"+leaveDTO);
			
			List<Object[]> empObj = employeeRepository.getManagerEmail(leaveDTO.getEmpId());
			
			// added by anurag
			
			Employee findEmployee= employeeRepository.findByEmpId((long)leaveDTO.getManagerId());
			System.out.println(" Reporting manager details : "+findEmployee);
			
			EmployeeDTO empDto = new EmployeeDTO();
			
				empObj.forEach((object) -> {
					
					empDto.setEmail(object[0] != null ? object[0].toString() : null);
					empDto.setManagerEmail(object[1] != null ? object[1].toString() : null);
					empDto.setName(object[2] != null ? object[2].toString() : null);
					empDto.setEmployeementId(object[3] != null ? Long.parseLong(object[3].toString()): null);
					empDto.setManagerName(object[4] != null ? object[4].toString() : null);
					});
				
				System.out.println("Senior manager data :: "+findEmployee.getManagerId());
				
			Float availableCompOffBalance = 0.0F;
			if(leaveDTO.getLeaveTypeCode().equalsIgnoreCase("CO")) {
				ServiceResponse compOffResponse = compOffLeaveService.getCompOffBalanceDetailsByEmpIdAndFromDate(leaveDTO);
				
				if(compOffResponse.getServiceStatus().equals("Success")) {
					List<LeaveDTO> availableCompOffList = (List<LeaveDTO>)compOffResponse.getServiceResponse();
					
					for(LeaveDTO compOff: availableCompOffList){
						availableCompOffBalance = availableCompOffBalance + compOff.getNoOfDays();
					};
				}
			}
				
			
			// HERE : Effective Leave Balance = employeeLeavesMap.getBalance()
			if (!leaveDTO.getLeaveTypeCode().equalsIgnoreCase("LWP") && (employeeLeavesMap.getBalance() == 0
					|| employeeLeavesMap.getBalance() < leaveDTO.getNoOfDays())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Your available balance of " + employeeLeavesMap.getBalance()
						+ " day(s) is not sufficient for this Leave Application.");
				return response;
			}else if(leaveDTO.getLeaveTypeCode().equalsIgnoreCase("CO") && availableCompOffBalance < leaveDTO.getNoOfDays()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Your available Compensatory off balance of " + availableCompOffBalance
						+ " day(s) before "+ leaveDTO.getFromDate() +" is not sufficient for this Leave Application.");
				return response;
			}
			
			// added by anurag for CL
			
			if(leaveDTO.getLeaveTypeCode().equalsIgnoreCase("CL") && leaveDTO.getNoOfDays()>clLeaveDays) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);	
				response.setServiceResponse("Casual Leave Can't take more than "+clLeaveDays+" days");
				return response;
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);	
				response.setServiceResponse("Leave application submitted. Your timesheet will be automatically added by system");
			}
			
			
			
			// ended

			EmployeeLeave leaveApplication = new EmployeeLeave();

			leaveApplication.setEmpId(leaveDTO.getEmpId());
			leaveApplication.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
			leaveApplication.setLeaveStatusId((short) 1);
			leaveApplication.setFromDate(stringToDateTimeParser.getDate(leaveDTO.getFromDate(), "yyyy-MM-dd"));
			leaveApplication.setToDate(stringToDateTimeParser.getDate(leaveDTO.getToDate(), "yyyy-MM-dd"));
			leaveApplication.setNoOfDays((Float) leaveDTO.getNoOfDays());
			leaveApplication.setReason(leaveDTO.getReason());
			leaveApplication.getCommonProperty().setCreatedBy(leaveDTO.getCreatedBy());
			leaveApplication.setFromDateDayType(leaveDTO.getFromDateDayType());
			leaveApplication.setToDateDayType(leaveDTO.getToDateDayType());	
			
			/* ----------- Multi-Level Approval ---------- */
			// Approval Status : NA - Pending - Approved - Rejected
			leaveApplication.setFinalApprovalLevel(leaveDTO.getFinalApprovalLevel());
			
			// By Default Current Approval level will be 1 i.e. Manager Approval			
			
			leaveApplication.setCurrentApprovalLevel(1);
			// added by anurag
						if(!findEmployee.getEmploymentstatus().equals("InActive")) {
							System.out.println(" Manager is active "+leaveDTO.getManagerId());
							leaveApplication.setManagerId(leaveDTO.getManagerId());
						}else {
							leaveApplication.setManagerId(Math.toIntExact(findEmployee.getManagerId()));
							System.err.println(" in case of inactive manager "+findEmployee.getManagerId());
						}
//			leaveApplication.setManagerId(leaveDTO.getManagerId());
			leaveApplication.setManagerApprovalStatus("Pending");
			
			if(leaveApplication.getFinalApprovalLevel() == 2) {
				leaveApplication.setLevel2ApproverId(leaveDTO.getLevel2ApproverId());
				leaveApplication.setLevel2ApprovalStatus("Pending");
				
				leaveApplication.setLevel3ApproverId(null);
				leaveApplication.setLevel3ApprovalStatus("NA");
				
			}else if(leaveApplication.getFinalApprovalLevel() == 3) {
				leaveApplication.setLevel2ApproverId(leaveDTO.getLevel2ApproverId());
				leaveApplication.setLevel2ApprovalStatus("Pending");
				
				leaveApplication.setLevel3ApproverId(leaveDTO.getLevel3ApproverId());
				leaveApplication.setLevel3ApprovalStatus("Pending");
			}else {
				leaveApplication.setLevel2ApproverId(null);
				leaveApplication.setLevel2ApprovalStatus("NA");
				
				leaveApplication.setLevel3ApproverId(null);
				leaveApplication.setLevel3ApprovalStatus("NA");
			}

			// Leave deduction from balance leaves
			Float balance = employeeLeavesMap.getBalance();
			if(leaveDTO.getLeaveTypeCode().equalsIgnoreCase("LWP")) {
				balance = 0F;
			}else {
				balance = balance - leaveDTO.getNoOfDays();
			}
			Float pendingForApproval = employeeLeavesMap.getPendingForApproval();
			pendingForApproval = pendingForApproval + leaveDTO.getNoOfDays();

			employeeLeavesMap.setBalance(balance);
			employeeLeavesMap.setPendingForApproval(pendingForApproval);

			EmployeeLeavesMap dbResponse1 = employeeLeavesMapRepository.save(employeeLeavesMap);

			EmployeeLeave dbResponse2 = employeeLeaveRepository.save(leaveApplication);

			if (dbResponse1 != null && dbResponse2 != null) {
				
				// 4 : compOff leave type Id
				if(leavetype.get().getLeaveTypeCode().equals("CO")) {
					long elapsedDays = ChronoUnit.DAYS.between(dbResponse2.getFromDate(),dbResponse2.getToDate());
					
					if(elapsedDays == 0) {
							CompOffLeave oldestCompOffApplication = compOffLeaveRepository.findOldestCompOffApplicationByEmpId(dbResponse2.getEmpId(), "Pending");
							
							if(oldestCompOffApplication != null) {
								oldestCompOffApplication.setCompOffStatus("Pending For Approval");
								oldestCompOffApplication.setLeaveId(dbResponse2.getLeaveId());
								
								compOffLeaveRepository.save(oldestCompOffApplication);
							}
					}
					if((elapsedDays != 0)) {
						LocalDate tempDate = dbResponse2.getFromDate();

						while(tempDate.compareTo(dbResponse2.getToDate()) != 1) {
								CompOffLeave oldestCompOffApplication = compOffLeaveRepository.findOldestCompOffApplicationByEmpId(dbResponse2.getEmpId(), "Pending");
								
								if(oldestCompOffApplication != null) {
									oldestCompOffApplication.setCompOffStatus("Pending For Approval");
									oldestCompOffApplication.setLeaveId(dbResponse2.getLeaveId());
									
									compOffLeaveRepository.save(oldestCompOffApplication);
								}
								tempDate = tempDate.plusDays(1);
							}
						}
				}
				
				LeaveBalanceLog log = new LeaveBalanceLog();
				
				log.setBalance(balance);
				log.setEmpId(leaveDTO.getEmpId());
				log.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
				log.setMessage(LeaveLogMessage.requestDeductLeave.replace("0.0", leaveDTO.getNoOfDays().toString()));
				log.setUpdateBalanceBy("-" + leaveDTO.getNoOfDays());

				leaveBalanceLogRepository.save(log);

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				
				if(LocalDate.parse(leaveDTO.getFromDate()).isBefore(LocalDate.now())) {
					response.setServiceResponse("Leave application submitted. If you already filled the timesheet,that will be automatically updated by system");
				}else {
					response.setServiceResponse("Leave application submitted. Your timesheet will be automatically added by system");
				}
				
				LeaveTypeMaster leaveType = leaveTypeMasterRepository.findByLeaveTypeCode(leaveDTO.getLeaveTypeCode());
				
				String managerEmail = "";
				if(!leaveDTO.getApproverEmail().equals(empDto.getManagerEmail())) {
					managerEmail = ","+ empDto.getManagerEmail();
				}
				
				if(leaveApplication.getFinalApprovalLevel() == 2) {
					managerEmail = ","+ leaveDTO.getLevel2ApproverEmail();
					
				}else if(leaveApplication.getFinalApprovalLevel() == 3) {
					managerEmail = ","+ leaveDTO.getLevel2ApproverEmail() + ","+ leaveDTO.getLevel3ApproverEmail();
				}
				
				if(leaveDTO.getCreatedBy().equals(leaveDTO.getEmpId())){
					//Leave Applied for self
					
					mailService.sendMailWithCC(leaveDTO.getApproverEmail(), hrMailAddress +","+ leaveDTO.getEmail()+ managerEmail,
							"Regarding Leave Application Request",
							"Dear "+ leaveDTO.getApproverName() + ","+"<br>"
							+"<br>"+" &nbsp"+" &nbsp"+" "+"Leave Application has been applied by "+ leaveDTO.getName() +" "+"for "+leaveDTO.getNoOfDays()+" day(s), Please take necessary action."+
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
					Optional<Employee> createdByEmp = employeeRepository.findById(leaveDTO.getCreatedBy());
					if(!createdByEmp.isEmpty()) {
						Employee createdByObj = createdByEmp.get();
						
						mailService.sendMailWithCC(leaveDTO.getApproverEmail(), hrMailAddress +","+ leaveDTO.getEmail() +","+ createdByObj.getEmail()+ managerEmail,
								"Regarding Leave Application Request",
								"Dear "+ leaveDTO.getApproverName() + ","
								+"<br> Leave Application has been applied for "+ leaveDTO.getName() +" for "+leaveDTO.getNoOfDays()+" day(s)"+" by "+createdByObj.getName()+","+"Please take necessary action."
								+"<br><br> Leave Details :"
								+"<br> EmpId : A-" + leaveDTO.getEmployeementId()
								+"<br> Name : " + leaveDTO.getName()
								+"<br> From Date : " + leaveDTO.getFromDate() 
								+"<br> To Date : " + leaveDTO.getToDate()
								+"<br> No. Of Days : " + leaveDTO.getNoOfDays() +" day(s)"
								+"<br> Leave Type :"+" "+leavetype.get().getLeaveType()
								+"<br> Leave reason : " + leaveDTO.getReason());
					}
				}
			
				apiLogInfo.setApiResponse("Leave application submitted.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);	
				System.out.println(leaveDTO.getFromDate());
				System.out.println(leaveDTO.getToDate());

				// IF Employee is Applying Leave for Half Day then, Automatic timesheet will not be filled as Leave
				if(leaveDTO.getNoOfDays() > 0.5) {
					LocalDate fromDate = LocalDate.parse(leaveDTO.getFromDate());
					LocalDate toDate = LocalDate.parse( leaveDTO.getToDate());

					long elapsedDays = ChronoUnit.DAYS.between(fromDate,toDate);
					
					List<Object[]> holidayList = holidayRepository.getHolidayWeekOffSize(leaveDTO.getFromDate(), leaveDTO.getToDate(),leaveDTO.getState());
					
				    List<Timesheet> empTimeSheet = timesheetsRepository.findTimesheetOnLeaveDate(leaveDTO.getEmpId(),leaveDTO.getFromDate(),leaveDTO.getToDate());
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
				    List<Timesheet> empTimeSheetAfterDelete = timesheetsRepository.findTimesheetOnLeaveDate(leaveDTO.getEmpId(),leaveDTO.getFromDate(),leaveDTO.getToDate());

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
							
//							if(tempDateToday.isEqual(fromDate) && leaveDTO.getFromDateDayType() == 0.5) {
//								System.out.println("From Date is Half Day");
//							}else if(tempDateToday.isEqual(toDate) && leaveDTO.getToDateDayType() == 0.5) {
//								System.out.println("To Date is Half Day");
							if (tempDateToday.isEqual(fromDate) && Objects.equals(leaveDTO.getFromDateDayType(), 0.5)) {
							    System.out.println("From Date is Half Day");
							} else if (tempDateToday.isEqual(toDate) && Objects.equals(leaveDTO.getToDateDayType(), 0.5)) {
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
public boolean isWeekOffFind(LocalDate fromDate , LocalDate toDate) {
		
		System.out.println(" from date :: "+fromDate);
		System.out.println("toDate :: "+toDate);
		List<Holiday> weekOffFind = holidayRepository.findWeekOffCountByFromAndToDate(fromDate, toDate);
		System.err.println("weekOffFind   ::   "+weekOffFind.size());
		for (Holiday holiday : weekOffFind) {
			System.err.println(holiday.toString()+"\n");
		}
		
		if(weekOffFind.size()>0) 
			return true;
		
		else 
			return false;
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
				
				//Get Expiration Period of CompOff
				Employee empObj = employeeRepository.findByEmpId(leaveToBeDeleted.getEmpId());
				
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
						
						log.setUpdateBalanceBy("+" + leaveDTO.getNoOfDays());
						
						leaveBalanceLogRepository.save(log);
					}
				}
				

				// CompOff Leave : 4 (LeaveTypeMasterId)
				if(leaveTypeObj.getLeaveTypeCode().equals("CO")) {
					
					List<CompOffLeave> compOffLeave = compOffLeaveRepository.findByLeaveId(leaveToBeDeleted.getLeaveId());
					
					if(!compOffLeave.isEmpty()) {
							
							for(CompOffLeave leave: compOffLeave) {
								
								LocalDate expireDate = leave.getFromDate().plusDays(expirationPeriod);
								if(LocalDate.now().isAfter(expireDate) || LocalDate.now().isEqual(expireDate)) {
									
									leave.setCompOffStatus("Expired");
									compOffLeaveRepository.save(leave);
								}else {
									
									//Update Leave Balance
									EmployeeLeavesMap employeeLeavesMap = employeeLeavesMapRepository.findByEmpIdAndLeaveTypeMasterId(
											leaveToBeDeleted.getEmpId(), leaveToBeDeleted.getLeaveTypeMasterId());
									
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

				
				List<Timesheet> empTimeSheet = timesheetsRepository.findTimesheetOnLeaveDate(leaveDTO.getEmpId(),start,end);

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
						
						//Timesheet Update
						// IF Employee is Applying Leave for Half Day then, Automatic timesheet will not be filled as Leave
						if(leaveDTO.getNoOfDays() > 0.5) {
							LocalDate fromDate = LocalDate.parse(leaveDTO.getFromDate());
							LocalDate toDate = LocalDate.parse( leaveDTO.getToDate());

							long elapsedDays = ChronoUnit.DAYS.between(fromDate,toDate);
							
							List<Object[]> holidayList = holidayRepository.getHolidayWeekOffSize(leaveDTO.getFromDate(), leaveDTO.getToDate(), leaveDTO.getState());
							
						    List<Timesheet> empTimeSheet = timesheetsRepository.findTimesheetOnLeaveDate(leaveDTO.getEmpId(),leaveDTO.getFromDate(),leaveDTO.getToDate());
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
						    List<Timesheet> empTimeSheetAfterDelete = timesheetsRepository.findTimesheetOnLeaveDate(leaveDTO.getEmpId(),leaveDTO.getFromDate(),leaveDTO.getToDate());

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
					
					dto.setApproverName(object[14] != null ? object[14].toString() : null);
					dto.setApproverEmail(object[15] != null ? object[15].toString() : null);
					dto.setManagerApprovalStatus(object[16] != null ? object[16].toString() : null);
					dto.setLevel2ApproverId(object[17] != null ? Long.parseLong(object[17].toString()) : null);
					dto.setLevel2ApproverName(object[18] != null ? object[18].toString() : null);
					dto.setLevel2ApproverEmail(object[19] != null ? object[19].toString() : null);
					dto.setLevel2ApprovalStatus(object[20] != null ? object[20].toString() : null);
					
					dto.setLevel3ApproverId(object[21] != null ? Long.parseLong(object[21].toString()) : null);
					dto.setLevel3ApproverName(object[22] != null ? object[22].toString() : null);
					dto.setLevel3ApprovalStatus(object[23] != null ? object[23].toString() : null);
					dto.setLevel3ApproverEmail(object[24] != null ? object[24].toString() : null);
					
					dto.setCurrentApprovalLevel(object[25] != null ? Integer.parseInt(object[25].toString()) : null);
					dto.setFinalApprovalLevel(object[26] != null ? Integer.parseInt(object[26].toString()) : null);
					dto.setLeaveEmpId(object[27] != null ? Long.parseLong(object[27].toString()) : null);
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
					.findByEmpIdAndLeaveTypeMasterId(leaveDTO.getLeaveEmpId(), leaveDTO.getLeaveTypeMasterId());
			Optional<Employee> employee = employeeRepository.findById(leaveDTO.getEmpId());
			System.out.println("leaveDTO.getEmpId() : -- " +leaveDTO.getEmpId());
			System.out.println("leaveDTO.getLeaveTypeMasterId() : -- " +leaveDTO.getLeaveTypeMasterId());
			if (leaveApplication.isPresent()) {
				EmployeeLeave pendingLeaveApplication = leaveApplication.get();

				pendingLeaveApplication.setLeaveStatusUpdatedBy(leaveDTO.getLeaveStatusUpdatedBy());
				pendingLeaveApplication.getCommonProperty().setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());


				// 1 = pending , 2 = Approved , 3= Rejected
				if (leaveDTO.getLeaveStatusId() == 2) {
					
					if(leaveDTO.getCurrentApprovalLevel() == 2) {
						pendingLeaveApplication.setLevel2ApprovalStatus("Approved");
						pendingLeaveApplication.setCurrentApprovalLevel(3);				
					}else if(leaveDTO.getCurrentApprovalLevel() == 3) {
						pendingLeaveApplication.setLevel3ApprovalStatus("Approved");
					}else {
						pendingLeaveApplication.setManagerApprovalStatus("Approved");
						pendingLeaveApplication.setCurrentApprovalLevel(2);
					}
					
					// Final Approval
					if((leaveDTO.getCurrentApprovalLevel() == null && leaveDTO.getFinalApprovalLevel() == null ) || 
							(leaveDTO.getCurrentApprovalLevel() != null && leaveDTO.getFinalApprovalLevel() != null && leaveDTO.getCurrentApprovalLevel() == leaveDTO.getFinalApprovalLevel()) ) {
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
							if(!leaveDTO.getApproverEmail().equals(reportingManager.get().getEmail())) {
								managerEmail = ","+ reportingManager.get().getEmail();
							}
							
							// Level 2/3 Approver Email
							if(leaveDTO.getFinalApprovalLevel() == 2) {
								managerEmail = ","+ leaveDTO.getLevel2ApproverEmail();
								
							}else if(leaveDTO.getFinalApprovalLevel() == 3) {
								managerEmail = ","+ leaveDTO.getLevel2ApproverEmail() + ","+ leaveDTO.getLevel3ApproverEmail();
							}
							
							if(!approver.isEmpty()) {
								Employee approverObj = approver.get();
								mailService.sendMailWithCC(empObj.getEmail(), hrMailAddress +","+ approverObj.getEmail()+ managerEmail,
										"Regarding leave Approval",
										"Dear "+ empObj.getName() + ","
										+" <br> "+ "Your leave request from"+"&nbsp;"+ leaveDTO.getFromDate()+" to "+leaveDTO.getToDate() + " has been approved"
										+"<br><br> Leave Application Details :"
										+"<br> From Date : " + leaveDTO.getFromDate() + "   To Date : " + leaveDTO.getToDate()
										+"<br> No. Of Days : " + leaveDTO.getNoOfDays()
										+"<br> Leave Type : " + leaveDTO.getLeaveType()
										+"<br> Final Approval Status : Approved");
							}
						}
						
//						mailService.sendMail(leaveDTO.getEmail(),
//								"Regarding leave Approval ", 
//						"Dear "+leaveDTO.getEmployeeName()+","+
//						" <br> "+ 
//						" <br> "+ "Your leave request from"+"&nbsp;"+ leaveDTO.getFromDate()+" to "+leaveDTO.getToDate() + " has been approved");
						
						
						// CompOff Leave : 4 (LeaveTypeMasterId)
						if(leaveTypeObj.getLeaveTypeCode().equals("CO")) {
							
							List<CompOffLeave> compOffLeave = compOffLeaveRepository.findByLeaveId(pendingLeaveApplication.getLeaveId());
							
							if(!compOffLeave.isEmpty()) {
								compOffLeave.forEach((leave) -> {
											leave.setCompOffStatus("Availed");
											compOffLeaveRepository.save(leave);
								});
							}
						}
						
					}else {
						// Intermediate Approval i.e. Level 1 OR 2
						
						// Next and Current Approval Email 
						String nextApproverName = null;
						String nextApproverEmail = null;
						
						String currentApproverName = null;
						String currentApproverEmail = null;
						
						if(leaveDTO.getCurrentApprovalLevel() == 2 && leaveDTO.getFinalApprovalLevel() != 2) {
							nextApproverEmail = leaveDTO.getLevel3ApproverEmail();
							nextApproverName = leaveDTO.getLevel3ApproverName();
							
							currentApproverEmail = leaveDTO.getLevel2ApproverEmail();
							currentApproverName = leaveDTO.getLevel2ApproverName();
							
						}else{
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
							if(!leaveDTO.getApproverEmail().equals(reportingManager.get().getEmail())) {
								managerEmail = ","+ reportingManager.get().getEmail();
							}
							
							// Level 2/3 Approver Email
							if(leaveDTO.getFinalApprovalLevel() == 2) {
								managerEmail = ","+ leaveDTO.getLevel2ApproverEmail();
								
							}else if(leaveDTO.getFinalApprovalLevel() == 3) {
								managerEmail = ","+ leaveDTO.getLevel2ApproverEmail() + ","+ leaveDTO.getLevel3ApproverEmail();
							}
							
							if(nextApproverName != null && currentApproverName != null) {
								mailService.sendMailWithCC(empObj.getEmail(), hrMailAddress +","+ currentApproverEmail+ managerEmail,
										"Regarding leave Approval",
										"Dear "+ empObj.getName() + ","
										+" <br> "+ "Your leave request from"+"&nbsp;"+ leaveDTO.getFromDate()+" to "+leaveDTO.getToDate() + " has been approved By "+ currentApproverName + "."
										+" <br> "+ "Next Approval will be done by"+"&nbsp;"+ nextApproverName+"."
										+"<br><br> Leave Application Details :"
										+"<br> From Date : " + leaveDTO.getFromDate() + "   To Date : " + leaveDTO.getToDate()
										+"<br> No. Of Days : " + leaveDTO.getNoOfDays()
										+"<br> Leave Type : " + leaveDTO.getLeaveType()
										+"<br> Final Approval Status : Pending");
							}
						}
						
					}
					
				} else if (leaveDTO.getLeaveStatusId() == 3) {
					
					pendingLeaveApplication.setLeaveStatusId((short) 3);
					
					if(leaveDTO.getCurrentApprovalLevel() == 2) {
						pendingLeaveApplication.setLevel2ApprovalStatus("Rejected");
						pendingLeaveApplication.setLevel3ApprovalStatus("NA");
					}else if(leaveDTO.getCurrentApprovalLevel() == 3) {
						pendingLeaveApplication.setLevel3ApprovalStatus("Rejected");
					}else {
						pendingLeaveApplication.setManagerApprovalStatus("Rejected");
						pendingLeaveApplication.setLevel2ApprovalStatus("NA");
						pendingLeaveApplication.setLevel3ApprovalStatus("NA");
					}
					
					//Get Expiration Period of CompOff
					Integer expirationPeriod = null;
					boolean isExpirationValid = false;
					Optional<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findById(leaveDTO.getLeaveTypeMasterId());
					LeaveTypeMaster leaveTypeObj = leaveType.get();
					if(leaveTypeObj.getLeaveTypeCode().equals("CO")) {
						LeavePolicyMaster leavePolicy  = leavePolicyMasterRepository.findByLeaveTypeMasterIdAndEmploymentStatus(leaveTypeObj.getLeaveTypeMasterId(),employee.get().getEmploymentstatus());
					      if(leavePolicy != null){
					    	  if(leavePolicy.getExpirationPeriod().equals("Yes")) {
					    		     isExpirationValid = true;
						        	 expirationPeriod = leavePolicy.getExpirationPeriodValue();
				               }
					      }
					}
					
					if(!leaveTypeObj.getLeaveTypeCode().equals("CO")) {
						pendingLeaveApplication.setRemark(leaveDTO.getRejectReason());
						
						Float balance = employeeLeavesMap.getBalance();
						if(leaveTypeObj.getLeaveTypeCode().equalsIgnoreCase("LWP")) {
							balance = 0F;
						}else {					
							balance = balance + pendingLeaveApplication.getNoOfDays();
							System.err.println("check balance :: "+balance);
						}
						
						employeeLeavesMap.setBalance(balance);
						

						LeaveBalanceLog log = new LeaveBalanceLog();
						log.setBalance(employeeLeavesMap.getBalance());
						log.setEmpId(leaveDTO.getEmpId());
						log.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId());
						if(leaveTypeObj.getLeaveTypeCode().equalsIgnoreCase("LWP")) {
							log.setMessage(LeaveLogMessage.requestAddLeave);
						}else {					
							log.setMessage(LeaveLogMessage.requestAddLeave.replace("0.0", pendingLeaveApplication.getNoOfDays().toString()));
						}
						log.setUpdateBalanceBy("+" + pendingLeaveApplication.getNoOfDays());
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
						if(!leaveDTO.getApproverEmail().equals(reportingManager.get().getEmail())) {
							managerEmail = ","+ reportingManager.get().getEmail();
						}
						
						// Level 2/3 Approver Email
						if(leaveDTO.getFinalApprovalLevel() == 2) {
							managerEmail = ","+ leaveDTO.getLevel2ApproverEmail();
							
						}else if(leaveDTO.getFinalApprovalLevel() == 3) {
							managerEmail = ","+ leaveDTO.getLevel2ApproverEmail() + ","+ leaveDTO.getLevel3ApproverEmail();
						}
						
						if(!approver.isEmpty()) {
							Employee approverObj = approver.get();
							mailService.sendMailWithCC(empObj.getEmail(), hrMailAddress +","+ approverObj.getEmail()+ managerEmail,
									"Regarding leave Rejection",
									"Dear "+ empObj.getName() + ","
									+" <br> "+ "Your leave request from"+"&nbsp;"+ leaveDTO.getFromDate()+" to "+leaveDTO.getToDate() + " has been rejected"
									+"<br><br> Leave Application Details :"
									+"<br> From Date : " + leaveDTO.getFromDate() + "   To Date : " + leaveDTO.getToDate()
									+"<br> No. Of Days : " + leaveDTO.getNoOfDays()
									+"<br> Leave Type : " + leaveDTO.getLeaveType()
									+"<br>"+" Reason -: "+leaveDTO.getRejectReason());
						}
					}
					 //Autofill timesheet delete on rejecting leave 
				    
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
				    DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				    
				    String start =  LocalDate.parse(leaveDTO.getFromDate(), formatter).format(formatter2);
				    String end =  LocalDate.parse(leaveDTO.getToDate(), formatter).format(formatter2);
					
				    List<Timesheet> empTimeSheet = timesheetsRepository.findTimesheetOnLeaveDate(leaveDTO.getEmpId(),start,end);

					if (empTimeSheet != null) {

						empTimeSheet.forEach((timesheet)->{
							timesheetsRepository.deleteById(timesheet.getTimesheetId());
						});
					}
//					mailService.sendMail(leaveDTO.getEmail(),
//							"Regarding leave Rejection ", 
//					" <br> "+"Dear "+leaveDTO.getEmployeeName()+","+
//					" <br> "+ "   Your leave has been rejected  "+
//							" <br>"+" Reason -: "+leaveDTO.getRejectReason());
				
					System.out.println(" leaveDTO.getEmployeementId() :  "+leaveDTO.getEmployeementId());
					System.out.println(" leaveDTO.getEmail()  :  "+leaveDTO.getEmail());
					System.out.println("  leaveDTO.getRejectReason()   :  "+leaveDTO.getRejectReason());
					
					// CompOff Leave : 4 (LeaveTypeMasterId)
					if(leaveTypeObj.getLeaveTypeCode().equals("CO")) {
						
						List<CompOffLeave> compOffLeave = compOffLeaveRepository.findByLeaveId(pendingLeaveApplication.getLeaveId());
						
						if(!compOffLeave.isEmpty()) {
								
								for(CompOffLeave leave: compOffLeave) {
									
									LocalDate expireDate = leave.getFromDate().plusDays(expirationPeriod);
									if(LocalDate.now().isAfter(expireDate) || LocalDate.now().isEqual(expireDate)) {
										pendingLeaveApplication.setRemark(leaveDTO.getRejectReason());
										
										leave.setCompOffStatus("Expired");
										compOffLeaveRepository.save(leave);
										
										response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
										response.setServiceResponse("CompOff Leave application rejected.");
										apiLogInfo.setApiResponse("CompOff Leave application rejected.");
									}else {
										
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

			Employee employee = employeeRepository.findByEmployeementId(leaveDTO.getEmployeementId());

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
						dto.setApplicationCount(object[0] != null ? Long.parseLong(object[0].toString()) : null);
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
						
					    List<Timesheet> empTimeSheet = timesheetsRepository.findTimesheetOnLeaveDate(leaveDTO.getEmpId(),start,end);

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
											employeeLeavesMap.setPendingForApproval(pendingForApproval);
											
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
								.findByEmpIdAndLeaveTypeMasterId(object.getEmpId(), (short) 18); // 18 is in local and Uat and 5 is in Prod
						
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
}
