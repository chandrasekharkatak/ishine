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
			+ "cd.docId)from EmployeeCertificates ec inner join CertificateDocumentMapping cd\n"
			+ "on ec.docId= cd.docId where ec.empId = :empId and cd.dActive = true order by ec.createdOn desc")
	public List<CertificateDTO> getAllCertificatesByEmpId(Long empId);
}
