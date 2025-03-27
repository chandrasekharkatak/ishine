package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.HelpDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.CommonProperties;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Help;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.HelpRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class HelpService {
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private HelpRepository helpRepository;
	
	@Value("${helpFile.document.location}")
	private String helpDocumentationLocation;

	public ServiceResponse uploadHelpDocument(List<MultipartFile> files, String helpDocumentName, Long uploadedBy) {
		ServiceResponse response = new ServiceResponse();

		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Help Document Configuration");
		apiLogInfo.setApiUrl("/api/uploadHelpDocument");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("helpDocumentName : " + helpDocumentName + "uploadedBy : " + uploadedBy);
		List<File> savedFiles = new ArrayList<File>();
		String errorMsg = "";

		try {
			System.out.println("uploadedBy : " + uploadedBy);
			Optional<Employee> employeeObject = employeeRepository.findById(uploadedBy);

			if (employeeObject.isPresent()) {
				if (!files.isEmpty()) {
					for (MultipartFile file : files) {
						byte[] bytes = file.getBytes();

						Path path = Paths.get(helpDocumentationLocation + File.separator + file.getOriginalFilename());
						File checkExistingFile = new File(path.toString());
						if (!checkExistingFile.exists()) {
							Files.write(path, bytes);
							File savedFile = new File(path.toString());

							if (savedFile.exists()) {
								savedFiles.add(savedFile);
							}
						} else {
							errorMsg = file.getOriginalFilename() + " already exist,";
							break;
						}

					}
					if (files.size() == savedFiles.size()) {
						for (MultipartFile file : files) {
							Help upload = new Help();
							upload.setHelpDocumentName(helpDocumentName);
							upload.setFileName(file.getOriginalFilename());

							CommonProperties commonProp = new CommonProperties();
							commonProp.setCreatedBy(uploadedBy);
							upload.setCommonProperty(commonProp);
							
							Help dbResponse = helpRepository.save(upload);
						}
						response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
						response.setServiceResponse("Document Uploaded.");

						apiLogInfo.setApiResponse("Document Uploaded");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					} else {
						for (File file : savedFiles) {
							file.delete();
						}
						response.setServiceResponse(errorMsg + "Upload document Failed !!");
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);

						apiLogInfo.setApiResponse("Upload document Failed !!");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}

				} else {
					response.setServiceResponse("uploaded document Not Found !!");
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);

					apiLogInfo.setApiResponse("uploaded document Not Found !!");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}

			} else {
				response.setServiceResponse("User Not Found !!");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);

				apiLogInfo.setApiResponse("User Not Found !!");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}

		} catch (IOException e) {
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

	public ServiceResponse getAllHelpDocument() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("get_AllHelpDocument");
		apiLogInfo.setApiUrl("/api/getAllHelpDocument");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("getAllHelpDocument size : "+helpRepository.getAllHelpDocument().size());
		try {
			List<Object[]> documentList = helpRepository.getAllHelpDocument();
			
			Optional.ofNullable(documentList).ifPresentOrElse((list)->{
				if(list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Document found. Document list is empty");
					apiLogInfo.setApiResponse("No Document found. Document list is empty");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}else {
					List<HelpDTO> dtoList = new ArrayList<HelpDTO>();
					
					list.forEach((object)->{
						
						HelpDTO helpDTO = new HelpDTO();
						
						helpDTO.setHelpDocId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						helpDTO.setCreatedByName(object[1] != null ? object[1].toString() : null);
						helpDTO.setUpdatedByName(object[2] != null ? object[2].toString() : null);
						helpDTO.setFileName(object[3] != null ? object[3].toString() : null);
						helpDTO.setHelpDocumentName(object[4] != null ? object[4].toString() : null);
						helpDTO.setCreatedOn(object[5] != null ? object[5].toString() : null);
						helpDTO.setUpdatedOn(object[6] != null ? object[6].toString() : null);
						helpDTO.setCreatedBy(object[7] != null ? Long.parseLong(object[7].toString()) : null);
						
						dtoList.add(helpDTO);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("dtoList size : "+dtoList.size());
				}
			}, ()->{
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Document list is empty.");
				apiLogInfo.setApiResponse("Document list is empty.");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			apiLogInfo.setApiResponse("Something Went Wrong.");
			apiLogInfo.setLogLevel("ERROR");
			response.setServiceError(e.getMessage());		
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}

	public ServiceResponse deleteHelpDocument(HelpDTO helpDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Delete Help Document");
		apiLogInfo.setApiUrl("/api/deleteHelpDocument");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("helpDocId : " +helpDTO.getHelpDocId());
		try {

			if (helpDTO.getHelpDocId() != null) {
				
				Optional<Help> helpDocumnet = helpRepository.findById(helpDTO.getHelpDocId());
				
				helpDocumnet.ifPresentOrElse((document) -> {
					File checkExistingFile = new File(helpDocumentationLocation + File.separator + document.getFileName());
					if (checkExistingFile.exists()) {

						checkExistingFile.delete();

						File checkdeletedFile = new File(
								helpDocumentationLocation + File.separator + document.getFileName());

						if (!checkdeletedFile.exists()) {
							helpRepository.deleteById(document.getHelpDocId());

							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Help Document deleted.");

							apiLogInfo.setApiResponse("Help Document deleted.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						} else {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Failed to delete Help Document.");

							apiLogInfo.setApiResponse("Failed to delete Help Document");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						}
					}
				}, () -> {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Document found for given id.");

					apiLogInfo.setApiResponse("No Document found for given id.");
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				});
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Help document id cannot be null.");

				apiLogInfo.setApiResponse("Help document id cannot be null.");
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

	public Resource getTemplateFile(long helpDocId) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getTemplateFile");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("helpDocId : "+helpRepository.findByHelpDocId(helpDocId));
		Resource resource=null;
		String filename=null;
		try {
		Help object = helpRepository.findByHelpDocId(helpDocId);
		
		if(object != null) {
			filename= object.getFileName();
			response.setServiceResponse("file fetched successfully.");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiResponse("file fetched successfully.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
		}
		
		String Location = helpDocumentationLocation + File.separator + filename;
		File file = new File(Location);
		if (file.exists()) {
			resource = new FileSystemResource(Location);
		}
		} catch (Exception e) {
			response.setServiceResponse("File not found.");
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			e.printStackTrace();
		// throw new FileNotFoundException("File not found ");
		}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return resource;
	}

}
