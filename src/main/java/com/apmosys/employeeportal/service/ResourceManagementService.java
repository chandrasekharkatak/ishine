package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ResourceManagementDTO;
import com.apmosys.employeeportal.model.DraftTeam;
import com.apmosys.employeeportal.repository.DraftTeamRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class ResourceManagementService {
	
	@Autowired
	ProjectService projectService;
	
	@Autowired
	DraftTeamRepository draftTeamRepository;

	public ServiceResponse createDraftTeam(ResourceManagementDTO resourceManagementDTO) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<DraftTeam> dratTeamObj = new ArrayList<DraftTeam>();
			resourceManagementDTO.getTeamList().forEach((teamObj) -> {
				DraftTeam newDraftTeam = new DraftTeam();
				
				newDraftTeam.setPoProjectId(resourceManagementDTO.getPoProjectId());
				newDraftTeam.setProjectName(resourceManagementDTO.getProjectName());
				newDraftTeam.setTeamName(teamObj.getTeamName());
				newDraftTeam.setDescription(teamObj.getDescription());
				newDraftTeam.setTeamLeadId(teamObj.getTeamLeadId());
				
				//Add team members
				StringBuilder teamMember = new StringBuilder("");
				for(String member: teamObj.getTeamMemberList()) {
					teamMember.append(member).append(",");
				}
				newDraftTeam.setTeamMember(teamMember.toString());
				
				//Add department
				StringBuilder department = new StringBuilder("");
				for(String deptId: teamObj.getDepartmentList()) {
					department.append(deptId).append(",");
				}
				newDraftTeam.setDeptIds(department.toString());
				dratTeamObj.add(newDraftTeam);
			});
			List<DraftTeam> dbResponse = draftTeamRepository.saveAll(dratTeamObj);
			
			if(!dbResponse.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("Team(s) created successfully.");
			}else {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Unable to create Team(s).");
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

}
