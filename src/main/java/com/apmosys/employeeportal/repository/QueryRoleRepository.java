package com.apmosys.employeeportal.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.apmosys.employeeportal.model.CustomQueryRoles;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface QueryRoleRepository extends JpaRepository<CustomQueryRoles, Long> {

    List<CustomQueryRoles> findByQueryId(Long queryId);

    @Transactional
    @Modifying
    void deleteByQueryId(Long queryId);
}
