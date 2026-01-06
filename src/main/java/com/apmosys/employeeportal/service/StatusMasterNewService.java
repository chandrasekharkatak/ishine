package com.apmosys.employeeportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.model.StatusMasterNew;
import com.apmosys.employeeportal.repository.StatusMasterNewRepository;

/**
 * Service for StatusMasterNew CRUD operations
 * 
 * @author System
 * @version 1.0
 */
@Service
public class StatusMasterNewService
        extends AbstractMasterService<StatusMasterNew, StatusMasterNewRepository> {

    @Autowired
    private StatusMasterNewRepository repository;

    @Override
    protected StatusMasterNewRepository getRepository() {
        return repository;
    }

    @Override
    protected void validateForCreate(StatusMasterNew entity) {
        if (entity.getStatus() == null || entity.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Status is mandatory");
        }
    }

    @Override
    protected void validateForUpdate(StatusMasterNew entity) {
        if (entity.getStatus() == null || entity.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Status is mandatory");
        }
    }
}

