package com.apmosys.employeeportal.service;

import java.io.File;
import org.springframework.data.domain.Sort;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
import com.apmosys.employeeportal.model.Client;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.EmployeeLeave;
import com.apmosys.employeeportal.model.HotelCategory;
import com.apmosys.employeeportal.model.HotelSubCategory;
import com.apmosys.employeeportal.model.Newsletter;
import com.apmosys.employeeportal.model.TravelClass;
import com.apmosys.employeeportal.model.TravelDesk;
import com.apmosys.employeeportal.model.TravelMode;
import com.apmosys.employeeportal.model.TravelReason;
import com.apmosys.employeeportal.repository.CityRepository;
import com.apmosys.employeeportal.repository.ClientsRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
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

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ClientsRepository clientsRepository;

	private static final long TRAVEL_PROJECT_OTHERS_ID = -1L;

	@Value("${level2Approver}")
	public String level2Approver;
	
	@Value("${file.location.documents.travelDesk}")
	private String traveldeskFileLocation;
	
	@Autowired
	EmployeeLeaveRepository employeeLeaveRepository;
	
	@Value("${hr.mail}")
	private String hrMailAddress;
	
	@Value("${admin.mail}")
	private String adminMail;
	
	@Value("${ticket.mail}")
	private String ticketMail;
	
	@Value("${admin.head}")
	private String adminHead;

	
	private void applyProjectAndClientOnTravelDesk(TravelDesk travelDesk, TravelDeskDTO travelData) {
		if (travelData.getProjectId() == null) {
			return;
		}
		long pid = travelData.getProjectId().longValue();
		if (pid == TRAVEL_PROJECT_OTHERS_ID) {
			travelDesk.setProjectId(null);
			String othersName = travelData.getOthersProjectName() != null
					? travelData.getOthersProjectName().trim()
					: (travelData.getProjectName() != null ? travelData.getProjectName().trim() : "");
			travelDesk.setProjectName(othersName);
			Integer cid = travelData.getOthersClientId() != null ? travelData.getOthersClientId() : travelData.getClientId();
			travelDesk.setClientId(cid);
			if (cid != null) {
				Client client = clientsRepository.findByClientId(cid);
				travelDesk.setClientName(client != null ? client.getClientName() : travelData.getClientName());
			} else {
				travelDesk.setClientName(travelData.getClientName());
			}
			return;
		}
		travelDesk.setProjectId(pid);
		Project project = projectRepository.findByProjectId((int) pid);
		if (project != null) {
			travelDesk.setProjectName(project.getProjectName());
			travelDesk.setClientId(project.getClientId());
			travelDesk.setClientName(project.getClientName());
		} else if (travelData.getProjectName() != null) {
			travelDesk.setProjectName(travelData.getProjectName());
			travelDesk.setClientId(travelData.getClientId());
			travelDesk.setClientName(travelData.getClientName());
		}
	}

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
		applyProjectAndClientOnTravelDesk(travelDesk, travelData);
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
            SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
            if(savedTravelDesk.getCity() != null ) {
            	try {
            	    mailService.sendMailWithCC(
            	        travelDesk.getLevel1ApproverEmail(),
            		    hrMailAddress,
            		    "Travel Request Approval Required",
            		    "Dear "+ travelDesk.getHodName() + ","+"<br>" +
            		    "<br>"+" &nbsp"+" &nbsp"+" "+"A travel request has been submitted by "+ emp.getName() + 
            		    " for the purpose of " + travelDesk.getPurpose()+". The planned travel is from "+ 
            		    sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+", and the employee will require accommodation in "+ travelDesk.getCity() +" Kindly review the request and take the necessary action at your earliest convenience. "+ 
            		
            		    
            		    "<br><br>Regards,<br>" +
            		    "IShine Support Team<br>" +
            		    "ApMoSys PVT. LTD.<br><br>"
            		);
            	}catch(Exception e) {
            			
            		e.printStackTrace();
//            		serviceResponse.setServiceError(e.getMessage());
//            		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            	}
            	
            	
            	try {
                	mailService.sendMail(
                		    travelDesk.getEmail(),
                		    "Your Travel Request Has Been Submitted",
                		    "Dear "+ travelDesk.getName() + ","+"<br>" +
                		    "<br>"+" &nbsp"+" &nbsp"+" "+" Your travel requests for the purpose of "+ travelDesk.getPurpose()+ 
                		    "has been successfully submitted. \n"
                		    + "The travel is scheduled from "+ 
                		    sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+" and a stay has been requested in "+ travelDesk.getCity()+"."+
                		    "The request has been forwarded to your reporting manager "+travelDesk.getHodName()+" for approval. You will be notified once action is taken."+
                		    "<br><br>Regards,<br>" +
                		    "IShine Support Team<br>" +
                		    "ApMoSys PVT. LTD.<br><br>"
                		);
                	}catch(Exception e) {
                			
                		e.printStackTrace();
//                		serviceResponse.setServiceError(e.getMessage());
//                		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
                	}
            	
//            	try {
//                	mailService.sendMailWithCC(
//                			hrMailAddress, "shivtosh.pal@apmosys.com",
//                		    " Travel Request Submitted by "+travelDesk.getName()+"",
//                		    "Dear "+"HR Team" + ","+"<br>" +
//                		    "<br>"+" &nbsp"+" &nbsp"+" "+"A travel request has been submitted by "+travelDesk.getName()+ 
//                		    "for the purpose of \n"+ travelDesk.getPurpose()+"."
//                		    + "The proposed travel is scheduled from "+ 
//                		    sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+" and accommodation is requested in "+travelDesk.getCity()+"."+
//                		   	 "The request has been forwarded to "+travelDesk.getHodName()+" for approval. This is for your information."+   
//                		    "<br><br>Regards,<br>" +
//                		    "IShine Support Team<br>" +
//                		    "ApMoSys PVT. LTD.<br><br>"
//                		);
//                	}catch(Exception e) {
//                			
//                		e.printStackTrace();
//                		serviceResponse.setServiceError(e.getMessage());
//                		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//                	}
						

            }else {
            	try {
            	    mailService.sendMailWithCC(
            	    	travelDesk.getLevel1ApproverEmail(),
            		    hrMailAddress,
            		    "Travel Request Approval Required",
            		    "Dear "+ travelDesk.getHodName() + ","+"<br>" +
            		    "<br>"+" &nbsp"+" &nbsp"+" "+"A travel request has been submitted by "+ emp.getName()+ 
            		    " for the purpose of"+ travelDesk.getPurpose()+". The planned travel is from "+ 
            		    sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+", and the employee has requested to travel via " + travelDesk.getTravelMode() + " in " + travelDesk.getTravelClass() + ". Kindly review the request and take the necessary action at your earliest convenience. "+ 
            		    travelDesk.getName() +" "+" for "+travelDesk.getName()+" day(s), Please take necessary action." +
            		    
            		    "<br><br>Regards,<br>" +
            		    "IShine Support Team<br>" +
            		    "ApMoSys PVT. LTD.<br><br>"
            		);
            	}catch(Exception e) {
            			
            		e.printStackTrace();
//            		serviceResponse.setServiceError(e.getMessage());
//            		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            	}
            	
            	try {
            	mailService.sendMail(
            			travelDesk.getEmail(),
            		    "Your Travel Request Has Been Submitted",
            		    "Dear "+ travelDesk.getName() + ","+"<br>" +
            		    "<br>"+" &nbsp"+" &nbsp"+" "+"Your travel requests for the purpose of "+ travelDesk.getPurpose()+ 
            		    " has been successfully submitted. \n"
            		    + "The travel is scheduled from "+ 
            		    sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+", and you have requested to travel via "+ travelDesk.getTravelMode()+" in "+ travelDesk.getTravelClass()+"."+
            		   	    
            		    "<br><br>Regards,<br>" +
            		    "IShine Support Team<br>" +
            		    "ApMoSys PVT. LTD.<br><br>"
            		);
            	}catch(Exception e) {
            			
            		e.printStackTrace();
//            		serviceResponse.setServiceError(e.getMessage());
//            		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            	}

//            	try {
//                	mailService.sendMailWithCC(
//                			hrMailAddress, "shivtosh.pal@apmosys.com",
//                		    " Travel Request Submitted by "+travelDesk.getName()+"",
//                		    "Dear "+"HR Team" + ","+"<br>" +
//                		    "<br>"+" &nbsp"+" &nbsp"+" "+"A travel request has been submitted by "+travelDesk.getName()+ 
//                		    "for the purpose of \n"+ travelDesk.getPurpose()+"."
//                		    + "The proposed travel is scheduled from "+ 
//                		    sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+", and you have requested to travel via"+ travelDesk.getTravelMode()+" in "+ travelDesk.getTravelClass()+"."+
//                		   	 "The request has been forwarded to "+travelDesk.getHodName()+" for approval. This is for your information."+   
//                		    "<br><br>Regards,<br>" +
//                		    "IShine Support Team<br>" +
//                		    "ApMoSys PVT. LTD.<br><br>"
//                		);
//                	}catch(Exception e) {
//                			
//                		e.printStackTrace();
//                		serviceResponse.setServiceError(e.getMessage());
//                		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//                	}

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
		 SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
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
					 
						try {
		            	    mailService.sendMailWithCC(
		            	        existingTravelDesk.getLevel1ApproverEmail(),
		            		    hrMailAddress,
		            		    "Updated Travel Request - "+emp.getName()+"",
		            		    "Dear "+ existingTravelDesk.getHodName() + ","+"<br>" +
		            		    "<br>"+" &nbsp"+" &nbsp"+" "+"The travel requests previously submitted by "+ emp.getName()+ 
		            		    " has been updated. Kindly review the revised details and take appropriate action."+
		            		    "<br>Updated Details:"+
		            		    "<br>&bull; Purpose: "+existingTravelDesk.getPurpose() +
		            		    "<br>&bull; Travel Dates: "+sdf1.format(existingTravelDesk.getFromDate())+" to "+ sdf1.format(existingTravelDesk.getToDate())+
		            		    "<br>&bull; City: "+existingTravelDesk.getCity() +
		            		    "<br> Please log in to the system to view the full details."+
		            		    "<br><br>Regards,<br>" +
		            		    "IShine Support Team<br>" +
		            		    "ApMoSys PVT. LTD.<br><br>"
		            		);
		            	}catch(Exception e) {
		            			
		            		e.printStackTrace();
//		            		serviceResponse.setServiceError(e.getMessage());
//		            		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		            	}
		            	
		            	try {
		            	mailService.sendMail(
		            			existingTravelDesk.getEmail(),
		            		    "Your Travel Request Has Been Updated",
		            		    "Dear "+ existingTravelDesk.getName() + ","+"<br>" +
		            		    "<br>"+" &nbsp"+" &nbsp"+" "+"Your travel requests for the purpose of "+ existingTravelDesk.getPurpose()+ 
		            		    "  scheduled from "+sdf1.format(existingTravelDesk.getFromDate())+" to "+ sdf1.format(existingTravelDesk.getToDate())+", has been successfully updated."
		            		    + " <br> The updated request has been sent again to your reporting manager"+ existingTravelDesk.getHodName()+" for approval. "+ 
		            		   
		            		   	    
		            		    "<br><br>Regards,<br>" +
		            		    "IShine Support Team<br>" +
		            		    "ApMoSys PVT. LTD.<br><br>"
		            		);
		            	}catch(Exception e) {
		            			
		            		e.printStackTrace();
//		            		serviceResponse.setServiceError(e.getMessage());
//		            		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		            	}
						

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
		SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
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
			try {
        	    mailService.sendMailWithCC(
        	    	travelDesk.getLevel1ApproverEmail(),
        		    hrMailAddress,
        		    "Travel Request Revoked – "+travelDesk.getName()+"",
        		    "Dear "+ travelDesk.getHodName() + ","+"<br>" +
        		    "<br>"+" &nbsp"+" &nbsp"+" "+"Please be informed that the travel request submitted by"+ travelDesk.getName()+ 
        		    " for the period "+sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+" has been revoked by the employee."+
        		    
        		    "<br><br> No further action is required from your end on this request."+
        		    "<br><br>Regards,<br>" +
        		    "IShine Support Team<br>" +
        		    "ApMoSys PVT. LTD.<br><br>"
        		);
        	}catch(Exception e) {
        			
        		e.printStackTrace();
//        		serviceResponse.setServiceError(e.getMessage());
//        		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        	}
        	
        	try {
        	mailService.sendMail(
        			travelDesk.getEmail(),
        		    "Your Travel Request Has Been Revoked",
        		    "Dear "+ travelDesk.getName() + ","+"<br>" +
        		    "<br>"+" &nbsp"+" &nbsp"+" "+"You have successfully revoked your travel request for the purpose of  "+ travelDesk.getPurpose()+ 
        		    "  originally scheduled from  "+sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+"."
        		    + "<br>If you wish to submit a new request, you may do so via the travel request system."+ 
        		   
        		   	    
        		    "<br><br>Regards,<br>" +
        		    "IShine Support Team<br>" +
        		    "ApMoSys PVT. LTD.<br><br>"
        		);
        	}catch(Exception e) {
        			
        		e.printStackTrace();
//        		serviceResponse.setServiceError(e.getMessage());
//        		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        	}
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
	    File file = null;
	    SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
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
	            if(travelDesk.getLevel1ApproveOn() != null && "Approved".equals(travelDesk.getStatus())) {

try {
//	travelDesk.getLevel2ApproverEmail()
	            	    mailService.sendMailWithCC(
	            	    	travelDesk.getLevel2ApproverEmail(),
	            		    hrMailAddress,
	            		    "Travel Request Approved – "+travelDesk.getName()+"",
	            		    "Dear "+ travelDesk.getLevel2approverName() + ","+"<br>" +
	            		    "<br>"+" &nbsp"+" &nbsp"+" "+"The travel request submitted by "+ travelDesk.getName()+ "for the purpose of "+travelDesk.getPurpose()+" from "+	            		
	            		     sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+", has been approved by"+travelDesk.getHodName()+
	            		    "<br>Please take final action on this request."+
	            		   
	            		    "<br><br>Regards,<br>" +
	            		    "IShine Support Team<br>" +
	            		    "ApMoSys PVT. LTD.<br><br>"
	            		);
	            	}catch(Exception e) {
	            			
	            		e.printStackTrace();
//	            		serviceResponse.setServiceError(e.getMessage());
//	            		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            	}
	            	
	            	try {
//	            		travelDesk.getEmail()
	            	mailService.sendMail(
	            			travelDesk.getEmail(),
	            		    "Travel Request Approved by Your Reporting Manager",
	            		    "Dear "+ travelDesk.getName() + ","+"<br>" +
	            		    "<br>"+" &nbsp"+" &nbsp"+" "+"Your travel requests for the purpose of "+ travelDesk.getPurpose()+ 
	            		    ",scheduled from "+sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+
	            		    ", has been reviewed and approved by your reporting manager"+travelDesk.getHodName()+"."+
	            		    "<br> The approved request will now be processed to the HOD for 2nd Level Approval."
	            		    + " You will be notified once the approval is received from HOD. "+
	            		      
	            		    "<br><br>Regards,<br>" +
	            		    "IShine Support Team<br>" +
	            		    "ApMoSys PVT. LTD.<br><br>"
	            		);
	            	}catch(Exception e) {
	            			
	            		e.printStackTrace();
//	            		serviceResponse.setServiceError(e.getMessage());
//	            		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            	}
		            }else {
		            	
//		            	try {
//		            	    mailService.sendMailWithCC(
//		            	    	travelDesk.getLevel2ApproverEmail(),
//		            		    hrMailAddress,
//		            		    "Travel Request Rejected  – "+travelDesk.getName()+"",
//		            		    "Dear "+ travelDesk.getLevel2approverName() + ","+"<br>" +
//		            		    "<br>"+" &nbsp"+" &nbsp"+" "+"The travel request submitted by "+ travelDesk.getName()+ "for the purpose of "+travelDesk.getPurpose()+" from "+	            		
//		            		     sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+", has been approved by"+travelDesk.getHodName()+
//		            		    "<br>Please take final action on this request."+
//		            		   
//		            		    "<br><br>Regards,<br>" +
//		            		    "IShine Support Team<br>" +
//		            		    "ApMoSys PVT. LTD.<br><br>"
//		            		);
//		            	}catch(Exception e) {
//		            			
//		            		e.printStackTrace();
//		            		serviceResponse.setServiceError(e.getMessage());
//		            		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//		            	}
		            	
		            	try {
//		            		travelDesk.getEmail()
		            	mailService.sendMailWithCC(
		            			travelDesk.getEmail(),
		            			hrMailAddress,
		            		    "Travel Request Rejected by Your Manager",
		            		    "Dear "+ travelDesk.getName() + ","+"<br>" +
		            		    "<br>"+" &nbsp"+" &nbsp"+" "+"Your travel requests for the purpose of "+ travelDesk.getPurpose()+ 
		            		    ",scheduled from "+sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+
		            		    ", has been reviewed and rejected by your reporting manager"+travelDesk.getHodName()+"."+
		            		    "<br> For more details regarding this decision, please reach out directly to your reporting manager. "+
		            		      
		            		    "<br><br>Regards,<br>" +
		            		    "IShine Support Team<br>" +
		            		    "ApMoSys PVT. LTD.<br><br>"
		            		);
		            	}catch(Exception e) {
		            			
		            		e.printStackTrace();
//		            		serviceResponse.setServiceError(e.getMessage());
//		            		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		            	}
		            }
	        } else if (travelDesk.getLevel() == 2) {
	            updateApprovalLevel(travelDesk, currentTimestamp, travelData);
	            if( travelDesk.getLevel2ApproveOn() != null && "Approved".equals(travelDesk.getLevel2approverStatus())) {
	            	
	    			    
	            	try {
	            		            	    mailService.sendMail(
	            		            		    hrMailAddress,
	            		            		    "Travel Request Approved – "+travelDesk.getName()+"",
	            		            		    "Dear "+ "HR Team" + ","+"<br>" +
	            		            		    "<br>"+" &nbsp"+" &nbsp"+" "+"The travel request submitted by "+ travelDesk.getName()+ "for the purpose of "+travelDesk.getPurpose()+" from "+	            		
	            		            		     sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+", has been approved by"+travelDesk.getLevel2approverName()+
	            		            		    "<br>No further action is required from your end at this time."+
	            		            		   
	            		            		    "<br><br>Regards,<br>" +
	            		            		    "IShine Support Team<br>" +
	            		            		    "ApMoSys PVT. LTD.<br><br>"
	            		            		);
	            		            	}catch(Exception e) {
	            		            			
	            		            		e.printStackTrace();
//	            		            		serviceResponse.setServiceError(e.getMessage());
//	            		            		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            		            	}
	            		            	
	            		            	try {
//	            		            		travelDesk.getEmail()
	            		            		mailService.sendMail(
	            		            				travelDesk.getEmail(),
	            			            		    "Travel Request Approved by Your HOD",
	            			            		    "Dear "+ travelDesk.getName() + ","+"<br>" +
	            			            		    "<br>"+" &nbsp"+" &nbsp"+" "+"Your travel requests for the purpose of "+ travelDesk.getPurpose()+ 
	            			            		    ",scheduled from "+sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+
	            			            		    ", has been reviewed and approved by your HOD "+travelDesk.getLevel2approverName()+"."+
	            			            		   
	            			            		      
	            			            		    "<br><br>Regards,<br>" +
	            			            		    "IShine Support Team<br>" +
	            			            		    "ApMoSys PVT. LTD.<br><br>"
	            			            		);
	            			            	}catch(Exception e) {
	            			            			
	            			            		e.printStackTrace();
//	            			            		serviceResponse.setServiceError(e.getMessage());
//	            			            		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            			            	}
	            		            	
	            		            	
	            		    			
	        	    			        try {
	        	    			        
	        	    			            Long docId = travelDesk.getKycDocumentId() ;
	        	    			            Resource resource = getTemplateFile(docId);
	        	    			            if (resource != null && resource.exists()) {
	        	    			                file = resource.getFile();
	        	    			               
	        	    			            }
	        	    			        } catch (Exception e) {
	        	    			        	e.printStackTrace();
//	            		            		serviceResponse.setServiceError(e.getMessage());
//	            		            		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        	    			        }
	            		            	try {
	            		            		mailService.sendMailWithAttachment(
	            		            				ticketMail,
	            			            			adminHead,
	            			            		    "Approved Travel Request – Ticket Booking Required for"+ travelDesk.getName()+"",
	            			            		    "Dear "+ "Admin Team" + ","+"<br>" +
	            			            		    "<br>"+" &nbsp"+" &nbsp"+" "+"A travel request submitted by "+ travelDesk.getName()+" for the purpose of "+ travelDesk.getPurpose()+ 
	            			            		    ",has been approved by the HOD "+travelDesk.getLevel2approverName()+"."+
	            			            		    
	            			            		    "<br>Updated Details:"+
	            			            		    "<br>&bull; Purpose: "+travelDesk.getPurpose() +
	            			            		    "<br>&bull; Travel Dates: "+sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+
	            			            		    "<br>&bull; Travel Mode: "+travelDesk.getTravelMode() +
	            			            		    "<br>&bull; Travel Class: "+travelDesk.getTravelClass() +
	            			            		    "<br> Kindly proceed with the necessary arrangements, including ticket booking and/or hotel reservations, as applicable."+
	            			            		    "<br>Please refer to the attachment for further information."+
	            			            		      
	            			            		    "<br><br>Regards,<br>" +
	            			            		    "IShine Support Team<br>" +
	            			            		    "ApMoSys PVT. LTD.<br><br>"
	            			            		,file);
	            			            	}catch(Exception e) {
	            			            			
	            			            		e.printStackTrace();
//	            			            		serviceResponse.setServiceError(e.getMessage());
//	            			            		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            			            	}
	            			            }else {
	            			            	
//	            			            	try {
//	            			            	    mailService.sendMailWithCC(
//	            			            	    	travelDesk.getLevel2ApproverEmail(),
//	            			            		    hrMailAddress,
//	            			            		    "Travel Request Rejected  – "+travelDesk.getName()+"",
//	            			            		    "Dear "+ travelDesk.getLevel2approverName() + ","+"<br>" +
//	            			            		    "<br>"+" &nbsp"+" &nbsp"+" "+"The travel request submitted by "+ travelDesk.getName()+ "for the purpose of "+travelDesk.getPurpose()+" from "+	            		
//	            			            		     sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+", has been approved by"+travelDesk.getHodName()+
//	            			            		    "<br>Please take final action on this request."+
//	            			            		   
//	            			            		    "<br><br>Regards,<br>" +
//	            			            		    "IShine Support Team<br>" +
//	            			            		    "ApMoSys PVT. LTD.<br><br>"
//	            			            		);
//	            			            	}catch(Exception e) {
//	            			            			
//	            			            		e.printStackTrace();
//	            			            		serviceResponse.setServiceError(e.getMessage());
//	            			            		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//	            			            	}
	            			            	
	            			            	try {
//	            			            		travelDesk.getEmail()
	            			            	mailService.sendMailWithCC(
	            			            			travelDesk.getEmail()
	            			            			,
	            			            			hrMailAddress,
	            			            		    "Travel Request Rejected by Your HOD",
	            			            		    "Dear "+ travelDesk.getName() + ","+"<br>" +
	            			            		    "<br>"+" &nbsp"+" &nbsp"+" "+"Your travel requests for the purpose of "+ travelDesk.getPurpose()+ 
	            			            		    ",scheduled from "+sdf1.format(travelDesk.getFromDate())+" to "+ sdf1.format(travelDesk.getToDate())+
	            			            		    ", has been reviewed and rejected by your HOD "+travelDesk.getLevel2approverName()+"."+
	            			            		   "<br>For more details regarding this decision, please reach out directly to your reporting manager."+
	            			            		      
	            			            		    "<br><br>Regards,<br>" +
	            			            		    "IShine Support Team<br>" +
	            			            		    "ApMoSys PVT. LTD.<br><br>"
	            			            		);
	            			            	}catch(Exception e) {
	            			            			
	            			            		e.printStackTrace();
//	            			            		serviceResponse.setServiceError(e.getMessage());
//	            			            		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	            			            	}
	            			            }
	            
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
	        if (travelReasonDTO.getTravelReasonName() == null
	                || travelReasonDTO.getTravelReasonName().trim().isEmpty()) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError("Travel reason name is required.");
	            return serviceResponse;
	        }
	        final String name = travelReasonDTO.getTravelReasonName().trim();
	        Employee emp = travelReasonDTO.getCreatedBy() != null
	                ? employeeRepository.findByEmpId(travelReasonDTO.getCreatedBy())
	                : null;
	        final String empName = emp != null ? emp.getName() : "System";

	        if (travelReasonDTO.getId() != null) {
	            TravelReason existing = travelReasonRepository.findById(travelReasonDTO.getId())
	                    .orElseThrow(() -> new RuntimeException("Travel reason not found: " + travelReasonDTO.getId()));
	            Optional<TravelReason> other = travelReasonRepository.findByTravelReasonName(name);
	            if (other.isPresent() && !other.get().getId().equals(existing.getId())) {
	                throw new RuntimeException("Travel reason name already exists.");
	            }
	            existing.setTravelReasonName(name);
	            existing.setDescription(travelReasonDTO.getDescription());
	            existing.setUpdatedBy(travelReasonDTO.getCreatedBy());
	            TravelReason savedReason = travelReasonRepository.save(existing);
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(savedReason);
	        } else {
	            if (travelReasonRepository.findByTravelReasonName(name).isPresent()) {
	                throw new RuntimeException("Travel reason name already exists.");
	            }
	            TravelReason travelReason = new TravelReason();
	            travelReason.setTravelReasonName(name);
	            travelReason.setDescription(travelReasonDTO.getDescription());
	            travelReason.setIsActive("Y");
	            travelReason.setCreatedBy(travelReasonDTO.getCreatedBy());
	            travelReason.setCreatedByName(empName);
	            TravelReason savedReason = travelReasonRepository.save(travelReason);
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(savedReason);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceError("Failed to save travel reason: " + e.getMessage());
	    }

	    return serviceResponse;
	}

	public ServiceResponse deleteTravelReason(Long id) {
	    ServiceResponse response = new ServiceResponse();
	    try {
	        if (id == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError("id is required.");
	            return response;
	        }
	        if (travelModeRepository.countByTravelReason_Id(id) > 0) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError("Cannot delete: travel modes exist for this reason. Remove them first.");
	            return response;
	        }
	        if (travelClassRepository.countByTravelReason_Id(id) > 0) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError("Cannot delete: travel classes exist for this reason. Remove them first.");
	            return response;
	        }
	        travelReasonRepository.deleteById(id);
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Travel reason deleted.");
	    } catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceError(e.getMessage());
	    }
	    return response;
	}
	
	public ServiceResponse getAllTravelReasons() {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	        List<TravelReason> reasonList = travelReasonRepository.findByIsActiveOrderByTravelReasonNameAsc("Y");				


	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse(reasonList);
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
	        if (travelModeDTO.getModeType() == null || travelModeDTO.getModeType().trim().isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError("Mode type is required.");
	            return response;
	        }
	        TravelReason travelReason = travelReasonRepository
	            .findByTravelReasonName(travelModeDTO.getTravelReason())
	            .orElseThrow(() -> new RuntimeException("TravelReason not found: " + travelModeDTO.getTravelReason()));
	        final String modeType = travelModeDTO.getModeType().trim();
	        Employee empName = travelModeDTO.getCreatedBy() != null
	                ? employeeRepository.findByEmpId(travelModeDTO.getCreatedBy())
	                : null;
	        final String creatorName = empName != null ? empName.getName() : "System";

	        if (travelModeDTO.getTravelModeId() != null) {
	            TravelMode existing = travelModeRepository.findById(travelModeDTO.getTravelModeId())
	                    .orElseThrow(() -> new RuntimeException("Travel mode not found: " + travelModeDTO.getTravelModeId()));
	            List<TravelMode> siblings = travelModeRepository.findByTravelReasonId(travelReason.getId());
	            for (TravelMode m : siblings) {
	                if (!m.getTravelModeId().equals(existing.getTravelModeId())
	                        && m.getModeType() != null
	                        && m.getModeType().trim().equalsIgnoreCase(modeType)) {
	                    throw new RuntimeException("Mode type already exists for this travel reason.");
	                }
	            }
	            existing.setTravelReason(travelReason);
	            existing.setModeType(modeType);
	            existing.setDescription(travelModeDTO.getDescription());
	            travelModeRepository.save(existing);
	        } else {
	            List<TravelMode> siblings = travelModeRepository.findByTravelReasonId(travelReason.getId());
	            for (TravelMode m : siblings) {
	                if (m.getModeType() != null && m.getModeType().trim().equalsIgnoreCase(modeType)) {
	                    throw new RuntimeException("Mode type already exists for this travel reason.");
	                }
	            }
	            TravelMode mode = new TravelMode();
	            mode.setTravelReason(travelReason);
	            mode.setModeType(modeType);
	            mode.setDescription(travelModeDTO.getDescription());
	            mode.setIsActive("Y");
	            mode.setCreatedBy(travelModeDTO.getCreatedBy());
	            mode.setCreatedByName(creatorName);
	            travelModeRepository.save(mode);
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Travel Mode saved successfully.");
	    } catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceError(e.getMessage());
	    }

	    return response;
	}

	public ServiceResponse deleteTravelMode(Long travelModeId) {
	    ServiceResponse response = new ServiceResponse();
	    try {
	        if (travelModeId == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError("travelModeId is required.");
	            return response;
	        }
	        if (travelClassRepository.countByTravelMode_TravelModeId(travelModeId) > 0) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError("Cannot delete: travel classes use this mode. Remove them first.");
	            return response;
	        }
	        travelModeRepository.deleteById(travelModeId);
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Travel mode deleted.");
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
	        List<TravelMode> modeList = travelModeRepository.findAllWithReason();

	        List<TravelModeDTO> dtoList = new ArrayList<>();

	        for (TravelMode mode : modeList) {
	            TravelModeDTO dto = new TravelModeDTO();
	            dto.setTravelModeId(mode.getTravelModeId());
	            dto.setModeType(mode.getModeType());
	            dto.setDescription(mode.getDescription());
	            dto.setIsActive(mode.getIsActive());
	            dto.setCreatedBy(mode.getCreatedBy());
	            dto.setCreatedOn(mode.getCreatedOn());
	            dto.setCreatedByName(mode.getCreatedByName());
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
	        serviceResponse.setServiceError("Error fetching travel modes: " + e.getMessage());
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
	            TravelReason travelReason = travelReasonRepository
	     	            .findByTravelReasonName(dto.getTravelReason())
	     	            .orElseThrow(() -> new RuntimeException("TravelReason not found: " + dto.getTravelReason()));

	        	 Long modeId = Long.parseLong(dto.getTravelMode());
	 	        TravelMode travelMode = travelModeRepository.findById(modeId)
	 	                .orElseThrow(() -> new RuntimeException("Travel Mode not found: " + modeId));
	            final String className = dto.getTravelClass() != null ? dto.getTravelClass().trim() : "";
	            if (className.isEmpty()) {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceError("Travel class name is required.");
	                return response;
	            }
	            Employee empName = dto.getCreatedBy() != null
	                    ? employeeRepository.findByEmpId(dto.getCreatedBy())
	                    : null;
	            final String creatorName = empName != null ? empName.getName() : "System";

	            if (dto.getTravelClassId() != null) {
	                TravelClass existing = travelClassRepository.findById(dto.getTravelClassId())
	                        .orElseThrow(() -> new RuntimeException("Travel class not found: " + dto.getTravelClassId()));
	                existing.setTravelReason(travelReason);
	                existing.setTravelMode(travelMode);
	                existing.setTravelClass(className);
	                existing.setDescription(dto.getDescription());
	                travelClassRepository.save(existing);
	            } else {
	                TravelClass travelClass = new TravelClass();
	                travelClass.setTravelReason(travelReason);
	                travelClass.setTravelMode(travelMode);
	                travelClass.setTravelClass(className);
	                travelClass.setDescription(dto.getDescription());
	                travelClass.setCreatedBy(dto.getCreatedBy());
	                travelClass.setIsActive("Y");
	                travelClass.setCreatedByName(creatorName);
	                travelClassRepository.save(travelClass);
	            }

	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("Travel Class saved successfully.");
	        } catch (Exception e) {
	        	e.printStackTrace();
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError(e.getMessage());
	        }

	        return response;
	    }

	public ServiceResponse deleteTravelClass(Long travelClassId) {
	    ServiceResponse response = new ServiceResponse();
	    try {
	        if (travelClassId == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError("travelClassId is required.");
	            return response;
	        }
	        travelClassRepository.deleteById(travelClassId);
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Travel class deleted.");
	    } catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceError(e.getMessage());
	    }
	    return response;
	}


	public ServiceResponse uploadTicket(MultipartFile file, String displayName, Long uploadedBy, BigInteger requestId) {

	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("Upload Ticket");
	    apiLogInfo.setApiUrl("/api/uploadTicket");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
	    logBuilder.append("Newsletter: ").append(displayName)
	              .append(", uploadedBy: ").append(uploadedBy);

	    try {
	        TravelDesk details=tavelDeskRepository.findByRequestId(requestId);
	        SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
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
	            
	            try {
	            	File file1 = null;
	            	 try {
	    			        
 			            Long docId = details.getTicketDocId();
 			            Resource resource = getTemplateFile(docId);
 			            if (resource != null && resource.exists()) {
 			                file1 = resource.getFile();
 			               
 			            }
 			        } catch (Exception e) {
 			        	e.printStackTrace();
//		            		serviceResponse.setServiceError(e.getMessage());
//		            		serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
 			        }

	            	if(details.getCity()!= null) {
	            		try {
	            		mailService.sendMail(
		            			details.getEmail(),
		            		    "Travel Accommodation Issued for Your Approved Request"+"",
		            		    "Dear "+ details.getName() + ","+"<br>" +
		            		    "<br>"+" &nbsp"+" &nbsp"+" "+"The Administration Team has successfully issued you the Accommodation Deatils for your travel"+ details.getRequestId()+ 
		            		    " for the purpose of"+details.getPurpose()+", scheduled from"+sdf1.format(details.getFromDate())+" to "+ sdf1.format(details.getToDate())+
		            		    ".It is uploaded now on IShine."+
		            		   
		            		    "<br>Details:"+
		            		    "<br>&bull; Hotel Booking Category:  "+details.getHotelCategory()+
		            		    "<br>&bull; Accommodation: "+"Yes"+
		            		    "<br>&bull; Destination City: "+details.getCity()+
		            		   
		            		   
		            		    "<br> Please review the Accommodation details and contact the Admin team immediately if any corrections are required."+
		            		    "<br>Wishing you a safe and successful journey."+
		            		      
		            		    "<br><br>Regards,<br>" +
		            		    "IShine Support Team<br>" +
		            		    "ApMoSys PVT. LTD.<br><br>"
	                		);}catch(Exception e) {
	                			 e.printStackTrace();
	                		}
	            		try {
	            			mailService.sendMailWithAttachment(
			            			adminHead,
			            			hrMailAddress,
			            		    "Travel Accommodation Issued - "+details.getName()+"",
			            		    "Dear "+ "HR Team" + ","+"<br>" +
			            		    "<br>"+" &nbsp"+" &nbsp"+" "+"The Accommodation request submitted by "+ details.getName()+" for the purpose of"+details.getPurpose()+
			            		    ",scheduled from "+sdf1.format(details.getFromDate())+" to "+ sdf1.format(details.getToDate())+
			            		    ", has been approved, and the Accommodation Details has been issued by the Admin team."+
			            		    
			            		    "<br>Details:"+
			            		    "<br>&bull; Hotel Booking Category:  "+details.getHotelCategory()+
			            		    "<br>&bull; Accommodation: "+"Yes"+
			            		    "<br>&bull; Destination City: "+details.getCity()+
			            		   
			            		    "<br>This is for your records and no further action is required at this time."+
			            		      
			            		    "<br><br>Regards,<br>" +
			            		    "IShine Support Team<br>" +
			            		    "ApMoSys PVT. LTD.<br><br>"
			            		,file1);
	            		}catch(Exception e){
	            			 e.printStackTrace();
	     	     	       
	            		}
	            	}else {
	            		
	            		try {
	            		mailService.sendMail(
		            			details.getEmail(),
		            		    "Travel Ticket Issued for Your Approved Request"+"",
		            		    "Dear "+ details.getName() + ","+"<br>" +
		            		    "<br>"+" &nbsp"+" &nbsp"+" "+"The Administration Team has successfully issued you the travel tickets for your travel"+ details.getRequestId()+ 
		            		    " for the purpose of"+details.getPurpose()+", scheduled from"+sdf1.format(details.getFromDate())+" to "+ sdf1.format(details.getToDate())+
		            		    ".It is uploaded now on IShine."+
		            		    
		            		    "<br>Details:"+
		            		    "<br>&bull; Travel Mode:  "+details.getTravelMode()+
		            		    "<br>&bull; Travel Class: "+details.getTravelClass()+
		            		   
		            		   
		            		    "<br> Please review the ticket details and contact the Admin team immediately if any corrections are required."+
		            		    "<br>Wishing you a safe and successful journey."+
		            		      
		            		    "<br><br>Regards,<br>" +
		            		    "IShine Support Team<br>" +
		            		    "ApMoSys PVT. LTD.<br><br>"
	                		);
	            		}catch(Exception e) {
	                			 e.printStackTrace();
	         	     	       
	                		}
	            		try {
	            			mailService.sendMailWithAttachment(
			            			adminHead,
			            			hrMailAddress,
			            		    "Travel Ticket Issued - "+details.getName()+"",
			            		    "Dear "+ "HR Team" + ","+"<br>" +
			            		    "<br>"+" &nbsp"+" &nbsp"+" "+"The Ticket request submitted by "+ details.getName()+" for the purpose of"+details.getPurpose()+
			            		    ",scheduled from "+sdf1.format(details.getFromDate())+" to "+ sdf1.format(details.getToDate())+
			            		    ", has been approved, and the Ticket Details has been issued by the Admin team."+
			            		    
                                    "<br>Details:"+
                                    "<br>&bull; Travel Mode:  "+details.getTravelMode()+
                                    "<br>&bull; Travel Class: "+details.getTravelClass()+
			            		   
			            		    "<br>This is for your records and no further action is required at this time."+
			            		      
			            		    "<br><br>Regards,<br>" +
			            		    "IShine Support Team<br>" +
			            		    "ApMoSys PVT. LTD.<br><br>"
			            		,file1);
	            		}catch(Exception e){
	            			 e.printStackTrace();
	     	     	       
	            		}

	            	}
	            	
	            }catch(Exception e) {
	            	 e.printStackTrace();
	     	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	     	        response.setServiceResponse("Something Went Wrong.");
	     	        response.setServiceError(e.getMessage());
	     	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	     	        apiLogInfo.setLogLevel("ERROR");
	            }
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

	
	private boolean isActiveMasterRow(String isActive) {
	    if (isActive == null || isActive.trim().isEmpty()) {
	        return true;
	    }
	    String v = isActive.trim().toUpperCase(Locale.ROOT);
	    return "Y".equals(v) || "YES".equals(v) || "TRUE".equals(v) || "1".equals(v);
	}

	private boolean isActiveMasterRow(TravelMode mode) {
	    return mode != null && isActiveMasterRow(mode.getIsActive());
	}

	private boolean isActiveMasterRow(HotelSubCategory sub) {
	    return sub != null && isActiveMasterRow(sub.getIsActive());
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

	        List<TravelMode> modeList = travelModeRepository.findByTravelReasonId(travelReasonId).stream()
	        		.filter(this::isActiveMasterRow)
	        		.collect(Collectors.toList());

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
	        if (hotelCategoryDTO.getHotelCategory() == null
	                || hotelCategoryDTO.getHotelCategory().trim().isEmpty()) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError("Hotel category name is required.");
	            return serviceResponse;
	        }
	        final String name = hotelCategoryDTO.getHotelCategory().trim();
	        Employee emp = hotelCategoryDTO.getCreatedBy() != null
	                ? employeeRepository.findByEmpId(hotelCategoryDTO.getCreatedBy())
	                : null;
	        final String empNameStr = emp != null ? emp.getName() : "System";

	        if (hotelCategoryDTO.getId() != null) {
	            HotelCategory existing = hotelCategoryRepository.findById(hotelCategoryDTO.getId())
	                    .orElseThrow(() -> new RuntimeException("Hotel category not found: " + hotelCategoryDTO.getId()));
	            existing.setHotelCategory(name);
	            existing.setDescription(hotelCategoryDTO.getDescription());
	            existing.setUpdatedBy(hotelCategoryDTO.getCreatedBy());
	            HotelCategory savedCategory = hotelCategoryRepository.save(existing);
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(savedCategory);
	        } else {
	            HotelCategory hotelCategory = new HotelCategory();
	            hotelCategory.setHotelCategory(name);
	            hotelCategory.setDescription(hotelCategoryDTO.getDescription());
	            hotelCategory.setIsActive("Y");
	            hotelCategory.setCreatedBy(hotelCategoryDTO.getCreatedBy());
	            hotelCategory.setCreatedByName(empNameStr);
	            HotelCategory savedCategory = hotelCategoryRepository.save(hotelCategory);
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(savedCategory);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        serviceResponse.setServiceError("Failed to save hotel category: " + e.getMessage());
	    }

	    return serviceResponse;
	}

	public ServiceResponse deleteHotelCategory(Long id) {
	    ServiceResponse response = new ServiceResponse();
	    try {
	        if (id == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError("id is required.");
	            return response;
	        }
	        if (hotelSubCategoryRepository.countByHotelCategory_Id(id) > 0) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError("Cannot delete: hotel sub-categories exist. Remove them first.");
	            return response;
	        }
	        if (cityRepository.countByHotelCategory_Id(id) > 0) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError("Cannot delete: cities are linked to this category. Remove them first.");
	            return response;
	        }
	        hotelCategoryRepository.deleteById(id);
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Hotel category deleted.");
	    } catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceError(e.getMessage());
	    }
	    return response;
	}
	
	
	public ServiceResponse getHotelCategory() {
	    ServiceResponse response = new ServiceResponse();
	    try {
	        List<HotelCategory> hotelCategories =
	            hotelCategoryRepository.findByIsActiveOrderByHotelCategoryAsc("Y");
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(hotelCategories);
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceError("Error fetching hotel categories: " + e.getMessage());
	    }
	    return response;
	}

	
	public ServiceResponse saveHotelSubCategory(HotelSubCategoryDTO dto) {
	    ServiceResponse response = new ServiceResponse();

	    try {
	    	Long hotelCategoryId = Long.parseLong(dto.getHotelCategory());
	    	HotelCategory hotelCategory = hotelCategoryRepository
	    	    .findById(hotelCategoryId)
	    	    .orElseThrow(() -> new RuntimeException("HotelCategory not found: " + dto.getHotelCategory()));
	        final String subName = dto.getHotelSubCategoryName() != null ? dto.getHotelSubCategoryName().trim() : "";
	        if (subName.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError("Sub-category name is required.");
	            return response;
	        }
	        Employee emp = dto.getCreatedBy() != null ? employeeRepository.findByEmpId(dto.getCreatedBy()) : null;
	        final String empNameStr = emp != null ? emp.getName() : "System";

	        if (dto.getId() != null) {
	            HotelSubCategory existing = hotelSubCategoryRepository.findById(dto.getId())
	                    .orElseThrow(() -> new RuntimeException("Hotel sub-category not found: " + dto.getId()));
	            existing.setHotelCategory(hotelCategory);
	            existing.setHotelSubCategoryName(subName);
	            existing.setDescription(dto.getDescription());
	            hotelSubCategoryRepository.save(existing);
	        } else {
	            HotelSubCategory subCategory = new HotelSubCategory();
	            subCategory.setHotelCategory(hotelCategory);
	            subCategory.setHotelSubCategoryName(subName);
	            subCategory.setDescription(dto.getDescription());
	            subCategory.setIsActive("Y");
	            subCategory.setCreatedBy(dto.getCreatedBy());
	            subCategory.setCreatedByName(empNameStr);
	            hotelSubCategoryRepository.save(subCategory);
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Hotel Sub-Category saved successfully.");
	    } catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceError("Error saving Hotel Sub-Category: " + e.getMessage());
	    }

	    return response;
	}

	public ServiceResponse deleteHotelSubCategory(Long id) {
	    ServiceResponse response = new ServiceResponse();
	    try {
	        if (id == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError("id is required.");
	            return response;
	        }
	        if (cityRepository.countByHotelSubCategory_Id(id) > 0) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError("Cannot delete: cities are linked to this sub-category. Remove them first.");
	            return response;
	        }
	        hotelSubCategoryRepository.deleteById(id);
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Hotel sub-category deleted.");
	    } catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceError(e.getMessage());
	    }
	    return response;
	}
	
	
	public ServiceResponse getHotelSubCategory() {
	    ServiceResponse response = new ServiceResponse();
	    try {
	        List<HotelSubCategory> hotelSubCategories = hotelSubCategoryRepository.findAll().stream()
	        		.filter(this::isActiveMasterRow)
	        		.collect(Collectors.toList());
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(hotelSubCategories);
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceError("Error fetching hotel sub-categories: " + e.getMessage());
	    }
	    return response;
	}

	public ServiceResponse getHotelSubCategoryByCategory(String hotelCategoryName) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	        if (hotelCategoryName == null || hotelCategoryName.trim().isEmpty()) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError("Hotel category is required.");
	            return serviceResponse;
	        }
	        List<HotelSubCategory> list = hotelSubCategoryRepository
	                .findByHotelCategory_HotelCategoryAndIsActiveOrderByHotelSubCategoryNameAsc(
	                        hotelCategoryName.trim(), "Y");
	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse(list);
	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        serviceResponse.setServiceError("Error fetching hotel sub-categories: " + e.getMessage());
	    }
	    return serviceResponse;
	}

	public ServiceResponse getCitiesByHotelSubCategoryId(Long hotelSubCategoryId) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	        if (hotelSubCategoryId == null) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError("Hotel sub-category is required.");
	            return serviceResponse;
	        }
	        if (!hotelSubCategoryRepository.existsById(hotelSubCategoryId)) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError("Hotel sub-category not found.");
	            return serviceResponse;
	        }
	        List<City> cityList = cityRepository
	                .findByHotelSubCategory_IdAndIsActiveOrderByCityNameAsc(hotelSubCategoryId, "Y");
	        if (cityList.isEmpty()) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError("No cities configured for this sub-category.");
	        } else {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(cityList);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        serviceResponse.setServiceError("Error fetching cities: " + e.getMessage());
	    }
	    return serviceResponse;
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

	        final String cityName = dto.getCityName() != null ? dto.getCityName().trim() : "";
	        if (cityName.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError("City name is required.");
	            return response;
	        }
	        Employee emp = dto.getCreatedBy() != null ? employeeRepository.findByEmpId(dto.getCreatedBy()) : null;
	        final String empNameStr = emp != null ? emp.getName() : "System";

	        if (dto.getCityId() != null) {
	            City existing = cityRepository.findById(dto.getCityId())
	                    .orElseThrow(() -> new RuntimeException("City not found: " + dto.getCityId()));
	            existing.setHotelCategory(hotelCategory);
	            existing.setHotelSubCategory(hotelSubCategory);
	            existing.setCityName(cityName);
	            existing.setDescription(dto.getDescription());
	            cityRepository.save(existing);
	        } else {
	            City city = new City();
	            city.setHotelCategory(hotelCategory);
	            city.setHotelSubCategory(hotelSubCategory);
	            city.setCityName(cityName);
	            city.setDescription(dto.getDescription());
	            city.setIsActive("Y");
	            city.setCreatedBy(dto.getCreatedBy());
	            city.setCreatedByName(empNameStr);
	            cityRepository.save(city);
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("City saved successfully.");
	    } catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceError("Error saving city: " + e.getMessage());
	    }

	    return response;
	}

	public ServiceResponse deleteCity(Long cityId) {
	    ServiceResponse response = new ServiceResponse();
	    try {
	        if (cityId == null) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceError("cityId is required.");
	            return response;
	        }
	        cityRepository.deleteById(cityId);
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("City deleted.");
	    } catch (Exception e) {
	        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceError(e.getMessage());
	    }
	    return response;
	}
	
	
	public ServiceResponse getTravelClassByTravelModeId(Long travelModeId) {
	    ServiceResponse serviceResponse = new ServiceResponse();
	    try {
	        if (travelModeId == null) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError("Travel mode is required.");
	            return serviceResponse;
	        }
	        List<TravelClass> classList = travelClassRepository.findAllByTravelMode_TravelModeId(travelModeId).stream()
	        		.filter(tc -> isActiveMasterRow(tc.getIsActive()))
	        		.collect(Collectors.toList());
	        if (classList.isEmpty()) {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            serviceResponse.setServiceError("No travel classes found for the selected mode.");
	        } else {
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            serviceResponse.setServiceResponse(classList);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        serviceResponse.setServiceError("Error fetching travel classes: " + e.getMessage());
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

	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse(cityList);
	        return serviceResponse;
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

	        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        serviceResponse.setServiceResponse(classList);
	        return serviceResponse;
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




