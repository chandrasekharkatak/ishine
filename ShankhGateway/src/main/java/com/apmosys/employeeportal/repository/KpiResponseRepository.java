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
    List<KpiResponse> getResponsesByEmpIdAndQuarterId(Long empId, Long quarterId);

    List<KpiResponse> findByQuarter(String quarter);

    List<KpiResponse> findByQuarterAndDepartmentName(String quarter, String departmentName);

    
    @Query(nativeQuery = true, value = 
            "INSERT INTO kpi_response (emp_id, department_name) " +
            "SELECT us.emp_id, d.name " +
            "FROM user_session us " +
            "JOIN department d ON d.hod_id = us.emp_id " +
            "WHERE us.user_session_id = :sessionId " +
            "RETURNING id")
     Long createKpiResponseFromUserSession(@Param("sessionId") Long sessionId);
    
    @Query(nativeQuery = true,value ="SELECT k.score FROM kpi_response k WHERE k.emp_id = :emp_id AND k.quarter_id = :quarter_id")
    public Float calculateTotalScoreByEmpIdAndQuarter(@Param("emp_id") Long empId, @Param("quarter_id") Long quarterId);

    
    
}