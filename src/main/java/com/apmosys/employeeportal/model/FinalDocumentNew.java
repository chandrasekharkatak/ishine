package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "final_document_new")
public class FinalDocumentNew {

	@Id
    @Column(name = "final_doc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long finalDocId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "mime_type_id")
    private Integer mimeTypeId;

    @Column(name = "doc_name")
    private String docName;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;
}
