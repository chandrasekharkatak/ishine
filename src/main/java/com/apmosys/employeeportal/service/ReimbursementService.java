package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ReimbursementDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Newsletter;
import com.apmosys.employeeportal.model.ReimbursementData;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.NewsletterRepository;
import com.apmosys.employeeportal.repository.ReimbursementDataRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class ReimbursementService {
	
	@Autowired
	private ReimbursementDataRepository reimbursementDataRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private NewsletterRepository newsletterRepository;
	
	@Value("${file.location.documents.reimbursement}")
	private String reimbursementFileLocation;
	
	
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
			reimbursementData.setLevel(1);
			reimbursementData.setCurrency(reimbursementObj.getSelectedCurrency());
			reimbursementData.setExpenditureType(reimbursementObj.getExpenditureType());
			if(reimbursementObj.getExpenditureType().equalsIgnoreCase("Travel")) {
				reimbursementData.setTravelMode(reimbursementObj.getTravelMode());
				reimbursementData.setDistance(reimbursementObj.getDistance());
			}
			BigInteger approver1 = BigInteger.valueOf((reimbursementObj.getLevelOneApprover()));
			Employee level1 = employeeRepository.findByEmpId(Long.valueOf(reimbursementObj.getLevelOneApprover()));
			reimbursementData.setLevel1ApproverEmail(level1.getEmail());
			reimbursementData.setApprover1(approver1);
			reimbursementData.setFoodAllowanceType(reimbursementObj.getFoodAllowanceType());
			reimbursementData.setDateOfFood(reimbursementObj.getDateOfFood());
			reimbursementData.setFromDate(reimbursementObj.getFromDate());
			reimbursementData.setVehicleType(reimbursementObj.getVehicleType());
			reimbursementData.setDistance(reimbursementObj.getDistance());
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
	
	
	public ServiceResponse uploadFile(MultipartFile file, String displayName, Long uploadedBy) {

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
	            response.setServiceResponse("Uploaded Reimbursement Document Not Found !!");
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiResponse("Uploaded Reimbursement Document Not Found !!");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	            apiLogInfo.setApiRequest(logBuilder.toString());
	            return response;
	        }

	        // Prepare the file path
	        Path directory = Paths.get(reimbursementFileLocation);
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

	        // Confirm file existence
	        File savedFile = path.toFile();
	        if (savedFile.exists()) {
	            Newsletter newsletter = new Newsletter();
	            newsletter.setDisplayName(displayName);
	            newsletter.setFileName(newFileName); // Save actual saved name
	            newsletter.setType("Reimbursement");
	            newsletter.setReadEnabled("true");
	            newsletter.setCreatedBy(Integer.parseInt(uploadedBy.toString()));
	            Newsletter reimbursementDocDetails = newsletterRepository.save(newsletter);

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse(reimbursementDocDetails);
	            response.setServiceMessage("Reimbursement Document uploaded successfully.");
	            apiLogInfo.setApiResponse("Reimbursement Document uploaded successfully");
	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        } else {
	            response.setServiceResponse("Failed to upload Reimbursement Document.");
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
	
	@SuppressWarnings("deprecation")
	public ServiceResponse approveOrRejectReimbursement(ReimbursementDTO reimbursementObj) {
	    ServiceResponse serviceResponse = new ServiceResponse();

	    try {
			ReimbursementData existingReimbursementData = reimbursementDataRepository.findByRequestId(reimbursementObj.getRequestId());
	        
	        if (existingReimbursementData == null) {
	            serviceResponse.setServiceError("Request Data Not Found...!!");
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            return serviceResponse;
	        }

	        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
	        currentTimestamp.setNanos(currentTimestamp.getNanos() / 1000 * 1000);
            Employee emp = employeeRepository.findByEmpId(Long.valueOf(existingReimbursementData.getEmpId().toString()));
	        if (existingReimbursementData.getLevel() == 1) {
	            updateApprovalLevel(existingReimbursementData, currentTimestamp, reimbursementObj);
	            if(existingReimbursementData.getLevel1ApproveOn() != null && existingReimbursementData.getStatus() == "Approved") {
		            mailService.sendMailforReimbursement(existingReimbursementData.getLevel1ApproverEmail(),
			        		"Reimbursement request approval required",
			        		"Dear"+existingReimbursementData.getHodName()+", <br><br>" + "A Reimbursement request is applied by"+ emp.getName()+"<br> for the purpose of:"+
			        				existingReimbursementData.getPurpose()+".<br>From date:"+existingReimbursementData.getFromDate().toGMTString()+"and will return on:"+existingReimbursementData.getToDate().toGMTString()+
			        		".<br> For this the mode of travel will be:"+existingReimbursementData.getTravelMode()+".<>"+"<br>Kindly take action on this application");
		            }
	        } else if (existingReimbursementData.getLevel() == 2) {
	            updateApprovalLevel(existingReimbursementData, currentTimestamp, reimbursementObj);
	            
	        }

	        ReimbursementData updatedReimbursementData = reimbursementDataRepository.save(existingReimbursementData);
	        	        
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse(updatedReimbursementData);
	        serviceResponse.setServiceMessage("Updated Successfully..!!");

	    } catch (Exception e) {
	        serviceResponse.setServiceError(e.getMessage());
	        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        // Consider logging the error here for better debugging
	    }

	    return serviceResponse;
	}

	private void updateApprovalLevel(ReimbursementData reimbursementData, Timestamp currentTimestamp, ReimbursementDTO reimbursementObj) {
		if (reimbursementObj.getStatus().equalsIgnoreCase("Rejected")) {
			if (reimbursementData.getLevel() == 1) {
				reimbursementData.setLevel1ApproveOn(currentTimestamp);
				reimbursementData.setStatus(reimbursementObj.getStatus());
				reimbursementData.setFinalStatus(reimbursementObj.getStatus());
			}else if (reimbursementData.getLevel() == 2){
				reimbursementData.setLevel2ApproveOn(currentTimestamp);
				reimbursementData.setLevel2approverStatus(reimbursementObj.getStatus());
				reimbursementData.setFinalStatus(reimbursementObj.getStatus());
			}else if (reimbursementData.getLevel() == 3){
				reimbursementData.setLevel3ApproveOn(currentTimestamp);
				reimbursementData.setLevel3approverStatus(reimbursementObj.getStatus());
				reimbursementData.setFinalStatus(reimbursementObj.getStatus());
			}
			reimbursementData.setLevel1approverRemarks(reimbursementObj.getLevel1approverRemarks());
		}else {
	    if (reimbursementData.getLevel() == 1) {
	    	reimbursementData.setLevel1ApproveOn(currentTimestamp);
	    	reimbursementData.setLevel(reimbursementData.getLevel()+1);
	    	reimbursementData.setStatus(reimbursementObj.getStatus());
	    	reimbursementData.setLevel1approverRemarks(reimbursementObj.getLevel1approverRemarks());
	    } else if (reimbursementData.getLevel() == 2) {
	    	reimbursementData.setLevel2ApproveOn(currentTimestamp);
	    	reimbursementData.setLevel2approverStatus(reimbursementObj.getStatus());
	        reimbursementData.setFinalStatus(reimbursementObj.getStatus());
	        reimbursementData.setLevel2approverRemarks(reimbursementObj.getLevel2approverRemarks());
	    }else if (reimbursementData.getLevel() == 3) {
	    	reimbursementData.setLevel3ApproveOn(currentTimestamp);
	    	reimbursementData.setLevel3approverStatus(reimbursementObj.getStatus());
	        reimbursementData.setFinalStatus(reimbursementObj.getStatus());
	        reimbursementData.setLevel3approverRemarks(reimbursementObj.getLevel3approverRemarks());
	    }
	}
	}

	
	
}
