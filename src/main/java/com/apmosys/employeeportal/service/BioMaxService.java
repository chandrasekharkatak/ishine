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
import java.time.format.DateTimeParseException;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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

	private volatile Connection cachedConnection = null;
	private volatile LocalDateTime lastConnectionUseTime = null;
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private static final int CONNECTION_TIMEOUT_MINUTES = 5;

	@PostConstruct
	public void init() {
		// Schedule a task to check and close idle connections every minute
		scheduler.scheduleAtFixedRate(() -> {
			try {
				if (cachedConnection != null && lastConnectionUseTime != null) {
					LocalDateTime now = LocalDateTime.now();
					if (Duration.between(lastConnectionUseTime, now).toMinutes() >= CONNECTION_TIMEOUT_MINUTES) {
						closeConnection();
						System.out.println("Connection closed due to inactivity after " + CONNECTION_TIMEOUT_MINUTES + " minutes");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, 1, 1, TimeUnit.MINUTES);
	}

	@PreDestroy
	public void destroy() {
		closeConnection();
		scheduler.shutdown();
	}

	private synchronized Connection getCachedConnection() throws SQLException {
		try {
			// Check if existing connection is valid
			if (cachedConnection != null && !cachedConnection.isClosed()) {
				lastConnectionUseTime = LocalDateTime.now();
				System.out.println("Reusing existing connection");
				return cachedConnection;
			}
			
			// Close old connection if exists
			closeConnection();
			
			// Create new connection
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			cachedConnection = DriverManager.getConnection(
				"jdbc:sqlserver://192.168.0.126:1433;databaseName=SmartOfficedb;encrypt=true;trustServerCertificate=true;",
				"apmosys",
				"apmosys@123"
			);
			
			lastConnectionUseTime = LocalDateTime.now();
			System.out.println("New connection created successfully!");
			return cachedConnection;
			
		} catch (ClassNotFoundException e) {
			throw new SQLException("SQL Server JDBC Driver not found", e);
		} catch (SQLException e) {
			closeConnection();
			throw e;
		}
	}

	private synchronized void closeConnection() {
		try {
			if (cachedConnection != null && !cachedConnection.isClosed()) {
				cachedConnection.close();
				System.out.println("Connection closed");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			cachedConnection = null;
			lastConnectionUseTime = null;
		}
	}

	// Add this method to manually reset connection if needed
	public synchronized void resetConnection() {
		closeConnection();
	}

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
	public Connection getConnection() throws SQLException {
		return getCachedConnection();
	}

	// public Connection getConnection() {
	// 	try {
	// 		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			
	// 		con = DriverManager.getConnection(
	// 			"jdbc:sqlserver://192.168.0.126:1433;databaseName=SmartOfficedb;encrypt=true;trustServerCertificate=true;",
	// 			"apmosys",
	// 			"apmosys@123"
	// 		);
			
	// 		if (con != null) {
	// 			System.out.println("Connection made successfully!");
	// 		}
	// 		return con;
			
	// 	} catch (Exception e) {
	// 		System.out.println("Connection failed: " + e.getMessage());
	// 		e.printStackTrace();
	// 	}
	// 	return null;
	// }

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
	            	statement.close();
	            }
	            if (statement != null) {
	                statement.close();
	            }
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	    }
		
		
		return timesheetId;
				
	}

	public ServiceResponse getEmpBioDataFromIshine(String startDate, String endDate, 
                                                Integer pageNumber, Integer pageSize) throws SQLException {
		return getEmpBioDataFromIshine(startDate, endDate, pageNumber, pageSize, new HashMap<>());
	}

	public ServiceResponse getBioDashboardData(String startDate, String endDate) {
		ServiceResponse serviceResponse = new ServiceResponse();
		Map<String, Object> responseData = new HashMap<>();

		try (Connection con = getConnection()) {
			LocalDate startLocalDate = parseDashboardDate(startDate);
			LocalDate endLocalDate = parseDashboardDate(endDate);

			long totalEmployees = Optional.ofNullable(employeeRepository.getTotalEmployeeCount()).orElse(0L);

			// Present count for selected end date
			long presentToday = 0L;
			String presentTodayQuery = "SELECT COUNT(DISTINCT Empcode) AS presentCount "
					+ "FROM IshineRawdata "
					+ "WHERE CAST(Logdatetime AS DATE) = ?";
			try (PreparedStatement presentStmt = con.prepareStatement(presentTodayQuery)) {
				presentStmt.setDate(1, java.sql.Date.valueOf(endLocalDate));
				try (ResultSet rs = presentStmt.executeQuery()) {
					if (rs.next()) {
						presentToday = rs.getLong("presentCount");
					}
				}
			}

			// Late arrivals on selected day (first punch after 10:00 AM)
			long lateArrivals = 0L;
			String lateArrivalsQuery = "SELECT COUNT(*) AS lateCount FROM ( "
					+ "SELECT Empcode, MIN(Logdatetime) AS InTime "
					+ "FROM IshineRawdata "
					+ "WHERE CAST(Logdatetime AS DATE) = ? "
					+ "GROUP BY Empcode "
					+ ") A WHERE CAST(A.InTime AS TIME) > '10:00:00'";
			try (PreparedStatement lateStmt = con.prepareStatement(lateArrivalsQuery)) {
				lateStmt.setDate(1, java.sql.Date.valueOf(endLocalDate));
				try (ResultSet rs = lateStmt.executeQuery()) {
					if (rs.next()) {
						lateArrivals = rs.getLong("lateCount");
					}
				}
			}

			// Employees with less than 9 hours on selected day
			long underNineHours = 0L;
			String underNineQuery = "SELECT COUNT(*) AS underNineCount FROM ( "
					+ "SELECT Empcode, DATEDIFF(MINUTE, MIN(Logdatetime), MAX(Logdatetime)) AS workedMinutes "
					+ "FROM IshineRawdata "
					+ "WHERE CAST(Logdatetime AS DATE) = ? "
					+ "GROUP BY Empcode "
					+ ") A WHERE A.workedMinutes > 0 AND A.workedMinutes < 540";
			try (PreparedStatement underNineStmt = con.prepareStatement(underNineQuery)) {
				underNineStmt.setDate(1, java.sql.Date.valueOf(endLocalDate));
				try (ResultSet rs = underNineStmt.executeQuery()) {
					if (rs.next()) {
						underNineHours = rs.getLong("underNineCount");
					}
				}
			}

			long absentToday = Math.max(totalEmployees - presentToday, 0L);
			long onTimeToday = Math.max(presentToday - lateArrivals, 0L);

			Map<String, Object> summary = new HashMap<>();
			summary.put("totalEmployees", totalEmployees);
			summary.put("presentToday", presentToday);
			summary.put("lateArrivals", lateArrivals);
			summary.put("underNineHours", underNineHours);
			summary.put("absentToday", absentToday);
			responseData.put("summary", summary);

			Map<String, Object> statusDistribution = new HashMap<>();
			statusDistribution.put("onTime", onTimeToday);
			statusDistribution.put("late", lateArrivals);
			statusDistribution.put("absent", absentToday);
			statusDistribution.put("total", totalEmployees);
			responseData.put("statusDistribution", statusDistribution);

			// Trend across selected range (daily present vs absent)
			List<Map<String, Object>> weeklyTrend = new ArrayList<>();
			String trendQuery = "SELECT CAST(Logdatetime AS DATE) AS AttendanceDate, COUNT(DISTINCT Empcode) AS PresentCount "
					+ "FROM IshineRawdata "
					+ "WHERE Logdatetime >= ? AND Logdatetime < ? "
					+ "GROUP BY CAST(Logdatetime AS DATE) "
					+ "ORDER BY AttendanceDate";
			try (PreparedStatement trendStmt = con.prepareStatement(trendQuery)) {
				trendStmt.setTimestamp(1, java.sql.Timestamp.valueOf(startLocalDate.atStartOfDay()));
				trendStmt.setTimestamp(2, java.sql.Timestamp.valueOf(endLocalDate.plusDays(1).atStartOfDay()));
				try (ResultSet rs = trendStmt.executeQuery()) {
					while (rs.next()) {
						long presentCount = rs.getLong("PresentCount");
						Map<String, Object> day = new HashMap<>();
						day.put("date", rs.getString("AttendanceDate"));
						day.put("presentCount", presentCount);
						day.put("absentCount", Math.max(totalEmployees - presentCount, 0L));
						weeklyTrend.add(day);
					}
				}
			}
			responseData.put("weeklyTrend", weeklyTrend);

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

	private LocalDate parseDashboardDate(String date) {
		if (date == null) {
			return LocalDate.now();
		}

		try {
			return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
		} catch (DateTimeParseException ignored) {
		}

		try {
			return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		} catch (DateTimeParseException ignored) {
		}

		return LocalDate.now();
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
		        
		        
		        ResultSet resultSet = statement.executeQuery();
		        
		        if (!resultSet.next()) {
		        } else {
		        	 while (resultSet.next()){
		                BioMaTO bioMaTO = new BioMaTO();
		               
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
		                } catch (ClassNotFoundException | SQLException e) {
		                    e.printStackTrace();
		                }
		                
		                
		            }
		        });
		        
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
		        if (item != null && !removedIdSet.contains(item.getEmployementId().toString())) {
		            newList.add(item);
		        }
		    	
		    }
			
		    return newList;
		}

	// private List<BioMaTO> mapEmployeeDetails(List<BioMaTO> bioMaxTOList) {
	// 	List<String> allEmployeeIds = bioMaxTOList.stream().map(e -> e.getEmployeeCode()).collect(Collectors.toList());
	// 	List<Object[]> employees = employeeRepository.findByPrefixedEmployeementIdIn(allEmployeeIds);
	// 	Map<String, Map<String, String>> employeeMap = new HashMap<>();
		
	// 	for(Object[] employee : employees) {
	// 		String employeeId = employee[4] != null ? employee[4].toString() : null;

	// 		if(employeeId == null ){
	// 			throw new NullPointerException("Employee Id is Null");
	// 		}

	// 		Map<String, String> employeeDetails = new HashMap<>();

	// 		String employeeName = employee[1] != null ? employee[1].toString() : "N/A";
	// 		String reportingManager = employee[2] != null ? employee[2].toString() : "N/A";
	// 		String departmentName = employee[3] != null ? employee[3].toString() : "N/A";

	// 		employeeDetails.put("reportingManager", reportingManager);
	// 		employeeDetails.put("employeeName", employeeName);
	// 		employeeDetails.put("departmentName", departmentName);

	// 		employeeMap.put(employeeId, employeeDetails);
	// 	}

	// 	for(BioMaTO bioMaTO : bioMaxTOList) {
	// 		String employeeId = bioMaTO.getEmployeeCode();
	// 		Map<String, String> employeeDetails = employeeMap.get(employeeId);
	// 		if(employeeDetails!=null) {
	// 			String reportingManager = employeeDetails.get("reportingManager");
	// 			bioMaTO.setReportingManagerName(reportingManager);
	// 			bioMaTO.setEmployeeName(employeeDetails.get("employeeName"));
	// 			bioMaTO.setDepartmentName(employeeDetails.get("departmentName"));
	// 		} else {
	// 			bioMaTO.setReportingManagerName("N/A");
	// 			bioMaTO.setEmployeeName("N/A");
	// 			bioMaTO.setDepartmentName("N/A");
	// 		}
	// 	}

	// 	return bioMaxTOList;
	// }

	private List<BioMaTO> mapEmployeeDetails(List<BioMaTO> bioMaxTOList, Map<String, String> searchParams) {
		if (bioMaxTOList.isEmpty()) {
			return bioMaxTOList;
		}
		
		// Extract numeric part from each employee code and also keep original for mapping
		List<String> allEmployeeIds = bioMaxTOList.stream()
			.map(e -> {
				String empCode = e.getEmployeeCode();
				// Extract numeric part (remove A, AP, CS, A-, AP-, etc.)
				String numericPart = empCode.replaceAll("[^0-9]", "");
				return numericPart;
			})
			.filter(num -> !num.isEmpty()) // Remove empty strings
			.collect(Collectors.toList());
		
		// Also create a map of original code to numeric part for later mapping
		Map<String, String> codeToNumericMap = bioMaxTOList.stream()
			.collect(Collectors.toMap(
				BioMaTO::getEmployeeCode,
				e -> e.getEmployeeCode().replaceAll("[^0-9]", ""),
				(existing, replacement) -> existing
			));
		
		// Extract filter parameters
		String employeeName = searchParams != null ? searchParams.get("employeeName") : null;
		String employeeCode = searchParams != null ? searchParams.get("employeeCode") : null;
		String departmentName = searchParams != null ? searchParams.get("departmentName") : null;
		String managerName = searchParams != null ? searchParams.get("reportingManagerName") : null;
		
		// Query with filters applied - but we need to search by employeement_id (numeric)
		List<Object[]> employees = employeeRepository.findByPrefixedEmployeementIdInWithFilters(
			allEmployeeIds,
			(employeeName != null && !employeeName.trim().isEmpty()) ? employeeName : null,
			(employeeCode != null && !employeeCode.trim().isEmpty()) ? employeeCode : null,
			(departmentName != null && !departmentName.trim().isEmpty()) ? departmentName : null,
			(managerName != null && !managerName.trim().isEmpty()) ? managerName : null
		);
		
		Map<String, Map<String, String>> employeeMap = new HashMap<>();
		
		for (Object[] employee : employees) {
			// Assuming the query returns: emp_id, name, reporting_manager_name, department_name, prefixed_id, employeement_id
			Long employeementId = employee[5] != null ? ((Number) employee[5]).longValue() : null;
			
			if (employeementId == null) {
				continue;
			}
			
			String employeementIdStr = String.valueOf(employeementId);
			
			Map<String, String> employeeDetails = new HashMap<>();
			
			String empName = employee[1] != null ? employee[1].toString() : "N/A";
			String reportingManager = employee[2] != null ? employee[2].toString() : "N/A";
			String deptName = employee[3] != null ? employee[3].toString() : "N/A";
			String prefixedId = employee[4] != null ? employee[4].toString() : null;
			
			employeeDetails.put("reportingManager", reportingManager);
			employeeDetails.put("employeeName", empName);
			employeeDetails.put("departmentName", deptName);
			if (prefixedId != null) {
				employeeDetails.put("employeeCode", prefixedId);
			}
			
			// Store by numeric employeement ID
			employeeMap.put(employeementIdStr, employeeDetails);
		}
		
		// Filter the bioMaxTOList based on which employees exist in the filtered results
		List<BioMaTO> filteredList = new ArrayList<>();
		for (BioMaTO bioMaTO : bioMaxTOList) {
			String originalCode = bioMaTO.getEmployeeCode();
			String numericId = codeToNumericMap.get(originalCode);
			
			Map<String, String> employeeDetails = employeeMap.get(numericId);
			
			if (employeeDetails != null) {
				bioMaTO.setReportingManagerName(employeeDetails.get("reportingManager"));
				bioMaTO.setEmployeeName(employeeDetails.get("employeeName"));
				bioMaTO.setDepartmentName(employeeDetails.get("departmentName"));
				if (employeeDetails.containsKey("employeeCode")) {
					bioMaTO.setEmployeeCode(employeeDetails.get("employeeCode"));
				}
				filteredList.add(bioMaTO);
			} else {
				// This employee doesn't match the filters, so skip adding to filteredList
			}
		}
		
		return filteredList;
	}
	
	public ServiceResponse getEmpBioDataFromIshine(String startDate, String endDate, 
                                                Integer pageNumber, Integer pageSize,
                                                Map<String, String> searchParams) throws SQLException {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			if (pageNumber == null || pageNumber < 1) pageNumber = 1;
			if (pageSize == null || pageSize < 1) pageSize = 100;
			
			int offset = (pageNumber - 1) * pageSize;
			
			List<BioMaTO> finalEmpBioData = new ArrayList<>();
			
			Connection con = getConnection();
			
			boolean searchInMySQL = true;
			Map<String, String> safeSearchParams = searchParams != null ? searchParams : new HashMap<>();
			List<String> employeeCodesFromMySQL = searchEmployeesInMySQL(safeSearchParams);
			
					if (employeeCodesFromMySQL.isEmpty()) {
						Map<String, Object> responseData = new HashMap<>();
						responseData.put("data", finalEmpBioData);
						responseData.put("totalRecords", 0);
						responseData.put("currentPage", pageNumber);
						responseData.put("pageSize", pageSize);
						responseData.put("totalPages", 0);
						
						serviceResponse.setServiceResponse(responseData);
						serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						return serviceResponse;
			}
			
			// First, get total count with filters
			String countQuery = buildCountQuery(searchInMySQL, employeeCodesFromMySQL, searchParams);
			PreparedStatement countStmt = con.prepareStatement(countQuery);
			setQueryParameters(countStmt, startDate, endDate, searchInMySQL, employeeCodesFromMySQL, searchParams, 1);
			
			ResultSet countRs = countStmt.executeQuery();
			
			int totalRecords = 0;
			if (countRs.next()) {
				totalRecords = countRs.getInt("TotalRecords");
			}
			countRs.close();
			countStmt.close();
			
			String query = buildDataQuery(searchInMySQL, employeeCodesFromMySQL, searchParams);
			PreparedStatement statement = con.prepareStatement(query);
			int paramIndex = setQueryParameters(statement, startDate, endDate, searchInMySQL, employeeCodesFromMySQL, searchParams, 1);
			statement.setInt(paramIndex++, offset);
			statement.setInt(paramIndex, pageSize);
			
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
			
			finalEmpBioData = mapEmployeeDetails(finalEmpBioData, searchParams);
			
			resultSet.close();
			statement.close();
			
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


	private List<String> searchEmployeesInMySQL(Map<String, String> searchParams) {
		List<String> employeeCodes = new ArrayList<>();
		
		try {
			String employeeName = searchParams.get("employeeName");
			String departmentName = searchParams.get("departmentName");
			String reportingManagerName = searchParams.get("reportingManagerName");
			String employeeCodeInput = searchParams.get("employeeCode");
			
			String numericCodeSearch = null;
			if (employeeCodeInput != null && !employeeCodeInput.trim().isEmpty()) {
				numericCodeSearch = employeeCodeInput.replaceAll("[a-zA-Z-]", "").trim();
				if (numericCodeSearch.isEmpty()) numericCodeSearch = null;
			}
			
			// Get raw numeric employee IDs based on search criteria
			List<String> rawIds = employeeRepository.findRawEmployeementIdsBySearchCriteria(
				(employeeName != null && !employeeName.trim().isEmpty()) ? employeeName : null,
				numericCodeSearch,
				(departmentName != null && !departmentName.trim().isEmpty()) ? departmentName : null,
				(reportingManagerName != null && !reportingManagerName.trim().isEmpty()) ? reportingManagerName : null
			);
			
			for (String rawId : rawIds) {
				employeeCodes.add("A-" + rawId);
				employeeCodes.add("AP-" + rawId);
				employeeCodes.add("CS-" + rawId);
				employeeCodes.add("A" + rawId);
				employeeCodes.add("AP" + rawId);
				employeeCodes.add("CS" + rawId);
			}
			
			if (employeeCodeInput != null && !employeeCodeInput.trim().isEmpty()) {
				if (!employeeCodeInput.contains("%")) {
					employeeCodes.add(employeeCodeInput);
				}
			}
			
			// Remove duplicates
			employeeCodes = employeeCodes.stream().distinct().collect(Collectors.toList());
			
			// Log for debugging
			System.out.println("Generated search patterns (exact matches only): " + employeeCodes.size() + " items");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return employeeCodes;
	}
	
	private void appendEmployeeCodesFilter(StringBuilder query, List<String> employeeCodes) {
		if (employeeCodes == null || employeeCodes.isEmpty()) return;
		
		query.append(" AND (");
		
		// Separate exact matches (no %) and pattern matches (with %)
		List<String> exactMatches = employeeCodes.stream()
			.filter(code -> !code.contains("%"))
			.collect(Collectors.toList());
		
		List<String> patternMatches = employeeCodes.stream()
			.filter(code -> code.contains("%"))
			.collect(Collectors.toList());
		
		boolean hasExact = !exactMatches.isEmpty();
		
		// Handle exact matches with IN clause
		if (hasExact) {
			// Split into batches of 500 if needed (to avoid query length limits)
			query.append("Empcode IN (");
			for (int i = 0; i < exactMatches.size(); i++) {
				if (i > 0) query.append(",");
				query.append("'").append(exactMatches.get(i).replace("'", "''")).append("'");
			}
			query.append(")");
		}
		
		// Handle pattern matches with LIKE
		if (!patternMatches.isEmpty()) {
			if (hasExact) {
				query.append(" OR ");
			}
			query.append("(");
			for (int i = 0; i < patternMatches.size(); i++) {
				if (i > 0) query.append(" OR ");
				query.append("Empcode LIKE '").append(patternMatches.get(i).replace("'", "''")).append("'");
			}
			query.append(")");
		}
		
		query.append(") ");
	}

	private String buildCountQuery(boolean searchInMySQL, List<String> employeeCodes, Map<String, String> searchParams) {
		StringBuilder query = new StringBuilder(
			"SELECT COUNT(*) AS TotalRecords FROM " +
			"( " +
			"    SELECT Empcode, MAX(EmpName) AS EmpName, CAST(Logdatetime AS DATE) AS AttendanceDate " +  // Use MAX() aggregate
			"    FROM IshineRawdata " +
			"    WHERE Logdatetime >= ? AND Logdatetime <= ? "
		);
		
		if (searchInMySQL && employeeCodes != null && !employeeCodes.isEmpty()) {
			appendEmployeeCodesFilter(query, employeeCodes);
		}
		
		boolean hasHavingClause = false;
		StringBuilder havingClause = new StringBuilder();
		
		if (searchParams != null && !searchParams.isEmpty()) {
			for (Map.Entry<String, String> entry : searchParams.entrySet()) {
				String field = entry.getKey();
				String value = entry.getValue();
				if (value != null && !value.trim().isEmpty()) {
					switch (field) {
						case "inTime":
							if (!hasHavingClause) {
								havingClause.append(" HAVING ");
								hasHavingClause = true;
							} else {
								havingClause.append(" AND ");
							}
							havingClause.append("LTRIM(RIGHT(CONVERT(VARCHAR(20), MIN(CASE WHEN LOWER(TRIM(Direction)) = 'in' THEN Logdatetime END), 100), 7)) LIKE ?");
							break;
						case "outTime":
							if (!hasHavingClause) {
								havingClause.append(" HAVING ");
								hasHavingClause = true;
							} else {
								havingClause.append(" AND ");
							}
							havingClause.append("LTRIM(RIGHT(CONVERT(VARCHAR(20), MAX(CASE WHEN LOWER(TRIM(Direction)) = 'out' THEN Logdatetime END), 100), 7)) LIKE ?");
							break;
						case "totalDuration":
							if (!hasHavingClause) {
								havingClause.append(" HAVING ");
								hasHavingClause = true;
							} else {
								havingClause.append(" AND ");
							}
							havingClause.append("RIGHT('0' + CAST(DATEDIFF(MINUTE, MIN(CASE WHEN LOWER(TRIM(Direction)) = 'in' THEN Logdatetime END), MAX(CASE WHEN LOWER(TRIM(Direction)) = 'out' THEN Logdatetime END)) / 60 AS VARCHAR), 2) + ':' + RIGHT('0' + CAST(DATEDIFF(MINUTE, MIN(CASE WHEN LOWER(TRIM(Direction)) = 'in' THEN Logdatetime END), MAX(CASE WHEN LOWER(TRIM(Direction)) = 'out' THEN Logdatetime END)) % 60 AS VARCHAR), 2) LIKE ?");
							break;
						case "logDate":
							if (!hasHavingClause) {
								havingClause.append(" HAVING ");
								hasHavingClause = true;
							} else {
								havingClause.append(" AND ");
							}
							havingClause.append("CONVERT(VARCHAR(10), CAST(Logdatetime AS DATE), 105) LIKE ?");
							break;
					}
				}
			}
		}
		
		// GROUP BY Empcode and AttendanceDate only, EmpName is aggregated with MAX()
		query.append("    GROUP BY Empcode, CAST(Logdatetime AS DATE) ");
		
		if (hasHavingClause) {
			query.append(havingClause);
		}
		
		query.append(") A");
		
		return query.toString();
	}
	
	private String buildDataQuery(boolean searchInMySQL, List<String> employeeCodes, Map<String, String> searchParams) {
		StringBuilder query = new StringBuilder(
			"SELECT " +
			"    Empcode, " +
			"    EmpName, " +
			"    CONVERT(VARCHAR(10), AttendanceDate, 105) AS AttendanceDate, " +
			"    LTRIM(RIGHT(CONVERT(VARCHAR(20), InTime, 100), 7)) AS InTime, " +
			"    LTRIM(RIGHT(CONVERT(VARCHAR(20), OutTime, 100), 7)) AS OutTime, " +
			"    RIGHT('0' + CAST(DATEDIFF(MINUTE, InTime, OutTime) / 60 AS VARCHAR), 2) + ':' + RIGHT('0' + CAST(DATEDIFF(MINUTE, InTime, OutTime) % 60 AS VARCHAR), 2) AS TotalWorkingHours " +
			"FROM " +
			"( " +
			"    SELECT " +
			"        Empcode, " +
			"        MAX(EmpName) AS EmpName, " +  // Use MAX() aggregate
			"        CAST(Logdatetime AS DATE) AS AttendanceDate, " +
			"        MIN(CASE WHEN LOWER(TRIM(Direction)) = 'in' THEN Logdatetime END) AS InTime, " +
			"        MAX(CASE WHEN LOWER(TRIM(Direction)) = 'out' THEN Logdatetime END) AS OutTime " +
			"    FROM IshineRawdata " +
			"    WHERE Logdatetime >= ? AND Logdatetime <= ? "
		);
		
		if (searchInMySQL && employeeCodes != null && !employeeCodes.isEmpty()) {
			appendEmployeeCodesFilter(query, employeeCodes);
		}
		
		boolean hasHavingClause = false;
		StringBuilder havingClause = new StringBuilder();
		
		if (searchParams != null && !searchParams.isEmpty()) {
			for (Map.Entry<String, String> entry : searchParams.entrySet()) {
				String field = entry.getKey();
				String value = entry.getValue();
				if (value != null && !value.trim().isEmpty()) {
					switch (field) {
						case "inTime":
							if (!hasHavingClause) {
								havingClause.append(" HAVING ");
								hasHavingClause = true;
							} else {
								havingClause.append(" AND ");
							}
							havingClause.append("LTRIM(RIGHT(CONVERT(VARCHAR(20), MIN(CASE WHEN LOWER(TRIM(Direction)) = 'in' THEN Logdatetime END), 100), 7)) LIKE ?");
							break;
						case "outTime":
							if (!hasHavingClause) {
								havingClause.append(" HAVING ");
								hasHavingClause = true;
							} else {
								havingClause.append(" AND ");
							}
							havingClause.append("LTRIM(RIGHT(CONVERT(VARCHAR(20), MAX(CASE WHEN LOWER(TRIM(Direction)) = 'out' THEN Logdatetime END), 100), 7)) LIKE ?");
							break;
						case "totalDuration":
							if (!hasHavingClause) {
								havingClause.append(" HAVING ");
								hasHavingClause = true;
							} else {
								havingClause.append(" AND ");
							}
							havingClause.append("RIGHT('0' + CAST(DATEDIFF(MINUTE, MIN(CASE WHEN LOWER(TRIM(Direction)) = 'in' THEN Logdatetime END), MAX(CASE WHEN LOWER(TRIM(Direction)) = 'out' THEN Logdatetime END)) / 60 AS VARCHAR), 2) + ':' + RIGHT('0' + CAST(DATEDIFF(MINUTE, MIN(CASE WHEN LOWER(TRIM(Direction)) = 'in' THEN Logdatetime END), MAX(CASE WHEN LOWER(TRIM(Direction)) = 'out' THEN Logdatetime END)) % 60 AS VARCHAR), 2) LIKE ?");
							break;
						case "logDate":
							if (!hasHavingClause) {
								havingClause.append(" HAVING ");
								hasHavingClause = true;
							} else {
								havingClause.append(" AND ");
							}
							havingClause.append("CONVERT(VARCHAR(10), CAST(Logdatetime AS DATE), 105) LIKE ?");
							break;
					}
				}
			}
		}
		
		query.append(" GROUP BY Empcode, CAST(Logdatetime AS DATE) ");  // EmpName is aggregated
		
		if (hasHavingClause) {
			query.append(havingClause);
		}
		
		query.append(") A " +
					"ORDER BY Empcode, AttendanceDate " +
					"OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
		
		return query.toString();
	}
	
	private int setQueryParameters(PreparedStatement stmt, String startDate, String endDate, 
                                boolean searchInMySQL, List<String> employeeCodes, 
                                Map<String, String> searchParams, int startIndex) throws SQLException {
		int index = startIndex;
		
		stmt.setString(index++, startDate + " 00:00:00");
		stmt.setString(index++, endDate + " 23:59:59");
		
		if (searchParams != null && !searchParams.isEmpty()) {
			for (Map.Entry<String, String> entry : searchParams.entrySet()) {
				String field = entry.getKey();
				String value = entry.getValue();
				if (value != null && !value.trim().isEmpty()) {
					switch (field) {
						case "inTime":
						case "outTime":
						case "totalDuration":
						case "logDate":
							stmt.setString(index++, "%" + value + "%");
							break;
					}
				}
			}
		}
		return index;
	}

	public ServiceResponse getBiomatricDataWithSearch(String startDate, String endDate, 
                                                   Integer pageNumber, Integer pageSize,
                                                   Map<String, String> searchParams) throws SQLException {
		return getEmpBioDataFromIshine(startDate, endDate, pageNumber, pageSize, searchParams);
	}

} 