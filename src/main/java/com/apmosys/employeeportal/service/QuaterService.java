package com.apmosys.employeeportal.service;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.QuarterDto;
import com.apmosys.employeeportal.model.QuarterModel;
import com.apmosys.employeeportal.model.QueryTable;
import com.apmosys.employeeportal.repository.QuarterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class QuaterService {
	@Autowired
	private QuarterRepository quarterRepository;
	
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		
	public ServiceResponse saveQuarter(QuarterDto quarterDto) {
		ServiceResponse response = new ServiceResponse();
		try {
		QuarterModel quarter = new QuarterModel();
		if(quarterDto.getStartDate()!= null)
		{
			quarter.setStartdate(LocalDate.parse(quarterDto.getStartDate(), formatter));
		}
		if(quarterDto.getEndDate()!= null)
		{
			quarter.setEnddate(LocalDate.parse(quarterDto.getEndDate(), formatter));
		}
		QuarterModel saveQuery = quarterRepository.save(quarter);
		if(saveQuery != null) {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Query saved successfully !!");
		}else {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse(" Query not saved !!");
		}
		}
		catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong !!");
		}
		return response;
//		return null;
	}
//	public Boolean checkcompletiontime(LocalDate actualCompletionDate, LocalDate expectedCompletionDate)
//	{
//		if(actualCompletionDate == null || expectedCompletionDate == null) {return false;}
//		
//		boolean isOnTime = !actualCompletionDate.isAfter(expectedCompletionDate);
//		
//		if(isOnTime)
//		{
//			System.out.print("Done");
//		}
//		return isOnTime;
//	}
	
	
	

}
	

