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

import com.apmosys.employeeportal.dto.ReimbursementApprovalMatrixDTO;
import com.apmosys.employeeportal.dto.ReimbursementApprovalMatrixDTO.ReimbursementApprovalMatrixFinanceDTO;
import com.apmosys.employeeportal.dto.ReimbursementApprovalMatrixDTO.ReimbursementApprovalMatrixLevelDTO;
import com.apmosys.employeeportal.dto.ReimbursementApprovalMatrixResolveDTO;
import com.apmosys.employeeportal.dto.ReimbursementApprovalMatrixResolveDTO.ApprovalLevelColumnDTO;
import com.apmosys.employeeportal.dto.ReimbursementApprovalMatrixSaveRequestDTO;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.ReimbursementApprovalMatrix;
import com.apmosys.employeeportal.model.ReimbursementApprovalMatrixAppDept;
import com.apmosys.employeeportal.model.ReimbursementApprovalMatrixAppRole;
import com.apmosys.employeeportal.model.ReimbursementApprovalMatrixLevel;
import com.apmosys.employeeportal.model.ReimbursementApprovalMatrixLevelDept;
import com.apmosys.employeeportal.model.ReimbursementApprovalMatrixLevelRole;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.ReimbursementApprovalMatrixAppDeptRepository;
import com.apmosys.employeeportal.repository.ReimbursementApprovalMatrixAppRoleRepository;
import com.apmosys.employeeportal.repository.ReimbursementApprovalMatrixLevelDeptRepository;
import com.apmosys.employeeportal.repository.ReimbursementApprovalMatrixLevelRepository;
import com.apmosys.employeeportal.repository.ReimbursementApprovalMatrixLevelRoleRepository;
import com.apmosys.employeeportal.repository.ReimbursementApprovalMatrixRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class ReimbursementApprovalMatrixService {

	@Autowired
	private ReimbursementApprovalMatrixRepository matrixRepository;

	@Autowired
	private ReimbursementApprovalMatrixAppDeptRepository appDeptRepository;

	@Autowired
	private ReimbursementApprovalMatrixAppRoleRepository appRoleRepository;

	@Autowired
	private ReimbursementApprovalMatrixLevelRepository levelRepository;

	@Autowired
	private ReimbursementApprovalMatrixLevelDeptRepository levelDeptRepository;

	@Autowired
	private ReimbursementApprovalMatrixLevelRoleRepository levelRoleRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private JobRoleRepository jobRoleRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	public ServiceResponse resolveForEmployee(Long empId) {
		ServiceResponse response = new ServiceResponse();
		try {
			ReimbursementApprovalMatrixResolveDTO resolved = buildResolveDto(empId, null);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(resolved);
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(e.getMessage());
		}
		return response;
	}

	public ReimbursementApprovalMatrixDTO getMatrixById(Long matrixId) {
		if (matrixId == null) {
			return null;
		}
		return matrixRepository.findById(matrixId).map(this::toDto).orElse(null);
	}

	public ReimbursementApprovalMatrixDTO findMatchingMatrixForEmployee(Long empId, String departmentName) {
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

	public ReimbursementApprovalMatrixResolveDTO buildResolveDto(Long empId, String departmentName) {
		ReimbursementApprovalMatrixDTO matrix = findMatchingMatrixForEmployee(empId, departmentName);
		ReimbursementApprovalMatrixResolveDTO dto = new ReimbursementApprovalMatrixResolveDTO();
		if (matrix == null) {
			dto.setApprovalFlowSummary("Reporting manager → HR → Finance");
			addLegacyDefaultColumns(dto);
			return dto;
		}
		dto.setMatrixId(matrix.getMatrixId());
		dto.setMatrixName(matrix.getName());
		dto.setApprovalFlowSummary(buildFlowSummary(matrix));
		for (ReimbursementApprovalMatrixLevelDTO lvl : matrix.getLevels()) {
			ApprovalLevelColumnDTO col = new ApprovalLevelColumnDTO();
			col.setOrder(lvl.getOrder());
			col.setLevelLabel(formatLevelLabel(lvl));
			col.setRouting(lvl.getRouting());
			col.setFinanceStep(false);
			dto.getLevelColumns().add(col);
		}
		ApprovalLevelColumnDTO fin = new ApprovalLevelColumnDTO();
		fin.setOrder(matrix.getLevels().size() + 1);
		fin.setLevelLabel("Finance");
		fin.setRouting("FINANCE");
		fin.setFinanceStep(true);
		dto.getLevelColumns().add(fin);
		return dto;
	}

	public String formatLevelLabel(ReimbursementApprovalMatrixLevelDTO lvl) {
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
				return "Specific approver";
			default:
				return lvl.getRouting().replace('_', ' ');
		}
	}

	private String buildFlowSummary(ReimbursementApprovalMatrixDTO matrix) {
		List<String> parts = new ArrayList<>();
		if (matrix.getLevels() != null) {
			for (ReimbursementApprovalMatrixLevelDTO lvl : matrix.getLevels()) {
				parts.add(formatLevelLabel(lvl));
			}
		}
		parts.add("Finance");
		return String.join(" → ", parts);
	}

	private void addLegacyDefaultColumns(ReimbursementApprovalMatrixResolveDTO dto) {
		String[] labels = { "HOD", "HR", "Finance" };
		for (int i = 0; i < labels.length; i++) {
			ApprovalLevelColumnDTO col = new ApprovalLevelColumnDTO();
			col.setOrder(i + 1);
			col.setLevelLabel(labels[i]);
			col.setFinanceStep(i == 2);
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

	private ReimbursementApprovalMatrixDTO findBestMatchingMatrix(Long deptId, Long jobRoleId) {
		ReimbursementApprovalMatrixDTO best = null;
		int bestScore = -1;
		for (ReimbursementApprovalMatrix matrix : matrixRepository.findAllByOrderByMatrixIdAsc()) {
			if (matrix == null || !"Y".equalsIgnoreCase(matrix.getIsActive())) {
				continue;
			}
			ReimbursementApprovalMatrixDTO dto = toDto(matrix);
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

	private boolean matrixMatchesEmployee(ReimbursementApprovalMatrixDTO dto, Long deptId, Long jobRoleId) {
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

	private int specificityScore(ReimbursementApprovalMatrixDTO dto) {
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
			List<ReimbursementApprovalMatrix> rows = matrixRepository.findAllByOrderByMatrixIdAsc();
			List<ReimbursementApprovalMatrixDTO> dtoList = new ArrayList<>();
			for (ReimbursementApprovalMatrix matrix : rows) {
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
	public ServiceResponse saveAllApprovalMatrices(ReimbursementApprovalMatrixSaveRequestDTO request) {
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
			for (ReimbursementApprovalMatrixDTO incoming : request.getMatrices()) {
				ReimbursementApprovalMatrix matrix = persistOneMatrix(incoming, actorId, actorName);
				keepIds.add(matrix.getMatrixId());
			}

			for (ReimbursementApprovalMatrix existing : matrixRepository.findAllByOrderByMatrixIdAsc()) {
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
	public ServiceResponse saveApprovalMatrix(ReimbursementApprovalMatrixSaveRequestDTO request) {
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
			ReimbursementApprovalMatrix matrix = persistOneMatrix(request.getMatrices().get(0), actorId, actorName);
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

	private ReimbursementApprovalMatrix persistOneMatrix(ReimbursementApprovalMatrixDTO incoming, Long actorId,
			String actorName) {
		if (incoming == null || !StringUtils.hasText(incoming.getName())) {
			throw new IllegalArgumentException("Approval matrix must have a name.");
		}
		if (incoming.getLevels() == null || incoming.getLevels().isEmpty()) {
			throw new IllegalArgumentException("Approval matrix must have at least one approval level.");
		}
		ReimbursementApprovalMatrix matrix = resolveMatrixHeader(incoming, actorId, actorName);
		replaceMatrixChildren(matrix, incoming);
		return matrix;
	}

	private ReimbursementApprovalMatrix resolveMatrixHeader(ReimbursementApprovalMatrixDTO incoming, Long actorId,
			String actorName) {
		ReimbursementApprovalMatrix matrix;
		if (incoming.getMatrixId() != null) {
			matrix = matrixRepository.findById(incoming.getMatrixId())
					.orElseThrow(() -> new IllegalArgumentException("Approval matrix not found: " + incoming.getMatrixId()));
			matrix.setUpdatedBy(actorId);
			matrix.setUpdatedByName(actorName);
		} else {
			matrix = new ReimbursementApprovalMatrix();
			matrix.setCreatedBy(actorId);
			matrix.setCreatedByName(actorName);
			matrix.setIsActive("Y");
		}
		matrix.setMatrixName(incoming.getName().trim());
		ReimbursementApprovalMatrixFinanceDTO fin = incoming.getFinance();
		matrix.setFinanceDeptId(fin != null ? fin.getDepartmentId() : null);
		matrix.setFinanceAssigneeEmpId(fin != null ? fin.getAssigneeEmployeeId() : null);
		return matrixRepository.save(matrix);
	}

	private void replaceMatrixChildren(ReimbursementApprovalMatrix matrix, ReimbursementApprovalMatrixDTO incoming) {
		Long matrixId = matrix.getMatrixId();
		appDeptRepository.deleteByMatrixId(matrixId);
		appRoleRepository.deleteByMatrixId(matrixId);
		levelRepository.deleteByMatrixId(matrixId);

		if (incoming.getApplicabilityDepartmentIds() != null) {
			for (Long deptId : distinctLongs(incoming.getApplicabilityDepartmentIds())) {
				appDeptRepository.save(new ReimbursementApprovalMatrixAppDept(matrixId, deptId));
			}
		}
		if (incoming.getApplicabilityJobRoleIds() != null) {
			for (Long roleId : distinctLongs(incoming.getApplicabilityJobRoleIds())) {
				appRoleRepository.save(new ReimbursementApprovalMatrixAppRole(matrixId, roleId));
			}
		}

		int order = 1;
		for (ReimbursementApprovalMatrixLevelDTO lvl : incoming.getLevels()) {
			if (lvl == null) {
				continue;
			}
			ReimbursementApprovalMatrixLevel level = new ReimbursementApprovalMatrixLevel();
			level.setMatrixId(matrixId);
			level.setLevelOrder(lvl.getOrder() != null ? lvl.getOrder() : order);
			level.setRoutingMode(lvl.getRouting() != null ? lvl.getRouting().trim() : "REPORTING_MANAGER");
			level.setSpecificEmployeeId(lvl.getAssigneeEmployeeId());
			level = levelRepository.save(level);

			if (lvl.getDepartmentIds() != null) {
				for (Long deptId : distinctLongs(lvl.getDepartmentIds())) {
					levelDeptRepository.save(new ReimbursementApprovalMatrixLevelDept(level.getLevelId(), deptId));
				}
			}
			if (lvl.getJobRoleIds() != null) {
				for (Long roleId : distinctLongs(lvl.getJobRoleIds())) {
					levelRoleRepository.save(new ReimbursementApprovalMatrixLevelRole(level.getLevelId(), roleId));
				}
			}
			order++;
		}
	}

	private ReimbursementApprovalMatrixDTO toDto(ReimbursementApprovalMatrix matrix) {
		ReimbursementApprovalMatrixDTO dto = new ReimbursementApprovalMatrixDTO();
		dto.setMatrixId(matrix.getMatrixId());
		dto.setName(matrix.getMatrixName());
		dto.setCreatedByName(matrix.getCreatedByName());
		dto.setCreatedOn(matrix.getCreatedOn());
		dto.setUpdatedByName(matrix.getUpdatedByName());
		dto.setUpdatedOn(matrix.getUpdatedOn());
		dto.setApplicabilityDepartmentIds(
				appDeptRepository.findByMatrixId(matrix.getMatrixId()).stream().map(ReimbursementApprovalMatrixAppDept::getDeptId)
						.collect(Collectors.toList()));
		dto.setApplicabilityJobRoleIds(
				appRoleRepository.findByMatrixId(matrix.getMatrixId()).stream().map(ReimbursementApprovalMatrixAppRole::getJobRoleId)
						.collect(Collectors.toList()));
		ReimbursementApprovalMatrixFinanceDTO fin = new ReimbursementApprovalMatrixFinanceDTO();
		fin.setDepartmentId(matrix.getFinanceDeptId());
		fin.setAssigneeEmployeeId(matrix.getFinanceAssigneeEmpId());
		dto.setFinance(fin);

		List<ReimbursementApprovalMatrixLevelDTO> levels = new ArrayList<>();
		for (ReimbursementApprovalMatrixLevel level : levelRepository.findByMatrixIdOrderByLevelOrderAsc(matrix.getMatrixId())) {
			ReimbursementApprovalMatrixLevelDTO lvl = new ReimbursementApprovalMatrixLevelDTO();
			lvl.setOrder(level.getLevelOrder());
			lvl.setRouting(level.getRoutingMode());
			lvl.setAssigneeEmployeeId(level.getSpecificEmployeeId());
			lvl.setDepartmentIds(
					levelDeptRepository.findByLevelId(level.getLevelId()).stream().map(ReimbursementApprovalMatrixLevelDept::getDeptId)
							.collect(Collectors.toList()));
			lvl.setJobRoleIds(
					levelRoleRepository.findByLevelId(level.getLevelId()).stream()
							.map(ReimbursementApprovalMatrixLevelRole::getJobRoleId).collect(Collectors.toList()));
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
