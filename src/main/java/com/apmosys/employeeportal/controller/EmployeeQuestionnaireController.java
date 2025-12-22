//package com.apmosys.employeeportal.controller;
//
//import java.time.LocalDate;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.apmosys.employeeportal.dto.EmployeeGoalDTO;
//import com.apmosys.employeeportal.dto.GoalAssignmentRequest;
//import com.apmosys.employeeportal.dto.QuestionnaireAssignRequestDTO;
//import com.apmosys.employeeportal.service.EmployeeQuestionnaireService;
//import com.apmosys.employeeportal.utility.ServiceResponse;
//
//@RestController
//@RequestMapping("/api/EmployeeQuestionnaire")
//public class EmployeeQuestionnaireController {
//
//	@Autowired
//	private EmployeeQuestionnaireService	employeeQuestionnaireService; 
//	
//    @PostMapping("/assign")
//    public ServiceResponse assignQuestionnaire(@RequestBody QuestionnaireAssignRequestDTO request) {
//        ServiceResponse response = new ServiceResponse();
//        try {
//            
//            EmployeeGoalDTO assignedGoal = employeeQuestionnaireService.assignQuestionnaireToEmployee(
//            		   request.getEmpId(),
//            		    request.getTemplateId(),
//            		    request.getQuarterId()
//            );
//
//            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//            response.setServiceResponse(assignedGoal);
//            response.setServiceMessage("Goal assigned successfully.");
//        } catch (Exception e) {
//            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
//            response.setServiceError(e.getMessage());
//            response.setServiceMessage("Error assigning goal.");
//        }
//        return response;
//    }
//	
//}
