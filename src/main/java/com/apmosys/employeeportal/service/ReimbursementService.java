package com.apmosys.employeeportal.service;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.model.ReimbursementData;
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

}
