package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import javax.mail.MessagingException;

import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.BirthdayMail;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.Holiday;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.model.LeavePolicyMaster;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.model.PortalConfig;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.repository.BirthdayMailRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.HolidayRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.repository.LeavePolicyMasterRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.repository.PortalConfigRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TimesheetActivityMapRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.LeaveLogMessage;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class CronJobService {

	@Autowired
	ProjectRepository projectRepository;
	
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
	MailService mailService;

	@Value("${po.db.url}")
	private String url;

	@Value("${po.db.username}")
	private String username;

	@Value("${po.db.password}")
	private String password;
	
	@Value("${hr.mail}")
	private String hrMailAddress;
	
//	0 0 0 * * * for every midnight
//	*/20 * * * * *  for every 20 secs

	@Scheduled(cron = "0 0 0 * * *")
	public void authenticateUser() {

		System.out.println(LocalDateTime.now());
		try {

			String query = "SELECT cd.clientName,cd.clientLocation,cd.state,pfc.projectName,p.description,p.projectManagerId,u.empId,p.approvedOn FROM PoFixedCost pfc\n"
					+ "INNER JOIN ClientDetails cd ON pfc.clientId = cd.clientid\n"
					+ "INNER JOIN Project p ON p.name = pfc.projectName\n"
					+ "INNER JOIN User u ON u.id = p.projectManagerId\n"
					+ "WHERE p.status = \"Approved\" and p.approvedOn between now() - INTERVAL 5 DAY AND now() ORDER BY p.id DESC";
			Class.forName("com.mysql.cj.jdbc.Driver");
			int i = 0;

			try (Connection con = DriverManager.getConnection(url, username, password)) {
				PreparedStatement ps = con.prepareStatement(query);

				try (ResultSet rs = ps.executeQuery();) {

					while (rs.next()) {

						Project newProject = new Project();

						newProject.setClientName(rs.getString(1) != null ? rs.getString(1) : null);
						newProject.setClientLocation(rs.getString(2) != null ? rs.getString(2) : null);
						newProject.setState(rs.getString(3) != null ? rs.getString(3) : null);
						newProject.setProjectName(rs.getString(4) != null ? rs.getString(4) : null);
						newProject.setDescription(rs.getString(5) != null ? rs.getString(5) : null);
						newProject.setProjectManagerId(rs.getLong(6) != 0L ? rs.getLong(6) : null);

						if (rs.getString(7) != null) {
							String empIdString = rs.getString(7);
							empIdString = empIdString.replace("A-", "");
							newProject.setEmpId(Long.parseLong(empIdString));

						}
						newProject.setApprovedOn(rs.getTimestamp(8) != null ? rs.getTimestamp(8) : null);

						System.out.println(newProject);
						System.out.println(projectRepository.save(newProject) != null
								? "Project " + newProject.getProjectName() + " added to Employee portal"
								: "Failed to add " + newProject.getProjectName() + " project to Employee portal");
						i++;
					}

					System.out.println(
							i == 0 ? "No new projects found at PO portal." : i + " new project(s) found at PO portal");

				}

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(
						i == 0 ? "No new projects found at PO portal." : i + " new project(s) found at PO portal");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	//0 0 12 1 * ?  - Every month on the 1st, at noon
	@Scheduled(cron = "0 0 12 1 * ?")
	public void monthlyLeaveIncrement() {
		try {
		     List<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findAll();
		     List<Employee> employeeList = employeeRepository.findAll();
		     
		     if(!leaveType.isEmpty()) {
		    	 for(LeaveTypeMaster ltm :leaveType) {
		        	  for(Employee employeeObj : employeeList) {
		        		  
		        		  System.out.println(employeeObj.getEmploymentstatus() +"  "+ ltm.getLeaveTypeMasterId());
		        		  
		        		  Optional<LeavePolicyMaster> leavePolicy  = leavePolicyMasterRepository.
		        				  findByEmployentStatusAndLeaveTypeMasterId(employeeObj.getEmploymentstatus(),ltm.getLeaveTypeMasterId());
		        		  
		        		  if(!leavePolicy.isEmpty()) {
		        			  LeavePolicyMaster leavePolicyObj = leavePolicy.get();
		        			  if(leavePolicyObj.getIncrement().equals("Yes")){
		        				  
		        				  EmployeeLeavesMap employeeLeaveMap = employeeLeavesMapRepository.
		        						  findByEmpIdAndLeaveTypeMasterId(employeeObj.getEmpId(),ltm.getLeaveTypeMasterId());
		        				  
		        				  System.out.println(employeeLeaveMap.getBalance() +"  "+ leavePolicyObj.getIncrementValue());
		        				  
		        				          if(employeeLeaveMap != null) {
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
	
	@Scheduled(cron = "0 0 0 31 MAR ?")
	public void YearlyLeaveCronJob() {
		
		short leaveTypeMasterId = 0;
		try {
		     List<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findAll();
		     
		          for(LeaveTypeMaster ltm :leaveType) {
			      leaveTypeMasterId = ltm.getLeaveTypeMasterId();
			
			      List<LeavePolicyMaster> leavePolicy  = leavePolicyMasterRepository.findByLeaveTypeMasterId(leaveTypeMasterId);
			      
			          for(LeavePolicyMaster lpm : leavePolicy) {
				         if(lpm.getCarryForward().equals("Yes") || lpm.getCarryForward().equals("No")) {
					
					         List<EmployeeLeavesMap> employeeLeaveMap = employeeLeavesMapRepository.findByLeaveTypeMasterId(leaveTypeMasterId);
					       
					              for(EmployeeLeavesMap elm :employeeLeaveMap) {
						              float dbBalance = elm.getBalance();
						              
										if (lpm.getCarryForward().equals("No")) {
											
											float newBalance = 0;
											elm.setBalance(newBalance);
											EmployeeLeavesMap dbResponse = employeeLeavesMapRepository.save(elm);
											
											if (dbResponse != null) {
												LeaveBalanceLog log = new LeaveBalanceLog();

												log.setBalance(newBalance);
												log.setEmpId(elm.getEmpId());
												log.setLeaveTypeMasterId(ltm.getLeaveTypeMasterId());
												log.setMessage(LeaveLogMessage.autoDeductLeave.replace("0.0",
														Float.toString(dbBalance)));
												log.setUpdateBalanceBy("-" + dbBalance);

												leaveBalanceLogRepository.save(log);
											}
										} else if (lpm.getCarryForward().equals("Yes")) {
											float carryForwardValue = lpm.getCarryForwardValue();
											
											if (dbBalance > carryForwardValue) {
												float newBalance = carryForwardValue;
												float deductedLeaveCount = dbBalance - carryForwardValue;
												elm.setBalance(newBalance);
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
	
	@Scheduled(cron = "0 1 1 ? * *")
	public void LeaveExpirationCronJob() {
		
		short leaveTypeMasterId = 0;
		try {
		     List<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findAll();
		     
		          for(LeaveTypeMaster ltm :leaveType) {
			      leaveTypeMasterId = ltm.getLeaveTypeMasterId();
			
			      List<LeavePolicyMaster> leavePolicy  = leavePolicyMasterRepository.findByLeaveTypeMasterId(leaveTypeMasterId);
			      
			          for(LeavePolicyMaster lpm : leavePolicy) {
				         if(lpm.getExpirationPeriod().equals("Yes")) {
				        	 
				        	 Integer expirationPeriod = lpm.getExpirationPeriodValue();
				     	     Timestamp createdOnDate = lpm.getCreatedOn();
				     	     
				     	     Timestamp expirationDate = Timestamp.valueOf(createdOnDate.toLocalDateTime().plusDays(expirationPeriod));
				   
				    		 DateFormat f = new SimpleDateFormat("yyyy-MM-dd");
				    		 String expiration = f.format(expirationDate);
				    		 System.out.println(expiration);
				     	     
				     	     LocalDateTime dateTime = LocalDateTime.now();
				             String todayDate = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(dateTime);
				             System.out.println(todayDate);
					         
				             List<EmployeeLeavesMap> employeeLeaveMap = employeeLeavesMapRepository.findByLeaveTypeMasterId(leaveTypeMasterId);
					
					              for(EmployeeLeavesMap elm :employeeLeaveMap) {
						   
						              if(expiration.equals(todayDate)) {
						            	  float newBalance = 0;
						            	  elm.setBalance(newBalance);
							              employeeLeavesMapRepository.save(elm);
							              break;
						              }
					              }
				          }
			           }
		            }
		   }catch(Exception e) {
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
	
		@Scheduled(cron = "0 0 21 ? * *")
		public void automaticTimesheetFiller() {
			
			try {
				
				LocalDate dateToday = LocalDate.now();
				
				List<Object[]> allEmployee = employeeRepository.getEmployeeDetailForCron();
				List<Holiday> publicHoliday = holidayRepository.findByDateOfHoliday(dateToday);
				
			//	Timesheet filler for weekoff day : saturday & sunday
				
				if(!publicHoliday.isEmpty()) {
					
					for(Holiday holiday: publicHoliday) {
						String holidayOccassion = holiday.getOccasion();
						String dayOfWeek = holiday.getDayOfTheWeek();
						
						if((holiday.getHolidayType().equals("WeekOff") && dayOfWeek.equals("Saturday")) || (holiday.getHolidayType().equals("WeekOff") && dayOfWeek.equals("Sunday"))) {
							for(Object[] employeeList: allEmployee) {
								Long empId = employeeList[0] != null ? Long.parseLong(employeeList[0].toString()) : null;
								
								Timesheet empTimesheet = timesheetsRepository.findByEmpIdAndDate(empId,dateToday);
								
								if(empTimesheet == null) {
									Timesheet newTimesheet = new Timesheet();
									
									newTimesheet.getCommonProperty().setCreatedBy(empId);
									newTimesheet.setDate(dateToday);
									newTimesheet.setDayType("Holiday");
									if(holidayOccassion.equals("Saturday : second saturday") || holidayOccassion.equals("Saturday : fourth saturday")) {
										newTimesheet.setDescription("WeekOff : Saturday");
									}else{
										newTimesheet.setDescription("WeekOff : Sunday");
									}
									newTimesheet.setEmpId(empId);
									// For weekoff's managers don't have to approve the timesheet, if any employee worked on weekoff will revoke this ..
									newTimesheet.setStatus("Approved");
									
									timesheetsRepository.save(newTimesheet);
								}				
							}
						}
					}		
				}
				
		   //	Timesheet filler for public Holiday
				
				if(!publicHoliday.isEmpty()) {
					
					for(Holiday holidays: publicHoliday) {
						String holidayState = holidays.getState();
						
						for(Object[] employeeList: allEmployee) {
							Long empId = employeeList[0] != null ? Long.parseLong(employeeList[0].toString()) : null;
							String workLocation = employeeList[1] != null ? employeeList[1].toString() : null;

							if((holidayState.equals("all") && holidays.getOptionalHoliday().equals("false") && (holidays.getHolidayType().equals("Festival") || holidays.getHolidayType().equals("nonWorking")))
									|| (holidayState.equals(workLocation) && holidays.getOptionalHoliday().equals("false") && (holidays.getHolidayType().equals("Festival") || holidays.getHolidayType().equals("nonWorking")))){
								
								Timesheet newTimesheet = new Timesheet();
								
								newTimesheet.getCommonProperty().setCreatedBy(empId);
								newTimesheet.setDate(dateToday);
								newTimesheet.setDayType("Holiday");
								newTimesheet.setDescription("Public Holiday");
								newTimesheet.setEmpId(empId);
								newTimesheet.setStatus("Approved");
								
								timesheetsRepository.save(newTimesheet);
								
							}
						}	
					}
				}
				
			//	Timesheet filler for leave days
				
				List<EmployeeLeave> employeeLeave = employeeLeaveRepository.findByFromDate(dateToday);
				
				if(!employeeLeave.isEmpty()) {
					
					for(EmployeeLeave leaveObj: employeeLeave) {
						Long empId = leaveObj.getEmpId();
						Short approvedLeave = 2;
						
						long elapsedDays = ChronoUnit.DAYS.between(leaveObj.getFromDate(), leaveObj.getToDate());
						
						if((elapsedDays == 0) && leaveObj.getLeaveStatusId().equals(approvedLeave)) {
							
							Timesheet newTimesheet = new Timesheet();
							
							newTimesheet.getCommonProperty().setCreatedBy(empId);
							newTimesheet.setDate(dateToday);
							newTimesheet.setDayType("Holiday");
							newTimesheet.setDescription("On leave");
							newTimesheet.setEmpId(empId);
							newTimesheet.setStatus("Approved");
							
							timesheetsRepository.save(newTimesheet);
						}
						
						if((elapsedDays != 0) && leaveObj.getLeaveStatusId().equals(approvedLeave)) {
							LocalDate tempDateToday = dateToday;
							
							while(tempDateToday.compareTo(leaveObj.getToDate()) != 1) {
								
								Timesheet newTimesheet = new Timesheet();
								
								newTimesheet.getCommonProperty().setCreatedBy(empId);
								newTimesheet.setDate(tempDateToday);
								newTimesheet.setDayType("Holiday");
								newTimesheet.setDescription("On leave");
								newTimesheet.setEmpId(empId);
								newTimesheet.setStatus("Approved");
								
								timesheetsRepository.save(newTimesheet);
								
								tempDateToday = tempDateToday.plusDays(1);
							}
						}					
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		// 0 0 12 ? * * - At 12:00:00pm every day
		
		@Scheduled(cron="${mailTrigger.time}")
		public void protalConfigMailTrigger() {
			try {
				
				List<PortalConfig> portalConfigObj = portalConfigRepository.findAll();
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
							mailService.sendMailWithCC(employeeData.getEmail(),hrMailAddress,"Regarding Probation Period","Employee with EmpId : A-"
						                    + employeeData.getEmployeementId() + "<br> Name : " + employeeData.getName()
						                    + "<br> will complete its probation period in " + probationMailTrigger.shortValue() + " days");
						}
						
					}
					
					if(employeeData.getDateOfResign() != null){
						
						LocalDate relievingDate = stringToDateTimeParser.getDate(employeeData.getDateOfResign(), "yyyy-MM-dd").plusDays(employeeData.getNoticePeriod());
						LocalDate mailTriggerDate = relievingDate.minusDays(noticePeriodMailTrigger.shortValue());
						if(LocalDate.now().equals(mailTriggerDate)) {
							mailService.sendMailWithCC(employeeData.getEmail(),hrMailAddress,"Regarding Notice Period","Employee with EmpId : A-"
						                    + employeeData.getEmployeementId() + "<br> Name : " + employeeData.getName()
						                    + "<br> will complete its Notice period in " + noticePeriodMailTrigger.shortValue() + " days");
						}
						
					}
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
		@Scheduled(cron = "0 0 7 ? * *")
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
			Long id = (long) (random.nextInt(25 - 1) + 1); /* Random number will be generated between 1 and 25 */

			Optional<BirthdayMail> birthDayMail = birthdayMailRepository.findById(1l);

			if (birthDayMail.isPresent() && !employeeList.isEmpty()) {
				for (EmployeeDTO emp : employeeList) {
					String subject = "Happy Birthday " + emp.getName();
					String heading = birthDayMail.get().getHeading();
					String description = birthDayMail.get().getDescription();
					String mailBody = "<table style=\"background-color: #4E94CF; font-family: Arial; font-size: 14px; padding: 20px; width: 800px;\" align=\"center\">\n"
							+ "    <tbody>\n" + "    <tr>\n"
							+ "        <td class=\"wysiwyg-text-align-center\" style=\"padding: 20px;\"><span class=\"wysiwyg-color-black10\"></span><br />\n"
							+ "            <table style=\"background-color: #ffffff;\" border=\"0\" width=\"700px\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\">\n"
							+ "                <tbody>\n"
							+ "                <tr style=\"padding-top: 20px; text-align: center;\">\n"
							+ "                    <td style=\"padding: 30px;\">\n"
							+ "                        <div style=\"text-align: left;\">Dear " + emp.getName() + ",</div>\n"
							+ "<br>                    <p style=\"text-align: left;\"></p>\n"
							+ "                        <div style=\"text-align: left;\">" + heading + "</div><br>\n"
							+ "                        <div style=\"text-align: left;\">" + description + "</div>\n"
							+ "<br><img src=\"cid:image\" />"
							+ "                            <p style=\"text-align: left;\">Regards,<br>ApMoSyS</p>\n"
							+ "                </tr>\n" + "                </tbody>\n" + "            </table>\n"
							+ "        </td>\n" + "    </tr>\n" + "    </tbody>\n" + "</table>";

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
}	
