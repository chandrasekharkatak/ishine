package com.apmosys.employeeportal.controller;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.EmployeeKpiMappingDTO;
import com.apmosys.employeeportal.dto.EmployeeKpisDTO;
import com.apmosys.employeeportal.dto.EmployeeKpiMappingDTO;
import com.apmosys.employeeportal.dto.KpiDTO;
import com.apmosys.employeeportal.dto.QuestionnaireDTO;
import com.apmosys.employeeportal.model.KpiResponse;
import com.apmosys.employeeportal.model.Questionnaire;
import com.apmosys.employeeportal.service.KpiResponseService;
import com.apmosys.employeeportal.service.KpiService;
import com.apmosys.employeeportal.utility.ServiceResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityNotFoundException;

import java.util.List;


@RestController
@RequestMapping("/api/kpi")
public class KpiController {

    @Autowired
    KpiService kpiService;
    
    @Autowired
    KpiResponseService kpiResponseService;
    
    @PostMapping(value = "/createKpiTemplate/quarter/{quarterId}/department/{departmentId}/employeeRole/{employeeRole}")
    public ServiceResponse createKpi(@RequestBody KpiDTO kpiDTO, @PathVariable Long quarterId, @PathVariable Long departmentId,@PathVariable String employeeRole) {
        ServiceResponse response = new ServiceResponse();
        try {
        	//remove path variable use dto 
            KpiDTO createdKpi = kpiService.createKpi(kpiDTO, quarterId, departmentId,employeeRole);  
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

    @GetMapping("/getKpisByQuarter/{quarterId}")
    public ServiceResponse getKpisByQuarter(@PathVariable Long quarterId) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<KpiDTO> kpis = kpiService.getKpisByQuarter(quarterId);
            response.setServiceResponse(kpis);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("KPIs for quarter ID " + quarterId + " retrieved successfully.");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/department/name/{departmentName}")
    public ServiceResponse getKpisByDepartmentName(@PathVariable String departmentName) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<KpiDTO> kpis = kpiService.getKpisByDepartmentName(departmentName);
            response.setServiceResponse(kpis);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("KPIs for department name '" + departmentName + "' retrieved successfully.");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/department/{departmentId}")
    public ServiceResponse getKpisByDepartmentId(@PathVariable Long departmentId) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<KpiDTO> kpis = kpiService.getKpisByDepartmentId(departmentId);
            response.setServiceResponse(kpis);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("KPIs for department ID " + departmentId + " retrieved successfully.");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/getKpisByQuarter/{quarterId}/Department/{departmentId}/EmployeeRole/{employeeRole}")
    public ServiceResponse getKpisByQuarterAndDepartment(@PathVariable Long quarterId, @PathVariable Long departmentId,@PathVariable String employeeRole) {
        ServiceResponse response = new ServiceResponse();
        try {
           
            List<KpiDTO> kpis = kpiService.getKpisByQuarterAndDepartment(quarterId, departmentId,employeeRole);
            response.setServiceResponse(kpis);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("KPIs for quarter ID " + quarterId + " and department '" + departmentId + "Employee Role" + employeeRole + "' retrieved successfully.");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }
    @GetMapping("/getKpisByDepartment/{departmentId}/EmployeeRole/{employeeRole}")
    public ServiceResponse getKpisByQuarterAndDepartment(@PathVariable Long departmentId,@PathVariable String employeeRole) {
        ServiceResponse response = new ServiceResponse();
        try {
           
            List<KpiDTO> kpis = kpiService.getKpisByDepartmentAndEmployeerole(departmentId,employeeRole);
            response.setServiceResponse(kpis);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("KPIs for department '" + departmentId + "Employee Role" + employeeRole + "' retrieved successfully.");
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
    
    @PostMapping("/assign/employeeId/quarterId")
    public ServiceResponse assignKpi(@RequestBody EmployeeKpiMappingDTO employeeKpiMappingDTO) {
        ServiceResponse response = new ServiceResponse();
        try {
            // Remove path variable use dto 
            EmployeeKpiMappingDTO assignKpi = kpiService.assignKpi(
                employeeKpiMappingDTO.getId(),
                employeeKpiMappingDTO.getEmpId(),
                employeeKpiMappingDTO.getQuarterId()
            );  
            response.setServiceResponse(assignKpi);         
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("KPI created successfully.");
        } catch (IllegalStateException e) {
            // Handle duplicate KRA/KPI assignment error
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(e.getMessage());
            response.setServiceError(e.getMessage());
        } catch (EntityNotFoundException e) {
            // Handle KPI template not found error
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(e.getMessage()); // "KPI template not found with ID: " + id
            response.setServiceError(e.getMessage());
        } catch (Exception e) {
            // Handle other unexpected errors
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/employee/{empId}/quarter/{quarterId}")
    public ServiceResponse getKpisByEmployeeIdAndQuarter(@PathVariable Long empId, @PathVariable Long quarterId) {
        ServiceResponse response = new ServiceResponse();
        try {
            EmployeeKpiMappingDTO employeeKpi = kpiService.getKpiByEmployeeIdAndQuarterId(empId, quarterId);
            response.setServiceResponse(employeeKpi);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("KPI for employee ID " + empId + " and quarter ID " + quarterId + " retrieved successfully.");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/employee/{empId}/quarter/{quarterId}/addkpiList")
    public ServiceResponse addKpiDetails(
            @PathVariable Long empId, 
            @PathVariable Long quarterId, 
            @RequestBody EmployeeKpisDTO kpiDetails) {
        
        ServiceResponse response = new ServiceResponse();
        try {
            EmployeeKpiMappingDTO updatedMapping = kpiService.addKpiToEmployeeMapping(empId, quarterId, kpiDetails);
            response.setServiceResponse(updatedMapping);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceMessage("KPI detail added successfully to employee ID " + empId + 
                    " for quarter ID " + quarterId);
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceMessage(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

}

