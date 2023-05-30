package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.NotificationDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.EmployeeNotificationConsent;
import com.apmosys.employeeportal.model.Holiday;
import com.apmosys.employeeportal.model.Notification;
import com.apmosys.employeeportal.repository.EmployeeNotificationConsentRepository;
import com.apmosys.employeeportal.repository.NotificationRepository;
import com.apmosys.employeeportal.serviceInterface.NotificationService;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class NotificationServiceImpl implements NotificationService {

	@Autowired
	NotificationRepository notificationRepository;

	@Autowired
	StringToDateTimeParser stringToDateTimeParser;
	
	@Autowired
	EmployeeNotificationConsentRepository employeeNotificationConsentRepository;

	public ServiceResponse addNotification(NotificationDTO notificationDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			Notification notification = new Notification();

			notification.setCreatedBy(notificationDTO.getCreatedBy());
			notification.setNotificationMessage(notificationDTO.getNotificationMessage());
			notification.setNotificationType(notificationDTO.getNotificationType());
			notification.setIsActive("true");
			
			Notification newNotificationCreated = notificationRepository.save(notification);

			if (newNotificationCreated.getNotificationId() != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("New notification created.");

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Failed to create new notification.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse updateNotification(NotificationDTO notificationDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			Optional<Notification> existingNotification = notificationRepository
					.findById(notificationDTO.getNotificationId());

			if (existingNotification.isPresent()) {
				Notification notification = existingNotification.get();

				notification.setNotificationMessage(notificationDTO.getNotificationMessage());
				notification.setUpdatedBy(notificationDTO.getUpdatedBy());
				notification.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
				notification.setNotificationType(notificationDTO.getNotificationType());
				notification.setIsActive("true");

				Notification updatedNotification = notificationRepository.save(notification);

				if (updatedNotification.getNotificationId() != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Notification updated successfully.");
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to update notification.");
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No such notification available.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse deleteNotification(NotificationDTO notificationDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

			Optional<Notification> existingNotification = notificationRepository
					.findById(notificationDTO.getNotificationId());

			if (existingNotification.isPresent()) {
				Notification notification = existingNotification.get();

				notificationRepository.deleteById(notification.getNotificationId());

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Notification deleted successfully.");

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No such notification available.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse getAllNotifications() {
		ServiceResponse response = new ServiceResponse();
		try {

			List<Object[]> objectList = notificationRepository.getAllNotications();

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No notifications found. List is empty.");
				} else {
					List<NotificationDTO> dtoList = new ArrayList<NotificationDTO>();

					list.forEach((object) -> {

						NotificationDTO dto = new NotificationDTO();

						dto.setNotificationId(object[0] != null ? Integer.parseInt(object[0].toString()) : null);
						dto.setNotificationMessage(object[1] != null ? object[1].toString() : null);
						dto.setCreatedByName(object[2] != null ? object[2].toString() : null);
						dto.setCreatedOn(object[3] != null ? object[3].toString() : null);
						dto.setUpdatedByName(object[4] != null ? object[4].toString() : null);
						dto.setUpdatedOn(object[5] != null ? object[5].toString() : null);
						dto.setNotificationType(object[6] != null ? object[6].toString() : null);
						dto.setIsActive(object[7] != null ? object[7].toString() : null);
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No  notifications found. List is null.");
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse getNotificationById(NotificationDTO notificationDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Notification notificationObj = notificationRepository.findByNotificationId(notificationDTO.getNotificationId());
			
			if(notificationObj != null) {
				NotificationDTO dto = new NotificationDTO();

				dto.setNotificationId(notificationObj.getNotificationId());
				dto.setNotificationMessage(notificationObj.getNotificationMessage());
				dto.setNotificationType(notificationObj.getNotificationType());
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dto);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find notification.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse onDeleteNotification(NotificationDTO notificationDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			Notification notificationObj = notificationRepository.findByNotificationId(notificationDTO.getNotificationId());
			
			if(notificationObj != null) {
				if(notificationObj.getNotificationType().equals("consentNotification")) {
					notificationObj.setIsActive("false");
					notificationObj.setUpdatedBy(notificationDTO.getUpdatedBy());
					
					Notification dbResponse = notificationRepository.save(notificationObj);
					
					if(dbResponse != null) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Notification deleted Successfully.");
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Unable to delete notification.");
					}
				}else if(notificationObj.getNotificationType().equals("notification")) {
					
					notificationRepository.deleteById(notificationObj.getNotificationId());
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Notification deleted Successfully.");
				}else if(notificationObj.getNotificationType().equals("releaseNotes")) {
					
					notificationObj.setIsActive("false");
					notificationObj.setUpdatedBy(notificationDTO.getUpdatedBy());
					
					Notification dbResponse = notificationRepository.save(notificationObj);
					
					if(dbResponse != null) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Notification deleted Successfully.");
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Unable to delete notification.");
					}
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find notification.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse submitNotificationConsent(NotificationDTO notificationDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			EmployeeNotificationConsent consentObj = new EmployeeNotificationConsent();
			
			consentObj.setEmpId(notificationDTO.getEmpId());
			consentObj.setNotificationId(notificationDTO.getNotificationId());
			
			EmployeeNotificationConsent dbResponse = employeeNotificationConsentRepository.save(consentObj);
			
			if(dbResponse != null) {
				
				EmployeeDTO dto = new EmployeeDTO();
				
				if(notificationDTO.getNotificationType().equals("consentNotification")) {
					List<Notification> allConsentNotification = notificationRepository
							.findByNotificationTypeAndIsActive("consentNotification", "true");
					
					if(!allConsentNotification.isEmpty()) {
						for(Notification object: allConsentNotification) {
							EmployeeNotificationConsent consentObject = employeeNotificationConsentRepository
									.findByEmpIdAndNotificationId(notificationDTO.getEmpId(), object.getNotificationId());
							
							if(consentObject == null) {
								dto.setNotificationConsent(object);
								break;
							}
						}
					}
				}else {
					List<Notification> allReleaseNotes = notificationRepository
							.findByNotificationTypeAndIsActive("releaseNotes", "true");
					
					if(!allReleaseNotes.isEmpty()) {
						for(Notification object: allReleaseNotes) {
							EmployeeNotificationConsent releaseConsentObj = employeeNotificationConsentRepository
									.findByEmpIdAndNotificationId(notificationDTO.getEmpId(), object.getNotificationId());
							
							if(releaseConsentObj == null) {
								dto.setReleaseNoteNotification(object);
								break;
							}
						}
					}
				}
				
				System.out.println(dto + " \n\n\n");
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dto);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Override
	public ServiceResponse getConsentNotificationResponse(NotificationDTO notificationDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			
			List<Object[]> notificationResponse = employeeNotificationConsentRepository.getNotificationResponse(notificationDTO.getNotificationId());
			List<NotificationDTO> dtoList = new ArrayList<NotificationDTO>();
			
			if(!notificationResponse.isEmpty()) {
				
				notificationResponse.forEach((object) -> {
					NotificationDTO dto = new NotificationDTO();
					
					dto.setEmployeementId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					dto.setName(object[1] != null ? object[1].toString() : null);
					dto.setNotificationMessage(object[2] != null ? object[2].toString() : null);
					dto.setConsentOn(object[3] != null ? object[3].toString() : null);
					
					dtoList.add(dto);
				});
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No response found.");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

}
