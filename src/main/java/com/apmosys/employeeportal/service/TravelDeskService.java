package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.CityDTO;
import com.apmosys.employeeportal.dto.HotelCategoryDTO;
import com.apmosys.employeeportal.dto.HotelSubCategoryDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.TravelClassRequest;
import com.apmosys.employeeportal.dto.TravelDeskDTO;
import com.apmosys.employeeportal.dto.TravelModeDTO;
import com.apmosys.employeeportal.dto.TravelReasonDTO;
import com.apmosys.employeeportal.model.City;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.HotelCategory;
import com.apmosys.employeeportal.model.HotelSubCategory;
import com.apmosys.employeeportal.model.Newsletter;
import com.apmosys.employeeportal.model.TravelClass;
import com.apmosys.employeeportal.model.TravelDesk;
import com.apmosys.employeeportal.model.TravelMode;
import com.apmosys.employeeportal.model.TravelReason;
import com.apmosys.employeeportal.repository.CityRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.HotelCategoryRepository;
import com.apmosys.employeeportal.repository.HotelSubCategoryRepository;
import com.apmosys.employeeportal.repository.NewsletterRepository;
import com.apmosys.employeeportal.repository.TravelClassRepository;
import com.apmosys.employeeportal.repository.TravelDeskRepository;
import com.apmosys.employeeportal.repository.TravelModeRepository;
import com.apmosys.employeeportal.repository.TravelReasonRepository;
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
	
	@Autowired 
	private TravelReasonRepository travelReasonRepository;
	
	@Autowired
	private TravelModeRepository travelModeRepository;
	
	 @Autowired
	 private TravelClassRepository travelClassRepository;
	 
	 @Autowired
	 private HotelCategoryRepository hotelCategoryRepository;
	 
	 
	 @Autowired
	 private HotelSubCategoryRepository hotelSubCategoryRepository;
	 
	 @Autowired
	 private CityRepository cityRepository;
	
	@Value("${level2Approver}")
	public String level2Approver;
	
	@Value("${file.location.documents.travelDesk}")
	private String traveldeskFileLocation;
	
	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;
	
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
//		travelDesk.setHodName(travelData.getHodName());
		travelDesk.setRequestType(travelData.getAssociatedTravelRequest());
		travelDesk.setHotelCategory(travelData.getHotelCategory());
		travelDesk.setCityCategory(travelData.getCityCategory());
		travelDesk.setCity(travelData.getCity());
		travelDesk.setTravelMode(travelData.getTravelMode());
		travelDesk.setTravelClass(travelData.getTravelClass());
		travelDesk.setPurpose(travelData.getPurposeOfTravel());
		travelDesk.setFromLocation(travelData.getFromLocation());
		travelDesk.setFromDate(travelData.getFromDate());
		travelDesk.setToLocation(travelData.getToLocation());
		travelDesk.setToDate(travelData.getToDate());

		   if (travelData.getDocId() != null && !travelData.getDocId().isEmpty()) {
	            String docIdsString = String.join(",", travelData.getDocId().stream().map(String::valueOf).toArray(String[]::new));
	            travelDesk.setDocId(docIdsString);
	        }else {
	        	
	        	travelDesk.setDocId("NA");
	        }
		   
		   if(travelData.getKycDocumentId() != null) {
			   travelDesk.setKycDocumentId(travelData.getKycDocumentId());
			   
		   }else {
			   travelDesk.setKycDocumentId(null);
		   }
		//travelDesk.setDocId(travelData.getDocId());
		Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
		currentTimestamp.setNanos(currentTimestamp.getNanos() / 1000 * 1000);
		System.out.println(currentTimestamp);
		travelDesk.setAppliedOn(currentTimestamp);
		
		travelDesk.setStatus("Pending");
//		BigInteger approver1 = BigInteger.valueOf(travelData.getLevelOneApprover());
		String approver1 = travelData.getReportingManagerId();

		
//		Employee level1 = employeeRepository.findByEmpId(Long.valueOf(travelData.getLevelOneApprover()));
		Long reportingManagerId = Long.parseLong(travelData.getReportingManagerId());
		Timestamp currentTimestamp1 = new Timestamp(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String timestampString = sdf.format(currentTimestamp);
		List<Object[]> details=employeeLeaveRepository.reportingManagerIsOnLeave(reportingManagerId);
		if(details != null && !details.isEmpty()) {	
			BigInteger approver2 = BigInteger.valueOf(travelData.getLevelOneApprover());
			travelDesk.setApprover1(approver2.toString());
			Employee level1 = employeeRepository.findByEmpId(Long.valueOf(approver2.toString()));
	        travelDesk.setLevel1ApproverEmail(level1.getEmail());
			travelDesk.setHodName(level1.getName());
		}else {
			travelDesk.setApprover1(approver1);
			Employee level1 = employeeRepository.findByEmpId(reportingManagerId);
	        travelDesk.setLevel1ApproverEmail(level1.getEmail());
			travelDesk.setHodName(level1.getName());
		}
		

		
//		BigInteger approver2 = new BigInteger(level2Approver);
        BigInteger approver2 = BigInteger.valueOf(travelData.getLevelOneApprover());
		travelDesk.setApprover2(approver2);
		travelDesk.setLevel(1);
		travelDesk.setIsActive(1);
		travelDesk.setLevel2approverStatus("Pending");
		travelDesk.setFinalStatus("Pending");
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
	//		this.getTemplateFile(Long.parseLong(travelDesk.getDocId())); 
			//Resource resource = getTemplateFile(documentId);
//			File file = null;
//			try {
//			    Resource resource = getTemplateFile(Long.parseLong(travelDesk.getDocId()));
//			    if (resource != null && resource.exists()) {
//			        file = resource.getFile(); 
//			    }
//			} catch (IOException e) {
//			    e.printStackTrace();
//			}
			File file = null;
			try {
			    String docIdsString = savedTravelDesk.getDocId();
			    String[] docIdsArray = docIdsString.split(",");

			    for (String docIdStr : docIdsArray) {
			        try {
			            Long docId = Long.parseLong(docIdStr.trim());
			            Resource resource = getTemplateFile(docId);
			            if (resource != null && resource.exists()) {
			                file = resource.getFile();
			                break;
			            }
			        } catch (NumberFormatException e) {
			            System.out.println("Invalid docId: " + docIdStr);
			        }
			    }
			} catch (IOException e) {
			    e.printStackTrace();
			}
			
            Employee emp = employeeRepository.findByEmpId(Long.valueOf(travelDesk.getEmpId().toString()));
            if(savedTravelDesk.getRequestType().equalsIgnoreCase("HOTEL")) {
            	mailService.sendMailWithAttachment(travelDesk.getLevel1ApproverEmail(),travelDesk.getEmail(),
    	        		"Travel request approval required",
    	        		"Dear"+travelDesk.getHodName()+", <br><br>" + "A request is applied by "+ emp.getName()+"<br> for the purpose of : "+
    	        		travelDesk.getPurpose()+".<br>From date:"+travelDesk.getFromDate().toGMTString()+" and will return on : "+travelDesk.getToDate().toGMTString()+
    	        		".<br> For this he/she requires a stay at: "+travelDesk.getCity()+".<br>Kindly take action on this application .",file);

            }else {
            	
            	mailService.sendMailWithAttachment(travelDesk.getLevel1ApproverEmail(),travelDesk.getEmail(),
    	        		"Travel request approval required",
    	        		"Dear"+travelDesk.getHodName()+", <br><br>" + "A request is applied by "+ emp.getName()+"<br> for the purpose of : "+
    	        		travelDesk.getPurpose()+".<br>From date:"+travelDesk.getFromDate().toGMTString()+" and will return on : "+travelDesk.getToDate().toGMTString()+
    	        		".<br> For this he requires a stay in : "+travelDesk.getTravelMode()+" and travel class is:"+travelDesk.getTravelClass()+" .<br>Kindly take action on this application .",file);

            }
    		serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
    		serviceResponse.setServiceResponse("Success! Your request is processed successfully!\nAgainst requestId " + savedTravelDesk.getRequestId());
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
//			  List<TravelDesk> activeTravelDeskL1 = activeTravelDesk.stream()
//			            .filter(t -> t.getApprover1() == empId && t.getStatus().equalsIgnoreCase("Pending")&& t.getIsActive() != 0)  
//			            .collect(Collectors.toList());
//			  List<TravelDesk> activeTravelDeskL2 = activeTravelDesk.stream()
//			            .filter(t -> t.getApprover2() == empId && t.getLevel2approverStatus().equalsIgnoreCase("Pending") && t.getIsActive() != 0)  
//			            .collect(Collectors.toList());
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
	
	public ServiceResponse fetchUserTravelForApproval(BigInteger empId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			 List<TravelDesk> activeTravelDesk = new ArrayList<>() ;
			System.out.println(empId);
			List<TravelDesk> travelDeskL1 = tavelDeskRepository.findByApprover1(empId);
			System.out.println(travelDeskL1);
			List<TravelDesk> travelDeskL2 = tavelDeskRepository.findByApprover2(empId);
			System.out.println(travelDeskL2);
			if(!travelDeskL1.isEmpty()) {
				List<TravelDesk> activeTravelDeskL1 = travelDeskL1.stream()
			            .filter(t -> t.getStatus().equalsIgnoreCase("Pending")&& t.getIsActive() == 1 && t.getLevel() == 1)  
			            .collect(Collectors.toList());
			  System.out.println(activeTravelDeskL1);
			  activeTravelDesk.addAll(activeTravelDeskL1);
			  System.out.println(activeTravelDesk);
			}
			if(!travelDeskL2.isEmpty()) {
				List<TravelDesk> activeTravelDeskL2 = travelDeskL2.stream()
						  .filter(t -> t.getLevel2approverStatus().equalsIgnoreCase("Pending")&& t.getIsActive() == 1&& t.getLevel() == 2 && t.getFinalStatus().equalsIgnoreCase("Pending"))  
				            .collect(Collectors.toList());
				  System.out.println(activeTravelDeskL2);
				  activeTravelDesk.addAll(activeTravelDeskL2);
				  System.out.println(activeTravelDesk);
			}
			 
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
			e.printStackTrace();
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
//				BigInteger approver = BigInteger.valueOf(travelData.getLevelOneApprover());
//				existingTravelDesk.setApprover1(approver);
//				Employee level1 = employeeRepository.findByEmpId(Long.valueOf(travelData.getLevelOneApprover()));
//				existingTravelDesk.setLevel1ApproverEmail(level1.getEmail());
//				existingTravelDesk.setLevel(1);
//				BigInteger approver2 = new BigInteger(level2Approver);
//				existingTravelDesk.setApprover2(approver2);
//				existingTravelDesk.setIsActive(1);
				existingTravelDesk.setLevel2approverStatus("Pending");
				existingTravelDesk.setFinalStatus("Pending");
//				Employee level2 = employeeRepository.findByEmpId(Long.valueOf(travelData.getLevel2Approver().toString()));
//				existingTravelDesk.setLevel2ApproverEmail(level2.getEmail());
//				existingTravelDesk.setLevel2approverName(level2.getName());
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
		if (travelData.getStatus().equalsIgnoreCase("Rejected")) {
			if (travelDesk.getLevel() == 1) {
				travelDesk.setLevel1ApproveOn(currentTimestamp);
				travelDesk.setStatus(travelData.getStatus());
				travelDesk.setFinalStatus(travelData.getStatus());
			}else if (travelDesk.getLevel() == 2){
				travelDesk.setLevel2ApproveOn(currentTimestamp);
		        travelDesk.setLevel2approverStatus(travelData.getStatus());
		        travelDesk.setFinalStatus(travelData.getStatus());
			}
			travelDesk.setLevel1approverRemarks(travelData.getLevel1approverRemarks());
		}else {
	    if (travelDesk.getLevel() == 1) {
	        travelDesk.setLevel1ApproveOn(currentTimestamp);
	        travelDesk.setLevel(travelDesk.getLevel()+1);
	        travelDesk.setStatus(travelData.getStatus());
	        travelDesk.setLevel1approverRemarks(travelData.getLevel1approverRemarks());
	    } else if (travelDesk.getLevel() == 2) {
	        travelDesk.setLevel2ApproveOn(currentTimestamp);
	        travelDesk.setLevel2approverStatus(travelData.getStatus());
	        travelDesk.setFinalStatus(travelData.getStatus());
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
	
	public Resource getTemplateFile(Long documentId) throws FileNotFoundException {
		Resource resource=null;
		String filename=null;
		try {
		List<Object[]> object = newsletterRepository.findByDocumentId(documentId);
		for (Object[] objectlist : object) {
		     filename= (String)objectlist[1];
		    System.out.println("filename" +filename);
		}
		
		String Location = traveldeskFileLocation + File.separator + filename;
		File file = new File(Location);
		if (file.exists()) {
			resource = new FileSystemResource(Location);
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resource;

	} 
	
	
	public ServiceResponse totalTravelData() {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			 List<TravelDesk> totalTravelData = new ArrayList<>() ;
			 
			List<TravelDesk> travelData = tavelDeskRepository.findAll();
			 List<TravelDesk> activeTravelDesk = travelData.stream()
			            .filter(t -> t.getIsActive() != 0)  
			            .collect(Collectors.toList());
			
			if(!travelData.isEmpty()) {
				
			  totalTravelData.addAll(activeTravelDesk);
			  System.out.println(totalTravelData);
			}
			 
			if(totalTravelData.isEmpty()) {
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
			e.printStackTrace();
			serviceResponse.setServiceError(e.getMessage());
			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			return serviceResponse;
		}
	}
	
	
	
	public ServiceResponse saveTravelReason(TravelReasonDTO travelReasonDTO) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	        TravelReason travelReason = new TravelReason();
	        travelReason.setTravelReasonName(travelReasonDTO.getTravelReasonName());
	        travelReason.setDescription(travelReasonDTO.getDescription());
	        travelReason.setIsActive("Y");
	        travelReason.setCreatedBy(travelReasonDTO.getCreatedBy());

	        TravelReason savedReason = travelReasonRepository.save(travelReason);

	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse(savedReason); 
	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceError("Failed to save travel reason: " + e.getMessage());
	    }

	    return serviceResponse;
	}
	
	public ServiceResponse getAllTravelReasons() {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	        List<TravelReason> reasonList = travelReasonRepository.findAll();

	        if (reasonList.isEmpty()) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError("No travel reasons found.");
	        } else {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(reasonList);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        serviceResponse.setServiceError("Error fetching travel reasons: " + e.getMessage());
	    }

	    return serviceResponse;
	}
	
	//private TravelModeRepository travelModeRepository;

	public ServiceResponse saveTravelMode(TravelModeDTO travelModeDTO) {
	    ServiceResponse response = new ServiceResponse();

	    try {
	        // Fetch TravelReason entity by name
	        TravelReason travelReason = travelReasonRepository
	            .findByTravelReasonName(travelModeDTO.getTravelReason())
	            .orElseThrow(() -> new RuntimeException("TravelReason not found: " + travelModeDTO.getTravelReason()));

	        TravelMode mode = new TravelMode();
	        mode.setTravelReason(travelReason); // Set the entity, not the string
	        mode.setModeType(travelModeDTO.getModeType());
	        mode.setDescription(travelModeDTO.getDescription());
	        mode.setIsActive("Y");
	        mode.setCreatedBy(travelModeDTO.getCreatedBy());

	        travelModeRepository.save(mode);

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Travel Mode saved successfully.");
	    } catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceError(e.getMessage());
	    }

	    return response;
	}

    
//    public ServiceResponse getAllgetTravelModes() {
//	    ServiceResponse serviceResponse = new ServiceResponse();
//	    try {
//	        List<TravelMode> modeList = travelModeRepository.findAll();
//
//	        if (modeList.isEmpty()) {
//	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
//	            serviceResponse.setServiceError("No travel reasons found.");
//	        } else {
//	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	            serviceResponse.setServiceResponse(modeList);
//	        }
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//	        serviceResponse.setServiceError("Error fetching travel reasons: " + e.getMessage());
//	    }
//
//	    return serviceResponse;
//	}

	public ServiceResponse getAllgetTravelModes() {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
//	        List<TravelMode> modeList = travelModeRepository.findAll();
	        List<TravelMode> modeList = travelModeRepository.findAllData();

	        List<TravelModeDTO> dtoList = new ArrayList<>();

	        for (TravelMode mode : modeList) {
	            TravelModeDTO dto = new TravelModeDTO();
	            dto.setTravelModeId(mode.getTravelModeId());
	            dto.setModeType(mode.getModeType());
	            dto.setDescription(mode.getDescription());
	            dto.setIsActive(mode.getIsActive());
	            dto.setCreatedBy(mode.getCreatedBy());
	            dto.setCreatedOn(mode.getCreatedOn());
	            if (mode.getTravelReason() != null) {
	                dto.setTravelReasonName(mode.getTravelReason().getTravelReasonName());
	            } else {
	                dto.setTravelReasonName(null); 
	            }



	            dtoList.add(dto);
	        }

	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse(dtoList);

	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        serviceResponse.setServiceError("Error fetching travel reasons: " + e.getMessage());
	    }

	    return serviceResponse;
	}

	
	public ServiceResponse getAllDocsThroughReqId(BigInteger requestId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		List<String> docIdList = new ArrayList<>();
		try {
			TravelDesk travelData = tavelDeskRepository.findByRequestId(requestId);
			if(travelData == null) {
				serviceResponse.setServiceResponse("No data found for the given request !!");
				serviceResponse.setServiceMessage("No data found for the given request !!");
				serviceResponse.setServiceStatus(serviceResponse.STATUS_FAIL);
				return serviceResponse;
			}
			else {
			String docIds = travelData.getDocId();
			if(docIds != null) {
			docIdList = Arrays.stream(docIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
			serviceResponse.setServiceResponse(docIdList);
			serviceResponse.setServiceStatus(serviceResponse.STATUS_SUCCESS);
			return serviceResponse;
			}
			else {
				serviceResponse.setServiceResponse("No document found for the given request !!");
				serviceResponse.setServiceMessage("No document found for the given request !!");
				serviceResponse.setServiceStatus(serviceResponse.STATUS_FAIL);
				return serviceResponse;
			}
			}
		}
		catch(Exception e) {
			serviceResponse.setServiceResponse(e.getMessage());
			serviceResponse.setServiceStatus(serviceResponse.STATUS_FAIL);
			serviceResponse.setServiceMessage("Something went wrong !!");
			return serviceResponse;
		}
//		return serviceResponse;
	}
	
	
	 public ServiceResponse saveTravelClass(TravelClassRequest dto) {
	        ServiceResponse response = new ServiceResponse();

	        try {
	            // Fetch TravelReason by name
	        	 TravelReason travelReason = travelReasonRepository
	     	            .findByTravelReasonName(dto.getTravelReason())
	     	            .orElseThrow(() -> new RuntimeException("TravelReason not found: " + dto.getTravelReason()));

	        	 Long modeId = Long.parseLong(dto.getTravelMode()); 
	 	        

	 	        TravelMode modeList = travelModeRepository.findByModeId(modeId);

//	        	 String modeType = dto.getTravelMode();
	        	 
//	        	 TravelMode travelMode = travelModeRepository
//	        		        .findByModeType(modeType)
//	        		        .orElseThrow(() -> new RuntimeException("Travel Mode not found: " + dto.getTravelMode()));


	            TravelClass travelClass = new TravelClass();
	            travelClass.setTravelReason(travelReason);
	            travelClass.setTravelMode(modeList);
	            travelClass.setTravelClass(dto.getTravelClass());
	            travelClass.setDescription(dto.getDescription());
	            travelClass.setCreatedBy(dto.getCreatedBy());
	            travelClass.setIsActive("Y");

	            travelClassRepository.save(travelClass);

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Travel Class saved successfully.");
	        } catch (Exception e) {
	        	e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError(e.getMessage());
	        }

	        return response;
	    }
	


	public ServiceResponse uploadTicket(MultipartFile file, String displayName, Long uploadedBy, BigInteger requestId) {

	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Upload Newsletter");
	    apiLogInfo.setApiUrl("/api/newsletters/uploadNewsletter");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("Newsletter: ").append(displayName)
	              .append(", uploadedBy: ").append(uploadedBy);

	    try {
	        TravelDesk details=tavelDeskRepository.findByRequestId(requestId);
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
	            if(travelDocDetails !=null) {
	            	details.setTicketDocId(travelDocDetails.getDocumentId());
	            	tavelDeskRepository.save(details);
	            }
                
                
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

	
	public ServiceResponse getTravelModeByReason(String travelReasonName) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	        TravelReason travelModeBasedOnReason = travelReasonRepository
	            .findByTravelReasonName(travelReasonName)
	            .orElseThrow(() -> new RuntimeException("TravelReason not found: " + travelReasonName));

	        System.out.println(travelModeBasedOnReason);

	        Long travelReasonId = travelModeBasedOnReason.getId(); 
	        System.out.println("Travel Reason ID: " + travelReasonId);

	        List<TravelMode> modeList = travelModeRepository.findByTravelReasonId(travelReasonId);

	        if (modeList.isEmpty()) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError("No travel modes found.");
	        } else {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(modeList);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        serviceResponse.setServiceError("Error fetching travel modes: " + e.getMessage());
	    }

	    return serviceResponse;
	}
	
	
	public ServiceResponse saveHotelCategory(HotelCategoryDTO hotelCategoryDTO) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	        HotelCategory hotelCategory = new HotelCategory();
	        hotelCategory.setHotelCategory(hotelCategoryDTO.getHotelCategory());
	        hotelCategory.setDescription(hotelCategoryDTO.getDescription());
	        hotelCategory.setIsActive("Y");
	        hotelCategory.setCreatedBy(hotelCategoryDTO.getCreatedBy()); 

	        HotelCategory savedCategory = hotelCategoryRepository.save(hotelCategory);

	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse(savedCategory); 
	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceError("Failed to save hotel category: " + e.getMessage());
	    }

	    return serviceResponse;
	}
	
	
	public ServiceResponse getHotelCategory() {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	        List<HotelCategory> hotelCategoryist = hotelCategoryRepository.findAll();

	        if (hotelCategoryist.isEmpty()) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError("No hotel Category found.");
	        } else {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(hotelCategoryist);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        serviceResponse.setServiceError("Error fetching hotelCategoryist: " + e.getMessage());
	    }

	    return serviceResponse;
	}

	
	public ServiceResponse saveHotelSubCategory(HotelSubCategoryDTO dto) {
	    ServiceResponse response = new ServiceResponse();

	    try {
	        // Fetch HotelCategory entity by name
//	        HotelCategory hotelCategory = hotelCategoryRepository
//	            .findHotelCategoryById(dto.getHotelCategory())
//	            .orElseThrow(() -> new RuntimeException("HotelCategory not found: " + dto.getHotelCategory()));
	    	
	    	Long hotelCategoryId = Long.parseLong(dto.getHotelCategory()); // Convert String to Long

	    	HotelCategory hotelCategory = hotelCategoryRepository
	    	    .findById(hotelCategoryId)
	    	    .orElseThrow(() -> new RuntimeException("HotelCategory not found: " + dto.getHotelCategory()));

	        HotelSubCategory subCategory = new HotelSubCategory();
	        subCategory.setHotelCategory(hotelCategory); // Set the entity, not just ID
	        subCategory.setHotelSubCategoryName(dto.getHotelSubCategoryName());
	        subCategory.setDescription(dto.getDescription());
	        subCategory.setIsActive("Y");
	        subCategory.setCreatedBy(dto.getCreatedBy());

	        hotelSubCategoryRepository.save(subCategory);

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Hotel Sub-Category saved successfully.");
	    } catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceError("Error saving Hotel Sub-Category: " + e.getMessage());
	    }

	    return response;
	}
	
	
	public ServiceResponse getHotelSubCategory() {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	        List<HotelSubCategory> hotelSubCategoryist = hotelSubCategoryRepository.findAll();

	        if (hotelSubCategoryist.isEmpty()) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError("No hotel Category found.");
	            return serviceResponse;
	        } else {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(hotelSubCategoryist);
	            return serviceResponse;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        serviceResponse.setServiceError("Error fetching hotelCategoryist: " + e.getMessage());
	        return serviceResponse;
	    }

	    
	}
	
	
	public ServiceResponse saveCity(CityDTO dto) {
	    ServiceResponse response = new ServiceResponse();

	    try {
	        HotelCategory hotelCategory = hotelCategoryRepository
	                .findById(dto.getHotelCategoryId())
	                .orElseThrow(() -> new RuntimeException("HotelCategory not found with ID: " + dto.getHotelCategoryId()));

	        HotelSubCategory hotelSubCategory = hotelSubCategoryRepository
	                .findById(dto.getHotelSubCategoryId())
	                .orElseThrow(() -> new RuntimeException("HotelSubCategory not found with ID: " + dto.getHotelSubCategoryId()));

	        City city = new City();
	        city.setHotelCategory(hotelCategory);
	        city.setHotelSubCategory(hotelSubCategory);
	        city.setCityName(dto.getCityName());
	        city.setDescription(dto.getDescription());
	        city.setIsActive("Y");
	        city.setCreatedBy(dto.getCreatedBy());

	        cityRepository.save(city);

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("City saved successfully.");
	    } catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceError("Error saving city: " + e.getMessage());
	    }

	    return response;
	}
	
	
	public ServiceResponse getTravelClassByMode(String travelModeName) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {

	        
	        TravelMode getTravelClassByMode = travelModeRepository
		            .findByModeType(travelModeName)
		            .orElseThrow(() -> new RuntimeException("TravelClass not found: " + travelModeName));
		        
	        

	        System.out.println(getTravelClassByMode.toString());

	        Long travelModeId = getTravelClassByMode.getTravelModeId(); 
	        System.out.println("Travel Mode ID: " + travelModeId);

	        Optional<List<TravelClass>> classList = travelClassRepository.findByTravelModeId(travelModeId);

	        if (classList.isEmpty()) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError("No travel modes found.");
	        } else {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(classList);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        serviceResponse.setServiceError("Error fetching travel modes: " + e.getMessage());
	    }

	    return serviceResponse;
	}
	
	
	public ServiceResponse getCityBySubCategory(String travelModeName) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {

	        Optional<List<HotelSubCategory>> subCategories = hotelSubCategoryRepository.findAllByHotelSubCategoryName(travelModeName);

	        if (subCategories.isPresent()) {
	            List<HotelSubCategory> subCategoriesId = subCategories.get();

	            List<Long> subCategoryIds = subCategoriesId.stream()
	                .map(HotelSubCategory::getId)
	                .collect(Collectors.toList());

	            
	            List<City> cityList = cityRepository.findBySubCategoryIds(subCategoryIds);
	         
	        System.out.println(cityList.toString());

	        if (cityList.isEmpty()) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError("No travel modes found.");
	        } else {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(cityList);
	        }
	       }
	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        serviceResponse.setServiceError("Error fetching travel modes: " + e.getMessage());
	    }

	    return serviceResponse;
	}
	
	
	public ServiceResponse getCity() {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	        List<City> cityList = cityRepository.findAll();

	        if (cityList.isEmpty()) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError("No City found.");
	            return serviceResponse;
	        } else {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(cityList);
	            return serviceResponse;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        serviceResponse.setServiceError("Error fetching cityList: " + e.getMessage());
	        return serviceResponse;
	    }
	}
	
	
	public ServiceResponse onGetTravelCass() {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	        List<TravelClass> classList = travelClassRepository.findAll();

	        if (classList.isEmpty()) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError("No CLass found.");
	            return serviceResponse;
	        } else {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(classList);
	            return serviceResponse;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        serviceResponse.setServiceError("Error fetching classList: " + e.getMessage());
	        return serviceResponse;
	    }
	}


	public ServiceResponse uploadKycDocument(MultipartFile file, String displayName, Long uploadedBy) {

	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Upload Newsletter");
	    apiLogInfo.setApiUrl("/api/uploadKycDocument");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("uploadKycDocument:").append(displayName)
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
	            newsletter.setType("TRAVEL KYC DOCUMENT");
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




