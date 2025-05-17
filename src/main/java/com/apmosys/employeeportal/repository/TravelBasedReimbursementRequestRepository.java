package com.apmosys.employeeportal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.TravelBasedReimbursementRequest;


@Repository
public interface TravelBasedReimbursementRequestRepository extends JpaRepository<TravelBasedReimbursementRequest, Integer> {

	
	@Query(value="select * from travel_based_reimbursement_request where invoice_no= :invoiceNo",nativeQuery=true)
	TravelBasedReimbursementRequest findInvoiceDetails(String invoiceNo);
  
	
}
