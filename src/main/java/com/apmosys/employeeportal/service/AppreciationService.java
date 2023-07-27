package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.AppreciationDTO;
import com.apmosys.employeeportal.dto.AppreciationEventDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.Appreciation;
import com.apmosys.employeeportal.model.AppreciationEvent;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.SurveyEmployeeResponse;
import com.apmosys.employeeportal.repository.AppreciationRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EnableAppreciationRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class AppreciationService {

	@Autowired
	private AppreciationRepository appreciationRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private MailService mailService;
	
	@Autowired
	private EnableAppreciationRepository enableAppreciationRepository;

	@Value("${hr.mail}")
	private String hrMailAddress;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;

	public ServiceResponse saveAppreciation(AppreciationDTO appreciationDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("save_appreciation");
		apiLogInfo.setApiUrl("/api/saveAppreciation");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("appreciationEventId : " + appreciationDTO.getAppreciationEventId() + "appreciationBy: " +appreciationDTO.getAppreciationBy()+ "appreciationTo : " +appreciationDTO.getAppreciationTo()+ ", appreciateType : "+ appreciationDTO.getAppreciateType() +", managerName : "+ appreciationDTO.getManagerName()+", reason : "+ appreciationDTO.getReason());
		
		
		try {
			String mailAddresses = hrMailAddress;
			System.out.println(hrMailAddress);
			
			Appreciation appreciation = new Appreciation();
			//Long id=appreciationDTO.getAppreciationBy();
			
			String subject="Appreciation";
			String name="";
			String nameAppreciate="";
			String email="";
			String emailAppreciated="";
			String manager="";
			
			List<Object[]> multipleAppreciation = 
					employeeRepository.checkMultipleAppreciation(appreciationDTO.getAppreciationEventId(),appreciationDTO.getAppreciationTo(),appreciationDTO.getAppreciationBy());
			
			if(multipleAppreciation.isEmpty()) {
			
	
			appreciation.setAppreciationBy(appreciationDTO.getAppreciationBy());
			appreciation.setAppreciationTo(appreciationDTO.getAppreciationTo());
			
			appreciation.setAppreciateType(appreciationDTO.getAppreciateType());
			appreciation.setManagerName(appreciationDTO.getManagerName());
			appreciation.setReason(appreciationDTO.getReason());
			appreciation.setAppreciationEventId(appreciationDTO.getAppreciationEventId());
			Appreciation dbResponse=appreciationRepository.save(appreciation);
	  
			String text="Hi " + appreciationDTO.getNameAppreciate() + ",<br> " + "You have been appreciated by :" + appreciationDTO.getName() + " as<br> \""
					+ appreciationDTO.getAppreciateType() + "\"<br>" + "Comment: " + appreciationDTO.getReason();
			mailAddresses=mailAddresses +","+ appreciationDTO.getManagerMail()+","+appreciationDTO.getEmail();
			System.out.println(mailAddresses);
			mailService.sendMailWithCC(appreciationDTO.getEmailAppreciated(), mailAddresses, subject,text );

			if(dbResponse!=null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Appreciation submitted");
				
				apiLogInfo.setApiResponse("Appreciation submitted");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
				
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Appreciation not submitted.");
				
				apiLogInfo.setApiResponse("Appreciation not submitted");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
			}
			else {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("This person has already appreciated by You ");
				
				apiLogInfo.setApiResponse("This person has already appreciated by You");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}
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


	public ServiceResponse enableAppreciation(AppreciationEventDTO appreciationEventDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/enableAppreciation");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("appreciationEventName : " +appreciationEventDTO.getAppreciationEventName());
		try {
		AppreciationEvent appEvent = new AppreciationEvent();
		appEvent.setAppreciationEventName(appreciationEventDTO.getAppreciationEventName());
		appEvent.setFromDate(appreciationEventDTO.getFromDate());
		appEvent.setToDate(appreciationEventDTO.getToDate());
		appEvent.setAppreciationEventType(appreciationEventDTO.getAppreciationEventType());
		
		AppreciationEvent enableAppreciation = enableAppreciationRepository.save(appEvent);

		List<Employee> allEmployees1 = new ArrayList<Employee>();
		
		List<Employee> allEmployees = employeeRepository.getAllActiveEmployees();
		
		if (!allEmployees.isEmpty()) {
			for (Employee e : allEmployees) {			
				e.setIsAppreciationEnable("false");
				allEmployees1.add(e);
			}
		}
		employeeRepository.saveAll(allEmployees1);
		
		List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
		dtoList = appreciationEventDTO.getEnableAppreciationList();
		System.out.println(dtoList);
		
		for(EmployeeDTO emp : dtoList) {
			Long empid= emp.getEmpId();
			System.out.println(empid);
			 Employee emp1 = new Employee(); 
			 emp1=employeeRepository.findByEmpId(emp.getEmpId());
			 if(emp1.getIsAppreciationEnable().equalsIgnoreCase("false")) {
				 emp1.setIsAppreciationEnable(emp.getIsAppreciationEnable());
				 emp1 = employeeRepository.save(emp1);
				 System.out.println(emp1.getIsAppreciationEnable());
			 }
		}	 			
		
		if(enableAppreciation != null)
		{
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Appreciation Enabled");
			
			apiLogInfo.setApiResponse("Appreciation Enabled");			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		}
		else {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Appreciation not Enabled.");
			
			apiLogInfo.setApiResponse("Appreciation not Enabled");			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
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


	public AppreciationEventDTO getAppreciationEventInfo() {
		ServiceResponse response = new ServiceResponse();
		AppreciationEventDTO appreciationEventInfo = new AppreciationEventDTO();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAppreciationEventInfo");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("appreciationEventName : " +appreciationEventInfo.getAppreciationEventName());
		
		try {
		List<Object[]> objectArrayList = enableAppreciationRepository.getAppreciationEventInfo();
		if (objectArrayList.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("No appreciation event found.");
			apiLogInfo.setApiResponse("No appreciation event found.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			return appreciationEventInfo;
			
		} else {
			objectArrayList.forEach((object) -> {

				EmployeeDTO dto = new EmployeeDTO();

//				appreciationEventInfo.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
//				appreciationEventInfo.setName(object[1] != null ? object[1].toString() : null);
				appreciationEventInfo.setAppreciationEventId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
				//appreciationEventInfo.setCreatedOn(object[1] != null ? object[1].toString() : null);
			    appreciationEventInfo.setFromDate(object[2] != null ? object[2].toString() : null);
			    appreciationEventInfo.setToDate(object[3] != null ? object[3].toString() : null);
			});
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Appreciation event info fetched.");
			apiLogInfo.setApiResponse("Appreciation event info fetched.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			return appreciationEventInfo;
		}

	} catch (Exception e) {
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse("Something went wrong.");
		apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		apiLogInfo.setLogLevel("ERROR");
		e.printStackTrace();

	}
	apiLogInfo.setApiRequest(logBuilder.toString());
	logService.logMyInfo(httpRequest, apiLogInfo);
	return appreciationEventInfo;
}

	public ServiceResponse getAllAppreciationEvent() {
	   ServiceResponse response = new ServiceResponse();
	   AppreciationEventDTO appreciationEventInfo = new AppreciationEventDTO();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("get_getAllAppreciationEvent");
		apiLogInfo.setApiUrl("/api/getAllAppreciationEvent");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("AllEventList :" + enableAppreciationRepository.findAll().size());
	   		
	try {
	List<AppreciationEvent> allEvent = enableAppreciationRepository.findAll();
	System.out.println("allEvent" +allEvent);
	List<AppreciationEventDTO> appreciationEventDTO = new ArrayList<>();
	if (!allEvent.isEmpty()) {
	for(AppreciationEvent allevents :allEvent) {
		AppreciationEventDTO eventDTO = new AppreciationEventDTO();
		eventDTO.setAppreciationEventId(allevents.getAppreciationEventid());
		eventDTO.setAppreciationEventName(allevents.getAppreciationEventName());
		eventDTO.setFromDate(allevents.getFromDate());	
		eventDTO.setToDate(allevents.getToDate());;	
		eventDTO.setCreatedOn(allevents.getCreatedOn().toString());
		eventDTO.setAppreciationEventType(allevents.getAppreciationEventType());
		appreciationEventDTO.add(eventDTO);
		
	}
	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	response.setServiceResponse(appreciationEventDTO);
	apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	apiLogInfo.setApiResponse("List fetched of size : "+appreciationEventDTO.size());
	}else {
		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		response.setServiceResponse("Appreciation Event List is empty.");
		apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		apiLogInfo.setApiResponse("Appreciation Event List is empty.");
		}
	}catch (Exception e) {
		e.printStackTrace();
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse("Something Went Wrong.");
		apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		apiLogInfo.setLogLevel("ERROR");
		response.setServiceError(e.getMessage());
	}
		apiLogInfo.setApiRequest(logBuilder.toString());
		logService.logMyInfo(httpRequest, apiLogInfo);
		return response;
	}


	public ServiceResponse viewAppreciation(AppreciationEventDTO appreciationEventDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("View Employees Appreciation");
		apiLogInfo.setApiUrl("/api/viewAppreciation");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("appreciationEventId : " + appreciationEventDTO.getAppreciationEventId()+ "appreciationEventName : " +appreciationEventDTO.getAppreciationEventName() );
		try {
			
			System.out.println();
			System.out.println(appreciationEventDTO.getAppreciationEventId() + " :    appreciationEventDTO.getAppreciationEventId()");
			System.out.println();
			System.out.println(appreciationEventDTO.getAppreciateType() + " :   appreciationEventDTO.getAppreciateType()");
			
		List<Object[]> list = appreciationRepository.getAppreciationByCategories(appreciationEventDTO.getAppreciationEventId(),appreciationEventDTO.getAppreciateType());
		System.out.println("list" +list);
		List<AppreciationDTO> dtoList = new ArrayList<AppreciationDTO>();
		if (!list.isEmpty()) {
			list.forEach((object) -> {
				AppreciationDTO appDto = new AppreciationDTO();
				appDto.setAppreciateType(object[0] != null ? object[0].toString() : null);
				appDto.setAppreciationToName(object[5] != null ? object[5].toString() : null);
				appDto.setAppreciationDate(object[3] != null ? object[3].toString() : null);
				appDto.setManagerName(object[6] != null ? object[6].toString() : null);
				appDto.setReason(object[7] != null ? object[7].toString() : null);
				appDto.setAppreciationByName(object[2] != null ? object[2].toString() : null);
				dtoList.add(appDto);
				
			});
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);			
			response.setServiceResponse(dtoList);
			
			apiLogInfo.setApiResponse("dtoList"+dtoList);			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
		}
			else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("list is empty !!");
				
				apiLogInfo.setApiResponse("list is empty !!");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
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
	
//OnCheckEventName
	public ServiceResponse OnCheckEventName(AppreciationEventDTO appreciationEventDTO) {
		
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		//apiLogInfo.setSubFeatureName("Appreciation");
		apiLogInfo.setApiUrl("/api/OnCheckEventName");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("appreciationEventId : " + appreciationEventDTO.getAppreciationEventId()+ "appreciationEventName : " +appreciationEventDTO.getAppreciationEventName() );
		try {
			
			AppreciationEvent dbEventName = enableAppreciationRepository.findByAppreciationEventName(appreciationEventDTO.getAppreciationEventName()); 
			System.out.println("Db EventName "+dbEventName);
			System.out.println("appreciationEventDTO.getAppreciationEventName() "+appreciationEventDTO.getAppreciationEventName());
			if(dbEventName != null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Event Name already exist !!");
				
				apiLogInfo.setApiResponse("Event Name already exist !!");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				
				apiLogInfo.setApiResponse("Event Name does not exist !!");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}
			
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
	public ServiceResponse updateAppreciationEvent(AppreciationEventDTO appreciationEventDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Update_appreciationEvent");
		apiLogInfo.setApiUrl("/api/updateAppreciationEvent");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("appreciationEventId : " + appreciationEventDTO.getAppreciationEventId()+ "appreciationEventName : "  +appreciationEventDTO.getAppreciationEventName()+ "fromDate : " +appreciationEventDTO.getFromDate()+ "toDate : " +appreciationEventDTO.getToDate());
		try {
			Optional<AppreciationEvent> appreciationEvent = enableAppreciationRepository.findById(appreciationEventDTO.getAppreciationEventId());

			if(appreciationEvent.isPresent()) {
				
			AppreciationEvent appEvent = appreciationEvent.get();
			appEvent.setAppreciationEventName(appreciationEventDTO.getAppreciationEventName());
			appEvent.setFromDate(appreciationEventDTO.getFromDate());
			appEvent.setToDate(appreciationEventDTO.getToDate());
			appEvent.setAppreciationEventType(appreciationEventDTO.getAppreciationEventType());
			
			AppreciationEvent dbResponse = enableAppreciationRepository.save(appEvent);
			

			List<Employee> allEmployees1 = new ArrayList<Employee>();
			
			List<Employee> allEmployees = employeeRepository.getAllActiveEmployees();
			
			if (!allEmployees.isEmpty()) {
				for (Employee e : allEmployees) {			
					e.setIsAppreciationEnable("false");
					allEmployees1.add(e);
				}
			}
			employeeRepository.saveAll(allEmployees1);
			
			
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
			dtoList = appreciationEventDTO.getEnableAppreciationList();
			System.out.println(dtoList);
			
			for(EmployeeDTO emp : dtoList) {
				Long empid= emp.getEmpId();
				System.out.println(empid);
				 Employee emp1 = new Employee(); 
				 emp1=employeeRepository.findByEmpId(emp.getEmpId());
				 if(emp1.getIsAppreciationEnable().equalsIgnoreCase("false")) {
					 emp1.setIsAppreciationEnable(emp.getIsAppreciationEnable());
					 emp1 = employeeRepository.save(emp1);
					 System.out.println(emp1.getIsAppreciationEnable());
				 }
			}	
			
			if (dbResponse != null) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Appreciation Event Updated.");
				
				apiLogInfo.setApiResponse("Appreciation Event Updated.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			} else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Appreciation Event Updation Failed.");
				
				apiLogInfo.setApiResponse("Appreciation Event Updation Failed");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
			}
		} else {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Appreciation Event Not Found");
			
			apiLogInfo.setApiResponse("Appreciation Event Not Found");			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		}
			
			
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
	
	
	public ServiceResponse deleteAppreciationEvent(AppreciationEventDTO appreciationEventDTO) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("Delete Appreciation Event");
		apiLogInfo.setApiUrl("/api/deleteAppreciationEvent");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("appreciationEventId : " + appreciationEventDTO.getAppreciationEventId());
		
		
		try {
			Optional<AppreciationEvent> appreciationEventObj = enableAppreciationRepository
					.findById(appreciationEventDTO.getAppreciationEventId());
			if (appreciationEventObj.isPresent()) {

				AppreciationEvent appEvent = appreciationEventObj.get();
				enableAppreciationRepository.deleteById(appreciationEventDTO.getAppreciationEventId());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Appreciation Event Deleted Successfully.");
				
				apiLogInfo.setApiResponse("Appreciation Event Deleted Successfully.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			}
			 else {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Appreciation Event Not Found");
					
					apiLogInfo.setApiResponse("Appreciation Event Not Found");			
					apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
				}
					
			
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


	public ServiceResponse getAppreciateEmployeeByCurrentUser(AppreciationDTO appreciationDTO) {
		ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/getAppreciateEmployeeByCurrentUser");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("appreciateBy : " + appreciationDTO.getAppreciationBy()+ "appreciationEventId :" +appreciationDTO.getAppreciationEventId());
		try {
		List<Object[]> list = appreciationRepository.getAppreciateEmployeeByCurrentUser(appreciationDTO.getAppreciationBy(),appreciationDTO.getAppreciationEventId());
		System.out.println("list" +list);
		List<AppreciationDTO> dtoList = new ArrayList<AppreciationDTO>();
		if (!list.isEmpty()) {
			list.forEach((object) -> {
				AppreciationDTO appDto = new AppreciationDTO();
				appDto.setAppreciateType(object[0] != null ? object[0].toString() : null);
				appDto.setAppreciationBy(object[1] != null ? Long.parseLong(object[1].toString()) : null);
				appDto.setAppreciationDate(object[2] != null ? object[2].toString() : null);
				appDto.setAppreciationTo(object[3] != null ? Long.parseLong(object[3].toString()) : null);
				appDto.setAppreciationEventId(object[4] != null ? Long.parseLong(object[4].toString()) : null);
				dtoList.add(appDto);
				
			});
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);			
			response.setServiceResponse(dtoList);
			
			apiLogInfo.setApiResponse("dtoList : " +dtoList);			
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			
			
		}
			else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("list is empty !!");
				
				apiLogInfo.setApiResponse("list is empty !!");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
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
	
}
	
	

