package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.AppreciationDTO;
import com.apmosys.employeeportal.dto.AppreciationEventDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
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

	public ServiceResponse saveAppreciation(AppreciationDTO appreciationDTO) {
		ServiceResponse response = new ServiceResponse();
		
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
				//return response;	
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Appreciation not submitted.");
			}
			}
			else {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("This person has already appreciated by You ");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
		}
		
		return response;
		
	}


	public ServiceResponse enableAppreciation(AppreciationEventDTO appreciationEventDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
		AppreciationEvent appEvent = new AppreciationEvent();
		appEvent.setAppreciationEventName(appreciationEventDTO.getAppreciationEventName());
		appEvent.setFromDate(appreciationEventDTO.getFromDate());
		appEvent.setToDate(appreciationEventDTO.getToDate());
		
		AppreciationEvent enableAppreciation = enableAppreciationRepository.save(appEvent);

		List<Employee> allEmployees1 = new ArrayList<Employee>();
		
		List<Employee> allEmployees = employeeRepository.getAllActiveEmployees();
		
		if (!allEmployees.isEmpty()) {
			for (Employee e : allEmployees) {			
				e.setIsAppreciationEnable("false");
				allEmployees1.add(e);
			}
//		employeeRepository.saveAll(allEmployees1);
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
		}
		else {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Appreciation not Enabled.");
		}
		
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}

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
//				appreciationEventInfo.setName(object[1] != null ? object[1].toString() : null);
				appreciationEventInfo.setAppreciationEventId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
				appreciationEventInfo.setCreatedOn(object[1] != null ? object[1].toString() : null);
			    appreciationEventInfo.setFromDate(object[2] != null ? object[2].toString() : null);
			    appreciationEventInfo.setToDate(object[3] != null ? object[3].toString() : null);
			});
			return appreciationEventInfo;
		}

	} catch (Exception e) {
		e.printStackTrace();

	}
	return appreciationEventInfo;
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
		try {
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
		}
			else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("list is empty !!");
				
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}
	
//OnCheckEventName
	public ServiceResponse OnCheckEventName(AppreciationEventDTO appreciationEventDTO) {
		
		ServiceResponse response = new ServiceResponse();
		try {
			
			AppreciationEvent dbEventName = enableAppreciationRepository.findByAppreciationEventName(appreciationEventDTO.getAppreciationEventName()); 
			System.out.println("Db EventName "+dbEventName);
			System.out.println("appreciationEventDTO.getAppreciationEventName() "+appreciationEventDTO.getAppreciationEventName());
			if(dbEventName != null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Event Name already exist !!");
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
		
	}


}
