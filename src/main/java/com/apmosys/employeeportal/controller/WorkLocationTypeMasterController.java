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
import com.apmosys.employeeportal.model.WorkLocationTypeMaster;
import com.apmosys.employeeportal.service.WorkLocationTypeMasterService;
import com.apmosys.employeeportal.utility.ServiceResponse;

/**
 * REST Controller for WorkLocationTypeMaster CRUD operations
 * 
 * @author System
 * @version 1.0
 */
@RestController
@RequestMapping(path = "/api/v2/master/work-location-type")
public class WorkLocationTypeMasterController {

    @Autowired
    private WorkLocationTypeMasterService workLocationTypeMasterService;

    /**
     * Get all active work location types
     * GET /api/v2/master/work-location-type
     */
    @JobRoleAccess(featureIds = {15, 16})
    @GetMapping("/getAllActiveLocationTypes")
    public ServiceResponse getAllActiveLocationTypes() {
        ServiceResponse response = new ServiceResponse();
        try {
            List<WorkLocationTypeMaster> locationTypes = workLocationTypeMasterService.getAllActive();
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(locationTypes);
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    /**
     * Get work location type by ID
     * GET /api/v2/master/work-location-type/{id}
     */
    @JobRoleAccess(featureIds = {15, 16})
    @GetMapping("/{id}")
    public ServiceResponse getById(@PathVariable Integer id) {
        ServiceResponse response = new ServiceResponse();
        try {
            WorkLocationTypeMaster locationType = workLocationTypeMasterService.getById(id);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(locationType);
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Work location type not found");
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    /**
     * Create new work location type
     * POST /api/v2/master/work-location-type
     */
    @JobRoleAccess(featureIds = {15})
    @PostMapping
    public ServiceResponse create(@RequestBody WorkLocationTypeMaster entity) {
        ServiceResponse response = new ServiceResponse();
        try {
            WorkLocationTypeMaster created = workLocationTypeMasterService.create(entity);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(created);
            response.setServiceMessage("Work location type created successfully");
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
     * Update existing work location type
     * PUT /api/v2/master/work-location-type/{id}
     */
    @JobRoleAccess(featureIds = {15})
    @PutMapping("/{id}")
    public ServiceResponse update(@PathVariable Integer id, @RequestBody WorkLocationTypeMaster entity) {
        ServiceResponse response = new ServiceResponse();
        try {
            WorkLocationTypeMaster updated = workLocationTypeMasterService.update(id, entity);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(updated);
            response.setServiceMessage("Work location type updated successfully");
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
     * Deactivate work location type
     * DELETE /api/v2/master/work-location-type/{id}
     */
    @JobRoleAccess(featureIds = {15})
    @DeleteMapping("/{id}")
    public ServiceResponse deactivate(@PathVariable Integer id) {
        ServiceResponse response = new ServiceResponse();
        try {
            workLocationTypeMasterService.deactivate(id);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Work location type deactivated successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse("Work location type not found");
            response.setServiceError(e.getMessage());
        }
        return response;
    }
}

