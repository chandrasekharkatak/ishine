package com.apmosys.employeeportal.config;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import com.apmosys.employeeportal.dto.ColumnFilterDTO;
import com.apmosys.employeeportal.dto.GetEmployeeSummaryOnExportDTO;
/**
 * Cache key generator for getEmployeeViewForClientAttendanceStatus.
 * Key includes all request fields and filter fields so same request always produces the same key.
 * List fields (e.g. billableType) are normalized (sorted) for order-independent key.
 */
@Component("employeeViewClientAttendanceCacheKeyGenerator")
public class EmployeeViewClientAttendanceCacheKeyGenerator implements KeyGenerator {

	private static final String SEP = "|";
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

	@Override
	public Object generate(Object target, Method method, Object... params) {

	    if (params == null || params.length == 0 || !(params[0] instanceof GetEmployeeSummaryOnExportDTO)) {
	        return "employeeViewClientAttendance_" + (params != null ? params.length : 0);
	    }

	    GetEmployeeSummaryOnExportDTO dto = (GetEmployeeSummaryOnExportDTO) params[0];
	    StringBuilder sb = new StringBuilder();

	    // 🔹 Root DTO fields (normalized)
	    sb.append(normalize(dto.getProjectId())).append(SEP);
	    sb.append(normalize(dto.getMonth())).append(SEP);
	    sb.append(normalize(dto.getYear())).append(SEP);
	    sb.append(normalize(dto.getEmpId())).append(SEP);
	    sb.append(dto.getDate() != null ? DATE_FORMAT.format(dto.getDate()) : "").append(SEP);
	    sb.append(normalize(dto.getAllEmp())).append(SEP);
	    sb.append(normalizeList(dto.getBillableType())).append(SEP);
	    sb.append(normalize(dto.getProjectActive())).append(SEP);
	    sb.append(normalize(dto.getEmployeeActive())).append(SEP);
	    sb.append(normalize(dto.getStatus())).append(SEP);
	    sb.append(normalize(dto.getPage())).append(SEP);
	    sb.append(normalize(dto.getSize())).append(SEP);
	    sb.append(normalize(dto.getSortBy())).append(SEP);
	    sb.append(normalize(dto.getSortDirection())).append(SEP);
	    sb.append(normalize(dto.getClientSideFilter())).append(SEP);
	    sb.append(normalize(dto.getDeptId())).append(SEP);
	    sb.append(normalize(dto.getIsEmployeeRepeated())).append(SEP);
	    sb.append(normalize(dto.getMultiPOs())).append(SEP);

	    // 🔹 Filters
	    ColumnFilterDTO filters = dto.getFilters();
	    if (filters != null) {
	        sb.append(normalize(filters.getEmploymentId())).append(SEP);
	        sb.append(normalize(filters.getClientSideId())).append(SEP);
	        sb.append(normalize(filters.getEmployeeName())).append(SEP);
	        sb.append(normalize(filters.getBillableType())).append(SEP);
	        sb.append(normalize(filters.getProjectName())).append(SEP);
	        sb.append(normalize(filters.getPoNo())).append(SEP);
	        sb.append(normalize(filters.getProjectManagerName())).append(SEP);
	        sb.append(normalize(filters.getProjectManagers())).append(SEP);
	        sb.append(normalize(filters.getClientName())).append(SEP);
	        sb.append(normalize(filters.getTeamName())).append(SEP);
	        sb.append(normalize(filters.getDepartment())).append(SEP);
	        sb.append(normalize(filters.getEmploymentStatus())).append(SEP);
	        sb.append(normalize(filters.getProjectStatus())).append(SEP);
	    }

	    // RAW KEY
	    String rawKey = sb.toString();

	    // HASH (MD5)
	    String hashedKey = DigestUtils.md5DigestAsHex(
	            rawKey.getBytes(StandardCharsets.UTF_8)
	    );

	    //DEBUG (IMPORTANT for UAT verification)
	    System.out.println("CACHE RAW KEY: " + rawKey);
	    System.out.println("CACHE HASH KEY: " + hashedKey);

	    return "employeeViewClientAttendance_" + hashedKey;
	    }
	
	private static String normalize(Object o) {
	    return o == null ? "" : String.valueOf(o).trim().toLowerCase();
	}

	private static String normalizeList(List<String> list) {
	    if (list == null || list.isEmpty()) return "";
	    return list.stream()
	            .filter(e -> e != null)
	            .map(e -> e.trim().toLowerCase())
	            .sorted()
	            .collect(Collectors.joining(","));
	}
	
}
