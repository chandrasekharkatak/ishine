package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.EmployeeGoalDTO;
import com.apmosys.employeeportal.model.EmployeeGoals;
import com.apmosys.employeeportal.model.GoalTemplates;
import com.apmosys.employeeportal.model.QuaterCycle;
import com.apmosys.employeeportal.repository.EmployeeGoalRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.GoalTemplatesRepository;
import com.apmosys.employeeportal.repository.QuarterCycleRepository;
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
    
	@Autowired
	private EmployeeRepository employeeRepository;
    
    @Autowired
    private QuarterCycleRepository quarterCycleRepository;
    
    public EmployeeGoalDTO assignGoalToEmployee(Long empId, Long templateId, LocalDate expectedCompletionDate, Long quarterId) {
        Long tid = employeeGoalRepository.findByTemplateId(templateId,empId);
        
        
        if(templateId != tid || tid == null ) {

        
        
        Optional<GoalTemplates> vopt = goalTemplateRepo.findById(templateId);
        GoalTemplates template = null;
        if(vopt.isPresent()) template = vopt.get();
     
        


        List<Object[]> quarterIdList = quarterCycleRepository.findQuarterCycleById(quarterId);
        
        EmployeeGoalDTO employeeGoalDTO = new EmployeeGoalDTO();
            
        if (quarterIdList != null && !quarterIdList.isEmpty()) {
            Object[] quarterData = quarterIdList.get(0);
    
            for (int i = 0; quarterData != null && i < quarterData.length; i++) {
                if (quarterData[i] != null && quarterData[i].toString().matches("[A-Z]{3}-[A-Z]{3}")) {
                    employeeGoalDTO.setQuarter(quarterData[i].toString());
                    
                    break;
                }
            }

            if (employeeGoalDTO.getQuarter() == null && quarterData != null && quarterData.length > 0) {

                for (int i = 0; i < Math.min(3, quarterData.length); i++) {
                    if (quarterData[i] != null) {
                        employeeGoalDTO.setQuarter(quarterData[i].toString());
                        break;
                    }
                }
            }
        }
        
    
        if (employeeGoalDTO.getQuarter() == null) {
            employeeGoalDTO.setQuarter("Unknown Quarter");
        }
        Long iniprogress = (long) 0;
        
        employeeGoalDTO.setEmpId(empId);
        employeeGoalDTO.setTemplateId(templateId);
        employeeGoalDTO.setAssignedBy(template.getCreatedBy());
        employeeGoalDTO.setDescription(template.getDescription());
        employeeGoalDTO.setGoalTitle(template.getTitle());
        employeeGoalDTO.setGoalProgress(iniprogress); // Default status
        employeeGoalDTO.setGoalStatus("Pending"); // Default review status
        employeeGoalDTO.setExpectedCompletionDate(expectedCompletionDate);
        employeeGoalDTO.setCreatedDate(LocalDate.now());
        employeeGoalDTO.setQuarterId(quarterId);
        


        EmployeeGoals savedGoal = employeeGoalRepository.save(convertToEntity(employeeGoalDTO));
        return convertToDTO(savedGoal);}
        else {
            
        	throw new RuntimeException("Goal Already assigned to the employee: " + templateId);
        
        }
    }
    
    public List<EmployeeGoalDTO> assignGoalToMultipleEmployees(List<Long> empIds, Long templateId, LocalDate expectedCompletionDate, Long quarterId) {
        List<EmployeeGoalDTO> assignedGoals = new ArrayList<>();
        
        for (Long empId : empIds) {
            Long tid = employeeGoalRepository.findByTemplateId(templateId, empId);
            
            if(templateId != tid || tid == null) {
                Optional<GoalTemplates> vopt = goalTemplateRepo.findById(templateId);
                if (!vopt.isPresent()) {
                    throw new RuntimeException("Goal template not found with ID: " + templateId);
                }
                GoalTemplates template = vopt.get();
                
                EmployeeGoalDTO employeeGoalDTO = new EmployeeGoalDTO();
                List<Object[]> quarterIdList = quarterCycleRepository.findQuarterCycleById(quarterId);
                
                if (quarterIdList != null && !quarterIdList.isEmpty()) {
                    Object[] quarterData = quarterIdList.get(0);
                    
                    for (int i = 0; quarterData != null && i < quarterData.length; i++) {
                        if (quarterData[i] != null && quarterData[i].toString().matches("[A-Z]{3}-[A-Z]{3}")) {
                            employeeGoalDTO.setQuarter(quarterData[i].toString());
                            break;
                        }
                    }
                    
                    if (employeeGoalDTO.getQuarter() == null && quarterData != null && quarterData.length > 0) {
                        for (int i = 0; i < Math.min(3, quarterData.length); i++) {
                            if (quarterData[i] != null) {
                                employeeGoalDTO.setQuarter(quarterData[i].toString());
                                break;
                            }
                        }
                    }
                }
                
                if (employeeGoalDTO.getQuarter() == null) {
                    employeeGoalDTO.setQuarter("Unknown Quarter");
                }
                
                Long iniprogress = (long) 0;
                employeeGoalDTO.setEmpId(empId);
                employeeGoalDTO.setTemplateId(templateId);
                employeeGoalDTO.setAssignedBy(template.getCreatedBy());
                employeeGoalDTO.setDescription(template.getDescription());
                employeeGoalDTO.setGoalTitle(template.getTitle());
                employeeGoalDTO.setGoalProgress(iniprogress);
                employeeGoalDTO.setGoalStatus("Pending");
                employeeGoalDTO.setExpectedCompletionDate(expectedCompletionDate);
                employeeGoalDTO.setCreatedDate(LocalDate.now());
                employeeGoalDTO.setQuarterId(quarterId);
                
                EmployeeGoals savedGoal = employeeGoalRepository.save(convertToEntity(employeeGoalDTO));
                assignedGoals.add(convertToDTO(savedGoal));
            } 
        }
        
        if (assignedGoals.isEmpty()) {
            throw new RuntimeException("No goals were assigned. Goals might already be assigned to the selected employees.");
        }
        
        return assignedGoals;
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
    
    public ServiceResponse bulkAssignGoals(List<Long> empIds, Long templateId, LocalDate expectedCompletionDate,Long quarterId) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            List<EmployeeGoalDTO> assignedGoals = assignGoalToMultipleEmployees(empIds, templateId, expectedCompletionDate,quarterId);
            
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(assignedGoals);
            response.setServiceMessage("Goals assigned successfully to " + assignedGoals.size() + " employees");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error assigning goals to employees");
        }
        
        return response;
    }
    
    public ServiceResponse getEmployeeGoalsByEmpIdAndQuarterId(Long empId, Long quarterId) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<Object[]> results = employeeRepository.findEmployeeGoalsByEmpIdAndQuarterId(empId, quarterId);
            
            
              if (results != null && !results.isEmpty()) {
                Object[] firstRow = results.get(0);
             
                List<EmployeeGoalDTO> dtoList = new ArrayList<>();
                for (Object[] row : results) {
                    EmployeeGoalDTO dto = convertRowToDTO(row);
                    dtoList.add(dto);
                }
                
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dtoList);
                response.setServiceMessage("Employee Goals Retrieved Successfully");
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(null);
                response.setServiceMessage("No Employee Goals Found");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Add this to see full stack trace
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Retrieving Employee Goals");
        }
        return response;
    }
    
    public ServiceResponse updatestatusofGoal(EmployeeGoalDTO employeeGoalDto, Long id, Long empId)
    {
    	ServiceResponse response = new ServiceResponse();
    	try {
    		Optional<EmployeeGoals> existing = employeeGoalRepository.findByGoalId(id);
    		if(existing.isPresent())
    		{
    			EmployeeGoals employeeGoal = existing.get();
    			String managerR = employeeGoalDto.getManagerRemark();
    			String employeeR = employeeGoalDto.getEmployeeRemark();
    			Long progress = employeeGoalDto.getGoalProgress();
    			
    			
    			if(managerR != null) {
    				employeeGoal.setManagerRemark(managerR);}
    			
    			if(employeeR != null) {
    				employeeGoal.setEmployeeRemark(employeeR);
    			}
//    			employeeGoal.setGoalProgress(progress);
    			if(progress != null) {
                    employeeGoal.setGoalProgress(progress);
                    if(progress == 100) {
                        employeeGoal.setGoalStatus("Completed");
                    } else {
                        employeeGoal.setGoalStatus("Pending");
                    }
                }
    			
    			EmployeeGoals updated  = employeeGoalRepository.save(employeeGoal);
    			EmployeeGoalDTO updatedDto = convertToDTO(updated);
    			
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(updatedDto);
                response.setServiceMessage("Employee Goal Updated Successfully");
    			}
    		else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Error in updating");
    		}
    		
    	}
    	catch(Exception e){
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Updating");
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
        dto.setQuarter(entity.getQuarter());
        dto.setManagerRemark(entity.getManagerRemark());
        dto.setEmployeeRemark(entity.getEmployeeRemark());
        dto.setQuarterId(entity.getQuarterId());
        return dto;
    }
    
    private EmployeeGoals convertToEntity(EmployeeGoalDTO dto) {
        EmployeeGoals entity = new EmployeeGoals();
        entity.setGoalId(dto.getGoalId());
        entity.setEmpId(dto.getEmpId());
        entity.setTemplateId(dto.getTemplateId());
        entity.setDescription(dto.getDescription());
        entity.setAssignedBy(dto.getAssignedBy());
        entity.setGoalTitle(dto.getGoalTitle());
        entity.setGoalProgress(dto.getGoalProgress());
        entity.setGoalStatus(dto.getGoalStatus());
        entity.setExpectedCompletionDate(dto.getExpectedCompletionDate());
        entity.setActualCompletionDate(dto.getActualCompletionDate());
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setQuarter(dto.getQuarter());
        entity.setEmployeeRemark(dto.getEmployeeRemark());
        entity.setManagerRemark(dto.getManagerRemark());
        entity.setQuarterId(dto.getQuarterId());
        return entity;
    }
    private EmployeeGoalDTO convertRowToDTO(Object[] row) {
        EmployeeGoalDTO dto = new EmployeeGoalDTO();
        
        try {
            if (row[0] != null) dto.setGoalId(((Number)row[0]).longValue());
            // row[1] seems to be null in the example
            if (row[2] != null) dto.setEmpId(((Number)row[2]).longValue());
            if (row[3] != null) dto.setExpectedCompletionDate(parseDate(row[3]));
            if (row[11] != null) dto.setTemplateId(((Number)row[11]).longValue());
            if (row[6] != null) dto.setCreatedDate(parseDate(row[6]));
            if (row[7] != null) dto.setGoalProgress(Long.parseLong(row[7].toString()));
            if (row[8] != null) dto.setGoalStatus(row[8].toString());
            if (row[9] != null) dto.setGoalTitle(row[9].toString());
//            if (row[10] != null) dto.setQuarter(row[10].toString());
            if (row[5] != null) dto.setAssignedBy(((Number)row[5]).longValue());
            if(row[4]!= null) dto.setDescription(row[4].toString());
            if(row[12]!= null) dto.setQuarter(row[12].toString());
            if(row[13]!= null) dto.setQuarterId(Long.parseLong(row[13].toString()));
            if(row[14]!= null) dto.setEmployeeRemark(row[14].toString());
            if(row[15]!= null) dto.setManagerRemark(row[15].toString());
            
                    } catch (Exception e) {
                        throw new RuntimeException("Error");

                   
                    }
        
        return dto;
    }
    private LocalDate parseDate(Object dateObj) {
        if (dateObj == null) return null;
        
        if (dateObj instanceof LocalDate) {
            return (LocalDate) dateObj;
        } else if (dateObj instanceof java.sql.Date) {
            return ((java.sql.Date) dateObj).toLocalDate();
        } else if (dateObj instanceof String) {
            return LocalDate.parse((String) dateObj);
        }
        
        return null;
        
    }
    

}
