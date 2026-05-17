package com.apmosys.employeeportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apmosys.employeeportal.model.DocMimeTypeMasterNew;

public interface DocMimeTypeMasterNewRepository extends JpaRepository<DocMimeTypeMasterNew, Integer>{

    @Query("SELECT id FROM DocMimeTypeMasterNew WHERE mimeType = :mimeType")
    Optional<Integer> findByMimeType(@Param("mimeType") String mimeType);
    
}
