package com.apmosys.employeeportal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apmosys.employeeportal.model.TravelBasedReimbursementRequest;


@Repository
public interface TravelBasedReimbursementRequestRepository extends JpaRepository<TravelBasedReimbursementRequest, Integer> {

	
	@Query(value="select * from travel_based_reimbursement_request where invoice_no= :invoiceNo",nativeQuery=true)
	TravelBasedReimbursementRequest findInvoiceDetails(String invoiceNo);
	
	@Query(value="select tb.travel_Id,tb.invoice_No ,invoice_Date,tb.amount,tb.doc_Id,td.name,td.email,td.request_Type,td.travel_Mode,td.travel_Class,td.from_Location,td.to_Location,td.from_Date,td.to_Date,td.status,"
			+ "td.level1_approver_Remarks,td.hod_Name,td.level2_approver_name,td.level2_approver_status,tb.reimbursement_status,tb.final_reimbusement_status"
			+ " from travel_based_reimbursement_request tb inner join travel_desk td where td.request_id = tb.travel_id",nativeQuery=true)
	List<Object[]> findAllByTravel();
	
	@Query(value="select tb.travel_Id,tb.invoice_No ,invoice_Date,tb.amount,tb.doc_Id,td.name,td.email,td.request_Type,td.travel_Mode,td.travel_Class,td.from_Location,td.to_Location,td.from_Date,td.to_Date,td.status,"
			+ "td.level1_approver_Remarks,td.hod_Name,td.level2_approver_name,td.level2_approver_status,tb.reimbursement_status,tb.serial_no, tb.final_reimbusement_status"
			+ " from travel_based_reimbursement_request tb inner join travel_desk td where td.request_id = tb.travel_id and tb.uploaded_by = :empId",nativeQuery=true)
	List<Object[]> findAllByTravelByEmpId(String empId);
  
	
	@Query(value="select * from travel_based_reimbursement_request tb  where tb.serial_no= :serialNo",nativeQuery=true)
	TravelBasedReimbursementRequest findInvoiceBySerialNo(Integer serialNo);

	
	@Query(value="select * from travel_based_reimbursement_request tb  where tb.travel_id= :travelId",nativeQuery=true)
	List<TravelBasedReimbursementRequest> findAllByTravelId(Integer travelId);
	
	
    @Query(value ="SELECT \n"
    		+ "    CASE \n"
    		+ "        WHEN EXISTS (\n"
    		+ "            SELECT 1 FROM travel_based_reimbursement_request \n"
    		+ "            WHERE invoice_no = :invoiceNo\n"
    		+ "            AND serial_no != :serialNo\n"
    		+ "        ) THEN 'false'\n"
    		+ "        ELSE 'true'\n"
    		+ "    END AS can_update",nativeQuery=true)
    boolean canUpdateInvoiceSerial(@Param("invoiceNo") String invoiceNo, @Param("serialNo") Integer serialNo);
	
	
}
