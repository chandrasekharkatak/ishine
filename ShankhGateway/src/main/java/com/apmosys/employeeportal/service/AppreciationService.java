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
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.model.Appreciation;
import com.apmosys.employeeportal.model.AppreciationEvent;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.SurveyEmployeeResponse;
import com.apmosys.employeeportal.repository.AppreciationRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EnableAppreciationRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.StringToDateTimeParser;

@Service
public class AppreciationService {

	@Autowired
	private AppreciationRepository appreciationRepository;

	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	StringToDateTimeParser stringToDateTimeParser;

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
		apiLogInfo.setSubFeatureName("Appreciation");
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
		apiLogInfo.setSubFeatureName("Appreciation Configuration");
		apiLogInfo.setApiUrl("/api/enableAppreciation");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("appreciationEventName : " +appreciationEventDTO.getAppreciationEventName() );
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
		
		AppreciationEventDTO appreciationEventInfo = new AppreciationEventDTO();
		try {
		List<Object[]> objectArrayList = enableAppreciationRepository.getAppreciationEventInfo();
		if (objectArrayList.isEmpty()) {
			return appreciationEventInfo;
		} else {
			objectArrayList.forEach((object) -> {

				EmployeeDTO dto = new EmployeeDTO();

//				appreciationEventInfo.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
				appreciationEventInfo.setAppreciationEventId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
				appreciationEventInfo.setAppreciationEventName(object[1] !=null ? object[1].toString():null);
				appreciationEventInfo.setCreatedOn(object[2] != null ? object[2].toString() : null);
			    appreciationEventInfo.setFromDate(object[3] != null ? object[3].toString() : null);
			    appreciationEventInfo.setToDate(object[4] != null ? object[4].toString() : null);
			});
			return appreciationEventInfo;
		}

	} catch (Exception e) {
		e.printStackTrace();

	}
	return appreciationEventInfo;
}
	public ServiceResponse getAppreciationEventSummaryInfo(AppreciationEventDTO AppreciationEventDTO) {
		AppreciationEventDTO appreciationEventInfo = new AppreciationEventDTO();
		 ServiceResponse response = new ServiceResponse();
		try {
		
			List<Object[]> objectArrayList = enableAppreciationRepository.getAppreciationEventSummaryInfo(AppreciationEventDTO.getAppreciationEventId());

			
           Long totalCountStar = appreciationRepository.countTotalAppreciationByIDandType_You_are_my_star(AppreciationEventDTO.getAppreciationEventId());
           Long totalCountGem = appreciationRepository.countTotalAppreciationByIDandType_You_are_Gem_of_a_Person(AppreciationEventDTO.getAppreciationEventId());
            Long totalCountProblemSolver = appreciationRepository.countTotalAppreciationByIDandType_You_are_A_Problem_Solver(AppreciationEventDTO.getAppreciationEventId());
            Long totalCountSupportive = appreciationRepository.countTotalAppreciationByIDandType_You_are_Supportive(AppreciationEventDTO.getAppreciationEventId());
            Long totalCountReliable = appreciationRepository.countTotalAppreciationByIDandType_You_are_Reliable(AppreciationEventDTO.getAppreciationEventId());            
            Long totalCountMotivator = appreciationRepository.countTotalAppreciationByIDandType_You_are_a_Motivator(AppreciationEventDTO.getAppreciationEventId());
            //System.out.println(AppreciationEventDTO.getAppreciationEventId());
            
           // System.out.println(totalCountStar);
           // System.out.println(totalCountGem);
           // System.out.println(totalCountProblemSolver);
           // System.out.println(totalCountSupportive);
          //  System.out.println(totalCountReliable);
           // System.out.println(totalCountMotivator);
			if (objectArrayList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Appreciation EventSummmaryInfo is empty.");
				 if(totalCountStar==null && totalCountGem==null && totalCountProblemSolver==null
						 && totalCountSupportive==null && totalCountReliable==null && totalCountMotivator==null) {					 
					 response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("Appreciation Event Total Count is empty");
				 }
				
			} else {
				
				    appreciationEventInfo.setTotalYouAreMyStarCount(totalCountStar);
				    appreciationEventInfo.setTotalYouAreGemOfAPersonCount(totalCountGem);
				    appreciationEventInfo.setTotalYouAreAproblemSolverCount(totalCountProblemSolver);
				    appreciationEventInfo.setTotalYouAreSupportiveCount(totalCountSupportive);
				    appreciationEventInfo.setTotalYouAreReliableCount(totalCountReliable);
				    appreciationEventInfo.setTotalYouAreAMotivatorCount(totalCountMotivator);
				    
				
				objectArrayList.forEach((object) -> {

					appreciationEventInfo.setAppreciationEventName(object[0] !=null ? object[0].toString():null);
					appreciationEventInfo.setCreatedOn(object[1] != null ? object[1].toString() : null);
				    appreciationEventInfo.setFromDate(object[2] != null ? object[2].toString() : null);
				    appreciationEventInfo.setToDate(object[3] != null ? object[3].toString() : null);
				   
				    
				    });
				
							
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(appreciationEventInfo);	
				
			}

		} catch (Exception e) {
		 e.printStackTrace();
		}
		return response;
	}
	
	public ServiceResponse getAllEmployeeAppreciationListByCategory(AppreciationEventDTO AppreciationEventDTO) {
	
		 ServiceResponse response = new ServiceResponse();
		 
		 try {
			 List<Object[]> objectArrayList = appreciationRepository.getAllEmployeeAppreciationListByCategory(AppreciationEventDTO.getAppreciationEventId());
			 
			 if (objectArrayList.isEmpty()) {
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					response.setServiceResponse("All Employee Appreciation List is empty.");
					 }
			 else {
				 List<AppreciationEventDTO> appreciationEventDTO = new ArrayList<>();
				 
				 
				 objectArrayList.forEach((object) -> {
					 AppreciationEventDTO appreciationEmployeeList = new AppreciationEventDTO();
//					 appreciationEmployeeList.setEmployeement_id(object[0] != null ? Long.parseLong(object[0].toString()) : null);
					 appreciationEmployeeList.setEmploymentIdAccToET(object[0] != null ? object[0].toString() : null);			 				 
					 appreciationEmployeeList.setName(object[1] != null ? object[1].toString() : null);
					 appreciationEmployeeList.setDepartment(object[2] != null ? object[2].toString() : null);
					 Long star = object[3] != null ? Long.parseLong(object[3].toString()) : 0L;
					    Long gem = object[4] != null ? Long.parseLong(object[4].toString()) : 0L;
					    Long problemSolver = object[5] != null ? Long.parseLong(object[5].toString()) : 0L;
					    Long supportive = object[6] != null ? Long.parseLong(object[6].toString()) : 0L;
					    Long reliable = object[7] != null ? Long.parseLong(object[7].toString()) : 0L;
					    Long motivator = object[8] != null ? Long.parseLong(object[8].toString()) : 0L;

					    appreciationEmployeeList.setTotalYouAreMyStarCount(star);
					    appreciationEmployeeList.setTotalYouAreGemOfAPersonCount(gem);
					    appreciationEmployeeList.setTotalYouAreAproblemSolverCount(problemSolver);
					    appreciationEmployeeList.setTotalYouAreSupportiveCount(supportive);
					    appreciationEmployeeList.setTotalYouAreReliableCount(reliable);
					    appreciationEmployeeList.setTotalYouAreAMotivatorCount(motivator);

					    // 👇 Set total appreciation
					    appreciationEmployeeList.setTotalAppreciation(
					        star + gem + problemSolver + supportive + reliable + motivator
					    );

					 
					 
					    appreciationEventDTO.add(appreciationEmployeeList);
					   
					    
					    });
					
								
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(appreciationEventDTO);	
				 
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
			}
			return response;
			 
			 
		 }
	
	public ServiceResponse viewAppreciationInfo(AppreciationEventDTO AppreciationEventDTO) {
		 
		 ServiceResponse response = new ServiceResponse();
		 
		 try {
			 List<Object[]>objectArrayList = appreciationRepository.viewAppreciationInfo(AppreciationEventDTO.getAppreciationEventId(),AppreciationEventDTO.getEmployeement_id());
			 
			 System.out.println(AppreciationEventDTO.getAppreciationEventId());
			 
			 if(objectArrayList.isEmpty()) {
				 response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				 response.setServiceResponse("All Employee Appreciation info is empty.");
			 }
			 else {
				 List<AppreciationEventDTO> appreciationEventDTO = new ArrayList<>();
				 objectArrayList.forEach((object) -> {
					 AppreciationEventDTO appreciationInfoList = new AppreciationEventDTO();
					 appreciationInfoList.setAppreciateType(object[0] != null ? object[0].toString():null);
					 appreciationInfoList.setComment(object[1] != null ? object[1].toString() : null);
					 appreciationInfoList.setAppreciationDate(object[2] != null ? object[2].toString() : null);
		
					    appreciationEventDTO.add(appreciationInfoList);
					      
				});
								
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					response.setServiceResponse(appreciationEventDTO);	
				 
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
			}
			return response;	
	}
	
	
	public ServiceResponse appreciationByCurrentUser(EmployeeDTO employeeDTO) {
		 
		 ServiceResponse response = new ServiceResponse();
		 try {
		 List<AppreciationDTO>objectArrayList = appreciationRepository.appreciationByCurrentUserToEmployees(employeeDTO.getAppreciationBy());
		 response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(objectArrayList);
	     

	    } catch (Exception e) {
	    	e.printStackTrace();
	    	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Error: " + e.getMessage());
	    }
	    return response; 
	}
	
	
	public ServiceResponse appreciationToCurrentUser(EmployeeDTO employeeDTO) {
		 
		 ServiceResponse response = new ServiceResponse();
		 try {
		 List<AppreciationDTO>objectArrayList = appreciationRepository.appreciationToCurrentUserToEmployees(employeeDTO.getAppreciationBy());
		 response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(objectArrayList);
	     

	    } catch (Exception e) {
	    	e.printStackTrace();
	    	response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	        response.setServiceResponse("Error: " + e.getMessage());
	    }
	    return response; 
	}
	
	
	
	
	
	
	
	
	

	public ServiceResponse getAllAppreciationEvent() {
	   ServiceResponse response = new ServiceResponse();
	   		
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
		eventDTO.setCreatedOn(allevents.getCreatedOn()!= null ? allevents.getCreatedOn().toString():null);
		eventDTO.setAppreciationEventType(allevents.getAppreciationEventType());
		appreciationEventDTO.add(eventDTO);
		
	}
	response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	response.setServiceResponse(appreciationEventDTO);
	}else {
		response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		response.setServiceResponse("Appreciation Event List is empty.");
		}
	}catch (Exception e) {
		e.printStackTrace();
		response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
		response.setServiceResponse("Something Went Wrong.");
		response.setServiceError(e.getMessage());
	}
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
		apiLogInfo.setSubFeatureName("Update Appreciation Event");
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
		//apiLogInfo.setSubFeatureName("Delete Appreciation Event");
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
	
	
    
    public ServiceResponse CountMyAppreciationBYcurrentUser(AppreciationDTO appreciationDTO) {
        ServiceResponse response = new ServiceResponse();
		
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setApiUrl("/api/CountMyAppreciationBYcurrentUser");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append("AppreciationTo: " +appreciationDTO.getAppreciationTo());
		try {
		  Long appreciationRecieved = appreciationRepository.countRecievedAppreciationBYcurrentUser(appreciationDTO.getAppreciationTo());
		   
          Long appreciationSent = appreciationRepository.countSentAppreciationByCurrentUser(appreciationDTO.getAppreciationBy());  
     
          
          //RECIEVED Appreciation BY TYPE
          Long appTypeYouAreMyStar = appreciationRepository.countMyAppreciationType_You_are_my_Star(appreciationDTO.getAppreciationTo());
          
          Long appTypeYouAreGemOfaPerson = appreciationRepository.countMyAppreciationType_You_are_Gem_of_a_Person(appreciationDTO.getAppreciationTo());
          
          Long appTypeYouAreAProblemSolver = appreciationRepository.countMyAppreciationType_You_are_A_Problem_Solver(appreciationDTO.getAppreciationTo());
          
          Long appTypeYouAreSupportive = appreciationRepository.countMyAppreciationType_You_are_Supportive(appreciationDTO.getAppreciationTo());
          
          Long appTypeYouAreReliable = appreciationRepository.countMyAppreciationType_You_are_Reliable(appreciationDTO.getAppreciationTo());
          
          Long appTypeYouAreAMotivator = appreciationRepository.countMyAppreciationType_You_are_a_Motivator(appreciationDTO.getAppreciationTo());
          
          //SENT Appreciation BY TYPE
          Long sentTypeYouAreMyStar = appreciationRepository.countSentAppreciationType_You_are_my_Star(appreciationDTO.getAppreciationBy());
          
          Long sentTypeYouAreGemOfaPerson = appreciationRepository.countSentAppreciationType_You_are_Gem_of_a_Person(appreciationDTO.getAppreciationBy());
          
          Long sentTypeYouAreAProblemSolver = appreciationRepository.countSentAppreciationType_You_are_A_Problem_Solver(appreciationDTO.getAppreciationBy());
          
          Long sentTypeYouAreSupportive = appreciationRepository.countSentAppreciationType_You_are_Supportive(appreciationDTO.getAppreciationBy());
          
          Long sentTypeYouAreReliable = appreciationRepository.countSentAppreciationType_You_are_Reliable(appreciationDTO.getAppreciationBy());
          
          Long sentTypeYouAreAMotivator = appreciationRepository.countSentAppreciationType_You_are_a_Motivator(appreciationDTO.getAppreciationBy());
          
          
          
              if (appreciationSent==0 && appreciationRecieved==0) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL); 
				response.setServiceResponse("No Appreciations found.");
				
				apiLogInfo.setApiResponse("No Appreciations found.");			
				apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);

			} else {
				appreciationDTO = new AppreciationDTO();
				appreciationDTO.setYouAreMyStarCount(appTypeYouAreMyStar);
				appreciationDTO.setYouAreGemOfAPersonCount(appTypeYouAreGemOfaPerson);
				appreciationDTO.setYouAreAproblemSolverCount(appTypeYouAreAProblemSolver);
				appreciationDTO.setYouAreSupportiveCount(appTypeYouAreSupportive);
				appreciationDTO.setYouAreReliableCount(appTypeYouAreReliable);
				appreciationDTO.setYouAreAMotivatorCount(appTypeYouAreAMotivator);
				
				appreciationDTO.setSentYouAreMyStarCount(sentTypeYouAreMyStar);
				appreciationDTO.setSentYouAreGemOfAPersonCount(sentTypeYouAreGemOfaPerson);
				appreciationDTO.setSentYouAreAproblemSolverCount(sentTypeYouAreAProblemSolver);
				appreciationDTO.setSentYouAreSupportiveCount(sentTypeYouAreSupportive);
				appreciationDTO.setSentYouAreReliableCount(sentTypeYouAreReliable);
				appreciationDTO.setSentYouAreAMotivatorCount(sentTypeYouAreAMotivator);
				
				
				appreciationDTO.setAppreciationSent(appreciationSent);
				appreciationDTO.setAppreciationReceived(appreciationRecieved);
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(appreciationDTO);
				
				apiLogInfo.setApiResponse("Appreication Sent : " + appreciationSent + " ,AppreciationReceived :" + appreciationRecieved);			
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
    
    
    public ServiceResponse getMyAppreciationDetails(AppreciationDTO appreciationDTO) {
		 ServiceResponse response = new ServiceResponse();
		 
	        LogDTO apiLogInfo = new LogDTO();
	        //apiLogInfo.setSubFeatureName("get_AppreciationDetails");
	        apiLogInfo.setApiUrl("/api/getMyAppreciationDetails");
	        apiLogInfo.setLogLevel("INFO");
	        
	        StringBuilder logBuilder = new StringBuilder();
	        logBuilder.append("Fetching appreciation details for employeement ID: ")
           .append(appreciationDTO.getEmployeementId())
           .append(" between dates ")
           .append(appreciationDTO.getStartDate())
           .append("and")
           .append(appreciationDTO.getEndDate());      
           
           apiLogInfo.setApiRequest(logBuilder.toString());
           
           try {
               List<Object[]> appreciationList = appreciationRepository.getMyAppreciationDetails(
                   appreciationDTO.getStartDate(),
                   appreciationDTO.getEndDate(),
                   appreciationDTO.getEmployeementId() // Use employeementId from DTO
               );
               
               Optional.ofNullable(appreciationList).ifPresentOrElse((list) -> {
                   if (list.isEmpty()) {
                       response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                       response.setServiceResponse("No appreciation details found.");
                       apiLogInfo.setApiResponse("No appreciation details found. The list is empty.");
                       apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                   }else {
                       List<AppreciationDTO> dtoList = new ArrayList<>();
                       list.forEach((object) -> {
                           AppreciationDTO dto = new AppreciationDTO();
                           dto.setAppreciationDate(object[0] != null ? object[0].toString() : null);
                           dto.setAppreciationEventName(object[1] != null ? object[1].toString() : null);
                           dto.setAppreciationBy(object[2] != null ? Long.parseLong(object[2].toString()) : null);
                           dto.setAppreciationByName(object[3] != null ? object[3].toString() : null);
                           dto.setAppreciationTo(object[4] != null ? Long.parseLong(object[4].toString()) : null);
                           dto.setAppreciationToName(object[5] != null ? object[5].toString() : null);
                           dto.setAppreciateType(object[6] != null ? object[6].toString() : null);
                           dto.setComment(object[7] != null ? object[7].toString() : null);
                           dto.setAppreciationByByEmpId(object[8] != null ? Long.parseLong(object[8].toString()) : null);
                           dto.setAppreciationToByEmpId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
                           
                           dtoList.add(dto);
                       });
                       response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                       response.setServiceResponse(dtoList);
                       apiLogInfo.setApiResponse("Fetched " + dtoList.size() + " appreciation details.");
                   }
               }, () -> {
                   response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                   response.setServiceResponse("No appreciation details found.");
                   apiLogInfo.setApiResponse("No appreciation details found.");
                   apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
               });
           } catch (Exception e) {
               response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
               response.setServiceResponse("An error occurred while fetching appreciation details.");
               apiLogInfo.setApiResponse("Error: " + e.getMessage());
               apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
               e.printStackTrace(); // Consider using a logging framework for production
           }

           logService.logMyInfo(httpRequest, apiLogInfo);// Assuming this method logs the information
           return response;
       }
    
    public ServiceResponse getTeamAppreciationDetails(AppreciationDTO appreciationDTO) {
        ServiceResponse response = new ServiceResponse();
        
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/api/getTeamAppreciationDetails");
        apiLogInfo.setLogLevel("INFO");

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("Fetching team appreciation details for emp_id: ")
           .append(appreciationDTO.getEmpId())
           .append(" between dates ")
           .append(appreciationDTO.getStartDate())
           .append(" and ")
           .append(appreciationDTO.getEndDate());

        apiLogInfo.setApiRequest(logBuilder.toString());

        try {
            
//            Long currentUserEmployeementId = appreciationRepository.findEmployeementIdByEmpId(appreciationDTO.getEmpId());          
//           
//            System.out.print(currentUserEmployeementId);
            List<Object[]> appreciationList = appreciationRepository.getTeamAppreciationDetails(
                appreciationDTO.getStartDate(),
                appreciationDTO.getEndDate(),
                appreciationDTO.getEmpId()             
                
            );

            Optional.ofNullable(appreciationList).ifPresentOrElse((list) -> {
                if (list.isEmpty()) {
                    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                    response.setServiceResponse("No appreciation details found.");
                    apiLogInfo.setApiResponse("No appreciation details found. The list is empty.");
                    apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
                } else {
                    List<AppreciationDTO> dtoList = new ArrayList<>();
                    list.forEach((object) -> {
                        AppreciationDTO dto = new AppreciationDTO();
                        dto.setAppreciationDate(object[0] != null ? object[0].toString() : null);
                        dto.setAppreciationEventName(object[1] != null ? object[1].toString() : null);
                        dto.setAppreciationBy(object[2] != null ? Long.parseLong(object[2].toString()) : null);
                        dto.setAppreciationByName(object[3] != null ? object[3].toString() : null);
                        dto.setAppreciationTo(object[4] != null ? Long.parseLong(object[4].toString()) : null);
                        dto.setAppreciationToName(object[5] != null ? object[5].toString() : null);
                        dto.setAppreciateType(object[6] != null ? object[6].toString() : null);
                        dto.setComment(object[7] != null ? object[7].toString() : null);
                        dto.setAppreciationByByEmpId(object[8] != null ? Long.parseLong(object[8].toString()) : null);
                        dto.setAppreciationToByEmpId(object[9] != null ? Long.parseLong(object[9].toString()) : null);
                        dtoList.add(dto);
                    });
                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                    response.setServiceResponse(dtoList);
                    apiLogInfo.setApiResponse("Fetched " + dtoList.size() + " appreciation details for the team.");
                }
            }, () -> {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("No appreciation details found.");
                apiLogInfo.setApiResponse("No appreciation details found.");
                apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
            });
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("An error occurred while fetching appreciation details.");
            apiLogInfo.setApiResponse("Error: " + e.getMessage());
            apiLogInfo.setApiStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            e.printStackTrace();
        }

        logService.logMyInfo(httpRequest, apiLogInfo); 
        return response;
    }

    }
	
	

