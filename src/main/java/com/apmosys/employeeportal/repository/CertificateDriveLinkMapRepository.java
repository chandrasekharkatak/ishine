package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.CertificateDriveLinkMapping;


@Repository
public interface CertificateDriveLinkMapRepository extends JpaRepository<CertificateDriveLinkMapping,Long> {

}
