package com.apmosys.employeeportal.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Date;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.dto.ReimbursementRolePolicyResolveDTO;
import com.apmosys.employeeportal.dto.ReimbursementRolePolicyRowDTO;
import com.apmosys.employeeportal.dto.ReimbursementRolePolicySaveRequestDTO;
import com.apmosys.employeeportal.dto.ReimbursementRolePolicySummaryDTO;
import com.apmosys.employeeportal.dto.ReimbursementTicketClaimInputDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.ReimbursementExpensePolicy;
import com.apmosys.employeeportal.model.ReimbursementExpensePolicyDaySeq;
import com.apmosys.employeeportal.model.ReimbursementExpensePolicyJobRole;
import com.apmosys.employeeportal.model.ReimbursementRolePolicy;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.ReimbursementExpensePolicyDaySeqRepository;
import com.apmosys.employeeportal.repository.ReimbursementExpensePolicyJobRoleRepository;
import com.apmosys.employeeportal.repository.ReimbursementExpensePolicyRepository;
import com.apmosys.employeeportal.repository.ReimbursementRolePolicyRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class ReimbursementRolePolicyService {

	private static final String FOOD_EXPENDITURE = "Food";

	@Autowired
	private ReimbursementRolePolicyRepository policyRepository;

	@Autowired
	private ReimbursementExpensePolicyRepository expensePolicyRepository;

	@Autowired
	private ReimbursementExpensePolicyJobRoleRepository expensePolicyJobRoleRepository;

	@Autowired
	private ReimbursementExpensePolicyDaySeqRepository expensePolicyDaySeqRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Value("${reimbursement.ticket-id.zone:Asia/Kolkata}")
	private String reimbursementPolicyIdZone;

	@Autowired
	private JobRoleRepository jobRoleRepository;

	/** Human-facing policy reference; falls back to numeric PK when not yet migrated. */
	private String policyDisplayRef(ReimbursementExpensePolicy policy) {
		if (policy == null) {
			return "";
		}
		if (StringUtils.hasText(policy.getPolicyNo())) {
			return policy.getPolicyNo();
		}
		return policy.getExpensePolicyId() != null ? String.valueOf(policy.getExpensePolicyId()) : "";
	}

	private String policyDisplayRef(Long expensePolicyId) {
		if (expensePolicyId == null) {
			return "";
		}
		return expensePolicyRepository.findById(expensePolicyId)
				.map(this::policyDisplayRef)
				.orElse(String.valueOf(expensePolicyId));
	}

	/** Allocates the next APM-RMPOL-YYYYMMDD-#### for today (same pattern as reimbursement ticket_no). */
	private String allocateNextPublicPolicyNo() {
		String zoneId = StringUtils.hasText(reimbursementPolicyIdZone) ? reimbursementPolicyIdZone.trim()
				: "Asia/Kolkata";
		ZoneId zone = ZoneId.of(zoneId);
		String dayKey = LocalDate.now(zone).format(DateTimeFormatter.BASIC_ISO_DATE);
		expensePolicyDaySeqRepository.ensureDayRow(dayKey);
		ReimbursementExpensePolicyDaySeq row = expensePolicyDaySeqRepository.findByDayKeyForUpdate(dayKey)
				.orElseThrow(() -> new IllegalStateException("Missing expense policy day sequence for " + dayKey));
		int prev = row.getLastSeq() == null ? 0 : row.getLastSeq();
		int next = prev + 1;
		row.setLastSeq(next);
		expensePolicyDaySeqRepository.saveAndFlush(row);
		return "APM-RMPOL-" + dayKey + "-" + String.format("%04d", next);
	}

	@Transactional(readOnly = true)
	public ServiceResponse listAllPolicySummaries() {
		ServiceResponse resp = new ServiceResponse();
		try {
			List<Long> expensePolicyIds = expensePolicyRepository.findDistinctExpensePolicyIdsWithRules();
			if (expensePolicyIds == null || expensePolicyIds.isEmpty()) {
				resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				resp.setServiceResponse(List.of());
				return resp;
			}
			List<ReimbursementRolePolicySummaryDTO> summaries = new ArrayList<>();
			for (Long expensePolicyId : expensePolicyIds) {
				summaries.add(buildSummaryForExpensePolicy(expensePolicyId));
			}
			summaries.sort((a, b) -> {
				int byId = Long.compare(
						a.getExpensePolicyId() != null ? a.getExpensePolicyId() : 0L,
						b.getExpensePolicyId() != null ? b.getExpensePolicyId() : 0L);
				if (byId != 0) {
					return byId;
				}
				return String.CASE_INSENSITIVE_ORDER.compare(
						a.getJobRoleName() != null ? a.getJobRoleName() : "",
						b.getJobRoleName() != null ? b.getJobRoleName() : "");
			});
			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(summaries);
		} catch (Exception ex) {
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(ex.getMessage());
		}
		return resp;
	}

	@Transactional
	public ServiceResponse deletePoliciesForExpensePolicy(Long expensePolicyId) {
		ServiceResponse resp = new ServiceResponse();
		try {
			if (expensePolicyId == null) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Expense policy id is required.");
				return resp;
			}
			List<Long> mappedRoleIds = expensePolicyJobRoleRepository.findJobRoleIdsByExpensePolicyId(expensePolicyId);
			expensePolicyRepository.deleteById(expensePolicyId);
			Map<String, Object> out = new LinkedHashMap<>();
			out.put("expensePolicyId", expensePolicyId);
			out.put("appliedJobRoleIds", mappedRoleIds);
			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceMessage("Expense policy deleted for " + mappedRoleIds.size() + " job role record(s).");
			resp.setServiceResponse(out);
		} catch (Exception ex) {
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(ex.getMessage());
		}
		return resp;
	}

	@Transactional
	public ServiceResponse deletePoliciesForJobRole(Long jobRoleId) {
		Long expensePolicyId = resolveExpensePolicyIdForJobRole(jobRoleId);
		if (expensePolicyId == null) {
			ServiceResponse resp = new ServiceResponse();
			resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
			resp.setServiceError("No expense policy found for the selected job role.");
			return resp;
		}
		return deletePoliciesForExpensePolicy(expensePolicyId);
	}

	@Transactional(readOnly = true)
	public ServiceResponse fetchPoliciesForExpensePolicy(Long expensePolicyId) {
		ServiceResponse resp = new ServiceResponse();
		try {
			if (expensePolicyId == null) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Expense policy id is required.");
				return resp;
			}
			List<Long> mappedRoleIds = expensePolicyJobRoleRepository.findJobRoleIdsByExpensePolicyId(expensePolicyId);
			List<ReimbursementRolePolicyRowDTO> rows = loadPolicyRowsForExpensePolicy(expensePolicyId);
			ReimbursementExpensePolicy header = expensePolicyRepository.findById(expensePolicyId).orElse(null);
			Map<String, Object> out = new LinkedHashMap<>();
			out.put("expensePolicyId", expensePolicyId);
			out.put("policyNo", policyDisplayRef(header));
			out.put("policies", rows);
			out.put("appliedJobRoleIds", mappedRoleIds);
			out.put("jobRoleIds", collapseRepresentativeJobRoleIds(mappedRoleIds));
			out.put("jobRoleNames", resolveDistinctRoleNames(mappedRoleIds));
			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(out);
		} catch (Exception ex) {
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(ex.getMessage());
		}
		return resp;
	}

	@Transactional(readOnly = true)
	public ServiceResponse fetchPoliciesForJobRole(Long jobRoleId) {
		Long expensePolicyId = resolveExpensePolicyIdForJobRole(jobRoleId);
		if (expensePolicyId == null) {
			ServiceResponse resp = new ServiceResponse();
			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			Map<String, Object> out = new LinkedHashMap<>();
			out.put("jobRoleId", jobRoleId);
			out.put("policies", List.of());
			out.put("appliedJobRoleIds", List.of());
			resp.setServiceResponse(out);
			return resp;
		}
		ServiceResponse resp = fetchPoliciesForExpensePolicy(expensePolicyId);
		if (resp.getServiceResponse() instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> out = (Map<String, Object>) resp.getServiceResponse();
			out.put("jobRoleId", jobRoleId);
		}
		return resp;
	}

	@Transactional
	public ServiceResponse savePoliciesForJobRole(ReimbursementRolePolicySaveRequestDTO req) {
		ServiceResponse resp = new ServiceResponse();
		try {
			List<Long> selectedRepresentativeRoleIds = resolveSelectedRepresentativeRoleIds(req);
			if (selectedRepresentativeRoleIds.isEmpty()) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Select at least one job role.");
				return resp;
			}
			List<Long> targetRoleIds = expandSelectedRolesAcrossDepartments(selectedRepresentativeRoleIds);
			if (targetRoleIds.isEmpty()) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("No valid job roles found for the selection.");
				return resp;
			}

			Long expensePolicyId = req.getExpensePolicyId();
			ReimbursementExpensePolicy header;
			if (expensePolicyId != null) {
				header = expensePolicyRepository.findById(expensePolicyId).orElse(null);
				if (header == null) {
					resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
					resp.setServiceError("Expense policy not found.");
					return resp;
				}
				header.setUpdatedBy(req.getUpdatedBy());
				expensePolicyRepository.save(header);
				policyRepository.deleteByExpensePolicyId(expensePolicyId);
				expensePolicyJobRoleRepository.deleteByExpensePolicyId(expensePolicyId);
			} else {
				header = new ReimbursementExpensePolicy();
				header.setPolicyNo(allocateNextPublicPolicyNo());
				header.setCreatedBy(req.getUpdatedBy());
				header.setUpdatedBy(req.getUpdatedBy());
				header = expensePolicyRepository.save(header);
				expensePolicyId = header.getExpensePolicyId();
			}

			List<ReimbursementExpensePolicyJobRole> conflicts = expensePolicyJobRoleRepository
					.findConflicts(targetRoleIds, expensePolicyId);
			if (conflicts != null && !conflicts.isEmpty()) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError(buildRoleConflictMessage(conflicts));
				return resp;
			}

			for (Long targetRoleId : targetRoleIds) {
				expensePolicyJobRoleRepository.save(new ReimbursementExpensePolicyJobRole(expensePolicyId, targetRoleId));
			}
			savePolicyRules(expensePolicyId, req.getPolicies(), req.getUpdatedBy());

			Map<String, Object> out = new LinkedHashMap<>();
			out.put("expensePolicyId", expensePolicyId);
			out.put("policyNo", policyDisplayRef(header));
			out.put("jobRoleId", selectedRepresentativeRoleIds.get(0));
			out.put("jobRoleIds", selectedRepresentativeRoleIds);
			out.put("jobRoleNames", resolveDistinctRoleNames(targetRoleIds));
			out.put("appliedJobRoleIds", targetRoleIds);
			out.put("policies", loadPolicyRowsForExpensePolicy(expensePolicyId));
			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceMessage("Expense policy saved for " + selectedRepresentativeRoleIds.size()
					+ " role title(s) across " + targetRoleIds.size() + " job role record(s).");
			resp.setServiceResponse(out);
		} catch (Exception ex) {
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(ex.getMessage());
		}
		return resp;
	}

	private List<Long> resolveSelectedRepresentativeRoleIds(ReimbursementRolePolicySaveRequestDTO req) {
		LinkedHashSet<Long> ids = new LinkedHashSet<>();
		if (req.getJobRoleIds() != null) {
			for (Long id : req.getJobRoleIds()) {
				if (id != null && id > 0) {
					ids.add(id);
				}
			}
		}
		if (req.getJobRoleId() != null && req.getJobRoleId() > 0) {
			ids.add(req.getJobRoleId());
		}
		return collapseRepresentativeJobRoleIds(new ArrayList<>(ids));
	}

	private List<Long> collapseRepresentativeJobRoleIds(List<Long> roleIds) {
		if (roleIds == null || roleIds.isEmpty()) {
			return List.of();
		}
		Map<String, Long> representativeByNormName = new TreeMap<>();
		for (Long roleId : roleIds) {
			if (roleId == null) {
				continue;
			}
			JobRole jr = jobRoleRepository.findById(roleId).orElse(null);
			String norm = normalizeRoleName(jr != null ? jr.getName() : null);
			if (!StringUtils.hasText(norm)) {
				norm = "id:" + roleId;
			}
			Long existing = representativeByNormName.get(norm);
			if (existing == null || roleId < existing) {
				representativeByNormName.put(norm, roleId);
			}
		}
		return new ArrayList<>(representativeByNormName.values());
	}

	private List<Long> expandSelectedRolesAcrossDepartments(List<Long> representativeRoleIds) {
		LinkedHashSet<Long> ids = new LinkedHashSet<>();
		for (Long roleId : representativeRoleIds) {
			ids.addAll(resolveJobRoleIdsAcrossDepartments(roleId));
		}
		return new ArrayList<>(ids);
	}

	private void savePolicyRules(Long expensePolicyId, List<ReimbursementRolePolicyRowDTO> policies, Long updatedBy) {
		if (policies == null) {
			return;
		}
		for (ReimbursementRolePolicyRowDTO row : policies) {
			if (row == null || !StringUtils.hasText(row.getPolicyCategory())) {
				continue;
			}
			String cat = row.getPolicyCategory().trim().toUpperCase(Locale.ROOT);
			if (!isValidCategory(cat)) {
				continue;
			}
			ReimbursementRolePolicy p = new ReimbursementRolePolicy();
			p.setExpensePolicyId(expensePolicyId);
			p.setPolicyCategory(cat);
			p.setItemName(StringUtils.hasText(row.getItemName()) ? row.getItemName().trim() : null);
			p.setMaxAmount(row.getMaxAmount());
			p.setIsAllowed(Boolean.FALSE.equals(row.getAllowed()) ? "N" : "Y");
			p.setCreatedBy(updatedBy);
			p.setUpdatedBy(updatedBy);
			if (ReimbursementRolePolicy.CAT_AMOUNT_LIMIT.equals(cat)) {
				if (!StringUtils.hasText(p.getItemName()) || p.getMaxAmount() == null
						|| p.getMaxAmount().compareTo(BigDecimal.ZERO) <= 0) {
					continue;
				}
			} else if (ReimbursementRolePolicy.CAT_VEHICLE_RATE.equals(cat)
					|| ReimbursementRolePolicy.CAT_TRAVEL_MODE_LIMIT.equals(cat)) {
				if (!StringUtils.hasText(p.getItemName()) || p.getMaxAmount() == null
						|| p.getMaxAmount().compareTo(BigDecimal.ZERO) <= 0) {
					continue;
				}
			} else if (!StringUtils.hasText(p.getItemName())) {
				continue;
			}
			policyRepository.save(p);
		}
	}

	@Transactional(readOnly = true)
	public ServiceResponse resolveForEmployee(Long empId) {
		ServiceResponse resp = new ServiceResponse();
		try {
			if (empId == null) {
				resp.setServiceStatus(ServiceResponse.STATUS_FAIL);
				resp.setServiceError("Employee id is required.");
				return resp;
			}
			Employee emp = employeeRepository.findById(empId).orElse(null);
			if (emp == null || emp.getJobRoleId() == null) {
				resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				resp.setServiceResponse(new ReimbursementRolePolicyResolveDTO());
				return resp;
			}
			resp.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			resp.setServiceResponse(buildResolveDto(emp.getJobRoleId()));
		} catch (Exception ex) {
			resp.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			resp.setServiceError(ex.getMessage());
		}
		return resp;
	}

	@Transactional(readOnly = true)
	public ReimbursementRolePolicyResolveDTO buildResolveDto(Long jobRoleId) {
		ReimbursementRolePolicyResolveDTO dto = new ReimbursementRolePolicyResolveDTO();
		dto.setJobRoleId(jobRoleId);
		JobRole jr = jobRoleRepository.findById(jobRoleId).orElse(null);
		dto.setJobRoleName(jr != null ? jr.getName() : null);
		Long expensePolicyId = resolveExpensePolicyIdForJobRole(jobRoleId);
		if (expensePolicyId == null) {
			return dto;
		}
		List<ReimbursementRolePolicy> rows = policyRepository
				.findByExpensePolicyIdOrderByPolicyCategoryAscItemNameAsc(expensePolicyId);
		populateResolveDtoFromPolicies(dto, rows);
		dto.setTravelModeRestricted(hasCategoryRestriction(expensePolicyId, ReimbursementRolePolicy.CAT_TRAVEL_MODE));
		dto.setVehicleTypeRestricted(hasCategoryRestriction(expensePolicyId, ReimbursementRolePolicy.CAT_VEHICLE_TYPE));
		dto.setFoodAllowanceRestricted(hasCategoryRestriction(expensePolicyId, ReimbursementRolePolicy.CAT_FOOD_ALLOWANCE));
		return dto;
	}

	private void populateResolveDtoFromPolicies(ReimbursementRolePolicyResolveDTO dto, List<ReimbursementRolePolicy> rows) {
		for (ReimbursementRolePolicy p : rows) {
			if (p == null || !"Y".equalsIgnoreCase(p.getIsAllowed())) {
				continue;
			}
			switch (p.getPolicyCategory()) {
			case ReimbursementRolePolicy.CAT_AMOUNT_LIMIT:
				if (StringUtils.hasText(p.getItemName()) && p.getMaxAmount() != null) {
					dto.getAmountLimits().put(p.getItemName().trim(), p.getMaxAmount());
				}
				break;
			case ReimbursementRolePolicy.CAT_TRAVEL_MODE:
				if (StringUtils.hasText(p.getItemName())) {
					dto.getAllowedTravelModes().add(p.getItemName().trim());
				}
				break;
			case ReimbursementRolePolicy.CAT_TRAVEL_MODE_LIMIT:
				if (StringUtils.hasText(p.getItemName()) && p.getMaxAmount() != null) {
					dto.getTravelModeDailyLimits().put(p.getItemName().trim(), p.getMaxAmount());
				}
				break;
			case ReimbursementRolePolicy.CAT_VEHICLE_TYPE:
				if (StringUtils.hasText(p.getItemName())) {
					dto.getAllowedVehicleTypes().add(p.getItemName().trim());
				}
				break;
			case ReimbursementRolePolicy.CAT_VEHICLE_RATE:
				if (StringUtils.hasText(p.getItemName()) && p.getMaxAmount() != null
						&& p.getMaxAmount().compareTo(BigDecimal.ZERO) > 0) {
					dto.getVehicleRatesPerKm().put(p.getItemName().trim(), p.getMaxAmount());
				}
				break;
			case ReimbursementRolePolicy.CAT_FOOD_ALLOWANCE:
				if (StringUtils.hasText(p.getItemName())) {
					dto.getAllowedFoodAllowanceTypes().add(p.getItemName().trim());
				}
				break;
			default:
				break;
			}
		}
	}

	public void validateClaimAgainstRolePolicy(ReimbursementTicketClaimInputDTO c, int index, Long jobRoleId) {
		if (jobRoleId == null || c == null) {
			return;
		}
		ReimbursementRolePolicyResolveDTO policy = buildResolveDto(jobRoleId);
		String exp = c.getExpenditureType() != null ? c.getExpenditureType().trim() : "";
		if (StringUtils.hasText(exp) && policy.getAmountLimits().containsKey(exp)
				&& !"Travel".equalsIgnoreCase(exp)) {
			boolean teamMeal = FOOD_EXPENDITURE.equalsIgnoreCase(exp)
					&& isTeamMealFoodAllowanceType(c.getFoodAllowanceType());
			boolean sharedPoolFood = FOOD_EXPENDITURE.equalsIgnoreCase(exp) && !teamMeal;
			if (!sharedPoolFood) {
				BigDecimal perDayLimit = policy.getAmountLimits().get(exp);
				if (perDayLimit != null && c.getAmount() != null) {
					long days = countInclusiveDays(c.getFromDate(), c.getToDate());
					if (days <= 0) {
						throw new IllegalArgumentException("Claim " + index
								+ ": from and to dates are required to validate the per-day amount limit.");
					}
					if (teamMeal) {
						validateTeamMealClaimAmount(c, index, perDayLimit, days);
					} else {
						BigDecimal maxAllowed = perDayLimit.multiply(BigDecimal.valueOf(days));
						if (c.getAmount().compareTo(maxAllowed) > 0) {
							throw new IllegalArgumentException("Claim " + index + ": amount exceeds eligible limit of ₹ "
									+ maxAllowed.stripTrailingZeros().toPlainString() + " (₹ "
									+ perDayLimit.stripTrailingZeros().toPlainString() + " per day × " + days
									+ " day(s)) for your job role (" + exp + ").");
						}
					}
				}
			}
		}
		if ("Travel".equalsIgnoreCase(exp)) {
			validateTravelModeDailyLimit(c, index, policy);
		}
		if ("Travel".equalsIgnoreCase(exp) && policy.isTravelModeRestricted()) {
			String mode = c.getTravelMode() != null ? c.getTravelMode().trim() : "";
			if (!StringUtils.hasText(mode) || !containsIgnoreCase(policy.getAllowedTravelModes(), mode)) {
				throw new IllegalArgumentException("Claim " + index
						+ ": selected travel mode is not eligible for your job role.");
			}
		}
		if ("Travel".equalsIgnoreCase(exp) && isPersonalVehicleTravelMode(c.getTravelMode())) {
			validatePersonalVehicleClaimAmount(c, index, policy);
		}
		if ("Travel".equalsIgnoreCase(exp) && isPersonalVehicleTravelMode(c.getTravelMode())
				&& policy.isVehicleTypeRestricted()) {
			String vt = c.getVehicleType() != null ? c.getVehicleType().trim() : "";
			if (!StringUtils.hasText(vt) || !containsIgnoreCase(policy.getAllowedVehicleTypes(), vt)) {
				throw new IllegalArgumentException("Claim " + index
						+ ": selected vehicle type is not eligible for your job role.");
			}
		}
		if ("Food".equalsIgnoreCase(exp) && policy.isFoodAllowanceRestricted()) {
			String ft = c.getFoodAllowanceType() != null ? c.getFoodAllowanceType().trim() : "";
			if (!StringUtils.hasText(ft) || !containsIgnoreCase(policy.getAllowedFoodAllowanceTypes(), ft)) {
				throw new IllegalArgumentException("Claim " + index
						+ ": selected food allowance type is not eligible for your job role.");
			}
		}
	}

	public void validateTicketCombinedFoodDailyLimits(List<ReimbursementTicketClaimInputDTO> claims, Long jobRoleId) {
		validateTicketSharedFoodDailyLimits(claims, jobRoleId);
	}

	public void validateTicketSharedFoodDailyLimits(List<ReimbursementTicketClaimInputDTO> claims, Long jobRoleId) {
		if (jobRoleId == null || claims == null || claims.isEmpty()) {
			return;
		}
		ReimbursementRolePolicyResolveDTO policy = buildResolveDto(jobRoleId);
		BigDecimal dailyLimit = policy.getAmountLimits().get(FOOD_EXPENDITURE);
		if (dailyLimit == null) {
			return;
		}
		Map<LocalDate, BigDecimal> dailyTotals = new TreeMap<>();
		for (ReimbursementTicketClaimInputDTO c : claims) {
			if (c == null || !FOOD_EXPENDITURE.equalsIgnoreCase(
					c.getExpenditureType() != null ? c.getExpenditureType().trim() : "")) {
				continue;
			}
			if (isTeamMealFoodAllowanceType(c.getFoodAllowanceType())) {
				continue;
			}
			spreadClaimAmountAcrossDays(c.getFromDate(), c.getToDate(), c.getAmount(), dailyTotals);
		}
		for (Map.Entry<LocalDate, BigDecimal> entry : dailyTotals.entrySet()) {
			if (entry.getValue().compareTo(dailyLimit) > 0) {
				BigDecimal excess = entry.getValue().subtract(dailyLimit).setScale(2, RoundingMode.HALF_UP);
				throw new IllegalArgumentException(
						"Food claims in this ticket exceed your eligible daily limit on "
								+ entry.getKey() + ": applied ₹ "
								+ entry.getValue().stripTrailingZeros().toPlainString()
								+ " vs ₹ " + dailyLimit.stripTrailingZeros().toPlainString()
								+ " allowed per day (₹ " + excess.stripTrailingZeros().toPlainString()
								+ " over the daily cap).");
			}
		}
	}

	private void spreadClaimAmountAcrossDays(Date fromDate, Date toDate, BigDecimal amount,
			Map<LocalDate, BigDecimal> dailyTotals) {
		long days = countInclusiveDays(fromDate, toDate);
		if (days <= 0 || amount == null) {
			return;
		}
		BigDecimal perDay = amount.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);
		LocalDate cursor = toLocalDate(fromDate);
		LocalDate end = toLocalDate(toDate);
		while (!cursor.isAfter(end)) {
			dailyTotals.merge(cursor, perDay, BigDecimal::add);
			cursor = cursor.plusDays(1);
		}
	}

	private boolean isCombinedDailyFoodAllowanceType(String foodAllowanceType) {
		if (isTeamMealFoodAllowanceType(foodAllowanceType)) {
			return false;
		}
		if (!StringUtils.hasText(foodAllowanceType)) {
			return false;
		}
		String normalized = foodAllowanceType.trim().toLowerCase(Locale.ROOT);
		return normalized.contains("breakfast") || normalized.contains("lunch") || normalized.contains("dinner");
	}

	private boolean isTeamMealFoodAllowanceType(String foodAllowanceType) {
		if (!StringUtils.hasText(foodAllowanceType)) {
			return false;
		}
		String normalized = foodAllowanceType.trim().toLowerCase(Locale.ROOT);
		return normalized.contains("team meal") || normalized.contains("working lunch");
	}

	private void validateTeamMealClaimAmount(ReimbursementTicketClaimInputDTO c, int index, BigDecimal perDayLimit,
			long days) {
		Integer members = c.getTeamMemberCount();
		if (members == null || members < 2) {
			throw new IllegalArgumentException("Claim " + index
					+ ": enter the number of team members (minimum 2) for a team meal / working lunch.");
		}
		BigDecimal maxAllowed = perDayLimit.multiply(BigDecimal.valueOf(members)).multiply(BigDecimal.valueOf(days));
		if (c.getAmount().compareTo(maxAllowed) > 0) {
			BigDecimal perMemberPerDay = c.getAmount().divide(BigDecimal.valueOf(members * days), 2,
					RoundingMode.HALF_UP);
			BigDecimal excess = c.getAmount().subtract(maxAllowed).setScale(2, RoundingMode.HALF_UP);
			throw new IllegalArgumentException("Claim " + index + ": team meal amount exceeds eligible limit of ₹ "
					+ maxAllowed.stripTrailingZeros().toPlainString() + " (₹ "
					+ perDayLimit.stripTrailingZeros().toPlainString() + " per member per day × " + members
					+ " member(s) × " + days + " day(s)). Your claim is ₹ "
					+ perMemberPerDay.stripTrailingZeros().toPlainString() + " per member per day (₹ "
					+ excess.stripTrailingZeros().toPlainString() + " over).");
		}
	}

	private void validateTravelModeDailyLimit(ReimbursementTicketClaimInputDTO c, int index,
			ReimbursementRolePolicyResolveDTO policy) {
		if (c == null || c.getAmount() == null) {
			return;
		}
		String mode = c.getTravelMode() != null ? c.getTravelMode().trim() : "";
		if (isPersonalVehicleTravelMode(mode)) {
			return;
		}
		BigDecimal perDayLimit = resolveTravelModeDailyLimit(policy, mode);
		if (perDayLimit == null) {
			return;
		}
		long days = countInclusiveDays(c.getFromDate(), c.getToDate());
		if (days <= 0) {
			throw new IllegalArgumentException("Claim " + index
					+ ": from and to dates are required to validate the per-day travel mode limit.");
		}
		BigDecimal maxAllowed = perDayLimit.multiply(BigDecimal.valueOf(days));
		if (c.getAmount().compareTo(maxAllowed) > 0) {
			throw new IllegalArgumentException("Claim " + index + ": amount exceeds eligible limit of ₹ "
					+ maxAllowed.stripTrailingZeros().toPlainString() + " (₹ "
					+ perDayLimit.stripTrailingZeros().toPlainString() + " per day × " + days
					+ " day(s)) for travel mode \"" + mode + "\".");
		}
	}

	public BigDecimal resolveTravelModeDailyLimit(ReimbursementRolePolicyResolveDTO policy, String travelMode) {
		if (!StringUtils.hasText(travelMode) || policy == null || policy.getTravelModeDailyLimits() == null) {
			return null;
		}
		for (Map.Entry<String, BigDecimal> entry : policy.getTravelModeDailyLimits().entrySet()) {
			if (travelModeKeysMatch(travelMode, entry.getKey())) {
				BigDecimal limit = entry.getValue();
				return limit != null && limit.compareTo(BigDecimal.ZERO) > 0 ? limit : null;
			}
		}
		return null;
	}

	private boolean travelModeKeysMatch(String a, String b) {
		if (!StringUtils.hasText(a) || !StringUtils.hasText(b)) {
			return false;
		}
		return a.trim().equalsIgnoreCase(b.trim());
	}

	private void validatePersonalVehicleClaimAmount(ReimbursementTicketClaimInputDTO c, int index,
			ReimbursementRolePolicyResolveDTO policy) {
		if (c == null || c.getAmount() == null || c.getDistance() == null
				|| c.getDistance().compareTo(java.math.BigInteger.ZERO) <= 0) {
			return;
		}
		BigDecimal rate = resolveVehicleRatePerKm(policy, c.getVehicleType());
		if (rate == null) {
			throw new IllegalArgumentException("Claim " + index
					+ ": per km rate is not configured in Expense Policy for vehicle type \""
					+ c.getVehicleType() + "\". Contact HR.");
		}
		long days = countInclusiveDays(c.getFromDate(), c.getToDate());
		long dayFactor = days > 0 ? days : 1;
		BigDecimal expected = rate.multiply(new BigDecimal(c.getDistance())).multiply(BigDecimal.valueOf(dayFactor));
		if (c.getAmount().subtract(expected).abs().compareTo(new BigDecimal("0.02")) > 0) {
			String dayPart = dayFactor > 1 ? " × " + dayFactor + " day(s)" : "";
			throw new IllegalArgumentException("Claim " + index + ": amount must be ₹ "
					+ expected.stripTrailingZeros().toPlainString() + " (₹ "
					+ rate.stripTrailingZeros().toPlainString() + " per km × " + c.getDistance()
					+ " km" + dayPart + ") for " + c.getVehicleType() + ".");
		}
	}

	public BigDecimal resolveVehicleRatePerKm(ReimbursementRolePolicyResolveDTO policy, String vehicleType) {
		if (!StringUtils.hasText(vehicleType) || policy == null || policy.getVehicleRatesPerKm() == null) {
			return null;
		}
		for (Map.Entry<String, BigDecimal> entry : policy.getVehicleRatesPerKm().entrySet()) {
			if (vehicleTypeKeysMatch(vehicleType, entry.getKey())) {
				BigDecimal rate = entry.getValue();
				return rate != null && rate.compareTo(BigDecimal.ZERO) > 0 ? rate : null;
			}
		}
		return null;
	}

	public boolean isPersonalVehicleTravelMode(String travelMode) {
		if (!StringUtils.hasText(travelMode)) {
			return false;
		}
		String normalized = travelMode.trim().toLowerCase(Locale.ROOT);
		return normalized.contains("own vehicle") || normalized.contains("personal vehicle");
	}

	private boolean vehicleTypeKeysMatch(String a, String b) {
		return normalizeVehicleTypeKey(a).equals(normalizeVehicleTypeKey(b));
	}

	private String normalizeVehicleTypeKey(String vehicleType) {
		if (!StringUtils.hasText(vehicleType)) {
			return "";
		}
		return vehicleType.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
	}

	private List<Long> resolveJobRoleIdsAcrossDepartments(Long jobRoleId) {
		if (jobRoleId == null) {
			return List.of();
		}
		List<JobRole> siblings = jobRoleRepository.findAllWithSameTitleAs(jobRoleId);
		if (siblings == null || siblings.isEmpty()) {
			return List.of(jobRoleId);
		}
		LinkedHashSet<Long> ids = new LinkedHashSet<>();
		ids.add(jobRoleId);
		for (JobRole jr : siblings) {
			if (jr != null && jr.getJobRoleId() != null) {
				ids.add(jr.getJobRoleId());
			}
		}
		return new ArrayList<>(ids);
	}

	private Long resolveExpensePolicyIdForJobRole(Long jobRoleId) {
		if (jobRoleId == null) {
			return null;
		}
		Optional<Long> direct = expensePolicyJobRoleRepository.findExpensePolicyIdByJobRoleId(jobRoleId);
		if (direct.isPresent()) {
			return direct.get();
		}
		for (Long siblingId : resolveJobRoleIdsAcrossDepartments(jobRoleId)) {
			Optional<Long> mapped = expensePolicyJobRoleRepository.findExpensePolicyIdByJobRoleId(siblingId);
			if (mapped.isPresent()) {
				return mapped.get();
			}
		}
		return null;
	}

	private List<ReimbursementRolePolicyRowDTO> loadPolicyRowsForExpensePolicy(Long expensePolicyId) {
		return policyRepository.findByExpensePolicyIdOrderByPolicyCategoryAscItemNameAsc(expensePolicyId).stream()
				.map(this::toRowDto)
				.collect(Collectors.toList());
	}

	private boolean hasCategoryRestriction(Long expensePolicyId, String category) {
		if (expensePolicyId == null || !StringUtils.hasText(category)) {
			return false;
		}
		List<ReimbursementRolePolicy> rows = policyRepository
				.findByExpensePolicyIdOrderByPolicyCategoryAscItemNameAsc(expensePolicyId);
		for (ReimbursementRolePolicy row : rows) {
			if (row != null && category.equalsIgnoreCase(row.getPolicyCategory())) {
				return true;
			}
		}
		return false;
	}

	private boolean containsIgnoreCase(List<String> allowed, String value) {
		if (allowed == null || allowed.isEmpty() || !StringUtils.hasText(value)) {
			return false;
		}
		for (String a : allowed) {
			if (value.equalsIgnoreCase(a != null ? a.trim() : "")) {
				return true;
			}
		}
		return false;
	}

	private long countInclusiveDays(Date fromDate, Date toDate) {
		if (fromDate == null || toDate == null) {
			return 0;
		}
		LocalDate from = toLocalDate(fromDate);
		LocalDate to = toLocalDate(toDate);
		if (to.isBefore(from)) {
			return 0;
		}
		return ChronoUnit.DAYS.between(from, to) + 1;
	}

	private LocalDate toLocalDate(Date date) {
		return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
	}

	private ReimbursementRolePolicyRowDTO toRowDto(ReimbursementRolePolicy p) {
		ReimbursementRolePolicyRowDTO dto = new ReimbursementRolePolicyRowDTO();
		dto.setPolicyCategory(p.getPolicyCategory());
		dto.setItemName(p.getItemName());
		dto.setMaxAmount(p.getMaxAmount());
		dto.setAllowed("Y".equalsIgnoreCase(p.getIsAllowed()));
		return dto;
	}

	private boolean isValidCategory(String cat) {
		return ReimbursementRolePolicy.CAT_AMOUNT_LIMIT.equals(cat)
				|| ReimbursementRolePolicy.CAT_TRAVEL_MODE.equals(cat)
				|| ReimbursementRolePolicy.CAT_TRAVEL_MODE_LIMIT.equals(cat)
				|| ReimbursementRolePolicy.CAT_VEHICLE_TYPE.equals(cat)
				|| ReimbursementRolePolicy.CAT_VEHICLE_RATE.equals(cat)
				|| ReimbursementRolePolicy.CAT_FOOD_ALLOWANCE.equals(cat);
	}

	private ReimbursementRolePolicySummaryDTO buildSummaryForExpensePolicy(Long expensePolicyId) {
		List<Long> mappedRoleIds = expensePolicyJobRoleRepository.findJobRoleIdsByExpensePolicyId(expensePolicyId);
		List<String> roleNames = resolveDistinctRoleNames(mappedRoleIds);
		List<ReimbursementRolePolicy> entities = policyRepository
				.findByExpensePolicyIdOrderByPolicyCategoryAscItemNameAsc(expensePolicyId);
		ReimbursementExpensePolicy header = expensePolicyRepository.findById(expensePolicyId).orElse(null);
		ReimbursementRolePolicySummaryDTO dto = new ReimbursementRolePolicySummaryDTO();
		dto.setExpensePolicyId(expensePolicyId);
		dto.setPolicyNo(policyDisplayRef(header));
		dto.setJobRoleNames(roleNames);
		dto.setJobRoleName(roleNames.isEmpty() ? "—" : String.join(", ", roleNames));
		dto.setRepresentativeJobRoleId(mappedRoleIds.isEmpty() ? null : mappedRoleIds.get(0));
		dto.setPolicyCount(entities != null ? entities.size() : 0);
		dto.setPolicySummary(buildPolicySummaryText(entities));
		ReimbursementRolePolicy latest = findLatestUpdatedPolicy(entities);
		if (latest != null) {
			dto.setUpdatedOn(latest.getUpdatedOn());
			dto.setUpdatedByName(resolveEmployeeName(latest.getUpdatedBy()));
		}
		return dto;
	}

	private List<String> resolveDistinctRoleNames(List<Long> roleIds) {
		if (roleIds == null || roleIds.isEmpty()) {
			return List.of();
		}
		Map<String, String> namesByNorm = new TreeMap<>();
		for (Long roleId : collapseRepresentativeJobRoleIds(roleIds)) {
			JobRole jr = jobRoleRepository.findById(roleId).orElse(null);
			String name = jr != null ? jr.getName() : null;
			String norm = normalizeRoleName(name);
			if (StringUtils.hasText(norm) && !namesByNorm.containsKey(norm)) {
				namesByNorm.put(norm, StringUtils.hasText(name) ? name.trim() : "Role " + roleId);
			}
		}
		return new ArrayList<>(namesByNorm.values());
	}

	private String buildRoleConflictMessage(List<ReimbursementExpensePolicyJobRole> conflicts) {
		List<String> labels = new ArrayList<>();
		for (ReimbursementExpensePolicyJobRole conflict : conflicts) {
			if (conflict == null || conflict.getJobRoleId() == null) {
				continue;
			}
			JobRole jr = jobRoleRepository.findById(conflict.getJobRoleId()).orElse(null);
			String name = jr != null ? jr.getName() : "Role " + conflict.getJobRoleId();
			labels.add(name + " (" + policyDisplayRef(conflict.getExpensePolicyId()) + ")");
		}
		return "These job roles already belong to another expense policy: " + String.join(", ", labels);
	}

	private ReimbursementRolePolicy findLatestUpdatedPolicy(List<ReimbursementRolePolicy> entities) {
		if (entities == null || entities.isEmpty()) {
			return null;
		}
		ReimbursementRolePolicy latest = entities.get(0);
		for (ReimbursementRolePolicy p : entities) {
			if (p.getUpdatedOn() != null
					&& (latest.getUpdatedOn() == null || p.getUpdatedOn().after(latest.getUpdatedOn()))) {
				latest = p;
			}
		}
		return latest;
	}

	private String buildPolicySummaryText(List<ReimbursementRolePolicy> entities) {
		if (entities == null || entities.isEmpty()) {
			return "—";
		}
		int amountLimits = 0;
		int travelModes = 0;
		int travelModeLimits = 0;
		int vehicleTypes = 0;
		int vehicleRates = 0;
		int foodTypes = 0;
		for (ReimbursementRolePolicy p : entities) {
			if (p == null || !StringUtils.hasText(p.getPolicyCategory())) {
				continue;
			}
			String cat = p.getPolicyCategory().trim().toUpperCase(Locale.ROOT);
			switch (cat) {
			case ReimbursementRolePolicy.CAT_AMOUNT_LIMIT:
				amountLimits++;
				break;
			case ReimbursementRolePolicy.CAT_TRAVEL_MODE:
				travelModes++;
				break;
			case ReimbursementRolePolicy.CAT_TRAVEL_MODE_LIMIT:
				travelModeLimits++;
				break;
			case ReimbursementRolePolicy.CAT_VEHICLE_TYPE:
				vehicleTypes++;
				break;
			case ReimbursementRolePolicy.CAT_VEHICLE_RATE:
				vehicleRates++;
				break;
			case ReimbursementRolePolicy.CAT_FOOD_ALLOWANCE:
				foodTypes++;
				break;
			default:
				break;
			}
		}
		List<String> parts = new ArrayList<>();
		if (amountLimits > 0) {
			parts.add(amountLimits + " per-day limit" + (amountLimits == 1 ? "" : "s"));
		}
		if (travelModes > 0) {
			parts.add(travelModes + " travel mode" + (travelModes == 1 ? "" : "s"));
		}
		if (travelModeLimits > 0) {
			parts.add(travelModeLimits + " travel mode limit" + (travelModeLimits == 1 ? "" : "s"));
		}
		if (vehicleTypes > 0) {
			parts.add(vehicleTypes + " vehicle type" + (vehicleTypes == 1 ? "" : "s"));
		}
		if (vehicleRates > 0) {
			parts.add(vehicleRates + " vehicle rate" + (vehicleRates == 1 ? "" : "s"));
		}
		if (foodTypes > 0) {
			parts.add(foodTypes + " food allowance" + (foodTypes == 1 ? "" : "s"));
		}
		return parts.isEmpty() ? "—" : String.join(", ", parts);
	}

	private String resolveEmployeeName(Long empId) {
		if (empId == null) {
			return null;
		}
		Employee emp = employeeRepository.findByEmpId(empId);
		return emp != null ? emp.getName() : null;
	}

	private String normalizeRoleName(String name) {
		if (!StringUtils.hasText(name)) {
			return "";
		}
		return name.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
	}
}
