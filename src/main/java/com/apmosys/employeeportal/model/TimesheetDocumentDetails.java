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

import org.hibernate.envers.Audited;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Audited
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
	
	@Column(name = "doc_mime_type")
	private String docMimeType;
	
//	doc_path	String (UK)
	@Column(name = "timesheet_id")
	private Long timesheetId;
	
	@Column(name = "emp_id")
	private Long empId;
	
	@Column(name = "created_on")
	private LocalDateTime createdOn;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "created_by")
	private Long createdBy;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "updated_on")
	private LocalDateTime updatedOn;
	
	@Column(name = "updated_by")
	private Long updatedBy;
	
	@Column(name = "active")	
	private Boolean active;
	
	@Column(name = "client_approval_status")
	private String clientApprovalStatus;
	
	@Column(name = "rm_approval_status")
	private String rmApprovalStatus;
	
	@Column(name = "final_flag")
	private Boolean finalFlag;
}
