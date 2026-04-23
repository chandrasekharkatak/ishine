package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.CertificateDocumentMapping;

@Repository
public interface CertificateDocumentMapRepository extends JpaRepository<CertificateDocumentMapping,Long>{

}
