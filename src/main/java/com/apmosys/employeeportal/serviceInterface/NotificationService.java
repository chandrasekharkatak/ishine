package com.apmosys.employeeportal.serviceInterface;

import com.apmosys.employeeportal.dto.NotificationDTO;
import com.apmosys.employeeportal.utility.ServiceResponse;

public interface NotificationService {

	ServiceResponse addNotification(NotificationDTO notificationDTO);

	ServiceResponse updateNotification(NotificationDTO notificationDTO);

	ServiceResponse deleteNotification(NotificationDTO notificationDTO);

	ServiceResponse getAllNotifications();

}
