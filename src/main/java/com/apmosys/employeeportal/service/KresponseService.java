package com.apmosys.employeeportal.service;

	import com.apmosys.employeeportal.dto.KresponseDTO;
	import com.apmosys.employeeportal.model.Kresponse; 
	import com.apmosys.employeeportal.repository.QuarterCycleRepository;
	import com.apmosys.employeeportal.repository.KresponseRepository; 
	import com.apmosys.employeeportal.service.KresponseService;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.stereotype.Service;

	import java.util.List;
	import java.util.stream.Collectors;

	@Service
	public class KresponseService {

	    @Autowired
	    private KresponseRepository kresponseRepository;
	    
	    @Autowired
	    private QuarterCycleRepository quarteCycleRepository;

	    
	    public void saveResponses(List<KresponseDTO> responses,Long empId,Long quarterId) {
	    	String quarter = quarteCycleRepository.findquartercyclebyID(quarterId);
	        List<Kresponse> entities = responses.stream().map(response -> {
	            Kresponse entity = new Kresponse();
	            entity.setId(response.getId());
	            entity.setDescription(response.getDescription());
	            entity.setResponse(response.getResponse());
	            
	            entity.setEmpId(empId);
	            entity.setQuarterId(quarterId);
	            return entity;
	        }).collect(Collectors.toList());

	        kresponseRepository.saveAll(entities);
	    }
	}
	
	
	

