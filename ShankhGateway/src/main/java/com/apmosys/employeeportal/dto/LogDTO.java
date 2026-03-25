package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LogDTO {
	private Long empId;
    private String tabName;
    private String featureName;
    private String subFeatureName;
    private String apiUrl;
    private String apiStatus;
    private String apiRequest;
    private String apiResponse;
    private String apiError;
    private String loginTime;
    private String logoutTime;
    private String sessionDuration;
    private String logLevel;
}
