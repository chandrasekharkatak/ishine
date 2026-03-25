package com.apmosys.employeeportal.config;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

/**
 * Cache key generator for getTimesheetDashboardCountForEmployee.
 * Key: month|year|empId|isClientDashboard|sortedBillableTypes|employeeActive|clientSideFilter|multiPOs
 * so that same request parameters always produce the same key (billableTypes order-independent).
 */
@Component("timesheetDashboardCacheKeyGenerator")
public class TimesheetDashboardCacheKeyGenerator implements KeyGenerator {

    private static final String SEP = "|";

    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (params == null || params.length < 8) {
            return "timesheetDashboard_" + (params != null ? String.valueOf(params.length) : "0");
        }
        Integer month = (Integer) params[0];
        Integer year = (Integer) params[1];
        Long empId = (Long) params[2];
        Boolean isClientDashboard = (Boolean) params[3];
        @SuppressWarnings("unchecked")
        List<String> billableTypes = (List<String>) params[4];
        String employeeActive = (String) params[5];
        String clientSideFilter = (String) params[6];
        String multiPOs = (String) params[7];

        String billablePart = billableTypes == null ? "" : billableTypes.stream().sorted().collect(Collectors.joining(","));
        return String.join(SEP,
                String.valueOf(month),
                String.valueOf(year),
                String.valueOf(empId),
                String.valueOf(isClientDashboard),
                billablePart,
                nullToEmpty(employeeActive),
                nullToEmpty(clientSideFilter),
                nullToEmpty(multiPOs));
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
