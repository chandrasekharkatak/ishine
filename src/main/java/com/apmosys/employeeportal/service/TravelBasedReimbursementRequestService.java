package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.TravelBasedReimbursementRequestDTO;
import com.apmosys.employeeportal.model.TravelBasedReimbursementRequest;
import com.apmosys.employeeportal.repository.TravelBasedReimbursementRequestRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class TravelBasedReimbursementRequestService {
     
	@Autowired
	private TravelBasedReimbursementRequestRepository travelBasedReimbursementRequestRepository;
	
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
				    System.err.println("Invoice No: " + request.getInvoiceNo());
				    System.err.println("Invoice Date: " + request.getInvoiceDate());
				    System.err.println("Amount: " + request.getAmount());
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
	
	
}
