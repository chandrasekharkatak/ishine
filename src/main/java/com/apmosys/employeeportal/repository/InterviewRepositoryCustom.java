package com.apmosys.employeeportal.repository;

import java.util.List;

import com.apmosys.employeeportal.dto.InterviewVisibilityScope;

public interface InterviewRepositoryCustom {
    List<Object[]> findAllInterviewsWithColumnFilters(
            String startDate, String endDate,
            List<String> clients, List<Long> departmentIds, List<Long> employeeIds,
            String titleFilter, String dateFilter, String clientFilter, String roleFilter,
            String projectFilter, String departmentNameFilter, String employeeNameFilter,
            String modeFilter, String interviewStatusFilter, String selectionStatusFilter,
            String onboardingStatusFilter, String jdFilter, String scheduledByNameFilter,
            String interviewerNameFilter,
            InterviewVisibilityScope visibilityScope,
            String sortColumn, String sortDirection,
            int page, int size);

    long countAllInterviewsWithColumnFilters(
            String startDate, String endDate,
            List<String> clients, List<Long> departmentIds, List<Long> employeeIds,
            String titleFilter, String dateFilter, String clientFilter, String roleFilter,
            String projectFilter, String departmentNameFilter, String employeeNameFilter,
            String modeFilter, String interviewStatusFilter, String selectionStatusFilter,
            String onboardingStatusFilter, String jdFilter, String scheduledByNameFilter,
            String interviewerNameFilter,
            InterviewVisibilityScope visibilityScope);
}
