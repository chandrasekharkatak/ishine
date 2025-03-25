package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.KpiDTO;
import com.apmosys.employeeportal.dto.KpisDTO;
import com.apmosys.employeeportal.model.Kpi;
import com.apmosys.employeeportal.model.Kpis;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.KpiRepository;
import com.apmosys.employeeportal.repository.QuarterCycleRepository;
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
    
    @Autowired
    private QuarterCycleRepository quarterCycleRepository;

    private KpiDTO toDTO(Kpi kpi) {
        List<KpisDTO> kpisDTOList = null;
        if (kpi.getKpis() != null) {
            kpisDTOList = kpi.getKpis().stream()
                .map(kpis -> {
                    KpisDTO kpisDTO = new KpisDTO();
                    kpisDTO.setId(kpis.getId());
                    kpisDTO.setDescription(kpis.getDescription());
           
                    return kpisDTO;
                })
                .collect(Collectors.toList());
        }
        
        return KpiDTO.builder()
                .id(kpi.getId())
                .name(kpi.getName())
                .description(kpi.getDescription())
                .quarterId(kpi.getQuarterId())
                .quarter(kpi.getQuarter())
//                .createdAt(kpi.getCreatedAt())
//                .updatedAt(kpi.getUpdatedAt())
                .department(kpi.getDepartment()) 
                .approvedBy(kpi.getApprovedBy())
                .departmentId(kpi.getDepartmentId())
                
                .kpis(kpisDTOList)
                .build();
    }

    private Kpi toEntity(KpiDTO dto) {
        Kpi kpi = new Kpi();
        kpi.setName(dto.getName());
        kpi.setDescription(dto.getDescription());
        kpi.setApprovedBy(dto.getApprovedBy());
        kpi.setDepartment(dto.getDepartment()); 
        kpi.setQuarterId(dto.getQuarterId());
        kpi.setQuarter(dto.getQuarter());
        kpi.setDepartmentId(dto.getDepartmentId());
        
        if (dto.getKpis() != null) {
            dto.getKpis().forEach(kpisDTO -> {
                Kpis kpisItem = new Kpis();
                kpisItem.setDescription(kpisDTO.getDescription());
            
                kpi.addKpis(kpisItem);
            });
        }
        
        return kpi;
    }

    private String getQuarterNameFromCycle(Long quarterId) {
        if (quarterId == null) {
            return "Unknown Quarter";
        }
        
        List<Object[]> quarterIdList = quarterCycleRepository.findQuarterCycleById(quarterId);
        
        if (quarterIdList != null && !quarterIdList.isEmpty()) {
            Object[] quarterData = quarterIdList.get(0);
            
            for (int i = 0; quarterData != null && i < quarterData.length; i++) {
                if (quarterData[i] != null && quarterData[i].toString().matches("[A-Z]{3}-[A-Z]{3}")) {
                    return quarterData[i].toString();
                }
            }
            
            if (quarterData != null && quarterData.length > 0) {
                for (int i = 0; i < Math.min(3, quarterData.length); i++) {
                    if (quarterData[i] != null) {
                        return quarterData[i].toString();
                    }
                }
            }
        }
    
        return "Unknown Quarter";
    }
   
    @Autowired
    private DepartmentRepository departmentRepository;

    public KpiDTO createKpi(KpiDTO kpiDTO, Long quarterId, Long departmentId) {
        Kpi kpi = toEntity(kpiDTO);
        
        
        String department = departmentRepository.findNameByDeptId(departmentId);
    
        kpi.setQuarterId(quarterId);
        kpi.setDepartmentId(departmentId);
        kpi.setDepartment(department);

        if (kpi.getQuarterId() != null) {
            kpi.setQuarter(getQuarterNameFromCycle(kpi.getQuarterId()));
        }
  
        kpi.setQuarter(getQuarterNameFromCycle(quarterId));
        
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
            existingKpi.setApprovedBy(updatedKpiDTO.getApprovedBy());
          
            if (updatedKpiDTO.getQuarterId() != null) {
                existingKpi.setQuarterId(updatedKpiDTO.getQuarterId());
                existingKpi.setQuarter(getQuarterNameFromCycle(updatedKpiDTO.getQuarterId()));
            }
       
            if (updatedKpiDTO.getKpis() != null) {
                existingKpi.getKpis().clear();
            
                updatedKpiDTO.getKpis().forEach(kpisDTO -> {
                    Kpis kpisItem = new Kpis();
                    kpisItem.setDescription(kpisDTO.getDescription());
                 
                    existingKpi.addKpis(kpisItem);
                });
            }
            
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
            return toDTO(kpiRepository.save(kpi));
        }).orElseThrow(() -> new EntityNotFoundException("KPI not found with ID: " + id));
    }

    public KpiDTO approveKpi(Long id) {
        return kpiRepository.findById(id).map(kpi -> {
            kpi.setApprovedBy("HOD/Manager"); 
            return toDTO(kpiRepository.save(kpi));
        }).orElseThrow(() -> new EntityNotFoundException("KPI not found with ID: " + id));
    }

    public List<KpiDTO> bulkApproveKpis(List<Long> kpiIds) {
        List<Kpi> kpis = kpiRepository.findAllById(kpiIds);
        kpis.forEach(kpi -> kpi.setApprovedBy("HOD/Manager"));
        return kpiRepository.saveAll(kpis).stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    public List<KpiDTO> getKpisByQuarter(Long quarterId) {
        return kpiRepository.findByQuarterId(quarterId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<KpiDTO> getKpisByDepartmentId(Long departmentId) {
        return kpiRepository.findAll().stream()
                .filter(kpi -> departmentId.equals(kpi.getDepartmentId()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
 
    public List<KpiDTO> getKpisByDepartmentName(String departmentName) {
        return kpiRepository.findAll().stream()
                .filter(kpi -> departmentName.equalsIgnoreCase(kpi.getDepartment()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<KpiDTO> getKpisByQuarterAndDepartment(Long quarterId, Long departmentId) {
        return kpiRepository.findByQuarterId(quarterId).stream()
            .filter(kpi -> departmentId.equals(kpi.getDepartmentId()))
            .map(this::toDTO)
            .collect(Collectors.toList());
    }


}