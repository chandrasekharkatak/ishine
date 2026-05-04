package com.apmosys.employeeportal.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.model.LeaveRejectionMaster;
import com.apmosys.employeeportal.repository.LeaveRejectionMasterRepository;

@Service
public class LeaveRejectionService {
     @Autowired
    private LeaveRejectionMasterRepository repository;

    public List<LeaveRejectionMaster> getActiveReasons() {
        return repository.findByIsActiveTrueOrderByRejectionReasonIdAsc();
    }
}
