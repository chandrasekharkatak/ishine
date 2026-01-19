package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.PoDetailsDto;
import com.apmosys.employeeportal.model.PoDetails;

@Repository
public interface PoDetailsRepository extends JpaRepository<PoDetails, Long> {

    @Query(value = "Select new com.apmosys.employeeportal.dto.PoDetailsDto(p.id, p.poId, p.projectId, p.poNo, p.poStartDate, p.poEndDate, p.active) from PoDetails p where p.projectId=:projectId and p.active=true")
    List<PoDetailsDto> getActivePoDetailsDtoByProjectId(Integer projectId);

}
