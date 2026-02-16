package com.apmosys.employeeportal.service;

import java.text.SimpleDateFormat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.apmosys.employeeportal.dto.EmployeeImpactDTO;
import com.apmosys.employeeportal.dto.POResourceRequirementDTO;
import com.apmosys.employeeportal.dto.RequirementChangeDTO;
import com.apmosys.employeeportal.enums.ChangeType;
import com.apmosys.employeeportal.model.PoRequirementMapping;
import com.apmosys.employeeportal.model.RoleDetails;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.PoDepartmentMappingRepository;
import com.apmosys.employeeportal.repository.PoRequirementMappingRepository;
import com.apmosys.employeeportal.repository.RoleDetailsRepository;


@Service
public class ResourceRequirementService {
	
	@Autowired
	PoRequirementMappingRepository poRequirementMappingRepository;
	
	
	@Autowired
	RoleDetailsRepository roleDetailsRepository;
	
	@Autowired
	EmployeeTeamMapRepository employeeTeamMapRepository;
	
	@Autowired
	PoDepartmentMappingRepository poDepartmentMappingRepository;
	
	@Autowired
	MailService mailService;
	
//	public void syncRequirementsRTS(
//	        Long poId,
//	        List<POResourceRequirementDTO> incoming) {
//
//	    List<PoRequirementMapping> existing = poRequirementMappingRepository.findByPoId(poId);
//        Set<Long> matchedExistingIds = new HashSet<>();
//
//	   
//	    for (POResourceRequirementDTO r : incoming) {
//
//	        Optional<PoRequirementMapping> exactMatch =
//	                existing.stream()
//	                        .filter(e -> e.isActive() && isExactMatch(e, r))
//	                        .findFirst();
//
//	        if (exactMatch.isPresent()) {
//	          
//	            matchedExistingIds.add(
//	                    exactMatch.get().getPoRequirementMappingId());
//	            continue;
//	        }
//
//	       
//	        existing.stream()
//	                .filter(PoRequirementMapping::isActive)
//	                .filter(e -> !isExactMatch(e, r))
//	                .forEach(e -> e.setActive(false));
//
//	    
//	        PoRequirementMapping m = new PoRequirementMapping();
//	        m.setPoId(poId);
//	        m.setClientRoleId(r.getClientRoleId());
//	        m.setRole(r.getRole());
//	        m.setExperience(r.getExperience());
//	        m.setDepartment(r.getDepartment());
//	        m.setCount(r.getCount());
//	        m.setYearWiseRateCartStartDate(convert(r.getYearWiseRateCartStartDate()));
//	        m.setYearWiseRateCartEndDate(convert(r.getYearWiseRateCartEndDate()));
//	        m.setLineItemStartDate(convert(r.getLineItemStartDate()));
//	        m.setLineItemEndDate(convert(r.getLineItemEndDate()));
//	        m.setActive(true);
//
//	        poRequirementMappingRepository.save(m);
//	    }
//
//	  
//	    for (PoRequirementMapping e : existing) {
//
//	        boolean existsInIncoming =
//	                incoming.stream()
//	                        .anyMatch(r -> isExactMatch(e, r));
//
//	        if (!existsInIncoming && e.isActive()) {
//	            e.setActive(false);
//	        }
//	    }
//
//	    poRequirementMappingRepository.saveAll(existing);
//	}
//	
//	
//	private boolean isExactMatch(PoRequirementMapping e, POResourceRequirementDTO r) {
//	    return Objects.equals(e.getClientRoleId(), r.getClientRoleId()) &&
//	           Objects.equals(e.getRole(), r.getRole()) &&
//	           Objects.equals(e.getExperience(), r.getExperience()) &&
//	           Objects.equals(e.getDepartment(), r.getDepartment()) &&
//	           Objects.equals(e.getCount(), r.getCount()) &&
//	           Objects.equals(e.getYearWiseRateCartStartDate(), convert(r.getYearWiseRateCartStartDate())) &&
//	           Objects.equals(e.getYearWiseRateCartEndDate(), convert(r.getYearWiseRateCartEndDate())) &&
//	           Objects.equals(e.getLineItemStartDate(), convert(r.getLineItemStartDate())) &&
//	           Objects.equals(e.getLineItemEndDate(), convert(r.getLineItemEndDate()));
//	}
	
	
	
	public void syncRequirementsRTS(
	        Long poId,
	        List<POResourceRequirementDTO> incoming,Long empId) {

	    List<PoRequirementMapping> existing =
	            poRequirementMappingRepository.findByPoId(poId);

	    Set<Long> matchedExistingIds = new HashSet<>();

	    for (POResourceRequirementDTO r : incoming) {

	      
	        RoleDetails roleDetails = getOrCreateRole(r);

	      
	        Optional<PoRequirementMapping> exactMatch =
	                existing.stream()
	                        .filter(PoRequirementMapping::isActive)
	                        .filter(e -> isExactMatch(e, r, roleDetails.getRoleId()))
	                        .findFirst();

	        if (exactMatch.isPresent()) {
	            matchedExistingIds.add(
	                    exactMatch.get().getPoRequirementMappingId());
	            continue;
	        }

	   
	        PoRequirementMapping m = new PoRequirementMapping();
	        m.setPoId(poId);
	        m.setRoleId(roleDetails.getRoleId());
	        m.setCount(r.getCount());
	        m.setYearWiseRateCartStartDate(convert(r.getYearWiseRateCartStartDate()));
	        m.setYearWiseRateCartEndDate(convert(r.getYearWiseRateCartEndDate()));
	        m.setLineItemStartDate(convert(r.getLineItemStartDate()));
	        m.setLineItemEndDate(convert(r.getLineItemEndDate()));
	        m.setActive(true);
	        m.setCreatedBy(empId);

	        poRequirementMappingRepository.save(m);
	    }

	   
	    for (PoRequirementMapping e : existing) {

	        boolean existsInIncoming =
	                incoming.stream().anyMatch(r -> {
	                    RoleDetails roleDetails = getOrCreateRole(r);
	                    return isExactMatch(e, r, roleDetails.getRoleId());
	                });

	        if (!existsInIncoming && e.isActive()) {
	            e.setActive(false);
	            e.setUpdatedBy(empId);
	        }
	    }

	    poRequirementMappingRepository.saveAll(existing);
	}

	
	
	private boolean isExactMatch(PoRequirementMapping e,
            POResourceRequirementDTO r,
            Long roleId) {

return Objects.equals(e.getRoleId(), roleId) &&
Objects.equals(e.getCount(), r.getCount()) &&
Objects.equals(e.getYearWiseRateCartStartDate(),
  convert(r.getYearWiseRateCartStartDate())) &&
Objects.equals(e.getYearWiseRateCartEndDate(),
  convert(r.getYearWiseRateCartEndDate())) &&
Objects.equals(e.getLineItemStartDate(),
  convert(r.getLineItemStartDate())) &&
Objects.equals(e.getLineItemEndDate(),
  convert(r.getLineItemEndDate()));
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
	


		private RoleDetails getOrCreateRole(POResourceRequirementDTO r) {

		    return roleDetailsRepository
		            .findByRoleAndDepartmentAndExperience(
		                    r.getRole(),
		                    r.getDepartment(),
		                    r.getExperience())
		            .orElseGet(() -> {
		                RoleDetails role = new RoleDetails();
		                role.setRole(r.getRole());
		                role.setDepartment(r.getDepartment());
		                role.setExperience(r.getExperience());
		                return roleDetailsRepository.save(role);
		            });
		}
		
		
		
		public List<RequirementChangeDTO> detectRequirementChanges(
		        Long poId,
		        List<POResourceRequirementDTO> incoming) {

		    List<RequirementChangeDTO> changes = new ArrayList<>();

		    // OLD snapshot
		    List<PoRequirementMapping> oldList =
		            poRequirementMappingRepository.findByPoId(poId)
		                    .stream()
		                    .filter(PoRequirementMapping::isActive)
		                    .collect(Collectors.toList());

		    Map<String, PoRequirementMapping> oldMap = new HashMap<>();
		    for (PoRequirementMapping old : oldList) {
		        String key = buildKey(old.getRoleId(),
		                old.getLineItemStartDate(),
		                old.getLineItemEndDate());
		        oldMap.put(key, old);
		    }

		    // NEW snapshot (simulate)
		    Map<String, POResourceRequirementDTO> newMap = new HashMap<>();

		    for (POResourceRequirementDTO r : incoming) {

		        RoleDetails role = getOrCreateRole(r);

		        String key = buildKey(role.getRoleId(),
		                convert(r.getLineItemStartDate()),
		                convert(r.getLineItemEndDate()));

		        newMap.put(key, r);
		    }

		    // Detect removed & count changes
		    for (String key : oldMap.keySet()) {

		        PoRequirementMapping oldReq = oldMap.get(key);

		        if (!newMap.containsKey(key)) {

		            RequirementChangeDTO dto =
		                    buildChangeDTO(oldReq, null, ChangeType.ROLE_REMOVED);

		            populateEmployeeImpact(poId, oldReq.getRoleId(), dto);

		            changes.add(dto);
		            continue;
		        }

		        POResourceRequirementDTO newReq = newMap.get(key);

		        if (!Objects.equals(oldReq.getCount(), newReq.getCount())) {

		            ChangeType type =
		                    oldReq.getCount() > newReq.getCount()
		                            ? ChangeType.COUNT_DECREASED
		                            : ChangeType.COUNT_INCREASED;

		            RequirementChangeDTO dto =
		                    buildChangeDTO(oldReq, newReq.getCount(), type);

		            if (type == ChangeType.COUNT_DECREASED) {
		                populateEmployeeImpact(poId, oldReq.getRoleId(), dto);
		            }

		            changes.add(dto);
		        }
		    }

		    // Detect new roles
		    for (String key : newMap.keySet()) {

		        if (!oldMap.containsKey(key)) {

		            POResourceRequirementDTO r = newMap.get(key);
		            RoleDetails role = getOrCreateRole(r);

		            RequirementChangeDTO dto = new RequirementChangeDTO();
		            dto.setRoleId(role.getRoleId());
		            dto.setRole(role.getRole());
		            dto.setDepartment(role.getDepartment());
		            dto.setExperience(role.getExperience());
		            dto.setLineItemStartDate(convert(r.getLineItemStartDate()).toString());
		            dto.setLineItemEndDate(convert(r.getLineItemEndDate()).toString());
		            dto.setOldCount(null);
		            dto.setNewCount(r.getCount());
		            dto.setChangeType(ChangeType.NEW_ROLE_ADDED);

		            changes.add(dto);
		        }
		    }

		    return changes;
		}
		
		private String buildKey(Long roleId, LocalDateTime start, LocalDateTime end) {
		    return roleId + "_" + start + "_" + end;
		}
		
		private RequirementChangeDTO buildChangeDTO(
		        PoRequirementMapping oldReq,
		        Long newCount,
		        ChangeType type) {

		    RequirementChangeDTO dto = new RequirementChangeDTO();

		    RoleDetails role =
		            roleDetailsRepository.findById(oldReq.getRoleId()).orElse(null);

		    dto.setRoleId(oldReq.getRoleId());
		    dto.setRole(role.getRole());
		    dto.setDepartment(role.getDepartment());
		    dto.setExperience(role.getExperience());

		    dto.setLineItemStartDate(oldReq.getLineItemStartDate().toString());
		    dto.setLineItemEndDate(oldReq.getLineItemEndDate().toString());

		    dto.setOldCount(oldReq.getCount());
		    dto.setNewCount(newCount);
		    dto.setChangeType(type);

		    return dto;
		}
		
		private void populateEmployeeImpact(
		        Long poId,
		        Long roleId,
		        RequirementChangeDTO dto) {

		    List<EmployeeImpactDTO> impacted =
		            employeeTeamMapRepository
		                    .findActiveEmployeesByPoAndRole(poId, roleId);

		    dto.setImpactedEmployees(impacted);
		}
		
		public void sendRequirementChangeMail(
		        Long poId,
		        List<RequirementChangeDTO> changes) throws Exception {

		    List<String> hodEmails =
		    		poDepartmentMappingRepository.findHodEmailsByPoId(poId);

		    if (hodEmails.isEmpty()) return;

		    String to = String.join(",", hodEmails);
		    String cc = "prarthana.lenka@apmosys.com";

		    String subject = "PO Resource Requirement Update - PO ID: " + poId;

		    String body = buildHtmlBody(poId, changes);

		    mailService.sendMailWithCC(to, cc, subject, body);
		}
		
		
		private String buildHtmlBody(
		        Long poId,
		        List<RequirementChangeDTO> changes) {

		    StringBuilder sb = new StringBuilder();

		    sb.append("<h3>Resource Requirement Changes for PO ID: ")
		            .append(poId)
		            .append("</h3>");

		    sb.append("<table border='1' cellpadding='6'>");
		    sb.append("<tr>")
		            .append("<th>Role</th>")
		            .append("<th>Department</th>")
		            .append("<th>Experience</th>")
		            .append("<th>Line Item Range</th>")
		            .append("<th>Old Count</th>")
		            .append("<th>New Count</th>")
		            .append("<th>Change Type</th>")
		            .append("</tr>");

		    for (RequirementChangeDTO c : changes) {

		        sb.append("<tr>")
		                .append("<td>").append(c.getRole()).append("</td>")
		                .append("<td>").append(c.getDepartment()).append("</td>")
		                .append("<td>").append(c.getExperience()).append("</td>")
		                .append("<td>")
		                .append(c.getLineItemStartDate())
		                .append(" to ")
		                .append(c.getLineItemEndDate())
		                .append("</td>")
		                .append("<td>").append(c.getOldCount()).append("</td>")
		                .append("<td>").append(c.getNewCount()).append("</td>")
		                .append("<td>").append(c.getChangeType()).append("</td>")
		                .append("</tr>");

		        if (c.getImpactedEmployees() != null
		                && !c.getImpactedEmployees().isEmpty()) {

		            sb.append("<tr><td colspan='7'>")
		                    .append("<b>Impacted Employees:</b>")
		                    .append("<table border='1' cellpadding='4'>")
		                    .append("<tr><th>Name</th><th>Team</th></tr>");

		            for (EmployeeImpactDTO e : c.getImpactedEmployees()) {
		                sb.append("<tr>")
		                        .append("<td>").append(e.getEmployeeName()).append("</td>")
		                        .append("<td>").append(e.getTeamName()).append("</td>")
		                        .append("</tr>");
		            }

		            sb.append("</table></td></tr>");
		        }
		    }

		    sb.append("</table>");

		    return sb.toString();
		}






	}



