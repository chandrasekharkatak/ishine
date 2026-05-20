package com.apmosys.employeeportal.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.SkillCategorySaveRequest;
import com.apmosys.employeeportal.dto.SkillDomainFeatureSaveRequest;
import com.apmosys.employeeportal.dto.SkillDomainSaveRequest;
import com.apmosys.employeeportal.dto.SkillMatrixBulkUploadResultDTO;
import com.apmosys.employeeportal.dto.SkillMasterSaveRequest;
import com.apmosys.employeeportal.dto.SkillSubdomainSaveRequest;
import com.apmosys.employeeportal.dto.SubskillMasterSaveRequest;
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.SkillCategoryMaster;
import com.apmosys.employeeportal.model.SkillDomainMaster;
import com.apmosys.employeeportal.model.SkillSubdomainMaster;
import com.apmosys.employeeportal.model.SkillsMaster;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.SkillCategoryMasterRepository;
import com.apmosys.employeeportal.repository.SkillDomainMasterRepository;
import com.apmosys.employeeportal.repository.SkillSubdomainMasterRepository;
import com.apmosys.employeeportal.repository.SkillsMasterRepository;

@Service
public class SkillMatrixMasterBulkUploadService {

	public static final String TYPE_SKILL_CATEGORIES = "skill-categories";
	public static final String TYPE_SKILLS = "skills";
	public static final String TYPE_SUBSKILLS = "subskills";
	public static final String TYPE_SKILL_DOMAINS = "skill-domains";
	public static final String TYPE_SKILL_SUBDOMAINS = "skill-subdomains";
	public static final String TYPE_SKILL_DOMAIN_FEATURES = "skill-domain-features";

	private static final int MAX_DATA_ROWS = 2500;
	private static final int MAX_ROW_ERRORS_RETURNED = 80;
	private static final Pattern DIGITS_ONLY = Pattern.compile("^\\d+$");

	private final DataFormatter dataFormatter = new DataFormatter();

	@Autowired
	private SkillMatrixMasterDataService skillMatrixMasterDataService;

	@Autowired
	private SkillCategoryMasterRepository skillCategoryMasterRepository;

	@Autowired
	private SkillsMasterRepository skillsMasterRepository;

	@Autowired
	private SkillDomainMasterRepository skillDomainMasterRepository;

	@Autowired
	private SkillSubdomainMasterRepository skillSubdomainMasterRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	public String templateFilename(String masterType) {
		switch (normalizeMasterType(masterType)) {
		case TYPE_SKILL_CATEGORIES:
			return "skill-category-master-template.xlsx";
		case TYPE_SKILLS:
			return "skills-master-template.xlsx";
		case TYPE_SUBSKILLS:
			return "subskills-master-template.xlsx";
		case TYPE_SKILL_DOMAINS:
			return "skill-domain-master-template.xlsx";
		case TYPE_SKILL_SUBDOMAINS:
			return "skill-subdomain-master-template.xlsx";
		case TYPE_SKILL_DOMAIN_FEATURES:
			return "skill-domain-feature-master-template.xlsx";
		default:
			throw new IllegalArgumentException("Unknown master type");
		}
	}

	public byte[] buildTemplate(String masterType) throws IOException {
		switch (normalizeMasterType(masterType)) {
		case TYPE_SKILL_CATEGORIES:
			return buildCategoryTemplateBytes();
		case TYPE_SKILLS:
			return buildSkillsTemplateBytes();
		case TYPE_SUBSKILLS:
			return buildSubskillsTemplateBytes();
		case TYPE_SKILL_DOMAINS:
			return buildDomainsTemplateBytes();
		case TYPE_SKILL_SUBDOMAINS:
			return buildSubdomainsTemplateBytes();
		case TYPE_SKILL_DOMAIN_FEATURES:
			return buildDomainFeaturesTemplateBytes();
		default:
			throw new IllegalArgumentException("Unknown master type");
		}
	}

	public SkillMatrixBulkUploadResultDTO processUpload(String masterType, MultipartFile file)
			throws IOException, InvalidFormatException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Please choose an Excel file to upload.");
		}
		String name = file.getOriginalFilename();
		if (name == null || (!name.toLowerCase(Locale.ROOT).endsWith(".xlsx") && !name.toLowerCase(Locale.ROOT).endsWith(".xls"))) {
			throw new IllegalArgumentException("Upload must be an .xlsx or .xls file.");
		}
		switch (normalizeMasterType(masterType)) {
		case TYPE_SKILL_CATEGORIES:
			return processCategories(file);
		case TYPE_SKILLS:
			return processSkills(file);
		case TYPE_SUBSKILLS:
			return processSubskills(file);
		case TYPE_SKILL_DOMAINS:
			return processDomains(file);
		case TYPE_SKILL_SUBDOMAINS:
			return processSubdomains(file);
		case TYPE_SKILL_DOMAIN_FEATURES:
			return processDomainFeatures(file);
		default:
			throw new IllegalArgumentException("Unknown master type");
		}
	}

	private String normalizeMasterType(String masterType) {
		if (!StringUtils.hasText(masterType)) {
			throw new IllegalArgumentException("Invalid master type");
		}
		return masterType.trim().toLowerCase(Locale.ROOT);
	}

	private byte[] buildCategoryTemplateBytes() throws IOException {
		try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			XSSFSheet sh = wb.createSheet("Categories");
			writeDataRow(sh, 0, "Category name");
			wb.write(bos);
			return bos.toByteArray();
		}
	}

	private byte[] buildSkillsTemplateBytes() throws IOException {
		try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			XSSFSheet sh = wb.createSheet("Skills");
			writeDataRow(sh, 0, "Skill name", "Category (ID or name)", "Department (ID or name)", "Skill type (optional)",
					"Active (optional)");
			wb.write(bos);
			return bos.toByteArray();
		}
	}

	private byte[] buildSubskillsTemplateBytes() throws IOException {
		try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			XSSFSheet sh = wb.createSheet("Subskills");
			writeDataRow(sh, 0, "Subskill name", "Skill (ID or name)", "Active (optional)");
			wb.write(bos);
			return bos.toByteArray();
		}
	}

	private byte[] buildDomainsTemplateBytes() throws IOException {
		try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			XSSFSheet sh = wb.createSheet("Domains");
			writeDataRow(sh, 0, "Domain name");
			wb.write(bos);
			return bos.toByteArray();
		}
	}

	private byte[] buildSubdomainsTemplateBytes() throws IOException {
		try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			XSSFSheet sh = wb.createSheet("Subdomains");
			writeDataRow(sh, 0, "Sub domain name", "Domain (ID or name)", "Active (optional)");
			wb.write(bos);
			return bos.toByteArray();
		}
	}

	private byte[] buildDomainFeaturesTemplateBytes() throws IOException {
		try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			XSSFSheet sh = wb.createSheet("Domain features");
			writeDataRow(sh, 0, "Feature name", "Domain (ID or name)", "Sub domain (ID or name, optional)",
					"Active (optional)");
			wb.write(bos);
			return bos.toByteArray();
		}
	}

	private void writeDataRow(XSSFSheet sh, int rowIndex, String... values) {
		XSSFRow row = sh.createRow(rowIndex);
		for (int i = 0; i < values.length; i++) {
			row.createCell(i).setCellValue(values[i] != null ? values[i] : "");
		}
	}

	private SkillMatrixBulkUploadResultDTO processCategories(MultipartFile file)
			throws IOException, InvalidFormatException {
		SkillMatrixBulkUploadResultDTO out = new SkillMatrixBulkUploadResultDTO();
		try (InputStream in = file.getInputStream(); Workbook wb = WorkbookFactory.create(in)) {
			Sheet sheet = wb.getSheetAt(0);
			Map<String, Integer> headers = readHeaderMap(sheet.getRow(0));
			Integer colCat = findColumn(headers, "Category name");
			if (colCat == null) {
				throw new IllegalArgumentException("Missing required column header: Category name");
			}
			int last = Math.min(sheet.getLastRowNum(), MAX_DATA_ROWS);
			for (int r = 1; r <= last; r++) {
				Row row = sheet.getRow(r);
				if (rowBlank(row)) {
					continue;
				}
				int excelRow = r + 1;
				String catName = cellVal(row, colCat);
				if (!StringUtils.hasText(catName)) {
					appendRowError(out, excelRow, "Category name is required");
					continue;
				}
				try {
					SkillCategorySaveRequest body = new SkillCategorySaveRequest();
					body.setCategoryName(catName.trim());
					skillMatrixMasterDataService.createSkillCategory(body);
					out.setInserted(out.getInserted() + 1);
				} catch (IllegalArgumentException | IllegalStateException | DataIntegrityViolationException ex) {
					appendRowError(out, excelRow, messageOrDefault(ex));
				}
			}
		}
		return out;
	}

	private SkillMatrixBulkUploadResultDTO processSkills(MultipartFile file)
			throws IOException, InvalidFormatException {
		SkillMatrixBulkUploadResultDTO out = new SkillMatrixBulkUploadResultDTO();
		try (InputStream in = file.getInputStream(); Workbook wb = WorkbookFactory.create(in)) {
			Sheet sheet = wb.getSheetAt(0);
			Map<String, Integer> headers = readHeaderMap(sheet.getRow(0));
			Integer colSkill = findColumn(headers, "Skill name");
			Integer colCat = findColumn(headers, "Category (ID or name)", "Category ID or name");
			Integer colDept = findColumn(headers, "Department (ID or name)", "Department ID or name");
			Integer colType = findColumn(headers, "Skill type (optional)", "Skill type");
			Integer colActive = findColumn(headers, "Active (optional)", "Active");
			if (colSkill == null || colCat == null || colDept == null) {
				throw new IllegalArgumentException(
						"Missing required column(s). Expected: Skill name, Category (ID or name), Department (ID or name).");
			}
			int last = Math.min(sheet.getLastRowNum(), MAX_DATA_ROWS);
			for (int r = 1; r <= last; r++) {
				Row row = sheet.getRow(r);
				if (rowBlank(row)) {
					continue;
				}
				int excelRow = r + 1;
				String skillName = cellVal(row, colSkill);
				String catRaw = cellVal(row, colCat);
				String deptRaw = cellVal(row, colDept);
				if (!StringUtils.hasText(skillName)) {
					appendRowError(out, excelRow, "Skill name is required");
					continue;
				}
				Integer categoryId = resolveCategoryId(catRaw);
				if (categoryId == null) {
					appendRowError(out, excelRow, "Unknown or invalid category (use ID or exact name).");
					continue;
				}
				Long departmentId = resolveDepartmentId(deptRaw);
				if (departmentId == null) {
					appendRowError(out, excelRow, "Unknown or invalid department (use department ID or name).");
					continue;
				}
				String skillType = colType != null ? cellVal(row, colType) : "";
				Boolean active = colActive != null ? parseActive(cellVal(row, colActive), true) : Boolean.TRUE;
				try {
					SkillMasterSaveRequest body = new SkillMasterSaveRequest();
					body.setSkillName(skillName.trim());
					body.setCategoryId(categoryId);
					body.setDepartmentId(departmentId);
					body.setSkillType(StringUtils.hasText(skillType) ? skillType.trim() : "Optional");
					body.setIsActive(active);
					skillMatrixMasterDataService.createSkill(body);
					out.setInserted(out.getInserted() + 1);
				} catch (IllegalArgumentException | IllegalStateException | DataIntegrityViolationException ex) {
					appendRowError(out, excelRow, messageOrDefault(ex));
				}
			}
		}
		return out;
	}

	private SkillMatrixBulkUploadResultDTO processSubskills(MultipartFile file)
			throws IOException, InvalidFormatException {
		SkillMatrixBulkUploadResultDTO out = new SkillMatrixBulkUploadResultDTO();
		try (InputStream in = file.getInputStream(); Workbook wb = WorkbookFactory.create(in)) {
			Sheet sheet = wb.getSheetAt(0);
			Map<String, Integer> headers = readHeaderMap(sheet.getRow(0));
			Integer colSub = findColumn(headers, "Subskill name");
			Integer colSkill = findColumn(headers, "Skill (ID or name)", "Skill ID or name");
			Integer colActive = findColumn(headers, "Active (optional)", "Active");
			if (colSub == null || colSkill == null) {
				throw new IllegalArgumentException("Missing required column(s). Expected: Subskill name, Skill (ID or name).");
			}
			int last = Math.min(sheet.getLastRowNum(), MAX_DATA_ROWS);
			for (int r = 1; r <= last; r++) {
				Row row = sheet.getRow(r);
				if (rowBlank(row)) {
					continue;
				}
				int excelRow = r + 1;
				String subName = cellVal(row, colSub);
				String skillRaw = cellVal(row, colSkill);
				if (!StringUtils.hasText(subName)) {
					appendRowError(out, excelRow, "Subskill name is required");
					continue;
				}
				Integer skillId = resolveSkillId(skillRaw);
				if (skillId == null) {
					appendRowError(out, excelRow, "Unknown or ambiguous skill (use Skill ID or a unique skill name).");
					continue;
				}
				Boolean active = colActive != null ? parseActive(cellVal(row, colActive), true) : Boolean.TRUE;
				try {
					SubskillMasterSaveRequest body = new SubskillMasterSaveRequest();
					body.setSkillId(skillId);
					body.setSubskillName(subName.trim());
					body.setIsActive(active);
					skillMatrixMasterDataService.createSubskill(body);
					out.setInserted(out.getInserted() + 1);
				} catch (IllegalArgumentException | IllegalStateException | DataIntegrityViolationException ex) {
					appendRowError(out, excelRow, messageOrDefault(ex));
				}
			}
		}
		return out;
	}

	private SkillMatrixBulkUploadResultDTO processDomains(MultipartFile file)
			throws IOException, InvalidFormatException {
		SkillMatrixBulkUploadResultDTO out = new SkillMatrixBulkUploadResultDTO();
		try (InputStream in = file.getInputStream(); Workbook wb = WorkbookFactory.create(in)) {
			Sheet sheet = wb.getSheetAt(0);
			Map<String, Integer> headers = readHeaderMap(sheet.getRow(0));
			Integer colName = findColumn(headers, "Domain name");
			if (colName == null) {
				throw new IllegalArgumentException("Missing required column header: Domain name");
			}
			int last = Math.min(sheet.getLastRowNum(), MAX_DATA_ROWS);
			for (int r = 1; r <= last; r++) {
				Row row = sheet.getRow(r);
				if (rowBlank(row)) {
					continue;
				}
				int excelRow = r + 1;
				String domainName = cellVal(row, colName);
				if (!StringUtils.hasText(domainName)) {
					appendRowError(out, excelRow, "Domain name is required");
					continue;
				}
				try {
					SkillDomainSaveRequest body = new SkillDomainSaveRequest();
					body.setDomainName(domainName.trim());
					skillMatrixMasterDataService.createSkillDomain(body);
					out.setInserted(out.getInserted() + 1);
				} catch (IllegalArgumentException | IllegalStateException | DataIntegrityViolationException ex) {
					appendRowError(out, excelRow, messageOrDefault(ex));
				}
			}
		}
		return out;
	}

	private SkillMatrixBulkUploadResultDTO processSubdomains(MultipartFile file)
			throws IOException, InvalidFormatException {
		SkillMatrixBulkUploadResultDTO out = new SkillMatrixBulkUploadResultDTO();
		try (InputStream in = file.getInputStream(); Workbook wb = WorkbookFactory.create(in)) {
			Sheet sheet = wb.getSheetAt(0);
			Map<String, Integer> headers = readHeaderMap(sheet.getRow(0));
			Integer colSub = findColumn(headers, "Sub domain name", "Subdomain name");
			Integer colDom = findColumn(headers, "Domain (ID or name)", "Domain ID or name");
			Integer colActive = findColumn(headers, "Active (optional)", "Active");
			if (colSub == null || colDom == null) {
				throw new IllegalArgumentException(
						"Missing required column(s). Expected: Sub domain name, Domain (ID or name).");
			}
			int last = Math.min(sheet.getLastRowNum(), MAX_DATA_ROWS);
			for (int r = 1; r <= last; r++) {
				Row row = sheet.getRow(r);
				if (rowBlank(row)) {
					continue;
				}
				int excelRow = r + 1;
				String subName = cellVal(row, colSub);
				String domRaw = cellVal(row, colDom);
				if (!StringUtils.hasText(subName)) {
					appendRowError(out, excelRow, "Sub domain name is required");
					continue;
				}
				Integer domainId = resolveDomainId(domRaw);
				if (domainId == null) {
					appendRowError(out, excelRow, "Unknown or ambiguous domain (use Domain ID or a unique domain name).");
					continue;
				}
				Boolean active = colActive != null ? parseActive(cellVal(row, colActive), true) : Boolean.TRUE;
				try {
					SkillSubdomainSaveRequest body = new SkillSubdomainSaveRequest();
					body.setDomainId(domainId);
					body.setSubdomainName(subName.trim());
					body.setIsActive(active);
					skillMatrixMasterDataService.createSkillSubdomain(body);
					out.setInserted(out.getInserted() + 1);
				} catch (IllegalArgumentException | IllegalStateException | DataIntegrityViolationException ex) {
					appendRowError(out, excelRow, messageOrDefault(ex));
				}
			}
		}
		return out;
	}

	private SkillMatrixBulkUploadResultDTO processDomainFeatures(MultipartFile file)
			throws IOException, InvalidFormatException {
		SkillMatrixBulkUploadResultDTO out = new SkillMatrixBulkUploadResultDTO();
		try (InputStream in = file.getInputStream(); Workbook wb = WorkbookFactory.create(in)) {
			Sheet sheet = wb.getSheetAt(0);
			Map<String, Integer> headers = readHeaderMap(sheet.getRow(0));
			Integer colFeat = findColumn(headers, "Feature name");
			Integer colDom = findColumn(headers, "Domain (ID or name)", "Domain ID or name");
			Integer colSub = findColumn(headers, "Sub domain (ID or name, optional)", "Sub domain (ID or name)",
					"Subdomain (ID or name, optional)", "Subdomain (ID or name)");
			Integer colActive = findColumn(headers, "Active (optional)", "Active");
			if (colFeat == null || colDom == null) {
				throw new IllegalArgumentException("Missing required column(s). Expected: Feature name, Domain (ID or name).");
			}
			int last = Math.min(sheet.getLastRowNum(), MAX_DATA_ROWS);
			for (int r = 1; r <= last; r++) {
				Row row = sheet.getRow(r);
				if (rowBlank(row)) {
					continue;
				}
				int excelRow = r + 1;
				String featName = cellVal(row, colFeat);
				String domRaw = cellVal(row, colDom);
				if (!StringUtils.hasText(featName)) {
					appendRowError(out, excelRow, "Feature name is required");
					continue;
				}
				Integer domainId = resolveDomainId(domRaw);
				if (domainId == null) {
					appendRowError(out, excelRow, "Unknown or ambiguous domain (use Domain ID or a unique domain name).");
					continue;
				}
				Integer subdomainId = null;
				if (colSub != null) {
					String subRaw = cellVal(row, colSub);
					if (StringUtils.hasText(subRaw)) {
						subdomainId = resolveSubdomainId(domainId, subRaw.trim());
						if (subdomainId == null) {
							appendRowError(out, excelRow,
									"Unknown or invalid sub domain for this domain (use Sub domain ID or name under the selected domain).");
							continue;
						}
					}
				}
				Boolean active = colActive != null ? parseActive(cellVal(row, colActive), true) : Boolean.TRUE;
				try {
					SkillDomainFeatureSaveRequest body = new SkillDomainFeatureSaveRequest();
					body.setDomainId(domainId);
					body.setSubdomainId(subdomainId);
					body.setFeatureName(featName.trim());
					body.setIsActive(active);
					skillMatrixMasterDataService.createSkillDomainFeature(body);
					out.setInserted(out.getInserted() + 1);
				} catch (IllegalArgumentException | IllegalStateException | DataIntegrityViolationException ex) {
					appendRowError(out, excelRow, messageOrDefault(ex));
				}
			}
		}
		return out;
	}

	private Integer resolveCategoryId(String raw) {
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		String t = raw.trim();
		if (DIGITS_ONLY.matcher(t).matches()) {
			int id = Integer.parseInt(t);
			return skillCategoryMasterRepository.existsById(id) ? id : null;
		}
		Optional<SkillCategoryMaster> o = skillCategoryMasterRepository
				.findFirstByCategoryNameIgnoreCaseOrderByCategoryIdAsc(t);
		return o.map(SkillCategoryMaster::getCategoryId).orElse(null);
	}

	private Long resolveDepartmentId(String raw) {
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		String t = raw.trim();
		if (DIGITS_ONLY.matcher(t).matches()) {
			long id = Long.parseLong(t);
			return departmentRepository.findById(id).map(Department::getDeptId).orElse(null);
		}
		Optional<Department> o = departmentRepository.findFirstByNameIgnoreCase(t);
		return o.map(Department::getDeptId).orElse(null);
	}

	private Integer resolveSkillId(String raw) {
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		String t = raw.trim();
		if (DIGITS_ONLY.matcher(t).matches()) {
			int id = Integer.parseInt(t);
			return skillsMasterRepository.existsById(id) ? id : null;
		}
		List<SkillsMaster> list = skillsMasterRepository.findBySkillNameIgnoreCaseOrderBySkillIdAsc(t);
		if (list.isEmpty()) {
			return null;
		}
		if (list.size() > 1) {
			return null;
		}
		return list.get(0).getSkillId();
	}

	private Integer resolveDomainId(String raw) {
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		String t = raw.trim();
		if (DIGITS_ONLY.matcher(t).matches()) {
			int id = Integer.parseInt(t);
			return skillDomainMasterRepository.existsById(id) ? id : null;
		}
		List<SkillDomainMaster> list = skillDomainMasterRepository.findByDomainNameIgnoreCaseOrderByDomainIdAsc(t);
		if (list.isEmpty()) {
			return null;
		}
		if (list.size() > 1) {
			return null;
		}
		return list.get(0).getDomainId();
	}

	private Integer resolveSubdomainId(Integer domainId, String raw) {
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		String t = raw.trim();
		if (DIGITS_ONLY.matcher(t).matches()) {
			int id = Integer.parseInt(t);
			Optional<SkillSubdomainMaster> o = skillSubdomainMasterRepository.findById(id);
			if (!o.isPresent()) {
				return null;
			}
			if (!domainId.equals(o.get().getDomainId())) {
				return null;
			}
			return id;
		}
		List<SkillSubdomainMaster> list = skillSubdomainMasterRepository
				.findByDomainIdAndSubdomainNameIgnoreCaseOrderBySubdomainIdAsc(domainId, t);
		if (list.isEmpty()) {
			return null;
		}
		if (list.size() > 1) {
			return null;
		}
		return list.get(0).getSubdomainId();
	}

	private Map<String, Integer> readHeaderMap(Row headerRow) {
		if (headerRow == null) {
			throw new IllegalArgumentException("The first row must contain column headers.");
		}
		Map<String, Integer> map = new HashMap<>();
		short last = headerRow.getLastCellNum();
		for (int i = 0; i < last; i++) {
			String h = normHeader(cellVal(headerRow, i));
			if (StringUtils.hasText(h)) {
				map.put(h, i);
			}
		}
		if (map.isEmpty()) {
			throw new IllegalArgumentException("The first row must contain column headers.");
		}
		return map;
	}

	private Integer findColumn(Map<String, Integer> headers, String... candidates) {
		for (String c : candidates) {
			String key = normHeader(c);
			if (headers.containsKey(key)) {
				return headers.get(key);
			}
		}
		return null;
	}

	private String normHeader(String s) {
		if (s == null) {
			return "";
		}
		return s.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
	}

	private String cellVal(Row row, int colIdx) {
		if (row == null) {
			return "";
		}
		Cell cell = row.getCell(colIdx);
		if (cell == null) {
			return "";
		}
		return dataFormatter.formatCellValue(cell).trim();
	}

	private boolean rowBlank(Row row) {
		if (row == null) {
			return true;
		}
		for (Cell cell : row) {
			if (StringUtils.hasText(dataFormatter.formatCellValue(cell).trim())) {
				return false;
			}
		}
		return true;
	}

	private Boolean parseActive(String raw, boolean defaultIfBlank) {
		if (!StringUtils.hasText(raw)) {
			return defaultIfBlank;
		}
		String t = raw.trim();
		if ("yes".equalsIgnoreCase(t) || "y".equalsIgnoreCase(t) || "true".equalsIgnoreCase(t) || "1".equals(t)) {
			return Boolean.TRUE;
		}
		if ("no".equalsIgnoreCase(t) || "n".equalsIgnoreCase(t) || "false".equalsIgnoreCase(t) || "0".equals(t)) {
			return Boolean.FALSE;
		}
		return defaultIfBlank;
	}

	private void appendRowError(SkillMatrixBulkUploadResultDTO out, int excelRow1Based, String message) {
		out.setFailed(out.getFailed() + 1);
		if (out.getRowErrors().size() < MAX_ROW_ERRORS_RETURNED) {
			out.getRowErrors().add("Row " + excelRow1Based + ": " + message);
		}
	}

	private String messageOrDefault(Exception ex) {
		String m = ex.getMessage();
		return StringUtils.hasText(m) ? m : ex.getClass().getSimpleName();
	}
}
