package com.apmosys.employeeportal.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.model.StatusMasterNew;
import com.apmosys.employeeportal.service.StatusMasterNewService;
import com.apmosys.employeeportal.utility.ServiceResponse;

/**
 * REST Controller for StatusMasterNew CRUD operations
 * 
 * @author System
 * @version 1.0
 */
@RestController
@RequestMapping(path = "/api/v2/master/status")
public class StatusMasterNewController {

    @Autowired
    private StatusMasterNewService statusMasterNewService;

    /**
     * Get all active statuses
     * GET /api/v2/master/status
     */
    @JobRoleAccess(featureIds = {15, 16})
    @GetMapping
    public ServiceResponse getAllActive() {
        ServiceResponse response = new ServiceResponse();
        try {
            List<StatusMasterNew> statuses = statusMasterNewService.getAllActive();
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
     * Get status by ID
     * GET /api/v2/master/status/{id}
     */
    @JobRoleAccess(featureIds = {15, 16})
    @GetMapping("/{id}")
    public ServiceResponse getById(@PathVariable Integer id) {
        ServiceResponse response = new ServiceResponse();
        try {
            StatusMasterNew status = statusMasterNewService.getById(id);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(status);
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Status not found");
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    /**
     * Create new status
     * POST /api/v2/master/status
     */
    @JobRoleAccess(featureIds = {15})
    @PostMapping
    public ServiceResponse create(@RequestBody StatusMasterNew entity) {
        ServiceResponse response = new ServiceResponse();
        try {
            StatusMasterNew created = statusMasterNewService.create(entity);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(created);
            response.setServiceMessage("Status created successfully");
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
     * Update existing status
     * PUT /api/v2/master/status/{id}
     */
    @JobRoleAccess(featureIds = {15})
    @PutMapping("/{id}")
    public ServiceResponse update(@PathVariable Integer id, @RequestBody StatusMasterNew entity) {
        ServiceResponse response = new ServiceResponse();
        try {
            StatusMasterNew updated = statusMasterNewService.update(id, entity);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(updated);
            response.setServiceMessage("Status updated successfully");
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
     * Deactivate status
     * DELETE /api/v2/master/status/{id}
     */
    @JobRoleAccess(featureIds = {15})
    @DeleteMapping("/{id}")
    public ServiceResponse deactivate(@PathVariable Integer id) {
        ServiceResponse response = new ServiceResponse();
        try {
            statusMasterNewService.deactivate(id);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Status deactivated successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Status not found");
            response.setServiceError(e.getMessage());
        }
        return response;
    }
}

