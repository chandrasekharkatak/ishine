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

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.ActivityDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeDocumentDTO;
import com.apmosys.employeeportal.dto.EventPhotoDTO;
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
	
	@Value("${file.location.image}")
	private String imageFileLocation;
	
	public ServiceResponse uploadMultipleImages(List<MultipartFile> images, String eventName, Long uploadedBy) {
		ServiceResponse response = new ServiceResponse();
		List<File> savedFiles = new ArrayList<File>();
		String errorMsg = "";
		try {

			System.out.println("uploadedBy : " + uploadedBy);
			Optional<Employee> employeeObject = employeeRepository.findById(uploadedBy);

			if (employeeObject.isPresent()) {
				
				if(!images.isEmpty()) {
					imageUploader(images,imageFileLocation, savedFiles);
//					for(MultipartFile image: images){
//						
//		                byte[] bytes = image.getBytes();
//		                Path path = Paths.get(imageFileLocation +  File.separator +image.getOriginalFilename());
//		                
//		                File checkExistingFile = new File(path.toString());
//		                if(!checkExistingFile.exists()) {
//							Files.write(path, bytes);
//
//							File savedFile = new File(path.toString());
//
//							if (savedFile.exists()) {
//								savedFiles.add(savedFile);
//							}
//		                }else {
//		                	errorMsg = image.getOriginalFilename() + " already exist,";
//							break;
//		                }
//		            }
					
					if(images.size() == savedFiles.size()) {
						for(MultipartFile image: images){
							
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
					}else {
						for(File image: savedFiles){
							image.delete();
						}
						response.setServiceResponse(errorMsg + "Upload Image Failed !!");
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					}
				}else {
					response.setServiceResponse("uploaded images Not Found !!");
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				}

			} else {
				response.setServiceResponse("User Not Found !!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}
	
	// All Event Photos
	public ServiceResponse getAllEventPhotos() {
		ServiceResponse response = new ServiceResponse();

		try {

			List<Object[]> eventPhotolist = eventPhotosRepository.getAllImagePhotos();
			
			Optional.ofNullable(eventPhotolist).ifPresentOrElse((list) -> {

				if (list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Images found.Event Photos list is empty");
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
							imageByte = Files.readAllBytes(Paths.get(imageFileLocation + File.separator + object[2].toString()));
							photoDTO.setImageBytes(imageByte);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						
						
						dtoList.add(photoDTO);
					});
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}
			}, () -> {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Event photo list is empty.");
			});
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
	public ServiceResponse deleteEventPhoto(EventPhotoDTO eventPhotoDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (eventPhotoDTO.getEventPhotoId() != null) {
				Optional<EventPhoto> eventPhotoObject = eventPhotosRepository.findById(eventPhotoDTO.getEventPhotoId());
				eventPhotoObject.ifPresentOrElse((photo) -> {

					File checkExistingFile = new File(imageFileLocation + File.separator + photo.getImageName());
	                if(checkExistingFile.exists()) {
	                	
	                	checkExistingFile.delete();
	                	
	                	File checkdeletedFile = new File(imageFileLocation + File.separator + photo.getImageName());

						if (!checkdeletedFile.exists()) {
							eventPhotosRepository.deleteById(photo.getEventPhotoId());
							
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Event Photo deleted.");
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Failed to delete Event Photo.");
						}
	                }

				}, () -> {

					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Event Photo found for given id.");
				});
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Event Photo id cannot be null.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ServiceResponse uploadEmployeeDocument(List<MultipartFile> images,Long uploadedBy) {
		ServiceResponse response = new ServiceResponse();
		List<File> savedFiles = new ArrayList<File>();
		String errorMsg = "";
		try {

			Optional<Employee> employeeObject = employeeRepository.findById(uploadedBy);

			if (employeeObject.isPresent()) {
				
				if(!images.isEmpty()) {
					String newPath = Files.createDirectories(Paths.get(imageFileLocation + File.separator + uploadedBy)).toString();
					imageUploader(images,newPath, savedFiles);
//					for(MultipartFile image: images){
//		                byte[] bytes = image.getBytes();
//		                Path path = Paths.get(newPath +  File.separator +image.getOriginalFilename());
//		                
//		                File checkExistingFile = new File(path.toString());
//		                if(!checkExistingFile.exists()) {
//							Files.write(path, bytes);
//
//							File savedFile = new File(path.toString());
//
//							if (savedFile.exists()) {
//								savedFiles.add(savedFile);
//							}
//		                }else {
//		                	errorMsg = image.getOriginalFilename() + " already exist,";
//							break;
//		                }
//		            }
					
					if(images.size() == savedFiles.size()) {
						for(MultipartFile image: images){
							
							EmployeeDocument newDoc = new EmployeeDocument();
							newDoc.setDocumentName(image.getOriginalFilename());
							
				            CommonProperties commonProp = new CommonProperties();
				            commonProp.setCreatedBy(uploadedBy);
				            newDoc.setCommonProperty(commonProp);
				               
				            employeeDocumentRepository.save(newDoc);
						}
						 response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			             response.setServiceResponse("image Uploaded.");
					}else {
						for(File image: savedFiles){
							image.delete();
						}
						response.setServiceResponse(errorMsg + "Upload Image Failed !!");
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					}
				}else {
					response.setServiceResponse("uploaded images Not Found !!");
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				}

			} else {
				response.setServiceResponse("User Not Found !!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}
	
	public List<File> imageUploader(List<MultipartFile> images, String locationOfImage, List<File> savedFiles) {
		String errorMsg = "";
		try {
			for(MultipartFile image: images){
				
                byte[] bytes = image.getBytes();
                Path path = Paths.get(locationOfImage +  File.separator + image.getOriginalFilename());
                
                File checkExistingFile = new File(path.toString());
                if(!checkExistingFile.exists()) {
					Files.write(path, bytes);

					File savedFile = new File(path.toString());

					if (savedFile.exists()) {
						savedFiles.add(savedFile);
					}
                }else {
                	errorMsg = image.getOriginalFilename() + " already exist,";
					break;
                }
            }
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return savedFiles;
	}
	
	
//	public ServiceResponse fileUploader(List<MultipartFile> files,Long uploadedBy) {
//		ServiceResponse response = new ServiceResponse();
//		try {
//			
//			for(MultipartFile file: files){
//				
//				Path path = Paths
//		                .get(imageFileLocation + File.separator + ((MultipartFile) file).getOriginalFilename());
//		        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//				
//			}	
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
//		return response;
//	}
	
}
