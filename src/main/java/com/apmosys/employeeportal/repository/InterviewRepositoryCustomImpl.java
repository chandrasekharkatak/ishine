package com.apmosys.employeeportal.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.dto.InterviewVisibilityScope;

@Repository
public class InterviewRepositoryCustomImpl implements InterviewRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Object[]> findAllInterviewsWithColumnFilters(
            String startDate, String endDate,
            List<String> clients, List<Long> departmentIds, List<Long> employeeIds,
            String titleFilter, String dateFilter, String clientFilter, String roleFilter,
            String projectFilter, String departmentNameFilter, String employeeNameFilter,
            String modeFilter, String interviewStatusFilter, String selectionStatusFilter,
            String onboardingStatusFilter, String jdFilter, String scheduledByNameFilter,
            String interviewerNameFilter,
            InterviewVisibilityScope visibilityScope,
            String sortColumn, String sortDirection,
            int page, int size) {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT i.id, i.title, i.date, i.time, i.client, i.role, i.project, i.department_id, i.employee_id, i.mode, ");
        sql.append("i.interview_status, i.interview_remarks, i.selection_status, i.selection_remarks, i.onboarding_status, i.onboarding_remarks, ");
        sql.append("i.interview_status_change_date, i.selection_status_change_date, i.onboarding_status_change_date, ");
        sql.append("i.jd, i.scheduled_by_id, i.interviewer_name, i.additional_notes, i.resume_file_name, i.resume_file_path, ");
        sql.append("i.created_by, i.created_on, i.updated_by, i.updated_on, ");
        sql.append("d.name as department_name, e.name as employee_name, c.name as scheduled_by_name ");
        sql.append("FROM interview_tracker i ");
        sql.append("LEFT JOIN department d ON i.department_id = d.dept_id ");
        sql.append("LEFT JOIN employee e ON i.employee_id = e.emp_id ");
        sql.append("LEFT JOIN employee c ON i.created_by = c.emp_id ");
        sql.append("WHERE 1=1 ");

        if (startDate != null && endDate != null) {
            sql.append("AND i.date BETWEEN :startDate AND :endDate ");
        }

        if (clients != null && !clients.isEmpty()) {
            sql.append("AND i.client IN (:clients) ");
        }

        if (departmentIds != null && !departmentIds.isEmpty()) {
            sql.append("AND i.department_id IN (:departmentIds) ");
        }

        if (employeeIds != null && !employeeIds.isEmpty()) {
            sql.append("AND i.employee_id IN (:employeeIds) ");
        }

        addLikeCondition(sql, "i.title", "titleFilter", titleFilter);
        if (StringUtils.hasText(dateFilter)) {
            sql.append("AND i.date LIKE :dateFilter ");
        }
        addLikeCondition(sql, "i.client", "clientFilter", clientFilter);
        addLikeCondition(sql, "i.role", "roleFilter", roleFilter);
        addLikeCondition(sql, "i.project", "projectFilter", projectFilter);
        addLikeCondition(sql, "d.name", "departmentNameFilter", departmentNameFilter);
        addLikeCondition(sql, "e.name", "employeeNameFilter", employeeNameFilter);
        addLikeCondition(sql, "i.mode", "modeFilter", modeFilter);
        addLikeCondition(sql, "i.interview_status", "interviewStatusFilter", interviewStatusFilter);
        addLikeCondition(sql, "i.selection_status", "selectionStatusFilter", selectionStatusFilter);
        addLikeCondition(sql, "i.onboarding_status", "onboardingStatusFilter", onboardingStatusFilter);
        addLikeCondition(sql, "i.jd", "jdFilter", jdFilter);
        addLikeCondition(sql, "c.name", "scheduledByNameFilter", scheduledByNameFilter);
        addLikeCondition(sql, "i.interviewer_name", "interviewerNameFilter", interviewerNameFilter);

        appendVisibilityFilter(sql, visibilityScope);

        if (StringUtils.hasText(sortColumn)) {
            String sortField = mapSortColumn(sortColumn);
            String dir = "asc".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
            sql.append("ORDER BY ").append(sortField).append(" ").append(dir).append(" ");
        } else {
            sql.append("ORDER BY i.date DESC ");
        }

        sql.append("LIMIT :size OFFSET :offset");

        Query query = entityManager.createNativeQuery(sql.toString());

        if (startDate != null && endDate != null) {
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
        }

        if (clients != null && !clients.isEmpty()) {
            query.setParameter("clients", clients);
        }

        if (departmentIds != null && !departmentIds.isEmpty()) {
            query.setParameter("departmentIds", departmentIds);
        }

        if (employeeIds != null && !employeeIds.isEmpty()) {
            query.setParameter("employeeIds", employeeIds);
        }

        addLikeParameter(query, "titleFilter", titleFilter);
        if (StringUtils.hasText(dateFilter)) {
            query.setParameter("dateFilter", "%" + dateFilter + "%");
        }
        addLikeParameter(query, "clientFilter", clientFilter);
        addLikeParameter(query, "roleFilter", roleFilter);
        addLikeParameter(query, "projectFilter", projectFilter);
        addLikeParameter(query, "departmentNameFilter", departmentNameFilter);
        addLikeParameter(query, "employeeNameFilter", employeeNameFilter);
        addLikeParameter(query, "modeFilter", modeFilter);
        addLikeParameter(query, "interviewStatusFilter", interviewStatusFilter);
        addLikeParameter(query, "selectionStatusFilter", selectionStatusFilter);
        addLikeParameter(query, "onboardingStatusFilter", onboardingStatusFilter);
        addLikeParameter(query, "jdFilter", jdFilter);
        addLikeParameter(query, "scheduledByNameFilter", scheduledByNameFilter);
        addLikeParameter(query, "interviewerNameFilter", interviewerNameFilter);

        bindVisibilityParameters(query, visibilityScope);

        query.setParameter("size", size);
        query.setParameter("offset", page * size);

        return query.getResultList();
    }

    @Override
    public long countAllInterviewsWithColumnFilters(
            String startDate, String endDate,
            List<String> clients, List<Long> departmentIds, List<Long> employeeIds,
            String titleFilter, String dateFilter, String clientFilter, String roleFilter,
            String projectFilter, String departmentNameFilter, String employeeNameFilter,
            String modeFilter, String interviewStatusFilter, String selectionStatusFilter,
            String onboardingStatusFilter, String jdFilter, String scheduledByNameFilter,
            String interviewerNameFilter,
            InterviewVisibilityScope visibilityScope) {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) ");
        sql.append("FROM interview_tracker i ");
        sql.append("LEFT JOIN department d ON i.department_id = d.dept_id ");
        sql.append("LEFT JOIN employee e ON i.employee_id = e.emp_id ");
        sql.append("LEFT JOIN employee c ON i.created_by = c.emp_id ");
        sql.append("WHERE 1=1 ");

        if (startDate != null && endDate != null) {
            sql.append("AND i.date BETWEEN :startDate AND :endDate ");
        }

        if (clients != null && !clients.isEmpty()) {
            sql.append("AND i.client IN (:clients) ");
        }

        if (departmentIds != null && !departmentIds.isEmpty()) {
            sql.append("AND i.department_id IN (:departmentIds) ");
        }

        if (employeeIds != null && !employeeIds.isEmpty()) {
            sql.append("AND i.employee_id IN (:employeeIds) ");
        }

        addLikeCondition(sql, "i.title", "titleFilter", titleFilter);
        if (StringUtils.hasText(dateFilter)) {
            sql.append("AND i.date LIKE :dateFilter ");
        }
        addLikeCondition(sql, "i.client", "clientFilter", clientFilter);
        addLikeCondition(sql, "i.role", "roleFilter", roleFilter);
        addLikeCondition(sql, "i.project", "projectFilter", projectFilter);
        addLikeCondition(sql, "d.name", "departmentNameFilter", departmentNameFilter);
        addLikeCondition(sql, "e.name", "employeeNameFilter", employeeNameFilter);
        addLikeCondition(sql, "i.mode", "modeFilter", modeFilter);
        addLikeCondition(sql, "i.interview_status", "interviewStatusFilter", interviewStatusFilter);
        addLikeCondition(sql, "i.selection_status", "selectionStatusFilter", selectionStatusFilter);
        addLikeCondition(sql, "i.onboarding_status", "onboardingStatusFilter", onboardingStatusFilter);
        addLikeCondition(sql, "i.jd", "jdFilter", jdFilter);
        addLikeCondition(sql, "c.name", "scheduledByNameFilter", scheduledByNameFilter);
        addLikeCondition(sql, "i.interviewer_name", "interviewerNameFilter", interviewerNameFilter);

        appendVisibilityFilter(sql, visibilityScope);

        Query query = entityManager.createNativeQuery(sql.toString());

        if (startDate != null && endDate != null) {
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
        }

        if (clients != null && !clients.isEmpty()) {
            query.setParameter("clients", clients);
        }

        if (departmentIds != null && !departmentIds.isEmpty()) {
            query.setParameter("departmentIds", departmentIds);
        }

        if (employeeIds != null && !employeeIds.isEmpty()) {
            query.setParameter("employeeIds", employeeIds);
        }

        addLikeParameter(query, "titleFilter", titleFilter);
        if (StringUtils.hasText(dateFilter)) {
            query.setParameter("dateFilter", "%" + dateFilter + "%");
        }
        addLikeParameter(query, "clientFilter", clientFilter);
        addLikeParameter(query, "roleFilter", roleFilter);
        addLikeParameter(query, "projectFilter", projectFilter);
        addLikeParameter(query, "departmentNameFilter", departmentNameFilter);
        addLikeParameter(query, "employeeNameFilter", employeeNameFilter);
        addLikeParameter(query, "modeFilter", modeFilter);
        addLikeParameter(query, "interviewStatusFilter", interviewStatusFilter);
        addLikeParameter(query, "selectionStatusFilter", selectionStatusFilter);
        addLikeParameter(query, "onboardingStatusFilter", onboardingStatusFilter);
        addLikeParameter(query, "jdFilter", jdFilter);
        addLikeParameter(query, "scheduledByNameFilter", scheduledByNameFilter);
        addLikeParameter(query, "interviewerNameFilter", interviewerNameFilter);

        bindVisibilityParameters(query, visibilityScope);

        Number result = (Number) query.getSingleResult();
        return result != null ? result.longValue() : 0;
    }

    private void appendVisibilityFilter(StringBuilder sql, InterviewVisibilityScope scope) {
        if (scope == null || scope.isViewAll()) {
            return;
        }
        if (!scope.hasVisibility()) {
            sql.append("AND 1=0 ");
            return;
        }
        sql.append("AND (");
        boolean hasClause = false;
        if (!scope.getVisibleEmployeeIds().isEmpty()) {
            sql.append("(i.employee_id IN (:visibleEmployeeIds) OR i.created_by IN (:visibleEmployeeIds))");
            hasClause = true;
        }
        if (!scope.getVisibleDepartmentIds().isEmpty()) {
            if (hasClause) {
                sql.append(" OR ");
            }
            sql.append("i.department_id IN (:visibleDepartmentIds)");
            hasClause = true;
        }
        if (!hasClause) {
            sql.append("1=0");
        }
        sql.append(") ");
    }

    private void bindVisibilityParameters(Query query, InterviewVisibilityScope scope) {
        if (scope == null || scope.isViewAll() || !scope.hasVisibility()) {
            return;
        }
        if (!scope.getVisibleEmployeeIds().isEmpty()) {
            query.setParameter("visibleEmployeeIds", scope.getVisibleEmployeeIds());
        }
        if (!scope.getVisibleDepartmentIds().isEmpty()) {
            query.setParameter("visibleDepartmentIds", scope.getVisibleDepartmentIds());
        }
    }

    private void addLikeCondition(StringBuilder sql, String column, String paramName, String filterValue) {
        if (StringUtils.hasText(filterValue)) {
            sql.append("AND LOWER(").append(column).append(") LIKE :").append(paramName).append(" ");
        }
    }

    private void addLikeParameter(Query query, String paramName, String filterValue) {
        if (StringUtils.hasText(filterValue)) {
            query.setParameter(paramName, "%" + filterValue.toLowerCase() + "%");
        }
    }

    private String mapSortColumn(String sortColumn) {
        switch (sortColumn) {
            case "title": return "i.title";
            case "date": return "i.date";
            case "client": return "i.client";
            case "role": return "i.role";
            case "project": return "i.project";
            case "departmentName": return "d.name";
            case "employeeName": return "e.name";
            case "mode": return "i.mode";
            case "interviewStatus": return "i.interview_status";
            case "selectionStatus": return "i.selection_status";
            case "onboardingStatus": return "i.onboarding_status";
            case "scheduledByName": return "c.name";
            case "interviewerName": return "i.interviewer_name";
            default: return "i.date";
        }
    }
}
