package com.apmosys.employeeportal.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RewardConfigurationDTO implements Serializable{
	
	private String rewardName;
	private long id;
	private int categoryId;
	private String categoryName; 
	private List<String> rewardTypes;
	private List<CustomFilterDTO> customFilterDTOList;
	private long createdBy;
    private long updatedBy;
//    private String updatedOn;
    private int isTeam;
    private String createdByName; 
    private String updatedByName; 
    private LocalDateTime updatedOn;
    private LocalDateTime createdOn;
    
	private String employeeEmpId;
	private String employeeName;
    private String managerName;
    private String managerEmpId;
    private String filterConditions;

}