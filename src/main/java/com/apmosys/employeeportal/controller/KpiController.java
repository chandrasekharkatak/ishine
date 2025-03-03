package com.apmosys.employeeportal.controller;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.KpiDTO;
import com.apmosys.employeeportal.service.KpiService;
import com.apmosys.employeeportal.utility.ServiceResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityNotFoundException;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import java.util.List;


@RestController
@RequestMapping("/api/kpi")
public class KpiController {

    @Autowired
    KpiService kpiService;

   
    @PostMapping(value = "/createKpi")
    public ServiceResponse createKpi(@RequestBody KpiDTO kpiDTO) {
        ServiceResponse response = new ServiceResponse();
        try {
            KpiDTO createdKpi = kpiService.createKpi(kpiDTO);  
            response.setServiceResponse(createdKpi);         
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("KPI created successfully.");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }


   
    @GetMapping("/getAllKpis")
    public ServiceResponse getAllKpis() {
        ServiceResponse response = new ServiceResponse();
        try {
            List<KpiDTO> kpis = kpiService.getAllKpis();
            response.setServiceResponse(kpis);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("All KPIs retrieved successfully.");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    @GetMapping("/getKpiById/{id}")
    public ServiceResponse getKpiById(@PathVariable Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            KpiDTO kpiDTO = kpiService.getKpiById(id)
                                      .orElseThrow(() -> new EntityNotFoundException("KPI not found with ID: " + id));
            response.setServiceResponse(kpiDTO);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("KPI found with ID: " + id);
        } catch (EntityNotFoundException e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(e.getMessage());
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    
    @PutMapping("/updateKpi/{id}")
    public ServiceResponse updateKpi(@PathVariable Long id, @RequestBody KpiDTO updatedKpiDTO) {
        ServiceResponse response = new ServiceResponse();
        try {
            KpiDTO updatedKpi = kpiService.updateKpi(id, updatedKpiDTO);
            response.setServiceResponse(updatedKpi);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("KPI updated successfully.");
        } catch (EntityNotFoundException e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(e.getMessage());
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }


    
    @DeleteMapping("/deleteKpi/{id}")
    public ServiceResponse deleteKpi(@PathVariable Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            kpiService.deleteKpi(id);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("KPI with ID " + id + " deleted successfully.");
        } catch (EntityNotFoundException e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(e.getMessage());
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    
    @PutMapping("/{id}/reject")
    public ServiceResponse rejectKpi(@PathVariable Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            KpiDTO rejectedKpi = kpiService.rejectKpi(id);
            response.setServiceResponse(rejectedKpi);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("KPI rejected successfully.");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

   
    @PutMapping("/{id}/approve")
    public ServiceResponse approveKpi(@PathVariable Long id) {
        ServiceResponse response = new ServiceResponse();
        try {
            KpiDTO approvedKpi = kpiService.approveKpi(id);
            response.setServiceResponse(approvedKpi);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("KPI approved successfully.");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }
}

