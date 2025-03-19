package com.apmosys.employeeportal.service;

import java.math.BigInteger;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.TravelDeskDTO;
import com.apmosys.employeeportal.model.TravelDesk;
import com.apmosys.employeeportal.repository.TravelDeskRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class TravelDeskService {

	@Autowired
	private TravelDeskRepository tavelDeskRepository;
	
	
	public ServiceResponse saveTravelData(TravelDeskDTO travelData) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
		TravelDesk travelDesk = new TravelDesk();
		TravelDesk savedTravelDesk = new TravelDesk();
		
		travelDesk.setEmpId(travelData.getEmployeeId());
		travelDesk.setEmail(travelData.getEmail());
		travelDesk.setMobileNo(travelData.getMobileNo());
		travelDesk.setDepartment(travelData.getDepartmentName());
		travelDesk.setName(travelData.getFullName());
		travelDesk.setRequestType(travelData.getAssociatedTravelRequest());
		travelDesk.setTravelMode(travelData.getTravelMode());
		travelDesk.setTravelClass(travelData.getTravelClass());
		travelDesk.setPurpose(travelData.getPurposeOfTravel());
		travelDesk.setFromLocation(travelData.getFromLocation());
		travelDesk.setFromDate(travelData.getFromDate());
		travelDesk.setToLocation(travelData.getToLocation());
		travelDesk.setToDate(travelData.getToDate());
		travelDesk.setAppliedBy(travelData.getEmployeeId());
		Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
		currentTimestamp.setNanos(currentTimestamp.getNanos() / 1000 * 1000);
		System.out.println(currentTimestamp);
		travelDesk.setAppliedOn(currentTimestamp);
		travelDesk.setStatus("Pending");
//		BigInteger bigInteger = new BigInteger(numberString);
		BigInteger approver = BigInteger.valueOf(21329);
		travelDesk.setApprover(approver);
		travelDesk.setLevel(1);
		travelDesk.setActive(true);
		savedTravelDesk = tavelDeskRepository.save(travelDesk);
		if(savedTravelDesk == null) {
			serviceResponse.setServiceError("Unable to save..!!");
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			return serviceResponse;
		}
		else {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(savedTravelDesk);
			return serviceResponse;
		}
		}
		catch(Exception e) {
			serviceResponse.setServiceError(e.getMessage());
			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			return serviceResponse;
		}
		
	}
}
