package com.apmosys.employeeportal.service;

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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.Holiday;
import com.apmosys.employeeportal.model.LeavePolicyMaster;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.Timesheet;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.HolidayRepository;
import com.apmosys.employeeportal.repository.LeavePolicyMasterRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;

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

	@Value("${po.db.url}")
	private String url;

	@Value("${po.db.username}")
	private String username;

	@Value("${po.db.password}")
	private String password;
	
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
		
		short leaveTypeMasterId = 0;
		try {
		     List<LeaveTypeMaster> leaveType = leaveTypeMasterRepository.findAll();
		     
		          for(LeaveTypeMaster ltm :leaveType) {
			      leaveTypeMasterId = ltm.getLeaveTypeMasterId();
			
			      List<LeavePolicyMaster> leavePolicy  = leavePolicyMasterRepository.findByLeaveTypeMasterId(leaveTypeMasterId);
			      
			          for(LeavePolicyMaster lpm : leavePolicy) {
				         if((lpm.getCarryForward().equals("Yes") && lpm.getExpirationPeriod().equals("NA") && lpm.getIncrement().equals("Yes") && lpm.getLeaveApplication().equals("Yes")) || (lpm.getCarryForward().equals("No") && lpm.getExpirationPeriod().equals("NA") && lpm.getIncrement().equals("Yes") && lpm.getLeaveApplication().equals("Yes"))) {
				     	     float incrementValue = lpm.getIncrementValue();
					
					         List<EmployeeLeavesMap> employeeLeaveMap = employeeLeavesMapRepository.findByLeaveTypeMasterId(leaveTypeMasterId);
					       
					              for(EmployeeLeavesMap elm :employeeLeaveMap) {
						              float dbBalance = elm.getBalance();
						              float newBalance = dbBalance + incrementValue;
						
						              elm.setBalance(newBalance);
						              employeeLeavesMapRepository.save(elm);
						              break;
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
				         if(lpm.getCarryForward().equals("Yes")) {
				     	     float carryForwardValue = lpm.getCarryForwardValue();
					
					         List<EmployeeLeavesMap> employeeLeaveMap = employeeLeavesMapRepository.findByLeaveTypeMasterId(leaveTypeMasterId);
					       
					              for(EmployeeLeavesMap elm :employeeLeaveMap) {
						              float dbBalance = elm.getBalance();
						              
						              if(dbBalance > carryForwardValue) {
						            	  float newBalance = carryForwardValue;
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
	
	//0 0 1 ? JAN *  - At 01:00:00am, in January
	
	@Scheduled(cron = "0 0 1 ? JAN *")
	public void addingWeekOff() {
		
		try {
			int monthCount = 1;
			
			// For adding 2nd & 4th Saturday
			
			while(monthCount <= 12) {
				
				int currentYear = LocalDate.now().getYear();
				LocalDate dateToday = LocalDate.of(currentYear, monthCount, 1);
				
				LocalDate secondSaturday = dateToday.with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.SATURDAY));
				
				  Holiday newHoliday = new Holiday();
				  newHoliday.setDateOfHoliday(secondSaturday);
				  newHoliday.setDayOfTheWeek("Saturday");
				  newHoliday.setHolidayType("WeekOff");
				  newHoliday.setOccasion("Saturday : second saturday");
				  newHoliday.setState("all");
				  newHoliday.setOptionalHoliday("false");
				  
				  holidayRepository.save(newHoliday);
				  
				LocalDate fourthSaturday = dateToday.with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.SATURDAY));
                 
				  Holiday newHolidayObj = new Holiday();
				  newHolidayObj.setDateOfHoliday(fourthSaturday);
				  newHolidayObj.setDayOfTheWeek("Saturday");
				  newHolidayObj.setHolidayType("WeekOff");
				  newHolidayObj.setOccasion("Saturday : fourth saturday");
				  newHolidayObj.setState("all");
				  newHolidayObj.setOptionalHoliday("false");
				  
				  holidayRepository.save(newHolidayObj);
				  
			// For adding Sundays	  
				
				Calendar calander = new GregorianCalendar(currentYear, monthCount - 1, 1);
		        do {
		            int day = calander.get(Calendar.DAY_OF_WEEK);
		            if (day == Calendar.SUNDAY) {
		            	Date date = calander.getTime();
		            	LocalDate sundayDate = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(date));
		            	
		            	  Holiday holidayObj = new Holiday();
		            	  holidayObj.setDateOfHoliday(sundayDate);
		            	  holidayObj.setDayOfTheWeek("Sunday");
		            	  holidayObj.setHolidayType("WeekOff");
		            	  holidayObj.setOccasion("Sunday");
		            	  holidayObj.setState("all");
		            	  holidayObj.setOptionalHoliday("false");
		            	  
						  holidayRepository.save(holidayObj);
						  
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
				
				List<Employee> allEmployee = employeeRepository.findAll();
				List<Holiday> publicHoliday = holidayRepository.findByDateOfHoliday(dateToday);
				
			//	Timesheet filler for weekoff day : saturday & sunday
				
				if(!publicHoliday.isEmpty()) {
					
					for(Holiday holiday: publicHoliday) {
						String holidayOccassion = holiday.getOccasion();
						String dayOfWeek = holiday.getDayOfTheWeek();
						
						if((holiday.getHolidayType().equals("WeekOff") && dayOfWeek.equals("Saturday")) || (holiday.getHolidayType().equals("WeekOff") && dayOfWeek.equals("Sunday"))) {
							for(Employee empObj: allEmployee) {
								
								Timesheet empTimesheet = timesheetsRepository.findByEmpIdAndDate(empObj.getEmpId(),dateToday);
								
								if(empTimesheet == null) {
									Timesheet newTimesheet = new Timesheet();
									
									newTimesheet.getCommonProperty().setCreatedBy(empObj.getEmpId());
									newTimesheet.setDate(dateToday);
									newTimesheet.setDayType("Holiday");
									if(holidayOccassion.equals("Saturday")) {
										newTimesheet.setDescription("WeekOff : Saturday");
									}else{
										newTimesheet.setDescription("WeekOff : Sunday");
									}
									newTimesheet.setEmpId(empObj.getEmpId());
									newTimesheet.setStatus("Pending");
									
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
						
						for(Employee empObj: allEmployee) {
							String workLocation = empObj.getWorkLocation();

							if((holidayState.equals("all") && holidays.getOptionalHoliday().equals("false") && (holidays.getHolidayType().equals("Festival") || holidays.getHolidayType().equals("nonWorking")))
									|| (holidayState.equals(workLocation) && holidays.getOptionalHoliday().equals("false") && (holidays.getHolidayType().equals("Festival") || holidays.getHolidayType().equals("nonWorking")))){
								
								Timesheet newTimesheet = new Timesheet();
								
								newTimesheet.getCommonProperty().setCreatedBy(empObj.getEmpId());
								newTimesheet.setDate(dateToday);
								newTimesheet.setDayType("Holiday");
								newTimesheet.setDescription("Public Holiday");
								newTimesheet.setEmpId(empObj.getEmpId());
								newTimesheet.setStatus("Pending");
								
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
							newTimesheet.setDayType("Working");
							newTimesheet.setDescription("On leave");
							newTimesheet.setEmpId(empId);
							newTimesheet.setStatus("Pending");
							
							timesheetsRepository.save(newTimesheet);
						}
						
						if((elapsedDays != 0) && leaveObj.getLeaveStatusId().equals(approvedLeave)) {
							LocalDate tempDateToday = dateToday;
							
							while(tempDateToday.compareTo(leaveObj.getToDate()) != 1) {
								
								Timesheet newTimesheet = new Timesheet();
								
								newTimesheet.getCommonProperty().setCreatedBy(empId);
								newTimesheet.setDate(tempDateToday);
								newTimesheet.setDayType("Working");
								newTimesheet.setDescription("On leave");
								newTimesheet.setEmpId(empId);
								newTimesheet.setStatus("Pending");
								
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
		
}	
