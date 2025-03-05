package com.apmosys.employeeportal.service;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.GoalTemplatesRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

import java.util.List; 
import java.util.Optional;
//import java.util.*;
import com.apmosys.employeeportal.dto.*;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.GoalTemplates;

@Service
public class GoalTemplateService{
	

    @Autowired
    private GoalTemplatesRepository goalTemplatesRepository;
    
    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
//    private UserService userService;
    
    public ServiceResponse getAllGoalTemplates() {
        ServiceResponse response = new ServiceResponse();
        try {
            List<GoalTemplates> goalTemplates = goalTemplatesRepository.findAll();
            List<GoalTemplatesDto> dtoList = goalTemplates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(dtoList);
            response.setServiceMessage("Goal Templates Retrieved Successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Retrieving Goal Templates");
        }
        return response;
    }


    public ServiceResponse getGoalTemplateById(Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            Optional<GoalTemplates> goalTemplate = goalTemplatesRepository.findById(id);
            if (goalTemplate.isPresent()) {
                GoalTemplatesDto dto = convertToDto(goalTemplate.get());
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dto);
                response.setServiceMessage("Goal Template Retrieved Successfully");
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Goal Template Not Found");
            }
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Retrieving Goal Template");
        }
        return response;
    }

  
    public ServiceResponse createGoalTemplate(GoalTemplatesDto goalTemplatesDto) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Validate department
            Department department = departmentRepository.findByDeptId(goalTemplatesDto.getDepartmentId());
            if (department == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Invalid Department ID");
                return response;
            }

            // Convert DTO to Entity
            GoalTemplates goalTemplate = convertToEntity(goalTemplatesDto);
            
            // Save goal template
            GoalTemplates savedGoalTemplate = goalTemplatesRepository.save(goalTemplate);
            
            // Convert back to DTO for response
            GoalTemplatesDto savedDto = convertToDto(savedGoalTemplate);
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(savedDto);
            response.setServiceMessage("Goal Template Created Successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Creating Goal Template");
        }
        return response;
    }



    public GoalTemplatesDto updateGoalTemplate(Long id, GoalTemplatesDto goalTemplatesDto) {
        Optional<GoalTemplates> existingGoalTemplate = goalTemplatesRepository.findById(id);
        
        if (existingGoalTemplate.isPresent()) {
            GoalTemplates goalTemplate = existingGoalTemplate.get();
            goalTemplate.setTitle(goalTemplatesDto.getTitle());
            goalTemplate.setQuarterId(goalTemplatesDto.getQuarterId());
            goalTemplate.setDescription(goalTemplatesDto.getDescription());
            goalTemplate.setDepartment(goalTemplatesDto.getDepartment());
            goalTemplate.setApprovedBy(goalTemplatesDto.getApprovedById());
            goalTemplate.setIsApproved(goalTemplatesDto.getIsApproved());
            
            GoalTemplates updatedGoalTemplate = goalTemplatesRepository.save(goalTemplate);
            return convertToDto(updatedGoalTemplate);
        }
        
        return null;
    }
    public ServiceResponse getGoalTemplatesByDepartmentId(Long departmentId) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Validate department
            Department department = departmentRepository.findByDeptId(departmentId);
            if (department == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Invalid Department ID");
                return response;
            }

            // Find goal templates by department ID
            List<GoalTemplates> goalTemplates = goalTemplatesRepository.findByDepartmentId(departmentId);
            List<GoalTemplatesDto> dtoList = goalTemplates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(dtoList);
            response.setServiceMessage("Goal Templates Retrieved Successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Retrieving Goal Templates");
        }
        return response;
    }

    public List<GoalTemplatesDto> getGoalTemplatesByCreatedBy(Long createdById) {
        List<GoalTemplates> goalTemplates = goalTemplatesRepository.findByCreatedBy(createdById);
        
        System.out.print("GoalTemplatesDto"+goalTemplates);
        return goalTemplates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    public List<GoalTemplatesDto> getGoalTemplatesByApprovalStatus(Boolean isApproved) {
        List<GoalTemplates> goalTemplates = goalTemplatesRepository.findByIsApproved(isApproved);
        return goalTemplates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    public GoalTemplatesDto convertToDto(GoalTemplates goalTemplate) {
    	
        GoalTemplatesDto dto = new GoalTemplatesDto();
        Department departmentDetails = departmentRepository.findByDeptId(goalTemplate.getDepartmentId());
        if(departmentDetails != null) {
        dto.setTitle(goalTemplate.getTitle());
        dto.setDescription(goalTemplate.getDescription());
        dto.setDepartmentId(departmentDetails.getDeptId());
        dto.setDepartment(departmentDetails.getName());
        dto.setCreatedById(departmentDetails.getHodId());}
        
        return dto;
    }
    public GoalTemplates convertToEntity(GoalTemplatesDto dto) {
        GoalTemplates entity = new GoalTemplates();
        Department departmentDetails = departmentRepository.findByDeptId(dto.getDepartmentId());
        if (departmentDetails != null) {
            entity.setTitle(dto.getTitle());
            entity.setDescription(dto.getDescription());
            entity.setDepartmentId(departmentDetails.getDeptId());
            entity.setDepartment(departmentDetails.getName());
            entity.setCreatedBy(departmentDetails.getHodId());
            entity.setQuarterId(dto.getQuarterId());
//            entity.setApprovedBy(dto.getApprovedById());
//            entity.setIsApproved(dto.getIsApproved());
        }
        
        return entity;
    }
    
    

}