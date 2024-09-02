package com.apmosys.employeeportal.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder.In;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.BioMaTO;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class BioMaxService {

	@Value("${BioDbIp}")
	public String DBIp;
	@Value("${BioDbPort}")
	public String DBPort;
	@Value("${BioDbUser}")
	public String DBUsername;
	@Value("${BioDbPass}")
	public String DBPassowrd;

	@Value("${BioDbName}")
	public String DBName;

	@Autowired
	EmployeeRepository employeeRepository;

	public Connection getConnection() {

		String DB_URL = "jdbc:mysql://" + DBIp + ":" + DBPort + "/" + DBName;

		try {
			Connection con = DriverManager.getConnection(DB_URL, DBUsername, DBPassowrd);
			return con;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public ServiceResponse getEmpBioData(String date) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			Set<String> bioEmpIdSet = new HashSet<>();
			Map<String, List<String>> empMapById = new HashMap<>();

//			List<List<Object>> bioMaxAllList = new ArrayList();

			String Query = "SELECT UserId , LogDate  FROM device_logs WHERE logdate like '" + date
					+ "%' order by UserId , LogDate";
			Connection con = getConnection();
			PreparedStatement statement = con.prepareStatement(Query);
			ResultSet resultSet = statement.executeQuery();

			String temp1 = "";

			List<String> l = new ArrayList<>();

			while (resultSet.next()) {

				bioEmpIdSet.add(resultSet.getString(1).replace("A", "").trim());

				if (temp1.equals("")) {
					temp1 = resultSet.getString(1).replace("A", "").trim();
					l.add(resultSet.getString(2));
				} else if (resultSet.getString(1).replace("A", "").trim().equals(temp1)) {
					l.add(resultSet.getString(2));
				} else {
					empMapById.put(temp1, l);
					l = new ArrayList();
					temp1 = resultSet.getString(1).replace("A", "").trim();
					l.add(resultSet.getString(2));
				}

			}
			empMapById.put(temp1, l);
			
			
			List<Object[]> empNameFromIshine = employeeRepository.getDataByEmpId(bioEmpIdSet);
			Map<String, String> EmplNameIdMap = new HashMap();

			for (Object[] obj : empNameFromIshine) {
				EmplNameIdMap.put(obj[0].toString(), obj[1].toString());
			}
			
			List<BioMaTO> finalEmpBioData=new ArrayList();

			for (String empId : bioEmpIdSet) {
				List<String> empTimeList = empMapById.get(empId);
				String time1 = "";
				String time2 = "";
				String punchIn="";
				String punchOut="";
				long totalTime=0;
				for (String dateTime : empTimeList) {
					
					if(punchIn.equals(""))
					{
						punchIn=dateTime;
					}
					
					if (time1.equals("") && time2.equals("")) {
						time1 = dateTime;
					}
					else if (!time1.equals("") && time2.equals("")) {
						time2 = dateTime;
					}
					if (!time1.equals("") && !time2.equals("")) {
						//time calculation ...
						long cTime=calculateTime(time1,time2);
						totalTime += cTime;	
						punchOut=time2;
						time1="";
						time2="";
					}
				}
				Long hours = totalTime / 3600;
				Long minutes = (totalTime % 3600) / 60;
				Long seconds = totalTime % 60;
				String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		
				//deviation 
				long expectTime=32400;
				long deviationTime=expectTime-totalTime;
				String finalDevTion="";
				if(deviationTime<0)
				{
					deviationTime = Math.abs((int)deviationTime);
					finalDevTion="+";
				}
				else
				{
					finalDevTion="-";
				}
				
				hours = deviationTime / 3600;
				minutes = (deviationTime % 3600) / 60;
				seconds = deviationTime % 60;
				finalDevTion += String.format("%02d:%02d:%02d", hours, minutes, seconds);
		
				
				
				BioMaTO bioMaTO=new BioMaTO();
				
				bioMaTO.setEmpId(empId);
				bioMaTO.setEmpName(EmplNameIdMap.get(empId));
				bioMaTO.setEmploginTime(punchIn);
				bioMaTO.setEmplogoutTime(punchOut);
				bioMaTO.setEmpTotalWorkingHours(timeString);
				bioMaTO.setEmpDeviation(finalDevTion);
				
				finalEmpBioData.add(bioMaTO);
				
			}


			
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
				BioMaTO bioMaTO=new BioMaTO();
				
				bioMaTO.setEmpId(resultSet.getString(1).replace("A", "").trim());
				bioMaTO.setEmploginTime(resultSet.getString(2));
				bioMaTO.setEmpDeviceName(resultSet.getString(3));
				empDataList.add(bioMaTO);

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

} 