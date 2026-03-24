package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NotificationDTO {
	
	private Integer notificationId;	
	private String notificationMessage;	
	private String createdOn;	
	private Long createdBy;	
	private String createdByName;	
	private String updatedOn;	
	private Long updatedBy;
	private String updatedByName;
	private String notificationType;
	private String isActive;
	private Long empId;
	
	private Long employeementId;
	private String name;
	private String consentOn;

	private String isNotificationViewed;
	private String employeementIdAccToET;
}
