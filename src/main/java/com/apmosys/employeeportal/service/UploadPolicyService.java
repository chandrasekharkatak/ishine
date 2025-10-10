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
import com.apmosys.employeeportal.dto.UploadPolicyDTO;
import com.apmosys.employeeportal.model.CommonProperties;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeNotificationConsent;
import com.apmosys.employeeportal.model.Notification;
import com.apmosys.employeeportal.model.PolicyReadResponse;
import com.apmosys.employeeportal.model.UploadPolicy;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.PolicyReadResponseRepository;
import com.apmosys.employeeportal.repository.UploadPolicyRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class UploadPolicyService {
	
	@Autowired
	private  UploadPolicyRepository UploadPolicyRepository;
	
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	ValidationService validationService;
	
	@Autowired
	private PolicyReadResponseRepository PolicyReadResponseRepository;
	
	@Value("${file.location.document}")
	private String documentFileLocation;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	
	
	public ServiceResponse uploadPolicies(List<MultipartFile> files, String policyName, Long uploadedBy, String readEnabled) {
		
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Upload Policy");
		apiLogInfo.setApiUrl("/api/uploadPolicies");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("policyName : " +policyName+ "uploadedBy : " +uploadedBy+ "readEnabled : " +readEnabled );
		List<File> savedFiles = new ArrayList<File>();
		String errorMsg = "";
		
		try {
			System.out.println("uploadedBy : " + uploadedBy);
			Optional<Employee> employeeObject = employeeRepository.findById(uploadedBy);
			
			if(employeeObject.isPresent()) {
				if(!files.isEmpty()) {
					for(MultipartFile file: files){
			            byte[] bytes = file.getBytes();

			            Path path = Paths.get(documentFileLocation +  File.separator +file.getOriginalFilename());
			            File checkExistingFile = new File(path.toString());
			            if(!checkExistingFile.exists()) {
			            	Files.write(path, bytes);
			            	File savedFile = new File(path.toString());

							if (savedFile.exists()) {
								savedFiles.add(savedFile);
							}
							}else {
			                	errorMsg = file.getOriginalFilename() + " already exist,";
								break;
			                }
			            	
			            }
					if(files.size() == savedFiles.size()) {
						for(MultipartFile file: files){
							UploadPolicy uploadPolicy = new UploadPolicy();
							uploadPolicy.setPolicyName(policyName);
							uploadPolicy.setFileName(file.getOriginalFilename());
							
							CommonProperties commonProp = new CommonProperties();
				            commonProp.setCreatedBy(uploadedBy);
				            uploadPolicy.setReadEnabled(readEnabled);
							uploadPolicy.setCommonProperty(commonProp);
							UploadPolicyRepository.save(uploadPolicy);							
						}
						 response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			             response.setServiceResponse("Document Uploaded.");
			             
			             apiLogInfo.setApiResponse("Appreciation not submitted");			
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
						}else {
							for(File file: savedFiles){
								file.delete();
							}
							response.setServiceResponse(errorMsg + "Upload document Failed !!");
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							
							apiLogInfo.setApiResponse("Upload document Failed !!");			
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
						}

					}else {
						response.setServiceResponse("uploaded document Not Found !!");
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						
						apiLogInfo.setApiResponse("uploaded document Not Found !!");			
						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
					}
					
				}else {
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


	public ServiceResponse  getAllDocuments() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("");
		apiLogInfo.setApiUrl("/api/getAllDocument");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		try {
			List<Object[]> documentList = UploadPolicyRepository.getAllDocuments();
			logBuilder.append("AllDocumentList size : " + documentList.size());
			Optional.ofNullable(documentList).ifPresentOrElse((list)->{
				if(list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Document found.Document list is empty");
					apiLogInfo.setApiResponse("No Document found,Document List is empty");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				}else {
					List<UploadPolicyDTO> dtoList = new ArrayList<UploadPolicyDTO>();
					list.forEach((object)->{
						UploadPolicyDTO policyDTO = new UploadPolicyDTO(); 
						policyDTO.setPolicyID(object[0] != null ? Long.parseLong(object[0].toString()) : null);
						policyDTO.setFileName(object[3] != null ? object[3].toString() : null);
						policyDTO.setPolicyName(object[4] != null ? object[4].toString() : null);
						policyDTO.setCreatedByName(object[5] != null ? object[5].toString() : null);
						policyDTO.setCreatedOn(object[2] != null ? object[2].toString() : null);
						policyDTO.setCreatedBy(object[1] != null ? Long.parseLong(object[1].toString()) : null);
						policyDTO.setReadEnabled(object[6] != null ? object[6].toString() : null);
						dtoList.add(policyDTO);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
					apiLogInfo.setApiResponse("All Documents Fetched");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

				}
			}, ()->{
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Document list is empty.");
				apiLogInfo.setApiResponse("Document list is empty");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

				
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
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


	public ServiceResponse deleteDocument(UploadPolicyDTO uploadPolicyDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Delete Document");
		apiLogInfo.setApiUrl("/api/deletePolicyDocument");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("policyID : " +uploadPolicyDTO.getPolicyID());
		try {
		if(uploadPolicyDTO.getPolicyID() != null) {
			Optional<UploadPolicy> policyDocument = UploadPolicyRepository.findById(uploadPolicyDTO.getPolicyID());
			policyDocument.ifPresentOrElse((document) ->{
			File checkExistingFile = new File(documentFileLocation + File.separator + document.getFileName());
            if(checkExistingFile.exists()) {
            	
            	checkExistingFile.delete();
            	
            	File checkdeletedFile = new File(documentFileLocation + File.separator + document.getFileName());

				if (!checkdeletedFile.exists()) {
					UploadPolicyRepository.deleteById(document.getPolicyID());
					
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Policy Document deleted.");
					
					apiLogInfo.setApiResponse("Policy Document deleted.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Failed to delete Policy Document.");
					
					apiLogInfo.setApiResponse("Failed to delete Policy Document");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
            }
			}, () ->{
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Documentfound for given id.");
				
				apiLogInfo.setApiResponse("No Documentfound for given id.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			});
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("policy document id cannot be null.");
				
				apiLogInfo.setApiResponse("policy document id cannot be null.");			
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


	public Resource getTemplateFile(Long policyID) throws FileNotFoundException {
		Resource resource=null;
		String filename=null;
		try {
		List<Object[]> object = UploadPolicyRepository.findByPolicyID(policyID);
		for (Object[] objectlist : object) {
		     filename= (String)objectlist[3];
		    System.out.println("filename" +filename);
		}
		
		String Location = documentFileLocation + File.separator + filename;
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


	public ServiceResponse changepolicyEnabledMode(UploadPolicyDTO uploadPolicyDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("Appreciation");
		apiLogInfo.setApiUrl("/api/changepolicyEnabledMode");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("policyID : " +uploadPolicyDTO.getPolicyID()+ "readEnabled" +uploadPolicyDTO.getReadEnabled() );
		
		try {
		Optional<UploadPolicy> uploadPolicyObject = UploadPolicyRepository.findById(uploadPolicyDTO.getPolicyID());
		if(uploadPolicyObject.isPresent()) {
			UploadPolicy uploadPolicy = uploadPolicyObject.get();
			uploadPolicy.setReadEnabled(uploadPolicyDTO.getReadEnabled());
			CommonProperties commonProp = new CommonProperties();
			commonProp.setCreatedBy(uploadPolicyDTO.getUpdatedBy());
			uploadPolicy.setCommonProperty(commonProp);

			//commonProp.setUpdatedOn(stringToDateTimeParser.getCurrentDateTime());
			UploadPolicy updatedPolicy = UploadPolicyRepository.save(uploadPolicy);
			
			if(updatedPolicy.getPolicyID() != null) {
				if(updatedPolicy.getReadEnabled().equalsIgnoreCase("false")) {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Read Disable status changed.");
					
					apiLogInfo.setApiResponse("Read Disable status changed.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				}
				else {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Read Enable status changed.");
					
					apiLogInfo.setApiResponse("Read Enable status changed.");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
					
				}

			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Failed to change status.");
				
				apiLogInfo.setApiResponse("Failed to change status.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			
		} else {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(" Policy Not Found.");
			
			apiLogInfo.setApiResponse("Policy Not Found.");			
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


	public ServiceResponse setPolicyReadResponseByEmpId(UploadPolicyDTO uploadPolicyDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		// apiLogInfo.setSubFeatureName("Appreciation");
		apiLogInfo.setApiUrl("/api/setPolicyReadResponseByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("policyID : " + uploadPolicyDTO.getPolicyID() + "empID :" + uploadPolicyDTO.getEmpId());
		try {
			if (!validationService.validateEmpId(uploadPolicyDTO.getEmpId())) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Id does not exists.");

				apiLogInfo.setApiResponse("Employee Id does not exists");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				return response;
			}
			
			PolicyReadResponse policyreadresponse = new PolicyReadResponse();
			policyreadresponse.setEmpId(uploadPolicyDTO.getEmpId());
			policyreadresponse.setPolicyID(uploadPolicyDTO.getPolicyID());
			PolicyReadResponse dbResponse = PolicyReadResponseRepository.save(policyreadresponse);
			
			if (dbResponse != null) {
				
				EmployeeDTO dto = new EmployeeDTO();
				
				List<UploadPolicy> allPolicy = UploadPolicyRepository.findByReadEnabled("true");

				if (!allPolicy.isEmpty()) {
					for (UploadPolicy object : allPolicy) {
						PolicyReadResponse readResponse = PolicyReadResponseRepository
								.findByEmpIdAndPolicyID(dbResponse.getEmpId(), object.getPolicyID());

						if (readResponse == null) {
							dto.setPolicyReadConsent(object);
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
				response.setServiceResponse("Appreciation not submitted.");

				apiLogInfo.setApiResponse("Appreciation not submitted.");
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


	public ServiceResponse showPolicyReadResponseByPolicyID(UploadPolicyDTO uploadPolicyDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("Appreciation");
		apiLogInfo.setApiUrl("/api/showPolicyReadResponseByPolicyID");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("policyID: " +uploadPolicyDTO.getPolicyID());
		try {
		List<Object[]> objectList = PolicyReadResponseRepository
				.getPolicyAllResponsesByPolicyId(uploadPolicyDTO.getPolicyID());
		Optional.ofNullable(objectList).ifPresentOrElse((list) -> {
			
			if(list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No responses found for policy. List is empty.");
			} else {
				List<UploadPolicyDTO> dtoList = new ArrayList<UploadPolicyDTO>();
				
				list.forEach((object) -> {
					
					UploadPolicyDTO dto = new UploadPolicyDTO();
					dto.setEmpId(object[5] != null ? Long.parseLong(object[5].toString()) : null);
					dto.setPolicyName(object[3] != null ? object[3].toString() : null);
					dto.setPolicyID(object[4] != null ? Long.parseLong(object[1].toString()) : null);
					dto.setName(object[0] != null ? object[0].toString() : null);
					dto.setReadEnabled(object[2] != null ? object[2].toString() : null);
					dto.setDepartmentName(object[6] != null ? object[6].toString() : null);
					dtoList.add(dto);
									
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse("dtoList" +dtoList);			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}
		},()-> {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("No responses found for Policy. List is null.");
			
			apiLogInfo.setApiResponse("No responses found for Policy. List is null.");			
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


	public ServiceResponse getReadPoliciesByEmpId(UploadPolicyDTO uploadPolicyDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("Appreciation");
		apiLogInfo.setApiUrl("/api/getReadPoliciesByEmpId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("policyID : " +uploadPolicyDTO.getPolicyID()+ "empId : " +uploadPolicyDTO.getEmpId());
		try {
		List<Object[]> objectList = PolicyReadResponseRepository
				.getReadPoliciesByEmpId(uploadPolicyDTO.getEmpId());
        Optional.ofNullable(objectList).ifPresentOrElse((list) -> {
			
			if(list.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No responses found for policy. List is empty.");
				
				apiLogInfo.setApiResponse("No responses found for policy. List is empty..");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			} else {
				List<UploadPolicyDTO> dtoList = new ArrayList<UploadPolicyDTO>();
				
				list.forEach((object) -> {
					
					UploadPolicyDTO dto = new UploadPolicyDTO();
					
					dto.setPolicyID(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					
					dtoList.add(dto);
									
				});
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(dtoList);
				
				apiLogInfo.setApiResponse("dtoList" +dtoList);			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}
		},()-> {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("No responses found for Policy. List is null.");
			
			apiLogInfo.setApiResponse("No responses found for Policy. List is null");			
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


	public ServiceResponse isAllPolicyRead(UploadPolicyDTO uploadPolicyDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("IsALLPolicyRead");
		apiLogInfo.setApiUrl("/api/isAllPolicyRead");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("EmpId : "+ uploadPolicyDTO.getEmpId());
		try {
			UploadPolicyDTO policyDto = new UploadPolicyDTO();
			
			long policyCount = UploadPolicyRepository.countByReadEnabled("true");
			long empResponseCount = PolicyReadResponseRepository.countByEmpId(uploadPolicyDTO.getEmpId());
			
			if(policyCount > empResponseCount) {
				policyDto.setIsAllPolicyMarkAsRead("false");
			}else {
				policyDto.setIsAllPolicyMarkAsRead("true");
			}
			
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(policyDto);
			apiLogInfo.setApiResponse("PolicyDto:" + policyDto);			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);

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
	
	
}
