package com.apmosys.employeeportal.customRepository;

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
import com.apmosys.employeeportal.dto.ProjectSummaryDTO;

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

        TypedQuery<EmployeeDetailsDTO> dataQuery = entityManager.createQuery(listQuery.toString(),
                EmployeeDetailsDTO.class);

        if (!isAllAccessEmployee) {
            dataQuery.setParameter("deptIds", deptIds);
        }
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        List<EmployeeDetailsDTO> results = dataQuery.getResultList();
        appendPrefixToEmploymentId(results);

        String countQueryStr = new String("SELECT COUNT(DISTINCT e.empId) " + baseQuery);
        TypedQuery<Long> countQuery = entityManager.createQuery(countQueryStr, Long.class);
        if (!isAllAccessEmployee) {
            countQuery.setParameter("deptIds", deptIds);
        }
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
                deptIds, null);
        StringBuilder listQuery = new StringBuilder(
                " SELECT DISTINCT e.emp_id,CASE WHEN e.is_apmosys_product = 'true' then CONCAT('AP-', e.employeement_id) else CONCAT('A-', e.employeement_id) end as employeement_id,e.name,d.name as department_name,e.billable,e.billable_type,p.project_name,p.client_name,p.apmosysrm,p.clientrm,p.po_No,p.po_project_type,p.po_start_date,p.po_end_date \n")
                .append(baseQuery);

        if (empIds != null && !empIds.isEmpty()) {
            listQuery.append("AND e.emp_id IN :empIds ");
        }
        listQuery.append("" + String.format(" ORDER BY %s %s ", sortBy, sortDirection));

        Long total = 0l;
        List<EmployeeDetailsDTO> results = new ArrayList<>();
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<Object[]> projectQuery = session.createNativeQuery(listQuery.toString());
            if (!isAllAccessEmployee) {
                projectQuery.setParameterList("deptIds", deptIds);
            }
            if (empIds != null && !empIds.isEmpty()) {
                projectQuery.setParameterList("empIds", empIds);
            }

            results = groupEmployeesById(projectQuery.getResultList());
            String countQuery = "SELECT COUNT(DISTINCT e.emp_id) " + baseQuery;
            NativeQuery<?> countNative = session.createNativeQuery(countQuery);
            if (!isAllAccessEmployee) {
                countNative.setParameterList("deptIds", deptIds);
            }
            total = ((Number) countNative.getSingleResult()).longValue();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return new PageImpl<>(results, pageable, total);
    }

    public Slice<EmployeeDetailsDTO> getMappedToShankhEmployeeDetailsPage(boolean isAllAccessEmployee,
            PageDTO pageDTO, List<Long> deptIds, Set<Integer> projectIds) {
        String sortBy = getNativeQuerySortBy(pageDTO.getSortColumn(), true);
        String sortDirection = pageDTO.getSortDirection();
        Pageable pageable = PageRequest.of(pageDTO.getPage(), pageDTO.getSize(),
                Direction.fromString(sortDirection), sortBy);
        Map<String, String> searchFilter = pageDTO.getSearchFilter();

        String baseQuery = getMappedToShankhEmployeeDetailsQuery(isAllAccessEmployee, searchFilter);

        List<Long> empIds = getEmployeeIdsByBaseQuery(baseQuery, isAllAccessEmployee, sortBy, sortDirection, pageable,
                deptIds, projectIds);

        StringBuilder listQuery = new StringBuilder();
        listQuery.append(
                "SELECT DISTINCT e.emp_id,CASE WHEN e.is_apmosys_product = 'true' then CONCAT('AP-', e.employeement_id) else CONCAT('A-', e.employeement_id) end as employeement_id \n")
                .append(",e.name,d.name as department_name,e.billable,e.billable_type \n")
                .append(",p.project_name,p.client_name as client_name,p.apmosysrm,p.clientrm,p.po_No,p.po_project_type,p.po_start_date,p.po_end_date  \n")
                .append(",pm.name as project_manager_name,t.team_name,etm.employee_role \n")
                .append(baseQuery);

        if (empIds != null && !empIds.isEmpty()) {
            listQuery.append("AND e.emp_id IN :empIds ");
        }
        listQuery.append("" + String.format(" ORDER BY %s %s ", sortBy, sortDirection));

        Long total = 0l;
        List<EmployeeDetailsDTO> results = new ArrayList<>();
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<Object[]> projectQuery = session.createNativeQuery(listQuery.toString());
            if (empIds != null && !empIds.isEmpty()) {
                projectQuery.setParameterList("empIds", empIds);
            }
            projectQuery.setParameterList("projectIds", projectIds);
            results = groupEmployeesById(projectQuery.getResultList());

            String countQuery = "SELECT COUNT(DISTINCT e.emp_id) " + baseQuery;
            NativeQuery<?> countNative = session.createNativeQuery(countQuery);
            countNative.setParameterList("projectIds", projectIds);
            total = ((Number) countNative.getSingleResult()).longValue();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return new PageImpl<>(results, pageable, total);
    }

    public Slice<EmployeeDetailsDTO> getMappedToInternalAndShankhEmployeeDetailsPage(boolean isAllAccessEmployee,
            PageDTO pageDTO, List<Long> deptIds, Set<Integer> projectIds) {
        String sortBy = getNativeQuerySortBy(pageDTO.getSortColumn(), true);
        String sortDirection = pageDTO.getSortDirection();
        Pageable pageable = PageRequest.of(pageDTO.getPage(), pageDTO.getSize(),
                Direction.fromString(sortDirection), sortBy);
        Map<String, String> searchFilter = pageDTO.getSearchFilter();

        String baseQuery = getMappedToInternalAndShankhEmployeeDetailsQuery(isAllAccessEmployee, searchFilter);
        List<Long> empIds = getEmployeeIdsByBaseQuery(baseQuery, isAllAccessEmployee, sortBy, sortDirection, pageable,
                deptIds, projectIds);

        StringBuilder listQuery = new StringBuilder();
        listQuery.append(
                "SELECT DISTINCT e.emp_id,CASE WHEN e.is_apmosys_product = 'true' then CONCAT('AP-', e.employeement_id) else CONCAT('A-', e.employeement_id) end as employeement_id \n")
                .append(",e.name,d.name as department_name,e.billable,e.billable_type \n")
                .append(",p.project_name,p.client_name as client_name,p.apmosysrm,p.clientrm,p.po_No,p.po_project_type,p.po_start_date,p.po_end_date  \n")
                .append(",pm.name as project_manager_name,t.team_name,etm.employee_role \n")
                .append(baseQuery);

        if (empIds != null && !empIds.isEmpty()) {
            listQuery.append("AND e.emp_id IN :empIds ");
        }
        listQuery.append("" + String.format(" ORDER BY %s %s ", sortBy, sortDirection));

        Long total = 0l;
        List<EmployeeDetailsDTO> results = new ArrayList<>();

        try (Session session = entityManager.unwrap(Session.class)) {

            NativeQuery<Object[]> projectQuery = session.createNativeQuery(listQuery.toString());
            if (empIds != null && !empIds.isEmpty()) {
                projectQuery.setParameterList("empIds", empIds);
            }
            projectQuery.setParameterList("projectIds", projectIds);
            results = groupEmployeesById(projectQuery.getResultList());

            String countQuery = "SELECT COUNT(DISTINCT e.emp_id) " + baseQuery;
            NativeQuery<?> countNative = session.createNativeQuery(countQuery);
            countNative.setParameterList("projectIds", projectIds);
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

        Long total = 0l;
        List<EmployeeDetailsDTO> results = new ArrayList<>();
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<Object[]> projectQuery = session.createNativeQuery(query.toString());
            if (!isAllAccessEmployee) {
                projectQuery.setParameterList("deptIds", deptIds);
            }
            int offset = page.getPageNumber() * page.getPageSize();
            projectQuery.setFirstResult(offset);
            projectQuery.setMaxResults(page.getPageSize());

            results = projectQuery.getResultList().stream()
                    .map(EmployeeDetailsDTO::withoutBillability)
                    .collect(Collectors.toList());

            String countQuery = "SELECT COUNT(DISTINCT emp_id) FROM " + baseQuery;
            NativeQuery<?> countNative = session.createNativeQuery(countQuery);
            if (!isAllAccessEmployee) {
                countNative.setParameterList("deptIds", deptIds);
            }
            total = ((Number) countNative.getSingleResult()).longValue();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return new PageImpl<>(results, page, total);
    }

    private List<EmployeeDetailsDTO> getResultList(boolean isAllAccessEmployee, String query, String sortBy,
            String sortDirection, List<Long> deptIds, Pageable page, boolean deptFlag) {
        query = "SELECT * FROM " + query;
        System.out.println("================================= query");
        System.out.println(query);
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<Object[]> nativeQuery = session.createNativeQuery(query)
                    .unwrap(org.hibernate.query.NativeQuery.class);
            if (!isAllAccessEmployee) {
                nativeQuery.setParameter("deptIds", deptIds);
            }

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

            if (!isAllAccessEmployee) {
                nativeQuery.setParameter("deptIds", deptIds);
            }
            Object count = nativeQuery.getSingleResult();
            return count == null ? 0 : Long.parseLong(count.toString());

        } catch (Exception e) {
            throw e;
        }
    }

    public List<Long> getEmployeeIdsByBaseQuery(String baseQuery, boolean isAllAccessEmployee, String sortBy,
            String sortDirection, Pageable pageable, List<Long> deptIds, Set<Integer> projectIds) {
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

        List<Long> empIds;
        try (Session session = entityManager.unwrap(Session.class)) {
            NativeQuery<?> nativeQuery = session.createNativeQuery(query.toString());
            if (!isAllAccessEmployee) {
                nativeQuery.setParameterList("deptIds", deptIds);
            }
            if (projectIds != null) {
                nativeQuery.setParameter("projectIds", projectIds);
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
                .append("                  WHERE etm.empId = e.empId AND etm.active != 0 AND t.isActive = 'Y' AND p.active = 'true') \n")
                .append(" and e.employmentstatus != 'InActive' and e.empId NOT BETWEEN 1 AND 6");

        if (!isAllAccessEmployee) {
            query.append("and d.deptId IN :deptIds ");
        }
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
                .append(",p.project_name,pm.name as project_manager_name, t.team_name, etm.employee_role \n")
                .append("FROM employee_team_mapping etm \n")
                .append("INNER JOIN employee e ON e.emp_id = etm.emp_id \n")
                .append("INNER JOIN job_role jr ON e.job_role_id = jr.job_role_id \n")
                .append("INNER JOIN department d ON jr.dept_id = d.dept_id \n")
                .append("INNER JOIN teams t ON etm.team_id = t.team_id \n")
                .append("INNER JOIN projects p ON t.project_id = p.project_id \n")
                .append("INNER JOIN clients c ON p.client_id = c.client_id \n")
                .append("INNER JOIN project_manager_mapping pmm ON pmm.project_id = p.project_id \n")
                .append("INNER JOIN employee pm ON pm.emp_id = pmm.project_manager_id \n")
                .append("WHERE 1=1 \n")
                .append("AND etm.active != 0 \n")
                .append("AND internal_project_type = 'Bench' \n")
                .append("AND t.is_active = 'Y' \n")
                .append("AND p.active = 'true' \n")
                .append("AND DATEDIFF(CURDATE(), etm.start_date) > 30 \n")
                .append("AND e.employmentstatus != 'InActive' \n")
                .append("AND e.billable_type = 'Bench' \n");

        if (isAllAccessEmployee) {
            query.append(" AND pmm.active = 1 \n");
        } else {
            query.append(" AND d.dept_id IN :deptIds \n");
            if (deptFlag) {
                query.append(" AND pmm.active = 1 \n");
            }
        }
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
                .append("AND e.billable_type = 'Bench' AND (p.po_project_type like 'FIXED%COST' OR p.po_project_type like '%TNM%') \n")
                .append("AND e.emp_id NOT BETWEEN 1 AND 6 \n");

        if (!isAllAccessEmployee) {
            query.append(" AND d.dept_Id IN :deptIds ");
        }
        appenCustomSearchToNativeQuery(searchFilter, query, false);
        return query.toString();
    }

    public String getMappedToShankhEmployeeDetailsQuery(boolean isAllAccessEmployee, Map<String, String> searchFilter) {
        StringBuilder query = new StringBuilder();
        query.append("FROM employee_team_mapping etm \n")
                .append("RIGHT JOIN employee e ON e.emp_id = etm.emp_id  \n")
                .append("RIGHT JOIN teams t ON t.team_id = etm.team_id  \n")
                .append("INNER JOIN projects p ON p.project_id = t.project_id  \n")
                .append("INNER JOIN clients c ON c.client_id = p.client_id  \n")
                .append("INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id  \n")
                .append("INNER JOIN department d ON d.dept_id = jr.dept_id  \n")
                .append("LEFT JOIN  project_manager_mapping pmm ON pmm.project_id = p.project_id  \n")
                .append("LEFT JOIN  employee pm ON pm.emp_id = pmm.project_manager_id \n")
                .append("WHERE p.project_id IN :projectIds  \n")
                .append("AND etm.active != 0  \n")
                .append("AND t.is_active = 'Y'  \n")
                .append("AND e.employmentstatus != 'InActive'  \n")
                .append("AND pmm.active = 1 AND e.emp_id NOT BETWEEN 1 AND  6 \n");
        appenCustomSearchToNativeQuery(searchFilter, query, false);
        return query.toString();
    }

    public String getMappedToInternalAndShankhEmployeeDetailsQuery(boolean isAllAccessEmployee,
            Map<String, String> searchFilter) {
        StringBuilder query = new StringBuilder();
        query.append("FROM employee_team_mapping etm  \n")
                .append("RIGHT JOIN employee e ON e.emp_id = etm.emp_id  \n")
                .append("RIGHT JOIN teams t ON t.team_id = etm.team_id  \n")
                .append("INNER JOIN projects p ON p.project_id = t.project_id  \n")
                .append("INNER JOIN clients c ON c.client_id = p.client_id  \n")
                .append("INNER JOIN job_role jr ON jr.job_role_id = e.job_role_id  \n")
                .append("INNER JOIN department d ON d.dept_id = jr.dept_id  \n")
                .append("LEFT JOIN project_manager_mapping pmm ON pmm.project_id = p.project_id  \n")
                .append("LEFT JOIN employee pm ON pm.emp_id = pmm.project_manager_id  \n")
                .append("WHERE 1=1  \n")
                .append("AND e.emp_id IN ( \n")
                .append("SELECT e1.emp_id  \n")
                .append("FROM employee_team_mapping etm1  \n")
                .append("JOIN employee e1 ON e1.emp_id = etm1.emp_id  \n")
                .append("JOIN teams t1 ON t1.team_id = etm1.team_id  \n")
                .append("JOIN projects p1 ON p1.project_id = t1.project_id  \n")
                .append("JOIN project_manager_mapping pmm1 ON pmm1.project_id = p1.project_id  \n")
                .append("WHERE etm1.active != 0 AND t1.is_active = 'Y' AND e1.employmentstatus != 'InActive'  \n")
                .append("AND pmm1.active = 1 AND p1.project_id IN :projectIds  \n")
                .append("GROUP BY e1.emp_id  \n")
                .append("HAVING COUNT(CASE WHEN p1.po_project_id IS NULL THEN 1 END) > 0  \n")
                .append("AND COUNT(CASE WHEN p1.po_project_id IS NOT NULL THEN 1 END) > 0  \n")
                .append(" ) \n")
                .append("AND etm.active != 0  \n")
                .append("AND t.is_active = 'Y'  \n")
                .append("AND e.employmentstatus != 'InActive'  \n")
                .append("AND pmm.active = 1  \n")
                .append("AND e.emp_id NOT BETWEEN 1 AND 6 \n");
        appenCustomSearchToNativeQuery(searchFilter, query, true);
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
                .append("AND e.billable_type IS NULL  \n")
                .append("AND e.emp_id NOT BETWEEN 1 AND 6  \n")
                .append("AND e.employmentstatus != 'InActive' \n");

        if (!isAllAccessEmployee) {
            query.append(" AND d.dept_id IN :deptIds \n");
        }
        query.append(" ) as T1 \n WHERE 1=1 \n");
        appenCustomSearchToNativeQuery(searchFilter, query, true);
        return query.toString();
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
                return "job_role_name";
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
                return e;
            });

            // Only add to expandedRowDetails if it's NOT the first project
            if (!dto.getProjectName().equals(employee.getProjectName())) {
                employee.getExpandedRowDetails().add(
                        new ProjectSummaryDTO(
                                dto.getProjectName(),
                                dto.getClientName(),
                                dto.getApmosysRM(),
                                dto.getClientRM(),
                                dto.getPoNo(),
                                dto.getPoProjectType(),
                                dto.getPoStartDate(),
                                dto.getPoEndDate(),
                                dto.getProjectManagerName(),
                                dto.getTeamName()));
            }
        }
        employeeMap.values().forEach(e -> e.setExpandableRow(e.getExpandedRowDetails().size() > 0));
        return new ArrayList<>(employeeMap.values());
    }

}
