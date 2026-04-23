package com.apmosys.employeeportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.ClientStatusMasterNew;

public interface ClientStatusMasterNewRepository extends JpaRepository<ClientStatusMasterNew, Integer>{

    @Query("SELECT csm.id FROM ClientStatusMasterNew csm WHERE LOWER(csm.status) = LOWER(:status)")
    Optional<Integer> findClientStatusIdByClientStatusName(@Param("status") String status);

    @Query("SELECT csm.status FROM ClientStatusMasterNew csm WHERE csm.id = :clientStatusId")
    Optional<String> findClientStatusNameByClientStatusId(@Param("clientStatusId") Integer clientStatusId);
    
    
}
