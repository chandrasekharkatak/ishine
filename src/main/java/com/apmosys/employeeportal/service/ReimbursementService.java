package com.apmosys.employeeportal.service;

import java.math.BigInteger;
import java.util.List;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ReimbursementDTO;
import com.apmosys.employeeportal.model.ReimbursementData;
import com.apmosys.employeeportal.model.TravelDesk;
import com.apmosys.employeeportal.repository.ReimbursementDataRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class ReimbursementService {
	
	@Autowired
	private ReimbursementDataRepository reimbursementDataRepository;
	
	
	public ServiceResponse fetchReimbursementData(BigInteger empId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			List<ReimbursementData> reimbursementData = reimbursementDataRepository.findByEmpId(empId);
			if(reimbursementData.isEmpty() || reimbursementData == null) {
				serviceResponse.setServiceError("Data Not Found...!!");
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return serviceResponse;
			}
			else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(reimbursementData);
				return serviceResponse;
			}
		}
		catch(Exception e){
			serviceResponse.setServiceError(e.getMessage());
			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			return serviceResponse;
		}
	}

	public ServiceResponse saveTravelData(ReimbursementDTO reimbursementObj) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			
			ReimbursementData reimbursementData = new ReimbursementData();
			ReimbursementData savedReimbursementData = new ReimbursementData();
			
			reimbursementData.setEmpId(reimbursementObj.getEmpId());
			reimbursementData.setFullName(reimbursementObj.getName());
			reimbursementData.setDepartment(reimbursementObj.getDepartmentName());
			reimbursementData.setEmail(reimbursementObj.getEmail());
			reimbursementData.setAmount(reimbursementObj.getAmount());
			reimbursementData.setCurrency(reimbursementObj.getSelectedCurrency());
			reimbursementData.setExpenditureType(reimbursementObj.getSelectedReason());
			if(reimbursementObj.getSelectedReason().equalsIgnoreCase("Travel")) {
				reimbursementData.setTravelMode(reimbursementObj.getTravelMode());
				reimbursementData.setDistance(reimbursementObj.getDistance());
			}
			reimbursementData.setFromDate(reimbursementObj.getFromDate());
			reimbursementData.setToDate(reimbursementObj.getToDate());
			reimbursementData.setActive(true);
			reimbursementData.setAppliedBy(reimbursementObj.getEmpId());
			Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
			currentTimestamp.setNanos(currentTimestamp.getNanos() / 1000 * 1000);
			System.out.println(currentTimestamp);
			reimbursementData.setAppliedOn(currentTimestamp);
//			BigInteger bigInteger = new BigInteger(numberString);
			BigInteger approver = BigInteger.valueOf(21329);
			reimbursementData.setApprover(approver);
			reimbursementData.setStatus("Pending");
			savedReimbursementData = reimbursementDataRepository.save(reimbursementData);
			
			if(savedReimbursementData == null) {
				serviceResponse.setServiceError("Unable to save..!!");
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceMessage("Failed to save...!!");
				return serviceResponse;
			}
			else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(savedReimbursementData);
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
	
}
