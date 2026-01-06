package com.apmosys.employeeportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.model.ClientStatusMasterNew;
import com.apmosys.employeeportal.repository.ClientStatusMasterNewRepository;

/**
 * Service for ClientStatusMasterNew CRUD operations
 * 
 * @author System
 * @version 1.0
 */
@Service
public class ClientStatusMasterNewService
        extends AbstractMasterService<ClientStatusMasterNew, ClientStatusMasterNewRepository> {

    @Autowired
    private ClientStatusMasterNewRepository repository;

    @Override
    protected ClientStatusMasterNewRepository getRepository() {
        return repository;
    }

    @Override
    protected void validateForCreate(ClientStatusMasterNew entity) {
        if (entity.getStatus() == null || entity.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Client status is mandatory");
        }
    }

    @Override
    protected void validateForUpdate(ClientStatusMasterNew entity) {
        if (entity.getStatus() == null || entity.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Client status is mandatory");
        }
    }
}

