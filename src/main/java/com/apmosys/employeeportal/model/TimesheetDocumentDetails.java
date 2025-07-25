package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "timesheet_document_details")
public class TimesheetDocumentDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "doc_id")
	private Long docId;
	
	@Column(name = "doc_name")	
	private String docName;
	
	@Lob
	@Column(name = "doc_data")
	private byte[] docData;
	
//	doc_path	String (UK)
	@Column(name = "timesheet_id")
	private Long timesheetId;
	
	@Column(name = "emp_id")
	private Long empId;
	
	@Column(name = "created_on")
	private LocalDateTime createdOn;
	
	@Column(name = "created_by")
	private Long createdBy;
	
	@Column(name = "updated_on")
	private LocalDateTime updatedOn;
	
	@Column(name = "updated_by")
	private Long updated_by;
	
	@Column(name = "active")	
	private Boolean active;
	
	@Column(name = "client_approval_status")
	private String clientApprovalStatus;
	
	@Column(name = "rm_approval_status")
	private String rmApprovalStatus;
	
	@Column(name = "final_flag")
	private Boolean finalFlag;
}
