package com.apmosys.employeeportal.repository;

import com.apmosys.employeeportal.model.KpiResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KpiResponseRepository extends JpaRepository<KpiResponse, Long> {
    List<KpiResponse> findByEmpId(Long empId);
    List<KpiResponse> findByKpiId(Long kpiId);
    List<KpiResponse> findByQuarterId(Long quarterId);
    List<KpiResponse> findByDepartmentId(Long departmentId);
    
    @Query(nativeQuery = true, value = 
            "INSERT INTO kpi_responses (emp_id, department_name) " +
            "SELECT us.emp_id, d.name " +
            "FROM user_session us " +
            "JOIN department d ON d.hod_id = us.emp_id " +
            "WHERE us.user_session_id = :sessionId " +
            "RETURNING id")
     Long createKpiResponseFromUserSession(@Param("sessionId") Long sessionId);
}