package com.apmosys.employeeportal.service;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.BioMaTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.MilestoneExpireDto;
import com.apmosys.employeeportal.dto.ProjectPoPortalDTO;
import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.dto.ResourceRequirementDTO;
import com.apmosys.employeeportal.dto.RmAndHodEmailDto;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.BiomaxDefaulter;
import com.apmosys.employeeportal.model.BiomaxRequest;
import com.apmosys.employeeportal.model.BirthdayMail;
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.ClientLocation;
import com.apmosys.employeeportal.model.CompOffLeave;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.Holiday;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.model.LeavePolicyMaster;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.model.PortalConfig;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectDepartmentMap;
import com.apmosys.employeeportal.model.ProjectPo;
import com.apmosys.employeeportal.model.ResourceRequirement;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.model.UserSession;
import com.apmosys.employeeportal.repository.BiomaxDefaulterRepository;
import com.apmosys.employeeportal.repository.BiomaxRequestRepository;
import com.apmosys.employeeportal.repository.BirthdayMailRepository;
import com.apmosys.employeeportal.repository.ClientLocationRepository;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.CompOffLeaveRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.HolidayRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.repository.LeavePolicyMasterRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.repository.PortalConfigRepository;
import com.apmosys.employeeportal.repository.ProjectDepartmentMapRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.ResourceRequirementRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.repository.TimesheetActivityMapRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.repository.UserSessionRepository;
import com.apmosys.employeeportal.utility.LeaveLogMessage;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;
import com.fasterxml.jackson.annotation.JsonValue;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
@EnableAsync
public class CronJobService {
	@Value("${biomaxleavedeductForEmployee}")
	private String biomaxleavedeductForEmployee;
	@Value("${biomaxleavedeductForDepartment}")
	private String biomaxleavedeductForDepartment;
	@Autowired
	private BiomaxRequestRepository biomaxRequestRepository;
	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	ClientLocationRepository clientLocationRepository;
	
	@Autowired
	BioMaxService bioMaxService;
	
	@Autowired
	JobRoleRepository jobRoleRepository;
	//added by rahul

	
	//end of rahul 
	
	@Autowired
	LeaveTypeMasterRepository leaveTypeMasterRepository;
	
	@Autowired
	LeavePolicyMasterRepository leavePolicyMasterRepository;
	
	@Autowired
	EmployeeLeavesMapRepository employeeLeavesMapRepository;
	
	@Autowired				
	EmployeeRepository employeeRepository;
	
	@Autowired
	TimesheetsRepository timesheetsRepository;
	
	@Autowired
	HolidayRepository holidayRepository;
	
	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;
	
	@Autowired
	PortalConfigRepository portalConfigRepository;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	TimesheetService timesheetService;
	
	@Autowired
	TimesheetActivityMapRepository timesheetActivityMapRepository;
	
	@Autowired
	BirthdayMailRepository birthdayMailRepository;
	
	@Autowired
	LeaveBalanceLogRepository leaveBalanceLogRepository;
	
	@Autowired
	CompOffLeaveRepository compOffLeaveRepository;
	
	@Autowired
	DepartmentRepository departmentRepository;
	
	@Autowired
	TeamRepository teamRepository;
	
	@Autowired
	ClientsRepository clientsRepository;
	
	@Autowired
	AuthenticationService authenticationService;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Autowired
	ProjectDepartmentMapRepository projectDepartmentMapRepository;
	
	@Autowired
	ResourceRequirementRepository resourceRequirementRepository;
	
	@Autowired
	MailService mailService;
	
	@Autowired
	private UserSessionRepository userSessionRepository;
	
	@Autowired
	BiomaxDefaulterRepository biomaxDefaulterRepository;
	
	@Autowired
	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${po.db.url}")
	private String url;

	@Value("${po.db.username}")
	private String username;

	@Value("${po.db.password}")
	private String password;
	
	@Value("${hr.mail}")
	private String hrMailAddress;
	
	@Value("${timesheet.reconcile.days}")
	private Long timesheetReconcileDays;
	
	@Value("${billablechange.mail}")
	private String billablechangeMailAddress;
	
	@Value("${finance.mail}")
	private String financeMail;
	
	@Value("${allEmployeeDSR.file.location}")
	private String allEmployeeDSRFileLocation;
	
	@Value("${rmg.mail}")
	private String rmgMail;
	
	@Value("${poPortal.api.allProjects}")
	private String allPoPortalProjects;
	
	@Value("${resignation.consent.link}")
	private String resignationConsentLink;
	
	@Value("${usersession.inactive.timeout:10}")
	private Long userSessionInactiveTimeout;
	
	@Value("${valid.attempt:5}")
	private Long validAttempt;
	
	@Value("${leavetypeId}")
	private int leaveTypeId;
	
	@Value("${admin.mail}")
	private String adminMail;
	
 
	
	 @PersistenceContext
	 EntityManager entityManager;
	 
	 public CronJobService(EntityManager entityManager) {
	        this.entityManager = entityManager;
	    }
	 
	 

		
//0 0 12 1 * ?  - Every month on the 1st, at noon
//	0 0/2 * ? * *
//	@Scheduled(cron = "0 0 12 1 * ?")
//	 At 00:00 (midnight) on the first day of every month
	@Scheduled(cron = "0 0 0 1 * ?")
	public void monthlyLeaveIncrement() {
		try {
		     List<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findAll();
		     
		     List<Employee> employeeList = employeeRepository.findAll();
		     
		     LocalDate dateToday = LocalDate.now(); 
		     
		     LocalDate prevMonthStart = dateToday.minusMonths(1).withDayOfMonth(1);  //2024-11-01
		     
		     YearMonth thisYearMonth = YearMonth.of(prevMonthStart.getYear(), prevMonthStart.getMonthValue());  //2024, 11
		     
		     LocalDate prevMonthEnd = thisYearMonth.atEndOfMonth();   //2024-11-30
		     
		     if(!leaveType.isEmpty()) {
		    	 
		    	 for(LeaveTypeMaster ltm :leaveType) {
		        	  for(Employee employeeObj : employeeList) {
		        		  
		        		  System.out.println("checked     "+employeeObj.getEmploymentstatus().equals("Retain"));
		        		  
		        		  if(employeeObj.getEmploymentstatus().equals("Retain")) {
	        				  
	        				  System.out.println("@@@  "+employeeObj.getName());
	        				  
	        				  System.out.println("@@@  "+employeeObj.getIsRetain());	
	        				  }
		        		 
		        		  System.out.println(employeeObj.getEmploymentstatus() +"  "+ ltm.getLeaveTypeMasterId());
		        		  
		        		  Optional<LeavePolicyMaster> leavePolicy  = leavePolicyMasterRepository.
		        				  findByEmployentStatusAndLeaveTypeMasterId(employeeObj.getEmploymentstatus(),ltm.getLeaveTypeMasterId());
		        		  System.err.println("Red"+employeeObj.getEmpId() +"  "+ ltm.getLeaveTypeMasterId());
		        		  if(!leavePolicy.isEmpty() ) {
		        			 
		        			  LeavePolicyMaster leavePolicyObj = leavePolicy.get();

		        			  if(leavePolicyObj.getIncrement().equals("Yes")){
		        				  
		        				
		        				  
		        				  EmployeeLeavesMap employeeLeaveMap = employeeLeavesMapRepository.
		        						  findByEmpIdAndLeaveTypeMasterId(employeeObj.getEmpId(),ltm.getLeaveTypeMasterId());
		        				  
		        				  System.err.println("Red"+employeeObj.getEmpId() +"  "+ ltm.getLeaveTypeMasterId());
		        				  
		        				          if(employeeLeaveMap != null) {
		        				        	  
		        				        	  // Manage Balance if user In-Between a month
		        				        	  Boolean isContains = (employeeObj.getDateOfJoining().isBefore(prevMonthEnd) ) && (employeeObj.getDateOfJoining().isAfter(prevMonthStart));
		        				        	  float retainValue = 0.0F;
		        				        	  
		        				        	  if(isContains) {
		        				        		  Float newBalance = 0.0F;
		        				        		  
		        				        		    Period period = Period.between(employeeObj.getDateOfJoining(), dateToday);
		        									long elapsedDays = period.getDays();
		        									double leavesForDays = (double)((leavePolicyObj.getIncrementValue()*elapsedDays)/30);
		        									
		        									
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
		        									
		        									newBalance =  (float)(employeeLeaveMap.getBalance() + leavesForDays);
		        									
		        									employeeLeaveMap.setBalance(newBalance);
				        				        	 EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(employeeLeaveMap);
				        				        	 System.out.println("bsjhsdh"+employeeObj.getEmpId() +"  "+ ltm.getLeaveTypeMasterId());
				        				        	 if(dbResponse != null) {
															LeaveBalanceLog log = new LeaveBalanceLog();

															log.setBalance(newBalance);
															log.setEmpId(employeeObj.getEmpId());
															log.setLeaveTypeMasterId(ltm.getLeaveTypeMasterId());
															log.setMessage(LeaveLogMessage.autoAddLeave.replace("0.0",
																	leavePolicyObj.getIncrementValue().toString()));
															log.setUpdateBalanceBy("+" + leavePolicyObj.getIncrementValue());

															leaveBalanceLogRepository.save(log);
				        				        	  }
		        				        		  
		        				        	  }
		        				        	  else {
		        				        		  
		        				        		  

		        				        		  
		        				        		  LocalDate resignedDate = employeeObj.getDateOfResign();
		        				        		  LocalDate retainedDate = employeeObj.getDateOfRetain();
		        				        	      String  isRetain = employeeObj.getIsRetain();
		        				        	      System.out.println("Yesssss     "+employeeObj);
		        				        	      
		        				        	      System.out.println("1    "+resignedDate); 
		        				        	      System.out.println("2    "+retainedDate);
		        				        	      System.out.println("3   "+isRetain);
		        				        	      
		        				        	      System.out.println("1    "+employeeObj.getDateOfResign()); 
		        				        	      System.out.println("2    "+employeeObj.getDateOfRetain());
		        				        	      System.out.println("3   "+employeeObj.getIsRetain());
		        				        	      
		        				        	      if ("Yes".equals(isRetain)) { 
		        				        	    	  System.out.println("bsjhsdh"+employeeObj.getEmpId() +"  "+ ltm.getLeaveTypeMasterId());
		        				        	    	  
		        				        	    	    if (resignedDate != null && retainedDate != null) {
		        				        	    	    	System.err.println("jhbshj"+employeeObj.getEmpId());
		        				        	    	    	System.out.println("bsjhsdh"+employeeObj.getEmpId() +"  "+ ltm.getLeaveTypeMasterId());
		        				        	    	        LocalDate startDate = resignedDate;
		        				        	    	        LocalDate endDate = retainedDate;
		        				        	    	        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);

		        				        	    	        long fullMonths = totalDays / 30;
		        				        	    	        
		        				        	    	        System.out.print("fullMonth  "+fullMonths);

		        				        	    	        for (long i = 0; i < fullMonths; i++) {
		        				        	    	            retainValue = retainValue + leavePolicyObj.getIncrementValue();
		        				        	    	        }

		        				        	    	        if (retainValue > 0.0F) {
//		        				        	    	            executeQueryForRetain(employeeObj);
		        				        	    	        	System.err.println("jhbshj"+employeeObj.getEmpId());
		        				        	    	        	employeeRepository.updateIsRetain(employeeObj.getEmpId());
		        				        	    	        }
		        				        	    	    } else {
		        				        	    	        // Handle the case where either date is null
		        				        	    	        System.out.println("Warning: resignedDate or retainedDate is null. Skipping retain calculation.");
		        				        	    	        // Proceed with further execution
		        				        	    	    }
		        				        	    	}

		        			
		        				        		  
		        				        		  
		        				        		  
  

		        				        		  float newBalance = employeeLeaveMap.getBalance() + leavePolicyObj.getIncrementValue();
			        				        	  System.out.println(newBalance);
			        				        	  employeeLeaveMap.setBalance(newBalance);
			        				        	  EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(employeeLeaveMap);
			        				        	  
			        				        	  if(dbResponse != null) {
														LeaveBalanceLog log = new LeaveBalanceLog();

														log.setBalance(newBalance);
														log.setEmpId(employeeObj.getEmpId());
														log.setLeaveTypeMasterId(ltm.getLeaveTypeMasterId());
														log.setMessage(LeaveLogMessage.autoAddLeave.replace("0.0",
																leavePolicyObj.getIncrementValue().toString()));
														log.setUpdateBalanceBy("+" + leavePolicyObj.getIncrementValue());

														leaveBalanceLogRepository.save(log);
			        				        	  }
		        				        		  
			        				        	  
			        				        	  
			        				        	  
			        				        	  
			        				        	  
			        				        	  
		        				        	  }
		        				          }
		        			  }
		        		  }else {
		        			  System.out.println("Leave Policy not found");
		        		  }
		        	  }
		            }
		     }
		   }catch(Exception e) {
			e.printStackTrace();
		   }
	}
	
	
	
		
	
	// 0 0 0 31 MAR ? - AT 00:00 AT 31 DAY AT MARCH MONTH
	
//	@Scheduled(cron = "0 0 0 31 MAR ?")
//	public void YearlyLeaveCronJob() {
//		
//		short leaveTypeMasterId = 0;
//		try {
//		     List<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findAll();
//		     
//		          for(LeaveTypeMaster ltm :leaveType) {
//			      leaveTypeMasterId = ltm.getLeaveTypeMasterId();
//			
//			      List<LeavePolicyMaster> leavePolicy  = leavePolicyMasterRepository.findByLeaveTypeMasterId(leaveTypeMasterId);
//			      
//			          for(LeavePolicyMaster lpm : leavePolicy) {
//				         if(lpm.getCarryForward().equals("Yes") || lpm.getCarryForward().equals("No")) {
//					
//					         List<EmployeeLeavesMap> employeeLeaveMap = employeeLeavesMapRepository.findByLeaveTypeMasterId(leaveTypeMasterId);
//					       
//					              for(EmployeeLeavesMap elm :employeeLeaveMap) {
//						              float dbBalance = elm.getBalance();
//						              
//										if (lpm.getCarryForward().equals("No")) {
//											
//											float newBalance = 0;
//											elm.setBalance(newBalance);
//											EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(elm);
//											
//											if (dbResponse != null) {
//												LeaveBalanceLog log = new LeaveBalanceLog();
//
//												log.setBalance(newBalance);
//												log.setEmpId(elm.getEmpId());
//												log.setLeaveTypeMasterId(ltm.getLeaveTypeMasterId());
//												log.setMessage(LeaveLogMessage.autoDeductLeave.replace("0.0",
//														Float.toString(dbBalance)));
//												log.setUpdateBalanceBy("-" + dbBalance);
//
//												leaveBalanceLogRepository.save(log);
//											}
//										} else if (lpm.getCarryForward().equals("Yes")) {
//											float carryForwardValue = lpm.getCarryForwardValue();
//											
//											if (dbBalance > carryForwardValue) {
//												float newBalance = carryForwardValue;
//												float deductedLeaveCount = dbBalance - carryForwardValue;
//												elm.setBalance(newBalance);
//												EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(elm);
//
//												if (dbResponse != null) {
//													LeaveBalanceLog log = new LeaveBalanceLog();
//
//													log.setBalance(newBalance);
//													log.setEmpId(elm.getEmpId());
//													log.setLeaveTypeMasterId(ltm.getLeaveTypeMasterId());
//													log.setMessage(LeaveLogMessage.autoDeductLeave.replace("0.0",
//															Float.toString(deductedLeaveCount)));
//													log.setUpdateBalanceBy("-" + deductedLeaveCount);
//
//													leaveBalanceLogRepository.save(log);
//												}
//											}
//						              }
//					              }
//				          }
//			           }
//		            }
//		   }catch(Exception e) {
//			e.printStackTrace();
//		   }
//	}
	 
	// At 09:00 PM, on day 16th of the month, only in January
//	@Scheduled(cron = "0 0 21 16 01 ?")

//	@Scheduled(cron = "0 0 21 31 12 ?")
	@Scheduled(cron = "0 28 17 3 11 ?")


		public void YearlyLeaveCronJob() {
			System.out.println("*************************************************************************");
			
			short leaveTypeMasterId = 0;	//initialize with zero
			try {
			     List<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findAll();	//get all leave type
			     
			          for(LeaveTypeMaster ltm :leaveType) {
				      leaveTypeMasterId = ltm.getLeaveTypeMasterId();	//iterate each leave type and get the leaveTypeMasterId
				
				      List<LeavePolicyMaster> leavePolicy  = leavePolicyMasterRepository.findByLeaveTypeMasterId(leaveTypeMasterId);	//get the leave policy details from leaveTypeMasterId
				      
				          for(LeavePolicyMaster lpm : leavePolicy) {
					         if(lpm.getCarryForward().equals("Yes") || lpm.getCarryForward().equals("No")) {	//check either carry forwared is possible or not both
						
//						         List<EmployeeLeavesMap> employeeLeaveMap = employeeLeavesMapRepository.findByLeaveTypeMasterId(leaveTypeMasterId);		//get all the list of employee by using leaveTypeMasterId
					        	 List<Object[]> employeeLeaveMap = employeeLeavesMapRepository.getEmpDetailsByLeaveTypeMasterId(leaveTypeMasterId);
					        	 
//						              for(EmployeeLeavesMap elm :employeeLeaveMap) {
					              		for(Object[] object : employeeLeaveMap) {

					        	 		
//							              float dbBalance = elm.getBalance();	//store the DB balance
					              			float dbBalance = (object[1] != null ? Float.parseFloat(object[1].toString()) : null);
					              			
					              			Date dateOfJoin = (Date) (object[6]);
					              			Short probationDays = (object[5] != null ? Short.parseShort(object[5].toString()) : null);
					              	        Date probationCompleteDate = null;

					              			System.err.println("Prabation days ::   "+probationDays);

					              			if (probationDays != null) {
					              			    Calendar cal = Calendar.getInstance();
					              			    cal.setTime(dateOfJoin);
					              			    cal.add(Calendar.DAY_OF_MONTH, probationDays);
					              			    probationCompleteDate = cal.getTime();
					              			    System.err.println("Complete probation Period ::  "+probationCompleteDate);
					              			}
					              	        
					              	        int currentYear = LocalDate.now().getYear();
					              			
					              	        // Create Calendar instances for the start and end dates
					              	        Calendar startDate = Calendar.getInstance();
					              	        startDate.set(currentYear-1, Calendar.JULY, 1);
					              	        
					              	        Calendar endDate = Calendar.getInstance();
					              	        endDate.set(currentYear, Calendar.JANUARY, 10);
					              	        
					              	        System.out.println("Start Date and End Date     ::   "+startDate.getTime()+" = "+endDate.getTime());
					              	        
					              	        // Check if probationCompleteDate is between July 1, 2023, and December 31, 2023
					              	        boolean isWithinRange = probationCompleteDate.after(startDate.getTime()) && probationCompleteDate.before(endDate.getTime());
	               //false
					              	        System.err.println("probationCompleteDate.after(startDate.getTime())    :: "+startDate.getTime() );
					              	        System.err.println("probationCompleteDate.before(endDate.getTime()    ::    "+endDate.getTime());
					              	        if (!isWithinRange) {
					//true              	        	
					              	            System.out.println("Probation complete date is between July 1, 2023, and December 31, 2023.");
					              	            
												if (lpm.getCarryForward().equals("No")) {
													
//													List<EmployeeLeave> employeeLeave = employeeLeaveRepository.findAll();
													List<EmployeeLeave> employeeLeaveforApproved = employeeLeaveRepository.findByEmployeeforApproved();
													List<EmployeeLeave> employeeLeaveforPending = employeeLeaveRepository.findByEmployeeforPending();
													EmployeeLeavesMap elm = new EmployeeLeavesMap();
													EmployeeLeavesMap dbResponse = new EmployeeLeavesMap();


													List<EmployeeLeave> updatedEmployeeLeaveList = new ArrayList<>(); // List to hold mapped EmployeeLeave entities
													float newBalance = 0; 


													if(!employeeLeaveforApproved.isEmpty()) {
														for(EmployeeLeave leave : employeeLeaveforApproved) {
															if(leave != null) {
																
																Date createdOnDate = leave.getCommonProperty().getCreatedOn(); // Assuming this returns java.util.Date
																LocalDate createdOnLocalDate = createdOnDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
																System.out.println("Checking Year " + createdOnLocalDate.getYear());
																if(createdOnLocalDate.getYear() == (currentYear - 1) && 
																		leave.getFromDate().getYear() == currentYear) {
																	
																	newBalance=1;
																	LeaveDTO leaveDTO = new LeaveDTO();
																	leaveDTO.setLeaveStatusId((short)5);
																	leaveDTO.setManagerApprovalStatus("Revoked");
																	leaveDTO.setLevel2ApprovalStatus("NA");
																	leaveDTO.setLevel3ApprovalStatus("NA");
																	leaveDTO.setLevel2ApproverId(null);		
																	leaveDTO.setLevel3ApproverId(null);
																	leaveDTO.setReason("Application revoked as CL exceeded its limit !!");
																	leaveDTO.setUpdatedBy(3);
																	leaveDTO.setCurrentApprovalLevel(1);
																	leaveDTO.setFinalApprovalLevel(1);
																	
																	leave.setLeaveStatusId(leaveDTO.getLeaveStatusId());
													                leave.setManagerApprovalStatus(leaveDTO.getManagerApprovalStatus());
													                leave.setLevel2ApprovalStatus(leaveDTO.getLevel2ApprovalStatus());
													                leave.setLevel3ApprovalStatus(leaveDTO.getLevel3ApprovalStatus());
													                leave.setLevel2ApproverId(leaveDTO.getLevel2ApproverId());
													                leave.setLevel3ApproverId(leaveDTO.getLevel3ApproverId());
													                leave.setReason(leaveDTO.getReason());
													                leave.getCommonProperty().setUpdatedBy((Long.valueOf(leaveDTO.getUpdatedBy())));
													                leave.setCurrentApprovalLevel(leaveDTO.getCurrentApprovalLevel());
													                leave.setFinalApprovalLevel(leaveDTO.getFinalApprovalLevel());
																	
													                updatedEmployeeLeaveList.add(leave);
													                elm.setBalance(newBalance);
																	elm.setEmpId(leave.getEmpId());
																	elm.setLeaveTypeMasterId(leave.getLeaveTypeMasterId());
																	List<Long> employeeLeaveMapIds = employeeLeaveforApproved.stream()
																		    .map(record -> record.getEmpId())  
																		    .collect(Collectors.toList());     

																	List<Object[]> matchingEmployeeIds = employeeLeaveMap.stream()
																		    .filter(employeeLeave -> employeeLeaveMapIds.contains(((BigInteger) employeeLeave[2]).longValue())) 
																		    .collect(Collectors.toList());
																	EmployeeLeavesMap lst = employeeLeavesMapRepository.findByEmpIdAndLeaveTypeMasterId(elm.getEmpId(),elm.getLeaveTypeMasterId());
															        
																	if (!matchingEmployeeIds.isEmpty()) {
																	    matchingEmployeeIds.forEach(list -> {
																	        if (list[0] instanceof BigInteger) {
//																	            // Convert BigInteger to Long
																	        	elm.setEmployeeLeavesMapId(lst.getEmployeeLeavesMapId());
////																	            elm.setEmployeeLeavesMapId(((BigInteger) list[0]).longValue());
																	        } else if (list[0] instanceof Long) {
																	            // If it is already a Long, no conversion needed
																	        	elm.setEmployeeLeavesMapId(lst.getEmployeeLeavesMapId());
																	        } 
																	        if (list[4] instanceof Float) {
																	            elm.setPendingForApproval((Float) list[4]);
																	        } 
																	   });
																	}	
																	dbResponse = employeeLeavesMapRepository.save(elm);
																}
																
//																float newBalance = 1; 
													            if (createdOnLocalDate.getYear() == leave.getFromDate().getYear() &&
													                leave.getNoOfDays() > 1) {
													                newBalance = 1; 
													                LeaveDTO leaveDTO = new LeaveDTO();
													                leaveDTO.setLeaveStatusId((short) 5);
													                leaveDTO.setManagerApprovalStatus("Revoked");
													                leaveDTO.setLevel2ApprovalStatus("NA");
													                leaveDTO.setLevel3ApprovalStatus("NA");
													                leaveDTO.setLevel2ApproverId(null);
													                leaveDTO.setLevel3ApproverId(null);
													                leaveDTO.setReason("Application revoked as CL exceeded its limit !!");
													                leaveDTO.setUpdatedBy(3);
													                leaveDTO.setCurrentApprovalLevel(1);
													                leaveDTO.setFinalApprovalLevel(1);

													                leave.setLeaveStatusId(leaveDTO.getLeaveStatusId());
													                leave.setManagerApprovalStatus(leaveDTO.getManagerApprovalStatus());
													                leave.setLevel2ApprovalStatus(leaveDTO.getLevel2ApprovalStatus());
													                leave.setLevel3ApprovalStatus(leaveDTO.getLevel3ApprovalStatus());
													                leave.setLevel2ApproverId(leaveDTO.getLevel2ApproverId());
													                leave.setLevel3ApproverId(leaveDTO.getLevel3ApproverId());
													                leave.setReason(leaveDTO.getReason());
													                leave.getCommonProperty().setUpdatedBy(Long.valueOf(leaveDTO.getUpdatedBy()));
													                leave.setCurrentApprovalLevel(leaveDTO.getCurrentApprovalLevel());
													                leave.setFinalApprovalLevel(leaveDTO.getFinalApprovalLevel());

													                updatedEmployeeLeaveList.add(leave);
													                elm.setBalance(newBalance);
																	elm.setEmpId(leave.getEmpId());
																	elm.setLeaveTypeMasterId(leave.getLeaveTypeMasterId());
																	List<Long> employeeLeaveMapIds = employeeLeaveforApproved.stream()
																		    .map(record -> record.getEmpId())  
																		    .collect(Collectors.toList());     

																	List<Object[]> matchingEmployeeIds = employeeLeaveMap.stream()
																		    .filter(employeeLeave -> employeeLeaveMapIds.contains(((BigInteger) employeeLeave[2]).longValue())) 
																		    .collect(Collectors.toList());
																	EmployeeLeavesMap lst = employeeLeavesMapRepository.findByEmpIdAndLeaveTypeMasterId(elm.getEmpId(),elm.getLeaveTypeMasterId());
																	if (!matchingEmployeeIds.isEmpty()) {
																	    matchingEmployeeIds.forEach(list -> {
																	    	if (list[0] instanceof BigInteger) {
//																	            // Convert BigInteger to Long
																	        	elm.setEmployeeLeavesMapId(lst.getEmployeeLeavesMapId());
////																	            elm.setEmployeeLeavesMapId(((BigInteger) list[0]).longValue());
																	        } else if (list[0] instanceof Long) {
																	            // If it is already a Long, no conversion needed
																	        	elm.setEmployeeLeavesMapId(lst.getEmployeeLeavesMapId());
																	        } 
																	        if (list[4] instanceof Float) {
																	            elm.setPendingForApproval((Float) list[4]);
																	        }
																	    });
																	}
																	dbResponse = employeeLeavesMapRepository.save(elm);
													            }else if(createdOnLocalDate.getYear() == leave.getFromDate().getYear() &&
														                leave.getNoOfDays() == 1){
													            	newBalance = 0;
													            	elm.setBalance(newBalance);
																	elm.setEmpId(leave.getEmpId());
																	elm.setLeaveTypeMasterId(leave.getLeaveTypeMasterId());
																	List<Long> employeeLeaveMapIds = employeeLeaveforApproved.stream()
																		    .map(record -> record.getEmpId())  
																		    .collect(Collectors.toList());     

																	List<Object[]> matchingEmployeeIds = employeeLeaveMap.stream()
																		    .filter(employeeLeave -> employeeLeaveMapIds.contains(((BigInteger) employeeLeave[2]).longValue())) 
																		    .collect(Collectors.toList());
																	EmployeeLeavesMap lst = employeeLeavesMapRepository.findByEmpIdAndLeaveTypeMasterId(elm.getEmpId(),elm.getLeaveTypeMasterId());
																	if (!matchingEmployeeIds.isEmpty()) {
																	    matchingEmployeeIds.forEach(list -> {
																	       if (list[0] instanceof BigInteger) {
//																            // Convert BigInteger to Long
																        	elm.setEmployeeLeavesMapId(lst.getEmployeeLeavesMapId());
////																            elm.setEmployeeLeavesMapId(((BigInteger) list[0]).longValue());
																        } else if (list[0] instanceof Long) {
																            // If it is already a Long, no conversion needed
																        	elm.setEmployeeLeavesMapId(lst.getEmployeeLeavesMapId());
																        } 															       
																	        if (list[4] instanceof Float) {
																	            elm.setPendingForApproval((Float) list[4]);
																	        }
																	    });
																	}
																	
																	dbResponse = employeeLeavesMapRepository.save(elm);
													            }
															}
											
														}
														
													}
//													float newBalance = 0;
													if(!employeeLeaveforPending.isEmpty()) {
															for(EmployeeLeave leave : employeeLeaveforPending) {
																if(leave != null) {
																		Date createdOnDate = leave.getCommonProperty().getCreatedOn(); // Assuming this returns java.util.Date
																		LocalDate createdOnLocalDate = createdOnDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
																		System.out.println("Checking Year " + createdOnLocalDate.getYear());
																		if( createdOnLocalDate.getYear() == (currentYear - 1) && 
																				leave.getFromDate().getYear() == currentYear) {
																			
																				newBalance=1;
																				
																				LeaveDTO leaveDTO = new LeaveDTO();
																				leaveDTO.setLeaveStatusId((short)3);
																				leaveDTO.setManagerApprovalStatus("Rejected");
																				leaveDTO.setLevel2ApprovalStatus("NA");
																				leaveDTO.setLevel3ApprovalStatus("NA");
																				leaveDTO.setLevel2ApproverId(null);		
																				leaveDTO.setLevel3ApproverId(null);
																				leaveDTO.setReason("Application rejected as CL exceeded its limit !!");
																				leaveDTO.setUpdatedBy(leave.getManagerId());
																				leaveDTO.setCurrentApprovalLevel(1);
																				leaveDTO.setFinalApprovalLevel(1);
																				
																				leave.setLeaveStatusId(leaveDTO.getLeaveStatusId());
																                leave.setManagerApprovalStatus(leaveDTO.getManagerApprovalStatus());
																                leave.setLevel2ApprovalStatus(leaveDTO.getLevel2ApprovalStatus());
																                leave.setLevel3ApprovalStatus(leaveDTO.getLevel3ApprovalStatus());
																                leave.setLevel2ApproverId(leaveDTO.getLevel2ApproverId());
																                leave.setLevel3ApproverId(leaveDTO.getLevel3ApproverId());
																                leave.setReason(leaveDTO.getReason());
																                leave.getCommonProperty().setUpdatedBy((Long.valueOf(leaveDTO.getUpdatedBy())));
																                leave.setCurrentApprovalLevel(leaveDTO.getCurrentApprovalLevel());
																                leave.setFinalApprovalLevel(leaveDTO.getFinalApprovalLevel());
																				
																                updatedEmployeeLeaveList.add(leave);
																				elm.setBalance(newBalance);
																				elm.setEmpId(leave.getEmpId());
																				elm.setLeaveTypeMasterId(leave.getLeaveTypeMasterId());
																				
																				List<Long> employeeLeaveMapIds = employeeLeaveforPending.stream()
																					    .map(record -> record.getEmpId())  
																					    .collect(Collectors.toList());     

																				List<Object[]> matchingEmployeeIds = employeeLeaveMap.stream()
																					    .filter(employeeLeave -> employeeLeaveMapIds.contains(((BigInteger) employeeLeave[2]).longValue())) 
																					    .collect(Collectors.toList());
																				EmployeeLeavesMap lst = employeeLeavesMapRepository.findByEmpIdAndLeaveTypeMasterId(elm.getEmpId(),elm.getLeaveTypeMasterId());
																				if (!matchingEmployeeIds.isEmpty()) {
																				    matchingEmployeeIds.forEach(list -> {
																				    	if (list[0] instanceof BigInteger) {
//																				            // Convert BigInteger to Long
																				        	elm.setEmployeeLeavesMapId(lst.getEmployeeLeavesMapId());
////																				            elm.setEmployeeLeavesMapId(((BigInteger) list[0]).longValue());
																				        } else if (list[0] instanceof Long) {
																				            // If it is already a Long, no conversion needed
																				        	elm.setEmployeeLeavesMapId(lst.getEmployeeLeavesMapId());
																				        } 
																			             if (list[4] instanceof Float) {
																				            elm.setPendingForApproval((Float) list[4]);
																				        }
																				    });
																				}


																				
																				dbResponse = employeeLeavesMapRepository.save(elm);
																			}
																		
															            if (createdOnLocalDate.getYear() == leave.getFromDate().getYear() &&
															                leave.getNoOfDays() > 1) {
															                newBalance = 1; 

															            	LeaveDTO leaveDTO = new LeaveDTO();
																			leaveDTO.setLeaveStatusId((short)3);
																			leaveDTO.setManagerApprovalStatus("Rejected");
																			leaveDTO.setLevel2ApprovalStatus("NA");
																			leaveDTO.setLevel3ApprovalStatus("NA");
																			leaveDTO.setLevel2ApproverId(null);		
																			leaveDTO.setLevel3ApproverId(null);
																			leaveDTO.setReason("Application rejected as CL exceeded its limit !!");
																			leaveDTO.setUpdatedBy(leave.getManagerId());
																			leaveDTO.setCurrentApprovalLevel(1);
																			leaveDTO.setFinalApprovalLevel(1);
																			
																			leave.setLeaveStatusId(leaveDTO.getLeaveStatusId());
															                leave.setManagerApprovalStatus(leaveDTO.getManagerApprovalStatus());
															                leave.setLevel2ApprovalStatus(leaveDTO.getLevel2ApprovalStatus());
															                leave.setLevel3ApprovalStatus(leaveDTO.getLevel3ApprovalStatus());
															                leave.setLevel2ApproverId(leaveDTO.getLevel2ApproverId());
															                leave.setLevel3ApproverId(leaveDTO.getLevel3ApproverId());
															                leave.setReason(leaveDTO.getReason());
															                leave.getCommonProperty().setUpdatedBy((Long.valueOf(leaveDTO.getUpdatedBy())));
															                leave.setCurrentApprovalLevel(leaveDTO.getCurrentApprovalLevel());
															                leave.setFinalApprovalLevel(leaveDTO.getFinalApprovalLevel());
																			
															                updatedEmployeeLeaveList.add(leave);
																            elm.setBalance(newBalance);
																			elm.setEmpId(leave.getEmpId());
																			elm.setLeaveTypeMasterId(leave.getLeaveTypeMasterId());
																			
																			List<Long> employeeLeaveMapIds = employeeLeaveforPending.stream()
																				    .map(record -> record.getEmpId())  
																				    .collect(Collectors.toList());     

																			List<Object[]> matchingEmployeeIds = employeeLeaveMap.stream()
																				    .filter(employeeLeave -> employeeLeaveMapIds.contains(((BigInteger) employeeLeave[2]).longValue())) 
																				    .collect(Collectors.toList());
																			EmployeeLeavesMap lst = employeeLeavesMapRepository.findByEmpIdAndLeaveTypeMasterId(elm.getEmpId(),elm.getLeaveTypeMasterId());
																			if (!matchingEmployeeIds.isEmpty()) {
																			    matchingEmployeeIds.forEach(list -> {
																			    	if (list[0] instanceof BigInteger) {
//																			            // Convert BigInteger to Long
																			        	elm.setEmployeeLeavesMapId(lst.getEmployeeLeavesMapId());
////																			            elm.setEmployeeLeavesMapId(((BigInteger) list[0]).longValue());
																			        } else if (list[0] instanceof Long) {
																			            // If it is already a Long, no conversion needed
																			        	elm.setEmployeeLeavesMapId(lst.getEmployeeLeavesMapId());
																			        } 
																			        if (list[4] instanceof Float) {
																			            elm.setPendingForApproval((Float) list[4]);
																			        }
																			    });
																			}																		
																			dbResponse = employeeLeavesMapRepository.save(elm);
//																			
															            }else if(createdOnLocalDate.getYear() == leave.getFromDate().getYear() &&
																                leave.getNoOfDays() == 1){
															            	newBalance = 0;
																            elm.setBalance(newBalance);
																			elm.setEmpId(leave.getEmpId());
																			elm.setLeaveTypeMasterId(leave.getLeaveTypeMasterId());
																			List<Long> employeeLeaveMapIds = employeeLeaveforPending.stream()
																				    .map(record -> record.getEmpId())  
																				    .collect(Collectors.toList());     

																			List<Object[]> matchingEmployeeIds = employeeLeaveMap.stream()
																				    .filter(employeeLeave -> employeeLeaveMapIds.contains(((BigInteger) employeeLeave[2]).longValue())) 
																				    .collect(Collectors.toList());
																			EmployeeLeavesMap lst = employeeLeavesMapRepository.findByEmpIdAndLeaveTypeMasterId(elm.getEmpId(),elm.getLeaveTypeMasterId());
																			if (!matchingEmployeeIds.isEmpty()) {
																			    matchingEmployeeIds.forEach(list -> {
																			    	if (list[0] instanceof BigInteger) {
//																			            // Convert BigInteger to Long
																			        	elm.setEmployeeLeavesMapId(lst.getEmployeeLeavesMapId());
////																			            elm.setEmployeeLeavesMapId(((BigInteger) list[0]).longValue());
																			        } else if (list[0] instanceof Long) {
																			            // If it is already a Long, no conversion needed
																			        	elm.setEmployeeLeavesMapId(lst.getEmployeeLeavesMapId());
																			        } 
																			        if (list[4] instanceof Float) {
																			            elm.setPendingForApproval((Float) list[4]);
																			        }
																			    });
																			}
																			dbResponse = employeeLeavesMapRepository.save(elm);
															            }
																	}
																	
															}
														}
													System.out.println("hjvbshv"+employeeLeaveforPending.isEmpty()+"nshjbv"+employeeLeaveforApproved.isEmpty());
													if(!employeeLeaveforPending.isEmpty() && !employeeLeaveforApproved.isEmpty()) {
														newBalance=1;
														elm.setBalance(newBalance);
														elm.setEmployeeLeavesMapId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
														elm.setEmpId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
														elm.setLeaveTypeMasterId(object[3] != null ? Short.parseShort(object[3].toString()) : null);
														elm.setPendingForApproval(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
														
														System.out.println("Check CL" + elm.getBalance());
														dbResponse = employeeLeavesMapRepository.save(elm);
													}
													employeeLeaveRepository.saveAll(updatedEmployeeLeaveList);	
													System.out.println("Completed Successfully");
//													elm.setBalance(newBalance);
//													float newBalance = 1;
//													EmployeeLeavesMap elm = new EmployeeLeavesMap();
//													elm.setEmployeeLeavesMapId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
//													elm.setBalance(newBalance);
//													elm.setEmpId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
//													elm.setLeaveTypeMasterId(object[3] != null ? Short.parseShort(object[3].toString()) : null);
//													elm.setPendingForApproval(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
													
//													System.out.println("Check CL" + elm.getBalance());
//													dbResponse = employeeLeavesMapRepository.save(elm);
													
													if (dbResponse != null) {	//set the changes in log table after alter the leave
														LeaveBalanceLog log = new LeaveBalanceLog();

														log.setBalance(elm.getBalance());
														log.setEmpId(elm.getEmpId());
														log.setLeaveTypeMasterId(ltm.getLeaveTypeMasterId());
														log.setMessage(LeaveLogMessage.autoDeductLeave.replace("0.0",
																Float.toString(dbBalance)));
														log.setUpdateBalanceBy("-" + dbBalance);

														leaveBalanceLogRepository.save(log);
													}
												}
												
					              	        } 
					              			
											if (lpm.getCarryForward().equals("Yes")) {	// check if carry forawrd or not
												float carryForwardValue = lpm.getCarryForwardValue();
												
												System.err.println(" this is carryForwardValue   ::   "+carryForwardValue);
												
												
												
												if (dbBalance > carryForwardValue) {
													float newBalance = carryForwardValue;	//set new value as per the leave policy 
													float deductedLeaveCount = dbBalance - carryForwardValue;	
													System.out.println(deductedLeaveCount);
													System.err.println(newBalance);
//													elm.setBalance(newBalance);
													
													EmployeeLeavesMap elm = new EmployeeLeavesMap();
													elm.setEmployeeLeavesMapId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
													newBalance = (float) (newBalance);
													System.err.println(" Plan leaved PL deduct and set increases value ");
													elm.setBalance(newBalance);
													elm.setEmpId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
													elm.setLeaveTypeMasterId(object[3] != null ? Short.parseShort(object[3].toString()) : null);
													elm.setPendingForApproval(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
													
													EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(elm);

													if (dbResponse != null) {
														LeaveBalanceLog log = new LeaveBalanceLog();

														log.setBalance(newBalance);
														log.setEmpId(elm.getEmpId());
														log.setLeaveTypeMasterId(ltm.getLeaveTypeMasterId());
														log.setMessage(LeaveLogMessage.autoDeductLeave.replace("0.0",
																Float.toString(deductedLeaveCount)));
														log.setUpdateBalanceBy("-" + deductedLeaveCount);

														leaveBalanceLogRepository.save(log);
													}
												}
												else if(dbBalance == carryForwardValue) {
													float newBalance = carryForwardValue;
													EmployeeLeavesMap elm = new EmployeeLeavesMap();
													elm.setEmployeeLeavesMapId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
													
													System.err.println(" Plan leaved PL deduct and set increases value ");
													elm.setBalance(newBalance);
													elm.setEmpId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
													elm.setLeaveTypeMasterId(object[3] != null ? Short.parseShort(object[3].toString()) : null);
													elm.setPendingForApproval(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
													
													EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(elm);

													if (dbResponse != null) {
														LeaveBalanceLog log = new LeaveBalanceLog();

														log.setBalance(newBalance);
														log.setEmpId(elm.getEmpId());
														log.setLeaveTypeMasterId(ltm.getLeaveTypeMasterId());
														leaveBalanceLogRepository.save(log);
													}
												}else if(dbBalance < carryForwardValue) {
													float newBalance = dbBalance;
													EmployeeLeavesMap elm = new EmployeeLeavesMap();
													elm.setEmployeeLeavesMapId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
													newBalance = (float) (newBalance);
													System.err.println(" Plan leaved PL deduct and set increases value ");
													elm.setBalance(newBalance);
													elm.setEmpId(object[2] != null ? Long.parseLong(object[2].toString()) : null);
													elm.setLeaveTypeMasterId(object[3] != null ? Short.parseShort(object[3].toString()) : null);
													elm.setPendingForApproval(object[4] != null ? Float.parseFloat(object[4].toString()) : null);
													
													EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(elm);
													if (dbResponse != null) {
														LeaveBalanceLog log = new LeaveBalanceLog();

														log.setBalance(newBalance);
														log.setEmpId(elm.getEmpId());
														log.setLeaveTypeMasterId(ltm.getLeaveTypeMasterId());
														leaveBalanceLogRepository.save(log);
													}
												}
							              }
						              }
					          }
				           }
			            }
			   }catch(Exception e) {
				e.printStackTrace();
			   }
		}

	
	
	// 0 1 1 ? * * - At 01:01:00am every day
//	@Scheduled(cron = "0 1 1 ? * *")

	@Scheduled(cron="${compOff_Expiration}")
	public void LeaveExpirationCronJob() {
		System.err.println("Cron work");
	    try {
	        // Find leave type
	        LeaveTypeMaster leaveType = leaveTypeMasterRepository.findByLeaveTypeCode("CO");
	        if (leaveType != null) {
	            // Find leave policies for the leave type
	            List<LeavePolicyMaster> leavePolicies = leavePolicyMasterRepository.findByLeaveTypeMasterId(leaveType.getLeaveTypeMasterId());
	            for (LeavePolicyMaster leavePolicy : leavePolicies) {
	                if ("Yes".equals(leavePolicy.getExpirationPeriod())) {
	                    Integer expirationPeriod = leavePolicy.getExpirationPeriodValue();
	                    String employmentStatus = leavePolicy.getEmploymentStatus();

	                    // Find employees with the specified employment status
	                    List<Employee> employees = employeeRepository.findByEmploymentstatus(employmentStatus);
	                    for (Employee employee : employees) {
	                        // Get employee leave balance
	                        EmployeeLeavesMap empLeaveMapObj = employeeLeavesMapRepository.findByEmpIdAndLeaveTypeMasterId(employee.getEmpId(), leaveType.getLeaveTypeMasterId());

	                        // Get CompOff leave applications
	                        List<CompOffLeave> compOffLeaveObj = compOffLeaveRepository.findAllPendingApplicationByEmpId(employee.getEmpId(), Timestamp.valueOf(LocalDate.now().minusDays(45).atStartOfDay()), "Pending");
	                        System.err.println(" compOffLeaveObj.size()     "+compOffLeaveObj.size());
	                        for (CompOffLeave compOffObj : compOffLeaveObj) {
	                            LocalDate expirationDate;
	                            if (compOffObj.getApproverDate() != null) {
	                            expirationDate = compOffObj.getApproverDate().plusDays(expirationPeriod);
	                            System.err.println("expirationDate   "+expirationDate);
	                            System.out.println("LocalDate.now()     "+LocalDate.now());
	                            System.err.println(" compoff id   "+compOffObj.getCompOffLeaveId());
	                                if (expirationDate.equals(LocalDate.now())) {
		                                Float currentBalance = empLeaveMapObj.getBalance();
//		                                if (currentBalance != 0f) {
		                                    Float newBalance = currentBalance - compOffObj.getNoOfDays();
		                                    compOffObj.setCompOffStatus("Expired");
		                                    compOffLeaveRepository.save(compOffObj);
		                                    
//		                                    if(compOffObj.getCompOffStatus().equals("Expired")) {
//		                                    	compOffLeaveRepository.deleteById(compOffObj.getCompOffLeaveId());
//		                                    }

		                                    empLeaveMapObj.setBalance(newBalance);
		                                    EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(empLeaveMapObj);

		                                    if (dbResponse != null) {
		                                        LeaveBalanceLog log = new LeaveBalanceLog();
		                                        log.setBalance(newBalance);
		                                        log.setEmpId(employee.getEmpId());
		                                        log.setLeaveTypeMasterId(leaveType.getLeaveTypeMasterId());
		                                        log.setMessage(LeaveLogMessage.compOffExpire.replace("0.0", Float.toString(compOffObj.getNoOfDays())));
		                                        log.setUpdateBalanceBy("-" + compOffObj.getNoOfDays());
		                                        leaveBalanceLogRepository.save(log);
		                                        System.out.println("CompOff balance updated successfully");
		                                    } else {
		                                        System.out.println("CompOff balance updation failed");
		                                    }
//		                                }
		                            }
	                            
	                            } 
//	                            else {
//	                                expirationDate = compOffObj.getFromDate().plusDays(expirationPeriod);
//	                            }  // as discussed with bansi sir and pooja , comp off expiration should be happend Once Approved 
//	                            System.err.println(" compOffObj.getUpdatedOn() expirationDate     "+compOffObj.getUpdatedOn());
//	                            System.err.println(" compOffObj.getFromDate()  expirationDate     "+compOffObj.getFromDate());
//	                           
//	                            System.out.println(" today date "+LocalDate.now());
	                            
	                        }
	                    }
	                } else {
	                    System.err.println("Please update Policy");
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	//0 0 1 1,2,3,4,5,6,7 JAN ? - At 01:00:00am, on the 1st, 2nd, 3rd, 4th, 5th, 6th and 7th day, in January
	
	@Scheduled(cron = "0 0 1 1,2,3,4,5,6,7 JAN ?")
	public void addingWeekOff() {
		
		
		try {
			int monthCount = 1;
			
			// For adding 2nd & 4th Saturday
			
			while(monthCount <= 12) {
				
				int currentYear = LocalDate.now().getYear();
				LocalDate dateToday = LocalDate.of(currentYear, monthCount, 1);
				
				LocalDate secondSaturday = dateToday.with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.SATURDAY));
				List<Holiday> secondSaturdayData = holidayRepository.findByOccasionAndDateOfHoliday("Saturday : second saturday",secondSaturday);
				
				if(secondSaturdayData.isEmpty()) {
					
					Holiday newHoliday = new Holiday();
					  newHoliday.setDateOfHoliday(secondSaturday);
					  newHoliday.setDayOfTheWeek("Saturday");
					  newHoliday.setHolidayType("WeekOff");
					  newHoliday.setOccasion("Saturday : second saturday");
					  newHoliday.setState("all");
					  newHoliday.setOptionalHoliday("false");
					  
					  holidayRepository.save(newHoliday);
				}
				
				LocalDate fourthSaturday = dateToday.with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.SATURDAY));
                List<Holiday> fourthSaturdayData = holidayRepository.findByOccasionAndDateOfHoliday("Saturday : fourth saturday",fourthSaturday);
				
				if(fourthSaturdayData.isEmpty()) {
					
					Holiday newHoliday = new Holiday();
					  newHoliday.setDateOfHoliday(fourthSaturday);
					  newHoliday.setDayOfTheWeek("Saturday");
					  newHoliday.setHolidayType("WeekOff");
					  newHoliday.setOccasion("Saturday : fourth saturday");
					  newHoliday.setState("all");
					  newHoliday.setOptionalHoliday("false");
					  
					  holidayRepository.save(newHoliday);
				}
				  
			// For adding Sundays	  
				
				Calendar calander = new GregorianCalendar(currentYear, monthCount - 1, 1);
		        do {
		            int day = calander.get(Calendar.DAY_OF_WEEK);
		            if (day == Calendar.SUNDAY) {
		            	Date date = calander.getTime();
		            	LocalDate sundayDate = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(date));
		            	 List<Holiday> sundayData = holidayRepository.findByOccasionAndDateOfHoliday("Sunday",sundayDate);
		            	
		            	 if(sundayData.isEmpty()) {
		            		 Holiday holidayObj = new Holiday();
			            	  holidayObj.setDateOfHoliday(sundayDate);
			            	  holidayObj.setDayOfTheWeek("Sunday");
			            	  holidayObj.setHolidayType("WeekOff");
			            	  holidayObj.setOccasion("Sunday");
			            	  holidayObj.setState("all");
			            	  holidayObj.setOptionalHoliday("false");
			            	  
							  holidayRepository.save(holidayObj);
		            	 } 
		            }
		            calander.add(Calendar.DAY_OF_YEAR, 1);
		        }  while (calander.get(Calendar.MONTH) == monthCount-1);
				
				monthCount++;
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	//0 0 21 ? * * - At 21:00:00pm every day
	
	@Scheduled(cron = "0 10 16 * * ?")
//	@Scheduled(cron = "0 0 21 ? * *")
//	@Scheduled(cron = "0 36 17 3 11 ?")

		public void automaticTimesheetFiller() {
		
		System.out.println("Cron----**********----started");
			
			try {
				//for hardcoded
//				LocalDate dateToday = LocalDate.parse("2024-12-14");
				LocalDate dateToday = LocalDate.parse("2025-11-17");
//				LocalDate dateToday = LocalDate.now();
//				System.out.println("filling timesheet method started");
				List<Object[]> allEmployee = employeeRepository.getEmployeeDetailForCronExludingSomeEmployees();
				System.err.println("vghgc"+dateToday);
				List<Holiday> publicHoliday = holidayRepository.findByDateOfHoliday(dateToday);
				
			//	Timesheet filler for weekoff day : saturday & sunday
				
				if(!publicHoliday.isEmpty()) {
					
					System.out.println("holiday_size"+publicHoliday.size());
					
					for(Holiday holiday: publicHoliday) {
						String holidayOccassion = holiday.getOccasion();
						String dayOfWeek = holiday.getDayOfTheWeek();
						
						if((holiday.getHolidayType().equals("WeekOff") && dayOfWeek.equals("Saturday")) || (holiday.getHolidayType().equals("WeekOff") && dayOfWeek.equals("Sunday"))) {
							for(Object[] employeeList: allEmployee) {
								Long empId = employeeList[0] != null ? Long.parseLong(employeeList[0].toString()) : null;
								String billableType = employeeList[7] != null ?employeeList[7].toString() : null;
								if(("TNM").equalsIgnoreCase(billableType)) {
									System.out.println("TNM");
								}
								Timesheet empTimesheet = timesheetsRepository.findByEmpIdAndDate(empId, dateToday);

								if (empTimesheet == null) {

									if (holidayOccassion.equals("Saturday : second saturday")
											|| holidayOccassion.equals("Saturday : fourth saturday")) {
										if (billableType != null && !("TNM").equalsIgnoreCase(billableType)) {
											Timesheet newTimesheet = new Timesheet();
											newTimesheet.getCommonProperty().setCreatedBy(empId);
											newTimesheet.setDate(dateToday);
											newTimesheet.setDayType("Week Off");
											newTimesheet.setDescription("WeekOff : Saturday");
											newTimesheet.setTotalTime((float) 0);
											newTimesheet.setTotalWorkingHours("0");
											newTimesheet.setEmpId(empId);
											newTimesheet.setStatus("Approved");
											timesheetsRepository.save(newTimesheet);
										}
									} else {
										Timesheet newTimesheet = new Timesheet();
										newTimesheet.getCommonProperty().setCreatedBy(empId);
										newTimesheet.setDate(dateToday);
										newTimesheet.setDayType("Week Off");
										newTimesheet.setDescription("WeekOff : Sunday");
										newTimesheet.setTotalTime((float) 0);
										newTimesheet.setTotalWorkingHours("0");
										newTimesheet.setEmpId(empId);
										newTimesheet.setStatus("Approved");
										timesheetsRepository.save(newTimesheet);
									}

									// For weekoff's managers don't have to approve the timesheet, if any employee
									// worked on weekoff will revoke this ..

//									System.out.println("filling weekoffs");

								}
							}
						}
					}		
				}
				
		   //	Timesheet filler for public Holiday
				
				if(!publicHoliday.isEmpty()) {
					System.out.println("vghgc"+publicHoliday.isEmpty());
					System.out.println("holiday_size_holiday"+publicHoliday.size());
					for(Holiday holidays: publicHoliday) {
						String holidayState = holidays.getState();
						
						for(Object[] employeeList: allEmployee) {
							Long empId = employeeList[0] != null ? Long.parseLong(employeeList[0].toString()) : null;
							String workLocation = employeeList[1] != null ? employeeList[1].toString() : null;
							String billableType = employeeList[7] != null ?employeeList[7].toString() : null;
							if(("TNM").equalsIgnoreCase(billableType)) {
								System.out.println("TNM");
							}
							System.out.println("vghgc"+empId);
							Timesheet empTimesheet = timesheetsRepository.findByEmpIdAndDate(empId,dateToday);
							if(empTimesheet == null) {
								System.out.println("vghgc"+publicHoliday.isEmpty());
								if(((holidayState.equals("all") && holidays.getOptionalHoliday().equals("false") && (holidays.getHolidayType().equals("Festival") || holidays.getHolidayType().equals("nonWorking")))
										|| (holidayState.equals(workLocation) && holidays.getOptionalHoliday().equals("false") && (holidays.getHolidayType().equals("Festival") || holidays.getHolidayType().equals("nonWorking"))))&& billableType != null &&!("TNM").equalsIgnoreCase(billableType)){
									
									Timesheet newTimesheet = new Timesheet();
									newTimesheet.getCommonProperty().setCreatedBy(empId);
									newTimesheet.setDate(dateToday);
												
									newTimesheet.setDayType("Public Holiday");
									newTimesheet.setDescription("Public Holiday : " + holidays.getOccasion());
									newTimesheet.setEmpId(empId);
									newTimesheet.setStatus("Approved");
									
//									System.out.println("filling holiday");
									
									timesheetsRepository.save(newTimesheet);
									System.out.println("vghgc"+newTimesheet);
									
								}
							}
						}	
					}
				}
				
			//	Timesheet filler for leave days
				
//				List<EmployeeLeave> employeeLeave = employeeLeaveRepository.findByFromDate(dateToday);
//				
//				if(!employeeLeave.isEmpty()) {
//					
//					for(EmployeeLeave leaveObj: employeeLeave) {
//						Long empId = leaveObj.getEmpId();
//						Short approvedLeave = 2;
//						
//						long elapsedDays = ChronoUnit.DAYS.between(leaveObj.getFromDate(), leaveObj.getToDate());
//						
//						if((elapsedDays == 0) && leaveObj.getLeaveStatusId().equals(approvedLeave)) {
//							
//							Timesheet newTimesheet = new Timesheet();
//							
//							newTimesheet.getCommonProperty().setCreatedBy(empId);
//							newTimesheet.setDate(dateToday);
//							newTimesheet.setDayType("Holiday");
//							newTimesheet.setDescription("On leave");
//							newTimesheet.setEmpId(empId);
//							newTimesheet.setStatus("Approved");
//							
//							timesheetsRepository.save(newTimesheet);
//						}
//						
//						if((elapsedDays != 0) && leaveObj.getLeaveStatusId().equals(approvedLeave)) {
//							LocalDate tempDateToday = dateToday;
//							
//							while(tempDateToday.compareTo(leaveObj.getToDate()) != 1) {
//								
//								Timesheet newTimesheet = new Timesheet();
//								
//								newTimesheet.getCommonProperty().setCreatedBy(empId);
//								newTimesheet.setDate(tempDateToday);
//								newTimesheet.setDayType("Holiday");
//								newTimesheet.setDescription("On leave");
//								newTimesheet.setEmpId(empId);
//								newTimesheet.setStatus("Approved");
//								
//								timesheetsRepository.save(newTimesheet);
//								
//								tempDateToday = tempDateToday.plusDays(1);
//							}
//						}					
//					}
//				}
				System.out.println("Method end reached");
				}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		// 0 0 12 ? * * - At 12:00:00pm every day
		
		@Scheduled(cron="${mailTrigger.time}")
		public void protalConfigMailTrigger() {
			try {
				
				List<PortalConfig> portalConfigObj = portalConfigRepository.findAll();
				List<EmployeeDTO> probationElapsedDays = new ArrayList<EmployeeDTO>();
				List<EmployeeDTO> resignElapsedDays = new ArrayList<EmployeeDTO>();
				
				LocalDate dateToday = LocalDate.now();
				Float probationMailTrigger = null;
				Float noticePeriodMailTrigger = null;
				
				for(PortalConfig portalObj :portalConfigObj) {
					if(portalObj.getConfigName().equals("Probation Period")) {
						probationMailTrigger = portalObj.getMailTrigger();
					}else if(portalObj.getConfigName().equals("Notice Period")) {
						noticePeriodMailTrigger = portalObj.getMailTrigger();
					} 
				}
				
				List<Object[]> employeeList = employeeRepository.getEmployeeInProbationAndNotice();
				List<EmployeeDTO> listDTO = new ArrayList<EmployeeDTO>();
				
				if(employeeList != null) {
					for(Object[] object: employeeList) {
						EmployeeDTO empdto = new EmployeeDTO();
						empdto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						empdto.setName(object[1] != null ? object[1].toString() : null);
						empdto.setEmail(object[2] != null ? object[2].toString() : null);
						empdto.setProbationPeriod(object[3] != null ? Short.parseShort(object[3].toString()) : null);
						empdto.setNoticePeriod(object[4] != null ? Short.parseShort(object[4].toString()) : null);
						empdto.setDateOfJoining(object[5] != null ? object[5].toString() : null);
						empdto.setDateOfResign(object[6] != null ? object[6].toString() : null);
						
						listDTO.add(empdto);
					}
				}
				for(EmployeeDTO employeeData: listDTO) {
					if(employeeData.getDateOfResign() == null) {
						LocalDate confirmationDate = stringToDateTimeParser.getDate(employeeData.getDateOfJoining(), "yyyy-MM-dd").plusDays(employeeData.getProbationPeriod());
						LocalDate mailTriggerDate = confirmationDate.minusDays(probationMailTrigger.shortValue());
						if(LocalDate.now().equals(mailTriggerDate)) {
							mailService.sendMailWithCC(employeeData.getEmail(),
									hrMailAddress,
									"Regarding Probation Period",
									"Employee with EmpId : A-"+ employeeData.getEmployeementId() 
						          + "<br> Name : " + employeeData.getName()
						          + "<br> will complete its probation period in " + probationMailTrigger.shortValue() + " days");
						}
					}
					
					if(employeeData.getDateOfResign() != null){
						
						LocalDate relievingDate = stringToDateTimeParser.getDate(employeeData.getDateOfResign(), "yyyy-MM-dd").plusDays(employeeData.getNoticePeriod());
						LocalDate mailTriggerDate = relievingDate.minusDays(noticePeriodMailTrigger.shortValue());
						if(LocalDate.now().equals(mailTriggerDate)) {
							mailService.sendMailWithCC(employeeData.getEmail(),
									hrMailAddress,
									"Regarding Notice Period","Employee with EmpId : A-"+ employeeData.getEmployeementId() 
									+ "<br> Name : " + employeeData.getName()
						            + "<br> will complete its Notice period in " + noticePeriodMailTrigger.shortValue() + " days");
						}
					}
				}
				
				// elapsedDays : Probation or Resigned
				List<Object[]> elapsedEmpList = employeeRepository.getElapsedEmpInProbationAndNotice();
				List<EmployeeDTO> elapseddtoList = new ArrayList<EmployeeDTO>();
				if(!elapsedEmpList.isEmpty()){
					elapsedEmpList.forEach((object) -> {
						
						EmployeeDTO empdto = new EmployeeDTO();
						empdto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						empdto.setName(object[1] != null ? object[1].toString() : null);
						empdto.setEmail(object[2] != null ? object[2].toString() : null);
						empdto.setProbationPeriod(object[3] != null ? Short.parseShort(object[3].toString()) : null);
						empdto.setNoticePeriod(object[4] != null ? Short.parseShort(object[4].toString()) : null);
						empdto.setDateOfJoining(object[5] != null ? object[5].toString() : null);
						empdto.setDateOfResign(object[6] != null ? object[6].toString() : null);
						empdto.setEmploymentstatus(object[7] != null ? object[7].toString() : null);
						
						elapseddtoList.add(empdto);
					});
				}
				
				if(!elapseddtoList.isEmpty()) {
					elapseddtoList.forEach((employeeData) -> {
						if(employeeData.getDateOfResign() == null) {
							LocalDate confirmationDate = stringToDateTimeParser.getDate(employeeData.getDateOfJoining(), "yyyy-MM-dd").plusDays(employeeData.getProbationPeriod());
							// elapsedDays reminder *15 days*
							if(confirmationDate.isBefore(dateToday)) {
								Long elapsedDays = ChronoUnit.DAYS.between(confirmationDate, dateToday);
								
								if(elapsedDays/15 == 1) {
									probationElapsedDays.add(employeeData);
								}							
							}
						}
						
						if(employeeData.getDateOfResign() != null){
							LocalDate relievingDate = stringToDateTimeParser.getDate(employeeData.getDateOfResign(), "yyyy-MM-dd").plusDays(employeeData.getNoticePeriod());
							// elapsedDays reminder *15 days*
							if(relievingDate.isBefore(dateToday)) {
								Long elapsedDays = ChronoUnit.DAYS.between(relievingDate, dateToday);
								if(elapsedDays/15 == 1) {
									resignElapsedDays.add(employeeData);
								}							
							}
						}
					});
				}
				
				// Probation mail reminder
				if(!probationElapsedDays.isEmpty()) {
					
					StringBuilder html = new StringBuilder();
					html.append("<html>\n" +
				            "  <head>\n" +
				            "    <style>\n" +
				            "      table, th, td {\n" +
				            "        border: 1px solid black;\n" +
				            "      }\n" +
				            "      table {\n" +
				            "        border-collapse: collapse;\n" +
				            "      }\n" +
				            "    </style>\n" +
				            "  </head>\n" +
				            "  <body>\n" +
				            "    <table>\n" +
				            "      <tr>\n" +
				            "        <th>Emp ID</th>\n" +
				            "        <th>Name</th>\n" +
				            "        <th>Date Of Joining</th>\n" +
				            "      </tr>\n");
					// add rows to the table
					for(EmployeeDTO emp: probationElapsedDays) {
						html.append("      <tr>\n");
						  // add cells to the row
						  html.append("        <td>" + "A-"+emp.getEmployeementId() + "</td>\n");
						  html.append("        <td>" + emp.getName() + "</td>\n");
						  html.append("        <td>" + emp.getDateOfJoining() + "</td>\n");
						  html.append("      </tr>\n");
					}
					html.append("    </table>\n" +
					            "  </body>\n" +
					            "</html>");
					
					mailService.sendMail(hrMailAddress,
							"Regarding Employee's Probation Period",
							"Dear team, <br><br>"
	                      + "Following employee's has crossed there expected probation period confirmation date. <br><br>"
						  + html.toString()
							);
				}
				
				//Resigned mail reminder
                if(!resignElapsedDays.isEmpty()) {
					
					StringBuilder html = new StringBuilder();
					html.append("<html>\n" +
				            "  <head>\n" +
				            "    <style>\n" +
				            "      table, th, td {\n" +
				            "        border: 1px solid black;\n" +
				            "      }\n" +
				            "      table {\n" +
				            "        border-collapse: collapse;\n" +
				            "      }\n" +
				            "    </style>\n" +
				            "  </head>\n" +
				            "  <body>\n" +
				            "    <table>\n" +
				            "      <tr>\n" +
				            "        <th>Emp ID</th>\n" +
				            "        <th>Name</th>\n" +
				            "        <th>Date Of Resign</th>\n" +
				            "      </tr>\n");
					// add rows to the table
					for(EmployeeDTO emp: resignElapsedDays) {
						html.append("      <tr>\n");
						  // add cells to the row
						  html.append("        <td>" + "A-"+emp.getEmployeementId() + "</td>\n");
						  html.append("        <td>" + emp.getName() + "</td>\n");
						  html.append("        <td>" + emp.getDateOfResign() + "</td>\n");
						  html.append("      </tr>\n");
					}
					html.append("    </table>\n" +
					            "  </body>\n" +
					            "</html>");
					
					mailService.sendMail(hrMailAddress,
							"Regarding Employee's Notice Period",
							"Dear team, <br><br>"
	                      + "Following employee's has crossed there expected relieving date. <br><br>"
						  + html.toString()
							);
				}
				
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		
		//Confirmation Mail ///Raj Alpha Swain
//		@Scheduled(cron = "${mailTrigger.time}")
		@Scheduled(cron = "0 0 10 * * *")
		public void portalConfigConfirmation() {
			try {
				sendProbationReminder();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		public void sendProbationReminder() {
			LocalDate today = LocalDate.now();
			List<Employee> employeesInProbation = employeeRepository.getEmployeeInProbation();
			List<Employee> employeesInProbationExtended = employeeRepository.getEmployeeInProbationExtended();
			
			List<Employee> preConfirmationList = new ArrayList<>();
	        List<Employee> overdueList = new ArrayList<>();
	        List<Long> employeeIdsToUpdate = new ArrayList<>();
			
	        if(!employeesInProbation.isEmpty()) {
			for(Employee employee : employeesInProbation) {
				
				if(employee.getProbationPeriod() == null || employee.getDateOfJoining() == null)
				{
					continue;
				}

				LocalDate confirmationDate = employee.getDateOfJoining().plusDays(employee.getProbationPeriod());
				
				Long daysLeftUntilConfirmation = ChronoUnit.DAYS.between(today,confirmationDate);
				
				if(daysLeftUntilConfirmation == 30 || daysLeftUntilConfirmation == 6 || daysLeftUntilConfirmation == 3 || 
						daysLeftUntilConfirmation == 1 || daysLeftUntilConfirmation == 0)
				{
					 preConfirmationList.add(employee);
				}
				else if(confirmationDate.isBefore(today))
				{
					Long daysoverdue = ChronoUnit.DAYS.between(confirmationDate,today);
					boolean hasExtensionReason = employee.getReasonOfExtension() != null && !employee.getReasonOfExtension().isEmpty();
					
					if(daysoverdue >= 100 && hasExtensionReason && !employee.isLongOverdueNotified())
					{
						 if (daysoverdue % 5 == 0) {
			                    overdueList.add(employee);
			                    employeeIdsToUpdate.add(employee.getEmpId());
			                }
					}
					
					else if(daysoverdue >0 && daysoverdue % 5 == 0)
					{
						overdueList.add(employee);
					}
				}    
			} 
	        }
	        else if(!employeesInProbationExtended.isEmpty()) {
			for(Employee employee : employeesInProbationExtended) {
				
				LocalDate confirmationDate = employee.getDateOfJoining().plusDays(employee.getProbationPeriod());
				
				if(confirmationDate.isBefore(today))
				{
					Long daysLeftUntilConfirmation = ChronoUnit.DAYS.between(today,confirmationDate);
					Long daysoverdue = ChronoUnit.DAYS.between(confirmationDate,today);
					boolean hasExtensionReason = employee.getReasonOfExtension() != null && !employee.getReasonOfExtension().isEmpty();
					
					if(daysoverdue >= 100 && hasExtensionReason && !employee.isLongOverdueNotified())
					{
						 if (daysoverdue % 5 == 0) {
			                    overdueList.add(employee);
			                    employeeIdsToUpdate.add(employee.getEmpId());
			                }
					}
					
					else if(daysoverdue >0 && daysoverdue % 5 == 0 && !employee.isLongOverdueNotified())
					{
						overdueList.add(employee);
					}
				}  
			}
	        }
			if (!preConfirmationList.isEmpty()) {
				sendPreConfirmationEmail(preConfirmationList);
	        }
			if (!overdueList.isEmpty()) {
				sendOverdueConfirmationEmail(overdueList);
	        }
			List<Employee> empList=new ArrayList<>();
			 if (!employeeIdsToUpdate.isEmpty()) {
			        for (Long empId : employeeIdsToUpdate) {
			        	Optional<Employee> empOpt=employeeRepository.findById(empId);
			        	if(empOpt.isPresent()) {
			        		Employee emp=empOpt.get();
			        		emp.setLongOverdueNotified(true);
			        		empList.add(emp);			        	}
			        }
			        
			        employeeRepository.saveAll(empList);
			 }
		}
		
		
		public void sendPreConfirmationEmail(List<Employee> employees)
		{	
			 if (employees == null || employees.isEmpty()) {
		            return; 
		        }
		        
			 
		        Map<String, List<Employee>> employeesByManager = new HashMap<>();
		        
		        Map<String, List<Employee>> employeesByHod = new HashMap<>();

		        for (Employee employee : employees) {
		        	
		        	String dept = employeeRepository.getDepartment(employee.getEmpId());
		        	
		            if (employee.getManagerId() != null) {
		            	String mail = employeeRepository.getMailByEmpId(employee.getManagerId());
		                employeesByManager.computeIfAbsent(mail, k -> new ArrayList<>()).add(employee);
		            }
		            
		            Long hodId = employeeRepository.getDepartmentHod(employee.getEmpId());
		            
		            String HODMail = employeeRepository.findHodEmailById(hodId);
		            if (dept!= null && HODMail != null) {
		                employeesByHod.computeIfAbsent(HODMail, k -> new ArrayList<>()).add(employee);
		            }
		        }

		           for (Map.Entry<String, List<Employee>> entry : employeesByManager.entrySet()) {
		            String managerEmail = entry.getKey();
		            List<Employee> directReports = entry.getValue();
		            
		            String subject = "Action Required: Probation Confirmations for Your Team";
		            String intro = "Dear Team,<br><br>This is a reminder that the probation period for the following members of your team is due for confirmation soon. Please take the necessary action.<br><br>";
		            String body = buildHtmlEmailBody(intro, directReports);
		            
		            try {
		                mailService.sendMail(managerEmail, subject, body);
		            } catch (Exception e) {
		                e.printStackTrace(); 		            }
		        }

		        	for (Map.Entry<String, List<Employee>> entry : employeesByHod.entrySet()) {
		            String hodEmail = entry.getKey();
		            List<Employee> departmentEmployees = entry.getValue();
		            
		            String subject = "Department Update: Upcoming Probation Confirmations";
		            String intro = "Dear HOD,<br><br>This is a summary of all employees in your department whose probation period is due for confirmation soon.<br><br>";
		            String body = buildHtmlEmailBody(intro, departmentEmployees);
		            
		            try {
		                mailService.sendMail(hodEmail, subject, body);
		            } catch (Exception e) {
		                e.printStackTrace();
		            }
		        }
		        String hrSubject = "Consolidated Report: Upcoming Probation Confirmations";
		        String hrIntro = "Dear HR Team,<br><br>Here is the consolidated list of all employees whose probation is due for confirmation.<br><br>";
		        String hrBody = buildHtmlEmailBody(hrIntro, employees); 
		        
		        try {
		        	String email = "raj.swain@apmosys.com";
		        	
//		        	mailService.sendMail(hrMailAddress, hrSubject, hrBody);
		            mailService.sendMail(email, hrSubject, hrBody);
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
	    }
		
		
		
		
		
		public void sendOverdueConfirmationEmail(List<Employee> employees) {
			 if (employees == null || employees.isEmpty()) {
		            return; 
		        }

		 
		        Map<String, List<Employee>> employeesByManager = new HashMap<>();
		        Map<String, List<Employee>> employeesByHod = new HashMap<>();

		        for (Employee employee : employees) {
		        	
		        	String dept = employeeRepository.getDepartment(employee.getEmpId());
		        	
		            if (employee.getManagerId() != null) {
		            	String mail = employeeRepository.getMailByEmpId(employee.getManagerId());
		                employeesByManager.computeIfAbsent(mail, k -> new ArrayList<>()).add(employee);
		            }
		            
		            Long hodId = employeeRepository.getDepartmentHod(employee.getEmpId());
		            
		            String HODMail = employeeRepository.findHodEmailById(hodId);
		            if (dept!= null && HODMail != null) {
		                employeesByHod.computeIfAbsent(HODMail, k -> new ArrayList<>()).add(employee);
		            }
		        }

		        for (Map.Entry<String, List<Employee>> entry : employeesByManager.entrySet()) {
		            String managerEmail = entry.getKey();
		            List<Employee> directReports = entry.getValue();
		            
		            String subject = "URGENT ACTION: Overdue Probation Confirmations for Your Team";
		            String intro = "Dear Team,<br><br>This is an urgent reminder that the probation confirmation for the following members of your team is overdue. Please prioritize and complete the process immediately.<br><br>";
		            String body = buildHtmlEmailBody(intro, directReports);
		            
		            try {
		                mailService.sendMail(managerEmail, subject, body);
		            } catch (Exception e) {
		                e.printStackTrace(); 
		            }
		        }

		        for (Map.Entry<String, List<Employee>> entry : employeesByHod.entrySet()) {
		            String hodEmail = entry.getKey();
		            List<Employee> departmentEmployees = entry.getValue();
		            
		            String subject = "URGENT Department Update: Overdue Probation Confirmations";
		            String intro = "Dear HOD,<br><br>This is an urgent summary of all employees in your department whose probation confirmation is overdue.<br><br>";
		            String body = buildHtmlEmailBody(intro, departmentEmployees);
		            
		            try {
		                mailService.sendMail(hodEmail, subject, body);
		            } catch (Exception e) {
		                e.printStackTrace();
		            }
		        }

		        String hrSubject = "URGENT Consolidated Report: Overdue Probation Confirmations";
		        String hrIntro = "Dear HR Team,<br><br>Here is the consolidated list of all employees whose probation confirmation is overdue. Please ensure immediate follow-up.<br><br>";
		        String hrBody = buildHtmlEmailBody(hrIntro, employees);
		        
		        try {
		        	String email = "raj.swain@apmosys.com";
//		            mailService.sendMail(hrMailAddress, hrSubject, hrBody);
		            mailService.sendMail(email, hrSubject, hrBody);
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
	    }
		
		
		 private String buildHtmlEmailBody(String introduction, List<Employee> employees) {
		        StringBuilder body = new StringBuilder();
		        body.append(introduction);

		        body.append("<table border='1' style='border-collapse:collapse; padding: 5px;'>");
		        body.append("<thead><tr style='background-color:#f2f2f2;'>")
		            .append("<th style='padding: 8px;'>Employee ID</th>")
		            .append("<th style='padding: 8px;'>Name</th>")
		            .append("<th style='padding: 8px;'>Department</th>")
		            .append("<th style='padding: 8px;'>Date of Joining</th>")
		            .append("<th style='padding: 8px;'>Due Days</th>")
		            .append("</tr></thead>");
		        
		        body.append("<tbody>");
		        LocalDate today = LocalDate.now();

		        for (Employee emp : employees) {
		            LocalDate confirmationDate = emp.getDateOfJoining().plusDays(emp.getProbationPeriod());
		            long daysLeft = today.isBefore(confirmationDate) 
		            	    ? ChronoUnit.DAYS.between(today, confirmationDate)
		            	    : ChronoUnit.DAYS.between(confirmationDate, today);
		            String daysLeftString = daysLeft == 0 ? "<b>Today</b>" : String.valueOf(daysLeft);
		            String departmentName = employeeRepository.getDepartment(emp.getEmpId());

		            body.append("<tr>")
		                .append("<td style='padding: 8px;'>A-").append(emp.getEmployeementId()).append("</td>")
		                .append("<td style='padding: 8px;'>").append(emp.getName()).append("</td>")
		                .append("<td style='padding: 8px;'>").append(departmentName).append("</td>")
		                .append("<td style='padding: 8px;'>").append(emp.getDateOfJoining()).append("</td>")
		                .append("<td style='padding: 8px; text-align:center;'>").append(daysLeftString).append("</td>")
		                .append("</tr>");
		        }
		        body.append("</tbody></table><br>");
		        body.append("Thank you.<br>");
		        body.append("<a href=\"https://ishine.apmosys.com/\">Visit iShine Portal</a>");		     

		        return body.toString();
		    }

		
		 
		@Scheduled(cron = "0 0 10 * * *") 
		public void automaticConfirmation()
		{
			try {
			LocalDate today = LocalDate.now();
			List<Employee> employeeList = employeeRepository.getEmployeeProbationAndIsClicked();
			
			if(employeeList != null) {
				for(Employee employee :employeeList) {

//		            LocalDate probationEndDate = employee.getDateOfJoining().plusDays(employee.getProbationPeriod());
//		            )
//		            if (employee.getIsConfirmedClicked() == 1 && employee.getEmploymentstatus().equals("Probation") && today.isEqual(probationEndDate))
//		            {
		            	Long hodId = employeeRepository.getDepartmentHod(employee.getEmpId());
		            	employee.setEmploymentstatus("Confirmed");
		            	employee.setUpdatedOn(LocalDateTime.now());
		                employee.setUpdatedBy(Integer.parseInt(hodId.toString()));
		                employeeRepository.save(employee);
//		            }
				}
				
				System.out.print("++++++++++++ =========== Running"+ today);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		//0 0 4 2 * ? - At 04:00:00am, on the 2nd day, every month
 		//0 0/2 * ? * * - Run at every 2 min
		
//		@Scheduled(cron="${monthlyTimesheetExcelGenerator.expression}")
		public ServiceResponse monthlyTimesheetExcelGenerator() {
			ServiceResponse response = new ServiceResponse();
			try {
				
				Calendar calendar = Calendar.getInstance();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				calendar.add(Calendar.MONTH, -1);
				calendar.set(Calendar.DATE, 1);

				LocalDate firstDateOfPreviousMonth = LocalDate.parse(dateFormat.format(calendar.getTime()));
				
				calendar.set(Calendar.DATE,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				LocalDate lastDateOfPreviousMonth = LocalDate.parse(dateFormat.format(calendar.getTime()));
				
				List<Object[]> employeeList = employeeRepository.getEmployeeDetailForCron();
				for(Object[] empObj : employeeList) {
					
					Long empId = empObj[0] != null ? Long.parseLong(empObj[0].toString()) : null;
					String empName = empObj[2] != null ? empObj[2].toString() : null;
					Long employeementId = empObj[3] != null ? Long.parseLong(empObj[3].toString()) : null;
					
					System.out.println("Emp ID :" + empId);
					System.out.println("Employment ID :" + employeementId);
					
					List<Timesheet> monthlyTimesheet = timesheetsRepository.
							findAllByEmpIdAndDateBetweenOrderByDateDesc(empId, firstDateOfPreviousMonth, lastDateOfPreviousMonth);
					
					List<PortalConfig> portalConfig = portalConfigRepository.findAll();
					String folderPath = null;
					if(!portalConfig.isEmpty()) {
						for(PortalConfig portalConfigObj : portalConfig) {
							if(portalConfigObj.getConfigName().equals("DSR Download Path")) {
								folderPath = portalConfigObj.getConfigValue();
							}
						}
					}
					System.out.println("Folder Path : " + folderPath);
					
					    Path path = Files.createDirectories(Paths.get(folderPath +"DSR" + File.separator + firstDateOfPreviousMonth.getYear() + File.separator + firstDateOfPreviousMonth.getMonth()));
						var f = new File(path + File.separator + employeementId + "-" + empName + "-" + firstDateOfPreviousMonth.getMonth() + ".xlsx");
						
						if(f.exists()) {
							f.delete();
						}
						
				        try (var fos = new FileOutputStream(f)) {

				            var wb = new Workbook(fos, "Application", "1.0");
				            Worksheet ws = wb.newWorksheet(firstDateOfPreviousMonth.getMonth() + " DSR");
				            
				            ws.value(0, 0, "Date");
				            ws.value(0, 1, "Day Type");
				            ws.value(0, 2, "Client");
				            ws.value(0, 3, "Client Location");
				            ws.value(0, 4, "Project");
				            ws.value(0, 5, "Activity");
				            ws.value(0, 6, "Total Working Hour");
				            ws.value(0, 7, "Holiday Type");
				            ws.value(0, 8, "Status");

				            int rowNum = 1;
							for(Timesheet timesheetObj: monthlyTimesheet) {								
								List<Object[]> objectList = timesheetActivityMapRepository.activitiesByTimesheetId(timesheetObj.getTimesheetId());
								
								String perviousProject = "";
								String perviousDate = "";
								String perviousClientName = "";
								String perviousClientLocation = "";
								
								if(!objectList.isEmpty()) {
									for(Object[] object : objectList) {
										
										String activity = object[1] != null ? object[1].toString() : null;
										String project = object[5] != null ? object[5].toString() : null;
										String clientName = object[6] != null ? object[6].toString() : null;
										String clientLocation = object[7] != null ? object[7].toString() : null;
										
										ws.style(rowNum, 0).format("yyyy-MM-dd").set();
										
										if(timesheetObj.getDate().toString().equals(perviousDate)) {
											ws.range(rowNum - 1, 0, rowNum, 0).merge();
											ws.range(rowNum - 1, 1, rowNum, 1).merge();
											ws.range(rowNum - 1, 6, rowNum, 6).merge();
											ws.range(rowNum - 1, 8, rowNum, 8).merge();
										}else {
											ws.value(rowNum, 0, timesheetObj.getDate());
											ws.value(rowNum, 1, timesheetObj.getDayType());
											ws.value(rowNum, 6, timesheetObj.getTotalTime());
											ws.value(rowNum, 8, timesheetObj.getStatus());
										}
										if(clientName.equals(perviousClientName) && timesheetObj.getDate().toString().equals(perviousDate)) {
											ws.range(rowNum - 1, 2, rowNum, 2).merge();
										}else {
											ws.value(rowNum, 2, clientName);
										}
										if(clientLocation.equals(perviousClientLocation) && timesheetObj.getDate().toString().equals(perviousDate)) {
											ws.range(rowNum - 1, 3, rowNum, 3).merge();
										}else {
											ws.value(rowNum, 3, clientLocation);
										}
										if(project.equals(perviousProject) && timesheetObj.getDate().toString().equals(perviousDate)) {
											ws.range(rowNum - 1, 4, rowNum, 4).merge();
										}else {
											ws.value(rowNum, 4, project);
										}
										if(!objectList.isEmpty()) {
											ws.value(rowNum, 5, activity);
										}else {
											ws.value(rowNum, 5, timesheetObj.getDescription());
										}
										
										
										rowNum++;
										perviousProject = project;
										perviousDate = timesheetObj.getDate().toString();
										perviousClientName = clientName;
										perviousClientLocation = clientLocation;
									}
								}else {
									
									// Fill data of weekoff & leave
									ws.style(rowNum, 0).format("yyyy-MM-dd").set();
									
									ws.value(rowNum, 0, timesheetObj.getDate());
									ws.value(rowNum, 1, timesheetObj.getDayType());
									ws.value(rowNum, 6, timesheetObj.getTotalTime());
									ws.value(rowNum, 7, timesheetObj.getDescription());
									ws.value(rowNum, 8, timesheetObj.getStatus());
									
									rowNum++;
									
									System.out.println("Activity List is empty");
								}
				        }
				            wb.finish();
				            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("DSR Generated Successfully.");
				            
				        }catch(Exception e) {
				        	e.printStackTrace();
				        	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("DSR creation failed");
				        }
				}
				
			}catch(Exception e) {
				e.printStackTrace();
				response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
				response.setServiceResponse("Something Went Wrong.");
				response.setServiceError(e.getMessage());
			}
			return response;
		}
		
//		0 0 7 ? * * - At 07:00:00am every day
		@Async
		@Scheduled(cron="${birthdaymail.cron.expression}")
		public void birthdayGreetingMail() {
			StringBuilder builder = new StringBuilder();
			
			List<Object[]> employeeObj = employeeRepository.getAllEmployeesBirthDayToday();
			List<EmployeeDTO> employeeList = new ArrayList<EmployeeDTO>();

			if(!employeeObj.isEmpty()) {
				for(Object[] object: employeeObj) {
					EmployeeDTO employee = new EmployeeDTO();

					employee.setName(object[0] != null ? object[0].toString() : null);
					employee.setEmail(object[2] != null ? object[2].toString() : null);

					employeeList.add(employee);
				}
			}
			Random random = new Random();
			Long id = (long) (random.nextInt(10 - 1) + 1); /* Random number will be generated between 1 and 25 */

			Optional<BirthdayMail> birthDayMail = birthdayMailRepository.findById(id);

			if (birthDayMail.isPresent() && !employeeList.isEmpty()) {
				for (EmployeeDTO emp : employeeList) {
					String subject = "Happy Birthday " + emp.getName();
					String heading = birthDayMail.get().getHeading();
					String description = birthDayMail.get().getDescription();
//					String mailBody = "<table style=\"background-color: #4E94CF; font-family: Arial; font-size: 14px; padding: 20px; width: 800px;\" align=\"center\">\n"
//							+ "    <tbody>\n" + "    <tr>\n"
//							+ "        <td class=\"wysiwyg-text-align-center\" style=\"padding: 20px;\"><span class=\"wysiwyg-color-black10\"></span><br />\n"
//							+ "            <table style=\"background-color: #ffffff;\" border=\"0\" width=\"700px\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\">\n"
//							+ "                <tbody>\n"
//							+ "                <tr style=\"padding-top: 20px; text-align: center;\">\n"
//							+ "                    <td style=\"padding: 30px;\">\n"
//							+ "                        <div style=\"text-align: left;\">Dear " + emp.getName() + ",</div>\n"
//							+ "<br>                    <p style=\"text-align: left;\"></p>\n"
//							+ "                        <div style=\"text-align: left;\">" + heading + "</div><br>\n"
//							+ "                        <div style=\"text-align: left;\">" + description + "</div>\n"
//							+ "<br><img src=\"cid:image\" />"
//							+ "                            <p style=\"text-align: left;\">Regards,<br>ApMoSyS</p>\n"
//							+ "                </tr>\n" + "                </tbody>\n" + "            </table>\n"
//							+ "        </td>\n" + "    </tr>\n" + "    </tbody>\n" + "</table>";
					
					String mailBody = "<!DOCTYPE html\n"
							+ "    PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
							+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">\n"
							+ "\n"
							+ "<head>\n"
							+ "    <meta charset=\"UTF-8\">\n"
							+ "    <meta content=\"width=device-width, initial-scale=1\" name=\"viewport\">\n"
							+ "    <meta name=\"x-apple-disable-message-reformatting\">\n"
							+ "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n"
							+ "    <meta content=\"telephone=no\" name=\"format-detection\">\n"
							+ "    <title></title>\n"
							+ "    <!--[if (mso 16)]>    <style type=\"text/css\">    a {text-decoration: none;}    </style>    <![endif]-->\n"
							+ "    <!--[if gte mso 9]><style>sup { font-size: 100% !important; }</style><![endif]-->\n"
							+ "    <!--[if gte mso 9]>\n"
							+ "<xml>\n"
							+ "    <o:OfficeDocumentSettings>\n"
							+ "    <o:AllowPNG></o:AllowPNG>\n"
							+ "    <o:PixelsPerInch>96</o:PixelsPerInch>\n"
							+ "    </o:OfficeDocumentSettings>\n"
							+ "</xml>\n"
							+ "<![endif]-->\n"
							+ "    <style>\n"
							+ "        /* CONFIG STYLES Please do not delete and edit CSS styles below */\n"
							+ "        /* IMPORTANT THIS STYLES MUST BE ON FINAL EMAIL */\n"
							+ "        #outlook a {\n"
							+ "            padding: 0;\n"
							+ "        }\n"
							+ "\n"
							+ "        .ExternalClass {\n"
							+ "            width: 100%;\n"
							+ "        }\n"
							+ "\n"
							+ "        .ExternalClass,\n"
							+ "        .ExternalClass p,\n"
							+ "        .ExternalClass span,\n"
							+ "        .ExternalClass font,\n"
							+ "        .ExternalClass td,\n"
							+ "        .ExternalClass div {\n"
							+ "            line-height: 100%;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-button {\n"
							+ "            mso-style-priority: 100 !important;\n"
							+ "            text-decoration: none !important;\n"
							+ "        }\n"
							+ "\n"
							+ "        a[x-apple-data-detectors] {\n"
							+ "            color: inherit !important;\n"
							+ "            text-decoration: none !important;\n"
							+ "            font-size: inherit !important;\n"
							+ "            font-family: inherit !important;\n"
							+ "            font-weight: inherit !important;\n"
							+ "            line-height: inherit !important;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-desk-hidden {\n"
							+ "            display: none;\n"
							+ "            float: left;\n"
							+ "            overflow: hidden;\n"
							+ "            width: 0;\n"
							+ "            max-height: 0;\n"
							+ "            line-height: 0;\n"
							+ "            mso-hide: all;\n"
							+ "        }\n"
							+ "\n"
							+ "        /*\n"
							+ "END OF IMPORTANT\n"
							+ "*/\n"
							+ "        s {\n"
							+ "            text-decoration: line-through;\n"
							+ "        }\n"
							+ "\n"
							+ "        html,\n"
							+ "        body {\n"
							+ "            width: 100%;\n"
							+ "            -webkit-text-size-adjust: 100%;\n"
							+ "            -ms-text-size-adjust: 100%;\n"
							+ "        }\n"
							+ "\n"
							+ "        body {\n"
							+ "            font-family: helvetica, 'helvetica neue', arial, verdana, sans-serif;\n"
							+ "        }\n"
							+ "\n"
							+ "        table {\n"
							+ "            mso-table-lspace: 0pt;\n"
							+ "            mso-table-rspace: 0pt;\n"
							+ "            border-collapse: collapse;\n"
							+ "            border-spacing: 0px;\n"
							+ "        }\n"
							+ "\n"
							+ "        table td,\n"
							+ "        html,\n"
							+ "        body,\n"
							+ "        .es-wrapper {\n"
							+ "            padding: 0;\n"
							+ "            Margin: 0;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-content,\n"
							+ "        .es-header,\n"
							+ "        .es-footer {\n"
							+ "            table-layout: fixed !important;\n"
							+ "            width: 100%;\n"
							+ "        }\n"
							+ "\n"
							+ "        img {\n"
							+ "            display: block;\n"
							+ "            border: 0;\n"
							+ "            outline: none;\n"
							+ "            text-decoration: none;\n"
							+ "            -ms-interpolation-mode: bicubic;\n"
							+ "        }\n"
							+ "\n"
							+ "        table tr {\n"
							+ "            border-collapse: collapse;\n"
							+ "        }\n"
							+ "\n"
							+ "        p,\n"
							+ "        hr {\n"
							+ "            Margin: 0;\n"
							+ "        }\n"
							+ "\n"
							+ "        h1,\n"
							+ "        h2,\n"
							+ "        h3,\n"
							+ "        h4,\n"
							+ "        h5 {\n"
							+ "            Margin: 0;\n"
							+ "            line-height: 120%;\n"
							+ "            mso-line-height-rule: exactly;\n"
							+ "            font-family: helvetica, 'helvetica neue', arial, verdana, sans-serif;\n"
							+ "        }\n"
							+ "\n"
							+ "        p,\n"
							+ "        ul li,\n"
							+ "        ol li,\n"
							+ "        a {\n"
							+ "            -webkit-text-size-adjust: none;\n"
							+ "            -ms-text-size-adjust: none;\n"
							+ "            mso-line-height-rule: exactly;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-left {\n"
							+ "            float: left;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-right {\n"
							+ "            float: right;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p5 {\n"
							+ "            padding: 5px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p5t {\n"
							+ "            padding-top: 5px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p5b {\n"
							+ "            padding-bottom: 5px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p5l {\n"
							+ "            padding-left: 5px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p5r {\n"
							+ "            padding-right: 5px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p10 {\n"
							+ "            padding: 10px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p10t {\n"
							+ "            padding-top: 10px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p10b {\n"
							+ "            padding-bottom: 10px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p10l {\n"
							+ "            padding-left: 10px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p10r {\n"
							+ "            padding-right: 10px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p15 {\n"
							+ "            padding: 15px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p15t {\n"
							+ "            padding-top: 15px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p15b {\n"
							+ "            padding-bottom: 15px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p15l {\n"
							+ "            padding-left: 15px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p15r {\n"
							+ "            padding-right: 15px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p20 {\n"
							+ "            padding: 20px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p20t {\n"
							+ "            padding-top: 20px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p20b {\n"
							+ "            padding-bottom: 20px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p20l {\n"
							+ "            padding-left: 20px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p20r {\n"
							+ "            padding-right: 20px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p25 {\n"
							+ "            padding: 25px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p25t {\n"
							+ "            padding-top: 25px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p25b {\n"
							+ "            padding-bottom: 25px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p25l {\n"
							+ "            padding-left: 25px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p25r {\n"
							+ "            padding-right: 25px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p30 {\n"
							+ "            padding: 30px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p30t {\n"
							+ "            padding-top: 30px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p30b {\n"
							+ "            padding-bottom: 30px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p30l {\n"
							+ "            padding-left: 30px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p30r {\n"
							+ "            padding-right: 30px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p35 {\n"
							+ "            padding: 35px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p35t {\n"
							+ "            padding-top: 35px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p35b {\n"
							+ "            padding-bottom: 35px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p35l {\n"
							+ "            padding-left: 35px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p35r {\n"
							+ "            padding-right: 35px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p40 {\n"
							+ "            padding: 40px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p40t {\n"
							+ "            padding-top: 40px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p40b {\n"
							+ "            padding-bottom: 40px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p40l {\n"
							+ "            padding-left: 40px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p40r {\n"
							+ "            padding-right: 40px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-menu td {\n"
							+ "            border: 0;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-menu td a img {\n"
							+ "            display: inline-block !important;\n"
							+ "        }\n"
							+ "\n"
							+ "        /* END CONFIG STYLES */\n"
							+ "        a {\n"
							+ "            text-decoration: underline;\n"
							+ "        }\n"
							+ "\n"
							+ "        p,\n"
							+ "        ul li,\n"
							+ "        ol li {\n"
							+ "            font-family: helvetica, 'helvetica neue', arial, verdana, sans-serif;\n"
							+ "            line-height: 150%;\n"
							+ "        }\n"
							+ "\n"
							+ "        ul li,\n"
							+ "        ol li {\n"
							+ "            Margin-bottom: 15px;\n"
							+ "            margin-left: 0;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-menu td a {\n"
							+ "            text-decoration: none;\n"
							+ "            display: block;\n"
							+ "            font-family: helvetica, 'helvetica neue', arial, verdana, sans-serif;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-wrapper {\n"
							+ "            width: 100%;\n"
							+ "            height: 100%;\n"
							+ "            background-repeat: repeat;\n"
							+ "            background-position: center top;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-wrapper-color,\n"
							+ "        .es-wrapper {\n"
							+ "            background-color: #f6f6f6;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-header {\n"
							+ "            background-color: transparent;\n"
							+ "            background-repeat: repeat;\n"
							+ "            background-position: center top;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-header-body {\n"
							+ "            background-color: transparent;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-header-body p,\n"
							+ "        .es-header-body ul li,\n"
							+ "        .es-header-body ol li {\n"
							+ "            color: #999999;\n"
							+ "            font-size: 14px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-header-body a {\n"
							+ "            color: #999999;\n"
							+ "            font-size: 14px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-content-body {\n"
							+ "            background-color: #ffffff;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-content-body p,\n"
							+ "        .es-content-body ul li,\n"
							+ "        .es-content-body ol li {\n"
							+ "            color: #040404;\n"
							+ "            font-size: 14px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-content-body a {\n"
							+ "            color: #040404;\n"
							+ "            font-size: 14px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-footer {\n"
							+ "            background-color: transparent;\n"
							+ "            background-repeat: repeat;\n"
							+ "            background-position: center top;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-footer-body {\n"
							+ "            background-color: #ffffff;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-footer-body p,\n"
							+ "        .es-footer-body ul li,\n"
							+ "        .es-footer-body ol li {\n"
							+ "            color: #ffffff;\n"
							+ "            font-size: 14px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-footer-body a {\n"
							+ "            color: #ffffff;\n"
							+ "            font-size: 14px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-infoblock,\n"
							+ "        .es-infoblock p,\n"
							+ "        .es-infoblock ul li,\n"
							+ "        .es-infoblock ol li {\n"
							+ "            line-height: 120%;\n"
							+ "            font-size: 12px;\n"
							+ "            color: #cccccc;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-infoblock a {\n"
							+ "            font-size: 12px;\n"
							+ "            color: #cccccc;\n"
							+ "        }\n"
							+ "\n"
							+ "        h1 {\n"
							+ "            font-size: 30px;\n"
							+ "            font-style: normal;\n"
							+ "            font-weight: bold;\n"
							+ "            color: #040404;\n"
							+ "        }\n"
							+ "\n"
							+ "        h2 {\n"
							+ "            font-size: 24px;\n"
							+ "            font-style: normal;\n"
							+ "            font-weight: bold;\n"
							+ "            color: #040404;\n"
							+ "        }\n"
							+ "\n"
							+ "        h3 {\n"
							+ "            font-size: 20px;\n"
							+ "            font-style: normal;\n"
							+ "            font-weight: bold;\n"
							+ "            color: #040404;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-header-body h1 a,\n"
							+ "        .es-content-body h1 a,\n"
							+ "        .es-footer-body h1 a {\n"
							+ "            font-size: 30px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-header-body h2 a,\n"
							+ "        .es-content-body h2 a,\n"
							+ "        .es-footer-body h2 a {\n"
							+ "            font-size: 24px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-header-body h3 a,\n"
							+ "        .es-content-body h3 a,\n"
							+ "        .es-footer-body h3 a {\n"
							+ "            font-size: 20px;\n"
							+ "        }\n"
							+ "\n"
							+ "        a.es-button,\n"
							+ "        button.es-button {\n"
							+ "            display: inline-block;\n"
							+ "            background: #38c2f1;\n"
							+ "            border-radius: 25px;\n"
							+ "            font-size: 18px;\n"
							+ "            font-family: helvetica, 'helvetica neue', arial, verdana, sans-serif;\n"
							+ "            font-weight: bold;\n"
							+ "            font-style: normal;\n"
							+ "            line-height: 120%;\n"
							+ "            color: #ffffff;\n"
							+ "            text-decoration: none;\n"
							+ "            width: auto;\n"
							+ "            text-align: center;\n"
							+ "            padding: 10px 30px 10px 30px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-button-border {\n"
							+ "            border-style: solid solid solid solid;\n"
							+ "            border-color: #38c2f1 #38c2f1 #38c2f1 #38c2f1;\n"
							+ "            background: #38c2f1;\n"
							+ "            border-width: 0px 0px 0px 0px;\n"
							+ "            display: inline-block;\n"
							+ "            border-radius: 25px;\n"
							+ "            width: auto;\n"
							+ "            mso-border-alt: 10px;\n"
							+ "        }\n"
							+ "\n"
							+ "        /* RESPONSIVE STYLES Please do not delete and edit CSS styles below. If you don't need responsive layout, please delete this section. */\n"
							+ "        @media only screen and (max-width: 600px) {\n"
							+ "\n"
							+ "            p,\n"
							+ "            ul li,\n"
							+ "            ol li,\n"
							+ "            a {\n"
							+ "                line-height: 150% !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            h1,\n"
							+ "            h2,\n"
							+ "            h3,\n"
							+ "            h1 a,\n"
							+ "            h2 a,\n"
							+ "            h3 a {\n"
							+ "                line-height: 120% !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            h1 {\n"
							+ "                font-size: 28px !important;\n"
							+ "                text-align: center;\n"
							+ "            }\n"
							+ "\n"
							+ "            h2 {\n"
							+ "                font-size: 26px !important;\n"
							+ "                text-align: center;\n"
							+ "            }\n"
							+ "\n"
							+ "            h3 {\n"
							+ "                font-size: 20px !important;\n"
							+ "                text-align: center;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-header-body h1 a,\n"
							+ "            .es-content-body h1 a,\n"
							+ "            .es-footer-body h1 a {\n"
							+ "                font-size: 28px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-header-body h2 a,\n"
							+ "            .es-content-body h2 a,\n"
							+ "            .es-footer-body h2 a {\n"
							+ "                font-size: 26px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-header-body h3 a,\n"
							+ "            .es-content-body h3 a,\n"
							+ "            .es-footer-body h3 a {\n"
							+ "                font-size: 20px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-menu td a {\n"
							+ "                font-size: 12px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-header-body p,\n"
							+ "            .es-header-body ul li,\n"
							+ "            .es-header-body ol li,\n"
							+ "            .es-header-body a {\n"
							+ "                font-size: 12px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-content-body p,\n"
							+ "            .es-content-body ul li,\n"
							+ "            .es-content-body ol li,\n"
							+ "            .es-content-body a {\n"
							+ "                font-size: 14px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-footer-body p,\n"
							+ "            .es-footer-body ul li,\n"
							+ "            .es-footer-body ol li,\n"
							+ "            .es-footer-body a {\n"
							+ "                font-size: 14px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-infoblock p,\n"
							+ "            .es-infoblock ul li,\n"
							+ "            .es-infoblock ol li,\n"
							+ "            .es-infoblock a {\n"
							+ "                font-size: 11px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            *[class=\"gmail-fix\"] {\n"
							+ "                display: none !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-txt-c,\n"
							+ "            .es-m-txt-c h1,\n"
							+ "            .es-m-txt-c h2,\n"
							+ "            .es-m-txt-c h3 {\n"
							+ "                text-align: center !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-txt-r,\n"
							+ "            .es-m-txt-r h1,\n"
							+ "            .es-m-txt-r h2,\n"
							+ "            .es-m-txt-r h3 {\n"
							+ "                text-align: right !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-txt-l,\n"
							+ "            .es-m-txt-l h1,\n"
							+ "            .es-m-txt-l h2,\n"
							+ "            .es-m-txt-l h3 {\n"
							+ "                text-align: left !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-txt-r img,\n"
							+ "            .es-m-txt-c img,\n"
							+ "            .es-m-txt-l img {\n"
							+ "                display: inline !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-button-border {\n"
							+ "                display: block !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            a.es-button,\n"
							+ "            button.es-button {\n"
							+ "                font-size: 14px !important;\n"
							+ "                display: block !important;\n"
							+ "                border-left-width: 0px !important;\n"
							+ "                border-right-width: 0px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-btn-fw {\n"
							+ "                border-width: 10px 0px !important;\n"
							+ "                text-align: center !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-adaptive table,\n"
							+ "            .es-btn-fw,\n"
							+ "            .es-btn-fw-brdr,\n"
							+ "            .es-left,\n"
							+ "            .es-right {\n"
							+ "                width: 100% !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-content table,\n"
							+ "            .es-header table,\n"
							+ "            .es-footer table,\n"
							+ "            .es-content,\n"
							+ "            .es-footer,\n"
							+ "            .es-header {\n"
							+ "                width: 100% !important;\n"
							+ "                max-width: 600px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-adapt-td {\n"
							+ "                display: block !important;\n"
							+ "                width: 100% !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .adapt-img {\n"
							+ "                width: 100% !important;\n"
							+ "                height: auto !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-p0 {\n"
							+ "                padding: 0px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-p0r {\n"
							+ "                padding-right: 0px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-p0l {\n"
							+ "                padding-left: 0px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-p0t {\n"
							+ "                padding-top: 0px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-p0b {\n"
							+ "                padding-bottom: 0 !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-p20b {\n"
							+ "                padding-bottom: 20px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-mobile-hidden,\n"
							+ "            .es-hidden {\n"
							+ "                display: none !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            tr.es-desk-hidden,\n"
							+ "            td.es-desk-hidden,\n"
							+ "            table.es-desk-hidden {\n"
							+ "                width: auto !important;\n"
							+ "                overflow: visible !important;\n"
							+ "                float: none !important;\n"
							+ "                max-height: inherit !important;\n"
							+ "                line-height: inherit !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            tr.es-desk-hidden {\n"
							+ "                display: table-row !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            table.es-desk-hidden {\n"
							+ "                display: table !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            td.es-desk-menu-hidden {\n"
							+ "                display: table-cell !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-menu td {\n"
							+ "                width: 1% !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            table.es-table-not-adapt,\n"
							+ "            .esd-block-html table {\n"
							+ "                width: auto !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            table.es-social {\n"
							+ "                display: inline-block !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            table.es-social td {\n"
							+ "                display: inline-block !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-desk-hidden {\n"
							+ "                display: table-row !important;\n"
							+ "                width: auto !important;\n"
							+ "                overflow: visible !important;\n"
							+ "                max-height: inherit !important;\n"
							+ "            }\n"
							+ "        }\n"
							+ "\n"
							+ "        /* END RESPONSIVE STYLES */\n"
							+ "    </style>\n"
							+ "\n"
							+ "</head>\n"
							+ "\n"
							+ "\n"
							+ "<body>\n"
							+ "    <div class=\"es-wrapper-color\">\n"
							+ "        <!--[if gte mso 9]>\n"
							+ "			<v:background xmlns:v=\"urn:schemas-microsoft-com:vml\" fill=\"t\">\n"
							+ "				<v:fill type=\"tile\" color=\"#f6f6f6\"></v:fill>\n"
							+ "			</v:background>\n"
							+ "		<![endif]-->\n"
							+ "        <table class=\"es-wrapper\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n"
							+ "            <tbody>\n"
							+ "                <tr>\n"
							+ "                    <td class=\"esd-email-paddings\" valign=\"top\">\n"
							+ "                        <table cellpadding=\"0\" cellspacing=\"0\" class=\"es-header esd-header-popover\" align=\"center\">\n"
							+ "                            <tbody>\n"
							+ "                                <tr>\n"
							+ "                                    <td class=\"esd-stripe\" align=\"center\" esd-custom-block-id=\"54593\">\n"
							+ "                                        <table bgcolor=\"transparent\" class=\"es-header-body\" align=\"center\"\n"
							+ "                                            cellpadding=\"0\" cellspacing=\"0\" width=\"600\"\n"
							+ "                                            style=\"background-color: transparent;\">\n"
							+ "                                            <tbody>\n"
							+ "                                                <tr>\n"
							+ "                                                    <td class=\"esd-structure es-p5\" align=\"left\"\n"
							+ "                                                        style=\"background-color: #ffffff;\" bgcolor=\"#ffffff\">\n"
							+ "                                                        <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n"
							+ "                                                            <tbody>\n"
							+ "                                                                <tr>\n"
							+ "                                                                    <td width=\"590\" class=\"esd-container-frame\"\n"
							+ "                                                                        align=\"left\">\n"
							+ "                                                                        <table cellpadding=\"0\" cellspacing=\"0\"\n"
							+ "                                                                            width=\"100%\">\n"
							+ "                                                                            <tbody>\n"
							+ "                                                                                <tr>\n"
							+ "                                                                                    <td align=\"left\"\n"
							+ "                                                                                        class=\"esd-block-image es-m-txt-c\"\n"
							+ "                                                                                        style=\"font-size: 0px;\"><a\n"
							+ "                                                                                            target=\"_blank\"\n"
							+ "                                                                                            href=\"https://viewstripo.email\"><img\n"
							+ "                                                                                                src=\"https://demo.stripocdn.email/content/guids/92d837ed-6ce0-4988-936d-da3c37cb746a/images/ishine_logo.jpeg\"\n"
							+ "                                                                                                alt\n"
							+ "                                                                                                style=\"display: block;\"\n"
							+ "                                                                                                width=\"135\"></a></td>\n"
							+ "                                                                                </tr>\n"
							+ "                                                                            </tbody>\n"
							+ "                                                                        </table>\n"
							+ "                                                                    </td>\n"
							+ "                                                                </tr>\n"
							+ "                                                            </tbody>\n"
							+ "                                                        </table>\n"
							+ "                                                    </td>\n"
							+ "                                                </tr>\n"
							+ "                                            </tbody>\n"
							+ "                                        </table>\n"
							+ "                                    </td>\n"
							+ "                                </tr>\n"
							+ "                            </tbody>\n"
							+ "                        </table>\n"
							+ "                        <table cellpadding=\"0\" cellspacing=\"0\" class=\"es-content\" align=\"center\">\n"
							+ "                            <tbody>\n"
							+ "                                <tr>\n"
							+ "                                    <td class=\"esd-stripe\" align=\"center\">\n"
							+ "                                        <table bgcolor=\"#ffffff\" class=\"es-content-body\" align=\"center\" cellpadding=\"0\"\n"
							+ "                                            cellspacing=\"0\" width=\"600\">\n"
							+ "                                            <tbody>\n"
							+ "                                                <tr>\n"
							+ "                                                    <td class=\"esd-structure\" align=\"left\"\n"
							+ "                                                        style=\"background-position: center top; background-color: #202447;\"\n"
							+ "                                                        bgcolor=\"#202447\" esd-custom-block-id=\"54591\">\n"
							+ "                                                        <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n"
							+ "                                                            <tbody>\n"
							+ "                                                                <tr>\n"
							+ "                                                                    <td width=\"600\"\n"
							+ "                                                                        class=\"esd-container-frame esd-checked\"\n"
							+ "                                                                        align=\"center\" valign=\"top\">\n"
							+ "                                                                        <table cellpadding=\"0\" cellspacing=\"0\"\n"
							+ "                                                                            width=\"100%\"\n"
							+ "                                                                            style=\"background-image:url(https://tlr.stripocdn.email/content/guids/CABINET_58bdfab47b91421ec71c0b7efc174ad6/images/3021564570245556.gif);background-position: left top; background-repeat: no-repeat;\"\n"
							+ "                                                                            background=\"https://tlr.stripocdn.email/content/guids/CABINET_58bdfab47b91421ec71c0b7efc174ad6/images/3021564570245556.gif\">\n"
							+ "                                                                            <tbody>\n"
							+ "                                                                                <tr>\n"
							+ "                                                                                    <td align=\"center\"\n"
							+ "                                                                                        class=\"esd-block-spacer\"\n"
							+ "                                                                                        height=\"118\"></td>\n"
							+ "                                                                                </tr>\n"
							+ "                                                                                <tr>\n"
							+ "                                                                                    <td align=\"center\"\n"
							+ "                                                                                        class=\"esd-block-text\">\n"
							+ "                                                                                        <h1 style=\"color: #ffffff;\">Happy Birthday</h1>\n"
							+ "                                                                                    </td>\n"
							+ "                                                                                </tr>\n"
							+ "                                                                                <tr>\n"
							+ "                                                                                    <td align=\"center\"\n"
							+ "                                                                                        class=\"esd-block-spacer\"\n"
							+ "                                                                                        height=\"118\"></td>\n"
							+ "                                                                                </tr>\n"
							+ "                                                                            </tbody>\n"
							+ "                                                                        </table>\n"
							+ "                                                                    </td>\n"
							+ "                                                                </tr>\n"
							+ "                                                            </tbody>\n"
							+ "                                                        </table>\n"
							+ "                                                    </td>\n"
							+ "                                                </tr>\n"
							+ "                                                <tr>\n"
							+ "                                                    <td class=\"esd-structure es-p20t es-p10b es-p20r es-p20l\"\n"
							+ "                                                        align=\"left\" style=\"background-position: center top;\"\n"
							+ "                                                        esd-custom-block-id=\"54592\">\n"
							+ "                                                        <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n"
							+ "                                                            <tbody>\n"
							+ "                                                                <tr>\n"
							+ "                                                                    <td width=\"560\" class=\"esd-container-frame\"\n"
							+ "                                                                        align=\"center\" valign=\"top\">\n"
							+ "                                                                        <table cellpadding=\"0\" cellspacing=\"0\"\n"
							+ "                                                                            width=\"100%\"\n"
							+ "                                                                            style=\"background-position: left top;\">\n"
							+ "                                                                            <tbody>\n"
							+ "                                                                                <tr>\n"
							+ "                                                                                    <td align=\"center\"\n"
							+ "                                                                                        class=\"esd-block-text es-p10b es-m-txt-c\">\n"
							+ "                                                                                        <h2>"+heading+"\n"
							+ "                                                                                        </h2>\n"
							+ "                                                                                    </td>\n"
							+ "                                                                                </tr>\n"
							+ "                                                                                <tr>\n"
							+ "                                                                                    <td align=\"center\"\n"
							+ "                                                                                        class=\"esd-block-text es-m-txt-c\">\n"
							+ "                                                                                        <h3>"+emp.getName()+"</h3>\n"
							+ "                                                                                    </td>\n"
							+ "                                                                                </tr>\n"
							+ "                                                                            </tbody>\n"
							+ "                                                                        </table>\n"
							+ "                                                                    </td>\n"
							+ "                                                                </tr>\n"
							+ "                                                            </tbody>\n"
							+ "                                                        </table>\n"
							+ "                                                    </td>\n"
							+ "                                                </tr>\n"
							+ "                                                <tr>\n"
							+ "                                                    <td class=\"esd-structure es-p15t\" align=\"left\">\n"
							+ "                                                        <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n"
							+ "                                                            <tbody>\n"
							+ "                                                                <tr>\n"
							+ "                                                                    <td width=\"600\" class=\"esd-container-frame\"\n"
							+ "                                                                        align=\"center\" valign=\"top\">\n"
							+ "                                                                        <table cellpadding=\"0\" cellspacing=\"0\"\n"
							+ "                                                                            width=\"100%\">\n"
							+ "                                                                            <tbody>\n"
							+ "                                                                                <tr>\n"
							+ "                                                                                    <td align=\"center\"\n"
							+ "                                                                                        class=\"esd-block-image\"\n"
							+ "                                                                                        style=\"font-size: 0px;\"><a\n"
							+ "                                                                                            target=\"_blank\"><img\n"
							+ "                                                                                                class=\"adapt-img\"\n"
							+ "                                                                                                <img src=\"cid:image\"\n"
							+ "                                                                                                alt\n"
							+ "                                                                                                style=\"display: block;\"\n"
							+ "                                                                                                width=\"546\"/></a></td>\n"
							+ "                                                                                </tr>\n"
							+ "                                                                            </tbody>\n"
							+ "                                                                        </table>\n"
							+ "                                                                    </td>\n"
							+ "                                                                </tr>\n"
							+ "                                                            </tbody>\n"
							+ "                                                        </table>\n"
							+ "                                                    </td>\n"
							+ "                                                </tr>\n"
							+ "                                            </tbody>\n"
							+ "                                        </table>\n"
							+ "                                    </td>\n"
							+ "                                </tr>\n"
							+ "                            </tbody>\n"
							+ "                        </table>\n"
							+ "                        <table cellpadding=\"0\" cellspacing=\"0\" class=\"es-footer esd-footer-popover\" align=\"center\">\n"
							+ "                            <tbody>\n"
							+ "                                <tr>\n"
							+ "                                    <td class=\"esd-stripe\" align=\"center\" esd-custom-block-id=\"54594\">\n"
							+ "                                        <table bgcolor=\"#ffffff\" class=\"es-footer-body\" align=\"center\" cellpadding=\"0\"\n"
							+ "                                            cellspacing=\"0\" width=\"600\">\n"
							+ "                                            <tbody>\n"
							+ "                                                <tr>\n"
							+ "                                                    <td class=\"esd-structure esd-checked es-p20t es-p20b es-p20r es-p20l\"\n"
							+ "                                                        align=\"left\"\n"
							+ "                                                        style=\"background-image: url('https://tlr.stripocdn.email/content/guids/CABINET_58bdfab47b91421ec71c0b7efc174ad6/images/63821564496145694.jpg'); background-position: left top; background-repeat: no-repeat; background-color: #333333;\"\n"
							+ "                                                        background=\"https://tlr.stripocdn.email/content/guids/CABINET_58bdfab47b91421ec71c0b7efc174ad6/images/63821564496145694.jpg\"\n"
							+ "                                                        bgcolor=\"#333333\">\n"
							+ "                                                        <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n"
							+ "                                                            <tbody>\n"
							+ "                                                                <tr>\n"
							+ "                                                                    <td width=\"560\" class=\"esd-container-frame\"\n"
							+ "                                                                        align=\"left\">\n"
							+ "                                                                        <table cellpadding=\"0\" cellspacing=\"0\"\n"
							+ "                                                                            width=\"100%\">\n"
							+ "                                                                            <tbody>\n"
							+ "                                                                                <tr>\n"
							+ "                                                                                    <td align=\"left\"\n"
							+ "                                                                                        class=\"esd-block-text es-m-txt-c\">\n"
							+ "                                                                                        <p style=\"line-height: 150%;\">\n"
							+ "                                                                                            <strong>Regards,</strong><strong></strong>\n"
							+ "                                                                                        </p>\n"
							+ "                                                                                        <p style=\"line-height: 150%;\">\n"
							+ "                                                                                            <strong>ApMoSys Technologies\n"
							+ "                                                                                                Pvt Ltd</strong><br></p>\n"
							+ "                                                                                    </td>\n"
							+ "                                                                                </tr>\n"
							+ "                                                                            </tbody>\n"
							+ "                                                                        </table>\n"
							+ "                                                                    </td>\n"
							+ "                                                                </tr>\n"
							+ "                                                            </tbody>\n"
							+ "                                                        </table>\n"
							+ "                                                    </td>\n"
							+ "                                                </tr>\n"
							+ "                                            </tbody>\n"
							+ "                                        </table>\n"
							+ "                                    </td>\n"
							+ "                                </tr>\n"
							+ "                            </tbody>\n"
							+ "                        </table>\n"
							+ "                    </td>\n"
							+ "                </tr>\n"
							+ "            </tbody>\n"
							+ "        </table>\n"
							+ "    </div>\n"
							+ "</body>\n"
							+ "\n"
							+ "</html>";

					try {
						boolean flag = mailService.sendMailWithImage(emp.getEmail(),hrMailAddress,subject, mailBody,birthDayMail.get().getBirthdayImage());
						String msg = "";
						if (flag) {
						//	System.out.println("mail sent to " + empEmail);
							msg = "Mail sent to " + emp.getEmail()+" ";
						} else {
						//	System.out.println("mail not sent to"+ empEmail+" ");
							msg = "Mail not sent to "+ emp.getEmail()+" ";
						}
						builder.append(msg);
					} catch (MessagingException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		
//		0 0 7 ? * * - At 07:00:00am every day
		@Async
		@Scheduled(cron="${birthdaymail.cron.expression}")
		public void birthdayReminderMail() {
			StringBuilder builder = new StringBuilder();
			
			List<Object[]> employeeObj = employeeRepository.getAllEmployeesBirthDayTomorrow();
			List<EmployeeDTO> employeeList = new ArrayList<EmployeeDTO>();

			if(!employeeObj.isEmpty()) {
				for(Object[] object: employeeObj) {
					EmployeeDTO employee = new EmployeeDTO();

					employee.setName(object[0] != null ? object[0].toString() : null);
					employee.setEmail(object[2] != null ? object[2].toString() : null);
					employee.setEmpId(object[3] != null ? Long.parseLong(object[3].toString()): null);

					employeeList.add(employee);
				}
			}
			

			if (!employeeList.isEmpty()) {
				for (EmployeeDTO emp : employeeList) {
					/* Team member List*/ 
					StringBuilder teamMemeberEmailbuilder = new StringBuilder();
					System.out.print("getAllTeamMemberView For  : "+ emp.getEmpId());
					List<Object[]> list = employeeRepository.getAllTeamMemberView(emp.getEmpId());
					List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
					
					System.out.print("Team Members  : "+ list);
					
					if (!list.isEmpty()) {
						for(Object[] object : list){
							if(object[2] != null) {
								if(list.lastIndexOf(object) == (list.size()-1)) {
									teamMemeberEmailbuilder.append(object[2].toString());								
								}else {
									teamMemeberEmailbuilder.append(object[2].toString() + ",");
								}								
							}
						};
					}
					
					System.out.print("teamMemeberEmailbuilder : "+ teamMemeberEmailbuilder.toString());
					
					String subject = "Birthday Reminder For " + emp.getName();
					String heading = "Tomorrow our colleague celebrates his/her birthday.";
					
					String mailBody = "<!DOCTYPE html\n"
							+ "    PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
							+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">\n"
							+ "\n"
							+ "<head>\n"
							+ "    <meta charset=\"UTF-8\">\n"
							+ "    <meta content=\"width=device-width, initial-scale=1\" name=\"viewport\">\n"
							+ "    <meta name=\"x-apple-disable-message-reformatting\">\n"
							+ "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n"
							+ "    <meta content=\"telephone=no\" name=\"format-detection\">\n"
							+ "    <title></title>\n"
							+ "    <!--[if (mso 16)]>    <style type=\"text/css\">    a {text-decoration: none;}    </style>    <![endif]-->\n"
							+ "    <!--[if gte mso 9]><style>sup { font-size: 100% !important; }</style><![endif]-->\n"
							+ "    <!--[if gte mso 9]>\n"
							+ "<xml>\n"
							+ "    <o:OfficeDocumentSettings>\n"
							+ "    <o:AllowPNG></o:AllowPNG>\n"
							+ "    <o:PixelsPerInch>96</o:PixelsPerInch>\n"
							+ "    </o:OfficeDocumentSettings>\n"
							+ "</xml>\n"
							+ "<![endif]-->\n"
							+ "    <style>\n"
							+ "        /* CONFIG STYLES Please do not delete and edit CSS styles below */\n"
							+ "        /* IMPORTANT THIS STYLES MUST BE ON FINAL EMAIL */\n"
							+ "        #outlook a {\n"
							+ "            padding: 0;\n"
							+ "        }\n"
							+ "\n"
							+ "        .ExternalClass {\n"
							+ "            width: 100%;\n"
							+ "        }\n"
							+ "\n"
							+ "        .ExternalClass,\n"
							+ "        .ExternalClass p,\n"
							+ "        .ExternalClass span,\n"
							+ "        .ExternalClass font,\n"
							+ "        .ExternalClass td,\n"
							+ "        .ExternalClass div {\n"
							+ "            line-height: 100%;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-button {\n"
							+ "            mso-style-priority: 100 !important;\n"
							+ "            text-decoration: none !important;\n"
							+ "        }\n"
							+ "\n"
							+ "        a[x-apple-data-detectors] {\n"
							+ "            color: inherit !important;\n"
							+ "            text-decoration: none !important;\n"
							+ "            font-size: inherit !important;\n"
							+ "            font-family: inherit !important;\n"
							+ "            font-weight: inherit !important;\n"
							+ "            line-height: inherit !important;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-desk-hidden {\n"
							+ "            display: none;\n"
							+ "            float: left;\n"
							+ "            overflow: hidden;\n"
							+ "            width: 0;\n"
							+ "            max-height: 0;\n"
							+ "            line-height: 0;\n"
							+ "            mso-hide: all;\n"
							+ "        }\n"
							+ "\n"
							+ "        /*\n"
							+ "END OF IMPORTANT\n"
							+ "*/\n"
							+ "        s {\n"
							+ "            text-decoration: line-through;\n"
							+ "        }\n"
							+ "\n"
							+ "        html,\n"
							+ "        body {\n"
							+ "            width: 100%;\n"
							+ "            -webkit-text-size-adjust: 100%;\n"
							+ "            -ms-text-size-adjust: 100%;\n"
							+ "        }\n"
							+ "\n"
							+ "        body {\n"
							+ "            font-family: helvetica, 'helvetica neue', arial, verdana, sans-serif;\n"
							+ "        }\n"
							+ "\n"
							+ "        table {\n"
							+ "            mso-table-lspace: 0pt;\n"
							+ "            mso-table-rspace: 0pt;\n"
							+ "            border-collapse: collapse;\n"
							+ "            border-spacing: 0px;\n"
							+ "        }\n"
							+ "\n"
							+ "        table td,\n"
							+ "        html,\n"
							+ "        body,\n"
							+ "        .es-wrapper {\n"
							+ "            padding: 0;\n"
							+ "            Margin: 0;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-content,\n"
							+ "        .es-header,\n"
							+ "        .es-footer {\n"
							+ "            table-layout: fixed !important;\n"
							+ "            width: 100%;\n"
							+ "        }\n"
							+ "\n"
							+ "        img {\n"
							+ "            display: block;\n"
							+ "            border: 0;\n"
							+ "            outline: none;\n"
							+ "            text-decoration: none;\n"
							+ "            -ms-interpolation-mode: bicubic;\n"
							+ "        }\n"
							+ "\n"
							+ "        table tr {\n"
							+ "            border-collapse: collapse;\n"
							+ "        }\n"
							+ "\n"
							+ "        p,\n"
							+ "        hr {\n"
							+ "            Margin: 0;\n"
							+ "        }\n"
							+ "\n"
							+ "        h1,\n"
							+ "        h2,\n"
							+ "        h3,\n"
							+ "        h4,\n"
							+ "        h5 {\n"
							+ "            Margin: 0;\n"
							+ "            line-height: 120%;\n"
							+ "            mso-line-height-rule: exactly;\n"
							+ "            font-family: helvetica, 'helvetica neue', arial, verdana, sans-serif;\n"
							+ "        }\n"
							+ "\n"
							+ "        p,\n"
							+ "        ul li,\n"
							+ "        ol li,\n"
							+ "        a {\n"
							+ "            -webkit-text-size-adjust: none;\n"
							+ "            -ms-text-size-adjust: none;\n"
							+ "            mso-line-height-rule: exactly;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-left {\n"
							+ "            float: left;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-right {\n"
							+ "            float: right;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p5 {\n"
							+ "            padding: 5px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p5t {\n"
							+ "            padding-top: 5px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p5b {\n"
							+ "            padding-bottom: 5px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p5l {\n"
							+ "            padding-left: 5px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p5r {\n"
							+ "            padding-right: 5px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p10 {\n"
							+ "            padding: 10px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p10t {\n"
							+ "            padding-top: 10px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p10b {\n"
							+ "            padding-bottom: 10px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p10l {\n"
							+ "            padding-left: 10px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p10r {\n"
							+ "            padding-right: 10px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p15 {\n"
							+ "            padding: 15px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p15t {\n"
							+ "            padding-top: 15px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p15b {\n"
							+ "            padding-bottom: 15px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p15l {\n"
							+ "            padding-left: 15px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p15r {\n"
							+ "            padding-right: 15px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p20 {\n"
							+ "            padding: 20px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p20t {\n"
							+ "            padding-top: 20px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p20b {\n"
							+ "            padding-bottom: 20px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p20l {\n"
							+ "            padding-left: 20px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p20r {\n"
							+ "            padding-right: 20px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p25 {\n"
							+ "            padding: 25px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p25t {\n"
							+ "            padding-top: 25px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p25b {\n"
							+ "            padding-bottom: 25px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p25l {\n"
							+ "            padding-left: 25px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p25r {\n"
							+ "            padding-right: 25px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p30 {\n"
							+ "            padding: 30px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p30t {\n"
							+ "            padding-top: 30px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p30b {\n"
							+ "            padding-bottom: 30px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p30l {\n"
							+ "            padding-left: 30px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p30r {\n"
							+ "            padding-right: 30px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p35 {\n"
							+ "            padding: 35px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p35t {\n"
							+ "            padding-top: 35px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p35b {\n"
							+ "            padding-bottom: 35px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p35l {\n"
							+ "            padding-left: 35px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p35r {\n"
							+ "            padding-right: 35px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p40 {\n"
							+ "            padding: 40px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p40t {\n"
							+ "            padding-top: 40px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p40b {\n"
							+ "            padding-bottom: 40px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p40l {\n"
							+ "            padding-left: 40px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-p40r {\n"
							+ "            padding-right: 40px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-menu td {\n"
							+ "            border: 0;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-menu td a img {\n"
							+ "            display: inline-block !important;\n"
							+ "        }\n"
							+ "\n"
							+ "        /* END CONFIG STYLES */\n"
							+ "        a {\n"
							+ "            text-decoration: underline;\n"
							+ "        }\n"
							+ "\n"
							+ "        p,\n"
							+ "        ul li,\n"
							+ "        ol li {\n"
							+ "            font-family: helvetica, 'helvetica neue', arial, verdana, sans-serif;\n"
							+ "            line-height: 150%;\n"
							+ "        }\n"
							+ "\n"
							+ "        ul li,\n"
							+ "        ol li {\n"
							+ "            Margin-bottom: 15px;\n"
							+ "            margin-left: 0;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-menu td a {\n"
							+ "            text-decoration: none;\n"
							+ "            display: block;\n"
							+ "            font-family: helvetica, 'helvetica neue', arial, verdana, sans-serif;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-wrapper {\n"
							+ "            width: 100%;\n"
							+ "            height: 100%;\n"
							+ "            background-repeat: repeat;\n"
							+ "            background-position: center top;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-wrapper-color,\n"
							+ "        .es-wrapper {\n"
							+ "            background-color: #f6f6f6;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-header {\n"
							+ "            background-color: transparent;\n"
							+ "            background-repeat: repeat;\n"
							+ "            background-position: center top;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-header-body {\n"
							+ "            background-color: transparent;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-header-body p,\n"
							+ "        .es-header-body ul li,\n"
							+ "        .es-header-body ol li {\n"
							+ "            color: #999999;\n"
							+ "            font-size: 14px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-header-body a {\n"
							+ "            color: #999999;\n"
							+ "            font-size: 14px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-content-body {\n"
							+ "            background-color: #ffffff;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-content-body p,\n"
							+ "        .es-content-body ul li,\n"
							+ "        .es-content-body ol li {\n"
							+ "            color: #040404;\n"
							+ "            font-size: 14px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-content-body a {\n"
							+ "            color: #040404;\n"
							+ "            font-size: 14px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-footer {\n"
							+ "            background-color: transparent;\n"
							+ "            background-repeat: repeat;\n"
							+ "            background-position: center top;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-footer-body {\n"
							+ "            background-color: #ffffff;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-footer-body p,\n"
							+ "        .es-footer-body ul li,\n"
							+ "        .es-footer-body ol li {\n"
							+ "            color: #ffffff;\n"
							+ "            font-size: 14px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-footer-body a {\n"
							+ "            color: #ffffff;\n"
							+ "            font-size: 14px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-infoblock,\n"
							+ "        .es-infoblock p,\n"
							+ "        .es-infoblock ul li,\n"
							+ "        .es-infoblock ol li {\n"
							+ "            line-height: 120%;\n"
							+ "            font-size: 12px;\n"
							+ "            color: #cccccc;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-infoblock a {\n"
							+ "            font-size: 12px;\n"
							+ "            color: #cccccc;\n"
							+ "        }\n"
							+ "\n"
							+ "        h1 {\n"
							+ "            font-size: 30px;\n"
							+ "            font-style: normal;\n"
							+ "            font-weight: bold;\n"
							+ "            color: #040404;\n"
							+ "        }\n"
							+ "\n"
							+ "        h2 {\n"
							+ "            font-size: 24px;\n"
							+ "            font-style: normal;\n"
							+ "            font-weight: bold;\n"
							+ "            color: #040404;\n"
							+ "        }\n"
							+ "\n"
							+ "        h3 {\n"
							+ "            font-size: 20px;\n"
							+ "            font-style: normal;\n"
							+ "            font-weight: bold;\n"
							+ "            color: #040404;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-header-body h1 a,\n"
							+ "        .es-content-body h1 a,\n"
							+ "        .es-footer-body h1 a {\n"
							+ "            font-size: 30px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-header-body h2 a,\n"
							+ "        .es-content-body h2 a,\n"
							+ "        .es-footer-body h2 a {\n"
							+ "            font-size: 24px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-header-body h3 a,\n"
							+ "        .es-content-body h3 a,\n"
							+ "        .es-footer-body h3 a {\n"
							+ "            font-size: 20px;\n"
							+ "        }\n"
							+ "\n"
							+ "        a.es-button,\n"
							+ "        button.es-button {\n"
							+ "            display: inline-block;\n"
							+ "            background: #38c2f1;\n"
							+ "            border-radius: 25px;\n"
							+ "            font-size: 18px;\n"
							+ "            font-family: helvetica, 'helvetica neue', arial, verdana, sans-serif;\n"
							+ "            font-weight: bold;\n"
							+ "            font-style: normal;\n"
							+ "            line-height: 120%;\n"
							+ "            color: #ffffff;\n"
							+ "            text-decoration: none;\n"
							+ "            width: auto;\n"
							+ "            text-align: center;\n"
							+ "            padding: 10px 30px 10px 30px;\n"
							+ "        }\n"
							+ "\n"
							+ "        .es-button-border {\n"
							+ "            border-style: solid solid solid solid;\n"
							+ "            border-color: #38c2f1 #38c2f1 #38c2f1 #38c2f1;\n"
							+ "            background: #38c2f1;\n"
							+ "            border-width: 0px 0px 0px 0px;\n"
							+ "            display: inline-block;\n"
							+ "            border-radius: 25px;\n"
							+ "            width: auto;\n"
							+ "            mso-border-alt: 10px;\n"
							+ "        }\n"
							+ "\n"
							+ "        /* RESPONSIVE STYLES Please do not delete and edit CSS styles below. If you don't need responsive layout, please delete this section. */\n"
							+ "        @media only screen and (max-width: 600px) {\n"
							+ "\n"
							+ "            p,\n"
							+ "            ul li,\n"
							+ "            ol li,\n"
							+ "            a {\n"
							+ "                line-height: 150% !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            h1,\n"
							+ "            h2,\n"
							+ "            h3,\n"
							+ "            h1 a,\n"
							+ "            h2 a,\n"
							+ "            h3 a {\n"
							+ "                line-height: 120% !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            h1 {\n"
							+ "                font-size: 28px !important;\n"
							+ "                text-align: center;\n"
							+ "            }\n"
							+ "\n"
							+ "            h2 {\n"
							+ "                font-size: 26px !important;\n"
							+ "                text-align: center;\n"
							+ "            }\n"
							+ "\n"
							+ "            h3 {\n"
							+ "                font-size: 20px !important;\n"
							+ "                text-align: center;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-header-body h1 a,\n"
							+ "            .es-content-body h1 a,\n"
							+ "            .es-footer-body h1 a {\n"
							+ "                font-size: 28px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-header-body h2 a,\n"
							+ "            .es-content-body h2 a,\n"
							+ "            .es-footer-body h2 a {\n"
							+ "                font-size: 26px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-header-body h3 a,\n"
							+ "            .es-content-body h3 a,\n"
							+ "            .es-footer-body h3 a {\n"
							+ "                font-size: 20px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-menu td a {\n"
							+ "                font-size: 12px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-header-body p,\n"
							+ "            .es-header-body ul li,\n"
							+ "            .es-header-body ol li,\n"
							+ "            .es-header-body a {\n"
							+ "                font-size: 12px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-content-body p,\n"
							+ "            .es-content-body ul li,\n"
							+ "            .es-content-body ol li,\n"
							+ "            .es-content-body a {\n"
							+ "                font-size: 14px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-footer-body p,\n"
							+ "            .es-footer-body ul li,\n"
							+ "            .es-footer-body ol li,\n"
							+ "            .es-footer-body a {\n"
							+ "                font-size: 14px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-infoblock p,\n"
							+ "            .es-infoblock ul li,\n"
							+ "            .es-infoblock ol li,\n"
							+ "            .es-infoblock a {\n"
							+ "                font-size: 11px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            *[class=\"gmail-fix\"] {\n"
							+ "                display: none !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-txt-c,\n"
							+ "            .es-m-txt-c h1,\n"
							+ "            .es-m-txt-c h2,\n"
							+ "            .es-m-txt-c h3 {\n"
							+ "                text-align: center !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-txt-r,\n"
							+ "            .es-m-txt-r h1,\n"
							+ "            .es-m-txt-r h2,\n"
							+ "            .es-m-txt-r h3 {\n"
							+ "                text-align: right !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-txt-l,\n"
							+ "            .es-m-txt-l h1,\n"
							+ "            .es-m-txt-l h2,\n"
							+ "            .es-m-txt-l h3 {\n"
							+ "                text-align: left !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-txt-r img,\n"
							+ "            .es-m-txt-c img,\n"
							+ "            .es-m-txt-l img {\n"
							+ "                display: inline !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-button-border {\n"
							+ "                display: block !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            a.es-button,\n"
							+ "            button.es-button {\n"
							+ "                font-size: 14px !important;\n"
							+ "                display: block !important;\n"
							+ "                border-left-width: 0px !important;\n"
							+ "                border-right-width: 0px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-btn-fw {\n"
							+ "                border-width: 10px 0px !important;\n"
							+ "                text-align: center !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-adaptive table,\n"
							+ "            .es-btn-fw,\n"
							+ "            .es-btn-fw-brdr,\n"
							+ "            .es-left,\n"
							+ "            .es-right {\n"
							+ "                width: 100% !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-content table,\n"
							+ "            .es-header table,\n"
							+ "            .es-footer table,\n"
							+ "            .es-content,\n"
							+ "            .es-footer,\n"
							+ "            .es-header {\n"
							+ "                width: 100% !important;\n"
							+ "                max-width: 600px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-adapt-td {\n"
							+ "                display: block !important;\n"
							+ "                width: 100% !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .adapt-img {\n"
							+ "                width: 100% !important;\n"
							+ "                height: auto !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-p0 {\n"
							+ "                padding: 0px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-p0r {\n"
							+ "                padding-right: 0px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-p0l {\n"
							+ "                padding-left: 0px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-p0t {\n"
							+ "                padding-top: 0px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-p0b {\n"
							+ "                padding-bottom: 0 !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-m-p20b {\n"
							+ "                padding-bottom: 20px !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-mobile-hidden,\n"
							+ "            .es-hidden {\n"
							+ "                display: none !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            tr.es-desk-hidden,\n"
							+ "            td.es-desk-hidden,\n"
							+ "            table.es-desk-hidden {\n"
							+ "                width: auto !important;\n"
							+ "                overflow: visible !important;\n"
							+ "                float: none !important;\n"
							+ "                max-height: inherit !important;\n"
							+ "                line-height: inherit !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            tr.es-desk-hidden {\n"
							+ "                display: table-row !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            table.es-desk-hidden {\n"
							+ "                display: table !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            td.es-desk-menu-hidden {\n"
							+ "                display: table-cell !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-menu td {\n"
							+ "                width: 1% !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            table.es-table-not-adapt,\n"
							+ "            .esd-block-html table {\n"
							+ "                width: auto !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            table.es-social {\n"
							+ "                display: inline-block !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            table.es-social td {\n"
							+ "                display: inline-block !important;\n"
							+ "            }\n"
							+ "\n"
							+ "            .es-desk-hidden {\n"
							+ "                display: table-row !important;\n"
							+ "                width: auto !important;\n"
							+ "                overflow: visible !important;\n"
							+ "                max-height: inherit !important;\n"
							+ "            }\n"
							+ "        }\n"
							+ "\n"
							+ "        /* END RESPONSIVE STYLES */\n"
							+ "    </style>\n"
							+ "\n"
							+ "</head>\n"
							+ "\n"
							+ "\n"
							+ "<body>\n"
							+ "    <div class=\"es-wrapper-color\">\n"
							+ "        <!--[if gte mso 9]>\n"
							+ "			<v:background xmlns:v=\"urn:schemas-microsoft-com:vml\" fill=\"t\">\n"
							+ "				<v:fill type=\"tile\" color=\"#f6f6f6\"></v:fill>\n"
							+ "			</v:background>\n"
							+ "		<![endif]-->\n"
							+ "        <table class=\"es-wrapper\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n"
							+ "            <tbody>\n"
							+ "                <tr>\n"
							+ "                    <td class=\"esd-email-paddings\" valign=\"top\">\n"
							+ "                        <table cellpadding=\"0\" cellspacing=\"0\" class=\"es-header esd-header-popover\" align=\"center\">\n"
							+ "                            <tbody>\n"
							+ "                                <tr>\n"
							+ "                                    <td class=\"esd-stripe\" align=\"center\" esd-custom-block-id=\"54593\">\n"
							+ "                                        <table bgcolor=\"transparent\" class=\"es-header-body\" align=\"center\"\n"
							+ "                                            cellpadding=\"0\" cellspacing=\"0\" width=\"600\"\n"
							+ "                                            style=\"background-color: transparent;\">\n"
							+ "                                            <tbody>\n"
							+ "                                                <tr>\n"
							+ "                                                    <td class=\"esd-structure es-p5\" align=\"left\"\n"
							+ "                                                        style=\"background-color: #ffffff;\" bgcolor=\"#ffffff\">\n"
							+ "                                                        <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n"
							+ "                                                            <tbody>\n"
							+ "                                                                <tr>\n"
							+ "                                                                    <td width=\"590\" class=\"esd-container-frame\"\n"
							+ "                                                                        align=\"left\">\n"
							+ "                                                                        <table cellpadding=\"0\" cellspacing=\"0\"\n"
							+ "                                                                            width=\"100%\">\n"
							+ "                                                                            <tbody>\n"
							+ "                                                                                <tr>\n"
							+ "                                                                                    <td align=\"left\"\n"
							+ "                                                                                        class=\"esd-block-image es-m-txt-c\"\n"
							+ "                                                                                        style=\"font-size: 0px;\"><a\n"
							+ "                                                                                            target=\"_blank\"\n"
							+ "                                                                                            href=\"https://viewstripo.email\"><img\n"
							+ "                                                                                                src=\"https://demo.stripocdn.email/content/guids/92d837ed-6ce0-4988-936d-da3c37cb746a/images/ishine_logo.jpeg\"\n"
							+ "                                                                                                alt\n"
							+ "                                                                                                style=\"display: block;\"\n"
							+ "                                                                                                width=\"135\"></a></td>\n"
							+ "                                                                                </tr>\n"
							+ "                                                                            </tbody>\n"
							+ "                                                                        </table>\n"
							+ "                                                                    </td>\n"
							+ "                                                                </tr>\n"
							+ "                                                            </tbody>\n"
							+ "                                                        </table>\n"
							+ "                                                    </td>\n"
							+ "                                                </tr>\n"
							+ "                                            </tbody>\n"
							+ "                                        </table>\n"
							+ "                                    </td>\n"
							+ "                                </tr>\n"
							+ "                            </tbody>\n"
							+ "                        </table>\n"
							+ "                        <table cellpadding=\"0\" cellspacing=\"0\" class=\"es-content\" align=\"center\">\n"
							+ "                            <tbody>\n"
							+ "                                <tr>\n"
							+ "                                    <td class=\"esd-stripe\" align=\"center\">\n"
							+ "                                        <table bgcolor=\"#ffffff\" class=\"es-content-body\" align=\"center\" cellpadding=\"0\"\n"
							+ "                                            cellspacing=\"0\" width=\"600\">\n"
							+ "                                            <tbody>\n"
							+ "                                                <tr>\n"
							+ "                                                    <td class=\"esd-structure\" align=\"left\"\n"
							+ "                                                        style=\"background-position: center top; background-color: #202447;\"\n"
							+ "                                                        bgcolor=\"#202447\" esd-custom-block-id=\"54591\">\n"
							+ "                                                        <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n"
							+ "                                                            <tbody>\n"
							+ "                                                                <tr>\n"
							+ "                                                                    <td width=\"600\"\n"
							+ "                                                                        class=\"esd-container-frame esd-checked\"\n"
							+ "                                                                        align=\"center\" valign=\"top\">\n"
							+ "                                                                        <table cellpadding=\"0\" cellspacing=\"0\"\n"
							+ "                                                                            width=\"100%\"\n"
							+ "                                                                            style=\"background-image:url(https://tlr.stripocdn.email/content/guids/CABINET_58bdfab47b91421ec71c0b7efc174ad6/images/3021564570245556.gif);background-position: left top; background-repeat: no-repeat;\"\n"
							+ "                                                                            background=\"https://tlr.stripocdn.email/content/guids/CABINET_58bdfab47b91421ec71c0b7efc174ad6/images/3021564570245556.gif\">\n"
							+ "                                                                            <tbody>\n"
							+ "                                                                                <tr>\n"
							+ "                                                                                    <td align=\"center\"\n"
							+ "                                                                                        class=\"esd-block-spacer\"\n"
							+ "                                                                                        height=\"118\"></td>\n"
							+ "                                                                                </tr>\n"
							+ "                                                                                <tr>\n"
							+ "                                                                                    <td align=\"center\"\n"
							+ "                                                                                        class=\"esd-block-text\">\n"
							+ "                                                                                        <h1 style=\"color: #ffffff;\">A\n"
							+ "                                                                                            friendly reminder of an\n"
							+ "                                                                                            important<br></h1>\n"
							+ "                                                                                        <h1 style=\"color: #ffffff;\">date\n"
							+ "                                                                                            in our team.<br></h1>\n"
							+ "                                                                                    </td>\n"
							+ "                                                                                </tr>\n"
							+ "                                                                                <tr>\n"
							+ "                                                                                    <td align=\"center\"\n"
							+ "                                                                                        class=\"esd-block-spacer\"\n"
							+ "                                                                                        height=\"118\"></td>\n"
							+ "                                                                                </tr>\n"
							+ "                                                                            </tbody>\n"
							+ "                                                                        </table>\n"
							+ "                                                                    </td>\n"
							+ "                                                                </tr>\n"
							+ "                                                            </tbody>\n"
							+ "                                                        </table>\n"
							+ "                                                    </td>\n"
							+ "                                                </tr>\n"
							+ "                                                <tr>\n"
							+ "                                                    <td class=\"esd-structure es-p20t es-p10b es-p20r es-p20l\"\n"
							+ "                                                        align=\"left\" style=\"background-position: center top;\"\n"
							+ "                                                        esd-custom-block-id=\"54592\">\n"
							+ "                                                        <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n"
							+ "                                                            <tbody>\n"
							+ "                                                                <tr>\n"
							+ "                                                                    <td width=\"560\" class=\"esd-container-frame\"\n"
							+ "                                                                        align=\"center\" valign=\"top\">\n"
							+ "                                                                        <table cellpadding=\"0\" cellspacing=\"0\"\n"
							+ "                                                                            width=\"100%\"\n"
							+ "                                                                            style=\"background-position: left top;\">\n"
							+ "                                                                            <tbody>\n"
							+ "                                                                                <tr>\n"
							+ "                                                                                    <td align=\"center\"\n"
							+ "                                                                                        class=\"esd-block-text es-p10b es-m-txt-c\">\n"
							+ "                                                                                        <h2>"+heading+"\n"
							+ "                                                                                        </h2>\n"
							+ "                                                                                    </td>\n"
							+ "                                                                                </tr>\n"
							+ "                                                                                <tr>\n"
							+ "                                                                                    <td align=\"center\"\n"
							+ "                                                                                        class=\"esd-block-text es-m-txt-c\">\n"
							+ "                                                                                        <h3>"+emp.getName()+"</h3>\n"
							+ "                                                                                    </td>\n"
							+ "                                                                                </tr>\n"
							+ "                                                                            </tbody>\n"
							+ "                                                                        </table>\n"
							+ "                                                                    </td>\n"
							+ "                                                                </tr>\n"
							+ "                                                            </tbody>\n"
							+ "                                                        </table>\n"
							+ "                                                    </td>\n"
							+ "                                                </tr>\n"
							+ "                                            </tbody>\n"
							+ "                                        </table>\n"
							+ "                                    </td>\n"
							+ "                                </tr>\n"
							+ "                            </tbody>\n"
							+ "                        </table>\n"
							+ "                        <table cellpadding=\"0\" cellspacing=\"0\" class=\"es-footer esd-footer-popover\" align=\"center\">\n"
							+ "                            <tbody>\n"
							+ "                                <tr>\n"
							+ "                                    <td class=\"esd-stripe\" align=\"center\" esd-custom-block-id=\"54594\">\n"
							+ "                                        <table bgcolor=\"#ffffff\" class=\"es-footer-body\" align=\"center\" cellpadding=\"0\"\n"
							+ "                                            cellspacing=\"0\" width=\"600\">\n"
							+ "                                            <tbody>\n"
							+ "                                                <tr>\n"
							+ "                                                    <td class=\"esd-structure esd-checked es-p20t es-p20b es-p20r es-p20l\"\n"
							+ "                                                        align=\"left\"\n"
							+ "                                                        style=\"background-image: url('https://tlr.stripocdn.email/content/guids/CABINET_58bdfab47b91421ec71c0b7efc174ad6/images/63821564496145694.jpg'); background-position: left top; background-repeat: no-repeat; background-color: #333333;\"\n"
							+ "                                                        background=\"https://tlr.stripocdn.email/content/guids/CABINET_58bdfab47b91421ec71c0b7efc174ad6/images/63821564496145694.jpg\"\n"
							+ "                                                        bgcolor=\"#333333\">\n"
							+ "                                                        <table cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n"
							+ "                                                            <tbody>\n"
							+ "                                                                <tr>\n"
							+ "                                                                    <td width=\"560\" class=\"esd-container-frame\"\n"
							+ "                                                                        align=\"left\">\n"
							+ "                                                                        <table cellpadding=\"0\" cellspacing=\"0\"\n"
							+ "                                                                            width=\"100%\">\n"
							+ "                                                                            <tbody>\n"
							+ "                                                                                <tr>\n"
							+ "                                                                                    <td align=\"left\"\n"
							+ "                                                                                        class=\"esd-block-text es-m-txt-c\">\n"
							+ "                                                                                        <p style=\"line-height: 150%;\">\n"
							+ "                                                                                            <strong>Regards,</strong><strong></strong>\n"
							+ "                                                                                        </p>\n"
							+ "                                                                                        <p style=\"line-height: 150%;\">\n"
							+ "                                                                                            <strong>ApMoSys Technologies\n"
							+ "                                                                                                Pvt Ltd</strong><br></p>\n"
							+ "                                                                                    </td>\n"
							+ "                                                                                </tr>\n"
							+ "                                                                            </tbody>\n"
							+ "                                                                        </table>\n"
							+ "                                                                    </td>\n"
							+ "                                                                </tr>\n"
							+ "                                                            </tbody>\n"
							+ "                                                        </table>\n"
							+ "                                                    </td>\n"
							+ "                                                </tr>\n"
							+ "                                            </tbody>\n"
							+ "                                        </table>\n"
							+ "                                    </td>\n"
							+ "                                </tr>\n"
							+ "                            </tbody>\n"
							+ "                        </table>\n"
							+ "                    </td>\n"
							+ "                </tr>\n"
							+ "            </tbody>\n"
							+ "        </table>\n"
							+ "    </div>\n"
							+ "</body>\n"
							+ "\n"
							+ "</html>";

					try {
						boolean flag = mailService.sendMailWithCC(teamMemeberEmailbuilder.toString(),hrMailAddress,subject, mailBody);
						String msg = "";
						if (flag) {
						//	System.out.println("mail sent to " + empEmail);
							msg = "Mail sent to " + emp.getEmail()+" ";
						} else {
						//	System.out.println("mail not sent to"+ empEmail+" ");
							msg = "Mail not sent to "+ emp.getEmail()+" ";
							
						}
						builder.append(msg);
					} catch (MessagingException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		@Async
		@Scheduled(cron = "0 0 9 ? * *")
		public void resignationMailConsent() {
			try {
				List<Object[]> employeeObj = employeeRepository.getEmployeeByDateOfRelieving();
				
				if(!employeeObj.isEmpty()) {
					employeeObj.forEach((object) -> {
						//send mail to manager
						Long empId = object[1] != null ? Long.parseLong(object[1].toString()) : null;
						String managerEmail = object[3] != null ? object[3].toString() : null;
						String managerName = object[4] != null ? object[4].toString() : null;
						String empName = object[5] != null ? object[5].toString() : null;
						String dateOfResign = object[6] != null ? object[6].toString() : null;
						String dateOfRelieving = object[7] != null ? object[7].toString() : null;
						String department = object[8] != null ? object[8].toString() : null;
						try {
							mailService.sendMail(managerEmail, "Asset Consent", 
									"Dear " + managerName + ",<br><br>"
									+ "Please provide asset consent of " + empName + "<br>"
									+ "<br><br>"
									+ "Employee Info :<br>"
									+ "EmpId: " + empId +"<br>"
									+ "Name : " + empName + "<br>"
									+ "Department : " + department + "<br>"
									+ "Manager : " + managerName +"<br>"
									+ "Date Of resignation : " + dateOfResign + "<br>"
									+ "Date of relieving : " + dateOfRelieving + "<br>"
									+ "<br>"
									+ "Link : "+resignationConsentLink +empId);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						//send mail to HR,IT,Admin,Accounts department head
						List<Object[]> emailList = employeeRepository.getEmailForMailConsent();
						
						emailList.forEach((mailObj) -> {
							String mailAddress = object[4] != null ? object[4].toString() : null;
							String name = object[3] != null ? object[3].toString() : null;
							try {
								mailService.sendMail(mailAddress, "Asset Consent", 
										"Dear " + name + ",<br><br>"
										+ "Please provide asset consent of " + empName + "<br>"
										+ "<br><br>"
										+ "Employee Info :<br>"
										+ "EmpId: " + empId +"<br>"
										+ "Name : " + empName + "<br>"
										+ "Department : " + department + "<br>"
										+ "Manager : " + managerName +"<br>"
										+ "Date Of resignation : " + dateOfResign + "<br>"
										+ "Date of relieving : " + dateOfRelieving + "<br>"
										+ "<br>"
										+ "Link : "+resignationConsentLink +empId);
							} catch (Exception e) {
								e.printStackTrace();
							}
						});
					});
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		// 0 0 10 ? * MON - At 10:00:00am, on every Monday, every month
		// 0 0/2 * ? * *
//		@Async
//		@Scheduled(cron="${timesheetDefaulter.time}")
//		public void timesheetDefaulterWeeklyMail() {
//			try {
//				List<Department> allDepartment = departmentRepository.findAll();			
//				if(!allDepartment.isEmpty()) {
//					allDepartment.forEach((object) -> {
//						StringBuilder defaulterMail = new StringBuilder();
//						
//						if(!object.getName().equals("Super Admin") && !object.getName().equals("Director") && !object.getName().equals("unKnown Department")) {
//							int currentYear = LocalDate.now().getYear();
//							int currentMonth = LocalDate.now().getMonthValue();
//							
//							LocalDate firstOfMonth = LocalDate.of(currentYear, currentMonth, 1);
//							LocalDate end = LocalDate.now().minusDays(1);
//							
//							Long period = ChronoUnit.DAYS.between(firstOfMonth, end) + 1;
//
//							List<Object[]> timesheetList = timesheetsRepository.getLast9DaysPendingTimesheetReport(firstOfMonth, end);
//							List<Object[]> employeeList = employeeRepository.getEmployeeByDepartmentId(object.getDeptId());
//
//							List<TimesheetDTO> dtoList = new ArrayList<>();
//							String hodMail = null;
//							
//							if (!timesheetList.isEmpty()){
//								for(Object[] employee: employeeList) {
//									TimesheetDTO dto = new TimesheetDTO();
//
//									dto.setEmployeementId(employee[0] != null ? Long.parseLong(employee[0].toString()) : null);
//									dto.setEmployeeName(employee[1] != null ? employee[1].toString() : null);
//									dto.setDepartmentName(employee[2] != null ? employee[2].toString() : null);
//									dto.setEmail(employee[3] != null ? employee[3].toString() : null);
//									dto.setManagerName(employee[4] != null ? employee[4].toString() : null);
//									dto.setEmpId(employee[5] != null ? Long.parseLong(employee[5].toString()) : null);
//									dto.setPendingEodCount(period);
//									dto.setEmploymentstatus(employee[6] != null ? employee[6].toString() : null);
//									hodMail = employee[7] != null ? employee[7].toString() : null;
//
//									timesheetList.forEach((timesheet) -> {
//
//										Long timesheetEmpId = timesheet[0] != null ? Long.parseLong(timesheet[0].toString()) : null;
//										Long employeeEmpId = employee[5] != null ? Long.parseLong(employee[5].toString()) : null;
//
//										if (timesheetEmpId.equals(employeeEmpId)) {
//											Long filledEodCount = timesheet[1] != null ? Long.parseLong(timesheet[1].toString()) : 0L;
//											Long pendingEodCount = period - filledEodCount;
//
//											dto.setPendingEodCount(pendingEodCount);
//										}
//									});
//									
//									if(dto.getPendingEodCount() >= 3) {
//										defaulterMail.append(employee[3] != null ? employee[3].toString() : null);
//										defaulterMail.append(",");
//									}
//									dtoList.add(dto);
//								}
//							}
//							//Filter 0 pending EOD counts
//							
//							dtoList = dtoList.stream().filter(timesheet -> timesheet.getPendingEodCount() >= 3).collect(Collectors.toList());
//									
//							//Mail timesheet defaulter list to: user cc: HR, HOD	
//							if(!dtoList.isEmpty()) {
//								
//								StringBuilder html = new StringBuilder();
//								html.append("<html>\n" +
//							            "  <head>\n" +
//							            "    <style>\n" +
//							            "      table, th, td {\n" +
//							            "        border: 1px solid black;\n" +
//							            "      }\n" +
//							            "      table {\n" +
//							            "        border-collapse: collapse;\n" +
//							            "      }\n" +
//							            "    </style>\n" +
//							            "  </head>\n" +
//							            "  <body>\n" +
//							            "    <table>\n" +
//							            "      <tr>\n" +
//							            "        <th>Emp ID</th>\n" +
//							            "        <th>Name</th>\n" +
//							            "        <th>Email</th>\n" +
//							            "        <th>Manager Name</th>\n" +
//							            "        <th>Expected Timesheet Count</th>\n" +
//							            "        <th>Filled Timesheet Count</th>\n" +
//							            "        <th>Deaprtment</th>\n" +
//							            "      </tr>\n");
//								// add rows to the table
//								for(TimesheetDTO timesheet: dtoList) {
//									Long filledEOD = period - timesheet.getPendingEodCount();
//									html.append("      <tr>\n");
//									  // add cells to the row
//									  html.append("        <td>" + "A-"+timesheet.getEmployeementId() + "</td>\n");
//									  html.append("        <td>" + timesheet.getEmployeeName() + "</td>\n");
//									  html.append("        <td>" + timesheet.getEmail() + "</td>\n");
//									  html.append("        <td>" + timesheet.getManagerName() + "</td>\n");
//									  html.append("        <td>" + period + "</td>\n");
//									  html.append("        <td>" + filledEOD + "</td>\n");
//									  html.append("        <td>" + timesheet.getDepartmentName() + "</td>\n");
//									  html.append("      </tr>\n");
//								}
//								
//								html.append("    </table>\n" +
//								            "  </body>\n" +
//								            "</html>");
//								
//								try {
////									mailService.sendMailWithCC(defaulterMail.toString(),
////											hodMail+","+hrMailAddress,
////											"EOD Timesheet Defaulters List for "+firstOfMonth+" to "+end,
////											"Dear IShine Members, <br><br>"
////		                                  + "This is to bring it to your attention that you are in the defaulters list."
////		                                  + " You have missed filling Timesheets consecutively for 3 continuous days.<br><br>"
////		                                  + "Your team's planning, productivity and your salary calculation depend on timely filling of the Timesheets.<br><br>"
////										  + "To enable seriousness of filling timesheets in timely manner system is going to enforce locking 3 days of timesheet"
////										  + " from 23rd January onwards if it remains unfilled for consecutive 3 days. <br><br>"
////										  + "Thus, ensure you fill timesheets on a daily basis to avoid lock of the timesheets and impacting salary.<br><br>"
////										  +	html.toString());
//									
//									mailService.sendMailWithCC(hodMail,
//											hrMailAddress,
//											"EOD Timesheet Defaulters List for "+firstOfMonth+" to "+end,
//											"Dear IShine Members, <br><br>"
//													+ "We regret to inform you that you have been added to our defaulters list due to your failure to submit your timesheets for three consecutive days. We would like to remind you that timely submission of timesheets is crucial for effective team planning, productivity, and accurate salary calculation.<br><br>"
//													+ "To encourage timely submission of timesheets, our system has implemented a three-day lock on timesheets that remain unfilled for three consecutive days starting from 20th February. We urge you to fill out your timesheets on a daily basis to avoid the lock and any adverse impact on your salary.<br><br>"
//													+ "In case you have a legitimate reason for not being able to submit your timesheets on time, please do not hesitate to reach out to your reporting manager or HR.<br><br>"
//													+ "Thank you for your attention to this matter.<br><br><br><br>"
//													+ "Sincerely,<br>"
//													+ "ApMoSys Technologies"
//										  +	html.toString());
//								} catch (MessagingException e) {
//									System.out.println(object.getName() + " dept name \n\n\n");
//									e.printStackTrace();
//								}
//								
//								String[] emailId = defaulterMail.toString().split(",");
//								for (String email : emailId) {
//									try {
//										mailService.sendMail(email,
//												"EOD Timesheet Defaulters List for "+firstOfMonth+" to "+end,
//												"Dear IShine Members, <br><br>"
//													+ "We regret to inform you that you have been added to our defaulters list due to your failure to submit your timesheets for three consecutive days. We would like to remind you that timely submission of timesheets is crucial for effective team planning, productivity, and accurate salary calculation.<br><br>"
//													+ "To encourage timely submission of timesheets, our system has implemented a three-day lock on timesheets that remain unfilled for three consecutive days starting from 20th February. We urge you to fill out your timesheets on a daily basis to avoid the lock and any adverse impact on your salary.<br><br>"
//													+ "In case you have a legitimate reason for not being able to submit your timesheets on time, please do not hesitate to reach out to your reporting manager or HR.<br><br>"
//													+ "Thank you for your attention to this matter.<br><br><br><br>"
//													+ "Sincerely,<br>"
//													+ "ApMoSys Technologies"
//											  +	html.toString());
//									} catch (Exception e) {
//										e.printStackTrace();
//									}
//								}
//							}
//						}
//					});
//				}
//			}catch(Exception e) {
//				e.printStackTrace();
//			}
//		}
		
		
//		<-------Divya Code ----->
		@Async
		@Scheduled(cron="${timesheetDefaulter.time}")
		public void timesheetDefaulterWeeklyMail() {
			
			System.out.println("***********JOB STARTED*******************");
		    try {
		        List<Department> allDepartment = departmentRepository.findAll();

		        if (!allDepartment.isEmpty()) {
		            for (Department department : allDepartment) {
		               
		                if (department.getName().equals("Super Admin") ||
		                    department.getName().equals("Director") ||
		                    department.getName().equals("unKnown Department")) {
		                    continue;
		                }    

		                System.out.println("\n==========================================");
		                System.out.println("Processing Department: " + department.getName() + " (ID: " + department.getDeptId() + ")");
		                System.out.println("==========================================");

		                Set<String> defaulterEmails = new HashSet<>();
		                List<TimesheetDTO> dtoList = new ArrayList<>();

		                int currentYear = LocalDate.now().getYear();
		                int currentMonth = LocalDate.now().getMonthValue();
		                LocalDate firstOfMonth = LocalDate.of(currentYear, currentMonth, 1);
		                LocalDate end = LocalDate.now().minusDays(1);
		                Long period = ChronoUnit.DAYS.between(firstOfMonth, end) + 1;
//		                Long dept= (long) 26;

		                List<Object[]> timesheetList = timesheetsRepository.getLast9DaysPendingTimesheetReport(firstOfMonth, end);
		                List<Object[]> employeeList = employeeRepository.getEmployeeByDepartmentId(department.getDeptId());
		                System.out.println("Employee List (Total: " + employeeList.size() + "):");
		                String hodMail = null;

		                
						for (Object[] emp : employeeList) {
		                    System.out.println("  -> A-" + emp[0] + " | Email: " + emp[3]);

		                    TimesheetDTO dto = new TimesheetDTO();
		                    dto.setEmployeementId(emp[0] != null ? Long.parseLong(emp[0].toString()) : null);
		                    dto.setEmployeeName(emp[1] != null ? emp[1].toString() : null);
		                    dto.setDepartmentName(emp[2] != null ? emp[2].toString() : null);
		                    dto.setEmail(emp[3] != null ? emp[3].toString() : null);
		                    dto.setManagerName(emp[4] != null ? emp[4].toString() : null);
		                    dto.setEmpId(emp[5] != null ? Long.parseLong(emp[5].toString()) : null);
//		                    dto.setPendingEodCount(period);
		                    dto.setExpectedEODCount(period);
		                    dto.setEmploymentstatus(emp[6] != null ? emp[6].toString() : null);
		                    hodMail = emp[7] != null ? emp[7].toString() : null;
		                    
		                    
		                    long filled = 0;
		                    for (Object[] ts : timesheetList) {
		                        Long tsEmpId = ts[0] != null ? Long.parseLong(ts[0].toString()) : null;
		                        Long empId = emp[5] != null ? Long.parseLong(emp[5].toString()) : null;
		                        
		                        System.out.println("Total Expected value ::::" +period);

		                        if (tsEmpId != null && tsEmpId.equals(empId)) {
		                            filled = ts[1] != null ? Long.parseLong(ts[1].toString()) : 0L;
		                            dto.setPendingEodCount(period - filled);
		                            break;
		                        }
		                    }
		                    dto.setFilledTimesheetCount(filled);

		                    if (dto.getPendingEodCount() >= 3 && dto.getEmail() != null) {
		                        defaulterEmails.add(dto.getEmail().toLowerCase().trim());
		                    }

		                    dtoList.add(dto);
		                }

						
		                dtoList = dtoList.stream()
		                        .filter(d -> d.getPendingEodCount() >= 3)
		                        .collect(Collectors.toList());
		                System.out.println("Defaulters (Pending EOD ≥ 3):");
		                for (TimesheetDTO dto : dtoList) {
		                    System.out.println("  -> A-" + dto.getEmployeementId() + " | " + dto.getEmail() + " | Pending: " + dto.getPendingEodCount() + " | Filled Count : " + dto.getFilledTimesheetCount());
		                }

		                if (!dtoList.isEmpty()) {
		                    // Build HTML table
		                    StringBuilder html = new StringBuilder();
		                    html.append("<html><head><style>")
		                            .append("table, th, td { border: 1px solid black; }")
		                            .append("table { border-collapse: collapse; }")
		                            .append("</style></head><body><table>")
		                            .append("<tr><th>Emp ID</th><th>Name</th><th>Email</th><th>Manager Name</th>")
		                            .append("<th>Expected Timesheet Count</th><th>Filled Timesheet Count</th><th>Department</th></tr>");

		                    for (TimesheetDTO dto : dtoList) {
		                        html.append("<tr>")
		                                .append("<td>A-").append(dto.getEmployeementId()).append("</td>")
		                                .append("<td>").append(dto.getEmployeeName()).append("</td>")
		                                .append("<td>").append(dto.getEmail()).append("</td>")
		                                .append("<td>").append(dto.getManagerName()).append("</td>")
		                                .append("<td>").append(dto.getExpectedEODCount()).append("</td>")
		                                .append("<td>").append(dto.getFilledTimesheetCount()).append("</td>")
		                                .append("<td>").append(dto.getDepartmentName()).append("</td>")
		                                .append("</tr>");
		                    }

		                    html.append("</table></body></html>");

		                    // Send mail to HOD + HR
		                    try {
		                        mailService.sendMailWithCC(
		                                hodMail,
		                                hrMailAddress,
		                                "EOD Timesheet Defaulters List for " + firstOfMonth + " to " + end,
		                                "Dear IShine Members,<br><br>"
		                                        + "You have been added to the defaulters list for missing timesheet submissions for three or more days.<br><br>"
		                                        + "This affects salary processing and project planning.<br><br>"
		                                        + "Please submit your pending timesheets as soon as possible.<br><br>"
		                                        + "Regards,<br>ApMoSys Technologies"
		                                        + html.toString()
		                        );
		                        System.out.println(" HOD+HR mail sent for: " + department.getName());
		                    } catch (MessagingException e) {
		                        System.out.println(" Failed sending HOD+HR mail for: " + department.getName());
		                        e.printStackTrace();
		                    }
//		     
		                    // Send mails to each individual employee
		                    System.out.println("Individual defaulter emails (Total: " + defaulterEmails.size() + "):");
		                    for (String email : defaulterEmails) {
		                        System.out.println("  -> " + email);
		                        try {
		                            mailService.sendMail(
		                                    email,
		                                    "EOD Timesheet Defaulters List for " + firstOfMonth + " to " + end,
		                                    "Dear IShine Member,<br><br>"
		                                            + "You have failed to submit timesheets for three or more days.<br><br>"
		                                            + "Please fill your timesheets to avoid system lock and salary delay.<br><br>"
		                                            + html.toString()
		                                            + "Regards,<br>ApMoSys Technologies"
		                            );
		                        } catch (Exception e) {
		                            System.out.println("Failed sending mail to: " + email);
		                            e.printStackTrace();
		                        }
		                    }		                    
		                } else {
		                    System.out.println("No defaulters in " + department.getName());
		                }
		            }
		        }
		    } catch (Exception e) {
		        System.out.println(" Exception occurred in timesheetDefaulterWeeklyMail()");
		        e.printStackTrace();
		    }
		}

		
//		*/20 * * * * *  for every 20 secs
//      @Scheduled(cron = "0 0 8 ? * *")  // At 08:00 AM		
		@Async
		@Scheduled(cron = "0 0 8 ? * *")  // At 08:00 AM	
		public void timesheetCheckEnable() {

			try {
				LocalDate dateToday = LocalDate.now();
				List<Object[]> employeeList = employeeRepository.getEmployeeDetailForCron();
				List<EmployeeDTO> listDTO = new ArrayList<EmployeeDTO>();
				
				if(!employeeList.isEmpty()) {
					for(Object[] object: employeeList) {
						EmployeeDTO empdto = new EmployeeDTO();
						empdto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						empdto.setName(object[2] != null ? object[2].toString() : null);
						empdto.setEmployeementId(object[3] != null ? Long.parseLong(object[3].toString()) : null);
						empdto.setIsTimesheetLockCheckEnable(object[4] != null ? object[4].toString() : null);
						empdto.setTimesheetLockUpdatedOn(object[5] != null ? object[5].toString() : null);
						
						listDTO.add(empdto);
					}
				}
				
				if(!listDTO.isEmpty()) {
					listDTO.forEach((employeeDTO) -> {
						
						if(employeeDTO.getTimesheetLockUpdatedOn() != null) {
							DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
							
							LocalDate lastUpdatedDate = LocalDate.parse(employeeDTO.getTimesheetLockUpdatedOn(), format);			
							long elapsedDays = ChronoUnit.DAYS.between(lastUpdatedDate, dateToday);
							
							System.out.println("today : "+ dateToday + " lastUpdatedDate : "+ lastUpdatedDate);
							System.out.println("elapsedDays for "+ employeeDTO.getEmpId() + " : "+  elapsedDays);
							
							if(elapsedDays >= timesheetReconcileDays && employeeDTO.getIsTimesheetLockCheckEnable().equals("false")) {
								Optional<Employee> emp = employeeRepository.findById(employeeDTO.getEmpId());
								if(emp.isPresent()) {
									Employee employeeObj = emp.get();								
									employeeObj.setIsTimesheetLockCheckEnable("true");
									employeeObj.setTimesheetLockUpdatedOn(LocalDate.now());
									
									employeeRepository.save(employeeObj);
								}
							}
						}
					});
				}
				
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		@Async
		@Scheduled(cron = "0 0 10 ? * MON")
		public void weeklyAllEmployeeDsrReport() {
			try {
				TimesheetDTO timesheetDto = new TimesheetDTO();
				timesheetDto.setIsCron("true");
				
				ServiceResponse response = allEmployeeDsrReport(timesheetDto);
				
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		// Below method: will generate Excel with muliple row for projects & client
//		public ServiceResponse allEmployeeDsrReport(TimesheetDTO timesheetdto) {
//			ServiceResponse response = new ServiceResponse();
//			try {
//				
//				// Create Excel
//				LocalDate firstOfMonth = null;
//				LocalDate currentDate = null;
//				String subject = null;
//				int currentYear = 0;
//				
//				if(timesheetdto.getIsCron().equals("true")) {
//					currentYear = LocalDate.now().getYear();
//					int currentMonth = LocalDate.now().getMonthValue();
//					
//					firstOfMonth = LocalDate.of(currentYear, currentMonth, 1);
//					currentDate = LocalDate.now().minusDays(1);
//					subject = "All Employee's DSR report from "+firstOfMonth+" to "+currentDate;
//					
//				}else if(timesheetdto.getIsCron().equals("false")) {
//					int month = Month.valueOf(timesheetdto.getMonth().toUpperCase()).getValue();
//					currentYear = LocalDate.now().getYear();
//					
//					firstOfMonth = LocalDate.of(timesheetdto.getYear(), month, 1);
//					currentDate = YearMonth.of(timesheetdto.getYear(), month).atEndOfMonth();
//					subject = "All Employee's DSR report of month : "+timesheetdto.getMonth() + " " + currentYear;
//				}
//					
//					String fileName = "EmployeeDSR"+"-"+firstOfMonth.getMonth()+".xlsx";
//					var file = new File(fileName);
//						
//					try (var fos = new FileOutputStream(file)) {
//
//						var wb = new Workbook(fos, "Application", "1.0");
//						Worksheet ws = wb.newWorksheet(firstOfMonth.getMonth() + " DSR");
//
//						ws.value(0, 0, "EmpId");
//						ws.value(0, 1, "Emp Name");
//						ws.value(0, 2, "Date");
//						ws.value(0, 3, "Day Type");
//						ws.value(0, 4, "In-Time");
//						ws.value(0, 5, "Out-Time");
//						ws.value(0, 6, "Total Working Hours");
//						ws.value(0, 7, "Client");
//						ws.value(0, 8, "Client Location");
//						ws.value(0, 9, "Project");
//						ws.value(0, 10, "Activity");
//						ws.value(0, 11, "Total Activity Time");
//						ws.value(0, 12, "Shift");
//						ws.value(0, 13, "Description");
//						ws.value(0, 14, "Status");
//
//						int rowNum = 1;
//
//						List<Object[]> employeeList = employeeRepository.getEmployeeDetailForCron();
//						for (Object[] empObj : employeeList) {
//
//							Long empId = empObj[0] != null ? Long.parseLong(empObj[0].toString()) : null;
//							String empName = empObj[2] != null ? empObj[2].toString() : null;
//							Long employeementId = empObj[3] != null ? Long.parseLong(empObj[3].toString()) : null;
//
//							System.out.println("Emp ID :" + empId);
//							System.out.println("Employment ID :" + employeementId);
//
//							List<Timesheet> monthlyTimesheet = timesheetsRepository
//									.findAllByEmpIdAndDateBetweenOrderByDateDesc(empId, firstOfMonth, currentDate);
//
//							if (!monthlyTimesheet.isEmpty()) {
//								for (Timesheet timesheetObj : monthlyTimesheet) {
//									List<Object[]> objectList = timesheetActivityMapRepository
//											.activitiesByTimesheetId(timesheetObj.getTimesheetId());
//
//									if (!objectList.isEmpty()) {
//										for (Object[] object : objectList) {
//
//											String activity = object[1] != null ? object[1].toString() : null;
//											String project = object[5] != null ? object[5].toString() : null;
//											String clientName = object[6] != null ? object[6].toString() : null;
//											String clientLocation = object[7] != null ? object[7].toString() : null;
//
//											ws.style(rowNum, 2).format("dd-MM-yyyy").set();
//											ws.style(rowNum, 4).format("dd-MM-yyyy HH:mm:ss").set();
//											ws.style(rowNum, 5).format("dd-MM-yyyy HH:mm:ss").set();
//											
//											ws.value(rowNum, 0, "A-" + employeementId);
//											ws.value(rowNum, 1, empName);
//											ws.value(rowNum, 2, timesheetObj.getDate());
//											ws.value(rowNum, 3, timesheetObj.getDayType());
//											ws.value(rowNum, 4, timesheetObj.getOfficeInTime());
//											ws.value(rowNum, 5, timesheetObj.getOfficeOutTime());
//											ws.value(rowNum, 6, timesheetObj.getTotalWorkingHours());
//											ws.value(rowNum, 7, clientName);
//											ws.value(rowNum, 8, clientLocation);
//											ws.value(rowNum, 9, project);
//											if (!objectList.isEmpty()) {
//												ws.value(rowNum, 10, activity);
//											} else {
//												ws.value(rowNum, 10, timesheetObj.getDescription());
//											}
//											ws.value(rowNum, 11, timesheetObj.getTotalTime());
//											if(timesheetObj.getIsNightShift() == null) {
//												ws.value(rowNum, 12, "Regular Shift");
//											}else {
//												ws.value(rowNum, 12, timesheetObj.getIsNightShift().equals("true") ? "Night Shift" : "Regular Shift");
//											}
//											ws.value(rowNum, 14, timesheetObj.getStatus());
//
//											rowNum++;
//										}
//									} else {
//
//										// Fill data of weekoff & leave
//										ws.style(rowNum, 2).format("dd-MM-yyyy").set();
//										ws.style(rowNum, 4).format("dd-MM-yyyy HH:mm:ss").set();
//										ws.style(rowNum, 5).format("dd-MM-yyyy HH:mm:ss").set();
//
//										ws.value(rowNum, 0, "A-" + employeementId);
//										ws.value(rowNum, 1, empName);
//										ws.value(rowNum, 2, timesheetObj.getDate());
//										ws.value(rowNum, 3, timesheetObj.getDayType());
//										ws.value(rowNum, 6, timesheetObj.getTotalWorkingHours());
//										ws.value(rowNum, 13, timesheetObj.getDescription());
//										ws.value(rowNum, 14, timesheetObj.getStatus());
//
//										rowNum++;
//
//										System.out.println("Activity List is empty");
//									}
//								}
//							}
//						}
//						wb.finish();
//					}catch(Exception e) {
//						e.printStackTrace();
//					}
//					
//					// Send mail
//					
//					 boolean mailSent = mailService.sendMailWithAttachment(financeMail,
//							 hrMailAddress,
//							 subject,
//							 "Dear Team, <br><br>"
//	                       + "Please find " + subject + " attached below.",
//	                       file);
//					
//					 if(mailSent) {
//						 response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//						 response.setServiceResponse("All Employee's DSR report sent on mail to finance & HR department successfully.");
//					 }else {
//						 response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//						 response.setServiceResponse("Unable to sent Mail.");
//					 }
//					
//			}catch(Exception e) {
//				e.printStackTrace();
//				response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//				response.setServiceResponse("Something Went Wrong.");
//				response.setServiceError(e.getMessage());
//			}
//			return response;
//		}
		
		public ServiceResponse allEmployeeDsrReport(TimesheetDTO timesheetdto)
		{
			ServiceResponse response = new ServiceResponse();
		
//		List<BioMaTO> finalEmpBioData=bioMaxService.getBioInOut(timesheetdto);
		
//		System.out.println("print---"+finalEmpBioData);
		
try {
			
			// Create Excel
			LocalDate firstOfMonth = null;
			LocalDate currentDate = null;
			String subject = null;
			int currentYear = 0;
			
			if(timesheetdto.getIsCron().equals("true")) {
				currentYear = LocalDate.now().getYear();
				int currentMonth = LocalDate.now().getMonthValue();
				
				firstOfMonth = LocalDate.of(currentYear, currentMonth, 1);
				currentDate = LocalDate.now().minusDays(1);
				subject = "All Employee's DSR report from "+firstOfMonth+" to "+currentDate;
				
			}else if(timesheetdto.getIsCron().equals("false")) {
				int month = Month.valueOf(timesheetdto.getMonth().toUpperCase()).getValue();
				currentYear = LocalDate.now().getYear();
				
				firstOfMonth = LocalDate.of(timesheetdto.getYear(), month, 1);
				currentDate = YearMonth.of(timesheetdto.getYear(), month).atEndOfMonth();
				subject = "All Employee's DSR report of month : "+timesheetdto.getMonth() + " " + currentYear;
			}
				
			
			String fileName = "EmployeeDSR"+"-"+firstOfMonth.getMonth()+".xlsx";
			var file = new File(fileName);
			
			if(allEmployeeDSRFileLocation != null) {
				Path path = Files.createDirectories(Paths.get(allEmployeeDSRFileLocation + "AllEmployeeDSR"));
				file = new File(path + File.separator + "EmployeeDSR"+"-"+firstOfMonth.getMonth()+".xlsx");				
			}
			
					
				try (var fos = new FileOutputStream(file)) {

					var wb = new Workbook(fos, "Application", "1.0");
					Worksheet ws = wb.newWorksheet(firstOfMonth.getMonth() + " DSR");

					ws.value(0, 0, "EmpId");
					ws.value(0, 1, "Emp Name");
					ws.value(0, 2, "Department Name");
					ws.value(0, 3, "Date");
					ws.value(0, 4, "Day Type");
					ws.value(0, 5, "Leave Type");
					ws.value(0, 6, "In-Time");
					ws.value(0, 7, "Out-Time");
					ws.value(0, 8, "Shift");
					ws.value(0, 9, "Total Working Hours");
					ws.value(0, 10, "Activity"); //Comma seperated
					ws.value(0, 11, "Description");
					ws.value(0, 12, "Client"); // comma seperated
					ws.value(0, 13, "Project"); // comma seperated
					ws.value(0, 14, "Status");
//					ws.value(0, 14, "BiomaxInTime");
//					ws.value(0, 15, "BiomaxOutTime");
//					
					
					int rowNum = 1;

					List<Object[]> employeeList = employeeRepository.getEmployeeDetailForDSRCron(firstOfMonth, currentDate);
					for (Object[] empObj : employeeList) {

						Long empId = empObj[0] != null ? Long.parseLong(empObj[0].toString()) : null;
						String empName = empObj[2] != null ? empObj[2].toString() : null;
						Long employeementId = empObj[3] != null ? Long.parseLong(empObj[3].toString()) : null;
						String departmentName = empObj[6] != null ? empObj[6].toString() : null;

						System.out.println("Emp ID :" + empId);
						System.out.println("Employment ID :" + employeementId);

						List<Timesheet> monthlyTimesheet = timesheetsRepository
								.findAllByEmpIdAndDateBetweenOrderByDateDesc(empId, firstOfMonth, currentDate);

						if (!monthlyTimesheet.isEmpty()) {
							for (Timesheet timesheetObj : monthlyTimesheet) {
								List<Object[]> objectList = timesheetActivityMapRepository
										.activitiesByTimesheetId(timesheetObj.getTimesheetId());

								StringBuilder activity = new StringBuilder();
								StringBuilder description = new StringBuilder();
								Set<String> project = new HashSet<>();
								Set<String> clientName = new HashSet<>();
								
								if (!objectList.isEmpty()) {
									for (Object[] object : objectList) {
										activity.append(object[1] != null ? object[1].toString() : null).append(",");
										description.append(object[3] != null ? object[3].toString() : null).append(",");							
										project.add(object[5] != null ? object[5].toString() : null);
										
										clientName.add(object[6] != null ? object[6].toString() : null);
									}
									
									ws.style(rowNum, 3).format("dd-MM-yyyy").set();
									ws.style(rowNum, 6).format("dd-MM-yyyy HH:mm:ss").set();
									ws.style(rowNum, 7).format("dd-MM-yyyy HH:mm:ss").set();
									
									ws.value(rowNum, 0, "A-" + employeementId);
									ws.value(rowNum, 1, empName);
									ws.value(rowNum, 2, departmentName);
									ws.value(rowNum, 3, timesheetObj.getDate());
									ws.value(rowNum, 4, timesheetObj.getDayType());
									ws.value(rowNum, 6, timesheetObj.getOfficeInTime());
									ws.value(rowNum, 7, timesheetObj.getOfficeOutTime());
									if(timesheetObj.getIsNightShift() == null) {
										ws.value(rowNum, 8, "Regular Shift");
									}else {
										ws.value(rowNum, 8, timesheetObj.getIsNightShift().equals("true") ? "Night Shift" : "Regular Shift");
									}
									ws.value(rowNum, 9, timesheetObj.getTotalWorkingHours());
									if (!objectList.isEmpty()) {
										ws.value(rowNum, 10, activity.toString());
									} else {
										ws.value(rowNum, 10, timesheetObj.getDescription());
									}
									if(!objectList.isEmpty()) {
										ws.value(rowNum,11, description.toString());
										}else {
											ws.value(rowNum, 11, (String)null); 
										}
									ws.value(rowNum, 12, String.join(",", clientName));
									ws.value(rowNum, 13, String.join(",", project));
									ws.value(rowNum, 14, timesheetObj.getStatus());

									rowNum++;
									
									
									 // Check if employeementId exists in finalEmpBioData
//						            for (BioMaTO bio : finalEmpBioData) {
//						                if (bio.getEmployeeCode().equalsIgnoreCase(String.valueOf(employeementId))) {
//				
//						                	ws.value(rowNum, 14, bio.getInTime());
//						                	ws.value(rowNum, 15, bio.getOutTime());
//						                	
//						                    break; // Exit the loop if found
//						                }
//						            }
									
									
									
									
								} else {
									
									//Get leave type
									List<Object[]> empLeave = employeeLeaveRepository
											.findLeaveTypeFromEmpIdAndDate(empId, timesheetObj.getDate().toString());
									
									String leaveType = null;
									String dayType = timesheetObj.getDayType();
									if(!empLeave.isEmpty()) {
										for(Object[] object: empLeave) {
											leaveType = object[0] != null ? object[0].toString() : null;
											dayType = "Leave";
										}
									}

									// Fill data of weekoff & leave

									ws.style(rowNum, 3).format("dd-MM-yyyy").set();
									ws.style(rowNum, 6).format("dd-MM-yyyy HH:mm:ss").set();
									ws.style(rowNum, 7).format("dd-MM-yyyy HH:mm:ss").set();

									ws.value(rowNum, 0, "A-" + employeementId);
									ws.value(rowNum, 1, empName);
									ws.value(rowNum, 2, departmentName);
									ws.value(rowNum, 3, timesheetObj.getDate());
									ws.value(rowNum, 4, dayType);
									ws.value(rowNum, 5, leaveType);
									ws.value(rowNum, 9, timesheetObj.getTotalWorkingHours());
								    ws.value(rowNum, 10, timesheetObj.getDescription());
								    ws.value(rowNum, 11, (String)null); 
									ws.value(rowNum, 14, timesheetObj.getStatus());
									
									 // Check if employeementId exists in finalEmpBioData
//						            for (BioMaTO bio : finalEmpBioData) {
//						                if (bio.getEmployeeCode().equalsIgnoreCase(String.valueOf(employeementId))) {
//				
//						                	ws.value(rowNum, 14, bio.getInTime());
//						                	ws.value(rowNum, 15, bio.getOutTime());
//						                	
//						                    break; // Exit the loop if found
//						                }
//						            }

									rowNum++;

									System.out.println("Activity List is empty");
								}
							}
						}
					}
					wb.finish();
				}catch(Exception e) {
					e.printStackTrace();
				}
				
				// Send mail
				
				 boolean mailSent = mailService.sendMailWithAttachment(financeMail,
						 hrMailAddress,
						 subject,
						 "Dear Team, <br><br>"
                       + "Please find " + subject + " attached below.",
                       file);
				
				 if(mailSent) {
					 response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					 response.setServiceResponse("All Employee's DSR report sent on mail to finance & HR department successfully.");
				 }else {
					 response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					 response.setServiceResponse("Unable to sent Mail.");
				 }
				
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
		
		}
		
		
	
		

		
		
		// To Remove any InActive / Blocked / Check Idle user within 1hr
		// "0 0 0/1 ? * *" - Run at every 1 Hr
		// "0 0/1 * ? * *" - Run at every 1 min
		// "0 0/30 * ? * *" - Run at evry 30 mins
		
		@Async
		@Scheduled(cron = "0 0/10 * ? * *")
		public void loggedInUserAudit() {
			
			System.out.println(new Date() + " Running LoggedIn User Audit ... ");
			
			try {
				List<UserSession> userSessionList = userSessionRepository.findAll();
				List<String> loggedOutUsers = new ArrayList<String>();
				
				if(!userSessionList.isEmpty()){
					for (UserSession session : userSessionList) {
					      String user = null;
					      
					      LocalDateTime loginTime = session.getLoginTime();
					      LocalDateTime lastCheckedTime = session.getLastCheckTime();
					      LocalDateTime today = LocalDateTime.now();
					      Long elapsedMinsAfterLastCheck = 0L;
					      if(lastCheckedTime != null) elapsedMinsAfterLastCheck = ChronoUnit.MINUTES.between(lastCheckedTime, today);
					      
//					      System.out.println("key: " + key + " value: " + value + " loginTime : "+ loginTime+ " currentDateTime : "+ today + " elapsedHours : "+ elapsedHours);
					      
					      if(session.getSessionKey() != null) {
					    	  List<Object[]> employeeData =  employeeRepository.getEmploymentStatusAndInvalidAccessAttemptByEmpId(session.getEmpId());
					    	  
					    	  EmployeeDTO employee = new EmployeeDTO();
					    	  
					    	  if (!employeeData.isEmpty()) {
					    		  employeeData.forEach((data) -> {
					    			  employee.setEmpId((data[0] != null) ? Long.parseLong(data[0].toString()) : null);
					    			  employee.setEmploymentstatus((data[1] != null) ? data[1].toString() : null);
					    			  employee.setInvalidAccessAttempt((data[2] != null) ? Integer.parseInt(data[2].toString()) : null);
									});
					    	  };
					    	  
					    	  
					    	  final long VALID_ATTEMPT_LIMIT = validAttempt;
					    	  final long SESSION_CHECK_INACTIVE_LIMIT = userSessionInactiveTimeout;
					    	  
					    	  if(employee.getEmpId() != null && (employee.getEmploymentstatus().equals("InActive") || employee.getInvalidAccessAttempt() > VALID_ATTEMPT_LIMIT)) {
					    		  user = session.getEmpId() + " - "+ "InActive/Blocked";
					    		  userSessionRepository.deleteById(session.getUserSessionId());
					    	  }else if(elapsedMinsAfterLastCheck >= SESSION_CHECK_INACTIVE_LIMIT){
					    		  user = session.getEmpId() + " - "+ " Time elapsed After Last Check : "+ elapsedMinsAfterLastCheck + " min.";
					    		  userSessionRepository.deleteById(session.getUserSessionId());
					    	  }
					    	  
					    	  if(user != null) {
					    		  loggedOutUsers.add(user);				    		  
					    	  } 
					      }
					}
				}
				
				System.out.println("LoggedOutusers : "+ loggedOutUsers.toString());
				
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		//0 0 0 1/3 * ? - At 00:00:00am, every 3 days starting on the 1st, every month
		@Scheduled(cron = "0 0 0 1/3 * ?")
		public void oldProjectAlertMail() {
			try {
				List<ResourceManagementDTO> dtoList = new ArrayList<ResourceManagementDTO>();
				
				OkHttpClient client = new OkHttpClient();
				Request request = new Request.Builder()
				  .url(allPoPortalProjects)
				  .get()
				  .addHeader("accept", "application/json")
				  .build();
				Response httpResponse = client.newCall(request).execute();
				String jsonData = httpResponse.body().string();
				JSONArray jsonArr = new JSONArray(jsonData);
				
				for (int i = 0; i < jsonArr.length(); i++) {
			        JSONObject jsonObj = jsonArr.getJSONObject(i);
			        Long poProjectId = jsonObj.getLong("id");
			        String projectName = jsonObj.getString("name");
			        String createdOn = jsonObj.getString("createdOn");
			        String clientName = jsonObj.getString("clientName");
			        JSONArray deptartmentName = jsonObj.getJSONArray("department");
			        
			        Project projectObj = projectRepository.findByPoProjectId(poProjectId);
			        
			        if(projectObj != null) {
			        	List<Team> isTeamCreated = teamRepository.findByProjectId(projectObj.getProjectId());
			        	
			        	if(isTeamCreated.isEmpty()) {
			        		//Client info
			        		Client clientObj = clientsRepository.findByClientId(projectObj.getClientId());
			        		List<ProjectDepartmentMap> projDeptMap = projectDepartmentMapRepository.findByProjectId(projectObj.getProjectId());
			        		List<String> deptList = new ArrayList<>();
			        		
			        		if(!projDeptMap.isEmpty()) {
			        			projDeptMap.forEach((dept) -> {
			        				Department deptObject = departmentRepository.findByDeptId(dept.getDeptId());
			        				if(deptObject != null) {
			        					deptList.add(deptObject.getName());			        							        					
			        				}
			        			});
			        		}
			        		
			        		ResourceManagementDTO rmgDTO = new ResourceManagementDTO();
			        		
			        		rmgDTO.setName(projectObj.getProjectName());
			        		rmgDTO.setCreatedOn(projectObj.getCreatedOn().toString());
			        		rmgDTO.setClientName(clientObj != null ? clientObj.getClientName() : null);
			        		rmgDTO.setDeptName(!deptList.isEmpty() ? String.join(",", deptList) : null);
			        		dtoList.add(rmgDTO);
			        	}
			        }else {
			        	StringJoiner stringJoiner = new StringJoiner(",");

			        	for (Object jsonValue : deptartmentName) {
			        	    stringJoiner.add(jsonValue.toString());
			        	}
			        	
			        	ResourceManagementDTO rmgDTO = new ResourceManagementDTO();
		        		
		        		rmgDTO.setName(projectName);
		        		rmgDTO.setCreatedOn(createdOn);
		        		rmgDTO.setClientName(clientName);
		        		rmgDTO.setDeptName(stringJoiner.toString());
		        		dtoList.add(rmgDTO);
			        }
				}
				
				
				List<Department> allDepartment = departmentRepository.findAll();
				
				if(!allDepartment.isEmpty()) {
					allDepartment.forEach((dept) -> {
						
						List<ResourceManagementDTO> filteredList = new ArrayList<>();
						
						for (ResourceManagementDTO dto : dtoList) {
						    if (dto.getDeptName().contains(dept.getName())) {
						        filteredList.add(dto);
						    }
						}
						
						
						if(!filteredList.isEmpty()) {
							
							//Create Proj Info table
			        		StringBuilder html = new StringBuilder();
							html.append("<html>\n" +
						            "  <head>\n" +
						            "    <style>\n" +
						            "      table, th, td {\n" +
						            "        border: 1px solid black;\n" +
						            "      }\n" +
						            "      table {\n" +
						            "        border-collapse: collapse;\n" +
						            "      }\n" +
						            "    </style>\n" +
						            "  </head>\n" +
						            "  <body>\n" +
						            "    <table>\n" +
						            "      <tr>\n" +
						            "        <th>Project Name</th>\n" +
						            "        <th>Client Name</th>\n" +
						            "        <th>Created On</th>\n" +
						            "        <th>Project Department</th>\n" +
						            "      </tr>\n");
							// add rows to the table
							for(ResourceManagementDTO rmgDTO: filteredList) {
								
								DateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
								Date inputDate = null;
								try {
									inputDate = inputFormatter.parse(rmgDTO.getCreatedOn());
								} catch (ParseException e) {
									e.printStackTrace();
								}

								DateFormat outputFormatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
								String outputDateStr = outputFormatter.format(inputDate);
								
								html.append("      <tr>\n");
								  // add cells to the row
								  html.append("        <td>" + rmgDTO.getName() + "</td>\n");
								  html.append("        <td>" + rmgDTO.getClientName() + "</td>\n");
								  html.append("        <td>" + outputDateStr + "</td>\n");
								  html.append("        <td>" + dept.getName() + "</td>\n");
								  html.append("      </tr>\n");
							}
							
							html.append("    </table>\n" +
							            "  </body>\n" +
							            "</html>");
							
							//Send Mail regarding oldProject where team not created
							
							Employee empObj = employeeRepository.findByEmpId(dept.getHodId());						
							try {
								mailService.sendMailWithCC(rmgMail,empObj != null ? empObj.getEmail() : rmgMail,
										"Reminder for Project - Resource OnBoarding",
										"Dear team, <br><br>" +
										"Below projects are onboarded in PoPortal/Ishine in which team and resources are not added. Please take necessary action.<br><br>"
									  +	html.toString());
							} catch (AddressException e) {
								e.printStackTrace();
							} catch (MessagingException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		//@Scheduled(cron = "0 0 0 1/3 * ?")
		public void ProjectClonefromPoPortal() {
			try {
				List<ResourceManagementDTO> dtoList = new ArrayList<ResourceManagementDTO>();
				
				OkHttpClient client = new OkHttpClient();
				Request request = new Request.Builder()
				  .url(allPoPortalProjects)
				  .get()
				  .addHeader("accept", "application/json")
				  .build();
				Response httpResponse = client.newCall(request).execute();
				String jsonData = httpResponse.body().string();
				JSONArray jsonArr = new JSONArray(jsonData);
				
				for (int i = 0; i < jsonArr.length(); i++) {
			        JSONObject jsonObj = jsonArr.getJSONObject(i);
			        Long poProjectId = jsonObj.getLong("id");
			        String projectName = jsonObj.getString("name");
			        String createdOn = jsonObj.getString("createdOn");
			        String clientName = jsonObj.getString("clientName");
			        JSONArray deptartmentName = jsonObj.getJSONArray("department");
			        
			        Project projectObj = projectRepository.findByPoProjectId(poProjectId);
			        
			        if(projectObj != null) {
			        	List<Team> isTeamCreated = teamRepository.findByProjectId(projectObj.getProjectId());
			        	
			        	if(isTeamCreated.isEmpty()) {
			        		//Client info
			        		Client clientObj = clientsRepository.findByClientId(projectObj.getClientId());
			        		List<ProjectDepartmentMap> projDeptMap = projectDepartmentMapRepository.findByProjectId(projectObj.getProjectId());
			        		List<String> deptList = new ArrayList<>();
			        		
			        		if(!projDeptMap.isEmpty()) {
			        			projDeptMap.forEach((dept) -> {
			        				Department deptObject = departmentRepository.findByDeptId(dept.getDeptId());
			        				if(deptObject != null) {
			        					deptList.add(deptObject.getName());			        							        					
			        				}
			        			});
			        		}
			        		
			        		ResourceManagementDTO rmgDTO = new ResourceManagementDTO();
			        		
			        		rmgDTO.setName(projectObj.getProjectName());
			        		rmgDTO.setCreatedOn(projectObj.getCreatedOn().toString());
			        		rmgDTO.setClientName(clientObj != null ? clientObj.getClientName() : null);
			        		rmgDTO.setDeptName(!deptList.isEmpty() ? String.join(",", deptList) : null);
			        		dtoList.add(rmgDTO);
			        	}
			        }else {
			        	StringJoiner stringJoiner = new StringJoiner(",");

			        	for (Object jsonValue : deptartmentName) {
			        	    stringJoiner.add(jsonValue.toString());
			        	}
			        	
			        	ResourceManagementDTO rmgDTO = new ResourceManagementDTO();
		        		
		        		rmgDTO.setName(projectName);
		        		rmgDTO.setCreatedOn(createdOn);
		        		rmgDTO.setClientName(clientName);
		        		rmgDTO.setDeptName(stringJoiner.toString());
		        		dtoList.add(rmgDTO);
			        }
				}
				
				
				List<Department> allDepartment = departmentRepository.findAll();
				
				if(!allDepartment.isEmpty()) {
					allDepartment.forEach((dept) -> {
						
						List<ResourceManagementDTO> filteredList = new ArrayList<>();
						
						for (ResourceManagementDTO dto : dtoList) {
						    if (dto.getDeptName().contains(dept.getName())) {
						        filteredList.add(dto);
						    }
						}
						
						
						if(!filteredList.isEmpty()) {
							
							//Create Proj Info table
			        		StringBuilder html = new StringBuilder();
							html.append("<html>\n" +
						            "  <head>\n" +
						            "    <style>\n" +
						            "      table, th, td {\n" +
						            "        border: 1px solid black;\n" +
						            "      }\n" +
						            "      table {\n" +
						            "        border-collapse: collapse;\n" +
						            "      }\n" +
						            "    </style>\n" +
						            "  </head>\n" +
						            "  <body>\n" +
						            "    <table>\n" +
						            "      <tr>\n" +
						            "        <th>Project Name</th>\n" +
						            "        <th>Client Name</th>\n" +
						            "        <th>Created On</th>\n" +
						            "        <th>Project Department</th>\n" +
						            "      </tr>\n");
							// add rows to the table
							for(ResourceManagementDTO rmgDTO: filteredList) {
								
								DateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
								Date inputDate = null;
								try {
									inputDate = inputFormatter.parse(rmgDTO.getCreatedOn());
								} catch (ParseException e) {
									e.printStackTrace();
								}

								DateFormat outputFormatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
								String outputDateStr = outputFormatter.format(inputDate);
								
								html.append("      <tr>\n");
								  // add cells to the row
								  html.append("        <td>" + rmgDTO.getName() + "</td>\n");
								  html.append("        <td>" + rmgDTO.getClientName() + "</td>\n");
								  html.append("        <td>" + outputDateStr + "</td>\n");
								  html.append("        <td>" + dept.getName() + "</td>\n");
								  html.append("      </tr>\n");
							}
							
							html.append("    </table>\n" +
							            "  </body>\n" +
							            "</html>");
							
							//Send Mail regarding oldProject where team not created
							
							Employee empObj = employeeRepository.findByEmpId(dept.getHodId());						
							try {
								mailService.sendMailWithCC(rmgMail,empObj != null ? empObj.getEmail() : rmgMail,
										"Reminder for Project - Resource OnBoarding",
										"Dear team, <br><br>" +
										"Below projects are onboarded in PoPortal/Ishine in which team and resources are not added. Please take necessary action.<br><br>"
									  +	html.toString());
							} catch (AddressException e) {
								e.printStackTrace();
							} catch (MessagingException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		// 0 0 9 ? * * - At 09:00:00am every day
		@Async
		@Scheduled(cron = "0 0 9 ? * *")
		public void pendingKycDefaulterMail() {
			
			List<Department> allDeptList = departmentRepository.findAll();
			
			if(!allDeptList.isEmpty()) {
				allDeptList.forEach((dept) -> {
					
					if(!dept.getName().equals("Super Admin") && !dept.getName().equals("Director") && !dept.getName().equals("unKnown Department")) {
						List<Object[]> managerList = employeeRepository.getManagerByDepartment(dept.getDeptId());
						List<EmployeeDTO> finalPendingList = new ArrayList<>();
						
						if(!managerList.isEmpty()){
							managerList.forEach((object) -> {
								Long managerId = object[0] != null ? Long.parseLong(object[0].toString()) : null;
								String managerName = object[1] != null ? object[1].toString() : null;
								String hodMail = object[2] != null ? object[2].toString() : null;
								String managerMail = object[3] != null ? object[3].toString() : null;
								
								List<Object[]> employeeList = employeeRepository.getEmployeeByManager(managerId);
								
								if(!employeeList.isEmpty()) {
									employeeList.forEach((employee) -> {
										
										EmployeeDTO dto = new EmployeeDTO();
										
										dto.setEmployeementId(employee[0] != null ? Long.parseLong(employee[0].toString()) : null);
										dto.setName(employee[1] != null ? employee[1].toString() : null);
										dto.setEmail(employee[2] != null ? employee[2].toString() : null);
										dto.setHodEmail(hodMail);
										dto.setManagerName(managerName);
										dto.setManagerEmail(managerMail);
										dto.setDepartmentName(employee[3] != null ? employee[3].toString() : null);
										
										finalPendingList.add(dto);
									});
								}
							});
						}
						
						if(!finalPendingList.isEmpty()) {
							StringBuilder defaulterMail = new StringBuilder();
							Set<String> managerMail = new HashSet<>();
							Set<String> hodMail = new HashSet<>();
							
							StringBuilder html = new StringBuilder();
							html.append("<html>\n" +
						            "  <head>\n" +
						            "    <style>\n" +
						            "      table, th, td {\n" +
						            "        border: 1px solid black;\n" +
						            "      }\n" +
						            "      table {\n" +
						            "        border-collapse: collapse;\n" +
						            "      }\n" +
						            "    </style>\n" +
						            "  </head>\n" +
						            "  <body>\n" +
						            "    <table>\n" +
						            "      <tr>\n" +
						            "        <th>Emp ID</th>\n" +
						            "        <th>Name</th>\n" +
						            "        <th>Email</th>\n" +
						            "        <th>Manager Name</th>\n" +
						            "        <th>Department</th>\n" +
						            "      </tr>\n");
							// add rows to the table
							for(EmployeeDTO employee: finalPendingList) {
									defaulterMail.append(employee.getEmail());
									defaulterMail.append(",");
									managerMail.add(employee.getManagerEmail());
									hodMail.add(employee.getHodEmail());
									
									html.append("      <tr>\n");
									  // add cells to the row
									  html.append("        <td>" + "A-"+employee.getEmployeementId()+ "</td>\n");
									  html.append("        <td>" + employee.getName() + "</td>\n");
									  html.append("        <td>" + employee.getEmail() + "</td>\n");
									  html.append("        <td>" + employee.getManagerName() + "</td>\n");
									  html.append("        <td>" + employee.getDepartmentName() + "</td>\n");
									  html.append("      </tr>\n");
							}
							
							html.append("    </table>\n" +
							            "  </body>\n" +
							            "</html>");
							
							try {
								mailService.sendMailWithCC(defaulterMail.toString(), String.join(",", hodMail)+","+String.join(",", managerMail),
										"Defaulter : Profile not yet updated in ishine",
										"Dear Ishine Member,"
										+ "<br><br>"
										+ "You are in Defaulters list !"
										+ "<br><br>"
										+ "You are receiving this email because either you or your reportee has not filled the ishine Profile completely."
										+ "<br><br>"
										+ "We are writing to bring to your attention the fact that there are some mandatory fields in your KYC that have yet to be filled out. It is important to note that if your KYC remains incomplete, failing which your March month salary will be put on hold.\n"
										+ "<br><br>"
										+ "In order to avoid any such complications, Please take immediate action and complete your KYC as soon as possible.\n"
										+ "<br><br>"
										+ "For any further assistance please reach out to HR department.For any technical challenge please mail with the screenshots to Prasad more (prasad.more@apmosys.com)/ Harshit Toxia (harshit.toxia@apmosys.com).\n"
										+ "<br><br>"
										+ "Sincerely,<br>"
										+ "ApMoSys Technologies"
										+ "<br> <br>"
									  +	html.toString());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});	
			}
		}
		
//		0 0 2 ? * * : At 02:00:00am every day
//      0 0/1 * ? * * - Run at every 1 min
//		@Async
//		@Scheduled(cron = "0 0 2 ? * *")
		
// Currently Disabled to sysnc stataus from PO Portal to ishine
		public void updateProjectStatus() {
			LogDTO apiLogInfo = new LogDTO();
			apiLogInfo.setSubFeatureName("Update Project Status CronJob");
			apiLogInfo.setApiUrl("updateProjectStatus");
			apiLogInfo.setLogLevel("INFO");
			StringBuilder logBuilder = new StringBuilder();
			
			try {
				
				OkHttpClient client = new OkHttpClient();
				Request request = new Request.Builder()
				  .url(allPoPortalProjects)
				  .get()
				  .addHeader("accept", "application/json")
				  .build();
				Response httpResponse = client.newCall(request).execute();
				String jsonData = httpResponse.body().string();
				JSONArray jsonArr = new JSONArray(jsonData);
				
				for (int i = 0; i < jsonArr.length(); i++) {
			        JSONObject jsonObj = jsonArr.getJSONObject(i);
			        Long poProjectId = jsonObj.getLong("id");
			        
			        if(jsonObj.getString("status").equals("Completed")){
			        	Project projectObj = projectRepository.findByPoProjectId(poProjectId);
			        	
			        	if(projectObj != null) {
			        		logBuilder.append("PoProject Id : " + poProjectId + "Project Name : " + projectObj.getProjectName() + "projectId : " + projectObj.getProjectId());
				        	
				        	projectObj.setActive("false");
				        	
				        	Project dbResponse = projectRepository.save(projectObj);
				        	
				        	if(dbResponse != null) {
				        		apiLogInfo.setApiResponse("Project Status Updated Successfully.");			
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				        	}else {
				        		apiLogInfo.setApiResponse("Unable to Update Project Status." + "projectId : " + projectObj.getProjectId());
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				        	}
			        	}else {
			        		apiLogInfo.setApiResponse("Unable to find Project." + "poProjectId : " + poProjectId);
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			        	}
			        }
				}
			}catch(Exception e) {
				e.printStackTrace();
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				apiLogInfo.setLogLevel("ERROR");
			}
			
			apiLogInfo.setApiRequest(logBuilder.toString());
			RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
			if (attributes != null) {
			    HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
			    logService.logMyInfo(request, apiLogInfo);
			}
		}
		
		/*
		 * Deduct Leave cron job
		 * 
		 * if below condition for 3 consecutive days
		 * 
		 * conditions :
		 * Confirmed
		 * 1) Reporting to work on or before shift starting time and not completing 9 hours
		 *    : Deduct ½ Day Leave
		 * 2) Reporting late to work (after 30 minutes from shift starting time) and completing 9 hours
		 *    : Deduct ½ Day Leave
		 * 3) Reporting late to work (after 30 minutes from shift starting time) and not completing 9 hours
		 *    : Deduct 1 full Day Leave
		 * 
		 * Probation
		 * 1) Reporting to work on or before shift starting time and not completing 9 hours
		 * 	  : Deduct ½ Day Salary
		 * 2) Reporting late to work (after 30 minutes from shift starting time) and completing 9 hours
		 *    : Deduct ½ Day Salary
		 * 3) Reporting late to work (after 30 minutes from shift starting time) and not completing 9 hours
		 *    : Deduct 1 Day Salary
		 * */
public List<Holiday> getHolidayListTestData(){
	List<Holiday> holi=new ArrayList<>();
	Holiday holid=new Holiday();
	holid.setDateOfHoliday(LocalDate.now());
	holid.setDayOfTheWeek("1");
	holi.add(holid);
	return holi;
}

public List<BiomaxRequest> getBiomaxRequestTest(){
	List<BiomaxRequest> biomax=new ArrayList<>();
	BiomaxRequest bio=new BiomaxRequest();
	bio.setBiomaxStatus("Approved");
	bio.setBiomaxrequestDate(LocalDateTime.now());
	bio.setEmpId(1993L);
	biomax.add(bio);
//	BiomaxRequest bio1=new BiomaxRequest();
//	bio1.setBiomaxStatus("Approved");
//	bio1.setBiomaxrequestDate(LocalDateTime.now().plusDays(-2));
//	bio1.setEmpId(1993L);
//	biomax.add(bio1);
//	BiomaxRequest bio12=new BiomaxRequest();
//	bio12.setBiomaxStatus("Approved");
//	bio12.setBiomaxrequestDate(LocalDateTime.now().plusDays(4));
//	bio12.setEmpId(1993L);
//	biomax.add(bio12);
	return biomax;
}
//		
//		@Async
//		@Scheduled(cron = "0 0 9 ? * *") // runs everyday at 9 pm
		@SuppressWarnings("unused")
//		public void leaveDeduct() {
//			 String depart="";
//				String employee="";
//				
////				List<BioMaTO> biomaxDataList = new ArrayList<>();
////				BioMaTO biomatObj = new BioMaTO();
////				biomatObj.setEmployeeCode("A2");
////				biomatObj.setAttendanceDate("2025-03-20 00:00:00.0");
////				biomatObj.setInTime("00:00");
////				biomatObj.setOutTime("00:00");
////				biomatObj.setTotalDuration("200");
////				biomatObj.setShiftName("General");
////				biomatObj.setBeginTime("00:00");
////				biomatObj.setEndTime("00:00");
////				biomatObj.setDeduct("0.5");
////				biomaxDataList.add(biomatObj);
////				
////				BioMaTO biomatObj1 = new BioMaTO();
////				biomatObj.setEmployeeCode("A2");
////				biomatObj1.setAttendanceDate("2025-01-19 00:00:00.0");
////				biomatObj1.setInTime("00:00");
////				biomatObj1.setOutTime("00:00");
////				biomatObj1.setTotalDuration("200");
////				biomatObj1.setShiftName("General");
////				biomatObj1.setBeginTime("00:00");
////				biomatObj1.setEndTime("00:00");
////				biomatObj1.setDeduct("0.5");
////				biomaxDataList.add(biomatObj1);
////				
////				BioMaTO biomatObj2 = new BioMaTO();
////				biomatObj.setEmployeeCode("A2");
////				biomatObj2.setAttendanceDate("2025-01-18 00:00:00.0");
////				biomatObj2.setInTime("00:00");
////				biomatObj2.setOutTime("00:00");
////				biomatObj2.setTotalDuration("200");
////				biomatObj2.setShiftName("General");
////				biomatObj2.setBeginTime("00:00");
////				biomatObj2.setEndTime("00:00");
////				biomatObj2.setDeduct("0.5");
////				biomaxDataList.add(biomatObj2);
//
//				
//				
//				//start the rahul code
//				
//			List<BioMaTO> biomaxDataList =bioMaxService.getBiomaxDataForLeaveDeduct();
//			System.out.println("Total Data Comming from Biomax"+biomaxDataList.size());
//			
//			List<Long> dataToBeDeleted = new ArrayList<>();
//		
//			List<Long> removeemployeeId=new ArrayList<>();
//				//leave for findEmployeeIsOnCompOffLeaveToday
////			if(!biomaxDataList.isEmpty()) {
//				
//				 
////				 List<BiomaxRequest> approvedLeaveList = biomaxRequestRepository.findByEmployeementIdLeaveNotDeduct();
//					
////				 if (!approvedLeaveList.isEmpty()) {
////					 approvedLeaveList.forEach((empId)->{
////						 Employee emp=employeeRepository.findByEmpId(empId.getEmpId());
////						// empId.setEmpId(emp.getEmployeementId());
////						  removeemployeeId.add(emp.getEmployeementId());
////							
////					 });
////					    		
////				 }
//				
//					
////				 }
//			// Fetch comp-off leave employees for today and filter biomaxDataList
//			List<CompOffLeave> compOffLeaveList = compOffLeaveRepository.findEmployeeIsOnCompOffLeaveToday();
//			if (!compOffLeaveList.isEmpty()) {
//				compOffLeaveList.forEach((comof)->{
//					 Employee empcomof=employeeRepository.findByEmpId(comof.getEmpId());
//					  removeemployeeId.add(empcomof.getEmployeementId());
//						
//				});
//			}
//
//			// Fetch department configuration and filter biomaxDataList for non-leave-deducted departments
//			Optional<PortalConfig> departmentConfig = portalConfigRepository.findByportalConfigById(Short.parseShort(biomaxleavedeductForDepartment));
//			if (departmentConfig.isPresent()) {
//			    PortalConfig portalConfig = departmentConfig.get();
//			    String departmentConfigValue = portalConfig.getConfigValue().replace("[", "").replace("]", "");  // Remove square brackets
//			    String[] departmentIds = departmentConfigValue.split(",");  // Split the string into an array
//
//			    Set<Long> departmentNotLeaveDeduct = Arrays.stream(departmentIds)
//			            .map(Long::parseLong)
//			            .collect(Collectors.toSet());  // Store department IDs in a set for faster lookup
//			    biomaxDataList.removeIf(bio -> departmentNotLeaveDeduct.contains(bio.getDepartmentId()));
//			}
//
//			// Fetch employees on leave today and filter biomaxDataList for them
//			List<EmployeeLeave> employeeLeaveList = employeeLeaveRepository.findEmployeeIsOnLeaveToday();
//			if (!employeeLeaveList.isEmpty()) {
//				employeeLeaveList.forEach((empleave)->{
//					 Employee empcomof=employeeRepository.findByEmpId(empleave.getEmpId());
//					 removeemployeeId.add(empcomof.getEmployeementId())	;// Use a set for faster lookup
//					   	
//				});
//			  	}
//
//			// Fetch employees with leave deduction exemptions and filter biomaxDataList for them
//			Optional<PortalConfig> employeeLeaveDeductConfig = portalConfigRepository.findByportalConfigById(Short.parseShort(biomaxleavedeductForEmployee));
//			if (employeeLeaveDeductConfig.isPresent()) {
//			    PortalConfig portalConfig = employeeLeaveDeductConfig.get();
//			    String employeeConfigValue = portalConfig.getConfigValue().replace("[", "").replace("]", "");  // Remove square brackets
//			    String[] employeeIds = employeeConfigValue.split(",");  // Split the string into an array
//
//			    Set<Long> employeeNotLeaveDeduct = Arrays.stream(employeeIds)
//			            .map(Long::parseLong)
//			            .collect(Collectors.toSet());  // Use a set for faster lookup
//			    
//			   // removeemployeeId.addAll(employeeNotLeaveDeduct);
//			    if(employeeNotLeaveDeduct.size()>0) {
//			    biomaxDataList.removeIf(bio -> employeeNotLeaveDeduct.contains(bio.getEmpId()));
//			    }
//				
//			}
//			 if(removeemployeeId.size()>0) {
//			biomaxDataList.removeIf(bio -> removeemployeeId.contains(bio.getEmployementId()));
//			 }
//				List<BioMaTO> finalEmpBioData = new ArrayList<>();
//						
//					
//				
//				//end of the code
////				
//				String fileName = "LeaveDeduct.xlsx";
//				var file = new File(fileName);
//
//				try (var fos = new FileOutputStream(file)) {
//
//					var wb = new Workbook(fos, "Application", "1.0");
//					Worksheet ws = wb.newWorksheet("Employee in Probation");
//
//					ws.value(0, 0, "EmpId");
//					ws.value(0, 1, "Emp Name");
//					ws.value(0, 2, "Date");
//					ws.value(0, 3, "In-Time");
//					ws.value(0, 4, "Out-Time");
//					ws.value(0, 5, "Total Working Hours");
//					ws.value(0, 6, "Salary to be deducted (In days)");
//
//					int rowNum = 1;
//					//biomaxDataList=new ArrayList();
//					if (!biomaxDataList.isEmpty()) {
//						
//						List<BioMaTO> biomaxDataFilterList = biomaxDataList.stream()
//								.filter(obj -> Double.parseDouble(obj.getDeduct()) > 0).collect(Collectors.toList());
//
//						if (!biomaxDataFilterList.isEmpty()) {
//							
//							biomaxDataFilterList.forEach((object) -> {
//
//								Long employmentId = Long.parseLong(object.getEmployeeCode().replaceAll("\\D", ""));
//								List<BiomaxDefaulter> defaulterList = biomaxDefaulterRepository
//										.findByEmployeementIdForDefaulterBiomax(employmentId);
//								//For employee configuration in biomax
//								
//							
//								
//								Employee employeeObj = employeeRepository.findByEmployeementId(employmentId);
//								JobRole departmentjon=jobRoleRepository.findByjobRoleId(employeeObj.getJobRoleId());
//								
//								if (!defaulterList.isEmpty()) {
//									
//									
//										/*
//										 * Deduct leave for confirmed employee Deduct salary for probation employee
//										 * (send mail to HR in excel format) Remove defaulter from BiomaxDefaulter after
//										 * Deduction
//										 */
//
//										if (employeeObj != null) {
//											// Deduct leave
//
//											
//											if (employeeObj.getEmploymentstatus().equals("Confirmed")) {
//												EmployeeLeavesMap employeeLeaveMapObject = employeeLeavesMapRepository
//														.findByEmpIdAndLeaveTypeMasterId(employeeObj.getEmpId(), (short) 3);
//
//												if (employeeLeaveMapObject != null) {
//													Float newBalance = employeeLeaveMapObject.getBalance()
//															- Float.parseFloat(object.getDeduct());
//
//													employeeLeaveMapObject.setBalance(newBalance);
//													EmployeeLeavesMap dbResponse = employeeLeavesMapRepository
//															.save(employeeLeaveMapObject);
//
//													if (dbResponse != null) {
//														LeaveBalanceLog log = new LeaveBalanceLog();
//
//														log.setBalance(newBalance);
//														log.setEmpId(employeeObj.getEmpId());
//														log.setLeaveTypeMasterId((short) 3);
//														log.setMessage(LeaveLogMessage.autoDeductLeaveOnTimesheetDefaulter
//																.replace("0.0", object.getDeduct()));
//														log.setUpdateBalanceBy("-" + object.getDeduct());
//
//														LeaveBalanceLog leaveLogDbResponse = leaveBalanceLogRepository
//																.save(log);
//													}
//												}
//											} else if (employeeObj.getEmploymentstatus().equals("Probation")) {
//												// Deduct salary --> create excel and send to HR
//
//												ws.style(rowNum, 2).format("dd-MM-yyyy").set();
//												ws.style(rowNum, 3).format("dd-MM-yyyy HH:mm:ss").set();
//												ws.style(rowNum, 4).format("dd-MM-yyyy HH:mm:ss").set();
//
//
//												ws.value(rowNum, 0, "A-" + employmentId);
//												ws.value(rowNum, 1, employeeObj.getName());
//												ws.value(rowNum, 2, LocalDate.now());
//												ws.value(rowNum, 3, object.getInTime());
//												ws.value(rowNum, 4, object.getOutTime());
//												ws.value(rowNum, 5, object.getTotalDuration());
//												ws.value(rowNum, 6, object.getDeduct());
//
//											}
//										// delete employee from defaulter table
//										dataToBeDeleted.add(employmentId);
//									} else {
//													BiomaxDefaulter newDefaulterObj = new BiomaxDefaulter();
//										newDefaulterObj.setEmpId(employeeObj.getEmpId());
//										newDefaulterObj.setEmployeementId(employmentId);
//										newDefaulterObj.setDepartmentId(departmentjon.getDeptId());
//										BiomaxDefaulter defaulterNewEntry = biomaxDefaulterRepository.save(newDefaulterObj);
//										
//										}
//								} else {
//									// New defaulter entry
//									
//											BiomaxDefaulter newDefaulterObj = new BiomaxDefaulter();
//											newDefaulterObj.setEmpId(employeeObj.getEmpId());
//											newDefaulterObj.setEmployeementId(employmentId);
//											newDefaulterObj.setDepartmentId(departmentjon.getDeptId());
//											BiomaxDefaulter newDefaulter = biomaxDefaulterRepository.save(newDefaulterObj);
//										
//										
//								}
//							});
//						}
//					}
//					wb.finish();
//					
//					// Send mail
//
//					 boolean mailSent = mailService.sendMailWithAttachment(hrMailAddress,
//							 hrMailAddress,
//							 "Salary to be deducted of Employees in probation due to defaulter in working time",
//							 "Dear Team, <br><br>"
//		                   + "Please find Salary to be deducted of Employees in probation due to defaulter in working time attached below.",
//		                   file);
//					
//					 if(mailSent) {
//						System.out.println("Salary to be deducted of Employees Mail sent successfully !!");
//					 }else {
//						 System.out.println("Unable to sent salary to be deducted of Employees Mail !!");
//					 }
//					 
//					 
//					 //Delete all the employee in defaulter by employeemnetId
//				      biomaxDefaulterRepository.deleteAllByEmployeementIds(dataToBeDeleted);
//					 
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			
//			
//			   
//		}

		@Transactional
		public void leaveDeduct() {
			String depart = "";
			String employee = "";

//			List<BioMaTO> biomaxDataList = new ArrayList<>();
//			BioMaTO biomatObj = new BioMaTO();
//			biomatObj.setEmployeeCode("3");
//			biomatObj.setAttendanceDate("2025-03-07 00:00:00.0");
//			biomatObj.setInTime("00:00");
//			biomatObj.setOutTime("00:00");
//			biomatObj.setTotalDuration("200");
//			biomatObj.setShiftName("General");
//			biomatObj.setBeginTime("00:00");
//			biomatObj.setEndTime("00:00");
//			biomatObj.setDeduct("1");
//			biomaxDataList.add(biomatObj);
//
//			BioMaTO biomatObj1 = new BioMaTO();
//			biomatObj1.setEmployeeCode("3");
//			biomatObj1.setAttendanceDate("2025-03-12 00:00:00.0");
//			biomatObj1.setInTime("00:00");
//			biomatObj1.setOutTime("00:00");
//			biomatObj1.setTotalDuration("200");
//			biomatObj1.setShiftName("General");
//			biomatObj1.setBeginTime("00:00");
//			biomatObj1.setEndTime("00:00");
//			biomatObj1.setDeduct("1");
//			biomaxDataList.add(biomatObj1);
//
//			BioMaTO biomatObj2 = new BioMaTO();
//			biomatObj2.setEmployeeCode("3");
//			biomatObj2.setAttendanceDate("2025-03-15 00:00:00.0");
//			biomatObj2.setInTime("00:00");
//			biomatObj2.setOutTime("00:00");
//			biomatObj2.setTotalDuration("200");
//			biomatObj2.setShiftName("General");
//			biomatObj2.setBeginTime("00:00");
//			biomatObj2.setEndTime("00:00");
//			biomatObj2.setDeduct("0.5");
//			biomaxDataList.add(biomatObj2);
//			
//			BioMaTO biomatObj3 = new BioMaTO();
//			biomatObj3.setEmployeeCode("3");
//			biomatObj3.setAttendanceDate("2025-03-26 00:00:00.0");
//			biomatObj3.setInTime("00:00");
//			biomatObj3.setOutTime("00:00");
//			biomatObj3.setTotalDuration("200");
//			biomatObj3.setShiftName("General");
//			biomatObj3.setBeginTime("00:00");
//			biomatObj3.setEndTime("00:00");
//			biomatObj3.setDeduct("0.5");
//			biomaxDataList.add(biomatObj3);
//			
//			BioMaTO biomatObj4 = new BioMaTO();
//			biomatObj4.setEmployeeCode("3");
//			biomatObj4.setAttendanceDate("2025-03-29 00:00:00.0");
//			biomatObj4.setInTime("00:00");
//			biomatObj4.setOutTime("00:00");
//			biomatObj4.setTotalDuration("200");
//			biomatObj4.setShiftName("General");
//			biomatObj4.setBeginTime("00:00");
//			biomatObj4.setEndTime("00:00");
//			biomatObj4.setDeduct("1");
//			biomaxDataList.add(biomatObj4);
//			
//			BioMaTO biomatObj5 = new BioMaTO();
//			biomatObj5.setEmployeeCode("3");
//			biomatObj5.setAttendanceDate("2025-03-31 00:00:00.0");
//			biomatObj5.setInTime("00:00");
//			biomatObj5.setOutTime("00:00");
//			biomatObj5.setTotalDuration("200");
//			biomatObj5.setShiftName("General");
//			biomatObj5.setBeginTime("00:00");
//			biomatObj5.setEndTime("00:00");
//			biomatObj5.setDeduct("0.5");
//			biomaxDataList.add(biomatObj5);

			// start the rahul code
		    List<BioMaTO> biomaxDataList = bioMaxService.getBiomaxDataForLeaveDeduct();
			System.out
					.println("Total Data Coming from Biomax: " + (biomaxDataList != null ? biomaxDataList.size() : 0));

			List<Long> dataToBeDeleted = new ArrayList<>();
			List<Long> removeemployeeId = new ArrayList<>();

			// Fetch comp-off leave employees for today and filter biomaxDataList
//			List<CompOffLeave> compOffLeaveList = compOffLeaveRepository.findEmployeeIsOnCompOffLeaveToday();
//			if (compOffLeaveList != null && !compOffLeaveList.isEmpty()) {
//				compOffLeaveList.forEach((comoff) -> {
//					Employee empcomof = employeeRepository.findByEmpId(comoff.getEmpId());
//					if (empcomof != null) {
//						removeemployeeId.add(empcomof.getEmployeementId());
//					}
//				});
//			}
			
//			BioMaTO biomatObj5 = new BioMaTO();
//			biomatObj5.setEmployeeCode("3");
//			biomatObj5.setAttendanceDate("2025-03-25 00:00:00.0");
//			biomatObj5.setInTime("00:00");
//			biomatObj5.setOutTime("00:00");
//			biomatObj5.setTotalDuration("200");
//			biomatObj5.setShiftName("General");
//			biomatObj5.setBeginTime("00:00");
//			biomatObj5.setEndTime("00:00");
//			biomatObj5.setDeduct("0.5");
//			biomaxDataList.add(biomatObj5);
//
//			// start the rahul code
////		    List<BioMaTO> biomaxDataList = bioMaxService.getBiomaxDataForLeaveDeduct();
//			System.out
//					.println("Total Data Coming from Biomax: " + (biomaxDataList != null ? biomaxDataList.size() : 0));
//
//			List<Long> dataToBeDeleted = new ArrayList<>();
//			List<Long> removeemployeeId = new ArrayList<>();

			// Fetch comp-off leave employees for today and filter biomaxDataList
//			List<CompOffLeave> compOffLeaveList = compOffLeaveRepository.findEmployeeIsOnCompOffLeaveToday();
//			if (compOffLeaveList != null && !compOffLeaveList.isEmpty()) {
//				compOffLeaveList.forEach((comoff) -> {
//					Employee empcomof = employeeRepository.findByEmpId(comoff.getEmpId());
//					if (empcomof != null) {
//						removeemployeeId.add(empcomof.getEmployeementId());
//					}
//				});
//			}
			
			// Fetch department configuration and filter biomaxDataList for
			// non-leave-deducted departments
			Optional<PortalConfig> departmentConfig = portalConfigRepository
					.findByportalConfigById(Short.parseShort(biomaxleavedeductForDepartment));
			if (departmentConfig.isPresent()) {
				PortalConfig portalConfig = departmentConfig.get();
				if(portalConfig.getConfigValue() != null) {
				String departmentConfigValue = portalConfig.getConfigValue().replace("[", "").replace("]", ""); // Remove
				
				if (!departmentConfigValue.isEmpty()) {
					String[] departmentIds = departmentConfigValue.split(","); // Split the string into an array
					Set<Long> departmentNotLeaveDeduct = Arrays.stream(departmentIds).map(Long::parseLong)
							.collect(Collectors.toSet()); // Store department IDs in a set for faster lookup
					biomaxDataList.removeIf(bio -> departmentNotLeaveDeduct.contains(bio.getDepartmentId()));
				}
				
				
				}																							
																												
			}

			// Fetch employees on leave today and filter biomaxDataList for them
			List<EmployeeLeave> employeeLeaveList = employeeLeaveRepository.findEmployeeIsOnLeaveToday();
			if (employeeLeaveList != null && !employeeLeaveList.isEmpty()) {
				employeeLeaveList.forEach((empleave) -> {
					Employee empcomof = employeeRepository.findByEmpId(empleave.getEmpId());
					if (empcomof != null) {
						removeemployeeId.add(empcomof.getEmployeementId());
					}
				});
			}

//			 Fetch employees with leave deduction exemptions and filter biomaxDataList for
//			 them
			Optional<PortalConfig> employeeLeaveDeductConfig = portalConfigRepository
					.findByportalConfigById(Short.parseShort(biomaxleavedeductForEmployee));
			if (employeeLeaveDeductConfig.isPresent()) {
				PortalConfig portalConfig = employeeLeaveDeductConfig.get();
				String employeeConfigValue = portalConfig.getConfigValue().replace("[", "").replace("]", ""); // Remove
																												// square
																												// brackets
				if (!employeeConfigValue.isEmpty()) {
					String[] employeeIds = employeeConfigValue.split(","); // Split the string into an array
					Set<Long> employeeNotLeaveDeduct = Arrays.stream(employeeIds).map(Long::parseLong)
							.collect(Collectors.toSet()); // Use a set for faster lookup
					biomaxDataList.removeIf(bio -> employeeNotLeaveDeduct.contains(bio.getEmpId()));
				}
			}

			if (removeemployeeId != null && !removeemployeeId.isEmpty()) {
				biomaxDataList.removeIf(bio -> removeemployeeId.contains(bio.getEmployementId()));
			}

			List<BioMaTO> finalEmpBioData = new ArrayList<>();
			// end of the code

			String fileName = "LeaveDeduct.xlsx";
			var file = new File(fileName);

			try (var fos = new FileOutputStream(file)) {
				// Try to create the Workbook and handle any exceptions
				try {
					var wb = new Workbook(fos, "Application", "1.0");
					Worksheet ws = wb.newWorksheet("Employee in Probation");

					ws.value(0, 0, "EmpId");
					ws.value(0, 1, "Emp Name");
					ws.value(0, 2, "Date");
					ws.value(0, 3, "In-Time");
					ws.value(0, 4, "Out-Time");
					ws.value(0, 5, "Total Working Hours");
					ws.value(0, 6, "Salary to be deducted (In days)");

//		            int rowNum = 1;
					if (biomaxDataList != null && !biomaxDataList.isEmpty()) {

//						 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//					        // Filter out biomax data where AttendanceDate is a holiday
//					        List<BioMaTO> filteredList = biomaxDataList.stream()
//					                .filter(biomaxData -> {
//					                	 String attendanceDateStr = biomaxData.getAttendanceDate();
//					                	 String dateOnlyStr = attendanceDateStr.split(" ")[0];
//					                     LocalDate attendanceDate = LocalDate.parse(dateOnlyStr, formatter);
//
//					                     // Check if the formatted date is a holiday
//					                     List<Holiday> holidays = holidayRepository.findByDateOfHoliday(attendanceDate);
//					                     return holidays.isEmpty(); // Keep the data if no holiday is found
//					                })
//					                .collect(Collectors.toList());
						
						List<BioMaTO> biomaxDataFilterList = biomaxDataList.stream()
								.filter(obj -> (!(obj.getDeduct().isEmpty()) && obj.getDeduct() != null
										&& Double.parseDouble(obj.getDeduct()) > 0))
								.collect(Collectors.toList());
						List<Long> employmentIdList = biomaxDataFilterList.stream()
							    .filter(obj -> !(obj.getDeduct().isEmpty()) && obj.getDeduct() != null
					            && Double.parseDouble(obj.getDeduct()) > 0)
					    .map(obj -> Long.parseLong(obj.getEmployeeCode())) 
					    .collect(Collectors.toList());
						dataToBeDeleted.addAll(employmentIdList);

						if (biomaxDataFilterList != null && !biomaxDataFilterList.isEmpty()) {
							biomaxDataFilterList.forEach((object) -> {
								int rowNum = 1;
								Long employmentId = Long.parseLong(object.getEmployeeCode().replaceAll("\\D", ""));
								List<BiomaxDefaulter> defaulterList = biomaxDefaulterRepository
										.findByEmployeementIdForDefaulterBiomax(employmentId);
								Employee employeeObj = employeeRepository.findByEmployeementId(employmentId);
								JobRole departmentjon = jobRoleRepository.findByjobRoleId(employeeObj.getJobRoleId());

								if (employeeObj != null) {
									if (!defaulterList.isEmpty() && defaulterList.size() >= 2) {
										BiomaxDefaulter newDefaulterObj = new BiomaxDefaulter();
										newDefaulterObj.setEmpId(employeeObj.getEmpId());
										newDefaulterObj.setEmployeementId(employmentId);
										newDefaulterObj.setDepartmentId(departmentjon.getDeptId());
										newDefaulterObj.setLeaveToDeduct(object.getDeduct());
										newDefaulterObj.setDefaultedDate(object.getAttendanceDate());
										newDefaulterObj.setIsDeducted(false);
										newDefaulterObj.setIsApprovedByManager(false);
										biomaxDefaulterRepository.save(newDefaulterObj);
										
										List<BiomaxDefaulter> defaulterListLatest = biomaxDefaulterRepository
												.findByEmployeementIdForDefaulterBiomax(employmentId);

										String mostFrequentLeaveToDeduct = defaulterListLatest.stream()
												.collect(Collectors.groupingBy(BiomaxDefaulter::getLeaveToDeduct,
														Collectors.counting()))
												.entrySet().stream().max(Comparator.comparingLong(Map.Entry::getValue))
												.map(Map.Entry::getKey).orElse("No data found");

										if (employeeObj.getEmploymentstatus().equals("Confirmed")
												&& !mostFrequentLeaveToDeduct.equalsIgnoreCase("No data found")) {
											// Deduct leave logic
											EmployeeLeavesMap employeeLeaveMapObject = employeeLeavesMapRepository
													.findByEmpIdAndLeaveTypeMasterId(employeeObj.getEmpId(), (short) leaveTypeId);

											if (employeeLeaveMapObject != null) {
												Float newBalance = employeeLeaveMapObject.getBalance()
														- Float.parseFloat(mostFrequentLeaveToDeduct);

												employeeLeaveMapObject.setBalance(newBalance);
												EmployeeLeavesMap dbResponse = employeeLeavesMapRepository
														.save(employeeLeaveMapObject);

												if (dbResponse != null) {
													LeaveBalanceLog log = new LeaveBalanceLog();
													log.setBalance(newBalance);
													log.setEmpId(employeeObj.getEmpId());
													log.setLeaveTypeMasterId((short) leaveTypeId);
													log.setMessage(LeaveLogMessage.autoDeductLeaveOnTimesheetDefaulter
															.replace("0.0", object.getDeduct()));
													log.setUpdateBalanceBy("-" + mostFrequentLeaveToDeduct);

													LeaveBalanceLog leaveLogDbResponse = leaveBalanceLogRepository
															.save(log);
												}
											}
										} else if (employeeObj.getEmploymentstatus().equals("Probation")
												&& !mostFrequentLeaveToDeduct.equalsIgnoreCase("No data found")) {
											// Deduct salary logic --> create excel and send to HR
											ws.style(rowNum, 2).format("dd-MM-yyyy").set();
											ws.style(rowNum, 3).format("dd-MM-yyyy HH:mm:ss").set();
											ws.style(rowNum, 4).format("dd-MM-yyyy HH:mm:ss").set();

											ws.value(rowNum, 0, "A-" + employmentId);
											ws.value(rowNum, 1, employeeObj.getName());
											ws.value(rowNum, 2, LocalDate.now());
											ws.value(rowNum, 3, object.getInTime());
											ws.value(rowNum, 4, object.getOutTime());
											ws.value(rowNum, 5, object.getTotalDuration());
											ws.value(rowNum, 6, mostFrequentLeaveToDeduct);
											rowNum++;
										}
//		                                dataToBeDeleted.add(employmentId);
										Long maxGrpId = biomaxDefaulterRepository.findMaxDroupIdFromEmpId(employeeObj.getEmpId());
										defaulterListLatest.forEach(data -> {
											data.setIsDeducted(true);
											data.setGroupId(maxGrpId);
											biomaxDefaulterRepository.save(data);
										});

									} else {
										// New defaulter entry
										BiomaxDefaulter newDefaulterObj = new BiomaxDefaulter();
										newDefaulterObj.setEmpId(employeeObj.getEmpId());
										newDefaulterObj.setEmployeementId(employmentId);
										newDefaulterObj.setDepartmentId(departmentjon.getDeptId());
										newDefaulterObj.setLeaveToDeduct(object.getDeduct());
										newDefaulterObj.setDefaultedDate(object.getAttendanceDate());
										newDefaulterObj.setIsDeducted(false);
										newDefaulterObj.setIsApprovedByManager(false);
										biomaxDefaulterRepository.save(newDefaulterObj);
									}
								}
							});
						}
					}
					wb.finish();

				} catch (Exception e) {
					System.err.println("Error while creating or finishing the workbook: " + e.getMessage());
					e.printStackTrace();
					return;
				}

				// Send mail
				boolean mailSent = mailService.sendMailWithAttachment(hrMailAddress, hrMailAddress,
						"Salary to be deducted of Employees in probation due to defaulter in working time",
						"Dear Team, <br><br>Please find the salary to be deducted of Employees in probation due to defaulter in working time attached below.",
						file);

				if (mailSent) {
					System.out.println("Salary to be deducted of Employees Mail sent successfully!!");
				} else {
					System.out.println("Unable to send salary to be deducted of Employees Mail!!");
				}

				// Delete all the employees in defaulter by employmentId
				if (!dataToBeDeleted.isEmpty()) {
					biomaxDefaulterRepository.deleteAllByEmployeementIds(dataToBeDeleted);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		
		@Async
//		@Scheduled(cron = "0 0/5 * ? * *")
		//@Scheduled(cron = "0 9 12 * * ?")
		@Transactional
		public void getProjectCloneFromPoPortal() {
			
			LogDTO apiLogInfo = new LogDTO();
	        apiLogInfo.setSubFeatureName("getProjectCloneFromPoPortal");
	        apiLogInfo.setApiUrl("/api/getProjectCloneFromPoPortal");
	        apiLogInfo.setLogLevel("INFO");
	        StringBuilder logBuilder = new StringBuilder();
	        logBuilder.append("Cron to update existing po-project details in Ishine started! ");

		    List<ProjectPoPortalDTO> list = new ArrayList<>();
		    
		    try {
		        ProjectPoPortalDTO[] projects = restTemplate.getForObject(allPoPortalProjects, ProjectPoPortalDTO[].class);
		        
		        list = Arrays.asList(projects != null ? projects : new ProjectPoPortalDTO[0]);
		        logBuilder.append("Total Projects Fetched = " + list.size());

		    } catch (RestClientException e) {
		        logBuilder.append("Error fetching projects from PoPortal API: " + e.getMessage());
		    }

		    if (!list.isEmpty()) {
		        for (ProjectPoPortalDTO dto : list) {
		            try {
		            	 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		                Project project = projectRepository.findByPoProjectId(dto.getId());

		                if (project != null) {

//		                    project.setProjectName(dto.getName());
		                    project.setPoProjectType(dto.getProjectType());
		                    Date startDate = dto.getStartDate();
		                    String formattedStartDate = dateFormat.format(startDate);
		                    project.setPoStartDate(formattedStartDate);
		                    Date endDate = dto.getEndDate();
		                    String formattedEndDate = dateFormat.format(endDate);
		                    project.setPoEndDate(formattedEndDate);
		                    project.setStatus(dto.getStatus());
		                    //departmentIds
		                    List<String> departmentList = dto.getDepartment();
		                    List<String> deptIds = new ArrayList<String>();
		                    if(!departmentList.isEmpty()) {
		                    	departmentList.forEach(dept->{
		                    		Department deptList =  departmentRepository.findByName(dept);
		                    		if(deptList == null) {
		                		        logBuilder.append("No Dept Id fetched");
		                    		}
		                    		else {
		                    			deptIds.add(deptList.getDeptId().toString());
		                    		}
		                    	});
		                    	 if(!deptIds.isEmpty()) {
				                    	String deptIdStr = String.join(", ", deptIds);
				                        project.setDeptId(deptIdStr); 
				                    }
		                    }
		                   
		                    //clientId
		                    Integer clientId = null;
		    			    Optional<Client> clientObj = clientsRepository.findByClientName(dto.getClientName());
		    			    if (!clientObj.isEmpty()) {
		    			        Client clientPresent = clientObj.get();
		    			        clientId = clientPresent.getClientId();
		    			    } else {
		    			        // Add Client & Client Location
		    			        Client newClient = new Client();
		    			        newClient.setClientName(dto.getClientName());
		    			        Client clientDbResponse = clientsRepository.save(newClient);
		    			        if (clientDbResponse != null) {
		    			            clientId = clientDbResponse.getClientId();
		    			            List<ClientLocation> locations = new ArrayList<>();

		    			            for (String clientLocation : dto.getClientLocation()) {
		    			                ClientLocation newClientLocation = new ClientLocation();
		    			                newClientLocation.setClientId(clientDbResponse.getClientId());
		    			                newClientLocation.setClientLocation(clientLocation);
		    			                locations.add(newClientLocation);
		    			            }
		    			            // Add WFH location
		    			            boolean contains = dto.getClientLocation().stream().anyMatch("WFH"::equals);
		    			            if (!contains) {
		    			                ClientLocation newClientLocation = new ClientLocation();
		    			                newClientLocation.setClientId(clientDbResponse.getClientId());
		    			                newClientLocation.setClientLocation("WFH");
		    			                locations.add(newClientLocation);
		    			            }
		    			            List<ClientLocation> clientLocationDbResponse = clientLocationRepository.saveAll(locations);
		    			            if (!clientLocationDbResponse.isEmpty()) {
		    					        logBuilder.append("Client stored successfully in database");
		    			            } else {
		    					        logBuilder.append("Error occured while storing updated project details from PoPortal API.");
		    			            }
		    			        } else {
		    			            
		    			        }
		    			    }
		    			    project.setClientId(clientId); 
		    		        
		                    project.setPoNo(dto.getPoNo());
		                    project.setApmosysRM(dto.getApmosysRM());	
		                    project.setIsRenewable(dto.getIsRenewable());
		                    project.setClientRM(dto.getClientRM());
		                    project.setApmosysRmEmail(dto.getApmosysRmEmail());                    
		                    projectRepository.save(project);

		    		        logBuilder.append("Updated Project: ID=" + dto.getId() + ", PoNo=" + dto.getPoNo());
		    		        
		                } else {
		    		        logBuilder.append("Project table does not contain PoProjectId: " + dto.getId());
		                }

		            } catch (Exception ex) {
		            	ex.printStackTrace();
				        logBuilder.append("Error updating project ID: " + dto.getId() + " - " + ex.getMessage());
		            }
		            
		            
		        }
		    } else {
		        logBuilder.append("No projects found from PoPortal API.");
		    }
		}

		//@Scheduled(cron = "0 30 14 * * ?")
		public void sendMailForExpiryProjects() {
		    LogDTO apiLogInfo = new LogDTO();
		    apiLogInfo.setSubFeatureName("sendMailForExpiryProjects");
		    apiLogInfo.setApiUrl("/api/sendMailForExpiryProjects");
		    apiLogInfo.setLogLevel("INFO");

		    StringBuilder logBuilder = new StringBuilder();
		    logBuilder.append("Cron to send expiry PO mail notification started! ");

		    List<Object[]> expiredProjectRows = projectRepository.getExpiredPoProjects();
		    System.out.println("Total Records Fetched: " + expiredProjectRows.size());

		    if (expiredProjectRows.isEmpty()) {
		        logBuilder.append("No expired PO projects found.");
		        return;
		    }
		    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		    List<ProjectPo> projectPoList = new ArrayList<>();
		    for (Object[] row : expiredProjectRows) {
		        ProjectPo po = new ProjectPo();
		        po.setPoNo((String) row[0]);
		        po.setDepartment((String) row[1]);
		        po.setProjectName((String) row[2]);
//		        po.setPoStartDate(row[3] instanceof Date ? (Date) row[3] : null);
//		        po.setPoEndDate(row[4] instanceof Date ? (Date) row[4] : null);
		        if (row[3] instanceof Date) {
		            po.setPoStartDate((Date) row[3]);
		        } else if (row[3] instanceof Timestamp) {
		            po.setPoStartDate(new Date(((Timestamp) row[3]).getTime()));
		        } else if (row[3] instanceof String) {
		            try {
						po.setPoStartDate(formatter.parse((String) row[3]));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
		        // Handle End Date
		        if (row[4] instanceof Date) {
		            po.setPoEndDate((Date) row[4]);
		        } else if (row[4] instanceof Timestamp) {
		            po.setPoEndDate(new Date(((Timestamp) row[4]).getTime()));
		        } else if (row[4] instanceof String) {
		            try {
						po.setPoEndDate(formatter.parse((String) row[4]));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
		        po.setApmosysRm((String) row[5]);
		        po.setPoProjectType((String) row[7]);
		        po.setRenewable((row[6] instanceof Integer && (Integer) row[6] == 1));
		        po.setHodEmail((String) row[8]);
//		        po.setPoProjectId((String)row[9]);
		        po.setPoProjectId(((BigInteger) row[9]).toString());
		        projectPoList.add(po);
		    }

		    for (ProjectPo po : projectPoList) {
		    	Set<String> uniqueEmails = new HashSet<>();
		        String hodEmail = po.getHodEmail();
		    	//String hodEmail = "sakti.das@apmosys.com";
		        String department = po.getDepartment();
		        
		        StringBuilder ccEmailBuilder = new StringBuilder();

		        if (rmgMail != null && !rmgMail.trim().isEmpty()) {
		            String[] rmgEmails = rmgMail.split(",");
		            for (String email : rmgEmails) {
		                if (email != null && !email.trim().isEmpty()) {
		                    uniqueEmails.add(email.trim());
		                }
		            }
		        }

		        if (adminMail != null && !adminMail.trim().isEmpty()) {
		            String[] adminMails = adminMail.split(",");
		            for (String email : adminMails) {
		                if (email != null && !email.trim().isEmpty()) {
		                    uniqueEmails.add(email.trim());
		                }
		            }
		        }

		        for (String email : uniqueEmails) {
		            if (ccEmailBuilder.length() > 0) {
		                ccEmailBuilder.append(",");
		            }
		            ccEmailBuilder.append(email);
		        }

		        String ccEmails = ccEmailBuilder.toString();


		        if (hodEmail != null && !hodEmail.isEmpty()) {
		            try {
		            	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		                // Fetch employees for this PO
		                List<Object[]> employeeRows = projectRepository.getEmployeesByPoProjectId(po.getPoProjectId());

		                // Generate Project HTML table
		                StringBuilder projectHtmlTable = new StringBuilder();
		                projectHtmlTable.append("<table border='1' style='border-collapse: collapse;'>")
                        .append("<tr><th>Project Name</th><th>Po Start Date</th><th>Po End Date</th><th>Apmosys RM</th>"
                        		+ "<th>Is Renewable</th><th>Po Project Type</th></tr>");

		                projectHtmlTable.append("<tr>")
		                .append("<td>").append(po.getProjectName()).append("</td>")
		                .append("<td>").append(po.getPoStartDate() != null ? sdf.format(po.getPoStartDate()) : "").append("</td>")
		                .append("<td>").append(po.getPoEndDate() != null ? sdf.format(po.getPoEndDate()) : "").append("</td>")
		                .append("<td>").append(po.getApmosysRm()).append("</td>")
		                .append("<td>").append(po.isRenewable()).append("</td>") 
		                .append("<td>").append(po.getPoProjectType()).append("</td>")
		                .append("</tr>");
		                
		                projectHtmlTable.append("</table>");
		                // Generate employee HTML table
		                StringBuilder empHtmlTable = new StringBuilder();
		                empHtmlTable.append("<table border='1' style='border-collapse: collapse;'>")
		                            .append("<tr><th>Name</th><th>Employeement Id</th><th>Email</th></tr>");
		                for (Object[] empRow : employeeRows) {
		                    empHtmlTable.append("<tr>")
		                                .append("<td>").append(empRow[0]).append("</td>")
		                                .append("<td>").append(empRow[1]).append("</td>")
		                                .append("<td>").append(empRow[2]).append("</td>")
		                                .append("</tr>");
		                }
		                empHtmlTable.append("</table>");

		                // Generate Excel report for this department (group same dept POs)
		                List<ProjectPo> deptProjects = projectPoList.stream()
		                        .filter(p -> department.equals(p.getDepartment()))
		                        .collect(Collectors.toList());

//		                File report = generateDepartmentPoReport(department, deptProjects);

		                String mailBody = "<p>Dear HOD,</p>"
		                        + "<p>Please find attached the expired PO report for your department: <strong>" + department + "</strong>.</p>"
		                        + "<p>Below is the details of the project for this PO: <strong>" + po.getPoNo() + "</strong>.</p>"
		                        + projectHtmlTable.toString()
		                        + "<p>Below is the list of employees mapped to PO: <strong>" + po.getPoNo() + "</strong>.</p>"
		                        + empHtmlTable.toString()
		                        + "<br/><p>Regards,<br/>IShine Team</p>";

		                mailService.sendMailWithCC(
		                        hodEmail,
		                        ccEmails,
		                        "Expired PO Report - " + department,
		                        mailBody
		                        );

		                logBuilder.append("Report sent to HOD: ").append(hodEmail).append("\n");

		            } catch (Exception e) {
		                e.printStackTrace();
		                logBuilder.append("Error sending report to HOD (").append(hodEmail).append("): ")
		                        .append(e.getMessage()).append("\n");
		            }
		        } else {
		            logBuilder.append("No HOD email found for department: ").append(department).append("\n");
		        }
		    }

		    System.out.println(logBuilder.toString()); // Final log output
		}
		
//		public File generateDepartmentPoReport(String department, List<ProjectPo> projectList) throws IOException {
//		    String fileName = department.replaceAll("\\s+", "_") + "_Expired_PO_Report.xlsx";
//		    File file = new File("/home/apmosys/Desktop/FILE_MAIL", fileName);
//
//		    try (FileOutputStream fos = new FileOutputStream(file)) {
//		        var wb = new Workbook(fos, "Expired PO Report", "1.0");
//		        Worksheet ws = wb.newWorksheet(department + "_POs");
//
//		        // Headers
//		        String[] headers = {
//		            "PO No", "Department", "Project Name", "PO Start Date",
//		            "PO End Date", "APMOSYS RM", "Is Renewable", "PO Project Type"
//		        };
//		        for (int i = 0; i < headers.length; i++) {
//		            ws.value(0, i, headers[i]);
//		        }
//
//		        // Data
//		        int row = 1;
//		        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
//		        for (ProjectPo po : projectList) {
//		            ws.value(row, 0, po.getPoNo());
//		            ws.value(row, 1, po.getDepartment());
//		            ws.value(row, 2, po.getProjectName());
//		            ws.value(row, 3, po.getPoStartDate() != null ? sdf.format(po.getPoStartDate()) : "N/A");
//		            ws.value(row, 4, po.getPoEndDate() != null ? sdf.format(po.getPoEndDate()) : "N/A");
//		            ws.value(row, 5, po.getApmosysRm());
//		          //  ws.value(row, 6, po.getIsRenewable() ? "Yes" : "No");
//		            ws.value(row, 7, po.getPoProjectType());
//		            row++;
//		        }
//
//		        wb.finish();
//		    }
//
//		    return null;
//		}
				
		private String buildEmailContent(String employeeName, String projectName, String teamName, 
                 String poStartDate, String poEndDate, String allocationStartDate) {
			return  "<html><body>"
				    + "<p>Dear " + employeeName + ",</p>"
				    + "<p>The project that you are currently billed for is expiring soon. "
				    + "This will result in timesheet restriction in case your project mapping is not removed.</p>"
				    + "<p>Please contact the concerned authorities to take necessary action immediately, "
				    + "as this could hamper the billing generated for you.</p>"
				    + "<p>Below are the project details:</p>"
				    + "<table border='1' style='border-collapse: collapse; width: 100%;'>"
				    + "<tr><th>Project Name</th><th>Team Name</th><th>PO Start Date</th>"
				    + "<th>PO End Date</th><th>Employee Allocation Start Date</th></tr>"
				    + "<tr><td>" + projectName + "</td><td>" + teamName + "</td><td>" 
				    + poStartDate + "</td><td>" + poEndDate + "</td><td>" + allocationStartDate + "</td></tr>"
				    + "</table><br><br>"
				    + "<p>Regards,</p>"
				    + "<p>RMG Team</p>"
				    + "</body></html>";
		}
		
		private String buildEmailContent2(String employeeName, String projectName, String teamName, 
                String poStartDate, String poEndDate, String allocationStartDate) {
			return "<html><body>"
					+ "<p>Dear " + employeeName + ",</p>"
					+ "<p>The project that you are currently billed for has expired, hence your timesheet filling has been restricted.</p>"
					+ "<p>Please contact the concerned authorities to take necessary action immediately, "
					+ "as this could hamper the billing generated for you.</p>"
					+ "<p>Below are the project details:</p>"
					+ "<table border='1' style='border-collapse: collapse; width: 100%;'>"
					+ "<tr><th>Project Name</th><th>Team Name</th><th>PO Start Date</th>"
					+ "<th>PO End Date</th><th>Employee Allocation Start Date</th></tr>"
					+ "<tr><td>" + projectName + "</td><td>" + teamName + "</td><td>" 
					+ poStartDate + "</td><td>" + poEndDate + "</td><td>" + allocationStartDate + "</td></tr>"
					+ "</table><br><br>"
					+ "<p>Regards,</p>"
					+ "<p>RMG Team</p>"
					+ "</body></html>";
		}
		
		public void triggerBillableTypeChangeMail(Long empId, String newBillableType, String oldBillableType, Long updatedById) {
		    try {
		        Object[] details = (Object[]) employeeRepository.findEmployeeDepartmentDetails(empId);
		        if (details == null) return;

		        Long empIdd = ((Number) details[0]).longValue();
		        String empName = (String) details[1];
		        String departmentName = (String) details[2];
		        Long hodId = ((Number) details[3]).longValue();

		        String updatedByName = employeeRepository.findEmployeeNameById(updatedById);
		        String hodEmail = employeeRepository.findHodEmailById(hodId);

		        StringBuilder html = new StringBuilder();
		        html.append("<html><body>");
		        html.append("<p>Dear HOD,</p>");
		        html.append("<p>The following billable type change has been made by <b>")
		            .append(updatedByName)
		            .append("</b>:</p>");

		        html.append("<div style='overflow-x:auto;'>");
		        html.append("<table border='1' style='border-collapse: collapse; width: 100%; table-layout: auto;'>");
		        html.append("<tr>");
		        html.append("<th style='white-space: nowrap; padding: 4px; text-align: center;'>Employee ID</th>");
		        html.append("<th style='white-space: nowrap; padding: 4px; text-align: center;'>Name</th>");
		        html.append("<th style='white-space: nowrap; padding: 4px; text-align: center;'>Billable Type</th>");
		        html.append("</tr>");

		        html.append("<tr>");
		        html.append("<td style='white-space: nowrap; padding: 4px; text-align: center;'>A-").append(empIdd).append("</td>");
		        html.append("<td style='white-space: nowrap; padding: 4px; text-align: center;'>").append(empName).append("</td>");
		        html.append("<td style='white-space: nowrap; padding: 4px; text-align: center;'>")
		            .append(oldBillableType).append(" &rarr; ").append(newBillableType).append("</td>");
		        html.append("</tr>");
		        html.append("</table>");
		        html.append("</div>");

		        html.append("<p>Regards,<br/>Ishine Team</p>");
		        html.append("</body></html>");

		        String subject = "Billable Type Change Notification for " + departmentName + " Department";

		        mailService.sendMailWithCC(hodEmail, billablechangeMailAddress, subject, html.toString());

		    } catch (Exception e) {
		        e.printStackTrace();
		        // Optionally log or handle the error here
		    }
		}


		
		public void triggerBulkBillableChangeEmails(List<Long> empIds, Map<Long, String> oldBillableTypes, String newBillableType, Long updatedById) {
		    Map<Long, List<Object[]>> deptToEmployeeDetails = new HashMap<>();

		    for (Long empId : empIds) {
		        Object[] details = (Object[]) employeeRepository.findEmployeeDepartmentDetails(empId);
		        if (details == null) continue;

		        Long empIdd = ((Number) details[0]).longValue();
		        String empName = (String) details[1];
		        String departmentName = (String) details[2];
		        Long hodId = ((Number) details[3]).longValue();

		        deptToEmployeeDetails.computeIfAbsent(hodId, k -> new ArrayList<>())
		            .add(new Object[]{empIdd, empName, departmentName, oldBillableTypes.get(empId), newBillableType});
		    }

		    String updatedByName = employeeRepository.findEmployeeNameById(updatedById);

		    for (Map.Entry<Long, List<Object[]>> entry : deptToEmployeeDetails.entrySet()) {
		        Long hodId = entry.getKey();
		        List<Object[]> employees = entry.getValue();
		        String hodEmail = employeeRepository.findHodEmailById(hodId);

		        // Get department name from first employee (they're all from the same department)
		        String departmentName = (String) employees.get(0)[2];

		        StringBuilder html = new StringBuilder();
		        html.append("<html><body>");
		        html.append("<p>Dear HOD,</p>");
		        html.append("<p>The following billable type changes have been made by <b>")
		            .append(updatedByName)
		            .append("</b>:</p>");

		        html.append("<div style='overflow-x:auto;'>");
		        html.append("<table border='1' style='border-collapse: collapse; width: 100%; table-layout: auto;'>");
		        html.append("<tr>");
		        html.append("<th style='white-space: nowrap; padding: 4px; text-align: center;'>Employee ID</th>");
		        html.append("<th style='white-space: nowrap; padding: 4px; text-align: center;'>Name</th>");
		        html.append("<th style='white-space: nowrap; padding: 4px; text-align: center;'>Billable Type</th>");
		        html.append("</tr>");

		        for (Object[] emp : employees) {
		            html.append("<tr>");
		            html.append("<td style='white-space: nowrap; padding: 4px; text-align: center;'>A-").append(emp[0]).append("</td>");
		            html.append("<td style='white-space: nowrap; padding: 4px; text-align: center;'>").append(emp[1]).append("</td>");
		            html.append("<td style='white-space: nowrap; padding: 4px; text-align: center;'>")
		                .append(emp[3]).append(" &rarr; ").append(emp[4]).append("</td>");
		            html.append("</tr>");
		        }

		        html.append("</table>");
		        html.append("</div>");
		        html.append("<p>Regards,<br/>Ishine Team</p>");
		        html.append("</body></html>");

		        String subject = "Billable Type Change Notification for " + departmentName + " Department";

		        try {
		            mailService.sendMailWithCC(hodEmail, billablechangeMailAddress, subject, html.toString());
		        } catch (MessagingException e) {
		            e.printStackTrace();
		            
		        }
		    }
		}
		
		
		
		
		
		
		
		
	
				
		
		
		
		
		
}	
