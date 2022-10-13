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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.UploadPolicyDTO;
import com.apmosys.employeeportal.model.CommonProperties;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.UploadPolicy;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.UploadPolicyRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class UploadPolicyService {
	
	@Autowired
	private  UploadPolicyRepository UploadPolicyRepository;
	
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Value("${file.location.document}")
	private String documentFileLocation;
	
	
	public ServiceResponse uploadPolicies(List<MultipartFile> files, String policyName, Long uploadedBy) {
		
		ServiceResponse response = new ServiceResponse();
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
							uploadPolicy.setCommonProperty(commonProp);
							UploadPolicyRepository.save(uploadPolicy);							
						}
						 response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			             response.setServiceResponse("Document Uploaded.");
						}else {
							for(File file: savedFiles){
								file.delete();
							}
							response.setServiceResponse(errorMsg + "Upload document Failed !!");
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						}

					}else {
						response.setServiceResponse("uploaded document Not Found !!");
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					}
					
				}else {
					response.setServiceResponse("User Not Found !!");
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				}

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;
		
	}


	public ServiceResponse  getAllDocuments() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> documentList = UploadPolicyRepository.getAllDocuments();
			Optional.ofNullable(documentList).ifPresentOrElse((list)->{
				if(list.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("No Document found.Document list is empty");
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
						dtoList.add(policyDTO);
					});
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(dtoList);
				}
			}, ()->{
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Document list is empty.");
				
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());		
		}
		return response;
	}


	public ServiceResponse deleteDocument(UploadPolicyDTO uploadPolicyDTO) {
		ServiceResponse response = new ServiceResponse();
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
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse("Failed to delete Policy Document.");
				}
            }
			}, () ->{
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Documentfound for given id.");
			});
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("policy document id cannot be null.");
			}
		}catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}


	public Resource getTemplateFile(Long policyID) throws FileNotFoundException {
		Resource resource=null;
		String filename=null;
		try {
		List<Object[]> object = UploadPolicyRepository.findByPolicyID(policyID);
		for (Object[] objectlist : object) {
		     filename= (String)objectlist[3];
		    System.out.println("ssssssssssssssss" +filename);
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
}
