package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import lombok.Data;

@Entity
@Audited
@Table(name = "final_document_new")
@Data
public class FinalDocumentNew {

	@Id
    @Column(name = "final_doc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long finalDocId;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "mime_type_id")
    private Integer mimeTypeId;

    @Column(name = "doc_name")
    private String docName;

    @Column(name = "project_id", nullable = false)
    private Integer projectId;

    @Column(name = "updated_by")
    private Long updatedBy;

    
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;
    
   
    @Column(name = "prev_doc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prevDocId;

     @PreUpdate
    public void onUpdate() {
        this.updatedOn = LocalDateTime.now();
    }
}
