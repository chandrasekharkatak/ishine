package com.apmosys.employeeportal.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.BioMaxRequestDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.BioMaxRequestIssue;
import com.apmosys.employeeportal.model.BiomaxRequest;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeLeavesMap;
import com.apmosys.employeeportal.model.LeaveBalanceLog;
import com.apmosys.employeeportal.repository.BioMaxRequestIssueRepository;
import com.apmosys.employeeportal.repository.BiomaxRequestRepository;
import com.apmosys.employeeportal.repository.EmployeeLeavesMapRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.LeaveBalanceLogRepository;
import com.apmosys.employeeportal.serviceInterface.BioMaxRequestService;
import com.apmosys.employeeportal.utility.LeaveLogMessage;
import com.apmosys.employeeportal.utility.ServiceResponse;


@Service
public class BioMaxRequestServiceImp implements BioMaxRequestService{

	@Autowired
	private BiomaxRequestRepository bioMaxRequestRepository;
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired 
	private LeaveBalanceLogRepository leaveBalanceLogRepository;
	
	@Autowired
	private BioMaxRequestIssueRepository bioMaxRequestIssueRepository;
	@Autowired
	private CronJobService cronJobService;
	
	@Autowired
	private EmployeeLeavesMapRepository employeeLeavesMapRepository;
	
	@Override
	public ServiceResponse createBioMaxRequest(BioMaxRequestDTO biomaxRequest) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Add createBioMaxRequest");
		apiLogInfo.setApiUrl("/api/createBioMaxRequest");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BioMax Request Type :" + biomaxRequest.getEmpId() + " ,Approved By :" + biomaxRequest.getStatusBy());
		try {
			BiomaxRequest biomax=new BiomaxRequest();
			biomax.setBiomaxrequestDate(biomaxRequest.getBiomaxrequestDate());
			biomax.setEmpId(biomaxRequest.getEmpId());
			biomax.setBiomaxStatus("Pending");
			biomax.setCreatedOn(LocalDateTime.now());
			biomax.setBiomaxTitle(biomaxRequest.getBiomaxTitle());
			biomax.setRequestRemark(biomaxRequest.getRequestRemark());
			biomax.setReportingManagerId(biomaxRequest.getReportingManagerId());
			bioMaxRequestRepository.save(biomax);
			response.setServiceStatus("success");
			response.setServiceMessage("Biomax Request Has been Raised assign to your Reporting Manager");
			response.setServiceResponse(biomax);
			
		}catch(Exception e) {
			logBuilder.append("Error"+e.getMessage());
			response.setServiceStatus("fails");
			response.setServiceMessage("Some Thing Wrong"+e.getMessage());

		}
		return response;
	}

	@Override
	public ServiceResponse updateBioMaxRequest(Long id, BioMaxRequestDTO biomaxRequestDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("updateBioMaxRequest");
		apiLogInfo.setApiUrl("/api/updateBioMaxRequest");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BioMax Request Type :" + biomaxRequestDTO.getEmpId() + " ,Approved By :" + biomaxRequestDTO.getStatusBy());
		try {
			Optional<BiomaxRequest> exitingBiomaxRequestForUpdate=bioMaxRequestRepository.findById(id);
			if(exitingBiomaxRequestForUpdate.isPresent()) {
				BiomaxRequest biomax=exitingBiomaxRequestForUpdate.get();
				biomax.setBiomaxStatus(biomaxRequestDTO.getBiomaxStatus());
				biomax.setEnabled(true);
				//biomax.setStatusDate(LocalDateTime.now());
				biomax.setStatusBy(biomaxRequestDTO.getStatusBy());
				biomax.setBiomaxStatus(biomaxRequestDTO.getBiomaxStatus());
				if(biomax.getBiomaxrequestDate().isBefore(LocalDateTime.now())) {
					afterLeaveApprovedLeaveAdded(biomaxRequestDTO.getEmpId());
				}
				bioMaxRequestRepository.save(biomax);
				
				response.setServiceStatus("success");
				response.setServiceMessage("Biomax Request Has been "+biomaxRequestDTO.getBiomaxStatus()+" to your Reporting Manager");
				response.setServiceResponse(biomax);
			}else {
				response.setServiceStatus("fails");
				response.setServiceMessage("Biomax Request Not found");
				
			}
		
			
		}catch(Exception e) {
			logBuilder.append("Error"+e.getMessage());
			response.setServiceStatus("fails");
			response.setServiceMessage("Some Thing Wrong"+e.getMessage());

		}
		return response;
	}
public void afterLeaveApprovedLeaveAdded(Long empid) {
	try {
	List<LeaveBalanceLog> log1=leaveBalanceLogRepository.findLogValidation(empid);
	
	Employee employee=employeeRepository.findByEmpId(empid);
	System.out.println("log1"+log1.size());
	if(log1.size()==0) {
	
	if(employee.getEmploymentstatus().equals("Confirmed")) {
		EmployeeLeavesMap employeeLeaveMapObject = employeeLeavesMapRepository
				.findByEmpIdAndLeaveTypeMasterId(empid, (short) 3);
		Float newBalance = employeeLeaveMapObject.getBalance()
				+ Float.parseFloat("0.5");

		employeeLeaveMapObject.setBalance(newBalance);
		EmployeeLeavesMap dbResponse = employeeLeavesMapRepository
				.save(employeeLeaveMapObject);
		LeaveBalanceLog log = new LeaveBalanceLog();

		log.setBalance(newBalance);
		log.setEmpId(empid);
		log.setLeaveTypeMasterId((short) 3);
		log.setMessage(LeaveLogMessage.autoDeductLeaveOnTimesheetDefaulter
				.replace("0.0", "0.5"));
		log.setUpdateBalanceBy("+" + "0.5");

		LeaveBalanceLog leaveLogDbResponse = leaveBalanceLogRepository
				.save(log);
	}
	}
	}catch(Exception e) {
		e.printStackTrace();
	

	}
}
	@Override
	public ServiceResponse getByEmployeeId(Long empid) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getByEmployeeId");
		apiLogInfo.setApiUrl("/api/getByEmployeeId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BioMax Request Type :" +empid );
		List<BioMaxRequestDTO> listbiomax=new ArrayList<>();
	try {
		List<BiomaxRequest> list=bioMaxRequestRepository.findByEmpId(empid);
		if(list.size()>0) {
			list.forEach((e)->{
				BioMaxRequestDTO ob=new BioMaxRequestDTO();
				ob.setBiomaxreequestId(e.getBiomaxreequestId());
				ob.setBiomaxStatus(ob.getBiomaxStatus());
				ob.setBiomaxTitle(e.getBiomaxTitle());
				Optional<BioMaxRequestIssue> requestType=bioMaxRequestIssueRepository.findById(e.getBiomaxTitle());
				if(requestType.isPresent()) {
					ob.setBiomaxTitleValue(requestType.get().getBioMaxRequestIssueName());
				}
				ob.setReportingManagerId(e.getReportingManagerId());
				
				Optional<Employee> emp=employeeRepository.findById(e.getReportingManagerId());
				if(emp.isPresent()) {
					ob.setReportingManagerName(emp.get().getName());
				}
				ob.setCreatedOn(e.getCreatedOn());
				ob.setEmpId(e.getEmpId());
				ob.setBiomaxStatus(e.getBiomaxStatus());
				ob.setRequestRemark(e.getRequestRemark());
				ob.setBiomaxrequestDate(e.getBiomaxrequestDate());
				listbiomax.add(ob);
			});
			response.setServiceStatus("true");
			response.setServiceMessage("Record Found");
			response.setServiceResponse(listbiomax);
		}else {
			response.setServiceStatus("true");
			response.setServiceMessage("Not Record Found");
		}
	}catch(Exception e) {
		logBuilder.append("Exception found "+e.getMessage());
	}
		return response;
	}

	@Override
	public ServiceResponse getByRepostingManager(Long reportingManagerId) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getByEmployeeId");
		apiLogInfo.setApiUrl("/api/getByEmployeeId");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BioMax Request Type :" +reportingManagerId );
		
		try {
		List<BiomaxRequest> list=bioMaxRequestRepository.findByReportingManagerId(reportingManagerId);
		List<BioMaxRequestDTO> listbiomax=new ArrayList<>();
		
			if(list.size()>0) {
				list.forEach((e)->{
					BioMaxRequestDTO ob=new BioMaxRequestDTO();
					ob.setBiomaxreequestId(e.getBiomaxreequestId());
					ob.setBiomaxStatus(ob.getBiomaxStatus());
					ob.setBiomaxTitle(e.getBiomaxTitle());
					ob.setReportingManagerId(e.getReportingManagerId());
					Optional<BioMaxRequestIssue> requestType=bioMaxRequestIssueRepository.findById(e.getBiomaxTitle());
					if(requestType.isPresent()) {
						ob.setBiomaxTitleValue(requestType.get().getBioMaxRequestIssueName());
					}
					Optional<Employee> emp=employeeRepository.findById(e.getEmpId());
					if(emp.isPresent()) {
						ob.setEmpName(emp.get().getName());
					}
					Optional<Employee> rep=employeeRepository.findById(e.getReportingManagerId());
					if(rep.isPresent()) {
						ob.setReportingManagerName(rep.get().getName());
					}
					ob.setCreatedOn(e.getCreatedOn());
					ob.setEmpId(e.getEmpId());
					ob.setBiomaxStatus(e.getBiomaxStatus());
					ob.setRequestRemark(e.getRequestRemark());
					ob.setBiomaxrequestDate(e.getBiomaxrequestDate());
					listbiomax.add(ob);
				});
			response.setServiceStatus("true");
			response.setServiceMessage("Record Found");
			response.setServiceResponse(listbiomax);
		}else {
			response.setServiceStatus("true");
			response.setServiceMessage("Not Record Found");
		}

		return response;
		}
		catch(Exception e) {
			logBuilder.append("Exception found "+e.getMessage());
		}
		return response;
	}
	

	@Override
	public ServiceResponse deletedRequest(Long id) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("deletedRequest");
		apiLogInfo.setApiUrl("/api/deletedRequest");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BioMax Request Type Id :" +id );
	try {
		Optional<BiomaxRequest> list=bioMaxRequestRepository.findById(id);
		if(list.isPresent()) {
			bioMaxRequestRepository.delete(list.get());
			response.setServiceStatus("true");
			response.setServiceMessage("Record Found has been deleted");
		}else {
			response.setServiceStatus("true");
			response.setServiceMessage("Not Record Found");
		}
	}catch(Exception e) {
		logBuilder.append("Exception found "+e.getMessage());
	}
		return response;
	}

	@Override
	public ServiceResponse getById(Long id) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getById");
		apiLogInfo.setApiUrl("/api/getById");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BioMax Request Type :" +id );
	try {
		Optional<BiomaxRequest> list=bioMaxRequestRepository.findById(id);
		if(list.isPresent()) {
			response.setServiceStatus("true");
			response.setServiceMessage("Record Found");
			response.setServiceResponse(list.get());
		}else {
			response.setServiceStatus("true");
			response.setServiceMessage("Not Record Found");
		}
	}catch(Exception e) {
		logBuilder.append("Exception found "+e.getMessage());
	}
		return response;
	}

	@Override
	public ServiceResponse getBioMaxRequestType() {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getBioMaxRequestType");
		apiLogInfo.setApiUrl("/api/getBioMaxRequestType");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("BioMax Request Type Id :" );
	try {
		List<BioMaxRequestIssue> list=bioMaxRequestIssueRepository.findAll();
		if(list.size()>0) {
			
			response.setServiceStatus("true");
			response.setServiceMessage("Record Found has been deleted");
			response.setServiceResponse(list);
		}else {
			response.setServiceStatus("true");
			response.setServiceMessage("Not Record Found");
		}
	}catch(Exception e) {
		logBuilder.append("Exception found "+e.getMessage());
	}
		return response;
	}

	@Override
	public ServiceResponse getBioMaxRequestTypeCronJon() {
		// TODO Auto-generated method stub
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getById");
		apiLogInfo.setApiUrl("/api/getById");
		apiLogInfo.setLogLevel("INFO");
		cronJobService.leaveDeduct();
		System.out.println("Sdfdsg");
		
		return response;
	}
	
	@Override
	public ServiceResponse leaveDeductRoleBackForEmloyee(BioMaxRequestDTO bioMaxRequestDTO) {
		// TODO Auto-generated method stub
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("getById");
		apiLogInfo.setApiUrl("/api/getById");
		apiLogInfo.setLogLevel("INFO");
		response.setServiceResponse(bioMaxRequestDTO);
		
		return response;
	}


}
