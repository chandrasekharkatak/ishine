package com.apmosys.employeeportal.customrepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import com.apmosys.employeeportal.exception.BadRequestException;
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

    private static final StringBuilder projectDetailsStartQueryWithRequiredAndAllocatedCount = new StringBuilder()
            .append(" WITH REQUIREMENT_COUNT AS ( \n")
            .append(" 	SELECT p.project_id, coalesce(sum(prm.count), 0) required_resources \n")
            .append(" 	FROM projects p \n")
            .append("  LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id AND DATE(ppd.po_start_date) <= CURDATE() AND (ppd.po_end_date IS NULL OR DATE(ppd.po_end_date) >= CURDATE()) AND ppd.active = 1 \n")
            .append("  LEFT JOIN po_requirement_mapping prm ON prm.po_id = ppd.po_id  \n")
            .append(" 	WHERE 1=1  \n")
            .append("  GROUP BY p.project_id \n")
            .append(" ) , \n")
            .append(" ALLOCATED_COUNT AS ( \n")
            .append(" 	SELECT p.project_id, coalesce(count(distinct e.emp_id), 0) as active_resources \n")
            .append(" 	FROM projects p \n")
            .append("  LEFT JOIN teams t ON t.project_id = p.project_id and p.active= 'true' and t.is_active = 'Y' \n")
            .append("  LEFT JOIN employee_team_mapping etm ON etm.team_id = t.team_id and etm.active = 1 \n")
            .append("  LEFT JOIN employee e ON e.emp_id = etm.emp_id and upper(e.employmentstatus) != 'INACTIVE' \n")
            .append(" 	where 1=1 \n")
            .append("  GROUP BY p.project_id\n")
            .append(" ), \n")
            .append(" PROJECT_WISE_ACTIVE_AND_ALLOCATED_COUNT AS ( \n")
            .append(" select rc.project_id, rc.required_resources, ac.active_resources \n")
            .append(" FROM REQUIREMENT_COUNT rc \n")
            .append(" LEFT JOIN ALLOCATED_COUNT ac ON rc.project_id = ac.project_id )  \n");

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
            .append(",p.created_on, p.updated_on, p.project_completion_date, pwc.required_resources, pwc.active_resources \n")
            .append(" FROM projects p \n")
            .append(" LEFT JOIN PROJECT_WISE_ACTIVE_AND_ALLOCATED_COUNT pwc ON pwc.project_id = p.project_id \n")
            ;

    private static final StringBuilder projectDetailsGroupQuery = new StringBuilder()
            .append(" \n GROUP BY \n")
            .append(" p.project_id, p.project_name, p.internal_project_type \n")
            .append(" ,p.po_project_id, p.start_date, p.end_date \n")
            .append(" ,p.client_id,c.client_name \n")
            .append(" ,p.active, p.status , p.project_status, p.sync_project, p.created_by, p.updated_by, approval_status \n")
            .append(" ,project_overview_id, p.created_on, p.updated_on, p.project_completion_date, pwc.required_resources, pwc.active_resources \n");

    private void appendLinkSearchPrimaryProjectIdFilter(RMGDashboardProjectRequest req, StringBuilder... builders) {
        if (req == null) {
            return;
        }
        List<Integer> ids = req.getLinkSearchPrimaryProjectIds();
        String like = req.getLinkSearchNameLikeParameter();
        boolean hasIds = ids != null && !ids.isEmpty();
        boolean hasLike = like != null && !like.isEmpty();
        if (!hasIds && !hasLike) {
            return;
        }
        String frag;
        if (hasIds && hasLike) {
            frag = " AND ( p.project_id IN (:linkSearchPrimaryProjectIds) OR LOWER(p.project_name) LIKE :linkSearchNameLike ) \n";
        } else if (hasIds) {
            frag = " AND p.project_id IN (:linkSearchPrimaryProjectIds) \n";
        } else {
            frag = " AND LOWER(p.project_name) LIKE :linkSearchNameLike \n";
        }
        for (StringBuilder b : builders) {
            if (b != null) {
                b.append(frag);
            }
        }
    }

    public Slice<ProjectFetchDTO> handleProjectsByType(RMGDashboardProjectRequest rmgDashboardProjectRequest,
            List<Long> deptIds, String projectStatus, List<String> projectNames, String sortBy, String sortDirection,
            Pageable page) {

        // String query = getQuery(rmgDashboardProjectRequest, sortBy, sortDirection,
        //         projectStatus, false, projectNames);
        String projectType = getProjectType(projectStatus);
        String query = getAllProjectsQueryByProjectType(rmgDashboardProjectRequest, sortBy, sortDirection,
                projectNames, projectType);
        
        System.err.println(query);

        CompletableFuture<List<ProjectFetchDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page,
                        projectStatus, "", "", projectNames, null, "", false, false,
                        rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter(), null));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                                false, rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter(), null));
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

        String query = getAllProjectsQuery(rmgDashboardProjectRequest, sortBy, sortDirection,
                projectNames);
        
        System.err.println(query.toString());

        CompletableFuture<List<ProjectFetchDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page, projectStatus, "", "",
                        projectNames, null, "", false, false,
                        rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter() ,null));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                                false, rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter(), null));
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

        String query2 = "";
        if (rmgDashboardProjectRequest.getFixedCostFilter() != null
                && "all".equals(rmgDashboardProjectRequest.getFixedCostFilter())) {
            query2 = getAllProjectsQueryByProjectType(rmgDashboardProjectRequest, sortBy, sortDirection,
                    projectNames, "Fixed Cost");
        } else {
            query2 = getFCProjectQuery(rmgDashboardProjectRequest, sortBy, sortDirection,
                    rmgDashboardProjectRequest.getFixedCostFilter(), projectNames);
        }
        String query = query2;

        System.err.println(query);
        CompletableFuture<List<ProjectFetchDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page, projectStatus, "", "",
                        projectNames, null, "", false, false,
                        rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter(),rmgDashboardProjectRequest.getFixedCostFilter()));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                                false, rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter(), rmgDashboardProjectRequest.getFixedCostFilter()));
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

    public Slice<ProjectFetchDTO> handleTimesheetApplicableProjects(
            RMGDashboardProjectRequest rmgDashboardProjectRequest, List<Long> deptIds, Set<Integer> projectIds,
            String projectStatus, List<String> projectNames, String sortBy, String sortDirection, Pageable page) {

        String query = getTimesheetApplicableProjectQuery(rmgDashboardProjectRequest, sortBy, sortDirection,
                rmgDashboardProjectRequest.getTimesheetApplicableProjectTypeFilter(), projectNames);

        System.err.println(query);

        CompletableFuture<List<ProjectFetchDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page, projectStatus, "", "",
                        projectNames, null, "", false, false,
                        rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter(),null));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(() -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                        false, rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter(), null));
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

    private String getTimesheetApplicableProjectQuery(RMGDashboardProjectRequest rmgReq,
            String sortBy, String sortDirection, String timesheetApplicableProjectTypeFilter,
            List<String> projectNames) {
        Map<String, String> projectFilter = rmgReq != null ? rmgReq.getProjectFilter() : null;
        StringBuilder query = new StringBuilder(projectDetailsStartQuery);
        StringBuilder groupQuery = new StringBuilder(projectDetailsGroupQuery).append(" ) as T1");

        query.append(" INNER JOIN po_department_mapping pdm on pdm.project_id = p.project_id AND pdm.dept_id IN :deptIds \n")
                .append(" INNER JOIN teams t ON p.project_id = t.project_id AND t.is_active = 'Y' \n")
                .append(" INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id AND etm.active = 1 \n")
                .append(" LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id AND ppd.active  = 1 \n")
                .append(" LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id AND pm.active = 1 \n")
                .append(" LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n")
                .append(" LEFT JOIN department d ON pdm.dept_id = d.dept_id \n")
                .append(" LEFT JOIN clients c ON p.client_id = c.client_id \n")
                .append(" LEFT JOIN client_locations cl ON p.client_id = cl.client_id and lower(cl.client_location) != 'wfh' \n")
                .append(" WHERE 1=1 \n");
        appendLinkSearchPrimaryProjectIdFilter(rmgReq, query);
        query.append(" AND p.active = 'true' \n");

		Set<String> timesheetApplicableProjectTypes = Set.of("tnm", "monitoring");

        if (timesheetApplicableProjectTypeFilter != null) {
            if (timesheetApplicableProjectTypeFilter.equals("internal")) {
                query.append(" AND p.po_project_type IS NULL AND (LOWER(p.internal_project_type) = 'internalrndproducts' OR (p.internal_project_type) = 'internal' OR LOWER(p.internal_project_type) = 'bench') \n");
            }else if(timesheetApplicableProjectTypeFilter.equals("fixedCost")) {
                query.append(" AND p.po_project_type IS NOT NULL AND LOWER(p.po_project_type) = 'fixed cost' \n");
            }
            else if (timesheetApplicableProjectTypes.contains(timesheetApplicableProjectTypeFilter)) {
                query.append(" AND p.po_project_type IS NOT NULL AND LOWER(p.po_project_type) = '")
                        .append(timesheetApplicableProjectTypeFilter).append("' \n");
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

    public Slice<ProjectFetchDTO> handleOverboardedAndUnderboardedProjects(RMGDashboardProjectRequest rmgDashboardProjectRequest,
            List<Long> deptIds, String projectStatus, List<String> projectNames, String sortBy, String sortDirection,
            Pageable page) {

        String query = getOverboardedAndUnderboardedProjectQuery(rmgDashboardProjectRequest, sortBy,
                sortDirection, projectNames, projectStatus);
        
        System.err.println(query);

        CompletableFuture<List<ProjectFetchDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page, projectStatus, "", "",
                        projectNames, null, "", false, false,
                        rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter(),null));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                                false, rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter(), null));
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

        String query = getExpiredTNMQuery(rmgDashboardProjectRequest, sortBy,
                sortDirection, projectStatus, addStartAndEndDate, projectNames);
        
        System.err.println(query);
        final String sDate = startDate;
        final String eDate = endDate;
        final boolean faddStartAndEndDate = addStartAndEndDate;
        CompletableFuture<List<ProjectFetchDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page, projectStatus, sDate,
                        eDate, projectNames, null, "", false, faddStartAndEndDate,
                        rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter(),null));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(() -> getResultCount(query, deptIds, projectStatus, sDate, eDate, projectNames, null, "",
                        false, faddStartAndEndDate, rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter(), null));
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
        
        System.err.println(query);

        CompletableFuture<Long> countFuture;
        CompletableFuture<List<ProjectFetchDTO>> listFuture;

        listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(query, sortBy, sortDirection, deptIds, page, projectStatus, "", "",
                        projectNames, projectIds, dbProjectStatus, isProjectId, false,
                        req.getLinkSearchPrimaryProjectIds(), req.getLinkSearchNameLikeParameter(),null));
        countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, projectIds,
                                dbProjectStatus, isProjectId, false, req.getLinkSearchPrimaryProjectIds(), req.getLinkSearchNameLikeParameter(), null));
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

        String query = getAllProjectsQuery(rmgDashboardProjectRequest, sortBy, sortDirection,
                projectNames);

        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                                false, rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter(), null));
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

        String query2 = "";
        if (rmgDashboardProjectRequest.getFixedCostFilter() != null
                && "all".equals(rmgDashboardProjectRequest.getFixedCostFilter())) {
            query2 = getAllProjectsQueryByProjectType(rmgDashboardProjectRequest, sortBy, sortDirection,
                    projectNames, "Fixed Cost");
        } else {
            query2 = getFCProjectQuery(rmgDashboardProjectRequest, sortBy, sortDirection,
                    rmgDashboardProjectRequest.getFixedCostFilter(), projectNames);
        }
        String query = query2;

        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                                false, rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter(), rmgDashboardProjectRequest.getFixedCostFilter()));
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
                                dbProjectStatus, isProjectId, false, req.getLinkSearchPrimaryProjectIds(), req.getLinkSearchNameLikeParameter(), null));
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

        String query = getOverboardedAndUnderboardedProjectQuery(rmgDashboardProjectRequest, sortBy,
                sortDirection, projectNames, projectStatus);

        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                                false, rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter(), null));
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

        // String query = getQuery(rmgDashboardProjectRequest, sortBy, sortDirection,
        //         projectStatus, false, projectNames);

        String projectType = getProjectType(projectStatus);
        String query = getAllProjectsQueryByProjectType(rmgDashboardProjectRequest, sortBy, sortDirection,
                projectNames, projectType);
                
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(
                        () -> getResultCount(query, deptIds, projectStatus, "", "", projectNames, null, "", false,
                                false, rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter(), null));
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

        String query = getExpiredTNMQuery(rmgDashboardProjectRequest, sortBy,
                sortDirection, projectStatus, addStartAndEndDate, projectNames);

        final String sDate = startDate;
        final String eDate = endDate;
        final boolean faddStartAndEndDate = addStartAndEndDate;

        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(() -> getResultCount(query, deptIds, projectStatus, sDate, eDate, projectNames, null, "",
                        false, faddStartAndEndDate, rmgDashboardProjectRequest.getLinkSearchPrimaryProjectIds(),
                        rmgDashboardProjectRequest.getLinkSearchNameLikeParameter(), null));
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
            Set<Integer> projectIds, String dbProjectStatus, boolean isProjectId, boolean addStartAndEndDate,
            List<Integer> linkSearchPrimaryProjectIds, String linkSearchNameLikeParameter, String filterType) {

        if ("TOTAL_EXPIRED_TNM".equalsIgnoreCase(projectStatus)
                || ("TOTAL_FC".equalsIgnoreCase(projectStatus) && filterType != null
                        && (filterType.equals("ontime") || filterType.equals("defaulter")))) {
            query = " SELECT * FROM (" + query + " ) AS T7";
        } else {
            query = (projectDetailsStartQueryWithRequiredAndAllocatedCount + " SELECT * FROM " + query);
        }
        
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
            if (linkSearchPrimaryProjectIds != null && !linkSearchPrimaryProjectIds.isEmpty()) {
                nativeQuery.setParameter("linkSearchPrimaryProjectIds", linkSearchPrimaryProjectIds);
            }
            if (linkSearchNameLikeParameter != null && !linkSearchNameLikeParameter.isEmpty()) {
                nativeQuery.setParameter("linkSearchNameLike", linkSearchNameLikeParameter);
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
            boolean isProjectId, boolean addStartAndEndDate, List<Integer> linkSearchPrimaryProjectIds,
            String linkSearchNameLikeParameter, String filterType) {
                
        if ("TOTAL_EXPIRED_TNM".equalsIgnoreCase(projectStatus)
                || ("TOTAL_FC".equalsIgnoreCase(projectStatus) && filterType != null
                        && (filterType.equals("ontime") || filterType.equals("defaulter")))) {
            query = " SELECT count(*) FROM (" + query + " ) AS T7";
        } else {
            query = " SELECT count(*) FROM " + query;
            query = query.replace(
                    " LEFT JOIN PROJECT_WISE_ACTIVE_AND_ALLOCATED_COUNT pwc ON pwc.project_id = p.project_id \n", "")
                    .replace(", pwc.required_resources, pwc.active_resources", "");
        }

        if (query.contains("ORDER BY active_resources") || query.contains("ORDER BY required_resources")) {
            query = query.replace("ORDER BY active_resources asc", "")
                    .replace("ORDER BY active_resources desc", "")
                    .replace("ORDER BY required_resources asc", "")
                    .replace("ORDER BY required_resources desc", "");
        }

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
            if (linkSearchPrimaryProjectIds != null && !linkSearchPrimaryProjectIds.isEmpty()) {
                nativeQuery.setParameter("linkSearchPrimaryProjectIds", linkSearchPrimaryProjectIds);
            }
            if (linkSearchNameLikeParameter != null && !linkSearchNameLikeParameter.isEmpty()) {
                nativeQuery.setParameter("linkSearchNameLike", linkSearchNameLikeParameter);
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
            return getGeneralProjectFilterQuery(req, sortBy, sortDirection, projectStatus,
                    projectNames);
        }
        return getAllProjectsQuery(req, sortBy, sortDirection, projectNames);
    }

    private String getExpiredTNMQuery(RMGDashboardProjectRequest rmgReq, String sortBy,
            String sortDirection, String projectStatus, boolean addStartAndEndDate, List<String> projectNames) {
        Map<String, String> projectFilter = rmgReq != null ? rmgReq.getProjectFilter() : null;

        StringBuilder projectDetailsStartQueryWithRequiredAndAllocatedCountExpiredTNM = new StringBuilder()
                .append(" WITH REQUIREMENT_COUNT AS ( \n")
                .append(" 	SELECT p.project_id, coalesce(sum(prm.count), 0) required_resources \n")
                .append(" 	FROM projects p \n")
                .append("  LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id AND CURDATE() BETWEEN DATE(ppd.po_start_date) AND COALESCE(DATE(ppd.po_end_date), '9999-12-31') AND ppd.active = 1 \n")
                .append("  LEFT JOIN po_requirement_mapping prm ON prm.po_id = ppd.po_id  \n")
                .append(" 	WHERE 1=1  \n")
                .append("  GROUP BY p.project_id \n")
                .append(" ) , \n")
                .append(" ALLOCATED_COUNT AS ( \n")
                .append(" 	SELECT p.project_id, coalesce(count(distinct e.emp_id), 0) as active_resources \n")
                .append(" 	FROM projects p \n")
                .append("  LEFT JOIN teams t ON t.project_id = p.project_id and p.active= 'true' and t.is_active = 'Y' \n")
                .append("  LEFT JOIN employee_team_mapping etm ON etm.team_id = t.team_id and etm.active = 1 \n")
                .append("  LEFT JOIN employee e ON e.emp_id = etm.emp_id and upper(e.employmentstatus) != 'INACTIVE' \n")
                .append(" 	where 1=1 \n")
                .append("  GROUP BY p.project_id\n")
                .append(" ), \n")
                .append(" PROJECT_WISE_ACTIVE_AND_ALLOCATED_COUNT AS ( \n")
                .append(" select rc.project_id, rc.required_resources, ac.active_resources \n")
                .append(" FROM REQUIREMENT_COUNT rc \n")
                .append(" LEFT JOIN ALLOCATED_COUNT ac ON rc.project_id = ac.project_id )  \n")
                .append(" SELECT * FROM \n");

        StringBuilder query = new StringBuilder();
        query.append(projectDetailsStartQueryWithRequiredAndAllocatedCountExpiredTNM);
        query.append(projectDetailsStartQuery);
        StringBuilder groupQuery = new StringBuilder(projectDetailsGroupQuery).append(" ) as T1");

        query.append(" INNER JOIN teams t ON p.project_id = t.project_id AND t.is_active = 'Y' \n")
                .append(" INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id AND etm.active = 1 \n")
                .append(" LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id AND ppd.active = 1  \n")
                .append(" LEFT JOIN po_department_mapping pdm on ((ppd.po_id IS NOT NULL AND pdm.po_id = ppd.po_id) OR (pdm.project_id = p.project_id)) AND pdm.dept_id IN :deptIds  \n")
                .append(" LEFT JOIN department d ON pdm.dept_id = d.dept_id \n")
                .append(" LEFT JOIN clients c ON p.client_id = c.client_id \n")
                .append(" LEFT JOIN client_locations cl ON p.client_id = cl.client_id and lower(cl.client_location) != 'wfh' \n")
                .append(" LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id AND pm.active = 1  \n")
                .append(" LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n")
                .append(" WHERE 1=1 \n");
        appendLinkSearchPrimaryProjectIdFilter(rmgReq, query);

        query.append(" AND p.active != 'false' \n")
                .append(" AND (CASE WHEN p.po_project_type IS NOT NULL AND TRIM(p.po_project_type) != '' THEN p.po_project_type ELSE p.internal_project_type END) = 'TNM' \n")
                .append(" AND COALESCE(pwc.active_resources, 0) > 0 \n")
                .append(" AND EXISTS (SELECT 1 FROM project_po_details pex WHERE pex.project_id = p.project_id AND DATE(pex.po_end_date) < CURDATE()) \n")
                .append(" AND NOT EXISTS (SELECT 1 FROM project_po_details pac WHERE pac.project_id = p.project_id AND pac.active = 1 AND CURDATE() BETWEEN DATE(pac.po_start_date) AND COALESCE(DATE(pac.po_end_date), '9999-12-31')) \n");
        if (addStartAndEndDate) {
            query.append("  AND DATE(ppd.po_end_date) between :startDate and :endDate \n");
        }
        query.append(groupQuery);
        appenCustomSearchToQuery(projectFilter, query);
        query.append(String.format(" ORDER BY %s %s ", sortBy, sortDirection));
        return query.toString();
    }

    private String getAllProjectsQuery(RMGDashboardProjectRequest rmgReq, String sortBy,
			String sortDirection, List<String> projectNames) {
		Map<String, String> projectFilter = rmgReq != null ? rmgReq.getProjectFilter() : null;
		StringBuilder query = new StringBuilder(" ( " + projectDetailsStartQuery); // Approved
		StringBuilder query1 = new StringBuilder(projectDetailsStartQuery); // Pending for Approval
		StringBuilder query2 = new StringBuilder(projectDetailsStartQuery); // Not Started
		StringBuilder query3 = new StringBuilder(projectDetailsStartQuery); // Rejected
		StringBuilder query4 = new StringBuilder(projectDetailsStartQuery); // OffBoarded
//		StringBuilder query5 = new StringBuilder(projectDetailsStartQuery); // Scheduled
		StringBuilder groupQuery = new StringBuilder(projectDetailsGroupQuery).append(" )");

		StringBuilder queryJoins = new StringBuilder();
		queryJoins.append(" LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id AND DATE(ppd.po_start_date) <= CURRENT_DATE AND (ppd.po_end_date IS NULL OR DATE(ppd.po_end_date) >= CURRENT_DATE) AND ppd.active  = 1 \n")
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
        
        StringBuilder queryJoins2 = new StringBuilder();
        queryJoins2.append(" INNER JOIN po_department_mapping pdm on pdm.project_id = p.project_id AND (pdm.dept_id IN :deptIds)  \n")
                .append(" LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id \n")
                .append(" LEFT JOIN clients c ON c.client_id = p.client_id  \n")
                .append(" LEFT JOIN client_locations cl ON p.client_id = cl.client_id and lower(cl.client_location) != 'wfh' \n")
                .append(" LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id AND pm.active = 1  \n")
                .append(" LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n");
		query2.append(queryJoins2)
		      .append(" WHERE 1=1 \n")
              .append(" AND ((pdm.po_id IS NULL AND pdm.project_id = p.project_id) OR (pdm.po_id IS NOT NULL AND pdm.po_id = ppd.po_id AND DATE(ppd.po_start_date) <= CURDATE() AND DATE(ppd.po_end_date) >= CURDATE() AND ppd.active = 1)) \n")
		      .append(getNotStartedProjectsCondition());

		query3.append(queryJoins)
			  .append(" WHERE 1=1 \n")
			  .append(getRejectedProjectsCondition());
		
		query4.append(queryJoins)
			  .append(" WHERE 1=1 \n")
			  .append(getOffBoardedProjectsCondition());

		appendLinkSearchPrimaryProjectIdFilter(rmgReq, query, query1, query2, query3, query4);
		
		
//		query5.append(" INNER JOIN teams t ON p.project_id = t.project_id  \n")
//			  .append(" INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id  \n")
//			  .append(queryJoins)
//			  .append(" WHERE 1=1 \n")
//		      .append(getScheduledProjectsCondition());
		
		if (projectNames != null && !projectNames.isEmpty()) {
			query.append(" AND p.project_name IN (:projectNames) \n");
			query1.append(" AND p.project_name IN (:projectNames) \n");
			query2.append(" AND p.project_name IN (:projectNames) \n");
			query3.append(" AND p.project_name IN (:projectNames) \n");
			query4.append(" AND p.project_name IN (:projectNames) \n");
//			query5.append(" AND p.project_name IN (:projectNames) \n");
		}
		query.append(groupQuery);
		query1.append(groupQuery);
		query2.append(groupQuery);
		query3.append(groupQuery);
		query4.append(groupQuery);
//		query5.append(groupQuery);

		query.append(" UNION ALL \n")
			 .append(query1)
			 .append(" UNION ALL \n")
			 .append(query2)
			 .append(" UNION ALL \n")
			 .append(query3)
			 .append(" UNION ALL \n")
			 .append(query4)
//			 .append(" UNION ALL \n")
//			 .append(query5)
			 .append(" ) as T1");

		appenCustomSearchToQuery(projectFilter, query);
		query.append(String.format(" ORDER BY %s %s ", sortBy, sortDirection));
		return query.toString();
	}

    private String getAllProjectsQueryByProjectType(RMGDashboardProjectRequest rmgReq, String sortBy,
            String sortDirection, List<String> projectNames, String projectType) {
        Map<String, String> projectFilter = rmgReq != null ? rmgReq.getProjectFilter() : null;
        StringBuilder query = new StringBuilder(" ( " + projectDetailsStartQuery); // Approved
        StringBuilder query1 = new StringBuilder(projectDetailsStartQuery); // Pending for Approval
        StringBuilder query2 = new StringBuilder(projectDetailsStartQuery); // Not Started
        StringBuilder query3 = new StringBuilder(projectDetailsStartQuery); // Rejected
        StringBuilder query4 = new StringBuilder(projectDetailsStartQuery); // OffBoarded
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
                .append(" INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id  \n");

        StringBuilder queryJoins2 = new StringBuilder();
        queryJoins2.append(
                " INNER JOIN po_department_mapping pdm on pdm.project_id = p.project_id AND (pdm.dept_id IN :deptIds)  \n")
                .append(" LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id \n")
                .append(" LEFT JOIN clients c ON c.client_id = p.client_id  \n")
                .append(" LEFT JOIN client_locations cl ON p.client_id = cl.client_id and lower(cl.client_location) != 'wfh' \n")
                .append(" LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id AND pm.active = 1  \n")
                .append(" LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n");

        query.append(queryJoins)
                .append(" WHERE 1=1 \n");
        query1.append(queryJoins)
                .append(" WHERE 1=1 \n");

        query2.append(queryJoins2)
                .append(" WHERE 1=1 \n")
                .append(" AND ((pdm.po_id IS NULL AND pdm.project_id = p.project_id) OR (pdm.po_id IS NOT NULL AND pdm.po_id = ppd.po_id AND DATE(ppd.po_start_date) <= CURDATE() AND DATE(ppd.po_end_date) >= CURDATE() AND ppd.active = 1)) \n");

        query3.append(queryJoins)
                .append(" WHERE 1=1 \n");

        query4.append(queryJoins)
                .append(" WHERE 1=1 \n");

        if ("INTERNAL".equalsIgnoreCase(projectType)) {
            query.append(
                    " AND p.po_project_type IS NULL AND p.internal_project_type is not null AND TRIM(p.internal_project_type) != '' \n");
            query1.append(
                    " AND p.po_project_type IS NULL AND p.internal_project_type is not null AND TRIM(p.internal_project_type) != '' \n");
            query2.append(
                    " AND p.po_project_type IS NULL AND p.internal_project_type is not null AND TRIM(p.internal_project_type) != '' \n");
            query3.append(" AND UPPER(p.po_project_type) = ").append("'").append(projectType).append("'");
            query4.append(
                    " AND p.po_project_type IS NULL AND p.internal_project_type is not null AND TRIM(p.internal_project_type) != '' \n");
        } else {
            query.append(" AND UPPER(p.po_project_type) = ").append("'").append(projectType).append("'");
            query1.append(" AND UPPER(p.po_project_type) = ").append("'").append(projectType).append("'");
            query2.append(" AND UPPER(p.po_project_type) = ").append("'").append(projectType).append("'");
            query3.append(" AND UPPER(p.po_project_type) = ").append("'").append(projectType).append("'");
            query4.append(" AND UPPER(p.po_project_type) = ").append("'").append(projectType).append("'");
        }

        query.append(getApprovedProjectsCondition());
        query1.append(getPendingForApprovalProjectsCondition());
        query2.append(getNotStartedProjectsCondition());
        query3.append(getRejectedProjectsCondition());
        query4.append(getOffBoardedProjectsCondition());
        appendLinkSearchPrimaryProjectIdFilter(rmgReq, query, query1, query2, query3, query4);

        if (projectNames != null && !projectNames.isEmpty()) {
            query.append(" AND p.project_name IN (:projectNames) \n");
            query1.append(" AND p.project_name IN (:projectNames) \n");
            query2.append(" AND p.project_name IN (:projectNames) \n");
            query3.append(" AND p.project_name IN (:projectNames) \n");
            query4.append(" AND p.project_name IN (:projectNames) \n");
        }
        query.append(groupQuery);
        query1.append(groupQuery);
        query2.append(groupQuery);
        query3.append(groupQuery);
        query4.append(groupQuery);

        query.append(" UNION ALL \n")
                .append(query1)
                .append(" UNION ALL \n")
                .append(query2)
                .append(" UNION ALL \n")
                .append(query3)
                .append(" UNION ALL \n")
                .append(query4)
                .append(" ) as T1");

        appenCustomSearchToQuery(projectFilter, query);
        query.append(String.format(" ORDER BY %s %s ", sortBy, sortDirection));
        return query.toString();
    }

    // private String getApprovedAndNotStartedProjectsQuery(RMGDashboardProjectRequest rmgReq, String sortBy,
    //         String sortDirection, List<String> projectNames, String projectType) {
    //     Map<String, String> projectFilter = rmgReq != null ? rmgReq.getProjectFilter() : null;

    //     StringBuilder query = new StringBuilder(" ( " + projectDetailsStartQuery); // Approved
    //     StringBuilder query1 = new StringBuilder(projectDetailsStartQuery); // Not Started
    //     StringBuilder query2 = new StringBuilder(projectDetailsStartQuery); // OffBoarded
    //     StringBuilder groupQuery = new StringBuilder(projectDetailsGroupQuery).append(" )");

    //     StringBuilder queryJoins = new StringBuilder();
    //     queryJoins.append(" LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id AND DATE(ppd.po_start_date) <= CURRENT_DATE AND (ppd.po_end_date IS NULL OR DATE(ppd.po_end_date) >= CURRENT_DATE) AND ppd.active  = 1 \n")
    //             .append(" INNER JOIN po_department_mapping pdm on ((ppd.po_id IS NOT NULL AND pdm.po_id = ppd.po_id) OR (ppd.po_id IS NULL AND pdm.project_id = p.project_id)) AND (pdm.dept_id IN :deptIds)  \n")
    //             .append(" LEFT JOIN clients c ON c.client_id = p.client_id  \n")
    //             .append(" LEFT JOIN client_locations cl ON p.client_id = cl.client_id and lower(cl.client_location) != 'wfh' \n")
    //             .append(" LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id AND pm.active = 1  \n")
    //             .append(" LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n");

    //     query.append(" INNER JOIN teams t ON p.project_id = t.project_id  \n")
    //             .append(" INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id  \n")
    //             .append(queryJoins)
    //             .append(" WHERE 1=1 \n");

    //     StringBuilder queryJoins1 = new StringBuilder();
    //     queryJoins1.append(" INNER JOIN po_department_mapping pdm on pdm.project_id = p.project_id AND (pdm.dept_id IN :deptIds)  \n")
    //             .append(" LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id \n")
    //             .append(" LEFT JOIN clients c ON c.client_id = p.client_id  \n")
    //             .append(" LEFT JOIN client_locations cl ON p.client_id = cl.client_id and lower(cl.client_location) != 'wfh' \n")
    //             .append(" LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id AND pm.active = 1  \n")
    //             .append(" LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n");

    //     query1.append(queryJoins1)
    //             .append(" WHERE 1=1 \n")
    //             .append(" AND ((pdm.po_id IS NULL AND pdm.project_id = p.project_id) OR (pdm.po_id IS NOT NULL AND pdm.po_id = ppd.po_id AND DATE(ppd.po_start_date) <= CURDATE() AND DATE(ppd.po_end_date) >= CURDATE() AND ppd.active = 1)) \n");

    //     query2.append(queryJoins)
    //             .append(" WHERE 1=1 \n");

    //     if ("INTERNAL".equalsIgnoreCase(projectType)) {
    //         query.append(" AND p.po_project_type IS NULL AND p.internal_project_type is not null AND TRIM(p.internal_project_type) != '' \n");
    //         query1.append(" AND p.po_project_type IS NULL AND p.internal_project_type is not null AND TRIM(p.internal_project_type) != '' \n");
    //         query2.append(" AND p.po_project_type IS NULL AND p.internal_project_type is not null AND TRIM(p.internal_project_type) != '' \n");
    //     } else {
    //         query.append(" AND UPPER(p.po_project_type) = ").append("'").append(projectType).append("'");
    //         query1.append(" AND UPPER(p.po_project_type) = ").append("'").append(projectType).append("'");
    //         query2.append(" AND UPPER(p.po_project_type) = ").append("'").append(projectType).append("'");
    //     }

    //     query.append(getApprovedProjectsCondition());
    //     query1.append(getNotStartedProjectsCondition());
    //     query2.append(getOffBoardedProjectsCondition());
    //     appendLinkSearchPrimaryProjectIdFilter(rmgReq, query, query1, query2);

    //     if (projectNames != null && !projectNames.isEmpty()) {
    //         query.append(" AND p.project_name IN (:projectNames) \n");
    //         query1.append(" AND p.project_name IN (:projectNames) \n");
    //         query2.append(" AND p.project_name IN (:projectNames) \n");
    //     }
    //     query.append(groupQuery);
    //     query1.append(groupQuery);
    //     query2.append(groupQuery);

    //     query.append(" UNION ALL \n")
    //             .append(query1)
    //             .append(" UNION ALL \n")
    //             .append(query2)
    //             .append(" ) as T1");

    //     appenCustomSearchToQuery(projectFilter, query);
    //     query.append(String.format(" ORDER BY %s %s ", sortBy, sortDirection));
    //     return query.toString();
    // }

    private String getOverboardedAndUnderboardedProjectQuery(RMGDashboardProjectRequest rmgReq, String sortBy,
			String sortDirection, List<String> projectNames, String projectStatus) {
		Map<String, String> projectFilter = rmgReq != null ? rmgReq.getProjectFilter() : null;

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
				.append(" WHERE 1=1 \n");
		appendLinkSearchPrimaryProjectIdFilter(rmgReq, query);
		query.append(" AND p.po_project_type = 'TNM' AND p.active != 'false' AND t.is_active != 'N' \n")
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
				.append("AND etm2.active = 1 AND e3.employmentstatus != 'InActive' \n")
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

    private String getGeneralProjectFilterQuery(RMGDashboardProjectRequest rmgReq, String sortBy,
            String sortDirection, String projectStatus, List<String> projectNames) {
        Map<String, String> projectFilter = rmgReq != null ? rmgReq.getProjectFilter() : null;
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
		} else if (projectStatus.equalsIgnoreCase("NOT_STARTED")) {
            query.append(" INNER JOIN po_department_mapping pdm on pdm.project_id = p.project_id AND pdm.dept_id IN :deptIds \n")
                    .append(" LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id \n")
                    .append(" LEFT JOIN department d ON pdm.dept_id = d.dept_id \n")
                    .append(" LEFT JOIN clients c ON c.client_id = p.client_id \n")
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
        appendLinkSearchPrimaryProjectIdFilter(rmgReq, query);

		if (projectStatus.equalsIgnoreCase("NOT_STARTED")) {
            query.append(" AND ((pdm.po_id IS NULL AND pdm.project_id = p.project_id) OR (pdm.po_id IS NOT NULL AND pdm.po_id = ppd.po_id AND DATE(ppd.po_start_date) <= CURDATE() AND DATE(ppd.po_end_date) >= CURDATE() AND ppd.active = 1)) \n");
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
			query.append(" AND p.active = 'true' AND t.is_active != 'N' AND (etm.active != 0 OR (etm.active = 0 AND DATE(etm.start_date) > CURDATE())) \n")
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
    
    private String getFCProjectQuery(RMGDashboardProjectRequest rmgReq, String sortBy,
            String sortDirection, String fixedCostFilter, List<String> projectNames) {
        Map<String, String> projectFilter = rmgReq != null ? rmgReq.getProjectFilter() : null;

        StringBuilder projectDetailsStartQueryWithRequiredAndAllocatedCountFC = new StringBuilder()
                .append(" WITH REQUIREMENT_COUNT AS ( \n")
                .append(" 	SELECT p.project_id, coalesce(sum(prm.count), 0) required_resources \n")
                .append(" 	FROM projects p \n")
                .append(fixedCostFilter.equals("ontime")
                        ? "LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id AND DATE(ppd.po_start_date) <= CURDATE() AND (ppd.po_end_date IS NULL OR DATE(ppd.po_end_date) >= CURDATE()) AND ppd.active = 1 "
                        : "  LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id AND ppd.active = 1 \n")
                .append("  LEFT JOIN po_requirement_mapping prm ON prm.po_id = ppd.po_id  \n")
                .append(" 	WHERE 1=1  \n")
                .append("  GROUP BY p.project_id \n")
                .append(" ) , \n")
                .append(" ALLOCATED_COUNT AS ( \n")
                .append(" 	SELECT p.project_id, coalesce(count(distinct e.emp_id), 0) as active_resources \n")
                .append(" 	FROM projects p \n")
                .append("  LEFT JOIN teams t ON t.project_id = p.project_id and p.active= 'true' and t.is_active = 'Y' \n")
                .append(fixedCostFilter.equals("ontime")
                        ? "LEFT JOIN employee_team_mapping etm ON t.team_id = etm.team_id AND (etm.active = 1 OR (etm.active = 0 AND DATE(etm.start_date) > CURDATE()))"
                        : "LEFT JOIN employee_team_mapping etm ON etm.team_id = t.team_id and etm.active = 1 \n")
                .append("  LEFT JOIN employee e ON e.emp_id = etm.emp_id and upper(e.employmentstatus) != 'INACTIVE' \n")
                .append(" 	where 1=1 \n")
                .append("  GROUP BY p.project_id\n")
                .append(" ), \n")
                .append(" PROJECT_WISE_ACTIVE_AND_ALLOCATED_COUNT AS ( \n")
                .append(" select rc.project_id, rc.required_resources, ac.active_resources \n")
                .append(" FROM REQUIREMENT_COUNT rc \n")
                .append(" LEFT JOIN ALLOCATED_COUNT ac ON rc.project_id = ac.project_id )  \n")
                .append(" SELECT * FROM \n");

        StringBuilder query = new StringBuilder();
        query.append(projectDetailsStartQueryWithRequiredAndAllocatedCountFC);
        query.append(projectDetailsStartQuery);

        StringBuilder groupQuery = new StringBuilder(projectDetailsGroupQuery).append(" ) as T1");

        query.append(fixedCostFilter.equals("ontime") ? "INNER" : "LEFT")
                .append(" JOIN project_po_details ppd ON ppd.project_id = p.project_id AND ppd.active = 1 \n")
                .append(" INNER JOIN teams t ON p.project_id = t.project_id AND t.is_active = 'Y' \n")
                .append(fixedCostFilter.equals("ontime")
                        ? "INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id AND (etm.active = 1 OR (etm.active = 0 AND DATE(etm.start_date) > CURDATE()))"
                        : " INNER JOIN employee_team_mapping etm ON t.team_id = etm.team_id AND etm.active = 1 \n")
                .append(" LEFT JOIN po_department_mapping pdm on ((ppd.po_id IS NOT NULL AND pdm.po_id = ppd.po_id) OR (pdm.project_id = p.project_id)) AND pdm.dept_id IN :deptIds  \n")
                .append(" LEFT JOIN project_manager_mapping pm on p.project_id = pm.project_id AND pm.active = 1 \n")
                .append(" LEFT JOIN employee e1 on e1.emp_id = pm.project_manager_id \n")
                .append(" LEFT JOIN clients c ON p.client_id = c.client_id \n")
                .append(" LEFT JOIN client_locations cl ON p.client_id = cl.client_id and lower(cl.client_location) != 'wfh' \n")
                .append(" WHERE 1=1 \n");
        appendLinkSearchPrimaryProjectIdFilter(rmgReq, query);

        if (fixedCostFilter != null) {
            if (fixedCostFilter.equals("ontime")) {
                query.append(" AND p.po_project_type = 'Fixed Cost' AND p.active = 'true' \n")
                        .append(" AND (ppd.po_end_date IS NULL OR DATE(ppd.po_end_date) >= CURDATE()) \n")
                        .append(" AND EXISTS (SELECT 1 FROM teams t2 JOIN employee_team_mapping etm2 ON t2.team_id = etm2.team_id WHERE t2.project_id = p.project_id AND (etm2.active = 1 OR (etm2.active = 0 AND DATE(etm2.start_date) > CURDATE()))) \n");

            } else if (fixedCostFilter.equals("defaulter")) {
                query.append(
                        " AND (CASE WHEN p.po_project_type IS NOT NULL AND TRIM(p.po_project_type) != '' THEN p.po_project_type ELSE p.internal_project_type END) = 'Fixed Cost' \n")
                        .append(" AND COALESCE(pwc.active_resources, 0) > 0 \n")
                        .append(" AND EXISTS (SELECT 1 FROM project_po_details pex WHERE pex.project_id = p.project_id AND pex.active = 1 AND pex.po_end_date IS NOT NULL AND DATE(pex.po_end_date) < CURDATE()) \n");
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
//				.append(" AND (DATE(ppd.po_end_date) > CURDATE() OR ppd.po_end_date IS NULL ) \n")
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
		offBoardedCondition.append(" AND p.active= 'true' AND (p.is_draft_project IS NOT NULL AND UPPER(p.is_draft_project) != 'REJECTED') \n")
		.append(" AND EXISTS (SELECT 1 FROM teams t3 WHERE t3.project_id = p.project_id AND t3.is_active  = 'Y' ) \n")
		// .append(" AND p.project_id IN (SELECT t2.project_id FROM teams t2 INNER JOIN employee_team_mapping etm2 ON t2.team_id = etm2.team_id WHERE (etm2.active = 0 AND DATE(etm2.end_date) < CURDATE()))  \n")
		.append(" AND p.project_id NOT IN (SELECT t2.project_id FROM teams t2 LEFT JOIN employee_team_mapping etm2 ON (t2.team_id = etm2.team_id OR etm2.team_id IS NULL) WHERE 1 = 1 AND (etm2.active != 0 OR (etm2.active = 0 AND DATE(etm2.start_date) > CURDATE()))) \n");
		return offBoardedCondition.toString();
	}

	private String getScheduledProjectsCondition() {
		StringBuilder offBoardedCondition = new StringBuilder();
		offBoardedCondition.append(" AND p.active= 'true' AND t.is_active = 'Y' \n")
		.append("AND etm.active = 0 AND DATE(etm.start_date) > CURDATE() \n ");
//		.append(" AND NOT EXISTS ( SELECT 1 FROM teams t2 JOIN employee_team_mapping etm2 ON t2.team_id = etm2.team_id WHERE t2.project_id = p.project_id AND (etm2.active = 1 OR (etm2.active = 0 AND DATE(etm.start_date) <= CURDATE())) ) \n");
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
            case "projectName":
                return "project_name";
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
            case "requiredResources":
                return "required_resources";
            case "activeResources":
                return "active_resources";
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
               	   if (column.equalsIgnoreCase("start_date") 
                    || column.equalsIgnoreCase("end_date") 
                    || column.equalsIgnoreCase("created_on")) {

                    appendDateCondition(query, column, value);
                    continue;
                }

                    if(column.equals("poNo") || column.equals("po_no")){
                        continue;
                    }
                    query.append(String.format(" AND LOWER( %s ) LIKE '%%%s%%' \n", column,
                            value.replace("'", "''").toLowerCase()));
                }
            }
            query.append(" ");
            System.err.println(query.toString());
        }
    }
    
//    private void appendDateCondition(StringBuilder query, String column, String value) {
//
//    	 String normalizedValue = value.replace("/", "-").trim();
//        String formattedDate = convertToYYYYMMDD(normalizedValue);
//
//        if (formattedDate != null) {
//          
//            query.append(String.format(
//                " AND DATE(%s) = '%s' \n",
//                column,
//                formattedDate
//            ));
//        } else {
//           
//            query.append(String.format(
//                " AND DATE_FORMAT(%s, '%%d-%%m-%%Y') LIKE '%%%s%%' \n",
//                column,
//                normalizedValue.replace("'", "''")
//            ));
//        }
//    }
    
    
    private void appendDateCondition(StringBuilder query, String column, String value) {

        if (value == null || value.trim().isEmpty()) {
            return;
        }

        String normalizedValue = value.replace("/", "-").trim();

       
        
        if (!isValidDateOrPartial(normalizedValue)) {
            throw new BadRequestException("Invalid date format.Allowed Formats are dd-mm-yyyy / yyyy-mm-dd");
        }
        
        String formattedDate = convertToYYYYMMDD(normalizedValue);

        if (formattedDate != null) {
            // Exact full date match
            query.append(String.format(
                " AND DATE(%s) = '%s' \n",
                column,
                formattedDate
            ));
        } else {
            // Partial / flexible search using LIKE on BOTH formats
            query.append(String.format(
                " AND ( " +
                " DATE_FORMAT(%s, '%%d-%%m-%%Y') LIKE '%%%s%%' " +
                " OR DATE_FORMAT(%s, '%%Y-%%m-%%d') LIKE '%%%s%%' " +
                " ) \n",
                column,
                escape(normalizedValue),
                column,
                escape(normalizedValue)
            ));
        }
    }
    
    
    private String convertToYYYYMMDD(String value) {

        try {
            value = value.replace("/", "-").trim();

            // Try dd-MM-yyyy
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return LocalDate.parse(value, formatter1).toString();

        } catch (Exception e1) {
            try {
                // Try yyyy-MM-dd
                DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return LocalDate.parse(value, formatter2).toString();
            } catch (Exception e2) {
                return null;
            }
        }
    }
    
    private String escape(String input) {
        return input.replace("'", "''");
    }
    
    private boolean isValidDateOrPartial(String value) {
        String v = value.replace("/", "-").trim();
        DateTimeFormatter[] fullFormats = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
        };
        for (DateTimeFormatter formatter : fullFormats) {
            try {
                LocalDate.parse(v, formatter);
                return true;
            } catch (Exception ignored) {
            }
        }

        if (v.matches("^[0-9\\-]+$")) {
            return true;
        }
        return false;
    }

    String getProjectType(String projectStatus) {
        switch (projectStatus) {
            case "TOTAL_FC":
                return "FIXED COST";
            case "TOTAL_EXPIRED_TNM":
            case "TOTAL_TNM":
            case "ALL_TNM":
            case "TOTAL_ACTIVE_TNM":
                return "TNM";
            case "TOTAL_MONITORING":
                return "MONITORING";
            case "TOTAL_INTERNAL":
                return "INTERNAL";
            default:
                return null;
        }
    }

}
