package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.EmployeeGoalDTO;
import com.apmosys.employeeportal.model.EmployeeGoals;
import com.apmosys.employeeportal.repository.EmployeeGoalRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeGoalService {

    @Autowired
    private EmployeeGoalRepository employeeGoalRepository;

   
    private EmployeeGoalDTO convertToDTO(EmployeeGoals goal) {
        EmployeeGoalDTO dto = new EmployeeGoalDTO();
        
        dto.setGoalId(goal.getGoalId());
        dto.setEmpId(goal.getEmpId());
        dto.setAssignedBy(goal.getAssignedBy());
        dto.setGoalTitle(goal.getGoalTitle());
        dto.setGoalProgress(goal.getGoalProgress().name());
        dto.setReviewStatus(goal.getReviewStatus().name());
        dto.setExpectedCompletionDate(goal.getExpectedCompletionDate());
        dto.setActualCompletionDate(goal.getActualCompletionDate());
        dto.setCreatedDate(goal.getCreatedDate());
        dto.setUpdatedDate(goal.getUpdatedDate());
        
        return dto;
    }

        private EmployeeGoals convertToEntity(EmployeeGoalDTO dto) {
        EmployeeGoals goal = new EmployeeGoals();
        
        goal.setGoalId(dto.getGoalId());
        goal.setEmpId(dto.getEmpId());
        goal.setAssignedBy(dto.getAssignedBy());
        goal.setGoalTitle(dto.getGoalTitle());
        
                goal.setGoalProgress(EmployeeGoals.GoalProgress.valueOf(dto.getGoalProgress()));
        goal.setReviewStatus(EmployeeGoals.ReviewStatus.valueOf(dto.getReviewStatus()));
        
        goal.setExpectedCompletionDate(dto.getExpectedCompletionDate());
        goal.setActualCompletionDate(dto.getActualCompletionDate());
        
        return goal;
    }

        public ServiceResponse getAllEmployeeGoals() {
        ServiceResponse response = new ServiceResponse();
        
        try {
            List<EmployeeGoals> goals = employeeGoalRepository.findAll();
            
            if (goals.isEmpty()) {
            	
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceMessage("No employee goals found.");
                response.setServiceResponse(List.of());
            } else {
                List<EmployeeGoalDTO> goalDTOs = goals.stream()
                                                      .map(this::convertToDTO)
                                                      .collect(Collectors.toList());
                
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceMessage("Fetched all employee goals successfully.");
                response.setServiceResponse(goalDTOs);
            }
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        
        return response;
    }
        
        
    public ServiceResponse getEmployeeGoalById(Long id) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            Optional<EmployeeGoals> goal = employeeGoalRepository.findById(id);
            
            if (goal.isPresent()) {
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceMessage("Employee goal found.");
                response.setServiceResponse(convertToDTO(goal.get()));
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Goal not found for ID: " + id);
            }
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        
        return response;
    }

}
