package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.EmployeeGoalDTO;
import com.apmosys.employeeportal.model.EmployeeGoals;
import com.apmosys.employeeportal.model.GoalTemplates;
import com.apmosys.employeeportal.repository.EmployeeGoalRepository;
import com.apmosys.employeeportal.repository.GoalTemplatesRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import java.util.*;

@Service
public class EmployeeGoalService {

    @Autowired
    private EmployeeGoalRepository employeeGoalRepository;
    
    @Autowired
    private GoalTemplatesRepository goalTemplateRepo;
    
    @Autowired
    private GoalTemplateService goalTemplateService;
    
    public EmployeeGoalDTO assignGoalToEmployee(Long empId, Long templateId, LocalDate expectedCompletionDate) {
        ServiceResponse response = goalTemplateService.getGoalTemplateById(templateId);

        if (!ServiceResponse.STATUS_SUCCESS.equals(response.getServiceStatus())) {
            throw new RuntimeException("Goal template not found with ID: " + templateId);
        }

//        GoalTemplatesDto template = (GoalTemplatesDto) response.getServiceResponse();
        Optional<GoalTemplates> vopt=goalTemplateRepo.findById(templateId);
        GoalTemplates template=null;
          if(vopt.isPresent()) template=vopt.get();
//        System.out.println("Template: " + template.getTitle() + ", HOD ID: " + template.getCreatedById());

        EmployeeGoalDTO employeeGoalDTO = new EmployeeGoalDTO();
        employeeGoalDTO.setEmpId(empId);
        employeeGoalDTO.setTemplateId(templateId);
        employeeGoalDTO.setAssignedBy(template.getCreatedBy());
        employeeGoalDTO.setGoalTitle(template.getTitle());
//        employeeGoalDTO.setGoalTitle(template.getTitle());
        employeeGoalDTO.setGoalProgress("Not Started"); // Default status
        employeeGoalDTO.setGoalStatus("Pending"); // Default review status
        employeeGoalDTO.setExpectedCompletionDate(expectedCompletionDate);
        employeeGoalDTO.setCreatedDate(LocalDate.now());

        // Convert DTO to entity, save, and convert back to DTO
        EmployeeGoals savedGoal = employeeGoalRepository.save(convertToEntity(employeeGoalDTO));
        return convertToDTO(savedGoal);
    }
    
    public ServiceResponse getGoalsByEmployeeId(Long empId) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            List<EmployeeGoals> goals = employeeGoalRepository.findByEmpId(empId);
            
            if (!goals.isEmpty()) {
                List<EmployeeGoalDTO> dtoList = goals.stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
                
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dtoList);
                response.setServiceMessage("Employee goals retrieved successfully");
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("No goals found for employee with ID: " + empId);
            }
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error retrieving employee goals");
        }
        
        return response;
    }
    
    public ServiceResponse getAllEmployeeGoals() {
        ServiceResponse response = new ServiceResponse();
        
        try {
            List<EmployeeGoals> goals = employeeGoalRepository.findAll();
            List<EmployeeGoalDTO> dtoList = goals.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(dtoList);
            response.setServiceMessage("Employee goals retrieved successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error retrieving employee goals");
        }
        
        return response;
    }
    
    public ServiceResponse getEmployeeGoalById(Long goalId) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            Optional<EmployeeGoals> goalOpt = employeeGoalRepository.findById(goalId);
            
            if (goalOpt.isPresent()) {
                EmployeeGoalDTO dto = convertToDTO(goalOpt.get());
                
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dto);
                response.setServiceMessage("Employee goal retrieved successfully");
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Employee goal not found with ID: " + goalId);
            }
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error retrieving employee goal");
        }
        
        return response;
    }
    private EmployeeGoalDTO convertToDTO(EmployeeGoals entity) {
        EmployeeGoalDTO dto = new EmployeeGoalDTO();
        dto.setGoalId(entity.getGoalId());
        dto.setEmpId(entity.getEmpId());
        dto.setTemplateId(entity.getTemplateId());
        dto.setAssignedBy(entity.getAssignedBy());
        dto.setGoalTitle(entity.getGoalTitle());
        dto.setGoalProgress(entity.getGoalProgress());
        dto.setGoalStatus(entity.getGoalStatus());
        dto.setExpectedCompletionDate(entity.getExpectedCompletionDate());
        dto.setActualCompletionDate(entity.getActualCompletionDate());
        dto.setCreatedDate(entity.getCreatedDate());
        return dto;
    }
    
    private EmployeeGoals convertToEntity(EmployeeGoalDTO dto) {
        EmployeeGoals entity = new EmployeeGoals();
        entity.setGoalId(dto.getGoalId());
        entity.setEmpId(dto.getEmpId());
        entity.setTemplateId(dto.getTemplateId());
        entity.setAssignedBy(dto.getAssignedBy());
        entity.setGoalTitle(dto.getGoalTitle());
        entity.setGoalProgress(dto.getGoalProgress());
        entity.setGoalStatus(dto.getGoalStatus());
        entity.setExpectedCompletionDate(dto.getExpectedCompletionDate());
        entity.setActualCompletionDate(dto.getActualCompletionDate());
        entity.setCreatedDate(dto.getCreatedDate());
        return entity;
    }

}
