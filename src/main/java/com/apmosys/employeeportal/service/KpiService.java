package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.KpiDTO;
import com.apmosys.employeeportal.model.Kpi;
import com.apmosys.employeeportal.model.GoalStatus;
import com.apmosys.employeeportal.repository.KpiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KpiService {

    @Autowired
    private KpiRepository kpiRepository;

    private KpiDTO toDTO(Kpi kpi) {
        return KpiDTO.builder()
                .id(kpi.getId())
                .name(kpi.getName())
                .description(kpi.getDescription())
                .type(kpi.getType())
                .status(kpi.getStatus())
                .assignedBy(kpi.getAssignedBy())
                .createdBy(kpi.getCreatedBy())
                .updatedBy(kpi.getUpdatedBy())
                .createdAt(kpi.getCreatedAt())
                .updatedAt(kpi.getUpdatedAt())
                .department(kpi.getDepartment()) 
                .approvedBy(kpi.getApprovedBy())
                .rejectedBy(kpi.getRejectedBy())
                .build();
    }

    private Kpi toEntity(KpiDTO dto) {
        Kpi kpi = new Kpi();
        kpi.setName(dto.getName());
        kpi.setDescription(dto.getDescription());
        kpi.setType(dto.getType());
        kpi.setStatus(dto.getStatus() != null ? dto.getStatus() : GoalStatus.PENDING);
        kpi.setAssignedBy(dto.getAssignedBy());
        kpi.setCreatedBy(dto.getCreatedBy());
        kpi.setUpdatedBy(dto.getUpdatedBy());
        kpi.setApprovedBy(dto.getApprovedBy());
        kpi.setRejectedBy(dto.getRejectedBy());
        kpi.setDepartment(dto.getDepartment()); 
        return kpi;
    }

   
    public KpiDTO createKpi(KpiDTO kpiDTO) {
        Kpi kpi = toEntity(kpiDTO);
        kpi.setCreatedAt(LocalDateTime.now());
        kpi.setUpdatedAt(LocalDateTime.now());
        Kpi savedKpi = kpiRepository.save(kpi);
        return toDTO(savedKpi);
    }

    
    public List<KpiDTO> getAllKpis() {
        return kpiRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    
    public Optional<KpiDTO> getKpiById(Long id) {
        return kpiRepository.findById(id).map(this::toDTO);
    }

    
    public KpiDTO updateKpi(Long id, KpiDTO updatedKpiDTO) {
        return kpiRepository.findById(id).map(existingKpi -> {
            existingKpi.setName(updatedKpiDTO.getName());
            existingKpi.setDescription(updatedKpiDTO.getDescription());
            existingKpi.setType(updatedKpiDTO.getType());
            existingKpi.setAssignedBy(updatedKpiDTO.getAssignedBy());
            existingKpi.setUpdatedBy(updatedKpiDTO.getUpdatedBy());
            existingKpi.setStatus(updatedKpiDTO.getStatus() != null ? updatedKpiDTO.getStatus() : existingKpi.getStatus());
            existingKpi.setApprovedBy(updatedKpiDTO.getApprovedBy());
            existingKpi.setRejectedBy(updatedKpiDTO.getRejectedBy());
            existingKpi.setUpdatedAt(LocalDateTime.now()); 
            return toDTO(kpiRepository.save(existingKpi));
        }).orElseThrow(() -> new EntityNotFoundException("KPI not found with ID: " + id));
    }

    
    public void deleteKpi(Long id) {
        if (kpiRepository.existsById(id)) {
            kpiRepository.deleteById(id);
        } else {
            throw new EntityNotFoundException("KPI with ID " + id + " not found.");
        }
    }

   
    public KpiDTO rejectKpi(Long id) {
        return kpiRepository.findById(id).map(kpi -> {
            kpi.setStatus(GoalStatus.REJECTED);
            kpi.setRejectedBy("HOD/Manager");  
            kpi.setUpdatedAt(LocalDateTime.now());
            return toDTO(kpiRepository.save(kpi));
        }).orElseThrow(() -> new EntityNotFoundException("KPI not found with ID: " + id));
    }

   
    public KpiDTO approveKpi(Long id) {
        return kpiRepository.findById(id).map(kpi -> {
            kpi.setStatus(GoalStatus.APPROVED);
            kpi.setApprovedBy("HOD/Manager"); 
            kpi.setUpdatedAt(LocalDateTime.now());
            return toDTO(kpiRepository.save(kpi));
        }).orElseThrow(() -> new EntityNotFoundException("KPI not found with ID: " + id));
    }

   
    public List<KpiDTO> bulkApproveKpis(List<Long> kpiIds) {
        List<Kpi> kpis = kpiRepository.findAllById(kpiIds);
        kpis.forEach(kpi -> kpi.setStatus(GoalStatus.APPROVED));
        return kpiRepository.saveAll(kpis).stream().map(this::toDTO).collect(Collectors.toList());
    }

 
    public List<KpiDTO> getKpisByDepartment(String department) {
        
        return kpiRepository.findAll().stream()
                .filter(kpi -> department.equalsIgnoreCase(kpi.getDepartment()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
