package com.apmosys.employeeportal.config;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

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

    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (params == null || params.length == 0 || !(params[0] instanceof GetEmployeeSummaryOnExportDTO)) {
            return "employeeViewClientAttendance_" + (params != null ? params.length : 0);
        }
        GetEmployeeSummaryOnExportDTO dto = (GetEmployeeSummaryOnExportDTO) params[0];
        StringBuilder sb = new StringBuilder();

        // Root DTO fields
        sb.append(nullStr(dto.getProjectId())).append(SEP);
        sb.append(nullStr(dto.getMonth())).append(SEP);
        sb.append(nullStr(dto.getYear())).append(SEP);
        sb.append(nullStr(dto.getEmpId())).append(SEP);
        sb.append(dto.getDate() != null ? dto.getDate().toString() : "").append(SEP);
        sb.append(nullStr(dto.getAllEmp())).append(SEP);
        sb.append(normalizeList(dto.getBillableType())).append(SEP);
        sb.append(nullStr(dto.getProjectActive())).append(SEP);
        sb.append(nullStr(dto.getEmployeeActive())).append(SEP);
        sb.append(nullStr(dto.getStatus())).append(SEP);
        sb.append(nullStr(dto.getPage())).append(SEP);
        sb.append(nullStr(dto.getSize())).append(SEP);
        sb.append(nullStr(dto.getSortBy())).append(SEP);
        sb.append(nullStr(dto.getSortDirection())).append(SEP);
        sb.append(nullStr(dto.getClientSideFilter())).append(SEP);
        sb.append(nullStr(dto.getDeptId())).append(SEP);
        sb.append(nullStr(dto.getIsEmployeeRepeated())).append(SEP);
        sb.append(nullStr(dto.getMultiPOs())).append(SEP);

        // Filters (all fields used in repository calls)
        ColumnFilterDTO filters = dto.getFilters();
        if (filters != null) {
            sb.append(nullStr(filters.getEmploymentId())).append(SEP);
            sb.append(nullStr(filters.getClientSideId())).append(SEP);
            sb.append(nullStr(filters.getEmployeeName())).append(SEP);
            sb.append(nullStr(filters.getBillableType())).append(SEP);
            sb.append(nullStr(filters.getProjectName())).append(SEP);
            sb.append(nullStr(filters.getPoNo())).append(SEP);
            sb.append(nullStr(filters.getProjectManagerName())).append(SEP);
            sb.append(nullStr(filters.getProjectManagers())).append(SEP);
            sb.append(nullStr(filters.getClientName())).append(SEP);
            sb.append(nullStr(filters.getTeamName())).append(SEP);
            sb.append(nullStr(filters.getDepartment())).append(SEP);
            sb.append(nullStr(filters.getEmploymentStatus())).append(SEP);
            sb.append(nullStr(filters.getProjectStatus())).append(SEP);
        }

        return sb.toString();
    }

    private static String nullStr(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static String normalizeList(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return list.stream().sorted().collect(Collectors.joining(","));
    }
}
