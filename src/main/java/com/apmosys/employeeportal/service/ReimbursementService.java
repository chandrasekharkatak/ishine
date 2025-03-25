package com.apmosys.employeeportal.service;

import java.math.BigInteger;
import java.util.List;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ReimbursementDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.ReimbursementData;
import com.apmosys.employeeportal.model.TravelDesk;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.ReimbursementDataRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class ReimbursementService {
	
	@Autowired
	private ReimbursementDataRepository reimbursementDataRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	
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

	public ServiceResponse saveReimbursementData(ReimbursementDTO reimbursementObj) {
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
			reimbursementData.setExpenditureType(reimbursementObj.getExpenditureType());
			if(reimbursementObj.getExpenditureType().equalsIgnoreCase("Travel")) {
				reimbursementData.setTravelMode(reimbursementObj.getTravelMode());
				reimbursementData.setDistance(reimbursementObj.getDistance());
			}
			reimbursementData.setFromDate(reimbursementObj.getFromDate());
			reimbursementData.setToDate(reimbursementObj.getToDate());
			reimbursementData.setPurpose(reimbursementObj.getPurpose());			
			reimbursementData.setIsActive(1);
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
	
	public ServiceResponse updateReimbursementData(ReimbursementDTO reimbursementObj) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			ReimbursementData existingReimbursementData = reimbursementDataRepository.findByRequestId(reimbursementObj.getRequestId());
			if(existingReimbursementData == null) {
				serviceResponse.setServiceError("Data Not Found...!!");
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return serviceResponse;
			}
			else {
				existingReimbursementData.setEmpId(reimbursementObj.getEmpId());
				existingReimbursementData.setEmail(reimbursementObj.getEmail());
				existingReimbursementData.setMobileNo(reimbursementObj.getMobileNo());
				existingReimbursementData.setDepartment(reimbursementObj.getDepartmentName());
				existingReimbursementData.setFullName(reimbursementObj.getName());
				existingReimbursementData.setExpenditureType(reimbursementObj.getExpenditureType());
				if(reimbursementObj.getExpenditureType().equalsIgnoreCase("Travel")) {
					existingReimbursementData.setTravelMode(reimbursementObj.getTravelMode());
					existingReimbursementData.setDistance(reimbursementObj.getDistance());
				}
				existingReimbursementData.setEmail(reimbursementObj.getEmail());
				existingReimbursementData.setAmount(reimbursementObj.getAmount());
				existingReimbursementData.setCurrency(reimbursementObj.getSelectedCurrency());
				existingReimbursementData.setPurpose(reimbursementObj.getPurpose());
				existingReimbursementData.setFromDate(reimbursementObj.getFromDate());
				existingReimbursementData.setToDate(reimbursementObj.getToDate());
				existingReimbursementData.setAppliedBy(reimbursementObj.getEmpId());
				Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
				currentTimestamp.setNanos(currentTimestamp.getNanos() / 1000 * 1000);
				System.out.println(currentTimestamp);
				existingReimbursementData.setAppliedOn(currentTimestamp);
				existingReimbursementData.setStatus("Pending");
//				BigInteger bigInteger = new BigInteger(numberString);
				//BigInteger approver = BigInteger.valueOf(reimbursementObj.getLevelOneApprover());
				//existingReimbursementData.setApprover1(approver);
				existingReimbursementData.setLevel(1);
				//BigInteger approver2 = new BigInteger(level2Approver);
				//existingReimbursementData.setApprover2(approver2);
				existingReimbursementData.setIsActive(1);
				ReimbursementData savedReimbursementdata = reimbursementDataRepository.save(existingReimbursementData);
				if(savedReimbursementdata == null) {
					serviceResponse.setServiceError("Unable to update...!!");
					serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
					return serviceResponse;
				}
				else {
					serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					serviceResponse.setServiceResponse(savedReimbursementdata);
					serviceResponse.setServiceMessage("Updated Successfully..!!");
//					 Employee emp = employeeRepository.findByEmpId(Long.valueOf(existingReimbursementData.getEmpId().toString()));
//						mailService.sendMailforTravel(existingReimbursementData.getLevel1ApproverEmail(),
//				        		"Travel request Updated",
//				        		"Dear "+existingReimbursementData.getHodName()+", <br><br>" + "A travel request that was applied by "+ emp.getName()+" is upadated and now <br>the purpose is : "+
//				        				existingTravelDesk.getPurpose()+".<br>the From date is : "+existingReimbursementData.getFromDate().toGMTString()+" and will return on : "+existingTravelDesk.getToDate().toGMTString()+
//				        		".<br> For this the mode of travel will be : "+existingTravelDesk.getTravelMode()+" and travel class is : "+existingTravelDesk.getTravelClass()+"<br>Kindly take action on this application .");

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
	
	
	public ServiceResponse revokeReimbursement(BigInteger requestId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
		ReimbursementData existingReimbursementData = reimbursementDataRepository.findByRequestId(requestId);

		if(existingReimbursementData == null) {
			serviceResponse.setServiceError("Request Data Not Found...!!");
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			return serviceResponse;
		}
		else {
			existingReimbursementData.setIsActive(0);
			reimbursementDataRepository.save(existingReimbursementData);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			serviceResponse.setServiceResponse(existingReimbursementData);
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
