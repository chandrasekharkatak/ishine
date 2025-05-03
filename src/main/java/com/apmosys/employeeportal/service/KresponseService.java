package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.KresponseDTO;
import com.apmosys.employeeportal.model.Kresponse;
import com.apmosys.employeeportal.repository.QuarterCycleRepository;
import com.apmosys.employeeportal.repository.KresponseRepository; 
import com.apmosys.employeeportal.utility.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        List<Kresponse> entitiesToSave = new ArrayList<>();
        if(!responses.isEmpty()){
            List<Kresponse> dbResponse = kresponseRepository.findByEmpIdAndQuarterId(empId, quarterId);

            if(!dbResponse.isEmpty()){
                kresponseRepository.deleteAll(dbResponse);
            }
            

            responses.forEach((object) -> {
                Kresponse entity = new Kresponse();
//                entity.setKpiId(object.getId());
                entity.setDescription(object.getDescription());
                entity.setResponse(object.getResponse());
                entity.setEmpId(empId);
                entity.setQuarterId(quarterId);
                entity.setIsFixed(object.getIsFixed());
                entity.setRemark(object.getRemark());
                entity.setProgress(object.getProgress());
                entitiesToSave.add(entity);
            });
            
        }
        kresponseRepository.saveAll(entitiesToSave);
        
        // List<Kresponse> existingResponses = kresponseRepository.findByEmpIdAndQuarterId(empId, quarterId);
        // List<Kresponse> entitiesToSave = new ArrayList<>();
        
    
        // for (KresponseDTO dto : responses) {
        //     Long kpiId = dto.getId();
        //     boolean found = false;
        //     for (Kresponse existingResponse : existingResponses) {




        //         Long existingId  = existingResponse.getKpiId();
                
        //         if (existingId.equals(kpiId)) {
        //             existingResponse.setDescription(dto.getDescription());
        //             existingResponse.setResponse(dto.getResponse());
        //             existingResponse.setIsFixed(dto.getIsFixed());
        //             existingResponse.setRemark(dto.getRemark());
        //             existingResponse.setProgress(dto.getProgress());
        //             entitiesToSave.add(existingResponse);
        //             found = true;
        //             break;
        //         }
        //     }
            
        //     if (!found) {
        //         Kresponse entity = new Kresponse();
        //         entity.setKpiId(kpiId);
        //         entity.setDescription(dto.getDescription());
        //         entity.setResponse(dto.getResponse());
        //         entity.setEmpId(empId);
        //         entity.setQuarterId(quarterId);
        //         entity.setIsFixed(dto.getIsFixed());
        //         entity.setRemark(dto.getRemark());
        //         entity.setProgress(dto.getProgress());
        //         entitiesToSave.add(entity);
        //     }
        // }
        
        // kresponseRepository.saveAll(entitiesToSave);
        return flag;
    }
    public List<Kresponse> showResponse(Long empId, Long quarterId) {
        ServiceResponse response = new ServiceResponse();
        Long empId_quarterId = kresponseRepository.findempId(empId, quarterId);
        if (empId_quarterId == null) {
            response.setServiceStatus("NOT_FOUND");
            response.setServiceMessage("No data found for employee ID " + empId + " and quarter ID " + quarterId); // Return empty list instead of "return response;"
        }
        List<Kresponse> KResponse = kresponseRepository.findByEmpIdAndQuarterId(empId, quarterId);
        response.setServiceStatus("SUCCESS");
        response.setServiceMessage("Data retrieved successfully");
        response.setServiceResponse(KResponse);
        return KResponse;
    }
}