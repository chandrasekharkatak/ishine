//package com.apmosys.employeeportal.service;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//
//import org.springframework.beans.factory.annotation.Autowired;
//
//import com.apmosys.employeeportal.dto.EmployeeGoalDTO;
//import com.apmosys.employeeportal.dto.QuestionnaireResponseDTO;
//import com.apmosys.employeeportal.model.EmployeeGoals;
//import com.apmosys.employeeportal.model.GoalTemplates;
//import com.apmosys.employeeportal.model.Questionnaire;
//import com.apmosys.employeeportal.repository.QuarterCycleRepository;
//import com.apmosys.employeeportal.repository.QuestionnaireRepository;
//import com.apmosys.employeeportal.utility.ServiceResponse;
//
//
//
//public class EmployeeQuestionnaireService {
//	
//	@Autowired
//	private QuestionnaireService questionnaireService;
//	
//	@Autowired
//	private QuestionnaireRepository  questionnaireRepository;
//
//	@Autowired
//	private QuarterCycleRepository quarterCycleRe
//    public QuestionnaireResponseDTO assignQuestionnaireToEmployee(Long empId, Long templateId, Long quarterId) {
//        Questionnaire response = questionnaireService.getQuestionnaireById(templateId);
////        if (!ServiceResponse.STATUS_SUCCESS.equals(response.getServiceStatus())) {
////            throw new RuntimeException("Goal template not found with ID: " + templateId);
////        }
//        
//        Optional<Questionnaire> vopt = questionnaireRepository.findById(templateId);
//        Questionnaire template = null;
//        if(vopt.isPresent()) template = vopt.get();
//        
//        // Query quarter information ONCE
//        List<Object[]> quarterIdList = quarterCycleRepository.findQuarterCycleById(quarterId);
//        
//        EmployeeGoalDTO employeeGoalDTO = new EmployeeGoalDTO();
//        
//        // Let's debug what's actually in the quarterIdList
//        System.out.println("Quarter data size: " + (quarterIdList != null ? quarterIdList.size() : "null"));
//        if (quarterIdList != null && !quarterIdList.isEmpty()) {
//            Object[] quarterData = quarterIdList.get(0);
//            System.out.println("Quarter data length: " + (quarterData != null ? quarterData.length : "null"));
//            if (quarterData != null) {
//                for (int i = 0; i < quarterData.length; i++) {
//                    System.out.println("Data at index " + i + ": " + (quarterData[i] != null ? quarterData[i].toString() : "null"));
//                }
//            }
//            
//            // Try to find the value that contains month format like "APR-JUN"
//            for (int i = 0; quarterData != null && i < quarterData.length; i++) {
//                if (quarterData[i] != null && quarterData[i].toString().matches("[A-Z]{3}-[A-Z]{3}")) {
//                    employeeGoalDTO.setQuarter(quarterData[i].toString());
//                    System.out.println("Found quarter format at index " + i + ": " + quarterData[i].toString());
//                    break;
//                }
//            }
//            
//            // Fallback if we didn't find a matching format
//            if (employeeGoalDTO.getQuarter() == null && quarterData != null && quarterData.length > 0) {
//                // Try index 0, 1, and 2 in that order
//                for (int i = 0; i < Math.min(3, quarterData.length); i++) {
//                    if (quarterData[i] != null) {
//                        employeeGoalDTO.setQuarter(quarterData[i].toString());
//                        System.out.println("Using fallback quarter at index " + i + ": " + quarterData[i].toString());
//                        break;
//                    }
//                }
//            }
//        }
//        
//        // If still null, set a default
//        if (employeeGoalDTO.getQuarter() == null) {
//            employeeGoalDTO.setQuarter("Unknown Quarter");
//            System.out.println("No quarter data found, using default");
//        }
//        
//        employeeGoalDTO.setEmpId(empId);
//        employeeGoalDTO.setTemplateId(templateId);
//        employeeGoalDTO.setAssignedBy(template.getCreatedBy());
//        employeeGoalDTO.setDescription(template.getDescription());
//        employeeGoalDTO.setGoalTitle(template.getTitle());
//        employeeGoalDTO.setGoalProgress(null); // Default status
//        employeeGoalDTO.setGoalStatus("Pending"); // Default review status
//        employeeGoalDTO.setExpectedCompletionDate(expectedCompletionDate);
//        employeeGoalDTO.setCreatedDate(LocalDate.now());
//        
//        // Convert DTO to entity, save, and convert back to DTO
//        EmployeeGoals savedGoal = employeeGoalRepository.save(convertToEntity(employeeGoalDTO));
//        return convertToDTO(savedGoal);
//    }
//
//}
