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


import com.apmosys.employeeportal.dto.AutoMigrationDTO;
import com.apmosys.employeeportal.dto.EmployeeImpactDTO;
import com.apmosys.employeeportal.dto.POResourceRequirementDTO;
import com.apmosys.employeeportal.dto.RequirementChangeDTO;
import com.apmosys.employeeportal.enums.ChangeType;
import com.apmosys.employeeportal.model.PoRequirementMapping;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectPoDetails;
import com.apmosys.employeeportal.model.RoleDetails;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.PoDepartmentMappingRepository;
import com.apmosys.employeeportal.repository.PoRequirementMappingRepository;
import com.apmosys.employeeportal.repository.ProjectPoDetailsRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
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
	ProjectPoDetailsRepository projectPoDetailsRepository;
	
	@Autowired
	ProjectRepository projectRepository;
	
	
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
		
		
		
//		public List<RequirementChangeDTO> detectRequirementChanges(
//		        Long poId,
//		        List<POResourceRequirementDTO> incoming) {
//
//		    List<RequirementChangeDTO> changes = new ArrayList<>();
//
//		    // OLD snapshot
//		    List<PoRequirementMapping> oldList =
//		            poRequirementMappingRepository.findByPoId(poId)
//		                    .stream()
//		                    .filter(PoRequirementMapping::isActive)
//		                    .collect(Collectors.toList());
//
//		    Map<String, PoRequirementMapping> oldMap = new HashMap<>();
//		    for (PoRequirementMapping old : oldList) {
//		        String key = buildKey(old.getRoleId(),
//		                old.getLineItemStartDate(),
//		                old.getLineItemEndDate());
//		        oldMap.put(key, old);
//		    }
//
//		    // NEW snapshot (simulate)
//		    Map<String, POResourceRequirementDTO> newMap = new HashMap<>();
//
//		    for (POResourceRequirementDTO r : incoming) {
//
//		        RoleDetails role = getOrCreateRole(r);
//
//		        String key = buildKey(role.getRoleId(),
//		                convert(r.getLineItemStartDate()),
//		                convert(r.getLineItemEndDate()));
//
//		        newMap.put(key, r);
//		    }
//
//		    // Detect removed & count changes
//		    for (String key : oldMap.keySet()) {
//
//		        PoRequirementMapping oldReq = oldMap.get(key);
//
//		        if (!newMap.containsKey(key)) {
//
//		            RequirementChangeDTO dto =
//		                    buildChangeDTO(oldReq, 0l, ChangeType.ROLE_REMOVED);
//
//		            populateEmployeeImpact(poId, oldReq.getRoleId(), dto);
//
//		            changes.add(dto);
//		            continue;
//		        }
//
//		        POResourceRequirementDTO newReq = newMap.get(key);
//
//		        if (!Objects.equals(oldReq.getCount(), newReq.getCount())) {
//
//		            ChangeType type =
//		                    oldReq.getCount() > newReq.getCount()
//		                            ? ChangeType.COUNT_DECREASED
//		                            : ChangeType.COUNT_INCREASED;
//
//		            RequirementChangeDTO dto =
//		                    buildChangeDTO(oldReq, newReq.getCount(), type);
//
//		            if (type == ChangeType.COUNT_DECREASED) {
//		                populateEmployeeImpact(poId, oldReq.getRoleId(), dto);
//		            }
//
//		            changes.add(dto);
//		        }
//		    }
//
//		    // Detect new roles
//		    for (String key : newMap.keySet()) {
//
//		        if (!oldMap.containsKey(key)) {
//
//		            POResourceRequirementDTO r = newMap.get(key);
//		            RoleDetails role = getOrCreateRole(r);
//
//		            RequirementChangeDTO dto = new RequirementChangeDTO();
//		            dto.setRoleId(role.getRoleId());
//		            dto.setRole(role.getRole());
//		            dto.setDepartment(role.getDepartment());
//		            dto.setExperience(role.getExperience());
//		            dto.setLineItemStartDate(convert(r.getLineItemStartDate()).toString());
//		            dto.setLineItemEndDate(convert(r.getLineItemEndDate()).toString());
//		            dto.setOldCount(0l);
//		            dto.setNewCount(r.getCount());
//		            dto.setChangeType(ChangeType.NEW_ROLE_ADDED);
//
//		            changes.add(dto);
//		        }
//		    }
//
//		    return changes;
//		}
//		
//		private String buildKey(Long roleId, LocalDateTime start, LocalDateTime end) {
//		    return roleId + "_" + start + "_" + end;
//		}
//		
//		private RequirementChangeDTO buildChangeDTO(
//		        PoRequirementMapping oldReq,
//		        Long newCount,
//		        ChangeType type) {
//
//		    RequirementChangeDTO dto = new RequirementChangeDTO();
//
//		    RoleDetails role =
//		            roleDetailsRepository.findById(oldReq.getRoleId()).orElse(null);
//
//		    dto.setRoleId(oldReq.getRoleId());
//		    dto.setRole(role.getRole());
//		    dto.setDepartment(role.getDepartment());
//		    dto.setExperience(role.getExperience());
//
//		    dto.setLineItemStartDate(oldReq.getLineItemStartDate().toString());
//		    dto.setLineItemEndDate(oldReq.getLineItemEndDate().toString());
//
//		    dto.setOldCount(oldReq.getCount());
//		    dto.setNewCount(newCount);
//		    dto.setChangeType(type);
//
//		    return dto;
//		}
//		
//		private void populateEmployeeImpact(
//		        Long poId,
//		        Long roleId,
//		        RequirementChangeDTO dto) {
//
//		    List<EmployeeImpactDTO> impacted =
//		            employeeTeamMapRepository
//		                    .findActiveEmployeesByPoAndRole(poId, roleId);
//
//		    dto.setImpactedEmployees(impacted);
//		}
//		
//		public void sendRequirementChangeMail(
//		        Long poId,
//		        List<RequirementChangeDTO> changes) throws Exception {
//
//		    List<String> hodEmails =
//		    		poDepartmentMappingRepository.findHodEmailsByPoId(poId);
//
//		    if (hodEmails.isEmpty()) return;
//
//		    String to = String.join(",", hodEmails);
//		    String cc = "prarthana.lenka@apmosys.com";
//
//		    String subject = "PO Resource Requirement Update - PO ID: " + poId;
//
//		    String body = buildHtmlBody(poId, changes);
//
//		    mailService.sendMailWithCC("priyadarshini.singh@apmosys.com", cc, subject, body);
//		}
//		
//		
//		private String buildHtmlBody(
//		        Long poId,
//		        List<RequirementChangeDTO> changes) {
//
//		    StringBuilder sb = new StringBuilder();
//
//		    ProjectPoDetails po = projectPoDetailsRepository.findByPoId(poId);
//
//		    sb.append("<h3>Resource Requirement Changes for PO No: ")
//		            .append(po.getPoNo())
//		            .append("</h3>");
//
//		    sb.append("<table border='1' cellpadding='6'>");
//		    sb.append("<tr>")
//		            .append("<th>Role</th>")
//		            .append("<th>Department</th>")
//		            .append("<th>Experience</th>")
//		            .append("<th>Line Item Range</th>")
//		            .append("<th>Old Count</th>")
//		            .append("<th>New Count</th>")
//		            .append("<th>Change</th>")
//		            .append("</tr>");
//
//		    for (RequirementChangeDTO c : changes) {
//
//		        sb.append("<tr>")
//		                .append("<td>").append(c.getRole()).append("</td>")
//		                .append("<td>").append(c.getDepartment()).append("</td>")
//		                .append("<td>").append(c.getExperience()).append("</td>")
//		                .append("<td>")
//		                .append(c.getLineItemStartDate())
//		                .append(" to ")
//		                .append(c.getLineItemEndDate())
//		                .append("</td>")
//		                .append("<td>").append(c.getOldCount()).append("</td>")
//		                .append("<td>").append(c.getNewCount()).append("</td>")
//		                .append("<td>").append(c.getChangeType().getDescription()).append("</td>")
//		                .append("</tr>");
//
//		        if (c.getImpactedEmployees() != null
//		                && !c.getImpactedEmployees().isEmpty()) {
//
//		            sb.append("<tr><td colspan='7'>")
//		                    .append("<b>Impacted Employees (Reason: ")
//		                    .append(getImpactReason(c.getChangeType()))
//		                    .append(")</b>")
//		                    .append("<br/><br/>")
//		                    .append("<table border='1' cellpadding='4'>")
//		                    .append("<tr><th>Name</th><th>Team</th></tr>");
//
//		            for (EmployeeImpactDTO e : c.getImpactedEmployees()) {
//		                sb.append("<tr>")
//		                        .append("<td>").append(e.getEmployeeName()).append("</td>")
//		                        .append("<td>").append(e.getTeamName()).append("</td>")
//		                        .append("</tr>");
//		            }
//
//		            sb.append("</table></td></tr>");
//		        }
//		    }
//
//		    sb.append("</table>");
//
//		    return sb.toString();
//		}
//		
//		
//		private String getImpactReason(ChangeType type) {
//
//		    switch (type) {
//		        case ROLE_REMOVED:
//		            return "This role has been removed from the PO";
//
//		        case COUNT_DECREASED:
//		            return "Role count reduced — reassignment may be required";
//
//		        case COUNT_INCREASED:
//		            return "Role count increased (for visibility only)";
//
//		        case NEW_ROLE_ADDED:
//		            return "New role introduced in PO";
//
//		        default:
//		            return "";
//		    }
//		}
		
		
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
	                        buildChangeDTO(oldReq, 0l, ChangeType.ROLE_REMOVED);

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
	                dto.setOldCount(0l);
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
	            List<RequirementChangeDTO> changes, List<AutoMigrationDTO> autoMigrated) throws Exception {
	    	 ProjectPoDetails po = projectPoDetailsRepository.findByPoId(poId);

	        List<String> hodEmails =
	                poDepartmentMappingRepository.findHodEmailsByPoId(poId);

	        if (hodEmails.isEmpty()) return;

	        String to = String.join(",", hodEmails);
	        String cc = "prarthana.lenka@apmosys.com";

	        String subject = "PO Resource Requirement Update - PO NO: " + po.getPoNo();

	        String body = buildHtmlBody(poId, changes ,autoMigrated);

	        mailService.sendMailWithCC("priyadarshini.singh@apmosys.com", cc, subject, body);
	    }

	    private String buildHtmlBody(
	            Long poId,
	            List<RequirementChangeDTO> changes, List<AutoMigrationDTO> autoMigrated) {

	        StringBuilder sb = new StringBuilder();

	        ProjectPoDetails po = projectPoDetailsRepository.findByPoId(poId);
	        Optional<Project> project = projectRepository.findById(po.getProjectId());

	        // HTML Head & Styling
	        sb.append("<!DOCTYPE html>")
	                .append("<html>")
	                .append("<head>")
	                .append("<meta charset='UTF-8'>")
	                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
	                .append("<style>")
	                .append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #4a4a4a; }")
	                .append("container { max-width: 800px; margin: 0 auto; padding: 20px; }")
	                .append(".header { background: linear-gradient(135deg, #e6d9f3 0%, #f3d5e8 100%); padding: 30px; border-radius: 10px; margin-bottom: 30px; text-align: center; }")
	                .append(".header h1 { color: #6b4c7a; margin: 0; font-size: 26px; }")
	                .append(".po-info { color: #8b6b9d; font-size: 14px; margin-top: 8px; }")
	                .append(".section-title { color: #6b4c7a; font-size: 18px; font-weight: 600; margin: 25px 0 15px 0; padding-bottom: 10px; border-bottom: 3px solid #d9c9e8; }")
	                .append(".change-card { background-color: #fafaf8; border-left: 5px solid #c9b3d9; padding: 18px; margin-bottom: 15px; border-radius: 6px; }")
	                .append(".card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }")
	                .append(".role-name { font-size: 16px; font-weight: 600; color: #5a3d6d; }")
	                .append(".badge { display: inline-block; padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }")
	                .append(".badge-new { background-color: #d4e8d4; color: #4a6f4a; }")
	                .append(".badge-increased { background-color: #cce5f3; color: #1e5ba8; }")
	                .append(".badge-decreased { background-color: #fde8d4; color: #b8650e; }")
	                .append(".badge-removed { background-color: #f3d5d5; color: #8b3a3a; }")
	                .append(".card-details { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 12px; }")
	                .append(".detail-item { font-size: 13px; }")
	                .append(".detail-label { color: #8b7a9d; font-weight: 500; }")
	                .append(".detail-value { color: #3a3a3a; margin-top: 3px; }")
	                .append(".count-row { display: flex; justify-content: space-around; align-items: center; background: #f5f1f8; padding: 12px; border-radius: 5px; margin-bottom: 12px; }")
	                .append(".count-box { text-align: center; }")
	                .append(".count-label { font-size: 12px; color: #8b7a9d; margin-bottom: 4px; }")
	                .append(".count-value { font-size: 20px; font-weight: 700; color: #6b4c7a; }")
	                .append(".arrow { color: #c9b3d9; font-size: 18px; }")
	                .append(".employees-section { background: #f1eef5; padding: 12px; border-radius: 5px; margin-top: 12px; }")
	                .append(".employees-title { font-size: 13px; font-weight: 600; color: #5a3d6d; margin-bottom: 8px; }")
	                .append(".employees-list { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; font-size: 12px; }")
	                .append(".employee-item { background: white; padding: 8px; border-radius: 4px; border-left: 3px solid #d9c9e8; }")
	                .append(".employee-name { font-weight: 500; color: #4a4a4a; }")
	                .append(".employee-team { color: #8b7a9d; font-size: 11px; }")
	                .append(".impact-reason { font-size: 11px; color: #8b7a9d; font-style: italic; margin-top: 4px; }")
	                .append(".footer { background-color: #f5f1f8; padding: 20px; border-radius: 8px; text-align: center; margin-top: 30px; font-size: 12px; color: #8b7a9d; }")
	                .append("</style>")
	                .append("</head>")
	                .append("<body>");

	        // Header
	        sb.append("<div class='header'>")
	                .append("<h1>Resource Requirement Update</h1>")
	                .append("<div class='po-info'>PO No: <strong>").append(po.getPoNo()).append("</strong></div>")
	                .append("<div class='po-info'>Project: <strong>").append(project.get().getProjectName()).append("</strong></div>")
	                .append("</div>");
	        
	        
	        // Separate changes by type
	        List<RequirementChangeDTO> newRoles = new ArrayList<>();
	        List<RequirementChangeDTO> increasedCounts = new ArrayList<>();
	        List<RequirementChangeDTO> decreasedCounts = new ArrayList<>();
	        List<RequirementChangeDTO> removedRoles = new ArrayList<>();

	        for (RequirementChangeDTO change : changes) {
	            switch (change.getChangeType()) {
	                case NEW_ROLE_ADDED:
	                    newRoles.add(change);
	                    break;
	                case COUNT_INCREASED:
	                    increasedCounts.add(change);
	                    break;
	                case COUNT_DECREASED:
	                    decreasedCounts.add(change);
	                    break;
	                case ROLE_REMOVED:
	                    removedRoles.add(change);
	                    break;
	            }
	        }

	        // New Roles Section
	        if (!newRoles.isEmpty()) {
	            sb.append("<div class='section-title'>New Roles Added</div>");
	            for (RequirementChangeDTO change : newRoles) {
	                renderChangeCard(sb, change, "badge-new");
	            }
	        }

	        // Increased Count Section
	        if (!increasedCounts.isEmpty()) {
	            sb.append("<div class='section-title'>Role Count Increased</div>");
	            for (RequirementChangeDTO change : increasedCounts) {
	                renderChangeCard(sb, change, "badge-increased");
	            }
	        }

	        // Decreased Count Section
	        if (!decreasedCounts.isEmpty()) {
	            sb.append("<div class='section-title'>Role Count Decreased</div>");
	            for (RequirementChangeDTO change : decreasedCounts) {
	                renderChangeCard(sb, change, "badge-decreased");
	            }
	        }

	        // Removed Roles Section
	        if (!removedRoles.isEmpty()) {
	            sb.append("<div class='section-title'>Roles Removed</div>");
	            for (RequirementChangeDTO change : removedRoles) {
	                renderChangeCard(sb, change, "badge-removed");
	            }
	        }
	        
	        if (autoMigrated != null && !autoMigrated.isEmpty()) {

	            sb.append("<div class='section-title'>Automatic Resource Onboarding</div>");

	            sb.append("<div style='margin-bottom:15px; font-size:13px; color:#6b4c7a;'>")
	              .append("Resources have been automatically onboarded as part of PO update because matching roles were found between an expired previous PO and the current PO.")
	              .append("</div>");

	            for (AutoMigrationDTO migration : autoMigrated) {

	                sb.append("<div class='change-card'>")

	               
	                  .append("<div style='margin-bottom:10px; font-size:13px;'>")
	                  .append("<b>Previous PO:</b> ").append(migration.getPreviousPoNumber())
	                  .append(" &nbsp;&nbsp; ➝ &nbsp;&nbsp; ")
	                  .append("<b>Current PO:</b> ").append(migration.getCurrentPoNumber())
	                  .append("</div>")

	                  .append("<div class='card-header'>")
	                  .append("<div class='role-name'>")
	                  .append(migration.getRoleName())
	                  .append("</div>")
	                  .append("<span class='badge badge-new'>Auto Onboarded</span>")
	                  .append("</div>");

	                sb.append("<div class='employees-section'>")
	                  .append("<div class='employees-title'>Onboarded Employees</div>")
	                  .append("<div class='employees-list'>");

	                for (EmployeeImpactDTO emp : migration.getEmployees()) {

	                    sb.append("<div class='employee-item'>")
	                      .append("<div class='employee-name'>")
	                      .append(emp.getEmployeeName())
	                      .append("</div>")
	                      .append("<div class='employee-team'>Team: ")
	                      .append(emp.getTeamName())
	                      .append("</div>")
	                      .append("<div class='impact-reason'>")
	                      .append("Automatically onboarded due to role continuity between expired previous PO and updated PO")
	                      .append("</div>")
	                      .append("</div>");
	                }

	                sb.append("</div></div></div>");
	            }
	        }



	        // Footer
	        sb.append("<div class='footer'>")
	                .append("<p><strong>Note:</strong> This is an automated notification. ")
	                .append("Please contact the PO Manager for further details or clarifications.</p>")
	                .append("</div>");

	        sb.append("</body>")
	                .append("</html>");

	        return sb.toString();
	    }

	    private void renderChangeCard(StringBuilder sb, RequirementChangeDTO change, String badgeClass) {
	        sb.append("<div class='change-card'>")
	                .append("<div class='card-header'>")
	                .append("<div class='role-name'>").append(change.getRole()).append("</div>")
	                .append("<span class='badge ").append(badgeClass).append("'>")
	                .append(change.getChangeType().getDescription())
	                .append("</span>")
	                .append("</div>");

	        sb.append("<div class='card-details'>")
	                .append("<div class='detail-item'>")
	                .append("<div class='detail-label'>Department</div>")
	                .append("<div class='detail-value'>").append(change.getDepartment()).append("</div>")
	                .append("</div>")
	                .append("<div class='detail-item'>")
	                .append("<div class='detail-label'>Experience Level</div>")
	                .append("<div class='detail-value'>").append(change.getExperience()).append("</div>")
	                .append("</div>")
	                .append("<div class='detail-item'>")
	                .append("<div class='detail-label'>Start Date</div>")
	                .append("<div class='detail-value'>").append(change.getLineItemStartDate()).append("</div>")
	                .append("</div>")
	                .append("<div class='detail-item'>")
	                .append("<div class='detail-label'>End Date</div>")
	                .append("<div class='detail-value'>").append(change.getLineItemEndDate()).append("</div>")
	                .append("</div>")
	                .append("</div>");

	        // Count comparison
	        sb.append("<div class='count-row'>")
	                .append("<div class='count-box'>")
	                .append("<div class='count-label'>Old Count</div>")
	                .append("<div class='count-value'>").append(change.getOldCount()).append("</div>")
	                .append("</div>")
	                .append("<div class='arrow'></div>")
	                .append("<div class='count-box'>")
	                .append("<div class='count-label'>New Count</div>")
	                .append("<div class='count-value'>").append(change.getNewCount()).append("</div>")
	                .append("</div>")
	                .append("</div>");

	        // Impacted Employees
	        if (change.getImpactedEmployees() != null && !change.getImpactedEmployees().isEmpty()) {
	            sb.append("<div class='employees-section'>")
	                    .append("<div class='employees-title'>Impacted Employees</div>")
	                    .append("<div class='employees-list'>");

	            for (EmployeeImpactDTO employee : change.getImpactedEmployees()) {
	                sb.append("<div class='employee-item'>")
	                        .append("<div class='employee-name'>").append(employee.getEmployeeName()).append("</div>")
	                        .append("<div class='employee-team'><span class='team-label'>Team Name:</span> ").append(employee.getTeamName()).append("</div>")
	                        .append("<div class='impact-reason'>").append(getImpactReason(change.getChangeType())).append("</div>")
	                        .append("</div>");
	            }

	            sb.append("</div>")
	                    .append("</div>");
	        }

	        sb.append("</div>");
	    }

	    private String getImpactReason(ChangeType type) {
	        switch (type) {
	            case ROLE_REMOVED:
	                return "This role has been removed from the PO";
	            case COUNT_DECREASED:
	                return "Role count reduced — reassignment may be required";
	            case COUNT_INCREASED:
	                return "Role count increased (for visibility only)";
	            case NEW_ROLE_ADDED:
	                return "New role introduced in PO";
	            default:
	                return "";
	        }
	    }








	}



