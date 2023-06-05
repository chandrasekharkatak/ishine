package com.apmosys.employeeportal.serviceInterface;

import com.apmosys.employeeportal.dto.NotificationDTO;
import com.apmosys.employeeportal.utility.ServiceResponse;

public interface NotificationService {

	ServiceResponse addNotification(NotificationDTO notificationDTO);

	ServiceResponse updateNotification(NotificationDTO notificationDTO);

	ServiceResponse deleteNotification(NotificationDTO notificationDTO);

	ServiceResponse getAllNotifications();

	ServiceResponse getNotificationById(NotificationDTO notificationDTO);

	ServiceResponse onDeleteNotification(NotificationDTO notificationDTO);
	
	ServiceResponse onInActivateNotification(NotificationDTO notificationDTO);

	ServiceResponse submitNotificationConsent(NotificationDTO notificationDTO);

	ServiceResponse getConsentNotificationResponse(NotificationDTO notificationDTO);
	
	ServiceResponse getAllNotificationsByNotificationTypeAndEmpId(NotificationDTO notificationDTO);

}
