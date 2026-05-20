package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.dto.GetDeptIdByRoleDTO;
import com.apmosys.employeeportal.model.EmployeeAccessOverride;

public interface EmployeeAccessOverrideRepository  extends JpaRepository<EmployeeAccessOverride, Long>  {

    @Query(value = " select sub.sub_feature_master_id,sub.sub_feature_name, \n"
            + " fm.feature_id,fm.feature_name, \n"
            + " tab.tab_name,tab.tab_icon,tab.tab_route_name \n"
            + " From employee_access_override eao \n"
            + " INNER JOIN sub_feature_master sub ON eao.sub_feature_master_id = sub.sub_feature_master_id \n"
            + " INNER JOIN feature_master fm ON fm.feature_id = sub.feature_id \n"
            + " INNER JOIN tab_master tab ON tab.tab_id = fm.tab_id \n"
            + " WHERE eao.emp_id = :empId and eao.is_active = true \n"
            + " order by tab.tab_sequence\n", nativeQuery = true)
    public List<Object[]> findActiveTabRowsByEmpId(Long empId);

    @Query(value = " select distinct fm.feature_id \n"
            + " From employee_access_override eao \n"
            + " INNER JOIN sub_feature_master sub ON eao.sub_feature_master_id = sub.sub_feature_master_id \n"
            + " INNER JOIN feature_master fm ON fm.feature_id = sub.feature_id \n"
            + " WHERE eao.emp_id = :empId and eao.is_active = true \n", nativeQuery = true)
    public List<Long> findActiveFeatureIdsByEmpId(@Param("empId") Long empId);

    @Query(value = " select distinct eaod.dept_id \n"
                    + " From employee_access_override eao \n"
                    + " INNER JOIN employee_access_override_department eaod on eao.override_id = eaod.override_id \n"
                    + " WHERE eao.emp_id = :empId and eao.is_active = true and eaod.is_active = true and eaod.sub_feature_name  = :subFeatureName \n", nativeQuery = true)
    public List<Long> findActiveDeptIdsByEmpId(Long empId, String subFeatureName);

    @Query(value = "select new com.apmosys.employeeportal.dto.GetDeptIdByRoleDTO(d.deptId, d.name)  \n"
                    + " From EmployeeAccessOverride eao \n"
                    + " INNER JOIN EmployeeAccessOverrideDepartment eaod on eao.overrideId = eaod.overrideId \n"
                    + " INNER JOIN Department d on d.deptId = eaod.deptId \n"
                    + " WHERE eao.empId = :empId and eao.isActive = true and eaod.isActive = true and eaod.subFeatureName  = :subFeatureName \n")
    public List<GetDeptIdByRoleDTO> findActiveDeptIdsAndSubFeatureNameByEmpId(Long empId, String subFeatureName);
    
}
