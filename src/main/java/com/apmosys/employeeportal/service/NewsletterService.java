package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.FileNotFoundException;
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

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.NewsletterDTO;
import com.apmosys.employeeportal.dto.UploadPolicyDTO;
import com.apmosys.employeeportal.model.CommonProperties;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Newsletter;
import com.apmosys.employeeportal.model.NewsletterReadResponse;
import com.apmosys.employeeportal.model.PolicyReadResponse;
import com.apmosys.employeeportal.model.UploadPolicy;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.NewsletterReadResponseRepository;
import com.apmosys.employeeportal.repository.NewsletterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class NewsletterService {

	@Autowired
	private NewsletterRepository newsletterRepository;
	
	@Autowired
	private NewsletterReadResponseRepository newsletterReadResponseRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	ValidationService validationService;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Value("${file.location.documents.newsletter}")
	private String newsletterFileLocation;
	
	
	public ServiceResponse uploadNewsletter(MultipartFile file, String displayName, Long uploadedBy) {
		
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Upload Newsletter");
		apiLogInfo.setApiUrl("/api/newsletters/uploadNewsletter");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Newsletter : " +displayName+ "uploadedBy : " +uploadedBy);
		List<File> savedFiles = new ArrayList<File>();
		String errorMsg = "";
		
		try {
			System.out.println("uploadedBy : " + uploadedBy);
			Optional<Employee> employeeObject = employeeRepository.findById(uploadedBy);
			
			if (employeeObject.isPresent()) {
				if (file != null) {

					byte[] bytes = file.getBytes();

					Path path = Paths.get(newsletterFileLocation + File.separator + file.getOriginalFilename());
					File checkExistingFile = new File(path.toString());
					if (!checkExistingFile.exists()) {
						Files.write(path, bytes);
						File savedFile = new File(path.toString());

						if (savedFile.exists()) {
							Newsletter newsletter = new Newsletter();
							newsletter.setDisplayName(displayName);
							newsletter.setFileName(file.getOriginalFilename());
							newsletter.setType("newsletter");
							newsletter.setReadEnabled("true");
							newsletter.setCreatedBy(Integer.parseInt(uploadedBy.toString()));

							newsletterRepository.save(newsletter);

							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Newsletter uploaded successfully.");

							apiLogInfo.setApiResponse("Newsletter uploaded successfully");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						} else {
							response.setServiceResponse("Failed to upload newsletter.");
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);

							apiLogInfo.setApiResponse("Failed to upload newsletter.");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}
					} else {
						response.setServiceResponse(
								"newsletter named " + file.getOriginalFilename() + " already exist.");
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);

						apiLogInfo.setApiResponse("newsletter named " + file.getOriginalFilename() + " already exist.");
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}

				} else {
					response.setServiceResponse("uploaded newsletter Not Found !!");
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);

					apiLogInfo.setApiResponse("uploaded newsletter Not Found !!");
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


	public ServiceResponse getAllNewsletters() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/newsletters/getAllNewsletters");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			List<Object[]> documentList = newsletterRepository.getAllNewsletters();
			Optional.ofNullable(documentList).ifPresentOrElse((list)->{
				if(list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Newsletter found.Newsletter list is empty");
					apiLogInfo.setApiResponse("No Newsletter found,Newsletter List is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				}else {
					List<NewsletterDTO> dtoList = new ArrayList<NewsletterDTO>();
					
					list.forEach((object)->{
						NewsletterDTO newsletterDTO = new NewsletterDTO(); 
						
						newsletterDTO.setDocumentId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						newsletterDTO.setFileName(object[1] != null ? object[1].toString() : null);
						newsletterDTO.setDisplayName(object[2] != null ? object[2].toString() : null);
						newsletterDTO.setType(object[3] != null ? object[3].toString() : null);
						newsletterDTO.setReadEnabled(object[4] != null ? object[4].toString() : null);
						newsletterDTO.setCreatedBy(object[5] != null ? Integer.parseInt(object[5].toString()) : null);
						newsletterDTO.setCreatedByName(object[6] != null ? object[6].toString() : null);
						newsletterDTO.setCreatedOn(object[7] != null ? object[7].toString() : null);
						
						dtoList.add(newsletterDTO);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse(dtoList.size() + "Newsletters Found.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				}
			}, ()->{
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Newsletter list is empty.");
				apiLogInfo.setApiResponse("Newsletter list is empty");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				
			});
		}catch (Exception e) {
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


	public ServiceResponse deleteNewsletter(Long documentId) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Delete Newsletter");
		apiLogInfo.setApiUrl("/api/newsletters/deleteNewsletter");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Document ID : "+ documentId);
		try {
			
			if(documentId != null) {
				Optional<Newsletter> policyDocument = newsletterRepository.findById(documentId);
				List<NewsletterReadResponse> readResponses =  newsletterReadResponseRepository.findByDocumentId(documentId);
				
				policyDocument.ifPresentOrElse((document) ->{
					File checkExistingFile = new File(newsletterFileLocation + File.separator + document.getFileName());
		            if(checkExistingFile.exists()) {
		            	
		            	checkExistingFile.delete();
		            	
		            	File checkdeletedFile = new File(newsletterFileLocation + File.separator + document.getFileName());
		
						if (!checkdeletedFile.exists()) {
							
							if(!readResponses.isEmpty()) {
								readResponses.forEach(data -> {
									newsletterReadResponseRepository.deleteById(data.getDocumentReadResponseId());
								});
							}
							
							newsletterRepository.deleteById(document.getDocumentId());
							
							
							
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Newsletter deleted successfully.");
							
							apiLogInfo.setApiResponse("Newsletter deleted successfully.");			
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						}else {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Failed to delete newsletter.");
							
							apiLogInfo.setApiResponse("Failed to delete newsletter");			
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						}
		            }
				}, () ->{
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Newsletter found for given id.");
					
					apiLogInfo.setApiResponse("No Newsletter found for given id.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				});
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Newsletter id cannot be null.");
					
				apiLogInfo.setApiResponse("Newsletter id cannot be null.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		}catch (Exception e) {
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
	
	public Resource getTemplateFile(Long documentId) throws FileNotFoundException {
		Resource resource=null;
		String filename=null;
		try {
		List<Object[]> object = newsletterRepository.findByDocumentId(documentId);
		for (Object[] objectlist : object) {
		     filename= (String)objectlist[1];
		    System.out.println("filename" +filename);
		}
		
		String Location = newsletterFileLocation + File.separator + filename;
		File file = new File(Location);
		if (file.exists()) {
			resource = new FileSystemResource(Location);
		}
		} catch (Exception e) {
			e.printStackTrace();
		// throw new FileNotFoundException("File not found ");
		}
		return resource;

	}
	
	public ServiceResponse setNewsletterReadResponseByEmpId(NewsletterDTO newsletterDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/setNewsletterReadResponseByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Newsletter ID : " + newsletterDTO.getDocumentId() + ", Emp ID :" + newsletterDTO.getEmpId());
		try {
			if (!validationService.validateEmpId(newsletterDTO.getEmpId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Id does not exists.");

				apiLogInfo.setApiResponse("Employee Id does not exists");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}
			
			NewsletterReadResponse newsletterReadResponse = new NewsletterReadResponse();
			newsletterReadResponse.setEmpId(newsletterDTO.getEmpId());
			newsletterReadResponse.setDocumentId(newsletterDTO.getDocumentId());
			
			NewsletterReadResponse dbResponse = newsletterReadResponseRepository.save(newsletterReadResponse);
			
			if (dbResponse != null) {
				
				EmployeeDTO dto = new EmployeeDTO();
				
				List<Newsletter> allNewsletters = newsletterRepository.findAll();

				if (!allNewsletters.isEmpty()) {
					for (Newsletter object : allNewsletters) {
						NewsletterReadResponse readResponse = newsletterReadResponseRepository
									.findByEmpIdAndDocumentId(dbResponse.getEmpId(), object.getDocumentId());
						
						if (readResponse == null) {
							dto.setNewsletterReadCheck(object);
							break;
						}
					}
				}
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dto);
				
				apiLogInfo.setApiResponse("Your response has been submitted");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Response not submitted.");

				apiLogInfo.setApiResponse("Response not submitted.");
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
	
	public ServiceResponse getAllReadNewslettersByEmpId(NewsletterDTO newsletterDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAllReadNewslettersByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("Emp ID : " +newsletterDTO.getEmpId());
		try {
			
		List<Object[]> objectList = newsletterReadResponseRepository.getReadNewslettersByEmpId(newsletterDTO.getEmpId());
		
        Optional.ofNullable(objectList).ifPresentOrElse((list) -> {
			
			if(list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No responses found for newsletter. List is empty.");
				
				apiLogInfo.setApiResponse("No responses found for newsletter. List is empty..");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {
				List<NewsletterDTO> dtoList = new ArrayList<NewsletterDTO>();
				
				list.forEach((object) -> {
					
					NewsletterDTO dto = new NewsletterDTO();
					
					dto.setDocumentId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					
					dtoList.add(dto);
									
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse(dtoList.size() + " Newsletter response found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}
		},()-> {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("No responses found for Newsletter. List is null.");
			
			apiLogInfo.setApiResponse("No responses found for Newsletter. List is null");			
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
