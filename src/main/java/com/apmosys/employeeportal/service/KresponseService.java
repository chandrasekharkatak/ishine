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

	    
	    public void saveResponses(List<KresponseDTO> responses,Long empId,Long quarterId) {
	    	List<Kresponse> entities=new ArrayList<>();
	        for(KresponseDTO obj:responses){
	            Long employeeId12 = kresponseRepository.findempId(empId, quarterId);
	            

	            Long employeeId=employeeId12;
	            System.out.println("++++++++++"+employeeId);
	        	Kresponse entity = new Kresponse();

	            if(obj.getResponse() == null &	employeeId ==  null) {
	        		
	        		entity.setId(obj.getId());
	                entity.setDescription(obj.getDescription());
	                entity.setResponse(null);
	        	}else {
	            entity.setId(obj.getId());
	            entity.setDescription(obj.getDescription());
	            entity.setResponse(obj.getResponse());
	            entity.setEmpId(empId);
	            entity.setQuarterId(quarterId);
	        }
	        entities.add(entity);  
	        }
	        this.kresponseRepository.saveAll(entities);
	    }
	}
	
	
	

