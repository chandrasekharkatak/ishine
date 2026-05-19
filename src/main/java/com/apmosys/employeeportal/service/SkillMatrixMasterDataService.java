package com.apmosys.employeeportal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.core.JdbcTemplate;

import com.apmosys.employeeportal.dto.SkillCategorySaveRequest;
import com.apmosys.employeeportal.dto.SkillMatrixAspirationChipMasterListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixAspirationChipSaveRequest;
import com.apmosys.employeeportal.dto.SkillDomainSaveRequest;
import com.apmosys.employeeportal.dto.SkillMatrixCategoryListDTO;
import com.apmosys.employeeportal.dto.SkillDomainFeatureSaveRequest;
import com.apmosys.employeeportal.dto.SkillMatrixDomainFeatureListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixDomainListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixSkillListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixSubdomainListDTO;
import com.apmosys.employeeportal.dto.SkillMatrixSubskillListDTO;
import com.apmosys.employeeportal.dto.SkillMasterSaveRequest;
import com.apmosys.employeeportal.dto.SkillSubdomainSaveRequest;
import com.apmosys.employeeportal.dto.SubskillMasterSaveRequest;
import com.apmosys.employeeportal.model.SkillCategoryMaster;
import com.apmosys.employeeportal.model.SkillDomainFeatureMaster;
import com.apmosys.employeeportal.model.SkillDomainMaster;
import com.apmosys.employeeportal.model.SkillSubdomainMaster;
import com.apmosys.employeeportal.model.SkillsMaster;
import com.apmosys.employeeportal.model.SubskillsMaster;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.SkillCategoryMasterRepository;
import com.apmosys.employeeportal.repository.SkillDomainFeatureMasterRepository;
import com.apmosys.employeeportal.repository.SkillDomainMasterRepository;
import com.apmosys.employeeportal.repository.SkillSubdomainMasterRepository;
import com.apmosys.employeeportal.repository.SkillsMasterRepository;
import com.apmosys.employeeportal.repository.SubskillsMasterRepository;
import com.apmosys.employeeportal.skillmatrix.SkillMatrixMasterSpecifications;

@Service
public class SkillMatrixMasterDataService {

	private static final int DEFAULT_SIZE = 15;
	private static final int MAX_SIZE = 100;

	@Autowired
	private SkillCategoryMasterRepository skillCategoryMasterRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private SkillsMasterRepository skillsMasterRepository;

	@Autowired
	private SubskillsMasterRepository subskillsMasterRepository;

	@Autowired
	private SkillDomainMasterRepository skillDomainMasterRepository;

	@Autowired
	private SkillSubdomainMasterRepository skillSubdomainMasterRepository;

	@Autowired
	private SkillDomainFeatureMasterRepository skillDomainFeatureMasterRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private int normalizeSize(int size) {
		if (size <= 0) {
			return DEFAULT_SIZE;
		}
		return Math.min(size, MAX_SIZE);
	}

	/**
	 * Builds a page request with sort from optional API params. {@code sortColumn} must match a key in
	 * {@code allowedApiToProperty} (case-insensitive); otherwise {@code defaultProperty} ascending is used.
	 */
	private Pageable pageableWithSort(int page, int size, String sortColumn, String sortDirection,
			Map<String, String> allowedApiToProperty, String defaultProperty) {
		int p = Math.max(0, page);
		int s = normalizeSize(size);
		String dirRaw = sortDirection != null ? sortDirection.trim() : "";
		Sort.Direction direction = "desc".equalsIgnoreCase(dirRaw) ? Sort.Direction.DESC : Sort.Direction.ASC;
		String key = sortColumn != null ? sortColumn.trim() : "";
		String jpaProperty = null;
		if (StringUtils.hasText(key)) {
			jpaProperty = allowedApiToProperty.get(key.toLowerCase(Locale.ROOT));
		}
		if (!StringUtils.hasText(jpaProperty)) {
			return PageRequest.of(p, s, Sort.by(Sort.Direction.ASC, defaultProperty));
		}
		return PageRequest.of(p, s, Sort.by(direction, jpaProperty));
	}

	@Transactional(readOnly = true)
	public Page<SkillMatrixCategoryListDTO> listSkillCategories(int page, int size, String categoryId,
			String categoryName, String sortColumn, String sortDirection) {
		Map<String, String> allowed = new HashMap<>();
		allowed.put("categoryid", "categoryId");
		allowed.put("categoryname", "categoryName");
		Pageable pageable = pageableWithSort(page, size, sortColumn, sortDirection, allowed, "categoryName");
		Specification<SkillCategoryMaster> spec = SkillMatrixMasterSpecifications.skillCategoryFilter(categoryId,
				categoryName);
		return skillCategoryMasterRepository.findAll(spec, pageable)
				.map(c -> new SkillMatrixCategoryListDTO(c.getCategoryId(), c.getCategoryName()));
	}

	@Transactional(readOnly = true)
	public Page<SkillMatrixSkillListDTO> listSkills(int page, int size, String skillId, String skillName,
			String categoryName, String skillType, String activeDisplay, String departmentName, String sortColumn,
			String sortDirection) {
		Map<String, String> allowed = new HashMap<>();
		allowed.put("skillid", "skillId");
		allowed.put("skillname", "skillName");
		allowed.put("categoryid", "categoryId");
		allowed.put("categoryname", "skillCategory.categoryName");
		allowed.put("skilltype", "skillType");
		allowed.put("activedisplay", "isActive");
		allowed.put("departmentid", "departmentId");
		allowed.put("departmentname", "department.name");
		Pageable pageable = pageableWithSort(page, size, sortColumn, sortDirection, allowed, "skillName");
		Specification<SkillsMaster> spec = SkillMatrixMasterSpecifications.skillsMasterFilter(skillId, skillName,
				categoryName, skillType, activeDisplay, departmentName);
		return skillsMasterRepository.findAll(spec, pageable).map(s -> {
			String cn = "";
			if (s.getSkillCategory() != null) {
				cn = s.getSkillCategory().getCategoryName();
			}
			return new SkillMatrixSkillListDTO(s.getSkillId(), s.getSkillName(), s.getCategoryId(), cn,
					s.getSkillType(), s.getIsActive(), s.getDepartmentId());
		});
	}

	@Transactional(readOnly = true)
	public Page<SkillMatrixSubskillListDTO> listSubskills(int page, int size, String subskillId, String subskillName,
			String skillName, String activeDisplay, String sortColumn, String sortDirection) {
		Map<String, String> allowed = new HashMap<>();
		allowed.put("subskillid", "subskillId");
		allowed.put("subskillname", "subskillName");
		allowed.put("skillid", "skillId");
		allowed.put("skillname", "skill.skillName");
		allowed.put("activedisplay", "isActive");
		Pageable pageable = pageableWithSort(page, size, sortColumn, sortDirection, allowed, "subskillName");
		Specification<SubskillsMaster> spec = SkillMatrixMasterSpecifications.subskillsMasterFilter(subskillId,
				subskillName, skillName, activeDisplay);
		return subskillsMasterRepository.findAll(spec, pageable).map(su -> {
			String sn = "";
			if (su.getSkill() != null) {
				sn = su.getSkill().getSkillName();
			}
			return new SkillMatrixSubskillListDTO(su.getSubskillId(), su.getSkillId(), sn, su.getSubskillName(),
					su.getIsActive());
		});
	}

	@Transactional(readOnly = true)
	public Page<SkillMatrixDomainListDTO> listSkillDomains(int page, int size, String domainId, String domainName,
			String sortColumn, String sortDirection) {
		Map<String, String> allowed = new HashMap<>();
		allowed.put("domainid", "domainId");
		allowed.put("domainname", "domainName");
		Pageable pageable = pageableWithSort(page, size, sortColumn, sortDirection, allowed, "domainName");
		Specification<SkillDomainMaster> spec = SkillMatrixMasterSpecifications.skillDomainFilter(domainId, domainName);
		return skillDomainMasterRepository.findAll(spec, pageable)
				.map(d -> new SkillMatrixDomainListDTO(d.getDomainId(), d.getDomainName()));
	}

	@Transactional(readOnly = true)
	public Page<SkillMatrixSubdomainListDTO> listSkillSubdomains(int page, int size, String subdomainId,
			String subdomainName, String domainName, String activeDisplay, Integer domainIdFilter, String sortColumn,
			String sortDirection) {
		Map<String, String> allowed = new HashMap<>();
		allowed.put("subdomainid", "subdomainId");
		allowed.put("subdomainname", "subdomainName");
		allowed.put("domainid", "domainId");
		allowed.put("domainname", "domain.domainName");
		allowed.put("activedisplay", "isActive");
		Pageable pageable = pageableWithSort(page, size, sortColumn, sortDirection, allowed, "subdomainName");
		Specification<SkillSubdomainMaster> spec = SkillMatrixMasterSpecifications.skillSubdomainFilter(subdomainId,
				subdomainName, domainName, activeDisplay, domainIdFilter);
		return skillSubdomainMasterRepository.findAll(spec, pageable).map(su -> {
			String dn = "";
			if (su.getDomain() != null) {
				dn = su.getDomain().getDomainName();
			}
			return new SkillMatrixSubdomainListDTO(su.getSubdomainId(), su.getDomainId(), dn, su.getSubdomainName(),
					su.getIsActive());
		});
	}

	@Transactional(readOnly = true)
	public Page<SkillMatrixDomainFeatureListDTO> listSkillDomainFeatures(int page, int size, String featureId,
			String featureName, String domainName, String subdomainName, String activeDisplay, String sortColumn,
			String sortDirection) {
		Map<String, String> allowed = new HashMap<>();
		allowed.put("featureid", "featureId");
		allowed.put("featurename", "featureName");
		allowed.put("domainname", "domain.domainName");
		allowed.put("subdomainname", "subdomain.subdomainName");
		allowed.put("activedisplay", "isActive");
		Pageable pageable = pageableWithSort(page, size, sortColumn, sortDirection, allowed, "featureName");
		Specification<SkillDomainFeatureMaster> spec = SkillMatrixMasterSpecifications.skillDomainFeatureFilter(
				featureId, featureName, domainName, subdomainName, activeDisplay);
		return skillDomainFeatureMasterRepository.findAll(spec, pageable).map(f -> {
			String dn = "";
			if (f.getDomain() != null) {
				dn = f.getDomain().getDomainName();
			}
			Integer sid = f.getSubdomainId();
			String sn = null;
			if (f.getSubdomain() != null) {
				sn = f.getSubdomain().getSubdomainName();
			}
			return new SkillMatrixDomainFeatureListDTO(f.getFeatureId(), f.getDomainId(), dn, sid, sn,
					f.getFeatureName(), f.getIsActive());
		});
	}

	@Transactional
	public void createSkillCategory(SkillCategorySaveRequest body) {
		if (body == null || !StringUtils.hasText(body.getCategoryName())) {
			throw new IllegalArgumentException("Category name is required");
		}
		String name = body.getCategoryName().trim();
		if (skillCategoryMasterRepository.existsByCategoryNameIgnoreCase(name)) {
			throw new IllegalStateException("A category with this name already exists");
		}
		SkillCategoryMaster e = new SkillCategoryMaster();
		e.setCategoryName(name);
		skillCategoryMasterRepository.save(e);
	}

	@Transactional
	public void updateSkillCategory(Integer id, SkillCategorySaveRequest body) {
		if (body == null || !StringUtils.hasText(body.getCategoryName())) {
			throw new IllegalArgumentException("Category name is required");
		}
		SkillCategoryMaster e = skillCategoryMasterRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Category not found"));
		String name = body.getCategoryName().trim();
		if (skillCategoryMasterRepository.existsByCategoryNameIgnoreCaseAndCategoryIdNot(name, id)) {
			throw new IllegalStateException("A category with this name already exists");
		}
		e.setCategoryName(name);
		skillCategoryMasterRepository.save(e);
	}

	@Transactional
	public void deleteSkillCategory(Integer id) {
		if (!skillCategoryMasterRepository.existsById(id)) {
			throw new IllegalArgumentException("Category not found");
		}
		try {
			skillCategoryMasterRepository.deleteById(id);
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalStateException("Cannot delete: skills or other data still reference this category.");
		}
	}

	@Transactional
	public void createSkill(SkillMasterSaveRequest body) {
		validateSkillBody(body);
		SkillsMaster s = new SkillsMaster();
		applySkillFields(s, body);
		skillsMasterRepository.save(s);
	}

	@Transactional
	public void updateSkill(Integer id, SkillMasterSaveRequest body) {
		SkillsMaster s = skillsMasterRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Skill not found"));
		validateSkillBody(body);
		applySkillFields(s, body);
		skillsMasterRepository.save(s);
	}

	@Transactional
	public void deleteSkill(Integer id) {
		if (!skillsMasterRepository.existsById(id)) {
			throw new IllegalArgumentException("Skill not found");
		}
		try {
			skillsMasterRepository.deleteById(id);
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalStateException("Cannot delete: subskills still reference this skill.");
		}
	}

	@Transactional
	public void createSubskill(SubskillMasterSaveRequest body) {
		validateSubskillBody(body);
		SubskillsMaster su = new SubskillsMaster();
		applySubskillFields(su, body);
		subskillsMasterRepository.save(su);
	}

	@Transactional
	public void updateSubskill(Integer id, SubskillMasterSaveRequest body) {
		SubskillsMaster su = subskillsMasterRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Subskill not found"));
		validateSubskillBody(body);
		applySubskillFields(su, body);
		subskillsMasterRepository.save(su);
	}

	@Transactional
	public void deleteSubskill(Integer id) {
		if (!subskillsMasterRepository.existsById(id)) {
			throw new IllegalArgumentException("Subskill not found");
		}
		subskillsMasterRepository.deleteById(id);
	}

	@Transactional
	public void createSkillDomain(SkillDomainSaveRequest body) {
		if (body == null || !StringUtils.hasText(body.getDomainName())) {
			throw new IllegalArgumentException("Domain name is required");
		}
		String name = body.getDomainName().trim();
		if (skillDomainMasterRepository.existsByDomainNameIgnoreCase(name)) {
			throw new IllegalStateException("A domain with this name already exists");
		}
		SkillDomainMaster e = new SkillDomainMaster();
		e.setDomainName(name);
		skillDomainMasterRepository.save(e);
	}

	@Transactional
	public void updateSkillDomain(Integer id, SkillDomainSaveRequest body) {
		if (body == null || !StringUtils.hasText(body.getDomainName())) {
			throw new IllegalArgumentException("Domain name is required");
		}
		SkillDomainMaster e = skillDomainMasterRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Domain not found"));
		String name = body.getDomainName().trim();
		if (skillDomainMasterRepository.existsByDomainNameIgnoreCaseAndDomainIdNot(name, id)) {
			throw new IllegalStateException("A domain with this name already exists");
		}
		e.setDomainName(name);
		skillDomainMasterRepository.save(e);
	}

	@Transactional
	public void deleteSkillDomain(Integer id) {
		if (!skillDomainMasterRepository.existsById(id)) {
			throw new IllegalArgumentException("Domain not found");
		}
		if (skillSubdomainMasterRepository.existsByDomainId(id)) {
			throw new IllegalStateException("Cannot delete: sub domains still exist for this domain.");
		}
		if (skillDomainFeatureMasterRepository.existsByDomainId(id)) {
			throw new IllegalStateException("Cannot delete: domain features still reference this domain.");
		}
		skillDomainMasterRepository.deleteById(id);
	}

	@Transactional
	public void createSkillSubdomain(SkillSubdomainSaveRequest body) {
		validateSkillSubdomainBody(body);
		SkillSubdomainMaster su = new SkillSubdomainMaster();
		applySkillSubdomainFields(su, body);
		skillSubdomainMasterRepository.save(su);
	}

	@Transactional
	public void updateSkillSubdomain(Integer id, SkillSubdomainSaveRequest body) {
		SkillSubdomainMaster su = skillSubdomainMasterRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Sub domain not found"));
		validateSkillSubdomainBody(body);
		applySkillSubdomainFields(su, body);
		skillSubdomainMasterRepository.save(su);
	}

	@Transactional
	public void deleteSkillSubdomain(Integer id) {
		if (!skillSubdomainMasterRepository.existsById(id)) {
			throw new IllegalArgumentException("Sub domain not found");
		}
		if (skillDomainFeatureMasterRepository.existsBySubdomainId(id)) {
			throw new IllegalStateException("Cannot delete: features still reference this sub domain.");
		}
		skillSubdomainMasterRepository.deleteById(id);
	}

	private void validateSkillSubdomainBody(SkillSubdomainSaveRequest body) {
		if (body == null || !StringUtils.hasText(body.getSubdomainName())) {
			throw new IllegalArgumentException("Sub domain name is required");
		}
		if (body.getDomainId() == null) {
			throw new IllegalArgumentException("Domain is required");
		}
		if (!skillDomainMasterRepository.existsById(body.getDomainId())) {
			throw new IllegalArgumentException("Invalid domain");
		}
	}

	private void applySkillSubdomainFields(SkillSubdomainMaster su, SkillSubdomainSaveRequest body) {
		su.setDomainId(body.getDomainId());
		su.setSubdomainName(body.getSubdomainName().trim());
		su.setIsActive(body.getIsActive() != null ? body.getIsActive() : Boolean.TRUE);
	}

	@Transactional
	public void createSkillDomainFeature(SkillDomainFeatureSaveRequest body) {
		validateSkillDomainFeatureBody(body);
		SkillDomainFeatureMaster f = new SkillDomainFeatureMaster();
		applySkillDomainFeatureFields(f, body);
		skillDomainFeatureMasterRepository.save(f);
	}

	@Transactional
	public void updateSkillDomainFeature(Integer id, SkillDomainFeatureSaveRequest body) {
		SkillDomainFeatureMaster f = skillDomainFeatureMasterRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Feature not found"));
		validateSkillDomainFeatureBody(body);
		applySkillDomainFeatureFields(f, body);
		skillDomainFeatureMasterRepository.save(f);
	}

	@Transactional
	public void deleteSkillDomainFeature(Integer id) {
		if (!skillDomainFeatureMasterRepository.existsById(id)) {
			throw new IllegalArgumentException("Feature not found");
		}
		skillDomainFeatureMasterRepository.deleteById(id);
	}

	private void validateSkillDomainFeatureBody(SkillDomainFeatureSaveRequest body) {
		if (body == null || !StringUtils.hasText(body.getFeatureName())) {
			throw new IllegalArgumentException("Feature name is required");
		}
		if (body.getDomainId() == null) {
			throw new IllegalArgumentException("Domain is required");
		}
		if (!skillDomainMasterRepository.existsById(body.getDomainId())) {
			throw new IllegalArgumentException("Invalid domain");
		}
		if (body.getSubdomainId() != null) {
			SkillSubdomainMaster su = skillSubdomainMasterRepository.findById(body.getSubdomainId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid sub domain"));
			if (!body.getDomainId().equals(su.getDomainId())) {
				throw new IllegalArgumentException("Sub domain must belong to the selected domain");
			}
		}
	}

	private void applySkillDomainFeatureFields(SkillDomainFeatureMaster f, SkillDomainFeatureSaveRequest body) {
		f.setDomainId(body.getDomainId());
		f.setSubdomainId(body.getSubdomainId());
		f.setFeatureName(body.getFeatureName().trim());
		f.setIsActive(body.getIsActive() != null ? body.getIsActive() : Boolean.TRUE);
	}

	private void validateSkillBody(SkillMasterSaveRequest body) {
		if (body == null || !StringUtils.hasText(body.getSkillName())) {
			throw new IllegalArgumentException("Skill name is required");
		}
		if (body.getCategoryId() == null) {
			throw new IllegalArgumentException("Category is required");
		}
		if (!skillCategoryMasterRepository.existsById(body.getCategoryId())) {
			throw new IllegalArgumentException("Invalid category");
		}
		if (body.getDepartmentId() == null) {
			throw new IllegalArgumentException("Department ID is required");
		}
	}

	private void applySkillFields(SkillsMaster s, SkillMasterSaveRequest body) {
		s.setSkillName(body.getSkillName().trim());
		s.setCategoryId(body.getCategoryId());
		String st = body.getSkillType();
		s.setSkillType(StringUtils.hasText(st) ? st.trim() : "Optional");
		s.setIsActive(body.getIsActive() != null ? body.getIsActive() : Boolean.TRUE);
		s.setDepartmentId(body.getDepartmentId());
	}

	private void validateSubskillBody(SubskillMasterSaveRequest body) {
		if (body == null || !StringUtils.hasText(body.getSubskillName())) {
			throw new IllegalArgumentException("Subskill name is required");
		}
		if (body.getSkillId() == null) {
			throw new IllegalArgumentException("Skill is required");
		}
		if (!skillsMasterRepository.existsById(body.getSkillId())) {
			throw new IllegalArgumentException("Invalid skill");
		}
	}

	private void applySubskillFields(SubskillsMaster su, SubskillMasterSaveRequest body) {
		su.setSkillId(body.getSkillId());
		su.setSubskillName(body.getSubskillName().trim());
		su.setIsActive(body.getIsActive() != null ? body.getIsActive() : Boolean.TRUE);
	}

	// ------------------------------------------------------------------
	// Submit Step 5: aspiration chips (JDBC master; no JPA entity)
	// ------------------------------------------------------------------

	@Transactional(readOnly = true)
	public Page<SkillMatrixAspirationChipMasterListDTO> listAspirationChips(int page,
			int size, String chipId, String deptId, String chipLabel, String departmentName, String activeDisplay,
			String sortColumn, String sortDirection) {
		int p = Math.max(0, page);
		int s = normalizeSize(size);
		StringBuilder where = new StringBuilder(" WHERE 1=1 ");
		List<Object> args = new ArrayList<>();
		appendAspirationChipFilters(where, args, chipId, deptId, chipLabel, departmentName, activeDisplay);
		String orderBy = aspirationChipOrderBySql(sortColumn, sortDirection);
		String baseFrom = "FROM skillmatrix_aspiration_chip_master c "
				+ "LEFT JOIN department d ON d.dept_id = c.dept_id AND c.dept_id <> 0 ";
		Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) " + baseFrom + where, Long.class, args.toArray());
		if (total == null) {
			total = 0L;
		}
		List<Object> listArgs = new ArrayList<>(args);
		listArgs.add(s);
		listArgs.add(p * s);
		String sql = "SELECT c.chip_id, c.dept_id, c.chip_label, c.sort_order, c.is_active, "
				+ "CASE WHEN c.dept_id = 0 THEN 'Global (all departments)' ELSE COALESCE(d.name, '') END AS dept_name "
				+ baseFrom + where + orderBy + " LIMIT ? OFFSET ?";
		List<SkillMatrixAspirationChipMasterListDTO> content = jdbcTemplate.query(sql, (rs, rowNum) -> {
			return new SkillMatrixAspirationChipMasterListDTO(rs.getInt("chip_id"), rs.getLong("dept_id"),
					rs.getString("dept_name"), rs.getString("chip_label"), rs.getInt("sort_order"),
					readMysqlBool(rs, "is_active"));
		}, listArgs.toArray());
		return new PageImpl<>(content, PageRequest.of(p, s), total);
	}

	private static void appendAspirationChipFilters(StringBuilder where, List<Object> args, String chipId,
			String deptId, String chipLabel, String departmentName, String activeDisplay) {
		if (StringUtils.hasText(chipId)) {
			where.append(" AND c.chip_id = ? ");
			args.add(Integer.valueOf(chipId.trim()));
		}
		if (StringUtils.hasText(deptId)) {
			where.append(" AND c.dept_id = ? ");
			args.add(Long.valueOf(deptId.trim()));
		}
		if (StringUtils.hasText(chipLabel)) {
			where.append(" AND LOWER(c.chip_label) LIKE LOWER(?) ");
			args.add("%" + chipLabel.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
		}
		if (StringUtils.hasText(departmentName)) {
			where.append(" AND LOWER(CASE WHEN c.dept_id = 0 THEN 'Global (all departments)' ELSE COALESCE(d.name, '') END) LIKE LOWER(?) ");
			args.add("%" + departmentName.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
		}
		if (StringUtils.hasText(activeDisplay)) {
			String ad = activeDisplay.trim().toLowerCase(Locale.ROOT);
			if ("yes".equals(ad) || "y".equals(ad) || "1".equals(ad) || "true".equals(ad)) {
				where.append(" AND c.is_active = 1 ");
			} else if ("no".equals(ad) || "n".equals(ad) || "0".equals(ad) || "false".equals(ad)) {
				where.append(" AND (c.is_active = 0 OR c.is_active IS NULL) ");
			}
		}
	}

	private static String aspirationChipOrderBySql(String sortColumn, String sortDirection) {
		String dir = "desc".equalsIgnoreCase(sortDirection != null ? sortDirection.trim() : "") ? "DESC" : "ASC";
		String col = sortColumn != null ? sortColumn.trim().toLowerCase(Locale.ROOT) : "";
		String orderCol;
		switch (col) {
		case "chipid":
			orderCol = "c.chip_id";
			break;
		case "deptid":
			orderCol = "c.dept_id";
			break;
		case "chiplabel":
			orderCol = "c.chip_label";
			break;
		case "sortorder":
			orderCol = "c.sort_order";
			break;
		case "activedisplay":
			orderCol = "c.is_active";
			break;
		case "departmentname":
			orderCol = "CASE WHEN c.dept_id = 0 THEN 'Global (all departments)' ELSE COALESCE(d.name, '') END";
			break;
		default:
			orderCol = "c.sort_order";
			dir = "ASC";
		}
		return " ORDER BY " + orderCol + " " + dir + ", c.chip_id ASC ";
	}

	private static boolean readMysqlBool(ResultSet rs, String column) throws SQLException {
		Object v = rs.getObject(column);
		if (v == null) {
			return false;
		}
		if (v instanceof Boolean) {
			return (Boolean) v;
		}
		if (v instanceof Number) {
			return ((Number) v).intValue() != 0;
		}
		String s = v.toString().trim();
		return "1".equals(s) || "true".equalsIgnoreCase(s);
	}

	private void validateAspirationChipSave(SkillMatrixAspirationChipSaveRequest body) {
		if (body == null) {
			throw new IllegalArgumentException("Body is required");
		}
		if (body.getDeptId() == null) {
			throw new IllegalArgumentException("Department is required (use Global with id 0).");
		}
		if (!StringUtils.hasText(body.getChipLabel())) {
			throw new IllegalArgumentException("Chip label is required");
		}
		String label = body.getChipLabel().trim();
		if (label.length() > 200) {
			throw new IllegalArgumentException("Chip label must be at most 200 characters");
		}
		long dept = body.getDeptId();
		if (dept != 0L && !departmentRepository.existsById(dept)) {
			throw new IllegalArgumentException("Invalid department id");
		}
	}

	@Transactional
	public void createAspirationChip(SkillMatrixAspirationChipSaveRequest body) {
		validateAspirationChipSave(body);
		String label = body.getChipLabel().trim();
		int sort = body.getSortOrder() != null ? body.getSortOrder() : 0;
		boolean active = body.getIsActive() != null ? body.getIsActive() : Boolean.TRUE;
		try {
			jdbcTemplate.update(
					"INSERT INTO skillmatrix_aspiration_chip_master (dept_id, chip_label, sort_order, is_active) VALUES (?,?,?,?)",
					body.getDeptId(), label, sort, active ? 1 : 0);
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalStateException("A chip with this label already exists for this department scope.", ex);
		}
	}

	@Transactional
	public void updateAspirationChip(Integer id, SkillMatrixAspirationChipSaveRequest body) {
		if (id == null) {
			throw new IllegalArgumentException("Chip id is required");
		}
		validateAspirationChipSave(body);
		Integer exists = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM skillmatrix_aspiration_chip_master WHERE chip_id = ?", Integer.class, id);
		if (exists == null || exists == 0) {
			throw new IllegalArgumentException("Chip not found");
		}
		String label = body.getChipLabel().trim();
		int sort = body.getSortOrder() != null ? body.getSortOrder() : 0;
		boolean active = body.getIsActive() != null ? body.getIsActive() : Boolean.TRUE;
		try {
			jdbcTemplate.update(
					"UPDATE skillmatrix_aspiration_chip_master SET dept_id=?, chip_label=?, sort_order=?, is_active=? WHERE chip_id=?",
					body.getDeptId(), label, sort, active ? 1 : 0, id);
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalStateException("A chip with this label already exists for this department scope.", ex);
		}
	}

	@Transactional
	public void deleteAspirationChip(Integer id) {
		if (id == null) {
			throw new IllegalArgumentException("Chip id is required");
		}
		int n = jdbcTemplate.update("DELETE FROM skillmatrix_aspiration_chip_master WHERE chip_id = ?", id);
		if (n == 0) {
			throw new IllegalArgumentException("Chip not found");
		}
	}
}
