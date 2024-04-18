package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.DocumentDTO;
import com.apmosys.employeeportal.dto.NewsletterDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Newsletter;
import com.apmosys.employeeportal.model.TypeDocument;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.NewsletterRepository;
import com.apmosys.employeeportal.repository.TypeDocumentRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class TypeDocumentService {
	
	@Value("${file.location.documents.newsletter}")
	private String newsletterFileLocation;
	
	@Autowired
	TypeDocumentRepository typeDocumentRepository;
	
	@Autowired
	NewsletterRepository newsletterRepository;
	
	@Autowired
	EmployeeRepository employeeRepository;

	public ServiceResponse addTypeDocument(DocumentDTO documentDto) {
		
		ServiceResponse response = new ServiceResponse();
		try {
			TypeDocument typeDocument = new TypeDocument();
			
			typeDocument.setTypeName(documentDto.getTypeName());
			typeDocument.setCreatedBy(documentDto.getCreatedBy());
			typeDocument.setCreatedOn(LocalDate.now());
			
			TypeDocument dbResponse = typeDocumentRepository.save(typeDocument);
			
			if(dbResponse != null) {
				response.setServiceResponse("Document Type created !!");
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			}else {
				response.setServiceResponse(" Type Document not save");
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		}
		return response;
	}

	public ServiceResponse checkTypeName(String typeName) {
		
		ServiceResponse response = new ServiceResponse();
		Optional<TypeDocument> findType = typeDocumentRepository.findByTypeName(typeName);
		if(!findType.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Type name already exist");
		}
		
		return response;
	}

//	public ServiceResponse getAllTypeName() {
//		
//		ServiceResponse response = new ServiceResponse();
//		
//		List<TypeDocument> getAllType = typeDocumentRepository.findAll();
//		if(getAllType != null) {
//			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//			response.setServiceResponse(getAllType);
//		}else {
//			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//			response.setServiceResponse("There is no type present");
//		}
//		
//		return response;
//	}
	
	 public ServiceResponse getAllTypeName() {
			
			ServiceResponse response = new ServiceResponse();
			
			List<Object[]> getAllType = typeDocumentRepository.findAllType();
			List<DocumentDTO> listOfTypes = new ArrayList<DocumentDTO>();
			
			getAllType.forEach((type)->{
				DocumentDTO doc = new DocumentDTO();
				doc.setTypeName(type[0] != null ? type[0].toString() : null);
				doc.setCreatedOn(type[1] != null ? LocalDate.parse(type[1].toString()) : null);
				doc.setCreatedBy(type[2] != null ? type[2].toString() : null);
				doc.setName(type[3] != null ? type[3].toString() : null);
				doc.setTypeId(type[4] != null ? Long.parseLong(type[4].toString()) : null);
				listOfTypes.add(doc);
			});
			
			if(listOfTypes != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(listOfTypes);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("There is no type present");
			}
			
			return response;
		}
	
	
	public ServiceResponse uploadDocument(MultipartFile file, String displayName, Long uploadedBy, Long typeId,
			String readEnabled) throws IOException {
		
		ServiceResponse response = new ServiceResponse();
		
		Optional<Employee> employeeObject = employeeRepository.findById(uploadedBy);
		if(employeeObject.isPresent()) {
			if(file != null) {
				byte [] bytes = file.getBytes();
				Path path = Paths.get(newsletterFileLocation + File.separator + file.getOriginalFilename());
				File checkExistingFile = new File(path.toString());
				if (!checkExistingFile.exists()) {
					Files.write(path, bytes);
					File savedFile = new File(path.toString());
					if(savedFile.exists()) {
						Newsletter document = new Newsletter();
						
						document.setDisplayName(displayName);
						document.setCreatedBy(Integer.parseInt(uploadedBy.toString()));
						document.setFileName(file.getOriginalFilename());
						document.setTypeId(typeId);
						document.setReadEnabled(readEnabled);
						Newsletter dbDocument =	newsletterRepository.save(document);
						
						if(dbDocument != null) {
							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse("Document Uploaded successfully");
							
						}else {
							response.setServiceResponse("Failed to upload Document.");
							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
						}
						
						
					}else {
						response.setServiceResponse("Failed to upload document.");
						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					}
					
					
					
				}else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse(file.getOriginalFilename()+" already exist !!");
				}
				
				
			}
		}else {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("User not found !!");
		}
		
		
		
		return response;
	}

	public ServiceResponse deleteType(DocumentDTO documentDto) {
	
		ServiceResponse response = new ServiceResponse();
		try {
			if(documentDto.getTypeId() != null) {
				
				typeDocumentRepository.deleteById(documentDto.getTypeId());
				
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(documentDto.getTypeName()+" has successfully deleted !!");	
			
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(" Type name not found !!");	
			}
			
			} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong !!");	
			}
		
		return response;
	}

	public ServiceResponse getTypeById(DocumentDTO documentDto) {
		
		ServiceResponse response = new ServiceResponse();
		try {
			Long typeId = documentDto.getTypeId();

			TypeDocument findType = typeDocumentRepository.findByTypeId(typeId);

			if(findType != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(findType);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Type not found !! ");
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong !! ");
		}
		return response;
	}

	public ServiceResponse updateType(DocumentDTO documentDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			TypeDocument dbType = typeDocumentRepository.findByTypeId(documentDto.getTypeId());
			
			dbType.setTypeName(documentDto.getTypeName());
			dbType.setUpdatedBy(documentDto.getUpdatedBy());
			dbType.setUpdatedOn(LocalDate.now());
			
			TypeDocument updateType = typeDocumentRepository.save(dbType);
			if(updateType != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Type updated successfully !! ");
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Type not update !! ");
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong ");
			
		}		
		return response;
	}

	

//	public ServiceResponse findAllDocument() {
//		
//		ServiceResponse response = new ServiceResponse();
//		
//		try {
//			List<Object[]> getAllDoc = typeDocumentRepository.getAllTypeDocuments();
//			
//			Optional.ofNullable(getAllDoc).ifPresentOrElse((list)->{
//				if(list.isEmpty()) {
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//					response.setServiceResponse("List is empty");
//				}else {
//					List<NewsletterDTO> dtoList = new ArrayList<>();
//					list.forEach((object)->{
//						NewsletterDTO documentDto = new NewsletterDTO(); 
//						
//						documentDto.setDocumentId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
//						documentDto.setFileName(object[1] != null ? object[1].toString() : null);
//						documentDto.setDisplayName(object[2] != null ? object[2].toString() : null);
//						documentDto.setType(object[3] != null ? object[3].toString() : null);
//						documentDto.setReadEnabled(object[4] != null ? object[4].toString() : null);
//						documentDto.setCreatedBy(object[5] != null ? Integer.parseInt(object[5].toString()) : null);
//						documentDto.setCreatedByName(object[6] != null ? object[6].toString() : null);
//						documentDto.setCreatedOn(object[7] != null ? object[7].toString() : null);
//						documentDto.setTypeId(object[8] != null ? Long.parseLong(object[8].toString()) : null);
//						documentDto.setTypeName(object[9] != null ? object[9].toString() : null);
//						
//						dtoList.add(documentDto);
//					});
//					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//					response.setServiceResponse(dtoList);
//				}
//				
//			}, ()->{
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//				response.setServiceResponse("Document list is empty.");
//			});
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//		}
//		return response;
//	}
//	
//	
	

}
