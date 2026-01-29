package com.apmosys.employeeportal.service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.POResourceRequirementDTO;
import com.apmosys.employeeportal.model.PoRequirementMapping;
import com.apmosys.employeeportal.repository.PoRequirementMappingRepository;


@Service
public class ResourceRequirementService {
	
	@Autowired
	PoRequirementMappingRepository poRequirementMappingRepository;
	
	
	public void syncRequirementsRTS(
	        Long poId,
	        List<POResourceRequirementDTO> incoming) {

	    List<PoRequirementMapping> existing = poRequirementMappingRepository.findByPoId(poId);
        Set<Long> matchedExistingIds = new HashSet<>();

	   
	    for (POResourceRequirementDTO r : incoming) {

	        Optional<PoRequirementMapping> exactMatch =
	                existing.stream()
	                        .filter(e -> e.isActive() && isExactMatch(e, r))
	                        .findFirst();

	        if (exactMatch.isPresent()) {
	          
	            matchedExistingIds.add(
	                    exactMatch.get().getPoRequirementMappingId());
	            continue;
	        }

	       
	        existing.stream()
	                .filter(PoRequirementMapping::isActive)
	                .filter(e -> !isExactMatch(e, r))
	                .forEach(e -> e.setActive(false));

	    
	        PoRequirementMapping m = new PoRequirementMapping();
	        m.setPoId(poId);
	        m.setClientRoleId(r.getClientRoleId());
	        m.setRole(r.getRole());
	        m.setExperience(r.getExperience());
	        m.setDepartment(r.getDepartment());
	        m.setCount(r.getCount());
	        m.setYearWiseRateCartStartDate(convert(r.getYearWiseRateCartStartDate()));
	        m.setYearWiseRateCartEndDate(convert(r.getYearWiseRateCartEndDate()));
	        m.setLineItemStartDate(convert(r.getLineItemStartDate()));
	        m.setLineItemEndDate(convert(r.getLineItemEndDate()));
	        m.setActive(true);

	        poRequirementMappingRepository.save(m);
	    }

	  
	    for (PoRequirementMapping e : existing) {

	        boolean existsInIncoming =
	                incoming.stream()
	                        .anyMatch(r -> isExactMatch(e, r));

	        if (!existsInIncoming && e.isActive()) {
	            e.setActive(false);
	        }
	    }

	    poRequirementMappingRepository.saveAll(existing);
	}
	
	
	private boolean isExactMatch(PoRequirementMapping e, POResourceRequirementDTO r) {
	    return Objects.equals(e.getClientRoleId(), r.getClientRoleId()) &&
	           Objects.equals(e.getRole(), r.getRole()) &&
	           Objects.equals(e.getExperience(), r.getExperience()) &&
	           Objects.equals(e.getDepartment(), r.getDepartment()) &&
	           Objects.equals(e.getCount(), r.getCount()) &&
	           Objects.equals(e.getYearWiseRateCartStartDate(), convert(r.getYearWiseRateCartStartDate())) &&
	           Objects.equals(e.getYearWiseRateCartEndDate(), convert(r.getYearWiseRateCartEndDate())) &&
	           Objects.equals(e.getLineItemStartDate(), convert(r.getLineItemStartDate())) &&
	           Objects.equals(e.getLineItemEndDate(), convert(r.getLineItemEndDate()));
	}
	
	private LocalDateTime convert(Date date) {
	    if (date == null) return null;

	    if (date instanceof java.sql.Date) {
	        return ((java.sql.Date) date)
	                .toLocalDate()
	                .atStartOfDay();
	    }

	    return date.toInstant()
	            .atZone(ZoneId.systemDefault())
	            .toLocalDateTime();
	}

}
