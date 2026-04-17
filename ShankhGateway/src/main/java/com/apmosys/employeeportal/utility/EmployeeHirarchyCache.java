package com.apmosys.employeeportal.utility;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;

@Service
public class EmployeeHirarchyCache {

    @Autowired
	EmployeeLeaveRepository employeeLeaveRepository;

    private static final long CACHE_TTL_MS = 5 * 60 * 1000; // 5 minutes

    private final Map<Long, CacheEntry> cache = new ConcurrentHashMap<>();

    public List<Long> getEmployeesUnderAnyLeadingPerson(Long hodId) {

        if (hodId == null) {
            return Collections.emptyList(); 
        }
        long now = System.currentTimeMillis();

        CacheEntry entry = cache.get(hodId);

        if (entry == null || now - entry.timestamp > CACHE_TTL_MS) {
            // Cache missing or expired → fetch from DB
            List<Long> employeeIds = employeeLeaveRepository.fetchEmployeeIdsByHirarchy(hodId);
            employeeIds.remove(hodId);
            cache.put(hodId, new CacheEntry(employeeIds, now));
            return employeeIds;
        }

        // Cache valid → return cached data
        return entry.employeeIds;
    }

    private static class CacheEntry {
        final List<Long> employeeIds;
        final long timestamp;

        CacheEntry(List<Long> employeeIds, long timestamp) {
            this.employeeIds = employeeIds;
            this.timestamp = timestamp;
        }
    }


}
