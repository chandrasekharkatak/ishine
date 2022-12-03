package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeDocumentDTO;
import com.apmosys.employeeportal.dto.EventPhotoDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.CommonProperties;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeDocument;
import com.apmosys.employeeportal.model.EventPhoto;
import com.apmosys.employeeportal.repository.EmployeeDocumentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EventPhotosRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class ImageService {

	@Autowired
	EventPhotosRepository eventPhotosRepository;

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	EmployeeDocumentRepository employeeDocumentRepository;

	@Autowired
	private HttpServletRequest httpRequest;

	@Autowired
	private LogService logService;

	@Value("${file.location.image}")
	private String imageFileLocation;

	public ServiceResponse uploadMultipleImages(List<MultipartFile> images, String eventName, Long uploadedBy) {
		ServiceResponse response = new ServiceResponse();
		List<File> savedFiles = new ArrayList<File>();
		String errorMsg = "";

		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Upload");
		apiLogInfo.setApiUrl("/api/uploadMultipleImages");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Event Name : " + eventName);
		logBuilder.append("Uploaded By : " + uploadedBy);

		try {

			System.out.println("uploadedBy : " + uploadedBy);
			Optional<Employee> employeeObject = employeeRepository.findById(uploadedBy);

			if (employeeObject.isPresent()) {

				if (!images.isEmpty()) {
					// imageUploader(images,imageFileLocation, savedFiles);
					for (MultipartFile image : images) {

						byte[] bytes = image.getBytes();
						Path path = Paths.get(imageFileLocation + File.separator + image.getOriginalFilename());

						File checkExistingFile = new File(path.toString());
						if (!checkExistingFile.exists()) {
							Files.write(path, bytes);

							File savedFile = new File(path.toString());

							if (savedFile.exists()) {
								savedFiles.add(savedFile);

								apiLogInfo.setApiResponse("file saved .");
								apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

							}
						} else {
							errorMsg = image.getOriginalFilename() + " already exist,";

							apiLogInfo.setApiResponse("event photo already exist !!.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
							break;

						}
					}

					if (images.size() == savedFiles.size()) {
						for (MultipartFile image : images) {

							EventPhoto newPhoto = new EventPhoto();
							newPhoto.setEventName(eventName);
							newPhoto.setImageName(image.getOriginalFilename());

							CommonProperties commonProp = new CommonProperties();
							commonProp.setCreatedBy(uploadedBy);
							newPhoto.setCommonProperty(commonProp);

							eventPhotosRepository.save(newPhoto);
						}
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Event photos Uploaded.");

						apiLogInfo.setApiResponse("Event photos Uploaded.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

					} else {
						for (File image : savedFiles) {
							image.delete();
						}
						response.setServiceResponse(errorMsg + "Upload Image Failed !!");
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);

						apiLogInfo.setApiResponse("Upload Image Failed !!");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

					}
				} else {
					response.setServiceResponse("uploaded images Not Found !!");
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);

					apiLogInfo.setApiResponse("uploaded images Not Found !!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}

			} else {
				response.setServiceResponse("User Not Found !!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);

				apiLogInfo.setApiResponse("User Not Found !!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceError(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			apiLogInfo.setLogLevel("ERROR");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	// All Event Photos
	public ServiceResponse getAllEventPhotos() {
		ServiceResponse response = new ServiceResponse();

		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("View All Event Photos");
		apiLogInfo.setApiUrl("/api/getAllEventPhotos");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();

		try {

			List<Object[]> eventPhotolist = eventPhotosRepository.getAllImagePhotos();

			Optional.ofNullable(eventPhotolist).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Images found.Event Photos list is empty");

					apiLogInfo.setApiResponse("No Images found.Event Photos list is empty.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				} else {

					List<EventPhotoDTO> dtoList = new ArrayList<EventPhotoDTO>();

					list.forEach((object) -> {

						EventPhotoDTO photoDTO = new EventPhotoDTO();

						photoDTO.setEventPhotoId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						photoDTO.setEventName(object[1] != null ? object[1].toString() : null);
						photoDTO.setImageName(object[2] != null ? object[2].toString() : null);
						photoDTO.setCreatedOn(object[3] != null ? object[3].toString() : null);
						photoDTO.setCreatedByName(object[4] != null ? object[4].toString() : null);
						photoDTO.setCreatedBy(object[5] != null ? Long.parseLong(object[5].toString()) : null);

						byte[] imageByte;

						try {
							imageByte = Files
									.readAllBytes(Paths.get(imageFileLocation + File.separator + object[2].toString()));
							photoDTO.setImageBytes(imageByte);

							apiLogInfo.setApiResponse("Image set ");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

						} catch (IOException e) {
							e.printStackTrace();
							response.setServiceError(e.getMessage());
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
							apiLogInfo.setLogLevel("ERROR");
						}

						dtoList.add(photoDTO);
					});

					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);

					apiLogInfo.setApiResponse("Get all event photos ");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Event photo list is empty.");

				apiLogInfo.setApiResponse("Event photo list is empty.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse deleteEventPhoto(EventPhotoDTO eventPhotoDTO) {
		ServiceResponse response = new ServiceResponse();

		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Delete Event Photos");
		apiLogInfo.setApiUrl("/api/deleteEventPhoto");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Event Name : " + eventPhotoDTO.getEventName());
		logBuilder.append("Updated By : " + eventPhotoDTO.getUpdatedBy());

		try {
			if (eventPhotoDTO.getEventPhotoId() != null) {
				Optional<EventPhoto> eventPhotoObject = eventPhotosRepository.findById(eventPhotoDTO.getEventPhotoId());
				eventPhotoObject.ifPresentOrElse((photo) -> {

					File checkExistingFile = new File(imageFileLocation + File.separator + photo.getImageName());
					if (checkExistingFile.exists()) {

						checkExistingFile.delete();

						File checkdeletedFile = new File(imageFileLocation + File.separator + photo.getImageName());

						if (!checkdeletedFile.exists()) {
							eventPhotosRepository.deleteById(photo.getEventPhotoId());

							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Event Photo deleted.");

							apiLogInfo.setApiResponse("Event Photo deleted.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

						} else {
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse("Failed to delete Event Photo.");

							apiLogInfo.setApiResponse("Failed to delete Event Photo.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}
					}

				}, () -> {

					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Event Photo found for given id.");

					apiLogInfo.setApiResponse("No Event Photo found for given id.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				});
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Event Photo id cannot be null.");

				apiLogInfo.setApiResponse("Event Photo id cannot be null.");
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

	public ServiceResponse uploadEmployeeDocument(List<MultipartFile> images, Long uploadedBy, Long employeementId,
			Long empId) {

		ServiceResponse response = new ServiceResponse();
		List<File> savedFiles = new ArrayList<File>();
		String errorMsg = "";

		LogDTO apiLogInfo = new LogDTO();

		apiLogInfo.setSubFeatureName("Upload");
		apiLogInfo.setApiUrl("/api/uploadEmployeeDocument");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Employeement ID : " + employeementId);
		logBuilder.append("Uploaded By : " + uploadedBy);

		try {

			Optional<Employee> employeeObject = employeeRepository.findById(uploadedBy);

//			if (employeeObject.isPresent()) {

			if (!images.isEmpty()) {
				String newPath = Files.createDirectories(Paths.get(imageFileLocation + File.separator + "Documents"
						+ File.separator + "Draft" + File.separator + employeementId)).toString();

				for (MultipartFile image : images) {
					byte[] bytes = image.getBytes();
					String extension = FilenameUtils.getExtension(image.getOriginalFilename());
					Path path = Paths.get(newPath + File.separator + image.getOriginalFilename());

					System.out.println("path : " + path);

					Files.write(path, bytes);

					File savedFile = new File(path.toString());

					if (savedFile.exists()) {
						savedFiles.add(savedFile);

						apiLogInfo.setApiResponse("Image File save.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					}
				}

				if (images.size() == savedFiles.size()) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("image Uploaded.");

					apiLogInfo.setApiResponse("image Uploaded.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				} else {
					for (File image : savedFiles) {
						image.delete();
					}
					response.setServiceResponse(errorMsg + "Upload Image Failed !!");
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);

					apiLogInfo.setApiResponse("Upload Image Failed !!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			} else {
				response.setServiceResponse("uploaded images Not Found !!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);

				apiLogInfo.setApiResponse("uploaded images Not Found !!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

//			} else {
//				response.setServiceResponse("User Not Found !!");
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			}

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

	public ServiceResponse saveEmployeeDocuments(EmployeeDTO employeeDTO) {
		ServiceResponse response = new ServiceResponse();
		List<EmployeeDocument> documentList = new ArrayList<EmployeeDocument>();

		LogDTO apiLogInfo = new LogDTO();

		apiLogInfo.setSubFeatureName("Save");
		apiLogInfo.setApiUrl("/api/saveEmployeeDocuments");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();

		try {

			if (!employeeDTO.getDocumentList().isEmpty()) {
				for (EmployeeDocumentDTO doc : employeeDTO.getDocumentList()) {

					if (doc.getEmployeeDocumentId() != null) {
						Optional<EmployeeDocument> checkDoc = employeeDocumentRepository
								.findByEmployeeDocumentIdAndIsDraft(doc.getEmployeeDocumentId(),
										employeeDTO.getIsDraft());

						if (checkDoc.isPresent()) {

							if (!checkDoc.get().getDocumentName().equals(doc.getDocumentName())) {
								EmployeeDocument newDoc = checkDoc.get();

								File savedFile = new File(imageFileLocation + File.separator + "Documents"
										+ File.separator + "Draft" + File.separator + newDoc.getEmployeementId()
										+ File.separator + newDoc.getDocumentName());

								if (savedFile.delete()) {

									newDoc.setDocumentName(doc.getDocumentName());

									CommonProperties commonProp = new CommonProperties();
									commonProp.setCreatedBy(doc.getCreatedBy());
									commonProp.setUpdatedBy(employeeDTO.getEmpId());
									newDoc.setCommonProperty(commonProp);

									documentList.add(newDoc);
								}
							} else {
								documentList.add(checkDoc.get());
							}
						} else {
							EmployeeDocument newDoc = new EmployeeDocument();
							newDoc.setDocumentName(doc.getDocumentName());
							newDoc.setDocumentType(doc.getDocumentType());
							newDoc.setEmpId(employeeDTO.getEmpId());
							newDoc.setEmployeementId(employeeDTO.getEmployeementId());
							newDoc.setIsDraft(employeeDTO.getIsDraft());

							CommonProperties commonProp = new CommonProperties();
							commonProp.setCreatedBy(employeeDTO.getEmpId());
							newDoc.setCommonProperty(commonProp);

							documentList.add(newDoc);
						}
					} else {
						EmployeeDocument newDoc = new EmployeeDocument();
						newDoc.setDocumentName(doc.getDocumentName());
						newDoc.setDocumentType(doc.getDocumentType());
						newDoc.setEmpId(employeeDTO.getEmpId());
						newDoc.setEmployeementId(employeeDTO.getEmployeementId());
						newDoc.setIsDraft(employeeDTO.getIsDraft());

						CommonProperties commonProp = new CommonProperties();
						commonProp.setCreatedBy(employeeDTO.getEmpId());
						newDoc.setCommonProperty(commonProp);

						documentList.add(newDoc);
					}
				}

				System.out.println(" ================= documentList ===================");
				System.out.println(documentList);
				System.out.println(" ================= documentList ===================");

				List<EmployeeDocument> uploadedDocList = employeeDocumentRepository.saveAll(documentList);

				if (!uploadedDocList.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Documents Uploaded.");

					apiLogInfo.setApiResponse("Documents Uploaded.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				} else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Failed to upload Documents.");

					apiLogInfo.setApiResponse("Failed to upload Documents.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
			} else {
				response.setServiceResponse("Uploaded Documents Not Found !!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);

				apiLogInfo.setApiResponse("Uploaded Documents Not Found !!");
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

	public ServiceResponse getEmployeeDocuments(EmployeeDTO employeeDTO) {
		ServiceResponse response = new ServiceResponse();
		List<EmployeeDocumentDTO> documentList = new ArrayList<EmployeeDocumentDTO>();

		LogDTO apiLogInfo = new LogDTO();

		apiLogInfo.setSubFeatureName("Save");
		apiLogInfo.setApiUrl("/api/getEmployeeDocuments");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		
		try {

			List<EmployeeDocument> documents = employeeDocumentRepository.findByEmpIdAndIsDraft(employeeDTO.getEmpId(),
					employeeDTO.getIsDraft());

			if (!documents.isEmpty()) {
				for (EmployeeDocument doc : documents) {

					EmployeeDocumentDTO docDTO = new EmployeeDocumentDTO();

					docDTO.setEmployeeDocumentId(doc.getEmployeeDocumentId());
					docDTO.setDocumentName(doc.getDocumentName());
					docDTO.setDocumentType(doc.getDocumentType());
					docDTO.setIsDraft(doc.getIsDraft());

					byte[] imageByte;

					try {
						imageByte = Files.readAllBytes(Paths.get(imageFileLocation + File.separator + "Documents"
								+ File.separator + "Draft" + File.separator + doc.getEmployeementId() + File.separator
								+ doc.getDocumentName()));
						docDTO.setDocumentBytes(imageByte);
					} catch (IOException e) {
						e.printStackTrace();
					}

					documentList.add(docDTO);
				}

				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(documentList);
				
				apiLogInfo.setApiResponse("Documents Found !!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
			} else {
				response.setServiceResponse("Documents Not Found !!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				
				apiLogInfo.setApiResponse("Documents Not Found !!");
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

	public ServiceResponse uploadDocumentByList(EmployeeDTO employeeDTO) {
		ServiceResponse response = new ServiceResponse();
		try {

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

}
