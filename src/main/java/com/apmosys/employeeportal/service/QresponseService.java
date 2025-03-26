package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.QresponseDTO;
import com.apmosys.employeeportal.model.Qresponse; // Assuming you have an entity class for database mapping
import com.apmosys.employeeportal.repository.QuarterCycleRepository;
import com.apmosys.employeeportal.repository.QresponseRepository; // Assuming you have a JPA repository
import com.apmosys.employeeportal.service.QresponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class  QresponseService {

    @Autowired
    private QresponseRepository qresponseRepository;
    
    @Autowired
    private QuarterCycleRepository quarteCycleRepository;

    
    public void saveResponses(List<QresponseDTO> responses,Long empId,Long quarterId) {
    	//String quarter = quarteCycleRepository.findquartercyclebyID(quarterId);
        List<Qresponse> entities=new ArrayList<>();
        for(QresponseDTO obj:responses){
            List<Qresponse> employeeId12 = qresponseRepository.findempId(empId, quarterId);
            

            Qresponse employeeId=employeeId12.get(0);
        	Qresponse entity = new Qresponse();

            if(obj.getResponse() == null &	employeeId.getId()==  null) {
        		
        		entity.setId(obj.getId());
                entity.setQuestionText(obj.getQuestionText());
                entity.setResponse(null);
        	}else {
            entity.setId(obj.getId());
            entity.setQuestionText(obj.getQuestionText());
            entity.setResponse(obj.getResponse());
            entity.setEmpId(empId);
            entity.setQuarterId(quarterId);
        }
        entities.add(entity);  
        }
        this.qresponseRepository.saveAll(entities);
      
        

       
    }
}
