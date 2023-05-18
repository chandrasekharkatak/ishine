package com.apmosys.employeeportal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.NotificationDTO;
import com.apmosys.employeeportal.serviceInterface.NotificationService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class NotificationController {
	
	@Autowired
	NotificationService notificationService;
	
	@RequestMapping(value = "/addNotification", method = RequestMethod.POST)
	public ServiceResponse addNotification(@RequestBody NotificationDTO notificationDTO) {

		ServiceResponse response = notificationService.addNotification(notificationDTO);
		return response;
	}

	@RequestMapping(value = "/updateNotification", method = RequestMethod.POST)
	public ServiceResponse updateNotification(@RequestBody NotificationDTO notificationDTO) {

		ServiceResponse response = notificationService.updateNotification(notificationDTO);
		return response;
	}
	
	@RequestMapping(value = "/deleteNotification", method = RequestMethod.POST)
	public ServiceResponse deleteNotification(@RequestBody NotificationDTO notificationDTO) {

		ServiceResponse response = notificationService.deleteNotification(notificationDTO);
		return response;
	}

	@RequestMapping(value = "/getAllNotifications", method = RequestMethod.GET)
	public ServiceResponse getAllNotifications() {

		ServiceResponse response = notificationService.getAllNotifications();
		return response;
	}
	
	@RequestMapping(value = "/getNotificationById", method = RequestMethod.POST)
	public ServiceResponse getNotificationById(@RequestBody NotificationDTO notificationDTO) {

		ServiceResponse response = notificationService.getNotificationById(notificationDTO);
		return response;
	}
	
	@RequestMapping(value = "/onDeleteNotification", method = RequestMethod.POST)
	public ServiceResponse onDeleteNotification(@RequestBody NotificationDTO notificationDTO) {

		ServiceResponse response = notificationService.onDeleteNotification(notificationDTO);
		return response;
	}
	
	@RequestMapping(value = "/submitNotificationConsent", method = RequestMethod.POST)
	public ServiceResponse submitNotificationConsent(@RequestBody NotificationDTO notificationDTO) {

		ServiceResponse response = notificationService.submitNotificationConsent(notificationDTO);
		return response;
	}
	
	@RequestMapping(value = "/getConsentNotificationResponse", method = RequestMethod.POST)
	public ServiceResponse getConsentNotificationResponse(@RequestBody NotificationDTO notificationDTO) {

		ServiceResponse response = notificationService.getConsentNotificationResponse(notificationDTO);
		return response;
	}

}
