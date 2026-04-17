package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.QresponseDTO;
import com.apmosys.employeeportal.model.Qresponse;
import com.apmosys.employeeportal.repository.QuarterCycleRepository;
import com.apmosys.employeeportal.repository.QresponseRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QresponseService {

    @Autowired
    private QresponseRepository qresponseRepository;

    @Autowired
    private QuarterCycleRepository quarteCycleRepository;

    public Boolean saveResponses(List<QresponseDTO> responses, Long empId, Long quarterId) {
        boolean flag = true;
        
        // First, fetch all existing responses for this employee and quarter
        List<Qresponse> existingResponses = qresponseRepository.findByEmpIdAndQuarterId(empId, quarterId);
        
        List<Qresponse> entitiesToSave = new ArrayList<>();
        
        for(QresponseDTO dto : responses) {
            // Try to find an existing response with the same question ID
            Optional<Qresponse> existingResponse = existingResponses.stream()
                .filter(resp -> resp.getId().equals(dto.getId()))
                .findFirst();
            
            if(existingResponse.isPresent()) {
                // Update existing response
                Qresponse entity = existingResponse.get();
                entity.setQuestionText(dto.getQuestionText());
                entity.setResponse(dto.getResponse());
                entity.setManagerRating(dto.getManagerRating());
                entity.setManagerRemark(dto.getManagerRemark());
                entitiesToSave.add(entity);
            } else {
                // Create new response
                Qresponse entity = new Qresponse();
                entity.setId(dto.getId());
                entity.setQuestionText(dto.getQuestionText());
                entity.setResponse(dto.getResponse());
                entity.setEmpId(empId);
                entity.setQuarterId(quarterId);
                entity.setManagerRating(dto.getManagerRating());
                entity.setManagerRemark(dto.getManagerRemark());
                entitiesToSave.add(entity);
            }
        }
        
        // Save all entities (both new and updated)
        this.qresponseRepository.saveAll(entitiesToSave);
        return flag;
    }

    public List<Qresponse> showResponse(Long empId, Long quarterId) {
        ServiceResponse response = new ServiceResponse();

        Long empId_quarterId = qresponseRepository.findempId(empId, quarterId);


        System.out.println("++++++++++++++++++++++++++++++++++++++"+empId_quarterId);
        if (empId_quarterId == null) {
            response.setServiceStatus("NOT_FOUND");
            response.setServiceMessage("No data found for employee ID " + empId + " and quarter ID " + quarterId);
            return new ArrayList<>();
        }

        List<Qresponse> qResponse = qresponseRepository.findByEmpIdAndQuarterId(empId, quarterId);

        response.setServiceStatus("SUCCESS");
        response.setServiceMessage("Data retrieved successfully");
        response.setServiceResponse(qResponse);

        return qResponse;
    }
}