package com.apmosys.employeeportal.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.AppreciationDTO;
import com.apmosys.employeeportal.model.Appreciation;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.repository.AppreciationRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class AppreciationService {

	@Autowired
	private AppreciationRepository appreciationRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private MailService mailService;

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
			
	
			appreciation.setAppreciationBy(appreciationDTO.getAppreciationBy());
			appreciation.setAppreciationTo(appreciationDTO.getAppreciationTo());
			
			appreciation.setAppreciateType(appreciationDTO.getAppreciateType());
			appreciation.setManagerName(appreciationDTO.getManagerName());
			appreciation.setReason(appreciationDTO.getReason());
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
			
		}
		
		return response;
		
	}



}
