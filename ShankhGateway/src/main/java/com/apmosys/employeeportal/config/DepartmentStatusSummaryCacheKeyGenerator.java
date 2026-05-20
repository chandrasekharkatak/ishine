package com.apmosys.employeeportal.config;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.dto.GetEmployeeSummaryOnExportDTO;

/**
 * Cache key generator for getDepartmentStatusSummary.
 * Key: empId|month|year|clientSideFilter|billableType(normalized)|employeeActive|multiPOs
 * (only the parameters passed to the repository).
 */
@Component("departmentStatusSummaryCacheKeyGenerator")
public class DepartmentStatusSummaryCacheKeyGenerator implements KeyGenerator {

    private static final String SEP = "|";

    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (params == null || params.length == 0 || !(params[0] instanceof GetEmployeeSummaryOnExportDTO)) {
            return "departmentStatusSummary_" + (params != null ? params.length : 0);
        }
        GetEmployeeSummaryOnExportDTO dto = (GetEmployeeSummaryOnExportDTO) params[0];
        return String.join(SEP,
                nullStr(dto.getEmpId()),
                nullStr(dto.getMonth()),
                nullStr(dto.getYear()),
                nullStr(dto.getClientSideFilter()),
                normalizeList(dto.getBillableType()),
                nullStr(dto.getEmployeeActive()),
                nullStr(dto.getMultiPOs()));
    }

    private static String nullStr(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static String normalizeList(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return list.stream().sorted().collect(Collectors.joining(","));
    }
}
