package com.apmosys.employeeportal.service;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.BioMaTO;
import com.apmosys.employeeportal.dto.BioMax360;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class BioMaxService {
	@Autowired
	TimesheetService TimesheetService;
	@Value("${BioDbIp}")
	public String DBIp;
	Connection con = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
	
//	@Value("${BioDbPort}")
	//public String DBPort;
	@Value("${BioDbUser}")
	public String DBUsername;
	@Value("${BioDbPass}")
	public String DBPassowrd;

	@Value("${BioDbName}")
	public String DBName;

	@Autowired
	EmployeeRepository employeeRepository;

	public Connection getConnection() {
		
		

		String DB_URL = "jdbc:sqlserver://" + DBIp + "/" + DBName;

		try {
			
			 Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		
//			Connection con = DriverManager.getConnection("jdbc:sqlserver://yourserver:1433;databaseName=yourdatabase;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");
//			Connection con = DriverManager.getConnection("jdbc:sqlserver://yourserver:1433;databaseName=yourdatabase;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");
//			 Connection con = DriverManager.getConnection("jdbc:sqlserver://192.168.0.126:1433;databaseName=SmartOfficedb;user=sa;password=biomax;");
			  con = DriverManager.getConnection("jdbc:sqlserver://192.168.0.126:1433;databaseName=smartofficedb;encrypt=true;trustServerCertificate=true;user=sa;password=biomax;sslProtocol=TLSv1.2");

			 
			 if (con !=null) {
				System.out.println("Connection made");
			}
			return con;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	 public Connection getConnectionForProject() {
	        String DB_URL = "jdbc:mysql://localhost:3306/db_emp_portal"; // Replace with your MySQL server info
	        String DB_USER = "root"; // Replace with your MySQL username
	        String DB_PASSWORD = "Welcome@2024"; // Replace with your MySQL password

	        try {
	            // Load MySQL JDBC driver
	            Class.forName("com.mysql.cj.jdbc.Driver");

	            // Establish connection
	             con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

	            if (con != null) {
	                System.out.println("Connection made to MySQL database");
	            }
	            return con;
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return null;
	    }
	 
	public String getProjectTimeSheet(String date,String employeeId) {
		String timesheetId="0";
	
		String Query="SELECT * FROM employee_timesheets AS e \n"
				+ "WHERE \n"
				+ "e.emp_id IN (\n"
				+ "    SELECT employee.emp_id \n"
				+ "    FROM db_emp_portal.employee \n"
				+ "    WHERE employee.emp_id = '"+employeeId+"'"
				+ ")\n"
				+ "AND\n"
				+ "e.date=DATE_FORMAT(STR_TO_DATE('"+date+"', '%d-%b-%Y'), '%Y-%m-%d')\n";
		
		try {
			con = getConnectionForProject();
			 statement=con.prepareStatement(Query);
			 resultSet=statement.executeQuery();
				 if(resultSet.next()) {
					 
				 timesheetId=resultSet.getString("timesheet_id");
			 }else {
				 timesheetId="0";
			 }
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
	        // Properly close the resources in the finally block
	        try {
	            if (resultSet != null) {
	            	resultSet.close();
	            }
	            if (statement != null) {
	                statement.close();
	            }
	            if (con != null) {
	            	con.close();
	            }
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	    }
		
		
		return timesheetId;
				
	}
	public ServiceResponse getEmpBioData(String date) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			Set<String> bioEmpIdSet = new HashSet<>();
			Map<String, List<String>> empMapById = new HashMap<>();
			List<BioMaTO> finalEmpBioData=new ArrayList();
			 String Query = "WITH LatestLogDate AS (\n"
			 		+ "    SELECT \n"
			 		+ "        dl.UserId,\n"
			 		+ "        MAX(dl.LogDate) AS LastLogDate\n"
			 		+ "    FROM \n"
			 		+ "        [SmartOfficedb].[dbo].[DeviceLogs_10_2024] dl\n"
			 		+ "    INNER JOIN \n"
			 		+ "        [SmartOfficedb].[dbo].[Employees] emp ON dl.UserId = emp.EmployeeCode\n"
			 		+ "    INNER JOIN \n"
			 		+ "        [SmartOfficedb].[dbo].[AttendanceLogs] adl ON emp.EmployeeId = adl.EmployeeId\n"
			 		+ "    WHERE \n"
			 		+ "        adl.AttendanceDateStr = ?"
			 		+ "    GROUP BY \n"
			 		+ "        dl.UserId\n"
			 		+ ")\n"
			 		+ "SELECT \n"
			 		+ "    dl.LogDate,\n"
			 		+ "    emp.EmployeeCode,\n"
			 		+ "    emp.EmployeeName,\n"
			 		+ "     adl.TotalDuration,\n"
			 		+ "     s.ShiftName ,\n"
			 		+ "    adl.BeginTime,\n"
			 		+ "    adl.EndTime,\n"
			 		+ "    adl.Status,\n"
			 		+ "    adl.PunchRecords,\n"
			 		+ "    adl.EarlyBy,\n"
			 		+ "    adl.LateBy,\n"
			 		+ "    adl.Duration,\n"
			 		+ "    adl.InTime,\n"
			 		+ "    adl.OutTime,\n"
			 		+ "    adl.ShiftDuration\n"
			 		+ "FROM \n"
			 		+ "    LatestLogDate lld\n"
			 		+ "INNER JOIN \n"
			 		+ "    [SmartOfficedb].[dbo].[DeviceLogs_10_2024] dl ON lld.UserId = dl.UserId AND lld.LastLogDate = dl.LogDate\n"
			 		+ "INNER JOIN \n"
			 		+ "    [SmartOfficedb].[dbo].[Employees] emp ON dl.UserId = emp.EmployeeCode\n"
			 		+ "INNER JOIN \n"
			 		+ "    [SmartOfficedb].[dbo].[AttendanceLogs] adl ON emp.EmployeeId = adl.EmployeeId\n"
			 		+ "INNER JOIN \n"
			 		+ "    [SmartOfficedb].[dbo].[Shifts] s ON adl.ShiftId = s.ShiftId\n"
			 		+ "    \n"
			 		+ "WHERE \n"
			 		+ "    adl.AttendanceDateStr = ?"; // Use ? as a placeholder

			Connection con = getConnection();
			PreparedStatement statement = con.prepareStatement(Query);
			
			// Set the date parameter
		    statement.setString(1, date); // Set the first placeholder
		    statement.setString(2, date); // Set the second placeholder

			
			ResultSet resultSet = statement.executeQuery();

//			String temp1 = "";

//			List<String> l = new ArrayList<>();

			while (resultSet.next()) {
				
				BioMaTO bioMaTO=new BioMaTO();
				bioMaTO.setLogDate(resultSet.getString("LogDate"));
				bioMaTO.setEmployeeCode(resultSet.getString("EmployeeCode"));
			    bioMaTO.setEmployeeName(resultSet.getString("EmployeeName"));
			    bioMaTO.setTotalDuration(resultSet.getString("TotalDuration")); // Assuming TotalDuration is a String
			    bioMaTO.setShiftName(resultSet.getString("ShiftName")); // Added shift name from your query
			    bioMaTO.setBeginTime(resultSet.getString("BeginTime"));
			    bioMaTO.setEndTime(resultSet.getString("EndTime"));
			    bioMaTO.setStatus(resultSet.getString("Status"));
			    bioMaTO.setPunchRecords(resultSet.getString("PunchRecords"));
			    bioMaTO.setEarlyBy(resultSet.getString("EarlyBy"));
			    bioMaTO.setLateBy(resultSet.getString("LateBy"));
			    bioMaTO.setDuration(resultSet.getString("Duration")); // Assuming Duration is a String
			    bioMaTO.setInTime(resultSet.getString("InTime"));
			    bioMaTO.setOutTime(resultSet.getString("OutTime"));
			    bioMaTO.setShiftDuration(resultSet.getString("ShiftDuration")); // Assuming ShiftDuration is a String

			    finalEmpBioData.add(bioMaTO);
			} 	    
				
				
				

//				bioEmpIdSet.add(resultSet.getString(1).replace("A", "").trim());
//
//				if (temp1.equals("")) {
//					temp1 = resultSet.getString(1).replace("A", "").trim();
//					l.add(resultSet.getString(2));
//				} else if (resultSet.getString(1).replace("A", "").trim().equals(temp1)) {
//					l.add(resultSet.getString(2));
//				} else {
//					empMapById.put(temp1, l);
//					l = new ArrayList();
//					temp1 = resultSet.getString(1).replace("A", "").trim();
//					l.add(resultSet.getString(2));
//				}

//			}
//			empMapById.put(temp1, l);
			
			
//			List<Object[]> empNameFromIshine = employeeRepository.getDataByEmpId(bioEmpIdSet);
//			Map<String, String> EmplNameIdMap = new HashMap();
//
//			for (Object[] obj : empNameFromIshine) {
//				EmplNameIdMap.put(obj[0].toString(), obj[1].toString());
//			}
			
			
//
//			for (String empId : bioEmpIdSet) {
//				List<String> empTimeList = empMapById.get(empId);
//				String time1 = "";
//				String time2 = "";
//				String punchIn="";
//				String punchOut="";
//				long totalTime=0;
//				for (String dateTime : empTimeList) {
//					
//					if(punchIn.equals(""))
//					{
//						punchIn=dateTime;
//					}
//					
//					if (time1.equals("") && time2.equals("")) {
//						time1 = dateTime;
//					}
//					else if (!time1.equals("") && time2.equals("")) {
//						time2 = dateTime;
//					}
//					if (!time1.equals("") && !time2.equals("")) {
//						//time calculation ...
//						long cTime=calculateTime(time1,time2);
//						totalTime += cTime;	
//						punchOut=time2;
//						time1="";
//						time2="";
//					}
//				}
//				Long hours = totalTime / 3600;
//				Long minutes = (totalTime % 3600) / 60;
//				Long seconds = totalTime % 60;
//				String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
//		
//				//deviation 
//				long expectTime=32400;
//				long deviationTime=expectTime-totalTime;
//				String finalDevTion="";
//				if(deviationTime<0)
//				{
//					deviationTime = Math.abs((int)deviationTime);
//					finalDevTion="+";
//				}
//				else
//				{
//					finalDevTion="-";
//				}
//				
//				hours = deviationTime / 3600;
//				minutes = (deviationTime % 3600) / 60;
//				seconds = deviationTime % 60;
//				finalDevTion += String.format("%02d:%02d:%02d", hours, minutes, seconds);
//		
//				
//				
				
				
				
				
//			}


			
//
//			System.out.println("bioEmpIdList  ::  " + bioEmpIdSet);
//			System.out.println("bioEmpIdList  ::  " + EmplNameIdMap);

//			for (List list : bioMaxAllList) {
//				list.add(EmplNameIdMap.get(list.get(0)));
//
//				String startDate = list.get(1).toString();
//				String endDate = list.get(2).toString();
//				String totalTime = calculateTime(startDate, endDate);
//				list.add(totalTime);
//
//			}

			serviceResponse.setServiceResponse(finalEmpBioData);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();

			serviceResponse.setServiceResponse("");
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceError(e.getMessage());
		}

		return serviceResponse;
		
		
	}
	

	public ServiceResponse getEmpBioData360(String startdate, String endDate, String employeeId) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    Connection con = null;
	    PreparedStatement statement = null;
	    ResultSet resultSet = null;

	    try {
	        Set<String> bioEmpIdSet = new HashSet<>();
	        Map<String, List<String>> empMapById = new HashMap<>();
	        List<BioMax360> finalEmpBioData = new ArrayList();

	        String Query = "WITH LatestLogDate AS ("
	                + "    SELECT "
	                + "        dl.UserId, "
	                + "        MAX(dl.LogDate) AS LastLogDate "
	                + "    FROM "
	                + "        [SmartOfficedb].[dbo].[DeviceLogs_10_2024] dl "
	                + "    INNER JOIN "
	                + "        [SmartOfficedb].[dbo].[Employees] emp ON dl.UserId = emp.EmployeeCode "
	                + "    INNER JOIN "
	                + "        [SmartOfficedb].[dbo].[AttendanceLogs] adl ON emp.EmployeeId = adl.EmployeeId "
	                + "    WHERE "
	                + "        adl.AttendanceDateStr BETWEEN '" + startdate + "' AND '" + endDate + "' "
	                + "        AND emp.EmployeeCode IN ( '" + employeeId + "' )"
	                + "    GROUP BY "
	                + "        dl.UserId "
	                + ") "
	                + "SELECT "
	                + "   adl.AttendanceDateStr,"
	                + "     dl.LogDate, "
	                + "    emp.EmployeeCode, "
	                + "    emp.EmployeeName, "
	                + "    adl.TotalDuration, "
	                + "    s.ShiftName, "
	                + "    adl.BeginTime, "
	                + "    adl.EndTime, "
	                + "    adl.Status, "
	                + "    adl.PunchRecords, "
	                + "    adl.EarlyBy, "
	                + "    adl.LateBy, "
	                + "    adl.Duration, "
	                + "    adl.InTime, "
	                + "    adl.OutTime, "
	                + "    adl.ShiftDuration "
	                + "FROM "
	                + "    LatestLogDate lld "
	                + "INNER JOIN "
	                + "    [SmartOfficedb].[dbo].[DeviceLogs_10_2024] dl ON lld.UserId = dl.UserId AND lld.LastLogDate = dl.LogDate "
	                + "INNER JOIN "
	                + "    [SmartOfficedb].[dbo].[Employees] emp ON dl.UserId = emp.EmployeeCode "
	                + "INNER JOIN "
	                + "    [SmartOfficedb].[dbo].[AttendanceLogs] adl ON emp.EmployeeId = adl.EmployeeId "
	                + "INNER JOIN "
	                + "    [SmartOfficedb].[dbo].[Shifts] s ON adl.ShiftId = s.ShiftId "
	                + "WHERE "
	                + "    adl.AttendanceDateStr BETWEEN '" + startdate + "' AND '" + endDate + "' "
	                + "    AND emp.EmployeeCode IN( '" + employeeId + "' )"; // Direct values in the query
System.out.println("Query"+Query);
	        // Getting the database connection
	        con = getConnection();
	        statement = con.prepareStatement(Query);

	        // Execute the query and retrieve the result set
	        resultSet = statement.executeQuery();

	        // Process the result set
	        while (resultSet.next()) {
	            BioMax360 bioMaTO = new BioMax360();
	            bioMaTO.setAttendanceDateStr(resultSet.getString("AttendanceDateStr"));
	            bioMaTO.setLogDate(resultSet.getString("LogDate"));
	            bioMaTO.setEmployeeCode(resultSet.getString("EmployeeCode"));
	            bioMaTO.setEmployeeName(resultSet.getString("EmployeeName"));
	            bioMaTO.setTotalDuration(resultSet.getString("TotalDuration")); // Assuming TotalDuration is a String
	            bioMaTO.setShiftName(resultSet.getString("ShiftName")); // Added shift name from your query
	            bioMaTO.setBeginTime(resultSet.getString("BeginTime"));
	            bioMaTO.setEndTime(resultSet.getString("EndTime"));
	            bioMaTO.setStatus(resultSet.getString("Status"));
	            bioMaTO.setPunchRecords(resultSet.getString("PunchRecords"));
	            bioMaTO.setEarlyBy(resultSet.getString("EarlyBy"));
	            bioMaTO.setLateBy(resultSet.getString("LateBy"));
	            bioMaTO.setDuration(resultSet.getString("Duration")); // Assuming Duration is a String
	            bioMaTO.setInTime(resultSet.getString("InTime"));
	            bioMaTO.setOutTime(resultSet.getString("OutTime"));
	            bioMaTO.setShiftDuration(resultSet.getString("ShiftDuration")); 
	            TimesheetDTO timesheetdto=new TimesheetDTO();
	            timesheetdto.setTimesheetId(Long.parseLong("51058"));
	        	TimesheetDTO ob=new TimesheetDTO();
	        	ob.setClientId(1);
	        	ob.setProjectId(1);
	        	ob.setProjectName("Test");
	        	TimesheetDTO ob1=new TimesheetDTO();
	        	ob1.setClientId(1);
	        	ob1.setProjectId(1);
	        	ob1.setProjectName("Test2");
	        	List<TimesheetDTO> dto=new ArrayList<>();
	        	dto.add(ob);
	        	dto.add(ob1);
	        	bioMaTO.setTimesheetdto(dto);
	          //  bioMaTO.setTimesheetdto(TimesheetService.getAllProjectsByEmpIdForBioMax(Long.parseLong("51058")));
	            finalEmpBioData.add(bioMaTO);
	        }
	        serviceResponse.setServiceResponse(finalEmpBioData);
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);

	    } catch (Exception e) {
	        e.printStackTrace();

	        // Set the error in response
	        serviceResponse.setServiceResponse("");
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceError(e.getMessage());
	    } finally {
	        // Properly close the resources in the finally block
	        try {
	            if (resultSet != null) {
	                resultSet.close();
	            }
	            if (statement != null) {
	                statement.close();
	            }
	            if (con != null) {
	                con.close();
	            }
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	    }

	    return serviceResponse;
	}

	public long calculateTime(String startDateStr, String endDateStr) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		// Parse the strings into LocalDateTime objects
		LocalDateTime startDate = LocalDateTime.parse(startDateStr, formatter);
		LocalDateTime endDate = LocalDateTime.parse(endDateStr, formatter);

		// Calculate the time interval in minutes
		long totalSecs = Duration.between(startDate, endDate).toSeconds();

//		Long hours = totalSecs / 3600;
//		Long minutes = (totalSecs % 3600) / 60;
//		Long seconds = totalSecs % 60;
//		String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
//
//		return timeString;
		return totalSecs;
	}

	public ServiceResponse getEmpBioDataById(String empId, String date) {

		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			List<BioMaTO> empDataList = new ArrayList();

			String Query = "SELECT UserId,LogDate,DeviceName FROM device_logs WHERE	logdate like '" + date
					+ "%' and UserId = 'A" + empId + "' order by LogDate";

			Connection con = getConnection();
			PreparedStatement statement = con.prepareStatement(Query);
			ResultSet resultSet = statement.executeQuery();

			while (resultSet.next()) {
//				BioMaTO bioMaTO=new BioMaTO();
//				
//				bioMaTO.setEmpId(resultSet.getString(1).replace("A", "").trim());
//				bioMaTO.setEmploginTime(resultSet.getString(2));
//				bioMaTO.setEmpDeviceName(resultSet.getString(3));
//				empDataList.add(bioMaTO);

			}
			serviceResponse.setServiceResponse(empDataList);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceResponse("");
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceError(e.getMessage());
		}

		return serviceResponse;
	}
	
	
	
	
//	public int getNumberOfTheMonth(String month) {
//	    int num = 0;
//
//	    switch (month) {
//	        case "January":
//	            num = 1;
//	            break;
//
//	        case "February":
//	            num = 2;
//	            break;
//
//	        case "March":
//	            num = 3;
//	            break;
//
//	        case "April":
//	            num = 4;
//	            break;
//
//	        case "May":
//	            num = 5;
//	            break;
//
//	        case "June":
//	            num = 6;
//	            break;
//
//	        case "July":
//	            num = 7;
//	            break;
//
//	        case "August":
//	            num = 8;
//	            break;
//
//	        case "September":
//	            num = 9;
//	            break;
//
//	        case "October":
//	            num = 10;
//	            break;
//
//	        case "November":
//	            num = 11;
//	            break;
//
//	        case "December":
//	            num = 12;
//	            break;
//
//	        default:
//	            num = -1; // Indicate invalid input
//	            break;
//	    }
//
//	    return num;
//	}
//
//	
//	
//	
//	
	 public List<BioMaTO> getBioInOut() {
		    List<BioMaTO> finalEmpBioData = new ArrayList<>();
		    
		    try {
		        Set<String> bioEmpIdSet = new HashSet<>();
		        Map<String, List<String>> empMapById = new HashMap<>();
		        
		        String query = "SELECT " +
		            "adl.AttendanceDate, " +
		            "emp.EmployeeCode, " +
		            "emp.EmployeeName, " +
		            "adl.TotalDuration, " +
		            "s.ShiftName, " +
		            "adl.BeginTime, " +
		            "adl.EndTime, " +
		            "adl.Status, " +
		            "adl.PunchRecords, " +
		            "adl.EarlyBy, " +
		            "adl.LateBy, " +
		            "adl.Duration, " +
		            "adl.InTime, " +
		            "adl.OutTime, " +
		            "adl.ShiftDuration " +
		            "FROM [SmartOfficedb].[dbo].[AttendanceLogs] adl " +
		            "INNER JOIN [SmartOfficedb].[dbo].[Employees] emp ON adl.EmployeeId = emp.EmployeeId " +
		            "INNER JOIN [SmartOfficedb].[dbo].[Shifts] s ON adl.ShiftId = s.ShiftId " +
		            "WHERE YEAR(adl.AttendanceDate) = ? AND MONTH(adl.AttendanceDate) = ?;";
		        
		        
		        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

				 Connection con = getConnection();

		        PreparedStatement statement = con.prepareStatement(query);
		       
		        // Set the date parameters
		        statement.setString(1, String.valueOf(2024)); // Set the year
		        statement.setString(2, String.valueOf(9));    // Set the month
		        
		        System.out.println("Executing query: " + statement.toString());
		        
		        ResultSet resultSet = statement.executeQuery();
		        
		        if (!resultSet.next()) {
		            System.out.println("No results found.");
		        } else {
		        	 while (resultSet.next()){
		                BioMaTO bioMaTO = new BioMaTO();
		                
		                System.out.println(resultSet.getString("EmployeeCode"));
		                System.out.println(resultSet.getString("ShiftName"));
		                System.out.println(resultSet.getString("EmployeeName"));
		                System.out.println(resultSet.getString("TotalDuration"));
		               
		                // Populate bioMaTO object
		                bioMaTO.setEmployeeCode(resultSet.getString("EmployeeCode"));
		                bioMaTO.setEmployeeName(resultSet.getString("EmployeeName"));
		                bioMaTO.setTotalDuration(resultSet.getString("TotalDuration"));
		                bioMaTO.setShiftName(resultSet.getString("ShiftName"));
		                bioMaTO.setBeginTime(resultSet.getString("BeginTime"));
		                bioMaTO.setEndTime(resultSet.getString("EndTime"));
		                bioMaTO.setStatus(resultSet.getString("Status"));
		                bioMaTO.setPunchRecords(resultSet.getString("PunchRecords"));
		                bioMaTO.setEarlyBy(resultSet.getString("EarlyBy"));
		                bioMaTO.setLateBy(resultSet.getString("LateBy"));
		                bioMaTO.setDuration(resultSet.getString("Duration"));
		                bioMaTO.setInTime(resultSet.getString("InTime"));
		                bioMaTO.setOutTime(resultSet.getString("OutTime"));
		                bioMaTO.setShiftDuration(resultSet.getString("ShiftDuration"));

		                finalEmpBioData.add(bioMaTO);
		                
		            }
		            
		        }
		        
		    } catch (Exception e) {
		        e.printStackTrace(); // Log the exception message
		    }
		    
		    return finalEmpBioData;
		}
		
	


} 