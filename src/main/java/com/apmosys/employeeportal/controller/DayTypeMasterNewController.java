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
import com.apmosys.employeeportal.model.DayTypeMasterNew;
import com.apmosys.employeeportal.service.DayTypeMasterNewService;
import com.apmosys.employeeportal.utility.ServiceResponse;

/**
 * REST Controller for DayTypeMasterNew CRUD operations
 * 
 * @author System
 * @version 1.0
 */
@RestController
@RequestMapping(path = "/api/v2/master/day-type")
public class DayTypeMasterNewController {

    @Autowired
    private DayTypeMasterNewService dayTypeMasterNewService;

    /**
     * Get all active day types
     * GET /api/v2/master/day-type
     */
    @JobRoleAccess(featureIds = {15, 16})
    @GetMapping
    public ServiceResponse getAllActive() {
        ServiceResponse response = new ServiceResponse();
        try {
            List<DayTypeMasterNew> dayTypes = dayTypeMasterNewService.getAllActive();
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(dayTypes);
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    /**
     * Get day type by ID
     * GET /api/v2/master/day-type/{id}
     */
    @JobRoleAccess(featureIds = {15, 16})
    @GetMapping("/{id}")
    public ServiceResponse getById(@PathVariable Integer id) {
        ServiceResponse response = new ServiceResponse();
        try {
            DayTypeMasterNew dayType = dayTypeMasterNewService.getById(id);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(dayType);
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Day type not found");
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    /**
     * Create new day type
     * POST /api/v2/master/day-type
     */
    @JobRoleAccess(featureIds = {15})
    @PostMapping
    public ServiceResponse create(@RequestBody DayTypeMasterNew entity) {
        ServiceResponse response = new ServiceResponse();
        try {
            DayTypeMasterNew created = dayTypeMasterNewService.create(entity);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(created);
            response.setServiceMessage("Day type created successfully");
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
     * Update existing day type
     * PUT /api/v2/master/day-type/{id}
     */
    @JobRoleAccess(featureIds = {15})
    @PutMapping("/{id}")
    public ServiceResponse update(@PathVariable Integer id, @RequestBody DayTypeMasterNew entity) {
        ServiceResponse response = new ServiceResponse();
        try {
            DayTypeMasterNew updated = dayTypeMasterNewService.update(id, entity);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(updated);
            response.setServiceMessage("Day type updated successfully");
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
     * Deactivate day type
     * DELETE /api/v2/master/day-type/{id}
     */
    @JobRoleAccess(featureIds = {15})
    @DeleteMapping("/{id}")
    public ServiceResponse deactivate(@PathVariable Integer id) {
        ServiceResponse response = new ServiceResponse();
        try {
            dayTypeMasterNewService.deactivate(id);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Day type deactivated successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Day type not found");
            response.setServiceError(e.getMessage());
        }
        return response;
    }
}

