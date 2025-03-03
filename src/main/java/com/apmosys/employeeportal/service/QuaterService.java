package com.apmosys.employeeportal.service;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.QuarterDto;
import com.apmosys.employeeportal.model.QuarterModel;
import com.apmosys.employeeportal.repository.QuarterRepository;

@Service
public class QuaterService {
	@Autowired
	private QuarterRepository quarterRepository;
	
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	
	public QuarterModel saveQuarter(QuarterDto quarterDto) {
		QuarterModel quarter = new QuarterModel();
		if(quarterDto.getStartDate()!= null)
		{
			quarter.setStartdate(LocalDate.parse(quarterDto.getStartDate(), formatter));
		}
		if(quarterDto.getEndDate()!= null)
		{
			quarter.setEnddate(LocalDate.parse(quarterDto.getEndDate(), formatter));
		}
		 return quarterRepository.save(quarter);
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
	

