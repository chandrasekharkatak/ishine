package com.apmosys.employeeportal.service;

	import com.apmosys.employeeportal.dto.KresponseDTO;
	import com.apmosys.employeeportal.model.Kresponse; 
	import com.apmosys.employeeportal.repository.QuarterCycleRepository;
	import com.apmosys.employeeportal.repository.KresponseRepository; 
	import com.apmosys.employeeportal.service.KresponseService;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
	import java.util.stream.Collectors;

	@Service
	public class KresponseService {

	    @Autowired
	    private KresponseRepository kresponseRepository;
	    
	    @Autowired
	    private QuarterCycleRepository quarteCycleRepository;

	    //change whole for checking primary key
	    public void saveResponses(List<KresponseDTO> responses,Long empId,Long quarterId) {
	    	List<Kresponse> entities=new ArrayList<>();
	        for(KresponseDTO obj:responses){
	            Long employeeId = kresponseRepository.findempId(empId, quarterId);
	            

//	            Long employeeId=employeeId12;
	        	Kresponse entity = new Kresponse();//check for primary key 
	        	//no twice calling of api

	            if(obj.getResponse() == null &&	employeeId ==  null) {
	        		
	        		entity.setKpiId(obj.getId());
	                entity.setDescription(obj.getDescription());
	                entity.setResponse(null);
	                entity.setReview(null);
	                entity.setIsFixed(obj.getIsFixed());
	        	}else {
	            entity.setKpiId(obj.getId());
	            entity.setDescription(obj.getDescription());
	            entity.setResponse(obj.getResponse());
	            entity.setEmpId(empId);
	            entity.setQuarterId(quarterId);
	            entity.setReview(obj.getReview());
	            entity.setIsFixed(obj.getIsFixed());
	        	}
	        entities.add(entity);  
	        }
	        this.kresponseRepository.saveAll(entities);
	    }
	}
	
	
	

