package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.Newsletter;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Long >{

	@Query(nativeQuery = true)
	public List<Object[]> getAllNewsletters();
	
	@Query(nativeQuery = true)
	public List<Object[]> findByDocumentId(Long documentId);

	public long countByReadEnabled(String readEnabled);

	public List<Newsletter> findByReadEnabled(String readEnabled);

	@Query(nativeQuery = true , value = "select d.type_id ,d.display_name, d.created_on,d.created_by, e.name, d.document_id, d.file_name, td.type_name from documents d "
			+ "inner join employee e on e.emp_id=d.created_by "
			+ "inner join type_document td on td.type_id = d.type_id "
			+ " where d.type_id = :typeId")
	public List<Object[]> findByTypeId(Long typeId);

	@Query(nativeQuery = true , value = "select * from documents where document_id = :docId")
	public Newsletter findByDocId(Long docId);
}
