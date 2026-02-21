package com.apmosys.employeeportal.service.helper;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.LocationSessionDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.model.EmployeeTimesheetLocationMapping;
import com.apmosys.employeeportal.repository.EmployeeTimesheetLocationMappingRepository;
import com.apmosys.employeeportal.repository.TimesheetRejectionDetailsNewRepository;
import com.apmosys.employeeportal.service.ActivityTimesheetService;
import com.apmosys.employeeportal.service.ProjectTimesheetService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TimesheetStructureCleanupService {

	@Autowired
	private EmployeeTimesheetLocationMappingRepository locationRepo;

	@Autowired
	private ProjectTimesheetService projectTimesheetService;

	@Autowired
	private ActivityTimesheetService activityTimesheetService;
	
	@Autowired
	private TimesheetRejectionDetailsNewRepository timesheetRejectionDetailsNewRepository;

	public void cleanupRemovedLocations(Long timesheetId, List<LocationSessionDTO> incomingLocations) {

		List<EmployeeTimesheetLocationMapping> dbLocations = locationRepo.findByTimesheetId(timesheetId);

		if (dbLocations == null || dbLocations.isEmpty()) {
			return;
		}

		Set<Long> incomingMappingIds = incomingLocations == null ? Set.of()
				: incomingLocations.stream().map(LocationSessionDTO::getLocationMappingId).filter(Objects::nonNull)
						.collect(Collectors.toSet());

		for (EmployeeTimesheetLocationMapping dbLoc : dbLocations) {

			Long locationMappingId = dbLoc.getLocationMappingId();

			if (!incomingMappingIds.contains(locationMappingId)) {

				log.info("Deleting removed locationMappingId={}", locationMappingId);

				deleteLocationCascade(timesheetId, locationMappingId);
			}
		}
	}

	public void cleanupRemovedProjects(Long timesheetId, LocationSessionDTO locationDTO) {

		Long locationMappingId = locationDTO.getLocationMappingId();

		// New location → nothing to clean
		if (locationMappingId == null) {
			return;
		}

		Set<Integer> incomingProjectIds = locationDTO.getProjects() == null ? Set.of()
				: locationDTO.getProjects().stream().map(ProjectTimesheetDTO::getProjectId).collect(Collectors.toSet());

		List<ProjectTimesheetDTO> dbProjects = projectTimesheetService
				.findByTimesheetIdAndLocationMappingId(timesheetId, locationMappingId);

		for (ProjectTimesheetDTO dbProject : dbProjects) {

			Integer projectId = dbProject.getProjectId();

			if (!incomingProjectIds.contains(projectId)) {

				log.info("Deleting removed projectId={} from locationMappingId={}", projectId, locationMappingId);

				deleteProjectCascade(timesheetId, locationMappingId, projectId);
			}
		}
	}

	private void deleteProjectCascade(Long timesheetId, Long locationMappingId, Integer projectId) {
		
		if (projectTimesheetService.isProjectApproved(
		        timesheetId, locationMappingId, projectId)) {
		    throw new IllegalStateException(
		        "Approved project cannot be deleted. "
		        + "ProjectId=" + projectId);
		}


		timesheetRejectionDetailsNewRepository.deleteRow(timesheetId, locationMappingId, projectId);
		activityTimesheetService.deleteActivitiesForProject(timesheetId, locationMappingId, projectId);

		projectTimesheetService.deleteByTimesheetIdAndLocationMappingIdAndProjectId(timesheetId, locationMappingId,
				projectId);
	}

	private void deleteLocationCascade(Long timesheetId, Long locationMappingId) {

		List<ProjectTimesheetDTO> projects = projectTimesheetService.findByLocationMappingId(locationMappingId);

		for (ProjectTimesheetDTO project : projects) {
			deleteProjectCascade(timesheetId, locationMappingId, project.getProjectId());
		}

		locationRepo.deleteById(locationMappingId);
	}
	
	
	public void cleanTimesheetStructure(
	        Long timesheetId,
	        List<LocationSessionDTO> incomingLocations) {

	    // 1️ Remove deleted locations
	    cleanupRemovedLocations(timesheetId, incomingLocations);

	    // 2️ Remove deleted projects inside remaining locations
	    if (incomingLocations != null) {
	        for (LocationSessionDTO loc : incomingLocations) {
	            cleanupRemovedProjects(timesheetId, loc);
	        }
	    }
	}
	


}
