package com.apmosys.employeeportal.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeavePolicyMaster;
import com.apmosys.employeeportal.model.LeaveTypeMaster;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.LeavePolicyMasterRepository;
import com.apmosys.employeeportal.repository.LeaveTypeMasterRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;

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

	
}	
