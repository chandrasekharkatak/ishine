package com.apmosys.employeeportal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.model.ClientStatusMasterNew;
import com.apmosys.employeeportal.service.ClientStatusMasterNewService;
import com.apmosys.employeeportal.utility.ServiceResponse;

/**
 * REST Controller for ClientStatusMasterNew CRUD operations
 * 
 * @author System
 * @version 1.0
 */
@RestController
@RequestMapping(path = "/api/v2/master/client-status")
public class ClientStatusMasterNewController {

    @Autowired
    private ClientStatusMasterNewService clientStatusMasterNewService;

    /**
     * Get all active client statuses
     * GET /api/v2/master/client-status
     */
    @JobRoleAccess(featureIds = {15, 16})
    @GetMapping("/getAllActiveStatusForClient")
    public ServiceResponse getAllActiveStatusForClient() {
        ServiceResponse response = new ServiceResponse();
        try {
            List<ClientStatusMasterNew> statuses = clientStatusMasterNewService.getAllActive();
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(statuses);
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    /**
     * Get client status by ID
     * GET /api/v2/master/client-status/{id}
     */
    @JobRoleAccess(featureIds = {15, 16})
    @GetMapping("/id/{id}")
    public ServiceResponse getById(@PathVariable Integer id) {
        ServiceResponse response = new ServiceResponse();
        try {
            ClientStatusMasterNew status = clientStatusMasterNewService.getById(id);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(status);
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Client status not found");
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    /**
     * Create new client status
     * POST /api/v2/master/client-status
     */
    @JobRoleAccess(featureIds = {15})
    @PostMapping
    public ServiceResponse create(@RequestBody ClientStatusMasterNew entity) {
        ServiceResponse response = new ServiceResponse();
        try {
            ClientStatusMasterNew created = clientStatusMasterNewService.create(entity);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(created);
            response.setServiceMessage("Client status created successfully");
        } catch (IllegalArgumentException e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Validation failed: " + e.getMessage());
            response.setServiceError(e.getMessage());
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    /**
     * Update existing client status
     * PUT /api/v2/master/client-status/{id}
     */
    @JobRoleAccess(featureIds = {15})
    @PutMapping("/{id}")
    public ServiceResponse update(@PathVariable Integer id, @RequestBody ClientStatusMasterNew entity) {
        ServiceResponse response = new ServiceResponse();
        try {
            ClientStatusMasterNew updated = clientStatusMasterNewService.update(id, entity);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(updated);
            response.setServiceMessage("Client status updated successfully");
        } catch (IllegalArgumentException e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Validation failed: " + e.getMessage());
            response.setServiceError(e.getMessage());
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    /**
     * Deactivate client status
     * DELETE /api/v2/master/client-status/{id}
     */
    @JobRoleAccess(featureIds = {15})
    @DeleteMapping("/{id}")
    public ServiceResponse deactivate(@PathVariable Integer id) {
        ServiceResponse response = new ServiceResponse();
        try {
            clientStatusMasterNewService.deactivate(id);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Client status deactivated successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Client status not found");
            response.setServiceError(e.getMessage());
        }
        return response;
    }
}

