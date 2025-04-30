package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.KresponseDTO;
import com.apmosys.employeeportal.model.Kresponse;
import com.apmosys.employeeportal.repository.QuarterCycleRepository;
import com.apmosys.employeeportal.repository.KresponseRepository; 
import com.apmosys.employeeportal.utility.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KresponseService {
    @Autowired
    private KresponseRepository kresponseRepository;
    
    @Autowired
    private QuarterCycleRepository quarteCycleRepository;
    
    public Boolean saveResponses(List<KresponseDTO> responses, Long empId, Long quarterId) {
        boolean flag = true;
        
      
        List<Kresponse> existingResponses = kresponseRepository.findByEmpIdAndQuarterId(empId, quarterId);
        List<Kresponse> entitiesToSave = new ArrayList<>();
        int i=0;
        for(KresponseDTO dto : responses){
            
            if(existingResponses.size() != 0 ) {
    
                Kresponse entity = existingResponses.get(i);
                entity.setDescription(dto.getDescription());
                entity.setResponse(dto.getResponse());

                entity.setIsFixed(dto.getIsFixed());

                entity.setRemark(dto.getRemark());
                entity.setProgress(dto.getProgress());
                entitiesToSave.add(entity);

                i++;
                
            } else {
                Kresponse entity = new Kresponse();
                entity.setKpiId(dto.getId());
                entity.setDescription(dto.getDescription());
                entity.setResponse(dto.getResponse());
                entity.setEmpId(empId);
                entity.setQuarterId(quarterId);
                entity.setIsFixed(dto.getIsFixed());
                entity.setRemark(dto.getRemark());
                entity.setProgress(dto.getProgress());
                entitiesToSave.add(entity);
            }
        }
        

        this.kresponseRepository.saveAll(entitiesToSave);
        return flag;
    }
    public List<Kresponse> showResponse(Long empId, Long quarterId) {
        ServiceResponse response = new ServiceResponse();
        Long empId_quarterId = kresponseRepository.findempId(empId, quarterId);
        if (empId_quarterId == null) {
            // Handle case where no data exists for this employee and quarter
            response.setServiceStatus("NOT_FOUND");
            response.setServiceMessage("No data found for employee ID " + empId + " and quarter ID " + quarterId); // Return empty list instead of "return response;"
        }
        // Fetch the response data for the given employee and quarter
        List<Kresponse> KResponse = kresponseRepository.findByEmpIdAndQuarterId(empId, quarterId);
        // If you want to return the entire table data
        response.setServiceStatus("SUCCESS");
        response.setServiceMessage("Data retrieved successfully");
        response.setServiceResponse(KResponse);
        return KResponse;
    }
}