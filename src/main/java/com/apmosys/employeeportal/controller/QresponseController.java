package com.apmosys.employeeportal.controller;

import com.apmosys.employeeportal.dto.QresponseDTO;
import com.apmosys.employeeportal.model.Qresponse;
import com.apmosys.employeeportal.service.QresponseService;
import com.apmosys.employeeportal.utility.ServiceResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qresponses")
public class QresponseController {
    @Autowired
    private QresponseService qresponseService;
    
    @PostMapping("/save/{empId}/quarter/{quarterId}")
    public ServiceResponse saveResponses(@RequestBody List<QresponseDTO> responses, @PathVariable Long empId, @PathVariable Long quarterId) {
        ServiceResponse response = new ServiceResponse();
        try {
            boolean flag = qresponseService.saveResponses(responses, empId, quarterId);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("Responses saved successfully!");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceError(e.getMessage());
            response.setErrorStackTrace(e.toString());
        }
        return response;
    }
    
    @GetMapping("/show/{empId}/{quarterId}")
    public List<Qresponse> showResponse(@PathVariable Long empId, @PathVariable Long quarterId) {
        return qresponseService.showResponse(empId, quarterId);
    }
}