package com.apmosys.employeeportal.customrepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.EmployeeDetailsDTO;
import com.apmosys.employeeportal.dto.PageDTO;

@SuppressWarnings("unchecked")
@Repository
public class EmployeeCustomRepository {

    @Autowired
    private EntityManager entityManager;

    public Slice<EmployeeDetailsDTO> getNotMappedToAnyProjectEmployeeDetailsPage(boolean isAllAccessEmployee,
            PageDTO pageDTO, List<Long> deptIds) {
        String sortBy = getSortBy(pageDTO.getSortColumn(), true);
        String sortDirection = pageDTO.getSortDirection();
        Pageable pageable = PageRequest.of(pageDTO.getPage(), pageDTO.getSize(),
                Direction.fromString(sortDirection), sortBy);
        Map<String, String> searchFilter = pageDTO.getSearchFilter();

        String baseQuery = getNotMappedToAnyProjectEmployeeDetailsQuery(isAllAccessEmployee, searchFilter);
        StringBuilder listQuery = new StringBuilder(
                "SELECT new com.apmosys.employeeportal.dto.EmployeeDetailsDTO(e.empId,e.employeementId,e.name,e.email,e.employmentstatus,e.mobileNo,jr.name,d.name,e.isConsultant,e.isApprenticeship,e.isApmosysProduct,e.managerId,em.name,e.billableType) ");
        listQuery.append(baseQuery + String.format(" ORDER BY %s %s ", sortBy, sortDirection));
        System.out.println("NOT_MAPPED_PROJECT ================================= query");
        System.out.println(listQuery.toString());
        TypedQuery<EmployeeDetailsDTO> dataQuery = entityManager.createQuery(listQuery.toString(),
                EmployeeDetailsDTO.class);

//        if (!isAllAccessEmployee) {
            dataQuery.setParameter("deptIds", deptIds);
//        }
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        List<EmployeeDetailsDTO> results = dataQuery.getResultList();
        appendPrefixToEmploymentId(results);

        String countQueryStr = new String("SELECT COUNT(DISTINCT e.empId) " + baseQuery);
        TypedQuery<Long> countQuery = entityManager.createQuery(countQueryStr, Long.class);
//        if (!isAllAccessEmployee) {
            countQuery.setParameter("deptIds", deptIds);
//        }
        long total = countQuery.getSingleResult();
        return new PageImpl<>(results, pageable, total);
    }

    public Slice<EmployeeDetailsDTO> getOnBenchForMoreThan30DaysEmployeeDetailsPage(boolean isAllAccessEmployee,
            PageDTO pageDTO, List<Long> deptIds, boolean deptFlag) {
        String sortBy = getCustomQuerySortBy(pageDTO.getSortColumn(), true);
        String sortDirection = pageDTO.getSortDirection();
        Pageable page = PageRequest.of(pageDTO.getPage(), pageDTO.getSize(),
                Direction.fromString(sortDirection), sortBy);
        Map<String, String> searchFilter = pageDTO.getSearchFilter();

        String query = getOnBenchForMoreThan30DaysEmployeeDetailsQuery(isAllAccessEmployee,
                searchFilter, sortBy, sortDirection, deptFlag);
        
        System.err.println(query);

        CompletableFuture<List<EmployeeDetailsDTO>> listFuture = CompletableFuture
                .supplyAsync(() -> getResultList(isAllAccessEmployee, query, sortBy, sortDirection, deptIds, page,
                        deptFlag));
        CompletableFuture<Long> countFuture = CompletableFuture
                .supplyAsync(() -> getResultCount(isAllAccessEmployee, query, deptIds, deptFlag));
        CompletableFuture.allOf(listFuture, countFuture).join();

        List<EmployeeDetailsDTO> list = null;
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

    public Slice<EmployeeDetailsDTO> getOnBenchButProjectMappedEmployeeDetailsPage(boolean isAllAccessEmployee,
            PageDTO pageDTO, List<Long> deptIds) {
        String sortBy = getNativeQuerySortBy(pageDTO.getSortColumn(), true);
        String sortDirection = pageDTO.getSortDirection();
        Pageable pageable = PageRequest.of(pageDTO.getPage(), pageDTO.getSize(),
                Direction.fromString(sortDirection), sortBy);
        Map<String, String> searchFilter = pageDTO.getSearchFilter();

        String baseQuery = getOnBenchButProjectMappedEmployeeDetailsBaseQuery(isAllAccessEmployee, searchFilter);

        List<Long> empIds = getEmployeeIdsByBaseQuery(baseQuery, isAllAccessEmployee, sortBy, sortDirection, pageable,
                deptIds, null, null, null, null, false);
        StringBuilder listQuery = new StringBuilder(
                " SELECT DISTINCT e.emp_id,CASE WHEN e.is_apmosys_product = 'true' then CONCAT('AP-', e.employeement_id) else CONCAT('A-', e.employeement_id) end as employeement_id,e.name,d.name as department_name,e.billable,e.billable_type,p.project_name,p.client_name,p.apmosysrm,p.clientrm,p.po_No,p.po_project_type,p.po_start_date,p.po_end_date \n")
                .append(baseQuery);

        if (empIds != null && !empIds.isEmpty()) {
            listQuery.append("AND e.emp_id IN :empIds \n");
        }
        listQuery.append("" + String.format(" ORDER BY %s %s ", sortBy, sortDirection));
        System.out.println("ON_BENCH_BUT_PROJECT_MAPPED ================================= query");
        System.out.println(listQuery.toString());
        Long total = 0l;
        List<EmployeeDetailsDTO> results = new ArrayList<>();
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<Object[]> projectQuery = session.createNativeQuery(listQuery.toString());
//            if (!isAllAccessEmployee) {
                projectQuery.setParameterList("deptIds", deptIds);
//            }
            if (empIds != null && !empIds.isEmpty()) {
                projectQuery.setParameterList("empIds", empIds);
            }

            results = groupEmployeesById(projectQuery.getResultList());
            String countQuery = "SELECT COUNT(DISTINCT e.emp_id) " + baseQuery;
            NativeQuery<?> countNative = session.createNativeQuery(countQuery);
//            if (!isAllAccessEmployee) {
                countNative.setParameterList("deptIds", deptIds);
//            }
            total = ((Number) countNative.getSingleResult()).longValue();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return new PageImpl<>(results, pageable, total);
    }

    public Slice<EmployeeDetailsDTO> getMappedToShankhEmployeeDetailsPage(boolean isAllAccessEmployee,
            PageDTO pageDTO, List<Long> deptIds, Set<Integer> projectIds, String projectStatus,
            String expiredProjectTimeFrameFilter) {
        String sortBy = getCustomQuerySortBy(pageDTO.getSortColumn(), true);
        String sortDirection = pageDTO.getSortDirection();
        Pageable pageable = PageRequest.of(pageDTO.getPage(), pageDTO.getSize(),
                Direction.fromString(sortDirection), sortBy);
        Map<String, String> searchFilter = pageDTO.getSearchFilter();
        boolean addStartAndEndDate = false;
        String startDate = null;
        String endDate = null;

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
        String sortByTemp = getNativeQuerySortBy(pageDTO.getSortColumn(), true);
        String baseQuery = getMappedToShankhEmployeeDetailsQuery(isAllAccessEmployee, searchFilter, projectStatus,
                addStartAndEndDate);

        List<Long> empIds = getEmployeeIdsByBaseQuery(baseQuery, isAllAccessEmployee, sortByTemp, sortDirection,
                pageable,
                deptIds, projectIds, projectStatus, startDate, endDate, addStartAndEndDate);

        String listQuery = "SELECT * FROM \n"
                + getMappedToShankhEmployeeDetailsListQuery(baseQuery, sortBy, sortDirection, searchFilter,
                        empIds);

        System.out.println("MAPPED_TO_SHANKH ================================= query");
        System.out.println(listQuery);

        Long total = 0l;
        List<EmployeeDetailsDTO> results = new ArrayList<>();
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<Object[]> projectQuery = session.createNativeQuery(listQuery);
            if (empIds != null && !empIds.isEmpty()) {
                projectQuery.setParameterList("empIds", empIds);
            }
            projectQuery.setParameterList("projectIds", projectIds);
//            if (!isAllAccessEmployee) {
                projectQuery.setParameter("deptIds", deptIds);
//            }
            if (addStartAndEndDate) {
                projectQuery.setParameter("startDate", startDate);
                projectQuery.setParameter("endDate", endDate);
            }
            results = groupEmployeesById(projectQuery.getResultList());

            String countQuery = "SELECT COUNT(DISTINCT e.emp_id) " + baseQuery;
            NativeQuery<?> countNative = session.createNativeQuery(countQuery);
            countNative.setParameterList("projectIds", projectIds);
//            if (!isAllAccessEmployee) {
                countNative.setParameter("deptIds", deptIds);
//            }
            if (addStartAndEndDate) {
                countNative.setParameter("startDate", startDate);
                countNative.setParameter("endDate", endDate);
            }
            total = ((Number) countNative.getSingleResult()).longValue();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return new PageImpl<>(results, pageable, total);
    }

    public Slice<EmployeeDetailsDTO> getMappedToInternalAndShankhEmployeeDetailsPage(boolean isAllAccessEmployee,
            PageDTO pageDTO, List<Long> deptIds, Set<Integer> projectIds, String projectStatus,
            String expiredProjectTimeFrameFilter) {
        String sortBy = getNativeQuerySortBy(pageDTO.getSortColumn(), true);
        String sortDirection = pageDTO.getSortDirection();
        Pageable pageable = PageRequest.of(pageDTO.getPage(), pageDTO.getSize(),
                Direction.fromString(sortDirection), sortBy);
        Map<String, String> searchFilter = pageDTO.getSearchFilter();
        boolean addStartAndEndDate = false;
        String startDate = null;
        String endDate = null;

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

        String baseQuery = getMappedToInternalAndShankhEmployeeDetailsQuery(isAllAccessEmployee, searchFilter,
                projectStatus, addStartAndEndDate);
        List<Long> empIds = getEmployeeIdsByBaseQuery(baseQuery, isAllAccessEmployee, sortBy, sortDirection, pageable,
                deptIds, projectIds, projectStatus, startDate, endDate, addStartAndEndDate);

        StringBuilder listQuery = new StringBuilder();
        listQuery.append(
                "SELECT DISTINCT e.emp_id,CASE WHEN e.is_apmosys_product = 'true' then CONCAT('AP-', e.employeement_id) else CONCAT('A-', e.employeement_id) end as employeement_id \n")
                .append(",e.name,d.name as department_name,e.billable,e.billable_type \n")
                .append(",p.project_name,p.client_name as client_name,p.apmosysrm,p.clientrm,p.po_No,p.po_project_type,p.po_start_date,p.po_end_date  \n")
                .append(",pm.name as project_manager_name,t.team_name,etm.employee_role \n")
                .append(baseQuery);

        if (empIds != null && !empIds.isEmpty()) {
            listQuery.append("AND e.emp_id IN :empIds \n");
        }
        listQuery.append("" + String.format(" ORDER BY %s %s ", sortBy, sortDirection));
        System.out.println("MAPPED_TO_INTERNAL_AND_SHANKH ================================= query");
        System.out.println(listQuery.toString());

        Long total = 0l;
        List<EmployeeDetailsDTO> results = new ArrayList<>();

        try (Session session = entityManager.unwrap(Session.class)) {

            NativeQuery<Object[]> projectQuery = session.createNativeQuery(listQuery.toString());
            if (empIds != null && !empIds.isEmpty()) {
                projectQuery.setParameterList("empIds", empIds);
            }
            projectQuery.setParameterList("projectIds", projectIds);
//            if (!isAllAccessEmployee) {
                projectQuery.setParameter("deptIds", deptIds);
//            }
            if (addStartAndEndDate) {
                projectQuery.setParameter("startDate", startDate);
                projectQuery.setParameter("endDate", endDate);
            }

            results = groupEmployeesById(projectQuery.getResultList());

            String countQuery = "SELECT COUNT(DISTINCT e.emp_id) " + baseQuery;
            NativeQuery<?> countNative = session.createNativeQuery(countQuery);
            countNative.setParameterList("projectIds", projectIds);
//            if (!isAllAccessEmployee) {
                countNative.setParameter("deptIds", deptIds);
//            }
            if (addStartAndEndDate) {
                countNative.setParameter("startDate", startDate);
                countNative.setParameter("endDate", endDate);
            }
            total = ((Number) countNative.getSingleResult()).longValue();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return new PageImpl<>(results, pageable, total);
    }

    public Slice<EmployeeDetailsDTO> getWithoutAnyBillabilityEmployeeDetailsPage(boolean isAllAccessEmployee,
            PageDTO pageDTO, List<Long> deptIds, Set<Integer> projectIds) {

        String sortBy = getCustomQuerySortBy(pageDTO.getSortColumn(), true);
        String sortDirection = pageDTO.getSortDirection();
        Pageable page = PageRequest.of(pageDTO.getPage(), pageDTO.getSize(),
                Direction.fromString(sortDirection), sortBy);
        Map<String, String> searchFilter = pageDTO.getSearchFilter();

        String baseQuery = getWithoutAnyBillabilityEmployeeDetailsQuery(isAllAccessEmployee, searchFilter);

        StringBuilder query = new StringBuilder(
                "SELECT * FROM " + baseQuery + String.format(" ORDER BY %s %s ", sortBy, sortDirection));

        System.out.println("WITHOUT_ANY_BILLABILITY ================================= query");
        System.out.println(query.toString());

        Long total = 0l;
        List<EmployeeDetailsDTO> results = new ArrayList<>();
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<Object[]> projectQuery = session.createNativeQuery(query.toString());
//            if (!isAllAccessEmployee) {
                projectQuery.setParameterList("deptIds", deptIds);
//            }
            int offset = page.getPageNumber() * page.getPageSize();
            projectQuery.setFirstResult(offset);
            projectQuery.setMaxResults(page.getPageSize());

            results = projectQuery.getResultList().stream()
                    .map(EmployeeDetailsDTO::withoutBillability)
                    .collect(Collectors.toList());

            String countQuery = "SELECT COUNT(DISTINCT emp_id) FROM " + baseQuery;
            NativeQuery<?> countNative = session.createNativeQuery(countQuery);
//            if (!isAllAccessEmployee) {
                countNative.setParameterList("deptIds", deptIds);
//            }
            total = ((Number) countNative.getSingleResult()).longValue();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return new PageImpl<>(results, page, total);
    }

    public Slice<EmployeeDetailsDTO> getUnfilledTimesheetProjectDetailsPage(boolean isAllAccessEmployee,
            List<Long> deptIds, PageDTO pageDTO, Set<Integer> projectIds,
            LocalDate fromDate, LocalDate toDate, String projectType) {

        String sortBy = getCustomQuerySortBy(pageDTO.getSortColumn(), true);
        String sortDirection = pageDTO.getSortDirection();
        Pageable pageable = PageRequest.of(pageDTO.getPage(), pageDTO.getSize(), Direction.fromString(sortDirection),
                sortBy);

        Map<String, String> searchFilter = pageDTO.getSearchFilter();
        String baseQuery = getUnfilledTimesheetProjectDetailsQuery(isAllAccessEmployee, fromDate, toDate);
        
        if (projectType != null && !projectType.trim().isEmpty()) {
            if ("TNM".equalsIgnoreCase(projectType)) {
                baseQuery += " AND p.po_project_type = 'TNM' ";
            } else if ("Fixed Cost".equalsIgnoreCase(projectType)) {
                baseQuery += " AND p.po_project_type = 'Fixed Cost' ";
            } else if ("Monitoring".equalsIgnoreCase(projectType)) {
                baseQuery += " AND p.po_project_type = 'Monitoring' ";
            } else if ("Internal".equalsIgnoreCase(projectType)) {
                baseQuery += " AND p.po_project_type IS NULL AND p.internal_project_type IS NOT NULL ";
            }
        }
        
        List<Long> projectIdsTemp = getProjectIdsByBaseQuery(baseQuery, pageDTO.getSortColumn(), sortDirection,
                pageable, projectIds,
                fromDate, toDate, searchFilter, isAllAccessEmployee, deptIds);

        String listQuery = "SELECT * FROM \n"
                + getUnfilledTimesheetProjectDetailsListQuery(baseQuery, sortBy, sortDirection, searchFilter,
                        projectIdsTemp);

        System.out.println("UNFILLED_TIMESHEET_PROJECT ================================= query");
        System.out.println(listQuery.toString());

        Long total = 0l;
        List<EmployeeDetailsDTO> results = new ArrayList<>();

        try (Session session = entityManager.unwrap(Session.class)) {

            NativeQuery<Object[]> projectQuery = session.createNativeQuery(listQuery.toString());
            projectQuery.setParameterList("projectIds", projectIds);
//            if (!isAllAccessEmployee) {
                projectQuery.setParameter("deptIds", deptIds);
//            }
            if (projectIdsTemp != null && !projectIdsTemp.isEmpty()) {
                projectQuery.setParameterList("projectIdsTemp", projectIdsTemp);
            }
            if (fromDate != null && toDate != null) {
                projectQuery.setParameter("fromDate", fromDate);
                projectQuery.setParameter("toDate", toDate);
            }
            results = groupEmployeesUnfilledTimesheetProjectEmployees(projectQuery.getResultList());

            StringBuilder countQuery = new StringBuilder("SELECT COUNT(DISTINCT p.project_id) ").append(baseQuery);

            appenCustomSearchToNativeQuery(searchFilter, countQuery, false);

            NativeQuery<?> countNative = session.createNativeQuery(countQuery.toString());
            countNative.setParameterList("projectIds", projectIds);
//            if (!isAllAccessEmployee) {
                countNative.setParameter("deptIds", deptIds);
//            }
            if (fromDate != null && toDate != null) {
                countNative.setParameter("fromDate", fromDate);
                countNative.setParameter("toDate", toDate);
            }

            total = ((Number) countNative.getSingleResult()).longValue();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return new PageImpl<>(results, pageable, total);
    }

    private List<EmployeeDetailsDTO> getResultList(boolean isAllAccessEmployee, String query, String sortBy,
            String sortDirection, List<Long> deptIds, Pageable page, boolean deptFlag) {
        query = "SELECT * FROM " + query;
        System.out.println("================================= query");
        System.out.println(query);
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<Object[]> nativeQuery = session.createNativeQuery(query)
                    .unwrap(org.hibernate.query.NativeQuery.class);
//            if (!isAllAccessEmployee) {
                nativeQuery.setParameter("deptIds", deptIds);
//            }

            // Set pagination offsets
            int offset = page.getPageNumber() * page.getPageSize();
            nativeQuery.setFirstResult(offset);
            nativeQuery.setMaxResults(page.getPageSize());

            List<Object[]> list = nativeQuery.getResultList();
            return list.stream()
                    .map(EmployeeDetailsDTO::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private Long getResultCount(boolean isAllAccessEmployee, String query, List<Long> deptIds, boolean deptFlag) {
        query = "SELECT count(*) FROM " + query;
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<Object[]> nativeQuery = session.createNativeQuery(query)
                    .unwrap(org.hibernate.query.NativeQuery.class);

//            if (!isAllAccessEmployee) {
                nativeQuery.setParameter("deptIds", deptIds);
//            }
            Object count = nativeQuery.getSingleResult();
            return count == null ? 0 : Long.parseLong(count.toString());

        } catch (Exception e) {
            throw e;
        }
    }

    public List<Long> getEmployeeIdsByBaseQuery(String baseQuery, boolean isAllAccessEmployee, String sortBy,
            String sortDirection, Pageable pageable, List<Long> deptIds, Set<Integer> projectIds, String projectStatus,
            String startDate, String endDate, boolean addStartAndEndDate) {
        StringBuilder query = new StringBuilder();
        query.append("WITH ranked_employees AS (\n")
                .append(" SELECT \n")
                .append(" e.emp_id, \n")
                .append(" ROW_NUMBER() OVER ( PARTITION BY e.emp_id ")
                .append(String.format(" ORDER BY %s %s ", sortBy, sortDirection))
                .append(" ) AS rn \n")
                .append(baseQuery)
                .append(") \n")
                .append("SELECT DISTINCT emp_id \n")
                .append("FROM ranked_employees \n")
                .append("WHERE rn = 1 \n")
                .append("ORDER BY emp_id \n")
                .append("LIMIT :pageSize OFFSET :offset \n");

        System.out.println("EMPIDS FOR BASE QUERY ================================= query");
        System.out.println(query.toString());

        List<Long> empIds;
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<?> nativeQuery = session.createNativeQuery(query.toString());
            if (deptIds != null && !deptIds.isEmpty()) {
                nativeQuery.setParameterList("deptIds", deptIds);
            }
            if (projectIds != null) {
                nativeQuery.setParameter("projectIds", projectIds);
            }
            if (addStartAndEndDate) {
                nativeQuery.setParameter("startDate", startDate);
                nativeQuery.setParameter("endDate", endDate);
            }
            nativeQuery.setParameter("pageSize", pageable.getPageSize());
            nativeQuery.setParameter("offset", (int) pageable.getOffset());
            empIds = (List<Long>) nativeQuery.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return empIds;
    }

    public List<Long> getProjectIdsByBaseQuery(String baseQuery, String sortByColumn,
            String sortDirection, Pageable pageable, Set<Integer> projectIds, LocalDate fromDate, LocalDate toDate,
            Map<String, String> searchFilter,boolean isAllAccessEmployee,List<Long> deptIds) {
        StringBuilder query = new StringBuilder();

        String sortBy = getNativeQuerySortBy(sortByColumn, false);

        query.append("SELECT * FROM ( \n")
                .append("WITH ranked_projects AS (\n")
                .append(" SELECT \n")
                .append(" p.project_id, \n")
                .append(" ROW_NUMBER() OVER ( PARTITION BY p.project_id ")
                .append(String.format(" ORDER BY %s %s ", sortBy, sortDirection))
                .append(" ) AS rn \n")
                .append(baseQuery);
        appenCustomSearchToNativeQuery(searchFilter, query, false);
        query.append(") \n")
                .append("SELECT DISTINCT project_id \n")
                .append("FROM ranked_projects \n")
                .append("WHERE rn = 1 \n")
                .append("ORDER BY project_id \n")
                .append("LIMIT :pageSize OFFSET :offset \n");

        query.append(" ) AS T1 WHERE 1=1 \n");

        System.out.println("PROJECTIDS FOR BASE QUERY ================================= query");
        System.out.println(query.toString());

        List<Long> empIds;
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<?> nativeQuery = session.createNativeQuery(query.toString());

            if (projectIds != null) {
                nativeQuery.setParameter("projectIds", projectIds);
            }
//            if (!isAllAccessEmployee) {
                nativeQuery.setParameter("deptIds", deptIds);
//            }
            if (fromDate != null && toDate != null) {
                nativeQuery.setParameter("fromDate", fromDate);
                nativeQuery.setParameter("toDate", toDate);
            }
            nativeQuery.setParameter("pageSize", pageable.getPageSize());
            nativeQuery.setParameter("offset", (int) pageable.getOffset());
            empIds = (List<Long>) nativeQuery.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return empIds;
    }

    public String getNotMappedToAnyProjectEmployeeDetailsQuery(boolean isAllAccessEmployee,
            Map<String, String> searchFilter) {
        StringBuilder query = new StringBuilder();
        query.append(" FROM Employee e  \n")
                .append("INNER join JobRole jr on jr.jobRoleId = e.jobRoleId \n")
                .append("INNER join Department d on d.deptId = jr.deptId \n")
                .append("LEFT JOIN Employee em ON em.empId = e.managerId \n")
                .append("WHERE NOT EXISTS (SELECT 1 FROM EmployeeTeamMap etm \n")
                .append("                  JOIN Team t ON t.teamId = etm.teamId \n")
                .append("                  JOIN Project p ON p.projectId = t.projectId \n")
                .append("                  WHERE etm.empId = e.empId AND (etm.active = 1 OR (etm.active = 2 AND DATE(etm.startDate) <= CURDATE())) AND t.isActive = 'Y' AND p.active = 'true') \n")
                .append(" and e.employmentstatus != 'InActive' and e.empId NOT BETWEEN 1 AND 6 \n");

//        if (!isAllAccessEmployee) {
            query.append("and d.deptId IN :deptIds ");
//        }
        appenCustomSearchToQuery(searchFilter, query);
        return query.toString();
    }

    public String getOnBenchForMoreThan30DaysEmployeeDetailsQuery(boolean isAllAccessEmployee,
            Map<String, String> searchFilter, String sortBy, String sortDirection, boolean deptFlag) {
        StringBuilder query = new StringBuilder();
        query.append(" ( SELECT DISTINCT e.emp_id, e.name emp_name \n")
                .append(",CASE WHEN e.is_apmosys_product = 'true' then CONCAT('AP-', e.employeement_id) else CONCAT('A-', e.employeement_id) end as employeement_id \n")
                .append(",d.name as department_name,e.billable, e.billable_type, STR_TO_DATE(etm.start_date, '%Y-%m-%d') as onbench_datetime \n")
                .append(",TIMESTAMPDIFF(DAY, STR_TO_DATE(etm.start_date, '%Y-%m-%d'), CURRENT_DATE()) as days_on_bench \n")
                .append(",p.project_name,GROUP_CONCAT(DISTINCT pm.name ORDER BY pm.name SEPARATOR ', ') as project_manager_name, t.team_name, etm.employee_role \n")
                .append("FROM employee_team_mapping etm \n")
                .append("INNER JOIN employee e ON e.emp_id = etm.emp_id \n")
                .append("INNER JOIN job_role jr ON e.job_role_id = jr.job_role_id \n")
                .append("INNER JOIN department d ON jr.dept_id = d.dept_id \n")
                .append("INNER JOIN teams t ON etm.team_id = t.team_id \n")
                .append("INNER JOIN projects p ON t.project_id = p.project_id \n")
                .append("INNER JOIN clients c ON p.client_id = c.client_id \n")
                .append("LEFT JOIN project_manager_mapping pmm ON pmm.project_id = p.project_id AND pmm.active = 1 \n")
                .append("LEFT JOIN employee pm ON pm.emp_id = pmm.project_manager_id \n")
                .append("WHERE 1=1 \n")
                .append("AND etm.active != 0 \n")
                .append("AND internal_project_type = 'Bench' \n")
                .append("AND t.is_active = 'Y' \n")
                .append("AND p.active = 'true' \n")
                .append("AND DATEDIFF(CURDATE(), etm.start_date) > 30 \n")
                .append("AND e.employmentstatus != 'InActive' \n")
                .append("AND e.billable_type = 'Bench' \n");

//        if (!isAllAccessEmployee) {
        	query.append(" AND d.dept_id IN :deptIds \n");
//        } 
        
        query.append(" GROUP BY e.emp_id,e.name,e.is_apmosys_product,e.employeement_id,d.name,e.billable \n")
                .append(" ,e.billable_type,etm.start_date,p.project_name,t.team_name,etm.employee_role \n");
        query.append(" ) as T1 \n WHERE 1=1 \n");
        appenCustomSearchToNativeQuery(searchFilter, query, true);
        query.append(String.format(" ORDER BY %s %s ", sortBy, sortDirection));
        return query.toString();
    }

    public String getOnBenchButProjectMappedEmployeeDetailsBaseQuery(boolean isAllAccessEmployee,
            Map<String, String> searchFilter) {
        StringBuilder query = new StringBuilder();
        query.append("FROM projects p \n")
                .append("INNER JOIN teams t on p.project_id = t.project_id  \n")
                .append("INNER JOIN employee_team_mapping etm on t.team_id = etm.team_id  \n")
                .append("INNER JOIN employee e on e.emp_id = etm.emp_id  \n")
                .append("INNER JOIN job_role jr on e.job_role_id = jr.job_role_id  \n")
                .append("INNER JOIN department d on d.dept_Id = jr.dept_Id  \n")
                .append("where p.active = 'true' AND t.is_active != 'N' AND etm.active != 0  \n")
                .append("AND e.employmentstatus != 'InActive'  \n")
                .append("AND e.billable_type = 'Bench' AND ((p.po_project_type IS NOT NULL AND (p.po_project_type like 'FIXED%COST' OR p.po_project_type like '%TNM%' OR p.po_project_type like '%Monitoring%) OR (p.po_project_type IS NULL AND p.internal_project_type = 'InternalRNDProducts')) \n")
                .append("AND e.emp_id NOT BETWEEN 1 AND 6 \n");

//        if (!isAllAccessEmployee) {
            query.append(" AND d.dept_Id IN :deptIds ");
//        }
        appenCustomSearchToNativeQuery(searchFilter, query, false);
        return query.toString();
    }

    public String getMappedToShankhEmployeeDetailsQuery(boolean isAllAccessEmployee, Map<String, String> searchFilter,
            String projectStatus, boolean addStartAndEndDate) {
        StringBuilder query = new StringBuilder();
        query.append("FROM employee_team_mapping etm \n")
                .append("RIGHT JOIN employee e ON e.emp_id = etm.emp_id  \n")
                .append("RIGHT JOIN teams t ON t.team_id = etm.team_id  \n")
                .append("INNER JOIN projects p ON p.project_id = t.project_id  \n")
                .append("INNER JOIN clients c ON c.client_id = p.client_id  \n")
                .append("INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id  \n")
                .append("INNER JOIN department d ON d.dept_id = jr.dept_id  \n")
                .append("LEFT JOIN  project_manager_mapping pmm ON pmm.project_id = p.project_id AND pmm.active = 1   \n")
                .append("LEFT JOIN  employee pm ON pm.emp_id = pmm.project_manager_id \n")
                .append("WHERE p.project_id IN :projectIds  \n")
                .append("AND e.employmentstatus != 'InActive'  \n")
                .append("AND e.emp_id NOT BETWEEN 1 AND  6 \n");
//        if (!isAllAccessEmployee) {
            query.append(" AND d.dept_Id IN :deptIds \n");
//        }
        if (!projectStatus.equals("COMPLETED_IN_SHANKH")) {
            query.append(" AND (etm.active = 1 OR (etm.active = 2 AND DATE(etm.start_date) <= CURDATE())) AND t.is_active = 'Y' \n");
        }

        if ("COMPLETED_IN_SHANKH".equals(projectStatus)
                || projectStatus.equals("COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE")) {
            query.append("AND p.status = 'Completed' \n");
        }
        if (projectStatus.equals("TOTAL_ACTIVE_TNM") || projectStatus.equals("TOTAL_EXPIRED_TNM")
                || projectStatus.equals("TOTAL_TNM")) {
            query.append(" AND po_project_type = 'TNM' \n");
        }
        if (projectStatus.equals("TOTAL_EXPIRED_TNM")) {
            query.append(" AND DATE(p.po_end_date) < CURDATE() \n");
            if (addStartAndEndDate) {
                query.append(" AND DATE(p.po_end_date) between :startDate and :endDate \n");
            }
        } else if (projectStatus.equals("TOTAL_INTERNAL")) {
            query.append(" AND p.internal_project_type is not null \n");
        } else if (projectStatus.equals("TOTAL_MONITORING")) {
            query.append(" AND p.po_project_type = 'Monitoring' \n");
        } else if (projectStatus.equals("TOTAL_FC")) {
            query.append(" AND p.po_project_type = 'Fixed Cost' \n");
        }
        appenCustomSearchToNativeQuery(searchFilter, query, false);
        return query.toString();
    }

    public String getMappedToInternalAndShankhEmployeeDetailsQuery(boolean isAllAccessEmployee,
            Map<String, String> searchFilter, String projectStatus, boolean addStartAndEndDate) {
        StringBuilder query = new StringBuilder();
        query.append("FROM employee_team_mapping etm  \n")
                .append("RIGHT JOIN employee e ON e.emp_id = etm.emp_id  \n")
                .append("RIGHT JOIN teams t ON t.team_id = etm.team_id  \n")
                .append("INNER JOIN projects p ON p.project_id = t.project_id  \n")
                .append("INNER JOIN clients c ON c.client_id = p.client_id  \n")
                .append("INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id  \n")
                .append("INNER JOIN department d ON d.dept_id = jr.dept_id  \n")
                .append("LEFT JOIN project_manager_mapping pmm ON pmm.project_id = p.project_id  AND pmm.active = 1  \n")
                .append("LEFT JOIN employee pm ON pm.emp_id = pmm.project_manager_id  \n")
                .append("WHERE 1=1  \n")
                .append("AND e.emp_id IN ( \n")
                .append("SELECT e1.emp_id  \n")
                .append("FROM employee_team_mapping etm1  \n")
                .append("JOIN employee e1 ON e1.emp_id = etm1.emp_id  \n")
                .append("JOIN teams t1 ON t1.team_id = etm1.team_id  \n")
                .append("JOIN projects p1 ON p1.project_id = t1.project_id  \n")
                .append("LEFT JOIN project_manager_mapping pmm1 ON pmm1.project_id = p1.project_id AND pmm1.active = 1   \n")
                .append("WHERE etm1.active != 0 AND t1.is_active = 'Y' AND e1.employmentstatus != 'InActive'  \n")
                .append("AND p1.project_id IN :projectIds  \n")
                .append("GROUP BY e1.emp_id  \n")
                .append("HAVING COUNT(CASE WHEN p1.po_project_id IS NULL THEN 1 END) > 0  \n")
                .append("AND COUNT(CASE WHEN p1.po_project_id IS NOT NULL THEN 1 END) > 0  \n")
                .append(" ) \n")
                .append("AND e.employmentstatus != 'InActive'  \n")
                .append("AND e.emp_id NOT BETWEEN 1 AND 6 \n");
//        if (!isAllAccessEmployee) {
            query.append(" AND d.dept_Id IN :deptIds ");
//        }
        if (!projectStatus.equals("COMPLETED_IN_SHANKH")) {
            query.append("AND etm.active != 0 \n")
                    .append("AND t.is_active = 'Y'  \n");
        }
        if ("COMPLETED_IN_SHANKH".equals(projectStatus)
                || projectStatus.equals("COMPLETED_IN_SHANKH_BUT_TEAM_ACTIVE")) {
            query.append("AND p.status = 'Completed' \n");
        }
        if (projectStatus.equals("TOTAL_ACTIVE_TNM") || projectStatus.equals("TOTAL_EXPIRED_TNM")
                || projectStatus.equals("TOTAL_TNM")) {
            query.append(" AND po_project_type = 'TNM' \n");
        }
        if (projectStatus.equals("TOTAL_EXPIRED_TNM")) {
            query.append(" AND DATE(p.po_end_date) < CURDATE() \n");
            if (addStartAndEndDate) {
                query.append(" AND DATE(p.po_end_date) between :startDate and :endDate \n");
            }
        } else if (projectStatus.equals("TOTAL_INTERNAL")) {
            query.append(" AND p.internal_project_type is not null \n");
        } else if (projectStatus.equals("TOTAL_MONITORING")) {
            query.append(" AND p.po_project_type = 'Monitoring' \n");
        } else if (projectStatus.equals("TOTAL_FC")) {
            query.append(" AND p.po_project_type = 'Fixed Cost' \n");
        }
        appenCustomSearchToNativeQuery(searchFilter, query, false);
        return query.toString();
    }

    public String getWithoutAnyBillabilityEmployeeDetailsQuery(boolean isAllAccessEmployee,
            Map<String, String> searchFilter) {
        StringBuilder query = new StringBuilder();
        query.append(" ( SELECT DISTINCT \n")
                .append("e.emp_id,CASE WHEN e.is_apmosys_product = 'true' then CONCAT('AP-', e.employeement_id) else CONCAT('A-', e.employeement_id) end as employeement_id  \n")
                .append(",e.name emp_name,d.name as department_name, em.name as manager_name, jr.name as job_role_name  \n")
                .append("FROM employee e  \n")
                .append("INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id \n")
                .append("INNER JOIN department d ON d.dept_id = jr.dept_id \n")
                .append("LEFT JOIN employee em ON e.manager_id = em.emp_id  \n")
                .append(" WHERE 1=1 \n")
                .append("AND (e.billable_type IS NULL OR LOWER(e.billable_type) = 'none')\n")
                .append("AND e.emp_id NOT BETWEEN 1 AND 6  \n")
                .append("AND e.employmentstatus != 'InActive' \n");

//        if (!isAllAccessEmployee) {
            query.append(" AND d.dept_id IN :deptIds \n");
//        }
        query.append(" ) as T1 \n WHERE 1=1 \n");
        appenCustomSearchToNativeQuery(searchFilter, query, true);
        return query.toString();
    }

    // public EmployeeDetailsDTO(Long empId, Long employeementId, String name, String departmentName, String projectName, String teamName, String clientName, String apmosysRM, String clientRM, String poNo,
    //     String poProjectType, String poStartDate, String poEndDate, Date etmStartDate, Long etmActive

    public String getFutureStartDateAssignedEmployeeDetailsQuery(boolean isAllAccessEmployee, Map<String, String> searchFilter) {
        StringBuilder query = new StringBuilder();
        query.append(" ( SELECT DISTINCT \n")
                .append("e.emp_id,CASE WHEN e.is_apmosys_product = 'true' then CONCAT('AP-', e.employeement_id) else CONCAT('A-', e.employeement_id) end as employeement_id  \n")
                .append(",e.name emp_name,d.name as department_name, p.project_name, t.team_name, c.client_name, ppd.apmosys_rm, ppd.client_rm, ppd.po_no, p.po_project_type, DATE(ppd.po_start_date), DATE(ppd.po_end_date), etm.start_date as etm_start_date, etm.active as etm_active  \n")
                .append("FROM employee e  \n")
                .append("INNER JOIN employee_team_mapping etm ON etm.emp_id = e.emp_id AND etm.active IN(2, 0) AND DATE(etm.start_date) > CURDATE() \n")
                .append("INNER JOIN teams t ON t.team_id = etm.team_id  \n")
                .append("INNER JOIN projects p ON p.project_id = t.project_id  \n")
                .append("LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id AND etm.po_id = ppd.po_id AND DATE(ppd.po_start_date) <= CURRENT_DATE AND (ppd.po_end_date IS NULL OR DATE(ppd.po_end_date) >= CURRENT_DATE) AND ppd.active  = 1 \n")
                .append("INNER JOIN clients c ON c.client_id = p.client_id  \n")
                .append("INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id \n")
                .append("INNER JOIN department d ON d.dept_id = jr.dept_id \n")
                .append("LEFT JOIN employee em ON e.manager_id = em.emp_id  \n")
                .append("WHERE 1=1 \n")
                .append("AND e.emp_id NOT BETWEEN 1 AND 6  \n")
                .append("AND e.employmentstatus != 'InActive' \n");
        query.append(" AND d.dept_id IN :deptIds \n");
        query.append(" ) as T1 \n WHERE 1=1 \n");
        appenCustomSearchToNativeQuery(searchFilter, query, true);
        return query.toString();
    }

    public String getUnfilledTimesheetProjectDetailsQuery(boolean isAllAccessEmployee, LocalDate fromDate, LocalDate toDate) {
        StringBuilder query = new StringBuilder();
        query
                .append(" FROM projects p  \n")
                .append(" INNER JOIN teams t ON t.project_id = p.project_id  \n")
                .append(" INNER JOIN employee_team_mapping etm ON etm.team_id = t.team_id  \n")
                .append(" LEFT JOIN project_po_details ppd ON ppd.project_id = p.project_id AND etm.po_id = ppd.po_id AND DATE(ppd.po_start_date) <= CURRENT_DATE AND (ppd.po_end_date IS NULL OR DATE(ppd.po_end_date) >= CURRENT_DATE) AND ppd.active  = 1 \n")
                .append(" LEFT JOIN po_department_mapping pdm on ((ppd.po_id IS NOT NULL AND pdm.po_id = ppd.po_id) OR (ppd.po_id IS NULL AND pdm.project_id = p.project_id)) \n")
                .append(" LEFT JOIN project_manager_mapping pmm ON p.project_id = pmm.project_id  AND pmm.active = 1  \n")
                .append(" LEFT JOIN employee pm ON pm.emp_id = pmm.project_manager_id \n")
                .append(" INNER JOIN employee e ON e.emp_id = etm.emp_id  \n")
                .append(" INNER JOIN clients c ON c.client_id = p.client_id  \n")
                .append(" INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id  \n")
                .append(" INNER JOIN department d ON jr.dept_id = d.dept_id  \n")
                .append(" WHERE 1=1 \n")
                .append(" AND p.active = 'true' AND t.is_active = 'Y'  \n")
                .append(" AND etm.active != 0 AND e.employmentstatus != 'InActive' \n")
                .append(" AND p.project_id IN :projectIds  \n");
//        if (!isAllAccessEmployee) {
            query.append(" AND d.dept_id IN :deptIds ");
//        }

        query.append(" AND p.project_id NOT IN (SELECT p2.project_id FROM employee_timesheets_new et \n")
                .append(" INNER JOIN employee_timesheet_activities_mapping_new etam ON et.timesheet_id = etam.timesheet_id \n")
                .append(" INNER JOIN activities a ON a.activity_id = etam.activity_id  \n")
                .append(" RIGHT JOIN teams t2 ON t2.team_id = a.team_id  \n")
                .append(" INNER JOIN projects p2 ON p2.project_id = t2.project_id  \n")
                .append(" WHERE 1=1 AND p2.start_date IS NOT NULL ) \n");
                if (fromDate != null && toDate != null) {
                    query.append("  AND p.start_date IS NOT NULL AND DATE(p.start_date) BETWEEN DATE(:fromDate) AND DATE(:toDate) \n");
                }
        return query.toString();
    }

    public String getMappedToShankhEmployeeDetailsListQuery(String baseQuery, String sortBy, String sortDirection,
            Map<String, String> searchFilter, List<Long> empIds) {
        StringBuilder listQuery = new StringBuilder();
        listQuery.append(" ( SELECT DISTINCT e.emp_id,CASE WHEN e.is_apmosys_product = 'true' then CONCAT('AP-', e.employeement_id) else CONCAT('A-', e.employeement_id) end as employeement_id \n")
                .append(" ,e.name emp_name,d.name as department_name,e.billable,e.billable_type \n")
                .append(" ,p.project_name,p.client_name as client_name,p.apmosysrm,p.clientrm,p.po_No,p.po_project_type,DATE(p.po_start_date),DATE(p.po_end_date)  \n")
                .append(" ,GROUP_CONCAT(DISTINCT pm.name ORDER BY pm.name SEPARATOR ', ') as project_manager_name,t.team_name,etm.employee_role \n")
                .append(baseQuery);

        if (empIds != null && !empIds.isEmpty()) {
            listQuery.append("AND e.emp_id IN :empIds \n");
        }

        listQuery.append(
                "GROUP BY e.emp_id,e.is_apmosys_product,e.employeement_id,e.name,d.name,e.billable,e.billable_type,p.project_name \n")
                .append(",p.client_name,p.apmosysrm,p.clientrm,p.po_no,p.po_project_type,p.po_start_date,p.po_end_date,t.team_name,etm.employee_role \n");

        listQuery.append(" ) as T1 \n WHERE 1=1 \n");
        appenCustomSearchToNativeQuery(searchFilter, listQuery, true);
        listQuery.append("" + String.format(" ORDER BY %s %s ", sortBy, sortDirection));
        return listQuery.toString();
    }

    public String getUnfilledTimesheetProjectDetailsListQuery(String baseQuery, String sortBy, String sortDirection,
            Map<String, String> searchFilter, List<Long> projectIdsTemp) {
        StringBuilder listQuery = new StringBuilder();
        listQuery.append("( SELECT DISTINCT \n")
                .append("p.project_id, p.project_name, ppd.apmosys_rm, ppd.client_rm, ppd.po_start_date, ppd.po_end_date, ppd.po_no, p.po_project_type,  \n")
                .append("c.client_name, \n")
                .append("GROUP_CONCAT(DISTINCT pm.name ORDER BY pm.name SEPARATOR ', ') AS project_manager_name, \n")
                .append("t.team_id, t.team_name,  \n")
                .append("e.emp_id, e.name emp_name , jr.name job_role_name, d.name department_name , e.mobile_no, e.email,  e.billable, e.billable_type, p.start_date effective_start_date, e.employeement_id  \n")
                .append(baseQuery);

        if (projectIdsTemp != null && !projectIdsTemp.isEmpty()) {
            listQuery.append("AND p.project_id IN :projectIdsTemp \n");
        }

        listQuery.append(
                "GROUP BY p.project_id, p.project_name, ppd.apmosys_rm, ppd.client_rm, ppd.po_start_date, ppd.po_end_date, ppd.po_no, p.po_project_type,  \n")
                .append("c.client_name, \n")
                .append("t.team_id, t.team_name,  \n")
                .append("e.emp_id, e.name , jr.name , d.name  , e.mobile_no, e.email,  e.billable, e.billable_type, etm.start_date, e.employeement_id");

        listQuery.append(" ) as T1 \n WHERE 1=1 \n");
        appenCustomSearchToNativeQuery(searchFilter, listQuery, true);
        listQuery.append("" + String.format(" ORDER BY %s %s ", sortBy, sortDirection));
        return listQuery.toString();
    }

    private String getSortBy(String sortColumn, boolean defaultFlag) {
        switch (sortColumn) {
            case "name":
                return "e.name";
            case "managerName":
                return "em.name";
            case "employmentIdAcToET":
                return "e.employeementId";
            case "departmentName":
                return "d.name";
            case "billableType":
                return "e.billableType";
            case "billable":
                return "e.billable";
            case "poNo":
                return "p.poNo";
            case "projectName":
                return "p.projectName";
            case "clientName":
                return "p.clientName";
            case "poProjectType":
                return "p.poProjectType";
            case "poStartDate":
                return "p.poStartDate";
            case "poEndDate":
                return "p.poEndDate";
            case "jobRoleName":
                return "jr.name";
            case "apmosysRM":
                return "p.apmosysRM";
            case "clientRM":
                return "p.clientRM";
            default:
                return defaultFlag ? "e.employeementId" : null;
        }
    }

    private String getCustomQuerySortBy(String sortColumn, boolean defaultFlag) {
        switch (sortColumn) {
            case "name":
                return "emp_name";
            case "employmentIdAcToET":
                return "employeement_id";
            case "billable":
                return "billable";
            case "billableType":
                return "billable_type";
            case "departmentName":
                return "department_name";
            case "projectName":
                return "project_name";
            case "teamName":
                return "team_name";
            case "employeeRole":
                return "employee_role";
            case "projectManagerName":
                return "project_manager_name";
            case "onbenchDate":
                return "onbench_datetime";
            case "dayOnbench":
                return "days_on_bench";
            case "managerName":
                return "manager_name";
            case "jobRoleName":
                return "job_role_name";
            case "apmosysRM":
                return "apmosysrm";
            case "clientRM":
                return "clientrm";
            case "poProjectType":
                return "po_project_type";
            case "poNo":
                return "po_no";
            case "clientName":
                return "client_name";
            case "effectiveStartDate":
                return "effective_start_date";
            case "email":
                return "email";
            case "poStartDate":
                return "po_start_date";
            case "poEndDate":
                return "po_end_date";
            default:
                return defaultFlag ? "employeement_id" : null;
        }
    }

    private String getNativeQuerySortBy(String sortColumn, boolean defaultFlag) {
        switch (sortColumn) {
            case "name":
                return "e.name";
            case "employmentIdAcToET":
                return "e.employeement_id";
            case "billable":
                return "e.billable";
            case "billableType":
                return "e.billable_type";
            case "departmentName":
                return "d.name";
            case "projectName":
                return "project_name";
            case "poStartDate":
                return "po_start_date";
            case "poEndDate":
                return "po_end_date";
            case "apmosysRM":
                return "apmosysrm";
            case "clientRM":
                return "clientrm";
            case "poProjectType":
                return "po_project_type";
            case "poNo":
                return "po_no";
            case "clientName":
                return "p.client_name";
            case "teamName":
                return "team_name";
            case "employeeRole":
                return "etm.employee_role";
            case "projectManagerName":
                return "pm.name";
            case "onbenchDate":
                return "onbench_datetime";
            case "dayOnbench":
                return "days_on_bench";
            case "jobRoleName":
                return "jr.name";
            case "email":
                return "e.email";
            default:
                return defaultFlag ? "e.employeement_id" : null;
        }
    }

    private void appenCustomSearchToQuery(Map<String, String> searchFilter, StringBuilder query) {
        if (searchFilter != null && !searchFilter.isEmpty()) {
            for (Map.Entry<String, String> entry : searchFilter.entrySet()) {
                String column = getSortBy(entry.getKey(), false);
                String value = entry.getValue();
                if (column != null && !column.trim().isEmpty() && value != null && !value.trim().isEmpty()) {
                    query.append(String.format(" AND LOWER(%s) LIKE '%%%s%%' \n", column,
                            value.replace("'", "''").toLowerCase()));
                }
            }
            query.append(" ");
        }
    }

    private void appenCustomSearchToNativeQuery(Map<String, String> searchFilter, StringBuilder query,
            boolean useCustomSort) {
        if (searchFilter != null && !searchFilter.isEmpty()) {
            for (Map.Entry<String, String> entry : searchFilter.entrySet()) {
                String column = useCustomSort ? getCustomQuerySortBy(entry.getKey(), false)
                        : getNativeQuerySortBy(entry.getKey(), false);
                String value = entry.getValue();
                if (column != null && !column.trim().isEmpty() && value != null && !value.trim().isEmpty()) {
                    query.append(String.format(" AND LOWER(%s) LIKE '%%%s%%' \n", column,
                            value.replace("'", "''").toLowerCase()));
                }
            }
            query.append(" ");
        }
    }

    private void appendPrefixToEmploymentId(List<EmployeeDetailsDTO> results) {
        for (EmployeeDetailsDTO dto : results) {
            String employmentId = dto.getEmployeementId() != null ? dto.getEmployeementId().toString() : null;
            if (employmentId == null)
                continue;
            String prefix = "A-";
            if ("true".equalsIgnoreCase(dto.getIsApmosysProduct())) {
                prefix = "AP-";
            }
            dto.setEmploymentIdAcToET(prefix + employmentId);
        }
    }

    public List<EmployeeDetailsDTO> groupEmployeesById(List<Object[]> rows) {
        List<EmployeeDetailsDTO> flatList = rows.stream()
                .map(EmployeeDetailsDTO::onBenchButProjectMapped)
                .collect(Collectors.toList());

        Map<Long, EmployeeDetailsDTO> employeeMap = new LinkedHashMap<>();

        for (EmployeeDetailsDTO dto : flatList) {
            EmployeeDetailsDTO employee = employeeMap.computeIfAbsent(dto.getEmpId(), id -> {
                EmployeeDetailsDTO e = new EmployeeDetailsDTO();
                e.setEmpId(dto.getEmpId());
                e.setEmploymentIdAcToET(dto.getEmploymentIdAcToET());
                e.setName(dto.getName());
                e.setDepartmentName(dto.getDepartmentName());
                e.setBillable(dto.getBillable());
                e.setBillableType(dto.getBillableType());

                // Set main row project info from the first project
                e.setProjectName(dto.getProjectName());
                e.setClientName(dto.getClientName());
                e.setApmosysRM(dto.getApmosysRM());
                e.setClientRM(dto.getClientRM());
                e.setPoNo(dto.getPoNo());
                e.setPoProjectType(dto.getPoProjectType());
                e.setPoStartDate(dto.getPoStartDate());
                e.setPoEndDate(dto.getPoEndDate());
                e.setExpandedRowDetails(new ArrayList<>());
                e.setProjectManagerName(dto.getProjectManagerName());
                e.setTeamName(dto.getTeamName());
                e.setEmployeeRole(dto.getEmployeeRole());
                e.setEffectiveStartDate(dto.getEffectiveStartDate());
                e.setJobRoleName(dto.getJobRoleName());
                e.setEmail(dto.getEmail());
                e.setMobileNo(dto.getMobileNo());
                return e;
            });

            // Only add to expandedRowDetails if it's NOT the first project
            if (dto.getProjectName() != null && employee.getProjectName() != null
                    && !dto.getProjectName().equals(employee.getProjectName())) {
                EmployeeDetailsDTO e = new EmployeeDetailsDTO();
                e.setEmpId(dto.getEmpId());
                e.setEmploymentIdAcToET(dto.getEmploymentIdAcToET());
                e.setName(dto.getName());
                e.setDepartmentName(dto.getDepartmentName());
                e.setBillable(dto.getBillable());
                e.setBillableType(dto.getBillableType());

                // Set main row project info from the first project
                e.setProjectName(dto.getProjectName());
                e.setClientName(dto.getClientName());
                e.setApmosysRM(dto.getApmosysRM());
                e.setClientRM(dto.getClientRM());
                e.setPoNo(dto.getPoNo());
                e.setPoProjectType(dto.getPoProjectType());
                e.setPoStartDate(dto.getPoStartDate());
                e.setPoEndDate(dto.getPoEndDate());
                e.setProjectManagerName(dto.getProjectManagerName());
                e.setTeamName(dto.getTeamName());
                e.setEmployeeRole(dto.getEmployeeRole());
                e.setEmployeeRole(dto.getEmployeeRole());
                e.setEffectiveStartDate(dto.getEffectiveStartDate());
                e.setJobRoleName(dto.getJobRoleName());
                e.setEmail(dto.getEmail());
                e.setMobileNo(dto.getMobileNo());
                employee.getExpandedRowDetails().add(e);
            }
        }
        employeeMap.values().forEach(e -> e.setExpandableRow(e.getExpandedRowDetails().size() > 0));
        return new ArrayList<>(employeeMap.values());
    }

    public List<EmployeeDetailsDTO> groupEmployeesUnfilledTimesheetProjectEmployees(List<Object[]> rows) {
        List<EmployeeDetailsDTO> flatList = rows.stream()
                .map(EmployeeDetailsDTO::unfilledTimesheet)
                .collect(Collectors.toList());

        Map<String, EmployeeDetailsDTO> employeeMap = new LinkedHashMap<>();

        for (EmployeeDetailsDTO dto : flatList) {
            EmployeeDetailsDTO employee = employeeMap.computeIfAbsent(dto.getProjectName(), id -> {
                EmployeeDetailsDTO e = new EmployeeDetailsDTO();
                e.setEmpId(dto.getEmpId());
                e.setEmploymentIdAcToET(dto.getEmploymentIdAcToET());
                e.setName(dto.getName());
                e.setDepartmentName(dto.getDepartmentName());
                e.setBillable(dto.getBillable());
                e.setBillableType(dto.getBillableType());
                e.setProjectName(dto.getProjectName());
                e.setClientName(dto.getClientName());
                e.setApmosysRM(dto.getApmosysRM());
                e.setClientRM(dto.getClientRM());
                e.setPoNo(dto.getPoNo());
                e.setPoProjectType(dto.getPoProjectType());
                e.setPoStartDate(dto.getPoStartDate());
                e.setPoEndDate(dto.getPoEndDate());
                e.setExpandedRowDetails(new ArrayList<>());
                e.setProjectManagerName(dto.getProjectManagerName());
                e.setTeamName(dto.getTeamName());
                e.setEmployeeRole(dto.getEmployeeRole());
                e.setEffectiveStartDate(dto.getEffectiveStartDate());
                e.setJobRoleName(dto.getJobRoleName());
                e.setEmail(dto.getEmail());
                e.setMobileNo(dto.getMobileNo());
                return e;
            });

            if (dto.getName() != null && employee.getName() != null
                    && !dto.getName().equals(employee.getName())) {

                EmployeeDetailsDTO e = new EmployeeDetailsDTO();
                e.setEmpId(dto.getEmpId());
                e.setEmploymentIdAcToET(dto.getEmploymentIdAcToET());
                e.setName(dto.getName());
                e.setDepartmentName(dto.getDepartmentName());
                e.setBillable(dto.getBillable());
                e.setBillableType(dto.getBillableType());
                e.setProjectName(dto.getProjectName());
                e.setClientName(dto.getClientName());
                e.setApmosysRM(dto.getApmosysRM());
                e.setClientRM(dto.getClientRM());
                e.setPoNo(dto.getPoNo());
                e.setPoProjectType(dto.getPoProjectType());
                e.setPoStartDate(dto.getPoStartDate());
                e.setPoEndDate(dto.getPoEndDate());
                e.setProjectManagerName(dto.getProjectManagerName());
                e.setTeamName(dto.getTeamName());
                e.setEmployeeRole(dto.getEmployeeRole());
                e.setEmployeeRole(dto.getEmployeeRole());
                e.setEffectiveStartDate(dto.getEffectiveStartDate());
                e.setJobRoleName(dto.getJobRoleName());
                e.setEmail(dto.getEmail());
                e.setMobileNo(dto.getMobileNo());
                employee.getExpandedRowDetails().add(e);
            }
        }
        employeeMap.values().forEach(e -> e.setExpandableRow(e.getExpandedRowDetails().size() > 0));
        return new ArrayList<>(employeeMap.values());
    }

    public Long getMappedToShankhEmployeeDetailsCount(boolean isAllAccessEmployee, List<Long> deptIds,
            Set<Integer> projectIds, String projectStatus, String expiredProjectTimeFrameFilter) {

        Long total = 0l;

        boolean addStartAndEndDate = false;
        String startDate = null;
        String endDate = null;

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
        String baseQuery = getMappedToShankhEmployeeDetailsQuery(isAllAccessEmployee, null, projectStatus,
                addStartAndEndDate);

        try (Session session = entityManager.unwrap(Session.class)) {
            String countQuery = "SELECT COUNT(DISTINCT e.emp_id) " + baseQuery;
            NativeQuery<?> countNative = session.createNativeQuery(countQuery);
            countNative.setParameterList("projectIds", projectIds);
//            if (!isAllAccessEmployee) {
                countNative.setParameter("deptIds", deptIds);
//            }
            if (projectStatus != null && projectStatus.equals("TOTAL_EXPIRED_TNM") && addStartAndEndDate) {
                countNative.setParameter("startDate", startDate);
                countNative.setParameter("endDate", endDate);
            }
            total = ((Number) countNative.getSingleResult()).longValue();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return total;
    }

    public Long getMappedToInternalAndShankhEmployeeDetailsCount(boolean isAllAccessEmployee, List<Long> deptIds,
            Set<Integer> projectIds, String projectStatus, String expiredProjectTimeFrameFilter) {

        Long total = 0l;

        boolean addStartAndEndDate = false;
        String startDate = null;
        String endDate = null;

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
        String baseQuery = getMappedToInternalAndShankhEmployeeDetailsQuery(isAllAccessEmployee, null, projectStatus,
                addStartAndEndDate);
        try (Session session = entityManager.unwrap(Session.class)) {
            String countQuery = "SELECT COUNT(DISTINCT e.emp_id) " + baseQuery;
            NativeQuery<?> countNative = session.createNativeQuery(countQuery);
            countNative.setParameterList("projectIds", projectIds);
//            if (!isAllAccessEmployee) {
                countNative.setParameter("deptIds", deptIds);
//            }
            if (projectStatus != null && projectStatus.equals("TOTAL_EXPIRED_TNM") && addStartAndEndDate) {
                countNative.setParameter("startDate", startDate);
                countNative.setParameter("endDate", endDate);
            }
            total = ((Number) countNative.getSingleResult()).longValue();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return total;
    }

    public Long getUnfilledTimesheetProjectDetailsCount(boolean isAllAccessEmployee, List<Long> deptIds,
            Set<Integer> projectIds, LocalDate fromDate, LocalDate toDate) {
        String baseQuery = getUnfilledTimesheetProjectDetailsQuery(isAllAccessEmployee, fromDate, toDate);
        Long total = 0l;
        try (Session session = entityManager.unwrap(Session.class)) {
            StringBuilder countQuery = new StringBuilder("SELECT COUNT(DISTINCT p.project_id) ").append(baseQuery);
            NativeQuery<?> countNative = session.createNativeQuery(countQuery.toString());
            countNative.setParameterList("projectIds", projectIds);
//            if (!isAllAccessEmployee) {
                countNative.setParameter("deptIds", deptIds);
//            }
            if (fromDate != null && toDate != null) {
                countNative.setParameter("fromDate", fromDate);
                countNative.setParameter("toDate", toDate);
            }
            total = ((Number) countNative.getSingleResult()).longValue();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return total;
    }
    
    
 // Add this new method alongside getUnfilledTimesheetProjectDetailsCount
    public Map<String, Long> getUnfilledTimesheetProjectDetailsCountByProjectType(
            boolean isAllAccessEmployee, List<Long> deptIds,
            Set<Integer> projectIds, LocalDate fromDate, LocalDate toDate) {

        String baseQuery = getUnfilledTimesheetProjectDetailsQuery(isAllAccessEmployee, fromDate, toDate);
        Map<String, Long> result = new LinkedHashMap<>();

        // Project type conditions
        Map<String, String> projectTypeConditions = new LinkedHashMap<>();
        projectTypeConditions.put("TNM",           "p.po_project_type = 'TNM'");
        projectTypeConditions.put("Fixed Cost",    "p.po_project_type = 'Fixed Cost'");
        projectTypeConditions.put("Monitoring",    "p.po_project_type = 'Monitoring'");
        projectTypeConditions.put("Internal",      "p.po_project_type IS NULL AND p.internal_project_type IS NOT NULL");

        try (Session session = entityManager.unwrap(Session.class)) {
            for (Map.Entry<String, String> entry : projectTypeConditions.entrySet()) {
                // Wrap the base query with an additional project-type filter
                StringBuilder countQuery = new StringBuilder("SELECT COUNT(DISTINCT p.project_id) ")
                        .append(baseQuery)
                        .append(" AND (").append(entry.getValue()).append(")");

                NativeQuery<?> countNative = session.createNativeQuery(countQuery.toString());
                countNative.setParameterList("projectIds", projectIds);
                countNative.setParameter("deptIds", deptIds);
                if (fromDate != null && toDate != null) {
                    countNative.setParameter("fromDate", fromDate);
                    countNative.setParameter("toDate", toDate);
                }
                Long count = ((Number) countNative.getSingleResult()).longValue();
                result.put(entry.getKey(), count);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return result;
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

    public Slice<EmployeeDetailsDTO> getFutureStartDateAssignedEmployeeDetailsPage(boolean isAllAccessEmployee,
            PageDTO pageDTO, List<Long> deptIds, Set<Integer> projectIds) {

        String sortBy = getCustomQuerySortBy(pageDTO.getSortColumn(), true);
        String sortDirection = pageDTO.getSortDirection();
        Pageable page = PageRequest.of(pageDTO.getPage(), pageDTO.getSize(),
                Direction.fromString(sortDirection), sortBy);
        Map<String, String> searchFilter = pageDTO.getSearchFilter();

        String baseQuery = getFutureStartDateAssignedEmployeeDetailsQuery(isAllAccessEmployee, searchFilter);

        StringBuilder query = new StringBuilder(
                "SELECT * FROM " + baseQuery + String.format(" ORDER BY %s %s ", sortBy, sortDirection));

        System.out.println("FUTURE_START_DATE_ASSIGNED ================================= query");
        System.out.println(query.toString());

        Long total = 0l;
        List<EmployeeDetailsDTO> results = new ArrayList<>();
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<Object[]> projectQuery = session.createNativeQuery(query.toString());
            projectQuery.setParameterList("deptIds", deptIds);
            int offset = page.getPageNumber() * page.getPageSize();
            projectQuery.setFirstResult(offset);
            projectQuery.setMaxResults(page.getPageSize());

            results = projectQuery.getResultList().stream()
                    .map(EmployeeDetailsDTO::futureStartDateAssigned)
                    .collect(Collectors.toList());

            String countQuery = "SELECT COUNT(DISTINCT emp_id) FROM " + baseQuery;
            NativeQuery<?> countNative = session.createNativeQuery(countQuery);
            countNative.setParameterList("deptIds", deptIds);
            total = ((Number) countNative.getSingleResult()).longValue();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return new PageImpl<>(results, page, total);
    }

}
