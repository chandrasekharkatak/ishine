package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.PoDetailsDto;
import com.apmosys.employeeportal.model.ProjectPoDetails;

@Repository
public interface ProjectPoDetailsRepository extends JpaRepository<ProjectPoDetails, Long> {

    @Query(value = "Select new com.apmosys.employeeportal.dto.PoDetailsDto(p.id, p.poId, p.projectId, p.poNo, p.poStartDate, p.poEndDate, p.active) \n"
            + " from ProjectPoDetails p where p.projectId=:projectId and p.active=true")
    List<PoDetailsDto> getActivePoDetailsDtoByProjectId(Integer projectId);

    @Query(value = "Select DISTINCT new com.apmosys.employeeportal.dto.PoDetailsDto(p.id, p.poId, p.projectId, p.poNo, p.poStartDate, p.poEndDate, p.active \n"
            + ",COUNT(DISTINCT CASE WHEN etm.active  = 1 THEN etm.empId END)  \n"
            + ",COUNT(DISTINCT CASE WHEN etm.active  = 2 THEN etm.empId END) \n"
            + ") \n"
            + "FROM ProjectPoDetails p  \n"
            + "INNER JOIN PoRequirementMapping prm ON p.poId=prm.poId \n"
            + "INNER JOIN EmployeeTeamMap etm ON etm.poRequirementMappingId = prm.poRequirementMappingId   \n"
            + "INNER JOIN Team t ON t.teamId = etm.teamId  \n"
            + "where p.projectId=:projectId \n"
            + "AND (p.active = true or etm.active IN (1, 2)) \n"
            + "GROUP BY p.id, p.poId, p.projectId, p.poNo, p.poStartDate, p.poEndDate, p.active")
    List<PoDetailsDto> getAllPoDetailsDtoByProjectId(Integer projectId);

}
