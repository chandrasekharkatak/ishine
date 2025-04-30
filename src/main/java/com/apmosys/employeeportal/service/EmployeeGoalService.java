package com.apmosys.employeeportal.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.EmployeeGoalDTO;
import com.apmosys.employeeportal.dto.GoalRemarksDTO;
import com.apmosys.employeeportal.model.EmployeeGoals;
import com.apmosys.employeeportal.model.GoalRemarks;
import com.apmosys.employeeportal.model.GoalTemplates;
import com.apmosys.employeeportal.repository.EmployeeGoalRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.GoalRemarksRepository;
import com.apmosys.employeeportal.repository.GoalTemplatesRepository;
import com.apmosys.employeeportal.repository.QuarterCycleRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

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
    
    @Autowired
    private GoalRemarksRepository goalRemarksRepository;
    
    public EmployeeGoalDTO assignGoalToEmployee(Long empId, Long templateId, LocalDate expectedCompletionDate, Long quarterId) {
        Long tid = employeeGoalRepository.findByTemplateId(templateId, empId);
        
        if(templateId != tid || tid == null) {
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
            
            Integer iniprogress = 0;
            
            employeeGoalDTO.setEmpId(empId);
            employeeGoalDTO.setTemplateId(templateId);
            employeeGoalDTO.setAssignedBy(template.getCreatedBy());
            employeeGoalDTO.setDescription(template.getDescription());
            employeeGoalDTO.setGoalTitle(template.getTitle());
            employeeGoalDTO.setGoalProgress(iniprogress); // Default status
            employeeGoalDTO.setGoalStatus("Pending"); // Default review status
            employeeGoalDTO.setExpectedCompletionDate(expectedCompletionDate.toString());
            employeeGoalDTO.setCreatedDate(LocalDate.now().toString());
            employeeGoalDTO.setQuarterId(quarterId);

            EmployeeGoals savedGoal = employeeGoalRepository.save(convertToEntity(employeeGoalDTO));
            return convertToDTO(savedGoal);
        } else {
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
                
                Integer iniprogress = 0;
                employeeGoalDTO.setEmpId(empId);
                employeeGoalDTO.setTemplateId(templateId);
                employeeGoalDTO.setAssignedBy(template.getCreatedBy());
                employeeGoalDTO.setDescription(template.getDescription());
                employeeGoalDTO.setGoalTitle(template.getTitle());
                employeeGoalDTO.setGoalProgress(iniprogress);
                employeeGoalDTO.setGoalStatus("Pending");
                employeeGoalDTO.setExpectedCompletionDate(expectedCompletionDate.toString());
                employeeGoalDTO.setCreatedDate(LocalDate.now().toString());
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
                        .map(this::convertToDTOWithLatestRemark)
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
                    .map(this::convertToDTOWithLatestRemark)
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
                EmployeeGoalDTO dto = convertToDTOWithLatestRemark(goalOpt.get());
                
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
    
    public ServiceResponse bulkAssignGoals(List<Long> empIds, Long templateId, LocalDate expectedCompletionDate, Long quarterId) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            List<EmployeeGoalDTO> assignedGoals = assignGoalToMultipleEmployees(empIds, templateId, expectedCompletionDate, quarterId);
            
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
    
    public ServiceResponse getEmployeeGoalsByGoalId(Long empId, Long quarterId) {
        ServiceResponse response = new ServiceResponse();
        try {
            // First try using the direct repository method
            List<EmployeeGoals> goals = employeeGoalRepository.findByEmpIdAndQuarterId(empId, quarterId);
            
            if (goals != null && !goals.isEmpty()) {
                List<EmployeeGoalDTO> dtoList = goals.stream()
                        .map(this::convertToDTOWithLatestRemark)
                        .collect(Collectors.toList());
                
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(dtoList);
                response.setServiceMessage("Employee Goals Retrieved Successfully");
            } 
            // If no results or method doesn't exist, fallback to custom query
            else {
                List<Object[]> results = employeeRepository.findEmployeeGoalsByEmpIdAndQuarterId(empId, quarterId);
                
                if (results != null && !results.isEmpty()) {
                    List<EmployeeGoalDTO> dtoList = new ArrayList<>();
                    for (Object[] row : results) {
                        EmployeeGoalDTO dto = convertRowToDTO(row);
                        
                        // Add latest remark if available
                        if (dto.getGoalId() != null) {
                            Optional<EmployeeGoals> goalOpt = employeeGoalRepository.findById(dto.getGoalId());
                            if (goalOpt.isPresent()) {
                                EmployeeGoals goal = goalOpt.get();
                                if (goal.getRemarks() != null && !goal.getRemarks().isEmpty()) {
                                    // Sort remarks by creation date in descending order
                                    List<GoalRemarks> sortedRemarks = goal.getRemarks().stream()
                                        .sorted(Comparator.comparing(GoalRemarks::getCreatedDate).reversed())
                                        .collect(Collectors.toList());
                                    
                                    if (!sortedRemarks.isEmpty()) {
                                        GoalRemarks latestRemark = sortedRemarks.get(0);
                                        // Update to use the correct field names
                                        dto.setManagerRemark(latestRemark.getRemarkText());
                                        // Populate remarks list
                                        List<GoalRemarksDTO> remarkDTOs = new ArrayList<>();
                                        GoalRemarksDTO remarkDTO = new GoalRemarksDTO();
                                        remarkDTO.setRemarkBy(latestRemark.getRemarkBy());
                                        remarkDTO.setRemarkByName(latestRemark.getRemarkByName());
                                        remarkDTO.setRemarkText(latestRemark.getRemarkText());
                                        remarkDTO.setDate(latestRemark.getCreatedDate().toString());
                                        remarkDTOs.add(remarkDTO);
                                        dto.setRemarks(remarkDTOs);
                                    }
                                }
                            }
                        }
                        
                        dtoList.add(dto);
                    }
                    
                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                    response.setServiceResponse(dtoList);
                    response.setServiceMessage("Employee Goals Retrieved Successfully");
                } else {
                    response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                    response.setServiceResponse(new ArrayList<>());
                    response.setServiceMessage("No Employee Goals Found For The Specified Quarter");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error Retrieving Employee Goals");
        }
        return response;
    }
    
    public ServiceResponse updateGoalWithRemarks(EmployeeGoalDTO employeeGoalDto, Long id, Long empId) {
        ServiceResponse response = new ServiceResponse();
        try {
            Optional<EmployeeGoals> existingOpt = employeeGoalRepository.findById(id);
            
            if(existingOpt.isPresent()) {
                EmployeeGoals existing = existingOpt.get();
                Integer progress = employeeGoalDto.getGoalProgress();
                
                // Update the goal progress and status in the main table
                if(progress != null) {
                    existing.setGoalProgress(progress.longValue());
                    if(progress == 100) {
                        existing.setGoalStatus("Completed");
                        existing.setActualCompletionDate(LocalDate.now());
                    } else {
                        existing.setGoalStatus("Pending");
                    }
                }
                
                // Update other fields if provided
                if(employeeGoalDto.getGoalTitle() != null) {
                    existing.setGoalTitle(employeeGoalDto.getGoalTitle());
                }
                
                if(employeeGoalDto.getDescription() != null) {
                    existing.setDescription(employeeGoalDto.getDescription());
                }
                
                if(employeeGoalDto.getExpectedCompletionDate() != null) {
                    existing.setExpectedCompletionDate(LocalDate.parse(employeeGoalDto.getExpectedCompletionDate()));
                }
                
                // Create a new entry in the remarks collection
                if(employeeGoalDto.getRemarks() != null && !employeeGoalDto.getRemarks().isEmpty()) {
                    // Get the latest remark from the provided array
                    GoalRemarksDTO latestRemark = employeeGoalDto.getRemarks().get(employeeGoalDto.getRemarks().size() - 1);
                    
                    GoalRemarks remark = new GoalRemarks();
                    remark.setEmployeeGoal(existing);
                    remark.setRemarkBy(latestRemark.getRemarkBy());
                    remark.setRemarkByName(latestRemark.getRemarkByName());
                    remark.setRemarkText(latestRemark.getRemarkText());
                    remark.setEmpId(empId);
                    remark.setCreatedDate(LocalDate.now());
                    
                   
                    existing.getRemarks().add(remark);
                }
                
                else if(employeeGoalDto.getManagerRemark() != null && !employeeGoalDto.getManagerRemark().isEmpty()) {
                    GoalRemarks remark = new GoalRemarks();
                    remark.setEmployeeGoal(existing);
                 
                    remark.setRemarkBy(empId); 
                    remark.setRemarkText(employeeGoalDto.getManagerRemark());
                    remark.setEmpId(empId);
                    remark.setCreatedDate(LocalDate.now());
                    
        
                    existing.getRemarks().add(remark);
                }
                
             
                EmployeeGoals updatedGoal = employeeGoalRepository.save(existing);
           
                EmployeeGoalDTO updatedDto = convertToDTO(updatedGoal);
                
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(updatedDto);
                response.setServiceMessage("Employee goal and remarks updated successfully");
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Employee goal not found with ID: " + id);
            }
        } catch(Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error updating goal and remarks");
        }
        return response;
    }

    // Method to convert entity to DTO including all remarks
    private EmployeeGoalDTO convertToDTO(EmployeeGoals goal) {
        EmployeeGoalDTO dto = new EmployeeGoalDTO();
        dto.setGoalId(goal.getGoalId());
        dto.setGoalTitle(goal.getGoalTitle());
        dto.setDescription(goal.getDescription());
        dto.setGoalStatus(goal.getGoalStatus());
        dto.setGoalProgress(goal.getGoalProgress().intValue());
        dto.setAssignedBy(goal.getAssignedBy());
        dto.setQuarter(goal.getQuarter());
        
        if(goal.getExpectedCompletionDate() != null) {
            dto.setExpectedCompletionDate(goal.getExpectedCompletionDate().toString());
        }
        
        if(goal.getCreatedDate() != null) {
            dto.setCreatedDate(goal.getCreatedDate().toString());
        }
        
        // Convert all remarks
        List<GoalRemarksDTO> remarksList = new ArrayList<>();
        if(goal.getRemarks() != null && !goal.getRemarks().isEmpty()) {
            for(GoalRemarks remark : goal.getRemarks()) {
                GoalRemarksDTO remarkDTO = new GoalRemarksDTO();
                remarkDTO.setId(remark.getId());
                remarkDTO.setRemarkBy(remark.getRemarkBy());
                remarkDTO.setRemarkByName(remark.getRemarkByName());
                remarkDTO.setRemarkText(remark.getRemarkText());
                
                if(remark.getCreatedDate() != null) {
                    remarkDTO.setDate(remark.getCreatedDate().toString());
                }
                
                remarksList.add(remarkDTO);
            }
        }
        
        dto.setRemarks(remarksList);
        
        // Set the latest remark in managerRemark for backward compatibility
        if(!remarksList.isEmpty()) {
            GoalRemarksDTO latestRemark = remarksList.get(remarksList.size() - 1);
            dto.setManagerRemark(latestRemark.getRemarkText());
        }
        
        return dto;
    }
    
    /**
     * Get all remarks history for a specific goal
     */
    public ServiceResponse getGoalRemarksHistory(Long goalId) {
        ServiceResponse response = new ServiceResponse();
        
        try {
            Optional<EmployeeGoals> goalOpt = employeeGoalRepository.findById(goalId);
            
            if (goalOpt.isPresent()) {
                EmployeeGoals goal = goalOpt.get();
                List<GoalRemarksDTO> remarks = goal.getRemarks().stream()
                    .sorted(Comparator.comparing(GoalRemarks::getCreatedDate).reversed())
                    .map(this::convertRemarksToDTO)
                    .collect(Collectors.toList());
                
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse(remarks);
                response.setServiceMessage("Goal remarks history retrieved successfully");
            } else {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceMessage("Employee goal not found with ID: " + goalId);
            }
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setServiceMessage("Error retrieving goal remarks history");
        }
        
        return response;
    }
    
    /**
     * Old update methods kept for backward compatibility
     */
    public ServiceResponse updatestatusofGoal(EmployeeGoalDTO employeeGoalDto, Long id, Long empId) {
        // Redirect to the new method
        return updateGoalWithRemarks(employeeGoalDto, id, empId);
    }
    
    public ServiceResponse updateGoalWithNewEntry(EmployeeGoalDTO employeeGoalDto, Long id, Long empId) {
        // Redirect to the new method
        return updateGoalWithRemarks(employeeGoalDto, id, empId);
    }
    
    private EmployeeGoalDTO convertToDTOWithLatestRemark(EmployeeGoals entity) {
        EmployeeGoalDTO dto = new EmployeeGoalDTO();
        dto.setGoalId(entity.getGoalId());
        dto.setEmpId(entity.getEmpId());
        dto.setGoalTitle(entity.getGoalTitle());
        dto.setDescription(entity.getDescription());
        dto.setGoalStatus(entity.getGoalStatus());
        dto.setGoalProgress(entity.getGoalProgress().intValue());
        dto.setAssignedBy(entity.getAssignedBy());
        dto.setQuarter(entity.getQuarter());
        dto.setTemplateId(entity.getTemplateId());
        dto.setQuarterId(entity.getQuarterId());
        
        if(entity.getExpectedCompletionDate() != null) {
            dto.setExpectedCompletionDate(entity.getExpectedCompletionDate().toString());
        }
        
        if(entity.getCreatedDate() != null) {
            dto.setCreatedDate(entity.getCreatedDate().toString());
        }
        
        // Add latest remark data if available
        if (entity.getRemarks() != null && !entity.getRemarks().isEmpty()) {
            // Find the most recent remark
            GoalRemarks latestRemark = entity.getRemarks().stream()
                .sorted(Comparator.comparing(GoalRemarks::getCreatedDate).reversed())
                .findFirst()
                .orElse(null);
            
            if (latestRemark != null) {
                dto.setManagerRemark(latestRemark.getRemarkText());
                
                // Also include in remarks list
                List<GoalRemarksDTO> remarksList = new ArrayList<>();
                GoalRemarksDTO remarkDTO = new GoalRemarksDTO();
                remarkDTO.setId(latestRemark.getId());
                remarkDTO.setRemarkBy(latestRemark.getRemarkBy());
                remarkDTO.setRemarkByName(latestRemark.getRemarkByName());
                remarkDTO.setRemarkText(latestRemark.getRemarkText());
                
                if(latestRemark.getCreatedDate() != null) {
                    remarkDTO.setDate(latestRemark.getCreatedDate().toString());
                }
                
                remarksList.add(remarkDTO);
                dto.setRemarks(remarksList);
            }
        }
        
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
        
        // Convert Integer to Long for goalProgress
        if (dto.getGoalProgress() != null) {
            entity.setGoalProgress(dto.getGoalProgress().longValue());
        }
        
        entity.setGoalStatus(dto.getGoalStatus());
        
        // Parse LocalDate from String if not null
        if (dto.getExpectedCompletionDate() != null) {
            entity.setExpectedCompletionDate(LocalDate.parse(dto.getExpectedCompletionDate()));
        }
        
//        // Handle actual completion date if present
//        if (dto.getActualCompletionDate() != null) {
//            entity.setActualCompletionDate(LocalDate.parse(dto.getActualCompletionDate()));
//        }
        
        // Parse created date from String if not null
        if (dto.getCreatedDate() != null) {
            entity.setCreatedDate(LocalDate.parse(dto.getCreatedDate()));
        } else {
            entity.setCreatedDate(LocalDate.now());
        }
        
        entity.setQuarter(dto.getQuarter());
        entity.setQuarterId(dto.getQuarterId());
        
        // Initialize remarks list
        entity.setRemarks(new ArrayList<>());
        
        return entity;
    }
    
    private GoalRemarksDTO convertRemarksToDTO(GoalRemarks entity) {
        GoalRemarksDTO dto = new GoalRemarksDTO();
        dto.setId(entity.getId());
        dto.setRemarkBy(entity.getRemarkBy());
        dto.setRemarkByName(entity.getRemarkByName());
        dto.setRemarkText(entity.getRemarkText());
        
        if (entity.getCreatedDate() != null) {
            dto.setDate(entity.getCreatedDate().toString());
        }
        
        return dto;
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
            if (row[7] != null) dto.setGoalProgress(Integer.parseInt(row[7].toString()));
            if (row[8] != null) dto.setGoalStatus(row[8].toString());
            if (row[9] != null) dto.setGoalTitle(row[9].toString());
            if (row[5] != null) dto.setAssignedBy((Long) row[5]);
            if (row[4] != null) dto.setDescription(row[4].toString());
            if (row[12] != null) dto.setQuarter(row[12].toString());
            if (row[13] != null) dto.setQuarterId(Long.parseLong(row[13].toString()));
        } catch (Exception e) {
            throw new RuntimeException("Error converting database row to DTO: " + e.getMessage());
        }
        
        return dto;
    }
    
    private String parseDate(Object dateObj) {
        if (dateObj == null) return null;
        
        if (dateObj instanceof LocalDate) {
            return dateObj.toString();
        } else if (dateObj instanceof java.sql.Date) {
            return ((java.sql.Date) dateObj).toLocalDate().toString();
        } else if (dateObj instanceof String) {
            return (String) dateObj;
        }
        
        return null;
    }
}