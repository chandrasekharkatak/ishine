//package com.apmosys.employeeportal.repository;
//
//import java.util.List;
//import java.util.Optional;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import com.apmosys.employeeportal.model.EmployeeClientSideIdMapId;
//import com.apmosys.employeeportal.model.EmployeeClientSideIdMapping;
//import com.apmosys.employeeportal.model.EmployeeClientSideIdMappingNew;
//
//public interface EmployeeClientSideIdMappingNewRepository extends JpaRepository<EmployeeClientSideIdMappingNew, EmployeeClientSideIdMapId>{

	//*** This repository is not used 
//	@Query("SELECT e.clientSideId FROM EmployeeClientSideIdMappingNew e WHERE e.projectId = :projectId AND e.isActive = true")
//	public Optional<String> findClientSideIdByProjectId(@Param("projectId") Long projectId);
//	
//	public Optional<EmployeeClientSideIdMappingNew> findByProjectIdAndIsActiveAndEmpId(Long projectId, Boolean active, Long empId);
//	
//	@Query("SELECT e.clientSideId FROM EmployeeClientSideIdMappingNew e WHERE e.projectId = :projectId AND e.empId = :empId")
//	public Optional<String> getClientSideIdByProjectIdAndEmpId(Long projectId, Long empId);
//	
//	List<EmployeeClientSideIdMapping> findByEmpIdInAndProjectIdInAndIsActive(List<Long> empIds, List<Long> projectIds, Boolean active);
//	
//}
