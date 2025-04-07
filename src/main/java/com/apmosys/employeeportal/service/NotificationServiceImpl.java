package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LogDTO;
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
	
	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private LogService logService;
	 
	
	
	public ServiceResponse addNotification(NotificationDTO notificationDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Add Notification");
		apiLogInfo.setApiUrl("/api/addNotification");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Notification Type :" + notificationDTO.getNotificationType() + " ,CreatedBy :" + notificationDTO.getCreatedBy());

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
				response.setServiceMessage(Integer.toString(newNotificationCreated.getNotificationId()));
                apiLogInfo.setApiResponse("New notification created");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);


			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Failed to create new notification.");

                apiLogInfo.setApiResponse("Failed to create new notification");
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
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse updateNotification(NotificationDTO notificationDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Update Notification");
		apiLogInfo.setApiUrl("/api/updateNotification");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Notification Type :" + notificationDTO.getNotificationType() + " ,UpdatedBy :" + notificationDTO.getUpdatedBy());

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
				
				// Delete existing consents when notification is updated.
				if(updatedNotification != null && "releaseNotes".equals(updatedNotification.getNotificationType())) {
					List<EmployeeNotificationConsent> existingConsents =  employeeNotificationConsentRepository.findByNotificationId(updatedNotification.getNotificationId());
					if(!existingConsents.isEmpty()) {
						for(EmployeeNotificationConsent consent : existingConsents) {
							employeeNotificationConsentRepository.deleteById(consent.getEmployeeNotificationConsentId());
						}
					}
				}
				

				if (updatedNotification.getNotificationId() != null) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Notification updated successfully.");
					apiLogInfo.setApiResponse("Notification updated successfully");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to update notification.");
					apiLogInfo.setApiResponse("Failed to update notification");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No such notification available.");
				apiLogInfo.setApiResponse("No such notification available");
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
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse deleteNotification(NotificationDTO notificationDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Delete Notification");
		apiLogInfo.setApiUrl("/api/deleteNotification");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Notification Type :" + notificationDTO.getNotificationType() + " ,NotificationId :" + notificationDTO.getNotificationId());

		try {

			Optional<Notification> existingNotification = notificationRepository
					.findById(notificationDTO.getNotificationId());

			if (existingNotification.isPresent()) {
				Notification notification = existingNotification.get();

				notificationRepository.deleteById(notification.getNotificationId());

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Notification deleted successfully.");
				apiLogInfo.setApiResponse("Notification deleted successfully.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No such notification available.");
				apiLogInfo.setApiResponse("No such notification available");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
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
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse getAllNotifications() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllNotifications");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Notification List :" + notificationRepository.getAllNotications().size());
		
		try {

			List<Object[]> objectList = notificationRepository.getAllNotications();

			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No notifications found. List is empty.");
                    apiLogInfo.setApiResponse("No Notification Found.");			
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
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
						dto.setCreatedBy(object[8] != null ? Long.parseLong(object[8].toString().toString()) : null);
						dto.setUpdatedBy(object[9] != null ? Long.parseLong(object[9].toString().toString()) : null);
						
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
                    apiLogInfo.setApiResponse("Notification List Fetched!");			
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No  notifications found. List is null.");
                apiLogInfo.setApiResponse("No notifications found");			
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");

		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse getNotificationById(NotificationDTO notificationDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getNotificationById");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("NotificationId : " + notificationDTO.getNotificationId());
		try {
			
			Notification notificationObj = notificationRepository.findByNotificationId(notificationDTO.getNotificationId());
			
			if(notificationObj != null) {
				NotificationDTO dto = new NotificationDTO();

				dto.setNotificationId(notificationObj.getNotificationId());
				dto.setNotificationMessage(notificationObj.getNotificationMessage());
				dto.setNotificationType(notificationObj.getNotificationType());
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dto);
                apiLogInfo.setApiResponse("Dto:" + dto);			
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find notification.");
                apiLogInfo.setApiResponse("Unable to find notification");			
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse onDeleteNotification(NotificationDTO notificationDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/onDeleteNotification");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("NotificationId : " + notificationDTO.getNotificationId());
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

                        apiLogInfo.setApiResponse("Notification deleted!");
                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Unable to delete notification.");
                        apiLogInfo.setApiResponse("Unable to delete Notification");
                        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
				}else if(notificationObj.getNotificationType().equals("notification")) {
					
					notificationRepository.deleteById(notificationObj.getNotificationId());
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Notification deleted Successfully.");
                    apiLogInfo.setApiResponse("Notification Deleted!");
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else if(notificationObj.getNotificationType().equals("releaseNotes")) {
					
					// Delete existing consents.
					List<EmployeeNotificationConsent> existingConsents =  employeeNotificationConsentRepository.findByNotificationId(notificationObj.getNotificationId());
					if(!existingConsents.isEmpty()) {
						for(EmployeeNotificationConsent consent : existingConsents) {
							employeeNotificationConsentRepository.deleteById(consent.getEmployeeNotificationConsentId());
						}
					}
					
					notificationRepository.deleteById(notificationObj.getNotificationId());
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Notification deleted Successfully.");
                    apiLogInfo.setApiResponse("Notification Deleted!");
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find notification.");
                apiLogInfo.setApiResponse("Unable to find Notification");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");

		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse onInActivateNotification(NotificationDTO notificationDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/onInActivateNotification");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("NotificationId : " + notificationDTO.getNotificationId() + " ,Notification Type" + notificationDTO.getNotificationType() + " ,Notification is Active? : " + notificationDTO.getIsActive());
		try {
			
			Notification notificationObj = notificationRepository.findByNotificationId(notificationDTO.getNotificationId());
			
			if(notificationObj != null) {
				if(notificationObj.getNotificationType().equals("consentNotification")) {
					notificationObj.setIsActive("false");
					notificationObj.setUpdatedBy(notificationDTO.getUpdatedBy());
					
					Notification dbResponse = notificationRepository.save(notificationObj);
					
					if(dbResponse != null) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Notification InActivated Successfully.");
						apiLogInfo.setApiResponse("Notification InActivated!");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Unable to InActivate notification.");
						apiLogInfo.setApiResponse("Unable to InActivate notificaion");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
				}else if(notificationObj.getNotificationType().equals("releaseNotes")) {
					
					notificationObj.setIsActive("false");
					notificationObj.setUpdatedBy(notificationDTO.getUpdatedBy());
					
					Notification dbResponse = notificationRepository.save(notificationObj);
					
					if(dbResponse != null) {
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Notification InActivated Successfully.");
						apiLogInfo.setApiResponse("Notification InActiivated SuccessFully");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}else {
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						response.setServiceResponse("Unable to InActivate notification.");
						apiLogInfo.setApiResponse("Unable to InActivate notification");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
				}
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to find notification.");
				apiLogInfo.setApiResponse("Unable to find notification");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	@Override
	public ServiceResponse submitNotificationConsent(NotificationDTO notificationDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Submit Notification Consent");
		apiLogInfo.setApiUrl("/api/submitNotificationConsent");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : " + notificationDTO.getEmpId() + " ,NotificationId :" + notificationDTO.getNotificationId());

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
                apiLogInfo.setApiResponse("Dto:" + dto);
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	@Override
	public ServiceResponse getConsentNotificationResponse(NotificationDTO notificationDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getConsentNotificationResponse");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Notification Id : " + notificationDTO.getNotificationId());
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
				apiLogInfo.setApiResponse("dto list:" + dtoList.size());
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No response found.");
				apiLogInfo.setApiResponse("No response found");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}
	
	@Override
	public ServiceResponse getAllNotificationsByNotificationTypeAndEmpId(NotificationDTO notificationDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllNotificationsByNotificationTypeAndEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId :" + notificationDTO.getEmpId() + " ,NotificationType : " + notificationDTO.getNotificationType() + " ,NotificationId : " + notificationDTO.getNotificationId());

		try {

			List<Object[]> objectList = notificationRepository.getAllNotificationsByNotificationTypeAndEmpId(notificationDTO.getNotificationType(), notificationDTO.getEmpId());
			
			Optional.ofNullable(objectList).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No notifications found. List is empty.");
                    apiLogInfo.setApiResponse("No Notifications Found !");			
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

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
						
						if(object[8] != null) {
							dto.setIsNotificationViewed("true");
						}else {
							dto.setIsNotificationViewed("false");
						}
						
						dtoList.add(dto);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
                    apiLogInfo.setApiResponse("NotificationList:" + dtoList.size());			
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				}

			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No  notifications found. List is null.");
                apiLogInfo.setApiResponse("No Notifications found !");			
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");

		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

}
