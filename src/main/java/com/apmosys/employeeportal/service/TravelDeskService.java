package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.naming.factory.SendMailFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.TravelDeskDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Newsletter;
import com.apmosys.employeeportal.model.TravelDesk;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.NewsletterRepository;
import com.apmosys.employeeportal.repository.TravelDeskRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class TravelDeskService {

	@Autowired
	private TravelDeskRepository tavelDeskRepository;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private NewsletterRepository newsletterRepository;
	
	@Value("${level2Approver}")
	public String level2Approver;
	
	@Value("${file.location.documents.travelDesk}")
	private String traveldeskFileLocation;
	
	
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
		travelDesk.setHotelCategory(travelData.getHotelCategory());
		travelDesk.setCityCategory(travelData.getCityCategory());
		travelDesk.setTravelMode(travelData.getTravelMode());
		travelDesk.setTravelClass(travelData.getTravelClass());
		travelDesk.setPurpose(travelData.getPurposeOfTravel());
		travelDesk.setFromLocation(travelData.getFromLocation());
		travelDesk.setFromDate(travelData.getFromDate());
		travelDesk.setToLocation(travelData.getToLocation());
		travelDesk.setToDate(travelData.getToDate());
		travelDesk.setDocId(travelData.getDocId());
		Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
		currentTimestamp.setNanos(currentTimestamp.getNanos() / 1000 * 1000);
		System.out.println(currentTimestamp);
		travelDesk.setAppliedOn(currentTimestamp);
		travelDesk.setStatus("Pending");
		BigInteger approver1 = BigInteger.valueOf(travelData.getLevelOneApprover());
		travelDesk.setApprover1(approver1);
		Employee level1 = employeeRepository.findByEmpId(Long.valueOf(travelData.getLevelOneApprover()));
        travelDesk.setLevel1ApproverEmail(level1.getEmail());
		
		BigInteger approver2 = new BigInteger(level2Approver);
		travelDesk.setApprover2(approver2);
		travelDesk.setLevel(1);
		travelDesk.setIsActive(1);
		travelDesk.setLevel2approverStatus("Pending");
		Employee level2 = employeeRepository.findByEmpId(Long.valueOf(travelDesk.getApprover2().toString()));
		travelDesk.setLevel2ApproverEmail(level2.getEmail());
		travelDesk.setLevel2approverName(level2.getName());
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
			File file = null;
            Employee emp = employeeRepository.findByEmpId(Long.valueOf(travelDesk.getEmpId().toString()));
			mailService.sendMailWithAttachment(travelDesk.getLevel1ApproverEmail(),travelDesk.getEmail(),
	        		"Travel request approval required",
	        		"Dear"+travelDesk.getHodName()+", <br><br>" + "A travel request is applied by "+ emp.getName()+"<br> for the purpose of : "+
	        		travelDesk.getPurpose()+".<br>From date:"+travelDesk.getFromDate().toGMTString()+" and will return on : "+travelDesk.getToDate().toGMTString()+
	        		".<br> For this the mode of travel will be : "+travelDesk.getTravelMode()+" and travel class is:"+travelDesk.getTravelClass()+" .<br>Kindly take action on this application .",file);

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
				existingTravelDesk.setHotelCategory(travelData.getHotelCategory());
				existingTravelDesk.setCityCategory(travelData.getCityCategory());
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
				Employee level1 = employeeRepository.findByEmpId(Long.valueOf(travelData.getLevelOneApprover()));
				existingTravelDesk.setLevel1ApproverEmail(level1.getEmail());
				existingTravelDesk.setLevel(1);
				BigInteger approver2 = new BigInteger(level2Approver);
				existingTravelDesk.setApprover2(approver2);
				existingTravelDesk.setIsActive(1);
				existingTravelDesk.setLevel2approverStatus("Pending");
				Employee level2 = employeeRepository.findByEmpId(Long.valueOf(travelData.getLevel2Approver().toString()));
				existingTravelDesk.setLevel2ApproverEmail(level2.getEmail());
				existingTravelDesk.setLevel2approverName(level2.getName());
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
					 Employee emp = employeeRepository.findByEmpId(Long.valueOf(existingTravelDesk.getEmpId().toString()));
						mailService.sendMailforTravel(existingTravelDesk.getLevel1ApproverEmail(),
				        		"Travel request Updated",
				        		"Dear "+existingTravelDesk.getHodName()+", <br><br>" + "A travel request that was applied by "+ emp.getName()+" is upadated and now <br>the purpose is : "+
				        				existingTravelDesk.getPurpose()+".<br>the From date is : "+existingTravelDesk.getFromDate().toGMTString()+" and will return on : "+existingTravelDesk.getToDate().toGMTString()+
				        		".<br> For this the mode of travel will be : "+existingTravelDesk.getTravelMode()+" and travel class is : "+existingTravelDesk.getTravelClass()+"<br>Kindly take action on this application .");

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
	
	
	@SuppressWarnings("deprecation")
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
            Employee emp = employeeRepository.findByEmpId(Long.valueOf(travelDesk.getEmpId().toString()));
	        if (travelDesk.getLevel() == 1) {
	            updateApprovalLevel(travelDesk, currentTimestamp, travelData);
	            if(travelDesk.getLevel1ApproveOn() != null && travelDesk.getStatus() == "Approved") {
		            mailService.sendMailforTravel(travelDesk.getLevel1ApproverEmail(),
			        		"Travel request approval required",
			        		"Dear"+travelDesk.getHodName()+", <br><br>" + "A travel request is applied by"+ emp.getName()+"<br> for the purpose of:"+
			        		travelDesk.getPurpose()+".<br>From date:"+travelDesk.getFromDate().toGMTString()+"and will return on:"+travelDesk.getToDate().toGMTString()+
			        		".<br> For this the mode of travel will be:"+travelDesk.getTravelMode()+"and travle class is:"+travelDesk.getTravelClass()+".<>"+"<br>Kindly take action on this application");
		            }
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
		if (travelData.getStatus().equals("Rejected")) {
			travelDesk.setStatus(travelData.getStatus());
			travelDesk.setLevel1approverRemarks(travelData.getLevel1approverRemarks());
		}else {
	    if (travelDesk.getLevel() == 1) {
	        travelDesk.setLevel1ApproveOn(currentTimestamp);
	        travelDesk.setLevel(travelDesk.getLevel()+1);
	        travelDesk.setStatus(travelData.getStatus());
	        travelDesk.setLevel1approverRemarks(travelData.getLevel1approverRemarks());
	    } else if (travelDesk.getLevel() == 2) {
	        travelDesk.setLevel2ApproveOn(currentTimestamp);
	        travelDesk.setStatus(travelData.getStatus());
	        travelDesk.setLevel2approverRemarks(travelData.getLevel2approverRemarks());
	    }
	}
	}
	
	
//public ServiceResponse uploadFile(MultipartFile file, String displayName, Long uploadedBy) {
//		
//		ServiceResponse response = new ServiceResponse();
//		
//		LogDTO apiLogInfo = new LogDTO();
//		apiLogInfo.setSubFeatureName("Upload Newsletter");
//		apiLogInfo.setApiUrl("/api/uploadFile");
//		apiLogInfo.setLogLevel("INFO");
//		StringBuilder logBuilder = new StringBuilder();
//		logBuilder.append("Newsletter : " +displayName+ "uploadedBy : " +uploadedBy);
//		List<File> savedFiles = new ArrayList<File>();
//		String errorMsg = "";
//		
//		try {
//			System.out.println("uploadedBy : " + uploadedBy);
//			Optional<Employee> employeeObject = employeeRepository.findById(uploadedBy);
//			
//			if (employeeObject.isPresent()) {
//				if (file != null) {
//
//					byte[] bytes = file.getBytes();
//
//					Path path = Paths.get(traveldeskFileLocation + File.separator + file.getOriginalFilename());
//					File checkExistingFile = new File(path.toString());
//					//if (!checkExistingFile.exists()) {
//						Files.write(path, bytes);
//						File savedFile = new File(path.toString());
//
//						if (savedFile.exists()) {
//							Newsletter newsletter = new Newsletter();
//							newsletter.setDisplayName(displayName);
//							newsletter.setFileName(file.getOriginalFilename());
//							newsletter.setType("TRAVEL ALLOWANCE");
//							newsletter.setReadEnabled("true");
//							newsletter.setCreatedBy(Integer.parseInt(uploadedBy.toString()));
//
//							newsletterRepository.save(newsletter);
//
//							response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//							response.setServiceResponse("Travel Document uploaded successfully.");
//
//							apiLogInfo.setApiResponse("Travel Document uploaded successfully");
//							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//						} else {
//							response.setServiceResponse("Failed to upload Travel Document.");
//							response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//
//							apiLogInfo.setApiResponse("Failed to upload Travel Document.");
//							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//						}
//				//	} else {
//						response.setServiceResponse(
//								"Travel Document named " + file.getOriginalFilename() + " already exist.");
//						response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//
//						apiLogInfo.setApiResponse("Travel Document named " + file.getOriginalFilename() + " already exist.");
//						apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//					//}
//
//				} else {
//					response.setServiceResponse("Uploaded Travel Document Not Found !!");
//					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//
//					apiLogInfo.setApiResponse("Uploaded Travel Document Not Found !!");
//					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//				}
//
//			} else {
//				response.setServiceResponse("User Not Found !!");
//				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//
//				apiLogInfo.setApiResponse("User Not Found !!");
//				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			}
//
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//			response.setServiceResponse("Something Went Wrong.");
//			response.setServiceError(e.getMessage());
//			
//			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//			apiLogInfo.setLogLevel("ERROR");
//			
//			
//		}
//		apiLogInfo.setApiRequest(logBuilder.toString());
//		return response;
//		
//	}
//
	
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

	        // Confirm file existence
	        File savedFile = path.toFile();
	        if (savedFile.exists()) {
	            Newsletter newsletter = new Newsletter();
	            newsletter.setDisplayName(displayName);
	            newsletter.setFileName(newFileName); // Save actual saved name
	            newsletter.setType("TRAVEL ALLOWANCE");
	            newsletter.setReadEnabled("true");
	            newsletter.setCreatedBy(Integer.parseInt(uploadedBy.toString()));
	            Newsletter travelDocDetails = newsletterRepository.save(newsletter);

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



}
