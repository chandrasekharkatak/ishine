package com.apmosys.employeeportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.model.DayTypeMasterNew;
import com.apmosys.employeeportal.repository.DayTypeMasterNewRepository;

/**
 * Service for DayTypeMasterNew CRUD operations
 * 
 * @author System
 * @version 1.0
 */
@Service
public class DayTypeMasterNewService
        extends AbstractMasterService<DayTypeMasterNew, DayTypeMasterNewRepository> {

    @Autowired
    private DayTypeMasterNewRepository repository;

    @Override
    protected DayTypeMasterNewRepository getRepository() {
        return repository;
    }

    @Override
    protected void validateForCreate(DayTypeMasterNew entity) {
        if (entity.getDayType() == null || entity.getDayType().trim().isEmpty()) {
            throw new IllegalArgumentException("Day type is mandatory");
        }
    }

    @Override
    protected void validateForUpdate(DayTypeMasterNew entity) {
        if (entity.getDayType() == null || entity.getDayType().trim().isEmpty()) {
            throw new IllegalArgumentException("Day type is mandatory");
        }
    }
}

