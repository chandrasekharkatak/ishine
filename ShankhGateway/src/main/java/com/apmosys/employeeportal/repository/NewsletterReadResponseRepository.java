package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.NewsletterReadResponse;

@Repository
public interface NewsletterReadResponseRepository extends JpaRepository<NewsletterReadResponse, Long>{
	
	@Query(nativeQuery = true)
	List<Object[]> getReadNewslettersByEmpId(Long empId);

	long countByEmpId(Long empId);

	NewsletterReadResponse findByEmpIdAndDocumentId(Long empId, Long documentId);

	List<NewsletterReadResponse> findAllByEmpIdAndDocumentId(Long empId, Long documentId);

	List<NewsletterReadResponse> findByDocumentId(Long documentId);
	
	
	 boolean existsByEmpIdAndDocumentId(Long empId, Long documentId);
}
