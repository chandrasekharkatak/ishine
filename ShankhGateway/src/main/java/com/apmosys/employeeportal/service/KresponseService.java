	package com.apmosys.employeeportal.service;
	
	import com.apmosys.employeeportal.dto.KresponseDTO;
	import com.apmosys.employeeportal.dto.KresponseRemarkDTO;
	import com.apmosys.employeeportal.model.Kresponse;
	import com.apmosys.employeeportal.model.KresponseRemark;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.KresponseRemarkRepository;
	import com.apmosys.employeeportal.repository.QuarterCycleRepository;
	import com.apmosys.employeeportal.repository.KresponseRepository; 
	import com.apmosys.employeeportal.utility.ServiceResponse;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.stereotype.Service;
	import org.springframework.transaction.annotation.Transactional;
	
	import java.util.ArrayList;
	import java.util.Date;
	import java.util.HashMap;
	import java.util.List;
	import java.util.Map;
	import java.util.Optional;
	import java.util.stream.Collectors;
	
	@Service
	public class KresponseService {
	    @Autowired
	    private KresponseRepository kresponseRepository;
	    
	    @Autowired
	    private KresponseRemarkRepository kresponseRemarkRepository;
	    
	    @Autowired
	    private QuarterCycleRepository quarteCycleRepository;
	    
	    @Autowired
	    private EmployeeRepository employeeRepository;
	    
	    @Transactional
	    public Boolean saveResponses(List<KresponseDTO> responses, Long empId, Long quarterId,Long currentUser) {
	        boolean flag = true;
	        String currentUsername = employeeRepository.findNameByEmpID(currentUser);
	        String employeeRole = employeeRepository.findemployeerole(currentUser);
	        if(!responses.isEmpty()) {
	            // Find existing responses
	            List<Kresponse> existingResponses = kresponseRepository.findByEmpIdAndQuarterId(empId, quarterId);
	            Map<Long, Kresponse> existingResponseMap = new HashMap<>();
	            
	            // Create a map of existing responses by ID for easier lookup
	            for (Kresponse response : existingResponses) {
	                existingResponseMap.put(response.getId(), response);
	            }
	            
	            List<Kresponse> entitiesToSave = new ArrayList<>();
	            
	            for (KresponseDTO dto : responses) {
	                Kresponse entity;
	                
	                // Check if response exists (by ID or by matching description)
	                if (dto.getId() != null && existingResponseMap.containsKey(dto.getId())) {
	                    // Update existing response by ID
	                    entity = existingResponseMap.get(dto.getId());
	                    existingResponseMap.remove(dto.getId());
	                } else {
	                    // Look for matching description if ID is not provided
	                    entity = null;
	                    for (Kresponse existingResponse : existingResponseMap.values()) {
	                        if (existingResponse.getDescription().equals(dto.getDescription())) {
	                            entity = existingResponse;
	                            existingResponseMap.remove(existingResponse.getId());
	                            break;
	                        }
	                    }
	                    
	                    // If still no match, create new
	                    if (entity == null) {
	                        entity = new Kresponse();
	                        entity.setEmpId(empId);
	                        entity.setQuarterId(quarterId);
	                    }
	                }
	                
	                // Update the entity properties
	                entity.setDescription(dto.getDescription());
	                entity.setResponse(dto.getResponse());
	                entity.setIsFixed(dto.getIsFixed());
	                entity.setProgress(dto.getProgress());
	                
	                // Save the entity to get its ID if it's new
	                Kresponse savedEntity = kresponseRepository.save(entity);
	                
	                // If there's a new remark, add it while preserving existing remarks
	                if (dto.getRemark() != null && !dto.getRemark().trim().isEmpty()) {
	                    KresponseRemark remarkEntity = new KresponseRemark();
	                    remarkEntity.setRemark(dto.getRemark());
	                    remarkEntity.setCreatedDate(new Date());
	                    remarkEntity.setCreatedBy(currentUsername); 
	                    remarkEntity.setKresponse(savedEntity);
	                    remarkEntity.setEmployeeRole(employeeRole);
	                    
	                    // Save the remark directly
	                    kresponseRemarkRepository.save(remarkEntity);
	                }
	                
	                entitiesToSave.add(savedEntity);
	            }
	            
	            // Delete any responses that weren't included in the update
	            if (!existingResponseMap.isEmpty()) {
	                kresponseRepository.deleteAll(existingResponseMap.values());
	            }
	            
	            // Save all updated entities
	            kresponseRepository.saveAll(entitiesToSave);
	        }
	        
	        return flag;
	    }
	    public List<KresponseDTO> showResponse(Long empId, Long quarterId) {
	        ServiceResponse response = new ServiceResponse();
	        Long empId_quarterId = kresponseRepository.findempId(empId, quarterId);
	        
	        if (empId_quarterId == null) {
	            response.setServiceStatus("NOT_FOUND");
	            response.setServiceMessage("No data found for employee ID " + empId + " and quarter ID " + quarterId);
	            return new ArrayList<>(); // Return empty list if no data found
	        }
	        
	        List<Kresponse> kResponseList = kresponseRepository.findByEmpIdAndQuarterId(empId, quarterId);
	        List<KresponseDTO> responseDTOs = new ArrayList<>();
	        
	        for (Kresponse kResponse : kResponseList) {
	            KresponseDTO dto = new KresponseDTO();
	            dto.setId(kResponse.getId());
	            dto.setResponse(kResponse.getResponse());
	            
	            dto.setEmpId(empId);
	            dto.setQuarterId(kResponse.getQuarterId());
	            dto.setDescription(kResponse.getDescription());
	            dto.setIsFixed(kResponse.getIsFixed());
	            dto.setProgress(kResponse.getProgress());
	            
	            // Convert remarks to DTOs to avoid circular references
	            List<KresponseRemarkDTO> remarkDTOs = kResponse.getRemarks().stream()
	                .map(remark -> {
	                    KresponseRemarkDTO remarkDTO = new KresponseRemarkDTO();
	                    remarkDTO.setId(remark.getId());
	                    remarkDTO.setRemark(remark.getRemark());
	                    remarkDTO.setCreatedBy(remark.getCreatedBy());
	                    remarkDTO.setCreatedDate(remark.getCreatedDate());
	                    remarkDTO.setKresponseId(kResponse.getId());
	                    remarkDTO.setEmployeeRole(remark.getEmployeeRole());
	                    return remarkDTO;
	                })
	                .collect(Collectors.toList());
	            
	            dto.setRemarks(remarkDTOs);
	            responseDTOs.add(dto);
	        }
	        
	        response.setServiceStatus("SUCCESS");
	        response.setServiceMessage("Data retrieved successfully");
	        response.setServiceResponse(responseDTOs);
	        
	        return responseDTOs;
	    }
	
	    @Transactional
	    public KresponseRemarkDTO addRemarkToResponse(Long kresponseId, KresponseRemarkDTO remarkDTO, String username) {
	        Optional<Kresponse> kresponseOptional = kresponseRepository.findById(kresponseId);
	        
	        if (!kresponseOptional.isPresent()) {
	            return null;
	        }
	        
	        Kresponse kresponse = kresponseOptional.get();
	        
	        KresponseRemark remark = new KresponseRemark();
	        remark.setRemark(remarkDTO.getRemark());
	        remark.setCreatedBy(null);
	        remark.setCreatedDate(new Date());
	        remark.setKresponse(kresponse);
	        
	        remark = kresponseRemarkRepository.save(remark);
	        
	        remarkDTO.setId(remark.getId());
	        remarkDTO.setCreatedBy(remark.getCreatedBy());
	        remarkDTO.setCreatedDate(remark.getCreatedDate());
	        remarkDTO.setKresponseId(kresponseId);
	        
	        return remarkDTO;
	    }
	    
	    public List<KresponseRemarkDTO> getRemarksByKresponseId(Long kresponseId) {
	        List<KresponseRemark> remarks = kresponseRemarkRepository.findByKresponseId(kresponseId);
	        
	        return remarks.stream()
	            .map(remark -> {
	                KresponseRemarkDTO dto = new KresponseRemarkDTO();
	                dto.setId(remark.getId());
	                dto.setRemark(remark.getRemark());
	                dto.setCreatedBy(remark.getCreatedBy());
	                dto.setCreatedDate(remark.getCreatedDate());
	                dto.setKresponseId(kresponseId);
	                return dto;
	            })
	            .collect(Collectors.toList());
	    }
	}