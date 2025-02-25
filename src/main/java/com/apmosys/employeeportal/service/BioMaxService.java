package com.apmosys.employeeportal.service;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.apmosys.employeeportal.dto.BioMaTO;
import com.apmosys.employeeportal.dto.BioMax360;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.Employee;
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
	
	@PersistenceContext
    private EntityManager entityManager;

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
	public ServiceResponse getEmpBioData(String startDate, String endDate) throws SQLException {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
//			Set<String> bioEmpIdSet = new HashSet<>();
			Map<String, List<String>> empMapById = new HashMap<>();
			List<BioMaTO> finalEmpBioData=new ArrayList();
			List<BioMaTO> tempBioDataList = new ArrayList<>();
//			String Query = "WITH LatestLogDate AS ("
//                    + "    SELECT "
//                    + "        dl.UserId, "
//                    + "        MAX(dl.LogDate) AS LastLogDate "
//                    + "    FROM "
//                    + "        [SmartOfficedb].[dbo].[DeviceLogs_10_2024] dl "
//                    + "    INNER JOIN "
//                    + "        [SmartOfficedb].[dbo].[Employees] emp ON dl.UserId = emp.EmployeeCode "
//                    + "    INNER JOIN "
//                    + "        [SmartOfficedb].[dbo].[AttendanceLogs] adl ON emp.EmployeeId = adl.EmployeeId "
//                    + "    WHERE "
//                    + "        adl.AttendanceDateStr >= ? AND adl.AttendanceDateStr <= ? "
//                    + "    GROUP BY "
//                    + "        dl.UserId "
//                    + ") "
//                    + "SELECT "
//                    + "    adl.AttendanceDateStr, "
//                    + "    dl.LogDate, "
//                    + "    emp.EmployeeCode, "
//                    + "    emp.EmployeeName, "
//                    + "    adl.TotalDuration, "
//                    + "    s.ShiftName, "
//                    + "    adl.BeginTime, "
//                    + "    adl.EndTime, "
//                    + "    adl.Status, "
//                    + "    adl.PunchRecords, "
//                    + "    adl.EarlyBy, "
//                    + "    adl.LateBy, "
//                    + "    adl.Duration, "
//                    + "    adl.InTime, "
//                    + "    adl.OutTime, "
//                    + "    adl.ShiftDuration "
//                    + "FROM "
//                    + "    LatestLogDate lld "
//                    + "INNER JOIN "
//                    + "    [SmartOfficedb].[dbo].[DeviceLogs_10_2024] dl ON lld.UserId = dl.UserId AND lld.LastLogDate = dl.LogDate "
//                    + "INNER JOIN "
//                    + "    [SmartOfficedb].[dbo].[Employees] emp ON dl.UserId = emp.EmployeeCode "
//                    + "INNER JOIN "
//                    + "    [SmartOfficedb].[dbo].[AttendanceLogs] adl ON emp.EmployeeId = adl.EmployeeId "
//                    + "INNER JOIN "
//                    + "    [SmartOfficedb].[dbo].[Shifts] s ON adl.ShiftId = s.ShiftId "
//                    + "WHERE "
//                    + "    adl.AttendanceDateStr >= ? AND adl.AttendanceDateStr <= ? ";
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
		            + "        CAST(adl.AttendanceDateStr AS DATE) >= ? AND CAST(adl.AttendanceDateStr AS DATE) <= ? "
		            + "    GROUP BY "
		            + "        dl.UserId "
		            + ") "
		            + "SELECT "
		            + "    FORMAT(CAST(adl.AttendanceDateStr AS DATE), 'dd-MMM-yyyy') AS AttendanceDateStr, "
		            + "    dl.LogDate, "
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
		            + "    CAST(adl.AttendanceDateStr AS DATE) >= ? AND CAST(adl.AttendanceDateStr AS DATE) <= ?";

//			String Query = "SELECT * " +
//		               "FROM AttendanceLogs al " +
//		               "INNER JOIN Employees e ON al.EmployeeId = e.EmployeeId " +
//		               "WHERE al.AttendanceDateStr >= ? " +
//		               "AND al.AttendanceDateStr <= ?";


			Connection con = getConnection();
			PreparedStatement statement = con.prepareStatement(Query);
			
			// Set the date parameter
		    statement.setString(1, startDate); // Set the first placeholder
		    statement.setString(2, endDate); // Set the second placeholder
		    statement.setString(3, startDate);
		    statement.setString(4, endDate);
			
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

			    tempBioDataList.add(bioMaTO);
			} 	    
				
			for (BioMaTO bioMaTO : tempBioDataList) {
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
//		finally {
//			con.close();
//		}

		return serviceResponse;
		
		
	}
	

	 public ServiceResponse getEmpBioData360(String startDate, String endDate, String employeeId) {
	        ServiceResponse serviceResponse = new ServiceResponse();
	        List<BioMax360> finalEmpBioData = new ArrayList<>();

	        String query = "SELECT AttendanceDate, al.EmployeeId AS EmployeeId, AttendanceDateStr, EmployeeName, EmployeeCode, InTime, OutTime, OverTime "
	                + "FROM AttendanceLogs al "
	                + "JOIN Employees e ON al.EmployeeId = e.EmployeeId "
	                + "WHERE e.EmployeeCode IN (?) "
	                + "AND al.AttendanceDate BETWEEN ? AND ?";

	        try (Connection con = getConnection();
	             PreparedStatement statement = con.prepareStatement(query)) {

	            statement.setString(1, employeeId);
	            statement.setString(2, startDate);
	            statement.setString(3, endDate);

	            try (ResultSet resultSet = statement.executeQuery()) {
	                while (resultSet.next()) {
	                    BioMax360 bioMaTO = new BioMax360();
	                    bioMaTO.setAttendanceDateStr(resultSet.getString("AttendanceDate"));
	                    bioMaTO.setLogDate(resultSet.getString("AttendanceDateStr"));
	                    bioMaTO.setEmployeeCode(resultSet.getString("EmployeeCode"));
	                    bioMaTO.setEmployeeName(resultSet.getString("EmployeeName"));
	                    bioMaTO.setTotalDuration(resultSet.getString("OverTime"));
	                    bioMaTO.setShiftName(resultSet.getString("OverTime"));
	                    bioMaTO.setBeginTime(resultSet.getString("InTime"));
	                    bioMaTO.setEndTime(resultSet.getString("OutTime"));
	                    bioMaTO.setStatus(resultSet.getString("EmployeeId"));

	                    SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MMM-yyyy");
	                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
	                    String outputDate;

	                    try {
	                        Date date = inputFormat.parse(resultSet.getString("AttendanceDateStr"));
	                        outputDate = outputFormat.format(date);
	                    } catch (ParseException e) {
	                        outputDate = resultSet.getString("AttendanceDateStr");
	                    }

	                    List<TimesheetDTO> timesh = TimesheetService.getAllProjectsByEmpIdForBioMax(resultSet.getString("EmployeeId"), outputDate);
	                    if (timesh.isEmpty()) {
	                        TimesheetDTO timesheetDTO = new TimesheetDTO();
	                        timesheetDTO.setClientName("Not Fill");
	                        timesheetDTO.setActivity("0");
	                        timesh.add(timesheetDTO);
	                    }
	                    bioMaTO.setTimesheetdto(timesh);

	                    finalEmpBioData.add(bioMaTO);
	                }
	            }
	            serviceResponse.setServiceResponse(finalEmpBioData);
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);

	        } catch (Exception e) {
	            serviceResponse.setServiceResponse("");
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError(e.getMessage());
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
	 
	 
	 public ServiceResponse getBioOverTimeandState(EmployeeDTO employeedto) {
		    HashMap<String, Object> res = new HashMap<>();
		    HashMap<String, Object> stats = new HashMap<>();
			ServiceResponse serviceResponse = new ServiceResponse();
		    ArrayList<HashMap<String, Object>> AllDataList =new   ArrayList<HashMap<String, Object>>();
		    
		    Long empId = employeedto.getEmpId();

		    
		    String strMYSQLQuery = "SELECT " +
	                "(SELECT COUNT(emp_id) " +
	                " FROM employee_leave " +
	                " WHERE emp_id = :empId " +
	                "   AND MONTH(created_on) = MONTH(CURRENT_DATE - INTERVAL 1 MONTH) " +
	                "   AND YEAR(created_on) = YEAR(CURRENT_DATE - INTERVAL 1 MONTH)) AS leave_count, " +

	                "(SELECT COUNT(reward_id) " +
	                " FROM employee_rewards " +
	                " WHERE created_by = :empId " +
	                "   AND MONTH(created_on) = MONTH(CURRENT_DATE - INTERVAL 1 MONTH) " +
	                "   AND YEAR(created_on) = YEAR(CURRENT_DATE - INTERVAL 1 MONTH)) AS rewards_count, " +

	                "(SELECT COUNT(id) " +
	                " FROM db_emp_portal.appreciation " +
	                " WHERE appreciation_to = :empId " +
	                "   AND MONTH(appreciation_date) = MONTH(CURRENT_DATE - INTERVAL 1 MONTH) " +
	                "   AND YEAR(appreciation_date) = YEAR(CURRENT_DATE - INTERVAL 1 MONTH)) AS appreciation_count";

	        // Execute the Query
	        Query query = entityManager.createNativeQuery(strMYSQLQuery);
	        query.setParameter("empId", empId);

	        // Fetch the result
	        Object[] result = (Object[]) query.getSingleResult();

	        // Map the results
	        
	        stats.put("leave_count",   result[0]);
	        stats.put("rewards_count", result[1]);
	        stats.put("appreciation_count",  result[2]);

	       
	        System.out.println("data  --===="+ stats);
	        

		    try {
		        // Fetch all employees from the repository
		        List<Employee> AllEmpList = employeeRepository.findAll();

		        // Loop through each employee and process
		        AllEmpList.forEach(emp -> {
		            if (emp.getEmpId().equals(employeedto.getEmpId())) {
		                // SQL query to fetch total overtime and other relevant data
		            	
		            	String EmploymentId = "A" + emp.getEmployeementId();
		            	
		            	
		            	String strMSSQLQuery = "SELECT "
		            	        + "SUM(CAST(AttendanceLogs.OverTime AS INT)) AS total_OverTime, "
		            	        + "CONCAT('-', ABS(SUM(CAST(AttendanceLogs.OverTimeE AS INT)))) AS total_UnderTimeE "
		            	        + "FROM AttendanceLogs "
		            	        + "INNER JOIN Employees ON AttendanceLogs.EmployeeId = Employees.EmployeeId "
		            	        + "WHERE Employees.EmployeeCode = ? "
		            	        + "AND MONTH(AttendanceLogs.AttendanceDate) = MONTH(DATEADD(MONTH, -1, GETDATE())) "
		            	        + "AND YEAR(AttendanceLogs.AttendanceDate) = YEAR(DATEADD(MONTH, -1, GETDATE()))";



		                try {
		                    // Load the SQL Server JDBC driver
		                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

		                    // Establish connection
		                    Connection con = getConnection();

		                    // Prepare the SQL statement
		                    PreparedStatement statement = con.prepareStatement(strMSSQLQuery);
		                    
		                    
		                    statement.setString(1, String.valueOf(EmploymentId)); 

		                    // Execute the query
		                    ResultSet resultSet = statement.executeQuery();

		                    // Process the result set
		                    if (resultSet.next()) {
		                        int totalOverTime = resultSet.getInt("total_OverTime");
		                        String totalOverTimeE = resultSet.getString("total_UnderTimeE");

		                        // Add results to the response
		                        res.put("totalOverTime", totalOverTime);
		                        res.put("totalOverTimeE", totalOverTimeE);
		                    }

		                    // Close resources
		                    resultSet.close();
		                    statement.close();
		                    con.close();
		                } catch (ClassNotFoundException | SQLException e) {
		                    e.printStackTrace();
		                }
		                
		                
		            }
		        });
		        
		        System.out.println("data res --===="+ res);

		    } catch (Exception e) {
		        e.printStackTrace();
		    }

		    
		    
		    AllDataList.add(res);
		    AllDataList.add(stats);
		  
		    serviceResponse.setServiceResponse(AllDataList);
		    serviceResponse.setServiceMessage("Success");
		    serviceResponse.setServiceError("");
	
		    return serviceResponse; 
		}
	 
	 

	 
	
	   
	   
	   
	 
		
	


} 