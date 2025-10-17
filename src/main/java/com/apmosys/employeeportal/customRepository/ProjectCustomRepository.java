package com.apmosys.employeeportal.customRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.ProjectFetchDTO;
import com.apmosys.employeeportal.dto.ProjectManagersDTO;
import com.apmosys.employeeportal.dto.ProjectOverheadsDTO;
import com.apmosys.employeeportal.dto.RMGDashboardProjectRequest;
import com.apmosys.employeeportal.repository.ProjectManagerMappingRepository;
import com.apmosys.employeeportal.repository.ProjectOverheadMappingRepository;

@SuppressWarnings("unchecked")
@Repository
public class ProjectCustomRepository {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProjectManagerMappingRepository projectManagerMappingRepository;

    @Autowired
    private ProjectOverheadMappingRepository projectOverheadMappingRepository;

    private static final StringBuilder projectDetailsStartQuery = new StringBuilder()
            .append(" ( SELECT DISTINCT \n")
            .append(" p.project_id, p.project_name, p.internal_project_type \n")
            .append(",CASE WHEN po_project_type IS NOT NULL AND TRIM(p.po_project_type) != '' THEN p.po_project_type ELSE p.internal_project_type END AS po_project_type \n")
            .append(",p.po_no, p.po_project_id, po_start_date, po_end_date \n")
            .append(",p.client_id,c.client_name, clientrm, apmosysrm,p.state, p.dept_id \n")
            .append(",GROUP_CONCAT(DISTINCT e1.name ORDER BY e1.name SEPARATOR ', ') AS project_manager \n")
            .append(",p.active, p.status po_project_status, p.project_status ishine_project_status, p.sync_project, p.created_by, p.updated_by \n")
            .append(",CASE  \n")
            .append(" WHEN p.is_draft_project = 'true' THEN 'Pending For Approval'  \n")
            .append(" WHEN p.is_draft_project = 'false' THEN 'Approved'  \n")
            .append(" WHEN p.is_draft_project = 'Rejected' THEN 'Rejected'  \n")
            .append(" WHEN p.is_draft_project = 'Completed' THEN 'Completed'  \n")
            .append(" WHEN p.is_draft_project IS NULL THEN 'Not Started'  \n")
            .append(" ELSE 'Un Mentioned Test Data'   \n")
            .append(" END as approval_status \n")
            .append(",CASE  \n")
            .append(" WHEN p.po_project_id IS NOT NULL THEN CONCAT('po', p.po_project_id) \n")
            .append(" ELSE CAST(p.project_id AS CHAR)  \n")
            .append(" END AS project_overview_id  \n")
            .append(",p.created_on, p.updated_on, p.project_completion_date \n")
            .append(" FROM projects p \n");

    private static final StringBuilder projectDetailsGroupQuery = new StringBuilder()
            .append(" GROUP BY \n")
            .append(" p.project_id, p.project_name, p.internal_project_type \n")
            .append(" ,p.po_no, p.po_project_id, po_start_date, po_end_date \n")
            .append(" ,p.client_id,c.client_name, clientrm, apmosysrm,p.state, p.dept_id \n")
            .append(" ,p.active, p.status , p.project_status, p.sync_project, p.created_by, p.updated_by, approval_status \n")
            .append(" ,project_overview_id, p.created_on, p.updated_on, p.project_completion_date \n");

    public Slice<ProjectFetchDTO> handleProjectsByType(RMGDashboardProjectRequest rmgDashboardProjectRequest,
            List<Long> deptIds, String type, List<String> projectNames) {

        String sortBy = getSortBy(rmgDashboardProjectRequest.getSortColumn(), true);
        String sortDirection = rmgDashboardProjectRequest.getSortDirection();
        Pageable page = PageRequest.of(rmgDashboardProjectRequest.getPage(), rmgDashboardProjectRequest.getPageSize(),
                Direction.fromString(sortDirection), sortBy);
        String query = getQuery(page, rmgDashboardProjectRequest.getProjectFilter(), sortBy, sortDirection, type,
                false, projectNames);

        CompletableFuture<List<ProjectFetchDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page,
                        type, "", "", projectNames));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(() -> getResultCount(query, deptIds, type, "", "", projectNames));
        CompletableFuture.allOf(listFuture, countFuture).join();

        List<ProjectFetchDTO> list = null;
        Long count = null;
        try {
            list = listFuture.get();
            count = countFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing async DB calls", e);
        }
        return new PageImpl<>(list, page, count);
    }

    public Slice<ProjectFetchDTO> handleAllProjects(RMGDashboardProjectRequest rmgDashboardProjectRequest,
            List<Long> deptIds, String type, List<String> projectNames) {

        String sortBy = getSortBy(rmgDashboardProjectRequest.getSortColumn(), true);
        String sortDirection = rmgDashboardProjectRequest.getSortDirection();
        Pageable page = PageRequest.of(rmgDashboardProjectRequest.getPage(), rmgDashboardProjectRequest.getPageSize(),
                Direction.fromString(sortDirection), sortBy);
        String query = getAllProjectsQuery(page, rmgDashboardProjectRequest.getProjectFilter(), sortBy, sortDirection,
                type, projectNames);

        CompletableFuture<List<ProjectFetchDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page, type, "", "",
                        projectNames));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(() -> getResultCount(query, deptIds, type, "", "", projectNames));
        CompletableFuture.allOf(listFuture, countFuture).join();

        List<ProjectFetchDTO> list = null;
        Long count = null;
        try {
            list = listFuture.get();
            count = countFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing async DB calls", e);
        }
        populateProjectManagersAndOverheads(list);
        return new PageImpl<>(list, page, count);
    }

    public Slice<ProjectFetchDTO> handleFCProjects(RMGDashboardProjectRequest rmgDashboardProjectRequest,
            List<Long> deptIds, String type, List<String> projectNames) {

        String sortBy = getSortBy(rmgDashboardProjectRequest.getSortColumn(), true);
        String sortDirection = rmgDashboardProjectRequest.getSortDirection();
        Pageable page = PageRequest.of(rmgDashboardProjectRequest.getPage(), rmgDashboardProjectRequest.getPageSize(),
                Direction.fromString(sortDirection), sortBy);
        String query = getFCProjectQuery(page, rmgDashboardProjectRequest.getProjectFilter(), sortBy, sortDirection,
                type, rmgDashboardProjectRequest.getFixedCostFilter(), projectNames);

        CompletableFuture<List<ProjectFetchDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page, type, "", "",
                        projectNames));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(() -> getResultCount(query, deptIds, type, "", "", projectNames));
        CompletableFuture.allOf(listFuture, countFuture).join();

        List<ProjectFetchDTO> list = null;
        Long count = null;
        try {
            list = listFuture.get();
            count = countFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing async DB calls", e);
        }
        populateProjectManagersAndOverheads(list);
        return new PageImpl<>(list, page, count);
    }

    public Slice<ProjectFetchDTO> handleUnfilledPositionProjects(RMGDashboardProjectRequest rmgDashboardProjectRequest,
            List<Long> deptIds, String type, List<String> projectNames) {

        String sortBy = getSortBy(rmgDashboardProjectRequest.getSortColumn(), true);
        String sortDirection = rmgDashboardProjectRequest.getSortDirection();
        Pageable page = PageRequest.of(rmgDashboardProjectRequest.getPage(), rmgDashboardProjectRequest.getPageSize(),
                Direction.fromString(sortDirection), sortBy);
        String query = getUnfilledPositionsProjectQuery(page, rmgDashboardProjectRequest.getProjectFilter(), sortBy,
                sortDirection, projectNames);

        CompletableFuture<List<ProjectFetchDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page, type, "", "",
                        projectNames));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(() -> getResultCount(query, deptIds, type, "", "", projectNames));
        CompletableFuture.allOf(listFuture, countFuture).join();

        List<ProjectFetchDTO> list = null;
        Long count = null;
        try {
            list = listFuture.get();
            count = countFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing async DB calls", e);
        }
        return new PageImpl<>(list, page, count);
    }

    public Slice<ProjectFetchDTO> handleExpiredTNMProjects(RMGDashboardProjectRequest rmgDashboardProjectRequest,
            List<Long> deptIds, String type, List<String> projectNames) {

        String sortBy = getSortBy(rmgDashboardProjectRequest.getSortColumn(), true);
        String sortDirection = rmgDashboardProjectRequest.getSortDirection();
        Pageable page = PageRequest.of(rmgDashboardProjectRequest.getPage(), rmgDashboardProjectRequest.getPageSize(),
                Direction.fromString(sortDirection), sortBy);

        boolean addStartAndEndDate = false;
        String startDate = null;
        String endDate = null;
        String expiredProjectTimeFrameFilter = rmgDashboardProjectRequest.getExpiredProjectFilter();

        if (expiredProjectTimeFrameFilter != null
                && !expiredProjectTimeFrameFilter.equalsIgnoreCase("allExpiredTNMProjectsCount")) {
            List<LocalDate> range = getDateRange(expiredProjectTimeFrameFilter);
            if (range != null && !range.isEmpty() && range.size() == 2) {
                addStartAndEndDate = true;
                startDate = range.get(0).toString();
                endDate = range.get(1).toString();
            }
        }
        if (expiredProjectTimeFrameFilter == null || startDate == null || endDate == null
                || (expiredProjectTimeFrameFilter != null
                        && expiredProjectTimeFrameFilter.equalsIgnoreCase("allExpiredTNMProjectsCount"))) {
            addStartAndEndDate = false;
        }

        String query = getQuery(page, rmgDashboardProjectRequest.getProjectFilter(), sortBy,
                sortDirection, type, addStartAndEndDate, projectNames);

        final String sDate = startDate;
        final String eDate = endDate;

        CompletableFuture<List<ProjectFetchDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page, type, sDate,
                        eDate, projectNames));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(() -> getResultCount(query, deptIds, type, sDate, eDate, projectNames));
        CompletableFuture.allOf(listFuture, countFuture).join();

        List<ProjectFetchDTO> list = null;
        Long count = null;
        try {
            list = listFuture.get();
            count = countFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing async DB calls", e);
        }
        return new PageImpl<>(list, page, count);
    }

    public Slice<ProjectFetchDTO> handleGeneralProjectFilters(RMGDashboardProjectRequest req,
            List<Long> deptIds, Set<Integer> projectIds, String approvalStatus, String completionStatus,
            String isDraftProjectStatus, boolean approvalCheck, String status, String projectStatus,
            List<String> projectNames) {

        String sortBy = getSortBy(req.getSortColumn(), true);
        String sortDirection = req.getSortDirection();
        Pageable page = PageRequest.of(req.getPage(), req.getPageSize(),
                Direction.fromString(sortDirection), sortBy);

        String type = req.getProjectStatus() != null
                ? req.getProjectStatus().trim().toLowerCase()
                : "";

        boolean isProjectId = !"ADMIN".equals(req.getCurrentUserType());
        String query = buildQueryForMode(req, page, sortBy, sortDirection, approvalStatus, completionStatus, type,
                projectNames);

        CompletableFuture<List<ProjectFetchDTO>> listFuture;
        CompletableFuture<Long> countFuture;
        if (completionStatus.equals("completedwithshankh") || completionStatus.equals("completedwithteam")) {
            listFuture = CompletableFuture.supplyAsync(() -> getGeneralProjectsFilterResultList(query, sortBy,
                    sortDirection, projectIds, page, completionStatus,
                    isProjectId, false, isDraftProjectStatus, status, projectStatus, projectNames));
            countFuture = CompletableFuture.supplyAsync(
                    () -> getGeneralProjectsFilterResultCount(query, projectIds, page, completionStatus, isProjectId,
                            false, isDraftProjectStatus, status, projectStatus, projectNames));
        } else {
            listFuture = CompletableFuture
                    .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page, type, "", "",
                            projectNames));
            countFuture = CompletableFuture
                    .supplyAsync(() -> getResultCount(query, deptIds, type, "", "", projectNames));
        }

        try {
            CompletableFuture.allOf(listFuture, countFuture).join();
            List<ProjectFetchDTO> list = listFuture.get();
            Long count = countFuture.get();
            return new PageImpl<>(list, page, count);
        } catch (InterruptedException | ExecutionException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing async DB calls");
        }
    }

    private String buildQueryForMode(RMGDashboardProjectRequest req, Pageable page, String sortBy, String sortDirection,
            String approvalStatus, String completionStatus, String type, List<String> projectNames) {
        Set<String> approvalStatusSet = Set.of("all", "not started", "completedinishine",
                "approvedprojects", "pendingprojects", "rejectedprojects", "completedwithshankh", "completedwithteam");
        String status = (completionStatus.equals("completedwithshankh") || completionStatus.equals("completedwithteam"))
                ? completionStatus
                : approvalStatus;
        if (status != null && approvalStatusSet.contains(status)) {
            return getGeneralProjectFilterQuery(page, req.getProjectFilter(), sortBy, sortDirection, status,
                    projectNames);
        }
        return getAllProjectsQuery(page, req.getProjectFilter(), sortBy, sortDirection, type, projectNames);
    }

    private List<ProjectFetchDTO> getResultList(String query, String sortBy, String sortDirection, List<Long> deptIds,
            Pageable page, String type, String startDate, String endDate, List<String> projectNames) {

        query = "SELECT * FROM " + query;
        System.out.println("================================= query");
        System.out.println(query);
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<Object[]> nativeQuery = session.createNativeQuery(query)
                    .unwrap(org.hibernate.query.NativeQuery.class);

            nativeQuery.setParameter("deptIds", deptIds);

            if (type != null && type.equals("expiredtnm") && startDate != null && endDate != null) {
                nativeQuery.setParameter("startDate", startDate);
                nativeQuery.setParameter("endDate", endDate);
            }
            if (projectNames != null && !projectNames.isEmpty()) {
                nativeQuery.setParameter("projectNames", projectNames);
            }
            // Set pagination offsets
            int offset = page.getPageNumber() * page.getPageSize();
            nativeQuery.setFirstResult(offset);
            nativeQuery.setMaxResults(page.getPageSize());

            List<Object[]> list = nativeQuery.getResultList();
            return list.stream()
                    .map(ProjectFetchDTO::projectDetailsBaseColumn)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Long getResultCount(String query, List<Long> deptIds, String type, String startDate, String endDate,
            List<String> projectNames) {
        query = "SELECT count(*) FROM " + query;
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<Object[]> nativeQuery = session.createNativeQuery(query)
                    .unwrap(org.hibernate.query.NativeQuery.class);
            nativeQuery.setParameter("deptIds", deptIds);

            if (type != null && type.equals("expiredtnm") && startDate != null && endDate != null) {
                nativeQuery.setParameter("startDate", startDate);
                nativeQuery.setParameter("endDate", endDate);
            }
            if (projectNames != null && !projectNames.isEmpty()) {
                nativeQuery.setParameter("projectNames", projectNames);
            }
            Object count = nativeQuery.getSingleResult();
            return count == null ? 0 : Long.parseLong(count.toString());

        } catch (Exception e) {
            throw e;
        }
    }

    private List<ProjectFetchDTO> getGeneralProjectsFilterResultList(String query, String sortBy, String sortDirection,
            Set<Integer> projectIds, Pageable page, String type, boolean isProjectId, boolean approvalCheck,
            String isDraftProjectStatus, String status, String projectStatus, List<String> projectNames) {

        query = "SELECT * FROM " + query;
        System.out.println("================================= query");
        System.out.println(query);
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<Object[]> nativeQuery = session.createNativeQuery(query)
                    .unwrap(org.hibernate.query.NativeQuery.class);
            nativeQuery.setParameter("projectIds", projectIds);
            if (type.equals("completedwithshankh")) {
                nativeQuery.setParameter("isProjectId", isProjectId);
                nativeQuery.setParameter("approvalCheck", approvalCheck);
                nativeQuery.setParameter("projectStatus", projectStatus);
            } else if (type.equals("completedwithteam")) {
                nativeQuery.setParameter("projectIds", projectIds);
            }

            if (projectNames != null && !projectNames.isEmpty()) {
                nativeQuery.setParameter("projectNames", projectNames);
            }
            // Set pagination offsets
            int offset = page.getPageNumber() * page.getPageSize();
            nativeQuery.setFirstResult(offset);
            nativeQuery.setMaxResults(page.getPageSize());

            List<Object[]> list = nativeQuery.getResultList();
            return list.stream()
                    .map(ProjectFetchDTO::projectDetailsBaseColumn)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Long getGeneralProjectsFilterResultCount(String query,
            Set<Integer> projectIds, Pageable page, String type, boolean isProjectId, boolean approvalCheck,
            String isDraftProjectStatus, String status, String projectStatus, List<String> projectNames) {
        query = "SELECT count(*) FROM " + query;
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<Object[]> nativeQuery = session.createNativeQuery(query)
                    .unwrap(org.hibernate.query.NativeQuery.class);
            nativeQuery.setParameter("projectIds", projectIds);

            if (type.equals("completedwithshankh")) {
                nativeQuery.setParameter("isProjectId", isProjectId);
                nativeQuery.setParameter("approvalCheck", approvalCheck);
                nativeQuery.setParameter("projectStatus", projectStatus);
            } else if (type.equals("completedwithteam")) {
                nativeQuery.setParameter("projectIds", projectIds);
            }

            if (projectNames != null && !projectNames.isEmpty()) {
                nativeQuery.setParameter("projectNames", projectNames);
            }
            Object count = nativeQuery.getSingleResult();
            return count == null ? 0 : Long.parseLong(count.toString());

        } catch (Exception e) {
            throw e;
        }
    }

    private String getQuery(Pageable page, Map<String, String> projectFilter, String sortBy,
            String sortDirection, String type, boolean addStartAndEndDate, List<String> projectNames) {
        StringBuilder query = new StringBuilder(projectDetailsStartQuery);
        StringBuilder groupQuery = new StringBuilder(projectDetailsGroupQuery).append(" ) as T1");

        query.append(" INNER JOIN project_department_map pdm ON p.project_id = pdm.project_id\n")
                .append(" INNER JOIN department d ON pdm.dept_id = d.dept_id \n")
                .append(" INNER JOIN teams t ON p.project_id = t.project_id \n")
                .append(" INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n")
                .append("activetnm".equals(type) || "expiredtnm".equals(type) ? " INNER JOIN" : " LEFT JOIN")
                .append(" clients c ON p.client_id = c.client_id \n")
                .append(" LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id \n")
                .append(" LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n");

        query.append(" WHERE 1=1 \n")
                .append(" AND etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' \n")
                .append(" AND d.dept_id in :deptIds \n");

        if (type.equals("activetnm") || type.equals("expiredtnm")) {
            query.append(" AND po_project_type = 'TNM' \n");
        }

        if (type.equals("expiredtnm")) {
            query.append("  AND DATE(p.po_end_date) < CURDATE() \n");
            if (addStartAndEndDate) {
                query.append("  AND DATE(p.po_end_date) between :startDate and :endDate \n");
            }
        } else if (type.equals("internal")) {
            query.append(" AND p.internal_project_type is not null \n");
        } else if (type.equals("monitoring")) {
            query.append(" AND p.po_project_type = 'Monitoring' \n");
        }

        if (projectNames != null && !projectNames.isEmpty()) {
            query.append(" AND p.project_name IN (:projectNames) \n");
        }
        query.append(groupQuery);
        appenCustomSearchToQuery(projectFilter, query);
        query.append(String.format(" ORDER BY %s %s ", sortBy, sortDirection));
        return query.toString();
    }

    private String getAllProjectsQuery(Pageable page, Map<String, String> projectFilter, String sortBy,
            String sortDirection, String type, List<String> projectNames) {
        StringBuilder query = new StringBuilder(" ( " + projectDetailsStartQuery);
        StringBuilder query1 = new StringBuilder(projectDetailsStartQuery);
        StringBuilder query2 = new StringBuilder(projectDetailsStartQuery);
        StringBuilder query3 = new StringBuilder(projectDetailsStartQuery);
        StringBuilder groupQuery = new StringBuilder(projectDetailsGroupQuery).append(" )");

        StringBuilder queryJoins = new StringBuilder();
        queryJoins.append(" INNER JOIN project_department_map pdm ON p.project_id = pdm.project_id \n")
                .append(" LEFT JOIN clients c ON c.client_id = p.client_id  \n")
                .append(" LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id \n")
                .append(" LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n");

        query.append(" INNER JOIN teams t ON p.project_id = t.project_id  \n")
                .append(" INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id  \n")
                .append(queryJoins);

        query.append(" WHERE p.active = 'true' AND t.is_active = 'Y' AND etm.active != 0 \n")
                .append(" AND p.is_draft_project = 'false' ")
                .append(" AND (pdm.dept_id IN :deptIds) \n");
        if (projectNames != null && !projectNames.isEmpty()) {
            query.append(" AND p.project_name IN (:projectNames) \n");
        }
        query.append(groupQuery);

        query1.append(queryJoins);
        query1.append(" WHERE p.active = 'true' AND p.is_draft_project = 'true' \n")
                .append(" AND (pdm.dept_id IN :deptIds) \n");
        if (projectNames != null && !projectNames.isEmpty()) {
            query1.append(" AND p.project_name IN (:projectNames) \n");
        }
        query1.append(groupQuery);

        query2.append(queryJoins);
        query2.append(" WHERE p.active= 'true' AND p.is_draft_project IS NULL \n")
                .append(" AND (p.status != 'Completed' OR p.status IS NULL) \n")
                .append(" AND (p.internal_project_type IS NOT NULL OR DATE(p.po_end_date) > CURDATE()) \n")
                .append(" AND (pdm.dept_id IN :deptIds) \n");
        if (projectNames != null && !projectNames.isEmpty()) {
            query2.append(" AND p.project_name IN (:projectNames) \n");
        }
        query2.append(groupQuery);

        query3.append(queryJoins);
        query3.append(" WHERE p.active= 'true' AND UPPER(p.is_draft_project) = 'REJECTED' \n")
                .append(" AND (pdm.dept_id IN :deptIds) \n");
        if (projectNames != null && !projectNames.isEmpty()) {
            query3.append(" AND p.project_name IN (:projectNames) \n");
        }
        query3.append(groupQuery);

        query.append(" UNION ALL \n").append(query1).append(" UNION ALL \n").append(query2).append(" UNION ALL \n")
                .append(query3).append(" ) as T1");

        appenCustomSearchToQuery(projectFilter, query);
        query.append(String.format(" ORDER BY %s %s ", sortBy, sortDirection));
        return query.toString();
    }

    private String getUnfilledPositionsProjectQuery(Pageable page, Map<String, String> projectFilter, String sortBy,
            String sortDirection, List<String> projectNames) {
        StringBuilder query2 = new StringBuilder(projectDetailsStartQuery);
        StringBuilder groupQuery = new StringBuilder(projectDetailsGroupQuery).append(" )");
        StringBuilder query = new StringBuilder();
        query.append(" ( WITH RelevantProjects AS ( \n")
                .append(query2)
                .append(" LEFT JOIN project_department_map pdm ON p.project_id = pdm.project_id \n")
                .append(" INNER JOIN department d ON pdm.dept_id = d.dept_id \n")
                .append(" INNER JOIN teams t ON p.project_id = t.project_id \n")
                .append(" INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n")
                .append(" LEFT JOIN clients c ON p.client_id = c.client_id \n")
                .append(" LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id \n")
                .append(" LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n")
                .append(" WHERE 1=1 \n")
                .append(" AND p.po_project_type = 'TNM' AND p.active != 'false' AND t.is_active != 'N' \n");

        if (projectNames != null && !projectNames.isEmpty()) {
            query.append(" AND p.project_name IN (:projectNames) \n");
        }
        query.append(groupQuery)
                .append(" ) \n")
                .append(" , FilledCounts AS ( \n")
                .append(" SELECT \n")
                .append(" etm.resource_overview_id, COUNT(DISTINCT etm.emp_id) AS filled_count \n")
                .append(" FROM employee_team_mapping etm \n")
                .append(" INNER JOIN teams t ON etm.team_id = t.team_id \n")
                .append(" INNER JOIN employee e ON etm.emp_id = e.emp_id \n")
                .append(" INNER JOIN projects p ON p.project_id = t.project_id \n")
                .append(" WHERE 1=1 \n")
                .append(" AND t.is_active != 'N' \n")
                .append(" AND e.employmentstatus != 'InActive' \n")
                .append(" AND p.po_project_type = 'TNM' \n")
                .append(" AND p.active != 'false'\n")
                .append(" AND etm.resource_overview_id IS NOT NULL \n")
                .append(" GROUP BY etm.resource_overview_id \n")
                .append(" ) \n")

                .append(" , TotalRequirements AS ( \n")
                .append(" select dept_id, project_id, sum(required_count) as required_count from \n")
                .append(" (SELECT DISTINCT \n")
                .append(" d.dept_id,rp.project_id,rr.count AS required_count,role \n")
                .append(" FROM RelevantProjects rp \n")
                .append(" INNER JOIN resource_requirement rr ON rp.project_id = rr.project_id \n")
                .append(" INNER JOIN department d on d.name = rr.department) req \n")
                .append(" GROUP BY dept_id, project_id \n")
                .append(" ) \n")

                .append(" , UnfilledPositions AS ( \n")
                .append(" SELECT \n")
                .append(" rp.project_id, \n")
                .append(" SUM(GREATEST(0, rr.count - IFNULL(fc.filled_count, 0))) AS unfilled_count \n")
                .append(" FROM RelevantProjects rp \n")
                .append(" INNER JOIN resource_requirement rr ON rp.project_id = rr.project_id \n")
                .append(" LEFT JOIN FilledCounts fc ON rr.resource_overview_id = fc.resource_overview_id \n")
                .append(" GROUP BY rp.project_id \n")
                .append(" ) \n")

                .append(" SELECT DISTINCT rp.* \n")
                .append(" FROM RelevantProjects rp \n")
                .append(" INNER JOIN TotalRequirements tr ON rp.project_id = tr.project_id \n")
                .append(" INNER JOIN UnfilledPositions up ON rp.project_id = up.project_id \n")
                .append(" WHERE IFNULL(tr.required_count, 0) > IFNULL(up.unfilled_count, 0) \n")
                .append(" AND rp.dept_id in :deptIds \n")
                .append(" AND up.unfilled_count > 0 \n");
        query.append(" ) as T1");
        appenCustomSearchToQuery(projectFilter, query);
        query.append(String.format(" ORDER BY %s %s ", sortBy, sortDirection));
        return query.toString();
    }

    private String getGeneralProjectFilterQuery(Pageable page, Map<String, String> projectFilter, String sortBy,
            String sortDirection, String approvalStatus, List<String> projectNames) {
        StringBuilder query = new StringBuilder(projectDetailsStartQuery);
        StringBuilder groupQuery = new StringBuilder(projectDetailsGroupQuery).append(" ) as T1");

        if (approvalStatus.equalsIgnoreCase("Not Started") || approvalStatus.equalsIgnoreCase("PendingProjects")
                || approvalStatus.equalsIgnoreCase("RejectedProjects") || approvalStatus.equalsIgnoreCase("All")) {
            query.append(approvalStatus.equalsIgnoreCase("All") ? " LEFT JOIN " : " INNER JOIN ")
                    .append(" project_department_map pdm ON p.project_id = pdm.project_id \n")
                    .append(" LEFT JOIN clients c ON c.client_id = p.client_id \n")
                    .append(" LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id \n")
                    .append(" LEFT JOIN employee e1 ON e1.emp_id = pm.project_manager_id \n");
        } else if (approvalStatus.equalsIgnoreCase("ApprovedProjects")) {
            query.append(" INNER JOIN teams t ON p.project_id = t.project_id \n")
                    .append(" INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n")
                    .append(" INNER JOIN project_department_map pdm ON p.project_id = pdm.project_id \n")
                    .append(" LEFT JOIN clients c ON c.client_id = p.client_id \n")
                    .append(" LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id \n")
                    .append(" LEFT JOIN employee e1 ON e1.emp_id = pm.project_manager_id \n");
        } else if (approvalStatus.equalsIgnoreCase("completedInIshine")) {
            query.append(" INNER JOIN project_department_map pdm ON p.project_id = pdm.project_id \n")
                    .append(" INNER JOIN department d ON pdm.dept_id = d.dept_id \n")
                    .append(" LEFT JOIN clients c ON c.client_id = p.client_id \n")
                    .append(" LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id \n")
                    .append(" LEFT JOIN employee e1 ON e1.emp_id = pm.project_manager_id \n");
        }
        // Query is pending for below 3
        else if (approvalStatus.equalsIgnoreCase("CompletedWithShankh")) {
            query.append("LEFT JOIN clients c ON c.client_id = p.client_id \n")
                    .append(" LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id \n")
                    .append(" LEFT JOIN employee e1 ON e1.emp_id = pm.project_manager_id \n");
        } else if (approvalStatus.equalsIgnoreCase("CompletedWithTeam")) {
            query.append(" INNER JOIN teams t ON p.project_id = t.project_id \n")
                    .append(" INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n")
                    .append(" INNER JOIN employee e ON e.emp_id = etm.emp_id \n")
                    .append(" INNER JOIN job_role j1 ON j1.job_role_id = e.job_role_id \n")
                    .append(" LEFT JOIN clients c ON p.client_id = c.client_id \n")
                    .append(" LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id \n")
                    .append(" LEFT JOIN employee e1 ON e1.emp_id = pm.project_manager_id \n");
        }

        query.append(" WHERE 1=1 \n");
        if (!approvalStatus.equalsIgnoreCase("CompletedWithShankh") &&
                !approvalStatus.equalsIgnoreCase("CompletedWithTeam") &&
                !approvalStatus.equalsIgnoreCase("All")) {
            query.append(" AND pdm.dept_id IN :deptIds ");
        }

        if (approvalStatus.equalsIgnoreCase("Not Started")) {
            query.append(" AND p.active= 'true' AND p.is_draft_project IS NULL \n")
                    .append(" AND (p.status != 'Completed' or p.status IS NULL) \n")
                    .append(" AND (p.internal_project_type IS NOT NULL or DATE(p.po_end_date) > CURDATE()) \n");
        } else if (approvalStatus.equalsIgnoreCase("PendingProjects")) {
            query.append(" AND p.active= 'true' AND p.is_draft_project = 'true' \n");
        } else if (approvalStatus.equalsIgnoreCase("ApprovedProjects")) {
            query.append(" AND p.active= 'true' AND t.is_active = 'Y' AND etm.active != 0 \n")
                    .append(" AND p.is_draft_project = 'false' ");
        } else if (approvalStatus.equalsIgnoreCase("RejectedProjects")) {
            query.append(" AND p.active= 'true' AND UPPER(p.is_draft_project) = 'REJECTED' \n");
        } else if (approvalStatus.equalsIgnoreCase("completedInIshine")) {
            query.append(" AND p.project_status = 'Completed' \n");
        } else if (approvalStatus.equalsIgnoreCase("CompletedWithShankh")) {
            query.append(" AND p.active= 'true' AND p.status = 'Completed' \n")
                    .append(" AND (:isProjectId IS false OR p.project_id IN :projectIds )  \n")
                    .append(" AND (:approvalCheck IS false) \n")
                    .append(" AND (:projectStatus IS NULL OR p.project_status = :projectStatus) \n")
                    .append(" AND p.is_draft_project IS NOT NULL \n");
        } else if (approvalStatus.equalsIgnoreCase("CompletedWithTeam")) {
            query.append(" AND p.active = 'true' AND t.is_active != 'N' AND etm.active != 0 \n")
                    .append(" AND e.employmentstatus != 'InActive' AND p.status = 'Completed'  \n")
                    .append(" AND p.project_id IN :projectIds \n");
        } else if (approvalStatus.equalsIgnoreCase("All")) {
            query.append(" AND (pdm.dept_id is NULL or pdm.dept_id IN :deptIds) \n");
        }

        if (projectNames != null && !projectNames.isEmpty()) {
            query.append(" AND p.project_name IN (:projectNames) \n");
        }

        query.append(groupQuery);
        appenCustomSearchToQuery(projectFilter, query);
        query.append(String.format(" ORDER BY %s %s ", sortBy, sortDirection));
        return query.toString();
    }

    private String getFCProjectQuery(Pageable page, Map<String, String> projectFilter, String sortBy,
            String sortDirection, String type, String fixedCostFilter, List<String> projectNames) {
        StringBuilder query = new StringBuilder(projectDetailsStartQuery);
        StringBuilder groupQuery = new StringBuilder(projectDetailsGroupQuery).append(" ) as T1");

        query.append(" INNER JOIN project_manager_mapping pm on p.project_id = pm.project_id \n")
                .append(" LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n")
                .append(" INNER JOIN project_department_map pd ON p.project_id = pd.project_id\n")
                .append(" INNER JOIN department d ON pd.dept_id = d.dept_id \n")
                .append(" INNER JOIN teams t ON p.project_id = t.project_id \n")
                .append(" INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n")
                .append(" INNER JOIN clients c ON p.client_id = c.client_id \n");

        query.append(" WHERE 1=1 \n")
                .append(" AND po_project_type = 'Fixed Cost' \n")
                .append(" AND etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' \n")
                .append("AND d.dept_id IN :deptIds \n ");

        if (fixedCostFilter.equals("defaulter")) {
            query.append(" AND DATE(p.po_end_date) < CURDATE() \n");
        } else if (fixedCostFilter.equals("ontime")) {
            query.append(" AND CURDATE() between DATE(p.po_start_date) AND DATE(p.po_end_date) \n");
        } else if (fixedCostFilter.equals("delays")) {
            query.append(" AND p.project_id IN (select m1.project_id from milestone_updated_logs m1) \n")
                    .append(" AND CURDATE() between DATE(p.po_start_date) and DATE(p.po_end_date) \n");
        }

        if (projectNames != null && !projectNames.isEmpty()) {
            query.append(" AND p.project_name IN (:projectNames)\n");
        }

        query.append(groupQuery);
        appenCustomSearchToQuery(projectFilter, query);
        query.append(String.format(" ORDER BY %s %s ", sortBy, sortDirection));
        return query.toString();
    }

    private void populateProjectManagersAndOverheads(List<ProjectFetchDTO> allProjects) {
        List<Long> projectIds = allProjects.stream()
                .peek(data -> data.setActive(null))
                .map(ProjectFetchDTO::getProjectId)
                .filter(Objects::nonNull)
                .map(Integer::longValue)
                .collect(Collectors.toList());

        if (projectIds == null || projectIds.isEmpty()) {
            return;
        }

        List<ProjectManagersDTO> pmData = projectManagerMappingRepository
                .getAllProjectManagerListWithNameThroughPids(projectIds);

        List<ProjectOverheadsDTO> overHeadData = projectOverheadMappingRepository
                .findProjectOverheadsPerProjectThroughPidList(projectIds);

        Map<Long, List<ProjectManagersDTO>> pmDataMap = pmData.stream()
                .filter(pm -> pm.getProjectId() != null)
                .collect(Collectors.groupingBy(ProjectManagersDTO::getProjectId));

        Map<Long, List<ProjectOverheadsDTO>> overheadDataMap = overHeadData.stream()
                .filter(oh -> oh.getProjectId() != null)
                .collect(Collectors.groupingBy(ProjectOverheadsDTO::getProjectId));

        allProjects.forEach(project -> {
            Long projectId = project.getProjectId() != null ? project.getProjectId().longValue() : null;
            if (projectId == null)
                return;

            // Set PM data
            List<ProjectManagersDTO> pmList = pmDataMap.getOrDefault(projectId, Collections.emptyList());
            project.setProjectManagers(pmList);
            project.setProjectManagerId(
                    pmList.stream()
                            .map(ProjectManagersDTO::getProjectManagerId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));

            // Set Overhead data
            List<ProjectOverheadsDTO> ohList = overheadDataMap.getOrDefault(projectId, Collections.emptyList());
            project.setProjectOverheads(ohList);
            project.setProjectOverheadId(
                    ohList.stream()
                            .map(ProjectOverheadsDTO::getProjectOverheadId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
        });
    }

    private List<LocalDate> getDateRange(String key) {
        LocalDate currentDate = LocalDate.now();
        Map<String, List<LocalDate>> dateRanges = Map.of(
                "expiredProjectsWithin1Month", List.of(currentDate.minusDays(30), currentDate),
                "expiredProjects1To2Months", List.of(currentDate.minusDays(60), currentDate.minusDays(31)),
                "expiredProjects2To3Months", List.of(currentDate.minusDays(90), currentDate.minusDays(61)),
                "expiredProjects3To6Months", List.of(currentDate.minusDays(180), currentDate.minusDays(91)),
                "expiredProjects6To9Months", List.of(currentDate.minusDays(270), currentDate.minusDays(181)),
                "expiredProjects9To12Months", List.of(currentDate.minusDays(365), currentDate.minusDays(271)),
                "expiredProjectsAbove12Months", List.of(currentDate.minusYears(10), currentDate.minusDays(366)));
        return dateRanges.getOrDefault(key, List.of());
    }

    private String getSortBy(String sortColumn, boolean defaultFlag) {
        switch (sortColumn) {
            case "name":
                return "project_name";
            case "poNo":
                return "po_no";
            case "poProjectType":
                return "po_project_type";
            case "projectManagerName":
                return "Project_Manager";
            case "clientName":
                return "client_name";
            case "apmosysRM":
                return "apmosysrm";
            case "clientRM":
                return "clientrm";
            case "poStartDate":
                return "po_start_date";
            case "poEndDate":
                return "po_end_date";
            case "state":
                return "state";
            case "createdOn":
                return "created_on";
            case "status":
                return " po_project_status";
            case "projectStatus":
                return " ishine_project_status";
            case "draftStatus":
                return "Approval_status";
            default:
                return defaultFlag ? "project_name" : null;
        }
    }

    private void appenCustomSearchToQuery(Map<String, String> projectFilter, StringBuilder query) {
        if (projectFilter != null && !projectFilter.isEmpty()) {
            query.append(" WHERE 1=1 ");
            for (Map.Entry<String, String> entry : projectFilter.entrySet()) {
                String column = getSortBy(entry.getKey(), false);
                String value = entry.getValue();
                if (column != null && !column.trim().isEmpty() && value != null && !value.trim().isEmpty()) {
                    query.append(String.format(" AND LOWER( %s ) LIKE '%%%s%%' \n", column,
                            value.replace("'", "''").toLowerCase()));
                }
            }
            query.append(" ");
        }
    }
}
