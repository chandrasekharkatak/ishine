package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.KpiResponseDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.KpiResponse;
import com.apmosys.employeeportal.model.UserSession;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.KpiResponseRepository;
import com.apmosys.employeeportal.repository.UserSessionRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

@Service
public class KpiResponseService {
    @Autowired
    private KpiResponseRepository repository;
    
    @Autowired
    private UserSessionRepository userSessionRepo;
    
    @Autowired
    private DepartmentRepository departmentRepo;
    
    @Autowired
    private EmployeeRepository employeeRepo;
    
    public KpiResponseDTO saveResponse(KpiResponseDTO dto) {

    
    	Object departmentDetails = departmentRepo.findbyEmpId();
    	Long employeeId = employeeRepo.findByEmpId();
    	if(departmentDetails!=null && employeeId!=null) {
    	  KpiResponse response = new KpiResponse();
        
    	  response.setDepartmentName(departmentDetails.toString());
          response.setEmpId(employeeId);
          response.setKpiId(dto.getKpiId());
          response.setResponse(dto.getResponse());
          response.setRemarks(dto.getRemarks());
          response.setScore(dto.getScore());
          response.setApprovedBy(dto.getApprovedBy());
          response.setCreatedAt(LocalDateTime.now());
         
//          response.setDepartmentId(dto.getDepartmentId());
          response.setQuarterId(dto.getQuarterId());
          
          response = repository.save(response);
          dto.setResponseId(response.getResponseId());
          dto.setEmpId(response.getEmpId());
          dto.setDepartmentName(response.getDepartmentName());
          return dto;
    	}else {
    		return null;
    	}
        
    }
    public List<KpiResponseDTO> getResponsesByEmpId(Long empId) {
        return repository.findByEmpId(empId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<KpiResponseDTO> getResponsesByKpiId(Long kpiId) {
        return repository.findByKpiId(kpiId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<KpiResponseDTO> getResponsesByQuarterId(Long quarterId) {
        return repository.findByQuarterId(quarterId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public void deleteResponse(Long responseId) {
        repository.deleteById(responseId);
    }

    private KpiResponseDTO convertToDTO(KpiResponse response) {
        KpiResponseDTO dto = new KpiResponseDTO();
        dto.setResponseId(response.getResponseId());
        dto.setEmpId(response.getEmpId());
        dto.setKpiId(response.getKpiId());
        dto.setResponse(response.getResponse());
        dto.setRemarks(response.getRemarks());
        dto.setScore(response.getScore());
        dto.setApprovedBy(response.getApprovedBy());
        dto.setCreatedAt(response.getCreatedAt());
        dto.setDepartmentName(response.getDepartmentName());
        dto.setDepartmentId(response.getDepartmentId());
        dto.setQuarterId(response.getQuarterId());
        return dto;
    }
}