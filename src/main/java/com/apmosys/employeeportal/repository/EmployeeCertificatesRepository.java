package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.dto.CertificateDTO;
import com.apmosys.employeeportal.model.EmployeeCertificates;

@Repository
public interface EmployeeCertificatesRepository extends JpaRepository<EmployeeCertificates,Long> {

	
	@Query(value="SELECT new com.apmosys.employeeportal.dto.CertificateDTO(ec.employeeCertificateId,ec.certificateName,\n"
			+ "ec.specialization,ec.issuingAuthority,ec.validFrom,ec.expiresOn,ec.certificateStatus,\n"
			+ "cd.docId,dl.driveLink)from EmployeeCertificates ec left join CertificateDocumentMapping cd\n"
			+ "on ec.docId= cd.docId and cd.dActive = true left join CertificateDriveLinkMapping dl on ec.driveId = dl.driveId where ec.empId = :empId and ec.cActive = true  order by ec.createdOn desc ")
	public List<CertificateDTO> getAllCertificatesByEmpId(Long empId);
	
	@Query(value ="Select DISTINCT(c) from EmployeeCertificates c where (c.deptId in :deptIds or c.empId in :empIds) and c.cActive = true ")
	public List<EmployeeCertificates> findCertficatesByDeptIdsAndOfReportees(List<Long> deptIds,List<Long> empIds);
	
	@Query(value ="Select DISTINCT(c) from EmployeeCertificates c where c.cActive = true ")
	public List<EmployeeCertificates> findAllActiveCertficates();
}
