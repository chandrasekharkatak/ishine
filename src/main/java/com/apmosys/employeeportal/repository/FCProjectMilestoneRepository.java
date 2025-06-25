package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apmosys.employeeportal.dto.FCProjectMilestoneDTO;
import com.apmosys.employeeportal.model.FCProjectMilestone;

public interface FCProjectMilestoneRepository extends JpaRepository<FCProjectMilestone, Long> {

    @Query("SELECT new com.apmosys.employeeportal.dto.FCProjectMilestoneDTO(pm.id,pm.poId ,pm.projectId , pm.name, pm.description, pm.startDate, pm.endDate, pm.status, pm.remarks,pm.lineItemId,li.name,li.status) \n"
            +" FROM FCProjectMilestone pm \n"
            +" INNER JOIN FCLineItem li ON pm.lineItemId = li.id \n"
            +" WHERE pm.projectId =:projectId")
    List<FCProjectMilestoneDTO> findByProjectId(Long projectId);

}
