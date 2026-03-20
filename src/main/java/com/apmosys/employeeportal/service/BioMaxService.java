package com.apmosys.employeeportal.service;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.apmosys.employeeportal.model.BiomaxRequest;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Holiday;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.PortalConfig;
import com.apmosys.employeeportal.repository.BiomaxRequestRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.HolidayRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.PortalConfigRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class BioMaxService {
	@Autowired
	TimesheetService TimesheetService;
	@Value("${BioDbIp}")
	public String DBIp;
	@Value("${biomaxleavedeductForEmployee}")
	private String biomaxleavedeductForEmployee;
	@Value("${biomaxleavedeductForDepartment}")
	private String biomaxleavedeductForDepartment;
	
	@Autowired
	private BiomaxRequestRepository biomaxRequestRepository;
	
	@Autowired
	private HolidayRepository holidayRepository;
	
	Connection con = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
	
	@Autowired
	PortalConfigRepository portalConfigRepository;
	@Autowired
	private JobRoleRepository jobRoleRepository;
	
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

// 	public Connection getConnection() {
		
		

// 		String DB_URL = "jdbc:sqlserver://" + DBIp + "/" + DBName;

// 		try {
			
// 			 Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		
// //			Connection con = DriverManager.getConnection("jdbc:sqlserver://yourserver:1433;databaseName=yourdatabase;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");
// //			Connection con = DriverManager.getConnection("jdbc:sqlserver://yourserver:1433;databaseName=yourdatabase;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");
// //			 Connection con = DriverManager.getConnection("jdbc:sqlserver://192.168.0.126:1433;databaseName=SmartOfficedb;user=sa;password=biomax;");
// 			  con = DriverManager.getConnection("jdbc:sqlserver://192.168.0.126:1433;databaseName=SmartOfficedb;encrypt=true;trustServerCertificate=true;user=apmosys;password=apmosys@123;");

			 
// 			 if (con !=null) {
// 				System.out.println("Connection made");
// 			}
// 			return con;

// 		} catch (Exception e) {
// 			e.printStackTrace();
// 		}

// 		return null;
// 	}

	public Connection getConnection() {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			
			con = DriverManager.getConnection(
				"jdbc:sqlserver://localhost:1433;databaseName=SmartOfficedb;encrypt=true;trustServerCertificate=true;",
				"apmosys",
				"apmosys@123"
			);
			
			if (con != null) {
				System.out.println("Connection made successfully!");
			}
			return con;
			
		} catch (Exception e) {
			System.out.println("Connection failed: " + e.getMessage());
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

	public ServiceResponse getEmpBioDataFromIshine(String startDate, String endDate, 
                                                Integer pageNumber, Integer pageSize) throws SQLException {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			if (pageNumber == null || pageNumber < 1) pageNumber = 1;
			if (pageSize == null || pageSize < 1) pageSize = 100;
			
			int offset = (pageNumber - 1) * pageSize;
			
			List<BioMaTO> finalEmpBioData = new ArrayList<>();
			
			Connection con = getConnection();
			
			// First, get total count
			String countQuery = "SELECT COUNT(*) AS TotalRecords FROM " +
				"( " +
				"    SELECT Empcode, EmpName, CAST(Logdatetime AS DATE) AS AttendanceDate " +
				"    FROM IshineRawdata " +
				"    WHERE Logdatetime >= ? AND Logdatetime <= ? " +
				"    GROUP BY Empcode, EmpName, CAST(Logdatetime AS DATE) " +
				") A";
			
			PreparedStatement countStmt = con.prepareStatement(countQuery);
			countStmt.setString(1, startDate + " 00:00:00");
			countStmt.setString(2, endDate + " 23:59:59");
			ResultSet countRs = countStmt.executeQuery();
			
			int totalRecords = 0;
			if (countRs.next()) {
				totalRecords = countRs.getInt("TotalRecords");
			}
			countRs.close();
			countStmt.close();
			
			// Get paginated data - WITHOUT Direction filter
			String query = "SELECT " +
				"    Empcode, " +
				"    EmpName, " +
				"    FORMAT(AttendanceDate,'dd-MM-yyyy') AS AttendanceDate, " +
				"    FORMAT(InTime,'hh:mm tt') AS InTime, " +
				"    FORMAT(OutTime,'hh:mm tt') AS OutTime, " +
				"    FORMAT(DATEADD(MINUTE, DATEDIFF(MINUTE, InTime, OutTime), 0),'HH:mm') AS TotalWorkingHours " +
				"FROM " +
				"( " +
				"    SELECT " +
				"        Empcode, " +
				"        EmpName, " +
				"        CAST(Logdatetime AS DATE) AS AttendanceDate, " +
				"        MIN(Logdatetime) AS InTime, " +
				"        MAX(Logdatetime) AS OutTime " +
				"    FROM IshineRawdata " +
				"    WHERE Logdatetime >= ? AND Logdatetime <= ? " +
				"    GROUP BY Empcode, EmpName, CAST(Logdatetime AS DATE) " +
				") A " +
				"ORDER BY Empcode, AttendanceDate " +
				"OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

			PreparedStatement statement = con.prepareStatement(query);
			statement.setString(1, startDate + " 00:00:00");
			statement.setString(2, endDate + " 23:59:59");
			statement.setInt(3, offset);
			statement.setInt(4, pageSize);

			ResultSet resultSet = statement.executeQuery();

			while (resultSet.next()) {
				BioMaTO bioMaTO = new BioMaTO();
				bioMaTO.setEmployeeCode(resultSet.getString("Empcode"));
				bioMaTO.setEmployeeName(resultSet.getString("EmpName"));
				bioMaTO.setLogDate(resultSet.getString("AttendanceDate"));
				bioMaTO.setInTime(resultSet.getString("InTime"));
				bioMaTO.setOutTime(resultSet.getString("OutTime"));
				bioMaTO.setTotalDuration(resultSet.getString("TotalWorkingHours"));
				
				finalEmpBioData.add(bioMaTO);
			}

			finalEmpBioData = mapEmployeeDetails(finalEmpBioData);

			resultSet.close();
			statement.close();
			con.close();

			Map<String, Object> responseData = new HashMap<>();
			responseData.put("data", finalEmpBioData);
			responseData.put("totalRecords", totalRecords);
			responseData.put("currentPage", pageNumber);
			responseData.put("pageSize", pageSize);
			responseData.put("totalPages", (int) Math.ceil((double) totalRecords / pageSize));

			serviceResponse.setServiceResponse(responseData);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceResponse("");
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceError(e.getMessage());
		}

		return serviceResponse;
	}

	 public ServiceResponse getEmpBioData360(String startDate, String endDate, List<String> employeeId) {
	        ServiceResponse serviceResponse = new ServiceResponse();
	        List<BioMax360> finalEmpBioData = new ArrayList<>();
	       
	        	for(String emp:employeeId)
	        {
	           
	        
	        String query = "SELECT AttendanceDate, al.EmployeeId AS EmployeeId, AttendanceDateStr, EmployeeName, EmployeeCode, InTime, OutTime, OverTime "
	                + "FROM AttendanceLogs al "
	                + "JOIN Employees e ON al.EmployeeId = e.EmployeeId "
	                + "WHERE e.EmployeeCode IN (?) "
	                + "AND al.AttendanceDate BETWEEN ? AND ?";

	        try (Connection con = getConnection();
	             PreparedStatement statement = con.prepareStatement(query)) {

	            statement.setString(1, emp.toString());
	            statement.setString(2, startDate + " 00:00:00");
	            statement.setString(3, endDate + " 23:59:59");

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
	                    Employee employee=employeeRepository.findByEmployeementId(Long.parseLong(resultSet.getString("EmployeeCode").replaceAll("A", "")));
	                    List<TimesheetDTO> timesh = TimesheetService.getAllProjectsByEmpIdForBioMax(employee.getEmpId(), outputDate);
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
	 
	 
	 public List<BioMaTO> getBiomaxDataForLeaveDeduct() {
		 
		 String depart="";
			String employee="";
			  List<BioMaTO> finalEmpBioData = new ArrayList<>();
			List<Holiday>  holiday= holidayRepository.currentDayHoliday();
			 if(holiday.size()==0) {
					
		    try {
		        String Query = "SELECT \n"
		        		+ "    e.EmployeeCode,  \n"
		        		+ "    al.AttendanceDate, \n"
		        		+ "    al.InTime, \n"
		        		+ "    al.OutTime, \n"
		        		+ "    al.TotalDuration, \n"
		        		+ "    s.ShiftName, \n"
		        		+ "    s.BeginTime, \n"
		        		+ "    s.EndTime,\n"
		        		+ "    CASE \n"
		        		+ "        WHEN al.TotalDuration < 540 AND al.InTime > DATEADD(MINUTE, 30, CAST(s.BeginTime AS DATETIME)) THEN 1\n"
		        		+ "        WHEN al.TotalDuration < 540 THEN 0.5\n"
		        		+ "        WHEN al.InTime > DATEADD(MINUTE, 30, CAST(s.BeginTime AS DATETIME)) THEN 0.5\n"
		        		+ "        ELSE 0\n"
		        		+ "    END AS Deduct\n"
		        		+ "FROM \n"
		        		+ "    [SmartOfficedb].[dbo].[AttendanceLogs] al\n"
		        		+ "INNER JOIN \n"
		        		+ "    [SmartOfficedb].[dbo].[Employees] e ON al.EmployeeId = e.EmployeeId\n"
		        		+ "INNER JOIN \n"
		        		+ "    [SmartOfficedb].[dbo].[Shifts] s ON s.ShiftId = al.ShiftId\n"
		        		+ "WHERE \n"
		        		+ "    al.AttendanceDateStr = ? \n"
		        		+ "    and s.ShiftName != 'NoShift' \n"
		        		+ "ORDER BY \n"
		        		+ "    al.AttendanceDateStr DESC;";
//		    
		        
		        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

				 Connection con = getConnection();

		        PreparedStatement statement = con.prepareStatement(Query);
		        LocalDate currentDate = LocalDate.now();
		        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
		        String currentDateString = currentDate.format(formatter);
		       
		        // Set the date parameters
		        statement.setString(1, currentDateString);

		        ResultSet resultSet = statement.executeQuery();
		        
		        if (!resultSet.next()) {
		            System.out.println("No results found.");
		        } else {
		        	 while (resultSet.next()){
		        		 BioMaTO bioMaTO = new BioMaTO();
			             Long employmentId = Long.parseLong(resultSet.getString("EmployeeCode").replaceAll("\\D", ""));
			             Employee employee1=employeeRepository.findByEmployeementId(employmentId);
						 if(employee1!=null) {
						bioMaTO.setEmployementId(employmentId);
					
		               	if (employee1.getJobRoleId() != null) {
		               	    JobRole departmentjon = jobRoleRepository.findByjobRoleId(employee1.getJobRoleId());
		               	    bioMaTO.setDepartmentId(departmentjon.getDeptId());
		               	} else {
		               	    throw new NullPointerException("JobRoleId is null, cannot proceed with the operation.");
		               	}

						bioMaTO.setEmployeeCode(resultSet.getString("EmployeeCode"));
		                bioMaTO.setAttendanceDate(resultSet.getString("AttendanceDate"));
		                bioMaTO.setInTime(resultSet.getString("InTime"));
		                bioMaTO.setOutTime(resultSet.getString("OutTime"));
		                bioMaTO.setTotalDuration(resultSet.getString("TotalDuration"));
		                bioMaTO.setShiftName(resultSet.getString("ShiftName"));
		                bioMaTO.setBeginTime(resultSet.getString("BeginTime"));
		                bioMaTO.setEndTime(resultSet.getString("EndTime"));
		                bioMaTO.setDeduct(resultSet.getString("Deduct"));
		             
		                finalEmpBioData.add(bioMaTO);
							}
		            }
		            
		        }
		        
		        
		    } catch (Exception e) {
		        e.printStackTrace(); // Log the exception message
		    }
			 }
			 
			 return finalEmpBioData;
		}
	 
	 
	 public List<BioMaTO> listFilter(List<BioMaTO> list1, List<Long> removedId) {
		    // Handle null inputs
		 System.out.println("List Size"+list1.size());
		    if (list1 == null || removedId == null) {
		        return new ArrayList<>(); // Return an empty list if inputs are null
		    }

		    // Convert removedId to a Set for faster lookups
		  //  Set<Long> removedIdSet = new HashSet<>(removedId);
		    
		    Set<Long> removedIdSet = removedId.stream()
		    	    .filter(Objects::nonNull) // Remove null values
		    	    .collect(Collectors.toSet());
		    // Filter the list
		    List<BioMaTO> newList = new ArrayList<>();
		    for (BioMaTO item : list1) {
		    	System.out.println("EmpId="+item.getEmpId());
		        if (item != null && !removedIdSet.contains(item.getEmployementId().toString())) {
		            newList.add(item);
		        }
		    	
		    }
		    System.out.println("newList Size"+newList.size());
			
		    return newList;
		}

	private List<BioMaTO> mapEmployeeDetails(List<BioMaTO> bioMaxTOList) {
		List<String> allEmployeeIds = bioMaxTOList.stream().map(e -> e.getEmployeeCode()).collect(Collectors.toList());
		List<Object[]> employees = employeeRepository.findByPrefixedEmployeementIdIn(allEmployeeIds);
		Map<String, Map<String, String>> employeeMap = new HashMap<>();
		
		for(Object[] employee : employees) {
			String employeeId = employee[4] != null ? employee[4].toString() : null;

			if(employeeId == null ){
				throw new NullPointerException("Employee Id is Null");
			}

			Map<String, String> employeeDetails = new HashMap<>();

			String employeeName = employee[1] != null ? employee[1].toString() : "N/A";
			String reportingManager = employee[2] != null ? employee[2].toString() : "N/A";
			String departmentName = employee[3] != null ? employee[3].toString() : "N/A";

			employeeDetails.put("reportingManager", reportingManager);
			employeeDetails.put("employeeName", employeeName);
			employeeDetails.put("departmentName", departmentName);

			employeeMap.put(employeeId, employeeDetails);
		}

		for(BioMaTO bioMaTO : bioMaxTOList) {
			String employeeId = bioMaTO.getEmployeeCode();
			Map<String, String> employeeDetails = employeeMap.get(employeeId);
			if(employeeDetails!=null) {
				String reportingManager = employeeDetails.get("reportingManager");
				bioMaTO.setReportingManagerName(reportingManager);
				bioMaTO.setEmployeeName(employeeDetails.get("employeeName"));
				bioMaTO.setDepartmentName(employeeDetails.get("departmentName"));
			} else {
				bioMaTO.setReportingManagerName("N/A");
				bioMaTO.setEmployeeName("N/A");
				bioMaTO.setDepartmentName("N/A");
			}
		}

		return bioMaxTOList;
	}

} 