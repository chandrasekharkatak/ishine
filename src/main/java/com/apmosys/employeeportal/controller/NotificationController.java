package com.apmosys.employeeportal.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.NotificationDTO;
import com.apmosys.employeeportal.service.ReleaseNotesVideosService;
import com.apmosys.employeeportal.serviceInterface.NotificationService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class NotificationController {
	
	@Autowired
	NotificationService notificationService;
	
	@Autowired
	ReleaseNotesVideosService releaseNotesVideosService;
	
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
	

	@RequestMapping(value = "/onInActivateNotification", method = RequestMethod.POST)
	public ServiceResponse onInActivateNotification(@RequestBody NotificationDTO notificationDTO) {

		ServiceResponse response = notificationService.onInActivateNotification(notificationDTO);
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
	
	@RequestMapping(value = "/getAllNotificationsByNotificationTypeAndEmpId", method = RequestMethod.POST)
	public ServiceResponse getAllNotificationsByNotificationTypeAndEmpId(@RequestBody NotificationDTO notificationDTO) {

		ServiceResponse response = notificationService.getAllNotificationsByNotificationTypeAndEmpId(notificationDTO);
		return response;
	}
	
	@RequestMapping(value = "/saveVideo", method = RequestMethod.POST)
	public ServiceResponse saveVideo(@RequestParam("file") MultipartFile file,@RequestParam("id") Integer id) throws IOException {
		ServiceResponse response = releaseNotesVideosService.saveVideo(file, id);
		return response;
	}
	
	
	@RequestMapping(value = "/getAllNotificationIds",method = RequestMethod.GET)
	public ServiceResponse getAllNotificationIds(){
		ServiceResponse response = releaseNotesVideosService.getAllNotificationIds();
		return response;
	}
	
	@RequestMapping(value = "/getReleaseNotesVideoName/{id}", method = RequestMethod.GET)
	 public ResponseEntity<?> getReleaseNotesVideoName(@PathVariable("id") Integer id) {
        // Call the service method to get video data
        return releaseNotesVideosService.getReleaseNotesVideoName(id);
    }
	
	
		
	

}
