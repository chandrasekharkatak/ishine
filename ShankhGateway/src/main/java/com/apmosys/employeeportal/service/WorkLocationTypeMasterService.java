package com.apmosys.employeeportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.model.WorkLocationTypeMaster;
import com.apmosys.employeeportal.repository.WorkLocationTypeMasterRepository;

/**
 * Service for WorkLocationTypeMaster CRUD operations
 * 
 * @author System
 * @version 1.0
 */
@Service
public class WorkLocationTypeMasterService
        extends AbstractMasterService<WorkLocationTypeMaster, WorkLocationTypeMasterRepository> {

    @Autowired
    private WorkLocationTypeMasterRepository repository;

    @Override
    protected WorkLocationTypeMasterRepository getRepository() {
        return repository;
    }

    @Override
    protected void validateForCreate(WorkLocationTypeMaster entity) {
        if (entity.getCode() == null || entity.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Work location type code is mandatory");
        }
        if (entity.getDescription() == null || entity.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Work location type description is mandatory");
        }
        if (entity.getRequiresLocationMaster() == null) {
            throw new IllegalArgumentException("Requires location master flag is mandatory");
        }
    }

    @Override
    protected void validateForUpdate(WorkLocationTypeMaster entity) {
        if (entity.getCode() == null || entity.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Work location type code is mandatory");
        }
        if (entity.getDescription() == null || entity.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Work location type description is mandatory");
        }
        if (entity.getRequiresLocationMaster() == null) {
            throw new IllegalArgumentException("Requires location master flag is mandatory");
        }
    }
}

