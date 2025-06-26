package com.apmosys.employeeportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apmosys.employeeportal.model.FCLineItem;

public interface FCLineItemRepository extends JpaRepository<FCLineItem, Long> {

    List<FCLineItem> findByProjectId(Long projectId);
    
    Optional<FCLineItem>  findByid(Long id);

}
