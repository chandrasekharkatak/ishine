package com.apmosys.employeeportal.service;

import com.apmosys.employeeportal.dto.KpiDTO;
import com.apmosys.employeeportal.dto.KpisDTO;
import com.apmosys.employeeportal.model.Kpi;
import com.apmosys.employeeportal.model.Kpis;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
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
    
    @Autowired
    private JobRoleRepository jobRoleRepository;

    private KpiDTO toDTO(Kpi kpi) {
        List<KpisDTO> kpisDTOList = null;
        if (kpi.getKpis() != null) {
            kpisDTOList = kpi.getKpis().stream()
                .map(kpis -> {
                    KpisDTO kpisDTO = new KpisDTO();
                    kpisDTO.setId(kpis.getId());
                    kpisDTO.setDescription(kpis.getDescription());
                    kpisDTO.setIsFixed(kpis.getIsFixed());
           
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
                .department(kpi.getDepartment()) 
                .approvedBy(kpi.getApprovedBy())
                .departmentId(kpi.getDepartmentId())
                .employeeRole(kpi.getEmployeeRole())
                .managerRating(kpi.getManagerRating())
                .managerRemark(kpi.getManagerRemark())
              
                
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
        kpi.setEmployeeRole(dto.getEmployeeRole());
        kpi.setManagerRating(dto.getManagerRating());
        kpi.setManagerRemark(dto.getManagerRemark());
        
        
        if (dto.getKpis() != null) {
            dto.getKpis().forEach(kpisDTO -> {
                Kpis kpisItem = new Kpis();
                kpisItem.setDescription(kpisDTO.getDescription());
                kpisItem.setIsFixed(kpisDTO.getIsFixed());
            
                kpi.addKpis(kpisItem);
            });
        }
        
        return kpi;
    }

    private String getQuarterNameFromCycle(Long quarterId) {
        if (quarterId == null) {
            return "Unknown Quarter";
        }
        else {     
        String name = quarterCycleRepository.findquartercyclebyID(quarterId); 
        return name;
    }
    }
   
    @Autowired
    private DepartmentRepository departmentRepository;

    public KpiDTO createKpi(KpiDTO kpiDTO, Long quarterId, Long departmentId, String employeeRole) {
        Kpi kpi = toEntity(kpiDTO);
        
        
        String department = departmentRepository.findNameByDeptId(departmentId);
        kpi.setQuarterId(quarterId);
        kpi.setDepartmentId(departmentId);
        kpi.setDepartment(department);
        kpi.setEmployeeRole(employeeRole);
        //normalize database
        if (kpi.getQuarterId() != null) {
            kpi.setQuarter(getQuarterNameFromCycle(kpi.getQuarterId()));
        }
//        we dont need to add more columns to databse we can use inner join
  
        
        
        Kpi savedKpi = kpiRepository.save(kpi);
        return toDTO(savedKpi);
    }


    public List<KpiDTO> getAllKpis() {
        return kpiRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
        //server side pagenation / search
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
                    kpisItem.setIsFixed(kpisDTO.getIsFixed());
                 
                    existingKpi.addKpis(kpisItem);
                });
            }
            
            return toDTO(kpiRepository.save(existingKpi));
        }).orElseThrow(() -> new EntityNotFoundException("KPI not found: " + id));
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
    
    public List<KpiDTO> getKpisByQuarterAndDepartment(Long quarterId, Long departmentId,String employeeRole) {
        return kpiRepository.findByQuarterId(quarterId).stream() 
            .filter(kpi -> departmentId.equals(kpi.getDepartmentId()))
            .filter(kpi-> employeeRole.equals(kpi.getEmployeeRole()))
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    public List<KpiDTO> getKpisByDepartmentAndEmployeerole(Long departmentId,String employeeRole) {
        return kpiRepository.findByDepartmentId(departmentId).stream() 
            .filter(kpi-> employeeRole.equals(kpi.getEmployeeRole()))
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
}