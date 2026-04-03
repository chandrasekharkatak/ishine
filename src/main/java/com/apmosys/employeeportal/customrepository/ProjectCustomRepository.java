package com.apmosys.employeeportal.customrepository;

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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
            .append(",GROUP_CONCAT(DISTINCT ppd.po_no ORDER BY ppd.po_start_date SEPARATOR ', ') AS po_no \n")
            .append(",p.po_project_id, p.start_date, p.end_date \n")
            .append(",p.client_id, c.client_name \n")
            .append(",GROUP_CONCAT(DISTINCT ppd.client_rm ORDER BY ppd.po_start_date SEPARATOR ', ') AS client_rm \n")
            .append(",GROUP_CONCAT(DISTINCT ppd.apmosys_rm ORDER BY ppd.po_start_date SEPARATOR ', ') AS apmosys_rm \n")
            .append(",GROUP_CONCAT(DISTINCT cl.client_state ORDER BY cl.client_state SEPARATOR ', ') AS state \n")
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
            .append(" \n GROUP BY \n")
            .append(" p.project_id, p.project_name, p.internal_project_type \n")
            .append(" ,p.po_project_id, p.start_date, p.end_date \n")
            .append(" ,p.client_id,c.client_name \n")
            .append(" ,p.active, p.status , p.project_status, p.sync_project, p.created_by, p.updated_by, approval_status \n")
            .append(" ,project_overview_id, p.created_on, p.updated_on, p.project_completion_date \n");

    public Slice<ProjectFetchDTO> handleProjectsByType(RMGDashboardProjectRequest rmgDashboardProjectRequest,
            List<Long> deptIds, String projectStatus, List<String> projectNames, String sortBy, String sortDirection,
            Pageable page) {

        String query = getQuery(rmgDashboardProjectRequest.getProjectFilter(), sortBy, sortDirection,
                projectStatus, false, projectNames);

        CompletableFuture<List<ProjectFetchDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page,
                        projectStatus, "", "", projectNames, null, "", false, false));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                                false));
        CompletableFuture.allOf(listFuture, countFuture).join();

        Long count = null;
        List<ProjectFetchDTO> list = null;
        try {
            count = countFuture.get();
            list = listFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing async DB calls", e);
        }
        return new PageImpl<>(list, page, count);
    }

    public Slice<ProjectFetchDTO> handleAllProjects(RMGDashboardProjectRequest rmgDashboardProjectRequest,
            List<Long> deptIds, String projectStatus, List<String> projectNames, String sortBy, String sortDirection,
            Pageable page) {

        String query = getAllProjectsQuery(rmgDashboardProjectRequest.getProjectFilter(), sortBy, sortDirection,
                projectNames);

        CompletableFuture<List<ProjectFetchDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page, projectStatus, "", "",
                        projectNames, null, "", false, false));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                                false));
        CompletableFuture.allOf(listFuture, countFuture).join();

        Long count = null;
        List<ProjectFetchDTO> list = null;
        try {
            count = countFuture.get();
            list = listFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing async DB calls", e);
        }
        populateProjectManagersAndOverheads(list);
        return new PageImpl<>(list, page, count);
    }

    public Slice<ProjectFetchDTO> handleFCProjects(RMGDashboardProjectRequest rmgDashboardProjectRequest,
            List<Long> deptIds, String projectStatus, List<String> projectNames, String sortBy, String sortDirection,
            Pageable page) {

        String query = getFCProjectQuery(rmgDashboardProjectRequest.getProjectFilter(), sortBy, sortDirection,
                rmgDashboardProjectRequest.getFixedCostFilter(), projectNames);

        CompletableFuture<List<ProjectFetchDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page, projectStatus, "", "",
                        projectNames, null, "", false, false));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                                false));
        CompletableFuture.allOf(listFuture, countFuture).join();

        Long count = null;
        List<ProjectFetchDTO> list = null;
        try {
            count = countFuture.get();
            list = listFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing async DB calls", e);
        }
        populateProjectManagersAndOverheads(list);
        return new PageImpl<>(list, page, count);
    }

    public Slice<ProjectFetchDTO> handleOverboardedAndUnderboardedProjects(RMGDashboardProjectRequest rmgDashboardProjectRequest,
            List<Long> deptIds, String projectStatus, List<String> projectNames, String sortBy, String sortDirection,
            Pageable page) {

        String query = getOverboardedAndUnderboardedProjectQuery(rmgDashboardProjectRequest.getProjectFilter(), sortBy,
                sortDirection, projectNames, projectStatus);

        CompletableFuture<List<ProjectFetchDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page, projectStatus, "", "",
                        projectNames, null, "", false, false));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                                false));
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
            List<Long> deptIds, String projectStatus, List<String> projectNames, String sortBy, String sortDirection,
            Pageable page) {

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

        String query = getQuery(rmgDashboardProjectRequest.getProjectFilter(), sortBy,
                sortDirection, projectStatus, addStartAndEndDate, projectNames);

        final String sDate = startDate;
        final String eDate = endDate;
        final boolean faddStartAndEndDate = addStartAndEndDate;
        CompletableFuture<List<ProjectFetchDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page, projectStatus, sDate,
                        eDate, projectNames, null, "", false, faddStartAndEndDate));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(() -> getResultCount(query, deptIds, projectStatus, sDate, eDate, projectNames, null, "",
                        false, faddStartAndEndDate));
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
            List<Long> deptIds, Set<Integer> projectIds, String projectStatus, List<String> projectNames, String sortBy,
            String sortDirection, Pageable page) {

        boolean isProjectId = !"ADMIN".equals(req.getCurrentUserType());
        String dbProjectStatus = getDBProjectStatus(projectStatus);
        String query = buildQueryForMode(req, sortBy, sortDirection, projectStatus, projectNames);

        CompletableFuture<Long> countFuture;
        CompletableFuture<List<ProjectFetchDTO>> listFuture;

        listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page, projectStatus, "", "",
                        projectNames, projectIds, dbProjectStatus, isProjectId, false));
        countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, projectIds,
                                dbProjectStatus, isProjectId, false));
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
    
    public Long handleAllProjectsCount(RMGDashboardProjectRequest rmgDashboardProjectRequest,
            List<Long> deptIds, String projectStatus, List<String> projectNames, String sortBy, String sortDirection) {

        String query = getAllProjectsQuery(rmgDashboardProjectRequest.getProjectFilter(), sortBy, sortDirection,
                projectNames);

        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                                false));
        CompletableFuture.allOf(countFuture).join();

        Long count = 0l;
        try {
            count = countFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing async DB calls", e);
        }
        return count;
    }

    public Long handleFCProjectsCount(RMGDashboardProjectRequest rmgDashboardProjectRequest,
            List<Long> deptIds, String projectStatus, List<String> projectNames, String sortBy, String sortDirection) {

        String query = getFCProjectQuery(rmgDashboardProjectRequest.getProjectFilter(), sortBy, sortDirection,
                rmgDashboardProjectRequest.getFixedCostFilter(), projectNames);

        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                                false));
        CompletableFuture.allOf(countFuture).join();

        Long count = 0l;
        try {
            count = countFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing async DB calls", e);
        }
        return count;
    }

    public Long handleGeneralProjectFiltersCount(RMGDashboardProjectRequest req,
            List<Long> deptIds, Set<Integer> projectIds, String projectStatus, List<String> projectNames, String sortBy,
            String sortDirection) {

        boolean isProjectId = !"ADMIN".equals(req.getCurrentUserType());
        String dbProjectStatus = getDBProjectStatus(projectStatus);
        String query = buildQueryForMode(req, sortBy, sortDirection, projectStatus, projectNames);

        CompletableFuture<Long> countFuture;

        countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, projectIds,
                                dbProjectStatus, isProjectId, false));
        Long count = 0l;
        try {
            CompletableFuture.allOf(countFuture).join();
            count = countFuture.get();
        } catch (InterruptedException | ExecutionException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing async DB calls");
        }
        return count;
    }

    public Long handleOverboardedAndUnderboardedProjectsCount(RMGDashboardProjectRequest rmgDashboardProjectRequest,
            List<Long> deptIds, String projectStatus, List<String> projectNames, String sortBy, String sortDirection) {

        String query = getOverboardedAndUnderboardedProjectQuery(rmgDashboardProjectRequest.getProjectFilter(), sortBy,
                sortDirection, projectNames, projectStatus);

        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                                false));
        CompletableFuture.allOf(countFuture).join();

        Long count = 0l;
        try {
            count = countFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing async DB calls", e);
        }
        return count;
    }

    public Long handleProjectsByTypeCount(RMGDashboardProjectRequest rmgDashboardProjectRequest,
            List<Long> deptIds, String projectStatus, List<String> projectNames, String sortBy, String sortDirection) {

        String query = getQuery(rmgDashboardProjectRequest.getProjectFilter(), sortBy, sortDirection,
                projectStatus, false, projectNames);

        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                                false));
        CompletableFuture.allOf(countFuture).join();

        Long count = 0l;
        try {
            count = countFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing async DB calls", e);
        }
        return count;
    }

    public Long handleExpiredTNMProjectsCount(RMGDashboardProjectRequest rmgDashboardProjectRequest,
            List<Long> deptIds, String projectStatus, List<String> projectNames, String sortBy, String sortDirection) {

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

        String query = getQuery(rmgDashboardProjectRequest.getProjectFilter(), sortBy,
                sortDirection, projectStatus, addStartAndEndDate, projectNames);

        final String sDate = startDate;
        final String eDate = endDate;
        final boolean faddStartAndEndDate = addStartAndEndDate;

        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(() -> getResultCount(query, deptIds, projectStatus, sDate, eDate, projectNames, null, "",
                        false, faddStartAndEndDate));
        CompletableFuture.allOf(countFuture).join();

        Long count = 0l;
        try {
            count = countFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing async DB calls", e);
        }
        return count;
    }

    private List<ProjectFetchDTO> getResultList(String query, String sortBy, String sortDirection, List<Long> deptIds,
            Pageable page, String projectStatus, String startDate, String endDate, List<String> projectNames,
            Set<Integer> projectIds, String dbProjectStatus, boolean isProjectId, boolean addStartAndEndDate) {

        query = "SELECT * FROM " + query;
        System.out.println(projectStatus + " : ================================= query");
        System.out.println(query);
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<Object[]> nativeQuery = session.createNativeQuery(query)
                    .unwrap(org.hibernate.query.NativeQuery.class);
           
            nativeQuery.setParameter("deptIds", deptIds);
            if (projectStatus != null && projectStatus.equals("TOTAL_EXPIRED_TNM") && addStartAndEndDate) {
                nativeQuery.setParameter("startDate", startDate);
                nativeQuery.setParameter("endDate", endDate);
            }
            
            if (projectStatus.equals("COMPLETED_IN_SHANKH")
                    || projectStatus.equals("COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE")) {
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

    private Long getResultCount(String query, List<Long> deptIds, String projectStatus, String startDate,
            String endDate, List<String> projectNames, Set<Integer> projectIds, String dbProjectStatus,
            boolean isProjectId, boolean addStartAndEndDate) {
        query = "SELECT count(*) FROM " + query;
        System.out.println(projectStatus + " : ================================= query");
        System.out.println(query);
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<Object[]> nativeQuery = session.createNativeQuery(query)
                    .unwrap(org.hibernate.query.NativeQuery.class);

            nativeQuery.setParameter("deptIds", deptIds);
            if (projectStatus != null && projectStatus.equals("TOTAL_EXPIRED_TNM") && addStartAndEndDate) {
                nativeQuery.setParameter("startDate", startDate);
                nativeQuery.setParameter("endDate", endDate);
            }
            if (projectStatus.equals("COMPLETED_IN_SHANKH")
                    || projectStatus.equals("COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE")) {
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
    
    private String buildQueryForMode(RMGDashboardProjectRequest req, String sortBy, String sortDirection,
            String projectStatus, List<String> projectNames) {
        Set<String> approvalStatusSet = Set.of("ALL", "NOT_STARTED", "COMPLETED_IN_ISHINE",
                "APPROVED", "PENDING_FOR_APPROVAL", "REJECTED", "COMPLETED_IN_SHANKH",
                "COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE","OFFBOARDED","SCHEDULED");
        if (projectStatus != null && approvalStatusSet.contains(projectStatus)) {
            return getGeneralProjectFilterQuery(req.getProjectFilter(), sortBy, sortDirection, projectStatus,
                    projectNames);
        }
        return getAllProjectsQuery(req.getProjectFilter(), sortBy, sortDirection, projectNames);
    }

    private String getQuery(Map<String, String> projectFilter, String sortBy,
            String sortDirection, String projectStatus, boolean addStartAndEndDate, List<String> projectNames) {
        StringBuilder query = new StringBuilder(projectDetailsStartQuery);
        StringBuilder groupQuery = new StringBuilder(projectDetailsGroupQuery).append(" ) as T1");

        query.append(" INNER JOIN teams t ON p.project_id = t.project_id \n")
                .append(" INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n")
                .append(" LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id AND DATE(ppd.po_start_date) <= CURRENT_DATE AND (ppd.po_end_date IS NULL OR DATE(ppd.po_end_date) >= CURRENT_DATE) AND ppd.active  = 1 \n")
                .append(" INNER JOIN po_department_mapping pdm on ((ppd.po_id IS NOT NULL AND pdm.po_id = ppd.po_id) OR (ppd.po_id IS NULL AND pdm.project_id = p.project_id)) AND pdm.dept_id IN :deptIds \n")
                .append(" LEFT JOIN department d ON pdm.dept_id = d.dept_id \n")
                .append(" LEFT JOIN clients c ON p.client_id = c.client_id \n")
                .append(" LEFT JOIN client_locations cl ON p.client_id = cl.client_id and lower(cl.client_location) != 'wfh' \n")
                .append(" LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id AND pm.active = 1  \n")
                .append(" LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n")
                .append(" WHERE 1=1 \n");
        
        if(!projectStatus.equals("ALL_TNM") ){
            query.append("  AND etm.active != 0 AND t.is_active != 'N' AND p.active != 'false' \n");
        }
        if (projectStatus.equals("TOTAL_ACTIVE_TNM") || projectStatus.equals("TOTAL_EXPIRED_TNM")
                || projectStatus.equals("TOTAL_TNM") || projectStatus.equals("ALL_TNM")) {
            query.append(" AND po_project_type = 'TNM' \n");
        }
        
        if (projectStatus.equals("TOTAL_INTERNAL")) {
            query.append(" AND p.internal_project_type is not null AND TRIM(p.internal_project_type) != '' \n");
        }
        else if (projectStatus.equals("TOTAL_MONITORING")) {
            query.append(" AND p.po_project_type = 'Monitoring' \n");
		}
        else if (projectStatus.equals("TOTAL_EXPIRED_TNM")) {
			query.append("  AND DATE(p.end_date) < CURDATE() \n");
			if (addStartAndEndDate) {
				query.append("  AND DATE(p.end_date) between :startDate and :endDate \n");
			}
		}
        
        if (projectNames != null && !projectNames.isEmpty()) {
            query.append(" AND p.project_name IN (:projectNames) \n");
        }
        
        query.append(groupQuery);
        appenCustomSearchToQuery(projectFilter, query);
        query.append(String.format(" ORDER BY %s %s ", sortBy, sortDirection));
        return query.toString();
    }

    private String getAllProjectsQuery(Map<String, String> projectFilter, String sortBy,
			String sortDirection, List<String> projectNames) {
		StringBuilder query = new StringBuilder(" ( " + projectDetailsStartQuery); // Approved
		StringBuilder query1 = new StringBuilder(projectDetailsStartQuery); // Pending for Approval
		StringBuilder query2 = new StringBuilder(projectDetailsStartQuery); // Not Started
		StringBuilder query3 = new StringBuilder(projectDetailsStartQuery); // Rejected
		StringBuilder query4 = new StringBuilder(projectDetailsStartQuery); // Offboarded 
		StringBuilder query5 = new StringBuilder(projectDetailsStartQuery); // Scheduled
		StringBuilder groupQuery = new StringBuilder(projectDetailsGroupQuery).append(" )");

		StringBuilder queryJoins = new StringBuilder();
		queryJoins.append(
				" LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id AND DATE(ppd.po_start_date) <= CURRENT_DATE AND (ppd.po_end_date IS NULL OR DATE(ppd.po_end_date) >= CURRENT_DATE) AND ppd.active  = 1 \n")
				.append(" INNER JOIN po_department_mapping pdm on ((ppd.po_id IS NOT NULL AND pdm.po_id = ppd.po_id) OR (ppd.po_id IS NULL AND pdm.project_id = p.project_id)) AND (pdm.dept_id IN :deptIds)  \n")
				.append(" LEFT JOIN clients c ON c.client_id = p.client_id  \n")
				.append(" LEFT JOIN client_locations cl ON p.client_id = cl.client_id and lower(cl.client_location) != 'wfh' \n")
				.append(" LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id AND pm.active = 1  \n")
				.append(" LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n");

		query.append(" INNER JOIN teams t ON p.project_id = t.project_id  \n")
				.append(" INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id  \n")
				.append(queryJoins)
				.append(" WHERE 1=1 \n")
				.append(getApprovedProjectsCondition());

		query1.append(queryJoins)
			  .append(" WHERE 1=1 \n")
			  .append(getPendingForApprovalProjectsCondition());

		query2.append(queryJoins)
		      .append(" WHERE 1=1 \n")
		      .append(getNotStartedProjectsCondition());

		query3.append(queryJoins)
			  .append(" WHERE 1=1 \n")
			  .append(getRejectedProjectsCondition());
		
		query4.append(queryJoins)
			  .append(" WHERE 1=1 \n")
			  .append(getOffBoardedProjectsCondition());
		
		query5.append(" INNER JOIN teams t ON p.project_id = t.project_id  \n")
			  .append(" INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id  \n")
			  .append(queryJoins)
			  .append(" WHERE 1=1 \n")
		      .append(getScheduledProjectsCondition());
		
		if (projectNames != null && !projectNames.isEmpty()) {
			query.append(" AND p.project_name IN (:projectNames) \n");
			query1.append(" AND p.project_name IN (:projectNames) \n");
			query2.append(" AND p.project_name IN (:projectNames) \n");
			query3.append(" AND p.project_name IN (:projectNames) \n");
			query4.append(" AND p.project_name IN (:projectNames) \n");
			query5.append(" AND p.project_name IN (:projectNames) \n");
		}
		query.append(groupQuery);
		query1.append(groupQuery);
		query2.append(groupQuery);
		query3.append(groupQuery);
		query4.append(groupQuery);
		query5.append(groupQuery);

		query.append(" UNION ALL \n")
			 .append(query1)
			 .append(" UNION ALL \n")
			 .append(query2)
			 .append(" UNION ALL \n")
			 .append(query3)
			 .append(" UNION ALL \n")
			 .append(query4)
			 .append(" UNION ALL \n")
			 .append(query5)
			 .append(" ) as T1");

		appenCustomSearchToQuery(projectFilter, query);
		query.append(String.format(" ORDER BY %s %s ", sortBy, sortDirection));
		return query.toString();
	}

    private String getOverboardedAndUnderboardedProjectQuery(Map<String, String> projectFilter, String sortBy,
			String sortDirection, List<String> projectNames, String projectStatus) {

		StringBuilder query2 = new StringBuilder(projectDetailsStartQuery.toString()
				.replace(" FROM projects p \n"," ,d.dept_id \n FROM projects p \n"));

		StringBuilder groupQuery = new StringBuilder(projectDetailsGroupQuery).append(" )");

		StringBuilder query = new StringBuilder();
		query.append(" ( ").append(query2)
				.append(" LEFT JOIN teams t ON p.project_id = t.project_id \n")
				.append(" LEFT JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n")
				.append(" LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id AND DATE(ppd.po_start_date) <= CURRENT_DATE AND (ppd.po_end_date IS NULL OR DATE(ppd.po_end_date) >= CURRENT_DATE) AND ppd.active  = 1 \n")
				.append(" INNER JOIN po_department_mapping pdm on ((ppd.po_id IS NOT NULL AND pdm.po_id = ppd.po_id) OR (ppd.po_id IS NULL AND pdm.project_id = p.project_id)) AND pdm.dept_id IN :deptIds \n")
				.append(" LEFT JOIN department d ON pdm.dept_id = d.dept_id \n")
				.append(" LEFT JOIN clients c ON p.client_id = c.client_id \n")
				.append(" LEFT JOIN client_locations cl ON p.client_id = cl.client_id and lower(cl.client_location) != 'wfh' \n")
				.append(" LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id AND pm.active = 1 \n")
				.append(" LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n")
				.append(" WHERE 1=1 \n")
				.append(" AND p.po_project_type = 'TNM' AND p.active != 'false' AND t.is_active != 'N' \n")
				.append(" AND EXISTS ( \n")
				.append("WITH ROLE_WISE_REQUIREMENT_COUNT AS ( \n")
				.append("SELECT p2.project_id, prm2.po_id, prm2.role_id, prm2.count required_count \n")
				.append("FROM po_requirement_mapping prm2 \n")
				.append("INNER JOIN project_po_details ppd2 ON ppd2.po_id = prm2.po_id AND DATE(ppd2.po_start_date) <= CURDATE() AND (ppd2.po_end_date IS NULL OR DATE(ppd2.po_end_date) >= CURDATE()) AND ppd2.active  = 1 \n")
				.append("INNER JOIN projects p2 ON p2.project_id = ppd2.project_id \n")
				.append("WHERE 1=1  \n")
				.append("AND p2.po_project_type = 'TNM'  \n")
				.append("AND p2.active != 'false' \n")
				.append(") , ROLE_WISE_ALLOCATED_COUNT AS ( \n")
				.append("SELECT p3.project_id, etm2.po_id, etm2.role_id, COUNT(DISTINCT etm2.emp_id) allocated_count \n")
				.append("from employee_team_mapping etm2 \n")
				.append("INNER JOIN teams t3 ON t3.team_id = etm2.team_id \n")
				.append("INNER JOIN projects p3 ON p3.project_id = t3.project_id  \n")
				.append("INNER JOIN project_po_details ppd3 ON ppd3.project_id = p3.project_id AND DATE(ppd3.po_start_date) <= CURDATE() AND (ppd3.po_end_date IS NULL OR DATE(ppd3.po_end_date) >= CURDATE()) AND ppd3.active  = 1 \n")
				.append("INNER JOIN employee e3 ON etm2.emp_id = e3.emp_id  \n")
				.append("WHERE 1=1 \n")
                .append("AND DATE(etm2.start_date) <= CURDATE() \n")
				.append("AND etm2.active != 0 AND e3.employmentstatus != 'InActive' \n")
				.append("AND t3.is_active != 'N' AND p3.po_project_type = 'TNM'  \n")
				.append("AND p3.active != 'false' \n")
				.append("GROUP BY p3.project_id, etm2.po_id, etm2.role_id \n")
				.append(") \n")
				.append("SELECT rc.project_id, COALESCE(rc.required_count,0), COALESCE(ac.allocated_count,0) \n")
				.append("from ROLE_WISE_REQUIREMENT_COUNT rc  \n")
				.append("LEFT JOIN ROLE_WISE_ALLOCATED_COUNT ac ON ac.role_id = rc.role_id AND ac.po_id = rc.po_id  \n")
				.append("WHERE 1=1 AND p.project_id = rc.project_id \n");

		if (projectStatus.equalsIgnoreCase("OVERBOARDED")) {
			query.append(" AND COALESCE(ac.allocated_count,0) > COALESCE(rc.required_count,0) \n ) \n");
		} else {
			query.append(" AND COALESCE(ac.allocated_count,0) < COALESCE(rc.required_count,0) \n ) \n");
		}

		if (projectNames != null && !projectNames.isEmpty()) {
			query.append(" AND p.project_name IN (:projectNames) \n");
		}

		query.append(groupQuery);
		query.append(" ) as T1");
		appenCustomSearchToQuery(projectFilter, query);
		query.append(String.format(" ORDER BY %s %s ", sortBy, sortDirection));
		return query.toString();
	}

    private String getGeneralProjectFilterQuery(Map<String, String> projectFilter, String sortBy,
            String sortDirection, String projectStatus, List<String> projectNames) {
        StringBuilder query = new StringBuilder(projectDetailsStartQuery);
        StringBuilder groupQuery = new StringBuilder(projectDetailsGroupQuery).append(" ) as T1");
        
		if (projectStatus.equalsIgnoreCase("ALL")) {
			query.append(" LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id AND ppd.active  = 1 AND (ppd.po_end_date IS NULL OR DATE(ppd.po_end_date) >= CURRENT_DATE \n")
					.append(" OR EXISTS ( SELECT 1 FROM project_po_details p2 WHERE p2.project_id = ppd.project_id \n")
					.append(" AND p2.po_no != ppd.po_no AND p2.active = 1 AND (p2.po_start_date <= ppd.po_end_date OR ppd.po_end_date IS NULL) AND  (p2.po_end_date >= ppd.po_start_date OR p2.po_end_date IS NULL))) \n")
					.append(" INNER JOIN po_department_mapping pdm on ((ppd.po_id IS NOT NULL AND pdm.po_id = ppd.po_id) OR (ppd.po_id IS NULL AND pdm.project_id = p.project_id)) AND pdm.dept_id IN :deptIds \n")
					.append(" LEFT JOIN clients c ON c.client_id = p.client_id \n")
					.append(" LEFT JOIN client_locations cl ON p.client_id = cl.client_id and lower(cl.client_location) != 'wfh' \n")
					.append(" LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id AND pm.active = 1 \n")
					.append(" LEFT JOIN employee e1 ON e1.emp_id = pm.project_manager_id \n");
		} else if (projectStatus.equalsIgnoreCase("COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE")) {
			query.append(" INNER JOIN teams t ON p.project_id = t.project_id \n")
					.append(" INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n")
					.append(" INNER JOIN employee e ON e.emp_id = etm.emp_id \n")
					.append(" INNER JOIN job_role j1 ON j1.job_role_id = e.job_role_id \n")
					.append(" LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id AND DATE(ppd.po_start_date) <= CURRENT_DATE AND (ppd.po_end_date IS NULL OR DATE(ppd.po_end_date) >= CURRENT_DATE) AND ppd.active  = 1 \n")
					.append(" INNER JOIN po_department_mapping pdm on ((ppd.po_id IS NOT NULL AND pdm.po_id = ppd.po_id) OR (ppd.po_id IS NULL AND pdm.project_id = p.project_id)) AND pdm.dept_id IN :deptIds \n")
					.append(" LEFT JOIN department d ON pdm.dept_id = d.dept_id \n")
					.append(" LEFT JOIN clients c ON p.client_id = c.client_id \n")
					.append(" LEFT JOIN client_locations cl ON p.client_id = cl.client_id and lower(cl.client_location) != 'wfh' \n")
					.append(" LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id AND pm.active = 1 \n")
					.append(" LEFT JOIN employee e1 ON e1.emp_id = pm.project_manager_id \n");
		} else {
			if (projectStatus.equalsIgnoreCase("APPROVED") || projectStatus.equalsIgnoreCase("SCHEDULED")) {
				query.append(" INNER JOIN teams t ON p.project_id = t.project_id \n")
						.append(" INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id \n");
			}
			query.append(" LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id AND DATE(ppd.po_start_date) <= CURRENT_DATE AND (ppd.po_end_date IS NULL OR DATE(ppd.po_end_date) >= CURRENT_DATE) AND ppd.active  = 1 \n")
					.append(" INNER JOIN po_department_mapping pdm on ((ppd.po_id IS NOT NULL AND pdm.po_id = ppd.po_id) OR (ppd.po_id IS NULL AND pdm.project_id = p.project_id)) AND pdm.dept_id IN :deptIds \n")
					.append(" LEFT JOIN department d ON pdm.dept_id = d.dept_id \n")
					.append(" LEFT JOIN clients c ON c.client_id = p.client_id \n")
					.append(" LEFT JOIN client_locations cl ON p.client_id = cl.client_id and lower(cl.client_location) != 'wfh' \n")
					.append(" LEFT JOIN project_manager_mapping pm ON p.project_id = pm.project_id AND pm.active = 1 \n")
					.append(" LEFT JOIN employee e1 ON e1.emp_id = pm.project_manager_id \n");
		}
        
        query.append(" WHERE 1=1 \n");

		if (projectStatus.equalsIgnoreCase("NOT_STARTED")) {
			query.append(getNotStartedProjectsCondition());
		}
		else if (projectStatus.equalsIgnoreCase("PENDING_FOR_APPROVAL")) {
			query.append(getPendingForApprovalProjectsCondition());
		}
		else if (projectStatus.equalsIgnoreCase("APPROVED")) {
			query.append(getApprovedProjectsCondition());
		} 
		else if (projectStatus.equalsIgnoreCase("REJECTED")) {
			query.append(getRejectedProjectsCondition());
		}
		else if (projectStatus.equalsIgnoreCase("OFFBOARDED")) {
			query.append(getOffBoardedProjectsCondition());
		} 
		else if (projectStatus.equalsIgnoreCase("SCHEDULED")) {
			query.append(getScheduledProjectsCondition());
		} 
		else if (projectStatus.equalsIgnoreCase("COMPLETED_IN_ISHINE")) {
			query.append(" AND p.project_status = 'Completed' \n");
		} 
		
		else if (projectStatus.equalsIgnoreCase("COMPLETED_IN_SHANKH")) {
			query.append(" AND p.active= 'true' AND p.status = 'Completed' \n")
					.append(" AND p.is_draft_project IS NOT NULL \n")
					.append(" AND p.project_id IN :projectIds  \n");
		}
		else if (projectStatus.equalsIgnoreCase("COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE")) {
			query.append(" AND p.active = 'true' AND t.is_active != 'N' AND etm.active != 0 \n")
					.append(" AND e.employmentstatus != 'InActive' AND p.status = 'Completed'  \n")
					.append(" AND p.project_id IN :projectIds \n");
		}

        if (projectNames != null && !projectNames.isEmpty()) {
            query.append(" AND p.project_name IN (:projectNames) \n");
        }

        query.append(groupQuery);
        appenCustomSearchToQuery(projectFilter, query);
        query.append(String.format(" ORDER BY %s %s ", sortBy, sortDirection));
        return query.toString();
    }
    
    private String getFCProjectQuery(Map<String, String> projectFilter, String sortBy,
			String sortDirection, String fixedCostFilter, List<String> projectNames) {
		StringBuilder query = new StringBuilder(projectDetailsStartQuery);
		StringBuilder groupQuery = new StringBuilder(projectDetailsGroupQuery).append(" ) as T1");

		query.append(" INNER JOIN project_po_details ppd ON ppd.project_id = p.project_id AND ppd.active  = 1 \n");
		query.append(" INNER JOIN po_department_mapping pdm on ((ppd.po_id IS NOT NULL AND pdm.po_id = ppd.po_id) OR (ppd.po_id IS NULL AND pdm.project_id = p.project_id)) AND pdm.dept_id IN :deptIds \n")
				.append(" INNER JOIN teams t ON p.project_id = t.project_id AND t.is_active != 'N' \n")
				.append(" INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id AND etm.active != 0 \n")
				.append(" LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id AND pm.active = 1 \n")
				.append(" LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n")
				.append(" LEFT JOIN department d ON pdm.dept_id = d.dept_id \n")
				.append(" LEFT JOIN clients c ON p.client_id = c.client_id \n")
				.append(" LEFT JOIN client_locations cl ON p.client_id = cl.client_id and lower(cl.client_location) != 'wfh' \n")
				.append(" WHERE 1=1 \n").append(" AND po_project_type = 'Fixed Cost' AND p.active = 'true' \n");

		if (fixedCostFilter != null) {
			if (fixedCostFilter.equals("ontime")) {
				query.append(" AND (ppd.po_end_date IS NULL OR DATE(ppd.po_end_date) >= CURDATE()) \n");
			} else if (fixedCostFilter.equals("defaulter")) {
				query.append(" AND DATE(ppd.po_end_date) < CURDATE() \n");
			}
		}

		if (projectNames != null && !projectNames.isEmpty()) {
			query.append(" AND p.project_name IN (:projectNames)\n");
		}

		query.append(groupQuery);
		appenCustomSearchToQuery(projectFilter, query);
		query.append(String.format(" ORDER BY %s %s ", sortBy, sortDirection));
		return query.toString();
	}
    
	private String getNotStartedProjectsCondition() {
		StringBuilder notStartedCondition = new StringBuilder();
		notStartedCondition.append(" AND p.active = 'true' AND p.is_draft_project IS NULL \n")
				.append(" AND (p.status != 'Completed' or p.status IS NULL) \n")
				.append(" AND (DATE(ppd.po_end_date) > CURDATE() OR ppd.po_end_date IS NULL ) \n")
				.append(" AND NOT EXISTS (SELECT 1 FROM teams t2 INNER JOIN employee_team_mapping etm2 ON t2.team_id = etm2.team_id WHERE t2.project_id = p.project_id ) \n")
				;
		return notStartedCondition.toString();
	}

	private String getPendingForApprovalProjectsCondition() {
		StringBuilder pendingForApprovalCondition = new StringBuilder();
		pendingForApprovalCondition.append(" AND p.active= 'true' AND p.is_draft_project = 'true' \n")
									.append(" AND EXISTS (SELECT 1 FROM teams t2 INNER JOIN employee_team_mapping etm2 ON t2.team_id = etm2.team_id WHERE t2.project_id = p.project_id AND etm2.active = 2 AND t2.is_active = 'Y' ) \n");
		return pendingForApprovalCondition.toString();
	}

	private String getApprovedProjectsCondition() {
		StringBuilder approvedCondition = new StringBuilder();
		approvedCondition.append(" AND p.active= 'true' AND p.is_draft_project = 'false' AND t.is_active = 'Y' \n")
				.append(" AND (etm.active = 1 OR (etm.active = 0 AND DATE(etm.start_date) > CURDATE())) \n ");
		return approvedCondition.toString();
	}

	private String getRejectedProjectsCondition() {
		StringBuilder rejectedCondition = new StringBuilder();
		rejectedCondition.append(" AND p.active= 'true' AND UPPER(p.is_draft_project) = 'REJECTED' \n");
		return rejectedCondition.toString();
	}
	
	private String getOffBoardedProjectsCondition() {
		StringBuilder offBoardedCondition = new StringBuilder();
		offBoardedCondition.append(" AND p.active= 'true' AND (p.is_draft_project IS NOT NULL OR UPPER(p.is_draft_project) != 'REJECTED') \n")
		.append(" AND EXISTS (SELECT 1 FROM teams t3 WHERE t3.project_id = p.project_id AND t3.is_active  = 'Y' ) \n")
		// .append(" AND p.project_id IN (SELECT t2.project_id FROM teams t2 INNER JOIN employee_team_mapping etm2 ON t2.team_id = etm2.team_id WHERE (etm2.active = 0 AND DATE(etm2.end_date) < CURDATE()))  \n")
		.append(" AND p.project_id NOT IN (SELECT t2.project_id FROM teams t2 LEFT JOIN employee_team_mapping etm2 ON (t2.team_id = etm2.team_id OR etm2.team_id IS NULL) WHERE 1 = 1 AND (etm2.active != 0 OR (etm2.active = 0 AND DATE(etm2.start_date) > CURDATE()))) \n");
		return offBoardedCondition.toString();
	}

	private String getScheduledProjectsCondition() {
		StringBuilder offBoardedCondition = new StringBuilder();
		offBoardedCondition.append(" AND p.active= 'true' AND t.is_active = 'Y' AND p.is_draft_project = 'false' \n")
		.append("AND etm.active = 0 AND DATE(etm.start_date) > CURDATE() \n ")
		.append(" AND NOT EXISTS ( SELECT 1 FROM teams t2 JOIN employee_team_mapping etm2 ON t2.team_id = etm2.team_id WHERE t2.project_id = p.project_id AND etm2.active != 0) \n");
		return offBoardedCondition.toString();
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
                "expiredProjects6To12Months", List.of(currentDate.minusDays(365), currentDate.minusDays(181)),
                "expiredProjects9To12Months", List.of(currentDate.minusDays(365), currentDate.minusDays(271)),
                "expiredProjectsAbove12Months", List.of(currentDate.minusYears(10), currentDate.minusDays(366)));
        return dateRanges.getOrDefault(key, List.of());
    }

    public String getSortBy(String sortColumn, boolean defaultFlag) {
        switch (sortColumn) {
            case "name":
                return "project_name";
            case "poNo":
                return "po_no";
            case "poProjectType":
                return "po_project_type";
            case "projectManagerName":
                return "project_Manager";
            case "clientName":
                return "client_name";
            case "apmosysRM":
                return "apmosys_rm";
            case "clientRM":
                return "client_rm";
            case "projectStartDate":
                return "start_date";
            case "projectEndDate":
                return "end_date";
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

    private String getDBProjectStatus(String projectStatus) {
        switch (projectStatus) {
            case "COMPLETED_IN_ISHINE":
            case "COMPLETED_IN_SHANKH":
            case "COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE":
                return "Completed";
            default:
                break;
        }
        return null;
    }

    private void appenCustomSearchToQuery(Map<String, String> projectFilter, StringBuilder query) {
        if (projectFilter != null && !projectFilter.isEmpty()) {
            query.append(" WHERE 1=1 ");
            for (Map.Entry<String, String> entry : projectFilter.entrySet()) {
                String column = getSortBy(entry.getKey(), false);
                String value = entry.getValue();
                if (column != null && !column.trim().isEmpty() && value != null && !value.trim().isEmpty()) {
                    if(column.equals("poNo") || column.equals("po_no")){
                        continue;
                    }
                    query.append(String.format(" AND LOWER( %s ) LIKE '%%%s%%' \n", column,
                            value.replace("'", "''").toLowerCase()));
                }
            }
            query.append(" ");
        }
    }

}
