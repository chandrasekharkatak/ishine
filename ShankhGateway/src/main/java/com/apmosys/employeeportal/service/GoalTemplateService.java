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
    
    public ServiceResponse getAllGoalTemplates() {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/getAllGoalTemplates");
        apiLogInfo.setFeatureName("getallgoaltemplate");
        apiLogInfo.setLogLevel("INFO");//constructor or builder design pattern
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("ALL GOAL TEMPLATES");

        try {
            List<GoalTemplates> goalTemplates = goalTemplatesRepository.findAll();
            List<GoalTemplatesDto> dtoList = goalTemplates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            //check for no goal templates //no content
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(goalTemplates);
            response.setServiceMessage("Goal Templates have been Retrieved Successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error in  Retrieving Goal Templates");
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
                response.setServiceMessage("Goal Template has not been  Found");
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
                response.setServiceMessage("Department ID not found");
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



    public GoalTemplatesDto updateGoalTemplate1(Long id, GoalTemplatesDto goalTemplatesDto) {
        Optional<GoalTemplates> existingGoalTemplate = goalTemplatesRepository.findById(id);
        
        if (existingGoalTemplate.isPresent()) {
            GoalTemplates goalTemplate = existingGoalTemplate.get();

                    if (goalTemplatesDto.getTitle() != null) {
                        goalTemplate.setTitle(goalTemplatesDto.getTitle());
                    }
                    
                    if (goalTemplatesDto.getDescription() != null) {
                        goalTemplate.setDescription(goalTemplatesDto.getDescription());
                    }
                    if (goalTemplatesDto.getDepartment() != null) {
                        goalTemplate.setDepartment(goalTemplatesDto.getDepartment());
                    }
                    
                    if (goalTemplatesDto.getDepartmentId() != null) {
                        goalTemplate.setDepartmentId(goalTemplatesDto.getDepartmentId());
                    }
                    
                    if (goalTemplatesDto.getApprovedById() != null) {
                        goalTemplate.setApprovedBy(goalTemplatesDto.getApprovedById());
                    }
                    
                    if (goalTemplatesDto.getIsApproved() != null) {
                        goalTemplate.setIsApproved(goalTemplatesDto.getIsApproved());
                    }
                    
                    GoalTemplates updatedGoalTemplate = goalTemplatesRepository.save(goalTemplate);
                    return convertToDto(updatedGoalTemplate);
                }
                
                // Consider throwing an exception instead of returning null
                return null;
                
    }
    public ServiceResponse getGoalTemplatesByDepartmentId(Long departmentId) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/getGoalTemplatesByDepartmentId");
        apiLogInfo.setFeatureName("getallgoaltemplateByDepartmentId");
        apiLogInfo.setLogLevel("INFO");//constructor or builder design pattern
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("ALL GOAL TEMPLATES BY DEPARTMENT");
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
    public ServiceResponse getGoalTemplatesByDepartmentName(String departmentName) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setApiUrl("/getGoalTemplatesByDepartmentName");
        apiLogInfo.setFeatureName("getallgoaltemplateByDepartmentName");
        apiLogInfo.setLogLevel("INFO");//constructor or builder design pattern
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("ALL GOAL TEMPLATES BY DEPARTMENT NAME");
        try {
            // Find department by name
            Department department = departmentRepository.findByName(departmentName);
            if (department == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Department Not Found: " + departmentName);
                return response;
            }

            // Find goal templates by department ID
            List<GoalTemplates> goalTemplates = goalTemplatesRepository.findByDepartmentId(department.getDeptId());
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
 // Update the existing updateGoalTemplate method to return ServiceResponse
    public ServiceResponse updateGoalTemplate(Long templateId, GoalTemplatesDto goalTemplatesDto) {
        ServiceResponse response = new ServiceResponse();
        try {
            Optional<GoalTemplates> existingGoalTemplate = goalTemplatesRepository.findById(templateId);
            
            if (existingGoalTemplate.isPresent()) {
                GoalTemplates goalTemplate = existingGoalTemplate.get();
                
                // Validate department if changed
                if (goalTemplatesDto.getDepartmentId() != null) {
                    Department department = departmentRepository.findByDeptId(goalTemplatesDto.getDepartmentId());
                    if (department == null) {
                        response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                        response.setServiceMessage("Invalid Department ID");
                        return response;
                    }
                    goalTemplate.setDepartmentId(department.getDeptId());
                    goalTemplate.setDepartment(department.getName());
                }
               
                if (goalTemplatesDto.getTitle() != null) {
                    goalTemplate.setTitle(goalTemplatesDto.getTitle());
                }
                if (goalTemplatesDto.getDescription() != null) {
                    goalTemplate.setDescription(goalTemplatesDto.getDescription());
                }
                if (goalTemplatesDto.getApprovedById() != null) {
                    goalTemplate.setApprovedBy(goalTemplatesDto.getApprovedById());
                }
                if (goalTemplatesDto.getIsApproved() != null) {
                    goalTemplate.setIsApproved(goalTemplatesDto.getIsApproved());
                }
                
                GoalTemplates updatedGoalTemplate = goalTemplatesRepository.save(goalTemplate);
                GoalTemplatesDto updatedDto = convertToDto(updatedGoalTemplate);
                
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(updatedDto);
                response.setServiceMessage("Goal Template Updated Successfully");
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Goal Template Not Found");
            }
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Updating Goal Template");
        }
        return response;
    }
    

    // Add a new deleteGoalTemplate method
    public ServiceResponse deleteGoalTemplate(Long templateId) {
        ServiceResponse response = new ServiceResponse();
        try {
            Optional<GoalTemplates> existingGoalTemplate = goalTemplatesRepository.findById(templateId);
            
            if (existingGoalTemplate.isPresent()) {
                goalTemplatesRepository.deleteById(templateId);
                
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceMessage("Goal Template Deleted Successfully");
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Goal Template Not Found");
            }
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Deleting Goal Template");
        }
        return response;
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
        }
        
        return entity;
    }
    
    

}