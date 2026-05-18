package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.CustomQuery;

public interface CustomQueryRepository extends JpaRepository<CustomQuery, Long> {

	@Query(value = "SELECT cq.* FROM custom_queries cq " +
			" JOIN custom_query_roles qr ON cq.query_id = qr.query_id " +
			" join employee emp on emp.job_role_id = qr.role_id " +
			" WHERE emp.emp_id =:empID AND cq.status = 1 ", nativeQuery = true)
	List<CustomQuery> findActiveQueriesByRoleId(@Param("empID") Long roleId);

	@Query(value = "SELECT cq.query_id, cq.query_name, cq.query_sql, cq.description, cq.status, " +
					" qr_all.role_id FROM custom_queries cq " +
					" JOIN custom_query_roles qr_filter ON cq.query_id = qr_filter.query_id " +
					" JOIN employee emp ON emp.job_role_id = qr_filter.role_id " +
					" JOIN custom_query_roles qr_all ON cq.query_id = qr_all.query_id " +
					" WHERE emp.emp_id = :empID AND cq.status = 1 ORDER BY cq.query_id DESC", nativeQuery = true)
	List<Object[]> findActiveQueriesByEmpId(@Param("empID") Long empID);

	Optional<CustomQuery> findByQueryNameAndQuerySql(String queryName, String querySql);
}
