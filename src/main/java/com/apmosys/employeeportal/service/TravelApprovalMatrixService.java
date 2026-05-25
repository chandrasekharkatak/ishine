package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.dto.TravelApprovalMatrixDTO;
import com.apmosys.employeeportal.dto.TravelApprovalMatrixDTO.TravelApprovalMatrixAdminDTO;
import com.apmosys.employeeportal.dto.TravelApprovalMatrixDTO.TravelApprovalMatrixLevelDTO;
import com.apmosys.employeeportal.dto.TravelApprovalMatrixResolveDTO;
import com.apmosys.employeeportal.dto.TravelApprovalMatrixResolveDTO.ApprovalLevelColumnDTO;
import com.apmosys.employeeportal.dto.TravelApprovalMatrixSaveRequestDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.TravelApprovalMatrix;
import com.apmosys.employeeportal.model.TravelApprovalMatrixAppDept;
import com.apmosys.employeeportal.model.TravelApprovalMatrixAppRole;
import com.apmosys.employeeportal.model.TravelApprovalMatrixLevel;
import com.apmosys.employeeportal.model.TravelApprovalMatrixLevelDept;
import com.apmosys.employeeportal.model.TravelApprovalMatrixLevelRole;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.TravelApprovalMatrixAppDeptRepository;
import com.apmosys.employeeportal.repository.TravelApprovalMatrixAppRoleRepository;
import com.apmosys.employeeportal.repository.TravelApprovalMatrixLevelDeptRepository;
import com.apmosys.employeeportal.repository.TravelApprovalMatrixLevelRepository;
import com.apmosys.employeeportal.repository.TravelApprovalMatrixLevelRoleRepository;
import com.apmosys.employeeportal.repository.TravelApprovalMatrixRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class TravelApprovalMatrixService {

	@Autowired
	private TravelApprovalMatrixRepository matrixRepository;

	@Autowired
	private TravelApprovalMatrixAppDeptRepository appDeptRepository;

	@Autowired
	private TravelApprovalMatrixAppRoleRepository appRoleRepository;

	@Autowired
	private TravelApprovalMatrixLevelRepository levelRepository;

	@Autowired
	private TravelApprovalMatrixLevelDeptRepository levelDeptRepository;

	@Autowired
	private TravelApprovalMatrixLevelRoleRepository levelRoleRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private JobRoleRepository jobRoleRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	public ServiceResponse resolveForEmployee(Long empId) {
		ServiceResponse response = new ServiceResponse();
		try {
			TravelApprovalMatrixResolveDTO resolved = buildResolveDto(empId, null);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(resolved);
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public TravelApprovalMatrixDTO getMatrixById(Long matrixId) {
		if (matrixId == null) {
			return null;
		}
		return matrixRepository.findById(matrixId).map(this::toDto).orElse(null);
	}

	public TravelApprovalMatrixDTO findMatchingMatrixForEmployee(Long empId, String departmentName) {
		Long deptId = resolveEmployeeDeptId(empId, departmentName);
		Long jobRoleId = null;
		if (empId != null) {
			Employee emp = employeeRepository.findByEmpId(empId);
			if (emp != null) {
				jobRoleId = emp.getJobRoleId();
			}
		}
		return findBestMatchingMatrix(deptId, jobRoleId);
	}

	public TravelApprovalMatrixResolveDTO buildResolveDto(Long empId, String departmentName) {
		TravelApprovalMatrixDTO matrix = findMatchingMatrixForEmployee(empId, departmentName);
		TravelApprovalMatrixResolveDTO dto = new TravelApprovalMatrixResolveDTO();
		if (matrix == null) {
			dto.setApprovalFlowSummary("HOD → Travel Admin");
			addLegacyDefaultColumns(dto);
			return dto;
		}
		dto.setMatrixId(matrix.getMatrixId());
		dto.setMatrixName(matrix.getName());
		dto.setApprovalFlowSummary(buildFlowSummary(matrix));
		for (TravelApprovalMatrixLevelDTO lvl : matrix.getLevels()) {
			ApprovalLevelColumnDTO col = new ApprovalLevelColumnDTO();
			col.setOrder(lvl.getOrder());
			col.setLevelLabel(formatLevelLabel(lvl));
			col.setRouting(lvl.getRouting());
			col.setFinanceStep(false);
			dto.getLevelColumns().add(col);
		}
		ApprovalLevelColumnDTO admin = new ApprovalLevelColumnDTO();
		admin.setOrder(matrix.getLevels().size() + 1);
		admin.setLevelLabel("Travel Admin");
		admin.setRouting("TRAVEL_ADMIN");
		admin.setFinanceStep(true);
		dto.getLevelColumns().add(admin);
		return dto;
	}

	public String formatLevelLabel(TravelApprovalMatrixLevelDTO lvl) {
		if (lvl == null || !StringUtils.hasText(lvl.getRouting())) {
			return "Approval level";
		}
		switch (lvl.getRouting().trim()) {
			case "REPORTING_MANAGER":
				return "Reporting manager";
			case "HOD_SUBMITTER_DEPT":
				return "HOD";
			case "POOL_ANY_IN_SCOPE":
				return "Approver pool";
			case "SPECIFIC_IN_SCOPE":
				return formatSpecificInScopeLevelLabel(lvl);
			default:
				return lvl.getRouting().replace('_', ' ');
		}
	}

	/** Column / flow label: exact department name(s) configured on this matrix level. */
	private String formatSpecificInScopeLevelLabel(TravelApprovalMatrixLevelDTO lvl) {
		if (lvl == null || lvl.getDepartmentIds() == null || lvl.getDepartmentIds().isEmpty()) {
			return "Department";
		}
		List<String> names = new ArrayList<>();
		for (Long deptId : lvl.getDepartmentIds()) {
			if (deptId == null) {
				continue;
			}
			Department dept = departmentRepository.findByDeptId(deptId);
			if (dept != null && StringUtils.hasText(dept.getName())) {
				String nm = dept.getName().trim();
				boolean seen = names.stream().anyMatch(n -> n.equalsIgnoreCase(nm));
				if (!seen) {
					names.add(nm);
				}
			}
		}
		names.sort(String.CASE_INSENSITIVE_ORDER);
		if (names.isEmpty()) {
			return "Department";
		}
		return String.join(", ", names);
	}

	private String buildFlowSummary(TravelApprovalMatrixDTO matrix) {
		List<String> parts = new ArrayList<>();
		if (matrix.getLevels() != null) {
			for (TravelApprovalMatrixLevelDTO lvl : matrix.getLevels()) {
				parts.add(formatLevelLabel(lvl));
			}
		}
		parts.add("Travel Admin");
		return String.join(" → ", parts);
	}

	private void addLegacyDefaultColumns(TravelApprovalMatrixResolveDTO dto) {
		String[] labels = { "HOD", "Travel Admin" };
		for (int i = 0; i < labels.length; i++) {
			ApprovalLevelColumnDTO col = new ApprovalLevelColumnDTO();
			col.setOrder(i + 1);
			col.setLevelLabel(labels[i]);
			col.setFinanceStep(i == 1);
			dto.getLevelColumns().add(col);
		}
	}

	private Long resolveEmployeeDeptId(Long empId, String departmentName) {
		if (empId != null) {
			Employee emp = employeeRepository.findByEmpId(empId);
			if (emp != null && emp.getJobRoleId() != null) {
				JobRole jr = jobRoleRepository.findById(emp.getJobRoleId()).orElse(null);
				if (jr != null && jr.getDeptId() != null) {
					return jr.getDeptId();
				}
			}
		}
		if (StringUtils.hasText(departmentName) && departmentRepository != null) {
			Department dept = departmentRepository.findByName(departmentName);
			if (dept != null) {
				return dept.getDeptId();
			}
		}
		return null;
	}

	private TravelApprovalMatrixDTO findBestMatchingMatrix(Long deptId, Long jobRoleId) {
		TravelApprovalMatrixDTO best = null;
		int bestScore = -1;
		for (TravelApprovalMatrix matrix : matrixRepository.findAllByOrderByMatrixIdAsc()) {
			if (matrix == null || !"Y".equalsIgnoreCase(matrix.getIsActive())) {
				continue;
			}
			TravelApprovalMatrixDTO dto = toDto(matrix);
			if (!matrixMatchesEmployee(dto, deptId, jobRoleId)) {
				continue;
			}
			int score = specificityScore(dto);
			if (score > bestScore) {
				bestScore = score;
				best = dto;
			}
		}
		return best;
	}

	private boolean matrixMatchesEmployee(TravelApprovalMatrixDTO dto, Long deptId, Long jobRoleId) {
		List<Long> deptIds = dto.getApplicabilityDepartmentIds();
		List<Long> roleIds = dto.getApplicabilityJobRoleIds();
		if (deptIds != null && !deptIds.isEmpty()) {
			if (deptId == null || !deptIds.contains(deptId)) {
				return false;
			}
		}
		if (roleIds != null && !roleIds.isEmpty()) {
			if (jobRoleId == null || !roleIds.contains(jobRoleId)) {
				return false;
			}
		}
		return true;
	}

	private int specificityScore(TravelApprovalMatrixDTO dto) {
		int score = 0;
		if (dto.getApplicabilityDepartmentIds() != null) {
			score += dto.getApplicabilityDepartmentIds().size() * 10;
		}
		if (dto.getApplicabilityJobRoleIds() != null) {
			score += dto.getApplicabilityJobRoleIds().size();
		}
		return score;
	}

	public ServiceResponse getAllApprovalMatrices() {
		ServiceResponse response = new ServiceResponse();
		try {
			List<TravelApprovalMatrix> rows = matrixRepository.findAllByOrderByMatrixIdAsc();
			List<TravelApprovalMatrixDTO> dtoList = new ArrayList<>();
			for (TravelApprovalMatrix matrix : rows) {
				if (matrix == null || !"Y".equalsIgnoreCase(matrix.getIsActive())) {
					continue;
				}
				dtoList.add(toDto(matrix));
			}
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(dtoList);
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	@Transactional
	public ServiceResponse saveAllApprovalMatrices(TravelApprovalMatrixSaveRequestDTO request) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (request == null || request.getMatrices() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceError("No approval matrix data to save.");
				return response;
			}
			Employee actor = request.getCreatedBy() != null ? employeeRepository.findByEmpId(request.getCreatedBy()) : null;
			final String actorName = actor != null ? actor.getName() : "System";
			final Long actorId = request.getCreatedBy();

			Set<Long> keepIds = new HashSet<>();
			for (TravelApprovalMatrixDTO incoming : request.getMatrices()) {
				TravelApprovalMatrix matrix = persistOneMatrix(incoming, actorId, actorName);
				keepIds.add(matrix.getMatrixId());
			}

			for (TravelApprovalMatrix existing : matrixRepository.findAllByOrderByMatrixIdAsc()) {
				if (existing.getMatrixId() != null && !keepIds.contains(existing.getMatrixId())) {
					matrixRepository.deleteById(existing.getMatrixId());
				}
			}

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(getAllApprovalMatrices().getServiceResponse());
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceError("Failed to save approval matrices: " + e.getMessage());
		}
		return response;
	}

	@Transactional
	public ServiceResponse saveApprovalMatrix(TravelApprovalMatrixSaveRequestDTO request) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (request == null || request.getMatrices() == null || request.getMatrices().isEmpty()) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceError("No approval matrix data to save.");
				return response;
			}
			Employee actor = request.getCreatedBy() != null ? employeeRepository.findByEmpId(request.getCreatedBy()) : null;
			final String actorName = actor != null ? actor.getName() : "System";
			final Long actorId = request.getCreatedBy();
			TravelApprovalMatrix matrix = persistOneMatrix(request.getMatrices().get(0), actorId, actorName);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(toDto(matrix));
		} catch (IllegalArgumentException e) {
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceError(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceError("Failed to save approval matrix: " + e.getMessage());
		}
		return response;
	}

	@Transactional
	public ServiceResponse deleteApprovalMatrix(Long matrixId) {
		ServiceResponse response = new ServiceResponse();
		try {
			if (matrixId == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceError("Approval matrix id is required.");
				return response;
			}
			if (!matrixRepository.existsById(matrixId)) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceError("Approval matrix not found.");
				return response;
			}
			matrixRepository.deleteById(matrixId);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse("Approval matrix deleted.");
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceError("Failed to delete approval matrix: " + e.getMessage());
		}
		return response;
	}

	private TravelApprovalMatrix persistOneMatrix(TravelApprovalMatrixDTO incoming, Long actorId,
			String actorName) {
		if (incoming == null || !StringUtils.hasText(incoming.getName())) {
			throw new IllegalArgumentException("Approval matrix must have a name.");
		}
		if (incoming.getLevels() == null || incoming.getLevels().isEmpty()) {
			throw new IllegalArgumentException("Approval matrix must have at least one approval level.");
		}
		TravelApprovalMatrix matrix = resolveMatrixHeader(incoming, actorId, actorName);
		replaceMatrixChildren(matrix, incoming);
		return matrix;
	}

	private TravelApprovalMatrix resolveMatrixHeader(TravelApprovalMatrixDTO incoming, Long actorId,
			String actorName) {
		TravelApprovalMatrix matrix;
		if (incoming.getMatrixId() != null) {
			matrix = matrixRepository.findById(incoming.getMatrixId())
					.orElseThrow(() -> new IllegalArgumentException("Approval matrix not found: " + incoming.getMatrixId()));
			matrix.setUpdatedBy(actorId);
			matrix.setUpdatedByName(actorName);
		} else {
			matrix = new TravelApprovalMatrix();
			matrix.setCreatedBy(actorId);
			matrix.setCreatedByName(actorName);
			matrix.setIsActive("Y");
		}
		matrix.setMatrixName(incoming.getName().trim());
		TravelApprovalMatrixAdminDTO fin = incoming.getAdmin();
		matrix.setAdminDeptId(fin != null ? fin.getDepartmentId() : null);
		matrix.setAdminAssigneeEmpId(fin != null ? fin.getAssigneeEmployeeId() : null);
		return matrixRepository.save(matrix);
	}

	private void replaceMatrixChildren(TravelApprovalMatrix matrix, TravelApprovalMatrixDTO incoming) {
		Long matrixId = matrix.getMatrixId();
		appDeptRepository.deleteByMatrixId(matrixId);
		appRoleRepository.deleteByMatrixId(matrixId);
		levelRepository.deleteByMatrixId(matrixId);

		if (incoming.getApplicabilityDepartmentIds() != null) {
			for (Long deptId : distinctLongs(incoming.getApplicabilityDepartmentIds())) {
				appDeptRepository.save(new TravelApprovalMatrixAppDept(matrixId, deptId));
			}
		}
		if (incoming.getApplicabilityJobRoleIds() != null) {
			for (Long roleId : distinctLongs(incoming.getApplicabilityJobRoleIds())) {
				appRoleRepository.save(new TravelApprovalMatrixAppRole(matrixId, roleId));
			}
		}

		int order = 1;
		for (TravelApprovalMatrixLevelDTO lvl : incoming.getLevels()) {
			if (lvl == null) {
				continue;
			}
			TravelApprovalMatrixLevel level = new TravelApprovalMatrixLevel();
			level.setMatrixId(matrixId);
			level.setLevelOrder(lvl.getOrder() != null ? lvl.getOrder() : order);
			level.setRoutingMode(lvl.getRouting() != null ? lvl.getRouting().trim() : "REPORTING_MANAGER");
			level.setSpecificEmployeeId(lvl.getAssigneeEmployeeId());
			level = levelRepository.save(level);

			if (lvl.getDepartmentIds() != null) {
				for (Long deptId : distinctLongs(lvl.getDepartmentIds())) {
					levelDeptRepository.save(new TravelApprovalMatrixLevelDept(level.getLevelId(), deptId));
				}
			}
			if (lvl.getJobRoleIds() != null) {
				for (Long roleId : distinctLongs(lvl.getJobRoleIds())) {
					levelRoleRepository.save(new TravelApprovalMatrixLevelRole(level.getLevelId(), roleId));
				}
			}
			order++;
		}
	}

	private TravelApprovalMatrixDTO toDto(TravelApprovalMatrix matrix) {
		TravelApprovalMatrixDTO dto = new TravelApprovalMatrixDTO();
		dto.setMatrixId(matrix.getMatrixId());
		dto.setName(matrix.getMatrixName());
		dto.setCreatedByName(matrix.getCreatedByName());
		dto.setCreatedOn(matrix.getCreatedOn());
		dto.setUpdatedByName(matrix.getUpdatedByName());
		dto.setUpdatedOn(matrix.getUpdatedOn());
		dto.setApplicabilityDepartmentIds(
				appDeptRepository.findByMatrixId(matrix.getMatrixId()).stream().map(TravelApprovalMatrixAppDept::getDeptId)
						.collect(Collectors.toList()));
		dto.setApplicabilityJobRoleIds(
				appRoleRepository.findByMatrixId(matrix.getMatrixId()).stream().map(TravelApprovalMatrixAppRole::getJobRoleId)
						.collect(Collectors.toList()));
		TravelApprovalMatrixAdminDTO fin = new TravelApprovalMatrixAdminDTO();
		fin.setDepartmentId(matrix.getAdminDeptId());
		fin.setAssigneeEmployeeId(matrix.getAdminAssigneeEmpId());
		dto.setAdmin(fin);

		List<TravelApprovalMatrixLevelDTO> levels = new ArrayList<>();
		for (TravelApprovalMatrixLevel level : levelRepository.findByMatrixIdOrderByLevelOrderAsc(matrix.getMatrixId())) {
			TravelApprovalMatrixLevelDTO lvl = new TravelApprovalMatrixLevelDTO();
			lvl.setOrder(level.getLevelOrder());
			lvl.setRouting(level.getRoutingMode());
			lvl.setAssigneeEmployeeId(level.getSpecificEmployeeId());
			lvl.setDepartmentIds(
					levelDeptRepository.findByLevelId(level.getLevelId()).stream().map(TravelApprovalMatrixLevelDept::getDeptId)
							.collect(Collectors.toList()));
			lvl.setJobRoleIds(
					levelRoleRepository.findByLevelId(level.getLevelId()).stream()
							.map(TravelApprovalMatrixLevelRole::getJobRoleId).collect(Collectors.toList()));
			levels.add(lvl);
		}
		dto.setLevels(levels);
		return dto;
	}

	private List<Long> distinctLongs(List<Long> ids) {
		if (ids == null) {
			return Collections.emptyList();
		}
		return ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
	}
}
