package com.apmosys.employeeportal.service;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.utility.ServiceResponse;
import java.text.ParseException;




@Service
public class LogService {
	
	private ConcurrentHashMap<Long, LogDTO> userLogInfoList = AuthenticationService.userLogInfoList;
	
	Logger logger = LoggerFactory.getLogger((LogService.class).getName());

	private LogDTO sessionLogInfo;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	public ServiceResponse setSessionInfo(LogDTO logDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("SetSessionInfo");
		apiLogInfo.setApiUrl("/api/setSessionInfo");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : " + logDTO.getEmpId());

		try {
			boolean isUserLogInfoAvailable = userLogInfoList.containsKey(logDTO.getEmpId());
			
			if(isUserLogInfoAvailable) userLogInfoList.put(logDTO.getEmpId(),logDTO);
			
			sessionLogInfo = userLogInfoList.get(logDTO.getEmpId());
			if(sessionLogInfo != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(sessionLogInfo);
				apiLogInfo.setApiResponse("Log Info Updated!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Failed to update Log Info !!");
				apiLogInfo.setApiResponse("Failed to update log info!!");
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
		logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	public void logMyInfo(HttpServletRequest request, LogDTO apiLogInfo) {
		StringBuilder logBuilder =new StringBuilder();
		logBuilder.append("Destination IP : " + request.getLocalAddr() + " | Source System IP : "+ request.getRemoteAddr() + " ");
		
		if(sessionLogInfo != null) {
			if(sessionLogInfo.getEmpId() != null) logBuilder.append("| EmpId : "+ sessionLogInfo.getEmpId() +" ");
			if(sessionLogInfo.getTabName() != null) logBuilder.append("| Tab : "+ sessionLogInfo.getTabName() +" ");
			if(sessionLogInfo.getFeatureName() != null) logBuilder.append("| Feature : "+ sessionLogInfo.getFeatureName() +" ");
			if(apiLogInfo.getSubFeatureName() != null) logBuilder.append("| SubFeature : "+ apiLogInfo.getSubFeatureName() +" ");
			if(apiLogInfo.getApiUrl() != null) logBuilder.append("| URL : "+ apiLogInfo.getApiUrl() +" ");
			if(apiLogInfo.getApiRequest() != null) logBuilder.append("| Request : "+ apiLogInfo.getApiRequest() +" ");
			if(apiLogInfo.getApiResponse() != null) logBuilder.append("| Response : "+ apiLogInfo.getApiResponse() +" ");
			if(apiLogInfo.getApiError() != null) logBuilder.append("| Error : "+ apiLogInfo.getApiError() +" ");
			if(apiLogInfo.getApiStatus() != null) logBuilder.append("| Status : "+ apiLogInfo.getApiStatus() +" ");
			if(sessionLogInfo.getLoginTime() != null) logBuilder.append("| Login Time : "+ sessionLogInfo.getLoginTime() +" ");
			if(sessionLogInfo.getLogoutTime() != null) logBuilder.append("| Logout Time : "+ sessionLogInfo.getLogoutTime() +" ");

			if (sessionLogInfo.getLoginTime() != null && sessionLogInfo.getLogoutTime() != null) {
				String loginTime = sessionLogInfo.getLoginTime();
				String logoutTime = sessionLogInfo.getLogoutTime();
				try {
					SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					Date LogInTime = df.parse(loginTime);
					Date LogOutTime = df.parse(logoutTime);
					Long LogIntime = LogInTime.getTime();
					Long LogOuttime = LogOutTime.getTime();

					Long sessionDurationTime = LogOuttime - LogIntime;
					Date sessionDuration = new Date(sessionDurationTime);

					long second = (sessionDurationTime / 1000) % 60;
					long minute = (sessionDurationTime / (1000 * 60)) % 60;
					long hour = (sessionDurationTime / (1000 * 60 * 60)) % 24;

					String time = String.format("%02d:%02d:%02d", hour, minute, second);
					apiLogInfo.setSessionDuration(time);
					logBuilder.append("| Session Duration : "+ apiLogInfo.getSessionDuration() +" ");

				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}else {
			/* API CALLED BEFORE AND AFTER USER SESSION */
			if(apiLogInfo.getEmpId() != null) logBuilder.append("EmpId : "+ apiLogInfo.getEmpId() +" ");
			if(apiLogInfo.getTabName() != null) logBuilder.append("| Tab : "+ apiLogInfo.getTabName() +" ");
			if(apiLogInfo.getFeatureName() != null) logBuilder.append("| Feature : "+ apiLogInfo.getFeatureName() +" ");
			if(apiLogInfo.getSubFeatureName() != null) logBuilder.append("| SubFeature : "+ apiLogInfo.getSubFeatureName() +" ");
			if(apiLogInfo.getApiUrl() != null) logBuilder.append("| URL : "+ apiLogInfo.getApiUrl() +" ");
			if(apiLogInfo.getApiRequest() != null) logBuilder.append("| Request : "+ apiLogInfo.getApiRequest() +" ");
			if(apiLogInfo.getApiResponse() != null) logBuilder.append("| Response : "+ apiLogInfo.getApiResponse() +" ");
			if(apiLogInfo.getApiError() != null) logBuilder.append("| Error : "+ apiLogInfo.getApiError() +" ");
			if(apiLogInfo.getApiStatus() != null) logBuilder.append("| Status : "+ apiLogInfo.getApiStatus() +" ");
		}
		
//		System.out.println(logBuilder);
		
//		if(apiLogInfo.getLogLevel().equals("INFO")) {
//			logger.info(logBuilder.toString());
//		}
		if(apiLogInfo.getLogLevel().equals("ERROR")) {
			logger.error(logBuilder.toString());
		}
	}
}
