package com.apmosys.employeeportal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.model.LeaveRejectionMaster;
import com.apmosys.employeeportal.service.LeaveRejectionService;

@RestController
@RequestMapping(path = "/api")
public class LeaveRejectionController {
     @Autowired
    private LeaveRejectionService leaveRejectionService;

    @GetMapping("/leave-rejection-reasons")
    public List<LeaveRejectionMaster> getLeaveRejectionReasons() {
        return leaveRejectionService.getActiveReasons();
    }
    
}
