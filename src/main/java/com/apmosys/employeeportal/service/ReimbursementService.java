package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.ExpenditureTypeDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.ReimbursementDTO;
import com.apmosys.employeeportal.dto.TravelBasedReimbursementRequestDTO;
import com.apmosys.employeeportal.dto.TravelModeDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.ExpenditureType;
import com.apmosys.employeeportal.model.FoodType;
import com.apmosys.employeeportal.model.Newsletter;
import com.apmosys.employeeportal.model.ReimbursementData;
import com.apmosys.employeeportal.model.ReimbursementTravelMode;
import com.apmosys.employeeportal.model.VehicleType;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.ExpenditureTypeRepository;
import com.apmosys.employeeportal.repository.FoodTypeRepository;
import com.apmosys.employeeportal.repository.NewsletterRepository;
import com.apmosys.employeeportal.repository.ReimbursementDataRepository;
import com.apmosys.employeeportal.repository.ReimbursementTravelModeRepository;
import com.apmosys.employeeportal.repository.VehicleTypeRepository;
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
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;	
	
	@Autowired
	private NewsletterRepository newsletterRepository;
	
	@Autowired
	private ExpenditureTypeRepository expenditureTypeRepository;
	
	@Autowired
	private VehicleTypeRepository vehicleTypeRepository;
	
	@Autowired
	private FoodTypeRepository foodTypeRepository;
	
	@Autowired
	private ReimbursementTravelModeRepository reimbursementTravelModeRepository;
	

	@Value("${file.location.documents.reimbursement}")
	private String reimbursementFileLocation;
	
	@Value("${level2.Approver}")
	private String level2Approver ;
	
	@Value("${level3.Approver}")
	private String level3Approver ;
	
	@Value("${level2.ApproverMail}")
	private String level2ApproverMail ;
	
	@Value("${level3.ApproverMail}")
	private String level3ApproverMail ;

	public ServiceResponse fetchReimbursementData(BigInteger empId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			List<ReimbursementData> reimbursementData = reimbursementDataRepository.findByEmpId(empId);

			List<ReimbursementData> activeReimbursementData = reimbursementData.stream()
					.filter(t -> t.getIsActive() != 0).collect(Collectors.toList());

			if (activeReimbursementData.isEmpty() || activeReimbursementData == null) {
				serviceResponse.setServiceError("Data Not Found...!!");
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return serviceResponse;
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(activeReimbursementData);
				return serviceResponse;
			}
		} catch (Exception e) {
			serviceResponse.setServiceError(e.getMessage());
			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			return serviceResponse;
		}
	}

	public ServiceResponse fetchReimbursementDataforApproval(BigInteger empId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			List<ReimbursementData> activeReimbursement = new ArrayList<>();
			System.out.println(empId);
			List<ReimbursementData> reimbursementL1 = reimbursementDataRepository.findByApprover1(empId);
			System.out.println(reimbursementL1);
			List<ReimbursementData> reimbursementL2 = reimbursementDataRepository.findByApprover2(empId.toString());
			System.out.println(reimbursementL2);
//			List<ReimbursementData> reimbursementL3 = reimbursementDataRepository.findByApprover3(empId.toString());
//			System.out.println(reimbursementL3);
			if (!reimbursementL1.isEmpty()) {
				List<ReimbursementData> activeReimbursementL1 = reimbursementL1.stream().filter(
						t -> t.getStatus().equalsIgnoreCase("Pending") && t.getIsActive() == 1 && t.getLevel() == 1)
						.collect(Collectors.toList());
				System.out.println(activeReimbursementL1);
				activeReimbursement.addAll(activeReimbursementL1);
				System.out.println(activeReimbursement);
			}
			if (!reimbursementL2.isEmpty()) {
				List<ReimbursementData> activeReimbursementL2 = reimbursementL2.stream()
						.filter(t -> t.getLevel2approverStatus().equalsIgnoreCase("Pending") && t.getIsActive() == 1
								&& t.getLevel() == 2 && t.getFinalStatus().equalsIgnoreCase("Pending"))
						.collect(Collectors.toList());
				System.out.println(activeReimbursementL2);
				activeReimbursement.addAll(activeReimbursementL2);
				System.out.println(activeReimbursement);
			}
//			if (!reimbursementL3.isEmpty()) {
//				List<ReimbursementData> activeReimbursementL3 = reimbursementL3.stream()
//						.filter(t -> t.getLevel3approverStatus().equalsIgnoreCase("Pending") && t.getIsActive() == 1
//								&& t.getLevel() == 3 && t.getFinalStatus().equalsIgnoreCase("Pending"))
//						.collect(Collectors.toList());
//				System.out.println(activeReimbursementL3);
//				activeReimbursement.addAll(activeReimbursementL3);
//				System.out.println(activeReimbursement);
//			}

			if (activeReimbursement.isEmpty()) {
				serviceResponse.setServiceError("Data Not Found...!!");
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return serviceResponse;
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(activeReimbursement);
				return serviceResponse;
			}
		} catch (Exception e) {
			e.printStackTrace();
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
			reimbursementData.setCurrency("Rupees");
			   if (reimbursementObj.getDocIds() != null && !reimbursementObj.getDocIds().isEmpty()) {
		            String docIdsString = String.join(",", reimbursementObj.getDocIds().stream().map(String::valueOf).toArray(String[]::new));
		            reimbursementData.setDocId(docIdsString);
		        }
			//reimbursementData.setDocId(reimbursementObj.getDocId());
			//reimbursementData.setDocIds(reimbursementObj.getDocIds());
			reimbursementData.setExpenditureType(reimbursementObj.getExpenditureType());
			if (reimbursementObj.getExpenditureType().equalsIgnoreCase("Travel")) {
				reimbursementData.setTravelMode(reimbursementObj.getTravelMode());
				reimbursementData.setDistance(reimbursementObj.getDistance());
			}
//			BigInteger approver1 = BigInteger.valueOf((reimbursementObj.getLevelOneApprover()));
			String approver1 = reimbursementObj.getReportingManagerId();
			Long reportingManagerId = Long.parseLong(reimbursementObj.getReportingManagerId());
			Employee level1 = employeeRepository.findByEmpId(reportingManagerId);
			String approver2 = reimbursementObj.getLevelOneApprover();			
			Long empId = Long.parseLong(approver2); 
			Employee level2 = employeeRepository.findByEmpId(empId);
		//	Employee level3 = employeeRepository.findByEmpId(Long.valueOf(level3Approver));
			reimbursementData.setLevel2approverName(level2.getName());
			//reimbursementData.setLevel3approverName(level3.getName());
	        
			reimbursementData.setApprover2(approver2); 
			//reimbursementData.setApprover3(level3Approver);
			reimbursementData.setLevel2ApproverEmail(level2ApproverMail);
			//reimbursementData.setLevel3ApproverEmail(level3ApproverMail);
			reimbursementData.setLevel1ApproverEmail(level1.getEmail());
			reimbursementData.setApprover1(approver1);
			reimbursementData.setHodName(level1.getName());			
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
			reimbursementData.setLevel2approverStatus("Pending");
			reimbursementData.setLevel3approverStatus("Pending");
			reimbursementData.setFinalStatus("Pending");

			savedReimbursementData = reimbursementDataRepository.save(reimbursementData);

			if (savedReimbursementData == null) {
				serviceResponse.setServiceError("Unable to save..!!");
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceMessage("Failed to save...!!");
				return serviceResponse;
			} else {
				
//				File file = null;
//				try {
//				    Resource resource = getTemplateFile(Long.parseLong(savedReimbursementData.getDocId()));
//				    if (resource != null && resource.exists()) {
//				        file = resource.getFile(); 
//				    }
//				} catch (IOException e) {
//				    e.printStackTrace();
//				}
				File file = null;
				try {
				    String docIdsString = savedReimbursementData.getDocId();
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
				Employee emp = employeeRepository.findByEmpId(Long.valueOf(savedReimbursementData.getEmpId().toString()));
				
				if(reimbursementObj.getExpenditureType().equals("Food")) {
			      	mailService.sendMailWithAttachment(savedReimbursementData.getLevel1ApproverEmail(),savedReimbursementData.getEmail(),
	    	        		"Reimbursement request approval required",
	    	        		"Dear " + savedReimbursementData.getHodName() + ",<br><br>" +
	    	        				"A reimbursement request has been submitted by " + emp.getName() + ".<br>" +
	    	        				"Purpose: " + savedReimbursementData.getPurpose() + "<br>" +
//	    	        				"Location: " + savedReimbursementData.getCity() + "<br><br>" +
	    	        				"Kindly review and take the necessary action on this reimbursement application.<br><br>" +
	    	        				"Regards,<br>" +
	    	        				"iShine Reimbursement Desk", file);
				}else {
	            
	            	mailService.sendMailWithAttachment(savedReimbursementData.getLevel1ApproverEmail(),savedReimbursementData.getEmail(),
	    	        		"Reimbursement request approval required",
	    	        		"Dear " + savedReimbursementData.getHodName() + ",<br><br>" +
	    	        				"A reimbursement request has been submitted by " + emp.getName() + ".<br>" +
	    	        				"Purpose: " + savedReimbursementData.getPurpose() + "<br>" +
	    	        				"Duration: From " + savedReimbursementData.getFromDate().toGMTString() +
	    	        				" to " + savedReimbursementData.getToDate().toGMTString() + "<br><br>" +
//	    	        				"Location: " + savedReimbursementData.getCity() + "<br><br>" +
	    	        				"Kindly review and take the necessary action on this reimbursement application.<br><br>" +
	    	        				"Regards,<br>" +
	    	        				"iShine Reimbursement Desk", file);

				}
	            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(savedReimbursementData);
				serviceResponse.setServiceMessage("Saved Successfully..!!");
	            return serviceResponse;
			}
		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceError(e.getMessage());
			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			return serviceResponse;
		}

	}

	public ServiceResponse updateReimbursementData(ReimbursementDTO reimbursementObj) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			ReimbursementData existingReimbursementData = reimbursementDataRepository
					.findByRequestId(reimbursementObj.getRequestId());
			if (existingReimbursementData == null) {
				serviceResponse.setServiceError("Data Not Found...!!");
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return serviceResponse;
			} else {
				existingReimbursementData.setEmpId(reimbursementObj.getEmpId());
//				existingReimbursementData.setFullName(existingReimbursementData.getFullName());
//				existingReimbursementData.setCurrency(existingReimbursementData.getCurrency());
				existingReimbursementData.setEmail(reimbursementObj.getEmail());
				existingReimbursementData.setMobileNo(reimbursementObj.getMobileNo());
				existingReimbursementData.setDepartment(reimbursementObj.getDepartmentName());
				//existingReimbursementData.setFullName(reimbursementObj.getName());
				existingReimbursementData.setExpenditureType(reimbursementObj.getExpenditureType());
				if (reimbursementObj.getExpenditureType().equalsIgnoreCase("Travel")) {
					existingReimbursementData.setTravelMode(reimbursementObj.getTravelMode());
					existingReimbursementData.setDistance(reimbursementObj.getDistance());
				}
				existingReimbursementData.setEmail(reimbursementObj.getEmail());
				existingReimbursementData.setAmount(reimbursementObj.getAmount());
				//existingReimbursementData.setCurrency(reimbursementObj.getSelectedCurrency());
				existingReimbursementData.setPurpose(reimbursementObj.getPurpose());
				existingReimbursementData.setFromDate(reimbursementObj.getFromDate());
				existingReimbursementData.setToDate(reimbursementObj.getToDate());
				existingReimbursementData.setDateOfFood(reimbursementObj.getDateOfFood());
				existingReimbursementData.setAppliedBy(reimbursementObj.getEmpId());
				Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
				currentTimestamp.setNanos(currentTimestamp.getNanos() / 1000 * 1000);
				System.out.println(currentTimestamp);
				existingReimbursementData.setAppliedOn(currentTimestamp);
				existingReimbursementData.setStatus("Pending");
//				BigInteger bigInteger = new BigInteger(numberString);
				// BigInteger approver =
				// BigInteger.valueOf(reimbursementObj.getLevelOneApprover());
				// existingReimbursementData.setApprover1(approver);
				existingReimbursementData.setLevel(1);
				// BigInteger approver2 = new BigInteger(level2Approver);
				// existingReimbursementData.setApprover2(approver2);
				existingReimbursementData.setIsActive(1);
				ReimbursementData savedReimbursementdata = reimbursementDataRepository.save(existingReimbursementData);
				if (savedReimbursementdata == null) {
					serviceResponse.setServiceError("Unable to update...!!");
					serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
					return serviceResponse;
				} else {
					serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					serviceResponse.setServiceResponse(savedReimbursementdata);
					serviceResponse.setServiceMessage("Updated Successfully..!!");
					return serviceResponse;
				}
			}

		} catch (Exception e) {
			serviceResponse.setServiceError(e.getMessage());
			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			return serviceResponse;
		}

	}

	public ServiceResponse revokeReimbursement(BigInteger requestId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			ReimbursementData existingReimbursementData = reimbursementDataRepository.findByRequestId(requestId);

			if (existingReimbursementData == null) {
				serviceResponse.setServiceError("Request Data Not Found...!!");
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return serviceResponse;
			} else {
				existingReimbursementData.setIsActive(0);
				reimbursementDataRepository.save(existingReimbursementData);
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(existingReimbursementData);
				return serviceResponse;
			}
		} catch (Exception e) {
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
		logBuilder.append("Newsletter: ").append(displayName).append(", uploadedBy: ").append(uploadedBy);

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
				Files.createDirectories(directory); // Ensure directory exists
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
			ReimbursementData existingReimbursementData = reimbursementDataRepository
					.findByRequestId(reimbursementObj.getRequestId());

			if (existingReimbursementData == null) {
				serviceResponse.setServiceError("Request Data Not Found...!!");
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return serviceResponse;
			}

			Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
			currentTimestamp.setNanos(currentTimestamp.getNanos() / 1000 * 1000);
			Employee emp = employeeRepository
					.findByEmpId(Long.valueOf(existingReimbursementData.getEmpId().toString()));
		//if(reimbursementObj.getExpenditureType().equals("Travel")) {
							
			if (existingReimbursementData.getLevel() == 1) {
				updateApprovalLevel(existingReimbursementData, currentTimestamp, reimbursementObj);

				if (existingReimbursementData.getStatus() == "Approved") {
					mailService.sendMailforReimbursement(existingReimbursementData.getLevel1ApproverEmail(),
							"Reimbursement request approval required",
							"Dear " + existingReimbursementData.getLevel2approverName() + ",<br><br>" +
									"A reimbursement request submitted by " + emp.getName() + " has been reviewed and approved at HOD Level.<br>" +
									"Details of the request are as follows:<br><br>" +
									"<strong>Purpose:</strong> " + existingReimbursementData.getPurpose() + "<br>" +
									"From date:" + existingReimbursementData.getFromDate().toGMTString() +
									" To date:" + existingReimbursementData.getToDate().toGMTString() + "<br><br>" +
//									"<strong>Mode of Travel:</strong> " + existingReimbursementData.getTravelMode() + "<br><br>" +
									"Kindly review and take the necessary action on this request at your level.<br><br>" +
									"Regards,<br>" +
									"iShine Reimbursement Desk");

				}
			} else if (existingReimbursementData.getLevel() == 2) {
				updateApprovalLevel(existingReimbursementData, currentTimestamp, reimbursementObj);
				
//				mailService.sendMailforReimbursement(existingReimbursementData.getLevel1ApproverEmail(),
//						"Reimbursement request approval required",
//						"Dear Approver"+ ",<br><br>" +
//								"A reimbursement request submitted by " + emp.getName() + " has been reviewed and approved at HOD Level and HRM Level.<br>" +
//								"Details of the request are as follows:<br><br>" +
//								"<strong>Purpose:</strong> " + existingReimbursementData.getPurpose() + "<br>" +
//								"From date:" + existingReimbursementData.getFromDate().toGMTString() +
//								" To date:" + existingReimbursementData.getToDate().toGMTString() + "<br><br>" +
//								"Kindly review and take the necessary action on this request at your level.<br><br>" +
//								"Regards,<br>" +
//								"iShine Reimbursement Desk");
				
			} else {
				updateApprovalLevel(existingReimbursementData, currentTimestamp, reimbursementObj);

			}
		//}

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

	private void updateApprovalLevel(ReimbursementData reimbursementData, Timestamp currentTimestamp,
			ReimbursementDTO reimbursementObj) {
		if (reimbursementObj.getStatus().equalsIgnoreCase("Rejected")) {
			if (reimbursementData.getLevel() == 1) {
				reimbursementData.setLevel1ApproveOn(currentTimestamp);
				reimbursementData.setStatus(reimbursementObj.getStatus());
				reimbursementData.setLevel1approverRemarks(reimbursementObj.getLevel1approverRemarks());
				reimbursementData.setFinalStatus(reimbursementObj.getStatus());
			} else if (reimbursementData.getLevel() == 2) {
				reimbursementData.setLevel2ApproveOn(currentTimestamp);
				reimbursementData.setLevel2approverStatus(reimbursementObj.getStatus());
				reimbursementData.setLevel2approverRemarks(reimbursementObj.getLevel1approverRemarks());
				reimbursementData.setFinalStatus(reimbursementObj.getStatus());
			} 
//			else if (reimbursementData.getLevel() == 3) {
//				reimbursementData.setLevel3ApproveOn(currentTimestamp);
//				reimbursementData.setLevel3approverStatus(reimbursementObj.getStatus());
//				reimbursementData.setLevel3approverRemarks(reimbursementObj.getLevel1approverRemarks());
//				reimbursementData.setFinalStatus(reimbursementObj.getStatus());
//			}

		} else {
			if (reimbursementObj.getStatus().equalsIgnoreCase("Approved")) {
				if (reimbursementData.getLevel() == 1) {
					reimbursementData.setLevel1ApproveOn(currentTimestamp);
					reimbursementData.setLevel(reimbursementData.getLevel() + 1);
					reimbursementData.setStatus(reimbursementObj.getStatus());
					// reimbursementData.setFinalStatus("Pending");
					reimbursementData.setLevel1approverRemarks(reimbursementObj.getLevel1approverRemarks());
				} else if (reimbursementData.getLevel() == 2) {
					reimbursementData.setLevel2ApproveOn(currentTimestamp);
					reimbursementData.setLevel(reimbursementData.getLevel() + 1);
					reimbursementData.setLevel2approverStatus(reimbursementObj.getStatus());
					// reimbursementData.setStatus("Pending");
					reimbursementData.setLevel2approverRemarks(reimbursementObj.getLevel2approverRemarks());
					reimbursementData.setFinalStatus(reimbursementObj.getStatus());
				} 
//				else if (reimbursementData.getLevel() == 3) {
//					reimbursementData.setLevel3ApproveOn(currentTimestamp);
//					reimbursementData.setLevel3approverStatus(reimbursementObj.getStatus());
//					reimbursementData.setFinalStatus(reimbursementObj.getStatus());
//					reimbursementData.setLevel3approverRemarks(reimbursementObj.getLevel3approverRemarks());
//				}
			}
		}
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
		
		String Location = reimbursementFileLocation + File.separator + filename;
		File file = new File(Location);
		if (file.exists()) {
			resource = new FileSystemResource(Location);
		}
		} catch (Exception e) {
			e.printStackTrace();
		 throw new FileNotFoundException("File not found ");
		}
		return resource;

	} 
	
	
	public ServiceResponse fetchTotalReimbursementData() {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			List<ReimbursementData> totalReimbursement = new ArrayList<>();
			List<ReimbursementData> reimbursementList = reimbursementDataRepository.findAll();
			System.out.println(reimbursementList);
			
			if (!reimbursementList.isEmpty()) {
				totalReimbursement.addAll(reimbursementList);
				System.out.println(totalReimbursement);
			}

			if (totalReimbursement.isEmpty()) {
				serviceResponse.setServiceError("Data Not Found...!!");
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				return serviceResponse;
			} else {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse(totalReimbursement);
				return serviceResponse;
			}
		} catch (Exception e) {
			e.printStackTrace();
			serviceResponse.setServiceError(e.getMessage());
			serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			return serviceResponse;
		}
	}

	
	
	public ServiceResponse getAllDocumentsReimbursmentThroughRequestId(BigInteger requestId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		List<String> docIdList = new ArrayList<>();
		try {
			
			ReimbursementData travelData = reimbursementDataRepository.findByRequestId(requestId);
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



public ServiceResponse previewDocumentReimbursment(TravelBasedReimbursementRequestDTO reimbursementRequestDTO) {
	ServiceResponse response = new ServiceResponse();
	
	LogDTO apiLogInfo = new LogDTO();

	apiLogInfo.setSubFeatureName("Save");
	apiLogInfo.setApiUrl("/api/previewDocument");
	apiLogInfo.setLogLevel("INFO");
	StringBuilder logBuilder = new StringBuilder();
	

	
	try {


		TravelBasedReimbursementRequestDTO docDTO = new TravelBasedReimbursementRequestDTO();
		Newsletter travelDocDetails = newsletterRepository.findByDocId(reimbursementRequestDTO.getDocId());
		
				byte[] imageByte;
				try {
					
//					System.err.println("document"+traveldeskFileLocation);
				    String fullPath = reimbursementFileLocation + File.separator
				   
				        + travelDocDetails.getFileName();

				    Path path = Paths.get(fullPath);

				    if (Files.exists(path)) {
				        imageByte = Files.readAllBytes(path);
				        docDTO.setDocumentBytes(imageByte);
				        docDTO.setTicketFileName(travelDocDetails.getFileName());
//				        docDto.setFileName(travelDocDetails.getFileName());
				    } else {
				        System.err.println("File not found at path: " + fullPath);
				    }
				} catch (IOException e) {
				    e.printStackTrace(); // or use a logger
				}

				


			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(docDTO);
			
			apiLogInfo.setApiResponse("Documents Found !!");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);


	} catch (Exception e) {
		e.printStackTrace();
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse("Something Went Wrong.");
		response.setServiceError(e.getMessage());
		apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		apiLogInfo.setLogLevel("ERROR");
	}

	apiLogInfo.setApiRequest(logBuilder.toString());
	logService.logMyInfo(httpRequest, apiLogInfo);
	return response;
	
}



public ServiceResponse updateReimbursementDetailsByAccountsTeam(ReimbursementDTO reimbursementRequestDTO) {
	 ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setSubFeatureName("updateInvoicesDetailsByAccountsTeam");
	    apiLogInfo.setApiUrl("/api/updateInvoicesDetailsByAccountsTeam");
	    apiLogInfo.setLogLevel("INFO");

	    StringBuilder logBuilder = new StringBuilder();
//	    logBuilder.append("Newsletter: ").append(displayName)
//	              .append(", uploadedBy: ").append(uploadedBy);

	    try {
	       
//	        TravelBasedReimbursementRequest invoiceDetails= travelBasedReimbursementRequestRepository.findInvoiceDetails(reimbursementRequestDTO.getInvoiceNo());
	        ReimbursementData existingReimbursementData = reimbursementDataRepository
					.findByRequestId(reimbursementRequestDTO.getRequestId());
	        if(existingReimbursementData != null) {
	        	existingReimbursementData.setIsValid(true);
	        	existingReimbursementData.setReimbursementStatus("Approved");
	        	if(reimbursementRequestDTO.getRejectReason() != null) {
	        		existingReimbursementData.setRejectReason(reimbursementRequestDTO.getRejectReason());
	        		existingReimbursementData.setIsValid(false);
	        		existingReimbursementData.setReimbursementStatus("Rejected");
	        	}
	        	
//	        	 invoiceDetails.setDocId(travelDocDetails.getDocumentId());
//		            System.err.println("uploadedByinvoiceNo: " + invoiceDetails);
	        	ReimbursementData dbResponse=  reimbursementDataRepository.save(existingReimbursementData);
		            
		            
		            if(dbResponse != null) {
		            	  response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
							response.setServiceResponse(dbResponse);
							
							apiLogInfo.setApiResponse("Update Invoices Details By Accounts Team  Completed!!");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		            }else {
		            	  response.setServiceStatus(ServiceResponse.STATUS_FAIL);
							response.setServiceResponse(dbResponse);
							
							apiLogInfo.setApiResponse("Update Invoices Details By Accounts Team  Failed!!");
							apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
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
	    apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
}

public ServiceResponse saveExpenditureType(ExpenditureTypeDTO expenditureTypeDTO) {
    ServiceResponse serviceResponse = new ServiceResponse();
    try {
    	ExpenditureType expenditureType = new ExpenditureType();
    	expenditureType.setExpenditureTypeName(expenditureTypeDTO.getExpenditureTypeName());
    	expenditureType.setDescription(expenditureTypeDTO.getDescription());
    	expenditureType.setIsActive("Y");
    	expenditureType.setCreatedBy(expenditureTypeDTO.getCreatedBy());
    	System.out.println(expenditureType.toString());

    	ExpenditureType savedType = expenditureTypeRepository.save(expenditureType);

        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        serviceResponse.setServiceResponse(savedType); 
    } catch (Exception e) {
        e.printStackTrace();
        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
        serviceResponse.setServiceError("Failed to save expenditure Type: " + e.getMessage());
    }

    return serviceResponse;
}

public ServiceResponse getAllExpenditureType() {
    ServiceResponse serviceResponse = new ServiceResponse();
    try {
        List<ExpenditureType> typeList = expenditureTypeRepository.findAll();

        if (typeList.isEmpty()) {
            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
            serviceResponse.setServiceError("No travel reasons found.");
        } else {
            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            serviceResponse.setServiceResponse(typeList);
        }
    } catch (Exception e) {
        e.printStackTrace();
        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        serviceResponse.setServiceError("Error fetching travel reasons: " + e.getMessage());
    }

    return serviceResponse;
}

public ServiceResponse saveTravelMode(TravelModeDTO travelModeDTO) {
    ServiceResponse response = new ServiceResponse();

    try {
        // Fetch TravelReason entity by name
        ExpenditureType expenditureType = expenditureTypeRepository
            .findByExpenditureTypeName(travelModeDTO.getExpenditureType())
            .orElseThrow(() -> new RuntimeException("Expenditure not found: " + travelModeDTO.getExpenditureType()));

        ReimbursementTravelMode mode = new ReimbursementTravelMode();
        mode.setExpenditureType(expenditureType); // Set the entity, not the string
        mode.setModeType(travelModeDTO.getModeType());
        mode.setDescription(travelModeDTO.getDescription());
        mode.setIsActive("Y");
        mode.setRequiresVehicleType(travelModeDTO.getRequiresVehicleType());
        mode.setCreatedBy(travelModeDTO.getCreatedBy());

        reimbursementTravelModeRepository.save(mode);

        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        response.setServiceResponse("Travel Mode saved successfully.");
    } catch (Exception e) {
        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
        response.setServiceError(e.getMessage());
    }

    return response;
}

public ServiceResponse getAllgetTravelModes() {
    ServiceResponse serviceResponse = new ServiceResponse();
    try {
        List<ReimbursementTravelMode> modeList = reimbursementTravelModeRepository.findAll();
        List<TravelModeDTO> dtoList = new ArrayList<>();

        for (ReimbursementTravelMode mode : modeList) {
            TravelModeDTO dto = new TravelModeDTO();
            dto.setTravelModeId(mode.getTravelModeId());
            dto.setModeType(mode.getModeType());
            dto.setRequiresVehicleType(mode.getRequiresVehicleType());
            dto.setDescription(mode.getDescription());
            dto.setIsActive(mode.getIsActive());
            dto.setCreatedBy(mode.getCreatedBy());
            dto.setCreatedOn(mode.getCreatedOn());



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


public ServiceResponse saveVehicleType(TravelModeDTO travelModeDTO) {
    ServiceResponse serviceResponse = new ServiceResponse();
    try {
    	VehicleType vehicleType = new VehicleType();
    	vehicleType.setVehicleTypeName(travelModeDTO.getVehicleTypeName());
    	vehicleType.setDescription(travelModeDTO.getDescription());
    	vehicleType.setIsActive("Y");
    	vehicleType.setCreatedBy(travelModeDTO.getCreatedBy());

    	VehicleType savedType = vehicleTypeRepository.save(vehicleType);

        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        serviceResponse.setServiceResponse(savedType); 
    } catch (Exception e) {
        e.printStackTrace();
        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
        serviceResponse.setServiceError("Failed to save vehicle Type: " + e.getMessage());
    }

    return serviceResponse;
}


public ServiceResponse getAllVehicleType() {
    ServiceResponse serviceResponse = new ServiceResponse();
    try {
        List<VehicleType> typeList = vehicleTypeRepository.findAll();

        if (typeList.isEmpty()) {
            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
            serviceResponse.setServiceError("No travel reasons found.");
        } else {
            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            serviceResponse.setServiceResponse(typeList);
        }
    } catch (Exception e) {
        e.printStackTrace();
        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        serviceResponse.setServiceError("Error fetching travel reasons: " + e.getMessage());
    }

    return serviceResponse;
}

public ServiceResponse saveFoodType(TravelModeDTO travelModeDTO) {
    ServiceResponse serviceResponse = new ServiceResponse();
    try {
    	FoodType foodType = new FoodType();
    	foodType.setFoodTypeName(travelModeDTO.getFoodTypeName());
    	foodType.setDescription(travelModeDTO.getDescription());
    	foodType.setIsActive("Y");
    	foodType.setCreatedBy(travelModeDTO.getCreatedBy());

    	FoodType savedType = foodTypeRepository.save(foodType);

        serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
        serviceResponse.setServiceResponse(savedType); 
    } catch (Exception e) {
        e.printStackTrace();
        serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
        serviceResponse.setServiceError("Failed to save food Type: " + e.getMessage());
    }

    return serviceResponse;
}


public ServiceResponse getAllFoodType() {
    ServiceResponse serviceResponse = new ServiceResponse();
    try {
        List<FoodType> typeList = foodTypeRepository.findAll();

        if (typeList.isEmpty()) {
            serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
            serviceResponse.setServiceError("No travel reasons found.");
        } else {
            serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            serviceResponse.setServiceResponse(typeList);
        }
    } catch (Exception e) {
        e.printStackTrace();
        serviceResponse.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
        serviceResponse.setServiceError("Error fetching food type " + e.getMessage());
    }

    return serviceResponse;
}


	
}
