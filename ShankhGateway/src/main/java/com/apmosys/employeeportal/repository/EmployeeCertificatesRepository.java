package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.CertificateDTO;
import com.apmosys.employeeportal.model.EmployeeCertificates;

@Repository
public interface EmployeeCertificatesRepository extends JpaRepository<EmployeeCertificates,Long> {

	
	@Query(value="SELECT new com.apmosys.employeeportal.dto.CertificateDTO(ec.employeeCertificateId,ec.certificateName,\n"
			+ "ec.specialization,ec.issuingAuthority,ec.validFrom,ec.expiresOn,ec.certificateStatus,\n"
			+ "cd.docId,dl.driveLink,p.proficiencyName)from EmployeeCertificates ec left join CertificateDocumentMapping cd\n"
			+ "on ec.docId= cd.docId and cd.dActive = true left join CertificateDriveLinkMapping dl on ec.driveId = dl.driveId inner join Proficiency p on ec.proficiencyId = p.proficiencyId where ec.empId = :empId and ec.cActive = true  order by ec.createdOn desc ")
	public List<CertificateDTO> getAllCertificatesByEmpId(Long empId);
	
	@Query(value ="Select DISTINCT(c) from EmployeeCertificates c where (c.deptId in :deptIds or c.empId in :empIds) and c.cActive = true ")
	public List<EmployeeCertificates> findCertficatesByDeptIdsAndOfReportees(List<Long> deptIds,List<Long> empIds);
	
	@Query(value ="Select DISTINCT(c) from EmployeeCertificates c where c.cActive = true ")
	public List<EmployeeCertificates> findAllActiveCertficates();
	
	
	
	@Query("SELECT ec FROM EmployeeCertificates ec " +
            "WHERE ec.empId = :empId " +
            "AND LOWER(ec.certificateName) = LOWER(:certificateName) " +
            "AND LOWER(ec.issuingAuthority) = LOWER(:issuingAuthority) and ec.cActive = true")
public EmployeeCertificates findByCertificateNameAndAuthorityNative(
     @Param("empId") Long empId,
     @Param("certificateName") String certificateName,
     @Param("issuingAuthority") String issuingAuthority);
	
	
	
	
	
	@Query(value="select ec.emp_id,ec.employee_certificate_id,ec.certificate_name,ec.specialization,\n"
			+ "ec.dept_id,ec.proficiency_id,ec.issuing_authority,ec.valid_from,ec.expires_on,\n"
			+ "cs.certificateskill_id,cs.skill_id,cs.additional_skill,cs.proficiency_id as skillprof,dl.drive_link,dl.drive_id\n"
			+ "from employee_certificates ec left join certificate_drive_link_mapping dl\n"
			+ "on ec.drive_id = dl.drive_id and dl.dr_active = true\n"
			+ "left join certificate_skill_mapping cs \n"
			+ "on cs.employee_certificate_id = ec.employee_certificate_id and cs.sc_active= true where ec.emp_id = :empId\n"
			+ "AND LOWER(ec.certificate_name) = LOWER(:certificateName)\n"
			+ "AND LOWER(ec.issuing_authority) = LOWER(:issuingAuthority) and ec.c_active = true",nativeQuery = true)
	List<Object[]> findExistingCert(Long empId,String certificateName,String issuingAuthority);
	
	
	
	
	@Query(value="SELECT new com.apmosys.employeeportal.dto.CertificateDTO(e.empId,ec.employeeCertificateId,ec.certificateName)\n"
			+ "from Employee e left join EmployeeCertificates ec on ec.empId = e.empId\n"
			+ "where e.empId IN :empIds")
	List<CertificateDTO> getEmployeeCertficatesByEmpIds(List<Long> empIds);
	
	
	
	
}
