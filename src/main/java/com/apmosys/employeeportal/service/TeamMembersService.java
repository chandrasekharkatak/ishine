package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.Exception.BadRequestException;
import com.apmosys.employeeportal.dto.BillableInfo;
import com.apmosys.employeeportal.dto.EmployeeDetailsForTeamMemberDTO;
import com.apmosys.employeeportal.dto.EmployeeInformationDTO;
import com.apmosys.employeeportal.dto.EmployeeOtherActiveProject;
import com.apmosys.employeeportal.dto.EmployeeProjectTimesheetDto;
import com.apmosys.employeeportal.dto.MigrateTeam;
import com.apmosys.employeeportal.dto.PoDetailsDto;
import com.apmosys.employeeportal.dto.PoTeamAndMemberDetailsDto;
import com.apmosys.employeeportal.dto.RmgMemberEndDateDto;
import com.apmosys.employeeportal.dto.RmgTeamDto;
import com.apmosys.employeeportal.dto.RmgTeamMemberDto;
import com.apmosys.employeeportal.dto.UnmappedEmployeeProjectDto;
import com.apmosys.employeeportal.model.Activity;
import com.apmosys.employeeportal.model.ActivityTemplate;
import com.apmosys.employeeportal.model.CommonProperties;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.EmpPrimaryProjectMapping;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeClientSideIdMapping;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.model.PoDepartmentMapping;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.model.ProjectManagerMapping;
import com.apmosys.employeeportal.model.ProjectOverheadMapping;
import com.apmosys.employeeportal.model.RoleDetails;
import com.apmosys.employeeportal.model.Team;
import com.apmosys.employeeportal.repository.ActivitiesRepository;
import com.apmosys.employeeportal.repository.ActivityTemplateRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmpPrimaryProjectMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeClientSideIdMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.PoDepartmentMappingRepository;
import com.apmosys.employeeportal.repository.ProjectManagerMappingRepository;
import com.apmosys.employeeportal.repository.ProjectOverheadMappingRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.repository.RoleDetailsRepository;
import com.apmosys.employeeportal.repository.TeamRepository;
import com.apmosys.employeeportal.repository.TimesheetsRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.utility.TypeConversionUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamMembersService {

	@Value("${hr.mail}")
	private String hrMailAddress;

	@Value("${rmg.mail}")
	private String rmgMail;

	@Value("${admin.mail}")
	private String adminMail;

	@Value("${bd.mail}")
	private String bdMail;

	@Value("${finance.mail}")
	private String financeMail;

	private final ActivityTemplateRepository activityTemplateRepository;
	private final ActivitiesRepository activitiesRepository;
	private final DepartmentRepository departmentRepository;
	private final EmployeeRepository employeeRepository;
	private final EmployeeClientSideIdMappingRepository employeeClientSideIdMappingRepository;
	private final EmpPrimaryProjectMappingRepository empPrimaryProjectMappingRepository;
	private final EmployeeTeamMapRepository employeeTeamMapRepository;
	private final ProjectRepository projectRepository;
	private final ProjectManagerMappingRepository projectManagerMappingRepository;
	private final ProjectOverheadMappingRepository projectOverheadMappingRepository;
	private final PoDepartmentMappingRepository poDepartmentMappingRepository;
	private final RoleDetailsRepository roleDetailsRepository;
	private final TeamRepository teamRepository;
	private final TimesheetsRepository timesheetsRepository;

	private final MailService mailService;

	private static final Logger log = LoggerFactory.getLogger(TeamMembersService.class);

	@Transactional(readOnly = true)
	public ServiceResponse getEmployeeExistingProjectDetailsByEmpId(Long empId, Integer projectId) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			if (empId == null) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Employee Id cannot be null!!");
				return serviceResponse;
			}
			if(projectId == null) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Project Id cannot be null!!");
				return serviceResponse;
			}

			List<PoTeamAndMemberDetailsDto> employeeExistingProjectDetailsList = projectRepository
					.getEmployeeExistingProjectDetailsByEmpId(empId, projectId);

			if (employeeExistingProjectDetailsList == null || employeeExistingProjectDetailsList.isEmpty()) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse("Employee Existing Project Details Not found!!");
				return serviceResponse;
			}

			serviceResponse.setServiceResponse(employeeExistingProjectDetailsList);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			serviceResponse.setServiceError(e);
			log.error("Error fetching Employee Existing Project Details by EmpId : ", e);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("Something went wrong!!");
		}
		return serviceResponse;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse addOrUpdateTeamMembers(RmgTeamDto rmgTeamDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (rmgTeamDto == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request cannot be null!!");
				return response;
			}
			if (rmgTeamDto.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}
			if (rmgTeamDto.getUpdatedBy() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Emp Id cannot be null!!");
				return response;
			}
			if (rmgTeamDto.getRmgTeamMemberList() == null || rmgTeamDto.getRmgTeamMemberList().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team Member List cannot be null!!");
				return response;
			}

			Project project = projectRepository.findByProjectId(rmgTeamDto.getProjectId());
			if (project == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}

			log.info("Adding/updating Team members for ProjectId : {}", rmgTeamDto.getProjectId());

			Team team = teamRepository.findByTeamId(rmgTeamDto.getTeamId());
			if (team == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team not found!!");
				return response;
			}

			handleAddOrUpdateTeamMembers(rmgTeamDto, project, team);

			response.setServiceResponse(rmgTeamDto.isIsupdate() ? "Team Member(s) Details updated successfully!!"
					: "New Team Member(s) Details Added successfully. Please approve Project to allow newly added employees to fill timesheet!!");
			response.setServiceResponse1(getDeptIdListFromString(team.getDeptIds()));
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			log.info("Adding/updating Team members Completed for ProjectId : {}", rmgTeamDto.getProjectId());
		} catch (Exception e) {
			log.error("Error in addOrUpdateTeamMembers : ", e);
			response.setServiceResponse("Something went wrong!!");
			response.setServiceError(e.getMessage());
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		}
		return response;
	}

	private void handleAddOrUpdateTeamMembers(RmgTeamDto dto, Project project, Team team) {
		ServiceResponse response = new ServiceResponse();
		Long currentUserEmpId = dto.getUpdatedBy();

		List<RmgTeamMemberDto> teamMemberDtoList = new ArrayList<>(dto.getRmgTeamMemberList());
		List<EmployeeTeamMap> existingMappedMember = Optional
				.ofNullable(employeeTeamMapRepository.findByTeamId(team.getTeamId())).orElse(Collections.emptyList());
		List<Long> newEmpIds = new ArrayList<>();

		Map<Long, EmployeeTeamMap> existingMappedMemberMap = existingMappedMember.stream().collect(Collectors.toMap(
				EmployeeTeamMap::getEmployeeTeamMapId, Function.identity(), (existing, replacement) -> replacement));

		List<EmployeeTeamMap> updatedMemberDbResponse = getTeamMembersObj(teamMemberDtoList, existingMappedMemberMap,
				project, team, currentUserEmpId, dto, newEmpIds);

		team = updateTeamDepartmentIds(team, updatedMemberDbResponse);

		List<Long> defaultProjectEmpIds = teamMemberDtoList.stream().filter(RmgTeamMemberDto::isDefaultProject)
				.map(RmgTeamMemberDto::getEmpId).distinct().collect(Collectors.toList());

		List<Long> allEmpIds = teamMemberDtoList.stream().map(RmgTeamMemberDto::getEmpId).distinct()
				.collect(Collectors.toList());

		updateEmployeeDefaultProjectIfUpdated(allEmpIds, defaultProjectEmpIds, project, currentUserEmpId,
				updatedMemberDbResponse);

		if (!newEmpIds.isEmpty()) {
			String clientName = Optional.ofNullable(dto.getClientName()).orElse("");
			Set<Long> newEmpIdSet = new HashSet<>(newEmpIds);
			List<Long> updatedEmpIds = allEmpIds.stream().filter(empId -> !newEmpIdSet.contains(empId))
					.collect(Collectors.toList());
			createActivityForEmployeeRole(team.getTeamId(), currentUserEmpId, updatedEmpIds, teamMemberDtoList);
			sendProjectMappingEmailToEmployee(project.getProjectName(), clientName, newEmpIds);
		}

		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		if (!dto.isIsupdate() && existingMappedMember.isEmpty()) {
			sendTeamCreationEmail(team, updatedMemberDbResponse);
		}
	}

	private List<EmployeeTeamMap> getTeamMembersObj(List<RmgTeamMemberDto> teamMemberDtoList,
			Map<Long, EmployeeTeamMap> existingMappedMemberMap, Project project, Team team, Long currentUserEmpId,
			RmgTeamDto dto, List<Long> newEmpIds) {

		List<EmployeeTeamMap> updatedMemberList = new ArrayList<>();

		boolean updateProjectFlag = false;
		for (RmgTeamMemberDto teamMember : teamMemberDtoList) {

			if (dto.getProjectType().equalsIgnoreCase("TNM")) {
				if (teamMember.getPoId() == null) {
					throw new BadRequestException(
							"Project Type is TNM & PO Id is null,for EmpId: " + teamMember.getEmpId());
				}
				if (teamMember.getRoleId() == null) {
					throw new BadRequestException(
							"Project Type is TNM & Role Id is null,for EmpId: " + teamMember.getEmpId());
				}
				Optional<RoleDetails> roleDetailsOpt = roleDetailsRepository.findById(teamMember.getRoleId());
				if (roleDetailsOpt.isEmpty()) {
					throw new BadRequestException("Requirement Role not found,for EmpId: " + teamMember.getEmpId()
							+ " and Role Id: " + teamMember.getRoleId());
				}
			}

			boolean shadowUpdatedFlag = false;
			EmployeeTeamMap shadowFlagUpdatedMember = new EmployeeTeamMap();

			EmployeeTeamMap presentMember = existingMappedMemberMap.getOrDefault(teamMember.getEtmId(), null);
			if (presentMember == null) {
				updateProjectFlag = true;
				presentMember = new EmployeeTeamMap();
				presentMember.setCreatedOn(new Timestamp(System.currentTimeMillis()));
				presentMember.setStartDate(
						teamMember.getStartDate() != null ? teamMember.getStartDate() : LocalDateTime.now());
				presentMember.setEndDate(teamMember.getEndDate());
				presentMember.setCreatedBy(currentUserEmpId);
				presentMember.setActive(2L);
				newEmpIds.add(teamMember.getEmpId());
			} else {
				boolean isTeamMemberUpdated = isTeamMemberValueChanged(presentMember, teamMember);
				if (!isTeamMemberUpdated) {
					continue;
				}

				// Create a new Entry of Shadow resource
				if (!Objects.equals(teamMember.getIsShadow(), presentMember.getIsShadow())) {
					shadowUpdatedFlag = true;
					presentMember.setActive(0L);
				}
				presentMember.setActive(Objects.equals(presentMember.getActive(), 1L) ? 1L : 2L);
				presentMember.setUpdatedOn(LocalDateTime.now());
				presentMember.setUpdatedBy(currentUserEmpId);
			}

			String employeeRole = teamMember.getEmployeeRoles().stream().map(String::valueOf)
					.collect(Collectors.joining(","));
			Integer isShadow = teamMember.getIsShadow() != null ? teamMember.getIsShadow() : null;

			presentMember.setEmpId(teamMember.getEmpId());
			presentMember.setEmployeeRole(employeeRole);
			presentMember.setTeamId(team.getTeamId());
			presentMember.setIsShadow(isShadow);
			presentMember.setStartDate(teamMember.getStartDate() != null ? teamMember.getStartDate() : null);
			presentMember.setEndDate(teamMember.getEndDate() != null ? teamMember.getEndDate() : null);
			presentMember.setRoleId(teamMember.getRoleId());
			presentMember.setPoId(teamMember.getPoId());
			presentMember.setEmpTeamDepartmentId(teamMember.getEmpTeamDepartmentId());
			updatedMemberList.add(presentMember);

			if (shadowUpdatedFlag) {
				shadowFlagUpdatedMember.setStartDate(LocalDateTime.now());
				shadowFlagUpdatedMember.setStartDate(
						teamMember.getStartDate() != null ? teamMember.getStartDate() : LocalDateTime.now());
				shadowFlagUpdatedMember.setEndDate(teamMember.getEndDate());
				shadowFlagUpdatedMember.setCreatedBy(currentUserEmpId);
				shadowFlagUpdatedMember.setActive(2L);
				shadowFlagUpdatedMember.setEmpId(teamMember.getEmpId());
				shadowFlagUpdatedMember.setEmployeeRole(employeeRole);
				shadowFlagUpdatedMember.setTeamId(team.getTeamId());
				shadowFlagUpdatedMember.setRoleId(teamMember.getRoleId());
				shadowFlagUpdatedMember.setPoId(teamMember.getPoId());
				shadowFlagUpdatedMember.setIsShadow(isShadow);
				shadowFlagUpdatedMember.setEmpTeamDepartmentId(teamMember.getEmpTeamDepartmentId());
				updatedMemberList.add(shadowFlagUpdatedMember);
				newEmpIds.add(teamMember.getEmpId());
			}
		}
		if (!updatedMemberList.isEmpty()) {
			updatedMemberList = employeeTeamMapRepository.saveAll(updatedMemberList);
		}

		if (updateProjectFlag) {
			project.setIsDraftProject("true");
			projectRepository.save(project);
		}
		return updatedMemberList;
	}

	private Team updateTeamDepartmentIds(Team team, List<EmployeeTeamMap> updatedMemberDbResponse) {
		if (updatedMemberDbResponse == null || updatedMemberDbResponse.isEmpty()) {
			return team;
		}

		Set<Long> employeeDeptIds = updatedMemberDbResponse.stream()
				.filter(emp -> emp.getActive() != null && emp.getActive() != 0L)
				.map(EmployeeTeamMap::getEmpTeamDepartmentId).filter(Objects::nonNull).collect(Collectors.toSet());

		Set<Long> teamDeptIdSet = (team.getDeptIds() != null && !team.getDeptIds().isBlank()) ? Arrays
				.stream(team.getDeptIds().split(",")).map(String::trim).map(Long::parseLong).collect(Collectors.toSet())
				: new HashSet<>();

		teamDeptIdSet.addAll(employeeDeptIds);
		team.setDeptIds(teamDeptIdSet.stream().map(String::valueOf).collect(Collectors.joining(",")));

		return teamRepository.save(team);
	}

	public void updateEmployeeDefaultProjectIfUpdated(List<Long> allEmpIds, List<Long> defaultEmpIds, Project project,
			Long updatedBy, List<EmployeeTeamMap> updatedMemberDbResponse) {
		LocalDateTime now = LocalDateTime.now();
		Long projectId = project.getProjectId().longValue();
		Set<Long> defaultEmpIdSet = new HashSet<>(defaultEmpIds);

		Map<Long, EmployeeTeamMap> empIdAndMemberMap = updatedMemberDbResponse.stream().collect(
				Collectors.toMap(EmployeeTeamMap::getEmpId, Function.identity(), (existing, replace) -> replace));

		List<EmpPrimaryProjectMapping> existingProjectMappings = empPrimaryProjectMappingRepository
				.findByEmpIdInAndIsMappedAndProjectIdNotIn(allEmpIds, project.getProjectId().longValue());

		existingProjectMappings.forEach(emp -> {
			if (defaultEmpIdSet.contains(emp.getEmpId())) {
				EmployeeTeamMap etm = empIdAndMemberMap.get(emp.getEmpId());
				if (etm != null) {
					if (!isDefaultFutureDate(etm.getStartDate(), now)) {
						emp.setIsMapped("N");
					}
				} else {
					emp.setIsMapped("N");
				}
				emp.setUpdatedBy(updatedBy);
				emp.setUpdatedOn(now);
			}
		});

		List<EmpPrimaryProjectMapping> existingCurrentProjectMappings = empPrimaryProjectMappingRepository
				.findByEmpIdInAndIsMappedAndProjectId(allEmpIds, project.getProjectId().longValue());

		Set<Long> existingMappingEmpIdSet = existingCurrentProjectMappings.stream()
				.map(EmpPrimaryProjectMapping::getEmpId)
				.collect(Collectors.toSet());

		if (Objects.equals(existingMappingEmpIdSet, defaultEmpIdSet)) {
			return;
		}

		existingCurrentProjectMappings.forEach(emp -> {
			if (!defaultEmpIdSet.contains(emp.getEmpId())) {
				EmployeeTeamMap etm = empIdAndMemberMap.get(emp.getEmpId());
				if (etm != null) {
					if (!isDefaultFutureDate(etm.getStartDate(), now)) {
						emp.setIsMapped("N");
					}
				} else {
					emp.setIsMapped("N");
				}
				emp.setUpdatedBy(updatedBy);
				emp.setUpdatedOn(now);
			}
		});

		Map<Long, Employee> employeeMap = employeeRepository.findByEmpIdIn(allEmpIds).stream()
				.collect(Collectors.toMap(Employee::getEmpId, Function.identity()));

		List<Long> shadowEmpIds = employeeTeamMapRepository.findShadowMembersByEmpIdsAndProjectId(allEmpIds,
				project.getProjectId());

		BillableInfo billableInfo = resolveBillableInfo(project);

		Map<Long, BillableInfo> billableUpdates = new HashMap<>();
		List<EmpPrimaryProjectMapping> mappingsToSave = new ArrayList<>(existingCurrentProjectMappings);

		for (Long empId : allEmpIds) {
			if (!existingMappingEmpIdSet.contains(empId) && defaultEmpIdSet.contains(empId)) {
				EmployeeTeamMap etm = empIdAndMemberMap.get(empId);
				boolean isDefaultProjectDateOfFuture = false;
				if (etm != null && isDefaultFutureDate(etm.getStartDate(), now)) {
					isDefaultProjectDateOfFuture = true;
				}
				mappingsToSave
						.add(createNewMapping(empId, project, projectId, updatedBy, now, isDefaultProjectDateOfFuture));
			}

			BillableInfo finalInfo = shadowEmpIds.contains(empId) ? new BillableInfo("Shadow", "No") : billableInfo;
			Employee emp = employeeMap.get(empId);
			if (emp == null || !Objects.equals(emp.getBillable(), finalInfo.getBillable())
					|| !Objects.equals(emp.getBillableType(), finalInfo.getBillableType())) {
				billableUpdates.put(empId, finalInfo);
			}
		}
		empPrimaryProjectMappingRepository.saveAll(mappingsToSave);

		for (Map.Entry<Long, BillableInfo> entry : billableUpdates.entrySet()) {
			BillableInfo empIdToBillable = entry.getValue();
			employeeRepository.updateBillableFields(entry.getKey(), empIdToBillable.getBillable(),
					empIdToBillable.getBillableType());
		}
	}

	private void createActivityForEmployeeRole(Long teamId, Long currentUserEmpId, List<Long> newEmpIds,
			List<RmgTeamMemberDto> teamMemberDtoList) {
		List<EmployeeDetailsForTeamMemberDTO> empInfoList = employeeRepository
				.getEmployeeDetailsAndEtmDeptIdForTeamByTeamId(newEmpIds, teamId);
		if (empInfoList == null || empInfoList.isEmpty()) {
			return;
		}

		Map<Long, EmployeeDetailsForTeamMemberDTO> empIdInfoMap = empInfoList.stream()
				.collect(Collectors.toMap(emp -> emp.getEmpId(), Function.identity(), (existing, replace) -> replace));

		List<Activity> newActivityList = new ArrayList<>();
		for (RmgTeamMemberDto teamMember : teamMemberDtoList) {
			EmployeeDetailsForTeamMemberDTO empInfoObj = empIdInfoMap.getOrDefault(teamMember.getEmpId(), null);

			if (empInfoObj == null || empInfoObj.getDeptId() == null) {
				continue;
			}

			Long departmentId = empInfoObj.getDeptId();
			for (String role : teamMember.getEmployeeRoles()) {
				role = role.trim();
				List<Activity> existingActivities = activitiesRepository
						.findByDeptIdsAndEmployeeRoleAndTeamId(departmentId.toString(), role, teamId);

				if (existingActivities.isEmpty()) {
					List<ActivityTemplate> activityTemplateList = activityTemplateRepository
							.getByDeptIdAndEmployeeRoleType(departmentId, role);
					if (!activityTemplateList.isEmpty()) {
						for (ActivityTemplate activityTemplate : activityTemplateList) {
							Activity newActivity = new Activity();
							newActivity.setActivity(activityTemplate.getTemplateActivity());
							newActivity.setTeamId(teamId);
							newActivity.setEmployeeRole(activityTemplate.getEmployeeRole());
							newActivity.setDeptIds(activityTemplate.getDeptId().toString());
							newActivity.getCommonProperty().setCreatedBy(currentUserEmpId);
							newActivityList.add(newActivity);
						}
					}
				}
			}
		}
		if (!newActivityList.isEmpty()) {
			activitiesRepository.saveAll(newActivityList);
		}
	}

	public void updateEmployeeDefaultProject(List<Long> empIds, Project project, Long updatedBy,
			LocalDateTime defaultProjectStartDate) {
		if (empIds == null || empIds.isEmpty()) {
			return;
		}
		if (project == null || project.getProjectId() == null) {
			throw new IllegalArgumentException("Project cannot be null");
		}
		if (updatedBy == null) {
			throw new IllegalArgumentException("UpdatedBy cannot be null");
		}

		List<Long> uniqueEmpIds = empIds.stream().distinct().collect(Collectors.toList());
		LocalDateTime now = LocalDateTime.now();
		Long projectId = project.getProjectId().longValue();

		boolean isDefaultProjectDateOfFuture = isDefaultFutureDate(defaultProjectStartDate, now);

		List<EmpPrimaryProjectMapping> existingMappings = empPrimaryProjectMappingRepository
				.findByEmpIdInAndIsMapped(uniqueEmpIds);
		existingMappings.forEach(emp -> {
			if (!isDefaultProjectDateOfFuture) {
				emp.setIsMapped("N");
			}
			emp.setUpdatedBy(updatedBy);
			emp.setUpdatedOn(now);
		});

		Set<Long> alreadyPrimaryEmpIds = new HashSet<>(empPrimaryProjectMappingRepository
				.findEmpIdByEmpIdInAndPrimaryProjectIdAndIsMapped(uniqueEmpIds, projectId, "Y"));

		Map<Long, Employee> employeeMap = employeeRepository.findByEmpIdIn(uniqueEmpIds).stream()
				.collect(Collectors.toMap(Employee::getEmpId, Function.identity()));

		Set<Long> shadowEmpIds = new HashSet<>(
				employeeTeamMapRepository.findShadowMembersByEmpIdsAndProjectId(uniqueEmpIds, project.getProjectId()));

		BillableInfo billableInfo = resolveBillableInfo(project);

		List<EmpPrimaryProjectMapping> mappingsToSave = new ArrayList<>(existingMappings);
		Map<Long, BillableInfo> billableUpdates = new HashMap<>();

		for (Long empId : uniqueEmpIds) {

			if (!alreadyPrimaryEmpIds.contains(empId)) {
				mappingsToSave
						.add(createNewMapping(empId, project, projectId, updatedBy, now, isDefaultProjectDateOfFuture));
			}
			BillableInfo finalInfo = shadowEmpIds.contains(empId) ? new BillableInfo("Shadow", "No") : billableInfo;

			Employee emp = employeeMap.get(empId);
			if (emp == null || !Objects.equals(emp.getBillable(), finalInfo.getBillable())
					|| !Objects.equals(emp.getBillableType(), finalInfo.getBillableType())) {
				billableUpdates.put(empId, finalInfo);
			}
		}
		if (!mappingsToSave.isEmpty()) {
			empPrimaryProjectMappingRepository.saveAll(mappingsToSave);
		}
		for (Map.Entry<Long, BillableInfo> entry : billableUpdates.entrySet()) {
			BillableInfo empIdToBillable = entry.getValue();
			employeeRepository.updateBillableFields(entry.getKey(), empIdToBillable.getBillable(),
					empIdToBillable.getBillableType());
		}
	}

	public void sendDepartmentWiseUnmappedEmployeeProjectMail() {
		try {
			List<Department> departments = departmentRepository.findAll();
			if (departments == null || departments.isEmpty()) {
				return;
			}
			Set<String> excludedDepartmentNames = Set.of("super admin", "director", "unknown department", "ceo office");

			List<Object[]> listObjArray = employeeTeamMapRepository
					.getUnmappedEmployeeProjectDetails(excludedDepartmentNames);
			if (listObjArray == null || listObjArray.isEmpty()) {
				return;
			}

			List<UnmappedEmployeeProjectDto> unmappedEmployeeDetails = listObjArray.stream()
					.map(UnmappedEmployeeProjectDto::unmappedEmployeeProject).collect(Collectors.toList());

			Map<Long, List<UnmappedEmployeeProjectDto>> deptIdAndEmployeeMap = unmappedEmployeeDetails.stream()
					.collect(Collectors.groupingBy(UnmappedEmployeeProjectDto::getDeptId));

			for (Map.Entry<Long, List<UnmappedEmployeeProjectDto>> deptIdAndEmployeeMapEntrySet : deptIdAndEmployeeMap
					.entrySet()) {

				List<UnmappedEmployeeProjectDto> employees = deptIdAndEmployeeMapEntrySet.getValue();
				if (employees == null || employees.isEmpty()) {
					continue;
				}

				String departmentName = employees.get(0).getDepartmentName();
				String subject = "Employee(s) Unmapped from Project.";
				String hodMail = employees.get(0).getHodMail();
				String htmlTable = createUnmappedEmployeeHtmlTable(employees, departmentName);

				try {
					mailService.sendMailWithCC(hodMail, "", subject, htmlTable);
				} catch (MessagingException e) {
					log.error("Error occured while sending mail to HOD : {} , Error : {} ", hodMail, e.getMessage());
				}

				employees.forEach(emp -> {
					try {
						mailService.sendMail(emp.getEmail(), subject, htmlTable);
					} catch (Exception e) {
						log.error("Error occured while sending mail to : {} , Error : {} ", emp.getEmail(),
								e.getMessage());
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateMemberShadowMapping(RmgTeamMemberDto teamMember) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (teamMember == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request cannot be null!!");
				return response;
			}
			if (teamMember.getEmpId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Emp Id cannot be null!!");
				return response;
			}
			if (teamMember.getUpdatedBy() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Current Emp Id cannot be null!!");
				return response;
			}
			if (teamMember.getEndDate() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Current End Date cannot be null!!");
				return response;
			}
			if (teamMember.getStartDate() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("New Start Date cannot be null!!");
				return response;
			}
			if (teamMember.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}
			if (teamMember.getTeamId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team Id cannot be null!!");
				return response;
			}

			Project project = projectRepository.findByProjectId(teamMember.getProjectId());
			if (project == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}

			Team team = teamRepository.findByTeamId(teamMember.getTeamId());
			if (team == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team not found!!");
				return response;
			}

			Long teamId = team.getTeamId();
			Long currentUserEmpId = teamMember.getUpdatedBy();

			EmployeeTeamMap existingMap = employeeTeamMapRepository
					.findByEmpIdAndTeamIdAndActiveStatus(teamMember.getEmpId(), teamId);
			if (existingMap == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Team Mapping not found!!");
				return response;
			}

			LocalDateTime now = LocalDateTime.now();
			if (!teamMember.getEndDate().toLocalDate().isAfter(now.toLocalDate())) {
				existingMap.setActive(0L);
			}
			existingMap.setEndDate(teamMember.getEndDate());
			existingMap.setUpdatedBy(currentUserEmpId);
			employeeTeamMapRepository.save(existingMap);

			EmployeeTeamMap empTeamMap = new EmployeeTeamMap();
			empTeamMap.setEmpId(teamMember.getEmpId());
			empTeamMap.setEmployeeRole(existingMap.getEmployeeRole());
			empTeamMap.setTeamId(teamId);
			empTeamMap
					.setStartDate(teamMember.getStartDate() != null ? teamMember.getStartDate() : LocalDateTime.now());
			empTeamMap.setActive(2L);
			empTeamMap.setIsShadow(teamMember.getIsShadow() != null ? teamMember.getIsShadow() : null);
			empTeamMap.setCreatedOn(new Timestamp(System.currentTimeMillis()));
			empTeamMap.setCreatedBy(currentUserEmpId);
			empTeamMap.setRoleId(existingMap.getRoleId());
			empTeamMap.setPoId(existingMap.getPoId());
			empTeamMap.setEmpTeamDepartmentId(existingMap.getEmpTeamDepartmentId());
			employeeTeamMapRepository.save(empTeamMap);

			response.setServiceResponse("Employee Project Mapping updated successfully!!");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			log.error("Error in updateMemberShadowMapping : ", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Transactional(readOnly = true)
	public ServiceResponse getTeamDetailsByProjectId(PoDetailsDto poDetailsDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (poDetailsDto == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request cannot be null!!");
				return response;
			}
			if (poDetailsDto.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}

			Project existingProject = projectRepository.findByProjectId(poDetailsDto.getProjectId());
			if (existingProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}

			List<RmgTeamMemberDto> teamMemberDetailsList = teamRepository.getAllTeamMemberDetailsDtoByProjectId(
					existingProject.getProjectId().longValue(), poDetailsDto.isActiveEtmFlag());
			if (teamMemberDetailsList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(new ArrayList<RmgTeamMemberDto>());
				return response;
			}

			List<Long> empIds = teamMemberDetailsList.stream().map(RmgTeamMemberDto::getEmpId)
					.collect(Collectors.toList());

			Map<Long, List<EmployeeOtherActiveProject>> empIdAndOtherProjectIdsMap = getEmployeeOtherActiveProjectIdMap(
					empIds, poDetailsDto.getProjectId());

			Map<Long, EmployeeInformationDTO> empIdInfoMap = getEmployeeInformationMap(empIds);

			for (RmgTeamMemberDto obj : teamMemberDetailsList) {

				EmployeeInformationDTO dto = empIdInfoMap.getOrDefault(obj.getEmpId(), null);
				if (dto != null) {
					obj.setEmpId(dto.getEmpId());
					obj.setEmployementId(dto.getEmploymentId());
					obj.setEmpTeamDepartmentId(dto.getDeptId());
					obj.setMemberDepartment(dto.getDeptName());
					obj.setJobRoleName(dto.getJobRole());
					obj.setBillableType(dto.getBillableType());
					obj.setPrevExp(dto.getPreviousExperience());
					obj.setCurrentExp(dto.getCurrentExperience());
					obj.setTotalExp(dto.getTotalExperience());
					obj.setEmploymentStatus(dto.getEmploymentStatus());
				}

				obj.setOtherActiveProjects(empIdAndOtherProjectIdsMap.getOrDefault(obj.getEmpId(), List.of()));
				if (obj.getOtherActiveProjects() != null && !obj.getOtherActiveProjects().isEmpty()) {
					List<Integer> projectIds = obj.getOtherActiveProjects().stream().map(e -> e.getProjectId())
							.collect(Collectors.toList());
					obj.setOtherActiveProjectIds(projectIds);
				} else {
					obj.setOtherActiveProjectIds(List.of());
				}
				obj.setDisplayRequirement(getDisplayRequirement(obj));
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(teamMemberDetailsList);
		} catch (Exception e) {
			log.error("Error in getTeamDetailsByTeamIdsAndProjectId : ", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong!!");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse extendTeamMembersEndDate(RmgTeamDto rmgTeamDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			response = validateExtendTeamMembersEndDateObject(rmgTeamDto);
			if (response != null && response.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
				return response;
			}
			response = new ServiceResponse();
			List<Long> etmIds = rmgTeamDto.getRmgMemberEndDateList().stream().map(RmgMemberEndDateDto::getEtmId)
					.filter(Objects::nonNull).collect(Collectors.toList());
			if (etmIds == null || etmIds.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("RMG Member(s) Employee Team Mapping Id(s) cannot be Null or Empty!!");
				return response;
			}

			Project project = projectRepository.findByProjectId(rmgTeamDto.getProjectId());
			if (project == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}

			List<EmployeeTeamMap> employeeTeamMappingList = employeeTeamMapRepository.findByEmployeeTeamMapIdIn(etmIds);
			if (employeeTeamMappingList == null || employeeTeamMappingList.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Team Mapping Not found!!");
				return response;
			}

			String sb = updateTeamMembersEndDateInETM(rmgTeamDto, employeeTeamMappingList);

			if (sb != null && !sb.isBlank()) {
				response.setServiceResponse(
						"Team Member(s) End Date updated successfully for selected records. \n However, some records could not be updated due to : \n"
								+ sb.toString());
			} else {
				response.setServiceResponse("Team Member(s) End Date updated Successfully!!");
			}

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			log.error("Error in extendTeamMembersEndDate : ", e);
			response.setServiceError(e.getMessage());
			response.setServiceResponse("Something went wrong!!");
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		}
		return response;
	}

	private String updateTeamMembersEndDateInETM(RmgTeamDto rmgTeamDto, List<EmployeeTeamMap> employeeTeamMappingList) {
		StringBuilder sb = new StringBuilder();

		Map<Long, RmgMemberEndDateDto> etmIdAndEtmMap = rmgTeamDto.getRmgMemberEndDateList().stream().collect(
				Collectors.toMap(RmgMemberEndDateDto::getEtmId, Function.identity(), (existing, replace) -> existing));

		for (EmployeeTeamMap employeeTeamMap : employeeTeamMappingList) {
			if (employeeTeamMap.getStartDate() == null) {
				sb.append("Start Date is null for EMP ID : ").append(employeeTeamMap.getEmpId())
						.append(", cannot update the End Date. \n");
				continue;
			}

			RmgMemberEndDateDto rmgMemberEndDateDto = etmIdAndEtmMap
					.getOrDefault(employeeTeamMap.getEmployeeTeamMapId(), null);

			if (rmgMemberEndDateDto != null) {
				if (employeeTeamMap.getStartDate().isAfter(rmgMemberEndDateDto.getEndDate())) {
					sb.append("Provided End Date is Before Start Date for EMP ID : ").append(employeeTeamMap.getEmpId())
							.append(", cannot update the End Date. \n");
				} else {
					employeeTeamMap.setEndDate(rmgMemberEndDateDto.getEndDate());
					employeeTeamMap.setUpdatedBy(rmgTeamDto.getUpdatedBy());
				}
			} else {
				sb.append("Employee not found for EMP ID : ").append(employeeTeamMap.getEmpId()).append(". \n");
			}
		}

		employeeTeamMapRepository.saveAll(employeeTeamMappingList);
		return sb.toString();
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateDefaultProjectCompletion(RmgTeamMemberDto teamMember) {
		ServiceResponse response = new ServiceResponse();
		try {
			response = validateUpdateDefaultProjectCompletionRequest(teamMember, response);
			if (response != null && response.getServiceStatus() != null
					&& response.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
				return response;
			}
			response = new ServiceResponse();

			Project project = projectRepository.findByProjectId(teamMember.getProjectId());
			if (project == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}

			Team team = teamRepository.findByTeamId(teamMember.getTeamId());
			if (team == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team not found!!");
				return response;
			}

			Long teamId = team.getTeamId();
			Long currentUserEmpId = teamMember.getUpdatedBy();

			List<Long> defaultProjectEmpIds = new ArrayList<>();
			List<RmgTeamMemberDto> teamMemberDtoList = new ArrayList<>();
			List<EmployeeTeamMap> newTeamMapList = new ArrayList<>();

			String employeeRoleString = teamMember.getEmployeeRoles().stream().map(String::valueOf)
					.collect(Collectors.joining(","));

			for (Long empId : teamMember.getSelectedEmpIds()) {
				EmployeeTeamMap existingMap = employeeTeamMapRepository.findByEmpIdAndTeamIdAndActiveStatus(empId,
						teamId);
				if (existingMap == null) {
					EmployeeTeamMap empTeamMap = new EmployeeTeamMap();
					empTeamMap.setEmpId(empId);
					empTeamMap.setEmployeeRole(employeeRoleString);
					empTeamMap.setTeamId(teamId);
					empTeamMap.setStartDate(teamMember.getStartDate() != null ? teamMember.getStartDate() : LocalDateTime.now());
					empTeamMap.setActive(2L);
					empTeamMap.setIsShadow(teamMember.getIsShadow() != null ? teamMember.getIsShadow() : null);
					empTeamMap.setCreatedBy(currentUserEmpId);
					empTeamMap.setRoleId(teamMember.getRoleId());
					empTeamMap.setPoId(teamMember.getPoId());
					empTeamMap.setEmpTeamDepartmentId(teamMember.getEmpTeamDepartmentId());
					if (teamMember.isDefaultProject()) {
						defaultProjectEmpIds.add(empId);
					}
					newTeamMapList.add(empTeamMap);
					teamMemberDtoList.add(new RmgTeamMemberDto(empId, teamMember.getEmployeeRoles()));
				}
			}

			List<EmployeeTeamMap> updatedMemberDbResponse = new ArrayList<>();
			if (!newTeamMapList.isEmpty()) {
				updatedMemberDbResponse = employeeTeamMapRepository.saveAll(newTeamMapList);
			}

			if (!defaultProjectEmpIds.isEmpty()) {
				updateEmployeeDefaultProject(defaultProjectEmpIds, project, currentUserEmpId, teamMember.getStartDate());
			}

			List<Long> newEmpIds = updatedMemberDbResponse.stream().map(e -> e.getEmpId()).distinct()
					.collect(Collectors.toList());

			if (newEmpIds != null && !newEmpIds.isEmpty()) {
				project.setIsDraftProject("true");
				projectRepository.save(project);
				String clientName = teamMember.getClientName();
				createActivityForEmployeeRole(teamId, currentUserEmpId, newEmpIds, teamMemberDtoList);
				sendProjectMappingEmailToEmployee(project.getProjectName(), clientName, newEmpIds);
			}
			response.setServiceResponse("Employee Project Mapping updated successfully!!");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			log.error("Error in updateDefaultProjectCompletion : ", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse removeTeamMembersFromProject(RmgTeamDto rmgTeamDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (rmgTeamDto == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request cannot be null!!");
				return response;
			}
			if (rmgTeamDto.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}
			if (rmgTeamDto.getUpdatedBy() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Current Emp Id cannot be null!!");
				return response;
			}
			if (rmgTeamDto.getRmgTeamMemberList() == null || rmgTeamDto.getRmgTeamMemberList().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team Member List cannot be null!!");
				return response;
			}

			Project project = projectRepository.findByProjectId(rmgTeamDto.getProjectId());
			if (project == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}

			Team team = teamRepository.findByTeamId(rmgTeamDto.getTeamId());
			if (team == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team not found!!");
				return response;
			}

			List<Long> empIds = rmgTeamDto.getRmgTeamMemberList().stream().map(emp -> emp.getEmpId())
					.collect(Collectors.toList());
			List<EmployeeTeamMap> empMappings = employeeTeamMapRepository.findByEmpIdInAndTeamIdAndActiveStatus(empIds,
					team.getTeamId());
			if (empMappings == null || empMappings.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Team Mapping not found!!");
				return response;
			}

			Map<Long, EmployeeTeamMap> empTeamMap = empMappings.stream().collect(
					Collectors.toMap(EmployeeTeamMap::getEmpId, Function.identity(), (existing, replace) -> replace));

			String removeTeamMemberMessage = handleRemoveTeamMembers(rmgTeamDto, project, team, empTeamMap);
			if (removeTeamMemberMessage != null && !removeTeamMemberMessage.isBlank()) {
				if (removeTeamMemberMessage.equals("Selected Employee(s) not Found!!")) {
					response.setServiceResponse("Selected Employee(s) not Found!!");
					response.setServiceStatus(ServiceResponse.STATUS_FAIL);
					return response;
				} else {
					response.setServiceResponse(
							"Team member(s) removed successfully. Members with a future end date will be removed on the specified date!! \n"
									+ removeTeamMemberMessage);
					response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
					return response;
				}
			}
			response.setServiceResponse(
					"Team member(s) removed successfully. Members with a future end date will be removed on the specified date!!");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			log.error("Error in removeTeamMembersFromProject : ", e);
			response.setServiceResponse("Something went wrong!!");
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
		}
		return response;
	}

	private String handleRemoveTeamMembers(RmgTeamDto rmgTeamDto, Project project, Team team,
			Map<Long, EmployeeTeamMap> empTeamMap) {
		StringBuilder sb = new StringBuilder();
		Long currentUserEmpId = rmgTeamDto.getUpdatedBy();

		List<Employee> empList = employeeRepository.findByEmpIdIn(rmgTeamDto.getRmgTeamMemberList().stream()
				.map(RmgTeamMemberDto::getEmpId).collect(Collectors.toList()));

		if (empList == null || empList.isEmpty()) {
			sb.append("Selected Employee(s) not Found!!");
			return sb.toString();
		}

		Map<Long, Employee> empIdAndEmployeeMap = empList.stream()
				.collect(Collectors.toMap(Employee::getEmpId, Function.identity()));

		for (RmgTeamMemberDto rmgTeamMember : rmgTeamDto.getRmgTeamMemberList()) {
			try {
				EmployeeTeamMap empTeamMapping = empTeamMap.get(rmgTeamMember.getEmpId());
				if (empTeamMapping == null) {
					sb.append("Employee Team Mapping not found for : ").append(rmgTeamMember.getEmpId()).append(" \n");
					continue;
				}

				Employee emp = empIdAndEmployeeMap.get(rmgTeamMember.getEmpId());
				if (emp == null) {
					sb.append("Employee not found for : ").append(empTeamMapping.getEmpId()).append(" \n");
					continue;
				}

				if (empTeamMapping.getStartDate() == null) {
					throw new IllegalArgumentException(
							"Member Start date cannot be null for EMP ID: " + emp.getEmpId());
				}

				empTeamMapping.setRescRemovedBy(currentUserEmpId);
				empTeamMapping.setIsCustomDate(rmgTeamDto.isCustomEndDate());
				empTeamMapping.setEndDate(rmgTeamDto.getEndDate());

				LocalDate today = LocalDate.now();
				LocalDate startDate = empTeamMapping.getStartDate().toLocalDate();
				LocalDate endDate = empTeamMapping.getEndDate() != null ? empTeamMapping.getEndDate().toLocalDate()
						: null;

				if (endDate != null && startDate.isAfter(endDate)) {
					throw new IllegalArgumentException("End date cannot be less than Start date: " + startDate);
				}

				// boolean startInFuture = startDate.isAfter(today);
				boolean endInPastOrToday = endDate != null && !endDate.isAfter(today);

				// Case 1: Start date in future → delete mapping
				// if (startInFuture) {
				// 	employeeTeamMapRepository.deleteById(empTeamMapping.getEmployeeTeamMapId());
				// 	log.info("Employee Team Mapping deleted for EMP ID : {}", rmgTeamMember.getEmpId());
				// 	continue;
				// }
				
				// Case 2: End date is past or today → deactivate
				if (endInPastOrToday) {
					empTeamMapping.setActive(0L);
				}
				// Case 3: No end date provided → remove immediately
				if (endDate == null) {
					empTeamMapping.setActive(0L);
					empTeamMapping.setEndDate(LocalDateTime.now());
				}

				employeeTeamMapRepository.save(empTeamMapping);
				sendResourceRemovalMailToRmg(emp.getName(), project.getProjectName(), team.getTeamName());
			} catch (Exception e) {
				log.error("Error in handleRemoveTeamMembers : ", e);
				sb.append("Employee Team Mapping not updated for : ").append(rmgTeamMember.getEmpId())
						.append(". Something went wrong. \n");
				;
			}
		}
		return sb.toString();
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateMappingToOtherProjectAsDefault(EmployeeOtherActiveProject empOtherActiveProject) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (empOtherActiveProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request cannot be null!!");
				return response;
			}
			if (empOtherActiveProject.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}
			if (empOtherActiveProject.getEmpId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Selected Employee Id cannot be null!!");
				return response;
			}
			if (empOtherActiveProject.getStartDate() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Member Start Date cannot be null!!");
				return response;
			}
			if (empOtherActiveProject.getUpdatedBy() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Updated By cannot be null!!");
				return response;
			}

			Project project = projectRepository.findByProjectId(empOtherActiveProject.getProjectId());
			if (project == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}

			log.info("Updating default project for empId={}, projectId={}", empOtherActiveProject.getEmpId(),
					empOtherActiveProject.getProjectId());

			List<Long> empIds = new ArrayList<>();
			empIds.add(empOtherActiveProject.getEmpId());
			updateEmployeeDefaultProject(empIds, project, empOtherActiveProject.getUpdatedBy(),
					empOtherActiveProject.getStartDate());
			response.setServiceResponse("Default Project updated Successfully!!");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);

			log.info("Successfully updated default project. empId={}, projectId={}", empOtherActiveProject.getEmpId(),
					empOtherActiveProject.getProjectId());
		} catch (Exception e) {
			log.error("Error in updateMappingToOtherProjectAsDefault : ", e);
			response.setServiceError(e.getMessage());
			response.setServiceResponse("Something went wrong!!");
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			throw e;
		}
		return response;
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse migrateTeamMembers(MigrateTeam migrateTeam) {
		ServiceResponse response = new ServiceResponse();
		try {
			// Validation
			response = validateMigrateTeamObject(migrateTeam);
			if (response != null && response.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
				return response;
			}
			response = new ServiceResponse();
			if (migrateTeam.getEmpIds() == null || migrateTeam.getEmpIds().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team Member Migration Emp Id(s) cannot be null!!");
				return response;
			}

			Project sourceProject = projectRepository.findByProjectId(migrateTeam.getSourceProjectId());
			if (sourceProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Source Project not found!!");
				return response;
			}
			Project targetProject = projectRepository.findByProjectId(migrateTeam.getTargetProjectId());
			if (targetProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Target Project not found!!");
				return response;
			}
			List<Team> activeTeams = teamRepository.findActiveTeamsByTeamIds(migrateTeam.getMigrationTeamIds());
			if (activeTeams == null || activeTeams.isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("No Active teams found for this Project");
				return response;
			}

			// Copy hasClientSideId from source -> target project
			targetProject.setHasClientSideId(sourceProject.getHasClientSideId());
			projectRepository.save(targetProject);

			// Project-level mappings copy
			copyAndSavePODepartmentMapping(migrateTeam); // PoDepartmentMapping
			copyAndSaveProjectManagerMapping(migrateTeam); // ProjectManagerMapping
			copyAndSaveProjectOverheadMapping(migrateTeam); // ProjectOverheadMapping

			response = copyAndSaveTeamAndEmployeeDetailsForMembersMigration(migrateTeam, activeTeams);
			if (response != null && response.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
				return response;
			}

			response = new ServiceResponse();
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(
					"Team Member Migration completed successfully for " + activeTeams.size() + " teams.");

			sendTeamMemberMigrationCompletedMail(migrateTeam, sourceProject, targetProject);
		} catch (Exception e) {
			log.error("Error in getTeamDetailsByTeamIdsAndProjectId : ", e);
			response = new ServiceResponse();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	private ServiceResponse copyAndSaveTeamAndEmployeeDetailsForMembersMigration(MigrateTeam migrateTeam,
			List<Team> activeSourceTeams) throws Exception {
		ServiceResponse serviceResponse = new ServiceResponse();

		Long srcProjectId = Long.parseLong(migrateTeam.getSourceProjectId().toString());
		Long tgtProjectId = Long.parseLong(migrateTeam.getTargetProjectId().toString());
		Long currentUserEmpId = migrateTeam.getCurrentUserEmpId();
		List<Long> migrationTeamIds = migrateTeam.getMigrationTeamIds();
		List<Long> migrationEmpIds = migrateTeam.getEmpIds();

		List<EmployeeTeamMap> sourceEmployeeTeamMapping = employeeTeamMapRepository
				.activeAndPendingEmployeesByTeamIdsAndEmpIds(migrationTeamIds, migrationEmpIds);
		if (sourceEmployeeTeamMapping == null || sourceEmployeeTeamMapping.isEmpty()) {
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("Selected Employee(s) not found in the Team Mapping!!");
			throw new Exception("Selected Employee(s) not found in the Team Mapping!!");
		}

		List<Activity> sourceTeamActivities = activitiesRepository.findByTeamIdIn(migrationTeamIds);

		if (sourceEmployeeTeamMapping != null && !sourceEmployeeTeamMapping.isEmpty()) {
			List<Long> sourceEmpIds = sourceEmployeeTeamMapping.stream().map(EmployeeTeamMap::getEmpId).distinct()
					.collect(Collectors.toList());

			List<Team> newTeams = copyAndSaveTeamForMembersMigration(currentUserEmpId, tgtProjectId, activeSourceTeams,
					migrateTeam);
			if (newTeams == null || newTeams.isEmpty()) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Failed to migrate team members to the Selected Project!!");
				throw new Exception("Failed to migrate members to the Selected Project!!");
			}

			// Build oldTeamId -> newTeam entity map
			Map<Long, Team> oldToNewTeamMap = newTeams.stream().filter(t -> t.getOldTeamId() != null)
					.collect(Collectors.toMap(Team::getOldTeamId, Function.identity()));

			// Mark old team mappings inactive
			markSourceTeamsEmployeeMappingInActive(sourceEmployeeTeamMapping, currentUserEmpId);
			copyAndSaveEmployeeTeamMapping(migrateTeam, currentUserEmpId, sourceEmployeeTeamMapping, oldToNewTeamMap);

			if (sourceEmpIds != null && !sourceEmpIds.isEmpty()) {
				boolean primaryMappingsChanged = false;
				List<EmpPrimaryProjectMapping> sourceEmpPrimaryProjectMapping = empPrimaryProjectMappingRepository
						.findByEmpIdInAndPrimaryProjectIdInAndIsMapped(sourceEmpIds, List.of(srcProjectId), "Y");

				if (sourceEmpPrimaryProjectMapping != null && !sourceEmpPrimaryProjectMapping.isEmpty()) {
					markSourceEmpPrimaryProjectMappingInActive(sourceEmpPrimaryProjectMapping, currentUserEmpId);
					primaryMappingsChanged = copyAndSaveEmpPrimaryProjectMapping(currentUserEmpId, tgtProjectId,
							sourceEmpPrimaryProjectMapping);
				}
				// update billable/billableType in employees
				if (primaryMappingsChanged) {
					updateEmployeeBillableType(migrateTeam.getTargetProjectId(), sourceEmpIds);
				}
			}

			// Mark old teams inactive only if no employees are remaining after migration
			markSourceTeamsInActiveForMembersMigration(migrateTeam, activeSourceTeams, currentUserEmpId);

			// Update Employee client-side ID mappings
			List<EmployeeClientSideIdMapping> oldClientSideIdMappings = employeeClientSideIdMappingRepository
					.findByEmpIdInAndProjectIdInAndActive(sourceEmpIds, List.of(srcProjectId), true);

			if (!oldClientSideIdMappings.isEmpty()) {
				markSourceClientSideIdMappingInActive(oldClientSideIdMappings, currentUserEmpId);
				copyAndSaveClientSideIdMapping(currentUserEmpId, tgtProjectId, oldClientSideIdMappings);
			}

			// Create New Activities
			copyAndSaveActivitiesForMembersMigration(currentUserEmpId, oldToNewTeamMap, sourceTeamActivities);
		}
		return null;
	}

	private List<Team> copyAndSaveTeamForMembersMigration(Long currentUserEmpId, Long tgtProjectId,
			List<Team> activeSourceTeams, MigrateTeam migrateTeam) {
		List<Team> newTeams = new ArrayList<>();

		for (Team oldTeam : activeSourceTeams) {
			Team exisingTeam = teamRepository.findByTeamNameAndProjectIdAndIsActive(oldTeam.getTeamName(),
					Integer.parseInt(tgtProjectId.toString()), "Y");
			if (exisingTeam != null) {
				exisingTeam.setOldTeamId(oldTeam.getTeamId());
				newTeams.add(exisingTeam);
				continue;
			}

			Team newTeam = new Team();
			newTeam.setTeamName(oldTeam.getTeamName());
			newTeam.setTeamLeadId(oldTeam.getTeamLeadId());
			newTeam.setProjectId(Integer.parseInt(tgtProjectId.toString()));
			newTeam.setTeamLeadName(oldTeam.getTeamLeadName());
			newTeam.setIsActive("Y");
			newTeam.setDescription(oldTeam.getDescription());
			newTeam.setDeptIds(oldTeam.getDeptIds());
			newTeam.setSpocId(oldTeam.getSpocId());
			newTeam.setCreatedBy(currentUserEmpId);
			newTeam.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
			newTeam.setOldTeamId(oldTeam.getTeamId());
			newTeams.add(newTeam);
		}
		return teamRepository.saveAll(newTeams);
	}

	private void markSourceTeamsInActiveForMembersMigration(MigrateTeam migrateTeam, List<Team> activeSourceTeams,
			Long currentUserEmpId) {
		List<Long> teamIds = activeSourceTeams.stream().map(Team::getTeamId).collect(Collectors.toList());
		List<EmployeeTeamMap> activeEtmMappings = employeeTeamMapRepository.findActiveByTeamIds(teamIds);
		Set<Long> currentTeamIdsWithActiveMembers = activeEtmMappings.stream().map(EmployeeTeamMap::getTeamId)
				.collect(Collectors.toSet());

		LocalDateTime now = LocalDateTime.now();
		activeSourceTeams.forEach(t -> {
			if (currentTeamIdsWithActiveMembers != null && !currentTeamIdsWithActiveMembers.isEmpty()
					&& !currentTeamIdsWithActiveMembers.contains(t.getTeamId())) {
				t.setIsActive("N");
				t.setUpdatedBy(currentUserEmpId);
				t.setUpdatedOn(now);
			}
		});
		teamRepository.saveAll(activeSourceTeams);
	}

	private void copyAndSaveActivitiesForMembersMigration(Long currentUserEmpId, Map<Long, Team> oldToNewTeamMap,
			List<Activity> sourceTeamActivities) {
		List<Activity> newActivities = new ArrayList<>();

		if (oldToNewTeamMap == null || oldToNewTeamMap.isEmpty()) {
			return;
		}

		List<Long> newTeamIds = oldToNewTeamMap.values().stream().map(Team::getTeamId).collect(Collectors.toList());
		Map<Long, Set<String>> teamIdAndEmployeeRoleMap = new HashMap<>();

		List<Activity> allNewTeamActivities = activitiesRepository.findByTeamIdIn(newTeamIds);
		if (allNewTeamActivities != null && !allNewTeamActivities.isEmpty()) {
			teamIdAndEmployeeRoleMap = allNewTeamActivities.stream().collect(Collectors.groupingBy(Activity::getTeamId,
					Collectors.mapping(Activity::getEmployeeRole, Collectors.toSet())));
		}

		for (Activity sourceActivity : sourceTeamActivities) {
			Team newTeam = oldToNewTeamMap.getOrDefault(sourceActivity.getTeamId(), null);
			if (newTeam != null) {
				Set<String> existingActivitiesEmployeeRole = teamIdAndEmployeeRoleMap.getOrDefault(newTeam.getTeamId(),
						Set.of());
				if (existingActivitiesEmployeeRole.contains(sourceActivity.getEmployeeRole())) {
					continue;
				}

				Activity newAct = new Activity();
				newAct.setTeamId(newTeam.getTeamId());
				newAct.setActivity(sourceActivity.getActivity());
				newAct.setEta(sourceActivity.getEta());
				newAct.setEmployeeRole(sourceActivity.getEmployeeRole());
				newAct.setDeptIds(sourceActivity.getDeptIds());
				CommonProperties cp = new CommonProperties();
				cp.setCreatedBy(currentUserEmpId);
				cp.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
				newAct.setCommonProperty(cp);
				newActivities.add(newAct);
				existingActivitiesEmployeeRole.add(sourceActivity.getEmployeeRole());
			}
		}
		if (!newActivities.isEmpty()) {
			activitiesRepository.saveAll(newActivities);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateTeamMembersStartDateAndEndDate(EmployeeProjectTimesheetDto employeeProjectTimesheetDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (employeeProjectTimesheetDto == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Request cannot be null!!");
				return response;
			}
			if (employeeProjectTimesheetDto.getUpdatedBy() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Current User Id cannot be null!!");
				return response;
			}
			if (employeeProjectTimesheetDto.getEmpId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Id cannot be null!!");
				return response;
			}
			if (employeeProjectTimesheetDto.getEtmId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Team Mapping Id cannot be null!!");
				return response;
			}
			if (employeeProjectTimesheetDto.getProjectId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Id cannot be null!!");
				return response;
			}
			if (employeeProjectTimesheetDto.getTeamId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Team Id cannot be null!!");
				return response;
			}
			if (employeeProjectTimesheetDto.getEmployeeTeamStartDate() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Team Start Date cannot be null!!");
				return response;
			}
			if (employeeProjectTimesheetDto.getEmployeeTeamEndDate() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Team End Date cannot be null!!");
				return response;
			}

			Project project = projectRepository.findByProjectId(employeeProjectTimesheetDto.getProjectId());
			if (project == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}

			EmployeeTeamMap employeeTeamMap = employeeTeamMapRepository.findByEmployeeTeamMapId(employeeProjectTimesheetDto.getEtmId());
			if (employeeTeamMap == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Team Mapping not found!!");
				return response;
			}

			employeeTeamMap.setStartDate(employeeProjectTimesheetDto.getEmployeeTeamStartDate().atStartOfDay());
			employeeTeamMap.setEndDate(employeeProjectTimesheetDto.getEmployeeTeamEndDate().atStartOfDay());
			employeeTeamMap.setUpdatedBy(employeeProjectTimesheetDto.getUpdatedBy());
			employeeTeamMap.setUpdatedOn(LocalDateTime.now());
			if(employeeProjectTimesheetDto.isRemovePermanently()){
				employeeTeamMap.setRescRemovedBy(employeeProjectTimesheetDto.getUpdatedBy());
			}
			EmployeeTeamMap newEmployeeTeamMap = employeeTeamMapRepository.save(employeeTeamMap);
			
			if (newEmployeeTeamMap != null && employeeProjectTimesheetDto.isRemovePermanently()) {
				employeeTeamMapRepository.deleteById(newEmployeeTeamMap.getEmployeeTeamMapId());
			}

			response.setServiceResponse("Team Members Start Date and End Date updated successfully!!");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			return response;
		} catch (Exception e) {
			log.error("Error in updateTeamMembersStartDateAndEndDate : ", e);
			response.setServiceResponse("Something went wrong, unable to update the Employee details at the moment!!");
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceError(e.getMessage());
			return response;
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public ServiceResponse updateEmployeeProjectMappingAsInActive(RmgTeamMemberDto rmgTeamMemberDto) {
		ServiceResponse serviceResponse = new ServiceResponse();
		try {
			if (rmgTeamMemberDto == null) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Request cannot be null!!");
				return serviceResponse;
			}
			if (rmgTeamMemberDto.getEtmId() == null) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Employee Team Mapping Id cannot be null!!");
				return serviceResponse;
			}
			Optional<EmployeeTeamMap> empTeamMapOpt = employeeTeamMapRepository.findById(rmgTeamMemberDto.getEtmId());
			if (empTeamMapOpt.isEmpty()) {
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
				serviceResponse.setServiceResponse("Employee Team Mapping not found!!");
				return serviceResponse;
			}
			EmployeeTeamMap empTeamMap = empTeamMapOpt.get();

			if (empTeamMap != null) {
				empTeamMap.setRescRemovedBy(rmgTeamMemberDto.getRescRemovedBy());
				empTeamMap.setUpdatedBy(rmgTeamMemberDto.getUpdatedBy());
				empTeamMap.setUpdatedOn(LocalDateTime.now());
				empTeamMap.setIsCustomDate(rmgTeamMemberDto.isCustomDate());
				empTeamMap.setEndDate(rmgTeamMemberDto.getRescEndDate());
				if (rmgTeamMemberDto.getRescEndDate() == null) {
					empTeamMap.setEndDate(LocalDateTime.now());
				}
				LocalDateTime now = LocalDateTime.now();
				if (!rmgTeamMemberDto.getRescEndDate().toLocalDate().isAfter(now.toLocalDate())) {
					empTeamMap.setActive(0l);
				}
				if(rmgTeamMemberDto.isRemovePermanently()){
					empTeamMap.setRescRemovedBy(rmgTeamMemberDto.getUpdatedBy());
				}

				EmployeeTeamMap newEmployeeTeamMap = employeeTeamMapRepository.save(empTeamMap);
				if (newEmployeeTeamMap != null && rmgTeamMemberDto.isRemovePermanently()) {
					employeeTeamMapRepository.deleteById(newEmployeeTeamMap.getEmployeeTeamMapId());
				}
				serviceResponse.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				serviceResponse.setServiceResponse("Resource removed successfully!!");
				log.info("Resource Removed Successfully : ETM_ID={}, REMOVED_BY={}",empTeamMap.getEmployeeTeamMapId(), rmgTeamMemberDto.getUpdatedBy());
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error updating Employee Project Mapping to Inactive : ", e);
			serviceResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
			serviceResponse.setServiceResponse("Something went wrong!!");
			throw e;
		}
		return serviceResponse;
	}

	@Transactional(readOnly = true)
	public ServiceResponse validateEmployeeProjectStartDate(RmgTeamMemberDto rmgTeamMemberDto) {
		ServiceResponse response = new ServiceResponse();
		try {
			response = validateEmployeeProjectStartDateObject(rmgTeamMemberDto, false);
			if (response != null && response.getServiceStatus().equals(ServiceResponse.STATUS_FAIL)) {
				return response;
			}
			response = new ServiceResponse();

			if (rmgTeamMemberDto.getEmpId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Employee Id cannot be null!!");
				return response;
			}

			Project currentProject = projectRepository.findByProjectId(rmgTeamMemberDto.getProjectId());
			if (currentProject == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project not found!!");
				return response;
			}
			if (currentProject.getStartDate() == null || currentProject.getStartDate().trim().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Project Start Date is null, Please contact Admin!!");
				return response;
			}

			LocalDate date = LocalDate.parse(currentProject.getStartDate().toString());
			if (rmgTeamMemberDto.getStartDate().toLocalDate().isBefore(date)) {
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse("PROJECT_START_DATE_LESS_THAN_MEMBER_START_DATE");
				return response;
			}

			if (rmgTeamMemberDto.getProjectIds() == null || rmgTeamMemberDto.getProjectIds().isEmpty()) {
				List<Integer> temp = new ArrayList<>();
				temp.add(currentProject.getProjectId());
				rmgTeamMemberDto.setProjectIds(temp);
			}
			
			List<Integer> projectIds = rmgTeamMemberDto.getProjectIds().stream().filter(Objects::nonNull).collect(Collectors.toList());
			if (projectIds.isEmpty()) {
				projectIds.add(currentProject.getProjectId());
			}

			response = validateEmployeeExistingProjectOverlapWithStartDate(rmgTeamMemberDto.getEmpId(),
					rmgTeamMemberDto.getStartDate(), rmgTeamMemberDto.getProjectType(), projectIds);
			if (response != null && response.getServiceResponse() != null
					&& (response.getServiceResponse().equals("OTHER_TNM_PROJECT_OVERLAPPING")
							|| response.getServiceResponse().equals("CURRENT_TNM_PROJECT_OVERLAPPING"))) {
				return response;
			}

			response = validateExistingEmployeeProjectTimesheet(rmgTeamMemberDto.getEmpId(),
					rmgTeamMemberDto.getStartDate(), projectIds);
			if (response != null && response.getServiceResponse() != null
					&& response.getServiceResponse().equals("CONFLICTING_TIMESHEET_RECORDS_FOUND")) {
				return response;
			}

			response = validateEmployeeProjectUnmappedCount(rmgTeamMemberDto.getEmpId(),
					rmgTeamMemberDto.getStartDate(), projectIds);
			if (response != null && response.getServiceResponse() != null
					&& response.getServiceResponse().equals("GAP_EXISTS")) {
				return response;
			}

			response.setServiceResponse("NO_CONFLICT");
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		} catch (Exception e) {
			log.error("Error in validateEmployeeTimesheetFilledToChangeStartDate : ", e);
			response.setServiceResponse(
					"Something went wrong, unable to validate the selected start date at the moment!!");
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	private ServiceResponse validateEmployeeExistingProjectOverlapWithStartDate(Long empId, LocalDateTime startDate,
			String projectType, List<Integer> projectIds) {
		ServiceResponse response = new ServiceResponse();
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);

		List<EmployeeProjectTimesheetDto> employeeProjectTimesheetDtoList = employeeTeamMapRepository
				.findByEmpIdAndDate(empId, startDate, projectIds);
		if (employeeProjectTimesheetDtoList == null || employeeProjectTimesheetDtoList.isEmpty()) {
			response.setServiceResponse("NO_OVERLAPPING_PROJECTS_FOUND");
			return response;
		}

		if ("TNM".equals(projectType)) {
			response.setServiceResponse("CURRENT_TNM_PROJECT_OVERLAPPING");
			response.setServiceResponse2(employeeProjectTimesheetDtoList);
			return response;
		}

		List<EmployeeProjectTimesheetDto> filteredEmployeeProjectTimesheetDtoList = employeeProjectTimesheetDtoList
				.stream().filter(e -> "TNM".equals(e.getProjectType())).collect(Collectors.toList());
		if (filteredEmployeeProjectTimesheetDtoList != null && !filteredEmployeeProjectTimesheetDtoList.isEmpty()) {
			response.setServiceResponse("OTHER_TNM_PROJECT_OVERLAPPING");
			response.setServiceResponse2(filteredEmployeeProjectTimesheetDtoList);
			return response;
		}

		response.setServiceResponse("NO_CONFLICT");
		return response;
	}

	private ServiceResponse validateExistingEmployeeProjectTimesheet(Long empId, LocalDateTime startDate,
			List<Integer> projectIds) {
		ServiceResponse response = new ServiceResponse();
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);

		List<EmployeeProjectTimesheetDto> employeeProjectTimesheetDtoList = timesheetsRepository
				.findByEmpIdAndDate(empId, startDate, projectIds);
		if (employeeProjectTimesheetDtoList == null || employeeProjectTimesheetDtoList.isEmpty()) {
			response.setServiceResponse("NO_TIMESHEET_RECORDS_FOUND");
			return response;
		}

		LocalDate newStartDate = startDate.toLocalDate();
		List<EmployeeProjectTimesheetDto> filteredEmployeeProjectTimesheetDtoList = new ArrayList<EmployeeProjectTimesheetDto>();
		for (EmployeeProjectTimesheetDto dto : employeeProjectTimesheetDtoList) {
			if (dto.getProjectStartDate() == null || dto.getTimesheetFilledCount() == null
					|| dto.getTimesheetFilledCount().equals(0l)) {
				continue;
			}
			if (isStartDateConflict(newStartDate, dto.getProjectStartDate(), dto.getEmployeeTeamStartDate())) {
				filteredEmployeeProjectTimesheetDtoList.add(dto);
			}
		}
		if (filteredEmployeeProjectTimesheetDtoList != null && !filteredEmployeeProjectTimesheetDtoList.isEmpty()) {
			response.setServiceResponse("CONFLICTING_TIMESHEET_RECORDS_FOUND");
			response.setServiceResponse2(filteredEmployeeProjectTimesheetDtoList);
			return response;
		}

		response.setServiceResponse("NO_CONFLICT");
		return response;
	}

	private ServiceResponse validateEmployeeProjectUnmappedCount(Long empId, LocalDateTime startDate,
			List<Integer> projectIds) {
		ServiceResponse response = new ServiceResponse();
		response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
		List<Object[]> objArrList = employeeTeamMapRepository.getUnmappedEmployeeProjectDate(empId, startDate,
				projectIds);
		if (objArrList != null && !objArrList.isEmpty() && objArrList.get(0) != null) {
			Object[] objArr = objArrList.get(0);
			EmployeeProjectTimesheetDto dto = new EmployeeProjectTimesheetDto();
			dto.setEmpId(TypeConversionUtil.safeParseLong(objArr[0]));
			dto.setEmployeeTeamStartDate(objArr[1] != null ? LocalDate.parse(objArr[1].toString()) : null);
			dto.setEmployeeTeamEndDate(objArr[2] != null ? LocalDate.parse(objArr[2].toString()) : null);
			response.setServiceResponse("GAP_EXISTS");
			response.setServiceResponse2(dto);
			return response;
		}

		response.setServiceResponse("NO_CONFLICT");
		return response;
	}

	public Map<Long, EmployeeInformationDTO> getEmployeeInformationMap(List<Long> empIds) {
		Map<Long, EmployeeInformationDTO> empIdInfoMap = new HashMap<>();
		List<Object[]> results = employeeRepository.getEmployeeInformationIn(empIds);
		if (results != null && !results.isEmpty()) {
			for (Object[] obj : results) {
				Long empId = TypeConversionUtil.safeParseLong(obj[0]);
				EmployeeInformationDTO dto = new EmployeeInformationDTO();
				dto.setEmpId(empId);
				dto.setEmploymentId(TypeConversionUtil.getSafeString(obj[1]));
				dto.setName(TypeConversionUtil.getSafeString(obj[2]));
				dto.setPreviousExperience(TypeConversionUtil.getSafeString(obj[3]));
				dto.setCurrentExperience(TypeConversionUtil.getSafeString(obj[4]));
				dto.setTotalExperience(TypeConversionUtil.getSafeString(obj[5]));
				dto.setBillableType(TypeConversionUtil.getSafeString(obj[6]));
				dto.setJobRole(TypeConversionUtil.getSafeString(obj[7]));
				dto.setDeptId(TypeConversionUtil.safeParseLong(obj[8]));
				dto.setDeptName(TypeConversionUtil.getSafeString(obj[9]));
				dto.setEmploymentStatus(TypeConversionUtil.getSafeString(obj[10]));
				empIdInfoMap.put(empId, dto);
			}
		}
		return empIdInfoMap;
	}

	public Map<Long, List<EmployeeOtherActiveProject>> getEmployeeOtherActiveProjectIdMap(List<Long> empIds,
			Integer projectId) {
		List<EmployeeOtherActiveProject> empOtherActiveProjectList = projectRepository
				.getOtherActiveProjectsByEmpIdIn(empIds, projectId);
		if (empOtherActiveProjectList == null || empOtherActiveProjectList.isEmpty()) {
			return new HashMap<>();
		}
		return empOtherActiveProjectList.stream().collect(Collectors.groupingBy(EmployeeOtherActiveProject::getEmpId));
	}

	public void markSourceClientSideIdMappingInActive(List<EmployeeClientSideIdMapping> oldClientSideIdMappings,
			Long currentUserEmpId) {
		oldClientSideIdMappings.forEach(m -> {
			m.setActive(false);
			m.setUpdatedOn(LocalDateTime.now());
			m.setUpdatedBy(currentUserEmpId);
		});
		employeeClientSideIdMappingRepository.saveAll(oldClientSideIdMappings);
	}

	public void copyAndSaveClientSideIdMapping(Long currentUserEmpId, Long tgtProjectId,
			List<EmployeeClientSideIdMapping> oldClientSideIdMappings) {
		List<EmployeeClientSideIdMapping> newClientSideIdMappings = new ArrayList<>();

		for (EmployeeClientSideIdMapping oldMap : oldClientSideIdMappings) {
			EmployeeClientSideIdMapping newMap = new EmployeeClientSideIdMapping();
			newMap.setClientSideId(oldMap.getClientSideId());
			newMap.setEmpId(oldMap.getEmpId());
			newMap.setProjectId(tgtProjectId);
			newMap.setActive(true);
			newMap.setCreatedBy(currentUserEmpId);
			newMap.setCreatedOn(LocalDateTime.now());
			newClientSideIdMappings.add(newMap);
		}
		if (!newClientSideIdMappings.isEmpty()) {
			newClientSideIdMappings = employeeClientSideIdMappingRepository.saveAll(newClientSideIdMappings);
		}
	}

	public void markSourceTeamsEmployeeMappingInActive(List<EmployeeTeamMap> sourceEmployeeTeamMapping,
			Long currentUserEmpId) {
		sourceEmployeeTeamMapping.forEach(etm -> {
			etm.setActive(0L);
			etm.setUpdatedOn(LocalDateTime.now());
			etm.setUpdatedBy(currentUserEmpId);
		});
		employeeTeamMapRepository.saveAll(sourceEmployeeTeamMapping);
	}

	public List<EmployeeTeamMap> copyAndSaveEmployeeTeamMapping(MigrateTeam migrateTeam, Long currentUserEmpId,
			List<EmployeeTeamMap> sourceEmployeeTeamMapping, Map<Long, Team> oldTeamIdNewTeamMap) {
		List<EmployeeTeamMap> newEmployeeTeamMapping = new ArrayList<>();

		for (EmployeeTeamMap oldMap : sourceEmployeeTeamMapping) {
			Team mappedNewTeam = oldTeamIdNewTeamMap.get(oldMap.getTeamId());

			if (mappedNewTeam != null) {
				EmployeeTeamMap newMap = new EmployeeTeamMap();
				newMap.setEmpId(oldMap.getEmpId());
				newMap.setTeamId(mappedNewTeam.getTeamId());
				newMap.setJobRoleId(oldMap.getJobRoleId());
				newMap.setActive(1L);
				newMap.setStartDate(LocalDateTime.now());
				newMap.setEmployeeRole(oldMap.getEmployeeRole());
				newMap.setEndDate(null);
				newMap.setIsShadow(oldMap.getIsShadow());
				newMap.setPoId(migrateTeam.getTargetPoId());
				newMap.setRoleId(migrateTeam.getTargetRoleId());
				newMap.setCreatedBy(currentUserEmpId);
				newMap.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
				newMap.setUpdatedBy(null);
				newMap.setUpdatedOn(null);
				newEmployeeTeamMapping.add(newMap);
			}
		}
		if (!newEmployeeTeamMapping.isEmpty()) {
			newEmployeeTeamMapping = employeeTeamMapRepository.saveAll(newEmployeeTeamMapping);
		}
		return newEmployeeTeamMapping;
	}

	public void markSourceEmpPrimaryProjectMappingInActive(
			List<EmpPrimaryProjectMapping> sourceEmpPrimaryProjectMapping, Long currentUserEmpId) {
		sourceEmpPrimaryProjectMapping.forEach(m -> {
			m.setIsMapped("N");
			m.setUpdatedOn(LocalDateTime.now());
			m.setUpdatedBy(currentUserEmpId);
		});
		empPrimaryProjectMappingRepository.saveAll(sourceEmpPrimaryProjectMapping);
	}

	public boolean copyAndSaveEmpPrimaryProjectMapping(Long currentUserEmpId, Long tgtProjectId,
			List<EmpPrimaryProjectMapping> sourceEmpPrimaryProjectMapping) {
		boolean flag = false;
		List<EmpPrimaryProjectMapping> newPrimaryMappings = new ArrayList<>();
		for (EmpPrimaryProjectMapping oldMap : sourceEmpPrimaryProjectMapping) {
			EmpPrimaryProjectMapping newMap = new EmpPrimaryProjectMapping();
			newMap.setEmpId(oldMap.getEmpId());
			newMap.setPrimaryProjectId(tgtProjectId);
			newMap.setPrimaryProjectName(oldMap.getPrimaryProjectName());
			newMap.setIsMapped("Y");
			newMap.setUpdatedBy(currentUserEmpId);
			newMap.setUpdatedOn(LocalDateTime.now());
			newPrimaryMappings.add(newMap);
		}
		if (!newPrimaryMappings.isEmpty()) {
			newPrimaryMappings = empPrimaryProjectMappingRepository.saveAll(newPrimaryMappings);
			flag = true;
		}
		return flag;
	}

	public List<PoDepartmentMapping> copyAndSavePODepartmentMapping(MigrateTeam migrateTeam) {
		Integer srcProjectId = migrateTeam.getSourceProjectId();
		Integer tgtProjectId = migrateTeam.getTargetProjectId();
		List<PoDepartmentMapping> migratedPoDepartmentMapping = new ArrayList<>();

		List<PoDepartmentMapping> sourceDeptMappings = poDepartmentMappingRepository
				.findByProjectIdAndActive(srcProjectId);
		List<Long> targetDeptIds = poDepartmentMappingRepository.findPoDeptIdsByProjectId(tgtProjectId, false);

		for (PoDepartmentMapping s : sourceDeptMappings) {
			if (!targetDeptIds.contains(s.getDeptId())) {
				PoDepartmentMapping nm = new PoDepartmentMapping();
				nm.setPoId(migrateTeam.getTargetPoId());
				nm.setDeptId(s.getDeptId());
				nm.setActive(true);
				migratedPoDepartmentMapping.add(nm);
			}
		}
		if (!migratedPoDepartmentMapping.isEmpty()) {
			migratedPoDepartmentMapping = poDepartmentMappingRepository.saveAll(migratedPoDepartmentMapping);
		}
		return migratedPoDepartmentMapping;
	}

	public List<ProjectManagerMapping> copyAndSaveProjectManagerMapping(MigrateTeam migrateTeam) {
		Long srcProjectId = Long.parseLong(migrateTeam.getSourceProjectId().toString());
		Long tgtProjectId = Long.parseLong(migrateTeam.getTargetProjectId().toString());
		Long currentUserEmpId = migrateTeam.getCurrentUserEmpId();
		List<ProjectManagerMapping> migratedProjectManagerMappingList = new ArrayList<>();

		List<ProjectManagerMapping> sourceManagerMappings = projectManagerMappingRepository
				.findByProjectIdAndActive(srcProjectId, 1);
		List<Long> targetManagerIds = projectManagerMappingRepository
				.findProjectManagerIdByProjectIdAndActive(tgtProjectId, 1);

		for (ProjectManagerMapping s : sourceManagerMappings) {
			if (!targetManagerIds.contains(s.getProjectManagerId())) {
				ProjectManagerMapping nm = new ProjectManagerMapping();
				nm.setProjectId(tgtProjectId);
				nm.setProjectManagerId(s.getProjectManagerId());
				nm.setActive(1);
				nm.setCreatedBy(currentUserEmpId);
				nm.setCreatedOn(new Timestamp(System.currentTimeMillis()));
				migratedProjectManagerMappingList.add(nm);
			}
		}
		if (!migratedProjectManagerMappingList.isEmpty()) {
			migratedProjectManagerMappingList = projectManagerMappingRepository
					.saveAll(migratedProjectManagerMappingList);
		}
		return migratedProjectManagerMappingList;
	}

	public List<ProjectOverheadMapping> copyAndSaveProjectOverheadMapping(MigrateTeam migrateTeam) {
		Long srcProjectId = Long.parseLong(migrateTeam.getSourceProjectId().toString());
		Long tgtProjectId = Long.parseLong(migrateTeam.getTargetProjectId().toString());
		Long currentUserEmpId = migrateTeam.getCurrentUserEmpId();
		List<ProjectOverheadMapping> migratedProjectOverheadMappingList = new ArrayList<>();

		List<ProjectOverheadMapping> sourceOverheadMappings = projectOverheadMappingRepository
				.findByProjectIdAndActive(srcProjectId, 1);

		List<Long> targetOverheadIds = projectOverheadMappingRepository
				.findProjectOverheadIdByProjectIdAndActive(tgtProjectId, 1);

		for (ProjectOverheadMapping s : sourceOverheadMappings) {
			if (!targetOverheadIds.contains(s.getProjectOverheadId())) {
				ProjectOverheadMapping nm = new ProjectOverheadMapping();
				nm.setProjectId(tgtProjectId);
				nm.setProjectOverheadId(s.getProjectOverheadId());
				nm.setActive(1);
				nm.setCreatedBy(currentUserEmpId);
				nm.setCreatedOn(new Timestamp(System.currentTimeMillis()));
				migratedProjectOverheadMappingList.add(nm);
			}
		}
		if (!migratedProjectOverheadMappingList.isEmpty()) {
			migratedProjectOverheadMappingList = projectOverheadMappingRepository
					.saveAll(migratedProjectOverheadMappingList);
		}
		return migratedProjectOverheadMappingList;
	}

//	Mail Method Start

	private void sendProjectMappingEmailToEmployee(String projectName, String clientName, List<Long> empIds) {
		List<Object[]> empMaildsForProjectMappingList = employeeRepository.findMailIdsForProjectMappingByEmpIds(empIds);
		for (Object[] object : empMaildsForProjectMappingList) {
			String empName = TypeConversionUtil.getSafeString(object[1]);
			String empEmail = TypeConversionUtil.getSafeString(object[2]);
			String managerEmail = TypeConversionUtil.getSafeString(object[3]);
			String hodEmail = TypeConversionUtil.getSafeString(object[4]);

			String ccMail = hodEmail + "," + managerEmail + "," + rmgMail + "," + adminMail;
			try {
				mailService.sendMailWithCC(empEmail, ccMail, "Regarding resource mapping to new project",
						"Dear " + empName + "<br>" + "You have been mapped to client name - " + clientName
								+ " under the project " + projectName + "<br><br><br>"
								+ "Sincerely,<br>Team RMG - ApMoSys Technologies");
			} catch (AddressException e) {
				log.error("Error in sendProjectMappingEmailToEmployee : ", e);
			} catch (MessagingException e) {
				log.error("Error in sendProjectMappingEmailToEmployee : ", e);
			}
		}
	}

	private void sendTeamCreationEmail(Team team, List<EmployeeTeamMap> mappings) {
		StringBuilder emailBody = new StringBuilder();
		emailBody.append("<html><body>").append("Dear RMG,<br><br>")
				.append("The Team has been created with the team name - <b>").append(team.getTeamName())
				.append("</b><br><br>").append("<table border='1' style='border-collapse: collapse; width: 100%;'>")
				.append("<tr>").append("<th style='padding: 8px;'>Employment ID</th>")
				.append("<th style='padding: 8px;'>Employee Name</th>")
				.append("<th style='padding: 8px;'>Job Role</th>").append("<th style='padding: 8px;'>Department</th>")
				.append("<th style='padding: 8px;'>Start Date</th>").append("</tr>");

		for (EmployeeTeamMap mapping : mappings) {
			List<EmployeeDetailsForTeamMemberDTO> employeeDetails = employeeRepository
					.getEmployeeDetailsForTeam(mapping.getEmpId());
			for (EmployeeDetailsForTeamMemberDTO dto : employeeDetails) {
				String empIdPrefix = "A-";
				if ("true".equalsIgnoreCase(dto.getIsConsultant())) {
					empIdPrefix = "CS-";
				}

				emailBody.append("<tr>").append("<td>").append(empIdPrefix).append(dto.getEmployeementId())
						.append("</td>").append("<td>").append(dto.getEmployeeName()).append("</td>").append("<td>")
						.append(dto.getJobRoleName()).append("</td>").append("<td>").append(dto.getDeptName())
						.append("</td>").append("<td>").append(dto.getStartDate()).append("</td>").append("</tr>");
			}
		}

		emailBody.append("</table><br><br>").append("Sincerely,<br><b>Team RMG - ApMoSys Technologies</b>")
				.append("</body></html>");

		try {
			String ccMails = getAllHodMails(mappings);
			mailService.sendMailWithCC(ccMails, rmgMail, "Regarding Team Creation", emailBody.toString());
		} catch (MessagingException e) {
			log.error("Error in sendTeamCreationEmail : ", e);
		}
	}

	private String createUnmappedEmployeeHtmlTable(List<UnmappedEmployeeProjectDto> unmappedEmployeeList,
			String departmentName) {
		StringBuilder html = new StringBuilder();
		html.append("<html>").append("<head><style>").append("table, th, td { border: 1px solid black; }")
				.append("table { border-collapse: collapse; }").append("</style></head>").append("<body>");

		html.append("<p>Dear Team,");
		html.append("<p>The following unmapped project details were identified:</p>");
		html.append("<table border='1' style='border-collapse:collapse;padding:8px'>");
		html.append("<tr>").append("<th>Employee ID</th>").append("<th>Name</th>").append("<th>Email</th>")
				.append("<th>Manager Name</th>").append("<th>Unmapped Start Date</th>")
				.append("<th>Unmapped End Date</th>").append("<th>Unmapped Days</th>").append("<th>Department</th>")
				.append("</tr>");

		for (UnmappedEmployeeProjectDto emp : unmappedEmployeeList) {
			html.append("<tr>").append("<td>").append(emp.getEmploymentIdStr()).append("</td>").append("<td>")
					.append(emp.getName()).append("</td>").append("<td>").append(emp.getEmail()).append("</td>")
					.append("<td>").append(emp.getReportingManagerName()).append("</td>").append("<td>")
					.append(emp.getUnmapStartDate()).append("</td>").append("<td>").append(emp.getUnmapEndDate())
					.append("</td>").append("<td>").append(emp.getUnmappedDaysCount()).append("</td>").append("<td>")
					.append(departmentName).append("</td>").append("</tr>");
		}
		html.append("</table>");
		html.append("<br>");
		html.append("<p>Please contact your reporting manager for project allocation.</p>");
		html.append("<br>");
		html.append("<p>Regards,<br>ApMoSys Technologies</p>");
		html.append("</body></html>");
		return html.toString();
	}

	private void sendResourceRemovalMailToRmg(String empName, String projectName, String teamName) {
		try {
			mailService.sendMail(rmgMail, "Regarding Resource removed from Project ",
					"Dear " + empName + "<br>" + "You have been removed from project " + projectName
							+ "under the team - " + teamName + "<br>" + "<br><br>" + "Sincerely," + "<br>"
							+ "Team RMG - ApMoSys Technologies");
		} catch (AddressException e) {
			log.error("Error in sendResourceRemovalMailToRmg : ", e);
		} catch (MessagingException e) {
			log.error("Error in sendResourceRemovalMailToRmg : ", e);
		}
	}

	private void sendTeamMemberMigrationCompletedMail(MigrateTeam migrateTeam, Project sourceProject,
			Project targetProject) throws Exception {
		try {
			List<Long> migrationEmpIds = migrateTeam.getEmpIds();
			Employee updatedByemp = employeeRepository.findByEmpId(migrateTeam.getCurrentUserEmpId());

			Set<String> toRecipients = new HashSet<>();
			toRecipients.add(bdMail);
			toRecipients.add(adminMail);
			toRecipients.add(rmgMail);
			toRecipients.add(financeMail);

			List<String> projManagerOverheadHODMails = projectRepository
					.findProjectManagerAndProjectoverheadEmails(targetProject.getProjectId());

			Set<String> ccRecipients = projManagerOverheadHODMails.stream().filter(Objects::nonNull).map(String::trim)
					.filter(s -> !s.isEmpty()).collect(Collectors.toCollection(LinkedHashSet::new));
			ccRecipients.removeAll(toRecipients);

			List<Employee> employees = employeeRepository.findByEmpIdIn(migrationEmpIds);

			List<String> empNames = employees.stream().map(Employee::getName).filter(Objects::nonNull)
					.collect(Collectors.toList());

			String currentDate = LocalDate.now().toString();
			String empNamesStr = String.join(", ", empNames);

			String subject = "Team Member Migration from " + sourceProject.getProjectName() + " to "
					+ targetProject.getProjectName();

			StringBuilder body = new StringBuilder();
			body.append("The ").append(empNames.size() == 1 ? "team member" : "team members").append("<b>")
					.append(empNamesStr).append(empNames.size() == 1 ? "has" : "have").append(" been migrated from <b>")
					.append(sourceProject.getProjectName()).append("</b> to <b>").append(targetProject.getProjectName())
					.append("</b> by <b>").append((updatedByemp != null ? updatedByemp.getName() : "System"))
					.append("</b> on ").append(currentDate).append(".");

			mailService.sendMailWithCC(String.join(",", toRecipients), String.join(",", ccRecipients), subject,
					body.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//  Validation Method Start

	private ServiceResponse validateExtendTeamMembersEndDateObject(RmgTeamDto rmgTeamDto) {
		ServiceResponse response = new ServiceResponse();
		if (rmgTeamDto == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Request cannot be null!!");
			return response;
		}
		if (rmgTeamDto.getProjectId() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Project Id cannot be null!!");
			return response;
		}
		if (rmgTeamDto.getUpdatedBy() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Current User Employee Id cannot be null!!");
			return response;
		}
		if (rmgTeamDto.getRmgMemberEndDateList() == null || rmgTeamDto.getRmgMemberEndDateList().isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("RMG Member(s) End Date List cannot be Null or Empty!!");
			return response;
		}

		for (RmgMemberEndDateDto dto : rmgTeamDto.getRmgMemberEndDateList()) {
			if (dto.getEndDate() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Provided End Date cannot be null for EMP ID : " + dto.getEmpId());
				return response;
			}
		}
		return null;
	}

	public ServiceResponse validateMigrateTeamObject(MigrateTeam migrateTeam) {
		ServiceResponse response = new ServiceResponse();
		if (migrateTeam == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Team Migration Object cannot be null!!");
			return response;
		}
		if (migrateTeam.getCurrentUserEmpId() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Employee Id cannot be null!!");
			return response;
		}
		if (migrateTeam.getSourceProjectId() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Source Project Id cannot be null!!");
			return response;
		}
		if (migrateTeam.getMigrationTeamIds() == null || migrateTeam.getMigrationTeamIds().isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Team Migration Ids cannot be null!!");
			return response;
		}
		if (migrateTeam.getTargetProjectId() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Target Project Id cannot be null!!");
			return response;
		}
		if (migrateTeam.getTargetPoId() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Target PO Id cannot be null!!");
			return response;
		}
		if (migrateTeam.isMergeTeam() && migrateTeam.getTargetTeamId() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("When Merging team, Target Team Id cannot be null!!");
			return response;
		}
		List<String> srcActiveTeamNames = teamRepository.findActiveTeamNameByTeamIds(migrateTeam.getMigrationTeamIds());
		if (srcActiveTeamNames == null || srcActiveTeamNames.isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Selected Migration Team(s) Not found!!");
			return response;
		}
		if (srcActiveTeamNames != null && !srcActiveTeamNames.isEmpty()) {
			List<String> tgtActiveTeamNames = teamRepository
					.findActiveTeamNameByProjectId(migrateTeam.getTargetProjectId());
			boolean hasCommonTeams = srcActiveTeamNames.stream().anyMatch(tgtActiveTeamNames::contains);
			if (hasCommonTeams) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse(
						"Team(s) with the same name already exists in the Target Project & PO, Kindly select the 'Merge into Existing Team' option!!");
				return response;
			}
		}
		return null;
	}

	private ServiceResponse validateUpdateDefaultProjectCompletionRequest(RmgTeamMemberDto teamMember,
			ServiceResponse response) {
		if (teamMember == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Request cannot be null!!");
			return response;
		}
		if (teamMember.getProjectId() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Project Id cannot be null!!");
			return response;
		}
		if (teamMember.getUpdatedBy() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Current Emp Id cannot be null!!");
			return response;
		}
		if (teamMember.getSelectedEmpIds() == null || teamMember.getSelectedEmpIds().isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Selected Employee Id(s) cannot be null!!");
			return response;
		}
		if (teamMember.getEmployeeRoles() == null || teamMember.getEmployeeRoles().isEmpty()) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Selected Employee Role cannot be null!!");
			return response;
		}
		return null;
	}

	private ServiceResponse validateEmployeeProjectStartDateObject(RmgTeamMemberDto rmgTeamMemberDto,
			boolean checkMultiple) {
		ServiceResponse response = new ServiceResponse();
		if (rmgTeamMemberDto == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Request body cannot be null!!");
			return response;
		}
		if (rmgTeamMemberDto.getProjectId() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Current Project Id cannot be null!!");
			return response;
		}
		if (rmgTeamMemberDto.getStartDate() == null) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Start date cannot be null.");
			return response;
		}
		if (checkMultiple) {
			for (Long empId : rmgTeamMemberDto.getSelectedEmpIds()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Invalid Start Date for EMP ID : " + empId);
				return response;
			}
		}
		return null;
	}

//	Helpers Method Start

	private String getAllHodMails(List<EmployeeTeamMap> mappings) {
		return mappings.stream().map(m -> employeeRepository.findHodMail(m.getEmpId())).filter(Objects::nonNull)
				.distinct().collect(Collectors.joining(","));
	}

	private boolean isTeamMemberValueChanged(EmployeeTeamMap existingMember, RmgTeamMemberDto dto) {
		List<String> dtoEmpRoles = dto.getEmployeeRoles() != null && !dto.getEmployeeRoles().isEmpty()
				? dto.getEmployeeRoles().stream().map(String::trim).map(String::toUpperCase)
						.filter(role -> !role.isEmpty()).collect(Collectors.toList())
				: Collections.emptyList();
		List<String> objEmpRoles = parseRoles(existingMember.getEmployeeRole());

		return isRoleChanged(dtoEmpRoles, objEmpRoles) || dateChanged(existingMember.getStartDate(), dto.getStartDate())
				|| dateChanged(existingMember.getEndDate(), dto.getEndDate())
				|| !Objects.equals(existingMember.getIsShadow(), dto.getIsShadow())
				|| !Objects.equals(existingMember.getActive(), dto.getIsMemberActive())
				|| !Objects.equals(existingMember.getPoId(), dto.getPoId())
				|| !Objects.equals(existingMember.getRoleId(), dto.getRoleId())
				|| !Objects.equals(existingMember.getEmpTeamDepartmentId(), dto.getEmpTeamDepartmentId());
	}

	private List<String> parseRoles(String roles) {
		if (roles == null || roles.isBlank()) {
			return Collections.emptyList();
		}
		return Arrays.stream(roles.split(",")).map(String::trim).map(String::toUpperCase)
				.filter(role -> !role.isEmpty()).collect(Collectors.toList());
	}

	private boolean isRoleChanged(List<String> dtoEmpRoles, List<String> objEmpRoles) {
		return !new HashSet<>(dtoEmpRoles).equals(new HashSet<>(objEmpRoles));
	}

	private boolean dateChanged(LocalDateTime a, LocalDateTime b) {
		if (a == null && b == null)
			return false;
		if (a == null || b == null)
			return true;
		return !a.toLocalDate().equals(b.toLocalDate());
	}

	private EmpPrimaryProjectMapping createNewMapping(Long empId, Project project, Long projectId, Long updatedBy,
			LocalDateTime now, boolean isDefaultProjectDateOfFuture) {
		EmpPrimaryProjectMapping mapping = new EmpPrimaryProjectMapping();
		mapping.setEmpId(empId);
		mapping.setPrimaryProjectId(projectId);
		mapping.setPrimaryProjectName(project.getProjectName());
		mapping.setIsMapped(isDefaultProjectDateOfFuture ? "N" : "Y");
		mapping.setUpdatedBy(updatedBy);
		mapping.setUpdatedOn(now);
		return mapping;
	}

	public void updateEmployeeBillableType(Integer tgtProjectId, List<Long> sourceEmpIds) {
		Project updatedTargetProject = projectRepository.findByProjectId(tgtProjectId);
		if (updatedTargetProject == null) {
			return;
		}
		String poProjectType = updatedTargetProject != null ? updatedTargetProject.getPoProjectType() : null;
		String ishineProjectType = updatedTargetProject != null ? updatedTargetProject.getInternalProjectType() : null;

		String billable = null;
		String billableType = null;

		if (poProjectType != null) {
			switch (poProjectType) {
			case "TNM":
				billable = "Yes";
				billableType = "TNM";
				break;
			case "Fixed Cost":
			case "Monitoring":
				billable = "No";
				billableType = "Fixed Cost";
				break;
			default:
				billable = null;
				billableType = null;
			}
		} else if (ishineProjectType != null) {
			switch (ishineProjectType) {
			case "InternalRNDProducts":
				billable = "No";
				billableType = "InternalRNDProducts";
				break;
			case "Bench":
				billable = "No";
				billableType = "Bench";
				break;
			default:
				billable = null;
				billableType = null;
			}
		}
		if (billable != null || billableType != null) {
			employeeRepository.updateBillableAndTypeForEmpIds(billable, billableType, sourceEmpIds);
		}
	}

	private BillableInfo resolveBillableInfo(Project project) {
		String YES = "Yes";
		String NO = "No";
		String Y = "Y";
		String N = "N";
		String SHADOW = "Shadow";
		String TNM = "TNM";
		String FIXED_COST = "Fixed Cost";
		String BENCH = "Bench";
		String INTERNAL_RND = "InternalRNDProducts";
		String MONITORING = "Monitoring";

		if (TNM.equalsIgnoreCase(project.getPoProjectType())) {
			return new BillableInfo(TNM, YES);
		}
		if (FIXED_COST.equalsIgnoreCase(project.getPoProjectType())
				|| MONITORING.equalsIgnoreCase(project.getPoProjectType())) {
			return new BillableInfo(FIXED_COST, NO);
		}
		if (BENCH.equalsIgnoreCase(project.getInternalProjectType())) {
			return new BillableInfo(BENCH, NO);
		}
		if (INTERNAL_RND.equalsIgnoreCase(project.getInternalProjectType())) {
			return new BillableInfo(INTERNAL_RND, NO);
		}
		return new BillableInfo(null, null);
	}

	public String getDisplayRequirement(RmgTeamMemberDto obj) {
		StringBuilder sb = new StringBuilder();
		appendIfNotNull(sb, "Role", obj.getRole());
		appendIfNotNull(sb, "Experience", obj.getExperience());
		appendIfNotNull(sb, "Department", obj.getDepartment());
		return sb.toString();
	}

	public void appendIfNotNull(StringBuilder sb, String label, Object value) {
		if (value != null) {
			if (sb.length() > 0) {
				sb.append(" | ");
			}
			sb.append(label).append(" : ").append(value);
		}
	}

	private boolean isStartDateConflict(LocalDate newDate, LocalDate projectStartDate, LocalDate oldStartDate) {
		return newDate.isAfter(projectStartDate) || newDate.isBefore(oldStartDate);
	}

	private boolean isDefaultFutureDate(LocalDateTime defaultProjectStartDate, LocalDateTime now) {
		if (now != null && defaultProjectStartDate != null) {
			if (defaultProjectStartDate.toLocalDate().isAfter(now.toLocalDate())) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	private List<Long> getDeptIdListFromString(String deptIds) {
		return Optional.ofNullable(deptIds).filter(s -> !s.isBlank())
				.map(s -> Arrays.stream(s.split(",")).map(String::trim).map(Long::valueOf).collect(Collectors.toList()))
				.orElse(List.of());
	}

}
