package com.apmosys.employeeportal.service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.TravelDeskDTO;
import com.apmosys.employeeportal.model.TravelDesk;
import com.apmosys.employeeportal.repository.TravelDeskRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class TravelDeskService {

	@Autowired
	private TravelDeskRepository tavelDeskRepository;
	
	@Value("${level2Approver}")
	public String level2Approver;
	
	
	@SuppressWarnings("unused")
	public ServiceResponse saveTravelData(TravelDeskDTO travelData) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
		TravelDesk travelDesk = new TravelDesk();
		TravelDesk savedTravelDesk = new TravelDesk();
		
		travelDesk.setEmpId(travelData.getEmployeeId());
		travelDesk.setEmail(travelData.getEmail());
		if(travelData.getMobileNo() != null) {
			travelDesk.setMobileNo(travelData.getMobileNo());
		}
		else {
			travelDesk.setMobileNo(BigInteger.valueOf(98996712));
		}
		travelDesk.setDepartment(travelData.getDepartmentName());
		travelDesk.setName(travelData.getFullName());
		travelDesk.setHodName(travelData.getHodName());
		travelDesk.setRequestType(travelData.getAssociatedTravelRequest());
		travelDesk.setTravelMode(travelData.getTravelMode());
		travelDesk.setTravelClass(travelData.getTravelClass());
		travelDesk.setPurpose(travelData.getPurposeOfTravel());
		travelDesk.setFromLocation(travelData.getFromLocation());
		travelDesk.setFromDate(travelData.getFromDate());
		travelDesk.setToLocation(travelData.getToLocation());
		travelDesk.setToDate(travelData.getToDate());
		Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
		currentTimestamp.setNanos(currentTimestamp.getNanos() / 1000 * 1000);
		System.out.println(currentTimestamp);
		travelDesk.setAppliedOn(currentTimestamp);
		travelDesk.setStatus("Pending");
		BigInteger approver1 = BigInteger.valueOf(travelData.getLevelOneApprover());
		travelDesk.setApprover1(approver1);
		BigInteger approver2 = new BigInteger(level2Approver);
		travelDesk.setApprover2(approver2);
		travelDesk.setLevel(1);
		travelDesk.setIsActive(1);
		System.out.println(travelDesk);
		savedTravelDesk=tavelDeskRepository.save(travelDesk);
		
		if(savedTravelDesk == null) {
			serviceResponse.setServiceError("Unable to save..!!");
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceMessage("Failed to save...!!");
			return serviceResponse;
		}
		else {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(savedTravelDesk);
			serviceResponse.setServiceMessage("Saved Successfully..!!");
			return serviceResponse;
		}
	}
	catch(Exception e) {
		e.printStackTrace();
		serviceResponse.setServiceError(e.getMessage());
		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		return serviceResponse;
	}
		
}
	
	public ServiceResponse fetchUserTravel(BigInteger empId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			List<TravelDesk> travelDesk = tavelDeskRepository.findByEmpId(empId);
			
			  List<TravelDesk> activeTravelDesk = travelDesk.stream()
			            .filter(t -> t.getIsActive() != 0)  
			            .collect(Collectors.toList());
			  
			if(activeTravelDesk.isEmpty()) {
				serviceResponse.setServiceError("Data Not Found...!!");
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return serviceResponse;
			}
			else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(activeTravelDesk);
				return serviceResponse;
			}
		}
		catch(Exception e){
			serviceResponse.setServiceError(e.getMessage());
			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			return serviceResponse;
		}
	}
	
	@SuppressWarnings("unused")
	public ServiceResponse updateTravelData(TravelDeskDTO travelData) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			TravelDesk existingTravelDesk = tavelDeskRepository.findByRequestId(travelData.getRequestId());
			if(existingTravelDesk == null) {
				serviceResponse.setServiceError("Data Not Found...!!");
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return serviceResponse;
			}
			else {
				existingTravelDesk.setEmpId(travelData.getEmployeeId());
				existingTravelDesk.setEmail(travelData.getEmail());
				existingTravelDesk.setMobileNo(travelData.getMobileNo());
				existingTravelDesk.setDepartment(travelData.getDepartmentName());
				existingTravelDesk.setName(travelData.getFullName());
				existingTravelDesk.setRequestType(travelData.getAssociatedTravelRequest());
				existingTravelDesk.setTravelMode(travelData.getTravelMode());
				existingTravelDesk.setTravelClass(travelData.getTravelClass());
				existingTravelDesk.setPurpose(travelData.getPurposeOfTravel());
				existingTravelDesk.setFromLocation(travelData.getFromLocation());
				existingTravelDesk.setFromDate(travelData.getFromDate());
				existingTravelDesk.setToLocation(travelData.getToLocation());
				existingTravelDesk.setToDate(travelData.getToDate());
				existingTravelDesk.setAppliedBy(travelData.getEmployeeId());
				Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
				currentTimestamp.setNanos(currentTimestamp.getNanos() / 1000 * 1000);
				System.out.println(currentTimestamp);
				existingTravelDesk.setAppliedOn(currentTimestamp);
				existingTravelDesk.setStatus("Pending");
//				BigInteger bigInteger = new BigInteger(numberString);
				BigInteger approver = BigInteger.valueOf(travelData.getLevelOneApprover());
				existingTravelDesk.setApprover1(approver);
				existingTravelDesk.setLevel(1);
				BigInteger approver2 = new BigInteger(level2Approver);
				existingTravelDesk.setApprover2(approver2);
				existingTravelDesk.setIsActive(1);
				TravelDesk savedTravelDesk = tavelDeskRepository.save(existingTravelDesk);
				if(savedTravelDesk == null) {
					serviceResponse.setServiceError("Unable to update...!!");
					serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
					return serviceResponse;
				}
				else {
					serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					serviceResponse.setServiceResponse(savedTravelDesk);
					serviceResponse.setServiceMessage("Updated Successfully..!!");
					return serviceResponse;
				}
			}
			
		}
		catch(Exception e) {
			serviceResponse.setServiceError(e.getMessage());
			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			return serviceResponse;
		}
		
	}
	
	public ServiceResponse revokeTravel(BigInteger requestId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
		TravelDesk travelDesk = tavelDeskRepository.findByRequestId(requestId);
		if(travelDesk == null) {
			serviceResponse.setServiceError("Request Data Not Found...!!");
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			return serviceResponse;
		}
		else {
			travelDesk.setIsActive(0);
			tavelDeskRepository.save(travelDesk);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(travelDesk);
			return serviceResponse;
		}
		}
		catch(Exception e) {
			serviceResponse.setServiceError(e.getMessage());
			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			return serviceResponse;
		}
		
		
	}
	
	
	public ServiceResponse approveRejectTravel(TravelDeskDTO travelData) {
	    ServiceResponse serviceResponse = new ServiceResponse();

	    try {
	        TravelDesk travelDesk = tavelDeskRepository.findByRequestId(travelData.getRequestId());
	        
	        if (travelDesk == null) {
	            serviceResponse.setServiceError("Request Data Not Found...!!");
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            return serviceResponse;
	        }

	        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
	        currentTimestamp.setNanos(currentTimestamp.getNanos() / 1000 * 1000);

	        if (travelDesk.getLevel() == 1) {
	            updateApprovalLevel(travelDesk, currentTimestamp, travelData);
	        } else if (travelDesk.getLevel() == 2) {
	            updateApprovalLevel(travelDesk, currentTimestamp, travelData);
	        }

	        TravelDesk updatedTravelDesk = tavelDeskRepository.save(travelDesk);
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse(updatedTravelDesk);
	        serviceResponse.setServiceMessage("Updated Successfully..!!");

	    } catch (Exception e) {
	        serviceResponse.setServiceError(e.getMessage());
	        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        // Consider logging the error here for better debugging
	    }

	    return serviceResponse;
	}

	private void updateApprovalLevel(TravelDesk travelDesk, Timestamp currentTimestamp, TravelDeskDTO travelData) {
	    if (travelDesk.getLevel() == 1) {
	        travelDesk.setLevel1ApproveOn(currentTimestamp);
	        travelDesk.setLevel(travelDesk.getLevel()+1);
	        travelDesk.setStatus(travelData.getStatus());
	        travelDesk.setLevel1approverRemarks(travelDesk.getLevel1approverRemarks());
	    } else if (travelDesk.getLevel() == 2) {
	        travelDesk.setLevel2ApproveOn(currentTimestamp);
	        travelDesk.setStatus(travelData.getStatus());
	        travelDesk.setLevel2approverRemarks(travelDesk.getLevel2approverRemarks());
	    }
	}

}
