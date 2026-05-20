package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "timesheet_document_details_new")
public class TimesheetDocumentDetailsNew {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id")
    private Long docId;

    @Column(name = "timesheet_id")
    private Long timesheetId;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "doc_name")
    private String docName;

    @Column(name = "mime_type_id")
    private Integer mimeTypeId;

    @Column(name = "client_approval_status_id")
    private Integer clientApprovalStatusId;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "updated_by")
    private Long updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

    @Column(name = "project_id", nullable = false)
    private Integer projectId;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "final_flag")
    private Boolean finalFlag;

    @Column(name = "bulk_approved_doc_id")
    private Long bulkApprovedDocId;
}
