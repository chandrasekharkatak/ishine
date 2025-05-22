package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.EmployeeDocumentDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.TravelBasedReimbursementRequestDTO;
import com.apmosys.employeeportal.dto.TravelDeskDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeDocument;
import com.apmosys.employeeportal.model.Newsletter;
import com.apmosys.employeeportal.model.TravelBasedReimbursementRequest;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.NewsletterRepository;
import com.apmosys.employeeportal.repository.TravelBasedReimbursementRequestRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class TravelBasedReimbursementRequestService {
     
	@Autowired
	private TravelBasedReimbursementRequestRepository travelBasedReimbursementRequestRepository;
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private NewsletterRepository newsletterRepository;
	
	@Value("${level2Approver}")
	public String level2Approver;
	
	@Value("${file.location.documents.travelDesk}")
	private String traveldeskFileLocation;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;	
	
	public ServiceResponse submitReimbursmentBasedOnTravelRequest(List<TravelBasedReimbursementRequestDTO> reimbursementRequestDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("ReimbursmentTravelRequest");
		apiLogInfo.setApiUrl("/api/submitReimbursmentBasedOnTravelRequest");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("TravelRequestId : " + reimbursementRequestDTO.getTravelId() + "appliedBy: " +reimbursementRequestDTO.getUploadedBy());
		
		try {
			if(reimbursementRequestDTO !=null) {
				
				for (TravelBasedReimbursementRequestDTO request : reimbursementRequestDTO) {
					TravelBasedReimbursementRequest reimbursmentDetails = new TravelBasedReimbursementRequest();
					reimbursmentDetails.setInvoiceNo(request.getInvoiceNo());
					reimbursmentDetails.setInvoiceDate(request.getInvoiceDate());
					reimbursmentDetails.setAmount(request.getAmount());
					reimbursmentDetails.setTravelId(request.getTravelId());
					reimbursmentDetails.setUploadedBy(request.getUploadedBy());
					reimbursmentDetails.setUpdatedOn(new Timestamp(System.currentTimeMillis()));
					travelBasedReimbursementRequestRepository.save(reimbursmentDetails);
				    
				}
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Request Submitted Successfully");
				apiLogInfo.setApiResponse("Reimbursment based On Travel Request : "+reimbursementRequestDTO);
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("No Invoice Details Provide");
				apiLogInfo.setApiResponse("No Invoice Details Provide");
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			}
			
		}catch(Exception e){
			
			apiLogInfo.setApiResponse(e.getMessage());
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(e.getMessage());
		}
		return response;
		
	}
	
	
	
	
	
	


public ServiceResponse uploadFile(MultipartFile file, String displayName, Long uploadedBy, String invoiceNo) {

	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Upload Newsletter");
	    apiLogInfo.setApiUrl("/api/newsletters/uploadNewsletter");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("Newsletter: ").append(displayName)
	              .append(", uploadedBy: ").append(uploadedBy);

	    try {
	        System.out.println("uploadedBy: " + uploadedBy);
	        
	        TravelBasedReimbursementRequest invoiceDetails= travelBasedReimbursementRequestRepository.findInvoiceDetails(invoiceNo);
	        
	        System.err.println("Invoice Details"+invoiceDetails);
	        Optional<Employee> employeeObject = employeeRepository.findById(uploadedBy);

	        if (employeeObject.isEmpty()) {
	            response.setServiceResponse("User Not Found !!");
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("User Not Found !!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            return response;
	        }

	        if (file == null || file.isEmpty()) {
	            response.setServiceResponse("Uploaded Travel Document Not Found !!");
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("Uploaded Travel Document Not Found !!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            return response;
	        }

	        // Prepare the file path
	        Path directory = Paths.get(traveldeskFileLocation);
	        if (!Files.exists(directory)) {
	            Files.createDirectories(directory);  // Ensure directory exists
	        }

	        // Use a timestamp to avoid filename conflicts
	        String newFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
	        Path path = directory.resolve(newFileName);
	        System.out.println("Saving file to: " + path.toString());

	        // Save file to disk
	        byte[] bytes = file.getBytes();
	        Files.write(path, bytes);

	        
	        File savedFile = path.toFile();
	        if (savedFile.exists()) {
	            Newsletter newsletter = new Newsletter();
	            newsletter.setDisplayName(displayName);
	            newsletter.setFileName(newFileName);
	            newsletter.setType("TRAVEL ALLOWANCE");
	            newsletter.setReadEnabled("true");
	            newsletter.setCreatedBy(Integer.parseInt(uploadedBy.toString()));
	            Newsletter travelDocDetails = newsletterRepository.save(newsletter);
	            
	            invoiceDetails.setDocId(travelDocDetails.getDocumentId());
	            System.err.println("uploadedByinvoiceNo: " + invoiceNo);
	            travelBasedReimbursementRequestRepository.save(invoiceDetails);
                System.err.println("Doc_id"+travelDocDetails.getDocumentId());
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(travelDocDetails);
	            response.setServiceMessage("Travel Document uploaded successfully.");
	            apiLogInfo.setApiResponse("Travel Document uploaded successfully");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        } else {
	            response.setServiceResponse("Failed to upload Travel Document.");
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("File did not exist after write operation.");
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
	    return response;
	}
	

public ServiceResponse checkInvoiceNumberPresentorNot(TravelBasedReimbursementRequestDTO reimbursementDTO) {
	 
	 
	 ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("checkInvoiceNumberPresentorNot");
	    apiLogInfo.setApiUrl("/api/checkInvoiceNumberPresentorNot");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
//	    logBuilder.append("Newsletter: ").append(displayName)
//	              .append(", uploadedBy: ").append(uploadedBy);

	    try {
	    	TravelBasedReimbursementRequest invoiceDetails= travelBasedReimbursementRequestRepository.findInvoiceDetails(reimbursementDTO.getInvoiceNo());
	       if(invoiceDetails != null) {
	    	   response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Dublicate Invoice Number");
	            response.setServiceMessage("Dublicate Invoice Number");
	            apiLogInfo.setApiResponse("Dublicate Invoice Number");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        } else {
	            response.setServiceResponse("Invoice Nuber is not present");
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("Invoice Number is not present");
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
	    return response;
	
	
}








public ServiceResponse previewDocument(TravelBasedReimbursementRequestDTO reimbursementRequestDTO) {
	ServiceResponse response = new ServiceResponse();
	
	LogDTO apiLogInfo = new LogDTO();

	apiLogInfo.setSubFeatureName("Save");
	apiLogInfo.setApiUrl("/api/previewDocument");
	apiLogInfo.setLogLevel("INFO");
	StringBuilder logBuilder = new StringBuilder();
	

	
	try {


		TravelBasedReimbursementRequestDTO docDTO = new TravelBasedReimbursementRequestDTO();
		Newsletter travelDocDetails = newsletterRepository.findByDocId(reimbursementRequestDTO.getDocId());
		
				byte[] imageByte;
				try {
				    String fullPath = traveldeskFileLocation + File.separator
				   
				        + travelDocDetails.getFileName();

				    Path path = Paths.get(fullPath);

				    if (Files.exists(path)) {
				        imageByte = Files.readAllBytes(path);
				        docDTO.setDocumentBytes(imageByte);
				        docDTO.setTicketFileName(travelDocDetails.getFileName());
//				        docDto.setFileName(travelDocDetails.getFileName());
				    } else {
				        System.err.println("File not found at path: " + fullPath);
				    }
				} catch (IOException e) {
				    e.printStackTrace(); // or use a logger
				}

				


			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(docDTO);
			
			apiLogInfo.setApiResponse("Documents Found !!");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);


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







public ServiceResponse getAllInvoices() {
	ServiceResponse response = new ServiceResponse();
	List<TravelDeskDTO> details= new ArrayList();
	LogDTO apiLogInfo = new LogDTO();

	apiLogInfo.setSubFeatureName("Get");
	apiLogInfo.setApiUrl("/api/getAllInvoices");
	apiLogInfo.setLogLevel("INFO");
	StringBuilder logBuilder = new StringBuilder();
	

	
	try {


		List<Object[]> invoicesDetails=travelBasedReimbursementRequestRepository.findAllByTravel();
		
       if(invoicesDetails != null) {
    	   for(Object[] object:invoicesDetails) {
    		   TravelDeskDTO dto= new TravelDeskDTO();
    		   dto.setTravelId(object[0] != null ? Integer.parseInt(object[0].toString()) : null);
    		   dto.setInvoiceNo(object[1] != null ? object[1].toString():null);
//    		   dto.setInvoiceDate(object[2] != null ?(object[2].toString()):null);
    		   dto.setAmount(object[3] != null ? Double.parseDouble(object[3].toString()):null);
    		   dto.setDocIdTrevel(object[4] != null ? Long.parseLong(object[4].toString()) : null);
    		   dto.setFullName(object[5] != null ? object[6].toString():null);
    		   dto.setEmail(object[6] != null ? object[6].toString():null);
    		   dto.setRequestType(object[7] != null ? object[7].toString():null);
    		   dto.setTravelMode(object[8] != null ? object[8].toString() : null);
    	        dto.setTravelClass(object[9] != null ? object[9].toString() : null);
    	        dto.setFromLocation(object[10] != null ? object[10].toString() : null);
    	        dto.setToLocation(object[11] != null ? object[11].toString() : null);
    	        dto.setFromDate(object[12] != null ?  Timestamp.valueOf(object[12].toString()) : null);
    	        dto.setToDate(object[13] != null ? Timestamp.valueOf(object[13].toString()): null);
    	        dto.setStatus(object[14] != null ? object[14].toString() : null);
    	        dto.setLevel1approverRemarks(object[15] != null ? object[15].toString() : null);
//    	        dto.setLevel1ApproveBy(object[15] != null ? object[15].toString() : null);
    	        dto.setHodName(object[16] != null ? object[16].toString() : null);
    	        dto.setLevel2Approver(object[17] != null ? object[17].toString() : null);
//    	        dto.setLevel2ApproverName(object[17] != null ? object[17].toString() : null);
    	        dto.setLevel2ApproverStatus(object[18] != null ? object[18].toString() : null);
    	       
    	        
    	        
    	        details.add(dto);
    	        
    		   
    		   }
    	  
    	   
    	   
    	  
       }

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(details);
			
			apiLogInfo.setApiResponse("Documents Found !!");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);


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
